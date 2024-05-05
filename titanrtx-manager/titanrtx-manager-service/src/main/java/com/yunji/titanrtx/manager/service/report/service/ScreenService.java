package com.yunji.titanrtx.manager.service.report.service;

import com.deepoove.poi.data.PictureRenderData;
import com.yunji.titanrtx.common.message.RespMsg;
import com.yunji.titanrtx.common.service.ScreenshotService;
import com.yunji.titanrtx.common.u.CommonU;
import com.yunji.titanrtx.manager.dao.entity.http.HttpSceneEntity;
import com.yunji.titanrtx.manager.service.common.SystemProperties;
import com.yunji.titanrtx.manager.service.http.HttpSceneService;
import com.yunji.titanrtx.manager.service.report.dto.FinalReportDTO;
import com.yunji.titanrtx.manager.service.report.dto.PictureDTO;
import com.yunji.titanrtx.manager.service.report.dto.PictureDataWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import static com.yunji.titanrtx.manager.service.report.support.ReportLog.log;

/**
 * ScreenService
 *
 * @author leihz
 * @since 2020-06-09 7:07 下午
 */
@Component
public class ScreenService implements InitializingBean {
    private static ScheduledExecutorService scheduledExecutors
            = Executors.newScheduledThreadPool(10);

    private List<String> perMinutesScreenTypeList = new ArrayList<>();

    private List<String> screenTypeList = new ArrayList<>();

    private List<String> totalScreenTypeList = new ArrayList<>();

    private Map<String, List<PictureDTO>> taskScreenshotListMap = new ConcurrentHashMap<>();


    @Resource
    private SystemProperties systemProperties;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private HttpSceneService httpSceneService;


    @Override
    public void afterPropertiesSet() throws Exception {
        perMinutesScreenTypeList.addAll(Arrays.asList("cgi_cost_order", "cgi_cost_order_invoke", "dubbo_cost_order", "dubbo_cost_order_invoke"));
        screenTypeList.addAll(Arrays.asList("all_qps", "mysql", "redis", "mongo"));

        totalScreenTypeList.addAll(perMinutesScreenTypeList);
        totalScreenTypeList.addAll(screenTypeList);

        scheduledExecutors.scheduleWithFixedDelay(this::cleanScreenMap, 0, 6, TimeUnit.HOURS);
    }

    /**
     * 压测5min之后,每分钟生成一张图.
     */
    public void schedulePerMinAfterDelayScreenshot(String taskNo) {
        int screenshotAfterMinutes = systemProperties.screenshotAfterMinutes;
        log.info("[截图服务]构造定时任务成功, taskNo: {}, {}min后进行截图.", taskNo, screenshotAfterMinutes);
        //开始压测后多久开始截图.

        scheduledExecutors.schedule(() -> {
            log.info("TaskNo:{} 在压测 {}min 后开始截图,每分钟一次.........", taskNo, screenshotAfterMinutes);
            Integer id = CommonU.parseTaskNoToId(taskNo);
            HttpSceneEntity sceneEntity;

            sceneEntity = httpSceneService.findById(id);
            if (sceneEntity.getStatus() != 1) {
                log.info("TaskNo:{} 在压测 {}min 后开始截图时，任务已停止,忽略继续截图.", taskNo, screenshotAfterMinutes);
            } else {
                log.info("TaskNo:{} 在压测 {}min 后开始截图.", taskNo, screenshotAfterMinutes);
                for (String screenType : screenTypeList) {
                    CompletableFuture.runAsync(() -> screenshotForList(taskNo, screenType, 0));
                }
            }

            log.info("开始每分钟截图项目.perMinutesScreenTypeList :{}", perMinutesScreenTypeList);
            //每分钟都要截图的项目.
            AtomicInteger atomicBatch = new AtomicInteger(0);
            while (true) {
                int batch = atomicBatch.incrementAndGet();
                sceneEntity = httpSceneService.findById(id);
                if (sceneEntity.getStatus() != 1) {
                    log.info("TaskNo:{} 在压测 {}min 后开始截图时，任务已停止,忽略继续截图.", taskNo, screenshotAfterMinutes);
                    break;
                } else {
                    log.info("TaskNo:{} 在压测 {}min 后开始截图.每分钟一次,第 {} 次.",
                            taskNo, screenshotAfterMinutes, atomicBatch.get());
                    for (String screenType : perMinutesScreenTypeList) {
                        CompletableFuture.runAsync(() -> screenshotForList(taskNo, screenType, batch));
                    }
                    try {
                        TimeUnit.SECONDS.sleep(70);
                    } catch (InterruptedException ignored) {
                    }
                }
            }

        }, screenshotAfterMinutes, TimeUnit.MINUTES);
    }

