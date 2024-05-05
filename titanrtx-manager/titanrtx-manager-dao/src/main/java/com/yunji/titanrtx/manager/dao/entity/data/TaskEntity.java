package com.yunji.titanrtx.manager.dao.entity.data;

import com.yunji.titanrtx.common.enums.Content;
import com.yunji.titanrtx.common.enums.Method;
import com.yunji.titanrtx.common.enums.ParamMode;
import com.yunji.titanrtx.common.enums.TaskDoMode;
import com.yunji.titanrtx.manager.dao.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;

import java.util.List;

/**
 * 任务信息
 *
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 16:10
 * @Version 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskEntity extends BaseEntity {
    private String name;                    // 任务名称
    private String tableName;               // 任务执行结果保存的表名
    private TaskDoMode doMode;              // 执行方式
    private String url;
    private Method method;
    private Content contentType;
    private Boolean isTarget; // 是否是目标任务
    private ParamMode paramMode; // 目标任务参数的使用方式
    private String template; // 数据模板
    private Integer status = 0; // 数据构造状态

    @Transient
    private List<TaskParamDeployEntity> listParamDeploy; //参数配置信息列表
    @Transient
    private List<TaskOutputDeployEntity> listOutputDeploy; //输出配置信息列表
}
