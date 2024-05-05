package com.yunji.titanrtx.agent.task.exec;

import com.yunji.titanrtx.agent.collect.CollectScheduler;
import com.yunji.titanrtx.agent.collect.CollectorUtils;
import com.yunji.titanrtx.agent.service.LiaisonService;
import com.yunji.titanrtx.agent.service.ParamsService;
import com.yunji.titanrtx.agent.task.exec.http.AbstractHttpExecute;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.domain.statistics.StatisticsDetail;
import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.domain.task.HttpLink;
import com.yunji.titanrtx.common.domain.task.Task;
import com.yunji.titanrtx.common.enums.Content;
import com.yunji.titanrtx.common.enums.Method;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.resp.RespCodeOperator;
import com.yunji.titanrtx.common.task.TaskDispatcher;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.plugin.http.AHCBuildTool;
import com.yunji.titanrtx.plugin.http.HttpBuildTool;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;

import org.asynchttpclient.*;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Slf4j
public class AHCExecuteTask extends AbstractHttpExecute {

    private AsyncHttpClient asyncHttpClient;

    private int maxConnection;

    public AHCExecuteTask(TaskDispatcher taskDispatcher,
                          LiaisonService liaisonService,
                          Task task,
                          CollectScheduler collectScheduler, int maxConnection,
                          ParamsService paramsService) throws InterruptedException {
        super(taskDispatcher, liaisonService, task, collectScheduler, paramsService);
        this.maxConnection = maxConnection;
        init();
    }

    @Override
    protected void configObjectPool() {
        objectPool = null;
    }

    @Override
    public void init() throws InterruptedException {
        initTask();
    }

    @Override
    public void doInvoke(Bullet bullet) {
        HttpLink httpLink = (HttpLink) bullet;
        Integer id = bullet.getId();
        //处理url,保存，给后续cgi查询.
        acquireUrl(id, httpLink.getUrl());

        /*String temp = httpLink.getUrl();
        int beginIndex = temp.indexOf("/");
        if (beginIndex > 0) {
            acquireUrl(id, temp.substring(beginIndex));
        }*/

        Method method = httpLink.getMethod();
        String param = getParam(httpLink);
        /*if (param == null) {
            log.warn("Bullet {} param is null.", bullet.getId());
        }*/
        String url = CommonU.buildFullUrl(httpLink.getProtocol().getMemo(), httpLink.getUrl());
        String params = CommonU.splitParams(param);
        String header = CommonU.splitHeader(param);
        String contentMemo = httpLink.getContentType().getMemo();
        String charsetMemo = httpLink.getCharset().getMemo();

        Request request;

        switch (method) {
            case GET:
                request = AHCBuildTool.buildHttpGet(url, params, header, contentMemo, charsetMemo);
                break;
            case POST:
                if (Content.XWWWFORMURLENCODED == httpLink.getContentType()) {
                    request = AHCBuildTool.buildHttpPostForm(url, params, header, charsetMemo);
                } else {
                    //
                    request = AHCBuildTool.buildHttpPost(url, params, header, contentMemo, charsetMemo);
                }
                break;
            default:
                throw new RuntimeException("暂不支持类型: " + method.name());
        }
        try {
            if (asyncHttpClient != null && !asyncHttpClient.isClosed()) {
                //Send period begin.
                CompletableFuture<Response> responseFuture = asyncHttpClient
                        .executeRequest(request)
                        .toCompletableFuture();

                long startTime = System.currentTimeMillis();
                CollectorUtils.collectRequest(openAgentCollectQps, httpLink.getUrl());
                //Send period end.

                responseFuture.whenComplete((result, ex) -> {
                    StatisticsDetail statisticsDetail = collectMap.get(id);
                    if (ex != null) {
                        statisticsDetail.addStatusCode(GlobalConstants.HTTP_ERROR_CODE);
                        if (ex instanceof TimeoutException) { // 超时请求
                            //add response error
                            responseErrorHandle(ex, id, "timeout");
                            CollectorUtils.collectTimeoutResponse(openAgentCollectQps, httpLink.getUrl());
                        } else {
                            //add response error
                            responseErrorHandle(ex, id, "other");
                            CollectorUtils.collectErrorResponse(openAgentCollectQps, httpLink.getUrl(), ex);
                        }
                        //doCompleted
                        countDownLatch.countDown();
                        return;
                    }
                    CollectorUtils.collectSuccessResponse(openAgentCollectQps, result.getStatusCode(), httpLink.getUrl());

                    statisticsDetail.addStatusCode(GlobalConstants.HTTP_SUCCESS_CODE);
                    long duration = System.currentTimeMillis() - startTime;
                    long totalDuration = duration < 0 ? 0 : duration;
                    statisticsDetail.setDuration(statisticsDetail.getDuration() + totalDuration);

                    int logicCode;
                    String content = "{}";
                    try {
                        content = result.getResponseBody(StandardCharsets.UTF_8);
                        logicCode = RespCodeOperator.getRespCode(content);

                        if (logicCode != 0 && task.getAlertHook() != null) {
                            //Newly add in 2020.07.22 如果配置了 AlertHook,就将错误的返回结果返回回去.
                            statisticsDetail.addWrongReturn("参数:[ " + param + "].\n结果: [" + content + "].");
                        }
                    } catch (Exception e) {
                        logicCode = GlobalConstants.YUNJI_ERROR_CODE;
                        //Newly add in 2020.07.22 如果配置了 AlertHook,就将错误的返回结果返回回去.
                        if (task.getAlertHook() != null) {
                            statisticsDetail.addWrongReturn("参数:[ " + param + "].\n结果: [" + content + "].");
                        }
                    }

                    statisticsDetail.addBusiness(logicCode);
                    countDownLatch.countDown();
                });
            }
        } catch (Exception e) {
            log.error("构建请求对象失败...........................:{}, cause: {}", httpLink, e.getMessage());
            e.printStackTrace();
        }
    }

