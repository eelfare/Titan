package com.yunji.titanrtx.manager.web.controller.auto;

import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.CommanderService;
import com.yunji.titanrtx.manager.dao.bos.top.TopLinkAsFilterBo;
import com.yunji.titanrtx.manager.dao.bos.top.TurnSceneBo;
import com.yunji.titanrtx.manager.dao.entity.auto.BlackGroupEntity;
import com.yunji.titanrtx.manager.dao.entity.auto.FilterEntity;
import com.yunji.titanrtx.manager.service.SceneOperatingCenterService;
import com.yunji.titanrtx.manager.service.auto.BlackGroupService;
import com.yunji.titanrtx.manager.service.auto.FilterService;
import com.yunji.titanrtx.manager.service.auto.WhiteListService;
import com.yunji.titanrtx.manager.service.http.LinkParamsService;
import com.yunji.titanrxt.plugin.influxdb.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("autoTop/")
public class AutoTopController {

    @Resource
    private StoreService storeService;

    @Resource
    private FilterService filterService;

    @Resource
    private LinkParamsService httpParamsService;

    @Resource
    private BlackGroupService blackGroupService;

    @Resource
    private WhiteListService whiteListService;

    @Resource
    SceneOperatingCenterService sceneCreateService;

    @Resource
    CommanderService commanderService;


    @RequestMapping("list.query")
    public RespMsg list() {
        List<TopLinkAsFilterBo> topLink = sceneCreateService.queryTipsTopLinks();
        return RespMsg.respSuc(topLink);
    }

    @RequestMapping("addBlack.do")
    public RespMsg addBlack(String domain, String path) {
        List<TopLinkAsFilterBo> topLink = sceneCreateService.queryTipsTopLinks();
        for (TopLinkAsFilterBo link : topLink) {
            if (StringUtils.compare(link.getDomain(), domain) == 0
                    && StringUtils.compare(link.getPath(), path) == 0) {
                BlackGroupEntity blackGroupEntity = blackGroupService.selectAll().get(0);
                FilterEntity filter = new FilterEntity();
                filter.setGroupId(blackGroupEntity.getId());
                filter.setDomain(link.getDomain());
                filter.setPath(link.getPath());
                filterService.insert(filter);
                link.setBlnBlack(true);
                break;
            }
        }
        sceneCreateService.addTipsTopLinks(topLink);
        return RespMsg.respSuc(topLink);
    }


    @RequestMapping("addWhite.do")
    public RespMsg addWhite(String domain, String path) {
        List<TopLinkAsFilterBo> topLink = sceneCreateService.queryTipsTopLinks();
        for (TopLinkAsFilterBo link : topLink) {
            if (StringUtils.compare(link.getDomain(), domain) == 0
                    && StringUtils.compare(link.getPath(), path) == 0) {
                FilterEntity filter = new FilterEntity();
                filter.setDomain(link.getDomain());
                filter.setPath(link.getPath());
                whiteListService.insert(filter);
                link.setBlnWhite(true);
                break;
            }
        }
        sceneCreateService.addTipsTopLinks(topLink);
        return RespMsg.respSuc(topLink);
    }


    @RequestMapping("remove.do")
    public RespMsg remove(String domain, String path) {
        List<TopLinkAsFilterBo> topLink = sceneCreateService.queryTipsTopLinks();
        for (TopLinkAsFilterBo link : topLink) {
            if (StringUtils.compare(link.getDomain(), domain) == 0
                    && StringUtils.compare(link.getPath(), path) == 0) {
                topLink.remove(link);
                break;
            }
        }
        sceneCreateService.addTipsTopLinks(topLink);
        return RespMsg.respSuc(topLink);
    }

    @RequestMapping("confirm.do")
    public RespMsg confirm(@RequestBody TurnSceneBo bo) {
        List<String> paths = bo.getUrl();
        List<TopLinkAsFilterBo> topLink = sceneCreateService.queryTipsTopLinks();
        topLink = topLink.stream().filter(link -> paths.contains(link.getDomain() + link.getPath())).collect(Collectors.toList());
        if (sceneCreateService.topLinkToAutoScene(topLink)) {
            sceneCreateService.deleteTips();
        }
        return RespMsg.respSuc();
    }

    @RequestMapping("top300Switch.do")
    public RespMsg top300Switch(Boolean topSwitch) {
        if (topSwitch == null) {
            return RespMsg.respErr();
        }
        Boolean result = commanderService.top300StressSwitch(topSwitch);
        if (result == null) {
            return RespMsg.respErr();
        }
        return RespMsg.respSuc();
    }

    @RequestMapping("topStress.query")
    public RespMsg getTop300Switch() {
        return RespMsg.respSuc(commanderService.getTop300StressSwitch());
    }
}
