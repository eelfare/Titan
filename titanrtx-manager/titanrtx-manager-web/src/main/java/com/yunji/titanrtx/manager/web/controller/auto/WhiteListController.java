package com.yunji.titanrtx.manager.web.controller.auto;

import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.entity.auto.FilterEntity;
import com.yunji.titanrtx.manager.service.auto.WhiteListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("whiteList/")
public class WhiteListController {

    @Resource
    WhiteListService service;

    @RequestMapping("list.query")
    public RespMsg list() {
        return RespMsg.respSuc(service.selectAll());
    }


    @RequestMapping("info.query")
    public RespMsg info(Integer id) {
        FilterEntity filterEntity;
        if (null == id) {
            filterEntity = new FilterEntity();
            filterEntity.setDomain(GlobalConstants.ALL);
            filterEntity.setPath(GlobalConstants.ALL);
        } else {
            filterEntity = service.findById(id);
        }
        return RespMsg.respSuc(filterEntity);
    }

    @RequestMapping("addOrUpdateRecord.do")
    public RespMsg addOrUpdateRecord(@RequestBody FilterEntity filterEntity) {
        Integer id = filterEntity.getId();
        if (id == null) {
            service.insert(filterEntity);
        } else {
            service.update(filterEntity);
        }
        return RespMsg.respSuc();
    }


    @RequestMapping("delete.do")
    public RespMsg deleteRules(Integer id) {
        return RespMsg.respSuc(service.deleteById(id));
    }
}
