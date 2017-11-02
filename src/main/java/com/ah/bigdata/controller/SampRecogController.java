package com.ah.bigdata.controller;

import com.ah.bigdata.config.ApiConfig;
import com.ah.bigdata.dao.SIRecogDAO;
import com.ah.bigdata.service.SecondaryIdentificationService;
import com.alibaba.fastjson.JSONObject;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by JoyLau on 2017/10/26.
 * com.ah.bigdata.controller
 * 2587038142@qq.com
 */
@Controller
@RequestMapping("test")
@NoArgsConstructor
public class SampRecogController {

    @Autowired
    private SIRecogDAO sampRecogDAO;

    @Autowired
    private SecondaryIdentificationService service;

    @Autowired
    private ApiConfig apiConfig;

    @Value("classpath:static/json/user_brand_attribute.json")
    Resource brand;

    @GetMapping("/test")
    @ResponseBody
    public String test(){
        JSONObject json = apiConfig.getJsonByFile(brand);
        json.size();
        return "";
    }
}
