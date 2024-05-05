package com.yunji.titanrtx.common.u;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.time.ZoneId.SHORT_IDS;

public class LocalDateU {
    /**
     * 21 min
     */
    private static DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("mm");
    /**
     * 04:21
     */
    private static DateTimeFormatter HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    /**
     * 04:21:22
     */
    private static DateTimeFormatter HOUR_MINUTE_SECOND_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    /**
     * 2020-05-08 04:21
     */
    private static DateTimeFormatter NORMAL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * fileTime
     */
    private static DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
    private static DateTimeFormatter FILE_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm");

    public static final List<String> MINUTE_TIME_QUARTS = new ArrayList<String>() {{
        add("15");
        add("40");
        add("45");
    }};


    public static String getHourMinuteTime(long timestamp) {
        return getTimeString(timestamp, HOUR_MINUTE_FORMATTER);
    }

    public static String getNormalDate(long timestamp) {
        return getTimeString(timestamp, NORMAL_FORMATTER);
    }

    public static String getHourMinSecDate(long timestamp) {
        return getTimeString(timestamp, HOUR_MINUTE_SECOND_FORMATTER);
    }

    public static String getFileTime(long timestamp) {
        return getTimeString(timestamp, FILE_TIME_FORMATTER);
    }

    public static String getFileDayTime(long timestamp) {
        return getTimeString(timestamp, FILE_DAY_FORMATTER);
    }


    public static String getMinuteTime(long timestamp) {
        return getTimeString(timestamp, MINUTE_FORMATTER);
    }


    public static int getCurrentHour() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of(SHORT_IDS.get("CTT")));
        return dateTime.getHour();
    }

    public static String getCurrentTime() {
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of(SHORT_IDS.get("CTT")));
        return dateTime.format(HOUR_MINUTE_SECOND_FORMATTER);
    }

    private static String getTimeString(long timestamp, DateTimeFormatter formatter) {
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        ZoneId.of(SHORT_IDS.get("CTT"))
                );
        return localDateTime.format(formatter);
    }

}
