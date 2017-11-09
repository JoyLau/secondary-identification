package com.ah.bigdata.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by JoyLau on 2017/10/26.
 * com.ah.bigdata.dao
 * 2587038142@qq.com
 */
@Entity(name = "t_two_recog_w")
@Data
public class SIRecog {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private int id; //主键
    private String src; // 来源，滁州，云南
    private String batch; // 批次
    private String device_type; //设备类型,电警&卡口
    private String img_url; // 图片路径
    private Date pass_time; // 过车时间
    private String plate_nbr; // 原车辆号牌
    private String brand; // 原品牌
    private String veh_type; // 原车辆类型
    private String veh_color; // 原车身颜色
    private String plate_color; // 原号牌颜色

    private String recog_plate_nbr; // 识别车牌号牌
    private String recog_brand; // 识别品牌
    private String recog_veh_type; // 识别车辆类型
    private String recog_veh_color; //识别车身颜色
    private String recog_plate_color; //识别号牌颜色
    private String recog_sticker_pos; //识别车年检标位置
    private String recog_pend_pos; //识别挂件位置
    private String recog_box_pos; //识别纸巾盒位置
    private String recog_veh_head_pos; //识别车身位置

    private boolean plate_nbr_right; //车辆号牌是否正确
    private boolean brand_right; //品牌是否正确
    private boolean veh_type_right; //车辆类型是否正确
    private boolean veh_color_right; //车身颜色是否正确
    private boolean plate_color_right; //号牌颜色是否正确
    private boolean sticker_pos_right; //车标位置是否全部正确
    private boolean pend_pos_right; //挂机位置是否正确
    private boolean box_pos_right; //纸巾盒位置是否正确
    private boolean veh_head_pos_right; //车身位置是否正确

    private boolean all_right; //是否全部正确
    private int check_status = 1; //核对状态 1.未核对，2，正在核对，3、已经核对
    private Date update_time; //更新时间
    private Date create_time; //创建时间
    private Date rec_time; //图片处理时间
    private String session_id; //session_id
    private double cost_time;//花费时间
    private String message;//返回消息是否成功
}

