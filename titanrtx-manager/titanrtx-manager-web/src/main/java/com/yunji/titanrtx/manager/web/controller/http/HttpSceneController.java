package com.yunji.titanrtx.manager.web.controller.http;

import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.http.AgentInfoBo;
import com.yunji.titanrtx.manager.dao.bos.http.HttpSceneBo;
import com.yunji.titanrtx.manager.dao.bos.http.SceneMetaBo;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import com.yunji.titanrtx.manager.service.SceneOperatingCenterService;
import com.yunji.titanrtx.manager.service.TaskService;
import com.yunji.titanrtx.manager.service.common.SystemProperties;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import com.yunji.titanrtx.manager.service.http.LinkParamsService;
import com.yunji.titanrtx.manager.service.http.LinkService;
import com.yunji.titanrtx.manager.web.config.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP场景管理
 */
@Slf4j
@RestController
@RequestMapping("httpScene/")
public class HttpSceneController {

    @Resource
    private SystemConfig systemConfig;

    @Resource
    SystemProperties systemProperties;

    @Resource
    private HttpSceneService httpSceneService;

    @Resource
    private LinkService linkService;

    @Resource
    private TaskService taskService;

    @Resource
    private SceneOperatingCenterService sceneCreateService;

    /**
     * http压测入口
     */
    @RequestMapping("start.do")
    public RespMsg start(Integer id) {
        return sceneCreateService.start(id);
    }

    @RequestMapping("stop.do")
    public RespMsg stop(Integer id) {
        HttpSceneEntity httpSceneEntity = httpSceneService.findById(id);
        if (httpSceneEntity.getStatus() != 1) {
            return RespMsg.respErr("当前场景未正在压测中");
        }
        log.info("停止压测开始中：{}.....................................................................", httpSceneEntity);

        RespMsg respMsg = taskService.doStop(id, TaskType.HTTP);
        if (respMsg.isSuccess()) {
            httpSceneService.updateStatus(id, 0);
            return RespMsg.respSuc();
        }
        return respMsg;
    }


    @RequestMapping("list.query")
    public RespMsg list() {
        return RespMsg.respSuc(httpSceneService.selectAll());
    }


    @RequestMapping("link.query")
    public RespMsg link(String linkIdsScale, String linkIdsWeight, String lingIdsQps) {
        return RespMsg.respSuc(fillLinkWeight(linkService.selectByIds(CommonU.parseIds(linkIdsWeight)), CommonU.parseIdWeightMap(linkIdsScale), CommonU.parseIdWeightMap(linkIdsWeight), CommonU.parseIdQpsMap(lingIdsQps)));
    }


    @RequestMapping("addOrUpdate.do")
    public RespMsg addOrUpdate(@RequestBody HttpSceneEntity httpSceneEntity) {
        Integer id = httpSceneEntity.getId();
        if (id == null) {
            httpSceneService.insert(httpSceneEntity);
        } else {
            httpSceneService.update(httpSceneEntity);
        }
        return RespMsg.respSuc();
    }


    @RequestMapping("meta.query")
    public RespMsg meta() {
        AgentInfoBo agentInfo = taskService.agentInfoBo();
        SceneMetaBo bo = new SceneMetaBo();
        bo.setConcurrent(systemProperties.httpConcurrent);
        bo.setAvailableSize(agentInfo.getAvailable().size());
        bo.setTotalSize(agentInfo.getTotal().size());
        return RespMsg.respSuc(bo);
    }


    @RequestMapping("info.query")
    public RespMsg info(Integer id) {
        HttpSceneBo bo = new HttpSceneBo();
        HttpSceneEntity httpSceneEntity = httpSceneService.findById(id);
        List<LinkEntity> links = getLinkEntities(httpSceneEntity);
        bo.setHttpSceneEntity(httpSceneEntity);
        bo.setLinks(links);
        return RespMsg.respSuc(bo);
    }

    private List<LinkEntity> getLinkEntities(HttpSceneEntity httpSceneEntity) {
        // tips(景风):不管是权重分配还是QPS分配，两者数据都会存在
        List<String> ids = CommonU.parseIds(httpSceneEntity.getIdsWeight());
        Map<Integer, Integer> idWeightMap = CommonU.parseIdWeightMap(httpSceneEntity.getIdsWeight());
        Map<Integer, Integer> idScaleMap = new HashMap<>();
        if (httpSceneEntity.getIdsScale().isEmpty()) {
            idScaleMap.putAll(idWeightMap);
        } else {
            idScaleMap = CommonU.parseIdWeightMap(httpSceneEntity.getIdsScale());
        }
        Map<Integer, Long> idQpsMap = CommonU.parseIdQpsMap(httpSceneEntity.getIdsQps());
        List<LinkEntity> entities = linkService.selectByIds(ids);
        return fillLinkWeight(entities, idScaleMap, idWeightMap, idQpsMap);
    }

    private List<LinkEntity> fillLinkWeight(List<LinkEntity> links, Map<Integer, Integer> linkIdsScale, Map<Integer, Integer> linkIdsWeight, Map<Integer, Long> linkIdsQps) {
        for (LinkEntity link : links) {
            link.setScale(linkIdsScale.get(link.getId()));
            link.setWeight(linkIdsWeight.get(link.getId()));
            Long qps = linkIdsQps.get(link.getId());
            link.setQps(qps == null ? 0 : qps);
        }
        return links;
    }

    @RequestMapping("search.query")
    public RespMsg search(String key) {
        if (StringUtils.isEmpty(key)) return list();
        return RespMsg.respSuc(httpSceneService.searchScenes(key));
    }

    @RequestMapping("delete.do")
    public RespMsg delete(Integer id) {
        return RespMsg.respCom(httpSceneService.deleteById(id));
    }


    @RequestMapping("concurrentInfo.query")
    public RespMsg concurrentInfo() {
        return RespMsg.respSuc(systemProperties.httpConcurrent);
    }

    @RequestMapping("concurrentConfig.do")
    public RespMsg concurrentInfo(Integer concurrent) {
        if (concurrent > SystemConfig.MAX_CONCURRENT || concurrent < SystemConfig.MIN_CONCURRENT) {
            return RespMsg.respErr("单机最大并发不能超过4W，最小并发不能小于1K");
        }
        systemProperties.httpConcurrent = concurrent;
        return RespMsg.respSuc();
    }


}
