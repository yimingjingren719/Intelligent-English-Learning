package com.english.platform.controller;

import com.english.platform.common.Result;
import com.english.platform.dto.AIDTO;
import com.english.platform.service.AIService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * AI 智能答疑
     */
    @PostMapping("/chat")
    public Result<String> chat(HttpServletRequest request, @Valid @RequestBody AIDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        String answer = aiService.chat(userId, dto.getQuestion(), dto.getContextType(), dto.getQuestionId());
        return Result.success(answer);
    }

    /**
     * 生成用户能力画像
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> profile = aiService.generateProfile(userId);
        return Result.success(profile);
    }

    /**
     * 薄弱点分析
     */
    @GetMapping("/weak-points")
    public Result<Map<String, Object>> getWeakPoints(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> weakPoints = aiService.analyzeWeakPoints(userId);
        return Result.success(weakPoints);
    }

    /**
     * 生成个性化学习建议
     */
    @GetMapping("/study-plan")
    public Result<Map<String, Object>> getStudyPlan(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> studyPlan = aiService.suggestStudyPlan(userId);
        return Result.success(studyPlan);
    }
}
