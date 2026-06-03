package com.english.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("error_records")
public class ErrorRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 题目ID */
    private Long questionId;

    /** 错误次数 */
    private Integer errorCount;

    /** 最近错误答案 */
    private String lastWrongAnswer;

    /** 是否已掌握：0-未掌握 1-已掌握 */
    private Integer mastered;

    /** 最后练习时间 */
    private LocalDateTime lastPracticeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
