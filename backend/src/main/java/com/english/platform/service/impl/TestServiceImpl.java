package com.english.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.english.platform.dto.TestSubmitDTO;
import com.english.platform.entity.*;
import com.english.platform.exception.BusinessException;
import com.english.platform.mapper.*;
import com.english.platform.service.TestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TestServiceImpl implements TestService {

    private final QuestionMapper questionMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final ErrorRecordMapper errorRecordMapper;
    private final LearningRecordMapper learningRecordMapper;

    public TestServiceImpl(QuestionMapper questionMapper, AnswerRecordMapper answerRecordMapper,
                           ErrorRecordMapper errorRecordMapper, LearningRecordMapper learningRecordMapper) {
        this.questionMapper = questionMapper;
        this.answerRecordMapper = answerRecordMapper;
        this.errorRecordMapper = errorRecordMapper;
        this.learningRecordMapper = learningRecordMapper;
    }

    @Override
    public Map<String, Object> generateTest(Long userId, String questionType,
                                             Integer difficulty, Integer count) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(questionType != null && !questionType.isEmpty(), Question::getQuestionType, questionType);
        wrapper.eq(difficulty != null, Question::getDifficulty, difficulty);
        wrapper.eq(Question::getStatus, 0);
        // 阅读理解类题型按ID排序（保证文章和题目在一起），选词填空和长篇匹配直接取count篇，其他随机
        boolean isReadingType = "READING".equals(questionType) || "CAREFUL_READING".equals(questionType);
        boolean isPassageType = "BANKED_CLOZE".equals(questionType) || "LONG_MATCH".equals(questionType);
        if (isReadingType) {
            wrapper.orderByAsc(Question::getId);
            wrapper.last("LIMIT " + count);
        } else if (isPassageType) {
            wrapper.orderByAsc(Question::getId);
            wrapper.last("LIMIT " + count);
        } else {
            wrapper.last("ORDER BY RAND() LIMIT " + count);
        }
        List<Question> questions = questionMapper.selectList(wrapper);

        String sessionId = "TEST-" + UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("questions", questions);
        result.put("totalCount", questions.size());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> submitTest(Long userId, TestSubmitDTO dto) {
        String sessionId = dto.getSessionId() != null ? dto.getSessionId()
                : "TEST-" + UUID.randomUUID().toString().substring(0, 8);

        int correctCount = 0;
        int totalTime = 0;
        List<Map<String, Object>> details = new ArrayList<>();

        int totalScore = 0; int maxScore = 0;

        for (TestSubmitDTO.AnswerItem item : dto.getAnswers()) {
            // 处理选词填空和长篇匹配的合成ID (originalId * 100 + subIndex)
            Long realQuestionId = item.getQuestionId();
            int subIndex = 0;
            Question question = questionMapper.selectById(realQuestionId);
            if (question == null && realQuestionId > 100) {
                realQuestionId = realQuestionId / 100;
                subIndex = (int)(item.getQuestionId() % 100);
                question = questionMapper.selectById(realQuestionId);
            }
            if (question == null) continue;

            boolean isCorrect = false;
            String correctAnswerPart = question.getCorrectAnswer();
            String userAnswerDisplay = item.getUserAnswer() != null ? item.getUserAnswer().trim() : "";

            // 根据题型判断答案，并附上选项具体内容
            if ("BANKED_CLOZE".equals(question.getQuestionType()) || "LONG_MATCH".equals(question.getQuestionType())) {
                String[] correctParts = correctAnswerPart.split(",");
                if (subIndex < correctParts.length) {
                    isCorrect = correctParts[subIndex].trim().equalsIgnoreCase(userAnswerDisplay);
                    correctAnswerPart = correctParts[subIndex].trim();
                }
                // 查找选项文本
                if (question.getOptions() != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                        com.fasterxml.jackson.databind.JsonNode opts = om.readTree(question.getOptions());
                        if ("BANKED_CLOZE".equals(question.getQuestionType())) {
                            // 选词填空：显示 A. word
                            for (com.fasterxml.jackson.databind.JsonNode opt : opts) {
                                if (opt.get("label").asText().equals(correctAnswerPart)) {
                                    correctAnswerPart = correctAnswerPart + ". " + opt.get("text").asText();
                                }
                                if (opt.get("label").asText().equals(userAnswerDisplay)) {
                                    userAnswerDisplay = userAnswerDisplay + ". " + opt.get("text").asText();
                                }
                            }
                        } else if ("LONG_MATCH".equals(question.getQuestionType())) {
                            // 长篇匹配：subIndex对应第几个陈述，附上该陈述文本
                            if (subIndex < opts.size()) {
                                String stmtText = opts.get(subIndex).get("text").asText();
                                correctAnswerPart = correctAnswerPart + " — " + stmtText;
                                // 用户答案也附上同一陈述文本
                                userAnswerDisplay = userAnswerDisplay + " — " + stmtText;
                            }
                        }
                    } catch (Exception ignored) {}
                }
            } else {
                isCorrect = correctAnswerPart.equalsIgnoreCase(userAnswerDisplay);
                // 仔细阅读：查找选项内容
                if (question.getOptions() != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                        com.fasterxml.jackson.databind.JsonNode opts = om.readTree(question.getOptions());
                        for (com.fasterxml.jackson.databind.JsonNode opt : opts) {
                            if (opt.get("label").asText().equals(correctAnswerPart)) {
                                correctAnswerPart = correctAnswerPart + ". " + opt.get("text").asText();
                            }
                            if (opt.get("label").asText().equals(userAnswerDisplay)) {
                                userAnswerDisplay = userAnswerDisplay + ". " + opt.get("text").asText();
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }

            int itemScore = item.getScore() != null ? item.getScore() : 0;
            maxScore += itemScore;

            AnswerRecord record = new AnswerRecord();
            record.setUserId(userId);
            record.setQuestionId(item.getQuestionId());
            record.setSessionId(sessionId);
            record.setUserAnswer(item.getUserAnswer());
            record.setIsCorrect(isCorrect ? 1 : 0);
            record.setTimeTaken(item.getTimeTaken());
            answerRecordMapper.insert(record);

            if (isCorrect) {
                correctCount++;
                totalScore += itemScore;
            } else {
                updateErrorRecord(userId, item.getQuestionId(), item.getUserAnswer());
            }

            totalTime += item.getTimeTaken() != null ? item.getTimeTaken() : 0;

            Map<String, Object> detail = new HashMap<>();
            detail.put("questionId", item.getQuestionId());
            detail.put("content", question.getContent() + (subIndex > 0 ? " (第" + (subIndex + 1) + "题)" : ""));
            detail.put("correctAnswer", correctAnswerPart);
            detail.put("userAnswer", userAnswerDisplay.isEmpty() ? item.getUserAnswer() : userAnswerDisplay);
            detail.put("isCorrect", isCorrect);
            detail.put("score", isCorrect ? itemScore : 0);
            detail.put("analysis", question.getAnalysis());
            details.add(detail);
        }

        int totalQuestions = dto.getAnswers().size();
        int score = totalScore;

        // 保存学习记录
        LearningRecord learningRecord = new LearningRecord();
        learningRecord.setUserId(userId);
        learningRecord.setRecordType("TEST");
        learningRecord.setTitle("在线测试 - " + sessionId);
        learningRecord.setScore(score);
        learningRecord.setTotalQuestions(totalQuestions);
        learningRecord.setCorrectCount(correctCount);
        learningRecord.setTimeTaken(totalTime);
        learningRecordMapper.insert(learningRecord);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("score", score);
        result.put("totalQuestions", totalQuestions);
        result.put("correctCount", correctCount);
        result.put("timeTaken", totalTime);
        result.put("details", details);
        return result;
    }

    @Override
    public Map<String, Object> getTestDetail(String sessionId) {
        LambdaQueryWrapper<AnswerRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnswerRecord::getSessionId, sessionId);
        List<AnswerRecord> records = answerRecordMapper.selectList(wrapper);

        int correctCount = (int) records.stream().filter(r -> r.getIsCorrect() == 1).count();
        int totalQuestions = records.size();
        int score = totalQuestions > 0 ? (int) ((double) correctCount / totalQuestions * 100) : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("score", score);
        result.put("totalQuestions", totalQuestions);
        result.put("correctCount", correctCount);
        result.put("records", records);
        return result;
    }

    private void updateErrorRecord(Long userId, Long questionId, String wrongAnswer) {
        LambdaQueryWrapper<ErrorRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrorRecord::getUserId, userId);
        wrapper.eq(ErrorRecord::getQuestionId, questionId);
        ErrorRecord errorRecord = errorRecordMapper.selectOne(wrapper);

        if (errorRecord == null) {
            errorRecord = new ErrorRecord();
            errorRecord.setUserId(userId);
            errorRecord.setQuestionId(questionId);
            errorRecord.setErrorCount(1);
            errorRecord.setLastWrongAnswer(wrongAnswer);
            errorRecord.setMastered(0);
            errorRecord.setLastPracticeTime(LocalDateTime.now());
            errorRecordMapper.insert(errorRecord);
        } else {
            errorRecord.setErrorCount(errorRecord.getErrorCount() + 1);
            errorRecord.setLastWrongAnswer(wrongAnswer);
            errorRecord.setLastPracticeTime(LocalDateTime.now());
            errorRecordMapper.updateById(errorRecord);
        }
    }
}
