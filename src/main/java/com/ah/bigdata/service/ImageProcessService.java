package com.ah.bigdata.service;

import com.ah.bigdata.config.ApiConfig;
import com.ah.bigdata.dao.SIRecogDAO;
import com.ah.bigdata.model.Result;
import com.ah.bigdata.model.SIRecog;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author liuf@ahtsoft.cn (刘法)
 * 2017/11/3
 * @version V1.0
 * com.ah.bigdata.service
 * Company: 合肥安慧软件有限公司
 * Copyright: Copyright (c) 2017
 */
@Service
public class ImageProcessService {

    private final ApiConfig apiConfig;

    private final RestTemplate restTemplate;

    private final SecondaryIdentificationService service;

    private final SIRecogDAO recogDAO;

    @Autowired
    public ImageProcessService(ApiConfig apiConfig, RestTemplate restTemplate, SecondaryIdentificationService service, SIRecogDAO recogDAO) {
        this.apiConfig = apiConfig;
        this.restTemplate = restTemplate;
        this.service = service;
        this.recogDAO = recogDAO;
    }
    /**
     * 处理单张图像
     * @param jsonObject json
     * @return json
     */
    public String processSingleImage(JSONObject jsonObject) {
        return restTemplate.postForObject(apiConfig.getSingleImageURL(),jsonObject,String.class);
    }

    /**
     * 处理多张图片
     * @param jsonObject json
     * @return json
     */
    public String processBatchImage(JSONObject jsonObject) {
        return restTemplate.postForObject(apiConfig.getBatchImageURL(),jsonObject,String.class);
    }

    /**
     * 批处理代码入库存储
     * @param json json
     */
    public String batchImageAndSave(JSONObject json,String batch) {
        String jsonStr = processBatchImage(json);
        Date recDate = new Date();
        JSONObject sourceJSON = JSONObject.parseObject(jsonStr);
        JSONArray arr = sourceJSON.getJSONArray("Results");
        if (arr.size() == 0) {
            return Result.error("no results");
        }
        List<SIRecog> list = new ArrayList<>();
        for (Object o : arr) {
            JSONObject obj = JSONObject.parseObject(o.toString());
            list = saveImage(sourceJSON,obj,batch,recDate,list);
        }
        recogDAO.save(list);
        return Result.success("success,date size:"+arr.size());
    }


    /**
     * 单张图片结果处理入库存储
     * @param sourceJSON 返回的元JSON数据
     * @param json 遍历结果的单个json数据
     * @param batch 批次
     * @param recDate 该批次开始存储的时间
     * @param list 存放的集合
     * @return 集合
     */
    private List<SIRecog> saveImage(JSONObject sourceJSON,JSONObject json,String batch,Date recDate,List<SIRecog> list){
        String imageUrl = json.getJSONObject("Image").getJSONObject("Data").getString("URI");

        SIRecog siRecog = new SIRecog();
        try {
            siRecog.setSrc(service.getFolderIndexName(imageUrl, 1));
            siRecog.setBatch(batch);
            siRecog.setDevice_type(service.getFolderIndexName(imageUrl, 2));
            siRecog.setImg_url(service.getImageURI(imageUrl));
            siRecog.setPass_time(service.getPassDate(imageUrl));
            siRecog.setPlate_nbr(service.getPlateNo(imageUrl));
            siRecog.setPlate_color(service.getPlateColor(imageUrl));

            siRecog.setRecog_plate_nbr(getRPlateNo(json));
            siRecog.setRecog_brand(getRBrand(json));
            siRecog.setRecog_veh_type(getRVenType(json));
            siRecog.setRecog_veh_color(getRVenColor(json));
            siRecog.setRecog_plate_color(getRPlateColor(json));
            executeSymbols(siRecog, json);
            siRecog.setRecog_veh_head_pos(getRVehHeadPos(json));

            siRecog.setCreate_time(new Date());
            siRecog.setRec_time(recDate);
            siRecog.setSession_id(getSessionId(sourceJSON));
            siRecog.setMessage(getMessage(sourceJSON));
        } catch (Exception e) {
            e.printStackTrace();
        }

        list.add(siRecog);
        return list;
    }

