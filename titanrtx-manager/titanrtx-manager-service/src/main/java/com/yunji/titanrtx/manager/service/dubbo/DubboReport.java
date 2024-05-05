package com.yunji.titanrtx.manager.service.dubbo;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.common.annotation.TaskAnnotation;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.PressureReportBo;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboReportEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboSceneEntity;
import com.yunji.titanrtx.manager.service.AbstractReportHandler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@TaskAnnotation(type = TaskType.DUBBO)
public class DubboReport extends AbstractReportHandler {

    @Resource
    private DubboSceneService dubboSceneService;

    @Resource
    private DubboServiceService dubboServiceService;

    @Resource
    private DubboReportService dubboReportService;


    @Override
    protected int doInsert(Integer sceneId, PressureReportBo reportBo) {
        DubboReportEntity entity = new DubboReportEntity();
        entity.setSceneId(sceneId);
        entity.setSnap(JSON.toJSONString(reportBo, features));
        dubboReportService.insert(entity);
        return entity.getId();
    }

    @Override
    protected List<? extends BaseEntity> selectBullet(BaseEntity baseEntity) {
        DubboSceneEntity sceneEntity = (DubboSceneEntity) baseEntity;
        return dubboServiceService.selectByIds(CommonU.stringJoinToList(sceneEntity.getIdsWeight()));
    }

    @Override
    protected BaseEntity updateSceneStatus(Integer sceneId) {
        dubboSceneService.updateStatus(sceneId, 0);
        return dubboSceneService.findById(sceneId);
    }


}
