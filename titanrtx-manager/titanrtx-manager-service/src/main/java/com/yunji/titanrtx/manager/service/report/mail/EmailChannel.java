package com.yunji.titanrtx.manager.service.report.mail;


import com.yunji.titanrtx.common.u.DateU;
import com.yunji.titanrtx.manager.service.report.mail.dto.MailAttach;
import com.yunji.titanrtx.manager.service.report.mail.dto.MailMsg;
import lombok.extern.slf4j.Slf4j;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yunji.titanrtx.manager.service.report.support.Constants.*;

@Slf4j
public class EmailChannel {

    public static void sendTitanReport(String path, List<String> toUsers) {
        List<MailAttach> attachList = Stream.of(
                new File(path))
                .map(x ->
                        new MailAttach("Titan压测报告-" + x.getName(), x.getAbsolutePath()))
                .collect(Collectors.toList());

        MailMsg msg = new MailMsg(
                "Titan自动化压测报告" + DateU.getCurrentTime(),
                "Dear All:<br/> <strong>Titan压测报告-" + DateU.getCurrentTime() + "</strong><br/>" +
                        "&nbsp;&nbsp; Titan压测记录报告见附件.<br/>",
                attachList
        );

        if (toUsers.isEmpty()) {
            send(msg, Arrays.asList("leihz@yunjiglobal.com"));
        } else {
            send(msg, toUsers);
        }
    }


    public static void send(MailMsg mailMsg, List<String> mailAddrList) {
        try {
            Properties properties = System.getProperties();
            // 设置邮件服务器
            properties.setProperty(NOTICE_EMAIL_SMTP_HOST_KEY, NOTICE_EMAIL_SMTP_HOST);
            properties.put(MAIL_SMTP_TIMEOUT_KEY, 10000);

            Session session = Session.getDefaultInstance(properties);

            Address[] addresses = new Address[mailAddrList.size()];
            for (int i = 0; i < mailAddrList.size(); i++) {
                addresses[i] = new InternetAddress(mailAddrList.get(i));
            }

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(NOTICE_EMAIL_FROM, "[Titan报告]"));
            message.addRecipients(Message.RecipientType.TO, addresses);
            message.setSubject(mailMsg.getSubject());

            //整封邮件的MINE消息体
            //混合的组合关系
            MimeMultipart msgMultipart = new MimeMultipart("mixed");
            //设置邮件的MINE消息体
            message.setContent(msgMultipart);

            mailMsg.getAttachList().forEach(attach -> {
                try {
                    MimeBodyPart mimeAttach = new MimeBodyPart();
                    //把文件添加到附件中,数据源
                    FileDataSource ds = new FileDataSource(new File(attach.getPath()));
                    //数据处理器
                    DataHandler dsHandler = new DataHandler(ds);
                    //设置第一个附件的数据
                    mimeAttach.setDataHandler(dsHandler);

                    //设置第一个附件的文件名
                    //        mimeAttach.setFileName(MimeUtility.encodeText(attach.name));
                    mimeAttach.setFileName(attach.getName());
                    msgMultipart.addBodyPart(mimeAttach);
                } catch (Exception e) {
                    throw new RuntimeException("Mail attach error.");
                }
            });
            //正文内容
            MimeBodyPart content = new MimeBodyPart();

            msgMultipart.addBodyPart(content);


            //正文（图片和文字部分）
            MimeMultipart bodyMultipart = new MimeMultipart("related");
            //设置内容为正文
            content.setContent(bodyMultipart);
            //html代码部分
            MimeBodyPart htmlPart = new MimeBodyPart();

            bodyMultipart.addBodyPart(htmlPart);

            //html代码
            htmlPart.setContent(mailMsg.getContent(), "text/html;charset=utf-8");
            //生成文件邮件
            message.saveChanges();
            Transport.send(message);
//            Transport.send(message, "beacon", "yunji@2019");
        } catch (Exception e) {
            log.error("发送邮件失败,原因: " + e.getMessage(), e);
        }
    }
}
