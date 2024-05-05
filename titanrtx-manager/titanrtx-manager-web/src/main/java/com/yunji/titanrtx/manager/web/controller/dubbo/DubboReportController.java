package com.yunji.titanrtx.manager.web.controller.dubbo;

import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.entity.dubbo.DubboSceneEntity;
import com.yunji.titanrtx.manager.service.dubbo.DubboReportService;
import com.yunji.titanrtx.manager.service.dubbo.DubboSceneService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;

@RestController
@RequestMapping("/dubboReport")
public class DubboReportController {


    @Resource
    private DubboReportService dubboReportService;

    @Resource
    private DubboSceneService dubboSceneService;


    @RequestMapping("list.query")
    public RespMsg list(){
        return RespMsg.respSuc(dubboReportService.selectAll());
    }

    @RequestMapping("search.query")
    public RespMsg search(String key){
        if (StringUtils.isEmpty(key)) RespMsg.respSuc(dubboReportService.selectAll());
        int id ;
        try {
            id = Integer.parseInt(key);
        }catch (NumberFormatException e){
            return RespMsg.respSuc(dubboReportService.searchSceneName(key));
        }
        DubboSceneEntity dubboSceneEntity = dubboSceneService.findById(id);
        if (null == dubboSceneEntity)return RespMsg.respSuc(new ArrayList<>());
        return RespMsg.respSuc(dubboReportService.selectBySceneId(dubboSceneEntity.getId()));
    }


    @RequestMapping("detail.query")
    public RespMsg detail(Integer id){
        return RespMsg.respSuc(dubboReportService.findById(id));
    }


    @RequestMapping("delete.do")
    public RespMsg delete(Integer id){
        return RespMsg.respCom(dubboReportService.deleteById(id));
    }




}
