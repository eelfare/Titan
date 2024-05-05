package com.yunji.titanrtx.manager.service.report.support;

import com.yunji.titanrtx.common.service.ScreenshotService;
import com.yunji.titanrtx.manager.service.report.dto.PictureDTO;
import com.yunji.titanrtx.manager.service.report.service.ScreenService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ReportTest
 *
 * @author leihz
 * @since 2020-06-30 11:05 下午
 */
public class ReportTest {
    private Map<String, List<String>> taskScreenshotListMap = new ConcurrentHashMap<>();

    private void screenshotForList(String taskNo, String type, int batch) {
        String key = taskNo + "_" + type;
        taskScreenshotListMap.compute(key, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
                v.add(batch + "");
            } else {
                v.add(batch + "");
            }
            return v;
        });
    }

    public static void main(String[] args) {
        ReportTest reportTest = new ReportTest();

        for (int i = 0; i < 100; i++) {
            reportTest.screenshotForList("abc", "dalao", i);
        }

        int size = reportTest.taskScreenshotListMap.size();

        System.out.println(size);

    }
}
