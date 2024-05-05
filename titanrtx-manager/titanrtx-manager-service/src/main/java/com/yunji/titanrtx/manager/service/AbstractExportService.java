package com.yunji.titanrtx.manager.service;

import com.deepoove.poi.data.NumbericRenderData;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.PressureReportBo;
import com.yunji.titanrtx.manager.dao.bos.http.ReportItemBo;
import com.yunji.titanrtx.manager.dao.bos.http.ReportRecordsBo;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractExportService implements ExportService{


    @Override
    public List<ReportItemBo> reportItemBos(String ids) {
        List<String> reportIds = CommonU.stringJoinToList(ids);
        List<PressureReportBo> reportBos = doSelectByIds(reportIds);
        return buildReportItemBos(reportBos);
    }


    private List<ReportItemBo> buildReportItemBos(List<PressureReportBo> reportBos) {
        List< List<PressureReportBo>> groupReports = new ArrayList<>();
        reportBos.stream().collect(Collectors.groupingBy(PressureReportBo::getReportId)).forEach((integer, bos) -> {
            groupReports.add(bos);
        });
        List<ReportItemBo> reportItemBos = new ArrayList<>();
        for (List<PressureReportBo> reports  : groupReports){
            ReportItemBo itemBo = parseToReportItemBo(reports);
            reportItemBos.add(itemBo);
        }
        return reportItemBos;
    }


    private ReportItemBo parseToReportItemBo(List<PressureReportBo> reports) {
        ReportItemBo bo = new ReportItemBo();
        ReportRecordsBo recordsBo = new ReportRecordsBo();
        bo.setRecordsBo(recordsBo);
        List<TextRenderData> urls = new ArrayList<>();
        NumbericRenderData urlNumberRenderData = new NumbericRenderData(urls);
        List<RowRenderData> records = new ArrayList<>();
        boolean flag = true;
        for (PressureReportBo reportBo : reports){
            if (flag){
                BaseEntity sceneEntity = reportBo.getSceneEntity();
                bo.setName(sceneName(sceneEntity));
                for (BaseEntity be : reportBo.getBulletEntity()){
                    urls.add(new TextRenderData(textRender(be)));
                }
                bo.setUrl(urlNumberRenderData);
                flag = false;
            }
            RowRenderData renderData = rowRender(reportBo);
            records.add(renderData);
        }
        recordsBo.setRecords(records);
        return bo;
    }

    protected abstract List<PressureReportBo> doSelectByIds(List<String> reportIds);

    protected abstract RowRenderData rowRender(PressureReportBo reportBo);

    protected abstract String sceneName(BaseEntity sceneEntity);

    protected abstract String textRender(BaseEntity be);


}
