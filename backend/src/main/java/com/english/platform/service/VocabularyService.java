package com.english.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.entity.Vocabulary;

public interface VocabularyService {
    /** 分页查询生词本 */
    Page<Vocabulary> pageQuery(Long userId, Long page, Long size);

    /** 添加生词 */
    Vocabulary add(Long userId, Vocabulary vocabulary);

    /** 删除生词 */
    void delete(Long id, Long userId);

    /** 标记已掌握 */
    void markMastered(Long id, Long userId);

    /** 获取待复习单词 */
    Page<Vocabulary> getReviewWords(Long userId, Long page, Long size);

    /** 获取系统词库（user_id=0的六级词汇） */
    Page<Vocabulary> getSystemWords(Long page, Long size, String keyword);

    /** 用户从系统词库学习单词（复制到个人生词本） */
    Vocabulary learnFromSystem(Long userId, Long vocabularyId);
}
