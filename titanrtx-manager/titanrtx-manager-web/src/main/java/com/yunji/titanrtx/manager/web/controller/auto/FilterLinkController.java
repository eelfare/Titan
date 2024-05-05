package com.yunji.titanrtx.manager.web.controller.auto;

import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.entity.auto.BlackGroupEntity;
import com.yunji.titanrtx.manager.dao.entity.auto.FilterEntity;
import com.yunji.titanrtx.manager.service.auto.BlackGroupService;
import com.yunji.titanrtx.manager.service.auto.FilterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("blackList/")
public class FilterLinkController {

    @Resource
    FilterService filterService;
    @Resource
    BlackGroupService blackGroupService;

    @RequestMapping("list.query")
    public RespMsg list() {
        return RespMsg.respSuc(filterService.selectAll());
    }


    @RequestMapping("info.query")
    public RespMsg info(Integer id, Integer groupId, String groupName) {
        FilterEntity filterEntity;
        if (null == id) {
            filterEntity = new FilterEntity();
            filterEntity.setDomain(GlobalConstants.ALL);
            filterEntity.setPath(GlobalConstants.ALL);
            filterEntity.setGroupId(groupId);
            filterEntity.setGroupName(groupName);
        } else {
            filterEntity = filterService.findById(id);
        }
        return RespMsg.respSuc(filterEntity);
    }

    @RequestMapping("addOrUpdateRecord.do")
    public RespMsg addOrUpdateRecord(@RequestBody FilterEntity filterEntity) {
        Integer groupId = filterEntity.getGroupId();
        if (groupId == null && StringUtils.isNotEmpty(filterEntity.getGroupName())) {
            // 先创建group
            BlackGroupEntity entity = new BlackGroupEntity();
            entity.setName(filterEntity.getGroupName());
            if (blackGroupService.findByName(entity.getName())) {
                return RespMsg.respErr("分组名称不能重复");
            }
            blackGroupService.insert(entity);
            groupId = entity.getId();
            filterEntity.setGroupId(groupId);
        }
        Integer id = filterEntity.getId();
        if (id == null) {
            filterService.insert(filterEntity);
        } else {
            filterService.update(filterEntity);
        }
        return RespMsg.respSuc(groupId);
    }


    @RequestMapping("delete.do")
    public RespMsg delete(Integer id) {
        return RespMsg.respSuc(filterService.deleteById(id));
    }
}
