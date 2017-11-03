package com.ah.bigdata.kafka;

import com.ah.bigdata.service.SecondaryIdentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Created by JoyLau on 2017/10/31.
 * com.ah.bigdata.kafka
 * 2587038142@qq.com
 */
@Component
public class MsgConsumer {
    @Autowired
    SecondaryIdentificationService service;
    @KafkaListener(topics = {"index-vehicle"})
    public void processMessage(String content) {
        System.out.println(content);
    }
}
