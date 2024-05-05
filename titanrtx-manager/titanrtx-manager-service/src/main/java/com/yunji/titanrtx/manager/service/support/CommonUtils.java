package com.yunji.titanrtx.manager.service.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.google.common.base.Splitter;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.u.LocalDateU;
import com.yunji.titanrtx.manager.service.report.support.MarkdownUtils;
import com.yunji.titanrtx.plugin.http.HttpSyncClientTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static com.yunji.titanrtx.manager.service.report.support.ReportUtils.*;

@Slf4j
public class CommonUtils {

    public static RespMsg httpGet(String url, String params) {
        try {
            return JSON.parseObject(HttpSyncClientTool.doGet(url, params), RespMsg.class);
        } catch (JSONException e) {
            return RespMsg.respSuc();
        }
    }

    public static String renderToHtml(List<String> results) {
        String content = MarkdownUtils.renderToHtml(StringUtils.join(results, "\n"));
        return renderHtml(content);
    }

    public static String renderHtml(String content) {
        StringBuilder sb = new StringBuilder();
        String title = "Titan场景链路请求报告";
        sb
                .append("<!DOCTYPE html>\n")
                .append("<html>\n<head><title>")
                .append(title)
                .append("</title></head>\n")
                .append("<body bgcolor=\"#f0f0f0\">\n")
                .append("<h1 align=\"center\">")
                .append(title)
                .append("</h1>")
                .append(content)
                .append("\n</body>\n</html>");

        return sb.toString();
    }

    public static List<String> getProviderAddressList(String providerAddress) {
        return providerAddress != null ?
                Splitter.on(",").omitEmptyStrings().splitToList(providerAddress) : new ArrayList<>();
    }
}
