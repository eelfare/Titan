package com.yunji.titanrtx.cia.agent.log.disruptor.handler;

import com.lmax.disruptor.WorkHandler;
import com.yunji.titanrtx.cia.agent.log.Material;
import com.yunji.titanrtx.cia.agent.log.disruptor.event.MateLog;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MateLogHandler implements WorkHandler<MateLog> {

    private Material<MateLog> material;

    private List<MateLog> mateLogs;

    private static final int size = 1024;

    public MateLogHandler(Material<MateLog> mateLogMaterial) {
        this.material = mateLogMaterial;
        this.mateLogs = new ArrayList<>(size);
    }

    @Override
    public void onEvent(MateLog event) {
        log.debug("MateLogHandler:{}......................................", event);
        try {
            material.push(event);
        } catch (Exception e) {
            log.error(e.getMessage() + ", time: " + (event != null ? event.getTime() : "-1"));
        }
/*        try {
            mateLogs.add(event);
            if (mateLogs.size() == size){
                mateLogs.stream()
                        .collect(Collectors.groupingBy(MateLog::getDomain, Collectors.groupingBy(MateLog::getPath, Collectors.groupingBy(MateLog::getTime))))
                        .forEach((domain, stringMapMap) -> stringMapMap.forEach((path, longListMap) -> longListMap.forEach((time, mateLogs1) ->
                                {
                                    MateLog mateLog = new MateLog();
                                    mateLog.setDomain(domain);
                                    mateLog.setPath(path);
                                    mateLog.setTime(time);
                                    mateLog.setElapsed(new BigDecimal(mateLogs1.stream().collect(Collectors.averagingDouble(MateLog::getElapsed))).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
                                    mateLog.setRequestTimes(mateLogs1.stream().mapToLong(MateLog::getRequestTimes).sum());
                                    mateLog.setSuccessTimes(mateLogs1.stream().mapToLong(MateLog::getSuccessTimes).sum());

                                    material.push(mateLog);

                                }
                        )));
                mateLogs = new ArrayList<>(size);;
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/
    }

}
