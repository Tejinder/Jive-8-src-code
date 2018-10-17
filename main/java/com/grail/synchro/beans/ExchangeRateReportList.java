package com.grail.synchro.beans;

import java.util.List;

public class ExchangeRateReportList {

	private List<ExchangeRateReport> exchangeRateReportList;
	private Integer year;
	
	public List<ExchangeRateReport> getExchangeRateReportList() {
		return exchangeRateReportList;
	}
	public void setExchangeRateReportList(
			List<ExchangeRateReport> exchangeRateReportList) {
		this.exchangeRateReportList = exchangeRateReportList;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}

}
