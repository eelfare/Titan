package com.yunji.titanrtx.manager.dao.bos.data.template;

import com.yunji.titanrtx.common.enums.Method;
import com.yunji.titanrtx.manager.dao.bos.data.TaskDoModeRestBo;
import lombok.Data;

/**
 * 任务执行体描述
 *
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 17:13
 * @Version 1.0
 */
@Data
public class Executor {
    /**
     * name,获取到的结果通过此name作为key,存储在上下文中
     */
    private String name;
    /**
     * 任务执行的方式
     */
    private String executorType;
    /**
     * 执行体配置信息
     */
    private TaskDoModeRest doMode;
    /**
     * 参数表达式组
     */
    private String params;

    @Data
    static class TaskDoModeRest {

        private String url;

        private Method method;

        private String contentType;
    }

    public void setDoMode(TaskDoModeRestBo bo) {
        doMode = new TaskDoModeRest();
        doMode.setUrl(bo.getUrl());
        doMode.setContentType(bo.getContentType() != null ? bo.getContentType().getMemo() : "");
        doMode.setMethod(bo.getMethod());
    }
}