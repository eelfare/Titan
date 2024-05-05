package com.yunji.titanrtx.manager.web.controller.http;

import com.github.pagehelper.PageInfo;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.ParamsBo;
import com.yunji.titanrtx.manager.dao.bos.http.LinkBo;
import com.yunji.titanrtx.manager.dao.entity.http.LinkEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkOrderEntity;
import com.yunji.titanrtx.manager.dao.entity.http.LinkParamsEntity;
import com.yunji.titanrtx.manager.service.common.eventbus.EventBusCenter;
import com.yunji.titanrtx.manager.service.common.eventbus.ParamEvent;
import com.yunji.titanrtx.manager.service.http.LinkParamsService;
import com.yunji.titanrtx.manager.service.http.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("link/")
public class LinkController {
    private ExecutorService es = Executors.newFixedThreadPool(5);

    @Resource
    private LinkService linkService;

    @Resource
    private LinkParamsService httpParamsService;

    @RequestMapping("list.query")
    public RespMsg list() {
        return RespMsg.respSuc(linkService.selectAll());
    }

    @RequestMapping("find.query")
    public RespMsg find(Integer id) {
        return RespMsg.respSuc(linkService.findById(id));
    }


    @RequestMapping("search.query")
    public RespMsg search(String key) {
        if (StringUtils.isEmpty(key)) return list();
        int id;
        try {
            id = Integer.parseInt(key);
        } catch (NumberFormatException e) {
            return RespMsg.respSuc(linkService.searchLinks(key));
        }
        List<LinkEntity> links = new ArrayList<>();
        LinkEntity linkEntity = linkService.findById(id);
        if (linkEntity != null) {
            links.add(linkEntity);
        }
        return RespMsg.respSuc(links);
    }

    @RequestMapping("info.query")
    public RespMsg info(Integer id) {
        LinkEntity link = linkService.findById(id);
        PageInfo<LinkParamsEntity> paramsEntityPageInfo = httpParamsService.selectByLinkId(link.getId(), 1);
        LinkBo bo = new LinkBo();
        bo.setLink(link);
        bo.setParams(paramsEntityPageInfo);
        return RespMsg.respSuc(bo);
    }


    /**
     * 链路管理页面添加参数接口.
     */
    @RequestMapping("addParams.do")
    public RespMsg addHttpParams(@RequestBody ParamsBo bo) {
        String params = bo.getParams();
        if (StringUtils.isEmpty(params)) return RespMsg.respErr("参数不能为空");
        String[] line = params.split("\n");
        for (String lineParam : line) {
            try {
                String decodeParams = CommonU.decodeParams(lineParam);
                httpParamsService.insert(new LinkParamsEntity(bo.getId(), decodeParams));
            } catch (UnsupportedEncodingException e) {
                return RespMsg.respErr("导入参数失败:" + e.getMessage());
            }
        }
        EventBusCenter.post(new ParamEvent(bo.getId(), TaskType.HTTP, ParamEvent.Event.ADD));
        return RespMsg.respSuc();
    }


    @RequestMapping("queryParams.query")
    public RespMsg queryHttpParams(Integer linkId, Integer currentPage) {
        Integer cp = currentPage == null ? 1 : currentPage;
        return RespMsg.respSuc(httpParamsService.selectByLinkId(linkId, cp));
    }

    /**
     * 链路管理页面修改 params.
     */
    @RequestMapping("updateParam.do")
    public RespMsg updateHttpParam(Integer paramId, String param) throws UnsupportedEncodingException {
        String decodeParam = CommonU.decodeParams(param);
        LinkParamsEntity paramsEntity = new LinkParamsEntity();
        paramsEntity.setId(paramId);
        paramsEntity.setParam(decodeParam);
        httpParamsService.update(paramsEntity);
        return RespMsg.respSuc();
    }

    /**
     * 链路管理页面删除 params.
     */
    @RequestMapping("paramDelete.do")
    public RespMsg httpParamDelete(Integer id) {
        LinkParamsEntity entity = httpParamsService.findById(id);
        int linkId = entity.getLinkId();

        int result = httpParamsService.deleteById(id);
        EventBusCenter.post(new ParamEvent(linkId, TaskType.HTTP, ParamEvent.Event.DELETE));
        return RespMsg.respCom(result);
    }

    @RequestMapping("clearParam.do")
    public RespMsg clearHttpParam(Integer linkId) {
        return RespMsg.respCom(httpParamsService.deleteAllByLinkId(linkId));
    }


    @RequestMapping("addOrUpdate.do")
    public RespMsg addOrUpdate(@RequestBody LinkEntity link) {
        Integer id = link.getId();
        if (id == null) {
            linkService.insert(link);
            EventBusCenter.post(new ParamEvent(link.getId(), TaskType.HTTP, ParamEvent.Event.INSERT_LINK));
        } else {
            linkService.update(link);
        }
//        EventBusCenter.post(new ParamEvent(link.getId(), ParamEvent.Event.ADD_OR_UPDATE));
        return RespMsg.respSuc(link.getId());
    }

    @RequestMapping("delete.do")
    public RespMsg delete(Integer id) {
        EventBusCenter.post(new ParamEvent(id, TaskType.HTTP, ParamEvent.Event.DELETE));
        int result = linkService.deleteById(id);

        es.execute(() -> {
            try {
                long st = System.currentTimeMillis();
                int paramsCount = httpParamsService.deleteAllByLinkId(id);
                log.info("删除链路:{},级联参数:{},耗时: {} ms", id, paramsCount, (System.currentTimeMillis() - st));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        String msg = String.format("删除链路:%s 成功,结果:%s,异步删除它的参数列表.", id, result);
        log.info(msg);
        return RespMsg.respSuc(msg);
    }

    @RequestMapping("linkParamOrder.query")
    public RespMsg queryLinkParamOrder() {
        long st = System.currentTimeMillis();
        List<LinkEntity> linkEntities = linkService.selectAll();

        List<LinkOrderEntity> linkOrderEntities = new ArrayList<>();
        for (LinkEntity linkEntity : linkEntities) {
            int totalRecords = httpParamsService.findTotalRecordsByLinkId(linkEntity.getId());
            LinkOrderEntity orderEntity = new LinkOrderEntity();
            BeanUtils.copyProperties(linkEntity, orderEntity);
            orderEntity.setCount(totalRecords);

            linkOrderEntities.add(orderEntity);
        }
        linkOrderEntities.sort((o1, o2) -> o2.getCount() - o1.getCount());

        log.info("大链路排行查询结束,数量:{},耗时:{}ms", linkOrderEntities.size(), (System.currentTimeMillis() - st));

        return RespMsg.respSuc(linkOrderEntities);
    }

    @RequestMapping("collectBadParamIds.do")
    public RespMsg collectBadParamIds(int linkId, boolean delete) {
        log.info("链路{}所有参数测试,不合格是否删除:{}", linkId, delete);
        return RespMsg.respSuc(linkService.collectBadParamIds(linkId, delete));
    }

}
