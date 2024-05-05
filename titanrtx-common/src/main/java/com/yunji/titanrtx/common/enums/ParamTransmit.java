package com.yunji.titanrtx.common.enums;

/**
 * ParamTransmit
 * Manager 进行大链路压测时(即一个链路可能时写压测，其携带的参数量非常大，比如500w),因此需要界定选择参数的传递方式.
 *
 * @author leihz
 * @since 2020-05-22 10:25 上午
 */
public enum ParamTransmit {
    /**
     * manager 将链路下的参数的所有id查出来,传递 id 列表。
     * 当参数过多时，比如几千万的参数量，在传送时，传送包可能过大.
     */
    IDS("IDS"),
    /**
     * 对已排好序的链路对应的链路参数，只要针对每个链路的参数传递起始和停止的区间值即可,例如10w个参数[1-10_0000]
     */
    ORDERS("ORDERS"),
    /**
     * 直接查出参数来,然后传递参数,针对链路参数不多的情况使用.10w以内。
     */
    PARAM_SELF("PARAM_SELF");


    private String memo;

    ParamTransmit(String memo) {
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public static void main(String[] args) {
        ParamTransmit orders = ParamTransmit.valueOf("ORDERS");

        System.out.println(orders);
    }
}
