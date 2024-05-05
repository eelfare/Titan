package com.yunji.titanrtx.manager.dao.bos.data;

import com.yunji.titanrtx.manager.dao.entity.data.TaskEntity;
import com.yunji.titanrtx.manager.dao.entity.data.TaskOutputDeployEntity;
import lombok.Data;

/** 参数关联的数据库配置信息
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 12:35
 * @Version 1.0
 */
@Data
public class SQLDeployBo {
    private TaskEntity taskEntity; // 关联的具体任务
    private TaskOutputDeployEntity columnOutput; // 关联数据库对应的输出字段信息
    private TaskOutputDeployEntity filter; // 关联数据可对应的过滤输出字段信息
    private boolean selectAll = false; // 是否需要把其他关联字段拉出
}
