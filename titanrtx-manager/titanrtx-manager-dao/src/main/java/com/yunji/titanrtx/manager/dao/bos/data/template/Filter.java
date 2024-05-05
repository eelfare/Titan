package com.yunji.titanrtx.manager.dao.bos.data.template;

import lombok.Data;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-12-31 17:22
 * @Version 1.0
 */
@Data
public class Filter {
    /**
     * 根据 name 名称进行过滤 ，例如 ticket,consumerId
     */
    private String name;
    /**
     * 通过表达式获取到，当前结果中对应name 的值, 例如元祖 expr -> param._1 得到 ticket ，然后和上下文中的 ticket 进行比较和过滤
     */
    private String expr;
}
