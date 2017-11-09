package com.ah.bigdata.controller;

import com.ah.bigdata.config.ApiConfig;
import com.ah.bigdata.config.ImageConfig;
import com.ah.bigdata.dao.SIRecogSpeedDAO;
import com.ah.bigdata.model.Result;
import com.ah.bigdata.model.SIRecogSpeed;
import com.ah.bigdata.service.ImageProcessService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * @author liuf@ahtsoft.cn (刘法)
 * 2017/11/3
 * @version V1.0
 * com.ah.bigdata.controller
 * Company: 合肥安慧软件有限公司
 * Copyright: Copyright (c) 2017
 */
@Log
@Api(description = "图像二次识别接口")
@RestController
@RequestMapping("image")
public class ImageProcessController {
    private final SIRecogSpeedDAO siRecogSpeedDAO;
    private final ApiConfig apiConfig;
    private final ImageConfig imageConfig;
    private final ImageProcessService imageProcessService;

    @Autowired
    public ImageProcessController(ImageProcessService imageProcessService, ImageConfig imageConfig, ApiConfig apiConfig, SIRecogSpeedDAO siRecogSpeedDAO) {
        this.imageProcessService = imageProcessService;
        this.imageConfig = imageConfig;
        this.apiConfig = apiConfig;
        this.siRecogSpeedDAO = siRecogSpeedDAO;
    }

