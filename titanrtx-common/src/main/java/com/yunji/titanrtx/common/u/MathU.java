package com.yunji.titanrtx.common.u;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * MathU
 *
 * @author leihz
 * @since 2020-05-25 3:37 下午
 */
public class MathU {

    /**
     * @param dividend 被除数
     * @param divisor  除数
     */
    public static Double calculatePercentRate(long dividend, long divisor) {
        BigDecimal result = calculateDivide(dividend, divisor);
        return result.multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    /**
     * @param dividend 被除数
     * @param divisor  除数
     */
    public static BigDecimal calculateDivide(long dividend, long divisor) {
        return BigDecimal.valueOf(dividend).divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
    }

}
