package com.english.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.dto.QuestionDTO;
import com.english.platform.entity.Question;
import com.english.platform.exception.BusinessException;
import com.english.platform.mapper.QuestionMapper;
import com.english.platform.service.QuestionService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;

    public QuestionServiceImpl(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    @Override
    public Page<Question> pageQuery(Long page, Long size, String questionType,
                                     Integer difficulty, String keyword) {
        Page<Question> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(questionType != null && !questionType.isEmpty(), Question::getQuestionType, questionType);
        wrapper.eq(difficulty != null, Question::getDifficulty, difficulty);
        wrapper.like(keyword != null && !keyword.isEmpty(), Question::getContent, keyword);
        wrapper.eq(Question::getStatus, 0);
        wrapper.orderByDesc(Question::getCreateTime);
        return questionMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Question getById(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        return question;
    }

    @Override
    public Question add(QuestionDTO dto, Long creatorId) {
        Question question = new Question();
        question.setQuestionType(dto.getQuestionType());
        question.setContent(dto.getContent());
        question.setOptions(dto.getOptions());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setAnalysis(dto.getAnalysis());
        question.setDifficulty(dto.getDifficulty());
        question.setTags(dto.getTags());
        question.setStatus(0);
        question.setCreatorId(creatorId);
        questionMapper.insert(question);
        return question;
    }

    @Override
    public Question update(Long id, QuestionDTO dto) {
        Question question = getById(id);
        question.setQuestionType(dto.getQuestionType());
        question.setContent(dto.getContent());
        question.setOptions(dto.getOptions());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setAnalysis(dto.getAnalysis());
        question.setDifficulty(dto.getDifficulty());
        question.setTags(dto.getTags());
        questionMapper.updateById(question);
        return question;
    }

    @Override
    public void delete(Long id) {
        Question question = getById(id);
        questionMapper.deleteById(question.getId());
    }

    @Override
    public List<Question> randomQuestions(String questionType, Integer difficulty, Integer count) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(questionType != null && !questionType.isEmpty(), Question::getQuestionType, questionType);
        wrapper.eq(difficulty != null, Question::getDifficulty, difficulty);
        wrapper.eq(Question::getStatus, 0);
        // 阅读理解按ID排序（文章+题目在一起），其他类型随机
        if ("READING".equals(questionType)) {
            wrapper.orderByAsc(Question::getId);
            wrapper.last("LIMIT " + count);
        } else {
            wrapper.last("ORDER BY RAND() LIMIT " + count);
        }
        return questionMapper.selectList(wrapper);
    }
}
