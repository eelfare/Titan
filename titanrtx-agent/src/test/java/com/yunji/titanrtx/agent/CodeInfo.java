package com.yunji.titanrtx.agent;

import lombok.Data;

/**
 * @Author: 景风（彭秋雁）
 * @Date: 2019-10-29 20:30
 * @Version 1.0
 */
@Data
public class CodeInfo {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String code;
    private String descr;
    private String descrE;
    private String createdBy;
    private Long createdAt;

    private String time;
    private String tagCode;
    private String tagName;
}
