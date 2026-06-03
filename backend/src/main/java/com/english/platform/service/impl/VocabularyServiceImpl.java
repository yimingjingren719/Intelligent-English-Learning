package com.english.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.entity.Vocabulary;
import com.english.platform.exception.BusinessException;
import com.english.platform.mapper.VocabularyMapper;
import com.english.platform.service.VocabularyService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VocabularyServiceImpl implements VocabularyService {

    private final VocabularyMapper vocabularyMapper;

    public VocabularyServiceImpl(VocabularyMapper vocabularyMapper) {
        this.vocabularyMapper = vocabularyMapper;
    }

    @Override
    public Page<Vocabulary> pageQuery(Long userId, Long page, Long size) {
        Page<Vocabulary> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Vocabulary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vocabulary::getUserId, userId);
        wrapper.eq(Vocabulary::getMastered, 0);
        wrapper.orderByDesc(Vocabulary::getCreateTime);
        return vocabularyMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Vocabulary add(Long userId, Vocabulary vocabulary) {
        vocabulary.setUserId(userId);
        vocabulary.setReviewCount(0);
        vocabulary.setMastered(0);
        vocabularyMapper.insert(vocabulary);
        return vocabulary;
    }

    @Override
    public void delete(Long id, Long userId) {
        Vocabulary v = vocabularyMapper.selectById(id);
        if (v == null || !v.getUserId().equals(userId)) {
            throw new BusinessException("生词记录不存在");
        }
        vocabularyMapper.deleteById(id);
    }

    @Override
    public void markMastered(Long id, Long userId) {
        Vocabulary v = vocabularyMapper.selectById(id);
        if (v == null || !v.getUserId().equals(userId)) {
            throw new BusinessException("生词记录不存在");
        }
        v.setMastered(1);
        vocabularyMapper.updateById(v);
    }

    @Override
    public Page<Vocabulary> getReviewWords(Long userId, Long page, Long size) {
        Page<Vocabulary> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Vocabulary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vocabulary::getUserId, userId);
        wrapper.eq(Vocabulary::getMastered, 0);
        wrapper.orderByAsc(Vocabulary::getLastReviewTime);
        wrapper.orderByAsc(Vocabulary::getReviewCount);
        return vocabularyMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Page<Vocabulary> getSystemWords(Long page, Long size, String keyword) {
        Page<Vocabulary> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Vocabulary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vocabulary::getUserId, 0L);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Vocabulary::getWord, keyword);
        }
        wrapper.orderByAsc(Vocabulary::getId);
        return vocabularyMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Vocabulary learnFromSystem(Long userId, Long vocabularyId) {
        Vocabulary sysWord = vocabularyMapper.selectById(vocabularyId);
        if (sysWord == null || !sysWord.getUserId().equals(0L)) {
            throw new BusinessException("系统词库中不存在该单词");
        }
        // 检查用户是否已有此词
        LambdaQueryWrapper<Vocabulary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vocabulary::getUserId, userId);
        wrapper.eq(Vocabulary::getWord, sysWord.getWord());
        if (vocabularyMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该单词已在您的生词本中");
        }
        Vocabulary userWord = new Vocabulary();
        userWord.setUserId(userId);
        userWord.setWord(sysWord.getWord());
        userWord.setTranslation(sysWord.getTranslation());
        userWord.setReviewCount(0);
        userWord.setMastered(0);
        vocabularyMapper.insert(userWord);
        return userWord;
    }
}
