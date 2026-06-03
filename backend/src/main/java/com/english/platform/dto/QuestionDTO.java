package com.english.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionDTO {
    @NotBlank(message = "题目类型不能为空")
    private String questionType;

    @NotBlank(message = "题目内容不能为空")
    private String content;

    private String options;

    @NotBlank(message = "正确答案不能为空")
    private String correctAnswer;

    private String analysis;

    @NotNull(message = "难度不能为空")
    private Integer difficulty;

    private String tags;
}
