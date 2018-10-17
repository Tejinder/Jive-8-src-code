package com.grail.synchro.beans;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/14/14
 * Time: 2:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class Quarter {
    private Integer startMonth;
    private Integer startYear;
    private Integer endMonth;
    private Integer endYear;

    public Integer getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(Integer startMonth) {
        this.startMonth = startMonth;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(Integer endMonth) {
        this.endMonth = endMonth;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public void setEndYear(Integer endYear) {
        this.endYear = endYear;
    }
}
