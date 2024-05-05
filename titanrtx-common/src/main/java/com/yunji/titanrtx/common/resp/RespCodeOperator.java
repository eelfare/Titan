package com.yunji.titanrtx.common.resp;

import com.alibaba.fastjson.JSON;
import com.yunji.titanrtx.common.domain.task.Pair;
import lombok.Data;

/**
 * RespCodeOperator 解析返回结果 实体 和 code
 *
 * @author leihz
 * @since 2020-09-25 6:44 下午
 */
public class RespCodeOperator {
    @Data
    public static class YunJiRespCode {
        private Integer errorCode;
        private Integer code;
    }

    @Data
    public static class YunJiRespDomain {

        private Integer errorCode;

        private Integer code;

        private String errorMessage;
        private String data;

    }

    public static Integer getRespCode(String retString) {
        Integer errorCode = -1;
        if (retString == null) {
            return errorCode;
        }
        try {
            YunJiRespCode respCode = JSON.parseObject(retString, YunJiRespCode.class);
            errorCode = respCode.getCode() != null ? respCode.getCode() : respCode.getErrorCode();
        } catch (Exception ignored) {
        }
        return errorCode;
    }

    public static Pair<Integer, YunJiRespDomain> getRespCodeAndDomain(String retString) {
        Integer errorCode = -1;
        if (retString == null) {
            return new Pair<>(-1, null);
        }
        try {
            YunJiRespDomain respDomain = JSON.parseObject(retString, YunJiRespDomain.class);
            errorCode = respDomain.getCode() != null ? respDomain.getCode() : respDomain.getErrorCode();
            return new Pair<>(errorCode, respDomain);
        } catch (Exception ignored) {
        }
        return new Pair<>(-1, null);
    }

}
