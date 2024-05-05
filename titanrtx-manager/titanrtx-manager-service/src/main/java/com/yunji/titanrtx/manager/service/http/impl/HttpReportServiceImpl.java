package com.yunji.titanrtx.manager.service.http.impl;

import com.alibaba.fastjson.JSON;
import com.deepoove.poi.data.RowRenderData;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.BaseLineBo;
import com.yunji.titanrtx.manager.dao.bos.PressureReportBo;
import com.yunji.titanrtx.manager.dao.bos.StatisticsBo;
import com.yunji.titanrtx.manager.dao.bos.SummaryStatistics;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpBaseLineEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpReportEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import com.yunji.titanrtx.manager.dao.mapper.http.HttpReportMapper;
import com.yunji.titanrtx.manager.service.AbstractExportService;
import com.yunji.titanrtx.manager.service.http.HttpBaseLineService;
import com.yunji.titanrtx.manager.service.http.HttpReportService;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class HttpReportServiceImpl extends AbstractExportService implements HttpReportService {


    @Resource
    private HttpReportMapper httpReportMapper;

    @Resource
    private HttpSceneService httpSceneService;

    @Resource
    private HttpBaseLineService httpBaseLineService;

    @Value("${rt.error:#{30}}")
    int rtError; // rt允许误差
    @Value("${tps.error:#{20}}")
    int tpsError; // tps允许误差

    @Override
    public int insert(HttpReportEntity httpReportEntity) {
        return httpReportMapper.insert(httpReportEntity);
    }

    @Override
    public List<PressureReportBo> selectBySceneId(Integer sceneId) {
        return reportBo(httpReportMapper.selectBySceneId(sceneId));
    }

    @Override
    public PressureReportBo findById(Integer id) {
        HttpReportEntity reportEntity = httpReportMapper.findById(id);
        PressureReportBo bo = JSON.parseObject(reportEntity.getSnap(), PressureReportBo.class);
        SummaryStatistics sum = bo.getSum();
        List<BaseLineBo> baseLineBos = new ArrayList<>();
        List<StatisticsBo> bos = sum.getBos();
        Integer sceneId = reportEntity.getSceneId();
        bos.stream().forEach(temp -> {
            HttpBaseLineEntity httpBaseLineEntity = httpBaseLineService.selectBySceneIdAndLinkId(sceneId, temp.getId());
            BaseLineBo baseLineBo = null;
            if (httpBaseLineEntity != null) {
                baseLineBo = JSON.parseObject(httpBaseLineEntity.getBaseLine(), BaseLineBo.class);
            }
            if (baseLineBo == null) {
                baseLineBo = new BaseLineBo();
            }
            baseLineBos.add(baseLineBo);
        });
        sum.setBaseLineBos(baseLineBos);
        bo.setReportId(reportEntity.getId());
        bo.setRtError(rtError);
        bo.setTpsError(tpsError);
        return bo;
    }

    @Override
    public List<PressureReportBo> selectAll() {
        return reportBo(httpReportMapper.selectAll());
    }

    @Override
    public int deleteById(Integer id) {
        return httpReportMapper.deleteById(id);
    }

    @Override
    public List<PressureReportBo> searchSceneName(String key) {
        List<HttpReportEntity> reportEntities = new ArrayList<>();
        List<Integer> sceneIds = httpSceneService.selectSceneIds(key);
        for (Integer id : sceneIds) {
            reportEntities.addAll(httpReportMapper.selectBySceneId(id));
        }
        return reportBo(reportEntities);
    }

    @Override
    public int count() {
        return httpReportMapper.count();
    }


    private List<PressureReportBo> reportBo(List<HttpReportEntity> entities) {
        List<PressureReportBo> reportBos = new ArrayList<>(entities.size());
        for (HttpReportEntity reportEntity : entities) {
            PressureReportBo bo = JSON.parseObject(reportEntity.getSnap(), PressureReportBo.class);
            bo.setReportId(reportEntity.getId());
            reportBos.add(bo);
        }
        return reportBos;
    }


    @Override
    protected List<PressureReportBo> doSelectByIds(List<String> reportIds) {
        List<HttpReportEntity> reportEntities = httpReportMapper.selectByIds(reportIds);
        return reportBo(reportEntities);
    }


    public RowRenderData rowRender(PressureReportBo reportBo) {
        HttpSceneEntity httpSceneEntity = (HttpSceneEntity) reportBo.getSceneEntity();
        return RowRenderData.build(
                String.valueOf(httpSceneEntity.getTotal()),
                String.valueOf(httpSceneEntity.getConcurrent()),
                reportBo.getSum().getRequestSuccessCodeRate(),
                reportBo.getSum().getBusinessSuccessCodeRate(),
                String.valueOf(reportBo.getSum().getQps()),
                String.valueOf(reportBo.getSum().getTotalDuration()),
                String.valueOf(reportBo.getSum().getAverageDuration()));
    }

    @Override
    protected String sceneName(BaseEntity sceneEntity) {
        HttpSceneEntity httpSceneEntity = (HttpSceneEntity) sceneEntity;
        return httpSceneEntity.getName();
    }

    @Override
    protected String textRender(BaseEntity be) {
        LinkEntity linkEntity = (LinkEntity) be;
        return CommonU.buildFullUrl(linkEntity.getProtocol().getMemo(), linkEntity.getUrl());
    }


    @Override
    public List<PressureReportBo> selectByIds(List<String> reportIds) {
        return reportBo(httpReportMapper.selectByIds(reportIds));
    }


}
