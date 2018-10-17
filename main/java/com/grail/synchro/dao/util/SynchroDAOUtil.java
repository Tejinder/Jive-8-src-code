package com.grail.synchro.dao.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.util.Log;

import com.grail.synchro.SynchroGlobal;
import com.jivesoftware.base.database.dao.JiveJdbcDaoSupport;
import com.jivesoftware.util.StringUtils;

/**
 * @author Samee K.S
 * @version 1.0, Date: 5/29/13
 */
public class SynchroDAOUtil extends JiveJdbcDaoSupport {

	private static final Logger LOGGER = Logger.getLogger(SynchroDAOUtil.class);
    private final String ENTITY_MAX_QUERY = "SELECT MAX(%s) FROM %s";
    private final String PROJECT_MAX_QUERY = "SELECT MAX(projectid) FROM grailprojectnew where projectid >= %s and projectid < %s";
    private final String WAIVER_MAX_QUERY = "SELECT MAX(waiverid) FROM grailwaiver where waiverid >= %s and waiverid < %s";
    private final int MAX_PROJECTCODE_DIGITS = 5;
    
    public Long nextSequenceID(final String columnName, final String tableName){
        Long currentMax = getSimpleJdbcTemplate().queryForLong(String.format(ENTITY_MAX_QUERY, columnName, tableName));
        if(currentMax == null || currentMax == 0){
            currentMax = 1L;
        }else{
            currentMax = (currentMax+1L);
        }
        return currentMax;
    }
    
    public String nextSequenceProjectID(final String columnName, final String tableName){
    	String projectID = "";
        Long currentMax = getSimpleJdbcTemplate().queryForLong(String.format(ENTITY_MAX_QUERY, columnName, tableName));
        if(currentMax == null || currentMax == 0){
            currentMax = 1L;
        }else{
            currentMax = (currentMax+1L);
        }
        projectID = currentMax.toString(); 
        if(projectID.length()<5)
        {
        	StringBuilder prepend = new StringBuilder();
        	int prependCount = MAX_PROJECTCODE_DIGITS - projectID.length();        	
        	for(int i=0; i<prependCount; i++)
        	{
        		prepend.append("0");
        	}
        	
        	projectID = prepend.toString() + projectID;
        }
        
        return projectID;
    }
    
    public Long generateProjectID(){
    	Calendar calender = Calendar.getInstance();
    	Date date = calender.getTime();
    	calender.setTime(date);
    	SimpleDateFormat sdf = new SimpleDateFormat("yy");
        int year = Integer.parseInt(sdf.format(date));
        int week = calender.get(Calendar.WEEK_OF_YEAR);
        int totalWeeks = calender.getMaximum(Calendar.WEEK_OF_YEAR); 
        String minValue = year+""+week+"000";
        String maxValue = year+""+(week+1)+"000";
        
        if((week+1) > totalWeeks )
        {
        	maxValue = (year+1)+"01000";
        }
       
        Long currentMax = getSimpleJdbcTemplate().queryForLong(String.format(PROJECT_MAX_QUERY, minValue, maxValue));
        if(currentMax == null || currentMax == 0){
            currentMax = Long.parseLong(year+""+week+"000");
        }else{
            currentMax = (currentMax+1L);
        }
        return currentMax;
    }
    
    public Long generateWaiverID(){
    	Calendar calender = Calendar.getInstance();
    	Date date = calender.getTime();
    	calender.setTime(date);
    	SimpleDateFormat sdf = new SimpleDateFormat("yy");
        int year = Integer.parseInt(sdf.format(date));
        int week = calender.get(Calendar.WEEK_OF_YEAR);
        int totalWeeks = calender.getMaximum(Calendar.WEEK_OF_YEAR); 
        String minValue = year+""+week+"000";
        String maxValue = year+""+(week+1)+"000";
        
        if((week+1) > totalWeeks )
        {
        	maxValue = (year+1)+"01000";
        }
       
        Long currentMax = getSimpleJdbcTemplate().queryForLong(String.format(WAIVER_MAX_QUERY, minValue, maxValue));
        if(currentMax == null || currentMax == 0){
            currentMax = Long.parseLong(year+""+week+"000");
        }else{
            currentMax = (currentMax+1L);
        }
        return currentMax;
    }
    
