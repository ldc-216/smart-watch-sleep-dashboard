package com.sleepdashboard.overview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sleepdashboard.overview.entity.YearlyTrend;

/**
 * MyBatis-Plus 的 BaseMapper 已经内置了 selectList/selectById 等常用方法，
 * 像这种简单聚合表不需要写任何 SQL，接口留空即可直接用。
 */
public interface YearlyTrendMapper extends BaseMapper<YearlyTrend> {
}
