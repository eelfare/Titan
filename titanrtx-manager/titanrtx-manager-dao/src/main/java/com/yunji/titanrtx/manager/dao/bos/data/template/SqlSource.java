package com.yunji.titanrtx.manager.dao.bos.data.template;

import lombok.Data;

import java.util.List;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 17:21
 * @Version 1.0
 */
@Data
public class SqlSource {
    private String table;               // 表名
    private String column;              // 列名
    private List<Filter> filters;              // 过滤条件组
}
