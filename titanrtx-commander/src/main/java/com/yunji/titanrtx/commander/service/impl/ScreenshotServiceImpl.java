package com.yunji.titanrtx.commander.service.impl;

import com.google.gson.Gson;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.ScreenshotService;
import com.yunji.titanrtx.common.u.AHCHttpU;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Param;
import org.asynchttpclient.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;

/**
 * ScreenshotServiceImpl
 *
 * @author leihz
 * @since 2020-06-02 9:51 上午
 */
@Slf4j
@Component
public class ScreenshotServiceImpl implements ScreenshotService {
    private Gson gson = new Gson();

    @Value("${pic.screenshot.url:http://127.0.0.1:9100/screenshot}")
    private String screenshotUrl;

    @Override
    public RespMsg screenshotPicture(String fileName, String type) {
        log.info("Screenshot service url:{}", screenshotUrl);
        try {
            Request request = Dsl
                    .post(screenshotUrl)
                    .setFormParams(Arrays.asList(
                            new Param("fileName", fileName),
                            new Param("type", type))
                    )
                    .setRequestTimeout(30000)
                    .build();

            String result = AHCHttpU.executeRequest(request);
            RespMsg respMsg = gson.fromJson(result, RespMsg.class);
            log.info("发起screenshot返回:{}", respMsg);
            return respMsg;
        } catch (Exception e) {
            log.error("截图 fileName:{}, type:{} 时出错: " + e.getMessage(), fileName, type);
        }
        return RespMsg.respErr();
    }

    @Override
    public byte[] renderPicture(String fileName) {
        log.info("....Render picture name: {} ....", fileName);
        try {
            File file = new File(fileName);
            return fileToByte(file);

        } catch (Exception e) {
            log.error("Render picture got error,cause: " + e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }


    private static byte[] fileToByte(File img) throws Exception {
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BufferedImage bi;
            bi = ImageIO.read(img);
            ImageIO.write(bi, "png", baos);
            bytes = baos.toByteArray();
            return bytes;
        }
    }
}
