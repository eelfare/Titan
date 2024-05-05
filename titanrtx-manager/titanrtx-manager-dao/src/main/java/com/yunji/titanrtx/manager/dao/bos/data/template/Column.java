package com.yunji.titanrtx.manager.dao.bos.data.template;

import lombok.Data;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 17:30
 * @Version 1.0
 */
@Data
public class Column {
    /**
     * 列名
     */
    private String name;
    /**
     * 数据来源方式（单值）
     */
    private String sourceExpr;
    /**
     * 数据遍历表达式,通过什么模式将数据组装好（多值）
     */
    private String iteratorExpr;
}
