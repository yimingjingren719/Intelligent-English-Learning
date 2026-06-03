package com.english.platform.common;

import lombok.Data;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
public class PageResult<T> {
    private Long total;
    private Long page;
    private Long size;
    private List<T> records;

    public static <T> PageResult<T> of(Long total, Long page, Long size, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.total = total;
        r.page = page;
        r.size = size;
        r.records = records;
        return r;
    }
}
