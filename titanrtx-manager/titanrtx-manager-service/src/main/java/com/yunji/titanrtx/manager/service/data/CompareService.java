package com.yunji.titanrtx.manager.service.data;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkParamsEntity;
import com.yunji.titanrtx.manager.service.http.LinkParamsService;
import com.yunji.titanrtx.manager.service.http.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CompareService
 *
 * @author leihz
 * @since 2020-08-28 4:03 下午
 */
@Service
@Slf4j
public class CompareService {
    @Resource
    private LinkParamsService linkParamsService;
    @Resource
    private LinkService linkService;


    public String compare() {
        List<Integer> linkIds = linkService.selectLinkIds();
        List<Integer> linkIdsOfParams = linkParamsService.selectLinkIdsOfParam();

        Set<Integer> linkIdSet = new HashSet<>();
        Set<Integer> linkIdParamsSet = new HashSet<>();
        linkIdSet.addAll(linkIds);
        linkIdParamsSet.addAll(linkIdsOfParams);

        log.info("全量比较 http_link ({}) 和 link_params ({}) link id 数量.", linkIds.size(), linkIdsOfParams.size());

        if (linkIdsOfParams.size() > linkIds.size()) {
            Sets.SetView<Integer> difference = Sets.difference(linkIdParamsSet, linkIdSet);
            log.info("difference:{} ", difference);

            for (Integer linkId : difference) {
                try {
                    log.info("准备删除链路 {}", linkId);
                    int i = linkParamsService.deleteAllByLinkId(linkId);
                    log.info("准备删除链路 {} ok, 条数:{}", linkId, i);
                } catch (Exception e) {
                    log.error("删除linkId:{} 异常 " + e.getMessage(), linkId, e);
                }
            }
        }

        return "OK";
    }

    public String deleteEmptyParamLinks() {
        List<Integer> ids = linkService.selectLinkIds();
        for (Integer id : ids) {
            PageInfo<LinkParamsEntity> linkParamsEntityPageInfo = linkParamsService.selectByLinkId(id, 10);
            if (linkParamsEntityPageInfo.getSize() == 0) {
                log.info("链路 {} 参数为空,删除之.", id);
                linkService.deleteById(id);
            }
        }
        return "OK";
    }
}
