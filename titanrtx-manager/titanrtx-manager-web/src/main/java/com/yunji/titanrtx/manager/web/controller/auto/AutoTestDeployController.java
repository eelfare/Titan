package com.yunji.titanrtx.manager.web.controller.auto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.auto.AbstractDeploy;
import com.yunji.titanrtx.common.domain.auto.BatchDeploy;
import com.yunji.titanrtx.common.domain.auto.CommonStressDeploy;
import com.yunji.titanrtx.common.domain.auto.TopStressDeploy;
import com.yunji.titanrtx.common.enums.AutoTestGrading;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.CommanderService;
import com.yunji.titanrtx.common.u.DateU;
import com.yunji.titanrtx.common.zookeeper.ZookeeperService;
import com.yunji.titanrtx.manager.dao.bos.auto.AutoDeployBo;
import com.yunji.titanrtx.manager.dao.entity.auto.AutoDeployHisEntity;
import com.yunji.titanrtx.manager.service.auto.AutoDeployHisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("autoDeploy")
public class AutoTestDeployController {

    @Resource
    private CommanderService commanderService;

    @Resource
    ZookeeperService zookeeperService;

    @Resource
    AutoDeployHisService autoDeployHisService;

    @RequestMapping("list.query")
    public RespMsg deploys() {
        List<AbstractDeploy> list = commanderService.listAutoDeploy();
        list.stream().filter(deploy -> deploy.getGrading() == AutoTestGrading.PRECISE && DateU.parseDate(deploy.getTime(), DateU.LONG_PATTERN).getTime() + 5 * 60 * 1000 < System.currentTimeMillis())
                .forEach(deploy -> {
                    commanderService.deleteAutoDeploy(deploy.getId());
                    // 保存数据到数据库中
                    AutoDeployHisEntity entity = autoDeployHisService.findById(deploy.getId());
                    String saveJson = JSON.toJSONString(deploy);
                    if (entity != null) {
                        entity.setContent(saveJson);
                        autoDeployHisService.update(entity);
                    } else {
                        entity = new AutoDeployHisEntity();
                        entity.setId(deploy.getId());
                        entity.setContent(saveJson);
                        autoDeployHisService.insert(entity);
                    }
                });
        List<AutoDeployHisEntity> autoDeployHisEntities = autoDeployHisService.selectAll();
        List<AbstractDeploy> active = commanderService.listAutoDeploy();

        autoDeployHisEntities.stream().forEach(his -> {
            String content = his.getContent();
            JSONObject jsonObject = JSONObject.parseObject(content);
            if (jsonObject.containsKey("batch") && jsonObject.getBoolean("batch")) {
                BatchDeploy batchDeploy = JSONObject.parseObject(content, BatchDeploy.class);
                batchDeploy.setHistory(true);
                active.add(batchDeploy);
                return;
            }
            if (jsonObject.containsKey("topStress") && jsonObject.getBoolean("topStress")) {
                TopStressDeploy topStressDeploy = JSONObject.parseObject(content, TopStressDeploy.class);
                topStressDeploy.setHistory(true);
                active.add(topStressDeploy);
                return;
            }
            if (jsonObject.containsKey("commonStress") && jsonObject.getBoolean("commonStress")) {
                CommonStressDeploy commonStressDeploy = JSONObject.parseObject(content, CommonStressDeploy.class);
                commonStressDeploy.setHistory(true);
                active.add(commonStressDeploy);
                return;
            }
        });
        return RespMsg.respSuc(active);
    }


    @RequestMapping("info.query")
    public RespMsg info(Integer id) {
        AbstractDeploy deploy;
        if (null == id) {
            deploy = new CommonStressDeploy();
        } else {
            deploy = commanderService.findAutoDeployById(id);
        }
        return RespMsg.respSuc(deploy);
    }

    @RequestMapping("addOrEditDeploy.do")
    public RespMsg addOrEditDeploy(@RequestBody AutoDeployBo autoTestDeployBo) {
        AbstractDeploy deploy = commanderService.findAutoDeployById(autoTestDeployBo.getId());
        if (deploy != null) {
            deploy.setGrading(autoTestDeployBo.getGrading());
            deploy.setTime(autoTestDeployBo.getTime());
            if (deploy instanceof TopStressDeploy) {
                ((TopStressDeploy) deploy).setContinuousTime(autoTestDeployBo.getContinuousTime());
            }
            return commanderService.updateAutoDeploy(deploy);
        } else {
            switch (autoTestDeployBo.getType()) {
                case COMMON_STRESS:
                case TOP_STRESS:
                    deploy = new CommonStressDeploy();
                    ((CommonStressDeploy) deploy).setSceneId(autoTestDeployBo.getBusinessId());
                    ((CommonStressDeploy) deploy).setContinuousTime(autoTestDeployBo.getContinuousTime());
                    break;
                case BATCH:
                    deploy = new BatchDeploy();
                    ((BatchDeploy) deploy).setBatchId(autoTestDeployBo.getBusinessId());
                    break;
                default:
            }
            deploy.setId(zookeeperService.nextId(GlobalConstants.AUTO_PATH));
            deploy.setName(autoTestDeployBo.getName());
            deploy.setTime(autoTestDeployBo.getTime());
            deploy.setGrading(autoTestDeployBo.getGrading());
            return commanderService.addAutoDeploy(deploy);
        }
    }


    @RequestMapping("delete.do")
    public RespMsg deleteDeploy(Integer id, boolean history) {
        if (history) {
            autoDeployHisService.deleteById(id);
            return RespMsg.respSuc();
        }
        return commanderService.deleteAutoDeploy(id);

    }


}
