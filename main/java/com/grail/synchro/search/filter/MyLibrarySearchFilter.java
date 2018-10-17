package com.grail.synchro.search.filter;

/**
 *
 */
public class MyLibrarySearchFilter {
    private String keyword;
    private Integer start;
    private Integer limit;
    private Long userId;
    private String sortField;
    private Integer ascendingOrder;

    public MyLibrarySearchFilter() {

    }

    public MyLibrarySearchFilter(String keyword, Integer start, Integer limit, Long userId,
                                 String sortField, Integer ascendingOrder) {
        this.keyword = keyword;
        this.start = start;
        this.limit = limit;
        this.userId = userId;
        this.sortField = sortField;
        this.ascendingOrder = ascendingOrder;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public Integer getAscendingOrder() {
        return ascendingOrder;
    }

    public void setAscendingOrder(Integer ascendingOrder) {
        this.ascendingOrder = ascendingOrder;
    }
}
