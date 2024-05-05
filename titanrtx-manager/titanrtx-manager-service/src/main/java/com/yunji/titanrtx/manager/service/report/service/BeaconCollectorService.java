package com.yunji.titanrtx.manager.service.report.service;

import com.yunji.titanrtx.common.domain.statistics.OutsideStatistics;
import com.yunji.titanrtx.common.u.LocalDateU;
import com.yunji.titanrtx.common.u.LogU;
import com.yunji.titanrtx.manager.dao.entity.data.BeaconAlertEntity;
import com.yunji.titanrtx.manager.dao.mapper.report.BeaconCollectorMapper;
import com.yunji.titanrtx.manager.service.common.datasource.DataSourceManager;
import com.yunji.titanrtx.manager.service.common.datasource.DataSources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


import static com.yunji.titanrtx.manager.service.report.support.ReportLog.log;

/**
 * 收集 beacon 告警历史
 *
 * @author leihz
 * @since 2020-05-11 2:51 下午
 */
@Component
public class BeaconCollectorService {
    //Dubbo失败和耗时告警,RocketMQ积压告警,CGI耗时告警
    private static final List<String> ALERT_ID_PREFIX_LIST = Arrays.asList("Dubbo_monitor%", "RocketMQ_diff%", "CGI_monitor%");

    @Resource
    private BeaconCollectorMapper beaconMapper;

    //    @DataSource("DATASOURCE_BEACON")
    public List<BeaconAlertEntity> collectBeaconAlerts(OutsideStatistics statistics) {
        String startDatetime = LocalDateU.getNormalDate(statistics.getStartTime());
        String endDatetime = LocalDateU.getNormalDate(statistics.getEndTime());

        List<CompletableFuture<List<BeaconAlertEntity>>> alertResultFutures = new ArrayList<>();

        for (String alertIdPrefix : ALERT_ID_PREFIX_LIST) {
            LogU.info("Query beacon alert id prefix -> [{}]", alertIdPrefix);

            CompletableFuture<List<BeaconAlertEntity>> alertEntities = CompletableFuture.supplyAsync(() -> {
                try {
                    DataSourceManager.set(DataSources.DATASOURCE_BEACON);
                    return beaconMapper.findAlertsBetweenTime(startDatetime, endDatetime, alertIdPrefix);
                } finally {
                    DataSourceManager.reset();
                }
            });
            alertResultFutures.add(alertEntities);
        }

        CompletableFuture<List<List<BeaconAlertEntity>>> listCompletableFuture =
                CompletableFuture
                        .allOf(alertResultFutures.toArray(new CompletableFuture[0]))
                        .thenApply(value -> alertResultFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        List<List<BeaconAlertEntity>> alertResultList = new ArrayList<>();
        try {
            alertResultList = listCompletableFuture.get();
        } catch (Exception e) {
            log.error("Got BeaconAlertEntity error,cause: " + e.getMessage(), e);
        }
        List<BeaconAlertEntity> alertEntities = new ArrayList<>();

        for (List<BeaconAlertEntity> beaconAlertEntities : alertResultList) {
            alertEntities.addAll(beaconAlertEntities);
        }

        log.info("Beacon alert 在 [{}] - [{}] 区间查询到告警信息 [{}] 条.", startDatetime, endDatetime, alertEntities.size());
        return alertEntities;
    }

}
