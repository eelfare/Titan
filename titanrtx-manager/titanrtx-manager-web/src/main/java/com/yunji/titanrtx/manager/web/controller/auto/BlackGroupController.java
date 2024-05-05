package com.yunji.titanrtx.manager.web.controller.auto;

import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.bos.top.BlackGroupBo;
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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("blackGroup")
public class BlackGroupController {

    @Resource
    BlackGroupService service;

    @Resource
    FilterService filterService;

    @RequestMapping("list.query")
    public RespMsg list() {
        return RespMsg.respSuc(service.selectAll());
    }


    @RequestMapping("info.query")
    public RespMsg info(Integer id) {
        BlackGroupBo bo = new BlackGroupBo();
        BlackGroupEntity blackGroupEntity = service.findById(id);
        List<FilterEntity> filters = getFilterEntities(blackGroupEntity.getId());
        bo.setBlackGroupEntity(blackGroupEntity);
        bo.setList(filters);
        return RespMsg.respSuc(bo);
    }


    private List<FilterEntity> getFilterEntities(int groupId) {
        return filterService.findByGroupId(groupId);
    }

    @RequestMapping("addOrUpdateRecord.do")
    public RespMsg addOrUpdateRules(@RequestBody BlackGroupEntity entity) {
        Integer id = entity.getId();
        if (id == null) {
            if (service.findByName(entity.getName())) {
                return RespMsg.respErr("分组名称不能重复");
            }
            service.insert(entity);
        } else {
            service.update(entity);
        }
        return RespMsg.respSuc();
    }

    @RequestMapping("search.query")
    public RespMsg search(String key) {
        if (StringUtils.isEmpty(key)) return list();
        return RespMsg.respSuc(service.search(key));
    }


    @RequestMapping("delete.do")
    public RespMsg deleteRules(Integer id) {
        List<FilterEntity> listFilter = filterService.findByGroupId(id);
        for (FilterEntity filter:listFilter) {
            filterService.deleteById(filter.getId());
        }
        return RespMsg.respSuc(service.deleteById(id));
    }
}
