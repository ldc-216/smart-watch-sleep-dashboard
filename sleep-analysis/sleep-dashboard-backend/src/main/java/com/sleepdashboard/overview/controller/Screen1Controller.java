package com.sleepdashboard.overview.controller;

import com.sleepdashboard.common.Result;
import com.sleepdashboard.overview.dto.Screen1OverviewDTO;
import com.sleepdashboard.overview.service.Screen1Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "屏1-总体睡眠健康大屏")
@RestController
@RequestMapping("/api/screen1")
public class Screen1Controller {

    @Autowired
    private Screen1Service screen1Service;

    /**
     * 一次性返回屏1所有图表数据：KPI卡 + 年度趋势 + 月度趋势 + 地域分布 + 设备份额。
     * 前端页面 mounted 时调这一个接口即可完成初始化。
     */
    @GetMapping("/overview")
    public Result<Screen1OverviewDTO> overview() {
        return Result.success(screen1Service.getOverview());
    }
}
