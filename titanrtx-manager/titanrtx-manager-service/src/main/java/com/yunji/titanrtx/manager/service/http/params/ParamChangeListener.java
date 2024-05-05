package com.yunji.titanrtx.manager.service.http.params;


import com.google.common.eventbus.Subscribe;
import com.yunji.titanrtx.common.enums.ParamStatus;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.manager.dao.bos.UserSessionBo;
import com.yunji.titanrtx.manager.dao.entity.system.UserEntity;
import com.yunji.titanrtx.manager.service.common.eventbus.EventBusCenter;
import com.yunji.titanrtx.manager.service.common.eventbus.ParamEvent;
import com.yunji.titanrtx.manager.service.dubbo.DubboServiceService;
import com.yunji.titanrtx.manager.service.http.LinkService;
import com.yunji.titanrtx.manager.service.system.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class ParamChangeListener implements InitializingBean {

    @Resource
    private LinkService linkService;
    @Resource
    private DubboServiceService dubboService;
    @Resource
    private UserService userService;


    @Subscribe
    public void onMessage(ParamEvent info) {
        TaskType taskType = info.getTaskType();

        if (taskType == TaskType.HTTP) {
            ParamEvent.Event event = info.getEvent();
            //linkId
            int id = info.getId();
            int updateStatus = linkService.updateParamOrderStatus(id, ParamStatus.OUT_OF_ORDER.getId());
            log.info("HTTP 链路[{}]的Params信息变更了,变更事件:{},updateStatus:{} ...", id, event, updateStatus);
        } else {
            //dubbo链路变更.
            ParamEvent.Event event = info.getEvent();
            //linkId
            int id = info.getId();
            int updateStatus = dubboService.updateDubboServiceOrderStatus(id, ParamStatus.OUT_OF_ORDER.getId());
            log.info("DUBBO 链路[{}]的Params信息变更了,变更事件:{},updateStatus:{} ...", id, event, updateStatus);
        }


    }

    @Subscribe
    public void onUserLoginMessage(UserSessionBo sessionBo) {
        try {
            log.info("事件监听: user: {}", sessionBo);
            UserEntity oldEntity = userService.findUserByPhone(sessionBo.getPhone());
            oldEntity.setLoginTimes(oldEntity.getLoginTimes() + 1);
            //为空才set
            if (StringUtils.isEmpty(oldEntity.getNickName())) {
                oldEntity.setNickName(sessionBo.getUserName());
            }
            userService.update(oldEntity);
        } catch (Exception e) {
            log.error("onUserLoginMessage error:", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventBusCenter.register(this);
    }
}
