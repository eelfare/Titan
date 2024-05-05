package com.yunji.titanrtx.manager.service.report.dto;

import com.deepoove.poi.data.PictureRenderData;
import lombok.Data;

import java.util.List;

/**
 * PictureDataWrapper
 *
 * @author leihz
 * @since 2020-06-30 4:26 下午
 */
@Data
public class PictureBizWrapper {

    public static class Wrapper {
        public String slogan;
        public PictureRenderData data;

        public Wrapper(String slogan, PictureRenderData data) {
            this.slogan = slogan;
            this.data = data;
        }
    }

    public String tag;
    private String time;
    private int batch;

   /* private Wrapper cgi;
    private Wrapper cgiInvoke;
    private Wrapper dubbo;
    private Wrapper dubboInvoke;*/

    private PictureRenderData cgi;
    private PictureRenderData cgiInvoke;
    private PictureRenderData dubbo;
    private PictureRenderData dubboInvoke;

    public PictureBizWrapper(String time,
                             int batch,
                             PictureRenderData cgi,
                             PictureRenderData cgiInvoke,
                             PictureRenderData dubbo,
                             PictureRenderData dubboInvoke) {
        this.time = time;
        this.batch = batch;
        this.tag = "[CGI、DUBBO指标报告] 批次: " + batch + ", 时间:" + time + ".";
        this.cgi = cgi;
        this.cgiInvoke = cgiInvoke;
        this.dubbo = dubbo;
        this.dubboInvoke = dubboInvoke;
    }


    /*public PictureBizWrapper(String time,
                             int batch,
                             PictureRenderData cgi,
                             PictureRenderData cgiInvoke,
                             PictureRenderData dubbo,
                             PictureRenderData dubboInvoke) {
        this.time = time;
        this.batch = batch;
        this.cgi = new Wrapper("CGI耗时排行(按耗时): 批次: " + batch + ", 时间: " + time, cgi);
        this.cgiInvoke = new Wrapper("CGI耗时排行(按成功次数): " + batch + ", 时间: " + time, cgiInvoke);
        this.dubbo = new Wrapper("DUBBO耗时排行(按耗时): " + batch + ", 时间: " + time, dubbo);
        this.dubboInvoke = new Wrapper("DUBBO耗时排行(按调用次数): " + batch + ", 时间: " + time, dubboInvoke);
    }*/
}
