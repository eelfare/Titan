package com.yunji.titanrtx.manager.tool;

import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.enums.OutputSource;
import com.yunji.titanrtx.common.enums.OutputType;
import com.yunji.titanrtx.common.enums.TaskDoMode;
import com.yunji.titanrtx.manager.dao.bos.data.ParamSourceBo;
import com.yunji.titanrtx.manager.dao.bos.data.SQLDeployBo;
import com.yunji.titanrtx.manager.dao.bos.data.TaskDoModeRestBo;
import com.yunji.titanrtx.manager.dao.bos.data.TaskSourceBo;
import com.yunji.titanrtx.manager.dao.bos.data.template.*;
import com.yunji.titanrtx.manager.dao.entity.data.TaskEntity;
import com.yunji.titanrtx.manager.dao.entity.data.TaskOutputDeployEntity;
import com.yunji.titanrtx.manager.dao.entity.data.TaskParamDeployEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2020-01-16 15:05
 * @Version 1.0
 */
@Slf4j
public final class JSONTemplateTool {
    /**
     * 获取任务名称
     *
     * @param taskSourceBo
     * @return
     */
    public static String getTaskName(TaskSourceBo taskSourceBo) {
        if (taskSourceBo == null) return null;
        TaskEntity task = taskSourceBo.getTaskEntity();
        if (task == null) return null;
        return task.getName();
    }

    /**
     * 获取任务参数配置
     *
     * @param taskSourceBo
     * @return
     */
    public static List<Param> getTaskParams(TaskSourceBo taskSourceBo) {
        List<Param> listParam = new ArrayList<>();
        List<ParamSourceBo> paramSourceBos = taskSourceBo.getParamSourceBos();

        Set<String> allTables = new HashSet<>();

        if (paramSourceBos == null) return null;
        for (ParamSourceBo paramSourceBo : paramSourceBos) {
            Param param = new Param();
            param.setName(paramSourceBo.getParamName());
            param.setUseType(paramSourceBo.getType());

            SQLDeployBo sqlDeployBo = paramSourceBo.getSqlDeployBo();
            if (sqlDeployBo == null) return null;
            TaskEntity task = sqlDeployBo.getTaskEntity();
            if (task == null) return null;
            if (allTables.contains(task.getTableName())) {
                continue;
            } else {
                allTables.add(task.getTableName());
            }

            // 过滤条件组
            TaskOutputDeployEntity filterDeploy = sqlDeployBo.getFilter();
            if (filterDeploy != null) {
                Filter filter = new Filter();
                filter.setName(filterDeploy.getName());
                StringBuilder builder = new StringBuilder();
                builder.append(filterDeploy.getSource().getMemo()).append(".").append(filterDeploy.getName());
                filter.setExpr(builder.toString());
                param.setFilter(filter);
            }

            StringBuilder iteratorExpr = new StringBuilder();
            TaskOutputDeployEntity columnOutput = sqlDeployBo.getColumnOutput();
            if (sqlDeployBo.isSelectAll()) {
                iteratorExpr = iteratorExpr.append("table(\"").append(GlobalConstants.TABEL_PREFIX).append(task.getTableName()).append("\")");
                if (param.getFilter() != null) {
                    iteratorExpr.append(".filter(\"").append(param.getName()).append("\").mapAll");
                } else {
                    iteratorExpr.append(".mapAll");
                }
            } else {
                iteratorExpr = iteratorExpr.append("table(\"").append(GlobalConstants.TABEL_PREFIX).append(task.getTableName()).append("\")");
                if (param.getFilter() != null) {
                    iteratorExpr.append(".filter(\"")
                            .append(param.getName())
                            .append("\").map(_.get[String](\"")
                            .append(columnOutput.getName()).append("\"))");
                } else {
                    iteratorExpr.append(".map(_.get[String](\"")
                            .append(columnOutput.getName()).append("\"))");
                }
            }
            param.setIteratorExpr(iteratorExpr.toString());

            listParam.add(param);
        }
        return listParam;
    }


