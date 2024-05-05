package com.yunji.titanrtx.manager.web.controller;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.DocxRenderData;
import com.yunji.titanrtx.common.GlobalConstants;
import com.yunji.titanrtx.common.enums.TaskType;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.ScreenshotService;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.bos.http.ReportBaseBo;
import com.yunji.titanrtx.manager.dao.bos.http.ReportItemBo;
import com.yunji.titanrtx.manager.service.common.SystemProperties;
import com.yunji.titanrtx.manager.service.dubbo.DubboReportService;
import com.yunji.titanrtx.manager.service.http.HttpReportService;
import com.yunji.titanrtx.manager.service.report.support.RecordsDetailTablePolicy;
import com.yunji.titanrtx.manager.web.config.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Controller
@RequestMapping("export/")
@Slf4j
public class ExportController {

    @Resource
    private SystemProperties systemProperties;

    private Path reportLocation;

    @Resource
    private SystemConfig systemConfig;

    @Resource
    private HttpReportService httpReportService;

    @Resource
    private DubboReportService dubboReportService;

    @Resource
    private ScreenshotService screenshotService;

    @PostConstruct
    public void init() {
        if ("local".equalsIgnoreCase(System.getProperty("env"))){
            return;
        }
        String outDir = systemProperties.outputPathPrefix;
        this.reportLocation = Paths.get(outDir).toAbsolutePath().normalize();
        log.info("Auto report location is {}", reportLocation);
        try {
            Files.createDirectories(this.reportLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }


    @RequestMapping("export.query")
    public RespMsg export(String ids, TaskType type, HttpServletResponse response) {
        List<ReportItemBo> reportItemBos;
        switch (type) {
            case HTTP:
                reportItemBos = httpReportService.reportItemBos(ids);
                break;
            case DUBBO:
                reportItemBos = dubboReportService.reportItemBos(ids);
                break;
            default:
                return RespMsg.respErr("暂不支持类型");
        }
        doExport(response, reportItemBos);
        return RespMsg.respSuc();
    }

    //http://127.0.0.1:8080/export/report/report-20200512-1645.docx
    @GetMapping("/report/{fileName:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        org.springframework.core.io.Resource resource = loadFileAsResource(fileName);
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }
        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @RequestMapping("upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("Upload fileName {}", file.getName());
        String fileName = file.getOriginalFilename();
        try {

            Path targetLocation = this.reportLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok(fileName + " upload successful.");
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    @RequestMapping("renderPicture/{pic}")
    public void renderPicture(@PathVariable String pic, HttpServletResponse response) throws IOException {
        try {
            String filePath = systemProperties.screenshotPicDir + pic;
            log.info("Render picture path:{}", filePath);

            response.setContentType(MediaType.IMAGE_PNG.toString());
            byte[] bytes = screenshotService.renderPicture(filePath);

            response.getOutputStream().write(bytes);
        } catch (Exception e) {
            log.error("Render pic got error,cause: " + e.getMessage(), e);
            response.getWriter().println("render pic error: " + e.getMessage());
        }
    }


    private void doExport(HttpServletResponse response, List<ReportItemBo> reportItemBos) {
        int recordEndIndex = 7;
        ReportBaseBo baseBo = new ReportBaseBo();
        baseBo.setDate(CommonU.getFormatDay());
        Configure config = Configure.newBuilder().customPolicy("recordsBo", new RecordsDetailTablePolicy(recordEndIndex)).build();
        try {
            DocxRenderData segment = new DocxRenderData(new File(systemConfig.getSonTempPath()), reportItemBos);
            baseBo.setReportItems(segment);

            String REPORT_PREFIX = "titan_report_";
            String fileName = REPORT_PREFIX + CommonU.getDate();

            response.setCharacterEncoding(GlobalConstants.URL_DECODER);
            response.setContentType("application/octet-stream; charset=utf-8");
            String REPORT_SUFFIX = ".docx";
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, GlobalConstants.URL_DECODER) + REPORT_SUFFIX);

            XWPFTemplate template = XWPFTemplate.compile(systemConfig.getParentTempPath(), config).render(baseBo);
            ServletOutputStream outputStream = response.getOutputStream();
            template.write(outputStream);
            template.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载文件
     *
     * @param fileName 文件名
     * @return 文件
     */
    private org.springframework.core.io.Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.reportLocation.resolve(fileName).normalize();
            org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }


}

