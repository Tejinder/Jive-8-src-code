package com.grail.synchro.search.filter;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/29/14
 * Time: 5:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class StandardReportFilter {

    private String keyword;
    private Integer start;
    private Integer limit;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        keyword = keyword.replaceAll("\\'","\\\\'");
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
}
