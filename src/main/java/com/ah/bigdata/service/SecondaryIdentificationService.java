package com.ah.bigdata.service;

import com.ah.bigdata.config.ApiConfig;
import com.ah.bigdata.config.ImageConfig;
import com.ah.bigdata.dao.SIRecogDAO;
import com.ah.bigdata.model.Result;
import com.ah.bigdata.model.SIRecog;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by JoyLau on 2017/10/30.
 * com.ah.bigdata.service
 * 2587038142@qq.com
 */
@Service
public class SecondaryIdentificationService {
    @Autowired
    private RestTemplate restTemplate;

    private final ApiConfig config;

    @Autowired
    private SIRecogDAO recogDAO;

    @Autowired
    private ImageConfig imageConfig;

    @Value("classpath:static/json/addSource.json")
    Resource addSource;

    @Value("classpath:static/json/addTask.json")
    Resource addTaskJson;

    @Value("classpath:static/json/searchVehicle.json")
    Resource searchVehicle;

    @Value("classpath:static/json/folder_path_yn.json")
    Resource sourceYunNan;

    @Value("classpath:static/json/folder_path_cz.json")
    Resource sourceChuZhou;

    private JSONObject brandFile;

    @Autowired
    public SecondaryIdentificationService(ApiConfig config) {
        this.config = config;
        this.brandFile = config.getBrand();
    }

    /**
     * 添加资源信息
     *
     * @return JSONObject
     */
    public String addSource(JSONObject json) {
        return restTemplate.postForObject(config.getAddSourceURL(), json, String.class);
    }

    /**
     * 添加任务
     *
     * @return json
     */
    public String addTask(JSONObject jsonObject) {
        return restTemplate.postForObject(config.getAddTaskURL(), jsonObject, String.class);
    }

    /**
     * 开启任务
     *
     * @return json
     */
    public String startTask(int taskId) {
        return restTemplate.getForObject(config.getStartTaskURL() + "?taskid=" + taskId, String.class);
    }

    /**
     * 查询结果
     *
     * @return String
     */
    public String searchVehicle(JSONObject jsonObject) {
        return restTemplate.postForObject(config.getSearchVehicleURL(), jsonObject, String.class);
    }

    /**
     * 保存机动车数据
     *
     * @return json
     */
    public String saveVehicle(JSONObject jsonObject) {
        String batch = "p-" + System.currentTimeMillis();
        JSONArray arr = getVehicleData(jsonObject);
        for (Object o : arr) {
            JSONObject obj = JSONObject.parseObject(o.toString());

            String imageUrl = obj.getJSONObject("Img").getString("URI");

            SIRecog siRecog = new SIRecog();
            siRecog.setSrc(getFolderIndexName(imageUrl, 1));
            siRecog.setBatch(batch);
            siRecog.setDevice_type(getFolderIndexName(imageUrl, 2));
            siRecog.setImg_url(getImageURI(imageUrl));
            siRecog.setPass_time(getPassDate(imageUrl));
            siRecog.setPlate_nbr(getPlateNo(imageUrl));
            siRecog.setPlate_color(getPlateColor(imageUrl));

            siRecog.setRecog_plate_nbr(getRPlateNo(obj));
            siRecog.setRecog_brand(getRBrand(obj));
            siRecog.setRecog_veh_type(getRVenType(obj));
            siRecog.setRecog_veh_color(getRVenColor(obj));
            siRecog.setRecog_plate_color(getRPlateColor(obj));
            executeSymbols(siRecog, obj);
            siRecog.setRecog_veh_head_pos(getRVehHeadPos(obj));

            siRecog.setCreate_time(new Date());
            siRecog.setRec_time(new Date(obj.getJSONObject("Metadata").getLongValue("Timestamp")));

            recogDAO.save(siRecog);
        }
        return Result.success("data total:"+arr.size());
    }

