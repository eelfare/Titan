package com.yunji.titanrtx.agent.future.core;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.agent.collect.CollectorUtils;
import com.yunji.titanrtx.agent.future.CallBack;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.statistics.StatisticsDetail;
import com.yunji.titanrtx.common.resp.RespCodeOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.http.HttpResponse;

import java.util.concurrent.CountDownLatch;

@Slf4j
public abstract class AbstractCallBack<T, R> implements CallBack<T, R> {

    private long startTime;

    private CountDownLatch countDownLatch;

    GenericObjectPool<T> pool;

    private StatisticsDetail statisticsDetail;

    private String path;

    boolean openAgentCollectQps;


    @Override
    public void completed(R r) {
        if (r instanceof HttpResponse) {
            CollectorUtils.collectSuccessResponse(openAgentCollectQps, ((HttpResponse) r).getStatusLine().getStatusCode(), path);
        } else {
            Integer respCode = RespCodeOperator.getRespCode(JSON.toJSONString(r));
            CollectorUtils.collectSuccessResponse(openAgentCollectQps, respCode, path);
        }

        statisticsDetail.addStatusCode(GlobalConstants.HTTP_SUCCESS_CODE);
        long duration = System.currentTimeMillis() - startTime;
        long totalDuration = duration < 0 ? 0 : duration;
        statisticsDetail.setDuration(statisticsDetail.getDuration() + totalDuration);
        int logicCode;
        try {
            logicCode = doLogic(r);
        } catch (Exception e) {
            logicCode = GlobalConstants.YUNJI_ERROR_CODE;
        }

        statisticsDetail.addBusiness(logicCode);
        doCompleted();
    }

    @Override
    public void cancelled() {
        statisticsDetail.addStatusCode(GlobalConstants.HTTP_ERROR_CODE);
        failResponse();
        doCompleted();
    }

    @Override
    public void failed(Exception ex) {
        statisticsDetail.addStatusCode(GlobalConstants.HTTP_ERROR_CODE);
        failResponse();
        doCompleted();
    }

    // 计算响应错误的数据
    private void failResponse() {
        CollectorUtils.collectErrorResponse(openAgentCollectQps, path, null);
    }

    private void doCompleted() {
        countDownLatch.countDown();
        returnObject();
    }


    @Override
    public void start() {
        this.startTime = System.currentTimeMillis();
    }


    @Override
    public void init(CountDownLatch countDownLatch, StatisticsDetail statisticsDetail, GenericObjectPool<T> pool, String path) {
        this.countDownLatch = countDownLatch;
        this.statisticsDetail = statisticsDetail;
        this.pool = pool;
        this.path = path;
    }


    protected abstract int doLogic(R r) throws Exception;

}
