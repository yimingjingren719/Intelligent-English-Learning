package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("learning_records")
public class LearningRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 记录类型：TEST / PRACTICE / AI_QA */
    private String recordType;

    /** 标题 */
    private String title;

    /** 详细内容（JSON格式） */
    private String content;

    /** 得分（测试类） */
    private Integer score;

    /** 总题数 */
    private Integer totalQuestions;

    /** 正确数 */
    private Integer correctCount;

    /** 用时（秒） */
    private Integer timeTaken;

    /** 涉及知识点 */
    private String tags;

    /** AI分析结果 */
    private String aiAnalysis;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
