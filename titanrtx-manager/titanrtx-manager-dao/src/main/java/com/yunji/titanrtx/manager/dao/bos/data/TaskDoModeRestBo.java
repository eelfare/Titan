package com.yunji.titanrtx.manager.dao.bos.data;

import com.yunji.titanrtx.common.enums.Content;
import com.yunji.titanrtx.common.enums.Method;
import lombok.Data;

/**
 * 任务执行方式为rest的配置信息
 *
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:17
 * @Version 1.0
 */
@Data
public class TaskDoModeRestBo {
    private String url;

    private Method method;

    private Content contentType;
}
