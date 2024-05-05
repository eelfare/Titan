package com.yunji.titanrtx.agent;

import org.junit.Test;

public class GetParamsTest {

    @Test
    public void testCeil() {
        int selectedCount = 2;
        int[] paramIds = {1};

        double ceil = Math.ceil(paramIds.length / (1000 * 1.0) + 1);

        System.out.println("ceil: " + ceil);

        int temp = selectedCount % (int) ceil;
        System.out.println(temp);
    }
}
