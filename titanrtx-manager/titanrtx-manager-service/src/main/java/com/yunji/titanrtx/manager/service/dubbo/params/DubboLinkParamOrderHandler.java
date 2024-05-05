package com.yunji.titanrtx.manager.service.dubbo.params;

import com.yunji.titanrtx.common.alarm.AlarmService;
import com.yunji.titanrtx.common.alarm.MessageSender;
import com.yunji.titanrtx.common.domain.task.Pair;
import com.yunji.titanrtx.common.enums.ParamStatus;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.common.u.LocalDateU;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import com.yunji.titanrtx.manager.service.dubbo.DubboServiceService;
import com.yunji.titanrtx.manager.service.dubbo.ServiceParamsService;
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

import static com.yunji.titanrtx.common.u.LocalDateU.MINUTE_TIME_QUARTS;
import static com.yunji.titanrtx.manager.service.report.support.ReportUtils.GSON;

/**
 * 处理 Dubbo 链路对应的参数的顺序问题.
 *
 * @author leihz
 * @since 2020-07-02 11:25 上午
 */
@Component
@Slf4j
public class DubboLinkParamOrderHandler {

    @Autowired
    @Qualifier("DATASOURCE_TITAN")
    private DataSource dataSource;

    /**
     * dubbo 链路管理
     */
    @Autowired
    private DubboServiceService linkService;
    /**
     * Dubbo 参数 service.
     */
    @Autowired
    private ServiceParamsService paramsService;

    @Autowired
    private AlarmService alarmService;

    /**
     * 5min 检查一次.处理无序链路并修改其状态.
     */
    @Scheduled(cron = "${dubbo.param.order.cron:10 */5 * * * ?}")
    public void scheduledOrderParam() {
        long st = System.currentTimeMillis();
        List<ServiceEntity> linkStatusList = linkService.selectAllOfParamStatus();
        int count = 0;
        for (ServiceEntity entity : linkStatusList) {
            //乱序的
            if (ParamStatus.OUT_OF_ORDER.getId() == entity.getParamStatus()) {
                count++;
                handlerOrderParams(entity.getId());
            }
        }
        String minuteTime = LocalDateU.getMinuteTime(System.currentTimeMillis());
        if (count > 0 || MINUTE_TIME_QUARTS.contains(minuteTime)) {
            log.info("[{}]本轮循环检测参数顺序结束,一共检测链路({})条,排序链路({})条,耗时:{}ms.",
                    minuteTime, linkStatusList.size(), count, (System.currentTimeMillis() - st));
        }
    }

    /**
     * 检查链路参数是否是顺序的.
     */
    @Scheduled(cron = "${link.param.check.cron:0 */1 * * * ?}")
    public void checkLinkParamOrder() {
        long st = System.currentTimeMillis();
        List<ServiceEntity> linkStatusList = linkService.selectAllOfParamStatus();
        int count = 0;
        List<Integer> nullIds = new ArrayList<>();
        for (ServiceEntity linkStatus : linkStatusList) {
            int id = linkStatus.getId();
            //只是查询了参数的id.
            int totalRecords = paramsService.findTotalRecordsByServiceId(id);
            //检查
            boolean check = paramsService.checkNullParamsOrder(id, totalRecords, nullIds);
            if (!check) {
                count++;
                linkService.updateDubboServiceOrderStatus(id, ParamStatus.OUT_OF_ORDER.getId());
            }
        }
        String minuteTime = LocalDateU.getMinuteTime(System.currentTimeMillis());
        if (count > 0 || MINUTE_TIME_QUARTS.contains(minuteTime)) {
            log.info("[{}]链路参数顺序验证结束,一共检测链路({})条,错误链路({})条.\n空参数链路:{},\n耗时:{}ms.",
                    minuteTime, linkStatusList.size(), count, nullIds, (System.currentTimeMillis() - st));
        }
    }


    public void handlerOrderParams(int dubboServiceId) {
        Pair<Boolean, String> resultPair = orderParams(dubboServiceId);
        if (resultPair.getKey()) {
            //更新此链路为顺序的链路.
            linkService.updateDubboServiceOrderStatus(dubboServiceId, ParamStatus.ORDER.getId());
        } else {
            String msg = "环境 [" + CommonU.getConfigEnv().toUpperCase() + "] Titan链路参数排序异常告警\n" +
                    "Dubbo 链路(" + dubboServiceId + ") 在参数排序时异常.\n"
                    + "异常原因: " + resultPair.getValue() + ".\n";

            alarmService.send(MessageSender.Type.MAINTAINER, msg);
        }
    }

    /**
     * 参数是不是顺序的.
     */
    public Pair<Boolean, String> orderParams(int dubboServiceId) {
        try {
            //锁.
            long startTime = System.currentTimeMillis();
            int totalRecords = paramsService.findTotalRecordsByServiceId(dubboServiceId);
            if (totalRecords == 0) {
                log.info("Dubbo链路({}) 的参数共计: {}条,默认不需要排序.", dubboServiceId, totalRecords);
                return new Pair<>(true, "");
            }

            log.info("开始排序Dubbo链路 ({}) 的参数共计: {}条.", dubboServiceId, totalRecords);
            try (
                    Connection conn = dataSource.getConnection();
                    Statement stmt = conn.createStatement();
            ) {
                stmt.execute("SET @count = 0");
                stmt.executeUpdate(String.format("UPDATE service_params SET orders = @count:=@count+1 WHERE serviceId =%d ORDER BY id ASC", dubboServiceId));
            }
            //检查
            boolean check = paramsService.checkParamIsOrder(dubboServiceId, totalRecords);
            log.info("排序DUBBO链路 ({}) 的所有参数完毕,totalRecords:{},check:{},耗时:{}ms",
                    dubboServiceId, totalRecords, check, (System.currentTimeMillis() - startTime));

            return new Pair<>(check, "");
        } catch (Exception e) {
            log.error("排序linkId ({})的参数失败" + e.getMessage(), dubboServiceId);
            return new Pair<>(false, e.getMessage());
        }
    }
}
