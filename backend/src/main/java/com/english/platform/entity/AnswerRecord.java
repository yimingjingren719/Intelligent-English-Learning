package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("answer_records")
public class AnswerRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 题目ID */
    private Long questionId;

    /** 测试会话ID（同一次测试的多道题共享） */
    private String sessionId;

    /** 用户答案 */
    private String userAnswer;

    /** 是否正确：0-错误 1-正确 */
    private Integer isCorrect;

    /** 答题用时（秒） */
    private Integer timeTaken;

    /** AI评价（JSON格式） */
    private String aiFeedback;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
