package com.yunji.titanrtx.agent.task;

import com.google.common.base.Splitter;
import com.yunji.titanrtx.bash.support.PingShell;
import com.yunji.titanrtx.common.enums.TaskType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class InternetRepairThread extends Thread {

    private static final int RUN_INTERVAL = 1000;

    private volatile AtomicBoolean runFlag;

    private Map<Integer, Double> average;

    private Map<Integer, String> hostMap;

    private int destroyTimes;

    private TaskType taskType;


    public InternetRepairThread(AtomicBoolean runFlag, Map<Integer, String> hostMap,
                                Map<Integer, Double> average, TaskType taskType) {
        setName("internetRepairThread");
        this.runFlag = runFlag;
        this.hostMap = hostMap;
        this.average = average;
        this.taskType = taskType;
    }


    @Override
    public void run() {
        while (runFlag.get() && destroyTimes < 10) {
            for (Map.Entry<Integer, String> entry : hostMap.entrySet()) {
                Integer id = entry.getKey();
                String host = entry.getValue();
                try {
                    switch (taskType) {
                        case HTTP:
                            repairHttpRtt(id, host);
                            break;
                        case DUBBO:
                            repairDubboRtt(id, host);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                destroyTimes++;
                Thread.sleep(RUN_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void repairHttpRtt(Integer id, String host) throws IOException {
        double expend = new PingShell("-c 1", host).pingExpend();
        Double preExpend = average.get(id);
        if (preExpend == null) {
            average.put(id, expend);
        } else {
            Double avExpend = (preExpend + expend) / 2;
            average.put(id, avExpend);
        }
        //log.info(host+"网络平均耗时为:"+ expend);
    }

    private void repairDubboRtt(Integer id, String clusterAddress) throws IOException {
        if (clusterAddress != null) {
            List<String> address = Splitter.on(",").omitEmptyStrings().splitToList(clusterAddress);
            if (address.size() > 0) {
                double totalExpend = 0D;
                for (String addr : address) {
                    String host = addr.split(":")[0];
                    totalExpend += new PingShell("-c 1", host).pingExpend();
                }
                double avgExpend = totalExpend / address.size();
                Double preExpend = average.get(id);
                if (preExpend == null) {
                    average.put(id, avgExpend);
                } else {
                    Double avExpend = (preExpend + avgExpend) / 2;
                    average.put(id, avExpend);
                }
                //log.info(host+"网络平均耗时为:"+ expend);
            }
        }
    }

}
