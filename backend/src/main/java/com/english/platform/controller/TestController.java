package com.english.platform.controller;

import com.english.platform.common.Result;
import com.english.platform.dto.TestSubmitDTO;
import com.english.platform.service.TestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    /**
     * 生成测试题目
     */
    @GetMapping("/generate")
    public Result<Map<String, Object>> generateTest(
            HttpServletRequest request,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(defaultValue = "10") Integer count) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> testData = testService.generateTest(userId, questionType, difficulty, count);
        return Result.success(testData);
    }

    /**
     * 提交测试答案
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitTest(
            HttpServletRequest request,
            @Valid @RequestBody TestSubmitDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = testService.submitTest(userId, dto);
        return Result.success("提交成功", result);
    }

    /**
     * 获取测试详情
     */
    @GetMapping("/detail/{sessionId}")
    public Result<Map<String, Object>> getTestDetail(@PathVariable String sessionId) {
        Map<String, Object> detail = testService.getTestDetail(sessionId);
        return Result.success(detail);
    }
}
