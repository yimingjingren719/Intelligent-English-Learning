package com.english.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.common.PageResult;
import com.english.platform.common.Result;
import com.english.platform.dto.QuestionDTO;
import com.english.platform.entity.Question;
import com.english.platform.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * 分页查询题目（管理员）
     */
    @GetMapping("/page")
    public Result<PageResult<Question>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword) {
        Page<Question> questionPage = questionService.pageQuery(page, size, questionType, difficulty, keyword);
        PageResult<Question> pageResult = PageResult.of(
                questionPage.getTotal(), questionPage.getCurrent(),
                questionPage.getSize(), questionPage.getRecords());
        return Result.success(pageResult);
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        Question question = questionService.getById(id);
        return Result.success(question);
    }

    /**
     * 新增题目（管理员）
     */
    @PostMapping
    public Result<Question> add(@Valid @RequestBody QuestionDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Question question = questionService.add(dto, userId);
        return Result.success("添加成功", question);
    }

    /**
     * 更新题目（管理员）
     */
    @PutMapping("/{id}")
    public Result<Question> update(@PathVariable Long id, @Valid @RequestBody QuestionDTO dto) {
        Question question = questionService.update(id, dto);
        return Result.success("更新成功", question);
    }

    /**
     * 删除题目（管理员）
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 随机获取题目（用于测试和练习）
     */
    @GetMapping("/random")
    public Result<List<Question>> random(
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(defaultValue = "10") Integer count) {
        List<Question> questions = questionService.randomQuestions(questionType, difficulty, count);
        return Result.success(questions);
    }
}
