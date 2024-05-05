package com.yunji.titanrtx.manager.web;

import com.yunji.titanrtx.common.resp.RespCodeOperator;

/**
 * JSONTest
 *
 * @author leihz
 * @since 2020-05-20 3:44 下午
 */
public class JSONTest {

    public static void main(String[] args) {
        Object res = RespCodeOperator.getRespCodeAndDomain("{\"errorCode\":0,\"errorMessage\":\"获取用户开关成功\",\"data\":{\"appBannerReminder\":0,\"shareSwitchFlag\":1,\"advertisingInterceptionSwitch\":1,\"balanceSwitch\":0,\"vipToShopSwitch\":1,\"itemRegisterSwitch\":0,\"compatibilityModeSwitch\":0,\"timeAxisSwitch\":0}}\n" +
                "\n");
        System.out.println(res);
    }
}
