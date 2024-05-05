package com.yunji.titanrtx.manager.web.controller.system;

import com.yunji.titanrtx.common.domain.cia.Rules;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.CommanderService;
import com.yunji.titanrtx.manager.service.common.IncrementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("rulesLink/")
public class RulesLinkController {

    @Resource
    private CommanderService commanderService;

    @Resource
    private IncrementService incrementService;

    @RequestMapping("rules.query")
    public RespMsg rules(){
        return commanderService.rules();
    }


    @RequestMapping("info.query")
    public RespMsg info(Integer id){
        Rules rules;
        if (null == id){
           rules = new Rules();
        }else{
            rules = commanderService.findById(id);
        }
        return RespMsg.respSuc(rules);
    }

    @RequestMapping("addRules.do")
    public RespMsg addRules(@RequestBody Rules rules){
        Integer id = rules.getId();
        if (null == id){
            int incrementID = incrementService.incrementID();
            rules.setId(incrementID);
            return commanderService.addRules(rules);
        }
        return commanderService.updateRules(rules);
    }


    @RequestMapping("delete.do")
    public RespMsg deleteRules(Integer id){
        return commanderService.deleteRules(id);
    }



}
