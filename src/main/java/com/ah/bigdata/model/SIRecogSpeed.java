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
@Entity(name = "t_two_recog_speed")
@Data
public class SIRecogSpeed {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private int id; //主键
    private String batch_id; // 批次ID
    private Date start_time;//开始时间
    private Date end_time;//结束时间
    private long image_count;//图片总数
    private double speed;//处理速度
}

