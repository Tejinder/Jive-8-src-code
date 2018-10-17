package com.grail.synchro.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.google.common.base.Joiner;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.action.reports.SpendReportsActionNew;
import com.grail.synchro.beans.CurrencyExchangeRate;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectDashboardViewBeanNew;
import com.grail.synchro.beans.QPRProjectCostSnapshot;
import com.grail.synchro.beans.QPRProjectSnapshot;
import com.grail.synchro.beans.QPRSnapshot;
import com.grail.synchro.beans.SpendByReportBean;
import com.grail.synchro.beans.SpendReportExtractFilter;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.QPRSnapshotManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * @author: tejinder
 * @since: 1.0
 * Action class for QPR Snapshot
 */
public class QPRSnapshotAction extends JiveActionSupport  {

    private static final Logger LOG = Logger.getLogger(QPRSnapshotAction.class);
    private QPRSnapshot qprSnapShot;
    //Spring Managers
    
    private QPRSnapshotManager qprSnapshotManager;
    private List<QPRSnapshot> qprSnapShotList;
   
    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename;

    private String downloadStreamType = "application/vnd.ms-excel";
    private String timeStamp;
    
    private ProjectManagerNew synchroProjectManagerNew;
    
    private int columnWidth = 8000;
    
    private int bannerColumnWidth = 12000;
    private List<BigDecimal> totalCosts=new ArrayList<BigDecimal>();
    private List<String> selectedCrossTabSpends = new ArrayList<String>(); 
    
    public String input() {
        
    	// Synchro System Owner should have access to QPR Snapshot. http://redmine.nvish.com/redmine/issues/499
    	if(!SynchroPermHelper.isSystemAdmin() && !SynchroPermHelper.isSynchroSystemOwner())
		{
			return UNAUTHORIZED;
		}
    	qprSnapShotList=qprSnapshotManager.getAllSnapshots();
    	return INPUT;
    	
    }
    
    public String execute(){
    	 qprSnapShot = new QPRSnapshot();
    	
    	 ServletRequestDataBinder binder = new ServletRequestDataBinder(qprSnapShot);
         binder.bind(getRequest());
         
         
         qprSnapShot.setIsFreeze(true);
         qprSnapShot.setFreezeDate(new Date());
         
         qprSnapShot.setCreationBy(getUser().getID());
         qprSnapShot.setCreationDate(System.currentTimeMillis());

         qprSnapShot.setModifiedBy(getUser().getID());
         qprSnapShot.setModifiedDate(System.currentTimeMillis());
         
         
         // This is done for Overwriting the snapshot, as we have to overwrite the snapshot at Global level as well.
         Long snapShotId = qprSnapshotManager.getSnapShotId(qprSnapShot.getSpendFor(), qprSnapShot.getBudgetYear());
         if(snapShotId!=null && snapShotId.intValue() > 0)
         {
        	 qprSnapshotManager.deleteSnapshot(snapShotId);
        	 
        	 // This is done for preserving the ordering of snaphots as per http://redmine.nvish.com/redmine/issues/424
        	 qprSnapShot.setSnapShotID(snapShotId);
         }
         
         qprSnapshotManager.saveSnapshot(qprSnapShot);
         
         return SUCCESS;
    }

    public String openFreezeProjects()
    {
    	String entityType = getRequest().getParameter("entityType");
    	if(StringUtils.isNotBlank(entityType) && entityType.equalsIgnoreCase("Project"))
    	{
    	
	    	String operationType = getRequest().getParameter("operationType");
	    	if(StringUtils.isNotBlank(operationType) && operationType.equalsIgnoreCase("Open"))
	    	{
	    		openProjects();
	    	}
	    	else if (StringUtils.isNotBlank(operationType) && operationType.equalsIgnoreCase("Freeze"))
	    	{
	    		freezeProjects();
	    	}
    	}
    	else if(StringUtils.isNotBlank(entityType) && entityType.equalsIgnoreCase("Budget Location"))
    	{
    		 String budgetLocation = getRequest().getParameter("budgetLocation");
             String snapshotId = getRequest().getParameter("snapshotId");
             
             System.out.print("Budget Locatin ==> "+ budgetLocation);
             
            String operationType = getRequest().getParameter("operationType");
 	    	if(StringUtils.isNotBlank(operationType) && operationType.equalsIgnoreCase("Open"))
 	    	{
 	    		openBudgetLocation();
 	    	}
 	    	else if (StringUtils.isNotBlank(operationType) && operationType.equalsIgnoreCase("Freeze"))
 	    	{
 	    		freezeBudgetLocation();
 	    	}
    	}
    	return SUCCESS;
    }
    
    
    public String openProjects(){
    	 
    	
    	
         String projectIds = getRequest().getParameter("openProjectIds");
         String snapshotId = getRequest().getParameter("snapshotId");
         
         if(StringUtils.isNotBlank(projectIds) && StringUtils.isNotBlank(snapshotId))
         {
        	 if(projectIds.contains(","))
        	 {
        		 String[] splitProjectIds = projectIds.split(",");
        		 for(int i=0;i<splitProjectIds.length;i++)
        		 {
        			 try
        			 {
	        			 QPRProjectSnapshot qprProjectSnapShot = new QPRProjectSnapshot();
	        			 qprProjectSnapShot.setProjectID(Long.valueOf(splitProjectIds[i]));
	        			 qprProjectSnapShot.setSnapShotID(Long.valueOf(snapshotId));
	        			 qprProjectSnapShot.setModifiedBy(getUser().getID());
	        			 qprProjectSnapShot.setModifiedDate(System.currentTimeMillis());
	        			 qprProjectSnapShot.setIsFreeze(false);
	        			 qprSnapshotManager.updateProjectSnapshotFreeze(qprProjectSnapShot);
	        	         
        			 }
        			 catch(Exception e)
        			 {
        				 e.printStackTrace();
        			 }
        			 
        		 }
        	 }
        	 else
        	 {
        		 try
    			 {
        			 QPRProjectSnapshot qprProjectSnapShot = new QPRProjectSnapshot();
        			 qprProjectSnapShot.setProjectID(Long.valueOf(projectIds));
        			 qprProjectSnapShot.setSnapShotID(Long.valueOf(snapshotId));
        			 qprProjectSnapShot.setModifiedBy(getUser().getID());
        			 qprProjectSnapShot.setModifiedDate(System.currentTimeMillis());
        			 qprProjectSnapShot.setIsFreeze(false);
        			 qprSnapshotManager.updateProjectSnapshotFreeze(qprProjectSnapShot);
        	         
    			 }
    			 catch(Exception e)
    			 {
    				 e.printStackTrace();
    			 }
        	 }
         }
         
       
         
         return SUCCESS;
    }

    
    public String openBudgetLocation(){
   	 
    	
    	
    	String budgetLocation = getRequest().getParameter("budgetLocation");
        String snapshotId = getRequest().getParameter("snapshotId");
        
        if(StringUtils.isNotBlank(budgetLocation) && StringUtils.isNotBlank(budgetLocation))
        {
       	 	 try
   			 {
       	 		//This will update the QPR Snapshot at the main QPR Snapshot table level
       	 		 qprSnapshotManager.updateOpenBudgetLocation(Long.valueOf(snapshotId), Long.valueOf(budgetLocation));
       	 		 // This will fetch all the Freezed projects for a particular snapshot and budget location. 
       	 		 List<Long> projectIds = qprSnapshotManager.getAllOpenProjectIds(Long.valueOf(snapshotId), Long.valueOf(budgetLocation));
       			 for(Long projectId:projectIds )
       			 {
       				 QPRProjectSnapshot qprProjectSnapShot = new QPRProjectSnapshot();
       				 qprProjectSnapShot.setProjectID(projectId);
	       			 qprProjectSnapShot.setSnapShotID(Long.valueOf(snapshotId));
	       			 qprProjectSnapShot.setModifiedBy(getUser().getID());
	       			 qprProjectSnapShot.setModifiedDate(System.currentTimeMillis());
	       			 qprProjectSnapShot.setIsFreeze(false);
	       			 qprSnapshotManager.updateProjectSnapshotFreeze(qprProjectSnapShot);
       			 }
       	         
   			 }
   			 catch(Exception e)
   			 {
   				 e.printStackTrace();
   			 }
        }
        
        return SUCCESS;
   }
    public String freezeProjects(){
   	 
    	
    	
        
    	String projectIds = getRequest().getParameter("freezeProjectIds");
        String snapshotId = getRequest().getParameter("snapshotId");
        
        if(StringUtils.isNotBlank(projectIds) && StringUtils.isNotBlank(snapshotId))
        {
        	QPRSnapshot qprSnapshotDB =  qprSnapshotManager.getSnapshot(Long.valueOf(snapshotId));
        	
        if(projectIds.contains(","))
       	 {
       		
       		
       		
        	String[] splitProjectIds = projectIds.split(",");
       		 for(int i=0;i<splitProjectIds.length;i++)
       		 {
       			 try
       			 {
       				 QPRProjectSnapshot qprProjectSnapShotDB = qprSnapshotManager.getProjectSnapshot(Long.valueOf(snapshotId),Long.valueOf(splitProjectIds[i]));
       				 // This logic is added so that if the budget location is opened for a project, then that project should not get frozen.
       				 if(qprSnapshotDB.getOpenBudgetLocations()!=null && qprSnapshotDB.getOpenBudgetLocations().contains(Long.valueOf(qprProjectSnapShotDB.getBudgetLocation()+"")))
       				 {
       					 continue;
       				 }
       					 
       				 //http://redmine.nvish.com/redmine/issues/398
       				 
       				 QPRProjectSnapshot qprProjectSnapShot = new QPRProjectSnapshot();
        			 qprProjectSnapShot.setProjectID(Long.valueOf(splitProjectIds[i]));
        			 qprProjectSnapShot.setSnapShotID(Long.valueOf(snapshotId));
        			 qprProjectSnapShot.setModifiedBy(getUser().getID());
        			 qprProjectSnapShot.setModifiedDate(System.currentTimeMillis());
        			 qprProjectSnapShot.setIsFreeze(true);
        			 
        			 Project project = synchroProjectManagerNew.get(Long.valueOf(splitProjectIds[i]));
        			 qprProjectSnapShot.setProjectName(project.getName());
        	    	 qprProjectSnapShot.setBudgetLocation(project.getBudgetLocation());
        	    	 qprProjectSnapShot.setMethodologyDetails(project.getMethodologyDetails());
        	    	 qprProjectSnapShot.setBrandSpecificStudy(project.getBrandSpecificStudy());
        			 qprProjectSnapShot.setBrandSpecificStudyType(project.getBrandSpecificStudyType());
        			 qprProjectSnapShot.setBrand(project.getBrand());
        				
        	    	 qprProjectSnapShot.setTotalCost(project.getTotalCost());
        	    	 qprProjectSnapShot.setTotalCostCurrency(project.getTotalCostCurrency());
        	    	 
        	    	 qprProjectSnapShot.setCategoryType(project.getCategoryType());
        			 qprSnapshotManager.updateProjectSnapshot(qprProjectSnapShot);
	         	         
       			 }
       			 catch(Exception e)
       			 {
       				 e.printStackTrace();
       			 }
       			 
       		 }
       	 }
       	 else
       	 {
       		 try
   			 {
       			/* QPRProjectSnapshot qprProjectSnapShot = new QPRProjectSnapshot();
       			 qprProjectSnapShot.setProjectID(Long.valueOf(projectIds));
       			 qprProjectSnapShot.setSnapShotID(Long.valueOf(snapshotId));
       			 qprProjectSnapShot.setModifiedBy(getUser().getID());
       			 qprProjectSnapShot.setModifiedDate(System.currentTimeMillis());
       			 qprProjectSnapShot.setIsFreeze(true);
       			 qprSnapshotManager.updateProjectSnapshotFreeze(qprProjectSnapShot);
       			 */
       			 
       			 //http://redmine.nvish.com/redmine/issues/398
   				 
       			 QPRProjectSnapshot qprProjectSnapShotDB = qprSnapshotManager.getProjectSnapshot(Long.valueOf(snapshotId),Long.valueOf(projectIds));
       		// This logic is added so that if the budget location is opened for a project, then that project should not get frozen.
       			 if(qprSnapshotDB.getOpenBudgetLocations()!=null && qprSnapshotDB.getOpenBudgetLocations().contains(Long.valueOf(qprProjectSnapShotDB.getBudgetLocation()+"")))
   				 {
   					
   				 }
       			 else
       			 {
   				 
	   				 QPRProjectSnapshot qprProjectSnapShot = new QPRProjectSnapshot();
	    			 qprProjectSnapShot.setProjectID(Long.valueOf(projectIds));
	    			 qprProjectSnapShot.setSnapShotID(Long.valueOf(snapshotId));
	    			 qprProjectSnapShot.setModifiedBy(getUser().getID());
	    			 qprProjectSnapShot.setModifiedDate(System.currentTimeMillis());
	    			 qprProjectSnapShot.setIsFreeze(true);
	    			 
	    			 Project project = synchroProjectManagerNew.get(Long.valueOf(projectIds));
	    			 qprProjectSnapShot.setProjectName(project.getName());
	    	    	 qprProjectSnapShot.setBudgetLocation(project.getBudgetLocation());
	    	    	 qprProjectSnapShot.setMethodologyDetails(project.getMethodologyDetails());
	    	    	 qprProjectSnapShot.setBrandSpecificStudy(project.getBrandSpecificStudy());
	    			 qprProjectSnapShot.setBrandSpecificStudyType(project.getBrandSpecificStudyType());
	    			 qprProjectSnapShot.setBrand(project.getBrand());
	    				
	    	    	 qprProjectSnapShot.setTotalCost(project.getTotalCost());
	    	    	 qprProjectSnapShot.setTotalCostCurrency(project.getTotalCostCurrency());
	    	    	 qprProjectSnapShot.setCategoryType(project.getCategoryType());
	    	    	 
	    			 qprSnapshotManager.updateProjectSnapshot(qprProjectSnapShot);
       			 }
       	         
   			 }
   			 catch(Exception e)
   			 {
   				 e.printStackTrace();
   			 }
       	 }
        }
        
      
        
        return SUCCESS;
   }
    
    public String freezeBudgetLocation(){
   	 
    	
    	
        String budgetLocations = getRequest().getParameter("freezeBudgetLocations");
        String snapshotId = getRequest().getParameter("snapshotId");
       
        Long budgetYear = qprSnapshotManager.getBudgetYear(Long.valueOf(snapshotId));
        
        if(StringUtils.isNotBlank(budgetLocations) && StringUtils.isNotBlank(snapshotId))
        {
       	 if(budgetLocations.contains(","))
       	 {
       		 String[] splitBudgetLocations = budgetLocations.split(",");
       		 for(int i=0;i<splitBudgetLocations.length;i++)
       		 {
       			 try
       			 {
        			 //http://redmine.nvish.com/redmine/issues/398
       				 
       				 //This will update the QPR Snapshot at the main QPR Snapshot table level
           	 		 qprSnapshotManager.updateFreezeBudgetLocation(Long.valueOf(snapshotId));
           	 		 // This will fetch all the Freezed projects for a particular snapshot and budget location. 
           	 		 List<Long> projectIds = qprSnapshotManager.getAllOpenProjectIds(Long.valueOf(snapshotId), Long.valueOf(splitBudgetLocations[i]));
           	 		 
           	 		 List<Long> newCreatedProjectIds = synchroProjectManagerNew.getAllProjects(budgetYear, Long.valueOf(splitBudgetLocations[i]));
           	 		 
           	 		/* Set<Long> finalProjects = Collections.emptySet();
           	 		 
           	 		 finalProjects.addAll(projectIds);
           	 		 finalProjects.addAll(newCreatedProjectIds);
           	 		 */
           	 		 
           	 		Set<Long> finalProjects = new HashSet<Long>(projectIds);
           	 		finalProjects.addAll(newCreatedProjectIds);
           	 		
           	 		 for(Long projectId: finalProjects)
           	 		 {
       				 
	       				 QPRProjectSnapshot qprProjectSnapShot = new QPRProjectSnapshot();
	        			 qprProjectSnapShot.setProjectID(projectId);
	        			 qprProjectSnapShot.setSnapShotID(Long.valueOf(snapshotId));
	        			 qprProjectSnapShot.setModifiedBy(getUser().getID());
	        			 qprProjectSnapShot.setModifiedDate(System.currentTimeMillis());
	        			 qprProjectSnapShot.setIsFreeze(true);
	        			 
	        			 Project project = synchroProjectManagerNew.get(projectId);
	        			 qprProjectSnapShot.setProjectName(project.getName());
	        	    	 qprProjectSnapShot.setBudgetLocation(project.getBudgetLocation());
	        	    	 qprProjectSnapShot.setMethodologyDetails(project.getMethodologyDetails());
	        	    	 qprProjectSnapShot.setBrandSpecificStudy(project.getBrandSpecificStudy());
	        			 qprProjectSnapShot.setBrandSpecificStudyType(project.getBrandSpecificStudyType());
	        			 qprProjectSnapShot.setBrand(project.getBrand());
	        				
	        	    	 qprProjectSnapShot.setTotalCost(project.getTotalCost());
	        	    	 qprProjectSnapShot.setTotalCostCurrency(project.getTotalCostCurrency());
	        	    	 
	        	    	 qprProjectSnapShot.setCategoryType(project.getCategoryType());
	        	    	 
	        	    	 
	        	    	// Setting the Meta Data Fields
	        	    		if(project.getBudgetLocation()!=null && SynchroUtils.getRegionName(project.getBudgetLocation())!=null)
	        	    		{
	        	    			qprProjectSnapShot.setRegion(SynchroUtils.getRegionName(project.getBudgetLocation()));
	        	    		}
	        	    	
	        	    		if(project.getBudgetLocation()!=null && SynchroUtils.getAreaName(project.getBudgetLocation())!=null)
	        	    		{
	        	    			qprProjectSnapShot.setArea(SynchroUtils.getAreaName(project.getBudgetLocation()));
	        	    		}
	        	    		
	        	    		if(project.getBudgetLocation()!=null && SynchroUtils.getT20_T40Name(project.getBudgetLocation())!=null)
	        	    		{
	        	    			qprProjectSnapShot.setT20_t40(SynchroUtils.getT20_T40Name(project.getBudgetLocation()));
	        	    		}
	        	    		
	        	    		String methGroup = SynchroGlobal.getMethodologyGroupName(project.getMethodologyDetails().get(0));
	        	    		if(methGroup!=null)
	        	    		{
	        	    			qprProjectSnapShot.setMethGroup(methGroup);
	        	    		}
	        	    		
	        	    		// Dunhill, Kent, Rothmans, Vogue and Lucky Strike are GDB Brands, all other are non GDB Brands
	        	    		
	        	    		if(project.getBrandSpecificStudy().intValue()==1)
	        	    		{
	        		    		String brandType = SynchroUtils.getBrandBrandTypeFields().get(project.getBrand().intValue());
	        		    		if(brandType!=null)
	        		    		{
	        		    			qprProjectSnapShot.setBrandType(brandType);
	        		    		}
	        		    		else
	        		    		{
	        		    			qprProjectSnapShot.setBrandType("Non-GDB");
	        		    		}
	        	    		}
	        	    		else
	        	    		{
	        	    			qprProjectSnapShot.setBrandType("Non-GDB");
	        	    		}
	        	    	 
	        	    	 
	        	    	 
	        	    	 if(projectIds.contains(projectId))
		    			 {
		    	    	   	 qprSnapshotManager.updateProjectSnapshot(qprProjectSnapShot);
		    			 }
		    			 else
		    			 {
		    				 qprSnapshotManager.saveProjectSnapshot(qprProjectSnapShot);
		    			 }
           	 		 }
	         	         
       			 }
       			 catch(Exception e)
       			 {
       				 e.printStackTrace();
       			 }
       			 
       		 }
       	 }
       	 else
       	 {
       		 try
   			 {
       			
   				 
   				 //This will update the QPR Snapshot at the main QPR Snapshot table level
       	 		// qprSnapshotManager.updateFreezeBudgetLocation(Long.valueOf(snapshotId));
       	 		 
       	 		qprSnapshotManager.updateFreezeBudgetLocation(Long.valueOf(snapshotId), budgetLocations);
       	 		 // This will fetch all the Freezed projects for a particular snapshot and budget location. 
       	 		 List<Long> projectIds = qprSnapshotManager.getAllOpenProjectIds(Long.valueOf(snapshotId), Long.valueOf(budgetLocations));
       	 		 
       	 		 List<Long> newCreatedProjectIds = synchroProjectManagerNew.getAllProjects(budgetYear, Long.valueOf(budgetLocations));
   	 		 
       	 		// Set<Long> finalProjects = Collections.emptySet();
   	 		 
       	 		Set<Long> finalProjects = new HashSet<Long>(projectIds);
       	 		finalProjects.addAll(newCreatedProjectIds);
       	 	
       	 		// finalProjects.addAll(projectIds);
       	 		 //finalProjects.addAll(newCreatedProjectIds);
   				 
       	 		for(Long projectId: finalProjects)
      	 		{
	   				 QPRProjectSnapshot qprProjectSnapShot = new QPRProjectSnapshot();
	    			 qprProjectSnapShot.setProjectID(projectId);
	    			 qprProjectSnapShot.setSnapShotID(Long.valueOf(snapshotId));
	    			 qprProjectSnapShot.setModifiedBy(getUser().getID());
	    			 qprProjectSnapShot.setModifiedDate(System.currentTimeMillis());
	    			 qprProjectSnapShot.setIsFreeze(true);
	    			 
	    			 Project project = synchroProjectManagerNew.get(projectId);
	    			 qprProjectSnapShot.setProjectName(project.getName());
	    	    	 qprProjectSnapShot.setBudgetLocation(project.getBudgetLocation());
	    	    	 qprProjectSnapShot.setMethodologyDetails(project.getMethodologyDetails());
	    	    	 qprProjectSnapShot.setBrandSpecificStudy(project.getBrandSpecificStudy());
	    			 qprProjectSnapShot.setBrandSpecificStudyType(project.getBrandSpecificStudyType());
	    			 qprProjectSnapShot.setBrand(project.getBrand());
	    				
	    	    	 qprProjectSnapShot.setTotalCost(project.getTotalCost());
	    	    	 qprProjectSnapShot.setTotalCostCurrency(project.getTotalCostCurrency());
	    	    	 qprProjectSnapShot.setCategoryType(project.getCategoryType());
	    	    	 
	    	    	// Setting the Meta Data Fields
	    	    		if(project.getBudgetLocation()!=null && SynchroUtils.getRegionName(project.getBudgetLocation())!=null)
	    	    		{
	    	    			qprProjectSnapShot.setRegion(SynchroUtils.getRegionName(project.getBudgetLocation()));
	    	    		}
	    	    	
	    	    		if(project.getBudgetLocation()!=null && SynchroUtils.getAreaName(project.getBudgetLocation())!=null)
	    	    		{
	    	    			qprProjectSnapShot.setArea(SynchroUtils.getAreaName(project.getBudgetLocation()));
	    	    		}
	    	    		
	    	    		if(project.getBudgetLocation()!=null && SynchroUtils.getT20_T40Name(project.getBudgetLocation())!=null)
	    	    		{
	    	    			qprProjectSnapShot.setT20_t40(SynchroUtils.getT20_T40Name(project.getBudgetLocation()));
	    	    		}
	    	    		
	    	    		String methGroup = SynchroGlobal.getMethodologyGroupName(project.getMethodologyDetails().get(0));
	    	    		if(methGroup!=null)
	    	    		{
	    	    			qprProjectSnapShot.setMethGroup(methGroup);
	    	    		}
	    	    		
	    	    		// Dunhill, Kent, Rothmans, Vogue and Lucky Strike are GDB Brands, all other are non GDB Brands
	    	    		
	    	    		if(project.getBrandSpecificStudy().intValue()==1)
	    	    		{
	    		    		String brandType = SynchroUtils.getBrandBrandTypeFields().get(project.getBrand().intValue());
	    		    		if(brandType!=null)
	    		    		{
	    		    			qprProjectSnapShot.setBrandType(brandType);
	    		    		}
	    		    		else
	    		    		{
	    		    			qprProjectSnapShot.setBrandType("Non-GDB");
	    		    		}
	    	    		}
	    	    		else
	    	    		{
	    	    			qprProjectSnapShot.setBrandType("Non-GDB");
	    	    		}
	    	    	 
	    			 if(projectIds.contains(projectId))
	    			 {
	    	    	   	 qprSnapshotManager.updateProjectSnapshot(qprProjectSnapShot);
	    			 }
	    			 else
	    			 {
	    				 qprProjectSnapShot.setCreationBy(getUser().getID());
		    			 qprProjectSnapShot.setCreationDate(System.currentTimeMillis());
	    				 qprSnapshotManager.saveProjectSnapshot(qprProjectSnapShot);
	    			 }
      	 		}
   			 }
   			 catch(Exception e)
   			 {
   				 e.printStackTrace();
   			 }
       	 }
        }
        
      
        
        return SUCCESS;
   }
    