    /**
     * 获取 机动车数据
     *
     * @return JSONArray
     */
    private JSONArray getVehicleData(JSONObject jsonObject) {
        String result = restTemplate.postForObject(config.getSearchVehicleURL(), jsonObject, String.class);
        JSONObject resultJSON = JSONObject.parseObject(result);
        return resultJSON.getJSONObject("Data").getJSONArray("RetVehicles");
    }

    String getFolderIndexName(String url, int index) {
        String str = "";
        try {
            URI uri = new URI(url);
            String[] strArr = uri.getPath().split("/");
            str = strArr[index];
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 获取图片的uri
     *
     * @param url url
     * @return String
     */
    public String getImageURI(String url) {
        String uriStr = "";
        try {
            URI uri = new URI(url);
            uriStr = uri.getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uriStr;
    }

    /**
     * 获取文件名的各个信息
     *
     * @param fileName fileName
     * @param index    index
     * @return String
     */
    private String getFileIndexName(String fileName, int index) {
        String[] strArr = fileName.split("_");
        return strArr[index];
    }

    /**
     * 字符串时间转化为Data
     *
     * @param timeStr 字符串时间
     * @return Date
     */
    private Date strToDate(String timeStr) {
        try {
            return new SimpleDateFormat("yyyyMMddhhmmss").parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取过车时间
     *
     * @return String
     */
    public Date getPassDate(String url) {
        String fileName = getFolderIndexName(url, 4);
        String dateStr = getFileIndexName(fileName, 1);
        return strToDate(dateStr);
    }

    /**
     * 获取原车牌编号
     *
     * @param url 文件url
     * @return 车牌编号
     */
    public String getPlateNo(String url) {
        String fileName = getFolderIndexName(url, 4);
        String plateStr = getFileIndexName(fileName, 2);
        String plate = "";
        try {
            plate = URLDecoder.decode(plateStr, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return plate;
    }

    /**
     * 获取原车牌颜色
     *
     * @param url url
     * @return String
     */
    public String getPlateColor(String url) {
        String fileName = getFolderIndexName(url, 4);
        return getFileIndexName(fileName, 3).split("[.]")[0];
    }

    /**
     * 获取车牌的信息
     *
     * @param json json
     * @return JSON
     */
    private JSONObject getRPlateInfo(JSONObject json) {
        return (JSONObject) json.getJSONObject("RecVehicle").getJSONArray("Plates").get(0);
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
        int colorId = getRPlateInfo(json).getJSONObject("Color").getIntValue("ColorId");
        return getPlateColorStr(colorId);
    }

    /**
     * 获取的识别的品牌，主品牌+ 子品牌
     *
     * @param json json
     * @return String
     */
    private String getRBrand(JSONObject json) {
        JSONObject model = getModelType(json);
        int brandId = model.getIntValue("BrandId");
        int subBrandId = model.getIntValue("SubBrandId");
        JSONObject brandInfo = brandFile;
        JSONObject brand = brandInfo.getJSONObject("brand").getJSONObject(String.valueOf(brandId));
        String brandStr = brand.getJSONObject("name").getString("chs");
        String subBrandStr = brand.getJSONObject("subbrand").getJSONObject(String.valueOf(subBrandId)).getJSONObject("name").getString("chs");
        return brandStr + "--" + subBrandStr;
    }

    /**
     * 获取车型，车品牌，子品牌，出厂时间等一系列信息的JSON对象
     *
     * @param json json
     * @return json
     */
    private JSONObject getModelType(JSONObject json) {
        return json.getJSONObject("RecVehicle").getJSONObject("ModelType");
    }

    /**
     * 获取识别的车辆类型
     *
     * @param json json
     * @return String
     */
    private String getRVenType(JSONObject json) {
        JSONObject model = getModelType(json);
        int styleId = model.getIntValue("StyleId");
        if (styleId == 0) {
            return "轿车";
        }
        if (styleId == 1) {
            return "面包车";
        }
        if (styleId == 2) {
            return "皮卡";
        }
        if (styleId == 3) {
            return "越野车/SUV";
        }
        if (styleId == 4) {
            return "商务车/MPV";
        }
        if (styleId == 5) {
            return "轻型客车";
        }
        if (styleId == 6) {
            return "中型客车";
        }
        if (styleId == 7) {
            return "大型客车";
        }
        if (styleId == 8) {
            return "公交车";
        }
        if (styleId == 9) {
            return "校车";
        }
        if (styleId == 10) {
            return "微型货车";
        }
        if (styleId == 11) {
            return "轻型货车";
        }
        if (styleId == 12) {
            return "中型货车";
        }
        if (styleId == 13) {
            return "大型货车";
        }
        if (styleId == 14) {
            return "重型货车";
        }
        if (styleId == 15) {
            return "集装箱车";
        }
        if (styleId == 16) {
            return "三轮车";
        }
        return "其他";
    }

    /**
     * 获取识别的车身颜色
     *
     * @param json json
     * @return String
     */
    private String getRVenColor(JSONObject json) {
        JSONObject color = json.getJSONObject("RecVehicle").getJSONObject("Color");
        int colorId = color.getIntValue("ColorId");
        return getColorStr(colorId);
    }

    /**
     * 根据车牌颜色代码获取颜色字符串
     *
     * @param colorId int
     * @return String
     */
    private String getPlateColorStr(int colorId) {
        if (colorId == 1) {
            return "蓝";
        }
        if (colorId == 2) {
            return "黄";
        }
        if (colorId == 3) {
            return "白";
        }
        if (colorId == 4) {
            return "黑";
        }
        if (colorId == 5) {
            return "绿";
        }
        return "其他";
    }


    /**
     * 根据车身颜色代码获取颜色字符串
     *
     * @param colorId int
     * @return String
     */
    private String getColorStr(int colorId) {
        if (colorId == 1) {
            return "黑";
        }
        if (colorId == 2) {
            return "蓝";
        }
        if (colorId == 3) {
            return "棕";
        }
        if (colorId == 4) {
            return "绿";
        }
        if (colorId == 5) {
            return "灰";
        }
        if (colorId == 6) {
            return "橙";
        }
        if (colorId == 7) {
            return "粉";
        }
        if (colorId == 8) {
            return "紫";
        }
        if (colorId == 9) {
            return "红";
        }
        if (colorId == 10) {
            return "银";
        }
        if (colorId == 11) {
            return "白";
        }
        if (colorId == 12) {
            return "黄";
        }
        return "其他";
    }

    /**
     * 获取年检标，挂饰，纸巾盒等JSON对象
     *
     * @param json json
     * @return JSON
     */
    private JSONArray getSymbols(JSONObject json) {
        return json.getJSONObject("RecVehicle").getJSONArray("Symbols");
    }

    /**
     * 处理年检标，挂饰，纸巾盒等饰物进行存储
     *
     * @param siRecog 对象
     * @param json    json
     */
    private void executeSymbols(SIRecog siRecog, JSONObject json) {
        JSONArray symbols = getSymbols(json);
        StringBuilder sticker = new StringBuilder();
        StringBuilder pend = new StringBuilder();
        StringBuilder box = new StringBuilder();
        for (Object symbol : symbols) {
            JSONObject symbolJSON = JSONObject.parseObject(symbol.toString());
            if (symbolJSON.getIntValue("SymbolId") == 1) {
                //年检标
                sticker.append(getCutboardInfo(symbolJSON.getJSONObject("Cutboard"))).append(";");
            }
            if (symbolJSON.getIntValue("SymbolId") == 3) {
                //挂件
                pend.append(getCutboardInfo(symbolJSON.getJSONObject("Cutboard"))).append(";");
            }
            if (symbolJSON.getIntValue("SymbolId") == 4) {
                //纸巾盒
                box.append(getCutboardInfo(symbolJSON.getJSONObject("Cutboard"))).append(";");
            }
        }
        siRecog.setRecog_sticker_pos(sticker.toString());
        siRecog.setRecog_pend_pos(pend.toString());
        siRecog.setRecog_box_pos(box.toString());
    }

    /**
     * 根据提供的Cutboard对象信息转为位置信息
     *
     * @param json json
     * @return String
     */
    public String getCutboardInfo(JSONObject json) {
        int x = json.getIntValue("X");
        int y = json.getIntValue("Y");
        int width = json.getIntValue("Width");
        int height = json.getIntValue("Height");
        return x + "," + y + "," + width + "," + height;
    }

    /**
     * 获取车身位置
     *
     * @param json josn
     * @return String
     */
    private String getRVehHeadPos(JSONObject json) {
        return getCutboardInfo(json.getJSONObject("RecVehicle").getJSONObject("Img").getJSONObject("Cutboard"));
    }

    /**
     * 删除所有的资源
     *
     * @return json
     */
    public String delAllSource() {
        JSONObject resJSON = new JSONObject();
        JSONArray array = new JSONArray();
        String res = restTemplate.getForObject(config.getGetSourceURL(), String.class);
        JSONArray arr = JSONObject.parseObject(res).getJSONArray("data_list");
        for (Object o : arr) {
            JSONObject source = JSONObject.parseObject(o.toString());
            JSONObject delJSON = new JSONObject();
            delJSON.put("id", source.getIntValue("id"));
            delJSON.put("type", source.getIntValue("type"));
            String result = restTemplate.postForObject(config.getDelSourceURL(), delJSON, String.class);
            array.add(result);
        }
        resJSON.put("data", array);
        return resJSON.toJSONString();
    }

    /**
     * 删除所有区域
     *
     * @return String
     */
    public String delAllRepo() {
        JSONObject resJSON = new JSONObject();
        JSONArray array = new JSONArray();
        String res = restTemplate.getForObject(config.getGetRepoURL(), String.class);
        JSONArray arr = JSONObject.parseObject(res).getJSONArray("Data");
        for (Object o : arr) {
            JSONObject repo = JSONObject.parseObject(o.toString());
            int id = repo.getIntValue("RepoId");
            restTemplate.delete(config.getDelRepoURL() + "?id=" + id);
            array.add("success");
        }
        resJSON.put("data", array);
        return resJSON.toJSONString();
    }

    /**
     * 删除所有任务
     *
     * @return json
     */
    public String delAllTask() {
        JSONObject resJSON = new JSONObject();
        JSONArray array = new JSONArray();
        String res = restTemplate.getForObject(config.getGetTaskURL(), String.class);
        JSONArray arr = JSONObject.parseObject(res).getJSONArray("data_list");
        for (Object o : arr) {
            JSONObject task = JSONObject.parseObject(o.toString());
            int taskId = task.getIntValue("taskId");
            String result = restTemplate.getForObject(config.getDelTaskURL() + "?taskid=" + taskId, String.class);
            array.add(result);
        }
        resJSON.put("data", array);
        return resJSON.toJSONString();
    }

    public JSONObject getAllFolderName() {
        JSONObject json = new JSONObject();
        JSONArray arr = getDir(imageConfig.getAbsolutePath(), new JSONArray());
        json.put("data", arr);
        return json;
    }

    private JSONArray getDir(String path, JSONArray jsonArray) {
        File f = new File(path);
        if (f.isDirectory()) {
            File[] fList = f.listFiles();
            assert fList != null;
            for (File aFList : fList) {
                if (aFList.isDirectory()) {
                    jsonArray.add(aFList.getPath());
                    getDir(aFList.getPath(), jsonArray);
                }
            }
        }
        return jsonArray;
    }

    /**
     * 添加所有资源
     *
     * @return JSON
     */
    public JSONObject addAllSourceForYunNan() {
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();

        String resStr = restTemplate.getForObject(config.getGetRepoURL(), String.class);
        JSONArray arr = JSONObject.parseObject(resStr).getJSONArray("Data");
        for (Object o : arr) {
            JSONObject repo = JSONObject.parseObject(o.toString());
            JSONObject jsonObject = config.getJsonByFile(addSource);
            jsonObject.put("name", "二次识别-" + System.currentTimeMillis());
            jsonObject.put("repoId", String.valueOf(repo.getIntValue("RepoId")));
            jsonObject.put("sensorid", "sensor-" + System.currentTimeMillis());
            jsonObject.put("uri", imageConfig.getPath() + repo.getString("Name").replace("Repo-", ""));
            String res = restTemplate.postForObject(config.getAddSourceURL(), jsonObject, String.class);
            array.add(res);
        }
        json.put("data", array);
        return json;
    }

    public JSONObject addAllRepoForYunNan() {
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();

        JSONArray arr = config.getJsonByFile(sourceYunNan).getJSONArray("data");
        for (Object o : arr) {
            JSONObject repoJSON = new JSONObject();
            repoJSON.put("Name", "Repo-" + o.toString());
            String res = restTemplate.postForObject(config.getAddRepoURL(), repoJSON, String.class);
            array.add(res);
        }
        json.put("data", array);
        return json;
    }

    public JSONObject addAllTaskForYunNan() {
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();

        String res = restTemplate.getForObject(config.getGetSourceURL(), String.class);
        JSONArray arr = JSONObject.parseObject(res).getJSONArray("data_list");
        for (Object o : arr) {
            JSONObject source = JSONObject.parseObject(o.toString());
            int sourceId = source.getIntValue("id");
            JSONObject jsonObject = config.getJsonByFile(addTaskJson);
            jsonObject.put("name", "二次识别-" + System.currentTimeMillis());
            jsonObject.put("sourceId", sourceId);
            String resStr = restTemplate.postForObject(config.getAddTaskURL(), jsonObject, String.class);
            array.add(resStr);
        }
        json.put("data", array);
        return json;
    }

    /**
     * 临时写入记录云南
     *
     * @return json
     */
//    public JSONObject saveRecodeForYunNan() {
//        for (int i = 5; i < 498; i++) {
//            JSONObject jsonObject = config.getJsonByFile(searchVehicle);
//            JSONArray queryArr = jsonObject.getJSONObject("BaseQuery").getJSONArray("QueryTimeSpacialRanges");
//            JSONObject ob = JSONObject.parseObject(queryArr.get(0).toString());
//            ob.put("RepoId",i);
//            String result = restTemplate.postForObject(config.getSearchVehicleURL(), jsonObject, String.class);
//            JSONObject resultJSON = JSONObject.parseObject(result);
//            JSONArray retJSON = resultJSON.getJSONObject("Data").getJSONArray("RetVehicles");
//
//            String batch = "p-" + System.currentTimeMillis();
//            JSONArray arr = retJSON;
//            for (Object o : arr) {
//                JSONObject obj = JSONObject.parseObject(o.toString());
//
//                String imageUrl = obj.getJSONObject("Img").getString("URI");
//
//                SIRecog siRecog = new SIRecog();
//                siRecog.setSrc(getFolderIndexName(imageUrl, 1));
//                siRecog.setBatch(batch);
//                siRecog.setDevice_type(getFolderIndexName(imageUrl, 2));
//                siRecog.setImg_url(getImageURI(imageUrl));
//                siRecog.setPass_time(getPassDate(imageUrl));
//                siRecog.setPlate_nbr(getPlateNo(imageUrl));
//                siRecog.setPlate_color(getPlateColor(imageUrl));
//
//                siRecog.setRecog_plate_nbr(getRPlateNo(obj));
//                siRecog.setRecog_brand(getRBrand(obj));
//                siRecog.setRecog_veh_type(getRVenType(obj));
//                siRecog.setRecog_veh_color(getRVenColor(obj));
//                siRecog.setRecog_plate_color(getRPlateColor(obj));
//                executeSymbols(siRecog, obj);
//                siRecog.setRecog_veh_head_pos(getRVehHeadPos(obj));
//
//                siRecog.setCreate_time(new Date());
//                siRecog.setExecute_time(new Date(obj.getJSONObject("Metadata").getLongValue("Timestamp")));
//
//                recogDAO.save(siRecog);
//            }
//        }
//
//        return new JSONObject();
//    }
}
