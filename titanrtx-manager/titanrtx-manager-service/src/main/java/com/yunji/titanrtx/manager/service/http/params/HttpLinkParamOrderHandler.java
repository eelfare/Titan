package com.yunji.titanrtx.manager.service.http.params;

import com.yunji.titanrtx.common.alarm.AlarmService;
import com.yunji.titanrtx.common.alarm.FilterAlarmService;
import com.yunji.titanrtx.common.alarm.MessageSender;
import com.yunji.titanrtx.common.domain.task.Pair;
import com.yunji.titanrtx.common.enums.ParamStatus;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.common.u.DateU;
import com.yunji.titanrtx.common.u.LocalDateU;
import com.yunji.titanrtx.manager.dao.entity.http.LinkStatusEntity;
import com.yunji.titanrtx.manager.dao.entity.http.SceneStressEntity;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import com.yunji.titanrtx.manager.service.http.LinkParamsService;
import com.yunji.titanrtx.manager.service.http.LinkService;
import com.yunji.titanrtx.manager.service.report.alert.AlertReportGenerator.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.yunji.titanrtx.common.u.LocalDateU.MINUTE_TIME_QUARTS;
import static com.yunji.titanrtx.manager.service.report.support.ReportUtils.GSON;

/**
 * 处理 Http 链路对应的参数的顺序问题.
 *
 * @author leihz
 * @since 2020-05-19 5:25 下午
 */
@Component
@Slf4j
public class HttpLinkParamOrderHandler {

    @Autowired
    @Qualifier("DATASOURCE_TITAN")
    private DataSource dataSource;

    @Autowired
    private LinkService linkService;

    @Autowired
    private LinkParamsService paramsService;

    @Autowired
    private HttpSceneService sceneService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private FilterAlarmService filterAlarmService;

    /**
     * 正在排序中的链路.
     */
    private List<Integer> processingLinkList = new CopyOnWriteArrayList<>();

    /**
     * 5min 检查一次.处理无序链路并修改其状态.
     */
    @Scheduled(cron = "${link.param.order.cron:30 */5 * * * ?}")
    public void scheduledOrderParam() {
        if (checkCurrentStressing("[scheduledOrderParam 处理无序链路并修改其状态]")) {

            long st = System.currentTimeMillis();
            List<LinkStatusEntity> linkStatusList = linkService.selectAllOfId();
            int count = 0;
            for (LinkStatusEntity linkStatus : linkStatusList) {
                //乱序的
                if (ParamStatus.OUT_OF_ORDER.getId() == linkStatus.getParamStatus()) {
                    count++;
                    handlerOrderParams(linkStatus.getId());
                }
            }
            String minuteTime = LocalDateU.getMinuteTime(System.currentTimeMillis());
            if (count > 0 || MINUTE_TIME_QUARTS.contains(minuteTime)) {
                log.info("[{}]本轮循环检测参数顺序结束,一共检测链路({})条,排序链路({})条,耗时:{}ms.",
                        minuteTime, linkStatusList.size(), count, (System.currentTimeMillis() - st));
            }
        }
    }

    /**
     * 检查链路参数是否是顺序的.
     */
    @Scheduled(cron = "${link.param.check.cron:0 */1 * * * ?}")
    public void checkLinkParamOrder() {
        if (checkCurrentStressing("[checkLinkParamOrder 检查链路参数是否是顺序]")) {
            long st = System.currentTimeMillis();
            List<LinkStatusEntity> linkStatusList = linkService.selectAllOfId();
            int count = 0;
            List<Integer> nullIds = new ArrayList<>();
            for (LinkStatusEntity linkStatus : linkStatusList) {
                int id = linkStatus.getId();
                //只是查询了参数的id.
                int totalRecords = paramsService.findTotalRecordsByLinkId(id);
                //检查
                boolean check = paramsService.checkNullParamsOrder(id, totalRecords, nullIds);
                if (!check) {
                    count++;
                    linkService.updateParamOrderStatus(id, ParamStatus.OUT_OF_ORDER.getId());
                }
            }
            String minuteTime = LocalDateU.getMinuteTime(System.currentTimeMillis());
            if (count > 0 || MINUTE_TIME_QUARTS.contains(minuteTime)) {
                log.info("[{}]链路参数顺序验证结束,一共检测链路({})条,错误链路({})条.\n空参数链路:{},\n耗时:{}ms.",
                        minuteTime, linkStatusList.size(), count, nullIds, (System.currentTimeMillis() - st));
            }
        }
    }


    public void handlerOrderParams(Integer linkId) {
        Pair<Boolean, String> resultPair = orderParams(linkId);
        if (resultPair.getKey()) {
            //更新此链路为顺序的链路.
            linkService.updateParamOrderStatus(linkId, ParamStatus.ORDER.getId());
        } else {
            String msg = "环境 [" + CommonU.getConfigEnv().toUpperCase() + "] Titan链路参数排序异常告警\n" +
                    "链路(" + linkId + ")在参数排序时异常.\n"
                    + "异常原因: " + resultPair.getValue() + ".\n";

            alarmService.send(MessageSender.Type.MAINTAINER, msg);
            filterAlarmService.filterAlarm("OrderParams-" + linkId, 1800_000, MessageSender.Type.FLOW_CREATOR, msg);
        }
    }

    public Pair<Boolean, String> orderParams(Integer linkId) {
//        int partition = ParamPartitioner.partition(linkId);
        try {
            //锁.
            long startTime = System.currentTimeMillis();
            int totalRecords = paramsService.findTotalRecordsByLinkId(linkId);
            if (totalRecords == 0) {
                log.info("链路({}) 的参数共计: {}条,默认不需要排序.", linkId, totalRecords);
                return new Pair<>(true, "");
            }

            log.info("开始排序linkId ({}) 的参数共计: {}条.", linkId, totalRecords);
            try (
                    Connection conn = dataSource.getConnection();
                    Statement stmt = conn.createStatement();
            ) {
                stmt.execute("SET @count = 0");
                stmt.executeUpdate(String.format("UPDATE link_params SET orders = @count:=@count+1 WHERE linkId =%d ORDER BY id ASC", linkId));
            }
            //检查
            boolean check = paramsService.checkParamIsOrder(linkId, totalRecords);
            log.info("排序linkId ({}) 的所有参数完毕,totalRecords:{},check:{},耗时:{}ms",
                    linkId, totalRecords, check, (System.currentTimeMillis() - startTime));

            return new Pair<>(check, "");
        } catch (Exception e) {
            log.error("排序linkId ({})的参数失败 " + e.getMessage(), linkId);
            return new Pair<>(false, e.getMessage());
        }
    }

    /**
     * 检查当前是否有场景正在压测中.
     */
    private boolean checkCurrentStressing(String msg) {
        //非idc环境,不暂停排序.
        if (!CommonU.isIDC()) {
            return true;
        }

        List<Integer> stressing = new ArrayList<>();
        List<SceneStressEntity> sceneStressEntities = sceneService.selectAllSceneStresses();

        for (SceneStressEntity sceneStress : sceneStressEntities) {
            if (sceneStress.getStatus() == 1) {
                stressing.add(sceneStress.getId());
            }
        }

        if (!stressing.isEmpty()) {
            log.warn("目前正在压测的场景:{} 暂停 {} 参数排序 task.", stressing, msg);
            return false;
        }
        return true;

    }
}
