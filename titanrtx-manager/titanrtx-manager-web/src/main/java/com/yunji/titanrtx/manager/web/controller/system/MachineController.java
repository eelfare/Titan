package com.yunji.titanrtx.manager.web.controller.system;


import com.yunji.titanrtx.common.domain.meta.AgentMeta;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.CommanderService;
import com.yunji.titanrtx.manager.dao.bos.system.MachineMonitorBo;
import com.yunji.titanrtx.manager.web.controller.MonitorService;
import com.yunji.titanrtx.plugin.monitor.Monitor;
import com.yunji.titanrtx.plugin.monitor.MonitorBo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("machine/")
public class MachineController {

    @Resource
    private CommanderService commanderService;

    @Resource
    private MonitorService monitorService;


    @RequestMapping("list.query")
    public RespMsg  list(){
        return RespMsg.respSuc(commanderService.agentMetas());
    }


    @RequestMapping("hosts.query")
    public RespMsg hosts(String address){
        try {
            return RespMsg.respSuc(commanderService.hosts(address));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return RespMsg.respErr();
    }


    @RequestMapping("batchModify.do")
    public RespMsg batchModify(String content){

        List<String> agentAddress = agentAddress();

        StringBuilder sb = new StringBuilder();
        List<String> lineContent = Arrays.asList(content.split("\n"));

        Map<String, String> map = new HashMap<>();
        Integer cursor = null;
        for (int i = 0 ; i < lineContent.size() ; i++){
            String line = lineContent.get(i);
            if (StringUtils.isBlank(line) || !line.startsWith("##")){
                sb.append(line);
                sb.append("\n");
            }else{
                if (cursor == null){
                    cursor = i;
                }else {
                    map.put(lineContent.get(cursor),sb.toString());
                    sb = new StringBuilder();
                    cursor = i;
                }
            }
            if (i == lineContent.size() - 1 && cursor != null){
                map.putIfAbsent(lineContent.get(cursor),sb.toString());
            }
        }
        if (map.size() == 0){
            return RespMsg.respErr("无可更新hosts信息");
        }
        map.forEach((key, value) -> {
            Arrays.stream(key.replace("##", "").split(",")).forEach(index ->{
                try {
                    String address = agentAddress.get(Integer.valueOf(index) -1);
                    modifyHosts(address,value);
                }catch (IndexOutOfBoundsException ignored){
                }
            });
        });
        return RespMsg.respSuc();
    }

    private List<String> agentAddress(){
        List<AgentMeta> agentMetas = commanderService.agentMetas();
        List<String> addresses = new ArrayList<>(agentMetas.size());
        for (AgentMeta agentMeta : agentMetas){
            addresses.add(agentMeta.getAddress());
        }
        return addresses;
    }



    @RequestMapping("modifyHosts.do")
    public RespMsg modifyHosts(String address,String content){
        return commanderService.modifyHosts(address,content);
    }




    @RequestMapping("monitor.query")
    public RespMsg monitor(String id,String start,String end) throws Exception {
        List<MonitorBo> cpuMonitors = monitorService.cpuTotal(id, start, end);
        List<MonitorBo> memoryMonitors = monitorService.memoryUsed(id, start, end);
        List<MonitorBo> netInMonitors = monitorService.doNetInRateRequest(id, start, end);
        List<MonitorBo> netOutMonitors = monitorService.doNetOutRateRequest(id, start, end);
        MachineMonitorBo bo = MachineMonitorBo.builder().cpu(cpuMonitors).memory(memoryMonitors).netIn(netInMonitors).netOut(netOutMonitors).build();
        return RespMsg.respSuc(bo);
    }


    @RequestMapping("disable.do")
    public RespMsg disable(String address){
        commanderService.disable(address);
        return RespMsg.respSuc();
    }



    @RequestMapping("enable.do")
    public RespMsg enable(String address){
        commanderService.enable(address);
        return RespMsg.respSuc();
    }

}
