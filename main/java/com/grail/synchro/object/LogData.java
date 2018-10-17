package com.grail.synchro.object;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class LogData {

	private List<LogFieldObject> data;
	private Boolean multiple;
	private String description;
	
	public LogData()
	{
		this.multiple = true;
		this.description = StringUtils.EMPTY;
		this.data = new ArrayList<LogFieldObject>();
	}
	
	public LogData(String description)
	{
		this.multiple = false;
		this.description = description;
	}
	
	public List<LogFieldObject> getData() {
		return data;
	}

	public void setData(List<LogFieldObject> data) {
		this.data = data;
	}

	public Boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void addLogFieldObject(LogFieldObject obj)
	{
		this.data.add(obj);
	}
	
	public void emptyLogFieldObject()
	{
		this.data.clear();
	}
	
	@Override
    public String toString() {
        return String.format("{data:%s}", data);
    }

}
