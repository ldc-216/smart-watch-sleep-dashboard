package com.sleepdashboard.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * ES 返回的是灵活的 JSON 结构，这里直接用 Map 承接每条记录（字段就是
 * create_es_index.sh 里 mapping 定义的那些），避免为搜索结果单独定义死板的实体类。
 * total: 命中总数，用于前端分页。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {
    private long total;
    private List<Map<String, Object>> records;
    private Map<String, Object> stats;

    public SearchResultDTO(long total, List<Map<String, Object>> records) {
        this.total = total;
        this.records = records;
        this.stats = null;
    }
}
