package com.english.platform.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class TestSubmitDTO {
    @NotEmpty(message = "答案列表不能为空")
    private List<AnswerItem> answers;

    private String sessionId;

    @Data
    public static class AnswerItem {
        private Long questionId;
        private String userAnswer;
        private Integer timeTaken;
        private Integer score; // 单题分值（选词3/匹配7/阅读14）
    }
}
