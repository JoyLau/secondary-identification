package com.ah.bigdata.api.v1;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Created by joylau on 2017/10/13.
 * com.ah.bigdata.api.v1
 * 2587038142@qq.com
 */
@CrossOrigin
@RestController
@RequestMapping("/apis/v1")
@AllArgsConstructor
public class ApisV1Controller {
    private RestTemplate restTemplate;
}
