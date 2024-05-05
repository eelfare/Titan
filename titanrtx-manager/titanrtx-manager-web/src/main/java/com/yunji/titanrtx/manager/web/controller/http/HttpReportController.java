package com.yunji.titanrtx.manager.web.controller.http;


import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.PressureReportBo;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.service.http.HttpReportService;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import com.yunji.titanrtx.manager.service.report.dto.PressureReportDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;

@RestController
@RequestMapping("/httpReport")
public class HttpReportController {


    @Resource
    private HttpReportService httpReportService;

    @Resource
    private HttpSceneService httpSceneService;


    @RequestMapping("list.query")
    public RespMsg list() {
        return RespMsg.respSuc(httpReportService.selectAll());
    }


    @RequestMapping("search.query")
    public RespMsg search(String key) {
        if (StringUtils.isEmpty(key)) RespMsg.respSuc(httpReportService.selectAll());
        int id;
        try {
            id = Integer.parseInt(key);
        } catch (NumberFormatException e) {
            return RespMsg.respSuc(httpReportService.searchSceneName(key));
        }
        HttpSceneEntity sceneEntity = httpSceneService.findById(id);
        if (null == sceneEntity) {
            return RespMsg.respSuc(new ArrayList<>());
        }
        return RespMsg.respSuc(httpReportService.selectBySceneId(sceneEntity.getId()));
    }


    @RequestMapping("detail.query")
    public RespMsg detail(Integer id) {
        PressureReportBo reportBo = httpReportService.findById(id);

        PressureReportDTO reportVo = new PressureReportDTO();
        BeanUtils.copyProperties(reportBo, reportVo);
        //IDC环境不显示性能基线指标.
        if (!"IDC".equals(CommonU.getConfigEnv().toUpperCase())) {
            reportVo.setBos(reportBo.getSum().getBos());
        }
        return RespMsg.respSuc(reportVo);
    }


    @RequestMapping("delete.do")
    public RespMsg delete(Integer id) {
        return RespMsg.respCom(httpReportService.deleteById(id));
    }


}
