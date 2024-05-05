package com.yunji.titanrtx.manager.service.dubbo.impl;

import com.alibaba.fastjson.JSON;
import com.deepoove.poi.data.RowRenderData;
import com.yunji.titanrtx.manager.dao.bos.PressureReportBo;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboReportEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import com.yunji.titanrtx.manager.dao.mapper.dubbo.DubboReportMapper;
import com.yunji.titanrtx.manager.service.AbstractExportService;
import com.yunji.titanrtx.manager.service.dubbo.DubboReportService;
import com.yunji.titanrtx.manager.service.dubbo.DubboSceneService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DubboReportServiceImpl extends AbstractExportService implements DubboReportService {

    @Resource
    private DubboReportMapper dubboReportMapper;

    @Resource
    private DubboSceneService dubboSceneService;


    @Override
    public int insert(DubboReportEntity dubboReportEntity) {
        return dubboReportMapper.insert(dubboReportEntity);
    }

    @Override
    public List<PressureReportBo> selectBySceneId(Integer sceneId) {
        return reportBo(dubboReportMapper.selectBySceneId(sceneId));
    }

    @Override
    public PressureReportBo findById(Integer id) {
        DubboReportEntity reportEntity = dubboReportMapper.findById(id);
        PressureReportBo bo = JSON.parseObject(reportEntity.getSnap(), PressureReportBo.class);
        bo.setReportId(reportEntity.getId());
        return bo;
    }

    @Override
    public List<PressureReportBo> selectAll() {
        return reportBo(dubboReportMapper.selectAll());
    }

    @Override
    public int deleteById(Integer id) {
        return dubboReportMapper.deleteById(id);
    }

    @Override
    public List<PressureReportBo> searchSceneName(String key) {
        List<DubboReportEntity> reportEntities =new ArrayList<>();
        List<Integer> sceneIds = dubboSceneService.selectSceneIds(key);
        for (Integer id : sceneIds){
            reportEntities.addAll(dubboReportMapper.selectBySceneId(id));
        }
        return reportBo(reportEntities);
    }

    @Override
    public int count() {
        return dubboReportMapper.count();
    }


    @Override
    protected List<PressureReportBo> doSelectByIds(List<String> reportIds) {
        List<DubboReportEntity> reportEntities = dubboReportMapper.selectByIds(reportIds);
        return reportBo(reportEntities);
    }

    private List<PressureReportBo> reportBo(List<DubboReportEntity> entities) {
        List<PressureReportBo> reportBos = new ArrayList<>(entities.size());
        for (DubboReportEntity reportEntity : entities){
            PressureReportBo bo = JSON.parseObject(reportEntity.getSnap(), PressureReportBo.class);
            bo.setReportId(reportEntity.getId());
            reportBos.add(bo);
        }
        return reportBos;
    }

    @Override
    public List<PressureReportBo> selectByIds(List<String> reportIds) {
        return reportBo(dubboReportMapper.selectByIds(reportIds));
    }

    @Override
    protected RowRenderData rowRender(PressureReportBo reportBo) {
        DubboSceneEntity dubboSceneEntity = (DubboSceneEntity)reportBo.getSceneEntity();
        return RowRenderData.build(
                String.valueOf(dubboSceneEntity.getTotal()),
                String.valueOf(dubboSceneEntity.getConcurrent()),
                reportBo.getSum().getRequestSuccessCodeRate(),
                reportBo.getSum().getBusinessSuccessCodeRate(),
                String.valueOf(reportBo.getSum().getQps()),
                String.valueOf(reportBo.getSum().getTotalDuration()),
                String.valueOf(reportBo.getSum().getAverageDuration()));
    }

    @Override
    protected String sceneName(BaseEntity sceneEntity) {
        DubboSceneEntity dubboSceneEntity = (DubboSceneEntity) sceneEntity;
        return dubboSceneEntity.getName();
    }

    @Override
    protected String textRender(BaseEntity be) {
        ServiceEntity serviceEntity = (ServiceEntity) be;
        return serviceEntity.getServiceName();
    }
}
