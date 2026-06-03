package com.english.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.common.PageResult;
import com.english.platform.common.Result;
import com.english.platform.entity.ErrorRecord;
import com.english.platform.service.ErrorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/error")
public class ErrorController {

    private final ErrorService errorService;

    public ErrorController(ErrorService errorService) {
        this.errorService = errorService;
    }

    /**
     * 分页查询错题
     */
    @GetMapping("/page")
    public Result<PageResult<ErrorRecord>> page(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<ErrorRecord> errorPage = errorService.pageQuery(userId, page, size);
        PageResult<ErrorRecord> pageResult = PageResult.of(
                errorPage.getTotal(), errorPage.getCurrent(),
                errorPage.getSize(), errorPage.getRecords());
        return Result.success(pageResult);
    }

    /**
     * 获取错题详情（含题目信息）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        Map<String, Object> detail = errorService.getDetail(id);
        return Result.success(detail);
    }

    /**
     * 标记错题已掌握
     */
    @PutMapping("/{id}/master")
    public Result<?> markMastered(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        errorService.markMastered(id, userId);
        return Result.success("已标记为掌握");
    }

    /**
     * 错题练习 - 获取错题题目
     */
    @GetMapping("/practice")
    public Result<Map<String, Object>> practiceWrongQuestions(
            HttpServletRequest request,
            @RequestParam(defaultValue = "10") Integer count) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = errorService.practiceWrongQuestions(userId, count);
        return Result.success(result);
    }
}
