package com.yunji.titanrtx.manager.web.controller.system;

import com.yunji.titanrtx.manager.service.common.SystemProperties;
import com.yunji.titanrtx.manager.service.http.HttpStressService;
import com.yunji.titanrtx.manager.service.support.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

/**
 * 将 html 代码渲染并返回.
 *
 * @author leihz
 * @since 2020-05-13 10:04 上午
 */
@RequestMapping("/export")
@Controller
@Slf4j
public class HtmlRenderController {

    @Resource
    private SystemProperties systemProperties;

    @Resource
    private HttpStressService stressService;


    //    @RequestMapping("/render/{name}")
    @RequestMapping("/render/{name:.+}")
    public void renderHtml(@PathVariable String name, HttpServletResponse response) {
        try {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            String content;
            String outDir = systemProperties.outputPathPrefix;
            File file = new File(outDir + name);
            if (!file.exists()) {
                content = "<p>报告不存在,请检查.</p>";
            } else {
                try (FileReader reader = new FileReader(file)) {
                    content = IOUtils.toString(reader);
                }
            }
            String html = CommonUtils.renderHtml(content);
            out.println(html);
        } catch (Exception e) {
            log.error("Render html got error,cause: " + e.getMessage(), e);
        }
    }

    @RequestMapping("/scene/{id}/{status}")
    public void sceneRequest(@PathVariable int id,
                             @PathVariable boolean status,
                             HttpServletResponse response) {
        log.info("Http场景链路测试,场景id:{},showOnlyError:{}", id, status);
        response.setContentType("text/html;charset=UTF-8");
        try {
            Object result = stressService.reportSceneResult(id, status, false);

            if (result instanceof String) {
                response.getWriter().println(result);
            } else {
                response.getWriter().println(result.toString());
            }
        } catch (Exception e) {
            log.error("SceneRequest html got error,cause: " + e.getMessage(), e);
        }
    }

    @RequestMapping("/sceneDelete/{id}")
    public void sceneAndDeleteRequest(@PathVariable int id,
                                      HttpServletResponse response) {
        log.info("Http场景链路测试,场景id:{},showOnlyError:{},delete bad link param:{}", id, true, true);
        response.setContentType("text/html;charset=UTF-8");
        try {
            Object result = stressService.reportSceneResult(id, true, true);

            if (result instanceof String) {
                response.getWriter().println(result);
            } else {
                response.getWriter().println(result.toString());
            }
        } catch (Exception e) {
            log.error("SceneRequest html got error,cause: " + e.getMessage(), e);
        }
    }


}
