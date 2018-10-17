package com.grail.synchro.object;

public class LogFieldObject {
	private Integer fieldType; // Value from enum LogFieldType in SynchroGlobal.java
	private String fieldName;
	private String fieldValue;
	private String currency;
	private String fieldValue_Prev;
	private String currency_Prev;
	public LogFieldObject()
	{
		
	}
	
	public LogFieldObject(Integer fieldType, String fieldName)
	{
		this.fieldType = fieldType;
		this.fieldName = fieldName;		
	}
	
	public LogFieldObject(Integer fieldType, String fieldName, String fieldValue, String currency)
	{
		this.fieldType = fieldType;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
		this.currency = currency;
		
	}
	
	public LogFieldObject(Integer fieldType, String fieldName, String fieldValue)
	{
		this.fieldType = fieldType;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;		
		
	}
	
	public static LogFieldObject getLogEditedField(Integer fieldType, String fieldName, String fieldValue, String fieldValue_Prev)
	{
		LogFieldObject logFieldObj = new LogFieldObject();
		logFieldObj.fieldType = fieldType;
		logFieldObj.fieldName = fieldName;
		logFieldObj.fieldValue = fieldValue;
		logFieldObj.fieldValue_Prev = fieldValue_Prev;		
		return logFieldObj;
	}
	
	public LogFieldObject(Integer fieldType, String fieldName, String fieldValue, String currency, String fieldValue_Prev, String currency_Prev)
	{
		this.fieldType = fieldType;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
		this.currency = currency;
		this.fieldValue_Prev = fieldValue_Prev;
		this.currency_Prev = currency_Prev;
	}
	
	public Integer getFieldType() {
		return fieldType;
	}
	public void setFieldType(Integer fieldType) {
		this.fieldType = fieldType;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldValue() {
		return fieldValue;
	}
	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getFieldValue_Prev() {
		return fieldValue_Prev;
	}

	public void setFieldValue_Prev(String fieldValue_Prev) {
		this.fieldValue_Prev = fieldValue_Prev;
	}

	public String getCurrency_Prev() {
		return currency_Prev;
	}

	public void setCurrency_Prev(String currency_Prev) {
		this.currency_Prev = currency_Prev;
	}
	

}
