package com.yunji.titanrtx.log.agent;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class AccessLogTail extends TailerListenerAdapter {


    private KafkaProducer<String, String> producer;

    String topic = "titanrtx_top_link";


    public AccessLogTail(KafkaProducer<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public void handle(String line) {
        producer.send(new ProducerRecord<String, String>(topic,line));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