    public String downloadSnapshot() throws IOException
    {
    	
    	String downloadSnapshotId = getRequest().getParameter("downloadSnapshotId");
    	HSSFWorkbook workbook = null;
    	workbook = new HSSFWorkbook();
    	 
        downloadStreamType = "application/vnd.ms-excel";
         

    
    	 
    	 
    	workbook = generateQPRReport(workbook, downloadSnapshotId);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        downloadStream = new ByteArrayInputStream(baos.toByteArray());
        return DOWNLOAD_REPORT;
    	
    	
        
    }
    
    
    private HSSFWorkbook generateQPRReport(HSSFWorkbook workbook , String downloadSnapshotId) throws UnsupportedEncodingException, IOException
    {
   	    
   	 	Integer startRow = 0;
	      HSSFSheet sheet = workbook.createSheet("QPRSnapshot");
	      HSSFRow reportTypeHeader = sheet.createRow(startRow);
	      
	      HSSFFont sheetHeaderFont = workbook.createFont();
	      sheetHeaderFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	      
	      HSSFCellStyle sheetHeaderCellStyle = workbook.createCellStyle();
	      sheetHeaderCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	      sheetHeaderCellStyle.setFont(sheetHeaderFont);
	      sheetHeaderCellStyle.setWrapText(true);
	      sheetHeaderCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
	      sheetHeaderCellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	      sheetHeaderCellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
	      sheetHeaderCellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
	      sheetHeaderCellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
	      
	      
	      HSSFCell reportTypeHeaderColumn = reportTypeHeader.createCell(0);
	      reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	  
	      reportTypeHeader = sheet.createRow(startRow);
	      
	      int columnNo = 0;
	      reportTypeHeaderColumn = reportTypeHeader.createCell(columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Project Code");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Project Name");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Budget Location");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Region");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Area");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("T20/T40");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Methodology");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Methodology Group");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Branded/NonBranded");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Brand Type");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Category Type");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Total Cost (GBP)");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Agency");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Cost Component");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Cost Currency");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Estimated Cost");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(++columnNo);
	        reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	        reportTypeHeaderColumn.setCellValue("Agency Type");
	        
	        CellStyle noStyle = workbook.createCellStyle();
	         
	         noStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
	         noStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
	         noStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
	         noStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
	         
	        
	        if(StringUtils.isNotBlank(downloadSnapshotId))
	        {
	        	Long snapShotID = Long.parseLong(downloadSnapshotId);
	        	QPRSnapshot qprSnapShot = qprSnapshotManager.getSnapshot(snapShotID);  
	        	 //http://redmine.nvish.com/redmine/issues/379
		        downloadFilename = SynchroGlobal.getSpendForOptions().get(qprSnapShot.getSpendFor())+"_"+qprSnapShot.getBudgetYear();
				if(qprSnapShot.getFreezeDate()!=null)
		        {
					downloadFilename= downloadFilename+"_"+(new SimpleDateFormat("dd-MM-yyyy").format(qprSnapShot.getFreezeDate()));
		        }
		        
				downloadFilename=downloadFilename+".xls";
				List<QPRProjectSnapshot> qprProjectSnapShotList =  qprSnapshotManager.getProjectSnapshot(snapShotID);
		    	
		    	
				
		    	for(QPRProjectSnapshot qprProjectSnapShot : qprProjectSnapShotList)
		    	{

		    		List<QPRProjectCostSnapshot> qprProjectCostSnapshotList = qprSnapshotManager.getProjectCostSnapshot(snapShotID,qprProjectSnapShot.getProjectID());
		    		if(qprProjectCostSnapshotList!=null && qprProjectCostSnapshotList.size()>0)
		    		{
			    		for(QPRProjectCostSnapshot qprProjectCostSnapshot : qprProjectCostSnapshotList)
			    		{
			    			int cellNo = 0;
				        	 HSSFRow dataRow = sheet.createRow(++startRow);
					         HSSFCell dataCell = dataRow.createCell(cellNo);
					         dataCell.setCellValue(qprProjectSnapShot.getProjectID());
					    	 dataCell.setCellStyle(noStyle);
			    			
					    	 dataCell = dataRow.createCell(++cellNo);
					         dataCell.setCellValue(qprProjectSnapShot.getProjectName());
					    	 dataCell.setCellStyle(noStyle);
					    	 
					    	 dataCell = dataRow.createCell(++cellNo);
					         //dataCell.setCellValue(SynchroUtils.getBudgetLocationName(qprProjectSnapShot.getBudgetLocation()));
					    	 dataCell.setCellValue(SynchroUtils.getAllBudgetLocationName(qprProjectSnapShot.getBudgetLocation()));
					    	 dataCell.setCellStyle(noStyle);
			    			
					    	 dataCell = dataRow.createCell(++cellNo);
					         dataCell.setCellValue(qprProjectSnapShot.getRegion());
					    	 dataCell.setCellStyle(noStyle);
			    			
					    	 dataCell = dataRow.createCell(++cellNo);
					         dataCell.setCellValue(qprProjectSnapShot.getArea());
					    	 dataCell.setCellStyle(noStyle);
			    			
					    	 dataCell = dataRow.createCell(++cellNo);
					         dataCell.setCellValue(qprProjectSnapShot.getT20_t40());
					    	 dataCell.setCellStyle(noStyle);
			    			
			    		        
			    			if(qprProjectSnapShot.getMethodologyDetails()!=null && qprProjectSnapShot.getMethodologyDetails().size()>0)
			    			{
			    				
			    				dataCell = dataRow.createCell(++cellNo);
						         dataCell.setCellValue(SynchroDAOUtil.getMethodologyNames(Joiner.on(",").join(qprProjectSnapShot.getMethodologyDetails())));
						    	 dataCell.setCellStyle(noStyle);
			    			}
			    			else
			    			{
			    				
			    				dataCell = dataRow.createCell(++cellNo);
						         dataCell.setCellValue("-");
						    	 dataCell.setCellStyle(noStyle);
			    			}
			    			
			    			 dataCell = dataRow.createCell(++cellNo);
					         dataCell.setCellValue(qprProjectSnapShot.getMethGroup());
					    	 dataCell.setCellStyle(noStyle);
			    			
			    			
			    			if(qprProjectSnapShot.getBrandSpecificStudy()!=null && qprProjectSnapShot.getBrandSpecificStudy().intValue()==2)
			    			{
			    				if(qprProjectSnapShot.getBrandSpecificStudyType()!=null && qprProjectSnapShot.getBrandSpecificStudyType().intValue()==1)
			    				{
			    					dataCell = dataRow.createCell(++cellNo);
							        dataCell.setCellValue("Multi-Brand Study");
							    	dataCell.setCellStyle(noStyle);
			    				}
			    				else if(qprProjectSnapShot.getBrandSpecificStudyType()!=null && qprProjectSnapShot.getBrandSpecificStudyType().intValue()==2)
			    				{
			    				
			    					dataCell = dataRow.createCell(++cellNo);
							        dataCell.setCellValue("Non-brand related");
							        dataCell.setCellStyle(noStyle);
			    				}
			    				else
			    				{
			    					dataCell = dataRow.createCell(++cellNo);
							        dataCell.setCellValue("-");
							        dataCell.setCellStyle(noStyle);
			    				}
			    			}
			    			else
			    			{
			    				if(qprProjectSnapShot.getBrand()!=null)
			    				{
			    					if(SynchroGlobal.getBrands().get(qprProjectSnapShot.getBrand().intValue())!=null)
			    					{
			    						
			    						dataCell = dataRow.createCell(++cellNo);
								        dataCell.setCellValue(SynchroGlobal.getBrands().get(qprProjectSnapShot.getBrand().intValue()));
								        dataCell.setCellStyle(noStyle);
			    					}
			    					else
			    					{
			    						dataCell = dataRow.createCell(++cellNo);
								        dataCell.setCellValue("-");
								        dataCell.setCellStyle(noStyle);
			    					}
			    				}
			    				else
			    				{
			    					dataCell = dataRow.createCell(++cellNo);
							        dataCell.setCellValue("-");
							        dataCell.setCellStyle(noStyle);
			    				}
			    			}
			    			
			    			
			    			dataCell = dataRow.createCell(++cellNo);
					        dataCell.setCellValue(qprProjectSnapShot.getBrandType());
					        dataCell.setCellStyle(noStyle);
			    			//body.append(qprProjectSnapShot.getTotalCost()).append(",");
			    			
			    			if(qprProjectSnapShot.getCategoryType()!=null && qprProjectSnapShot.getCategoryType().size()>0)
			    			{
			    				dataCell = dataRow.createCell(++cellNo);
						        dataCell.setCellValue(SynchroDAOUtil.getCategoryNames(Joiner.on(",").join(qprProjectSnapShot.getCategoryType())));
						        dataCell.setCellStyle(noStyle);
			    			}
			    			else
			    			{
			    				dataCell = dataRow.createCell(++cellNo);
						        dataCell.setCellValue("-");
						        dataCell.setCellStyle(noStyle);
			    			}
			    			
			    			if(qprProjectCostSnapshot.getEstimatedCost()!=null && qprProjectCostSnapshot.getCostCurrency()!=null)
			    			{
			    						    				 
			    				// This is commented as for QPR snapshot download report we need to display the estimated cost for each agency row in GBP instead of Total cost  
			    			
			    				  
			    				  String totalCostString = "";
			    				  BigDecimal totalCostBD = null;
			    				  if(qprProjectCostSnapshot!=null && qprProjectCostSnapshot.getCostCurrency()!=null && (qprProjectCostSnapshot.getCostCurrency().intValue()==JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
					        	   {
					    				//dataCell.setCellValue(projectCostDetailsList.get(0).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
			    					  totalCostString = qprProjectCostSnapshot.getEstimatedCost().toPlainString();
			    					  totalCostBD = qprProjectCostSnapshot.getEstimatedCost();
					        	   }
					        	   else if(qprProjectCostSnapshot!=null && qprProjectCostSnapshot.getCostCurrency()!=null && (qprProjectCostSnapshot.getCostCurrency().intValue()!=JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
					        	   {
					        			BigDecimal gbpEstimatedCost = BigDecimal.valueOf((SynchroUtils.getCurrencyExchangeRate(qprProjectCostSnapshot.getCostCurrency())) * (Double.valueOf(qprProjectCostSnapshot.getEstimatedCost().doubleValue())));
					        			//dataCell.setCellValue(gbpEstimatedCost.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
					        			totalCostString = gbpEstimatedCost.toPlainString();
					        			totalCostBD = gbpEstimatedCost;
					        	   }
					        	   
			    				  if(totalCostString.contains("."))
			    				  {
			    					  String splitCost = totalCostString.split("\\.")[0];
			    					  if(splitCost.length()>14)
			    					  {
			    						 // body.append("'"+totalCostString).append(",");
			    						  dataCell = dataRow.createCell(++cellNo);
									        dataCell.setCellValue("'"+totalCostString);
									        dataCell.setCellStyle(noStyle);
			    					  }
			    					  else
			    					  {
			    						  
			    						  dataCell = dataRow.createCell(++cellNo);
									      dataCell.setCellValue(totalCostBD.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
									      dataCell.setCellStyle(noStyle);
			    					  }
			    				  }
			    				  else
			    				  {
			    					  if(totalCostString.length()>14)
			    					  {
			    						  dataCell = dataRow.createCell(++cellNo);
									      dataCell.setCellValue("'"+totalCostString);
									      dataCell.setCellStyle(noStyle);
			    					  }
			    					  else
			    					  {
			    						  dataCell = dataRow.createCell(++cellNo);
									      dataCell.setCellValue(totalCostBD.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
									      dataCell.setCellStyle(noStyle);
			    					  }
			    				  }
					        	  
			    				  
			    			}
			    			else
			    			{
			    				dataCell = dataRow.createCell(++cellNo);
							    dataCell.setCellValue("-");
							    dataCell.setCellStyle(noStyle);
			    			}
			    			
			    			if(qprProjectCostSnapshot.getAgencyId()!=null && SynchroGlobal.getAllResearchAgency().get(qprProjectCostSnapshot.getAgencyId().intValue())!=null)
			    			{
			    				dataCell = dataRow.createCell(++cellNo);
							    dataCell.setCellValue(SynchroGlobal.getAllResearchAgency().get(qprProjectCostSnapshot.getAgencyId().intValue()));
							    dataCell.setCellStyle(noStyle);
			    			}
			    			else
			    			{
			    				dataCell = dataRow.createCell(++cellNo);
							    dataCell.setCellValue("-");
							    dataCell.setCellStyle(noStyle);
			    			}
			    			if(qprProjectCostSnapshot.getCostComponent().intValue()==1)
			    			{
			    				dataCell = dataRow.createCell(++cellNo);
							    dataCell.setCellValue("Coordination");
							    dataCell.setCellStyle(noStyle);
			    			}
			    			else if(qprProjectCostSnapshot.getCostComponent().intValue()==2)
			    			{
			    				dataCell = dataRow.createCell(++cellNo);
							    dataCell.setCellValue("FieldWork");
							    dataCell.setCellStyle(noStyle);
			    			}
			    			else
			    			{
			    				dataCell = dataRow.createCell(++cellNo);
							    dataCell.setCellValue("Unclassified");
							    dataCell.setCellStyle(noStyle);
			    			}
			    			if(qprProjectCostSnapshot.getCostCurrency()!=null && SynchroGlobal.getCurrencies().get(qprProjectCostSnapshot.getCostCurrency())!=null)
			    			{
			    				
			    				dataCell = dataRow.createCell(++cellNo);
							    dataCell.setCellValue(SynchroGlobal.getCurrencies().get(qprProjectCostSnapshot.getCostCurrency()));
							    dataCell.setCellStyle(noStyle);
			    			}
			    			else
			    			{
			    				dataCell = dataRow.createCell(++cellNo);
							    dataCell.setCellValue("-");
							    dataCell.setCellStyle(noStyle);
			    			}
			    			
			    			if(qprProjectCostSnapshot.getEstimatedCost()!=null)
			    			{
			    				//body.append(qprProjectCostSnapshot.getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN)).append(",");
			    			
			    				 String estimatedCostString = qprProjectCostSnapshot.getEstimatedCost().toPlainString();
			    				  if(estimatedCostString.contains("."))
			    				  {
			    					  String splitCost = estimatedCostString.split("\\.")[0];
			    					  if(splitCost.length()>14)
			    					  {
			    						  //body.append("'"+qprProjectCostSnapshot.getEstimatedCost().toPlainString()).append(",");
			    						  dataCell = dataRow.createCell(++cellNo);
										  dataCell.setCellValue("'"+qprProjectCostSnapshot.getEstimatedCost().toPlainString());
										  dataCell.setCellStyle(noStyle);
			    					  }
			    					  else
			    					  {
			    						  //body.append(qprProjectCostSnapshot.getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN)).append(",");
			    						  dataCell = dataRow.createCell(++cellNo);
										  dataCell.setCellValue(qprProjectCostSnapshot.getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
										  dataCell.setCellStyle(noStyle);
			    					  }
			    				  }
			    				  else
			    				  {
			    					  if(estimatedCostString.length()>14)
			    					  {
			    						  //body.append("'"+qprProjectCostSnapshot.getEstimatedCost().toPlainString()).append(",");
			    						  dataCell = dataRow.createCell(++cellNo);
										  dataCell.setCellValue("'"+qprProjectCostSnapshot.getEstimatedCost().toPlainString());
										  dataCell.setCellStyle(noStyle);
			    					  }
			    					  else
			    					  {
			    						 // body.append(qprProjectCostSnapshot.getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN)).append(",");
			    						  dataCell = dataRow.createCell(++cellNo);
										  dataCell.setCellValue(qprProjectCostSnapshot.getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
										  dataCell.setCellStyle(noStyle);
			    					  }
			    				  }
			    			}
			    			else
			    			{
			    				//body.append(" ").append(",");
			    				dataCell = dataRow.createCell(++cellNo);
								dataCell.setCellValue("-");
								dataCell.setCellStyle(noStyle);
			    			}
			    			
			    			if(StringUtils.isNotBlank(qprProjectCostSnapshot.getAgencyType()))
			    			{
			    				//body.append(qprProjectCostSnapshot.getAgencyType()).append(",");
			    				dataCell = dataRow.createCell(++cellNo);
								dataCell.setCellValue(qprProjectCostSnapshot.getAgencyType());
								dataCell.setCellStyle(noStyle);
			    			}
			    			else
			    			{
			    				dataCell = dataRow.createCell(++cellNo);
								dataCell.setCellValue("-");
								dataCell.setCellStyle(noStyle);
			    			}
			    			
			    			//body.append(qprProjectCostSnapshot.getEstimatedCost()).append(",");
			    			
			    			//report.append(body.toString()).append("\n");
					         
			    		}
		    		}
		    		else
		    		{
		    			
		    			//body.append(qprProjectSnapShot.getProjectID()).append(",");
		    			int cellNo = 0;
			        	HSSFRow dataRow = sheet.createRow(++startRow);
				        HSSFCell dataCell = dataRow.createCell(cellNo);
				        dataCell.setCellValue(qprProjectSnapShot.getProjectID());
				    	dataCell.setCellStyle(noStyle);
				    	 
		    			
						
						dataCell = dataRow.createCell(++cellNo);
						dataCell.setCellValue(qprProjectSnapShot.getProjectName());
						dataCell.setCellStyle(noStyle);
						
		    			
		    			if(qprProjectSnapShot.getBudgetLocation()!=null)
		    			{
		    				dataCell = dataRow.createCell(++cellNo);
							dataCell.setCellValue(qprProjectSnapShot.getBudgetLocation());
							dataCell.setCellStyle(noStyle);
		    			}
		    			else
		    			{
		    				 dataCell = dataRow.createCell(++cellNo);
							 dataCell.setCellValue("-");
							 dataCell.setCellStyle(noStyle);
		    			}
		    			if(qprProjectSnapShot.getMethodologyDetails()!=null && qprProjectSnapShot.getMethodologyDetails().size()>0)
		    			{
		    				//body.append("\"").append(SynchroDAOUtil.getMethodologyNames(Joiner.on(",").join(qprProjectSnapShot.getMethodologyDetails()))).append("\"").append(",");
		    				dataCell = dataRow.createCell(++cellNo);
							dataCell.setCellValue(SynchroDAOUtil.getMethodologyNames(Joiner.on(",").join(qprProjectSnapShot.getMethodologyDetails())));
							dataCell.setCellStyle(noStyle);
		    			}
		    			else
		    			{
		    				dataCell = dataRow.createCell(++cellNo);
							dataCell.setCellValue("-");
							dataCell.setCellStyle(noStyle);
		    			}
		    			

		    			if(qprProjectSnapShot.getBrandSpecificStudy()!=null && qprProjectSnapShot.getBrandSpecificStudy().intValue()==2)
		    			{
		    				if(qprProjectSnapShot.getBrandSpecificStudyType()!=null && qprProjectSnapShot.getBrandSpecificStudyType().intValue()==1)
		    				{
		    					dataCell = dataRow.createCell(++cellNo);
								dataCell.setCellValue("Multi-Brand Study");
								dataCell.setCellStyle(noStyle);
		    				}
		    				else if(qprProjectSnapShot.getBrandSpecificStudyType()!=null && qprProjectSnapShot.getBrandSpecificStudyType().intValue()==2)
		    				{
		    					dataCell = dataRow.createCell(++cellNo);
								dataCell.setCellValue("Non-brand related");
								dataCell.setCellStyle(noStyle);
		    				}
		    				else
		    				{
		    					dataCell = dataRow.createCell(++cellNo);
								dataCell.setCellValue("-");
								dataCell.setCellStyle(noStyle);
		    				}
		    			}
		    			else
		    			{
		    				if(qprProjectSnapShot.getBrand()!=null)
		    				{
		    					if(SynchroGlobal.getBrands().get(qprProjectSnapShot.getBrand().intValue())!=null)
		    					{
		    						dataCell = dataRow.createCell(++cellNo);
									dataCell.setCellValue(SynchroGlobal.getBrands().get(qprProjectSnapShot.getBrand().intValue()));
									dataCell.setCellStyle(noStyle);
		    					}
		    					else
		    					{
		    						dataCell = dataRow.createCell(++cellNo);
									dataCell.setCellValue("-");
									dataCell.setCellStyle(noStyle);
		    					}
		    				}
		    				else
		    				{
		    					dataCell = dataRow.createCell(++cellNo);
								dataCell.setCellValue("-");
								dataCell.setCellStyle(noStyle);
		    				}
		    			}
		    			
		    			if(qprProjectSnapShot.getCategoryType()!=null && qprProjectSnapShot.getCategoryType().size()>0)
		    			{
		    				dataCell = dataRow.createCell(++cellNo);
							dataCell.setCellValue(SynchroDAOUtil.getCategoryNames(Joiner.on(",").join(qprProjectSnapShot.getCategoryType())));
							dataCell.setCellStyle(noStyle);
		    			}
		    			else
		    			{
		    				dataCell = dataRow.createCell(++cellNo);
							dataCell.setCellValue("-");
							dataCell.setCellStyle(noStyle);
		    			}
		    			
		    			
		    			if(qprProjectSnapShot.getTotalCost()!=null)
		    			{
		    				//body.append(qprProjectSnapShot.getTotalCost()).append(",");
		    				//body.append(qprProjectSnapShot.getTotalCost().setScale(2, BigDecimal.ROUND_HALF_EVEN)).append(",");
		    				dataCell = dataRow.createCell(++cellNo);
							dataCell.setCellValue(qprProjectSnapShot.getTotalCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
							dataCell.setCellStyle(noStyle);
		    				
		    			}
		    			else
		    			{
		    				dataCell = dataRow.createCell(++cellNo);
							dataCell.setCellValue("-");
							dataCell.setCellStyle(noStyle);
		    			}
		    			dataCell = dataRow.createCell(++cellNo);
						dataCell.setCellValue("-");
						dataCell.setCellStyle(noStyle);
						
						dataCell = dataRow.createCell(++cellNo);
						dataCell.setCellValue("-");
						dataCell.setCellStyle(noStyle);
						
						dataCell = dataRow.createCell(++cellNo);
						dataCell.setCellValue("-");
						dataCell.setCellStyle(noStyle);
						
						dataCell = dataRow.createCell(++cellNo);
						dataCell.setCellValue("-");
						dataCell.setCellStyle(noStyle);
						
						dataCell = dataRow.createCell(++cellNo);
						dataCell.setCellValue("-");
						dataCell.setCellStyle(noStyle);
		    			
		    			
		    		}
		    	
		    	}
	        	
	        	
	        }
	 
        return workbook;
        
        
   }
    public String downloadSnapshotOLD() throws IOException
    {
    	String downloadSnapshotId = getRequest().getParameter("downloadSnapshotId");
    	
    	//downloadFilename = "QPR_Snapshot.csv";
    	StringBuilder report = new StringBuilder();
    	if(StringUtils.isNotBlank(downloadSnapshotId))
    	{
	    	Long snapShotID = Long.parseLong(downloadSnapshotId);
	    	
	
	        StringBuilder header = new StringBuilder();
	       // header.append("Spend For").append(",");
	       // header.append("Budget year").append(",");
	       // header.append("Freeze Date").append(",");
	        header.append("Project Code").append(",");
	        header.append("Project Name").append(",");
	        header.append("Budget Location").append(",");
	        header.append("Region").append(",");
	        header.append("Area").append(",");
	        header.append("T20/T40").append(",");
	        
	        header.append("Methodology").append(",");
	        header.append("Methodology Group").append(",");
	        
	        header.append("Branded/NonBranded").append(",");
	        header.append("Brand Type").append(",");
	        
	        header.append("Category Type").append(",");
	        header.append("Total Cost (GBP)").append(",");
	        header.append("Agency").append(",");
	        header.append("Cost Component").append(",");
	        header.append("Cost Currency").append(",");
	        header.append("Estimated Cost").append(",");
	        header.append("Agency Type").append(",");
		        
	        report.append(header.toString()).append("\n");
	        
	        QPRSnapshot qprSnapShot = qprSnapshotManager.getSnapshot(snapShotID);  
	        
	        
	        //http://redmine.nvish.com/redmine/issues/379
	        downloadFilename = SynchroGlobal.getSpendForOptions().get(qprSnapShot.getSpendFor())+"_"+qprSnapShot.getBudgetYear();
			if(qprSnapShot.getFreezeDate()!=null)
	        {
				downloadFilename= downloadFilename+"_"+(new SimpleDateFormat("dd-MM-yyyy").format(qprSnapShot.getFreezeDate()));
	        }
	        
			downloadFilename=downloadFilename+".csv";
	        
	    	List<QPRProjectSnapshot> qprProjectSnapShotList =  qprSnapshotManager.getProjectSnapshot(snapShotID);
	    	
	    	
	    	for(QPRProjectSnapshot qprProjectSnapShot : qprProjectSnapShotList)
	    	{
	    		List<QPRProjectCostSnapshot> qprProjectCostSnapshotList = qprSnapshotManager.getProjectCostSnapshot(snapShotID,qprProjectSnapShot.getProjectID());
	    		if(qprProjectCostSnapshotList!=null && qprProjectCostSnapshotList.size()>0)
	    		{
		    		for(QPRProjectCostSnapshot qprProjectCostSnapshot : qprProjectCostSnapshotList)
		    		{
		    			StringBuilder body = new StringBuilder();
		    			/*body.append(SynchroGlobal.getSpendForOptions().get(qprSnapShot.getSpendFor())).append(",");
		    			body.append(qprSnapShot.getBudgetYear()).append(",");
		    			if(qprSnapShot.getFreezeDate()!=null)
				        {
		    				body.append(new SimpleDateFormat("dd/MM/yyyy").format(qprSnapShot.getFreezeDate())).append(",");;
				        }
				        else
				        {
				        	 body.append(" ").append(",");
				        }*/
		    			body.append(qprProjectSnapShot.getProjectID()).append(",");
		    			body.append(qprProjectSnapShot.getProjectName()).append(",");
		    			/*if(qprProjectSnapShot.getBudgetLocation().intValue()==-1)
		    			{
		    				body.append("Global").append(",");
		    			}
		    			else
		    			{
		    				body.append(SynchroGlobal.getEndMarkets().get(qprProjectSnapShot.getBudgetLocation())).append(",");
		    			}*/
		    			body.append(SynchroUtils.getBudgetLocationName(qprProjectSnapShot.getBudgetLocation())).append(",");
		    			
		    			body.append(qprProjectSnapShot.getRegion()).append(",");
		    			body.append(qprProjectSnapShot.getArea()).append(",");
		    			body.append(qprProjectSnapShot.getT20_t40()).append(",");
		    		        
		    			if(qprProjectSnapShot.getMethodologyDetails()!=null && qprProjectSnapShot.getMethodologyDetails().size()>0)
		    			{
		    				body.append("\"").append(SynchroDAOUtil.getMethodologyNames(Joiner.on(",").join(qprProjectSnapShot.getMethodologyDetails()))).append("\"").append(",");
		    			}
		    			else
		    			{
		    				body.append(" ").append(",");
		    			}
		    			
		    			body.append(qprProjectSnapShot.getMethGroup()).append(",");
		    			
		    			if(qprProjectSnapShot.getBrandSpecificStudy()!=null && qprProjectSnapShot.getBrandSpecificStudy().intValue()==2)
		    			{
		    				if(qprProjectSnapShot.getBrandSpecificStudyType()!=null && qprProjectSnapShot.getBrandSpecificStudyType().intValue()==1)
		    				{
		    					body.append("Multi-Brand Study").append(",");
		    				}
		    				else if(qprProjectSnapShot.getBrandSpecificStudyType()!=null && qprProjectSnapShot.getBrandSpecificStudyType().intValue()==2)
		    				{
		    					body.append("Non-brand related").append(",");
		    				}
		    				else
		    				{
		    					body.append(" ").append(",");
		    				}
		    			}
		    			else
		    			{
		    				if(qprProjectSnapShot.getBrand()!=null)
		    				{
		    					if(SynchroGlobal.getBrands().get(qprProjectSnapShot.getBrand().intValue())!=null)
		    					{
		    						body.append(SynchroGlobal.getBrands().get(qprProjectSnapShot.getBrand().intValue())).append(",");
		    					}
		    					else
		    					{
		    						body.append(" ").append(",");
		    					}
		    				}
		    				else
		    				{
		    					body.append(" ").append(",");
		    				}
		    			}
		    			
		    			body.append(qprProjectSnapShot.getBrandType()).append(",");
		    			//body.append(qprProjectSnapShot.getTotalCost()).append(",");
		    			
		    			if(qprProjectSnapShot.getCategoryType()!=null && qprProjectSnapShot.getCategoryType().size()>0)
		    			{
		    				body.append("\"").append(SynchroDAOUtil.getCategoryNames(Joiner.on(",").join(qprProjectSnapShot.getCategoryType()))).append("\"").append(",");
		    			}
		    			else
		    			{
		    				body.append(" ").append(",");
		    			}
		    			
		    			if(qprProjectCostSnapshot.getEstimatedCost()!=null && qprProjectCostSnapshot.getCostCurrency()!=null)
		    			{
		    						    				 
		    				// This is commented as for QPR snapshot download report we need to display the estimated cost for each agency row in GBP instead of Total cost  
		    				/* String totalCostString = qprProjectSnapShot.getTotalCost().toPlainString();
		    				  if(totalCostString.contains("."))
		    				  {
		    					  String splitCost = totalCostString.split("\\.")[0];
		    					  if(splitCost.length()>14)
		    					  {
		    						  body.append("'"+qprProjectSnapShot.getTotalCost().toPlainString()).append(",");
		    					  }
		    					  else
		    					  {
		    						  body.append(qprProjectSnapShot.getTotalCost().setScale(2, BigDecimal.ROUND_HALF_EVEN)).append(",");
		    					  }
		    				  }
		    				  else
		    				  {
		    					  if(totalCostString.length()>14)
		    					  {
		    						  body.append("'"+qprProjectSnapShot.getTotalCost().toPlainString()).append(",");
		    					  }
		    					  else
		    					  {
		    						  body.append(qprProjectSnapShot.getTotalCost().setScale(2, BigDecimal.ROUND_HALF_EVEN)).append(",");
		    					  }
		    				  }*/
		    				  
		    				  String totalCostString = "";
		    				  BigDecimal totalCostBD = null;
		    				  if(qprProjectCostSnapshot!=null && qprProjectCostSnapshot.getCostCurrency()!=null && (qprProjectCostSnapshot.getCostCurrency().intValue()==JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
				        	   {
				    				//dataCell.setCellValue(projectCostDetailsList.get(0).getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
		    					  totalCostString = qprProjectCostSnapshot.getEstimatedCost().toPlainString();
		    					  totalCostBD = qprProjectCostSnapshot.getEstimatedCost();
				        	   }
				        	   else if(qprProjectCostSnapshot!=null && qprProjectCostSnapshot.getCostCurrency()!=null && (qprProjectCostSnapshot.getCostCurrency().intValue()!=JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1)))
				        	   {
				        			BigDecimal gbpEstimatedCost = BigDecimal.valueOf((SynchroUtils.getCurrencyExchangeRate(qprProjectCostSnapshot.getCostCurrency())) * (Double.valueOf(qprProjectCostSnapshot.getEstimatedCost().doubleValue())));
				        			//dataCell.setCellValue(gbpEstimatedCost.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
				        			totalCostString = gbpEstimatedCost.toPlainString();
				        			totalCostBD = gbpEstimatedCost;
				        	   }
				        	   
		    				  if(totalCostString.contains("."))
		    				  {
		    					  String splitCost = totalCostString.split("\\.")[0];
		    					  if(splitCost.length()>14)
		    					  {
		    						  body.append("'"+totalCostString).append(",");
		    					  }
		    					  else
		    					  {
		    						  body.append(totalCostBD.setScale(2, BigDecimal.ROUND_HALF_EVEN)).append(",");
		    					  }
		    				  }
		    				  else
		    				  {
		    					  if(totalCostString.length()>14)
		    					  {
		    						  body.append("'"+totalCostString).append(",");
		    					  }
		    					  else
		    					  {
		    						  body.append(totalCostBD.setScale(2, BigDecimal.ROUND_HALF_EVEN)).append(",");
		    					  }
		    				  }
				        	  
		    				  
		    			}
		    			else
		    			{
		    				body.append(" ").append(",");
		    			}
		    			
		    			if(qprProjectCostSnapshot.getAgencyId()!=null && SynchroGlobal.getResearchAgency().get(qprProjectCostSnapshot.getAgencyId().intValue())!=null)
		    			{
		    				body.append(SynchroGlobal.getResearchAgency().get(qprProjectCostSnapshot.getAgencyId().intValue())).append(",");
		    			}
		    			else
		    			{
		    				body.append(" ").append(",");
		    			}
		    			if(qprProjectCostSnapshot.getCostComponent().intValue()==1)
		    			{
		    				body.append("Coordination").append(",");
		    			}
		    			else
		    			{
		    				body.append("FieldWork").append(",");
		    			}
		    			if(qprProjectCostSnapshot.getCostCurrency()!=null && SynchroGlobal.getCurrencies().get(qprProjectCostSnapshot.getCostCurrency())!=null)
		    			{
		    				body.append(SynchroGlobal.getCurrencies().get(qprProjectCostSnapshot.getCostCurrency())).append(",");
		    			}
		    			else
		    			{
		    				body.append(" ").append(",");
		    			}
		    			
		    			if(qprProjectCostSnapshot.getEstimatedCost()!=null)
		    			{
		    				//body.append(qprProjectCostSnapshot.getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN)).append(",");
		    			
		    				 String estimatedCostString = qprProjectCostSnapshot.getEstimatedCost().toPlainString();
		    				  if(estimatedCostString.contains("."))
		    				  {
		    					  String splitCost = estimatedCostString.split("\\.")[0];
		    					  if(splitCost.length()>14)
		    					  {
		    						  body.append("'"+qprProjectCostSnapshot.getEstimatedCost().toPlainString()).append(",");
		    					  }
		    					  else
		    					  {
		    						  body.append(qprProjectCostSnapshot.getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN)).append(",");
		    					  }
		    				  }
		    				  else
		    				  {
		    					  if(estimatedCostString.length()>14)
		    					  {
		    						  body.append("'"+qprProjectCostSnapshot.getEstimatedCost().toPlainString()).append(",");
		    					  }
		    					  else
		    					  {
		    						  body.append(qprProjectCostSnapshot.getEstimatedCost().setScale(2, BigDecimal.ROUND_HALF_EVEN)).append(",");
		    					  }
		    				  }
		    			}
		    			else
		    			{
		    				body.append(" ").append(",");
		    			}
		    			
		    			if(StringUtils.isNotBlank(qprProjectCostSnapshot.getAgencyType()))
		    			{
		    				body.append(qprProjectCostSnapshot.getAgencyType()).append(",");
		    			}
		    			else
		    			{
		    				body.append(" ").append(",");
		    			}
		    			
		    			//body.append(qprProjectCostSnapshot.getEstimatedCost()).append(",");
		    			
		    			report.append(body.toString()).append("\n");
				         
		    		}
	    		}
	    		else
	    		{
	    			StringBuilder body = new StringBuilder();
	    			/*body.append(SynchroGlobal.getSpendForOptions().get(qprSnapShot.getSpendFor())).append(",");
	    			body.append(qprSnapShot.getBudgetYear()).append(",");
	    			if(qprSnapShot.getFreezeDate()!=null)
			        {
	    				body.append(new SimpleDateFormat("dd/MM/yyyy").format(qprSnapShot.getFreezeDate())).append(",");
			        }
			        else
			        {
			        	 body.append(" ").append(",");
			        }*/
	    			body.append(qprProjectSnapShot.getProjectID()).append(",");
	    			body.append(qprProjectSnapShot.getProjectName()).append(",");
	    			if(qprProjectSnapShot.getBudgetLocation()!=null)
	    			{
	    				body.append(qprProjectSnapShot.getBudgetLocation()).append(",");
	    			}
	    			else
	    			{
	    				 body.append(" ").append(",");
	    			}
	    			if(qprProjectSnapShot.getMethodologyDetails()!=null && qprProjectSnapShot.getMethodologyDetails().size()>0)
	    			{
	    				body.append("\"").append(SynchroDAOUtil.getMethodologyNames(Joiner.on(",").join(qprProjectSnapShot.getMethodologyDetails()))).append("\"").append(",");
	    			}
	    			else
	    			{
	    				body.append(" ").append(",");
	    			}
	    			

	    			if(qprProjectSnapShot.getBrandSpecificStudy()!=null && qprProjectSnapShot.getBrandSpecificStudy().intValue()==2)
	    			{
	    				if(qprProjectSnapShot.getBrandSpecificStudyType()!=null && qprProjectSnapShot.getBrandSpecificStudyType().intValue()==1)
	    				{
	    					body.append("Multi-Brand Study").append(",");
	    				}
	    				else if(qprProjectSnapShot.getBrandSpecificStudyType()!=null && qprProjectSnapShot.getBrandSpecificStudyType().intValue()==2)
	    				{
	    					body.append("Non-brand related").append(",");
	    				}
	    				else
	    				{
	    					body.append(" ").append(",");
	    				}
	    			}
	    			else
	    			{
	    				if(qprProjectSnapShot.getBrand()!=null)
	    				{
	    					if(SynchroGlobal.getBrands().get(qprProjectSnapShot.getBrand().intValue())!=null)
	    					{
	    						body.append(SynchroGlobal.getBrands().get(qprProjectSnapShot.getBrand().intValue())).append(",");
	    					}
	    					else
	    					{
	    						body.append(" ").append(",");
	    					}
	    				}
	    				else
	    				{
	    					body.append(" ").append(",");
	    				}
	    			}
	    			
	    			if(qprProjectSnapShot.getCategoryType()!=null && qprProjectSnapShot.getCategoryType().size()>0)
	    			{
	    				body.append("\"").append(SynchroDAOUtil.getCategoryNames(Joiner.on(",").join(qprProjectSnapShot.getCategoryType()))).append("\"").append(",");
	    			}
	    			else
	    			{
	    				body.append(" ").append(",");
	    			}
	    			
	    			
	    			if(qprProjectSnapShot.getTotalCost()!=null)
	    			{
	    				//body.append(qprProjectSnapShot.getTotalCost()).append(",");
	    				body.append(qprProjectSnapShot.getTotalCost().setScale(2, BigDecimal.ROUND_HALF_EVEN)).append(",");
	    				
	    			}
	    			else
	    			{
	    				body.append(" ").append(",");
	    			}
	    			body.append(" ").append(",");
	    			body.append(" ").append(",");
	    			body.append(" ").append(",");
	    			body.append(" ").append(",");
	    			body.append(" ").append(",");
	    			
	    			report.append(body.toString()).append("\n");
	    		}
	    	}
	    	
	        
    	}
        
        downloadStream = new ByteArrayInputStream(report.toString().getBytes("utf-8"));
         return DOWNLOAD_REPORT;
    	
    }
    
    
    
    public String deleteSnapshot() throws IOException
    {
    	String deleteSnapshotId = getRequest().getParameter("deleteSnapshotId");
    	
    	try
    	{
	    	Long snapShotId = Long.valueOf(deleteSnapshotId);
	    	qprSnapshotManager.deleteSnapshot(snapShotId);
    	}
    	catch(Exception e)
    	{
    		LOG.error("Failed to delete QPR Snapshot ==>"+ deleteSnapshotId);
    	}
    	
    	 return SUCCESS;
    }
    
    private void generateDownloadFileName(final String year, final boolean multipleFiles) {
        String fileName = "Spend Reports.xls";
        
        
        Calendar calendar = Calendar.getInstance();
        timeStamp = calendar.get(Calendar.YEAR) +
                "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) ;
        
      

        if(multipleFiles) {
            downloadStreamType = "application/vnd.ms-excel";
        } else {
            downloadStreamType = "application/vnd.ms-excel";
        }

        downloadFilename = fileName;
    }
    
    public String downloadCrosstabSnapshot() throws IOException
    {
    	String downloadSnapshotId = getRequest().getParameter("downloadSnapshotId");
    	HSSFWorkbook workbook = null;
    	workbook = new HSSFWorkbook();
    	 generateDownloadFileName(null, false);
    	 
    	 SpendReportExtractFilter filter = new SpendReportExtractFilter();
    	 workbook = generateReport(workbook, filter, new Long(downloadSnapshotId), false, null);
        //downloadStream = new ByteArrayInputStream(report.toString().getBytes("utf-8"));
        //return DOWNLOAD_REPORT;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        downloadStream = new ByteArrayInputStream(baos.toByteArray());
        return DOWNLOAD_REPORT;
    }
    
    public String downloadLatestCostCrosstabSnapshot() throws IOException
    {
    	String downloadSnapshotId = getRequest().getParameter("downloadSnapshotId");
    	HSSFWorkbook workbook = null;
    	workbook = new HSSFWorkbook();
    	 generateDownloadFileName(null, false);
    	 
    	 SpendReportExtractFilter filter = new SpendReportExtractFilter();
    	 workbook = generateReport(workbook, filter, new Long(downloadSnapshotId), true, null);
        //downloadStream = new ByteArrayInputStream(report.toString().getBytes("utf-8"));
        //return DOWNLOAD_REPORT;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        downloadStream = new ByteArrayInputStream(baos.toByteArray());
        return DOWNLOAD_REPORT;
    }
    
    public String downloadCrossTabLatestCostBY() throws IOException
    {
    	String budgetYear = getRequest().getParameter("latestCostBY");
    	
    	Integer byear = Integer.parseInt(budgetYear);
    	
    	HSSFWorkbook workbook = null;
    	workbook = new HSSFWorkbook();
    	 generateDownloadFileName(null, false);
    	 
    	 SpendReportExtractFilter filter = new SpendReportExtractFilter();
    	 workbook = generateReport(workbook, filter, null, true, byear);
        //downloadStream = new ByteArrayInputStream(report.toString().getBytes("utf-8"));
        //return DOWNLOAD_REPORT;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        downloadStream = new ByteArrayInputStream(baos.toByteArray());
        return DOWNLOAD_REPORT;
    }
    
    
    private HSSFWorkbook generateReport( HSSFWorkbook workbook, final SpendReportExtractFilter filter, Long snapShotId, boolean latestCost, Integer budgetYear) throws UnsupportedEncodingException, IOException{
        
    	List<Long> snapShotIds = new ArrayList<Long>();
    	
    	Map<Integer, Map<Integer, Long>> snapShotMap = new HashMap<Integer, Map<Integer, Long>>();
    	
    	QPRSnapshot qprSnapshot = null;
    	
    	String years = "";
    	String spendForSnapshot = "";
    	
    	if(budgetYear!=null && budgetYear > 0)
    	{
    		years = budgetYear+"";
    	}
    	else
    	{
    		qprSnapshot = qprSnapshotManager.getSnapshot(snapShotId);
        	
        	years = qprSnapshot.getBudgetYear()+"";
        	spendForSnapshot = qprSnapshot.getSpendFor()+"";
    	}
    	
    	
    	
    	if(latestCost)
    	{
    		spendForSnapshot = "6";
    	}
    	Integer currencyId = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1);
			
		HashMap<Integer, Long> sMap = new HashMap<Integer, Long>();
		if(snapShotId!=null && snapShotId.intValue() > 0)
		{
			snapShotIds.add(snapShotId);
			
			sMap.put(qprSnapshot.getSpendFor(), snapShotId);
		}
			
		if(qprSnapshot!=null)
		{
			snapShotMap.put(qprSnapshot.getBudgetYear(),sMap );
		}
		
		String reportTypes = SynchroGlobal.SpendReportTypeNew.SPEND_BY_PROJECTS.getId() + "," + SynchroGlobal.SpendReportTypeNew.SPEND_BY_BUDGET_LOCATION.getId() +"," + SynchroGlobal.SpendReportTypeNew.SPEND_BY_METHODOLOGY.getId();
    	
    	if(reportTypes != null && !reportTypes.equals("")) {


            String [] reportTypesStrArr = reportTypes.split(",");
            CurrencyExchangeRate currencyExchangeRate = getUserCurrencyExchangeRate(getUser());
            Integer startColumn = 0;
            Integer startRow = 0;
            boolean showProjectCodeColumn = false;


            if(reportTypesStrArr != null && reportTypesStrArr.length > 0) {

                HSSFDataFormat df = workbook.createDataFormat();
                short currencyDataFormatIndex = df.getFormat("#,###0.0000");

           

                // Header Styles start
                HSSFFont sheetHeaderFont = workbook.createFont();
                sheetHeaderFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

                HSSFFont notesFont = workbook.createFont();
                notesFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                notesFont.setItalic(true);
                
                

                HSSFFont italicFont = workbook.createFont();
                italicFont.setItalic(true);

                HSSFCellStyle sheetHeaderCellStyle = workbook.createCellStyle();
                sheetHeaderCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                sheetHeaderCellStyle.setFont(sheetHeaderFont);
                sheetHeaderCellStyle.setWrapText(true);
                sheetHeaderCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                // Header Styles end


                // Table Header Column Styles start

                // Table header column1 style
                HSSFCellStyle tableHeaderProjectCodeColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THICK);
                tableHeaderProjectCodeColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                HSSFCellStyle tableHeaderProjectNameColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THIN);
                tableHeaderProjectNameColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                // Table header column1 style
                HSSFCellStyle tableHeaderColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM);
                tableHeaderColumn1Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                
                tableHeaderColumn1Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                tableHeaderColumn1Style.setAlignment(CellStyle.ALIGN_CENTER);
                tableHeaderColumn1Style.setWrapText(true);
            	
                // Table header column2 style
                HSSFCellStyle tableHeaderColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn2Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                
                tableHeaderColumn2Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                tableHeaderColumn2Style.setAlignment(CellStyle.ALIGN_CENTER);
                tableHeaderColumn2Style.setWrapText(true);
            	

                // Table header column3 style
                HSSFCellStyle tableHeaderColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM);
                tableHeaderColumn3Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                
                tableHeaderColumn3Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                tableHeaderColumn3Style.setAlignment(CellStyle.ALIGN_CENTER);
                tableHeaderColumn3Style.setWrapText(true);

                // Table header column4 style
                HSSFCellStyle tableHeaderColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn4Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                // Table header column4 style
                HSSFCellStyle tableHeaderColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn5Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                // Table Header Column Styles end

                // Data Row cell styles start
                HSSFCellStyle projectCodeDataRowColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_THICK);
                projectCodeDataRowColumnStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);

                HSSFCellStyle projectNameDataRowColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_THIN);
                projectNameDataRowColumnStyle.setWrapText(true);

                HSSFCellStyle dataRowColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THICK);
                dataRowColumn1Style.setWrapText(true);
                dataRowColumn1Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                dataRowColumn1Style.setAlignment(CellStyle.ALIGN_CENTER);

                HSSFCellStyle dataRowColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                
                dataRowColumn2Style.setWrapText(true);


                HSSFCellStyle dataRowColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN);
                dataRowColumn3Style.setAlignment(CellStyle.ALIGN_CENTER);

                HSSFCellStyle dataRowColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle dataRowColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE);
                // Data Row cell styles end

                // Total cost row styles start
                HSSFCellStyle totalCostRowProjectCodeColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                totalCostRowProjectCodeColumnStyle.setWrapText(true);
                totalCostRowProjectCodeColumnStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                totalCostRowProjectCodeColumnStyle.setAlignment(CellStyle.ALIGN_CENTER);
                totalCostRowProjectCodeColumnStyle.setFont(sheetHeaderFont);
                
                
                HSSFCellStyle totalCostRowTotalColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THIN);
                totalCostRowTotalColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                HSSFCellStyle totalCostRowColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THICK);
                totalCostRowColumn1Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                HSSFCellStyle totalCostRowColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle notesStyle = workbook.createCellStyle();
                notesStyle.setWrapText(true);
                notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
                
                notesStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
	            notesStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		    	
	            notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		        
	            notesStyle.setAlignment(CellStyle.ALIGN_LEFT);
		    	notesStyle.setWrapText(true);
                
                HSSFDataFormat cf = workbook.createDataFormat();
                short currencyDataFormatIndexString = cf.getFormat("#,###0");
     	       //short currencyDataFormatIndexString = cf.getFormat("@");
                
                HSSFCellStyle costFormatStyle = workbook.createCellStyle();
                costFormatStyle.setDataFormat(currencyDataFormatIndexString);
                
                short currencyDecimalDataFormatIndex = cf.getFormat("#,###0.00");
                HSSFCellStyle costDecimalFormatStyle = workbook.createCellStyle();
                costDecimalFormatStyle.setDataFormat(currencyDecimalDataFormatIndex);
              
                // Total cost row styles end
      
                for(String reportTypeStr : reportTypesStrArr) {
                    Integer reportType = Integer.parseInt(reportTypeStr);
                    startRow = 0;
                    startColumn = 0;
                    
                    // Create sheet for each report type

                    
                    // Spend By Projects (as this is cross tab of few fields )
                    if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_PROJECTS.getId())) 
                    {
                    	workbook = generateCrossTabReport(workbook, filter, snapShotId, latestCost, budgetYear);
                    }
                    
                    else
                    {
	                    HSSFSheet sheet = workbook.createSheet(SynchroGlobal.SpendReportTypeNew.getById(reportType).getName().replaceAll("/","or"));
	
	                    
	                    StringBuilder generatedBy = new StringBuilder();
	                    generatedBy.append("Spend Reports By ").append(SynchroGlobal.SpendReportTypeNew.getById(reportType).getDescription()).append("\n").
	                    append(" ( ").append(currencyExchangeRate.getCurrencyCode()).append(" )").append(" Budget Year - ").append(years).append("\n");
	                    String userName = getUser().getName();
	                    generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
	                   // generatedBy.append("\"").append("Filters:").append("\"").append("\n");
	                    
	                    
	
	                  //  generatedBy.append("\"").append("Filters:").append(Joiner.on(",").join(selectedFilters)).append("\"").append("\n");
	                    
	                    // Create sheet row1(Report Type Header)
	                    HSSFRow reportTypeHeader = sheet.createRow(startRow);
	                    HSSFCell reportTypeHeaderColumn = reportTypeHeader.createCell(startColumn);
	                    reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	                    reportTypeHeaderColumn.setCellValue(generatedBy.toString());
	
	                    // Create sheet row2
	                    HSSFRow userNameHeader = sheet.createRow(startRow + 1);
	                    HSSFCell userNameHeaderColumn = userNameHeader.createCell(startColumn);
	                    userNameHeaderColumn.setCellStyle(sheetHeaderCellStyle);
	                  //  userNameHeaderColumn.setCellValue("User: " + getUser().getFirstName() + " " + getUser().getLastName());
	
	                    Integer filtersRowCount = 0;
	
	                    if(filter != null && filter.getYears() != null && filter.getYears().size() > 0) {
	                        HSSFRow yearsFilterRow = sheet.createRow(startRow + 2);
	                        HSSFCell yearsFilterRowCell = yearsFilterRow.createCell(startColumn);
	                        yearsFilterRowCell.setCellStyle(sheetHeaderCellStyle);
	                    //    yearsFilterRowCell.setCellValue("Year(s): "+StringUtils.join(filter.getYears(),", "));
	                        filtersRowCount++;
	                    }
	

	                   
	                    
                    // Spend By Budget Location
                    if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_BUDGET_LOCATION.getId())) 
                    {

                        
                    	// Table Header row
                        HSSFRow tableBudgetYearHeaderRow = sheet.createRow(startRow + 3 + filtersRowCount);
                        String[] budgetYearsArr = years.split(",");
                      //  String[] spendForSnapshotArr = spendForSnapshot.split(",");
                        
                        String[] spendForSnapshotArrOriginal = spendForSnapshot.split(",");
                        
                        List<String> spendForSnapShotList = new ArrayList<String>();
                      
                        boolean showLatestCost = false;
                        
                        for(int z=0;z<spendForSnapshotArrOriginal.length;z++)
                        {
                        	if(!spendForSnapshotArrOriginal[z].equals("6"))
                        	{
                        		spendForSnapShotList.add(spendForSnapshotArrOriginal[z]);
                        	}
                        	else
                        	{
                        		showLatestCost = true;
                        	}
                        }
                        //String[] spendForSnapshotArr = (String[])spendForSnapShotList.toArray();
                        String [] spendForSnapshotArr = spendForSnapShotList.toArray(new String[spendForSnapShotList.size()]);
                        
                        int cellIndex = 4;
                        
                       // sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),3,5));
                       // sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),6,9));
                        
                        
                        
                    	//int firstRowColumns = 4 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                    	int firstRowColumns = 0;
                        if(showLatestCost)
                        {
                        	firstRowColumns = 4 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        }
                        else
                        {
                        	firstRowColumns = 4 + (budgetYearsArr.length) * (spendForSnapshotArr.length);
                        }
                        
                    	for(int m=0;m<firstRowColumns;m++)
                    	{
                    		HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(m);
                    		budgetYearColumn0.setCellType(HSSFCell.CELL_TYPE_STRING);
                    		budgetYearColumn0.setCellValue(" ");
                    		budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
                    	}
                	
                    	 totalCosts=new ArrayList<BigDecimal>();
                        
                        for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
                        	
                        	HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(cellIndex);
	                        budgetYearColumn0.setCellValue(budgetYearsArr[i]);
	                        budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
	                     /*   for(int j=0;j<(spendForSnapshotArr.length);j++)
	                        {
	                        	int emptyColum = cellIndex;
	                        	budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(++emptyColum);
		                        budgetYearColumn0.setCellValue(" ");
		                        budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
	                        }*/
	                       
	                        if(showLatestCost)
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
	                        	}
	                        }
	                        else
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+(spendForSnapshotArr.length-1))));
	                        	}
	                        }
	                        
	                        /* if(i==0)
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
                        	else
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
                        	
	                       
	                        cellIndex = cellIndex+spendForSnapshotArr.length+1;*/
	                        
	                        if(showLatestCost)
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length+1;
	                        }
	                        else
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length;
	                        }
		                    
                        }
                        
                        
                        HSSFRow tableHeaderRow = sheet.createRow(startRow + 4 + filtersRowCount);
                        
                        tableHeaderRow.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
                    	
                    	// Table Header column0(Project Code)
	                    HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn0.setCellValue("Region");
	                    tableHeaderColumn0.setCellStyle(tableHeaderColumn1Style);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                   // sheet.autoSizeColumn(startColumn);
	              
	
	                    HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn1.setCellValue("Area");
	                    tableHeaderColumn1.setCellStyle(tableHeaderColumn2Style);
	                    //sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    
	                    HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn2.setCellValue("T20, T40, or non-T40");
	                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                 //   sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	
	                    HSSFCell tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn3.setCellValue("Budget Location");
	                    tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                   // sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    
	                    HSSFCell costColumns;
	                    
	                    int budgetYearColumn=0;
	                    int dataStartColumn = 0;
	                    
	                    boolean isQPR1SpendFor=false;
                        boolean isQPR2SpendFor=false;
                        boolean isQPR3SpendFor=false;
                        boolean isFullYearSpendFor=false;
                        boolean isCOPLASpendFor=false;
                        boolean isLatestCost=false;
                        
	                    for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
	                    	isQPR1SpendFor=false;
	                        isQPR2SpendFor=false;
	                        isQPR3SpendFor=false;
	                        isFullYearSpendFor=false;
	                        isCOPLASpendFor=false;
	                        isLatestCost=false;
	                        
	                    	for(int j=0;j<spendForSnapshotArrOriginal.length;j++)
		                    {
	                        	if(spendForSnapshotArrOriginal[j].equals("1"))
	                        	{
	                        		isCOPLASpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("2"))
	                        	{
	                        		isQPR1SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("3"))
	                        	{
	                        		isQPR2SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("4"))
	                        	{
	                        		isQPR3SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("5"))
	                        	{
	                        		isFullYearSpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("6"))
	                        	{
	                        		isLatestCost=true;
	                        	}
	                        	
	                    		
		                    }
	                    	
	                    	
	                    	
	                    	if(isCOPLASpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("COPLA");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR1SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR1");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR2SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR2");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isQPR3SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR3");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	if(isFullYearSpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("FULL YEAR");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
	                    	// Latest Cost will be shown only if it selected in UI
	                    	if(isLatestCost)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("LATEST COST");
	                        	costColumns.setCellStyle(tableHeaderColumn3Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                    	}
                        	
                        	
                        	
                        	if(i==0)
                        	{
                        		startRow = startRow + 4 + filtersRowCount;
                        	}
                        	
                        	
                       /* 	List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByBudgetLocation(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                    	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor, isLatestCost);*/
                        	
                        }
                        	
	                    	//List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByBudgetLocation(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
	                    	Map<String,List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByBudgetLocation(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	
	                    	if(spendByReportMap!=null && spendByReportMap.size()>0)
                        	{
                        		for(int i=0;i<budgetYearsArr.length;i++)
                                {
	                        		calculateTotalCostBY(spendByReportMap, isQPR1SpendFor, 
	                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                                }
                        	}
	                    	// This is done in case there are no records for the budget location with which the user is associated with 
                        	else
                        	{
                        		for(int i=0;i<budgetYearsArr.length;i++)
                                {
                        			calculateTotalCostNoBY(spendByReportMap, isQPR1SpendFor, 
	                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                                }
                        	}
	                    	
	                    	if(spendByReportMap!=null && spendByReportMap.size()>0)
                        	{
                        		
                        		//calculateTotalCost(spendByReportMap, isQPR1SpendFor, 
                        	    	//	isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
                        		
                        		HSSFRow dataRow = null;
                    			HSSFCell projectCodeDataRowColumn = null;
                        		for(String bugLocKey:spendByReportMap.keySet() )
                        		{
	                        		
                        			Integer budgetLocation = null;
                        			
                        			try
                        			{
                        				budgetLocation = new Integer(bugLocKey.split("~")[0]);
                        			}
                        			catch(Exception e)
                        			{
                        				
                        			}

                        			String regionName = "";
                      				String areaName = "";
                      				String t20_40_Name = "";
                        			
                        			try
                      				{
                        				 regionName = bugLocKey.split("~")[1];
                      				}
                      				catch(Exception e)
                      				{
                      					
                      				}
                      				try
                      				{
                      					areaName = bugLocKey.split("~")[2];
                      				}
                      				catch(Exception e)
                      				{
                      					
                      				}
                      				try
                      				{
                      					t20_40_Name = bugLocKey.split("~")[3];
                      				}
                      				catch(Exception e)
                      				{
                      					
                      				}
                      				
	                        			
	                        			
		                        			dataStartColumn = 0;
		                        			dataRow = sheet.createRow(++startRow);
		                        		//	previousYearExist = true;
		                        			
			                    			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                    			sheet.setColumnWidth(dataStartColumn, columnWidth);
			                    			
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn1Style);
			                                if(StringUtils.isNotBlank(regionName))
			                                {
			                                	projectCodeDataRowColumn.setCellValue(regionName);
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(" ");
			                                	 projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			    	                             projectCodeDataRowColumn.setCellValue("-");
			    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                }
			                              
			                                dataStartColumn++;
			                                
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                sheet.setColumnWidth(dataStartColumn, columnWidth);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(StringUtils.isNotBlank(areaName))
			                                {
			                                	projectCodeDataRowColumn.setCellValue(areaName);
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(" ");
			                                	 projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			    	                             projectCodeDataRowColumn.setCellValue("-");
			    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                }
			                               
			                                dataStartColumn++;
			                                
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                sheet.setColumnWidth(dataStartColumn, columnWidth);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(StringUtils.isNotBlank(t20_40_Name))
			                                {
			                                	projectCodeDataRowColumn.setCellValue(t20_40_Name);
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(" ");
			                                	 projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			    	                             projectCodeDataRowColumn.setCellValue("-");
			    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                }
			                              
			                                dataStartColumn++;
			                                
			                                
		                        			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                        			sheet.setColumnWidth(dataStartColumn, columnWidth);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(budgetLocation!=null)
			                                {
			                                	projectCodeDataRowColumn.setCellValue(SynchroUtils.getBudgetLocationName(budgetLocation));
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(" ");
			                                	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			    	                             projectCodeDataRowColumn.setCellValue("-");
			    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                }
			                              
			                                dataStartColumn++;
			                                
			                                //int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
			                                
			                                int noOfColumnsToDecorate = 0;
			                                if(isLatestCost)
			                                {
			                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
			                                }
			                                else
			                                {
			                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length);
			                                }
			                                
			                                int decCol = dataStartColumn;
		                                	for(int k=0;k<noOfColumnsToDecorate;k++)
		                                	{
		                                		projectCodeDataRowColumn = dataRow.createCell(decCol);
		                                		sheet.setColumnWidth(decCol, columnWidth);
		    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		    	                                projectCodeDataRowColumn.setCellValue("-");
		    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		    	                                decCol++;
		                                	}
		                                	
		                                	int snapShotRef = 0;
		                                	for(SpendByReportBean spendByBean:spendByReportMap.get(bugLocKey))
					                      	{
					                        		
				                        			boolean isLatesCostNA = true;
				                        			
					                                if(isCOPLASpendFor)
					                                {
						                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
						                            	sheet.setColumnWidth(dataStartColumn, columnWidth);
						                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                if(spendByBean.getCoplaTotalCost()!=null)
						                                {
						                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getCoplaTotalCost().doubleValue());
						                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getCoplaTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
						                                	isLatesCostNA = false;
						                                }
						                                else
						                                {
						                                	
						                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}
						                                	
						                                	/*if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
						                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("1"))!=null)
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                		isLatesCostNA = false;
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}*/
						                                	
						                                	
						                                	//projectCodeDataRowColumn.setCellValue("-");
						                                }
						                                
						                                dataStartColumn++;
						                                snapShotRef++;
					                                }
					                                
					                                if(isQPR1SpendFor)
					                                {
						                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
						                                sheet.setColumnWidth(dataStartColumn, columnWidth);
						                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                if(spendByBean.getQpr1TotalCost()!=null)
						                                {
						                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr1TotalCost().doubleValue());
						                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr1TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
						                                	isLatesCostNA = false;
						                                }
						                                else
						                                {
						                                /*
						                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
						                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("2"))!=null)
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                		isLatesCostNA = false;
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}*/
						                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}
						                                	//projectCodeDataRowColumn.setCellValue("-");
						                                }
						                               
						                                dataStartColumn++;
						                                snapShotRef++;
					                                }
					                                if(isQPR2SpendFor)
					                                {
						                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
						                                sheet.setColumnWidth(dataStartColumn, columnWidth);
						                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                if(spendByBean.getQpr2TotalCost()!=null)
						                                {
						                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr2TotalCost().doubleValue());
						                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr2TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
						                                	isLatesCostNA = false;
						                                }
						                                else
						                                {
						                                
						                                	
						                                /*	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
						                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("3"))!=null)
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                		isLatesCostNA = false;
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}*/
						                                	//projectCodeDataRowColumn.setCellValue("-");
						                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}
						                                }
						                              
						                                dataStartColumn++;
						                                snapShotRef++;
					                                }
					                                
					                                if(isQPR3SpendFor)
					                                {
						                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
						                                sheet.setColumnWidth(dataStartColumn, columnWidth);
						                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                if(spendByBean.getQpr3TotalCost()!=null)
						                                {
						                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr3TotalCost().doubleValue());
						                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr3TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
						                                	isLatesCostNA = false;
						                                }
						                                else
						                                {
						                                	/*
						                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
						                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("4"))!=null)
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                		isLatesCostNA = false;
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}*/
						                                	//projectCodeDataRowColumn.setCellValue("-");
						                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}
						                                }
						                             //   projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                dataStartColumn++;
						                                snapShotRef++;
					                                }
					                                if(isFullYearSpendFor)
					                                {
						                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
						                                sheet.setColumnWidth(dataStartColumn, columnWidth);
						                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                if(spendByBean.getFullYearTotalCost()!=null)
						                                {
						                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getFullYearTotalCost().doubleValue());
						                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getFullYearTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
						                                	isLatesCostNA = false;
						                                }
						                                else
						                                {
						                                	/*
						                                	
						                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
						                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("5"))!=null)
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                		isLatesCostNA = false;
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	//projectCodeDataRowColumn.setCellValue("-");*/
						                                	
						                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}
						                                }
						                               // projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                dataStartColumn++;
						                                snapShotRef++;
					                                }
					                                
					                                if(isLatestCost)
					                                {
						                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
						                                sheet.setColumnWidth(dataStartColumn, columnWidth);
						                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
						                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                if(spendByBean.getLatestTotalCost()!=null)
						                                {
						                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getLatestTotalCost().doubleValue());
						                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getLatestTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
						                                	
						                                }
						                                else
						                                {
						                                	/*if(isLatesCostNA)
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{		                                	
							                                	projectCodeDataRowColumn.setCellValue("-");
							                                	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}*/
						                                	
						                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("NA");
						                                	}
						                                	else
						                                	{
						                                		projectCodeDataRowColumn.setCellValue("-");
						                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
						                                	}
						                                }
						                               // projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						                                dataStartColumn++;
						                                snapShotRef++;
					                                }
					                                
					                              /*  if(i==0)
					                                {
					                                	int noOfColumnsToDecorate = (budgetYearsArr.length-1) * (spendForSnapshotArr.length+1);
					                                	if(noOfColumnsToDecorate>0)
					                                	{
					                                		int columnLocation = dataStartColumn;
					                                		for(int k=0;k<noOfColumnsToDecorate;k++)
						                                	{
					                                			projectCodeDataRowColumn = dataRow.createCell(columnLocation);
						    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
						    	                                projectCodeDataRowColumn.setCellValue(" ");
						    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
						    	                                columnLocation++;
						                                	}
					                                	}
					                                }*/
					                                
				                        		
				                        		/*calculateTotaCost(spendByReportMap.get(bugLoc), isQPR1SpendFor, 
				                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor);*/	
				                        	}
	                        	budgetYearColumn = dataStartColumn;
                        	}
                        	}
                    
                        
                        
	                    // This is for merging the top row
	                    if(dataStartColumn > 0)
	                    {
	                    	//sheet.addMergedRegion(new CellRangeAddress(0,3,0,(dataStartColumn -1)));
	                    	
	                    	if(isLatestCost)
	                    	{
	                    		sheet.addMergedRegion(new CellRangeAddress(0,3,0,(3 + ((budgetYearsArr.length) * (spendForSnapshotArr.length+1)))));
	                    	}
	                    	else
	                    	{
	                    		sheet.addMergedRegion(new CellRangeAddress(0,3,0,(3 + ((budgetYearsArr.length) * (spendForSnapshotArr.length)))));
	                    	}
	                    }
	                   
	                    else
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(3+totalCosts.size() )));
	                    }
	                 
	                 
	                    HSSFRow dataRow = sheet.createRow(++startRow);
	                    HSSFCell totalCostColumn = dataRow.createCell(0);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue("Total Cost");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                   
	                    
	                    totalCostColumn = dataRow.createCell(1);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                    totalCostColumn = dataRow.createCell(2);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                    totalCostColumn = dataRow.createCell(3);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
                        
                        
	                    sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,3));
	                    
                        int totalCostColumnIndex = 4;
                        for(int i=0;i<totalCosts.size();i++)
                        {
                        	totalCostColumn = dataRow.createCell(totalCostColumnIndex);
                        	totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        	//totalCostColumn.setCellValue(totalCosts.get(i).doubleValue());
                        	//totalCostColumn = SynchroUtils.populateCost(totalCosts.get(i), totalCostColumn,costFormatStyle );
                        	totalCostColumn = SynchroUtils.populateSpendByProjectTotalCost(totalCosts.get(i), totalCostColumn,costFormatStyle, costDecimalFormatStyle );
                        	totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
                        	totalCostColumnIndex++;
                        	sheet.autoSizeColumn(totalCostColumnIndex);
                        }
                        
                        //Notes Region
                        
                        if(dataStartColumn > 0)
                        {
	                    	sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+11,0,dataStartColumn -1));
	                    	dataRow = sheet.createRow(startRow+5);
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence,it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                        else
                        {
                        	sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+11,0,(3+totalCosts.size() - 1)));
	                    	dataRow = sheet.createRow(startRow+5);
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence,it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                    
                    }
                    
                    // Spend By Methodology
                    if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_METHODOLOGY.getId()))
                    {

                    	// Table Header row
                        HSSFRow tableBudgetYearHeaderRow = sheet.createRow(startRow + 3 + filtersRowCount);
                        String[] budgetYearsArr = years.split(",");
                        //String[] spendForSnapshotArr = spendForSnapshot.split(",");
                        
                        String[] spendForSnapshotArrOriginal = spendForSnapshot.split(",");
                        
                        List<String> spendForSnapShotList = new ArrayList<String>();
                        boolean showLatestCost = false;
                        
                        for(int z=0;z<spendForSnapshotArrOriginal.length;z++)
                        {
                        	if(!spendForSnapshotArrOriginal[z].equals("6"))
                        	{
                        		spendForSnapShotList.add(spendForSnapshotArrOriginal[z]);
                        	}
                        	else
                        	{
                        		showLatestCost = true;
                        	}
                        }
                        String [] spendForSnapshotArr = spendForSnapShotList.toArray(new String[spendForSnapShotList.size()]);
                        
                        int cellIndex = 2;
                        
                        
                    	//int firstRowColumns = 2 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        
                        int firstRowColumns = 0;
                        if(showLatestCost)
                        {
                        	firstRowColumns = 2 + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                        }
                        else
                        {
                        	firstRowColumns = 2 + (budgetYearsArr.length) * (spendForSnapshotArr.length);
                        }
                        
                    	for(int m=0;m<firstRowColumns;m++)
                    	{
                    		HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(m);
                    		budgetYearColumn0.setCellType(HSSFCell.CELL_TYPE_STRING);
                    		budgetYearColumn0.setCellValue(" ");
                    		budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
                    	}
                	
                    	 totalCosts=new ArrayList<BigDecimal>();
                        
                        for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
                        	
                        	HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(cellIndex);
	                        budgetYearColumn0.setCellValue(budgetYearsArr[i]);
	                        budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
	                    
	                        /*if(i==0)
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
                        	else
                        	{
                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                        	}
	                       
	                        cellIndex = cellIndex+spendForSnapshotArr.length+1;
		                    */
	                        
	                        if(showLatestCost)
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
	                        	}
	                        }
	                        else
	                        {
	                        	if(spendForSnapshotArr.length > 1)
	                        	{
	                        		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+(spendForSnapshotArr.length-1))));
	                        	}
	                        }
	                        
	                        if(showLatestCost)
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length+1;
	                        }
	                        else
	                        {
	                        	cellIndex = cellIndex+spendForSnapshotArr.length;
	                        }
                        }
                        
                        
                        HSSFRow tableHeaderRow = sheet.createRow(startRow + 4 + filtersRowCount);
                        
                        tableHeaderRow.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
                    	
                    	// Table Header column0(Project Code)
	                  /*  HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn0.setCellValue("Project Code");
	                    tableHeaderColumn0.setCellStyle(tableHeaderColumn1Style);
	                   // sheet.autoSizeColumn(startColumn);
	              
	
	                    HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn1.setCellValue("Project Name");
	                    tableHeaderColumn1.setCellStyle(tableHeaderColumn2Style);
	                    sheet.autoSizeColumn(startColumn);
	*/
	                    HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(startColumn);
	                    tableHeaderColumn2.setCellValue("Methodology Group");
	                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                   // sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    
	                    tableHeaderColumn2 = tableHeaderRow.createCell(++startColumn);
	                    tableHeaderColumn2.setCellValue("Methodology");
	                    tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                   // sheet.autoSizeColumn(startColumn);
	                    sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    
	                    HSSFCell costColumns;
	                    
	                    int budgetYearColumn=0;
	                    int dataStartColumn = 0;
	                    
	                    
	                    boolean isQPR1SpendFor=false;
                        boolean isQPR2SpendFor=false;
                        boolean isQPR3SpendFor=false;
                        boolean isFullYearSpendFor=false;
                        boolean isCOPLASpendFor=false;
                        boolean isLatestCost = false;
                        
	                    for(int i=0;i<budgetYearsArr.length;i++)
                        {
	                        
	                    	isQPR1SpendFor=false;
	                        isQPR2SpendFor=false;
	                        isQPR3SpendFor=false;
	                        isFullYearSpendFor=false;
	                        isCOPLASpendFor=false;
	                        isLatestCost = false;
	                    	for(int j=0;j<spendForSnapshotArrOriginal.length;j++)
		                    {
	                        	if(spendForSnapshotArrOriginal[j].equals("1"))
	                        	{
	                        		isCOPLASpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("2"))
	                        	{
	                        		isQPR1SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("3"))
	                        	{
	                        		isQPR2SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("4"))
	                        	{
	                        		isQPR3SpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("5"))
	                        	{
	                        		isFullYearSpendFor=true;
	                        	}
	                        	if(spendForSnapshotArrOriginal[j].equals("6"))
	                        	{
	                        		isLatestCost=true;
	                        	}
	                        	
	                    		
		                    }
	                    	
                        
	                    	
	                    	if(isCOPLASpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("COPLA");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                    	if(isQPR1SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR1");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                    	if(isQPR2SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR2");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                    	if(isQPR3SpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("QPR3");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                    	if(isFullYearSpendFor)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("FULL YEAR");
	                        	costColumns.setCellStyle(tableHeaderColumn2Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}
	                        
	                    	// Latest Cost will be shown only if it selected in UI
	                    	if(isLatestCost)
	                    	{
		                    	costColumns = tableHeaderRow.createCell(++startColumn);
	                        	costColumns.setCellValue("LATEST COST");
	                        	costColumns.setCellStyle(tableHeaderColumn3Style);
	                        	//sheet.autoSizeColumn(startColumn);
	                        	sheet.setColumnWidth(startColumn.intValue(), bannerColumnWidth);
	                    	}	
                        	
                        	
                        	if(i==0)
                        	{
                        		startRow = startRow + 4 + filtersRowCount;
                        	}
                        	
                        	/*
                        	List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByMethodology(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                    	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor, isLatestCost);
                    	    */		
                        	
                        }
                        	//List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByMethodology(new Integer(budgetYearsArr[i]),currencyId,filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                        	
                        	//Map<String, List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByMethodologyMap(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
	                    	Map<String, List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByMethodology(budgetYearsArr, currencyId, filter, isLatestCost, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                          	
	                    	if(spendByReportMap!=null && spendByReportMap.size()>0)
                        	{
                        		for(int i=0;i<budgetYearsArr.length;i++)
                                {
	                        		calculateTotalCostBY(spendByReportMap, isQPR1SpendFor, 
	                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                                }
                        	}
	                    	// This is done in case there are no records for the budget location with which the user is associated with 
                        	else
                        	{
                        		for(int i=0;i<budgetYearsArr.length;i++)
                                {
                        			calculateTotalCostNoBY(spendByReportMap, isQPR1SpendFor, 
	                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                                }
                        	}
	                    	
	                    	if(spendByReportMap!=null && spendByReportMap.size()>0)
                          	{
                          		//calculateTotalCost(spendByReportMap, isQPR1SpendFor, 
                        	    	//	isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
                          		
                          		HSSFRow dataRow = null;
                      			HSSFCell projectCodeDataRowColumn = null;
                          		for(String  methKey:spendByReportMap.keySet() )
                          		{
  	                        		
                          			String meth = ""; 
                  					try
                  					{
                  						meth =	methKey.split("~")[0];
                  					}
                  					catch(Exception e)
                  					{
                  						
                  					}
                          			String methGroup = ""; 
                          			try
                  					{
                          				methGroup =	methKey.split("~")[1];
                  					}
                  					catch(Exception e)
                  					{
                  						
                  					}	
                          					
                        	
  	                        				 dataStartColumn = 0;
		                        			 dataRow = sheet.createRow(++startRow);
		                        			// previousYearExist = true;
			                    		
		                        		/*	HSSFCell projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			                                projectCodeDataRowColumn.setCellValue(" ");
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn1Style);
			                                dataStartColumn++;
			                                
			                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                projectCodeDataRowColumn.setCellValue(" ");
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                dataStartColumn++;
			                             */
		                        			
		                        			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                        			sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                
			                               /* String met = spendByBean.getMethodologies();
			                                String methGroup = "";
			                                if(met!=null)
			                                {
			                                	if(met.contains(","))
			                                	{
			                                		
			                                		methGroup = SynchroGlobal.getMethodologyGroupName(new Long (met.split(",")[0]));
			                                	}
			                                	else
			                                	{
			                                	
			                                		
			                                		methGroup = SynchroGlobal.getMethodologyGroupName(new Long (met));
			                                	}
			                                }
			                                */
			                                projectCodeDataRowColumn.setCellValue(methGroup);
			                                
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                               
			                                if(StringUtils.isBlank(methGroup))
			                                {
				                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			    	                            projectCodeDataRowColumn.setCellValue("-");
			    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                }
			                                dataStartColumn++;
		                        			 
		                        			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                        			sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
			                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
			                                if(meth!=null)
			                                {
			                                	projectCodeDataRowColumn.setCellValue(SynchroDAOUtil.getMethodologyNames(meth));
			                                }
			                                else
			                                {
			                                	//projectCodeDataRowColumn.setCellValue(" ");
			                                	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
			    	                             projectCodeDataRowColumn.setCellValue("-");
			    	                             projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
			                                }
			                              
			                                dataStartColumn++;
			                                
			                                
			                               // int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
			                                
			                                int noOfColumnsToDecorate = 0;
			                                if(isLatestCost)
			                                {
			                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
			                                }
			                                else
			                                {
			                                	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length);
			                                }
			                                
			                                int decCol = dataStartColumn;
		                                	for(int k=0;k<noOfColumnsToDecorate;k++)
		                                	{
		                                		projectCodeDataRowColumn = dataRow.createCell(decCol);
		                                		sheet.setColumnWidth(decCol, bannerColumnWidth);
		    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
		    	                                projectCodeDataRowColumn.setCellValue("-");
		    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		    	                                decCol++;
		                                	}
  	                        		
		                                	int snapShotRef = 0;
		                                	for(SpendByReportBean spendByBean:spendByReportMap.get(methKey))
					                      	{
  	                        		boolean isLatesCostNA = true;
  	                        		
  	                        		if(isCOPLASpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getCoplaTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getCoplaTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getCoplaTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("1"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	//projectCodeDataRowColumn.setCellValue("-");*/
		                                	
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isQPR1SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr1TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr1TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr1TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("2"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                if(isQPR2SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr2TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr2TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr2TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("3"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
//		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isQPR3SpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getQpr3TotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr3TotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr3TotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("4"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                if(isFullYearSpendFor)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getFullYearTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getFullYearTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getFullYearTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                	isLatesCostNA = false;
		                                }
		                                else
		                                {
		                                	/*
		                                	
		                                	if(snapShotMap!=null && snapShotMap.get(spendByBean.getBudgetYear())!=null 
		                                			&& snapShotMap.get(spendByBean.getBudgetYear()).get(new Integer("5"))!=null)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                		isLatesCostNA = false;
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                                if(isLatestCost)
	                                {
		                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, bannerColumnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                if(spendByBean.getLatestTotalCost()!=null)
		                                {
		                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getLatestTotalCost().doubleValue());
		                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getLatestTotalCost(), projectCodeDataRowColumn,costFormatStyle, costDecimalFormatStyle );
		                                }
		                                else
		                                {
		                                	/*if(isLatesCostNA)
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
			                                	projectCodeDataRowColumn.setCellValue("-");
			                                	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}*/
		                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("NA");
		                                	}
		                                	else
		                                	{
		                                		projectCodeDataRowColumn.setCellValue("-");
		                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                                	}
		                                }
		                                //projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                dataStartColumn++;
		                                snapShotRef++;
	                                }
	                                
	                           
	                                
                        		
                        	/*	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                        	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor);*/	
                        	}
                        	budgetYearColumn = dataStartColumn;
                          		}
                        	}
	                    
	                    // This is for merging the top row
	                    if(dataStartColumn > 0)
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(dataStartColumn -1)));
	                    }
	                    else
	                    {
	                    	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(totalCosts.size() + 1)));
	                    }
	                    
	                    HSSFRow dataRow = sheet.createRow(++startRow);
	                    HSSFCell totalCostColumn = dataRow.createCell(0);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue("Total Cost");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                    
	                    totalCostColumn = dataRow.createCell(1);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
	                    
                        
                        
	                    sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,1));
	                    
	                    
	                  /*  totalCostColumn = dataRow.createCell(1);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
	                    totalCostColumn = dataRow.createCell(2);
	                    totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                    totalCostColumn.setCellValue(" ");
	                    totalCostColumn.setCellStyle(dataRowColumn2Style);
                        
                        sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,2));
                        */
                        int totalCostColumnIndex = 2;
                        for(int i=0;i<totalCosts.size();i++)
                        {
                        	totalCostColumn = dataRow.createCell(totalCostColumnIndex);
                        	totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                        	//totalCostColumn.setCellValue(totalCosts.get(i).doubleValue());
                        	//totalCostColumn = SynchroUtils.populateCost(totalCosts.get(i), totalCostColumn,costFormatStyle );
                        	totalCostColumn = SynchroUtils.populateSpendByProjectTotalCost(totalCosts.get(i), totalCostColumn,costFormatStyle, costDecimalFormatStyle );
                        	totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
                        	totalCostColumnIndex++;
                        	sheet.autoSizeColumn(totalCostColumnIndex);
                        }
                    
                        //Notes Region
                        
                        if(dataStartColumn > 0)
                        {
	                    	sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+11,0,dataStartColumn -1));
	                    	dataRow = sheet.createRow(startRow+5);
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence,it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                        else
                        {
                        	sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+11,0,(3+totalCosts.size() - 1)));
	                    	dataRow = sheet.createRow(startRow+5);
	                    	
	                    	StringBuilder notes = new StringBuilder();
	                    	notes.append("Notes:").append("\n");
	                    	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
	                    	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
	                    	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence,it is not available.").append("\n");
	                    	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
	                    	notes.append("- Cancelled projects are not included in the above report.").append("\n");
	                    	HSSFCell notesColumn = dataRow.createCell(0);
	                    	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                    	notesColumn.setCellValue(notes.toString());
	                    	notesColumn.setCellStyle(notesStyle);
                        }
                    }

                   
                    
                
                    workbook = SynchroUtils.createExcelImage(workbook, sheet);
                }
                }
            }
        }

        return workbook;
    }
    
    private HSSFWorkbook generateCrossTabReport(HSSFWorkbook workbook, final SpendReportExtractFilter filter, Long snapShotId, boolean latestCost, Integer budgetYear) throws UnsupportedEncodingException, IOException
    {


        
    	List<Long> snapShotIds = new ArrayList<Long>();
    	Map<Integer, Map<Integer, Long>> snapShotMap = new HashMap<Integer, Map<Integer, Long>>();
    	
    	QPRSnapshot qprSnapshot = null;
    	
    	String years = "";
    	String spendForSnapshot = "";
    	
    	if(budgetYear!=null && budgetYear > 0)
    	{
    		years = budgetYear+"";
    	}
    	else
    	{
    		qprSnapshot = qprSnapshotManager.getSnapshot(snapShotId);
        	
        	years = qprSnapshot.getBudgetYear()+"";
        	spendForSnapshot = qprSnapshot.getSpendFor()+"";
    	}
    	
    	
    	/*QPRSnapshot qprSnapshot = qprSnapshotManager.getSnapshot(snapShotId);
    	
    	String years = qprSnapshot.getBudgetYear()+"";
    	String spendForSnapshot = qprSnapshot.getSpendFor()+"";
    	*/
    	
    	if(latestCost)
    	{
    		spendForSnapshot = "6";
    	}
    	Integer currencyId = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1);
			
		HashMap<Integer, Long> sMap = new HashMap<Integer, Long>();
		if(snapShotId!=null && snapShotId.intValue() > 0)
		{
			snapShotIds.add(snapShotId);
			
			sMap.put(qprSnapshot.getSpendFor(), snapShotId);
		}
		
		if(qprSnapshot!=null)
		{
	
			snapShotMap.put(qprSnapshot.getBudgetYear(),sMap );
		}
    	
		String reportTypes = SynchroGlobal.SpendReportTypeNew.SPEND_BY_PROJECTS.getId() + "," + SynchroGlobal.SpendReportTypeNew.SPEND_BY_BUDGET_LOCATION.getId() +"," + SynchroGlobal.SpendReportTypeNew.SPEND_BY_METHODOLOGY.getId()+"," + SynchroGlobal.SpendReportTypeNew.SPEND_BY_BRANDED_NON_BRANDED.getId()+"," + SynchroGlobal.SpendReportTypeNew.SPEND_BY_CATEGORY.getId();
    	
		if(reportTypes != null && !reportTypes.equals("")) {


            String [] reportTypesStrArr = reportTypes.split(",");
            CurrencyExchangeRate currencyExchangeRate = getUserCurrencyExchangeRate(getUser());
            Integer startColumn = 0;
            Integer startRow = 0;
            boolean showProjectCodeColumn = false;


            if(reportTypesStrArr != null && reportTypesStrArr.length > 0) {

                HSSFDataFormat df = workbook.createDataFormat();
                short currencyDataFormatIndex = df.getFormat("#,###0.0000");

           

                // Header Styles start
                HSSFFont sheetHeaderFont = workbook.createFont();
                sheetHeaderFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

                HSSFFont notesFont = workbook.createFont();
                notesFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                notesFont.setItalic(true);

                HSSFFont italicFont = workbook.createFont();
                italicFont.setItalic(true);

                HSSFCellStyle sheetHeaderCellStyle = workbook.createCellStyle();
                sheetHeaderCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                sheetHeaderCellStyle.setFont(sheetHeaderFont);
                sheetHeaderCellStyle.setWrapText(true);
                sheetHeaderCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                // Header Styles end


                // Table Header Column Styles start

                // Table header column1 style
                HSSFCellStyle tableHeaderProjectCodeColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THICK);
                tableHeaderProjectCodeColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                HSSFCellStyle tableHeaderProjectNameColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THIN);
                tableHeaderProjectNameColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                // Table header column1 style
                HSSFCellStyle tableHeaderColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM);
                tableHeaderColumn1Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                
                tableHeaderColumn1Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                tableHeaderColumn1Style.setAlignment(CellStyle.ALIGN_CENTER);
                tableHeaderColumn1Style.setWrapText(true);
            	
                // Table header column2 style
                HSSFCellStyle tableHeaderColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn2Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                
                tableHeaderColumn2Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                tableHeaderColumn2Style.setAlignment(CellStyle.ALIGN_CENTER);
                tableHeaderColumn2Style.setWrapText(true);
            	

                // Table header column3 style
                HSSFCellStyle tableHeaderColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM);
                tableHeaderColumn3Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                
                tableHeaderColumn3Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                tableHeaderColumn3Style.setAlignment(CellStyle.ALIGN_CENTER);
                tableHeaderColumn3Style.setWrapText(true);

                // Table header column4 style
                HSSFCellStyle tableHeaderColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn4Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                // Table header column4 style
                HSSFCellStyle tableHeaderColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);
                tableHeaderColumn5Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                // Table Header Column Styles end

                // Data Row cell styles start
                HSSFCellStyle projectCodeDataRowColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_THICK);
                projectCodeDataRowColumnStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);

                HSSFCellStyle projectNameDataRowColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_THIN);
                projectNameDataRowColumnStyle.setWrapText(true);

                HSSFCellStyle dataRowColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THICK);
                dataRowColumn1Style.setWrapText(true);
                dataRowColumn1Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                dataRowColumn1Style.setAlignment(CellStyle.ALIGN_CENTER);
                
                
             // Total cost row styles start
                HSSFCellStyle totalCostRowProjectCodeColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                totalCostRowProjectCodeColumnStyle.setWrapText(true);
                totalCostRowProjectCodeColumnStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
                totalCostRowProjectCodeColumnStyle.setAlignment(CellStyle.ALIGN_CENTER);
                totalCostRowProjectCodeColumnStyle.setFont(sheetHeaderFont);

                HSSFCellStyle dataRowColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_NONE);
                
                dataRowColumn2Style.setWrapText(true);


                
                HSSFCellStyle dataRowColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN, HSSFCellStyle.BORDER_THIN);
                dataRowColumn3Style.setAlignment(CellStyle.ALIGN_CENTER);

                HSSFCellStyle dataRowColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle dataRowColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_NONE,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_NONE);
                // Data Row cell styles end

                

                HSSFCellStyle totalCostRowTotalColumnStyle = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THIN);
                totalCostRowTotalColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                HSSFCellStyle totalCostRowColumn1Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_THICK);
                totalCostRowColumn1Style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

                HSSFCellStyle totalCostRowColumn2Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn3Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                
                HSSFCellStyle totalCostColumn4Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_NONE, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle totalCostColumn5Style = getCellStyle(workbook, HSSFCellStyle.BORDER_MEDIUM,
                        HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_MEDIUM, HSSFCellStyle.BORDER_NONE);

                HSSFCellStyle notesStyle = workbook.createCellStyle();
                notesStyle.setWrapText(true);
                notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
                
                notesStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
	            notesStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		    	
	            notesStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		        
	            notesStyle.setAlignment(CellStyle.ALIGN_LEFT);
		    	notesStyle.setWrapText(true);
                
               
                
                HSSFDataFormat cf = workbook.createDataFormat();
                short currencyDataFormatIndexString = cf.getFormat("#,###0");
     	       //short currencyDataFormatIndexString = cf.getFormat("@");
                
                HSSFCellStyle costFormatStyle = workbook.createCellStyle();
                costFormatStyle.setDataFormat(currencyDataFormatIndexString);
                
                short currencyDecimalDataFormatIndex = cf.getFormat("#,###0.00");
                HSSFCellStyle costDecimalFormatStyle = workbook.createCellStyle();
                costDecimalFormatStyle.setDataFormat(currencyDecimalDataFormatIndex);
              

                boolean isSpendByProject = false;
                boolean isSpendByBudgetLocation=false;
                boolean isSpendByBrand=false;
                boolean isSpendByMethodology=false;
                boolean isSpendByAgency=false;
                
                boolean isSpendByKantarNonKantar=false;
                
                boolean isSpendByCategoryType=false;
                
                for(String reportTypeStr : reportTypesStrArr)
                {
                	Integer reportType = Integer.parseInt(reportTypeStr);
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_BUDGET_LOCATION.getId())) 
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_BUDGET_LOCATION.getName());
                		isSpendByBudgetLocation=true;
                	}
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_METHODOLOGY.getId()))
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_METHODOLOGY.getName());
                		isSpendByMethodology=true;
                	}
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_BRANDED_NON_BRANDED.getId())) 
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_BRANDED_NON_BRANDED.getName());
                		isSpendByBrand=true;
                	}
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_AGENCY.getId())) 
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_AGENCY.getName());
                		isSpendByAgency=true;
                	}
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_KANTAR_NONKANTAR.getId())) 
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_KANTAR_NONKANTAR.getName());
                		isSpendByKantarNonKantar=true;
                	}
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_PROJECTS.getId())) 
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_PROJECTS.getName());
                		isSpendByProject=true;
                	}
                	
                	if(reportType.equals(SynchroGlobal.SpendReportTypeNew.SPEND_BY_CATEGORY.getId())) 
                	{
                		selectedCrossTabSpends.add(SynchroGlobal.SpendReportTypeNew.SPEND_BY_CATEGORY.getName());
                		isSpendByCategoryType=true;
                	}
                }
                
                HSSFSheet sheet = workbook.createSheet("CrossTab");
                StringBuilder generatedBy = new StringBuilder();
                generatedBy.append("\"").append(Joiner.on(",").join(selectedCrossTabSpends)).append("\"").append("\n");
                generatedBy.append(" ( ").append(currencyExchangeRate.getCurrencyCode()).append(" )").append(" Budget Year - ").append(years).append("\n");
                String userName = getUser().getName();
                generatedBy.append("\"").append(userName).append(", generated on ").append(timeStamp).append("\"").append("\n");
              //  generatedBy.append("\"").append("Filters:").append(Joiner.on(",").join(selectedFilters)).append("\"").append("\n");
                
                HSSFRow reportTypeHeader = sheet.createRow(startRow);
                HSSFCell reportTypeHeaderColumn = reportTypeHeader.createCell(startColumn);
                reportTypeHeaderColumn.setCellStyle(sheetHeaderCellStyle);
                reportTypeHeaderColumn.setCellValue(generatedBy.toString());
                
                // Create sheet row2
                HSSFRow userNameHeader = sheet.createRow(startRow + 1);
                HSSFCell userNameHeaderColumn = userNameHeader.createCell(startColumn);
                userNameHeaderColumn.setCellStyle(sheetHeaderCellStyle);
              //  userNameHeaderColumn.setCellValue("User: " + getUser().getFirstName() + " " + getUser().getLastName());

                Integer filtersRowCount = 0;

                if(filter != null && filter.getYears() != null && filter.getYears().size() > 0) {
                    HSSFRow yearsFilterRow = sheet.createRow(startRow + 2);
                    HSSFCell yearsFilterRowCell = yearsFilterRow.createCell(startColumn);
                    yearsFilterRowCell.setCellStyle(sheetHeaderCellStyle);
                //    yearsFilterRowCell.setCellValue("Year(s): "+StringUtils.join(filter.getYears(),", "));
                    filtersRowCount++;
                }


                
            	// Table Header row
                HSSFRow tableBudgetYearHeaderRow = sheet.createRow(startRow + 3 + filtersRowCount);
                String[] budgetYearsArr = years.split(",");
              //  String[] spendForSnapshotArr = spendForSnapshot.split(",");
                
                String[] spendForSnapshotArrOriginal = spendForSnapshot.split(",");
                
                List<String> spendForSnapShotList = new ArrayList<String>();
                
                boolean showLatestCost = false;
              
                
                for(int z=0;z<spendForSnapshotArrOriginal.length;z++)
                {
                	if(!spendForSnapshotArrOriginal[z].equals("6"))
                	{
                		spendForSnapShotList.add(spendForSnapshotArrOriginal[z]);
                	}
                   	else
                	{
                		showLatestCost = true;
                	}
                }
                //String[] spendForSnapshotArr = (String[])spendForSnapShotList.toArray();
                String [] spendForSnapshotArr = spendForSnapShotList.toArray(new String[spendForSnapShotList.size()]);
                
                int cellIndex = 0;
                
               // sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),3,5));
               // sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),6,9));
                
                int spendByColumns = 0;
                int totalCostColumns = 0;
                
                
                if(isSpendByBudgetLocation)
                {
                	spendByColumns=4;
                }
                if(isSpendByProject)
                {
                	spendByColumns=spendByColumns+2;
                }
                if(isSpendByMethodology)
                {
                	spendByColumns=spendByColumns+2;
                }
                if(isSpendByBrand)
                {
                	spendByColumns=spendByColumns+2;
                }
                if(isSpendByAgency || isSpendByKantarNonKantar)
                {
                	spendByColumns=spendByColumns+2;
                }
                else if(isSpendByKantarNonKantar)
                {
                	if(isSpendByBudgetLocation || isSpendByMethodology || isSpendByBrand)
                	{
                		
                	}
                	else
                	{
                		spendByColumns=spendByColumns+1;
                	}
                }
                if(isSpendByCategoryType)
                {
                	spendByColumns=spendByColumns+1;
                }
                
                cellIndex = spendByColumns;
                totalCostColumns = spendByColumns;
            	
                //int firstRowColumns = spendByColumns + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                
                int firstRowColumns = 0;
                if(showLatestCost)
                {
                	firstRowColumns = spendByColumns + (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                }
                else
                {
                	firstRowColumns = spendByColumns + (budgetYearsArr.length) * (spendForSnapshotArr.length);
                }
                
            	for(int m=0;m<firstRowColumns;m++)
            	{
            		HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(m);
            		budgetYearColumn0.setCellType(HSSFCell.CELL_TYPE_STRING);
            		budgetYearColumn0.setCellValue(" ");
            		budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
            	}
        	
            	 totalCosts=new ArrayList<BigDecimal>();
                
                for(int i=0;i<budgetYearsArr.length;i++)
                {
                    
                	
                	HSSFCell budgetYearColumn0 = tableBudgetYearHeaderRow.createCell(cellIndex);
                    budgetYearColumn0.setCellValue(budgetYearsArr[i]);
                    budgetYearColumn0.setCellStyle(tableHeaderColumn1Style);
                 
                    if(showLatestCost)
                    {
                    	if(spendForSnapshotArr.length > 1)
                    	{
                    		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+spendForSnapshotArr.length)));
                    	}
                    }
                    else
                    {
                    	if(spendForSnapshotArr.length > 1)
                    	{
                    		sheet.addMergedRegion(new CellRangeAddress((startRow + 3 + filtersRowCount),(startRow + 3 + filtersRowCount),cellIndex,(cellIndex+(spendForSnapshotArr.length-1))));
                    	}
                    }
                    
                    if(showLatestCost)
                    {
                    	cellIndex = cellIndex+spendForSnapshotArr.length+1;
                    }
                    else
                    {
                    	cellIndex = cellIndex+spendForSnapshotArr.length;
                    }
                    
                }
                
                
                HSSFRow tableHeaderRow = sheet.createRow(startRow + 4 + filtersRowCount);
                
                tableHeaderRow.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
                int initialColumnIndex = 0;
            	
                if(isSpendByProject)
                {
                	HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
 	                tableHeaderColumn0.setCellValue("Project Code");
 	                tableHeaderColumn0.setCellStyle(tableHeaderColumn1Style);
 	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
 	                initialColumnIndex++;
 	                
 	               HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(++startColumn);
                   tableHeaderColumn1.setCellValue("Project Name");
                   tableHeaderColumn1.setCellStyle(tableHeaderColumn2Style);
                  // sheet.autoSizeColumn(startColumn);
                   sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                }
                
                
                
                if(isSpendByBudgetLocation)
                {
	                
                	HSSFCell tableHeaderColumn0 = null;
                	if(startColumn==0 && initialColumnIndex==0)
	                {
                		tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
                		tableHeaderColumn0.setCellStyle(tableHeaderColumn1Style);
                		initialColumnIndex++;
	                }
	                else
	                {
	                	tableHeaderColumn0 = tableHeaderRow.createCell(++startColumn);
	                	tableHeaderColumn0.setCellStyle(tableHeaderColumn2Style);
	                }
                	
                	//HSSFCell tableHeaderColumn0 = tableHeaderRow.createCell(startColumn);
	                tableHeaderColumn0.setCellValue("Region");
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                
	              
	                HSSFCell tableHeaderColumn1 = tableHeaderRow.createCell(++startColumn);
	                tableHeaderColumn1.setCellValue("Area");
	                tableHeaderColumn1.setCellStyle(tableHeaderColumn2Style);
	               // sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                
	                HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(++startColumn);
	                tableHeaderColumn2.setCellValue("T20, T40, or non-T40");
	                tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	
	                HSSFCell tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                tableHeaderColumn3.setCellValue("Budget Location");
	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                }
                
                
                if(isSpendByMethodology)
                {
                	HSSFCell tableHeaderColumn3 = null;
                	if(startColumn==0 && initialColumnIndex==0)
	                {
                		tableHeaderColumn3 = tableHeaderRow.createCell(startColumn);
                		initialColumnIndex++;
	                }
	                else
	                {
	                	tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                }
                	
	                tableHeaderColumn3.setCellValue("Methodology Group");
	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                
	                HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(++startColumn);
	                tableHeaderColumn2.setCellValue("Methodology");
	                tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                }
                
                if(isSpendByBrand)
                {
	               // HSSFCell tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
                	HSSFCell tableHeaderColumn3 = null;
                	if(startColumn==0 && initialColumnIndex==0)
	                {
                		tableHeaderColumn3 = tableHeaderRow.createCell(startColumn);
                		initialColumnIndex++;
	                }
	                else
	                {
	                	tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                }
	                tableHeaderColumn3.setCellValue("Brand Type");
	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                
	                HSSFCell tableHeaderColumn2 = tableHeaderRow.createCell(++startColumn);
	                tableHeaderColumn2.setCellValue("Brand");
	                tableHeaderColumn2.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                }
                
                if(isSpendByAgency || isSpendByKantarNonKantar)
                {
	                //HSSFCell tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
                	HSSFCell tableHeaderColumn3 = null;
                	if(startColumn==0 && initialColumnIndex==0)
	                {
                		tableHeaderColumn3 = tableHeaderRow.createCell(startColumn);
                		initialColumnIndex++;
	                }
	                else
	                {
	                	tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                }
	                tableHeaderColumn3.setCellValue("Agency");
	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                
	                tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                tableHeaderColumn3.setCellValue("Kantar/Non-Kantar");
	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                }
                else if (isSpendByKantarNonKantar)
                {
                	// If Spend By Budget Location, Spend By Methodology, Spend By Brand is selected for Crosstab then wont add the Spend By Kantar Non Kantar fields 
                	if(isSpendByBudgetLocation || isSpendByMethodology || isSpendByBrand)
                	{
                		
                	}
                	else
                	{
	                	HSSFCell tableHeaderColumn3 = null;
	                	if(startColumn==0 && initialColumnIndex==0)
		                {
	                		tableHeaderColumn3 = tableHeaderRow.createCell(startColumn);
	                		initialColumnIndex++;
		                }
		                else
		                {
		                	tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
		                }
	                	tableHeaderColumn3.setCellValue("Kantar/Non-Kantar");
	 	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	 	             //   sheet.autoSizeColumn(startColumn);
	 	               sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                }
                
                
                if(isSpendByCategoryType)
                {
	               
                	HSSFCell tableHeaderColumn3 = null;
                	if(startColumn==0 && initialColumnIndex==0)
	                {
                		tableHeaderColumn3 = tableHeaderRow.createCell(startColumn);
                		initialColumnIndex++;
	                }
	                else
	                {
	                	tableHeaderColumn3 = tableHeaderRow.createCell(++startColumn);
	                }
	                tableHeaderColumn3.setCellValue("Category Type");
	                tableHeaderColumn3.setCellStyle(tableHeaderColumn2Style);
	                //sheet.autoSizeColumn(startColumn);
	                sheet.setColumnWidth(startColumn.intValue(), columnWidth);
	                
	               
                }
                
                HSSFCell costColumns;
                
                int budgetYearColumn=0;
                int dataStartColumn = 0;
                
                boolean isQPR1SpendFor=false;
                boolean isQPR2SpendFor=false;
                boolean isQPR3SpendFor=false;
                boolean isFullYearSpendFor=false;
                boolean isCOPLASpendFor=false;
                boolean isLatestCost=false;
                
                for(int i=0;i<budgetYearsArr.length;i++)
                {
                   
                	isQPR1SpendFor=false;
                    isQPR2SpendFor=false;
                    isQPR3SpendFor=false;
                    isFullYearSpendFor=false;
                    isCOPLASpendFor=false;
                    isLatestCost = false;
                    
                	for(int j=0;j<spendForSnapshotArrOriginal.length;j++)
                    {
                    	if(spendForSnapshotArrOriginal[j].equals("1"))
                    	{
                    		isCOPLASpendFor=true;
                    	}
                    	if(spendForSnapshotArrOriginal[j].equals("2"))
                    	{
                    		isQPR1SpendFor=true;
                    	}
                    	if(spendForSnapshotArrOriginal[j].equals("3"))
                    	{
                    		isQPR2SpendFor=true;
                    	}
                    	if(spendForSnapshotArrOriginal[j].equals("4"))
                    	{
                    		isQPR3SpendFor=true;
                    	}
                    	if(spendForSnapshotArrOriginal[j].equals("5"))
                    	{
                    		isFullYearSpendFor=true;
                    	}
                    	if(spendForSnapshotArrOriginal[j].equals("6"))
                    	{
                    		isLatestCost=true;
                    	}
                    	
                		
                    }
                	
                	
                	
                	if(isCOPLASpendFor)
                	{
                    	costColumns = tableHeaderRow.createCell(++startColumn);
                    	costColumns.setCellValue("COPLA");
                    	costColumns.setCellStyle(tableHeaderColumn2Style);
                    	//sheet.autoSizeColumn(startColumn);
                    	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                	if(isQPR1SpendFor)
                	{
                    	costColumns = tableHeaderRow.createCell(++startColumn);
                    	costColumns.setCellValue("QPR1");
                    	costColumns.setCellStyle(tableHeaderColumn2Style);
                    	//sheet.autoSizeColumn(startColumn);
                    	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                	if(isQPR2SpendFor)
                	{
                    	costColumns = tableHeaderRow.createCell(++startColumn);
                    	costColumns.setCellValue("QPR2");
                    	costColumns.setCellStyle(tableHeaderColumn2Style);
                    	//sheet.autoSizeColumn(startColumn);
                    	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                	if(isQPR3SpendFor)
                	{
                    	costColumns = tableHeaderRow.createCell(++startColumn);
                    	costColumns.setCellValue("QPR3");
                    	costColumns.setCellStyle(tableHeaderColumn2Style);
                    	//sheet.autoSizeColumn(startColumn);
                    	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                	if(isFullYearSpendFor)
                	{
                    	costColumns = tableHeaderRow.createCell(++startColumn);
                    	costColumns.setCellValue("FULL YEAR");
                    	costColumns.setCellStyle(tableHeaderColumn2Style);
                    	//sheet.autoSizeColumn(startColumn);
                    	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                	
                	// Latest Cost will be shown only if it selected in UI
                	if(isLatestCost)
                	{
	                    costColumns = tableHeaderRow.createCell(++startColumn);
	                	costColumns.setCellValue("LATEST COST");
	                	costColumns.setCellStyle(tableHeaderColumn3Style);
	                	//sheet.autoSizeColumn(startColumn);
	                	 sheet.setColumnWidth(startColumn.intValue(), columnWidth);
                	}
                	
                	
                	
                	if(i==0)
                	{
                		startRow = startRow + 4 + filtersRowCount;
                	}
                	
                	//List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByCrossTab(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost,isSpendByProject, isSpendByBudgetLocation,isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                	//calculateTotaCost(spendByReportList, isQPR1SpendFor, 
            	    	//	isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
                	
                }
                    
                	//List<SpendByReportBean> spendByReportList = qprSnapshotManager.getSpendByCrossTab(new Integer(budgetYearsArr[i]), currencyId,filter, isLatestCost,isSpendByProject, isSpendByBudgetLocation,isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                	Map<String, List<SpendByReportBean>> spendByReportMap = qprSnapshotManager.getSpendByCrossTab(budgetYearsArr, currencyId,filter, isLatestCost,isSpendByProject, isSpendByBudgetLocation,isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType, isCOPLASpendFor, isQPR1SpendFor, isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor);
                	
                	
                	if(spendByReportMap!=null && spendByReportMap.size()>0)
                	{
                		for(int i=0;i<budgetYearsArr.length;i++)
                        {
                    		calculateTotalCostBY(spendByReportMap, isQPR1SpendFor, 
                    	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                        }
                	}
                	// This is done in case there are no records for the budget location with which the user is associated with 
                	else
                	{
                		for(int i=0;i<budgetYearsArr.length;i++)
                        {
                			calculateTotalCostNoBY(spendByReportMap, isQPR1SpendFor, 
                    	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost, new Integer(budgetYearsArr[i]));
                        }
                	}
                	
                	if(spendByReportMap!=null && spendByReportMap.size()>0)
                  	{
                		//calculateTotalCost(spendByReportMap, isQPR1SpendFor, 
                    	  //  		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor,isLatestCost);
                		
                		HSSFRow dataRow = null;
              			HSSFCell projectCodeDataRowColumn = null;
              			
              			for(String  spendByReportKey :spendByReportMap.keySet() )
                  		{
                			
                			dataStartColumn = 0;
                			dataRow = sheet.createRow(++startRow);
                			
                			
                			
                			Long projectId= null;
                      		String projectName = "";
                			Integer budgetLocation = null;
                			
                			String regionName = "";
              				String areaName = "";
              				String t20_40_Name = "";
              				String brandName = "";
                			String brandType  = "";
                			String categoryName = "";
                			String agencyName="";
                			String agencyType="";
                			
              				
                			
              				try
                			{
              					
              					projectId = new Long(SpendReportsActionNew.getFieldName(spendByReportKey, "projectId", null, null, null, null, null,  null, null, null,  null, null, null,null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType));
                			}
                			catch(Exception e)
                			{
                				
                			}
                			try
                			{
                				projectName = SpendReportsActionNew.getFieldName(spendByReportKey, null, "projectName", null, null, null, null,null, null , null,  null, null, null,null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
                			}
                			catch(Exception e)
                			{
                				
                			}
                			
              				try
                			{
                				budgetLocation = new Integer(SpendReportsActionNew.getFieldName(spendByReportKey, null, null, "budgetLocation", null, null, null,null, null , null,  null, null, null,null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType));
                			}
                			catch(Exception e)
                			{
                				
                			}
              				try
              				{
                				 regionName = SpendReportsActionNew.getFieldName(spendByReportKey, null, null, null, "region", null, null,null, null , null,  null, null, null,null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
              				}
              				catch(Exception e)
              				{
              					
              				}
              				try
              				{
              					areaName = SpendReportsActionNew.getFieldName(spendByReportKey, null, null, null, null, "area",null, null, null , null,  null, null, null,null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
              				}
              				catch(Exception e)
              				{
              					
              				}
              				try
              				{
              					t20_40_Name = SpendReportsActionNew.getFieldName(spendByReportKey, null, null, null, null, null ,"t20_40", null, null , null,  null, null, null, null,isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
              				}
              				catch(Exception e)
              				{
              					
              				}
                			
              				String meth = ""; 
          					try
          					{
          						meth =	SpendReportsActionNew.getFieldName(spendByReportKey, null, null, null, null, null , null, "methodology", null , null,  null, null, null,null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
          					}
          					catch(Exception e)
          					{
          						
          					}
                  			String methGroup = ""; 
                  			try
          					{
                  				methGroup = SpendReportsActionNew.getFieldName(spendByReportKey, null, null, null, null, null , null,  null, "methGroup" , null,  null, null, null, null,isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
          					}
          					catch(Exception e)
          					{
          						
          					}	
                  			try
          					{
                  				 brandName = SpendReportsActionNew.getFieldName(spendByReportKey, null, null, null, null, null , null,  null,  null, "brand" , null, null, null,null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
          					}
          					catch(Exception e)
          					{
          						
          					}	
                  			try
          					{
                  				brandType  = SpendReportsActionNew.getFieldName(spendByReportKey, null, null, null, null, null , null,  null, null , null,  "brandType", null, null, null, isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
          					}
          					catch(Exception e)
          					{
          						
          					}	
                  			
                  			try
              				{
                				 categoryName = SynchroDAOUtil.getCategoryNames(SpendReportsActionNew.getFieldName(spendByReportKey, null, null, null, null, null , null,  null, null , null,  null, "categoryType", null, null,isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType));
              				}
              				catch(Exception e)
              				{
              					
              				}
                  			
                  			try
              				{
                				//Integer aid = new Integer(spendByReportKey.split("~")[11]);
                  				Integer aid = new Integer(SpendReportsActionNew.getFieldName(spendByReportKey, null, null, null, null, null , null,  null, null , null,  null, null, "agency", null,isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType));
                  				agencyName = SynchroGlobal.getResearchAgency().get(aid);
                  				
                  				agencyType = SpendReportsActionNew.getFieldName(spendByReportKey, null, null, null, null, null , null,  null, null , null,  null, null, null, "agencyType", isSpendByProject, isSpendByBudgetLocation, isSpendByBrand, isSpendByMethodology, isSpendByAgency, isSpendByKantarNonKantar, isSpendByCategoryType);
                  			/*	if(SynchroGlobal.getResearchAgencyGroupFromAgency(aid)!=null && SynchroGlobal.getResearchAgencyGroupFromAgency(aid)==1)
                				{
                  					agencyType = "Kantar";
                				}
                				else if(SynchroGlobal.getResearchAgencyGroupFromAgency(aid)!=null && SynchroGlobal.getResearchAgencyGroupFromAgency(aid)==2)
                				{
                					agencyType = "Non-Kantar";
                				}*/
                				
              				}
              				catch(Exception e)
              				{
              					
              				}
                  			
                			if(isSpendByProject)
                			{
                				projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                            sheet.setColumnWidth(dataStartColumn, columnWidth);
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn1Style);
	                            
	                            if(projectId!=null)
	                            {
	                            	projectCodeDataRowColumn.setCellValue(projectId);
	                            }
	                            else
	                            {
	                            	//projectCodeDataRowColumn.setCellValue(" ");
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
    	                            projectCodeDataRowColumn.setCellValue("-");
    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
	                          
	                         
                                sheet.setColumnWidth(dataStartColumn, columnWidth);
                                dataStartColumn++;
                                
                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
                                
                                
                                if(StringUtils.isNotBlank(projectName))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(projectName);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
                               
                                sheet.setColumnWidth(dataStartColumn, columnWidth);
                                dataStartColumn++;
                			}
                			
                			if(isSpendByBudgetLocation)
                			{
	                			
	                			projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                			sheet.setColumnWidth(dataStartColumn, columnWidth);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                           
	                            
	                            if(StringUtils.isNotBlank(regionName))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(regionName);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
                              
                                dataStartColumn++;
                                
                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                                sheet.setColumnWidth(dataStartColumn, columnWidth);
                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
                               
                                if(StringUtils.isNotBlank(areaName))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(areaName);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
                                
                                dataStartColumn++;
	                            
                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                                sheet.setColumnWidth(dataStartColumn, columnWidth);
                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
                               
                               
                                if(StringUtils.isNotBlank(t20_40_Name))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(t20_40_Name);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
                                dataStartColumn++;
	                            
	                            
                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                    			sheet.setColumnWidth(dataStartColumn, columnWidth);
                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
                                                               
                                if(budgetLocation!=null)
	                            {
	                            	//projectCodeDataRowColumn.setCellValue(budgetLocation);
	                            	projectCodeDataRowColumn.setCellValue(SynchroUtils.getBudgetLocationName(new Integer(budgetLocation)));
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
                                dataStartColumn++;
                			}
                            
                            if(isSpendByMethodology)
                            {
                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            sheet.setColumnWidth(dataStartColumn, columnWidth);
	                           	
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                            if(StringUtils.isNotBlank(methGroup))
	                            {
	                        	   projectCodeDataRowColumn.setCellValue(methGroup);
	                            }
	                            else
	                            {
	                        	   	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
                                
	                           
	                            dataStartColumn++;
                            	
                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                            	sheet.setColumnWidth(dataStartColumn, columnWidth);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                            if(StringUtils.isNotBlank(meth))
                                {
                                	projectCodeDataRowColumn.setCellValue(SynchroDAOUtil.getMethodologyNames(meth));
                                }
                                else
                                {
                                	//projectCodeDataRowColumn.setCellValue(" ");
                                	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
    	                            projectCodeDataRowColumn.setCellValue("-");
    	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
                                }
	                           
	                            dataStartColumn++;
                            }
                            
                            if(isSpendByBrand)
                            {
	                            
                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                            	sheet.setColumnWidth(dataStartColumn, columnWidth);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                           
	                            if(StringUtils.isNotBlank(brandType))
	                           	{
	                           		projectCodeDataRowColumn.setCellValue(brandType);
	                           	}
	                           	else
	                           	{
	                           		projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                           	}
	                          
	                          
	                            dataStartColumn++;
	                            
                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                            	sheet.setColumnWidth(dataStartColumn, columnWidth);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                            if(StringUtils.isNotBlank(brandName))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(brandName);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
	                            
	                          /*  if(spendByBean.getBrandNonBrandName()!=null)
	                            {
	                            	projectCodeDataRowColumn.setCellValue(spendByBean.getBrandNonBrandName());
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellValue("");
	                            }*/
	                            
	                            dataStartColumn++;
                            }
                            
                            if(isSpendByAgency || isSpendByKantarNonKantar)
                            {
	                            projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            sheet.setColumnWidth(dataStartColumn, columnWidth);
	                          
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                            
	                            if(StringUtils.isNotBlank(agencyName))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(agencyName);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
	                            
	                            dataStartColumn++;
	                            
	                            projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            sheet.setColumnWidth(dataStartColumn, columnWidth);
	                           
	                          //  projectCodeDataRowColumn.setCellValue(agencyType);
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                            
	                            if(StringUtils.isNotBlank(agencyType))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(agencyType);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
	                            
	                           
	                            dataStartColumn++;
                            }
                            else if(isSpendByKantarNonKantar)
                            {
                            	if(isSpendByBudgetLocation || isSpendByMethodology || isSpendByBrand)
                            	{
                            		
                            	}
                            	else
                            	{
	                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	 	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	 	                           
	 	                          //  projectCodeDataRowColumn.setCellValue(spendByBean.getResearchAgecnyType());
	 	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	 	                            dataStartColumn++;
                            	}
                            }
                            
                            if(isSpendByCategoryType)
                            {
	                            
                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
                            	sheet.setColumnWidth(dataStartColumn, columnWidth);
	                            projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                            projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                            
	                           // projectCodeDataRowColumn.setCellValue(categoryName);
	                            
	                            if(StringUtils.isNotBlank(categoryName))
	                            {
	                            	projectCodeDataRowColumn.setCellValue(categoryName);
	                            }
	                            else
	                            {
	                            	projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
   	                            	projectCodeDataRowColumn.setCellValue("-");
   	                            	projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                            }
	                            
	                           
	                            dataStartColumn++;
	                        	
                            }
                            
                           /* if(i>0)
                            {
                            	int noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                            	for(int k=0;k<noOfColumnsToDecorate;k++)
                            	{
                            		projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	                                projectCodeDataRowColumn.setCellValue(" ");
	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                                dataStartColumn++;
                            	}
                            	dataStartColumn=budgetYearColumn;
                            }
                           */
                            
                            int noOfColumnsToDecorate = 0;
                            if(isLatestCost)
                            {
                            	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length+1);
                            }
                            else
                            {
                            	noOfColumnsToDecorate = (budgetYearsArr.length) * (spendForSnapshotArr.length);
                            }
                            
                            int decCol = dataStartColumn;
                        	for(int k=0;k<noOfColumnsToDecorate;k++)
                        	{
                        		projectCodeDataRowColumn = dataRow.createCell(decCol);
                        		sheet.setColumnWidth(decCol, columnWidth);
                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                                projectCodeDataRowColumn.setCellValue("-");
                                projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
                                decCol++;
                        	}
                        	int snapShotRef = 0;
                        	
                        	for(SpendByReportBean spendByBean:spendByReportMap.get(spendByReportKey))
		                     {
	                        		
	                            if(isCOPLASpendFor)
	                            {
	                               
	                                
	                                projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                                
	                                if(spendByBean.getCoplaTotalCost()!=null)
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getCoplaTotalCost().doubleValue());
	                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getCoplaTotalCost(), projectCodeDataRowColumn,costFormatStyle,  costDecimalFormatStyle );
	                                }
	                                else
	                                {
	                                	/*
	                                	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
	                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("1"))!=null)
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}*/
	                                	
	                                	
	                                	if((snapShotRef < totalCosts.size()) && totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}
	                                }
	                               
	                                
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                snapShotRef++;
	                            }
	                            
	                            if(isQPR1SpendFor)
	                            {
	                            	 projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                                sheet.setColumnWidth(dataStartColumn, columnWidth);
		                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                
	                                if(spendByBean.getQpr1TotalCost()!=null)
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr1TotalCost().doubleValue());
	                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr1TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
	                                }
	                                else
	                                {
	                                	/*
	                                	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
	                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("2"))!=null)
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}*/
	                                	
	                                	if((snapShotRef < totalCosts.size()) && totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}
	                                }
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                snapShotRef++;
	                            }
	                            if(isQPR2SpendFor)
	                            {
	                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                                
	                                if(spendByBean.getQpr2TotalCost()!=null)
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr2TotalCost().doubleValue());
	                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr2TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
	                                }
	                                else
	                                {
	                                	/*
	                                	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
	                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("3"))!=null)
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}*/
	                                	if((snapShotRef < totalCosts.size()) && totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}
	                                }
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                snapShotRef++;
	                            }
	                            
	                            if(isQPR3SpendFor)
	                            {
	                            	projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	                                
	                                if(spendByBean.getQpr3TotalCost()!=null)
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getQpr3TotalCost().doubleValue());
	                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getQpr3TotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
	                                }
	                                else
	                                {
	                                	/*
	                                	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
	                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("4"))!=null)
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}*/
	                                	/*if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}
	                                	*/
	                                	if((snapShotRef < totalCosts.size()) && totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}
	                                }
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                snapShotRef++;
	                            }
	                            if(isFullYearSpendFor)
	                            {
	                            	 projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                             sheet.setColumnWidth(dataStartColumn, columnWidth);
		                             projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                                
		                             projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                                
	                                if(spendByBean.getFullYearTotalCost()!=null)
	                                {
	                                	//projectCodeDataRowColumn.setCellValue(spendByBean.getFullYearTotalCost().doubleValue());
	                                	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getFullYearTotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
	                                }
	                                else
	                                {
	                                	/*
	                                	if(snapShotMap!=null && snapShotMap.get(new Integer(budgetYearsArr[i]))!=null 
	                                			&& snapShotMap.get(new Integer(budgetYearsArr[i])).get(new Integer("5"))!=null)
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}*/
	                                	
	                                	if((snapShotRef < totalCosts.size()) && totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}
	                                	/*
	                                	if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}*/
	                                }
	                                sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                snapShotRef++;
	                            }
	                            
	                            if(isLatestCost)
                                {
	                            	
                                
	                            	 projectCodeDataRowColumn = dataRow.createCell(dataStartColumn);
		                             sheet.setColumnWidth(dataStartColumn, columnWidth);
		                             projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
		                             projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
		                            if(spendByBean.getLatestTotalCost()!=null)
		                            {
		                            	//projectCodeDataRowColumn.setCellValue(spendByBean.getLatestTotalCost().doubleValue());
		                            	projectCodeDataRowColumn = SynchroUtils.populateCost(spendByBean.getLatestTotalCost(), projectCodeDataRowColumn,costFormatStyle , costDecimalFormatStyle);
		                            }
		                            else
		                            {
		                            	projectCodeDataRowColumn.setCellValue("-");
                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
		                            	/*if(totalCosts.get(snapShotRef)!=null && SynchroUtils.displayCostType(totalCosts.get(snapShotRef)).equals("NA"))
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("NA");
	                                	}
	                                	else
	                                	{
	                                		projectCodeDataRowColumn.setCellValue("-");
	                                		projectCodeDataRowColumn.setCellStyle(dataRowColumn3Style);
	                                	}*/
		                            }
		                            sheet.setColumnWidth(dataStartColumn, columnWidth);
	                                dataStartColumn++;
	                                snapShotRef++;
                                }
	                            
	                            /*if(i==0)
	                            {
	                            	int noOfColumnsToDecorate = (budgetYearsArr.length-1) * (spendForSnapshotArr.length+1);
	                            	if(noOfColumnsToDecorate>0)
	                            	{
	                            		int columnLocation = dataStartColumn;
	                            		for(int k=0;k<noOfColumnsToDecorate;k++)
	                                	{
	                            			projectCodeDataRowColumn = dataRow.createCell(columnLocation);
	    	                                projectCodeDataRowColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
	    	                                projectCodeDataRowColumn.setCellValue(" ");
	    	                                projectCodeDataRowColumn.setCellStyle(dataRowColumn2Style);
	    	                                columnLocation++;
	                                	}
	                            	}
	                            }*/
	                            
	                		}
                	/*	calculateTotaCost(spendByReportList, isQPR1SpendFor, 
                	    		isQPR2SpendFor, isQPR3SpendFor, isFullYearSpendFor,isCOPLASpendFor); */	
                	}
                	budgetYearColumn = dataStartColumn;
                }
                
                // This is for merging the top row
                if(dataStartColumn > 0)
                {
                	sheet.addMergedRegion(new CellRangeAddress(0,3,0,(dataStartColumn -1)));
                }
                
                if(spendByReportMap!=null && spendByReportMap.size()>0)
            	{
            		
            	}
            	// This is done in case there are no records for the budget location with which the user is associated with 
            	else
            	{
            		sheet.addMergedRegion(new CellRangeAddress(0,3,0,(totalCostColumns)));
            	}
             
                HSSFRow dataRow = sheet.createRow(++startRow);
                HSSFCell totalCostColumn = dataRow.createCell(0);
                totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                totalCostColumn.setCellValue("Total Cost");
                totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
               
                for(int l=1;l<totalCostColumns;l++)
                {
                	 totalCostColumn = dataRow.createCell(l);
                     totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                     totalCostColumn.setCellValue(" ");
                     totalCostColumn.setCellStyle(dataRowColumn2Style);
                   /*  totalCostColumn = dataRow.createCell(2);
                     totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                     totalCostColumn.setCellValue(" ");
                     totalCostColumn.setCellStyle(dataRowColumn2Style);
                     totalCostColumn = dataRow.createCell(3);
                     totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                     totalCostColumn.setCellValue(" ");
                     totalCostColumn.setCellStyle(dataRowColumn2Style);*/
                }
                
               
                
                if(totalCostColumns>0)
                {
                	sheet.addMergedRegion(new CellRangeAddress(startRow,startRow,0,totalCostColumns-1));
                }
                
               // int totalCostColumnIndex = 4;
                int totalCostColumnIndex = totalCostColumns;
                for(int i=0;i<totalCosts.size();i++)
                {
                	totalCostColumn = dataRow.createCell(totalCostColumnIndex);
                	totalCostColumn.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                	//totalCostColumn.setCellValue(totalCosts.get(i).doubleValue());
                	//totalCostColumn = SynchroUtils.populateCost(totalCosts.get(i), totalCostColumn,costFormatStyle );
                	
                	totalCostColumn = SynchroUtils.populateSpendByProjectTotalCost(totalCosts.get(i), totalCostColumn,costFormatStyle, costDecimalFormatStyle );
                	totalCostColumn.setCellStyle(totalCostRowProjectCodeColumnStyle);
                	totalCostColumnIndex++;
                	sheet.autoSizeColumn(totalCostColumnIndex);
                	
                }
            
                //Notes Region
                
                if(dataStartColumn > 0)
                {
                	if(isSpendByAgency || isSpendByKantarNonKantar)
                	{
                		sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+12,0,dataStartColumn -1));
                	}
                	else
                	{
                		sheet.addMergedRegion(new CellRangeAddress(startRow+5,startRow+11,0,dataStartColumn -1));
                	}
                	dataRow = sheet.createRow(startRow+5);
                	
                	StringBuilder notes = new StringBuilder();
                	notes.append("Notes:").append("\n");
                	notes.append("- BAT current year exchange rate has been used for currency conversion purposes.").append("\n");
                	notes.append("- Entry of 'NA' in a field indicates that the QPR is not frozen yet.").append("\n");
                	notes.append("- Value of '–' in a field indicates that information was not available, when the QPR was frozen or information might have changed/updated hence,it is not available.").append("\n");
                	notes.append("- 'Latest Cost' is the current cost in the system.").append("\n");
                	notes.append("- Cancelled projects are not included in the above report.").append("\n");
                	if(isSpendByAgency || isSpendByKantarNonKantar)
                	{
                		notes.append("- Costs extracted from this report may slightly differ from the actual project cost due to currency conversion.").append("\n");
                	}
                	HSSFCell notesColumn = dataRow.createCell(0);
                	notesColumn.setCellType(HSSFCell.CELL_TYPE_STRING);
                	notesColumn.setCellValue(notes.toString());
                	notesColumn.setCellStyle(notesStyle);
                }
                
                workbook = SynchroUtils.createExcelImage(workbook, sheet);
            }
        }

    	
        return workbook;
    
    
    }
    
    private HSSFCellStyle getCellStyle(final HSSFWorkbook workbook, final short borderTop,
                                       final short borderRight, final short borderBottom, final short borderLeft) {
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(borderTop);
        cellStyle.setBorderRight(borderRight);
        cellStyle.setBorderBottom(borderBottom);
        cellStyle.setBorderLeft(borderLeft);
        return cellStyle;
    }
    
    private void calculateTotalCostBY(Map<String, List<SpendByReportBean>> spendByReportMap, boolean isQPR1SpendFor, 
    		boolean isQPR2SpendFor, boolean isQPR3SpendFor,boolean isFullYearSpendFor, boolean isCOPLASpendFor, boolean isLatestCost, Integer budgetYear)
    {
    	
    	
    	BigDecimal coplaTotalCost = new BigDecimal("0");
    	BigDecimal qpr1TotalCost = new BigDecimal("0");
    	BigDecimal qpr2TotalCost = new BigDecimal("0");
    	BigDecimal qpr3TotalCost = new BigDecimal("0");
    	BigDecimal fullYearTotalCost = new BigDecimal("0");
    	BigDecimal latestTotalCost = new BigDecimal("0");
    	
    	for(String  spendByReportKey :spendByReportMap.keySet() )
    	{
    		for(SpendByReportBean spendByBean:spendByReportMap.get(spendByReportKey))
	    	{
	    		if(spendByBean!=null && spendByBean.getBudgetYear()!=null && budgetYear.intValue() == spendByBean.getBudgetYear().intValue())
	    		{
	    			if(spendByBean.getCoplaTotalCost()!=null)
		    		{
		    			coplaTotalCost=coplaTotalCost.add(spendByBean.getCoplaTotalCost());
		    		}
		    		if(spendByBean.getQpr1TotalCost()!=null)
		    		{
		    			qpr1TotalCost=qpr1TotalCost.add(spendByBean.getQpr1TotalCost());
		    		}
		    		if(spendByBean.getQpr2TotalCost()!=null)
		    		{
		    			qpr2TotalCost=qpr2TotalCost.add(spendByBean.getQpr2TotalCost());
		    		}
		    		if(spendByBean.getQpr3TotalCost()!=null)
		    		{
		    			qpr3TotalCost=qpr3TotalCost.add(spendByBean.getQpr3TotalCost());
		    		}
		    		if(spendByBean.getFullYearTotalCost()!=null)
		    		{
		    			fullYearTotalCost=fullYearTotalCost.add(spendByBean.getFullYearTotalCost());
		    		}
		    		if(spendByBean.getLatestTotalCost()!=null)
		    		{
		    			latestTotalCost=latestTotalCost.add(spendByBean.getLatestTotalCost());
		    		}
	    		}
		    		
	    	}
    	}
    	if(isCOPLASpendFor)
		{
			totalCosts.add(coplaTotalCost);
		}
    	if(isQPR1SpendFor)
		{
			totalCosts.add(qpr1TotalCost);
		}
    	if(isQPR2SpendFor)
		{
			totalCosts.add(qpr2TotalCost);
		}
    	if(isQPR3SpendFor)
		{
			totalCosts.add(qpr3TotalCost);
		}
    	if(isFullYearSpendFor)
		{
			totalCosts.add(fullYearTotalCost);
		}
    	if(isLatestCost)
    	{
    		totalCosts.add(latestTotalCost);
    	}
    }
    
    private void calculateTotalCostNoBY(Map<String, List<SpendByReportBean>> spendByReportMap, boolean isQPR1SpendFor, 
    		boolean isQPR2SpendFor, boolean isQPR3SpendFor,boolean isFullYearSpendFor, boolean isCOPLASpendFor, boolean isLatestCost, Integer budgetYear)
    {
    	
    	Long coplaSnapShotId = qprSnapshotManager.getSnapShotId(new Integer("1"), budgetYear);
		Long qpr1SnapShotId = qprSnapshotManager.getSnapShotId(new Integer("2"), budgetYear);
		Long qpr2SnapShotId = qprSnapshotManager.getSnapShotId(new Integer("3"), budgetYear);
		Long qpr3SnapShotId = qprSnapshotManager.getSnapShotId(new Integer("4"), budgetYear);
		Long fullYearSnapShotId = qprSnapshotManager.getSnapShotId(new Integer("5"), budgetYear);
    	
    	BigDecimal coplaTotalCost = new BigDecimal("0");
    	BigDecimal qpr1TotalCost = new BigDecimal("0");
    	BigDecimal qpr2TotalCost = new BigDecimal("0");
    	BigDecimal qpr3TotalCost = new BigDecimal("0");
    	BigDecimal fullYearTotalCost = new BigDecimal("0");
    	BigDecimal latestTotalCost = new BigDecimal("0");
    	
    	
    	if(isCOPLASpendFor)
		{
			if(coplaSnapShotId!=null && coplaSnapShotId.intValue() > 0 )
			{
				totalCosts.add(null);
				
			}
			else
			{
				totalCosts.add(coplaTotalCost);
			}
		}
    	if(isQPR1SpendFor)
		{
			
			if(qpr1SnapShotId!=null && qpr1SnapShotId.intValue() > 0 )
			{
				totalCosts.add(null);
			}
			else
			{
				totalCosts.add(qpr1TotalCost);
			}
		}
    	if(isQPR2SpendFor)
		{
			if(qpr2TotalCost!=null && qpr2TotalCost.intValue() > 0 )
			{
				totalCosts.add(null);
			}
			else
			{
				totalCosts.add(qpr2TotalCost);
			}
		}
    	if(isQPR3SpendFor)
		{
			if(qpr3TotalCost!=null && qpr3TotalCost.intValue() > 0 )
			{
				totalCosts.add(null);
			}
			else
			{
				totalCosts.add(qpr3TotalCost);
			}
		}
    	if(isFullYearSpendFor)
		{
			if(fullYearTotalCost!=null && fullYearTotalCost.intValue() > 0 )
			{
				totalCosts.add(null);
			}
			else
			{
				totalCosts.add(fullYearTotalCost);
			}
		}
    	if(isLatestCost)
    	{
    		totalCosts.add(null);
    	}
    }
    private CurrencyExchangeRate getUserCurrencyExchangeRate(final User user) {
        CurrencyExchangeRate currencyExchangeRate = new CurrencyExchangeRate();
        Integer currencyId = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1);
        Integer currency = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1);
        if(currency != null) {
            String currencyCode = SynchroGlobal.getCurrencies().get(currency);
            String currencyKey = String.format(SynchroConstants.SYNCHRO_CURRENCY_VALUE, currencyCode);
            String currencyValue = JiveGlobals.getJiveProperty(currencyKey.toLowerCase(), SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_VALUE.toString());
            if(currencyCode != null) {
                currencyExchangeRate.setCurrencyCode(currencyCode);
            } else {
                currencyExchangeRate.setCurrencyCode("");
            }
            currencyExchangeRate.setCurrencyId(new Long(currency));
            currencyExchangeRate.setExchangeRate(new BigDecimal(currencyValue));
        } else {
            currencyExchangeRate.setExchangeRate(new BigDecimal(1.0));
            currencyExchangeRate.setCurrencyCode("GBP");
        }
        return currencyExchangeRate;
    }
	public QPRSnapshot getQprSnapShot() {
		return qprSnapShot;
	}

	public void setQprSnapShot(QPRSnapshot qprSnapShot) {
		this.qprSnapShot = qprSnapShot;
	}

	public QPRSnapshotManager getQprSnapshotManager() {
		return qprSnapshotManager;
	}

	public void setQprSnapshotManager(QPRSnapshotManager qprSnapshotManager) {
		this.qprSnapshotManager = qprSnapshotManager;
	}

	public List<QPRSnapshot> getQprSnapShotList() {
		return qprSnapShotList;
	}

	public void setQprSnapShotList(List<QPRSnapshot> qprSnapShotList) {
		this.qprSnapShotList = qprSnapShotList;
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

	public ProjectManagerNew getSynchroProjectManagerNew() {
		return synchroProjectManagerNew;
	}

	public void setSynchroProjectManagerNew(
			ProjectManagerNew synchroProjectManagerNew) {
		this.synchroProjectManagerNew = synchroProjectManagerNew;
	}

	public String getDownloadStreamType() {
		return downloadStreamType;
	}

	public void setDownloadStreamType(String downloadStreamType) {
		this.downloadStreamType = downloadStreamType;
	}
    
    
    

}
