package com.ah.bigdata.controller;

import com.ah.bigdata.service.SecondaryIdentificationService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by JoyLau on 2017/10/30.
 * com.ah.bigdata.controller
 * 2587038142@qq.com
 */
@RestController
@RequestMapping("si")
public class SecondaryIdentificationController {

    @Autowired
    private SecondaryIdentificationService service;

    @RequestMapping("addSource")
    public String addSource(){
        return service.addSource();
    }

    @RequestMapping("addTask")
    public String addTask(){
        return service.addTask();
    }

    @RequestMapping("startTask")
    public String startTask(){
        return service.startTask(5);
    }

    @RequestMapping("saveVehicle")
    public String saveVehicle(){
        return service.saveVehicle();
    }

    @RequestMapping("searchVehicle")
    public String searchVehicle(){
        return service.searchVehicle();
    }

    @RequestMapping("delAllSource")
    public String delAllSource(){
        return service.delAllSource();
    }

    @RequestMapping("delAllRepo")
    public String delAllRepo(){
        return service.delAllRepo();
    }

    @RequestMapping("delAllTask")
    public String delAllTask(){
        return service.delAllTask();
    }

    @RequestMapping("getAllFolderName")
    public JSONObject getAllFolderName(){
        return service.getAllFolderName();
    }

    @RequestMapping("addAllSourceForYunNan")
    public JSONObject addAllSourceForYunNan(){
        return service.addAllSourceForYunNan();
    }

    @RequestMapping("addAllRepoForYunNan")
    public JSONObject addAllRepoForYunNan(){
        return service.addAllRepoForYunNan();
    }

    @RequestMapping("addAllTaskForYunNan")
    public JSONObject addAllTaskForYunNan(){
        return service.addAllTaskForYunNan();
    }

    @RequestMapping("saveRecodeForYunNan")
    public JSONObject saveRecodeForYunNan(){
        return service.saveRecodeForYunNan();
    }

}
