package com.yunji.titanrtx.manager.service.common;

import com.yunji.titanrtx.common.enums.ParamTransmit;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SystemProperties
 *
 * @author leihz
 * @since 2020-05-12 5:53 下午
 */
@Component
public class SystemProperties implements InitializingBean {

    @Value("${dubboConcurrent:10000}")
    public volatile int  dubboConcurrent;

    @Value("${webSiteDomain:https://titanx-tx.yunjiglobal.com}")
    public String webSiteDomain;
    /**
     * influxdb 地址
     */
    @Value("${yj.influxdb.url:https://influxdb-tx-gamma.yunjiweidian.com/query}")
    public String influxdbUrl;

    @Value("${auto-report.template.path}")
    public String templatePath;

    @Value("${auto-report.out.dir}")
    public String outputPathPrefix;

    /**
     * 此目录和commander部署的目录同级.
     */
    @Value("${screenshot-pic.dir:/usr/local/yunji/titanx/pic/}")
    public String screenshotPicDir;

    @Value("#{'${auto-report.mail.users}'.split(';')}")
    public List<String> mailToUserList = new ArrayList<>();

    @Value("${httpConcurrent:10000}")
    public volatile int httpConcurrent;
    /**
     * 切换influxdb数据库进度的边界值（小时）
     */
    @Value("${change.influxdb.border.hour.length:3}")
    public int changeInfluxdbBorderHourLength;

    @Value("#{'${link.param.transmit:orders}'.toUpperCase()}")
    public ParamTransmit paramTransmit;

    @Value("${report.pic.size:700,400}")
    public String reportPictureSize;

    /**
     * 在压测开始后的多少分进行截图.
     */
    @Value("${report.pic.after.minutes:5}")
    public int screenshotAfterMinutes;
    /**
     * 截图指标
     */
    @Value("#{'${report.screen.metrics:mysql}'.split(',')}")
    public List<String> screenshotMetrics;


    /**
     * 从外部获取的报告,导出地址
     */
    public String autoOutsideReportUrl;
    /**
     * titan web站点上的报告链接
     */
    public String insideReportUrlPrefix;
    /**
     * titan html 展示页面
     */
    public String htmlReportUrl;



    @Override
    public void afterPropertiesSet() throws Exception {
        autoOutsideReportUrl = webSiteDomain + "/export/report/";
        insideReportUrlPrefix = webSiteDomain + "/view/main.html#/httpReportDetail?id=";
        htmlReportUrl = webSiteDomain + "/export/render/";
    }
}
