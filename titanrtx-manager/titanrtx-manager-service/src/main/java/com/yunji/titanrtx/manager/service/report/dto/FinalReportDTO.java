package com.yunji.titanrtx.manager.service.report.dto;

import com.deepoove.poi.data.PictureRenderData;
import com.yunji.titanrtx.common.u.LocalDateU;
import com.yunji.titanrtx.manager.dao.bos.http.ReportRecordsBo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yunji.titanrtx.manager.service.report.support.ReportLog.log;

/**
 * FinalReportDTO,最终渲染成 doc 时使用.
 *
 * @author leihz
 * @since 2020-06-10 11:52 上午
 */
@Data
public class FinalReportDTO {

    private String startDate;

    private String endDate;

    private ReportRecordsBo cgiReport;

    private ReportRecordsBo dubboCostReport;

    private ReportRecordsBo beaconAlertReport;


    /**
     * 渲染的截图.
     * all_qps", "dubbo_cost_order", "mysql", "redis", "mongo
     */
    private PictureRenderData all_qps;


    private PictureRenderData mysql;

    private PictureRenderData redis;

    private PictureRenderData mongo;

    /**
     * cgi
     */
    private List<PictureDataWrapper> cgiCostList = new ArrayList<>();
    private List<PictureDataWrapper> cgiCostbyInvoke = new ArrayList<>();

    /**
     * 有多张图的情况
     */
    private List<PictureDataWrapper> dubboCostList = new ArrayList<>();
    private List<PictureDataWrapper> dubboCostByInvokeList = new ArrayList<>();

    public void setCgi(PictureDataWrapper wrapper) {
        cgiCostList.add(wrapper);
    }

    public void setCgiBYInvoke(PictureDataWrapper wrapper) {
        cgiCostbyInvoke.add(wrapper);
    }

    public void setDubbo(PictureDataWrapper wrapper) {
        dubboCostList.add(wrapper);
    }

    public void setDubboByInvoke(PictureDataWrapper wrapper) {
        dubboCostByInvokeList.add(wrapper);
    }


    private List<PictureBizWrapper> bizWrappers;


    public void buildBizWrappers() {
        log.info("cgiCostList size: {} \n" +
                        "cgiCostbyInvoke size: {} \n" +
                        "dubboCostList size: {} \n" +
                        "dubboCostByInvokeList size: {} \n",
                cgiCostList.size(),
                cgiCostbyInvoke.size(),
                dubboCostList.size(),
                dubboCostByInvokeList.size()
        );

        bizWrappers = new LinkedList<>();
        List<Integer> batchList = cgiCostList
                .stream()
                .map(PictureDataWrapper::getBatch)
                .distinct()
                .sorted(Integer::compareTo)
                .collect(Collectors.toList());

        log.info("截图分批 BatchList value :[{}].", batchList);

        for (int batch : batchList) {
            PictureDataWrapper cgiCost = filter(cgiCostList, batch);
            PictureDataWrapper cgiCostByInvoke = filter(cgiCostbyInvoke, batch);
            PictureDataWrapper dubboCost = filter(dubboCostList, batch);
            PictureDataWrapper dubboCostByInvoke = filter(dubboCostByInvokeList, batch);

            String time = getPictureTime(cgiCost, cgiCostByInvoke, dubboCost, dubboCostByInvoke);

            bizWrappers.add(new PictureBizWrapper(time, batch,
                    getRenderData(cgiCost),
                    getRenderData(cgiCostByInvoke),
                    getRenderData(dubboCost),
                    getRenderData(dubboCostByInvoke)));

        }

        log.info("Build bizWrappers ok,size : {}", bizWrappers.size());

    }


    private PictureDataWrapper filter(List<PictureDataWrapper> wrappers, int batch) {
        for (PictureDataWrapper wrapper : wrappers) {
            if (wrapper.getBatch() == batch) {
                return wrapper;
            }
        }
        return null;
    }

    private PictureRenderData getRenderData(PictureDataWrapper wrapper) {
        if (wrapper != null)
            return wrapper.getRenderData();

        return new PictureRenderData(500, 400, ".png", new byte[0]);
    }

    private String getPictureTime(PictureDataWrapper cgiCost,
                                  PictureDataWrapper cgiCostByInvoke,
                                  PictureDataWrapper dubboCost,
                                  PictureDataWrapper dubboCostByInvoke) {
        if (cgiCost != null) {
            return cgiCost.getName().split("#")[1];
        }
        if (cgiCostByInvoke != null) {
            return cgiCostByInvoke.getName().split("#")[1];
        }
        if (dubboCost != null) {
            return dubboCost.getName().split("#")[1];
        }
        if (dubboCostByInvoke != null) {
            return dubboCostByInvoke.getName().split("#")[1];
        }
        return LocalDateU.getCurrentTime();
    }


}
