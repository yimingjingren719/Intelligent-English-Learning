package com.english.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.entity.AnswerRecord;
import com.english.platform.entity.ErrorRecord;
import com.english.platform.entity.LearningRecord;
import com.english.platform.entity.Question;
import com.english.platform.mapper.AnswerRecordMapper;
import com.english.platform.mapper.ErrorRecordMapper;
import com.english.platform.mapper.LearningRecordMapper;
import com.english.platform.mapper.QuestionMapper;
import com.english.platform.service.LearningService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LearningServiceImpl implements LearningService {

    private final LearningRecordMapper learningRecordMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final ErrorRecordMapper errorRecordMapper;
    private final QuestionMapper questionMapper;

    public LearningServiceImpl(LearningRecordMapper learningRecordMapper,
                                AnswerRecordMapper answerRecordMapper,
                                ErrorRecordMapper errorRecordMapper,
                                QuestionMapper questionMapper) {
        this.learningRecordMapper = learningRecordMapper;
        this.answerRecordMapper = answerRecordMapper;
        this.errorRecordMapper = errorRecordMapper;
        this.questionMapper = questionMapper;
    }

    @Override
    public Page<LearningRecord> pageQuery(Long userId, Long page, Long size, String recordType) {
        Page<LearningRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<LearningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningRecord::getUserId, userId);
        wrapper.eq(recordType != null && !recordType.isEmpty(), LearningRecord::getRecordType, recordType);
        wrapper.orderByDesc(LearningRecord::getCreateTime);
        return learningRecordMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Map<String, Object> getStatistics(Long userId, Integer recentCount) {
        Map<String, Object> statistics = new HashMap<>();

        LambdaQueryWrapper<AnswerRecord> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(AnswerRecord::getUserId, userId);
        answerWrapper.orderByDesc(AnswerRecord::getCreateTime);
        if (recentCount != null && recentCount > 0) {
            answerWrapper.last("LIMIT " + (recentCount * 30));
        }
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(answerWrapper);

        int totalAnswers = answerRecords.size();
        int correctAnswers = (int) answerRecords.stream().filter(r -> r.getIsCorrect() == 1).count();
        double accuracy = totalAnswers > 0 ? (double) correctAnswers / totalAnswers * 100 : 0;

        LambdaQueryWrapper<ErrorRecord> errorWrapper = new LambdaQueryWrapper<>();
        errorWrapper.eq(ErrorRecord::getUserId, userId);
        if (recentCount != null && recentCount > 0) {
            errorWrapper.orderByDesc(ErrorRecord::getCreateTime);
            errorWrapper.last("LIMIT " + (recentCount * 10));
        }
        List<ErrorRecord> errorRecords = errorRecordMapper.selectList(errorWrapper);
        int unmasteredErrors = (int) errorRecords.stream().filter(r -> r.getMastered() == 0).count();

        LambdaQueryWrapper<LearningRecord> lrWrapper = new LambdaQueryWrapper<>();
        lrWrapper.eq(LearningRecord::getUserId, userId);
        lrWrapper.orderByDesc(LearningRecord::getCreateTime);
        if (recentCount != null && recentCount > 0) {
            lrWrapper.last("LIMIT " + recentCount);
        }
        List<LearningRecord> learningRecords = learningRecordMapper.selectList(lrWrapper);
        double avgScore = learningRecords.stream().filter(r -> r.getScore() != null)
                .mapToInt(LearningRecord::getScore).average().orElse(0);

        statistics.put("totalAnswers", totalAnswers);
        statistics.put("correctAnswers", correctAnswers);
        statistics.put("accuracy", Math.round(accuracy * 100.0) / 100.0);
        statistics.put("unmasteredErrors", unmasteredErrors);
        statistics.put("averageScore", Math.round(avgScore * 100.0) / 100.0);
        statistics.put("totalSessions", learningRecords.size());
        int totalTime = learningRecords.stream().filter(r -> r.getTimeTaken() != null)
                .mapToInt(LearningRecord::getTimeTaken).sum();
        statistics.put("totalTimeTaken", totalTime);
        return statistics;
    }

    @Override
    public Map<String, Object> getStatistics(Long userId) {
        return getStatistics(userId, null);
    }

    @Override
    public void recordOnlineTime(Long userId, Integer seconds) {
        // 查找今天的STUDY记录
        LambdaQueryWrapper<LearningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningRecord::getUserId, userId);
        wrapper.eq(LearningRecord::getRecordType, "STUDY");
        wrapper.eq(LearningRecord::getCreateTime,
                java.time.LocalDateTime.now().toLocalDate().atStartOfDay());
        wrapper.last("LIMIT 1");
        LearningRecord record = learningRecordMapper.selectOne(wrapper);
        if (record == null) {
            record = new LearningRecord();
            record.setUserId(userId);
            record.setRecordType("STUDY");
            record.setTitle("在线学习");
            record.setTimeTaken(seconds);
            learningRecordMapper.insert(record);
        } else {
            record.setTimeTaken((record.getTimeTaken() != null ? record.getTimeTaken() : 0) + seconds);
            learningRecordMapper.updateById(record);
        }
    }

    @Override
    public List<Map<String, Object>> getCalendarData(Long userId, int year, int month) {
        List<Map<String, Object>> result = new ArrayList<>();
        java.time.LocalDate firstDay = java.time.LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();

        for (int d = 1; d <= daysInMonth; d++) {
            java.time.LocalDate date = firstDay.withDayOfMonth(d);
            java.time.LocalDateTime dayStart = date.atStartOfDay();
            java.time.LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            // 当天学习记录
            LambdaQueryWrapper<LearningRecord> lrWrapper = new LambdaQueryWrapper<>();
            lrWrapper.eq(LearningRecord::getUserId, userId);
            lrWrapper.ge(LearningRecord::getCreateTime, dayStart);
            lrWrapper.lt(LearningRecord::getCreateTime, dayEnd);
            List<LearningRecord> dayRecords = learningRecordMapper.selectList(lrWrapper);

            // 当天答题记录
            LambdaQueryWrapper<AnswerRecord> arWrapper = new LambdaQueryWrapper<>();
            arWrapper.eq(AnswerRecord::getUserId, userId);
            arWrapper.ge(AnswerRecord::getCreateTime, dayStart);
            arWrapper.lt(AnswerRecord::getCreateTime, dayEnd);
            long questionCount = answerRecordMapper.selectCount(arWrapper);

            int totalTime = dayRecords.stream().filter(r -> r.getTimeTaken() != null)
                    .mapToInt(LearningRecord::getTimeTaken).sum();
            int testCount = (int) dayRecords.stream()
                    .filter(r -> "TEST".equals(r.getRecordType())).count();

            Map<String, Object> day = new HashMap<>();
            day.put("date", d);
            day.put("testCount", testCount);
            day.put("questionCount", (int) questionCount);
            day.put("totalTime", totalTime);
            day.put("hasActivity", !dayRecords.isEmpty() || questionCount > 0);
            result.add(day);
        }
        return result;
    }

    @Override
    public Map<String, Object> getDetailedStatistics(Long userId, Integer recentCount) {
        Map<String, Object> result = new HashMap<>();

        // 获取答题记录（recentCount=null或0表示全部）
        LambdaQueryWrapper<AnswerRecord> allWrapper = new LambdaQueryWrapper<>();
        allWrapper.eq(AnswerRecord::getUserId, userId);
        allWrapper.orderByDesc(AnswerRecord::getCreateTime);
        if (recentCount != null && recentCount > 0) {
            allWrapper.last("LIMIT " + (recentCount * 30));
        }
        List<AnswerRecord> answers = answerRecordMapper.selectList(allWrapper);

        // 按原始题目ID分组（去掉合成ID的后两位）
        Map<Long, List<AnswerRecord>> byExercise = new HashMap<>();
        for (AnswerRecord a : answers) {
            Question q = questionMapper.selectById(a.getQuestionId());
            if (q != null) {
                Long exId = a.getQuestionId() / 100; // 同一大题的各小题 = 同一exercise
                byExercise.computeIfAbsent(exId, k -> new ArrayList<>()).add(a);
            } else {
                // real question ID (CAREFUL_READING小題)
                Long exId = a.getQuestionId();
                // group by the passage question (nearby passage)
                byExercise.computeIfAbsent(exId, k -> new ArrayList<>()).add(a);
            }
        }

        // 按题型分组统计
        Map<String, List<Double>> exerciseAccuracies = new HashMap<>();
        exerciseAccuracies.put("BANKED_CLOZE", new ArrayList<>());
        exerciseAccuracies.put("LONG_MATCH", new ArrayList<>());
        Map<String, Integer> crCorrect = new HashMap<>(); crCorrect.put("total", 0); crCorrect.put("correct", 0);

        for (Map.Entry<Long, List<AnswerRecord>> entry : byExercise.entrySet()) {
            List<AnswerRecord> recs = entry.getValue();
            if (recs.isEmpty()) continue;
            // 取第一个record的question来判断类型
            Question q = questionMapper.selectById(recs.get(0).getQuestionId());
            if (q == null) {
                // try with /100
                q = questionMapper.selectById(recs.get(0).getQuestionId() / 100);
            }
            if (q == null) continue;
            String type = q.getQuestionType();

            int correct = (int) recs.stream().filter(r -> r.getIsCorrect() == 1).count();
            int total = recs.size();

            if ("BANKED_CLOZE".equals(type)) {
                double acc = total > 0 ? (double) correct / total * 100.0 : 0;
                exerciseAccuracies.get("BANKED_CLOZE").add(acc);
            } else if ("LONG_MATCH".equals(type)) {
                double acc = total > 0 ? (double) correct / total * 100.0 : 0;
                exerciseAccuracies.get("LONG_MATCH").add(acc);
            } else if ("CAREFUL_READING".equals(type)) {
                crCorrect.merge("total", total, Integer::sum);
                crCorrect.merge("correct", correct, Integer::sum);
            }
        }

        Map<String, Object> typeStats = new HashMap<>();
        // 选词填空：各篇正确率取平均
        List<Double> bcAccs = exerciseAccuracies.get("BANKED_CLOZE");
        double bcAvg = bcAccs.isEmpty() ? 0 : bcAccs.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        Map<String, Object> bcStats = new HashMap<>();
        bcStats.put("accuracy", Math.round(bcAvg * 10.0) / 10.0);
        bcStats.put("exerciseCount", bcAccs.size());
        typeStats.put("BANKED_CLOZE", bcStats);

        // 长篇匹配：各篇正确率取平均
        List<Double> lmAccs = exerciseAccuracies.get("LONG_MATCH");
        double lmAvg = lmAccs.isEmpty() ? 0 : lmAccs.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        Map<String, Object> lmStats = new HashMap<>();
        lmStats.put("accuracy", Math.round(lmAvg * 10.0) / 10.0);
        lmStats.put("exerciseCount", lmAccs.size());
        typeStats.put("LONG_MATCH", lmStats);

        // 仔细阅读：总正确数/总题数
        int crT = crCorrect.get("total"); int crC = crCorrect.get("correct");
        double crAcc = crT > 0 ? (double) crC / crT * 100.0 : 0;
        Map<String, Object> crStats = new HashMap<>();
        crStats.put("total", crT);
        crStats.put("correct", crC);
        crStats.put("accuracy", Math.round(crAcc * 10.0) / 10.0);
        typeStats.put("CAREFUL_READING", crStats);
        result.put("typeStats", typeStats);

        // 最近模拟试卷成绩
        LambdaQueryWrapper<LearningRecord> lrWrapper = new LambdaQueryWrapper<>();
        lrWrapper.eq(LearningRecord::getUserId, userId);
        lrWrapper.eq(LearningRecord::getRecordType, "TEST");
        lrWrapper.orderByDesc(LearningRecord::getCreateTime);
        lrWrapper.last("LIMIT 10");
        List<LearningRecord> recentTests = learningRecordMapper.selectList(lrWrapper);
        List<Map<String, Object>> scores = new ArrayList<>();
        for (LearningRecord r : recentTests) {
            Map<String, Object> s = new HashMap<>();
            s.put("score", r.getScore());
            s.put("time", r.getCreateTime().toString().substring(0, 10));
            scores.add(s);
        }
        result.put("recentScores", scores);

        // 错题掌握率
        LambdaQueryWrapper<ErrorRecord> errWrapper = new LambdaQueryWrapper<>();
        errWrapper.eq(ErrorRecord::getUserId, userId);
        long totalErrors = errorRecordMapper.selectCount(errWrapper);
        errWrapper = new LambdaQueryWrapper<>();
        errWrapper.eq(ErrorRecord::getUserId, userId);
        errWrapper.eq(ErrorRecord::getMastered, 1);
        long masteredErrors = errorRecordMapper.selectCount(errWrapper);
        result.put("errorMasteryRate", totalErrors > 0 ? Math.round((double)masteredErrors/totalErrors*1000)/10.0 : 0);

        // 学习频率：近30天内每天完成的大题数（每篇选词/匹配/阅读各算1大题）
        LambdaQueryWrapper<LearningRecord> freqWrapper = new LambdaQueryWrapper<>();
        freqWrapper.eq(LearningRecord::getUserId, userId);
        freqWrapper.ge(LearningRecord::getCreateTime, java.time.LocalDateTime.now().minusDays(30));
        List<LearningRecord> recent30 = learningRecordMapper.selectList(freqWrapper);
        // 每次测试记录的总题数反映大题数（50小题≈5大题=1次模拟）
        int totalExercises = 0;
        Set<String> studyDays = new HashSet<>();
        for (LearningRecord r : recent30) {
            studyDays.add(r.getCreateTime().toLocalDate().toString());
            if (r.getTotalQuestions() != null) {
                // 粗略估算：阅读5题/篇，选词10题/篇，匹配10题/篇
                totalExercises += Math.max(1, r.getTotalQuestions() / 5);
            }
        }
        int activeDays = studyDays.size();
        double dailyAvg = activeDays > 0 ? (double) totalExercises / activeDays : 0;
        result.put("dailyAvgExercises", Math.round(dailyAvg * 10.0) / 10.0);

        return result;
    }
}