    /**
     * 组装生成报告.
     * 在生成报告时,从 map 中拿已缓存好的图片,如果图片没有,则进行截图.
     */
    public boolean screenshotMetric(String taskNo, FinalReportDTO reportDTO) {
        //total
        CountDownLatch latch = new CountDownLatch(totalScreenTypeList.size());
        try {
            for (String type : totalScreenTypeList) {
                List<PictureDTO> pictureDTOList = taskScreenshotListMap.get(generateKey(taskNo, type));
                log.info("Type [{}], picture dto size:{}", type, pictureDTOList.size());

                if (pictureDTOList.size() > 0) {
                    setToReport(type, reportDTO, pictureDTOList);
                    latch.countDown();
                } else {
                    CompletableFuture<PictureDTO> screenshotFuture = supplyAsync(() -> doScreenshot(taskNo, type, 100));

                    screenshotFuture.whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Get screenshot wrong,cause: " + ex.getMessage());
                        } else {
                            if (result != null) {
                                setToReport(type, reportDTO, Arrays.asList(result));
                            }
                        }
                        latch.countDown();
                    });
                }
            }
            latch.await();
            return true;
        } catch (Exception e) {
            log.error("Screenshot got error:{}", e.getMessage());
            for (long i = 0; i < latch.getCount(); i++) {
                latch.countDown();
            }
            return false;
        }
    }

    private void screenshotForList(String taskNo, String type, int batch) {
        String key = generateKey(taskNo, type);
        PictureDTO pictureDTO = doScreenshot(taskNo, type, batch);
        if (pictureDTO != null) {
            log.info("[截图服务]Screenshot ok,taskNo:{},type:{},fileName:{}", taskNo, type, pictureDTO);
            taskScreenshotListMap.compute(key, (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                    v.add(pictureDTO);
                } else {
                    v.add(pictureDTO);
                }
                return v;
            });
        }
    }

    //PictureDTO
    private PictureDTO doScreenshot(String taskNo, String type, int batch) {
        long timestamp = System.currentTimeMillis();
        String picName = "Pic_" + type.toUpperCase() + "_" + timestamp + ".png";
        log.info("[截图服务]开始截图 from taskNo:{},type:{},picName:{}.", taskNo, type, picName);

        RespMsg respMsg = screenshotService.screenshotPicture(picName, type);

        if (respMsg.getCode() == 200) {
            byte[] picBytes = new byte[0];
            try {
                picBytes = screenshotService.renderPicture((String) respMsg.getData());
            } catch (Exception e) {
                log.error("Render picture bytes got error: " + e.getMessage());
            }

            String filePath = systemProperties.outputPathPrefix + picName;
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                IOUtils.write(picBytes, fos);

                return new PictureDTO(type, timestamp, batch, filePath);
            } catch (IOException e) {
                log.error("Screenshot write pic to {} failed,cause: {}", filePath, e.getMessage());
            }
        } else {
            log.warn("Screenshot fileName:{} failed,respMsg:{}.", picName, respMsg);
        }
        return null;
    }


    /**
     * 将渲染好的图片放入最终的输出对象中
     */
    private void setToReport(String type, FinalReportDTO reportDTO, List<PictureDTO> pictureDTOList) {
        if (perMinutesScreenTypeList.contains(type)) {
            List<PictureDataWrapper> wrappers = null;
            switch (type) {
                case "dubbo_cost_order":
                    wrappers = reportDTO.getDubboCostList();
                    break;
                case "dubbo_cost_order_invoke":
                    wrappers = reportDTO.getDubboCostByInvokeList();
                    break;

                case "cgi_cost_order":
                    wrappers = reportDTO.getCgiCostList();
                    break;
                case "cgi_cost_order_invoke":
                    wrappers = reportDTO.getCgiCostbyInvoke();
                    break;

                default:
                    wrappers = new ArrayList<>();
                    log.warn("Type {} is not from perMinutesScreenTypeList", type);
                    break;
            }

            for (PictureDTO pictureDTO : pictureDTOList) {
                try (FileInputStream in = new FileInputStream(pictureDTO.getPath())) {
                    byte[] picBytes = IOUtils.toByteArray(in);
                    PictureRenderData renderData = convertToRenderData(picBytes);

                    wrappers
                            .add(
                                    new PictureDataWrapper(pictureDTO.getName(), pictureDTO.getBatch(), renderData)
                            );


                } catch (Exception e) {
                    log.error("SetToReport error:" + e.getMessage());
                }
            }

        } else {

            PictureDTO pictureDTO = pictureDTOList.get(0);
            try (FileInputStream in = new FileInputStream(pictureDTO.getPath())) {
                byte[] picBytes = IOUtils.toByteArray(in);
                PictureRenderData renderData = convertToRenderData(picBytes);

                BeanUtils.copyProperty(reportDTO, type, renderData);
            } catch (Exception e) {
                log.error("SetToReport error:" + e.getMessage());
            }
        }

    }


    private PictureRenderData convertToRenderData(byte[] picBytes) {
        String reportPictureSize = systemProperties.reportPictureSize;
        String[] sizes = reportPictureSize.split(",");

        return new PictureRenderData(
                Integer.parseInt(sizes[0]),
                Integer.parseInt(sizes[1]),
                ".png",
                picBytes
        );
    }


    private String generateKey(String taskNo, String type) {
        return taskNo + "_" + type;
    }

    private Integer getIdByKey(String key) {
        return CommonU.parseTaskNoToId(key.split("_")[0]);
    }

    private void cleanScreenMap() {
        try {
            int count = 0;
            Iterator<String> iterator = taskScreenshotListMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Integer taskId = getIdByKey(key);
                HttpSceneEntity httpSceneEntity = httpSceneService.findById(taskId);
                if (httpSceneEntity.getStatus() != 1) {
                    iterator.remove();
                    count++;
                }
            }
            log.info("[截图服务] 本轮清理移除screenMap key:{},目前map size:{}", count, taskScreenshotListMap.size());
        } catch (Exception e) {
            log.error("[截图服务]定时清理线程异常:" + e.getMessage());
        }
    }
}
