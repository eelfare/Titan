package com.yunji.titanrtx.manager.web;

import org.apache.dubbo.common.utils.NetUtils;

/**
 * IPTest
 *
 * @author leihz
 * @since 2020-09-11 6:55 下午
 */
public class IPTest {
    public static void main(String[] args) {
        String localHost = NetUtils.getLocalHost();
        System.out.println(localHost);
    }
}
