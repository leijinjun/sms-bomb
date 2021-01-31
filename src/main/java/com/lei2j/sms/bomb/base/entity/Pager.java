package com.lei2j.sms.bomb.base.entity;

import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

/**
 * @author leijinjun
 * @date 2021/1/17
 **/
public class Pager<T> {

    public static final Integer MAX_SIZE = 5000;

    private final Integer pageNo;

    private final Integer pageSize;

    private List<T> list;

    private Integer totalCount;

    private Integer totalPages;

    protected Pager(Integer pageNo, Integer pageSize) {
        this.pageNo = (pageNo == null || pageNo <= 0) ? 1 : pageNo;
        this.pageSize = pageSize == null ? 20 : (pageSize > MAX_SIZE ? MAX_SIZE : pageSize);
    }

    protected Pager(Integer pageNo, Integer pageSize, List<T> list, Integer totalCount, Integer totalPages) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.list = list == null ? Collections.emptyList() : list;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
    }

    public static <T> Pager<T> of(Integer pageNo, Integer pageSize) {
        return new Pager<>(pageNo, pageSize);
    }

    public static <T> Pager<T> of(Integer pageNo, Integer pageSize, Integer totalCount, Integer totalPages, List<T> content) {
        return new Pager<>(pageNo, pageSize, content, totalCount, totalPages);
    }

    public static <T> Pager<T> convert(Page<T> page) {
        return of(page.getNumber() + 1, page.getSize(), (int) page.getTotalElements(), page.getTotalPages(), page.getContent());
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getOffset() {
        return (pageNo - 1) * pageSize;
    }
}
