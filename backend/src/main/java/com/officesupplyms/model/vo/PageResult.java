package com.officesupplyms.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> list;
    private Long total;

    public static <T> PageResult<T> of(List<T> list, Long total) {
        return new PageResult<>(list, total);
    }
}
