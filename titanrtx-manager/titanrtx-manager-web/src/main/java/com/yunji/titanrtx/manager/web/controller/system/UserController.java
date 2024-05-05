package com.yunji.titanrtx.manager.web.controller.system;

import com.yunji.titanrtx.common.message.ErrorMsg;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.dao.entity.system.UserEntity;
import com.yunji.titanrtx.manager.service.system.PathService;
import com.yunji.titanrtx.manager.service.system.UserService;
import com.yunji.titanrtx.manager.web.support.u.RequestU;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("user/")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private PathService pathService;

    @RequestMapping("list.query")
    public RespMsg list() {
        return RespMsg.respSuc(userService.selectAll());
    }


    @RequestMapping("info.query")
    public RespMsg info(Integer id) {
        return RespMsg.respSuc(userService.userInfo(id));
    }


    @RequestMapping("session.query")
    public RespMsg userSession(HttpSession session) {
        return RespMsg.respSuc(RequestU.getUserSessionBo(session));
    }

    @RequestMapping("addOrUpdate.do")
    public RespMsg addOrUpdate(@RequestBody UserEntity userEntity) {
        Integer id = userEntity.getId();
        if (id == null) {
            UserEntity user = userService.findUserByPhone(userEntity.getPhone());
            if (user != null) {
                return RespMsg.respErr(ErrorMsg.PHONE_REPETITION);
            }
            userService.insert(userEntity);
        } else {
            userService.update(userEntity);
        }
        return RespMsg.respSuc();
    }


    @RequestMapping("disable.do")
    public RespMsg disable(Integer id) {
        return RespMsg.respCom(userService.disable(id));
    }

    @RequestMapping("enable.do")
    public RespMsg enable(Integer id) {
        return RespMsg.respCom(userService.enable(id));
    }

    @RequestMapping("permissionList.query")
    public RespMsg permissionList() {
        return RespMsg.respSuc(pathService.selectAll());
    }


}
