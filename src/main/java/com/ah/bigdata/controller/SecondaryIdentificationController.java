package com.ah.bigdata.controller;

import com.ah.bigdata.model.Result;
import com.ah.bigdata.service.SecondaryIdentificationService;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by JoyLau on 2017/10/30.
 * com.ah.bigdata.controller
 * 2587038142@qq.com
 */
@Api(description = "二次识别接口(已弃用)")
@RestController
@RequestMapping("si")
public class SecondaryIdentificationController {

    @Autowired
    private SecondaryIdentificationService service;

    @PostMapping("addSource")
    public String addSource(@RequestBody String json){
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(json);
        } catch (Exception e) {
            return Result.error("illegal params");
        }
        return service.addSource(jsonObject);
    }

    @PostMapping("addTask")
    public String addTask(@RequestBody String json){
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(json);
        } catch (Exception e) {
            return Result.error("illegal params");
        }
        return service.addTask(jsonObject);
    }

    @GetMapping("startTask/{taskId}")
    public String startTask(@PathVariable(name = "taskId") int taskId){
        return service.startTask(taskId);
    }

    @PostMapping("saveVehicle")
    public String saveVehicle(@RequestBody String json){
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(json);
        } catch (Exception e) {
            return Result.error("illegal params");
        }
        return service.saveVehicle(jsonObject);
    }

    @PostMapping("searchVehicle")
    public String searchVehicle(@RequestBody String json){
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(json);
        } catch (Exception e) {
            return Result.error("illegal params");
        }
        return service.searchVehicle(jsonObject);
    }

    @DeleteMapping("delAllSource")
    public String delAllSource(){
        return service.delAllSource();
    }

    @DeleteMapping("delAllRepo")
    public String delAllRepo(){
        return service.delAllRepo();
    }

    @DeleteMapping("delAllTask")
    public String delAllTask(){
        return service.delAllTask();
    }

    @GetMapping("getAllFolderName")
    public JSONObject getAllFolderName(){
        return service.getAllFolderName();
    }

    @GetMapping("addAllSourceForYunNan")
    public JSONObject addAllSourceForYunNan(){
        return service.addAllSourceForYunNan();
    }

    @GetMapping("addAllRepoForYunNan")
    public JSONObject addAllRepoForYunNan(){
        return service.addAllRepoForYunNan();
    }

    @GetMapping("addAllTaskForYunNan")
    public JSONObject addAllTaskForYunNan(){
        return service.addAllTaskForYunNan();
    }

//    @GetMapping("saveRecodeForYunNan")
//    public JSONObject saveRecodeForYunNan(){
//        return service.saveRecodeForYunNan();
//    }

}
