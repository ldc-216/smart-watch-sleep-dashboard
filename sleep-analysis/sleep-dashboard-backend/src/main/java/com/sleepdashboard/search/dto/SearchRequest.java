package com.sleepdashboard.search.dto;

import lombok.Data;

/**
 * 屏5搜索框的过滤条件，全部是可选字段（前端只传用户勾选/填写了的条件）。
 */
@Data
public class SearchRequest {
    private String userId;          // 精确匹配
    private String gender;          // female / male
    private Integer ageMin;         // 年龄区间下限
    private Integer ageMax;         // 年龄区间上限
    private Integer medicationFlag; // 0/1，是否服药
    private String region;          // 地区
    private String deviceModel;     // 设备型号
    private Integer insomniaFlag;   // 0/1，是否失眠
    private Integer sleepScoreMin;  // 睡眠得分区间下限
    private Integer sleepScoreMax;  // 睡眠得分区间上限
    private Integer snoreEventsMin; // 打鼾频次区间下限
    private Integer snoreEventsMax; // 打鼾频次区间上限
    private Double bmiMin;          // BMI区间下限
    private Double bmiMax;          // BMI区间上限
    private String sortField;       // 排序字段
    private String sortOrder;       // asc / desc
    private Integer page = 1;
    private Integer size = 20;
}
