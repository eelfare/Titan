package com.yunji.titanrtx.manager.service;

import com.alibaba.fastjson.JSONObject;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.enums.OutputSource;
import com.yunji.titanrtx.common.enums.TaskDoMode;
import com.yunji.titanrtx.manager.dao.bos.data.ParamSourceBo;
import com.yunji.titanrtx.manager.dao.bos.data.SQLDeployBo;
import com.yunji.titanrtx.manager.dao.bos.data.TaskSourceBo;
import com.yunji.titanrtx.manager.dao.bos.data.template.Executor;
import com.yunji.titanrtx.manager.dao.bos.data.template.Output;
import com.yunji.titanrtx.manager.dao.bos.data.template.Param;
import com.yunji.titanrtx.manager.dao.bos.data.template.TaskTemplateBo;
import com.yunji.titanrtx.manager.dao.entity.data.*;
import com.yunji.titanrtx.manager.service.data.TaskService;
import com.yunji.titanrtx.manager.service.data.*;
import com.yunji.titanrtx.manager.tool.JSONTemplateTool;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jingf
 */
@Slf4j
public class BatchCenterServiceImpl implements BatchCenterService {
    @Resource
    TaskBatchService taskBatchService;
    @Resource(name = "Task")
    TaskService taskService;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Value(value = "${yj.data.factory.addr}")
    String yjDataFactoryAddr;
    @Resource
    TaskParamSourceService taskParamSourceService;
    @Resource
    TaskParamDeployService taskParamDeployService;
    @Resource
    TaskOutputDeployService taskOutputDeployService;

