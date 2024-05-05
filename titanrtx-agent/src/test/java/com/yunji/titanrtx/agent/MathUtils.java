package com.yunji.titanrtx.agent;

/**
 * MathUtils
 *
 * @author leihz
 * @since 2020-05-27 11:53 上午
 */
public class MathUtils {


    public static void main(String[] args) {
        int paramSize = 10003 - 5;
        int lumpSize = 121;
        double v = paramSize / (lumpSize * 1.0) + 1;

        double ceil = Math.ceil(v);

        System.out.println(v);
        System.out.println(ceil);

        int x = 2;
        double y = x % ceil;


        System.out.println(y);
    }
}
