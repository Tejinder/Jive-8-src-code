package com.grail.synchro.action.reports;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.ExchangeRateReport;
import com.grail.synchro.beans.ExchangeRateReportFilter;
import com.grail.synchro.beans.ExchangeRateReportList;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.manager.SynchroReportManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroReportUtil;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

public class ExchangeRateReportAction extends JiveActionSupport {
	 private PermissionManager permissionManager;
	 private SynchroReportManager synchroReportManager;
	 private String token;
	 private String tokenCookie;
	 private int startYear;
	 private int endYear;
	 private final int DEFAULT_YEAR_REPORT = 10;
	 
	public PermissionManager getPermissionManager() {
	        if(permissionManager == null){
	        	permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
	        }
	        return permissionManager;
	    }
	
	public void setSynchroReportManager(SynchroReportManager synchroReportManager) {
			this.synchroReportManager = synchroReportManager;
		}	
    
    @Override
    public String input(){
       //TODO Permission check
    	// This will check whether the user has accepted the Disclaimer or not.
//    	if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//    	{
//    		return "disclaimer-error";
//    	}
    	final User jiveUser = getUser();
        if(jiveUser != null) {
            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
            	return UNAUTHORIZED;
            }
        }
    	if(SynchroPermHelper.isCommunicationAgencyUser() || SynchroPermHelper.isExternalAgencyUser())
    		return UNAUTHORIZED;
        return INPUT;
    }
    
    @Override
    public String execute(){
       //TODO Permission check 
    	final User jiveUser = getUser();
        if(jiveUser != null) {
            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
            	return UNAUTHORIZED;
            }
        }
    	if(SynchroPermHelper.isCommunicationAgencyUser() || SynchroPermHelper.isExternalAgencyUser())
    		return UNAUTHORIZED;
    	
    	ExchangeRateReportFilter exchangeRateReportFilter = new ExchangeRateReportFilter();
    
    	List<ExchangeRateReportList> exchangeRateReportList = new ArrayList<ExchangeRateReportList>();
    	
    	if(getRequest().getMethod().equalsIgnoreCase("POST")) 
    	{
 		    ServletRequestDataBinder binder = new ServletRequestDataBinder(exchangeRateReportFilter);
            binder.bind(getRequest());
            startYear = exchangeRateReportFilter.getStartYear();
            endYear = exchangeRateReportFilter.getEndYear();
            if(startYear > 0 && endYear > 0 && startYear==endYear)
            {
            	ExchangeRateReportList exchangeRateBeanList = new ExchangeRateReportList();
            	List<ExchangeRateReport> exchangeRateBean = new ArrayList<ExchangeRateReport>();
            	List<ExchangeRateReport> exchangeRateReport = synchroReportManager.getExchangeRateReport(startYear);
            	for(Integer key : SynchroGlobal.getCurrencies().keySet())
            	{
            		ExchangeRateReport rate = new ExchangeRateReport();
            		rate.setCurrencyCode(key);            		
            		for(ExchangeRateReport report : exchangeRateReport)
            		{
            			if(report.getCurrencyCode().intValue()==key.intValue())
            			{
            				rate.setExchangeRate(report.getExchangeRate());
            				break;
            			}
            		}
            		exchangeRateBean.add(rate);
            	}
            	exchangeRateBeanList.setExchangeRateReportList(exchangeRateBean);
            	exchangeRateBeanList.setYear(startYear);
            	exchangeRateReportList.add(exchangeRateBeanList);            	
            	
            }
            else if(startYear > 0 && endYear > 0 && startYear<endYear)
            {
            for(int i=startYear; i<=endYear; i++)
            {
            	ExchangeRateReportList exchangeRateBeanList = new ExchangeRateReportList();
            	List<ExchangeRateReport> exchangeRateBean = new ArrayList<ExchangeRateReport>();
            	List<ExchangeRateReport> exchangeRateReport = synchroReportManager.getExchangeRateReport(i);
            	for(Integer key : SynchroGlobal.getCurrencies().keySet())
            	{
            		ExchangeRateReport rate = new ExchangeRateReport();
            		rate.setCurrencyCode(key);            		
            		for(ExchangeRateReport report : exchangeRateReport)
            		{
            			if(report.getCurrencyCode().intValue()==key.intValue())
            			{
            				rate.setExchangeRate(report.getExchangeRate());
            				break;
            			}
            		}
            		exchangeRateBean.add(rate);
            	}
            	exchangeRateBeanList.setExchangeRateReportList(exchangeRateBean);
            	exchangeRateBeanList.setYear(i);
            	exchangeRateReportList.add(exchangeRateBeanList);            	
            	
            }
            	
            }
            else if(startYear > 0 )
            {
            	 int counter = 0;
            	 while(counter<DEFAULT_YEAR_REPORT)
            	 {

                 	ExchangeRateReportList exchangeRateBeanList = new ExchangeRateReportList();
                 	List<ExchangeRateReport> exchangeRateBean = new ArrayList<ExchangeRateReport>();
                 	List<ExchangeRateReport> exchangeRateReport = synchroReportManager.getExchangeRateReport(startYear);
                 	for(Integer key : SynchroGlobal.getCurrencies().keySet())
                 	{
                 		ExchangeRateReport rate = new ExchangeRateReport();
                 		rate.setCurrencyCode(key);            		
                 		for(ExchangeRateReport report : exchangeRateReport)
                 		{
                 			if(report.getCurrencyCode().intValue()==key.intValue())
                 			{
                 				rate.setExchangeRate(report.getExchangeRate());
                 				break;
                 			}
                 		}
                 		exchangeRateBean.add(rate);
                 	}
                 	exchangeRateBeanList.setExchangeRateReportList(exchangeRateBean);
                 	exchangeRateBeanList.setYear(startYear);
                 	exchangeRateReportList.add(exchangeRateBeanList);            	
                 	
                 
                 	counter = counter + 1;
                 	startYear = startYear + 1;
            	 }
            }
            else if(endYear > 0 )
            {
            	 int counter = 0;
            	 endYear = endYear - DEFAULT_YEAR_REPORT;
            	 while(counter<DEFAULT_YEAR_REPORT)
            	 {

                 	ExchangeRateReportList exchangeRateBeanList = new ExchangeRateReportList();
                 	List<ExchangeRateReport> exchangeRateBean = new ArrayList<ExchangeRateReport>();
                 	List<ExchangeRateReport> exchangeRateReport = synchroReportManager.getExchangeRateReport(endYear);
                 	for(Integer key : SynchroGlobal.getCurrencies().keySet())
                 	{
                 		ExchangeRateReport rate = new ExchangeRateReport();
                 		rate.setCurrencyCode(key);            		
                 		for(ExchangeRateReport report : exchangeRateReport)
                 		{
                 			if(report.getCurrencyCode().intValue()==key.intValue())
                 			{
                 				rate.setExchangeRate(report.getExchangeRate());
                 				break;
                 			}
                 		}
                 		exchangeRateBean.add(rate);
                 	}
                 	exchangeRateBeanList.setExchangeRateReportList(exchangeRateBean);
                 	exchangeRateBeanList.setYear(endYear);
                 	exchangeRateReportList.add(exchangeRateBeanList);            	
                 	counter = counter + 1;
                 	endYear = endYear + 1;
            	 }
            }
            else
            {
            	Calendar now = Calendar.getInstance();
            	int currentYear = now.get(Calendar.YEAR);
            	startYear = currentYear - DEFAULT_YEAR_REPORT/2;
            	endYear = currentYear + DEFAULT_YEAR_REPORT/2;
            	
            	for(int i=startYear; i<=endYear; i++)
                {
                	ExchangeRateReportList exchangeRateBeanList = new ExchangeRateReportList();
                	List<ExchangeRateReport> exchangeRateBean = new ArrayList<ExchangeRateReport>();
                	List<ExchangeRateReport> exchangeRateReport = synchroReportManager.getExchangeRateReport(i);
                	for(Integer key : SynchroGlobal.getCurrencies().keySet())
                	{
                		ExchangeRateReport rate = new ExchangeRateReport();
                		rate.setCurrencyCode(key);            		
                		for(ExchangeRateReport report : exchangeRateReport)
                		{
                			if(report.getCurrencyCode().intValue()==key.intValue())
                			{
                				rate.setExchangeRate(report.getExchangeRate());
                				break;
                			}
                		}
                		exchangeRateBean.add(rate);
                	}
                	exchangeRateBeanList.setExchangeRateReportList(exchangeRateBean);
                	exchangeRateBeanList.setYear(i);
                	exchangeRateReportList.add(exchangeRateBeanList);            	
                }
            }
            
            HSSFWorkbook workbook = SynchroReportUtil.generateExchangeRateReport(exchangeRateReportList);
 			try {
 				response.setHeader("Content-Disposition",
 						"attachment;filename=ExchangeRate.xls");
 				response.setContentType("application/vnd.ms-excel");
 				generateTokenCookie();
 				workbook.write(response.getOutputStream()); 				
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
    	generateTokenCookie();
    	}
    	
			
        return SUCCESS;
    }
    private void generateTokenCookie()
    {
    	token = request.getParameter("token");
    	tokenCookie = request.getParameter("tokenCookie");
    	if(token!=null && tokenCookie!=null)
    	{
    		Cookie cookie = new Cookie(tokenCookie, token);
    		cookie.setMaxAge(1000*60*10);    	
    		response.addCookie(cookie);
    	}
    	
    }
}