    /**
     * 获取返回的是否成功的消息
     * @param json json
     * @return String
     */
    private String getMessage(JSONObject json){
        return getContext(json).getString("Message");
    }

    /**
     * 获取一组数据所花费的时间
     * @param json json
     * @return time
     */
    private double getCostTime(JSONObject json) {
        JSONObject requestTs = getContext(json).getJSONObject("RequestTs");
        JSONObject responseTs = getContext(json).getJSONObject("ResponseTs");

        long reqSeconds = requestTs.getLongValue("Seconds");
        long reqNanoSecs = requestTs.getLongValue("NanoSecs");
        long resSeconds = responseTs.getLongValue("Seconds");
        long resNanoSecs = responseTs.getLongValue("NanoSecs");

        DecimalFormat decimalFormat=new DecimalFormat(".00");

        Double res = resSeconds + Double.valueOf(decimalFormat.format(resNanoSecs / 100000000.00));
        Double req = reqSeconds + Double.valueOf(decimalFormat.format(reqNanoSecs/100000000.00));
        return Double.valueOf(decimalFormat.format(res - req));
    }

    /**
     * 返回sessionID
     * @param json json
     * @return json
     */
    private String getSessionId(JSONObject json){
        return getContext(json).getString("SessionId");
    }

    /**
     * 获取Context对象
     * @param json json
     * @return json
     */
    private JSONObject getContext(JSONObject json){
        return json.getJSONObject("Context");
    }
    /**
     * 获取车牌的信息
     *
     * @param json json
     * @return JSON
     */
    private JSONObject getRPlateInfo(JSONObject json) {
        JSONArray arr = getVehicles(json).getJSONArray("Plates");
        if (arr == null ){
            return new JSONObject();
        }
        return (JSONObject) arr.get(0);
    }

    /**
     * 获取识别的车牌号码
     *
     * @param json json
     * @return String
     */
    private String getRPlateNo(JSONObject json) {
        return getRPlateInfo(json).getString("PlateText");
    }


    /**
     * 获取识别的车牌的颜色
     *
     * @param json json
     * @return String
     */
    private String getRPlateColor(JSONObject json) {
        if ( getRPlateInfo(json).getJSONObject("Color") == null ) {
            return "";
        }
        return getRPlateInfo(json).getJSONObject("Color").getString("ColorName");
    }

    /**
     * 获取车型，车品牌，子品牌，出厂时间等一系列信息的JSON对象
     *
     * @param json json
     * @return json
     */
    private JSONObject getModelType(JSONObject json) {
        if (getVehicles(json).getJSONObject("ModelType") == null) {
            return new JSONObject();
        }
        return getVehicles(json).getJSONObject("ModelType");
    }

    /**
     * 获取的识别的品牌，主品牌+ 子品牌+年份
     *
     * @param json json
     * @return String
     */
    private String getRBrand(JSONObject json) {
        JSONObject model = getModelType(json);
        String brandStr = model.getString("Brand");
        String subBrandStr = model.getString("SubBrand");
        String year = model.getString("ModelYear");
        return brandStr + "--" + subBrandStr+"--"+year;
    }

    /**
     * 获取识别的车辆类型
     *
     * @param json json
     * @return String
     */
    private String getRVenType(JSONObject json) {
        JSONObject model = getModelType(json);
        return model.getString("Style");
    }

    /**
     * 获取识别的车身颜色
     *
     * @param json json
     * @return String
     */
    private String getRVenColor(JSONObject json) {
        if (getVehicles(json).getJSONObject("Color") == null) {
            return "";
        }
        JSONObject color = getVehicles(json).getJSONObject("Color");
        return color.getString("ColorName");
    }

