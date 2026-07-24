package com.sleepdashboard.overview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首页 KPI 卡片：平均睡眠得分 / 平均睡眠时长 / 失眠人群比例 / 呼吸暂停高危人数
 * 由 Screen1Service 基于 dws_yearly_trend 全部年份加权计算得出（不是取某一年，是全量口径）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiCardDTO {
    private Double avgSleepScore;
    private Double avgDurationMinutes;
    private Double insomniaRatioPct;
    private Long highApneaRiskCnt;
    private Long totalRecordCnt;
}
