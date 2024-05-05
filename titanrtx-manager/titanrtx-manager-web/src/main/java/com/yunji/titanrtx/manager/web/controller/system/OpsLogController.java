package com.yunji.titanrtx.manager.web.controller.system;

import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.service.system.OpsLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("opsLog/")
public class OpsLogController {


    @Resource
    private OpsLogService opsLogService;


    @RequestMapping("list.query")
    public RespMsg list(){
        return RespMsg.respSuc(opsLogService.selectAll());
    }


    @RequestMapping("search.query")
    public RespMsg search(String key){
        if (StringUtils.isEmpty(key))return list();

        return RespMsg.respSuc(opsLogService.selectByUserName(key));
    }



}
