package com.ah.bigdata.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class Result {
    private boolean success;
    private String message;

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Result() {
    }

    public static String error(String msg) {
        return JSONObject.toJSONString(new Result(false,msg));
    }

    public static String success(String msg) {
        return JSONObject.toJSONString(new Result(true,msg));
    }
}
