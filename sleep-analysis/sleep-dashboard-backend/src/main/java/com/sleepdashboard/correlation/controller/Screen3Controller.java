package com.sleepdashboard.correlation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sleepdashboard.common.Result;
import com.sleepdashboard.correlation.entity.SnoreApneaBubble;
import com.sleepdashboard.correlation.dto.Screen3OverviewDTO;
import com.sleepdashboard.correlation.service.Screen3Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "屏3-生活与环境因素关联分析")
@RestController
@RequestMapping("/api/screen3")
public class Screen3Controller {

    @Autowired
    private Screen3Service screen3Service;

    /** 一次性返回：特征相关性热力图 + 温度-效率分桶 + 睡前运动对比 */
    @GetMapping("/overview")
    public Result<Screen3OverviewDTO> overview() {
        return Result.success(screen3Service.getOverview());
    }

    /** 打鼾-血氧气泡图，明细数据，必须传分页参数（已从屏2移动到屏3，用于关联分析） */
    @GetMapping("/snore-apnea-bubble")
    public Result<Page<SnoreApneaBubble>> snoreApneaBubble(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "500") int size
    ) {
        return Result.success(screen3Service.getSnoreApneaBubble(page, size));
    }
}
