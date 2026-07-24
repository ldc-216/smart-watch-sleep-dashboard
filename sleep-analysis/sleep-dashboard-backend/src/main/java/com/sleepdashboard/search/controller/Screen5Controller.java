package com.sleepdashboard.search.controller;

import com.sleepdashboard.common.Result;
import com.sleepdashboard.search.dto.SearchRequest;
import com.sleepdashboard.search.dto.SearchResultDTO;
import com.sleepdashboard.search.service.ElasticsearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "屏5-极速检索与高危日志明细")
@RestController
@RequestMapping("/api/screen5")
public class Screen5Controller {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private com.sleepdashboard.auth.service.SysUserService sysUserService;

    private boolean isNotAdmin() {
        Long currentUserId = com.sleepdashboard.auth.util.UserContext.getUserId();
        if (currentUserId == null) {
            return true;
        }
        com.sleepdashboard.auth.dto.UserVO userVO = sysUserService.getLoginUserInfo(currentUserId);
        return !"root".equals(userVO.getUsername());
    }

    /** 多条件极速搜索：user_id / 性别 / 年龄区间 / 是否服药 任意组合 */
    @PostMapping("/search")
    public Result<SearchResultDTO> search(@RequestBody SearchRequest request) throws Exception {
        return Result.success(elasticsearchService.search(request));
    }

    /** 导入记录到 ES 索引 */
    @PostMapping("/import")
    public Result<String> importRecords(@RequestBody java.util.List<java.util.Map<String, Object>> records) throws Exception {
        if (isNotAdmin()) {
            return Result.error(403, "无管理员权限");
        }
        elasticsearchService.importRecords(records);
        return Result.success("数据导入成功");
    }

    /** 删除记录 */
    @DeleteMapping("/delete")
    public Result<String> deleteRecord(@RequestParam String userId, @RequestParam String dateRecorded) throws Exception {
        if (isNotAdmin()) {
            return Result.error(403, "无管理员权限");
        }
        elasticsearchService.deleteRecord(userId, dateRecorded);
        return Result.success("数据删除成功");
    }

    /** 批量删除记录 */
    @PostMapping("/batch-delete")
    public Result<String> batchDelete(@RequestBody java.util.List<java.util.Map<String, String>> keys) throws Exception {
        if (isNotAdmin()) {
            return Result.error(403, "无管理员权限");
        }
        elasticsearchService.deleteRecords(keys);
        return Result.success("批量数据删除成功");
    }

    /** 更新记录 */
    @PutMapping("/update")
    public Result<String> updateRecord(@RequestBody java.util.Map<String, Object> record) throws Exception {
        if (isNotAdmin()) {
            return Result.error(403, "无管理员权限");
        }
        elasticsearchService.updateRecord(record);
        return Result.success("数据修改成功");
    }

    /** 获取检索条件的上限和下限 */
    @GetMapping("/range-limits")
    public Result<java.util.Map<String, Object>> getRangeLimits() {
        return Result.success(elasticsearchService.getRangeLimits());
    }
}
