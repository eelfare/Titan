package com.yunji.titanrtx.manager.web.controller.data;

import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.manager.service.data.CompareService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * CompareController
 *
 * @author leihz
 * @since 2020-08-28 4:02 下午
 */
@RestController
@RequestMapping("batch")
public class CompareController {
    @Resource
    private CompareService compareService;

    @RequestMapping("/compare.do")
    public Object compareLinkIdAndParams() {
        String ret = compareService.compare();
        return RespMsg.respSuc(ret);
    }

    @RequestMapping("/deleteEmptyLinks.do")
    public Object deleteEmptyParamLinks() {
        String ret = compareService.deleteEmptyParamLinks();
        return RespMsg.respSuc(ret);
    }
}
