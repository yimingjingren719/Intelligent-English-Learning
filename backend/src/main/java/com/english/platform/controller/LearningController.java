package com.english.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.common.PageResult;
import com.english.platform.common.Result;
import com.english.platform.entity.LearningRecord;
import com.english.platform.service.LearningService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/learning")
public class LearningController {

    private final LearningService learningService;

    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    /** 分页查询学习记录 */
    @GetMapping("/page")
    public Result<PageResult<LearningRecord>> page(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String recordType) {
        Long userId = (Long) request.getAttribute("userId");
        Page<LearningRecord> recordPage = learningService.pageQuery(userId, page, size, recordType);
        return Result.success(PageResult.of(recordPage.getTotal(), recordPage.getCurrent(),
                recordPage.getSize(), recordPage.getRecords()));
    }

    /** 日历视图数据 */
    @GetMapping("/calendar")
    public Result<List<Map<String, Object>>> calendar(
            HttpServletRequest request,
            @RequestParam int year,
            @RequestParam int month) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(learningService.getCalendarData(userId, year, month));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            HttpServletRequest request,
            @RequestParam(required = false) Integer recentCount) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(learningService.getStatistics(userId, recentCount));
    }

    @PostMapping("/heartbeat")
    public Result<?> heartbeat(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        learningService.recordOnlineTime(userId, 60);
        return Result.success();
    }

    @GetMapping("/statistics/detail")
    public Result<Map<String, Object>> getDetailedStatistics(
            HttpServletRequest request,
            @RequestParam(required = false) Integer recentCount) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(learningService.getDetailedStatistics(userId, recentCount));
    }
}