    public Long generateInviteUserID(final String columnName, final String tableName){
        Long currentMax = getSimpleJdbcTemplate().queryForLong(String.format(ENTITY_MAX_QUERY, columnName, tableName));
        if(currentMax == null || currentMax == 0){
            currentMax = 100000L;
        }else{
            currentMax = (currentMax+1L);
        }
        return currentMax;
    }

    public Long getStartDayOfYear(Integer year)
    {
    	Calendar calendarStart=Calendar.getInstance();
	    calendarStart.set(Calendar.YEAR,year);
	    calendarStart.set(Calendar.MONTH,0);
	    calendarStart.set(Calendar.DAY_OF_MONTH,1);
	    Date startDate=calendarStart.getTime();
	    return startDate.getTime();
    }
    
    public Long getEndDayOfYear(Integer year)
    {
	    Calendar calendarEnd=Calendar.getInstance();
	    calendarEnd.set(Calendar.YEAR,year);
	    calendarEnd.set(Calendar.MONTH,11);
	    calendarEnd.set(Calendar.DAY_OF_MONTH,31);
	    Date endDate=calendarEnd.getTime();
	    return endDate.getTime();
    }
    
    public List<Long> getProjectStatus(List<Long> genericStatus)
    {
    	List<Long> status = new ArrayList<Long>();
    	if(genericStatus!=null)
    	{
	    	for(Long gStatus : genericStatus)
	    	{
	    		if(SynchroGlobal.ProjectStatusFilterAll.OPEN.getId() == gStatus)
	    			{
						if(!status.contains(Long.parseLong(SynchroGlobal.Status.PIT_OPEN.ordinal()+"")))
							{
								status.add(Long.parseLong(SynchroGlobal.Status.PIT_OPEN.ordinal()+""));
							}
						if(!status.contains(Long.parseLong(SynchroGlobal.Status.PIB_OPEN.ordinal()+"")))
							{
								status.add(Long.parseLong(SynchroGlobal.Status.PIB_OPEN.ordinal()+""));
							}
						if(!status.contains(Long.parseLong(SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+"")))
							{
								status.add(Long.parseLong(SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()+""));
							}
	    				
	    			}
		    		else if(SynchroGlobal.ProjectStatusFilterAll.ONHOLD.getId() == gStatus)
		    			{
						if(!status.contains(Long.parseLong(SynchroGlobal.Status.PIT_ONHOLD.ordinal()+"")))
							{
								status.add(Long.parseLong(SynchroGlobal.Status.PIT_ONHOLD.ordinal()+""));
							}
						if(!status.contains(Long.parseLong(SynchroGlobal.Status.PIB_ONHOLD.ordinal()+"")))
							{
								status.add(Long.parseLong(SynchroGlobal.Status.PIB_ONHOLD.ordinal()+""));
							}
						if(!status.contains(Long.parseLong(SynchroGlobal.Status.INPROGRESS_ONHOLD.ordinal()+"")))
							{
								status.add(Long.parseLong(SynchroGlobal.Status.INPROGRESS_ONHOLD.ordinal()+""));
							}
					
		    			}
		    		else if(SynchroGlobal.ProjectStatusFilterAll.CANCEL.getId() == gStatus)
		    			{
						if(!status.contains(Long.parseLong(SynchroGlobal.Status.PIT_CANCEL.ordinal()+"")))
							{
								status.add(Long.parseLong(SynchroGlobal.Status.PIT_CANCEL.ordinal()+""));
							}
						if(!status.contains(Long.parseLong(SynchroGlobal.Status.PIB_CANCEL.ordinal()+"")))
							{
								status.add(Long.parseLong(SynchroGlobal.Status.PIB_CANCEL.ordinal()+""));
							}
						if(!status.contains(Long.parseLong(SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()+"")))
							{
								status.add(Long.parseLong(SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()+""));
							}
		    			}
		    		
		    		else if(SynchroGlobal.ProjectStatusFilterAll.COMPLETED.getId() == gStatus)
					{
		    			if(!status.contains(Long.parseLong(SynchroGlobal.Status.COMPLETED.ordinal()+"")))
						{
							status.add(Long.parseLong(SynchroGlobal.Status.COMPLETED.ordinal()+""));
						}
		    			
					}
		    		else if(SynchroGlobal.ProjectStatusFilterAll.DELETED.getId() == gStatus)
					{
		    			if(!status.contains(Long.parseLong(SynchroGlobal.Status.DELETED.ordinal()+"")))
						{
							status.add(Long.parseLong(SynchroGlobal.Status.DELETED.ordinal()+""));
						}
					}
	    	}
    	}
    	return status;
    }
    
