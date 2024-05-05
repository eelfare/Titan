package com.yunji.titanrtx.manager.web.controller.dubbo;

import com.github.pagehelper.PageInfo;
import com.yunji.titanrtx.common.alarm.AlarmService;
import com.yunji.titanrtx.common.alarm.MessageSender;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.ParamsBo;
import com.yunji.titanrtx.manager.dao.bos.dubbo.ServiceBo;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceEntity;
import com.yunji.titanrtx.manager.dao.entity.dubbo.ServiceParamsEntity;
import com.yunji.titanrtx.manager.service.OutService;
import com.yunji.titanrtx.manager.service.common.eventbus.EventBusCenter;
import com.yunji.titanrtx.manager.service.common.eventbus.ParamEvent;
import com.yunji.titanrtx.manager.service.dubbo.DubboServiceService;
import com.yunji.titanrtx.manager.service.dubbo.ServiceParamsService;
import com.yunji.titanrtx.manager.service.support.CommonUtils;
import com.yunji.titanrtx.manager.web.config.SystemConfig;
import com.yunji.titanrtx.manager.web.support.u.RequestU;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("service/")
public class DubboServiceController {

    @Resource
    private DubboServiceService dubboServiceService;

    @Resource
    private ServiceParamsService serviceParamsService;

    @Resource
    private OutService outService;

    @Resource
    private AlarmService alarmService;

    @RequestMapping("list.query")
    public RespMsg list() {
        return RespMsg.respSuc(dubboServiceService.selectAll());
    }

    @RequestMapping("find.query")
    public RespMsg find(Integer id) {
        return RespMsg.respSuc(dubboServiceService.findById(id));
    }


    @RequestMapping("addOrUpdate.do")
    //todo 保存 paramMode 2020.04.24
    public RespMsg addOrUpdate(@RequestBody ServiceEntity serviceEntity) {
        Integer id = serviceEntity.getId();

        if (CommonU.isIDC()) {
            //从 dubbo-admin获取集群所有provider地址,使用 "," join 为 String.
            String providerAddress = outService.getProviderAddress(serviceEntity.getApplicationName(), serviceEntity.getServiceName());
            log.info("DubboService addOrUpdate: service:{}, address:{}", serviceEntity.getServiceName(), providerAddress);

            if (StringUtils.isEmpty(providerAddress)) {
                String errorMsg = "[IDC - Dubbo链路地址检查]: 服务: (" + serviceEntity.getServiceName() + " ) 在注册中心没有找到服务节点,请检查服务是否注册成功";
                alarmService.send(MessageSender.Type.MAINTAINER, errorMsg);

                return RespMsg.respErr(errorMsg);
            }
            serviceEntity.setClusterAddress(providerAddress);
        }

        if (id == null) {
            dubboServiceService.insert(serviceEntity);
        } else {
            dubboServiceService.update(serviceEntity);
        }
        EventBusCenter.post(new ParamEvent(serviceEntity.getId(), TaskType.DUBBO, ParamEvent.Event.ADD_OR_UPDATE));
        return RespMsg.respSuc(serviceEntity.getId());
    }

    @RequestMapping("search.query")
    public RespMsg search(String key) {
        if (StringUtils.isEmpty(key)) return list();
        int id;
        try {
            id = Integer.parseInt(key);
        } catch (NumberFormatException e) {
            return RespMsg.respSuc(dubboServiceService.searchService(key));
        }
        List<ServiceEntity> links = new ArrayList<>();
        ServiceEntity service = dubboServiceService.findById(id);
        if (service != null) {
            links.add(service);
        }
        return RespMsg.respSuc(links);
    }


    @RequestMapping("info.query")
    public RespMsg info(Integer id) {
        ServiceEntity service = dubboServiceService.findById(id);
        PageInfo<ServiceParamsEntity> paramsEntityPageInfo = serviceParamsService.selectByServiceId(service.getId(), 1);
        ServiceBo bo = new ServiceBo();
        bo.setService(service);
        bo.setParams(paramsEntityPageInfo);
        return RespMsg.respSuc(bo);
    }


    @RequestMapping("addParams.do")
    public RespMsg addHttpParams(@RequestBody ParamsBo bo) {
        String params = bo.getParams();
        if (StringUtils.isEmpty(params)) return RespMsg.respErr("参数不能为空");
        String[] line = params.split("\n");
        for (String lineParam : line) {
            try {
                String decodeParams = CommonU.decodeParams(lineParam);
                serviceParamsService.insert(new ServiceParamsEntity(bo.getId(), decodeParams));
            } catch (UnsupportedEncodingException e) {
                return RespMsg.respErr("导入参数失败:" + e.getMessage());
            }
        }
        EventBusCenter.post(new ParamEvent(bo.getId(), TaskType.DUBBO, ParamEvent.Event.ADD));
        return RespMsg.respSuc();
    }

    @RequestMapping("updateParam.do")
    public RespMsg updateHttpParam(Integer paramId, String param) throws UnsupportedEncodingException {
        String decodeParam = CommonU.decodeParams(param);
        ServiceParamsEntity paramsEntity = new ServiceParamsEntity();
        paramsEntity.setId(paramId);
        paramsEntity.setParam(decodeParam);
        serviceParamsService.update(paramsEntity);
        EventBusCenter.post(new ParamEvent(paramsEntity.getId(), TaskType.DUBBO, ParamEvent.Event.ADD_OR_UPDATE));
        return RespMsg.respSuc();
    }

    @RequestMapping("paramDelete.do")
    public RespMsg httpParamDelete(Integer id) {
        ServiceParamsEntity entity = serviceParamsService.findById(id);
        int serviceId = entity.getServiceId();
        EventBusCenter.post(new ParamEvent(serviceId, TaskType.HTTP, ParamEvent.Event.DELETE));

        return RespMsg.respCom(serviceParamsService.deleteById(id));
    }

    @RequestMapping("clearParam.do")
    public RespMsg clearHttpParam(Integer id) {
        EventBusCenter.post(new ParamEvent(id, TaskType.HTTP, ParamEvent.Event.DELETE));
        return RespMsg.respCom(serviceParamsService.deleteAllByServiceId(id));
    }


    @RequestMapping("queryParams.query")
    public RespMsg queryHttpParams(Integer id, Integer currentPage) {
        Integer cp = currentPage == null ? 1 : currentPage;
        return RespMsg.respSuc(serviceParamsService.selectByServiceId(id, cp));
    }


    @RequestMapping("delete.do")
    public RespMsg delete(Integer id) {
        EventBusCenter.post(new ParamEvent(id, TaskType.HTTP, ParamEvent.Event.DELETE));
        return RespMsg.respCom(dubboServiceService.deleteById(id));
    }
}
