package com.english.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.dto.QuestionDTO;
import com.english.platform.entity.Question;

import java.util.List;

public interface QuestionService {
    /** 分页查询题目 */
    Page<Question> pageQuery(Long page, Long size, String questionType, Integer difficulty, String keyword);

    /** 获取题目详情 */
    Question getById(Long id);

    /** 新增题目 */
    Question add(QuestionDTO dto, Long creatorId);

    /** 更新题目 */
    Question update(Long id, QuestionDTO dto);

    /** 删除题目 */
    void delete(Long id);

    /** 随机获取指定数量题目（用于测试） */
    List<Question> randomQuestions(String questionType, Integer difficulty, Integer count);
}
