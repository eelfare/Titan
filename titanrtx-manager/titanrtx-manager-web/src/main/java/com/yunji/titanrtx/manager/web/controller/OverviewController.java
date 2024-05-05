package com.yunji.titanrtx.manager.web.controller;

import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.u.DateU;
import com.yunji.titanrtx.manager.dao.bos.OverviewMonitorBo;
import com.yunji.titanrtx.manager.dao.bos.http.AgentInfoBo;
import com.yunji.titanrtx.manager.dao.bos.http.OverviewInfoBo;
import com.yunji.titanrtx.manager.service.TaskService;
import com.yunji.titanrtx.manager.service.http.HttpReportService;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import com.yunji.titanrtx.manager.service.http.LinkService;
import com.yunji.titanrtx.plugin.monitor.MonitorBo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@RestController
@RequestMapping("overview/")
public class OverviewController {

    @Resource
    private LinkService linkService;

    @Resource
    private HttpSceneService httpSceneService;

    @Resource
    private HttpReportService httpReportService;

    @Resource
    private TaskService taskService;

    @Resource
    private MonitorService monitorService;


    @RequestMapping("info.query")
    public RespMsg overviewInfo(){
        AgentInfoBo agentInfo = taskService.agentInfoBo();
        OverviewInfoBo bo = new OverviewInfoBo();
        bo.setAgentInfo(agentInfo);
        bo.setLinkCount(linkService.count());
        bo.setReportCount(httpReportService.count());
        bo.setSceneCount(httpSceneService.count());
        return RespMsg.respSuc(bo);
    }


    @RequestMapping("netRateMonitor.query")
    public RespMsg netRateMonitor(){
        List<String> ins = taskService.instanceIds();

        String start = DateU.getCurrentTimeBeforeMinute(5);
        String end = DateU.getCurrentTimeBeforeSecond(15);

        final List<MonitorBo>  netInBos = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(2);

        new Thread(() -> {
            monitorService.doNetInRateRequest(ins, start, end, netInBos);
            latch.countDown();
        }).start();
        final List<MonitorBo>  netOutBos = new ArrayList<>();
        new Thread(() -> {
            monitorService.doNetOutRateRequest(ins, start, end, netOutBos);
            latch.countDown();
        }).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<MonitorBo> fuseNetInBos = fuseMonitorBo(netInBos);
        List<MonitorBo> fuseNetOutBos = fuseMonitorBo(netOutBos);
        OverviewMonitorBo bo = new OverviewMonitorBo();
        bo.setNetIn(fuseNetInBos);
        bo.setNetOut(fuseNetOutBos);
        return RespMsg.respSuc(bo);
    }


    @RequestMapping("restartAll.do")
    public RespMsg restartAll(){
         taskService.restartAll();
         return RespMsg.respSuc();
    }

    @RequestMapping("resetAll.do")
    public RespMsg resetAll(){
         taskService.reset();
        return RespMsg.respSuc();
    }



    private List<MonitorBo> fuseMonitorBo(List<MonitorBo> netBos) {
        List<MonitorBo> fuseBos = new ArrayList<>();
        netBos.stream().collect(Collectors.groupingBy(MonitorBo::getTimestamp)).forEach((timestamp,bos) ->{
            MonitorBo monitorBo = new MonitorBo();
            double sum = bos.stream().mapToDouble(MonitorBo::getValue).sum();
            monitorBo.setTimestamp(timestamp);
            monitorBo.setValue(sum);
            fuseBos.add(monitorBo);
        });
       fuseBos.sort(Comparator.comparingLong(MonitorBo::getTimestamp));
        return fuseBos;
    }



}
