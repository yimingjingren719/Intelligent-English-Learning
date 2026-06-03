package com.english.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.english.platform.config.AIConfig;
import com.english.platform.entity.*;
import com.english.platform.mapper.*;
import com.english.platform.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

    private final AIConfig aiConfig;
    private final AnswerRecordMapper answerRecordMapper;
    private final ErrorRecordMapper errorRecordMapper;
    private final LearningRecordMapper learningRecordMapper;
    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AIServiceImpl(AIConfig aiConfig, AnswerRecordMapper answerRecordMapper,
                         ErrorRecordMapper errorRecordMapper, LearningRecordMapper learningRecordMapper,
                         QuestionMapper questionMapper, UserMapper userMapper, ObjectMapper objectMapper) {
        this.aiConfig = aiConfig;
        this.answerRecordMapper = answerRecordMapper;
        this.errorRecordMapper = errorRecordMapper;
        this.learningRecordMapper = learningRecordMapper;
        this.questionMapper = questionMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String chat(Long userId, String userQuestion, String contextType, Long questionId) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("你是一个专业的英语学习助手。请用中文回答学生的问题。\n\n");

            // 如果有题目上下文，添加题目信息
            if (questionId != null) {
                Question question = questionMapper.selectById(questionId);
                if (question != null) {
                    prompt.append("关联题目：").append(question.getContent()).append("\n");
                    prompt.append("正确答案：").append(question.getCorrectAnswer()).append("\n");
                    if (question.getAnalysis() != null) {
                        prompt.append("题目解析：").append(question.getAnalysis()).append("\n");
                    }
                    prompt.append("\n");
                }
            }

            // 根据上下文类型调整回答方向
            if (contextType != null) {
                switch (contextType) {
                    case "GRAMMAR":
                        prompt.append("请重点从语法角度分析和解答。\n");
                        break;
                    case "VOCABULARY":
                        prompt.append("请重点从词汇用法角度分析和解答。\n");
                        break;
                    case "READING":
                        prompt.append("请重点从阅读理解角度分析和解答。\n");
                        break;
                    case "WRITING":
                        prompt.append("请重点从写作表达角度分析和解答。\n");
                        break;
                }
            }

            prompt.append("\n学生问题：").append(userQuestion);
            prompt.append("\n\n请提供详细、易懂的解答：");

            return callAI(prompt.toString());
        } catch (Exception e) {
            log.error("AI答疑异常", e);
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

    @Override
    public Map<String, Object> generateProfile(Long userId) {
        // 收集用户学习数据
        LambdaQueryWrapper<AnswerRecord> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(AnswerRecord::getUserId, userId);
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(answerWrapper);

        LambdaQueryWrapper<ErrorRecord> errorWrapper = new LambdaQueryWrapper<>();
        errorWrapper.eq(ErrorRecord::getUserId, userId);
        errorWrapper.eq(ErrorRecord::getMastered, 0);
        List<ErrorRecord> errorRecords = errorRecordMapper.selectList(errorWrapper);

        LambdaQueryWrapper<LearningRecord> learningWrapper = new LambdaQueryWrapper<>();
        learningWrapper.eq(LearningRecord::getUserId, userId);
        learningWrapper.orderByDesc(LearningRecord::getCreateTime);
        learningWrapper.last("LIMIT 50");
        List<LearningRecord> learningRecords = learningRecordMapper.selectList(learningWrapper);

        // 统计数据
        int totalAnswers = answerRecords.size();
        int correctAnswers = (int) answerRecords.stream().filter(r -> r.getIsCorrect() == 1).count();
        double accuracy = totalAnswers > 0 ? (double) correctAnswers / totalAnswers * 100 : 0;
        int unmasteredErrors = errorRecords.size();

        Map<String, Object> profile = new HashMap<>();
        profile.put("totalAnswers", totalAnswers);
        profile.put("correctAnswers", correctAnswers);
        profile.put("accuracy", Math.round(accuracy * 100.0) / 100.0);
        profile.put("unmasteredErrors", unmasteredErrors);
        profile.put("totalTests", learningRecords.size());

        // 调用AI生成能力画像
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("基于以下学生英语学习数据，生成一份能力画像分析报告，包括：\n");
            prompt.append("1. 整体水平评估\n2. 优势分析\n3. 薄弱环节\n4. 学习建议\n\n");
            prompt.append(String.format("总答题数：%d，正确数：%d，正确率：%.1f%%\n",
                    totalAnswers, correctAnswers, accuracy));
            prompt.append(String.format("未掌握错题数：%d\n", unmasteredErrors));
            prompt.append(String.format("总测试次数：%d\n", learningRecords.size()));

            String aiAnalysis = callAI(prompt.toString());
            profile.put("aiAnalysis", aiAnalysis);
        } catch (Exception e) {
            log.error("AI分析异常", e);
            profile.put("aiAnalysis", "AI分析暂时不可用");
        }

        return profile;
    }

    @Override
    public Map<String, Object> analyzeWeakPoints(Long userId) {
        // 从未掌握的错题中提取知识点
        LambdaQueryWrapper<ErrorRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrorRecord::getUserId, userId);
        wrapper.eq(ErrorRecord::getMastered, 0);
        List<ErrorRecord> errorRecords = errorRecordMapper.selectList(wrapper);

        // 按题目ID分组统计错误次数
        Map<Long, Integer> errorCountByQuestion = errorRecords.stream()
                .collect(Collectors.groupingBy(ErrorRecord::getQuestionId,
                        Collectors.summingInt(ErrorRecord::getErrorCount)));

        // 收集所有标签
        Set<String> allTags = new HashSet<>();
        List<Map<String, Object>> weakPoints = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : errorCountByQuestion.entrySet()) {
            Question question = questionMapper.selectById(entry.getKey());
            if (question != null && question.getTags() != null) {
                Collections.addAll(allTags, question.getTags().split(","));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalWeakPoints", errorCountByQuestion.size());
        result.put("tags", allTags);

        // 调用AI分析薄弱点
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("学生的薄弱知识点标签包括：").append(String.join("、", allTags)).append("\n");
            prompt.append(String.format("共有%d个薄弱题目。\n", errorCountByQuestion.size()));
            prompt.append("请分析这些薄弱点，并给出针对性的提升建议。");

            String aiAnalysis = callAI(prompt.toString());
            result.put("aiAnalysis", aiAnalysis);
        } catch (Exception e) {
            log.error("AI薄弱点分析异常", e);
            result.put("aiAnalysis", "AI分析暂时不可用");
        }

        return result;
    }

    @Override
    public Map<String, Object> suggestStudyPlan(Long userId) {
        Map<String, Object> profile = generateProfile(userId);
        Map<String, Object> weakPoints = analyzeWeakPoints(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("profile", profile);
        result.put("weakPoints", weakPoints);

        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("基于学生的英语学习数据，制定一个为期一周的个性化学习计划。\n");
            prompt.append("学习计划应包括每天的学习重点、推荐练习类型、预期目标。\n");
            prompt.append("请用中文输出具体的、可执行的每日计划。");

            String studyPlan = callAI(prompt.toString());
            result.put("studyPlan", studyPlan);
        } catch (Exception e) {
            log.error("AI学习计划生成异常", e);
            result.put("studyPlan", "AI学习计划生成暂时不可用");
        }

        return result;
    }

    /**
     * 调用第三方AI接口
     */
    private String callAI(String prompt) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();

        if ("openai".equalsIgnoreCase(aiConfig.getProvider())) {
            // 调用OpenAI API
            requestBody.put("model", aiConfig.getOpenai().getModel());
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user");
            msg.put("content", prompt);
            messages.add(msg);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiConfig.getOpenai().getBaseUrl() + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + aiConfig.getOpenai().getApiKey())
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> msgObj = (Map<String, Object>) choices.get(0).get("message");
                    return (String) msgObj.get("content");
                }
            } else {
                log.error("AI API调用失败: {}", response.body());
                throw new RuntimeException("AI API返回错误: " + response.statusCode());
            }
        } else if ("gemini".equalsIgnoreCase(aiConfig.getProvider())) {
            // 调用Gemini API
            requestBody.put("contents", List.of(Map.of(
                    "parts", List.of(Map.of("text", prompt))
            )));

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiConfig.getGemini().getBaseUrl() + "/models/" +
                            aiConfig.getGemini().getModel() + ":generateContent?key=" +
                            aiConfig.getGemini().getApiKey()))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) result.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    return (String) parts.get(0).get("text");
                }
            } else {
                log.error("Gemini API调用失败: {}", response.body());
                throw new RuntimeException("Gemini API返回错误: " + response.statusCode());
            }
        }

        return "AI服务暂不支持当前配置";
    }
}
