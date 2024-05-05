package com.yunji.titanrtx.cia.agent.log.disruptor.producer;

import com.yunji.titanrtx.cia.agent.log.Material;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.MateLog;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrxt.plugin.influxdb.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.dto.Point;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MateLogStoreProducer implements Material<MateLog> {

    private StoreService storeService;

    private String rpName;

    public MateLogStoreProducer(StoreService storeService,String rpName) {
        this.storeService = storeService;
        this.rpName = rpName;
    }

    @Override
    public void push(MateLog mateLog) {
        log.debug("MateLogStoreProducer:{}......................................",mateLog);
        Point point = Point.measurement(GlobalConstants.TOP_LINK_MATE_MEASUREMENT_NAME)
                .time(mateLog.getTime(), TimeUnit.NANOSECONDS)
                .addField("requestTimes", mateLog.getRequestTimes())
                .addField("successTimes", mateLog.getSuccessTimes())
                .addField("elapsed", mateLog.getElapsed())
                .tag("domain", mateLog.getDomain())
                .tag("path", mateLog.getPath())
                .build();

        storeService.write(GlobalConstants.TOP_LINK_MATE_DB_NAME,rpName,point);
    }


}
