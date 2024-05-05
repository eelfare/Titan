package com.yunji.titanrtx.common.service;

import com.yunji.titanrtx.common.domain.auto.AbstractDeploy;
import com.yunji.titanrtx.common.domain.cia.Rules;
import com.yunji.titanrtx.common.domain.meta.AgentMeta;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.task.Progress;
import com.yunji.titanrtx.common.task.Restart;
import com.yunji.titanrtx.common.task.Start;
import com.yunji.titanrtx.common.task.Stop;

import java.io.IOException;
import java.util.List;

public interface CommanderService extends Start,Stop,Progress,Restart {

    List<AgentMeta> agentMetas();

    void reset();

    RespMsg disable(String address);

    RespMsg enable(String address);

    RespMsg rules();

    RespMsg addRules(Rules rules);

    RespMsg deleteRules(Integer id);

    RespMsg updateRules(Rules rules);

    Rules  findById(Integer id);

    List<AbstractDeploy> listAutoDeploy();

    RespMsg addAutoDeploy(AbstractDeploy deploy);

    RespMsg deleteAutoDeploy(Integer id);

    RespMsg updateAutoDeploy(AbstractDeploy deploy);

    AbstractDeploy findAutoDeployById(Integer id);

    List<String> hosts(String address) throws IOException;

    RespMsg modifyHosts(String address, String content);

    Boolean top300StressSwitch(Boolean topSwitch);

    Boolean getTop300StressSwitch();
}