    /**
     * 获取任务执行体
     *
     * @param taskSourceBo
     * @return
     */
    public static Executor getExecutor(TaskSourceBo taskSourceBo, Integer batchId, Date doTime) throws UnsupportedEncodingException {
        Executor executor = new Executor();
        if (taskSourceBo == null) return null;
        List<ParamSourceBo> paramSourceBos = taskSourceBo.getParamSourceBos();

        if (paramSourceBos == null) return null;
        TaskEntity task = taskSourceBo.getTaskEntity();
        if (task == null) return null;

        StringBuilder params = new StringBuilder();
        StringBuilder fixeds = new StringBuilder();
        if (!task.getIsTarget()) {
            for (ParamSourceBo paramSourceBo : paramSourceBos) {
                params.append(paramSourceBo.getParamName())
                        .append("->")
                        .append(OutputSource.PARAM.getMemo())
                        .append(".")
                        .append(paramSourceBo
                                .getSqlDeployBo()
                                .getColumnOutput()
                                .getName()).append(",");
            }

        }

        TaskDoMode doMode = task.getDoMode();
        executor.setName(task.getTableName() + "Result");
        if (task.getIsTarget()) {
            executor.setExecutorType("COMBINATION");
        } else {
            executor.setExecutorType(doMode.getMemo());
            switch (doMode) {
                case HTTP: {
                    TaskDoModeRestBo restBo = new TaskDoModeRestBo();
                    restBo.setMethod(task.getMethod());
                    restBo.setContentType(task.getContentType());
                    String url = task.getUrl();
                    if (StringUtils.isNotEmpty(fixeds)) {
                        url = url + "?" + fixeds.substring(0, fixeds.length() - 1);
                    }
                    restBo.setUrl("http://" + url);
                    executor.setDoMode(restBo);
                }
                break;
                case DUBBO: {
                    TaskDoModeRestBo restBo = new TaskDoModeRestBo();
                    String toUrl = task.getUrl();
                    restBo.setUrl("dubbo://" + toUrl);
                    executor.setDoMode(restBo);
                }
                break;
                case SOCKET: {
                    TaskDoModeRestBo restBo = new TaskDoModeRestBo();
                    String toUrl = task.getUrl();
                    String[] split = toUrl.split("=");
                    StringBuilder data = new StringBuilder();
                    data.append(task.getId()).append("_").append(doTime.getTime() / 1000).append(":").append(batchId).append(doTime.getTime() / 1000).append("#").append(split[1]);
                    toUrl = split[0] + "=" + URLEncoder.encode(data.toString(), GlobalConstants.URL_DECODER);
                    restBo.setUrl("socket://" + toUrl);
                    executor.setDoMode(restBo);
                }
                break;
                case FILE: {
                    TaskDoModeRestBo restBo = new TaskDoModeRestBo();
                    restBo.setContentType(task.getContentType());
                    String url = task.getUrl();
                    if (StringUtils.isNotEmpty(fixeds)) {
                        url = url + "?" + fixeds.substring(0, fixeds.length() - 1);
                    }
                    restBo.setUrl("http://" + url);
                    executor.setDoMode(restBo);
                }
                break;
                default:
                    throw new IllegalArgumentException("DoModel not supported " + doMode);
            }
        }
        if (StringUtils.isNotEmpty(params)) {
            executor.setParams(params.substring(0, params.length() - 1));
        }
        return executor;
    }


    /**
     * 获取输出规则
     *
     * @param taskSourceBo
     * @return
     */
    public static Output getOutput(TaskSourceBo taskSourceBo) {
        Output output = new Output();

        if (taskSourceBo == null) return null;
        TaskEntity task = taskSourceBo.getTaskEntity();
        List<ParamSourceBo> paramSourceBos = taskSourceBo.getParamSourceBos();
        if (task == null) return null;
        output.setTable(GlobalConstants.TABEL_PREFIX + task.getTableName());

        List<Column> columns = new ArrayList<>();
        if (task.getIsTarget()) {
            List<TaskParamDeployEntity> params = task.getListParamDeploy();
            if (params == null) return null;
            for (int index = 0; index < params.size(); index++) {
                TaskParamDeployEntity paramDeployEntity = params.get(index);
                Column column = new Column();
                column.setName(paramDeployEntity.getName());
                StringBuilder sourceExpr = new StringBuilder()
                        .append(OutputSource.PARAM.getMemo())
                        .append(".").append(paramDeployEntity.getName());
                column.setSourceExpr(sourceExpr.toString());
                columns.add(column);
            }
        } else {
            List<TaskOutputDeployEntity> outputs = task.getListOutputDeploy();
            if (outputs == null) return null;
            for (TaskOutputDeployEntity outputDeployEntity : outputs) {
                Column column = new Column();
                column.setName(outputDeployEntity.getName());

                if (outputDeployEntity.getType() == null){
                    column.setSourceExpr(OutputSource.IGNORE.getMemo());
                } else if (outputDeployEntity.getType() == OutputType.STRING) {
                    StringBuilder sourceExpr = new StringBuilder();
                    if (outputDeployEntity.getSource() == OutputSource.PARAM) {
                        taskSourceBo.getTaskEntity().getListParamDeploy().stream().filter(paramDeploy -> (paramDeploy.getId() == Integer.valueOf(outputDeployEntity.getExpr()))).forEach(param -> {
                            sourceExpr.append(OutputSource.PARAM.getMemo()).append(".").append(param.getName());
                        });
                    } else {
                        sourceExpr.append(OutputSource.RESPONSE.getMemo()).append(".").append(outputDeployEntity.getExpr());
                    }
                    column.setSourceExpr(sourceExpr.toString());

                } else {
                    if (outputDeployEntity.getSource() == OutputSource.RESPONSE) {
                        StringBuilder iterableExpr = new StringBuilder().append(OutputSource.RESPONSE.getMemo()).append(".").append(outputDeployEntity.getExpr());
                        column.setIteratorExpr(iterableExpr.toString());
                    }
                }
                columns.add(column);
            }
        }
        output.setFields(columns);
        return output;
    }


}
