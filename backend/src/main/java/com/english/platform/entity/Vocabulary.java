package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("vocabulary")
public class Vocabulary {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 单词 */
    private String word;

    /** 中文释义 */
    private String translation;

    /** 复习次数 */
    private Integer reviewCount;

    /** 掌握程度 0-未掌握 1-已掌握 */
    private Integer mastered;

    /** 最近复习时间 */
    private LocalDateTime lastReviewTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
