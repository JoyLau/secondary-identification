package com.ah.bigdata.controller;

import com.ah.bigdata.dao.SampRecogDAO;
import com.ah.bigdata.model.SampRecog;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by JoyLau on 2017/10/26.
 * com.ah.bigdata.controller
 * 2587038142@qq.com
 */
@Controller
@RequestMapping("samprecog")
@NoArgsConstructor
public class SampRecogController {

    @Autowired
    private SampRecogDAO sampRecogDAO;

    @RequestMapping("/test")
    public void test(){
        SampRecog sampRecog = new SampRecog();
        sampRecog.setBrand("品牌");
        sampRecogDAO.save(sampRecog);
    }
}
