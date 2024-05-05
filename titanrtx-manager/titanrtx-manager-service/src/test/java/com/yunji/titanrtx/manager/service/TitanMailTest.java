package com.yunji.titanrtx.manager.service;

import com.yunji.titanrtx.manager.service.report.mail.EmailChannel;

import java.util.Arrays;

/**
 * TitanMailTest
 *
 * @author leihz
 * @since 2020-05-12 3:06 下午
 */
public class TitanMailTest {

    public static void main(String[] args) {
        EmailChannel.sendTitanReport("/Users/maple/logs/titanrtx/out/report-20200508-1440.docx", Arrays.asList("leihz@yunjiglobal.com"));
    }

}
