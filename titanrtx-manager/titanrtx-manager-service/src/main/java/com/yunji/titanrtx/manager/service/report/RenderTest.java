package com.yunji.titanrtx.manager.service.report;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.PictureRenderData;
import com.yunji.titanrtx.common.u.LocalDateU;
import com.yunji.titanrtx.manager.service.report.dto.FinalReportDTO;
import com.yunji.titanrtx.manager.service.report.dto.PictureBizWrapper;
import com.yunji.titanrtx.manager.service.report.dto.PictureDataWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * RenderTest
 *
 * @author leihz
 * @since 2020-06-30 4:33 下午
 */
@Slf4j
public class RenderTest {
    private static List<String> pathList = new ArrayList<>();
    private static LinkedList<PictureBizWrapper> bizWrappers = new LinkedList<>();
    private static FinalReportDTO reportDTO = new FinalReportDTO();

    static {
        pathList.add("/Users/maple/Pictures/job/07210314.jpeg");
        pathList.add("/Users/maple/Pictures/job/07210320.jpeg");
        pathList.add("/Users/maple/Pictures/job/1.jpeg");
        pathList.add("/Users/maple/Pictures/job/9551.jpeg");
        pathList.add("/Users/maple/Pictures/job/DSC_0053.jpg");
        pathList.add("/Users/maple/Pictures/job/IMG_2526.jpg");
        pathList.add("/Users/maple/Pictures/job/WechatIMG684.jpeg");


        for (int i = 0; i < pathList.size(); i++) {
            String hourMinSecDate = LocalDateU.getHourMinSecDate(System.currentTimeMillis());
            reportDTO.setCgi(
                    new PictureDataWrapper("cgiCost#" + hourMinSecDate, i,
                            build(pathList.get(i))));
            reportDTO.setCgiBYInvoke(
                    new PictureDataWrapper("cgiCostInvoke#" + hourMinSecDate, i,
                            build(pathList.get(i))));

            reportDTO.setDubbo(
                    new PictureDataWrapper("dubbo#" + hourMinSecDate, i,
                            build(pathList.get(i))));

            reportDTO.setDubboByInvoke(
                    new PictureDataWrapper("dubboInvoker#" + hourMinSecDate, i,
                            build(pathList.get(i))));
        }

        reportDTO.buildBizWrappers();

    }

    @Data
    private static class PO {
        private List<PictureDataWrapper> wrappers = new ArrayList<>();

    }


    public static void main(String[] args) {
//        new RenderTest().test();
        new RenderTest().generate(null);

    }

    public void test() {
        PO po = new PO();
        for (String path : pathList) {
            try (FileInputStream in = new FileInputStream(path)) {
                byte[] picBytes = IOUtils.toByteArray(in);
                PictureRenderData renderData = convertToRenderData(picBytes);

                po.getWrappers().add(new PictureDataWrapper(path, -1, renderData));
            } catch (Exception e) {
                log.error("SetToReport error:" + e.getMessage());
            }
        }
        generate(po);

    }

    public static PictureRenderData build(String path) {
        try (FileInputStream in = new FileInputStream(path)) {
            byte[] picBytes = IOUtils.toByteArray(in);
            return convertToRenderData(picBytes);
        } catch (Exception e) {
            log.error("SetToReport error:" + e.getMessage());
        }
        return null;
    }


    public void generate(PO po) {

        Configure config = Configure.newBuilder()
//                .customPolicy("cgiReport", new RecordsDetailTablePolicy(6))
//                .customPolicy("dubboCostReport", new RecordsDetailTablePolicy(7))
//                .customPolicy("beaconAlertReport", new RecordsDetailTablePolicy(3))
                .build();
        try {

            XWPFTemplate template = XWPFTemplate.compile(
                    "/Users/maple/yunji/titanrtx/titanrtx-manager/titanrtx-manager-web/src/main/resources/doc/auto-report2.docx"
                    , config).render(reportDTO);

            String outFilePath
                    = "/Users/maple/Downloads/auto-report-test.docx";

            FileOutputStream outputStream = new FileOutputStream(outFilePath);

            template.write(outputStream);
            template.close();
            log.info("......生成压测报告成功,存储路径:{}", outFilePath);
        } catch (Exception e) {
            log.error("生成压测报告失败: " + e.getMessage(), e);
        }
    }


    private static PictureRenderData convertToRenderData(byte[] picBytes) {
        return new PictureRenderData(500, 400,
                ".png", picBytes);
    }
}
