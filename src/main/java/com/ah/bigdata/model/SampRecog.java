package com.ah.bigdata.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by JoyLau on 2017/10/26.
 * com.ah.bigdata.dao
 * 2587038142@qq.com
 */
@Entity(name = "t_samp_recog")
@Data
public class SampRecog {
    @Id
    @GeneratedValue
    private int id; //主键
    private String batch; // 批次
    private String img_url; // 图片路径
    private String plate_nbr; // 车辆号牌
    private boolean plate_nbr_right; // 车辆号牌是否正确
    private String brand; // 品牌
    private boolean brand_right; // 品牌是否正确
    private String veh_type; // 车辆类型
    private boolean veh_type_right; // 车辆类型是否正确
    private String veh_color; // 车身颜色
    private boolean veh_color_right; // 车身颜色是否正确
    private String sticker_pos; // 车标位置
    private boolean sticker_pos_right; // 车标位置是否全部正确
    private boolean is_right; // 是否全部正确
    private int check_status; //核对状态 1.未核对，2，正在核对，3、已经核对
}

