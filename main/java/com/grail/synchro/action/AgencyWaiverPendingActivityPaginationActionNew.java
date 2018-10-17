package com.grail.synchro.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroLogUtilsNew;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroReminderUtils;
import com.grail.synchro.util.SynchroUtils;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.RegionUtil;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostDetailsBean;
import com.grail.synchro.beans.ProjectStage;
import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.beans.ProjectWaiverCatalogueBean;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ReportSummaryManagerNew;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.util.DateUtils;
import com.grail.util.URLUtils;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.CommunityManager;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.util.StringUtils;

@Decorate(false)
public class AgencyWaiverPendingActivityPaginationActionNew extends JiveActionSupport {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AgencyWaiverPendingActivityPaginationActionNew.class);
    private Integer page = 1;
    private static Integer LIMIT = 10;
    private Integer results;
    private Integer pages = 0;
    private Integer start;
    private Integer end;
    private List<PIBMethodologyWaiver> pibMethodologyWaiverList = new ArrayList<PIBMethodologyWaiver>();
    ProjectResultFilter projectResultFilter = new ProjectResultFilter();
    private Integer  pageLimit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, LIMIT);
    private String keyword;
    
    private ProjectManagerNew synchroProjectManagerNew;
    private PIBManagerNew pibManagerNew;
    
    private PIBMethodologyWaiver pibKantarMethodologyWaiver;
    private StageManager stageManager;
    private EmailNotificationManager emailNotificationManager;
    
    private String sortField;
    private Integer ascendingOrder;
    
    private Integer plimit;
    
    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename;

    private String downloadStreamType = "application/vnd.ms-excel";
    private ReportSummaryManagerNew reportSummaryManagerNew;
    
    private List<String> selectedFilters = new ArrayList<String>(); 
    
    private String redirectUrl;

    public String getSortField() {
        return sortField;
    }

    public Integer getAscendingOrder() {
        return ascendingOrder;
    }


   

    public void setSynchroProjectManagerNew(ProjectManagerNew synchroProjectManagerNew) {
        this.synchroProjectManagerNew = synchroProjectManagerNew;
    }

    public Integer getPages() {
        return pages;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public void setResults(Integer results) {
        this.results = results;
    }

    public Integer getResults() {
        return results;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String execute()
    {
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }
    	
    	
    	ProjectResultFilter filter = getSearchFilter();
        setPagination(pibManagerNew.getPIBPendingAgencyWaiversTotalCount(filter).intValue());
        updatePage();

      //  synchroProjectManagerNew.updatePendingActivityViews(SynchroReminderUtils.getPendingActivitySearchFilter(), getUser().getID());
        return SUCCESS;

    }
    
    public String downloadReport()
    {
    	downloadFilename = "PendingAgencyWaiver.xls";
    	downloadStreamType = "application/vnd.ms-excel";
    	
    	String downloadReportPage = getRequest().getParameter("downloadReportPage");
		String downloadReportKeyword = getRequest().getParameter("downloadReportKeyword");
		
		page = Integer.parseInt(downloadReportPage);
		keyword = downloadReportKeyword;
    
    	 start = (page-1) * LIMIT;
         end = start + LIMIT;
       
        try
        {
        	processReport();
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
      //Audit Logs
        SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.REPORTS.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
				0, "Pending Agency Waivers - Download Report", "", -1L, getUser().getID());
        
        return DOWNLOAD_REPORT;

    }
    
    public String approveAgencyWaiver()
    {
    	 
    	Long projectID=null;
		final String id = getRequest().getParameter("projectID");
		
	    if(id  != null ) 
	    {
	         try{
	             if(projectID==null)
	                 projectID = Long.parseLong(id);
	         } catch (NumberFormatException nfEx) {
	        	 LOGGER.error("Invalid ProjectID ");
	             throw nfEx;
	         }
	         
	    }
	    
	    approveWaiver(projectID, null);
	    
	    redirectUrl = "agency-waiver-pending-activities.jspa?approve=true";
         
         return "agency-waiver-pending-activities";
    }
    
    
    public String approveMultipleAgencyWaiver()
    {
    	
    	String multipleProjectIds = getRequest().getParameter("multipleProjectIds");
    	if(multipleProjectIds!=null && !multipleProjectIds.equals(""))
    	{
    	//	multipleProjectIds = multipleProjectIds.replaceAll("[", "").replaceAll("]", "");
    	
    		if(multipleProjectIds.contains(","))
    		{
    			String[] splitProjectId = multipleProjectIds.split(",");
    			for(int i=0;i<splitProjectId.length;i++)
    			{
    				Long projectID = new Long(splitProjectId[i]);
    				approveWaiver(projectID, null);
    			}
    		}
    		else
    		{
    			Long projectID = new Long(multipleProjectIds);
				approveWaiver(projectID, null);
    		}
    	
    	}
    	redirectUrl = "agency-waiver-pending-activities.jspa?approve=true";
         return "agency-waiver-pending-activities";
    }
    
    public void approveWaiver(Long projectID, String comment)
    {
    	Project project = this.synchroProjectManagerNew.get(projectID);
    	List<EndMarketInvestmentDetail> endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(projectID);
	    pibKantarMethodologyWaiver = this.pibManagerNew.getPIBKantarMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
	    
	    pibKantarMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
	    
	    if(comment!=null)
	    {
	    	pibKantarMethodologyWaiver.setMethodologyApproverComment(comment);
	    }
	    
    	this.pibManagerNew.approvePIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);

    	 String baseUrl = URLUtils.getRSATokenBaseURL(request);
         String stageUrl = baseUrl+"/new-synchro/pib-details!input.jspa?projectID=" + projectID; 
    	//EmailNotification#4 Recipients for Approval
         if(projectID!=null)
         {
             //String recp = projectOwnerEmail+","+spiContactEmail;
             
             Long waiverRequestor = pibKantarMethodologyWaiver.getCreationBy();
             String waiverRequestorEmail = SynchroUtils.getUserEmail(waiverRequestor);
             
            // EmailMessage email = stageManager.populateNotificationEmail(waiverRequestorEmail, null, null,"pib.approve.kantar.waiver.htmlBody","pib.approve.kantar.waiver.subject");
             
             
             
             String adminEmail = SynchroPermHelper.getSystemAdminEmail();                
             String adminName = SynchroPermHelper.getSystemAdminName();
             
             EmailMessage email = stageManager.populateNotificationEmail(waiverRequestorEmail, null, null,"approve.waiver.new.htmlBody","approve.waiver.new.subject");

             
             //email.getContext().put("projectId", projectID);

             email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
             email.getContext().put("projectName",project.getName());
             email.getContext().put ("stageUrl",stageUrl);
             email.getContext().put ("waiverApprover",getUser().getName());
             email.getContext().put ("waiverType","Agency");
             
             stageManager.sendNotificationNew(adminName, adminEmail, email);

             //Email Notification TimeStamp Storage
             EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
             emailNotBean.setProjectID(projectID);
             emailNotBean.setEndmarketID(endMarketDetails.get(0).getEndMarketID());
             emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
             emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
             emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver Approved");
             emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver Approved");
             emailNotBean.setEmailSender(adminEmail);
             emailNotBean.setEmailRecipients(waiverRequestorEmail);
             emailNotificationManager.saveDetails(emailNotBean);
         }

       //Approve Audit logs
         SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.APPROVE.getId(), 
         										SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.kantar.approve"), project.getName(), 
         												project.getProjectID(), getUser().getID());
    }

    public String rejectMultipleAgencyWaiver()
    {
    	
    	String multipleProjectIds = getRequest().getParameter("multipleRejectProjectIds");
    	if(multipleProjectIds!=null && !multipleProjectIds.equals(""))
    	{
    	//	multipleProjectIds = multipleProjectIds.replaceAll("[", "").replaceAll("]", "");
    	
    		if(multipleProjectIds.contains(","))
    		{
    			String[] splitProjectId = multipleProjectIds.split(",");
    			for(int i=0;i<splitProjectId.length;i++)
    			{
    				Long projectID = new Long(splitProjectId[i]);
    				rejectWaiver(projectID, null);
    			}
    		}
    		else
    		{
    			Long projectID = new Long(multipleProjectIds);
				rejectWaiver(projectID, null);
    		}
    	
    	}
    	redirectUrl = "agency-waiver-pending-activities.jspa?reject=true";
         return "agency-waiver-pending-activities";
    }
    
    public void rejectWaiver(Long projectID, String comment)
    {
    	Project project = this.synchroProjectManagerNew.get(projectID);
    	List<EndMarketInvestmentDetail> endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(projectID);
	    pibKantarMethodologyWaiver = this.pibManagerNew.getPIBKantarMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
	    
	    pibKantarMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
	    
	    if(comment!=null)
	    {
	    	pibKantarMethodologyWaiver.setMethodologyApproverComment(comment);
	    }
    	this.pibManagerNew.rejectPIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);

    	 String baseUrl = URLUtils.getRSATokenBaseURL(request);
         String stageUrl = baseUrl+"/new-synchro/pib-details!input.jspa?projectID=" + projectID; 
    	//EmailNotification#4 Recipients for Approval
         if(projectID!=null)
         {
        	//String recp = projectOwnerEmail+","+spiContactEmail;
        	 Long waiverRequestor = pibKantarMethodologyWaiver.getCreationBy();
             String waiverRequestorEmail = SynchroUtils.getUserEmail(waiverRequestor);
            
            String adminEmail = SynchroPermHelper.getSystemAdminEmail();                
            String adminName = SynchroPermHelper.getSystemAdminName();
            
            
            
           EmailMessage email = stageManager.populateNotificationEmail(waiverRequestorEmail, null, null,"reject.waiver.new.htmlBody","reject.waiver.new.subject");

           //email.getContext().put("projectId", projectID);
           email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
           email.getContext().put("projectName",project.getName());
           email.getContext().put ("stageUrl",stageUrl);
           
           email.getContext().put ("waiverRejector",getUser().getName());
           email.getContext().put ("waiverType","Agency");
           
           stageManager.sendNotificationNew(adminName, adminEmail, email);
            
            
            

            //Email Notification TimeStamp Storage
            EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
            emailNotBean.setProjectID(projectID);
            emailNotBean.setEndmarketID(endMarketDetails.get(0).getEndMarketID());
            emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
            emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
            emailNotBean.setEmailDesc("Notification | Kantar Agency Waiver Rejected");
            emailNotBean.setEmailSubject("Notification | Kantar Agency Waiver Rejected");
            emailNotBean.setEmailSender(adminEmail);
            emailNotBean.setEmailRecipients(waiverRequestorEmail);
            emailNotificationManager.saveDetails(emailNotBean);
         }

       //Approve Audit logs
         SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.REJECT.getId(), 
         										SynchroGlobal.LogProjectStage.PIB.getId(), getText("logger.project.kantar.approve"), project.getName(), 
         												project.getProjectID(), getUser().getID());
    }
    public String updateWaiver()
    {
    	Long projectID=null;
		String id = getRequest().getParameter("updateWaiverProjectID");
		if(id  != null ) 
	    {
	         try{
	             if(projectID==null)
	                 projectID = Long.parseLong(id);
	         } catch (NumberFormatException nfEx) {
	        	 LOGGER.error("Invalid ProjectID ");
	             throw nfEx;
	         }
	         
	    }
		
		String approverCommentParam = "methodologyApproverComment";
		String waiverActionParam = "methodologyWaiverAction";
		
		String approverComment = getRequest().getParameter(approverCommentParam);
		
		String waiverAction =  getRequest().getParameter(waiverActionParam);
		
		redirectUrl = "agency-waiver-pending-activities.jspa";
		
		if(waiverAction!=null && waiverAction.equalsIgnoreCase("RequestMoreInformation"))
		{
			requestMoreInfoWaiver(projectID,approverComment );
			redirectUrl = "agency-waiver-pending-activities.jspa?reqMoreInfo=true";
		}
		
		if(waiverAction!=null && waiverAction.equalsIgnoreCase("Approve"))
		{
			approveWaiver(projectID,approverComment );
			redirectUrl = "agency-waiver-pending-activities.jspa?waiverApp=true";
		}
		
		if(waiverAction!=null && waiverAction.equalsIgnoreCase("Reject"))
		{
			rejectWaiver(projectID,approverComment );
			redirectUrl = "agency-waiver-pending-activities.jspa?waiverRej=true";
		}
		
		
        return "agency-waiver-pending-activities";
    }
    
    public void requestMoreInfoWaiver(Long projectID, String comment)
    {
    	Project project = this.synchroProjectManagerNew.get(projectID);
    	List<EndMarketInvestmentDetail> endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(projectID);
    	pibKantarMethodologyWaiver = this.pibManagerNew.getPIBKantarMethodologyWaiver(projectID, endMarketDetails.get(0).getEndMarketID());
	    
    	pibKantarMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
    	 Long waiverRequestor = pibKantarMethodologyWaiver.getCreationBy();
         String waiverRequestorEmail = SynchroUtils.getUserEmail(waiverRequestor);
         
         String adminEmail = SynchroPermHelper.getSystemAdminEmail();                
         String adminName = SynchroPermHelper.getSystemAdminName();
         String baseUrl = URLUtils.getRSATokenBaseURL(request);
         String stageUrl = baseUrl+"/new-synchro/pib-details!input.jspa?projectID=" + projectID; 
         
         EmailMessage email = stageManager.populateNotificationEmail(waiverRequestorEmail, null, null,"request.more.information.waiver.new.htmlBody","request.more.information.waiver.new.subject");
         //email.getContext().put("projectId", projectID);
         email.getContext().put("projectId", SynchroUtils.generateProjectCode(projectID));
         email.getContext().put("projectName",project.getName());
         email.getContext().put ("stageUrl",stageUrl);
         
        // email.getContext().put ("waiverInitiator",getUser().getName());
         try
         {
         	email.getContext().put ("waiverInitiator",userManager.getUser(waiverRequestor).getName());
         }
         catch(UserNotFoundException e)
         {
         	e.printStackTrace();
         	email.getContext().put ("waiverInitiator","");
         }
         
         email.getContext().put ("waiverType","Agency");
         
         stageManager.sendNotificationNew(adminName, adminEmail, email);

         //Email Notification TimeStamp Storage
         EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
         emailNotBean.setProjectID(projectID);
         emailNotBean.setEndmarketID(endMarketDetails.get(0).getEndMarketID());
         emailNotBean.setStageID(SynchroConstants.PIB_STAGE);
         emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
         emailNotBean.setEmailDesc("Notification | More information required on Kantar Agency Waiver");
         emailNotBean.setEmailSubject("Notification | More information required on Kantar Agency Waiver");
         emailNotBean.setEmailSender(adminEmail);
         emailNotBean.setEmailRecipients(waiverRequestorEmail);
         emailNotificationManager.saveDetails(emailNotBean);
         
         if(comment!=null)
 	     {
 	    	pibKantarMethodologyWaiver.setMethodologyApproverComment(comment);
 	     }

         pibManagerNew.reqForInfoPIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);
         //pibManager.updatePIBStatus(projectID, SynchroGlobal.StageStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal());
         
       //Request More Information Audit logs
         SynchroLogUtilsNew.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.NOTIFICATION.getId(), 
					SynchroGlobal.LogProjectStage.PROJECT_IN_PLANNING.getId(), getText("logger.project.kantar.request.inf"), project.getName(), 
							project.getProjectID(), getUser().getID());
    }
    public void setPagination(final Integer count) {
    	if(plimit!=null && plimit > 0)
    	{
    		LIMIT=plimit;
    	}
    	
    	if(count > LIMIT) {
            double temp = count / (LIMIT * 1.0);
            if(count%LIMIT == 0) {
                pages = (int) temp;
            } else {
                pages = (int) temp + 1;
            }
        } else {
            pages = 1;
        }
    }

    public void updatePage() {
        start = (page-1) * LIMIT;
        end = start + LIMIT;
        pibMethodologyWaiverList = pibManagerNew.getPIBPendingAgencyWaivers(getSearchFilter());
        pibMethodologyWaiverList = toMethodologyWaiverBeans(pibMethodologyWaiverList);
    }

    private List<PIBMethodologyWaiver> toMethodologyWaiverBeans(final List<PIBMethodologyWaiver> methodologyWaivers) {
      
        if(methodologyWaivers != null && !methodologyWaivers.isEmpty()) 
        {
            for(PIBMethodologyWaiver methodologyWaiver : methodologyWaivers) 
            {
            	List<Long> emIds = synchroProjectManagerNew.getEndMarketIDs(methodologyWaiver.getProjectID());
        		if(emIds!=null && emIds.size()>0)
        		{
        			List<String> endMarketNames=new ArrayList<String>();
        			for(Long endMarketId : emIds )
        			{
        				endMarketNames.add(SynchroGlobal.getEndMarkets().get(endMarketId.intValue()));
        			}
        			methodologyWaiver.setEndMarketName(StringUtils.join(endMarketNames, ","));
        			
        		}
        		else
        		{
        			methodologyWaiver.setEndMarketName("");
        		}
        		Project project = synchroProjectManagerNew.get(methodologyWaiver.getProjectID());
        		if(project!=null)
        		{
        			methodologyWaiver.setStageURL(ProjectStage.generateURLNew(project));
        		}
        		// This is done for showing the Waiver Summary Attachment as per http://redmine.nvish.com/redmine/issues/406
        		if(emIds!=null && emIds.size()>0 && reportSummaryManagerNew.getDocumentAttachment(project.getProjectID(), new Long("-1"))!=null)
        		{
        			methodologyWaiver.setAttachmentMap(reportSummaryManagerNew.getDocumentAttachment(project.getProjectID(), new Long("-1")));
        		}
        		else
        		{
        			methodologyWaiver.setAttachmentMap(null);
        		}
        		
        		methodologyWaiver.setProjectDescription(project.getDescriptionText());
        		try
        		{
        			methodologyWaiver.setProjectStage(SynchroGlobal.ProjectStatusNew.getName(project.getStatus()));
        		}
        		catch(Exception e)
        		{
        			methodologyWaiver.setProjectStage("");
        		}
        		methodologyWaiver.setMethodology(SynchroDAOUtil.getMethodologyNames(StringUtils.join(project.getMethodologyDetails(),",")));
        		methodologyWaiver.setBudgetLocation(SynchroUtils.getBudgetLocationName(project.getBudgetLocation()));
        		methodologyWaiver.setProjectManager(project.getProjectManagerName());
        		try
        		{
        			methodologyWaiver.setWaiverInitiator(userManager.getUser(methodologyWaiver.getCreationBy()).getName());
        		}
        		catch(UserNotFoundException e)
        		{
        			methodologyWaiver.setWaiverInitiator("");
        		}
        		 
        		List<ProjectCostDetailsBean> projectCostDetailsList = synchroProjectManagerNew.getProjectCostDetails(project.getProjectID());
        		List<String> agencyNames = new ArrayList<String>();
        		for(ProjectCostDetailsBean pcb:projectCostDetailsList)
            	{
        			if(pcb.getAgencyId()!=null && SynchroGlobal.getResearchAgency().get(pcb.getAgencyId().intValue())!=null)
        			{
        				agencyNames.add(SynchroGlobal.getResearchAgency().get(pcb.getAgencyId().intValue()));
        			}
            	}
        		if(agencyNames!=null && agencyNames.size()>0)
        		{
        			methodologyWaiver.setAgencyNames(StringUtils.join(agencyNames,","));
        		}
        		else
        		{
        			methodologyWaiver.setAgencyNames("");
        		}
        		
            }
        }
      return methodologyWaivers;
    }
    
    private void processReport() throws IOException {
        HSSFWorkbook workbook = null;
        workbook = new HSSFWorkbook();
      
       
        workbook = generateReport(workbook);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        downloadStream = new ByteArrayInputStream(baos.toByteArray());
              
    }
	    private HSSFWorkbook generateReport(HSSFWorkbook workbook) throws UnsupportedEncodingException, IOException{
    
    	projectResultFilter = getSearchFilter();
    	
    	 Calendar calendar = Calendar.getInstance();
         String timeStamp = calendar.get(Calendar.YEAR) +
                 "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                 "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                 "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                 "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                 "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) ;
        
        
        
        StringBuilder generatedBy = new StringBuilder();
        generatedBy.append("Pending Approval Report ").append("\n");
       
        String userName = getUser().getName();
        generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
       
       

        generatedBy.append("\"").append("Filters:").append(Joiner.on(" | ").join(selectedFilters)).append("\"").append("\n");
        
    	projectResultFilter.setLimit(null);
    	projectResultFilter.setStart(null);
    	
    	pibMethodologyWaiverList = pibManagerNew.getPIBPendingAgencyWaivers(projectResultFilter);
    	
        pibMethodologyWaiverList = toMethodologyWaiverBeans(pibMethodologyWaiverList);
         
         Integer startRow = 4;
         HSSFSheet sheet = workbook.createSheet("PendingApprovalReport");
         HSSFRow reportTypeHeader = sheet.createRow(startRow);
         
         HSSFFont sheetHeaderFont = workbook.createFont();
         sheetHeaderFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	  sheetHeaderFont.setFontName("Calibri");
 	      
 	     HSSFFont callibiriFont = workbook.createFont();
         callibiriFont.setFontName("Calibri");
         
         
         HSSFCellStyle sheetHeaderCellStyle = workbook.createCellStyle();
         sheetHeaderCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
         sheetHeaderCellStyle.setFont(sheetHeaderFont);
         sheetHeaderCellStyle.setWrapText(true);
         sheetHeaderCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
         
         
         sheetHeaderCellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
	      sheetHeaderCellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
	      sheetHeaderCellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
	      sheetHeaderCellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
	      
	      sheetHeaderCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	      sheetHeaderCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
	      
	      
	      HSSFCellStyle generatedByCellStyle = workbook.createCellStyle();
	      
	      generatedByCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	      generatedByCellStyle.setFont(sheetHeaderFont);
	      generatedByCellStyle.setWrapText(true);
	      generatedByCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
         
         
         HSSFCell reportTypeHeaderColumn = reportTypeHeader.createCell(0);
         reportTypeHeaderColumn.setCellStyle(generatedByCellStyle);
         reportTypeHeaderColumn.setCellValue(generatedBy.toString());
         
         sheet.addMergedRegion(new CellRangeAddress(0,3,0,14));
         sheet.addMergedRegion(new CellRangeAddress(4,7,0,14));
         
         startRow = startRow + 4;
         
         reportTypeHeader = sheet.createRow(startRow);
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(0);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Project Code");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(1);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Project Name");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(2);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Project Description");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(3);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Project Stage");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(4);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Research End Market(s)");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(5);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Methodology");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(6);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Budget Year");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(7);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Budget Location");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(8);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Agency");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(9);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Rationale For Waiver");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(10);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("SP&I Contact");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(11);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Waiver Initiator");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(12);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Date Of Request");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(13);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Approver");
         
         reportTypeHeaderColumn = reportTypeHeader.createCell(14);
         reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
         reportTypeHeaderColumn.setCellValue("Status");
         
         CellStyle noStyle = workbook.createCellStyle();
         
         noStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
         noStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
         noStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
         noStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
         sheetHeaderFont.setFontName("Calibri");
         noStyle.setFont(callibiriFont);
         noStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
         
         
         if(pibMethodologyWaiverList!=null && pibMethodologyWaiverList.size()>0)
         {
	         for(PIBMethodologyWaiver waiver : pibMethodologyWaiverList)
	         {
	        	 int cellNo = 0;
	        	 HSSFRow dataRow = sheet.createRow(startRow + 1);
	        	 
		         HSSFCell dataCell = dataRow.createCell(cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getProjectID());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getProjectName());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(waiver.getProjectDescription()!=null)
		         {
		        	 dataCell.setCellValue(waiver.getProjectDescription().replaceAll("\\<.*?\\>", "").replaceAll("&nbsp;", " ").replaceAll("&nbsp", " "));
		         }
		         else
		         {
		        	 dataCell.setCellValue(" ");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getProjectStage());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getEndMarketName());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getMethodology());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getBudgetYear());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getBudgetLocation());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getAgencyNames());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(waiver.getMethodologyDeviationRationale()!=null)
		         {
		        	 dataCell.setCellValue(waiver.getMethodologyDeviationRationale().replaceAll("\\<.*?\\>", "").replaceAll("&nbsp;", " ").replaceAll("&nbsp", " "));
		         }
		         else
		         {
		        	 dataCell.setCellValue(" ");
		         }
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getProjectManager());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getWaiverInitiator());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         if(waiver.getCreationDate()!=null)
		         {
		        	 dataCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(new Date(waiver.getCreationDate())));
		         }
		         else
		         {
		        	 dataCell.setCellValue(" ");
		         }
		         		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue(waiver.getApproverName());
		         
		         dataCell = dataRow.createCell(++cellNo);
		         dataCell.setCellStyle(noStyle);
		         dataCell.setCellValue("Pending");
		         
		        
		         
		         startRow++;
		         
	         }
	         
	         	//sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+8,0,14));
	         HSSFRow notesRow  = sheet.createRow(startRow+3);
	         	
	         	HSSFCellStyle notesStyle = workbook.createCellStyle();
				 notesStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
	             notesStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
	             notesStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	             notesStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
	            notesStyle.setWrapText(true);
	            notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		  notesStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
	            notesStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		    	
	            notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		        
	            notesStyle.setAlignment(CellStyle.ALIGN_LEFT);
		    	notesStyle.setWrapText(true);
		    	notesStyle.setFont(callibiriFont);
		    	
	        	CellRangeAddress mergedRegion = new CellRangeAddress(startRow+3,startRow+8,0,14);
	        	
	        	sheet.addMergedRegion(mergedRegion);
	        	
	        	RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	            RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	            RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	            RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, mergedRegion, sheet, workbook);
	            
	            
	         	StringBuilder notes = new StringBuilder();
	         	notes.append("Notes:").append("\n");
	         	notes.append("- The cells with value '–' indiacate that the information is not available ").append("\n");
	         	notes.append("- Cancelled projects are not included in the above report");
	         	HSSFCell notesColumn = notesRow.createCell(0);
	         	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	         	notesColumn.setCellValue(notes.toString());
	         	notesColumn.setCellStyle(notesStyle);
         }
         workbook = SynchroUtils.createExcelImage(workbook, sheet);
         return workbook;
         
         
    }
    
    private ProjectResultFilter getSearchFilter() {
        projectResultFilter = new ProjectResultFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(projectResultFilter);
        binder.bind(getRequest());

        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) {
            
        }

        if(projectResultFilter.getKeyword() == null || projectResultFilter.getKeyword().equals("")) {
            projectResultFilter.setKeyword(keyword);
        }
        if(projectResultFilter.getStart() == null) {
            projectResultFilter.setStart(start);
        }
        if(plimit!=null && plimit > 0)
        {
        	projectResultFilter.setLimit(plimit);
        }
        else
        {
        	 if(projectResultFilter.getLimit() == null) {
                 projectResultFilter.setLimit(LIMIT);
             }
        }
        
        // Cancel Projects should be accessible to Admin users only
        // http://redmine.nvish.com/redmine/issues/465
        /*   if(SynchroPermHelper.isSystemAdmin())
        {
        	 projectResultFilter.setFetchCancelProjects(true);
        }
       */ 
        
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateBegin"))) {
            this.projectResultFilter.setStartDateBegin(DateUtils.parse(getRequest().getParameter("startDateBegin")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateComplete"))) {
            this.projectResultFilter.setStartDateComplete(DateUtils.parse(getRequest().getParameter("startDateComplete")));
        }
        
        if(StringUtils.isNotBlank(getRequest().getParameter("endDateBegin"))) {
            this.projectResultFilter.setEndDateBegin(DateUtils.parse(getRequest().getParameter("endDateBegin")));
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("endDateComplete"))) {
            this.projectResultFilter.setEndDateComplete(DateUtils.parse(getRequest().getParameter("endDateComplete")));
        }
        
        if(StringUtils.isNotBlank(getRequest().getParameter("startDateBegin")) || StringUtils.isNotBlank(getRequest().getParameter("startDateComplete")) ) 
        {
        	if(StringUtils.isNotBlank(getRequest().getParameter("startDateBegin")))
        	{
        		
        		if(StringUtils.isNotBlank(getRequest().getParameter("startDateComplete")))
        		{
        			selectedFilters.add("Date Of Request Between - " + getRequest().getParameter("startDateBegin") + " and "+getRequest().getParameter("startDateComplete"));
        		}
        		else
        		{
        			selectedFilters.add("Date Of Request - " +getRequest().getParameter("startDateBegin"));
        		}
        	}
        	else
        	{
        		selectedFilters.add("Date Of Request - " +getRequest().getParameter("startDateComplete"));
        	}
        }
        if(StringUtils.isNotBlank(getRequest().getParameter("endDateBegin")) || StringUtils.isNotBlank(getRequest().getParameter("endDateComplete")) ) 
        {
        	if(StringUtils.isNotBlank(getRequest().getParameter("endDateBegin")))
        	{
        		
        		if(StringUtils.isNotBlank(getRequest().getParameter("endDateComplete")))
        		{
        			selectedFilters.add("Date Of Approval Between - " + getRequest().getParameter("endDateBegin") + " and "+getRequest().getParameter("endDateComplete"));
        		}
        		else
        		{
        			selectedFilters.add("Date Of Approval - " + getRequest().getParameter("endDateBegin"));
        		}
        	}
        	else
        	{
        		selectedFilters.add("Date Of Approval - " + getRequest().getParameter("endDateComplete"));
        	}
        }
        if(projectResultFilter.getResearchEndMarkets()!=null && projectResultFilter.getResearchEndMarkets().size()>0)
        {
        	selectedFilters.add("Research End-Market(s) - "+ SynchroUtils.getEndMarketNames(projectResultFilter.getResearchEndMarkets()));
        }
        if(projectResultFilter.getMethDetails()!=null && projectResultFilter.getMethDetails().size()>0)
        {
        	selectedFilters.add("Methodology - "+ SynchroUtils.getMethodologyNames(projectResultFilter.getMethDetails()));
        }
        if(projectResultFilter.getBudgetYears()!=null && projectResultFilter.getBudgetYears().size()>0)
        {
        	selectedFilters.add("Budget Year - "+ StringUtils.join(projectResultFilter.getBudgetYears(), ","));
        }
        
        return projectResultFilter;
    }

	public List<PIBMethodologyWaiver> getPibMethodologyWaiverList() {
		return pibMethodologyWaiverList;
	}

	public void setPibMethodologyWaiverList(
			List<PIBMethodologyWaiver> pibMethodologyWaiverList) {
		this.pibMethodologyWaiverList = pibMethodologyWaiverList;
	}

	public PIBManagerNew getPibManagerNew() {
		return pibManagerNew;
	}

	public void setPibManagerNew(PIBManagerNew pibManagerNew) {
		this.pibManagerNew = pibManagerNew;
	}

	public ProjectManagerNew getSynchroProjectManagerNew() {
		return synchroProjectManagerNew;
	}

	public Integer getPlimit() {
		return plimit;
	}

	public void setPlimit(Integer plimit) {
		this.plimit = plimit;
	}

	public PIBMethodologyWaiver getPibKantarMethodologyWaiver() {
		return pibKantarMethodologyWaiver;
	}

	public void setPibKantarMethodologyWaiver(
			PIBMethodologyWaiver pibKantarMethodologyWaiver) {
		this.pibKantarMethodologyWaiver = pibKantarMethodologyWaiver;
	}

	public StageManager getStageManager() {
		return stageManager;
	}

	public void setStageManager(StageManager stageManager) {
		this.stageManager = stageManager;
	}

	public EmailNotificationManager getEmailNotificationManager() {
		return emailNotificationManager;
	}

	public void setEmailNotificationManager(
			EmailNotificationManager emailNotificationManager) {
		this.emailNotificationManager = emailNotificationManager;
	}

	public InputStream getDownloadStream() {
		return downloadStream;
	}

	public void setDownloadStream(InputStream downloadStream) {
		this.downloadStream = downloadStream;
	}

	public String getDownloadFilename() {
		return downloadFilename;
	}

	public void setDownloadFilename(String downloadFilename) {
		this.downloadFilename = downloadFilename;
	}

	public String getDownloadStreamType() {
		return downloadStreamType;
	}

	public void setDownloadStreamType(String downloadStreamType) {
		this.downloadStreamType = downloadStreamType;
	}

	public ReportSummaryManagerNew getReportSummaryManagerNew() {
		return reportSummaryManagerNew;
	}

	public void setReportSummaryManagerNew(
			ReportSummaryManagerNew reportSummaryManagerNew) {
		this.reportSummaryManagerNew = reportSummaryManagerNew;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	


   

}
