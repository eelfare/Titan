package com.yunji.titanrtx.manager.service.report.support;

import com.deepoove.poi.data.TextRenderData;
import com.deepoove.poi.data.builder.StyleBuilder;
import com.deepoove.poi.data.style.Style;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

@Slf4j
public class ReportUtils {

    private static final Executor asyncPool = new ForkJoinPool(20);

    private static final Style CONTENT_STYLE = StyleBuilder.newBuilder()
            .buildColor("000000")
            .buildFontSize(8)
            .buildFontFamily("Georgia, serif")
            .buildBold()
            .build();

    //FF0000
    private static final Style RED_STYLE = StyleBuilder.newBuilder()
            .buildColor("FF0000")
            .buildFontSize(8)
            .buildFontFamily("Georgia, serif")
            .buildBold()
            .build();

    public static final String REPORT_PREFIX = "report-";
    public static final String REPORT_SUFFIX = "-压测记录.docx";

    public static final String HTML_REPORT_PREFIX = "alert-";
    public static final String HTML_SUFFIX = ".html";
    private static final String IDC_ENV = "idc";

    public static final Gson GSON = new Gson();


    public static boolean isIDC() {
        String currentEnv = System.getProperty("config_env");
        if (StringUtils.isEmpty(currentEnv)) {
            return false;
        }
        return currentEnv.equalsIgnoreCase(IDC_ENV);
    }

    public static boolean notIDC() {
        return !isIDC();
    }


    public static TextRenderData textRenderCommon(String text) {
        return new TextRenderData(text, CONTENT_STYLE);
    }

    public static TextRenderData textRenderRed(String text) {
        return new TextRenderData(text, RED_STYLE);
    }


    public static boolean writeToFile(String path, String content) {
        try {
            File file = new File(path);
            try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
                out.write(content);
                out.flush();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public static <T> void supplierAsync(Supplier<T> supplier, Listener<T> listener) {
        try {
            CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, asyncPool);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.warn("[Report]Collect metrics error: " + ex.getMessage());
                    listener.onListen(null);
                } else {
                    listener.onListen(result);
                }
            });
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> CompletableFuture<T> supplierAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncPool);
    }
}
