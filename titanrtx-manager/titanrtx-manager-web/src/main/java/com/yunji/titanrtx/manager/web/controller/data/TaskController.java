package com.yunji.titanrtx.manager.web.controller.data;

import com.yunji.titanrtx.common.enums.OutputSource;
import com.yunji.titanrtx.common.enums.OutputType;
import com.yunji.titanrtx.common.enums.TaskDoMode;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.entity.data.TaskEntity;
import com.yunji.titanrtx.manager.dao.entity.data.TaskOutputDeployEntity;
import com.yunji.titanrtx.manager.dao.entity.data.TaskParamDeployEntity;
import com.yunji.titanrtx.manager.service.data.TaskBatchService;
import com.yunji.titanrtx.manager.service.data.TaskOutputDeployService;
import com.yunji.titanrtx.manager.service.data.TaskParamDeployService;
import com.yunji.titanrtx.manager.service.data.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-30 17:22
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("task")
public class TaskController {

    @Resource(name = "Task")
    TaskService taskService;

    @Resource
    TaskParamDeployService taskParamDeployService;

    @Resource
    TaskOutputDeployService taskOutputDeployService;

    @Resource
    TaskBatchService taskBatchService;

    @RequestMapping("list.query")
    public RespMsg list() {
        List<TaskEntity> data = taskService.selectAll();
        for (TaskEntity task : data) {
            List<TaskParamDeployEntity> params = taskParamDeployService.selectByTaskId(task.getId());
            List<TaskOutputDeployEntity> outputs = taskOutputDeployService.selectByTaskId(task.getId());
            task.setListParamDeploy(params);
            task.setListOutputDeploy(outputs);
        }
        return RespMsg.respSuc(data);
    }

    @RequestMapping("find.query")
    public RespMsg find(Integer id) {
        return RespMsg.respSuc(taskService.findById(id));
    }


    @RequestMapping("search.query")
    public RespMsg search(String key) {
        if (StringUtils.isEmpty(key)) return list();
        int id;
        try {
            id = Integer.parseInt(key);
        } catch (NumberFormatException e) {
            return RespMsg.respSuc(taskService.searchTasks(key));
        }
        List<TaskEntity> tasks = new ArrayList<>();
        TaskEntity taskEntity = taskService.findById(id);
        if (taskEntity != null) {
            tasks.add(taskEntity);
        }
        return RespMsg.respSuc(tasks);
    }


    @RequestMapping("info.query")
    public RespMsg info(Integer id) {
        TaskEntity task = taskService.findById(id);
        return RespMsg.respSuc(task);
    }


    @RequestMapping("addOrUpdateParamDeploy.do")
    public RespMsg addOrUpdateParamDeploy(@RequestBody TaskParamDeployEntity taskParamDeployEntity) {
        if (taskParamDeployEntity == null || taskParamDeployEntity.getTaskId() == null
                || StringUtils.isEmpty(taskParamDeployEntity.getName())) {
            return RespMsg.respErr("参数不能为空");
        }
        Integer id = taskParamDeployEntity.getId();
        // 获取任务信息
        TaskEntity task = taskService.findById(taskParamDeployEntity.getTaskId());
        if (id == null) {
            taskParamDeployService.insert(taskParamDeployEntity);
            if (task != null && task.getIsTarget()) {
                TaskOutputDeployEntity output = new TaskOutputDeployEntity();
                output.setTaskId(task.getId());
                output.setName(taskParamDeployEntity.getName());
                output.setType(OutputType.STRING);
                output.setSource(OutputSource.PARAM);
                output.setExpr(taskParamDeployEntity.getId() + "");
                taskOutputDeployService.insert(output);
            }
        } else {
            taskParamDeployService.update(taskParamDeployEntity);
        }
        return RespMsg.respSuc();
    }

    @RequestMapping("queryParamDeploy.query")
    public RespMsg queryParamDeploy(Integer taskId) {
        return RespMsg.respSuc(taskParamDeployService.selectByTaskId(taskId));
    }

    @RequestMapping("paramDeployDelete.do")
    public RespMsg paramDeployDelete(Integer id) {
        // 获取任务信息
        TaskParamDeployEntity param = taskParamDeployService.findById(id);
        TaskEntity task = taskService.findById(param.getTaskId());
        if (task != null && task.getIsTarget()) {
            // 删除对应的ouput信息
            taskOutputDeployService.deleteByTaskIdAndExpr(task.getId(), param.getId() + "");
        }
        return RespMsg.respCom(taskParamDeployService.deleteById(id));
    }


