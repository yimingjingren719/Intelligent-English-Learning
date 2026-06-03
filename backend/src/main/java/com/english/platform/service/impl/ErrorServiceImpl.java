package com.english.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.entity.ErrorRecord;
import com.english.platform.entity.Question;
import com.english.platform.exception.BusinessException;
import com.english.platform.mapper.ErrorRecordMapper;
import com.english.platform.mapper.QuestionMapper;
import com.english.platform.service.ErrorService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ErrorServiceImpl implements ErrorService {

    private final ErrorRecordMapper errorRecordMapper;
    private final QuestionMapper questionMapper;

    public ErrorServiceImpl(ErrorRecordMapper errorRecordMapper, QuestionMapper questionMapper) {
        this.errorRecordMapper = errorRecordMapper;
        this.questionMapper = questionMapper;
    }

    @Override
    public Page<ErrorRecord> pageQuery(Long userId, Long page, Long size) {
        Page<ErrorRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ErrorRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrorRecord::getUserId, userId);
        wrapper.eq(ErrorRecord::getMastered, 0);
        wrapper.orderByDesc(ErrorRecord::getUpdateTime);
        return errorRecordMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Map<String, Object> getDetail(Long errorId) {
        ErrorRecord errorRecord = errorRecordMapper.selectById(errorId);
        if (errorRecord == null) {
            throw new BusinessException("错题记录不存在");
        }
        Long qid = errorRecord.getQuestionId();
        // 处理合成ID（选词填空/长篇匹配的子题ID）
        Long realQid = qid > 100 ? qid / 100 : qid;
        Question question = questionMapper.selectById(realQid);

        Map<String, Object> result = new HashMap<>();
        result.put("errorRecord", errorRecord);
        result.put("question", question);

        // 仔细阅读：查找关联的PASSAGE和同一文章的其它题目
        if (question != null && "CAREFUL_READING".equals(question.getQuestionType())
                && !"PASSAGE".equals(question.getCorrectAnswer())) {
            // 向前查找最近的PASSAGE
            LambdaQueryWrapper<Question> passageWrapper = new LambdaQueryWrapper<>();
            passageWrapper.eq(Question::getQuestionType, "CAREFUL_READING");
            passageWrapper.eq(Question::getCorrectAnswer, "PASSAGE");
            passageWrapper.lt(Question::getId, realQid);
            passageWrapper.orderByDesc(Question::getId);
            passageWrapper.last("LIMIT 1");
            Question passage = questionMapper.selectOne(passageWrapper);
            if (passage != null) {
                result.put("passage", passage);
                // 查找该文章的所有小题
                LambdaQueryWrapper<Question> qsWrapper = new LambdaQueryWrapper<>();
                qsWrapper.eq(Question::getQuestionType, "CAREFUL_READING");
                qsWrapper.ne(Question::getCorrectAnswer, "PASSAGE");
                qsWrapper.gt(Question::getId, passage.getId());
                qsWrapper.lt(Question::getId,
                        questionMapper.selectList(new LambdaQueryWrapper<Question>()
                                .eq(Question::getQuestionType, "CAREFUL_READING")
                                .eq(Question::getCorrectAnswer, "PASSAGE")
                                .gt(Question::getId, passage.getId())
                                .orderByAsc(Question::getId)
                                .last("LIMIT 1")).stream().findFirst().map(Question::getId).orElse(realQid + 100));
                qsWrapper.orderByAsc(Question::getId);
                qsWrapper.last("LIMIT 5");
                List<Question> related = questionMapper.selectList(qsWrapper);
                if (!related.isEmpty()) result.put("relatedQuestions", related);
            }
        }
        return result;
    }

    @Override
    public void markMastered(Long errorId, Long userId) {
        ErrorRecord errorRecord = errorRecordMapper.selectById(errorId);
        if (errorRecord == null || !errorRecord.getUserId().equals(userId)) {
            throw new BusinessException("错题记录不存在");
        }
        errorRecord.setMastered(1);
        errorRecordMapper.updateById(errorRecord);
    }

    @Override
    public Map<String, Object> practiceWrongQuestions(Long userId, Integer count) {
        LambdaQueryWrapper<ErrorRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrorRecord::getUserId, userId);
        wrapper.eq(ErrorRecord::getMastered, 0);
        wrapper.orderByDesc(ErrorRecord::getErrorCount);
        wrapper.last("LIMIT " + count);
        List<ErrorRecord> errorRecords = errorRecordMapper.selectList(wrapper);

        List<Question> questions = new ArrayList<>();
        for (ErrorRecord er : errorRecords) {
            Question question = questionMapper.selectById(er.getQuestionId());
            if (question != null) {
                questions.add(question);
            }
        }

        String sessionId = "PRACTICE-" + UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("questions", questions);
        result.put("totalCount", questions.size());
        return result;
    }
}
