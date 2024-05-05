package com.yunji.titanrtx.manager.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunji.titanrtx.manager.dao.bos.BaseLineBo;
import com.yunji.titanrtx.manager.dao.bos.StatisticsBo;
import com.yunji.titanrtx.manager.dao.entity.http.HttpBaseLineEntity;
import com.yunji.titanrtx.manager.service.http.HttpBaseLineService;
import com.yunji.titanrtx.plugin.http.AHCClientTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 27/4/2020 2:40 下午
 * @Version 1.0
 */
@Slf4j
public class RemoteCgiThread {
    HttpBaseLineService httpBaseLineService;
    StatisticsBo innerBo;
    int sceneId;
    String url;
    long startTime;
    long endTime;
    String influxdbUrl;
    AtomicInteger emptyCGIDataCount; // CGI数据为空的计数器
    AtomicReference<String> improperLinkBaseLine;// 非正常新能基线链路
    int rtError; // rt允许误差
    int tpsError; // tps允许误差


    public RemoteCgiThread(HttpBaseLineService httpBaseLineService,
                           AtomicInteger emptyCGIDataCount,
                           AtomicReference<String> improperLinkBaseLine,
                           StatisticsBo innerBo,
                           int sceneId,
                           String url,
                           long startTime,
                           long endTime,
                           String influxdbUrl,
                           int rtError,
                           int tpsError) {
        this.httpBaseLineService = httpBaseLineService;
        this.emptyCGIDataCount = emptyCGIDataCount;
        this.improperLinkBaseLine = improperLinkBaseLine;
        this.innerBo = innerBo;
        this.sceneId = sceneId;
        this.url = url;
        this.startTime = startTime;
        this.endTime = endTime;
        this.influxdbUrl = influxdbUrl;
        this.rtError = rtError;
        this.tpsError = tpsError;
    }


    public StatisticsBo process() {
        StringBuilder select = new StringBuilder("select sum(success) as success,sum(failure) as failure,mean(avgElapsed) as rt from \"180d\".cgi_data where service='");
        select.append(url)
                .append("' and time >= ")
                .append(startTime)
                .append("ms and time <= ")
                .append(endTime)
                .append("ms group by time(1m)#default#cgi#cgi_data#jingf");
        String params = "u=admin&p=admin&db=sentinel&epoch=ms&q=" + select.toString();
        log.info("influx db request params: {}", params);
        String result = AHCClientTool.doGet(influxdbUrl, params, "", "application/json", null);
        log.info("influx db result: {}", result);
        JSONObject jsonObject = JSONObject.parseObject(result).getJSONArray("results").getJSONObject(0);
        JSONArray series = jsonObject.getJSONArray("series");
        if (series.isEmpty()) {
            log.info("针对链路：{}查询不到cgi数据,目前累计{}次", url, emptyCGIDataCount.incrementAndGet());
        } else {
            BaseLineBo baseLineBo = null;
            HttpBaseLineEntity httpBaseLineEntity = httpBaseLineService.selectBySceneIdAndLinkId(sceneId, innerBo.getId());
            if (httpBaseLineEntity != null) {
                baseLineBo = JSON.parseObject(httpBaseLineEntity.getBaseLine(), BaseLineBo.class);
            }
            // avg tps
            JSONArray values = series.getJSONObject(0).getJSONArray("values");
            int size = values.size();
            Long sumTps = 0L;
            Double sumRt = 0.0;
            for (int i = 0; i < size; i++) {
                JSONArray jsonArray = values.getJSONArray(i);
                Long time = jsonArray.getLong(0);
                Integer success = jsonArray.getInteger(1);
                Integer failure = jsonArray.getInteger(2);
                Double rt = jsonArray.getDouble(3);
                sumTps += (success == null ? 0 : success) + (failure == null ? 0 : failure);
                sumRt += rt;
            }
            BigDecimal bg = new BigDecimal(sumRt / size);
            innerBo.setAvgRt(bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            innerBo.setAvgDisposeCount(sumTps / size);
            if (baseLineBo != null) {
                if (innerBo.getAvgRt() > baseLineBo.getAvgRt() + rtError || innerBo.getAvgDisposeCount() < baseLineBo.getAvgDisposeCount() - tpsError) {
                    String update = "";
                    String current;
                    do {
                        current = improperLinkBaseLine.get();
                        if (current == null) {
                            update = (url + ",");
                        } else {
                            update = current + url + ",";
                        }
                        log.info("执行数据插入 {}", update);
                    } while (!improperLinkBaseLine.compareAndSet(current, update));
                }
            }
        }
        return innerBo;
    }
}
