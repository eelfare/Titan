package com.yunji.titanrtx.manager.web.controller.http;

import com.alibaba.fastjson.JSONObject;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.bos.BaseLineBo;
import com.yunji.titanrtx.manager.dao.bos.PressureReportBo;
import com.yunji.titanrtx.manager.dao.entity.http.HttpBaseLineEntity;
import com.yunji.titanrtx.manager.service.http.HttpBaseLineService;
import com.yunji.titanrtx.manager.service.http.HttpReportService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 13/4/2020 2:10 下午
 * @Version 1.0
 */
@RestController
@RequestMapping("httpBaseLine")
public class HttpBaseLineController {
    @Resource
    HttpBaseLineService httpBaseLineService;
    @Resource
    HttpReportService httpReportService;

    @RequestMapping("addOrUpdate.do")
    public RespMsg addOrUpdate(Integer id, Integer linkId, Double rt, Long tps) {
        if (!(rt != null && rt > 0 &&  tps != null && tps > 0)) {
            return RespMsg.respErr("请设置正确的基线");
        }
        PressureReportBo reportBo = httpReportService.findById(id);
        Integer sceneId = reportBo.getSceneEntity().getId();
        BaseLineBo bo = new BaseLineBo();
        bo.setAvgRt(rt);
        bo.setAvgDisposeCount(tps);
        String json = JSONObject.toJSONString(bo);

        HttpBaseLineEntity httpBaseLineEntity = httpBaseLineService.selectBySceneIdAndLinkId(sceneId, linkId);
        if (httpBaseLineEntity != null) {
            httpBaseLineEntity.setBaseLine(json);
            httpBaseLineService.update(httpBaseLineEntity);
        } else {
            httpBaseLineEntity = new HttpBaseLineEntity();
            httpBaseLineEntity.setSceneId(sceneId);
            httpBaseLineEntity.setLinkId(linkId);
            httpBaseLineEntity.setBaseLine(json);
            httpBaseLineService.insert(httpBaseLineEntity);

        }
        return RespMsg.respSuc();
    }
}
