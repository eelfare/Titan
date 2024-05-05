package com.yunji.titanrtx.manager.web.controller.auto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunji.titanrtx.manager.dao.bos.top.TopLinkAsFilterBo;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.service.SceneOperatingCenterService;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.common.u.DateU;
import com.yunji.titanrtx.manager.dao.bos.top.*;
import com.yunji.titanrtx.manager.dao.entity.auto.FilterEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkParamsEntity;
import com.yunji.titanrtx.manager.service.auto.FilterService;
import com.yunji.titanrtx.manager.service.http.LinkParamsService;
import com.yunji.titanrxt.plugin.influxdb.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.InfluxDBIOException;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("topLink/")
public class TopLinkController {

    @Resource
    private StoreService storeService;

    @Resource
    private FilterService filterService;

    @Resource
    private LinkParamsService httpParamsService;

    @Resource
    SceneOperatingCenterService sceneCreateService;

    @Value("${query.time.test:false}") // 是否是本地测试influxdb数据
    private boolean queryTimeTest;

    private static String sourceContent = "";


    @RequestMapping("list.query")
    public RespMsg list(String domain, Integer blackGroupId, String startTime, String endTime, Integer number, String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            sourceContent = "";
        }
        Date sDate, eDate;
        if (!queryTimeTest) {
            sDate = DateU.parseDate(startTime, DateU.LONG_PATTERN);
            eDate = DateU.parseDate(endTime, DateU.LONG_PATTERN);
        } else {
            // 模拟数据
            sDate = DateU.parseDate("2019-11-13 15:20:58", DateU.LONG_PATTERN);
            eDate = DateU.parseDate("2019-11-13 15:21:00", DateU.LONG_PATTERN);
        }
        List<TopLinkAsFilterBo> topLink = sceneCreateService.getTopLink(domain, blackGroupId, sDate, eDate, number, sourceContent);
        return RespMsg.respSuc(topLink);
    }

    @RequestMapping("upload.do")
    public RespMsg upload(@RequestBody String json) {
        JSONObject jsonObject = JSON.parseObject(json);
        TopLinkController.sourceContent = jsonObject.getString("sourceContent");
        return RespMsg.respSuc();
    }


    @RequestMapping("params.query")
    public RespMsg params(String url) {
//        log.info("开始获取链路{}的参数个数", url);
        URL inUrl;
        try {
            inUrl = new URL(url);
        } catch (MalformedURLException e) {
            return RespMsg.respErr("无效url信息：" + url);
        }
        String domain = inUrl.getHost();
        String path = "\"" + inUrl.getPath() + "\"";
        String commander = String.format("SELECT COUNT(param) FROM %s", path);
        List<ParamsSizeBo> sizeBos = doQuery(commander, domain, inUrl.getPath(), ParamsSizeBo.class);
        ParamsSizeBo bo = new ParamsSizeBo();
        if (sizeBos.size() > 0) {
            bo = sizeBos.get(0);
        }
        return RespMsg.respSuc(bo);
    }


    @RequestMapping("domains.query")
    public RespMsg domains() {
        List<String> databases = storeService.describeDatabases();
        List<String> domains = new ArrayList<>(databases.size());
        for (String s : databases) {
            if (s.endsWith(".com")) {
                domains.add(s);
            }
        }
        return RespMsg.respSuc(domains);
    }


    @RequestMapping("pullParams.query")
    public RespMsg pullParams(Integer id, String url, int size) throws UnsupportedEncodingException {
        sceneCreateService.pullParams(id,url,size);
        return RespMsg.respSuc();
    }


    @RequestMapping("turnSceneByUrl.do")
    public RespMsg turnSceneByUrl(@RequestBody TurnSceneBo bo) {
        HttpSceneEntity sceneEntity = sceneCreateService.createScene(bo);
        return sceneEntity == null ? RespMsg.respErr("场景已存在") : RespMsg.respSuc(sceneEntity.getId());
    }

    @RequestMapping("remove.do")
    public RespMsg remove(String path) {
        FilterEntity filterEntity = new FilterEntity();
        filterEntity.setDomain("*");
        filterEntity.setPath(path);
        return RespMsg.respSuc(filterService.insert(filterEntity));
    }


    private <T> List<T> doQuery(String commander, String databases, String measurementName, Class<T> clazz) {
        Query query = new Query(commander, databases);
        QueryResult result = null;
        try {
            result = storeService.query(query);
        } catch (InfluxDBIOException e) {
            throw new RuntimeException("查询超时，请缩小查询时间段");
        }
        if (result == null || result.getResults() == null || result.getResults().size() == 0 ||
                StringUtils.isNotEmpty(result.getResults().get(0).getError())) {
            return new ArrayList<>();
        }
        return new InfluxDBResultMapper().toPOJO(result, clazz, measurementName);
    }

}
