package com.yunji.titanrtx.manager.web.controller;

import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.service.TaskService;
import com.yunji.titanrtx.manager.service.dubbo.DubboSceneService;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("progress/")
public class ProgressController {

    @Resource
    private HttpSceneService httpSceneService;

    @Resource
    private DubboSceneService dubboSceneService;

    @Resource
    private TaskService  taskService;


    @RequestMapping("state.query")
    public RespMsg progress(Integer id,TaskType type){
        switch (type){
            case HTTP:
                HttpSceneEntity httpSceneEntity = httpSceneService.findById(id);
                if (httpSceneEntity.getStatus() != 1){
                    return RespMsg.respErr("当前场景未正在压测中");
                }
                break;
            case DUBBO:
                DubboSceneEntity dubboSceneEntity = dubboSceneService.findById(id);
                if (dubboSceneEntity.getStatus() != 1){
                    return RespMsg.respErr("当前场景未正在压测中");
                }
                break;
            default:
                return RespMsg.respErr("暂未支持类型");
        }
        return RespMsg.respSuc(taskService.progress(id,type));
    }




}
