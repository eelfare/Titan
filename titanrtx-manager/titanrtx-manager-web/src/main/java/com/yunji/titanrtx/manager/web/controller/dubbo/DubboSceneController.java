package com.yunji.titanrtx.manager.web.controller.dubbo;

import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.domain.task.DubboService;
import com.yunji.titanrtx.common.enums.Allot;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.dubbo.DubboSceneBo;
import com.yunji.titanrtx.manager.dao.bos.http.AgentInfoBo;
import com.yunji.titanrtx.manager.dao.bos.http.SceneMetaBo;
import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import com.yunji.titanrtx.manager.service.OutService;
import com.yunji.titanrtx.manager.service.TaskService;
import com.yunji.titanrtx.manager.service.dubbo.DubboSceneService;
import com.yunji.titanrtx.manager.service.dubbo.DubboServiceService;
import com.yunji.titanrtx.manager.service.dubbo.DubboStressService;
import com.yunji.titanrtx.manager.service.dubbo.ServiceParamsService;
import com.yunji.titanrtx.manager.service.support.CommonUtils;
import com.yunji.titanrtx.manager.web.config.SystemConfig;
import com.yunji.titanrtx.manager.web.support.u.RequestU;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("dubboScene/")
public class DubboSceneController {

    @Resource
    private DubboSceneService dubboSceneService;

    @Resource
    private DubboServiceService dubboServiceService;


    @Resource
    private ServiceParamsService serviceParamsService;

    @Resource
    private SystemConfig systemConfig;

    @Resource
    private TaskService taskService;

    @Resource
    private OutService outService;

    @Resource
    private DubboStressService dubboStressService;


    @RequestMapping("list.query")
    public RespMsg list() {
        return RespMsg.respSuc(dubboSceneService.selectAll());
    }


    @RequestMapping("search.query")
    public RespMsg search(String key) {
        if (StringUtils.isEmpty(key)) RespMsg.respSuc(list());
        int id;
        try {
            id = Integer.parseInt(key);
        } catch (NumberFormatException e) {
            return RespMsg.respSuc(dubboSceneService.searchScenes(key));
        }
        DubboSceneEntity entity = dubboSceneService.findById(id);
        List<DubboSceneEntity> dubboSceneEntities = new ArrayList<>();
        dubboSceneEntities.add(entity);
        return RespMsg.respSuc(dubboSceneEntities);
    }


    @RequestMapping("services.query")
    public RespMsg link(String serviceIds) {
        return RespMsg.respSuc(fillIdsWeight(dubboServiceService.selectByIds(CommonU.stringJoinToList(serviceIds)), CommonU.parseIdWeightMap(serviceIds)));
    }


    @RequestMapping("meta.query")
    public RespMsg meta() {
        AgentInfoBo agentInfo = taskService.agentInfoBo();
        SceneMetaBo bo = new SceneMetaBo();
        bo.setConcurrent(systemConfig.getDubboConcurrent());
        bo.setAvailableSize(agentInfo.getAvailable().size());
        bo.setTotalSize(agentInfo.getTotal().size());
        return RespMsg.respSuc(bo);
    }


    @RequestMapping("info.query")
    public RespMsg info(Integer id) {
        DubboSceneBo bo = new DubboSceneBo();
        DubboSceneEntity sceneEntity = dubboSceneService.findById(id);
        List<ServiceEntity> serviceEntities = getServiceEntities(sceneEntity);
        bo.setDubboSceneEntity(sceneEntity);
        bo.setServices(serviceEntities);
        return RespMsg.respSuc(bo);
    }

    private List<ServiceEntity> getServiceEntities(DubboSceneEntity sceneEntity) {
        List<String> ids = CommonU.parseIds(sceneEntity.getIdsWeight());
        Map<Integer, Integer> idWeightMap = CommonU.parseIdWeightMap(sceneEntity.getIdsWeight());
        List<ServiceEntity> entities = dubboServiceService.selectByIds(ids);
        return fillIdsWeight(entities, idWeightMap);
    }

    private List<ServiceEntity> fillIdsWeight(List<ServiceEntity> serviceEntities, Map<Integer, Integer> idsWeightMap) {
        for (ServiceEntity se : serviceEntities) {
            se.setWeight(idsWeightMap.get(se.getId()));
        }
        return serviceEntities;
    }


    @RequestMapping("delete.do")
    public RespMsg delete(Integer id) {
        return RespMsg.respCom(dubboSceneService.deleteById(id));
    }


    @RequestMapping("addOrUpdate.do")
    public RespMsg addOrUpdate(@RequestBody DubboSceneEntity dubboSceneEntity) {
        Integer id = dubboSceneEntity.getId();
        if (id == null) {
            dubboSceneService.insert(dubboSceneEntity);
        } else {
            dubboSceneService.update(dubboSceneEntity);
        }
        return RespMsg.respSuc();
    }


    @RequestMapping("start.do")
    public RespMsg start(Integer id) {
        DubboSceneEntity sceneEntity = dubboSceneService.findById(id);

        if (taskService.checkStart(sceneEntity.getConcurrent(), systemConfig.getDubboConcurrent()))
            return RespMsg.respErr("当前可用压测机数量不足");

        return dubboStressService.startPressure(id);
    }


    private List<Bullet> buildDubboParams(List<Bullet> bullets) {
        for (Bullet bullet : bullets) {
            Integer id = bullet.getId();
            List<Integer> paramsId = serviceParamsService.selectAllIdByServiceId(id);
            bullet.setParamIds(paramsId);
        }
        return bullets;
    }


    @RequestMapping("stop.do")
    public RespMsg stop(Integer id) {
        DubboSceneEntity sceneEntity = dubboSceneService.findById(id);
        if (sceneEntity.getStatus() != 1) {
            return RespMsg.respErr("当前场景未正在压测中");
        }
        log.info("停止压测开始中：{}.....................................................................", sceneEntity);
        RespMsg respMsg = taskService.doStop(id, TaskType.DUBBO);
        if (respMsg.isSuccess()) {
            dubboSceneService.updateStatus(id, 0);
            return RespMsg.respSuc();
        }
        return respMsg;
    }


}
