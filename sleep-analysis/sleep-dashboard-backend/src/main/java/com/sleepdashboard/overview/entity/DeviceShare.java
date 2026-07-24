package com.sleepdashboard.overview.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dws_device_share")
public class DeviceShare {
    @TableId
    private String deviceModel;
    private Long cnt;
    private Double pct;
}