    /**
     * 处理单张图像
     * @param json json
     * @return json
     */
    @PostMapping("singleImage")
    public String processSingleImage(@RequestBody String json){
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(json);
        } catch (Exception e) {
            return Result.error("illegal params");
        }
        return imageProcessService.processSingleImage(jsonObject);
    }


    /**
     * 处理多张图片
     * @param json json
     * @return json
     */
    @PostMapping("batchImage")
    public String processBatchImage(@RequestBody String json){
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(json);
        } catch (Exception e) {
            return Result.error("illegal params");
        }
        return imageProcessService.processBatchImage(jsonObject);
    }

    /**
     * 处理多张图片并入库保存
     * @param json json
     * @return json
     */
    @PostMapping("batchImageAndSave")
    public String batchImageAndSave(@RequestBody String json, String batch){
        JSONObject jsonObject;
        try {
            jsonObject = JSONObject.parseObject(json);
        } catch (Exception e) {
            return Result.error("illegal params");
        }
        if (StringUtils.isEmpty(batch)){
            batch = "p-"+System.currentTimeMillis();
        }
        return imageProcessService.batchImageAndSave(jsonObject,batch);
    }


    /**
     * 开启多线程处理指定目录下的任务
     * @param taskCount 线程数
     * @param sharePath 共享目录路径
     * @return String
     */
    @GetMapping("startBatchImageTask/{taskCount}/{sharePath}")
    public String startBatchImageTask(@PathVariable(value = "taskCount") int taskCount, @PathVariable(value =
            "sharePath") String sharePath) {
        String path = "";
        if ("yn".equals(sharePath)){
            path = imageConfig.getYunNanPath();
        }
        if ("cz".equals(sharePath)) {
            path = imageConfig.getChuZhouPath();
        }
        if (StringUtils.isBlank(path)){
            return Result.error("illegal params: sharePath");
        }

        JSONArray arr = imageProcessService.getAllFiles(path,new JSONArray());


        List<List<String>> lstUrls = imageProcessService.splitArrays(arr);
        System.setProperty(
                "java.util.concurrent.ForkJoinPool.common.parallelism", "100");

        long a = System.currentTimeMillis();
        String batch = "p-"+System.currentTimeMillis();
        Bag<Integer> count = Bag.create();
        Bag<Integer> oldCount = Bag.create();
        count.value = 0;
        oldCount.value = 0;
        lstUrls.parallelStream().forEach(urls-> {
            batchImageAndSave(buildJson(urls),"p-"+System.currentTimeMillis());
            synchronized (count) {
                count.value += urls.size();

                if(count.value - oldCount.value > 100){
                    long cost = (System.currentTimeMillis() - a) / 1000;
                    System.out.println("累计 " + count.value + "条； 已用时：" + cost + "秒; " + (count.value / (cost == 0?1:cost)) + "条/秒;");
                    oldCount.value = count.value;
                }
            }
        });

        System.out.println("共耗时 " + (System.currentTimeMillis() - a)/1000 +"秒");

//        /*组数*/
//        int group = (arr.size() % taskCount == 0) ? (arr.size() / taskCount) : (arr.size() / taskCount + 1);
//
//        List<List> taskList = imageProcessService.splitArrays(arr,group);
//
//        final CountDownLatch begin = new CountDownLatch(1);
//        final ExecutorService exec = Executors.newFixedThreadPool(taskCount);
//
//        taskList.stream().parallel().forEach(l->{
//            String batch = "p-"+System.currentTimeMillis();
//            List<List> groupList = imageProcessService.splitArrays(l,8);
//            for (List list : groupList) {
//                batchImageAndSave(buildJson(list),batch);
//            }
//        });

//        for (List batchList : taskList) {
//            String batch = "p-"+System.currentTimeMillis();
//            Runnable run = new Runnable() {
//                public void run() {
//                    List<List> groupList = imageProcessService.splitArrays(batchList,8);
//                    log.info("每个线程分为："+groupList.size()+"组");
//                    for (List list : groupList) {
//                        /*for (Object path : list) {
//                            System.out.println(getImageHttpPath(path.toString()));
//                        }*/
//                        batchImageAndSave(buildJson(list),batch);
//                    }
//                }
//            };
//            exec.submit(run);
//        }
//        // begin减一，开始并发执行
//        begin.();
//        //关闭执行
//        exec.shutdown();
        return Result.success("please waiting...");
    }

    /**
     * 开启单线程处理指定目录下的任务
     * @param sharePath 共享目录路径
     * @return String
     */
    @GetMapping("startBatchImageTask/{sharePath}")
    public String startBatchImageTask( @PathVariable(value =
            "sharePath") String sharePath) {
        String path = "";
        if ("yn".equals(sharePath)){
            path = imageConfig.getYunNanPath();
        }
        if ("cz".equals(sharePath)) {
            path = imageConfig.getChuZhouPath();
        }
        if (StringUtils.isBlank(path)){
            return Result.error("illegal params: sharePath");
        }
        JSONArray arr = imageProcessService.getAllFiles(path,new JSONArray());
        String batch = "p-"+System.currentTimeMillis();

        SIRecogSpeed speed = new SIRecogSpeed();
        speed.setBatch_id(batch);
        speed.setStart_time(new Date());
        speed.setImage_count(arr.size());
        List<List> groupList = imageProcessService.splitArrays(arr,8);
        log.info("分为："+groupList.size()+"组");
        for (List list : groupList) {
            try {
                batchImageAndSave(buildJson(list),batch);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        speed.setEnd_time(new Date());
        double time = (speed.getEnd_time().getTime()-speed.getStart_time().getTime())/1000.0;
        speed.setSpeed(speed.getImage_count()/time);
        siRecogSpeedDAO.save(speed);
        return Result.success("success");
    }
    /**
     * 处理转换图片URL
     * @param path path
     * @return path
     */
    private String getImageHttpPath(String path){
        path = path.replace(imageConfig.getSharePath(),imageConfig.getPath()).replaceAll("\\\\", "/");
        try {
            path = URLEncoder.encode(path,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        path = path.replaceAll("%2F","/").replaceAll("%3A",":");
        return path;
    }

    /**
     * 构建多任务处理图片json
     * @return json
     */
    private String buildJson(List pathList){
        JSONObject json = new JSONObject();
        JSONObject contextJson = new JSONObject();
        contextJson.put("SessionId","session-"+System.currentTimeMillis());
        contextJson.put("Type",3);

        JSONArray functionArr = apiConfig.getFunctions();
        contextJson.put("Functions",functionArr);
        json.put("Context",contextJson);

        JSONArray imagesArr = new JSONArray();
        for (Object path : pathList) {
            JSONObject j = new JSONObject();
            JSONObject dataJson = new JSONObject();
            dataJson.put("URI",getImageHttpPath(path.toString()));
            j.put("Data",dataJson);
            imagesArr.add(j);
        }
        json.put("Images",imagesArr);
        return json.toJSONString();
    }
}


class Bag<T> {

    public T value;

    public static <T> Bag<T> create() {
        return new Bag<T>();
    }

    private Bag() {
    }
}