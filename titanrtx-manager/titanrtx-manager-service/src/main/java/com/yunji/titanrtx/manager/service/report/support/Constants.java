package com.yunji.titanrtx.manager.service.report.support;

public class Constants {
    /**
     * 邮箱相关配置信息
     */
    public static final String NOTICE_EMAIL_FROM_KEY = "beacon.notice.mail.from";
    public static final String NOTICE_EMAIL_SMTP_HOST_KEY = "mail.smtp.host";
    public static final String MAIL_SMTP_TIMEOUT_KEY = "mail.smtp.timeout";

    public static final String NOTICE_EMAIL_SMTP_HOST =
            get(NOTICE_EMAIL_SMTP_HOST_KEY, "smtp.yunjiglobal.com");

    public static final String NOTICE_EMAIL_FROM =
            get(NOTICE_EMAIL_FROM_KEY, "beacon@yunjiglobal.com");

//    public static final String NOTICE_EMAIL_FROM =
//            get(NOTICE_EMAIL_FROM_KEY, "leihz@yunjiglobal.com");


    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        String envValue = System.getenv(key.replaceAll("\\.", "_"));
        if (envValue == null) {
            return System.getProperty(key, defaultValue);
        }
        return envValue;
    }
}
