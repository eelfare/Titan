package com.yunji.titanrtx.manager.web.controller.data;

import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.enums.*;
import com.yunji.titanrtx.common.message.ErrorMsg;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.u.CollectionU;
import com.yunji.titanrtx.manager.dao.bos.data.BatchBo;
import com.yunji.titanrtx.manager.dao.bos.data.TaskSourceBo;
import com.yunji.titanrtx.manager.dao.entity.data.BatchEntity;
import com.yunji.titanrtx.manager.dao.entity.data.TaskEntity;
import com.yunji.titanrtx.manager.dao.entity.data.TaskParamSourceEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceParamsEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkParamsEntity;
import com.yunji.titanrtx.manager.service.BatchCenterService;
import com.yunji.titanrtx.manager.service.WebSocketService;
import com.yunji.titanrtx.manager.service.data.*;
import com.yunji.titanrtx.manager.service.dubbo.DubboServiceService;
import com.yunji.titanrtx.manager.service.dubbo.ServiceParamsService;
import com.yunji.titanrtx.manager.service.http.LinkParamsService;
import com.yunji.titanrtx.manager.service.http.LinkService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Transient;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 17:22
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("batch")
public class BatchController {

    @Resource
    TaskBatchService taskBatchService;
    @Resource(name = "Task")
    TaskService taskService;
    @Resource
    TaskParamSourceService taskParamSourceService;
    @Resource
    TaskParamDeployService taskParamDeployService;
    @Resource
    TaskOutputDeployService taskOutputDeployService;

    @Value(value = "${yj.data.factory.addr}")
    String yjDataFactoryAddr;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    private LinkService linkService;
    @Resource
    private DubboServiceService dubboServiceService;
    @Resource
    private LinkParamsService linkParamsService;

    private ServiceParamsService serviceParamsService;

    @Value(value = "${export_link_max_thread_num:20}")
    private int exportLinkMaxThreadNum;
    @Value(value = "${export_link_batch_num:10000}")
    private int exportLinkBatchNum;

    @Resource
    BatchCenterService batchCenterService;

