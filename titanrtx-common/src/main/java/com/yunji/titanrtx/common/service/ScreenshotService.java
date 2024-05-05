package com.yunji.titanrtx.common.service;

import com.yunji.titanrtx.common.message.RespMsg;

import java.io.IOException;

/**
 * ScreenshotService
 *
 * @author leihz
 * @since 2020-06-01 11:34 上午
 */
public interface ScreenshotService {
    /**
     * 截图,如果成功,会在RespMsg.data中返回截好的图片文件path.
     *
     * @param fileName 文件名(不包括文件路径，just文件名称)
     * @param type     截图的type, all_qps, dubbo_cost_order.
     * @return RespMsg
     */
    RespMsg screenshotPicture(String fileName, String type);

    /**
     * @param fileName 文件名
     * @return 图片字节数组
     * @throws IOException 文件不存在
     */
    byte[] renderPicture(String fileName) throws IOException;

}