    /** Synchro Phase 4 Dao Utils*/
    public Long getDateTimeStamp(String date)
    {
         try{
        	 
        	 Date dateObj = new SimpleDateFormat("dd/MM/yyyy").parse(date);
        
        	 return dateObj.getTime();
        	 
         }catch(ParseException pex){LOGGER.error("Error parsing start and end dates in create project");}
         
         catch(Exception e){LOGGER.error("Error parsing start and end dates in create project"+e.getMessage());}
         
         return System.currentTimeMillis();
    }
    
    public String getDateString(Long timestamp)
    {
    	return new SimpleDateFormat("dd/MM/yyyy").format(new Date(timestamp));
    }
    
    public List<Long> getIDs(String array)
    {
    	List<Long> list = new ArrayList<Long>();
    	if(array!=null && array.length()>0)
    	{
	    	for (String id : array.split(","))
	    	    list.add(new Long(id));
    	}
    	return list;
    }
    
    public static List<String> getPortalNames(List<Long> portals)
    {
    	List<String> portalNames = new ArrayList<String>();
    	
    	if(portals!=null && portals.size() > 0)
    	{
    		for(Long portal : portals)
    		{
    			if(SynchroGlobal.getPortalTypes().containsKey(portal.intValue()))
    			{
    				portalNames.add("'"+SynchroGlobal.getPortalTypes().get(portal.intValue())+"'");
    			}
    		}
    	}
    	
    	return portalNames;
    }
    
    public static String getMethodologyNames(String methIds)
    {
    	if(StringUtils.isNotBlank(methIds))
    	{
	    	try
	    	{
	    		List<String> methNames = new ArrayList<String>();
		    	if(methIds.contains(","))
		    	{
		    		String[] mId =  methIds.split(",");
		    		for(int i=0;i<mId.length;i++)
		    		{
		    			//methNames.add(SynchroGlobal.getMethodologies().get(new Integer(mId[i])));
		    			methNames.add(SynchroGlobal.getAllMethodologies().get(new Integer(mId[i])));
		    		}
		    		return StringUtils.join(methNames, ",");
		    	}
		    	else
		    	{
		    		//return SynchroGlobal.getMethodologies().get(new Integer(methIds)).toString();
		    		return SynchroGlobal.getAllMethodologies().get(new Integer(methIds)).toString();
		    	}
	    	}
	    	catch(Exception e)
	    	{
	    		LOGGER.error("Error While get Methodology Name ==>"+ e.getMessage());
	    	}
		    	
    	}
    	return "";
    	

    }
    
    
    public static String getCategoryNames(String categoryIds)
    {
    	if(StringUtils.isNotBlank(categoryIds))
    	{
	    	try
	    	{
	    		List<String> categoryNames = new ArrayList<String>();
		    	if(categoryIds.contains(","))
		    	{
		    		String[] cId =  categoryIds.split(",");
		    		for(int i=0;i<cId.length;i++)
		    		{
		    			categoryNames.add(SynchroGlobal.getProductTypes().get(new Integer(cId[i])));
		       		}
		    		return StringUtils.join(categoryNames, ",");
		    	}
		    	else
		    	{
		    		return SynchroGlobal.getProductTypes().get(new Integer(categoryIds)).toString();
		    	}
	    	}
	    	catch(Exception e)
	    	{
	    		LOGGER.error("Error While get Category Name ==>"+ e.getMessage());
	    	}
		    	
    	}
    	return "";
    	

    }
    /**
     *
     * @param order
     * @return
     */
    public static String getSortType(final Integer order) {
        if(order != null && order.equals(1)) {
            return "desc"; // Descending
        } else {
            return "asc"; // Ascending
        }
    }
    
    public static String getFormattedProjectCode(final String keyword)
    {
    	if(keyword != null)
    	{    
    		//Checks if keyword length is greater than 0
    		if(keyword.length() > 0 && keyword.matches("^[0-9]+$"))
    		{
                if(keyword.matches("^0+[^1-9]$")) {
                    return "0";
                } else {
                    //Remove all 0s from string start, therefore returning formatted string without leading 0s
                    String formattedKeyword = keyword.replaceFirst("^(0*)([1-9]+)(0*)$", "$2$3");
                    return formattedKeyword;
                }

        	}
    	}
    	return keyword;
    }
}