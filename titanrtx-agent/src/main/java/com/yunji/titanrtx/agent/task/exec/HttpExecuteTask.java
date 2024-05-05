package com.yunji.titanrtx.agent.task.exec;

import com.yunji.titanrtx.agent.collect.CollectScheduler;
import com.yunji.titanrtx.agent.collect.CollectorUtils;
import com.yunji.titanrtx.agent.future.core.HttpFutureCallBack;
import com.yunji.titanrtx.agent.service.LiaisonService;
import com.yunji.titanrtx.agent.service.ParamsService;
import com.yunji.titanrtx.agent.task.exec.http.AbstractHttpExecute;
import com.yunji.titanrtx.common.domain.task.Bullet;
import com.yunji.titanrtx.common.domain.task.HttpLink;
import com.yunji.titanrtx.common.domain.task.Task;
import com.yunji.titanrtx.common.enums.Content;
import com.yunji.titanrtx.common.enums.Method;
import com.yunji.titanrtx.common.enums.ParamMode;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.task.TaskDispatcher;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.plugin.http.HttpAsyncClientTool;
import com.yunji.titanrtx.plugin.http.HttpBuildTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import java.io.IOException;

@Slf4j
public class HttpExecuteTask extends AbstractHttpExecute {

    private CloseableHttpAsyncClient asyncClient;

    public HttpExecuteTask(TaskDispatcher taskDispatcher,
                           LiaisonService liaisonService,
                           Task task,
                           CollectScheduler collectScheduler, ParamsService paramsService) throws InterruptedException {
        super(taskDispatcher, liaisonService, task, collectScheduler, paramsService);
        init();
    }

    @Override
    public void init() throws InterruptedException {
        initTask();
    }

    @Override
    public void doInvoke(Bullet bullet) {
        HttpLink httpLink = (HttpLink) bullet;
        Integer id = bullet.getId();

        Method method = httpLink.getMethod();
        HttpUriRequest request;
        String param = httpLink.getParamMode() == ParamMode.RANDOM ?
                randomParam(httpLink.getParams()) : getParam(httpLink);

        String url = CommonU.buildFullUrl(httpLink.getProtocol().getMemo(), httpLink.getUrl());
        String params = CommonU.splitParams(param);
        String header = CommonU.splitHeader(param);
        String contentMemo = httpLink.getContentType().getMemo();
        String charsetMemo = httpLink.getCharset().getMemo();
        switch (method) {
            case GET:
                request = HttpBuildTool.buildHttpGet(url, params, header, contentMemo, charsetMemo);
                break;
            case POST:
                if (Content.XWWWFORMURLENCODED == httpLink.getContentType()) {
                    request = HttpBuildTool.buildHttpPostForm(url, params, header, charsetMemo);
                } else {
                    request = HttpBuildTool.buildHttpPost(url, params, header, contentMemo, charsetMemo);
                }
                break;
            default:
                throw new RuntimeException("暂不支持类型");
        }
        try {
            HttpFutureCallBack future = objectPool.borrowObject();
            future.init(countDownLatch, collectMap.get(id), objectPool, httpLink.getUrl());
            if (asyncClient != null && asyncClient.isRunning()) {
                asyncClient.execute(request, future);
                future.start();
                CollectorUtils.collectRequest(openAgentCollectQps, httpLink.getUrl());
            }
        } catch (Exception e) {
            log.error("构建请求对象失败...........................:{}", httpLink);
            e.printStackTrace();
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
            asyncClient.close();
        } catch (IOException e) {
            log.error("关闭asyncClient异常:{}............", e.getMessage());
        }
    }

    @Override
    protected TaskType getTaskType() {
        return TaskType.HTTP;
    }

    @Override
    protected void prepareHttpClient() {
        asyncClient = HttpAsyncClientTool.createAsyncClient();
        asyncClient.start();
    }

    @Override
    protected void configObjectPool() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal((int) task.getConcurrent() * 10);
        poolConfig.setMinIdle((int) task.getConcurrent());
        objectPool = new GenericObjectPool<>(objectFactory(), poolConfig);
    }

}
