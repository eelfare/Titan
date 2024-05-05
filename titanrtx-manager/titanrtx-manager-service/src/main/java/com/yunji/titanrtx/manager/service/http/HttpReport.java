package com.yunji.titanrtx.manager.service.http;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.common.annotation.TaskAnnotation;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.PressureReportBo;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpReportEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.service.AbstractReportHandler;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@TaskAnnotation(type = TaskType.HTTP)
public class HttpReport extends AbstractReportHandler {

    @Resource
    private HttpSceneService httpSceneService;

    @Resource
    private LinkService linkService;

    @Resource
    private HttpReportService httpReportService;

    @Override
    protected int doInsert(Integer sceneId, PressureReportBo reportBo) {
        HttpReportEntity httpReportEntity = new HttpReportEntity();
        httpReportEntity.setSceneId(sceneId);
        httpReportEntity.setSnap(JSON.toJSONString(reportBo,features));
        httpReportService.insert(httpReportEntity);
        return httpReportEntity.getId();
    }

    @Override
    protected List<? extends BaseEntity> selectBullet(BaseEntity baseEntity) {
        HttpSceneEntity httpSceneEntity = (HttpSceneEntity)baseEntity;
        return linkService.selectByIds(CommonU.parseIds(httpSceneEntity.getIdsWeight()));
    }

    @Override
    protected BaseEntity updateSceneStatus(Integer sceneId) {
        httpSceneService.updateStatus(sceneId,0);
        return httpSceneService.findById(sceneId);
    }

}
