package com.yunji.titanrtx.log.agent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.Tailer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class KafkaProducerTest {

    public static void main(String[] args) throws InterruptedException, IOException {

        Properties props = new Properties();
        props.put("bootstrap.servers","127.0.0.1:9092");
        props.put("acks", "0");
        props.put("retries", 0);
        props.put("batch.size", 200);
        props.put("linger.ms", 100);
        props.put("buffer.memory", 1024*1024*32 );
        props.put("compression.type","lz4");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("max.in.flight.requests.per.connection","5");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        CountDownLatch count = new CountDownLatch(1);

        Thread thread = new Thread(new Tailer(new File("E:\\yunjiapp.log"), new AccessLogTail(producer)));
        thread.start();

        count.await();

       /* List<String> list = IOUtils.readLines(new FileReader("E:\\yunjiapp.log"));
        for (String s : list){
            producer.send(new ProducerRecord<String, String>(topic,s));
            Thread.sleep(1000);
        }*/

/*        for (int i= 0 ;; i ++){
            producer.send(new ProducerRecord<String, String>(topic,"[19/Mar/2019:14:22:25 +0800] vipapp.yunjiglobal.com /yunjiapp4buyer/app4buyer/bubble/getBubbleShowBoCache.json?ticket= HIT 200 - - - 0.000"));
            //producer.send(new ProducerRecord<String, String>(topic,"[19/Mar/2019:14:22:25 +0800] vipapp.yunjiweidian.com /favicon.ico - "+200+" - - - "+0.1*i));
            //producer.send(new ProducerRecord<String, String>(topic,"[19/Mar/2019:14:22:25 +0800] vipapp.yunjiglobal.com /yunjiapp4buyer/app4buyer/bubble/getBubbleShowBoCache.json?ticket= HIT 200 - - - 0.000"));
            Thread.sleep(100);
        }*/

    }
}
