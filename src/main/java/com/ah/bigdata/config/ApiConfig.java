package com.ah.bigdata.config;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import sun.misc.IOUtils;

import java.io.IOException;

/**
 * Created by JoyLau on 2017/10/30.
 * com.ah.bigdata.util
 * 2587038142@qq.com
 */
@Component
@Data
@ConfigurationProperties(prefix = "api")
public class ApiConfig {
    private String host;

    private int port;

    private String addSourceURL;

    private String addTaskURL;

    private String startTaskURL;

    private String searchVehicleURL;

    private String getSourceURL;

    private String delSourceURL;

    private String getTaskURL;

    private String delTaskURL;

    private String addRepoURL;

    private String getRepoURL;

    private String delRepoURL;

    /**
     * 从json文件转化为json对象
     * @param resource 文件
     * @return json
     */
    public JSONObject getJsonByFile(Resource resource){
        String json = null;
        try {
            json = new String(IOUtils.readFully(resource.getInputStream(), -1,true));
            JSONObject.parseObject(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSONObject.parseObject(json);
    }

    @Value("classpath:static/json/user_brand_attribute.json")
    Resource brand;
    public JSONObject getBrand(){
        return getJsonByFile(brand);
    }
}
