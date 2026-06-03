package com.english.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.english.platform.common.PageResult;
import com.english.platform.common.Result;
import com.english.platform.entity.Vocabulary;
import com.english.platform.service.VocabularyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @GetMapping("/page")
    public Result<PageResult<Vocabulary>> page(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "20") Long size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<Vocabulary> vPage = vocabularyService.pageQuery(userId, page, size);
        return Result.success(PageResult.of(vPage.getTotal(), vPage.getCurrent(), vPage.getSize(), vPage.getRecords()));
    }

    @PostMapping
    public Result<Vocabulary> add(HttpServletRequest request, @RequestBody Vocabulary vocabulary) {
        Long userId = (Long) request.getAttribute("userId");
        Vocabulary v = vocabularyService.add(userId, vocabulary);
        return Result.success("添加成功", v);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        vocabularyService.delete(id, userId);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/master")
    public Result<?> markMastered(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        vocabularyService.markMastered(id, userId);
        return Result.success("已标记掌握");
    }

    @GetMapping("/review")
    public Result<PageResult<Vocabulary>> review(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "20") Long size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<Vocabulary> vPage = vocabularyService.getReviewWords(userId, page, size);
        return Result.success(PageResult.of(vPage.getTotal(), vPage.getCurrent(), vPage.getSize(), vPage.getRecords()));
    }

    /** 系统词库（六级词汇） */
    @GetMapping("/system")
    public Result<PageResult<Vocabulary>> systemWords(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "50") Long size,
            @RequestParam(required = false) String keyword) {
        Page<Vocabulary> vPage = vocabularyService.getSystemWords(page, size, keyword);
        return Result.success(PageResult.of(vPage.getTotal(), vPage.getCurrent(), vPage.getSize(), vPage.getRecords()));
    }

    /** 从系统词库添加到我的生词本 */
    @PostMapping("/learn/{id}")
    public Result<Vocabulary> learnFromSystem(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Vocabulary v = vocabularyService.learnFromSystem(userId, id);
        return Result.success("已加入学习", v);
    }
}
