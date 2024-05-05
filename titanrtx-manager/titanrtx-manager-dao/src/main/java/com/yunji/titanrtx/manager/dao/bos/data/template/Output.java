package com.yunji.titanrtx.manager.dao.bos.data.template;

import lombok.Data;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 17:15
 * @Version 1.0
 */
@Data
public class Output {
    private String table;               // 数据存储的数据表名
    private List<Column> fields;       // 列信息
}
