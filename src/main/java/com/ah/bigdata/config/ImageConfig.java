package com.ah.bigdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by JoyLau on 2017/10/31.
 * com.ah.bigdata.util
 * 2587038142@qq.com
 */
@Data
@Component
@ConfigurationProperties(prefix = "image")
public class ImageConfig {
    private List<String> urls;

    private String absolutePath;

    private String path;
}