    @RequestMapping("addOrUpdateOutputDeploy.do")
    public RespMsg addOrUpdateOutputDeploy(@RequestBody TaskOutputDeployEntity taskOutputDeployEntity) {
        if (taskOutputDeployEntity == null || taskOutputDeployEntity.getTaskId() == null
                || StringUtils.isEmpty(taskOutputDeployEntity.getName())) {
            return RespMsg.respErr("参数不能为空");
        }
        Integer id = taskOutputDeployEntity.getId();
        if (id == null) {
            taskOutputDeployService.insert(taskOutputDeployEntity);
        } else {
            taskOutputDeployService.update(taskOutputDeployEntity);
        }
        return RespMsg.respSuc();
    }


    @RequestMapping("queryOutputDeploy.query")
    public RespMsg queryOutputDeploy(Integer taskId) {
        return RespMsg.respSuc(taskOutputDeployService.selectByTaskId(taskId));
    }

    @RequestMapping("outputInfo.query")
    public RespMsg outputInfo(Integer id) {
        if (id == null) {
            TaskOutputDeployEntity entity = new TaskOutputDeployEntity();
            entity.setSource(OutputSource.PARAM);
            entity.setType(OutputType.STRING);
            return RespMsg.respSuc(entity);
        }
        return RespMsg.respSuc(taskOutputDeployService.selectById(id));
    }

    @RequestMapping("outputDeployDelete.do")
    public RespMsg outputDeployDelete(Integer id) {
        return RespMsg.respCom(taskOutputDeployService.deleteById(id));
    }

    @RequestMapping("addOrUpdate.do")
    public RespMsg addOrUpdate(@RequestBody TaskEntity task) {
        if (task.getDoMode() == TaskDoMode.DUBBO) {
            if (!dubboPreCheck(task)) {
                if (task.getIsTarget()) {
                    return RespMsg.respErr("Dubbo目标任务,url配置有误,参考 ${service}/${method}/${version} 格式.");
                }
                return RespMsg.respErr("Dubbo url配置错误，需要 paramType,params,address等信息.");
            }
        }

        Integer id = task.getId();
        if (id == null) {
            taskService.insert(task);
        } else {
            taskService.update(task);
        }
        return RespMsg.respSuc(task.getId());
    }

    @RequestMapping("delete.do")
    public RespMsg delete(Integer id) {
        // 判断是否被使用为目标任务
        if (taskBatchService.usedTargetTask(id)) return RespMsg.respSuc("已经被使用为目标任务,不可删除");
        taskOutputDeployService.deleteAllByTaskId(id);
        taskParamDeployService.deleteAllByTaskId(id);
        return RespMsg.respCom(taskService.deleteById(id));
    }

    private boolean dubboPreCheck(TaskEntity task) {
        String urlStr = task.getUrl();
        log.info("Dubbo urlStr is {}", urlStr);

        if (task.getIsTarget()) {
            // -> com.yunji.service.Service/hello/1.0.0
            String[] configs = urlStr.split("/");
            if (configs.length != 3) {
                return false;
            }
            return true;
        }
        return urlStr.contains("paramType") &&
                urlStr.contains("params") &&
                urlStr.contains("address");

    }


    @RequestMapping("autoCreate.do")
    public RespMsg delete(String url, String socketListString) {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(socketListString)) {
            return RespMsg.respErr("数据不能为空");
        }
        String[] commands = socketListString.split(",");
        for (String command : commands) {
            // 搜索是否存在相同指令的任务
            String fullUrl = url + "?data=" + command;
            TaskEntity taskEntity = taskService.findByUrl(fullUrl);
            if (null == taskEntity) {
                taskEntity = new TaskEntity();
                taskEntity.setName("init_db_" + command);
                taskEntity.setDoMode(TaskDoMode.SOCKET);
                taskEntity.setTableName("clonedata");
                taskEntity.setUrl(fullUrl);
                taskEntity.setIsTarget(false);
                taskService.insert(taskEntity);
            }
        }
        return RespMsg.respSuc();
    }
}
