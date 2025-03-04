package com.zzz.account.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
    /**
     * 当前页数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private long total;
}