    /**
     * 获取年检标，挂饰，纸巾盒等JSON对象
     *
     * @param json json
     * @return JSON
     */
    private JSONArray getSymbols(JSONObject json) {
        return getVehicles(json).getJSONArray("Symbols");
    }

    /**
     * 处理年检标，挂饰，纸巾盒等饰物进行存储
     *
     * @param siRecog 对象
     * @param json    json
     */
    private void executeSymbols(SIRecog siRecog, JSONObject json) {
        JSONArray symbols = getSymbols(json);
        if (symbols == null) {
            return;
        }
        StringBuilder sticker = new StringBuilder();
        StringBuilder pend = new StringBuilder();
        StringBuilder box = new StringBuilder();
        for (Object symbol : symbols) {
            JSONObject symbolJSON = JSONObject.parseObject(symbol.toString());
            if (symbolJSON.getIntValue("SymbolId") == 1) {
                //年检标
                sticker.append(service.getCutboardInfo(symbolJSON.getJSONObject("Cutboard"))).append(";");
            }
            if (symbolJSON.getIntValue("SymbolId") == 3) {
                //挂件
                pend.append(service.getCutboardInfo(symbolJSON.getJSONObject("Cutboard"))).append(";");
            }
            if (symbolJSON.getIntValue("SymbolId") == 4) {
                //纸巾盒
                box.append(service.getCutboardInfo(symbolJSON.getJSONObject("Cutboard"))).append(";");
            }
        }
        siRecog.setRecog_sticker_pos(sticker.toString());
        siRecog.setRecog_pend_pos(pend.toString());
        siRecog.setRecog_box_pos(box.toString());
    }

    /**
     * 递归遍历指定路径下的文件
     * @param path 路径
     * @param jsonArray array
     * @return array
     */
    public JSONArray getAllFiles(String path, JSONArray jsonArray) {
        File f = new File(path);
        if (f.isDirectory()) {
            File[] fList = f.listFiles();
            assert fList != null;
            for (File aFList : fList) {
                if (aFList.isDirectory()) {
                    getAllFiles(aFList.getPath(), jsonArray);
                }else{
                    jsonArray.add(aFList.getPath());
                }
            }
        }
        return jsonArray;
    }
    /**
     * 获取车身位置
     *
     * @param json josn
     * @return String
     */
    private String getRVehHeadPos(JSONObject json) {
        if (getVehicles(json).getJSONObject("Img") == null) {
            return "";
        }
        return service.getCutboardInfo(getVehicles(json).getJSONObject("Img").getJSONObject("Cutboard"));
    }

    /**
     * 获取机动车对象
     * @return json
     */
    private JSONObject getVehicles(JSONObject json) {
        if(json.getJSONArray("Vehicles") == null){
            return new JSONObject();
        }
        return ((JSONObject) (json.getJSONArray("Vehicles").get(0)));
    }
    /**
     * 分割 list
     * @param data list
     * @param splitSize size
     * @return list
     */
    public List<List> splitArrays(List data, int splitSize) {
        if (data == null || splitSize < 1) {
            return null;
        }
        int totalSize = data.size();
        int count = (totalSize % splitSize == 0) ?
                (totalSize / splitSize) : (totalSize / splitSize + 1);
        List<List> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
        List cols = data.subList(i * splitSize,
                    (i == count - 1) ? totalSize : splitSize * (i + 1));
            rows.add(cols);
        }
        return rows;
    }


    public List<List<String>> splitArrays(List<Object> data) {

        int batchSize = 8;
        List<List<String>> lstUrls = new ArrayList<List<String>>(data.size() + 5);
        List<String> urls = null;
        for (Object oUrl: data) {
            if(urls == null) {
                urls = new ArrayList<String>(batchSize);
            }

            urls.add(oUrl.toString());

            if(urls.size() >= batchSize ) {
                lstUrls.add(urls);
                urls = null;
            }
        }

        if(urls != null) {
            lstUrls.add(urls);
        }

        return lstUrls;
    }

    public static void main(String[] args){
      System.out.println(1510049022.91386000-1510049021.653507000);
    }
}
