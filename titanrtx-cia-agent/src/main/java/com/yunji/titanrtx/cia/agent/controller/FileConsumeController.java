package com.yunji.titanrtx.cia.agent.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.yunji.titanrtx.cia.agent.message.kafka.LogKafkaMessage;
import com.yunji.titanrtx.common.message.RespMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * FileConsumeController
 *
 * @author leihz
 * @since 2020-08-20 10:30 上午
 */
@RestController
@Slf4j
public class FileConsumeController {

    @Autowired
    private LogKafkaMessage logKafkaMessage;

    @RequestMapping("/consume")
    public Object fileConsume(String path) {
        long st = System.currentTimeMillis();

        File file = new File(path);
        if (!file.exists()) {
            return RespMsg.respErr("指定文件路径文件不存在,path: " + path);
        }
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        ) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                count++;
                HashMap<String, String> map = Maps.newHashMap();
                map.put("message", line);
                String msg = JSON.toJSONString(map);
                logKafkaMessage.consumeFileMessage(msg);
            }

            long cost = System.currentTimeMillis() - st;
            log.info("文件内容消费完成,共完成 {}条, 耗时:{}. ", count, cost);

            return RespMsg.respSuc("消费 " + count + " 条,耗时: " + cost + "ms.");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return RespMsg.respErr(e.getMessage());
        }
    }
}
