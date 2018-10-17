package com.grail.synchro.beans;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 6/3/14
 * Time: 2:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class CurrencyExchangeRate {
    private Long currencyId;
    private BigDecimal exchangeRate;
    private String currencyCode;

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
