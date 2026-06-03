package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("questions")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题目类型：SINGLE_CHOICE / TRUE_FALSE / FILL_BLANK / READING */
    private String questionType;

    /** 题目内容（富文本HTML） */
    private String content;

    /** 选项JSON：[{"label":"A","text":"..."}] */
    private String options;

    /** 正确答案 */
    private String correctAnswer;

    /** 答案解析 */
    private String analysis;

    /** 难度等级：1-10 */
    private Integer difficulty;

    /** 所属知识点/标签 */
    private String tags;

    /** 状态：0-启用 1-禁用 */
    private Integer status;

    /** 创建者ID */
    private Long creatorId;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
