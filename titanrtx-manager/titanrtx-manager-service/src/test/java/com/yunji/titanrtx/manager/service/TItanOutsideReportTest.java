package com.yunji.titanrtx.manager.service;

import com.yunji.titanrtx.common.domain.statistics.OutsideStatistics;
import com.yunji.titanrtx.common.domain.statistics.Statistics;
import com.yunji.titanrtx.common.domain.statistics.StatisticsDetail;
import com.yunji.titanrtx.manager.service.report.support.MetricsCollector;
import com.yunji.titanrtx.manager.service.report.AutoReportHandler;
import org.apache.commons.collections.map.HashedMap;

import java.util.HashMap;
import java.util.Map;

public class TItanOutsideReportTest {

    public static void main(String[] args) {
        AutoReportHandler reportHandler = new AutoReportHandler(new MetricsCollector(), null);

        OutsideStatistics outsideStatistics = new OutsideStatistics();
        Statistics statistics = new Statistics();
        Map<Integer, String> urlIdMap = new HashMap<>();
        Map<Integer, StatisticsDetail> detailMap = new HashedMap();

        urlIdMap.put(1, "/yunjiapp/app/getshopitemsByPage.json");
        urlIdMap.put(2, "/yunjiapp/app/getNumShopItemQrcodeList.json");
        urlIdMap.put(3, "/yunjiapp/app/weixin/checkAndgetWeixinNicknameByCode.json");

        detailMap.put(1, new StatisticsDetail());
        detailMap.put(2, new StatisticsDetail());
        detailMap.put(3, new StatisticsDetail());

        statistics.setUrlIdMap(urlIdMap);
        statistics.setDetailMap(detailMap);

        outsideStatistics.setStatistics(statistics);
        outsideStatistics.setStartTime(System.currentTimeMillis() - 300000);
        outsideStatistics.setEndTime(System.currentTimeMillis());


        reportHandler.reportOutside(outsideStatistics);
    }
}
