package com.yunji.titanrtx.cia.agent.log.disruptor.producer;

import com.yunji.titanrtx.cia.agent.log.Material;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.ParamLog;
import com.yunji.titanrxt.plugin.influxdb.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.dto.Point;

import java.util.concurrent.TimeUnit;
@Slf4j
public class ParamLogStoreProducer implements Material<ParamLog> {

    private StoreService storeService;

    private String rpName;

    public ParamLogStoreProducer(StoreService storeService,String rpName) {
        this.storeService = storeService;
        this.rpName = rpName;
    }

    @Override
    public void push(ParamLog paramLog) {
        log.debug("ParamLogStoreProducer:{}......................................",paramLog);
        Point param = Point.measurement(paramLog.getPath())
                .time(paramLog.getTime(), TimeUnit.NANOSECONDS)
                .addField("param", paramLog.getParam())
                .addField("respCode", paramLog.getRespCode())
                .addField("elapsed", paramLog.getElapsed())
                .build();
        storeService.write(paramLog.getDomain(),rpName,param);
    }
}
