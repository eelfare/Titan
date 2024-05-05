package com.yunji.titanrtx.manager.dao.bos.data.template;

import lombok.Data;

/**
 * 参数描述
 *
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 17:12
 * @Version 1.0
 */
@Data
public class Param {
    /**
     * 参数名
     */
    private String name;
    /**
     * 代表单个结果,例子如下:
     * source="database=>tickets=>ticket" 代表从数据库表中获取,表名为 tickets, 需要获取的列的名称是 ticket
     */
    private String sourceExpr;

    /**
     * 代表多个结果的情况
     * from : table("yunbi").filter().map(_.yunbiId)
     */
    private String iteratorExpr;
    /**
     * 参数使用方式(0：可重用；1：不可重用；2：随机)
     */
    private Integer useType;
    /**
     * 过滤条件组
     */
    private Filter filter;
}
