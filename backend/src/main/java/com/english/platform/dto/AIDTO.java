package com.english.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AIDTO {
    @NotBlank(message = "问题不能为空")
    private String question;

    /** 上下文类型：GRAMMAR / VOCABULARY / READING / WRITING / GENERAL */
    private String contextType;

    /** 可选的题目ID关联 */
    private Long questionId;
}