    @Override
    public void start(Integer batchId) throws Exception {
        @Data
        class TasksResponse {
            List<TaskTemplateBo> tasks;
            Integer batchId;
        }
        TasksResponse tasksResponse = new TasksResponse();
        List<TaskTemplateBo> tasks = new ArrayList<>();
        BatchEntity batchEntity = taskBatchService.findById(batchId);
        batchEntity.setDoTime(new Date());
        if (batchEntity.getTarget() == null) {
            throw new Exception("当前批次没有目标任务，暂不能构建数据");
        }
        // 获取批次中的所有任务信息
        List<TaskSourceBo> tasksSource = getBatchAllTask(batchEntity);
        if (tasksSource == null) {
            throw new Exception("存在任务已被删除，不能正常构造数据");
        }

        // 重置批次信息
        reset(batchId);

        // 转换成模板数据
        for (TaskSourceBo taskSourceBo : tasksSource) {
            TaskTemplateBo taskTemplateBo = new TaskTemplateBo();
            // 设置顺序
            taskTemplateBo.setOrder(tasksSource.indexOf(taskSourceBo));
            // 任务名
            String taskName = JSONTemplateTool.getTaskName(taskSourceBo);
            if (StringUtils.isEmpty(taskName)) {
                log.info("解析任务名称失败：{}", taskSourceBo);
                throw new Exception("任务执行失败");
            }
            taskTemplateBo.setName(taskName);

            // 任务参数
            List<Param> taskParams = JSONTemplateTool.getTaskParams(taskSourceBo);
            if (taskParams == null) {
                log.info("解析任务参数失败：{}", taskSourceBo);
                throw new Exception("任务执行失败,请检查参数配置信息");
            }
            taskTemplateBo.setParams(taskParams);

            // 任务执行体
            Executor executor = null;
            try {
                executor = JSONTemplateTool.getExecutor(taskSourceBo, batchEntity.getId(), batchEntity.getDoTime());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (executor == null) {
                log.info("解析任务执行体失败：{}", taskSourceBo);
                throw new Exception("解析任务执行体失败");
            }
            taskTemplateBo.setExecutor(executor);

            // 任务保存规则
            Output output = JSONTemplateTool.getOutput(taskSourceBo);
            if (output == null) {
                log.info("解析任务保存规则失败：{}", taskSourceBo);
                throw new Exception("解析任务保存规则失败");
            }
            taskTemplateBo.setOutput(output);
            tasks.add(taskTemplateBo);
        }
        tasksResponse.setTasks(tasks);
        tasksResponse.setBatchId(batchId);
        String responseJson = JSONObject.toJSONString(tasksResponse);
        // 将生产的json传给scala模块进行解析
        executeHttpSend(GlobalConstants.DATA_FACTORY_EXECUTE, responseJson, batchEntity);
        // 更改当前批次的状态
        batchEntity.setStatus(1);
        taskBatchService.update(batchEntity);
    }

    @Override
    public boolean reset(Integer batchId) {
        if (batchId == null) {
            return false;
        }
        // 1、获取批次信息
        BatchEntity taskBatch = taskBatchService.findById(batchId);
        Connection connection = null;
        DataSource dataSource = null;
        List<TaskEntity> listTaskEntry = new ArrayList<>();
        try {
            dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
            connection = DataSourceUtils.getConnection(dataSource);
            DatabaseMetaData metaData = connection.getMetaData();
            // 获取任务清单
            String tasks = taskBatch.getTasks();
            if (StringUtils.isNotEmpty(tasks)) {
                String[] listTask = tasks.split(",");

                for (String taskId : listTask) {
                    TaskEntity task = taskService.findById(Integer.valueOf(taskId));
                    // 3、删除相关表
                    String tableName = GlobalConstants.TABEL_PREFIX + task.getTableName();
                    ResultSet tables = metaData.getTables(null, null, tableName, null);

                    List<TaskOutputDeployEntity> ignoreParamList = task.getListOutputDeploy()
                            .stream()
                            .filter(x -> x.getSource() == OutputSource.IGNORE)
                            .collect(Collectors.toList());

                    int ignoreParamsSize = ignoreParamList.size();

                    log.info(".......... Task {} ignoreParamsSize {}..........", task.getName(), ignoreParamsSize);

                    listTaskEntry.add(task);
                    if (ignoreParamsSize == 0 && tables.next()) {
                        Statement statement = connection.createStatement();
                        statement.executeUpdate("drop table " + tableName);
                    }
                    // 4、重置任务状态
                    task.setStatus(0);
                    taskService.update(task);
                }
            }
            // 重置socket任务
            resetSocket(listTaskEntry, taskBatch);
            // 重置批次状态
            taskBatch.setStatus(0);
            taskBatch.setDoTime(null);
            taskBatchService.update(taskBatch);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    // 重置socket任务
    private void resetSocket(List<TaskEntity> listTaskEntry, BatchEntity batchEntity) throws UnsupportedEncodingException {
        Date doTime = batchEntity.getDoTime();
        if (doTime == null) {
            log.info("批次已经是初始状态，不需要重置了");
            return;
        }
        List<String> listData = new ArrayList<>();
        listTaskEntry.stream().filter(task -> task.getDoMode() == TaskDoMode.SOCKET).forEach(taskEntity -> {
            String toUrl = taskEntity.getUrl();
            String[] split = toUrl.split("=");
            StringBuilder data = new StringBuilder();
            data.append(taskEntity.getId()).append("_").append(doTime.getTime() / 1000).append(":").append(0).append("#").append(split[1]);
            listData.add(data.toString());
        });
        if (CollectionUtils.isEmpty(listData)) {
            return;
        }
        String params = listData.stream().collect(Collectors.joining(","));
        @Data
        class MyData {
            String data;
        }
        MyData data = new MyData();
        data.setData(URLEncoder.encode(params, GlobalConstants.URL_DECODER));
        executeHttpSend(GlobalConstants.DATA_FACTORY_RESETTASK, JSONObject.toJSONString(data), null);
    }

    private void executeHttpSend(String path, String params, BatchEntity batchEntity) {
        log.info("发送给data-factory的数据为：{}", params);
        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();
        HttpPost httpPost = new HttpPost(yjDataFactoryAddr + path);
        httpPost.addHeader("Content-type", "application/json; charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        StringEntity stringEntity = new StringEntity(params, StandardCharsets.UTF_8);
        httpPost.setEntity(stringEntity);
        client.execute(httpPost, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                // 更改当前批次的状态
//                batchEntity.setStatus(2);
//                taskBatchService.update(batchEntity);
//                WebSocketService.sendMessage("数据构造执行完成");
            }

            @Override
            public void failed(Exception ex) {
                log.info("执行失败:{}", ex);
                switch (path) {
                    case GlobalConstants.DATA_FACTORY_EXECUTE:
                        batchEntity.setStatus(3);
                        taskBatchService.update(batchEntity);
                        WebSocketService.sendMessage("数据构造执行失败");
                        break;
                    case GlobalConstants.DATA_FACTORY_RESETTASK:
                        break;
                }
            }

            @Override
            public void cancelled() {
                switch (path) {
                    case GlobalConstants.DATA_FACTORY_EXECUTE:
                        batchEntity.setStatus(0);
                        taskBatchService.update(batchEntity);
                        break;
                    case GlobalConstants.DATA_FACTORY_RESETTASK:
                        break;
                }
            }
        });
    }


    /**
     * 获取批次中的任务清单列表
     *
     * @param batch
     * @return
     */
    private List<TaskSourceBo> getBatchAllTask(BatchEntity batch) {
        List<TaskSourceBo> result = new ArrayList<>();
        String[] listTask = batch.getTasks().split(",");
        for (String taskId : listTask) {
            TaskParamSourceEntity paramSourceEntity = taskParamSourceService.findByBatchIdAndTaskId(batch.getId(), Integer.valueOf(taskId));
            TaskSourceBo taskSource = getTaskSource(paramSourceEntity);
            if (taskSource == null) {
                return null;
            }
            result.add(taskSource);
        }
        return result;
    }


    /**
     * 获取任务完整的配置信息
     *
     * @param paramSourceEntity
     * @return
     */
    @Override
    public TaskSourceBo getTaskSource(TaskParamSourceEntity paramSourceEntity) {
        TaskSourceBo taskSourceBo = new TaskSourceBo();
        taskSourceBo.setId(paramSourceEntity.getId());
        Integer taskId = paramSourceEntity.getTaskId();
        // 获取任务信息
        TaskEntity taskEntity = taskService.findById(taskId);
        if (taskEntity == null) {
            return null;
        }
        List<TaskParamDeployEntity> taskParams = taskParamDeployService.selectByTaskId(taskId);
        List<TaskOutputDeployEntity> taskOutputs = taskOutputDeployService.selectByTaskId(taskId);

        taskEntity.setListParamDeploy(taskParams);
        taskEntity.setListOutputDeploy(taskOutputs);

        String paramsSource = paramSourceEntity.getParamsSource();
        List<ParamSourceBo> paramSourceBos = getParamSourceBos(taskParams, paramsSource);

        // 获取批次当前拥有的任务
        BatchEntity batchEntity = taskBatchService.findById(paramSourceEntity.getBatchId());
        String tasks = batchEntity.getTasks();
        String[] split = tasks.split(",");
        List<TaskEntity> listTask = new ArrayList<>();
        for (String temp : split) {
            Integer id = Integer.valueOf(temp);
            if (id.equals(taskId)) {
                continue;
            }

            TaskEntity task = taskService.findById(id);

            List<TaskOutputDeployEntity> listOutputDeploy = task.getListOutputDeploy();
            task.setListOutputDeploy(listOutputDeploy);

            listTask.add(task);
        }
        taskSourceBo.setBatchOwnTask(listTask);

        taskSourceBo.setTaskEntity(taskEntity);
        taskSourceBo.setParamSourceBos(paramSourceBos);
        return taskSourceBo;
    }

    private List<ParamSourceBo> getParamSourceBos(List<TaskParamDeployEntity> taskParams, String paramsSource) {
        List<ParamSourceBo> paramSourceBos = new ArrayList<>();
        if (StringUtils.isNotEmpty(paramsSource)) {
            for (TaskParamDeployEntity param : taskParams) {
                String[] listParamsource = paramsSource.split(",");
                boolean has = false;
                for (String paramSource : listParamsource) {
                    String[] deploy = paramSource.split("&");
                    if (StringUtils.equalsIgnoreCase(param.getName(), deploy[0])) {
                        ParamSourceBo paramSourceBo = new ParamSourceBo();
                        paramSourceBo.setParamName(deploy[0]);
                        paramSourceBo.setType(Integer.valueOf(deploy[1]));

                        SQLDeployBo sqlDeployBo = new SQLDeployBo();
                        Integer cTaskId = Integer.valueOf(deploy[3]);
                        TaskEntity cTask = taskService.findById(cTaskId);
                        List<TaskParamDeployEntity> taskParamDeployEntities = taskParamDeployService.selectByTaskId(cTaskId);
                        List<TaskOutputDeployEntity> taskOutputDeployEntities = taskOutputDeployService.selectByTaskId(cTaskId);
                        cTask.setListParamDeploy(taskParamDeployEntities);
                        cTask.setListOutputDeploy(taskOutputDeployEntities);
                        sqlDeployBo.setTaskEntity(cTask);
                        // 查找对应的output deploy
                        sqlDeployBo.setColumnOutput(selectOutputDeployWithName(taskOutputDeployEntities, deploy[4]));

                        paramSourceBo.setSqlDeployBo(sqlDeployBo);
                        sqlDeployBo.setSelectAll(StringUtils.equalsIgnoreCase(deploy[2], "1") ? true : false);

                        // 解析过滤信息
                        int size = (deploy.length - 5);
                        for (int index = 0; index < size; index++) {
                            String filterName = deploy[5 + index];
                            sqlDeployBo.setFilter(selectOutputDeployWithName(taskOutputDeployEntities, filterName));
                            break;
                        }
                        paramSourceBos.add(paramSourceBo);
                        has = true;
                        break;
                    }
                }
                if (!has) {
                    ParamSourceBo paramSourceBo = new ParamSourceBo();
                    paramSourceBo.setParamName(param.getName());
                    paramSourceBo.setSqlDeployBo(new SQLDeployBo());
                    paramSourceBos.add(paramSourceBo);
                }
            }
        } else {
            for (TaskParamDeployEntity taskParam : taskParams) {
                ParamSourceBo paramSourceBo = new ParamSourceBo();
                paramSourceBo.setParamName(taskParam.getName());
                paramSourceBo.setSqlDeployBo(new SQLDeployBo());
                paramSourceBos.add(paramSourceBo);
            }
        }
        return paramSourceBos;
    }


    private TaskOutputDeployEntity selectOutputDeployWithName
            (List<TaskOutputDeployEntity> taskOutputDeployEntities, String name) {
        for (TaskOutputDeployEntity outputDeployEntity : taskOutputDeployEntities) {
            if (outputDeployEntity.getName().equals(name)) {
                return outputDeployEntity;
            }
        }
        return null;
    }
}