    @RequestMapping("list.query")
    public RespMsg list() {
        List<BatchEntity> list = taskBatchService.selectAll();
        list.stream().filter(batchEntity -> ((batchEntity.getStatus() == 0
                || batchEntity.getStatus() == 1
                || batchEntity.getStatus() == 2)) && batchEntity.getTarget() != null).forEach(batchEntity -> {
            Integer target = batchEntity.getTarget();
            TaskEntity taskEntity = taskService.findById(target);
            String tableName = GlobalConstants.TABEL_PREFIX + taskEntity.getTableName();
            Connection connection = null;
            DataSource dataSource = null;
            PreparedStatement statement = null;
            try {
                dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
                connection = DataSourceUtils.getConnection(dataSource);
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet tables = metaData.getTables(null, null, tableName, null);
                if (tables.next()) {
                    statement = connection.prepareStatement("select count(*) from " + tableName);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        batchEntity.setDataTotal(count);
                    }
                } else {
                    batchEntity.setDataTotal(0);
                }
                taskBatchService.updateDataTotal(batchEntity);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    DataSourceUtils.releaseConnection(connection, dataSource);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return RespMsg.respSuc(taskBatchService.selectAll());
    }


    @RequestMapping("addOrUpdate.do")
    public RespMsg addOrUpdate(@RequestBody BatchEntity taskBatchEntity) {
        Integer id = taskBatchEntity.getId();
        if (id == null) {
            taskBatchService.insert(taskBatchEntity);
        } else {
            taskBatchService.update(taskBatchEntity);
        }

        // 需要添加任务参数元配置
        TaskParamSourceEntity paramSource = new TaskParamSourceEntity();
        Integer batchId = taskBatchEntity.getId();
        paramSource.setBatchId(batchId);
        String tasks = taskBatchEntity.getTasks();
        String[] split = tasks.split(",");
        // 查找目前已经存在的参数配置信息
        List<TaskParamSourceEntity> existParamSource = taskParamSourceService.findByBatchId(batchId);
        for (TaskParamSourceEntity entity : existParamSource) {
            boolean has = false;
            for (String taskId : split) {
                if (StringUtils.compare(taskId, entity.getTaskId() + "") == 0) {
                    has = true;
                    break;
                }
            }
            if (!has) {
                taskParamSourceService.deleteById(entity.getId());
            }
        }
        for (String item : split) {
            Integer taskId = Integer.valueOf(item);
            TaskParamSourceEntity temp =
                    taskParamSourceService.findByBatchIdAndTaskId(batchId, taskId);
            if (temp == null) {
                temp = new TaskParamSourceEntity();
                temp.setBatchId(batchId);
                temp.setTaskId(taskId);
                temp.setParamsSource("");
                taskParamSourceService.insert(temp);
            }
        }
        return RespMsg.respSuc(taskBatchEntity.getId());
    }

    @RequestMapping("search.query")
    public RespMsg search(String key) {
        if (StringUtils.isEmpty(key)) {
            return list();
        }
        int id;
        try {
            id = Integer.parseInt(key);
        } catch (NumberFormatException e) {
            return RespMsg.respSuc(taskBatchService.searchBatchs(key));
        }
        List<BatchEntity> list = new ArrayList<>();
        BatchEntity batchEntity = taskBatchService.findById(id);
        if (batchEntity != null) {
            list.add(batchEntity);
        } else {
            return RespMsg.respSuc(taskBatchService.searchBatchs(key));
        }
        return RespMsg.respSuc(list);
    }

    @RequestMapping("delete.do")
    public RespMsg delete(Integer id) {
        // 删除参数配置信息数据
        taskParamSourceService.deleteByBatchId(id);
        return RespMsg.respCom(taskBatchService.deleteById(id));
    }

    @RequestMapping("info.query")
    public RespMsg info(Integer id) {
        BatchBo bo = new BatchBo();
        BatchEntity taskBatch = taskBatchService.findById(id);
        // 获取目标任务
        if (taskBatch.getTarget() != null) {
            TaskEntity targetTask = taskService.findById(taskBatch.getTarget());
            bo.setTargetTask(targetTask);
        }
        // 获取任务清单
        String tasks = taskBatch.getTasks();
        boolean hasTaskDeleted = false;
        List<TaskEntity> taskEntities = new ArrayList<>();
        if (StringUtils.isNotEmpty(tasks)) {
            String[] listTask = tasks.split(",");
            for (String taskId : listTask) {
                TaskEntity task = taskService.findById(Integer.valueOf(taskId));
                if (task == null) {
                    hasTaskDeleted = true;
                    continue;
                }
                taskEntities.add(task);
            }
        }

        bo.setBatchEntity(taskBatch);
        bo.setTaskEntities(taskEntities);
        return hasTaskDeleted ? new RespMsg("存在任务已被删除，不能正常构造数据", 10021, bo) : RespMsg.respSuc(bo);
    }

    @RequestMapping("queryTaskParamSource.query")
    public RespMsg batchWithTask(Integer batchId, Integer taskId) {
        if (batchId == null || taskId == null) {
            TaskSourceBo taskSource = new TaskSourceBo();
            return RespMsg.respSuc(taskSource);
        }
        TaskParamSourceEntity taskParamSourceEntity = taskParamSourceService.findByBatchIdAndTaskId(batchId, taskId);
        TaskSourceBo taskSource = batchCenterService.getTaskSource(taskParamSourceEntity);
        return RespMsg.respSuc(taskSource);
    }

    @RequestMapping("addOrUpdateParamSource.do")
    public RespMsg addOrUpdateParamSource(@RequestBody TaskParamSourceEntity entity) {
        Integer id = entity.getId();
        if (id == null) {
            taskParamSourceService.insert(entity);
        } else {
            taskParamSourceService.update(entity);
        }
        return RespMsg.respSuc();
    }

    @RequestMapping("start.do")
    public RespMsg start(Integer id) {
        if (id == null || id < 0) {
            return RespMsg.respErr(ErrorMsg.PARAMS_INPUT_ERROR);
        }
        try {
            batchCenterService.start(id);
        } catch (Exception e) {
            return RespMsg.respErr(e.getMessage());
        }
        return RespMsg.respSuc();
    }

    @RequestMapping("export.do")
    public RespMsg export(Integer batchId) {
        // 1、获取批次信息
        BatchEntity batchEntity = taskBatchService.findById(batchId);
        // 2、获取目标任务
        Integer target = batchEntity.getTarget();
        TaskEntity task = taskService.findById(target);
        String templateString = task.getTemplate();
        if (StringUtils.isEmpty(templateString)) {
            return RespMsg.respErr("模板数据为空");
        }

        // 设置为导出状态
        BatchEntity batch = taskBatchService.findById(batchId);
        if (batch.getStatus() == 2 || batch.getStatus() == 6 || batch.getStatus() == 4) {
            batch.setStatus(5);
            taskBatchService.update(batch);
        } else {
            return RespMsg.respErr("当前状态有误，请刷新");
        }
        System.out.println(Thread.currentThread().getName());
        Thread thread = new Thread(() -> asyncExport(batch, task, templateString));
        thread.setName("other-yj-async");
        thread.start();

        return RespMsg.respSuc();
    }

    private void asyncExport(BatchEntity batch, TaskEntity targetTask, String templateString) {
        String tableName = GlobalConstants.TABEL_PREFIX + targetTask.getTableName();
        // 创链路
//        Integer linkId = createLink(targetTask.getUrl(), targetTask.getTableName(), targetTask.getParamMode());
        //1.使用任务名作为链路名
        Integer id = createTargetId(targetTask);
        if (id == null) {
            log.info("链路或dubbo服务创建失败");
            // 更改批次执行状态
            batch.setStatus(6);
            taskBatchService.update(batch);
            WebSocketService.sendMessage("数据导出失败");
            return;
        }
        // 6、插入数据
        batchInsertLinkParams(targetTask.getDoMode(), batch, tableName, templateString, id);
    }

    /**
     * 批次插入链路参数
     *
     * @param batch
     * @param tableName
     * @param templateString
     * @param id
     */
    private void batchInsertLinkParams(TaskDoMode doMode, BatchEntity batch, String tableName, String templateString, Integer id) {
        log.info("BatchInsertLinkParams doMode:{},tableName:{},templateString:{},id:{}",
                doMode, tableName, templateString, id);

        // 获取表中数据条数
        int size = getParamCount(tableName);
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(exportLinkMaxThreadNum / 4);
        threadPool.setMaxPoolSize(exportLinkMaxThreadNum);
        threadPool.setQueueCapacity(Integer.MAX_VALUE);
        threadPool.setKeepAliveSeconds(1);
        threadPool.initialize();
        int groupSize = exportLinkBatchNum; // 每次处理条数
        long startTime = System.currentTimeMillis();
        int totalPage = size / groupSize + 1;
        for (int index = 0; index < totalPage; index++) {
            int start = index * groupSize;
            int end = (start + groupSize) <= size ? start + groupSize : size;

            if (start != end) {
                log.info("开始组装数据,tableName:{},start:{},end:{}", tableName, start, end);
                threadPool.execute(() -> {
                    log.info(Thread.currentThread().getName() + "开始组装数据,tableName:{},start:{},end:{}", tableName, start, end);
                    // 执行数据准备
                    List<String> params = getParamsLimit(tableName, templateString, start, end);
//                log.info("搜索位置 start ={} end = {},length = {}",start,end,params.size());
                    if (CollectionU.isEmpty(params)) {
                        log.info("模板数据组装失败，cause tableName:{} 查询参数为空，start:{},end:{}", tableName, start, end);
                        // 更改批次执行状态
                        batch.setStatus(6);
                        taskBatchService.update(batch);
                        WebSocketService.sendMessage("数据导出失败");
                        threadPool.shutdown(); // 关停所有线程
                        return;
                    }
                    switch (doMode) {
                        case HTTP: {
                            List<LinkParamsEntity> list = new ArrayList<>();
                            params.stream().forEach(param -> {
                                LinkParamsEntity entity = new LinkParamsEntity();
                                entity.setLinkId(id);
                                entity.setParam(param);
                                list.add(entity);
                            });
                            try {
                                linkParamsService.batchInsertList(list);
                            } catch (Exception e) {
                                log.error("批量导出参数时异常,list size:{}, cause:{} ", list.size(), e.getMessage(), e);
                            }
                        }
                        break;
                        case DUBBO: {
                            List<ServiceParamsEntity> list = new ArrayList<>();
                            params.stream().forEach(param -> {
                                ServiceParamsEntity entity = new ServiceParamsEntity();
                                entity.setServiceId(id);
                                entity.setParam(param);
                                list.add(entity);
                            });
                            try {
                                serviceParamsService.batchInsertList(list);
                            } catch (Exception e) {
                                log.error("批量导出参数时异常,list size:{}, cause:{} ", list.size(), e.getMessage(), e);
                            }
                        }
                        break;
                        default:
                    }

                    if (end == size) {
                        performEnd(batch, threadPool, startTime);
                    }
                });
            }
        }
    }

    /**
     * 执行收尾
     *
     * @param batch
     * @param threadPool
     * @param startTime
     */
    private void performEnd(BatchEntity batch, ThreadPoolTaskExecutor threadPool, long startTime) {
        // 更改批次执行状态
        batch.setStatus(4);
        taskBatchService.update(batch);
        WebSocketService.sendMessage("数据导成功");
        log.info("当前批次：{} 导出所花时间{}", batch.getId(), System.currentTimeMillis() - startTime);
        threadPool.setWaitForTasksToCompleteOnShutdown(true); // 优雅关闭
        threadPool.shutdown(); // 关停所有线程
    }

    /**
     * 获取表格数据条数
     *
     * @param tableName
     * @return
     */
    private int getParamCount(String tableName) {
        Connection connection = null;
        DataSource dataSource = null;
        PreparedStatement statement = null;
        try {
            dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
            connection = DataSourceUtils.getConnection(dataSource);
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName, null);
            if (tables.next()) {
                statement = connection.prepareStatement("select count(*) from " + tableName);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    private Integer createTargetId(TaskEntity taskEntity) {
        String name = taskEntity.getName();
        String url = taskEntity.getUrl();
        ParamMode mode = taskEntity.getParamMode();
        TaskDoMode doMode = taskEntity.getDoMode();

        Integer id = null;
        switch (doMode) {
            case HTTP: {
                LinkEntity entity = new LinkEntity();
                entity.setName(name);
                entity.setUrl(url);
                entity.setProtocol(Protocol.HTTPS);
                entity.setCharset(Charset.UTF8);
                entity.setContentType(Content.JSON);
                entity.setMethod(Method.GET);
                entity.setParamMode(mode);
                linkService.insert(entity);
                id = entity.getId();
            }
            break;
            case DUBBO: {
                ServiceEntity entity = new ServiceEntity();
                // 解析dubbo url  完整示例：com.yunji.stock.biz.api.IWarehouseStockWriteService/reloadWarehouseStockToCache/1.0.0?paramType=java.util.List&address=10.0.104.147:29945
                // 服务名
                if (url.contains("/")) {
                    String[] split = url.split("/", 3);
                    String serviceName = split[0];
                    if (StringUtils.isEmpty(split[0])) {
                        return null;
                    }

                    String methodName = split[1];
                    if (StringUtils.isEmpty(methodName)) {
                        return null;
                    }
                    entity.setMethodName(methodName);

                    String version = split[2];
                    if (StringUtils.isEmpty(version)) {
                        return null;
                    }
                    if (version.contains("?")) {
                        String[] versionSplit = version.split("\\?", 2);
                        serviceName = serviceName + ":" + versionSplit[0];
                    }
                    entity.setServiceName(serviceName);
                } else {
                    log.info("dubbo 目标任务 url 配置有误 {}", url);
                    return null;
                }
                entity.setName(name);
                entity.setApplicationName(name);
                entity.setParamMode(mode);
                entity.setRpcContent("shadow=true");
                dubboServiceService.insert(entity);
                id = entity.getId();
            }
            break;
            default:
        }
        return id;
    }

    /**
     * 通过数据模板与数据得到最终的参数组合
     *
     * @param templateString
     * @param data
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    private String createContent(String templateString, Map data) throws IOException, TemplateException {
        //创建配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //获取模板内容
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateString);
        configuration.setTemplateLoader(stringTemplateLoader);
        Template template = configuration.getTemplate("template", "utf-8");
        //静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, data);
        return content;
    }

    /**
     * 获取参数
     *
     * @param tableName
     * @param templateString
     * @return
     */
    private List<String> getParamsLimit(String tableName, String templateString, int start, int end) {
        List<String> params = new ArrayList<>();
        PreparedStatement statement = null;
        Connection connection = null;
        DataSource dataSource = null;
        try {
            dataSource = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
            connection = DataSourceUtils.getConnection(dataSource);
            statement = connection.prepareStatement("select * from " + tableName + " where id > " + start + " and id <= " + end);

            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<String> columnNames = new ArrayList<>(columnCount);
            for (int column = 1; column <= columnCount; column++) {
                String columnName = metaData.getColumnName(column);
                if (!StringUtils.equalsIgnoreCase(columnName, "id")
                        && !StringUtils.equalsIgnoreCase(columnName, "createTime")
                        && !StringUtils.equalsIgnoreCase(columnName, "updateTime")) {
                    columnNames.add(columnName);
                }
            }

            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                columnNames.stream().forEach(key -> {
                    try {
                        String value = resultSet.getString(key);
                        map.put(key, value);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
                // 数据与模板结合
                String content = createContent(templateString, map);
                params.add(content);
            }
            resultSet.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                DataSourceUtils.releaseConnection(connection, dataSource);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return params;
    }

    /**
     * 重置批次信息，将会把批次状态重置，相关数据表删除，相关任务状态重置
     *
     * @param batchId
     * @return
     */
    @Transient
    @RequestMapping("resetBatch.do")
    public RespMsg resetBatch(Integer batchId) {
        if (batchId == null || batchId < 0) {
            return RespMsg.respErr("输入参数有误");
        }
        return batchCenterService.reset(batchId) ? RespMsg.respSuc() : RespMsg.respErr("批次充值失败");
    }


    @RequestMapping("moveTask.do")
    public RespMsg moveTask(String batchId, String taskId, int type) {
        if (StringUtils.isEmpty(batchId) || StringUtils.isEmpty(taskId)) {
            return RespMsg.respErr("数据错误");
        }
        BatchEntity batchEntity = taskBatchService.findById(Integer.valueOf(batchId));
        String tasks = batchEntity.getTasks();
        List<String> listTemp = Arrays.asList(tasks.split(","));
        ArrayList<String> listTask = new ArrayList<>(listTemp);
        if (!listTask.contains(taskId)) {
            listTask.add(taskId);
        }
        int size = listTask.size();
        for (int index = 0; index < size; index++) {
            String temp = listTask.get(index);
            if (StringUtils.compare(temp, taskId) == 0) {
                if (type == 1 && index == 0) { // 第一个点上移报错
                    return RespMsg.respErr("不能上移");
                } else if (type == 0 && index == size - 1) { // 最后一个点下移报错
                    return RespMsg.respErr("不能下移");
                } else {
                    String tempId = listTask.get(type == 1 ? index - 1 : index + 1);
                    listTask.set(type == 1 ? index - 1 : index + 1, temp);
                    listTask.set(index, tempId);
                    break;
                }
            }
        }

        String collect = listTask.stream().collect(Collectors.joining(","));

        batchEntity.setTasks(collect);
        taskBatchService.update(batchEntity);
        return RespMsg.respSuc();
    }
}
