package com.yunji.titanrtx.manager.service.report.mail.dto;

import lombok.Data;

@Data
public class MailAttach {
    /**
     * 附件名称
     */
    private String name;

    /**
     * 附件文件路径，绝对路径
     */
    private String path;


    public MailAttach(String name, String path) {
        this.name = name;
        this.path = path;
    }
}