    private void responseErrorHandle(Throwable ex, Integer id, String status) {
        log.debug("Error ({}) response link: ({}) cause: ({})", status, id, ex.getMessage(), ex);
    }

    // 获取http url

    /**
     * 去掉域名,只获取域名后的一级url
     * eg: HTTPS://logistics-do-dev.yunjiglobal.com/yunjilogisticsapp/app/getNewRecommendCommodityList.json
     * 得到 /yunjilogisticsapp/app/getNewRecommendCommodityList.json
     */
    private void acquireUrl(int id, String originUrl) {
        int index = originUrl.indexOf("/");
        if (index > 0) {
            String subUrl = originUrl.substring(index);
            urlIdMap.putIfAbsent(id, subUrl);

            /*String temp = urlIdMap.get(id);
            if (StringUtils.isEmpty(temp)) {
                urlIdMap.put(id, url);
            }*/
        }
    }

    @Override
    public TaskType getType() {
        return TaskType.HTTP;
    }

    @Override
    protected void doClose() {
        super.doClose();
        try {
            asyncHttpClient.close();
        } catch (IOException e) {
            log.error("关闭 asyncHttpClient 异常:{}............", e.getMessage());
        }
    }

    @Override
    protected TaskType getTaskType() {
        return TaskType.HTTP;
    }

    @Override
    protected void prepareHttpClient() {
        log.info("AHCExecuteTask prepareHttpClient, setMaxConnections {} ", maxConnection);
        try {
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

            asyncHttpClient = Dsl.asyncHttpClient(
                    Dsl.config()
                            //第一阶段
                            .setMaxConnections(maxConnection)
//                            .setMaxConnectionsPerHost(maxConnection)
                            .setKeepAlive(true)
                            .setSslContext(sslContext)
                            .setConnectTimeout(HttpBuildTool.CONNECT_TIMEOUT)
                            .setRequestTimeout(HttpBuildTool.REQUEST_TIMEOUT)
            );
        } catch (SSLException e) {
            log.error("PrepareHttpClient got error, cause: " + e.getMessage(), e);
        }
    }
}
