package com.yunji.titanrtx.common.u;

import com.yunji.titanrtx.common.GlobalConstants;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonU {

    public static String buildFullUrl(String protocol, String url) {
        return protocol + "://" + url;
    }


    public static List<String> parseIds(String idsWeight) {
        List<String> ids = new ArrayList<>();
        List<String> idsWeightPair = stringJoinToList(idsWeight);
        for (String pair : idsWeightPair) {
            String[] idWeight = pair.split(GlobalConstants.UNDERLINE);
            ids.add(idWeight[0]);
        }
        return ids;
    }

    public static Map<Integer, Integer> parseIdWeightMap(String idsWeight) {
        Map<Integer, Integer> idsWeightMap = new HashMap<>();
        List<String> idsWeightPair = stringJoinToList(idsWeight);
        for (String pair : idsWeightPair) {
            String[] idWeight = pair.split(GlobalConstants.UNDERLINE);
            idsWeightMap.put(Integer.valueOf(idWeight[0]), Integer.valueOf(idWeight[1]));
        }
        return idsWeightMap;
    }

    public static Map<Integer, Long> parseIdQpsMap(String idsWeight) {
        Map<Integer, Long> idsWeightMap = new HashMap<>();
        List<String> idsWeightPair = stringJoinToList(idsWeight);
        for (String pair : idsWeightPair) {
            String[] idWeight = pair.split(GlobalConstants.UNDERLINE);
            idsWeightMap.put(Integer.valueOf(idWeight[0]), Long.valueOf(idWeight[1]));
        }
        return idsWeightMap;
    }

    public static List<String> stringJoinToList(String joinStr) {
        List<String> ids = new ArrayList<>();
        if (StringUtils.isEmpty(joinStr)) return ids;
        String[] idsStr = joinStr.split(",");
        Collections.addAll(ids, idsStr);
        return ids;
    }

    public static String createTaskNo(Integer id) {
        return GlobalConstants.TASK_PREFIX + nowMonthDay() + random(6) + "-" + id;
    }

    private static String nowMonthDay() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int data = calendar.get(Calendar.DAY_OF_MONTH);
        return String.valueOf(month) + String.valueOf(data);
    }

    public static String random(int length) {
        StringBuilder sb = new StringBuilder();
        String str = "123456789";
        Random r = new Random();
        for (int i = 0; i < length; i++) {
            int num = r.nextInt(str.length());
            sb.append(str.charAt(num));
            str = str.replace((str.charAt(num) + ""), "");
        }
        return sb.toString();
    }

    public static Integer parseTaskNoToId(String taskNo) {
        if (StringUtils.isEmpty(taskNo)) return 0;
        String[] pair = taskNo.split("-");
        return Integer.parseInt(pair[2]);
    }

    public static String getFormatDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    public static String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }


    public static String divideRate(long part, long total, int fractionDigits) {
        if (total == 0D) return "1";
        NumberFormat num = NumberFormat.getPercentInstance();
        num.setMaximumIntegerDigits(3);
        num.setMaximumFractionDigits(fractionDigits);
        return num.format((double) part / total);
    }

    public static String divideRateNoSign(long part, long total, int fractionDigits) {
        return divideRate(part, total, fractionDigits).replace("%", "");
    }


    public static String splitParams(String paramsHeader) {
        if (StringUtils.isBlank(paramsHeader)) return null;
        return paramsHeader.split(GlobalConstants.PARAMS_HEADER_SEGMENT)[0];
    }


    public static String splitHeader(String paramsHeader) {
        if (StringUtils.isBlank(paramsHeader)) return null;
        String[] pair = paramsHeader.split(GlobalConstants.PARAMS_HEADER_SEGMENT);
        if (pair.length == 2) {
            return pair[1];
        }
        return null;
    }


    public static String decodeParams(String preParam) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(preParam)) return null;
        String aftParam;
        if (preParam.contains(GlobalConstants.PARAMS_HEADER_SEGMENT)) {
            String[] paramsHeaderPair = preParam.split(GlobalConstants.PARAMS_HEADER_SEGMENT, 2);
            if (paramsHeaderPair.length == 2) {
                aftParam = URLDecoder.decode(paramsHeaderPair[0], GlobalConstants.URL_DECODER) + GlobalConstants.PARAMS_HEADER_SEGMENT + paramsHeaderPair[1];
            } else {
                aftParam = URLDecoder.decode(paramsHeaderPair[0], GlobalConstants.URL_DECODER);
            }
        } else {
            aftParam = URLDecoder.decode(preParam, GlobalConstants.URL_DECODER);
        }
        return aftParam.trim();
    }


    public static double divide(double a, double b, int scale, RoundingMode roundingMode) {
        if (b == 0D) return 1D;
        return new BigDecimal((float) a / b).setScale(scale, roundingMode).doubleValue();
    }

    public static int roundToInt(Double a) {
        return new BigDecimal(a).intValue();
    }


    public static boolean istLinux() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("mac") || osName.contains("win");
    }

    public static String getConfigEnv() {
        String currentEnv = System.getProperty("config_env");
        if (StringUtils.isEmpty(currentEnv)) {
            return "NOT_IDC";
        }
        return currentEnv;
    }

    public static boolean isIDC() {
        String currentEnv = System.getProperty("config_env");
        return "idc".equalsIgnoreCase(currentEnv);
    }
}
