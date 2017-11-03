package com.ah.bigdata.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author liuf@ahtsoft.cn (刘法)
 * @version V1.0
 * com.ah.bigdata.kafka
 * Company: 合肥安慧软件有限公司
 * Copyright: Copyright (c) 2017
 * 2017/11/2
 */
@Component
public class MsgProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage() {
        kafkaTemplate.send("index-vehicle","key","hello,kafka"  + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
    }
}
