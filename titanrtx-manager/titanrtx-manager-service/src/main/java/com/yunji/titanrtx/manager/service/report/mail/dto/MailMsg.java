package com.yunji.titanrtx.manager.service.report.mail.dto;

import lombok.Data;

import java.util.List;

@Data
public class MailMsg {

    private String subject;

    private String content;


    private List<MailAttach> attachList;


    public MailMsg(String subject, String content, List<MailAttach> attachList) {
        this.subject = subject;
        this.content = content;
        this.attachList = attachList;
    }
}
