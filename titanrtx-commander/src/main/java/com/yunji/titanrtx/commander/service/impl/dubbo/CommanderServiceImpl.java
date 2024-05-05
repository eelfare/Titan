package com.yunji.titanrtx.commander.service.impl.dubbo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunji.titanrtx.commander.service.LiaisonService;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.auto.AbstractDeploy;
import com.yunji.titanrtx.common.domain.auto.BatchDeploy;
import com.yunji.titanrtx.common.domain.auto.CommonStressDeploy;
import com.yunji.titanrtx.common.domain.auto.TopStressDeploy;
import com.yunji.titanrtx.common.domain.cia.Rules;
import com.yunji.titanrtx.common.domain.meta.AgentMeta;
import com.yunji.titanrtx.common.domain.task.Task;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.AgentService;
import com.yunji.titanrtx.common.service.CommanderService;
import com.yunji.titanrtx.common.task.TaskDispatcher;
import com.yunji.titanrtx.common.u.CollectionU;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.common.u.LogU;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommanderServiceImpl implements CommanderService {

    @Resource
    private TaskDispatcher taskDispatcher;

    @Resource
    private LiaisonService liaisonService;

    @Resource
    private AgentService agentService;

    @Value("${user}")
    private String user;

    @Value("${passwd}")
    private String passwd;


    @Override
    public List<AgentMeta> agentMetas() {
        return liaisonService.agentMetas();
    }


    @Override
    public void restart() {
//        LinuxPartyManager linuxPartyManager = new LinuxPartyManager();
//        try {
//            linuxPartyManager.login("10.0.41.199", user, passwd);
//            linuxPartyManager.executeWithStdout("/usr/local/yunji/titanx/restart.sh");
//        } catch(Exception e){
//            log.error(e.getMessage(), e);
//        } finally {
//            linuxPartyManager.logout();
//        }
        List<String> agentAddress = liaisonService.getAgentAddress(liaisonService.agentMetas());
        for (String address : agentAddress) {
            liaisonService.ensureRemoteDomain(address);
            agentService.restart();
        }
    }

    @Override
    public void reset() {
        List<String> agentAddress = liaisonService.getAgentAddress(liaisonService.agentMetas());
        for (String address : agentAddress) {
            liaisonService.ensureRemoteDomain(address);
            agentService.stop();
        }
    }


    @Override
    public RespMsg start(Task task) {
        String taskNo = CommonU.createTaskNo(task.getId());
        log.info("接收到任务下发taskNo：{},Type:{} ..............................", taskNo, task.getTaskType());
        LogU.info("接收到任务下发taskNo：{},Type:{} ..............................", taskNo, task.getTaskType());
        InterProcessMutex lock = null;
        try {
            lock = liaisonService.acquireTaskStartLock();
            task.setTaskNo(taskNo);
            return taskDispatcher.start(task);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("任务下发准备异常:{}..........................................................", e.getMessage());
        } finally {
            log.info("task send over");
            try {
                if (lock != null) {
                    lock.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RespMsg.respErr();
    }

    @Override
    public RespMsg stop() {
        return taskDispatcher.stop();
    }

    @Override
    public RespMsg progress() {
        return taskDispatcher.progress();
    }

    @Override
    public RespMsg disable(String address) {
        liaisonService.ensureRemoteDomain(address);
        return agentService.disable();
    }

    @Override
    public RespMsg enable(String address) {
        liaisonService.ensureRemoteDomain(address);
        return agentService.enable();
    }

    @Override
    public RespMsg rules() {
        return RespMsg.respSuc(queryRules());
    }

    private List<Rules> queryRules() {
        String rulesData = liaisonService.getData(GlobalConstants.CIA_RULES_PATH);
        List<Rules> rulesList = JSON.parseArray(rulesData, Rules.class);
        if (CollectionU.isEmpty(rulesList)) {
            rulesList = new ArrayList<>();
        }
        return rulesList;
    }


    private void updateRules(List<Rules> rulesList) {
        liaisonService.updateData(GlobalConstants.CIA_RULES_PATH, JSON.toJSONString(rulesList));
    }

    @Override
    public RespMsg addRules(Rules rules) {
        List<Rules> rulesList = queryRules();
        rulesList.add(rules);
        updateRules(rulesList);
        return RespMsg.respSuc();
    }

    @Override
    public RespMsg deleteRules(Integer id) {
        List<Rules> rules = queryRules();
        for (Rules rule : rules) {
            Integer ruleId = rule.getId();
            if (ruleId.equals(id)) {
                rules.remove(rule);
                break;
            }
        }
        updateRules(rules);
        return RespMsg.respSuc();
    }

    @Override
    public RespMsg updateRules(Rules rule) {
        List<Rules> rules = queryRules();
        for (Rules innerRules : rules) {
            Integer innerRulesId = innerRules.getId();
            if (innerRulesId.equals(rule.getId())) {
                rules.remove(innerRules);
                break;
            }
        }
        rules.add(rule);
        updateRules(rules);
        return RespMsg.respSuc();
    }


    @Override
    public List<AbstractDeploy> listAutoDeploy() {
        return queryAutoTestDeploys();
    }

    private synchronized List<AbstractDeploy> queryAutoTestDeploys() {
        String deploysData = liaisonService.getData(GlobalConstants.AUTO_DEPLOYS_PATH);
        if (StringUtils.isEmpty(deploysData)) {
            return new ArrayList<>();
        }
        JSONArray jsonArray = JSON.parseArray(deploysData);
        int size = jsonArray.size();
        List<AbstractDeploy> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.containsKey("batch") && jsonObject.getBoolean("batch")) {
                BatchDeploy batchDeploy = jsonArray.getObject(i, BatchDeploy.class);
                result.add(batchDeploy);
                continue;
            }
            if (jsonObject.containsKey("topStress") && jsonObject.getBoolean("topStress")) {
                TopStressDeploy topStressDeploy = jsonArray.getObject(i, TopStressDeploy.class);
                result.add(topStressDeploy);
                continue;
            }
            if (jsonObject.containsKey("commonStress") && jsonObject.getBoolean("commonStress")) {
                CommonStressDeploy commonStressDeploy = jsonArray.getObject(i, CommonStressDeploy.class);
                result.add(commonStressDeploy);
                continue;
            }
        }
        return result;
    }

    private synchronized void updateAutoTestDeploy(List<AbstractDeploy> list) {
        liaisonService.updateData(GlobalConstants.AUTO_DEPLOYS_PATH, JSON.toJSONString(list));
    }

    @Override
    public RespMsg addAutoDeploy(AbstractDeploy deploy) {
        List<AbstractDeploy> list = queryAutoTestDeploys();
        list.add(deploy);
        updateAutoTestDeploy(list);
        return RespMsg.respSuc();
    }

    @Override
    public RespMsg deleteAutoDeploy(Integer id) {
        List<AbstractDeploy> deploys = queryAutoTestDeploys();
        for (AbstractDeploy deploy : deploys) {
            Integer ruleId = deploy.getId();
            if (ruleId.equals(id)) {
                deploys.remove(deploy);
                break;
            }
        }
        updateAutoTestDeploy(deploys);
        return RespMsg.respSuc();
    }

    @Override
    public AbstractDeploy findAutoDeployById(Integer id) {
        List<AbstractDeploy> deploys = queryAutoTestDeploys();
        for (AbstractDeploy deploy : deploys) {
            Integer innerId = deploy.getId();
            if (innerId.equals(id)) {
                return deploy;
            }
        }
        return null;
    }

    @Override
    public RespMsg updateAutoDeploy(AbstractDeploy deploy) {
        List<AbstractDeploy> deploys = queryAutoTestDeploys();
        for (AbstractDeploy innerDeploy : deploys) {
            Integer innerRulesId = innerDeploy.getId();
            if (innerRulesId.equals(deploy.getId())) {
                deploys.remove(innerDeploy);
                break;
            }
        }
        deploys.add(deploy);
        updateAutoTestDeploy(deploys);
        return RespMsg.respSuc();
    }

    @Override
    public Rules findById(Integer id) {
        List<Rules> rules = queryRules();
        for (Rules innerRules : rules) {
            Integer innerRulesId = innerRules.getId();
            if (innerRulesId.equals(id)) {
                return innerRules;
            }
        }
        return null;
    }

    @Override
    public List<String> hosts(String address) {
        liaisonService.ensureRemoteDomain(address);
        return agentService.hosts();
    }

    @Override
    public RespMsg modifyHosts(String address, String content) {
        liaisonService.ensureRemoteDomain(address);
        return agentService.modifyHosts(content);
    }

    @Override
    public Boolean top300StressSwitch(Boolean topSwitch) {
        return liaisonService.top300StressSwitch(topSwitch);
    }

    @Override
    public Boolean getTop300StressSwitch() {
        return liaisonService.getTop300StressSwitch();
    }


}
