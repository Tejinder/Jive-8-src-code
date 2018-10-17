package com.grail.synchro.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.esotericsoftware.minlog.Log;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.MigrateProjectBean;
import com.grail.synchro.beans.MigrateProjectCostBean;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostDetailsBean;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ReportSummaryDetails;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroStageToDoListBean;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProjectEvaluationManager;
import com.grail.synchro.manager.ProjectEvaluationManagerNew;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.manager.ProjectSpecsManagerNew;
import com.grail.synchro.manager.ProposalManager;
import com.grail.synchro.manager.ProposalManagerNew;
import com.grail.synchro.manager.ReportSummaryManager;
import com.grail.synchro.manager.ReportSummaryManagerNew;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.SynchroLogUtilsNew;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.community.user.profile.ProfileManager;
import com.opensymphony.xwork2.Preparable;
/**
 * @author: Tejinder
 * @since: 1.0
 */
public class FetchAttachmentActionNew extends JiveActionSupport implements Preparable {

    private static final Logger LOG = Logger.getLogger(FetchAttachmentActionNew.class);

    //Older references
    private ProjectManager synchroProjectManager;
    private PIBManager pibManager;
    private ProposalManager proposalManager;
    private ProjectSpecsManager projectSpecsManager;
    private ReportSummaryManager reportSummaryManager;
    private ProjectEvaluationManager projectEvaluationManager;
    
    
    //Spring Managers
    private ProjectManagerNew synchroProjectManagerNew;
    private PIBManagerNew pibManagerNew;
    private ProposalManagerNew proposalManagerNew;
    private ProjectSpecsManagerNew projectSpecsManagerNew;
    private ReportSummaryManagerNew reportSummaryManagerNew;
    private ProjectEvaluationManagerNew projectEvaluationManagerNew;
   
    
    private ProfileManager profileManager;
    private SynchroUtils synchroUtils;
     

    private AttachmentManager attachmentManager;
    
    //Form related fields
    private ProjectInitiation projectInitiation;
    private ProjectInitiation projectInitiation_DB;
    private List<EndMarketInvestmentDetail> endMarketDetails_DB;
    private EndMarketInvestmentDetail endMarketDetail_DB = null; 
    private PIBStakeholderList pibStakeholderList_DB;
    private Project project;
    private Project project_DB = null;
    private PIBReporting pibReporting_DB = null;
    private Long projectID;


    private PIBReporting pibReporting = new PIBReporting();
    private PIBStakeholderList pibStakeholderList = new PIBStakeholderList();
    private boolean isSave;

    private boolean editStage;

    private String notificationTabId;

    private String redirectURL;
    private String approve;
    private String recipients;
    private String subject;
    private String messageBody;

    List<SynchroStageToDoListBean> stageToDoList;
    private Integer stageId;

    private Map<String, String> approvers = new LinkedHashMap<String, String>();
    private StageManager stageManager;
    //private File[] attachFile;
    //private String[] attachFileContentType;
    //private String[] attachFileFileName;
    private int attachmentCount;
    private long[] removeAttachID;
    private long[] imageFile;
    private AttachmentHelper attachmentHelper;

    private String token;
    private String tokenCookie;



    private File attachFile;
    private String attachFileContentType;
    private String attachFileFileName;
    private Long attachmentId;
    private Long attachmentFieldID;
    private String attachmentName;
    private Long fieldCategoryId;
    private List<EndMarketInvestmentDetail> endMarketDetails;
    private Long endMarketId;
    // This field will contain the updated SingleMarketId in case the End market is changed
    private Long updatedSingleMarketId;
    //This map will contain the list of attachments for each field
    private Map<Integer, List<AttachmentBean>> attachmentMap = new HashMap<Integer, List<AttachmentBean>>();

    // This will contain  the Approvers data for the Checklist Tab
    Map<String,Map<String,String>> stageApprovers = new LinkedHashMap<String, Map<String,String>>();
    // This will containg the Methodology Waiver Related fields
    private PIBMethodologyWaiver pibMethodologyWaiver;

    // This will containg the Kantar Methodology Waiver Related fields
    private PIBMethodologyWaiver pibKantarMethodologyWaiver;

    // This field will check whether the user click on PIB Methodology Waiver is Approve or Reject button or Send for Information or Request more information
    private String methodologyWaiverAction;

    // This field will check whether the user click on PIB Kantar Methodology Waiver is Approve or Reject button or Send for Information or Request more information
    private String kantarMethodologyWaiverAction;

 // This field will check whether the request is from Pending Actions Page or not
    private String pageRequest;

    //private UserManager userManager;


    private List<ProjectInitiation> initiationList;
    private Map<Long,Long> attachmentUser;
    private Boolean showMandatoryFieldsError;
    

    // This flag will check whether a proposal has been awarded to an agency or not
    private Boolean isProposalAwarded=false;

    private String subjectSendToProjOwner;
    private String messageBodySendToProjOwner;
    private String subjectSendToSPI;
    private String messageBodySendToSPI;

    private String subjectAdminPIBComplete;
    private String messageBodyAdminPIBComplete;
    private String adminPIBCompleteRecipents;

    private EmailNotificationManager emailNotificationManager;
    private File[] mailAttachment;
    private String[] mailAttachmentFileName;
    private String[] mailAttachmentContentType;

    private String otherBATUsers;

    List<ProjectCostDetailsBean> projectCostDetailsList;
    Map<Long, List<User>> endMarketLegalApprovers;
    
    private List<ProjectCostDetailsBean> projectCostDetails;
    private List<Long> endMarketIds;
    private Map<Integer, BigDecimal> totalCosts;
    
    private List<MigrateProjectBean> generateReportBean = new ArrayList<MigrateProjectBean>();
    
    private List<MigrateProjectBean> generateErrorReportBean = new ArrayList<MigrateProjectBean>();
    
    private static DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    
    private String dummyUserName = "Dummy Approver - Migration";
    
    private Long dummyUserId = new Long("3767");

    public void prepare() throws Exception {

        final String id = getRequest().getParameter("projectID");

        if(id  != null ) {

            try{
                if(projectID==null)
                    projectID = Long.parseLong(id);
            } catch (NumberFormatException nfEx) {
                LOG.error("Invalid ProjectID ");
                throw nfEx;
            }
          /*  project = this.synchroProjectManagerNew.get(projectID);
      
            endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(projectID);
            endMarketIds = this.synchroProjectManagerNew.getEndMarketIDs(projectID);

            //    methDeviationField=stageManager.checkPartialMethodologyValidation(project);


            attachmentMap = this.pibManagerNew.getDocumentAttachment(projectID, endMarketDetails.get(0).getEndMarketID());
           
*/
        }

      

     
    }

    public String input() {
    	return INPUT;

    }
    
    public String execute()
    {
    	//migrateProject(projectID); 
    	return SUCCESS;
    }
    
    public String migrateProjectsBulk()
    {
    	try
    	{
	    	if(attachFile!=null && attachFileFileName.contains("migrateProjects-UAT-SM"))
	    	{
	    		FileInputStream fis = new FileInputStream(attachFile);
		    	XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
		    	XSSFSheet mySheet = myWorkBook.getSheetAt(0);
		    	Iterator<Row> rowIterator = mySheet.iterator();
		    	
		    	List<MigrateProjectBean> migrateProjectList = new ArrayList<MigrateProjectBean>();
		    	
		    	Map<Long, MigrateProjectBean> migrateProjectMap = new HashMap<Long, MigrateProjectBean>();
		    	 
		    	while (rowIterator.hasNext()) 
				{ 
					  Row row = rowIterator.next();
					  Iterator<Cell> cellIterator = row.cellIterator(); 
					  MigrateProjectBean migrateProject = new MigrateProjectBean();
					  
					  MigrateProjectCostBean migrateProjectCostBean = new MigrateProjectCostBean();
					  List<MigrateProjectCostBean> migrateProjectCostBeanList = new ArrayList<MigrateProjectCostBean>();
					  
					  
					  
					  
					  while (cellIterator.hasNext()) 
					  {
						  Cell cell = cellIterator.next();
						  if(cell.getColumnIndex()==0)
						  {
							  long pId = (long)cell.getNumericCellValue();
						  	  LOG.info("Adding SM Project Id For Migration ==>"+ pId);
						  	  migrateProject.setProjectID(new Long(pId));
						  	  
						  	if(migrateProjectMap!=null && migrateProjectMap.get(migrateProject.getProjectID())!=null
									  && migrateProjectMap.get(migrateProject.getProjectID()).getMigrateProjectCostBean()!=null
										  && migrateProjectMap.get(migrateProject.getProjectID()).getMigrateProjectCostBean().size()>0)
										  {
						  					migrateProjectCostBeanList = migrateProjectMap.get(migrateProject.getProjectID()).getMigrateProjectCostBean();
										  }
						  	else
							  {
								  
							  }
						  }
						  
						  //List<MigrateProjectCostBean> migrateProjectCostBeanList = new ArrayList<MigrateProjectCostBean>();
						  
						  
						  if(cell.getColumnIndex()==1)
						  {
						  	  LOG.info("Adding Research Agency For Migration ==>"+ cell.getStringCellValue());
						  	migrateProjectCostBean.setResearchAgency(cell.getStringCellValue());
						  }
						  
						  if(cell.getColumnIndex()==2)
						  {
						  	  LOG.info("Adding Cost Component For Migration ==>"+ cell.getStringCellValue());
						  	migrateProjectCostBean.setCostComponent(cell.getStringCellValue());
						  }
						  
						  if(cell.getColumnIndex()==3)
						  {
						  	  LOG.info("Adding Cost Currency For Migration ==>"+ cell.getStringCellValue());
						  	migrateProjectCostBean.setCostCurrency(cell.getStringCellValue());
						  }
						  
						  if(cell.getColumnIndex()==4)
						  {
						  	  LOG.info("Adding Estimated Cost For Migration ==>"+ cell.getNumericCellValue());
						  	migrateProjectCostBean.setEstimatedCost(new BigDecimal(cell.getNumericCellValue()));
						  }
						  
						  if(cell.getColumnIndex()==5)
						  {
						  	  LOG.info("Adding Total Cost For Migration ==>"+ cell.getNumericCellValue());
						  	  migrateProject.setTotalCost(new BigDecimal(cell.getNumericCellValue()));
						  }
						  
						  if(cell.getColumnIndex()==6)
						  {
						  	  LOG.info("Adding Project Type ==>"+ cell.getNumericCellValue());
						  	  migrateProject.setProjectType((int)cell.getNumericCellValue());
						  }
						  
						  if(cell.getColumnIndex()==7)
						  {
						  	  LOG.info("Adding Process Type For Migration ==>"+ cell.getNumericCellValue());
						  	 migrateProject.setProcessType((int)cell.getNumericCellValue());
						  }
						 
						  
						 
					  }
					  
					  migrateProjectCostBeanList.add(migrateProjectCostBean);
					  migrateProject.setMigrateProjectCostBean(migrateProjectCostBeanList);
					  
					  migrateProjectMap.put(migrateProject.getProjectID(), migrateProject);
					  migrateProjectList.add(migrateProject);
					  
				}
				/*if(migrateProjectList!=null && migrateProjectList.size()>0)
				{
					for(MigrateProjectBean mPBean : migrateProjectList)
					{
						migrateProject(mPBean); 
					}
				}*/
		    	
		    	if(migrateProjectMap!=null && migrateProjectMap.size()>0)
				{
					for(Long projectId  : migrateProjectMap.keySet())
					{
						migrateProject(migrateProjectMap.get(projectId), false); 
					}
				}
		    	
		    	try
		    	{
		    		generateMigrationReport("ProjectMigration-SM.xls");
		    		generateMigrationExceptionReport("ProjectMigrationException-SM.xls");
		    	}
		    	catch(Exception e)
		    	{
		    		e.printStackTrace();
		    	}
	    	}
	    	// For Multi Market Projects
	    	else if(attachFile!=null && attachFileFileName.contains("migrateProjects-UAT-MM"))
	    	{ 
				  
	    		FileInputStream fis = new FileInputStream(attachFile);
		    	XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
		    	XSSFSheet mySheet = myWorkBook.getSheetAt(0);
		    	Iterator<Row> rowIterator = mySheet.iterator();
		    	
		    	List<MigrateProjectBean> migrateProjectList = new ArrayList<MigrateProjectBean>();
		    	
		    	Map<Long, MigrateProjectBean> migrateProjectMap = new HashMap<Long, MigrateProjectBean>();
		    	 
		    	while (rowIterator.hasNext()) 
				{ 
		    		Row row = rowIterator.next();
				  Iterator<Cell> cellIterator = row.cellIterator(); 
				  MigrateProjectBean migrateProject = new MigrateProjectBean();
				  
				  MigrateProjectCostBean migrateProjectCostBean = new MigrateProjectCostBean();
				  List<MigrateProjectCostBean> migrateProjectCostBeanList = new ArrayList<MigrateProjectCostBean>();
				  
				  while (cellIterator.hasNext()) 
				  {
					  Cell cell = cellIterator.next();
					  if(cell.getColumnIndex()==0)
					  {
						  long pId = (long)cell.getNumericCellValue();
					  	  LOG.info("Adding mM Project Id For Migration ==>"+ pId);
					  	  migrateProject.setProjectID(new Long(pId));
					  	  
					  	if(migrateProjectMap!=null && migrateProjectMap.get(migrateProject.getProjectID())!=null
								  && migrateProjectMap.get(migrateProject.getProjectID()).getMigrateProjectCostBean()!=null
									  && migrateProjectMap.get(migrateProject.getProjectID()).getMigrateProjectCostBean().size()>0)
									  {
					  					migrateProjectCostBeanList = migrateProjectMap.get(migrateProject.getProjectID()).getMigrateProjectCostBean();
									  }
					  	else
						  {
							  
						  }
					  }
					  
					  //List<MigrateProjectCostBean> migrateProjectCostBeanList = new ArrayList<MigrateProjectCostBean>();
					  
					  
					  if(cell.getColumnIndex()==1)
					  {
					  	  LOG.info("Adding Research Agency For Migration ==>"+ cell.getStringCellValue());
					  	migrateProjectCostBean.setResearchAgency(cell.getStringCellValue());
					  }
					  
					  if(cell.getColumnIndex()==2)
					  {
					  	  LOG.info("Adding Cost Component For Migration ==>"+ cell.getStringCellValue());
					  	 migrateProjectCostBean.setCostComponent(cell.getStringCellValue());
					  }
					  
					  if(cell.getColumnIndex()==3)
					  {
					  	  LOG.info("Adding Cost Currency For Migration ==>"+ cell.getStringCellValue());
					  	migrateProjectCostBean.setCostCurrency(cell.getStringCellValue());
					  }
					  
					  if(cell.getColumnIndex()==4)
					  {
					  	  LOG.info("Adding Estimated Cost For Migration ==>"+ cell.getNumericCellValue());
					  	migrateProjectCostBean.setEstimatedCost(new BigDecimal(cell.getNumericCellValue()));
					  }
					  
					  if(cell.getColumnIndex()==5)
					  {
					  	  LOG.info("Adding Total Cost For Migration ==>"+ cell.getNumericCellValue());
					  	  migrateProject.setTotalCost(new BigDecimal(cell.getNumericCellValue()));
					  }
					  
					  if(cell.getColumnIndex()==6)
					  {
					  	  LOG.info("Adding Project Type ==>"+ cell.getNumericCellValue());
					  	  migrateProject.setProjectType((int)cell.getNumericCellValue());
					  }
					  
					  if(cell.getColumnIndex()==7)
					  {
					  	  LOG.info("Adding Process Type For Migration ==>"+ cell.getNumericCellValue());
					  	 migrateProject.setProcessType((int)cell.getNumericCellValue());
					  }
					  
					  if(cell.getColumnIndex()==8)
					  {
					  	  LOG.info("Adding Project Name For Migration ==>"+ cell.getStringCellValue());
					  	migrateProject.setProjectName(cell.getStringCellValue());
					  }
					  
					  if(cell.getColumnIndex()==9)
					  {
					  	  LOG.info("Adding Budget Location For Migration ==>"+ cell.getNumericCellValue());
					  	long bl = (long)cell.getNumericCellValue();
					  	migrateProject.setBudgetLocation(bl+"");
					  }
					 
					  if(cell.getColumnIndex()==10)
					  {
					  	  
					  	try
					  	  {
							  LOG.info("Adding End Market Ids For Migration ==>"+ cell.getStringCellValue());
						  	  migrateProject.setEndMarketIds(cell.getStringCellValue()+"");
					  	  }
					  	  catch(Exception e)
					  	  {
					  		  LOG.info("Adding End Market Ids For Exception Numeric Value Migration ==>"+ cell.getNumericCellValue());
					  		 long eId = (long)cell.getNumericCellValue();
						  	  migrateProject.setEndMarketIds(eId+"");
					  		
					  	  }
					  }
					  
					  if(cell.getColumnIndex()==11)
					  {
					  	  LOG.info("Adding End Market Funding Req For Migration ==>"+ cell.getNumericCellValue());
					  	  long fReq = (long)cell.getNumericCellValue();
					  	  migrateProject.setEndMarketFundingReq(fReq+"");
					  }
					  if(cell.getColumnIndex()==12)
					  {
						  try
					  	  {
					  		LOG.info("Adding End Market Funding Ids For Migration ==>"+ cell.getStringCellValue());
					  		 migrateProject.setEndMarketFundingIds(cell.getStringCellValue());
					  	  }
					  	  catch(Exception e)
					  	  {
					  		  LOG.info("Adding End Market Funding Ids For Migration  ==>"+ cell.getNumericCellValue());
					  		 long fId = (long)cell.getNumericCellValue();
						  	  migrateProject.setEndMarketFundingIds(fId+"");
					  		
					  	  }
					  }
					  if(cell.getColumnIndex()==13)
					  {
					  	  LOG.info("Adding Project Manager Name For Migration ==>"+ cell.getStringCellValue());
					  	  migrateProject.setProjectManagerName(cell.getStringCellValue());
					  }
					  
					 
				  }
				  
				  migrateProjectCostBeanList.add(migrateProjectCostBean);
				  migrateProject.setMigrateProjectCostBean(migrateProjectCostBeanList);
				  
				  migrateProjectMap.put(migrateProject.getProjectID(), migrateProject);
				  migrateProjectList.add(migrateProject);
				}
		    	
				if(migrateProjectMap!=null && migrateProjectMap.size()>0)
				{
					for(Long projectId  : migrateProjectMap.keySet())
					{
						migrateProject(migrateProjectMap.get(projectId), false); 
					}
				}
		    	
		    	try
		    	{
		    		generateMigrationReport("ProjectMigration-MM.xls");
		    		generateMigrationExceptionReport("ProjectMigrationException-MM.xls");
		    	}
		    	catch(Exception e)
		    	{
		    		e.printStackTrace();
		    	}
			    	
			}
	    	
	    	// For Multi Market Projects
	    	else if(attachFile!=null && attachFileFileName.contains("migrateProjects-UAT-Create-MM"))
	    	{ 
				  
	    		FileInputStream fis = new FileInputStream(attachFile);
		    	XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
		    	XSSFSheet mySheet = myWorkBook.getSheetAt(0);
		    	Iterator<Row> rowIterator = mySheet.iterator();
		    	
		    	List<MigrateProjectBean> migrateProjectList = new ArrayList<MigrateProjectBean>();
		    	
		    	Map<Long, MigrateProjectBean> migrateProjectMap = new HashMap<Long, MigrateProjectBean>();
		    	 
		    	while (rowIterator.hasNext()) 
				{ 
		    		Row row = rowIterator.next();
				  Iterator<Cell> cellIterator = row.cellIterator(); 
				  MigrateProjectBean migrateProject = new MigrateProjectBean();
				  
				  MigrateProjectCostBean migrateProjectCostBean = new MigrateProjectCostBean();
				  List<MigrateProjectCostBean> migrateProjectCostBeanList = new ArrayList<MigrateProjectCostBean>();
				  
				  while (cellIterator.hasNext()) 
				  {
					  Cell cell = cellIterator.next();
					  if(cell.getColumnIndex()==0)
					  {
						  long pId = (long)cell.getNumericCellValue();
					  	  LOG.info("Adding mM Project Id For Migration ==>"+ pId);
					  	  migrateProject.setProjectID(new Long(pId));
					  	  
					  	if(migrateProjectMap!=null && migrateProjectMap.get(migrateProject.getProjectID())!=null
								  && migrateProjectMap.get(migrateProject.getProjectID()).getMigrateProjectCostBean()!=null
									  && migrateProjectMap.get(migrateProject.getProjectID()).getMigrateProjectCostBean().size()>0)
									  {
					  					migrateProjectCostBeanList = migrateProjectMap.get(migrateProject.getProjectID()).getMigrateProjectCostBean();
									  }
					  	else
						  {
							  
						  }
					  }
					  
					  //List<MigrateProjectCostBean> migrateProjectCostBeanList = new ArrayList<MigrateProjectCostBean>();
					  
					  
					  if(cell.getColumnIndex()==1)
					  {
					  	  LOG.info("Adding Research Agency For Migration ==>"+ cell.getStringCellValue());
					  	migrateProjectCostBean.setResearchAgency(cell.getStringCellValue());
					  }
					  
					  if(cell.getColumnIndex()==2)
					  {
					  	  LOG.info("Adding Cost Component For Migration ==>"+ cell.getStringCellValue());
					  	 migrateProjectCostBean.setCostComponent(cell.getStringCellValue());
					  }
					  
					  if(cell.getColumnIndex()==3)
					  {
					  	  LOG.info("Adding Cost Currency For Migration ==>"+ cell.getStringCellValue());
					  	migrateProjectCostBean.setCostCurrency(cell.getStringCellValue());
					  }
					  
					  if(cell.getColumnIndex()==4)
					  {
					  	  LOG.info("Adding Estimated Cost For Migration ==>"+ cell.getNumericCellValue());
					  	migrateProjectCostBean.setEstimatedCost(new BigDecimal(cell.getNumericCellValue()));
					  }
					  
					  if(cell.getColumnIndex()==5)
					  {
					  	  LOG.info("Adding Total Cost For Migration ==>"+ cell.getNumericCellValue());
					  	  migrateProject.setTotalCost(new BigDecimal(cell.getNumericCellValue()));
					  }
					  
					  if(cell.getColumnIndex()==6)
					  {
					  	  LOG.info("Adding Project Type ==>"+ cell.getNumericCellValue());
					  	  migrateProject.setProjectType((int)cell.getNumericCellValue());
					  }
					  
					  if(cell.getColumnIndex()==7)
					  {
					  	  LOG.info("Adding Process Type For Migration ==>"+ cell.getNumericCellValue());
					  	 migrateProject.setProcessType((int)cell.getNumericCellValue());
					  }
					  
					  if(cell.getColumnIndex()==8)
					  {
					  	  LOG.info("Adding Project Name For Migration ==>"+ cell.getStringCellValue());
					  	migrateProject.setProjectName(cell.getStringCellValue());
					  }
					  
					  if(cell.getColumnIndex()==9)
					  {
					  	  LOG.info("Adding Budget Location For Migration ==>"+ cell.getNumericCellValue());
					  	long bl = (long)cell.getNumericCellValue();
					  	migrateProject.setBudgetLocation(bl+"");
					  }
					 
					  if(cell.getColumnIndex()==10)
					  {
					  	  try
					  	  {
							  LOG.info("Adding End Market Ids For Migration ==>"+ cell.getStringCellValue());
						  	  migrateProject.setEndMarketIds(cell.getStringCellValue()+"");
					  	  }
					  	  catch(Exception e)
					  	  {
					  		  LOG.info("Adding End Market Ids For Exception Numeric Value Migration ==>"+ cell.getNumericCellValue());
					  		 long eId = (long)cell.getNumericCellValue();
						  	  migrateProject.setEndMarketIds(eId+"");
					  		
					  	  }
					  }
					  
					  if(cell.getColumnIndex()==11)
					  {
					  	  LOG.info("Adding End Market Funding Req For Migration ==>"+ cell.getNumericCellValue());
					  	  long fReq = (long)cell.getNumericCellValue();
					  	  migrateProject.setEndMarketFundingReq(fReq+"");
					  }
					  if(cell.getColumnIndex()==12)
					  {
					  	  
					  	try
					  	  {
					  		LOG.info("Adding End Market Funding Ids For Migration ==>"+ cell.getStringCellValue());
					  		 migrateProject.setEndMarketFundingIds(cell.getStringCellValue());
					  	  }
					  	  catch(Exception e)
					  	  {
					  		  LOG.info("Adding End Market Funding Ids For Migration  ==>"+ cell.getNumericCellValue());
					  		 long fId = (long)cell.getNumericCellValue();
						  	  migrateProject.setEndMarketFundingIds(fId+"");
					  		
					  	  }
					  }
					  if(cell.getColumnIndex()==13)
					  {
					  	  LOG.info("Adding Project Manager Name For Migration ==>"+ cell.getStringCellValue());
					  	  migrateProject.setProjectManagerName(cell.getStringCellValue());
					  }
					 
				  }
				  
				  migrateProjectCostBeanList.add(migrateProjectCostBean);
				  migrateProject.setMigrateProjectCostBean(migrateProjectCostBeanList);
				  
				  migrateProjectMap.put(migrateProject.getProjectID(), migrateProject);
				  migrateProjectList.add(migrateProject);
				}
		    	
				if(migrateProjectMap!=null && migrateProjectMap.size()>0)
				{
					for(Long projectId  : migrateProjectMap.keySet())
					{
						migrateProject(migrateProjectMap.get(projectId), true); 
					}
				}
		    	
		    	try
		    	{
		    		generateMigrationReport("ProjectMigration-MM.xls");
		    		generateMigrationExceptionReport("ProjectMigrationException-MM.xls");
		    	}
		    	catch(Exception e)
		    	{
		    		e.printStackTrace();
		    	}
			    	
			}
	    	// For Field Work Projects
	    	else if(attachFile!=null && attachFileFileName.contains("migrateProjects-UAT-FieldWork"))
	    	{


	    		FileInputStream fis = new FileInputStream(attachFile);
		    	XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
		    	XSSFSheet mySheet = myWorkBook.getSheetAt(0);
		    	Iterator<Row> rowIterator = mySheet.iterator();
		    	
		    	List<MigrateProjectBean> migrateProjectList = new ArrayList<MigrateProjectBean>();
		    	
		    	Map<Long, MigrateProjectBean> migrateProjectMap = new HashMap<Long, MigrateProjectBean>();
		    	
		    	int i = 0;
		    	while (rowIterator.hasNext()) 
				{ 
					  
		    		  Row row = rowIterator.next();
		    		  if(i==0)
		    		  {
		    			  i++;
		    			  continue;
		    		  }
		    		 
					  Iterator<Cell> cellIterator = row.cellIterator(); 
					  MigrateProjectBean migrateProject = new MigrateProjectBean();
					  
					  MigrateProjectCostBean migrateProjectCostBean = new MigrateProjectCostBean();
					  List<MigrateProjectCostBean> migrateProjectCostBeanList = new ArrayList<MigrateProjectCostBean>();
					  
					  while (cellIterator.hasNext()) 
					  {
						  Cell cell = cellIterator.next();
						  if(cell.getColumnIndex()==0)
						  {
							  long pId = (long)cell.getNumericCellValue();
						  	  LOG.info("Adding Fieldwork Ref Project Id For Migration ==>"+ pId);
						  	  migrateProject.setProjectID(new Long(pId));
						  	  
						  	if(migrateProjectMap!=null && migrateProjectMap.get(migrateProject.getProjectID())!=null
									  && migrateProjectMap.get(migrateProject.getProjectID()).getMigrateProjectCostBean()!=null
										  && migrateProjectMap.get(migrateProject.getProjectID()).getMigrateProjectCostBean().size()>0)
										  {
									  migrateProjectCostBeanList = migrateProjectMap.get(migrateProject.getProjectID()).getMigrateProjectCostBean();
										  }
								  else
								  {
									  
								  }
						  }
						  
						  //List<MigrateProjectCostBean> migrateProjectCostBeanList = new ArrayList<MigrateProjectCostBean>();
						  
						  
						  if(cell.getColumnIndex()==1)
						  {
						  	  LOG.info("Adding Research Agency For Migration ==>"+ cell.getStringCellValue());
						  	migrateProjectCostBean.setResearchAgency(cell.getStringCellValue());
						  }
						  
						  if(cell.getColumnIndex()==2)
						  {
						  	  LOG.info("Adding Cost Component For Migration ==>"+ cell.getStringCellValue());
						  	migrateProjectCostBean.setCostComponent(cell.getStringCellValue());
						  }
						  
						  if(cell.getColumnIndex()==3)
						  {
						  	  LOG.info("Adding Cost Currency For Migration ==>"+ cell.getStringCellValue());
						  	migrateProjectCostBean.setCostCurrency(cell.getStringCellValue());
						  }
						  
						  if(cell.getColumnIndex()==4)
						  {
						  	  LOG.info("Adding Estimated Cost For Migration ==>"+ cell.getNumericCellValue());
						  	migrateProjectCostBean.setEstimatedCost(new BigDecimal(cell.getNumericCellValue()));
						  }
						  
						  if(cell.getColumnIndex()==5)
						  {
						  	  LOG.info("Adding Total Cost For Migration ==>"+ cell.getNumericCellValue());
						  	  migrateProject.setTotalCost(new BigDecimal(cell.getNumericCellValue()));
						  }
						  
						  if(cell.getColumnIndex()==6)
						  {
						  	  LOG.info("Adding Project Type ==>"+ cell.getNumericCellValue());
						  	  migrateProject.setProjectType((int)cell.getNumericCellValue());
						  }
						  
						  if(cell.getColumnIndex()==7)
						  {
						  	  LOG.info("Adding Process Type For Migration ==>"+ cell.getNumericCellValue());
						  	  migrateProject.setProcessType((int)cell.getNumericCellValue());
						  	  
						  	  // As only Germany end market projects should be migrated as EU Online projects
						  	  List<Long> endMkts = synchroProjectManagerNew.getEndMarketIDs(migrateProject.getProjectID());
						  	  if(endMkts!=null && endMkts.size()>0)
						  	  {
						  		  if(endMkts.get(0).intValue()==89)
						  		  {
						  			 migrateProject.setProcessType(1);
						  		  }
						  	  }
						  }
						 
						  if(cell.getColumnIndex()==8)
						  {
						  	  LOG.info("Adding Budget Location For Migration ==>"+ cell.getStringCellValue());
						  	 migrateProject.setBudgetLocation(cell.getStringCellValue());
						  }
						  
						  if(cell.getColumnIndex()==9)
						  {
						  	  LOG.info("Adding Status For Migration ==>"+ cell.getStringCellValue());
						  	 migrateProject.setStatus(cell.getStringCellValue());
						  }
						  
						  if(cell.getColumnIndex()==10)
						  {
						  	  
						  	 long rsc = (long)cell.getNumericCellValue();
						  	 LOG.info("Adding Ref Synchro Code For Migration ==>"+ rsc);
						  	  migrateProject.setRefSynchroCode(new Long(rsc));
						  }
						  
						 
					  }
					  
					  migrateProjectCostBeanList.add(migrateProjectCostBean);
					  migrateProject.setMigrateProjectCostBean(migrateProjectCostBeanList);
					  
					  migrateProjectMap.put(migrateProject.getProjectID(), migrateProject);
					  migrateProjectList.add(migrateProject);
					  
				}
				
		    	
		    	if(migrateProjectMap!=null && migrateProjectMap.size()>0)
				{
					for(Long projectId  : migrateProjectMap.keySet())
					{
						migrateProjectFieldWork(migrateProjectMap.get(projectId)); 
					}
				}
		    	
		    	try
		    	{
		    		generateMigrationReport("ProjectMigration-FieldWork.xls");
		    	}
		    	catch(Exception e)
		    	{
		    		e.printStackTrace();
		    	}
	    	
	    	
	    	}
	    	// For creating dummy Agency Waivers 
	    	else if(attachFile!=null && attachFileFileName.contains("create-dummy-agency-waivers"))
	    	{

	    		FileInputStream fis = new FileInputStream(attachFile);
		    	XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
		    	XSSFSheet mySheet = myWorkBook.getSheetAt(0);
		    	Iterator<Row> rowIterator = mySheet.iterator();
		    	
		    	List<MigrateProjectBean> migrateProjectList = new ArrayList<MigrateProjectBean>();
		    	 
		    	while (rowIterator.hasNext()) 
				{ 
					  Row row = rowIterator.next();
					  Iterator<Cell> cellIterator = row.cellIterator(); 
					  MigrateProjectBean migrateProject = new MigrateProjectBean();
					  
					  while (cellIterator.hasNext()) 
					  {
						  Cell cell = cellIterator.next();
						  if(cell.getColumnIndex()==0)
						  {
							  long pId = (long)cell.getNumericCellValue();
						  	  LOG.info("Adding SM Project Id For Dummy Agency Creation ==>"+ pId);
						  	  migrateProject.setProjectID(new Long(pId));
						  }
						  
						  if(cell.getColumnIndex()==1)
						  {
						  	  LOG.info("Adding Research Agency For Migration ==>"+ cell.getStringCellValue());
						  	  migrateProject.setAgencyDevRationale(cell.getStringCellValue());
						  }
					  }
					
					  migrateProjectList.add(migrateProject);
					  
				}
				
		    	if(migrateProjectList!=null && migrateProjectList.size()>0)
				{
					for(MigrateProjectBean mpb  : migrateProjectList)
					{
						createAgencyWaiver(mpb);
					}
				}
		    	
		    	
	    	
	    	}
	    	// For creating dummy Attachments for PIB BRIEF
	    	else if(attachFile!=null && attachFileFileName.contains("create-dummy-attachments-pib"))
	    	{

	    		FileInputStream fis = new FileInputStream(attachFile);
		    	XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
		    	XSSFSheet mySheet = myWorkBook.getSheetAt(0);
		    	Iterator<Row> rowIterator = mySheet.iterator();
		    	
		    	List<MigrateProjectBean> migrateProjectList = new ArrayList<MigrateProjectBean>();
		    	 
		    	while (rowIterator.hasNext()) 
				{ 
					  Row row = rowIterator.next();
					  Iterator<Cell> cellIterator = row.cellIterator(); 
					  MigrateProjectBean migrateProject = new MigrateProjectBean();
					  
					  while (cellIterator.hasNext()) 
					  {
						  Cell cell = cellIterator.next();
						  if(cell.getColumnIndex()==0)
						  {
							  long pId = (long)cell.getNumericCellValue();
						  	  LOG.info("Adding SM Project Id For Dummy PIB BRIEF Attachment Creation ==>"+ pId);
						  	  migrateProject.setProjectID(new Long(pId));
						  }
					  }
					
					  migrateProjectList.add(migrateProject);
					  
				}
				
		    	if(migrateProjectList!=null && migrateProjectList.size()>0)
				{
					for(MigrateProjectBean mpb  : migrateProjectList)
					{
						createDummyAttachments(mpb,SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId().longValue());
					}
				}
		 	
	    	}
	    	
	    	// For creating dummy Attachments for PROPOSAL BRIEF
	    	else if(attachFile!=null && attachFileFileName.contains("create-dummy-attachments-proposal"))
	    	{

	    		FileInputStream fis = new FileInputStream(attachFile);
		    	XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
		    	XSSFSheet mySheet = myWorkBook.getSheetAt(0);
		    	Iterator<Row> rowIterator = mySheet.iterator();
		    	
		    	List<MigrateProjectBean> migrateProjectList = new ArrayList<MigrateProjectBean>();
		    	 
		    	while (rowIterator.hasNext()) 
				{ 
					  Row row = rowIterator.next();
					  Iterator<Cell> cellIterator = row.cellIterator(); 
					  MigrateProjectBean migrateProject = new MigrateProjectBean();
					  
					  while (cellIterator.hasNext()) 
					  {
						  Cell cell = cellIterator.next();
						  if(cell.getColumnIndex()==0)
						  {
							  long pId = (long)cell.getNumericCellValue();
						  	  LOG.info("Adding SM Project Id For Dummy PROPOSAL BRIEF Attachment Creation ==>"+ pId);
						  	  migrateProject.setProjectID(new Long(pId));
						  }
					  }
					
					  migrateProjectList.add(migrateProject);
					  
				}
				
		    	if(migrateProjectList!=null && migrateProjectList.size()>0)
				{
					for(MigrateProjectBean mpb  : migrateProjectList)
					{
						createDummyAttachments(mpb,SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId().longValue());
					}
				}
		 	
	    	}
	    	
	    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	return SUCCESS;
    }
    
    public String migrateProject(MigrateProjectBean migrateProjectBean, boolean createNewProject)
    {
    	LOG.info("Starting Migration for Project Id ==>"+ migrateProjectBean.getProjectID());
    	Project project = synchroProjectManager.get(migrateProjectBean.getProjectID());
    	
    	boolean isCancelProject = false;
    	
    	
    	
    	Project newProject = synchroProjectManagerNew.get(migrateProjectBean.getProjectID());
    	if(newProject!=null && newProject.getNewSynchro() && !createNewProject)
    	{
    		LOG.info("This Project is already migrated to New Synchro ==>"+ migrateProjectBean.getProjectID());
    	}
    	else
    	{
	    	
    		// All the On-Hold, Cancelled and Deleted projects in Older Synchro will be migrated as Cancelled Projects in New Synchro
    		if(project.getStatus()==SynchroGlobal.Status.PIT_ONHOLD.ordinal() || project.getStatus()==SynchroGlobal.Status.PIT_CANCEL.ordinal()
    				|| project.getStatus()==SynchroGlobal.Status.PIB_ONHOLD.ordinal() || project.getStatus()==SynchroGlobal.Status.PIB_CANCEL.ordinal()
    				|| project.getStatus()==SynchroGlobal.Status.INPROGRESS_ONHOLD.ordinal() || project.getStatus()==SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()
    				|| project.getStatus()==SynchroGlobal.Status.DELETED.ordinal())
    		{
    			isCancelProject = true;
    		}
    		
    		String oldProjectStatus = SynchroGlobal.Status.getById(newProject.getStatus().intValue()).getValue();
    		List<EndMarketInvestmentDetail> endMarketDetails = this.synchroProjectManager.getEndMarketDetails(migrateProjectBean.getProjectID());
	    	if(project!=null)
	    	{
	    		if(!project.getMultiMarket())
	    		{
	    			boolean migrationError = false;
	    			int projectStatus = 1;
	    			try
	    			{
		    			
	    				List<ProjectInitiation> pibInitiationList = this.pibManager.getPIBDetails(migrateProjectBean.getProjectID());
		    			List<ProposalInitiation> proposalInitiationList = this.proposalManager.getProposalDetails(migrateProjectBean.getProjectID());
		    			List<ProjectSpecsInitiation> psInitiationList = this.projectSpecsManager.getProjectSpecsInitiation(migrateProjectBean.getProjectID());
		    			List<ReportSummaryInitiation> rsInitiationList = this.reportSummaryManager.getReportSummaryInitiation(migrateProjectBean.getProjectID());
		    			List<ProjectEvaluationInitiation> peInitiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(migrateProjectBean.getProjectID());
		    			
		    			
		    			List<ProjectCostDetailsBean> projectCostDetails = updateGrailProjectCostDetails(project, migrateProjectBean);
		    			
		    			
		    			BigDecimal totalCost = calculateTotalCost(projectCostDetails);
		    			project.setTotalCost(totalCost);
		    			updateGrailProjectSM(project, endMarketDetails, pibInitiationList, proposalInitiationList, psInitiationList, migrateProjectBean);
		    			
		    			
		    			//updateGrailProjectCostDetails(project, migrateProjectBean);
		    			synchroProjectManagerNew.saveProjectCostDetails(projectCostDetails);
		    			
		    			boolean addBriefDummyAttachment = false;
		    			boolean addProposalDummyAttachment = false;
		    			
		    			if(pibInitiationList!=null && pibInitiationList.size()>0)
			    		{
		    				addBriefDummyAttachment = updateGrailPIBDetails(project, pibInitiationList, migrateProjectBean);
		    				projectStatus = SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal();
			    		}
		    			
		    			if(proposalInitiationList!=null && proposalInitiationList.size()>0)
		    			{
		    				addProposalDummyAttachment = updateGrailProposalDetails(project, proposalInitiationList);
		    				projectStatus = SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal();
		    			}
		    			
		    			if(psInitiationList!=null && psInitiationList.size()>0)
		    			{
		    				updateGrailProjectSpecsDetails(project, psInitiationList);
		    				projectStatus = SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal();
		    			}
		    			
		    			if(rsInitiationList!=null && rsInitiationList.size()>0)
		    			{
		    				updateGrailReportSummaryDetails(project, rsInitiationList, migrateProjectBean);
		    				projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal();
		    			}
		    			
		    			if(peInitiationList!=null && peInitiationList.size()>0)
		    			{
		    				updateGrailProjectEvaluationDetails(project, peInitiationList, endMarketDetails.get(0).getEndMarketID(), migrateProjectBean, projectCostDetails);
		    				if(project.getStatus()==SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal())
		    				{
		    					projectStatus = SynchroGlobal.ProjectStatusNew.CLOSE.ordinal();
		    					if(addBriefDummyAttachment)
		    					{
		    						// Adding Dummy Attachment. It will be added only if the Project is Closed and there is no attachment added in the Older Synchro for this project on PIB Stage
		    						addDummyAttachment(project.getProjectID(),SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId().longValue());
		    					}
		    					
		    					if(addProposalDummyAttachment)
		    					{
		    						// Adding Dummy Attachment. It will be added only if the Project is Closed and there is no attachment added in the Older Synchro for this project on Proposal Stage
		    						addDummyAttachment(project.getProjectID(),SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId().longValue());
		    					}
		    				}
		    				else
		    				{
		    					projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal();
		    				}
		    				
		    			}
	    			}
	    			catch(Exception e)
	    			{
	    				e.printStackTrace();
	    				LOG.error("Exception while Migrating SM Project ==>"+ project.getProjectID());
	    				migrationError = true;
	    				String errLocMsg = e.getLocalizedMessage();
	    				String errMsg = e.getMessage();
	    				
	    				StringWriter sw = new StringWriter();
	    				e.printStackTrace(new PrintWriter(sw));
	    				String exceptionAsString = sw.toString();
	    				
	    				migrateProjectBean.setMigrationException(exceptionAsString);
	    			}
	    			
	    			//Update the final Project Status and isNewSynchro Flag, so that the migrated project becomes part of new Synchro
	    			if(!migrationError)
	    			{
	    				synchroProjectManagerNew.updateProjectStatus(project.getProjectID(), projectStatus);
	    				synchroProjectManagerNew.updateProjectNewSynchroFlag(project.getProjectID(), 1);
	    				if(isCancelProject)
	    				{
	    					synchroProjectManagerNew.updateCancelProject(project.getProjectID(), new Integer("1"));
	    				}
	    				
	    				String newProjectStatus = SynchroGlobal.ProjectStatusNew.getById(projectStatus).getValue();
	    				migrateProjectBean.setOldProjectStatus(oldProjectStatus);
	    				migrateProjectBean.setNewProjectStatus(newProjectStatus);
	    				generateReportBean.add(migrateProjectBean);
	    				
	    				LOG.info("SM Project migrated to  New Synchro Successfully==>"+ migrateProjectBean.getProjectID());
	    			}
	    			else
	    			{
	    				generateErrorReportBean.add(migrateProjectBean);
	    			}
	    		
	    		}
	    		else if(createNewProject)
	    		{
	    			createProjectMM(migrateProjectBean, isCancelProject, oldProjectStatus, project );
	    		}
	    		else
	    		{
	    			migrateProjectMM(migrateProjectBean, isCancelProject, oldProjectStatus, project );
	    		}
	    		
	    		/*else
	    		{
	    			
	    			boolean migrationError = false;
	    			int projectStatus = 1;
	    			
	    			try
	    			{
		    			List<ProjectInitiation> aboveMarketPibInitiationList = this.pibManager.getPIBDetails(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
		    			
		    			PIBStakeholderList pibStakeHolderList = this.pibManager.getPIBStakeholderList(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
		    			Long agencyId = pibStakeHolderList.getAgencyContact1();
		    			
		    			List<ProposalInitiation> aboveMarketProposalInitiationList = this.proposalManager.getProposalDetails(migrateProjectBean.getProjectID(), agencyId);
		    			List<ProjectSpecsInitiation> aboveMarketPSInitiationList = this.projectSpecsManager.getProjectSpecsInitiation(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
		    			
		    			List<ReportSummaryInitiation> aboveMarketRSInitiationList = this.reportSummaryManager.getReportSummaryInitiation(migrateProjectBean.getProjectID(),SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
		    			List<ProjectEvaluationInitiation> aboveMarketPEInitiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
		    			
		    			
		    			updateGrailProjectMM(project, endMarketDetails, aboveMarketPibInitiationList, aboveMarketProposalInitiationList, aboveMarketPSInitiationList, migrateProjectBean);
		    			
		    			updateGrailProjectCostDetails(project, migrateProjectBean);
		    			
		    			if(aboveMarketPibInitiationList!=null && aboveMarketPibInitiationList.size()>0)
			    		{
		    				updateGrailPIBDetailsMM(project, aboveMarketPibInitiationList);
		    				projectStatus = SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal();
			    		}
		    			
		    			if(aboveMarketProposalInitiationList!=null && aboveMarketProposalInitiationList.size()>0)
			    		{
		    				updateGrailProposalDetailsMM(project, aboveMarketProposalInitiationList);
		    				projectStatus = SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal();
			    		}
		    			
		    			if(aboveMarketPSInitiationList!=null && aboveMarketPSInitiationList.size()>0)
			    		{
		    				updateGrailProjectSpecsDetailsMM(project, aboveMarketPSInitiationList);
		    				projectStatus = SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal();
			    		}
		    			
		    			if(aboveMarketRSInitiationList!=null && aboveMarketRSInitiationList.size()>0)
		    			{
		    				updateGrailReportSummaryDetailsMM(project, aboveMarketRSInitiationList);
		    				projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal();
		    			}
		    			
		    			if(aboveMarketPEInitiationList!=null && aboveMarketPEInitiationList.size()>0)
		    			{
		    				updateGrailProjectEvaluationDetails(project, aboveMarketPEInitiationList,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, migrateProjectBean);
		    				projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal();
		    			}
		    			
		    			
		    			
	    			}
	    			
	    			catch(Exception e)
	    			{
	    				LOG.error("Exception while Migrating MM Project ==>"+ project.getProjectID());
	    				migrationError = true;
	    			}
	    			
	    			//Update the final Project Status and isNewSynchro Flag, so that the migrated project becomes part of new Synchro
	    			if(!migrationError)
	    			{
	    				synchroProjectManagerNew.updateProjectStatus(project.getProjectID(), projectStatus);
	    				synchroProjectManagerNew.updateProjectNewSynchroFlag(project.getProjectID(), 1);
	    				
	    				if(isCancelProject)
	    				{
	    					synchroProjectManagerNew.updateCancelProject(project.getProjectID(), new Integer("1"));
	    				}
	    			}
	    			
	    		}*/
	    	}
    	}
    	return "";
    }
    
    public String migrateProjectMM(MigrateProjectBean migrateProjectBean, boolean isCancelProject, String oldProjectStatus, Project project )
    {

		boolean migrationError = false;
		int projectStatus = 1;
		try
		{
			
			List<ProjectInitiation> pibInitiationList = this.pibManager.getPIBDetails(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
			
			PIBStakeholderList pibStakeHolderList = this.pibManager.getPIBStakeholderList(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
			
			List<ProposalInitiation> proposalInitiationList = null;
			
			if(pibStakeHolderList!=null && pibStakeHolderList.getAgencyContact1()!=null)
			{
				Long agencyId = pibStakeHolderList.getAgencyContact1();
				proposalInitiationList = this.proposalManager.getProposalDetails(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, agencyId);
			}
			
			List<ProjectSpecsInitiation> psInitiationList = this.projectSpecsManager.getProjectSpecsInitiation(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
			List<ReportSummaryInitiation> rsInitiationList = this.reportSummaryManager.getReportSummaryInitiation(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
			List<ProjectEvaluationInitiation> peInitiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
			
			
			List<ProjectCostDetailsBean> projectCostDetails = updateGrailProjectCostDetails(project, migrateProjectBean);
			
			
			BigDecimal totalCost = calculateTotalCost(projectCostDetails);
			project.setTotalCost(totalCost);
			project.setBudgetLocation(Integer.valueOf(migrateProjectBean.getBudgetLocation()));
			project.setName(migrateProjectBean.getProjectName());
			project.setProjectManagerName(migrateProjectBean.getProjectManagerName());
			updateGrailProjectMM(project, endMarketDetails, pibInitiationList, proposalInitiationList, psInitiationList, migrateProjectBean);
			
			
			//updateGrailProjectCostDetails(project, migrateProjectBean);
			synchroProjectManagerNew.saveProjectCostDetails(projectCostDetails);
			
			boolean addBriefDummyAttachment = false;
			boolean addProposalDummyAttachment = false;
			
			if(pibInitiationList!=null && pibInitiationList.size()>0)
    		{
				addBriefDummyAttachment = updateGrailPIBDetailsMM(project, pibInitiationList, migrateProjectBean);
				projectStatus = SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal();
    		}
			
			if(proposalInitiationList!=null && proposalInitiationList.size()>0)
			{
				addProposalDummyAttachment = updateGrailProposalDetailsMM(project, proposalInitiationList);
				projectStatus = SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal();
			}
			
			if(psInitiationList!=null && psInitiationList.size()>0)
			{
				updateGrailProjectSpecsDetailsMM(project, psInitiationList);
				projectStatus = SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal();
			}
			
			if(rsInitiationList!=null && rsInitiationList.size()>0)
			{
				updateGrailReportSummaryDetailsMM(project, rsInitiationList, migrateProjectBean);
				projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal();
			}
			
			if(peInitiationList!=null && peInitiationList.size()>0)
			{
				updateGrailProjectEvaluationDetails(project, peInitiationList, endMarketDetails.get(0).getEndMarketID(), migrateProjectBean, projectCostDetails);
				if(project.getStatus()==SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal())
				{
					projectStatus = SynchroGlobal.ProjectStatusNew.CLOSE.ordinal();
					if(addBriefDummyAttachment)
					{
						// Adding Dummy Attachment. It will be added only if the Project is Closed and there is no attachment added in the Older Synchro for this project on PIB Stage
						addDummyAttachment(project.getProjectID(),SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId().longValue());
					}
					
					if(addProposalDummyAttachment)
					{
						// Adding Dummy Attachment. It will be added only if the Project is Closed and there is no attachment added in the Older Synchro for this project on Proposal Stage
						addDummyAttachment(project.getProjectID(),SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId().longValue());
					}
				}
				else
				{
					projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal();
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOG.error("Exception while Migrating MM Project ==>"+ project.getProjectID());
			migrationError = true;
			String errLocMsg = e.getLocalizedMessage();
			String errMsg = e.getMessage();
			
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			
			migrateProjectBean.setMigrationException(exceptionAsString);
		}
		
		//Update the final Project Status and isNewSynchro Flag, so that the migrated project becomes part of new Synchro
		if(!migrationError)
		{
			synchroProjectManagerNew.updateProjectStatus(project.getProjectID(), projectStatus);
			synchroProjectManagerNew.updateProjectNewSynchroFlag(project.getProjectID(), 1);
			if(isCancelProject)
			{
				synchroProjectManagerNew.updateCancelProject(project.getProjectID(), new Integer("1"));
			}
			
			String newProjectStatus = SynchroGlobal.ProjectStatusNew.getById(projectStatus).getValue();
			migrateProjectBean.setOldProjectStatus(oldProjectStatus);
			migrateProjectBean.setNewProjectStatus(newProjectStatus);
			generateReportBean.add(migrateProjectBean);
			
			LOG.info("MM Project migrated to  New Synchro Successfully==>"+ migrateProjectBean.getProjectID());
		}
		else
		{
			generateErrorReportBean.add(migrateProjectBean);
		}
	
		return "";
    }
    
    public String createProjectMM(MigrateProjectBean migrateProjectBean, boolean isCancelProject, String oldProjectStatus, Project project )
    {

		boolean migrationError = false;
		int projectStatus = 1;
		Project newProject = synchroProjectManagerNew.get(project.getProjectID());
		try
		{
			
			List<ProjectInitiation> pibInitiationList = this.pibManagerNew.getPIBDetailsNew(migrateProjectBean.getProjectID());
			List<ProposalInitiation> proposalInitiationList = this.proposalManagerNew.getProposalInitiationNew(migrateProjectBean.getProjectID());
			List<ProjectSpecsInitiation> psInitiationList = this.projectSpecsManagerNew.getProjectSpecsInitiationNew(migrateProjectBean.getProjectID());
			List<ReportSummaryInitiation> rsInitiationList = this.reportSummaryManager.getReportSummaryInitiation(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
			List<ProjectEvaluationInitiation> peInitiationList = this.projectEvaluationManagerNew.getProjectEvaluationInitiation(migrateProjectBean.getProjectID());
			
			//updateGrailProjectMM(project, endMarketDetails, pibInitiationList, proposalInitiationList, psInitiationList, migrateProjectBean);
			
			
			newProject.setProjectID(null);
			newProject.setProcessType(migrateProjectBean.getProcessType());
			newProject.setBudgetLocation(Integer.valueOf(migrateProjectBean.getBudgetLocation()));
			newProject.setName(migrateProjectBean.getProjectName());
			newProject.setProjectManagerName(migrateProjectBean.getProjectManagerName());
			newProject.setIsMigrated(true);
			
			List<ProjectCostDetailsBean> projectCostDetails = updateGrailProjectCostDetails(newProject, migrateProjectBean);
			BigDecimal totalCost = calculateTotalCost(projectCostDetails);
			newProject.setTotalCost(totalCost);
			
			if(newProject.getProcessType()==4)
			{
				newProject.setFieldWorkStudy(1);
				newProject.setProjectType(3);
				newProject.setRefSynchroCode(project.getProjectID());
			}
		
			
			List<Long> endMarketIds = new ArrayList<Long>();
			if(migrateProjectBean.getEndMarketIds()!=null)
			{
				String[] splitIds = migrateProjectBean.getEndMarketIds().split(",");
				if(splitIds!=null && splitIds.length >0)
				{
					for(int i=0;i<splitIds.length;i++)
					{
						endMarketIds.add(new Long(splitIds[i]));
					}
				}
				else
				{
					// It is only for Global Only Projects
					if(migrateProjectBean.getEndMarketIds().equalsIgnoreCase("-1"))
					{
						project.setOnlyGlobalType(1);
						endMarketIds.add(new Long(JiveGlobals.getJiveIntProperty("synchro.global.endmarketId", 282)));
					}
					else
					{
						endMarketIds.add(new Long(migrateProjectBean.getEndMarketIds()));
					}
				}
			}
			
			
			
			newProject = synchroProjectManagerNew.save(newProject);
			
			List<Long> fundingMarkets = new ArrayList<Long>();
			try
			{
				if(migrateProjectBean.getEndMarketFundingIds()!=null)
				{
					String[] splitIds = migrateProjectBean.getEndMarketFundingIds().split(",");
					if(splitIds!=null && splitIds.length >0)
					{
						for(int i=0;i<splitIds.length;i++)
						{
							fundingMarkets.add(new Long(splitIds[i]));
						}
					}
					else
					{
						fundingMarkets.add(new Long(migrateProjectBean.getEndMarketFundingIds()));
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			saveEndMarketDetails(newProject, endMarketIds);
			
			if(newProject.getProcessType()==4)
			{
			
			}
			else
			{
				saveFundingEndMarkets(newProject, endMarketIds,fundingMarkets);
			}
			
			for(ProjectCostDetailsBean pcb : projectCostDetails)
			{
				pcb.setProjectId(newProject.getProjectID());
			}
		
			synchroProjectManagerNew.saveProjectCostDetails(projectCostDetails);
			
			boolean addBriefDummyAttachment = false;
			boolean addProposalDummyAttachment = false;
			
			if(newProject.getProcessType()==4)
			{
			
			}
			else
			{
				if(pibInitiationList!=null && pibInitiationList.size()>0)
	    		{
					addBriefDummyAttachment = createGrailPIBDetailsMM(project, newProject, pibInitiationList, migrateProjectBean);
					projectStatus = SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal();
	    		}
			}
			
			if(proposalInitiationList!=null && proposalInitiationList.size()>0)
			{
				addProposalDummyAttachment = createGrailProposalDetailsMM(project, newProject, proposalInitiationList);
				projectStatus = SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal();
			}
			
			if(newProject.getProcessType()==4)
			{
			
			}
			else
			{
				if(psInitiationList!=null && psInitiationList.size()>0)
				{
					createGrailProjectSpecsDetailsMM(project, newProject, psInitiationList);
					projectStatus = SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal();
				}
			}
			
			if(newProject.getProcessType()==4)
			{
			
			}
			else
			{
				
				if(rsInitiationList!=null && rsInitiationList.size()>0)
				{
					createGrailReportSummaryDetailsMM(project, newProject, rsInitiationList, migrateProjectBean);
					projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal();
				}
			}
			
			if(peInitiationList!=null && peInitiationList.size()>0)
			{
				createGrailProjectEvaluationDetailsMM(newProject, peInitiationList, null, migrateProjectBean, projectCostDetails);
				if(project.getStatus()==SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal())
				{
					projectStatus = SynchroGlobal.ProjectStatusNew.CLOSE.ordinal();
					if(addBriefDummyAttachment)
					{
						// Adding Dummy Attachment. It will be added only if the Project is Closed and there is no attachment added in the Older Synchro for this project on PIB Stage
						addDummyAttachment(newProject.getProjectID(),SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId().longValue());
					}
					
					if(addProposalDummyAttachment)
					{
						// Adding Dummy Attachment. It will be added only if the Project is Closed and there is no attachment added in the Older Synchro for this project on Proposal Stage
						addDummyAttachment(newProject.getProjectID(),SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId().longValue());
					}
				}
				else
				{
					projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal();
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOG.error("Exception while Creating Migrating MM Project ==>"+ newProject.getProjectID());
			migrationError = true;
			String errLocMsg = e.getLocalizedMessage();
			String errMsg = e.getMessage();
			
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			
			migrateProjectBean.setMigrationException(exceptionAsString);
		}
		
		//Update the final Project Status and isNewSynchro Flag, so that the migrated project becomes part of new Synchro
		if(!migrationError)
		{
			synchroProjectManagerNew.updateProjectStatus(newProject.getProjectID(), projectStatus);
			synchroProjectManagerNew.updateProjectNewSynchroFlag(newProject.getProjectID(), 1);
			//if(isCancelProject)
			// If Original Project is cancelled then only cancel the newly created project
			if(project.getIsCancel()!=null && project.getIsCancel())
			{
				synchroProjectManagerNew.updateCancelProject(newProject.getProjectID(), new Integer("1"));
			}
			
			String newProjectStatus = SynchroGlobal.ProjectStatusNew.getById(projectStatus).getValue();
			migrateProjectBean.setOldProjectStatus(oldProjectStatus);
			migrateProjectBean.setNewProjectStatus(newProjectStatus);
			generateReportBean.add(migrateProjectBean);
			
			LOG.info("MM Project created migrated to  New Synchro Successfully==>"+ newProject.getProjectID());
		}
		else
		{
			generateErrorReportBean.add(migrateProjectBean);
		}
	
		return "";
    }
    public String migrateProjectMM_OLD(MigrateProjectBean migrateProjectBean)
    {
    	LOG.info("Starting Migration for Project Id ==>"+ migrateProjectBean.getProjectID());
    	Project project = synchroProjectManager.get(migrateProjectBean.getProjectID());
    	
    	boolean isCancelProject = false;
    	
    	
    	
    	Project newProject = synchroProjectManagerNew.get(migrateProjectBean.getProjectID());
    	if(newProject!=null && newProject.getNewSynchro())
    	{
    		LOG.info("This Project is already migrated to New Synchro ==>"+ migrateProjectBean.getProjectID());
    	}
    	else
    	{
	    	
    		// All the On-Hold, Cancelled and Deleted projects in Older Synchro will be migrated as Cancelled Projects in New Synchro
    		if(project.getStatus()==SynchroGlobal.Status.PIT_ONHOLD.ordinal() || project.getStatus()==SynchroGlobal.Status.PIT_CANCEL.ordinal()
    				|| project.getStatus()==SynchroGlobal.Status.PIB_ONHOLD.ordinal() || project.getStatus()==SynchroGlobal.Status.PIB_CANCEL.ordinal()
    				|| project.getStatus()==SynchroGlobal.Status.INPROGRESS_ONHOLD.ordinal() || project.getStatus()==SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()
    				|| project.getStatus()==SynchroGlobal.Status.DELETED.ordinal())
    		{
    			isCancelProject = true;
    		}
    		
    		String oldProjectStatus = SynchroGlobal.Status.getById(newProject.getStatus().intValue()).getValue();
    		List<EndMarketInvestmentDetail> endMarketDetails = this.synchroProjectManager.getEndMarketDetails(migrateProjectBean.getProjectID());
	    	if(project!=null)
	    	{
	    		
	    			
    			boolean migrationError = false;
    			int projectStatus = 1;
    			
    			try
    			{
	    			List<ProjectInitiation> aboveMarketPibInitiationList = this.pibManager.getPIBDetails(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	    			
	    			PIBStakeholderList pibStakeHolderList = this.pibManager.getPIBStakeholderList(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	    			Long agencyId = pibStakeHolderList.getAgencyContact1();
	    			
	    			List<ProposalInitiation> aboveMarketProposalInitiationList = this.proposalManager.getProposalDetails(migrateProjectBean.getProjectID(), agencyId);
	    			List<ProjectSpecsInitiation> aboveMarketPSInitiationList = this.projectSpecsManager.getProjectSpecsInitiation(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	    			
	    			List<ReportSummaryInitiation> aboveMarketRSInitiationList = this.reportSummaryManager.getReportSummaryInitiation(migrateProjectBean.getProjectID(),SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	    			List<ProjectEvaluationInitiation> aboveMarketPEInitiationList = this.projectEvaluationManager.getProjectEvaluationInitiation(migrateProjectBean.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	    			
	    			
	    			updateGrailProjectMM(project, endMarketDetails, aboveMarketPibInitiationList, aboveMarketProposalInitiationList, aboveMarketPSInitiationList, migrateProjectBean);
	    			
	    			updateGrailProjectCostDetails(project, migrateProjectBean);
	    			
	    			if(aboveMarketPibInitiationList!=null && aboveMarketPibInitiationList.size()>0)
		    		{
	    				updateGrailPIBDetailsMM(project, aboveMarketPibInitiationList, migrateProjectBean);
	    				projectStatus = SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal();
		    		}
	    			
	    			if(aboveMarketProposalInitiationList!=null && aboveMarketProposalInitiationList.size()>0)
		    		{
	    				updateGrailProposalDetailsMM(project, aboveMarketProposalInitiationList);
	    				projectStatus = SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal();
		    		}
	    			
	    			if(aboveMarketPSInitiationList!=null && aboveMarketPSInitiationList.size()>0)
		    		{
	    				updateGrailProjectSpecsDetailsMM(project, aboveMarketPSInitiationList);
	    				projectStatus = SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal();
		    		}
	    			
	    			if(aboveMarketRSInitiationList!=null && aboveMarketRSInitiationList.size()>0)
	    			{
	    				updateGrailReportSummaryDetailsMM(project, aboveMarketRSInitiationList, migrateProjectBean);
	    				projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal();
	    			}
	    			
	    			if(aboveMarketPEInitiationList!=null && aboveMarketPEInitiationList.size()>0)
	    			{
	    				updateGrailProjectEvaluationDetails(project, aboveMarketPEInitiationList,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID, migrateProjectBean, null);
	    				projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal();
	    			}
    			}
    			
    			catch(Exception e)
    			{
    				LOG.error("Exception while Migrating MM Project ==>"+ project.getProjectID());
    				migrationError = true;
    			}
    			
    			//Update the final Project Status and isNewSynchro Flag, so that the migrated project becomes part of new Synchro
    			if(!migrationError)
    			{
    				// Status will also be picked from the excel
    				if(migrateProjectBean.getStatus().equals(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.getValue()))
    				{
    					projectStatus = SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal();
    				}
    				if(migrateProjectBean.getStatus().equals(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.getValue()))
    				{
    					projectStatus = SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal();
    				}
    				if(migrateProjectBean.getStatus().equals(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.getValue()))
    				{
    					projectStatus = SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal();
    				}
    				if(migrateProjectBean.getStatus().equals(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.getValue()))
    				{
    					projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal();
    				}
    				if(migrateProjectBean.getStatus().equals(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.getValue()))
    				{
    					projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal();
    				}
    				if(migrateProjectBean.getStatus().equals(SynchroGlobal.ProjectStatusNew.CLOSE.getValue()))
    				{
    					projectStatus = SynchroGlobal.ProjectStatusNew.CLOSE.ordinal();
    				}
    				
    				synchroProjectManagerNew.updateProjectStatus(project.getProjectID(), projectStatus);
    				synchroProjectManagerNew.updateProjectNewSynchroFlag(project.getProjectID(), 1);
    				
    				if(isCancelProject)
    				{
    					synchroProjectManagerNew.updateCancelProject(project.getProjectID(), new Integer("1"));
    				}
    				
    				String newProjectStatus = SynchroGlobal.ProjectStatusNew.getById(projectStatus).getValue();
    				migrateProjectBean.setOldProjectStatus(oldProjectStatus);
    				migrateProjectBean.setNewProjectStatus(newProjectStatus);
    				generateReportBean.add(migrateProjectBean);
    				
    				LOG.info("MM Project migrated to  New Synchro Successfully==>"+ migrateProjectBean.getProjectID());
    			}
	    			
	    		
	    	}
    	}
    	return "";
    }
    
    public void createAgencyWaiver(MigrateProjectBean migrateProject)
    {
    	pibKantarMethodologyWaiver = new PIBMethodologyWaiver();
    	pibKantarMethodologyWaiver.setProjectID(migrateProject.getProjectID());
    	pibKantarMethodologyWaiver.setMethodologyDeviationRationale(migrateProject.getAgencyDevRationale());
    	
    	endMarketIds = this.synchroProjectManagerNew.getEndMarketIDs(migrateProject.getProjectID());
    	if(endMarketIds!=null && endMarketIds.size()>0)
    	{
    		pibKantarMethodologyWaiver.setEndMarketID(endMarketIds.get(0));
    	}
    	else
    	{
    		pibKantarMethodologyWaiver.setEndMarketID(new Long("0"));
    	}
    	
        pibKantarMethodologyWaiver.setCreationBy(getUser().getID());
        pibKantarMethodologyWaiver.setCreationDate(System.currentTimeMillis());

        pibKantarMethodologyWaiver.setModifiedBy(getUser().getID());
        pibKantarMethodologyWaiver.setModifiedDate(System.currentTimeMillis());
        pibKantarMethodologyWaiver.setIsApproved(null);
        pibKantarMethodologyWaiver.setStatus(SynchroGlobal.MethodologyWaiverStatus.PIB_METH_WAIV_APP_PENDING.ordinal());
        
       // The System Owner will be the Methodology Approver
        List<User> systemOwnerList = getSynchroUtils().getSynchroSystemOwnerUsers();
        if(systemOwnerList!=null && systemOwnerList.size()>0)
        {
        	pibKantarMethodologyWaiver.setMethodologyApprover(systemOwnerList.get(0).getID());
        }
        this.pibManagerNew.savePIBKantarMethodologyWaiver(pibKantarMethodologyWaiver);
        addDummyAttachment( pibKantarMethodologyWaiver.getProjectID(),SynchroGlobal.SynchroAttachmentObject.AGENCY_WAIVER.getId().longValue());
      
      
    }
    
    public void createDummyAttachments(MigrateProjectBean migrateProject, Long fieldType)
    {
           addDummyAttachment(migrateProject.getProjectID(),fieldType );
    }
    
    public String migrateAttachments() 
    {
    	
    	//PIB Attachments
    	attachmentMap = this.pibManagerNew.getDocumentAttachment(projectID,endMarketDetails.get(0).getEndMarketID()); 
    	
		List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId()));
		}
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
           	 pibManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), projectID, new Long("-1"), SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId().longValue(), attachmentUser.get(ab.getID()));
            }
            catch(Exception e)
            {
                Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
            }

        }
    	
    	//Proposal Attachments
		attchBeanList = new ArrayList<AttachmentBean>();
		
		PIBStakeholderList pibStakeHolderList = this.pibManager.getPIBStakeholderList(projectID, endMarketDetails.get(0).getEndMarketID());
		Long agencyId = pibStakeHolderList.getAgencyContact1();
		attachmentMap = this.proposalManagerNew.getDocumentAttachment(projectID,endMarketDetails.get(0).getEndMarketID(),agencyId);
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId()));
		}
		
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
            	proposalManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), projectID, new Long("-1"), SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId().longValue(), attachmentUser.get(ab.getID()), new Long("-1"));
            }
            catch(Exception e)
            {
                Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
            }

        }
		
			//Project Specs Attachments
			attchBeanList = new ArrayList<AttachmentBean>();
			
			attachmentMap = this.projectSpecsManagerNew.getDocumentAttachment(projectID,endMarketDetails.get(0).getEndMarketID());
	    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId())!=null)
			{
				attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId()));
			}
	    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId())!=null)
			{
				attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId()));
			}
	    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId())!=null)
			{
				attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId()));
			}
	    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId())!=null)
			{
				attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId()));
			}
			
			attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
			for(AttachmentBean ab:attchBeanList)
	        {
	            try
	            {
	            	projectSpecsManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                        ab.getContentType(), projectID, new Long("-1"), SynchroGlobal.SynchroAttachmentObject.DOCUMENTATION.getId().longValue(), attachmentUser.get(ab.getID()));
	            }
	            catch(Exception e)
	            {
	                Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	            }

	        }	
    	
    	
		// Report Summary Attachment	
    	
    	 attachmentMap = this.reportSummaryManagerNew.getDocumentAttachment(projectID, endMarketDetails.get(0).getEndMarketID());
    	 int reportOrderId = 0;
    	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null)
    	 {
    		
               attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId());
                List<Long> attachmentIds = new ArrayList<Long>();
                for(AttachmentBean ab:attchBeanList)
                {
                    try
                    {
                    	long attachId =  reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                                ab.getContentType(), projectID, new Long("-1"), SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId().longValue(), getUserID());
                    	attachmentIds.add(attachId);
                    }
                    catch(Exception e)
                    {
                        Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
                    }

                }
    	         
                List<ReportSummaryDetails> reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
            	ReportSummaryDetails rsdbean = new ReportSummaryDetails();
            	rsdbean.setProjectID(projectID);
            	rsdbean.setReportOrderId(++reportOrderId);
            	//rsdbean.setLegalApprover("");
            	rsdbean.setReportType(1);
    		    rsdbean.setAttachmentId(attachmentIds);
    		    reportSummaryDetailsList.add(rsdbean);
    		    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
    	        
    	 }
    	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_REPORT.getId())!=null)
    	 {
    		 attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_REPORT.getId());
             List<Long> attachmentIds = new ArrayList<Long>();
             for(AttachmentBean ab:attchBeanList)
             {
                 try
                 {
                	 long attachId = reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                             ab.getContentType(), projectID, new Long("-1"), SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId().longValue(), getUserID());
                 	attachmentIds.add(attachId);
                 }
                 catch(Exception e)
                 {
                     Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
                 }

             }
 	         
             List<ReportSummaryDetails> reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
         	ReportSummaryDetails rsdbean = new ReportSummaryDetails();
         	rsdbean.setProjectID(projectID);
         	rsdbean.setReportOrderId(++reportOrderId);
         	//rsdbean.setLegalApprover("");
         	rsdbean.setReportType(2);
 		    rsdbean.setAttachmentId(attachmentIds);
 		    reportSummaryDetailsList.add(rsdbean);
 		    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
    	 }
    	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId())!=null)
    	 {
    		 attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId());
             List<Long> attachmentIds = new ArrayList<Long>();
             for(AttachmentBean ab:attchBeanList)
             {
                 try
                 {
                	 long attachId = reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                             ab.getContentType(), projectID, new Long("-1"), SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId().longValue(), getUserID());
                 	attachmentIds.add(attachId);
                 }
                 catch(Exception e)
                 {
                     Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
                 }

             }
 	         
             List<ReportSummaryDetails> reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
         	ReportSummaryDetails rsdbean = new ReportSummaryDetails();
         	rsdbean.setProjectID(projectID);
         	rsdbean.setReportOrderId(++reportOrderId);
         	//rsdbean.setLegalApprover("");
         	rsdbean.setReportType(4);
 		    rsdbean.setAttachmentId(attachmentIds);
 		    reportSummaryDetailsList.add(rsdbean);
 		    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
    	 }
    	 return SUCCESS;

    }
  
    
    public void updateGrailProjectSM(Project project, List<EndMarketInvestmentDetail> endMarketDetails, List<ProjectInitiation> projectInitiationList,List<ProposalInitiation> proposalInitiationList, List<ProjectSpecsInitiation> projectSpecslInitiationList, MigrateProjectBean migrateProjectBean) throws Exception
    	{
    	StringBuilder updateProjectsql= new StringBuilder("");
    	try
    	{
	    	
			
    		project.setProjectType(SynchroGlobal.ProjectType.ENDMARKET.getId());
    		//project.setProcessType(SynchroUtils.getProjectProcessType(project.getProjectType(), endMarketDetails.get(0).getEndMarketID(),endMarketDetails.get(0).getEndMarketID().intValue()));
    		project.setProcessType(migrateProjectBean.getProcessType());
			
			
			
			if(projectSpecslInitiationList!=null && projectSpecslInitiationList.size()>0 && projectSpecslInitiationList.get(0).getDeviationFromSM()==1)
			{
				project.setMethWaiverReq(1);
			}
			else if(projectSpecslInitiationList!=null && projectSpecslInitiationList.size()>0 && projectSpecslInitiationList.get(0).getDeviationFromSM()==0)
			{
				project.setMethWaiverReq(2);
			}
			else if(projectInitiationList!=null && projectInitiationList.size()>0 && projectInitiationList.get(0).getDeviationFromSM()==1)
			{
				project.setMethWaiverReq(1);
			}
			else if(projectInitiationList!=null && projectInitiationList.size()>0 && projectInitiationList.get(0).getDeviationFromSM()==0)
			{
				project.setMethWaiverReq(2);
			}
			else
			{
				project.setMethWaiverReq(-1);
			}
			
			
			
			// Take the Project Details from the latest stage
			if(projectSpecslInitiationList!=null && projectSpecslInitiationList.size()>0)
			{
				/*if(projectSpecslInitiationList.get(0).getBrand()!=null && SynchroGlobal.getAllBrands().get(projectSpecslInitiationList.get(0).getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(projectSpecslInitiationList.get(0).getBrand().intValue()).equals("Multi-Brand"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(1);
					project.setBrand(new Long("-1"));
				}
				else if(projectSpecslInitiationList.get(0).getBrand()!=null && SynchroGlobal.getAllBrands().get(projectSpecslInitiationList.get(0).getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(projectSpecslInitiationList.get(0).getBrand().intValue()).equals("Non-Brand Related"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(2);
					project.setBrand(new Long("-1"));
				}
				else
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(project.getBrand());
				}
				*/

				if(projectSpecslInitiationList.get(0).getBrand()!=null && projectSpecslInitiationList.get(0).getBrand().intValue()==30)
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(1);
					project.setBrand(new Long("-1"));
					project.setMultiBrandStudyText("Multiple Brands Studied");
				}
				else if(projectSpecslInitiationList.get(0).getBrand()!=null && projectSpecslInitiationList.get(0).getBrand().intValue()==1)
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(2);
					project.setBrand(new Long("-1"));
				}
				else if(projectSpecslInitiationList.get(0).getBrand()!=null)
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(projectSpecslInitiationList.get(0).getBrand());
				}
				else
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(project.getBrand());
				}
				
			/*	if(projectSpecslInitiationList.get(0).getProposedMethodology()!=null)
				{
					project.setMethodologyDetails(projectSpecslInitiationList.get(0).getProposedMethodology());
				}
				else
				{
					project.setMethodologyDetails(project.getProposedMethodology());
				}*/
				
				// Methodology Details should be taken from Proposal Stage and not from PS Stage
				//http://redmine.nvish.com/redmine/issues/500
				if(proposalInitiationList!=null && proposalInitiationList.size()>0 && proposalInitiationList.get(0).getProposedMethodology()!=null)
				{
					project.setMethodologyDetails(proposalInitiationList.get(0).getProposedMethodology());
				}
				else
				{
					project.setMethodologyDetails(project.getProposedMethodology());
				}
				
				/*if(projectSpecslInitiationList.get(0).getMethodologyType()!=null)
				{
					project.setMethodologyType(projectSpecslInitiationList.get(0).getMethodologyType());
				}
				else
				{
					project.setMethodologyType(project.getMethodologyType());
				}
				
				if(projectSpecslInitiationList.get(0).getMethodologyGroup()!=null)
				{
					migrateProjectBean.setOldMethodologyGroup(projectSpecslInitiationList.get(0).getMethodologyGroup());
					migrateProjectBean.setNewMethodologyGroup(projectSpecslInitiationList.get(0).getMethodologyGroup());
					
					project.setMethodologyGroup(projectSpecslInitiationList.get(0).getMethodologyGroup());
				}
				else
				{
					project.setMethodologyGroup(project.getMethodologyGroup());
					migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
					migrateProjectBean.setNewMethodologyGroup(project.getMethodologyGroup());
				}*/
				
				// Take the above values also from Proposal Stage
				
				if(proposalInitiationList.get(0).getMethodologyType()!=null)
				{
					project.setMethodologyType(proposalInitiationList.get(0).getMethodologyType());
				}
				else
				{
					project.setMethodologyType(project.getMethodologyType());
				}
				
				
				
				if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
				{
					project.setMethodologyType(SynchroUtils.getMethodologyTypeByProposedMethodology(project.getMethodologyDetails().get(0)));
					project.setMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					
				}
				
				if(proposalInitiationList.get(0).getMethodologyGroup()!=null)
				{
					migrateProjectBean.setOldMethodologyGroup(proposalInitiationList.get(0).getMethodologyGroup());
					if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
					{
						migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					}
					
					//project.setMethodologyGroup(proposalInitiationList.get(0).getMethodologyGroup());
				}
				else
				{
					//project.setMethodologyGroup(project.getMethodologyGroup());
					migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
					//migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
					{
						migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					}
				}
				
		/*		if(projectSpecslInitiationList.get(0).getCategoryType()!=null && projectSpecslInitiationList.get(0).getCategoryType().size()> 0)
				{
					migrateProjectBean.setOldCategoryType(projectSpecslInitiationList.get(0).getCategoryType());
					migrateProjectBean.setNewCategoryType(projectSpecslInitiationList.get(0).getCategoryType());
					
					project.setCategoryType(projectSpecslInitiationList.get(0).getCategoryType());
					
				}
				else
				{
					migrateProjectBean.setOldCategoryType(project.getCategoryType());
					migrateProjectBean.setNewCategoryType(project.getCategoryType());
					
					project.setCategoryType(project.getCategoryType());
				}
			*/	
				// Category Type should be taken from Proposal Stage and not from PS Stage
				//http://redmine.nvish.com/redmine/issues/500
				project.setCategoryType(project.getCategoryType());
				
				migrateProjectBean.setOldCategoryType(project.getCategoryType());
				migrateProjectBean.setNewCategoryType(project.getCategoryType());
				
				if(projectSpecslInitiationList.get(0).getStartDate()!=null)
				{
					project.setStartDate(projectSpecslInitiationList.get(0).getStartDate());
				}
				else
				{
					project.setStartDate(project.getStartDate());
				}
				
				if(projectSpecslInitiationList.get(0).getEndDate()!=null)
				{
					project.setEndDate(projectSpecslInitiationList.get(0).getEndDate());
				}
				else
				{
					project.setEndDate(project.getEndDate());
				}
				
			}
			else if(proposalInitiationList!=null && proposalInitiationList.size()>0)
			{
				/*if(proposalInitiationList.get(0).getBrand()!=null && SynchroGlobal.getAllBrands().get(proposalInitiationList.get(0).getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(proposalInitiationList.get(0).getBrand().intValue()).equals("Multi-Brand"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(1);
					project.setBrand(new Long("-1"));
				}
				else if(proposalInitiationList.get(0).getBrand()!=null && SynchroGlobal.getAllBrands().get(proposalInitiationList.get(0).getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(proposalInitiationList.get(0).getBrand().intValue()).equals("Non-Brand Related"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(2);
					project.setBrand(new Long("-1"));
				}
				else
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(project.getBrand());
				}*/
				
				if(proposalInitiationList.get(0).getBrand()!=null && proposalInitiationList.get(0).getBrand().intValue()==30)
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(1);
					project.setBrand(new Long("-1"));
				}
				else if(proposalInitiationList.get(0).getBrand()!=null && proposalInitiationList.get(0).getBrand().intValue()==1)
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(2);
					project.setBrand(new Long("-1"));
				}
				else if(proposalInitiationList.get(0).getBrand()!=null)
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(proposalInitiationList.get(0).getBrand());
				}
				else
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(project.getBrand());
				}
				
				if(proposalInitiationList.get(0).getProposedMethodology()!=null)
				{
					project.setMethodologyDetails(proposalInitiationList.get(0).getProposedMethodology());
				}
				else
				{
					project.setMethodologyDetails(project.getProposedMethodology());
				}
				
			/*	if(proposalInitiationList.get(0).getMethodologyType()!=null)
				{
					project.setMethodologyType(proposalInitiationList.get(0).getMethodologyType());
				}
				else
				{
					project.setMethodologyType(project.getMethodologyType());
				}*/
				
				
				if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
				{
					project.setMethodologyType(SynchroUtils.getMethodologyTypeByProposedMethodology(project.getMethodologyDetails().get(0)));
					project.setMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
				}
				
				if(proposalInitiationList.get(0).getMethodologyGroup()!=null)
				{
					migrateProjectBean.setOldMethodologyGroup(proposalInitiationList.get(0).getMethodologyGroup());
					//migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					
					if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
					{
						migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					}
					
					//project.setMethodologyGroup(proposalInitiationList.get(0).getMethodologyGroup());
					//project.setMethodologyGroup(SynchroGlobal.getMethodologyGroupId(project.getMethodologyDetails()));
				}
				else
				{
					//project.setMethodologyGroup(project.getMethodologyGroup());
				//	project.setMethodologyGroup(SynchroGlobal.getMethodologyGroupId(project.getMethodologyDetails()));
					migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
					//migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
					{
						migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					}
				}
				//
				if(proposalInitiationList.get(0).getStartDate()!=null)
				{
					project.setStartDate(proposalInitiationList.get(0).getStartDate());
				}
				else
				{
					project.setStartDate(project.getStartDate());
				}
				
				if(proposalInitiationList.get(0).getEndDate()!=null)
				{
					project.setEndDate(proposalInitiationList.get(0).getEndDate());
				}
				else
				{
					project.setEndDate(project.getEndDate());
				}
				
				project.setCategoryType(project.getCategoryType());
				
				migrateProjectBean.setOldCategoryType(project.getCategoryType());
				migrateProjectBean.setNewCategoryType(project.getCategoryType());
			}
	
			else
			{
				
				/*if(project.getBrand()!=null && SynchroGlobal.getAllBrands().get(project.getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(project.getBrand().intValue()).equals("Multi-Brand"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(1);
					project.setBrand(new Long("-1"));
				}
				else if(project.getBrand()!=null && SynchroGlobal.getAllBrands().get(project.getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(project.getBrand().intValue()).equals("Non-Brand Related"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(2);
					project.setBrand(new Long("-1"));
				}
				else
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(project.getBrand());
				}*/
				
				if(project.getBrand()!=null && project.getBrand().intValue()==30)
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(1);
					project.setBrand(new Long("-1"));
				}
				else if(project.getBrand()!=null && project.getBrand().intValue()==1)
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(2);
					project.setBrand(new Long("-1"));
				}
				else
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(project.getBrand());
				}
				
			
					project.setMethodologyDetails(project.getProposedMethodology());
				//	project.setMethodologyType(project.getMethodologyType());
				
					
					if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
					{
						project.setMethodologyType(SynchroUtils.getMethodologyTypeByProposedMethodology(project.getMethodologyDetails().get(0)));
						project.setMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					}
					
					if(project.getMethodologyGroup()!=null)
					{
						migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
						//migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
						if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
						{
							migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
						}
						
						//project.setMethodologyGroup(project.getMethodologyGroup());
					}
					else
					{
					//	project.setMethodologyGroup(project.getMethodologyGroup());
						migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
						//migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
						if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
						{
							migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
						}
					}
					
					project.setCategoryType(project.getCategoryType());
					
					migrateProjectBean.setOldCategoryType(project.getCategoryType());
					migrateProjectBean.setNewCategoryType(project.getCategoryType());
					
					project.setStartDate(project.getStartDate());
					project.setEndDate(project.getEndDate());
			
				
				
			}
						
			
			project.setBudgetLocation(endMarketDetails.get(0).getEndMarketID().intValue());
			
			String projectManagerName = "";
			try
			{
				projectManagerName = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getName();
			}
			catch(Exception e)
			{
				LOG.info("User Not Found ===>"+endMarketDetails.get(0).getSpiContact());
				
			}
			
			project.setProjectManagerName(projectManagerName);
			project.setFieldWorkStudy(2);
			
			//updateProjectsql.append(" newsynchro=1,");
			project.setFundingMarkets(null);
			project.setEndMarketFunding(null);
			project.setEuMarketConfirmation(null);
			
			//Total Cost should not be blank otherwise it will impact other functionalities
			/*if(endMarketDetails.get(0)!=null && endMarketDetails.get(0).getInitialCost()!=null)
			{
				project.setTotalCost(endMarketDetails.get(0).getInitialCost());
			}
			else
			{
				project.setTotalCost(new BigDecimal("0"));
			}*/
			
			//project.setTotalCost(migrateProjectBean.getTotalCost());
			int defaultCurrency = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1);
			project.setTotalCostCurrency(new Long(defaultCurrency));
		//	project.setMethodologyDetails(project.getProposedMethodology());
			//
			
			synchroProjectManagerNew.updateProjectMigrate(project);
			
		
			
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new Exception();
    	}
			
	
    
		
		
    }
    
    public void updateGrailProjectMM(Project project, List<EndMarketInvestmentDetail> endMarketDetails, List<ProjectInitiation> projectInitiationList,List<ProposalInitiation> proposalInitiationList, List<ProjectSpecsInitiation> projectSpecslInitiationList, MigrateProjectBean migrateProjectBean) throws Exception
	{
	StringBuilder updateProjectsql= new StringBuilder("");
	try
	{
    	
		
		project.setProjectType(SynchroGlobal.ProjectType.GLOBAL.getId());
		project.setProcessType(migrateProjectBean.getProcessType());
		
		if(projectSpecslInitiationList!=null && projectSpecslInitiationList.size()>0 && projectSpecslInitiationList.get(0).getDeviationFromSM()==1)
		{
			project.setMethWaiverReq(1);
		}
		else if(projectSpecslInitiationList!=null && projectSpecslInitiationList.size()>0 && projectSpecslInitiationList.get(0).getDeviationFromSM()==0)
		{
			project.setMethWaiverReq(2);
		}
		else if(projectInitiationList!=null && projectInitiationList.size()>0 && projectInitiationList.get(0).getDeviationFromSM()==1)
		{
			project.setMethWaiverReq(1);
		}
		else if(projectInitiationList!=null && projectInitiationList.size()>0 && projectInitiationList.get(0).getDeviationFromSM()==0)
		{
			project.setMethWaiverReq(2);
		}
		else
		{
			project.setMethWaiverReq(-1);
		}
		
		
		
		// Take the Project Details from the latest stage
		if(projectSpecslInitiationList!=null && projectSpecslInitiationList.size()>0)
		{
			if(projectSpecslInitiationList.get(0).getBrand()!=null && projectSpecslInitiationList.get(0).getBrand().intValue()==30)
			{
				
				project.setBrandSpecificStudy(2);
				project.setBrandSpecificStudyType(1);
				project.setBrand(new Long("-1"));
				project.setMultiBrandStudyText("Multiple Brands Studied");
			}
			else if(projectSpecslInitiationList.get(0).getBrand()!=null && projectSpecslInitiationList.get(0).getBrand().intValue()==1)
			{
				
				project.setBrandSpecificStudy(2);
				project.setBrandSpecificStudyType(2);
				project.setBrand(new Long("-1"));
			}
			else if(projectSpecslInitiationList.get(0).getBrand()!=null)
			{
				
				project.setBrandSpecificStudy(1);
				project.setBrandSpecificStudyType(-1);
				project.setBrand(projectSpecslInitiationList.get(0).getBrand());
			}
			else
			{
				
				project.setBrandSpecificStudy(1);
				project.setBrandSpecificStudyType(-1);
				project.setBrand(project.getBrand());
			}
			
			// Methodology Details should be taken from Proposal Stage and not from PS Stage
			//http://redmine.nvish.com/redmine/issues/500
			if(proposalInitiationList!=null && proposalInitiationList.size()>0 && proposalInitiationList.get(0).getProposedMethodology()!=null)
			{
				project.setMethodologyDetails(proposalInitiationList.get(0).getProposedMethodology());
			}
			else
			{
				project.setMethodologyDetails(project.getProposedMethodology());
			}
			
			// Take the above values also from Proposal Stage
		/*	if(proposalInitiationList.get(0).getMethodologyType()!=null)
			{
				project.setMethodologyType(proposalInitiationList.get(0).getMethodologyType());
			}
			else
			{
				project.setMethodologyType(project.getMethodologyType());
			}
		*/	
			
			if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
			{
				project.setMethodologyType(SynchroUtils.getMethodologyTypeByProposedMethodology(project.getMethodologyDetails().get(0)));
				project.setMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
			}
		    
			
		/*	if(proposalInitiationList.get(0).getMethodologyGroup()!=null)
			{
				migrateProjectBean.setOldMethodologyGroup(proposalInitiationList.get(0).getMethodologyGroup());
				migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
			}
			else
			{
				migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
				migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
			}
*/
			// Category Type should be taken from Proposal Stage and not from PS Stage
			//http://redmine.nvish.com/redmine/issues/500
			project.setCategoryType(project.getCategoryType());
			
			migrateProjectBean.setOldCategoryType(project.getCategoryType());
			migrateProjectBean.setNewCategoryType(project.getCategoryType());
			
			if(projectSpecslInitiationList!=null && projectSpecslInitiationList.get(0)!=null && projectSpecslInitiationList.get(0).getStartDate()!=null)
			{
				project.setStartDate(projectSpecslInitiationList.get(0).getStartDate());
			}
			else
			{
				project.setStartDate(project.getStartDate());
			}
			
			if(projectSpecslInitiationList!=null && projectSpecslInitiationList.get(0)!=null &&  projectSpecslInitiationList.get(0).getEndDate()!=null)
			{
				project.setEndDate(projectSpecslInitiationList.get(0).getEndDate());
			}
			else
			{
				project.setEndDate(project.getEndDate());
			}
			
		}
		else if(proposalInitiationList!=null && proposalInitiationList.size()>0)
		{
			
			if(proposalInitiationList.get(0).getBrand()!=null && proposalInitiationList.get(0).getBrand().intValue()==30)
			{
				
				project.setBrandSpecificStudy(2);
				project.setBrandSpecificStudyType(1);
				project.setBrand(new Long("-1"));
			}
			else if(proposalInitiationList.get(0).getBrand()!=null && proposalInitiationList.get(0).getBrand().intValue()==1)
			{
				
				project.setBrandSpecificStudy(2);
				project.setBrandSpecificStudyType(2);
				project.setBrand(new Long("-1"));
			}
			else if(proposalInitiationList.get(0).getBrand()!=null)
			{
				
				project.setBrandSpecificStudy(1);
				project.setBrandSpecificStudyType(-1);
				project.setBrand(proposalInitiationList.get(0).getBrand());
			}
			else
			{
				
				project.setBrandSpecificStudy(1);
				project.setBrandSpecificStudyType(-1);
				project.setBrand(project.getBrand());
			}
			
			if(proposalInitiationList.get(0).getProposedMethodology()!=null)
			{
				project.setMethodologyDetails(proposalInitiationList.get(0).getProposedMethodology());
			}
			else
			{
				project.setMethodologyDetails(project.getProposedMethodology());
			}
			
			if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
			{
				project.setMethodologyType(SynchroUtils.getMethodologyTypeByProposedMethodology(project.getMethodologyDetails().get(0)));
				project.setMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
			}
			
			if(proposalInitiationList.get(0).getMethodologyGroup()!=null)
			{
				migrateProjectBean.setOldMethodologyGroup(proposalInitiationList.get(0).getMethodologyGroup());
				//migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
				
				if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
				{
					migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
				}
			}
			else
			{
				//project.setMethodologyGroup(project.getMethodologyGroup());
			//	project.setMethodologyGroup(SynchroGlobal.getMethodologyGroupId(project.getMethodologyDetails()));
				migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
				//migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
				
				if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
				{
					migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
				}
			}
			//
			if(proposalInitiationList.get(0).getStartDate()!=null)
			{
				project.setStartDate(proposalInitiationList.get(0).getStartDate());
			}
			else
			{
				project.setStartDate(project.getStartDate());
			}
			
			if(proposalInitiationList.get(0).getEndDate()!=null)
			{
				project.setEndDate(proposalInitiationList.get(0).getEndDate());
			}
			else
			{
				project.setEndDate(project.getEndDate());
			}
			
			project.setCategoryType(project.getCategoryType());
			
			migrateProjectBean.setOldCategoryType(project.getCategoryType());
			migrateProjectBean.setNewCategoryType(project.getCategoryType());
		}

		else
		{
			
			if(project.getBrand()!=null && project.getBrand().intValue()==30)
			{
				
				project.setBrandSpecificStudy(2);
				project.setBrandSpecificStudyType(1);
				project.setBrand(new Long("-1"));
			}
			else if(project.getBrand()!=null && project.getBrand().intValue()==1)
			{
				
				project.setBrandSpecificStudy(2);
				project.setBrandSpecificStudyType(2);
				project.setBrand(new Long("-1"));
			}
			else
			{
				
				project.setBrandSpecificStudy(1);
				project.setBrandSpecificStudyType(-1);
				project.setBrand(project.getBrand());
			}
			
		
				project.setMethodologyDetails(project.getProposedMethodology());
			//	project.setMethodologyType(project.getMethodologyType());
			
				if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
				{
					project.setMethodologyType(SynchroUtils.getMethodologyTypeByProposedMethodology(project.getMethodologyDetails().get(0)));
					project.setMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
				}
				
				
				if(project.getMethodologyGroup()!=null)
				{
					migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
					//migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					
					if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
					{
						migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					}
					
					//project.setMethodologyGroup(project.getMethodologyGroup());
				}
				else
				{
				//	project.setMethodologyGroup(project.getMethodologyGroup());
					migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
					//migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					
					if(project.getMethodologyDetails()!=null && project.getMethodologyDetails().size() >0)
					{
						migrateProjectBean.setNewMethodologyGroup(new Long(SynchroGlobal.getAllMethodologyGroupId(project.getMethodologyDetails().get(0))));
					}
				}
				
				project.setCategoryType(project.getCategoryType());
				
				migrateProjectBean.setOldCategoryType(project.getCategoryType());
				migrateProjectBean.setNewCategoryType(project.getCategoryType());
				
				project.setStartDate(project.getStartDate());
				project.setEndDate(project.getEndDate());
			
		}
					
		
		//project.setBudgetLocation(endMarketDetails.get(0).getEndMarketID().intValue());
		
		//String projectManagerName = "";
		/*try
		{
			projectManagerName = userManager.getUser(endMarketDetails.get(0).getSpiContact()).getName();
		}
		catch(Exception e)
		{
			LOG.info("User Not Found ===>"+endMarketDetails.get(0).getSpiContact());
			
		}*/
		
		//project.setProjectManagerName(projectManagerName);
		project.setFieldWorkStudy(2);
		
		List<Long> endMarketIds = new ArrayList<Long>();
		if(migrateProjectBean.getEndMarketIds()!=null)
		{
			String[] splitIds = migrateProjectBean.getEndMarketIds().split(",");
			if(splitIds!=null && splitIds.length >0)
			{
				for(int i=0;i<splitIds.length;i++)
				{
					endMarketIds.add(new Long(splitIds[i]));
				}
			}
			else
			{
				// It is only for Global Only Projects
				if(migrateProjectBean.getEndMarketIds().equalsIgnoreCase("-1"))
				{
					project.setOnlyGlobalType(1);
					endMarketIds.add(new Long(JiveGlobals.getJiveIntProperty("synchro.global.endmarketId", 282)));
				}
				else
				{
					endMarketIds.add(new Long(migrateProjectBean.getEndMarketIds()));
				}
			}
		}
		
		List<Long> fundingMarkets = new ArrayList<Long>();
		if(migrateProjectBean.getEndMarketFundingIds()!=null)
		{
			try
			{
				String[] splitIds = migrateProjectBean.getEndMarketFundingIds().split(",");
				if(splitIds!=null && splitIds.length >0)
				{
					for(int i=0;i<splitIds.length;i++)
					{
						fundingMarkets.add(new Long(splitIds[i]));
					}
				}
				else
				{
					fundingMarkets.add(new Long(migrateProjectBean.getEndMarketFundingIds()));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		saveEndMarketDetails(project, endMarketIds);
		saveFundingEndMarkets(project, endMarketIds,fundingMarkets);
		
		project.setFundingMarkets(fundingMarkets);
		project.setEndMarketFunding(new Integer(migrateProjectBean.getEndMarketFundingReq()));
		project.setEuMarketConfirmation(null);
		
	
		int defaultCurrency = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1);
		project.setTotalCostCurrency(new Long(defaultCurrency));
		
		synchroProjectManagerNew.updateProjectMigrate(project);
	}
	catch(Exception e)
	{
		e.printStackTrace();
		throw new Exception();
	}
	
}
    
    private String saveEndMarketDetails(Project project , List<Long> endMarketIds) {
        final Long userID = getUser().getID();
        // The Existing end market needs to be deleted for SAVE a Draft
        synchroProjectManagerNew.deleteEndMarketDetail(project.getProjectID());

        if(project.getProjectID()!=null && endMarketIds != null){
            for(Long endMarketID : endMarketIds){
                if(endMarketID!=null)
                {
	            	final EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), endMarketID);
	                if(project.getInitialCost()!=null && project.getInitialCost().size()>0)
	                {
	                	endMarketInvestmentDetail.setInitialCost(SynchroUtils.formatDate(project.getInitialCost().get(0)));
	                }
	                if(project.getInitialCostCurrency()!=null && project.getInitialCostCurrency().size()>0)
	                {
	                    endMarketInvestmentDetail.setInitialCostCurrency(project.getInitialCostCurrency().get(0));
	                }
	                if(project.getSpiContact()!=null && project.getSpiContact().size()>0)
	                {
	                	endMarketInvestmentDetail.setSpiContact(project.getSpiContact().get(0));
	                }
	                endMarketInvestmentDetail.setCreationBy(userID);
	                endMarketInvestmentDetail.setModifiedBy(userID);
	                synchroProjectManagerNew.saveEndMarketDetail(endMarketInvestmentDetail);
	               
	              //  synchroProjectManager.setAllEndMarketStatus(project.getProjectID(), SynchroGlobal.ProjectActivationStatus.OPEN.ordinal());
                }   
            }
        }
        return SUCCESS;
    }
    
    private String saveFundingEndMarkets(Project project , List<Long> endMarketIds, List<Long> fundingMarketIds) {
        

        if(project.getProjectID()!=null && fundingMarketIds != null && endMarketIds!=null){
            
        	for(Long endMarketID : endMarketIds){
                if(endMarketID!=null)
                {
	            	final EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), endMarketID);
	            	endMarketInvestmentDetail.setIsFundingMarket(false);
	                synchroProjectManagerNew.updateFundingEndMarkets(endMarketInvestmentDetail);
                }   
            }
        	for(Long fundingMarketID : fundingMarketIds){
                if(fundingMarketID!=null)
                {
	            	final EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), fundingMarketID);
	            	endMarketInvestmentDetail.setIsFundingMarket(true);
	                synchroProjectManagerNew.updateFundingEndMarkets(endMarketInvestmentDetail);
	               
	             
                }   
            }
        }
        return SUCCESS;
    }
    public void updateGrailProjectMMOLD(Project project, List<EndMarketInvestmentDetail> endMarketDetails, List<ProjectInitiation> projectInitiationList,List<ProposalInitiation> proposalInitiationList, List<ProjectSpecsInitiation> projectSpecslInitiationList, MigrateProjectBean migrateProjectBean) throws Exception
    
   	{
		StringBuilder updateProjectsql= new StringBuilder("");
		try
		{
	    	
			
			project.setProjectType(SynchroGlobal.ProjectType.GLOBAL.getId());
			//project.setProcessType(SynchroUtils.getProjectProcessType(project.getProjectType(), endMarketDetails.get(0).getEndMarketID(),endMarketDetails.get(0).getEndMarketID().intValue()));
			project.setProcessType(migrateProjectBean.getProcessType());
			
			if(migrateProjectBean.getBudgetLocation().equals("Global"))
			{
				project.setBudgetLocation(new Integer("-1"));
			}
			else
			{
				for(Integer region : SynchroGlobal.getRegions().keySet())
				{
					if(SynchroGlobal.getRegions().get(region).equals(migrateProjectBean.getBudgetLocation()))
					{
						project.setBudgetLocation(region);
					}
				}
			}
			
			if(projectSpecslInitiationList!=null && projectSpecslInitiationList.size()>0 && projectSpecslInitiationList.get(0).getDeviationFromSM()==1)
			{
				project.setMethWaiverReq(1);
			}
			else if(projectSpecslInitiationList!=null && projectSpecslInitiationList.size()>0 && projectSpecslInitiationList.get(0).getDeviationFromSM()==0)
			{
				project.setMethWaiverReq(2);
			}
			else if(projectInitiationList!=null && projectInitiationList.size()>0 && projectInitiationList.get(0).getDeviationFromSM()==1)
			{
				project.setMethWaiverReq(1);
			}
			else if(projectInitiationList!=null && projectInitiationList.size()>0 && projectInitiationList.get(0).getDeviationFromSM()==0)
			{
				project.setMethWaiverReq(2);
			}
			else
			{
				project.setMethWaiverReq(-1);
			}
			
			
			
			// Take the Project Details from the latest stage
			if(projectSpecslInitiationList!=null && projectSpecslInitiationList.size()>0)
			{
				if(projectSpecslInitiationList.get(0).getBrand()!=null && SynchroGlobal.getAllBrands().get(projectSpecslInitiationList.get(0).getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(projectSpecslInitiationList.get(0).getBrand().intValue()).equals("Multi-Brand"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(1);
					project.setBrand(new Long("-1"));
				}
				else if(projectSpecslInitiationList.get(0).getBrand()!=null && SynchroGlobal.getAllBrands().get(projectSpecslInitiationList.get(0).getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(projectSpecslInitiationList.get(0).getBrand().intValue()).equals("Non-Brand Related"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(2);
					project.setBrand(new Long("-1"));
				}
				else
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(project.getBrand());
				}
				
			/*	if(projectSpecslInitiationList.get(0).getProposedMethodology()!=null)
				{
					project.setMethodologyDetails(projectSpecslInitiationList.get(0).getProposedMethodology());
				}
				else
				{
					project.setMethodologyDetails(project.getProposedMethodology());
				}
				*/
				
				// Methodology Details should be taken from Proposal Stage and not from PS Stage
				//http://redmine.nvish.com/redmine/issues/500
				if(proposalInitiationList!=null && proposalInitiationList.size()>0 && proposalInitiationList.get(0).getProposedMethodology()!=null)
				{
					project.setMethodologyDetails(proposalInitiationList.get(0).getProposedMethodology());
				}
				else
				{
					project.setMethodologyDetails(project.getProposedMethodology());
				}
				
				if(projectSpecslInitiationList.get(0).getMethodologyType()!=null)
				{
					project.setMethodologyType(projectSpecslInitiationList.get(0).getMethodologyType());
				}
				else
				{
					project.setMethodologyType(project.getMethodologyType());
				}
				
				if(projectSpecslInitiationList.get(0).getMethodologyGroup()!=null)
				{
					migrateProjectBean.setOldMethodologyGroup(projectSpecslInitiationList.get(0).getMethodologyGroup());
					migrateProjectBean.setNewMethodologyGroup(projectSpecslInitiationList.get(0).getMethodologyGroup());
					
					project.setMethodologyGroup(projectSpecslInitiationList.get(0).getMethodologyGroup());
				}
				else
				{
					project.setMethodologyGroup(project.getMethodologyGroup());
					migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
					migrateProjectBean.setNewMethodologyGroup(project.getMethodologyGroup());
				}
				
				/*if(projectSpecslInitiationList.get(0).getCategoryType()!=null && projectSpecslInitiationList.get(0).getCategoryType().size()> 0)
				{
					migrateProjectBean.setOldCategoryType(projectSpecslInitiationList.get(0).getCategoryType());
					migrateProjectBean.setNewCategoryType(projectSpecslInitiationList.get(0).getCategoryType());
					
					project.setCategoryType(projectSpecslInitiationList.get(0).getCategoryType());
					
				}
				else
				{
					migrateProjectBean.setOldCategoryType(project.getCategoryType());
					migrateProjectBean.setNewCategoryType(project.getCategoryType());
					
					project.setCategoryType(project.getCategoryType());
				}*/
				
				project.setCategoryType(project.getCategoryType());
				
				migrateProjectBean.setOldCategoryType(project.getCategoryType());
				migrateProjectBean.setNewCategoryType(project.getCategoryType());
				
				if(projectSpecslInitiationList.get(0).getStartDate()!=null)
				{
					project.setStartDate(projectSpecslInitiationList.get(0).getStartDate());
				}
				else
				{
					project.setStartDate(project.getStartDate());
				}
				
				if(projectSpecslInitiationList.get(0).getEndDate()!=null)
				{
					project.setEndDate(projectSpecslInitiationList.get(0).getEndDate());
				}
				else
				{
					project.setEndDate(project.getEndDate());
				}
				
			}
			else if(proposalInitiationList!=null && proposalInitiationList.size()>0)
			{
				if(proposalInitiationList.get(0).getBrand()!=null && SynchroGlobal.getAllBrands().get(proposalInitiationList.get(0).getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(proposalInitiationList.get(0).getBrand().intValue()).equals("Multi-Brand"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(1);
					project.setBrand(new Long("-1"));
				}
				else if(proposalInitiationList.get(0).getBrand()!=null && SynchroGlobal.getAllBrands().get(proposalInitiationList.get(0).getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(proposalInitiationList.get(0).getBrand().intValue()).equals("Non-Brand Related"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(2);
					project.setBrand(new Long("-1"));
				}
				else
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(project.getBrand());
				}
				
				
				if(proposalInitiationList.get(0).getProposedMethodology()!=null)
				{
					project.setMethodologyDetails(proposalInitiationList.get(0).getProposedMethodology());
				}
				else
				{
					project.setMethodologyDetails(project.getProposedMethodology());
				}
				
				if(proposalInitiationList.get(0).getMethodologyType()!=null)
				{
					project.setMethodologyType(proposalInitiationList.get(0).getMethodologyType());
				}
				else
				{
					project.setMethodologyType(project.getMethodologyType());
				}
				
				if(proposalInitiationList.get(0).getMethodologyGroup()!=null)
				{
					migrateProjectBean.setOldMethodologyGroup(proposalInitiationList.get(0).getMethodologyGroup());
					migrateProjectBean.setNewMethodologyGroup(proposalInitiationList.get(0).getMethodologyGroup());
					
					project.setMethodologyGroup(proposalInitiationList.get(0).getMethodologyGroup());
				}
				else
				{
					project.setMethodologyGroup(project.getMethodologyGroup());
					migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
					migrateProjectBean.setNewMethodologyGroup(project.getMethodologyGroup());
				}
				
				if(proposalInitiationList.get(0).getStartDate()!=null)
				{
					project.setStartDate(proposalInitiationList.get(0).getStartDate());
				}
				else
				{
					project.setStartDate(project.getStartDate());
				}
				
				if(proposalInitiationList.get(0).getEndDate()!=null)
				{
					project.setEndDate(proposalInitiationList.get(0).getEndDate());
				}
				else
				{
					project.setEndDate(project.getEndDate());
				}
				
				project.setCategoryType(project.getCategoryType());
				
				migrateProjectBean.setOldCategoryType(project.getCategoryType());
				migrateProjectBean.setNewCategoryType(project.getCategoryType());
			}
	
			else
			{
				if(project.getBrand()!=null && SynchroGlobal.getAllBrands().get(project.getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(project.getBrand().intValue()).equals("Multi-Brand"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(1);
					project.setBrand(new Long("-1"));
				}
				else if(project.getBrand()!=null && SynchroGlobal.getAllBrands().get(project.getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(project.getBrand().intValue()).equals("Non-Brand Related"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(2);
					project.setBrand(new Long("-1"));
				}
				else
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(project.getBrand());
				}
				
			
					project.setMethodologyDetails(project.getProposedMethodology());
					project.setMethodologyType(project.getMethodologyType());
				
					
					if(project.getMethodologyGroup()!=null)
					{
						migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
						migrateProjectBean.setNewMethodologyGroup(project.getMethodologyGroup());
						
						project.setMethodologyGroup(project.getMethodologyGroup());
					}
					else
					{
						project.setMethodologyGroup(project.getMethodologyGroup());
						migrateProjectBean.setOldMethodologyGroup(project.getMethodologyGroup());
						migrateProjectBean.setNewMethodologyGroup(project.getMethodologyGroup());
					}
					
					project.setCategoryType(project.getCategoryType());
					
					migrateProjectBean.setOldCategoryType(project.getCategoryType());
					migrateProjectBean.setNewCategoryType(project.getCategoryType());
					
					project.setStartDate(project.getStartDate());
					project.setEndDate(project.getEndDate());
			
				
				
			}
						
			
			//project.setBudgetLocation(endMarketDetails.get(0).getEndMarketID().intValue());
			
			String projectManagerName = "";
			try
			{
				projectManagerName = userManager.getUser(project.getProjectOwner()).getName();
			}
			catch(Exception e)
			{
				LOG.info("User Not Found ===>"+endMarketDetails.get(0).getSpiContact());
				
			}
			
			project.setProjectManagerName(projectManagerName);
			project.setFieldWorkStudy(2);
			
			//updateProjectsql.append(" newsynchro=1,");
			project.setFundingMarkets(null);
			project.setEndMarketFunding(null);
			project.setEuMarketConfirmation(null);
			
			//Total Cost should not be blank otherwise it will impact other functionalities
			/*if(endMarketDetails.get(0)!=null && endMarketDetails.get(0).getInitialCost()!=null)
			{
				project.setTotalCost(endMarketDetails.get(0).getInitialCost());
			}
			else
			{
				project.setTotalCost(new BigDecimal("0"));
			}*/
			
			project.setTotalCost(migrateProjectBean.getTotalCost());
			int defaultCurrency = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1);
			project.setTotalCostCurrency(new Long(defaultCurrency));
		//	project.setMethodologyDetails(project.getProposedMethodology());
			//
			
			synchroProjectManagerNew.updateProjectMigrate(project);
			
		
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new Exception();
		}
		
	}
    
    public void migrateProjectFieldWork(MigrateProjectBean migrateProjectBean)
    {
    	
			project = synchroProjectManagerNew.get(migrateProjectBean.getRefSynchroCode());
    		Project fieldWorkProject = new Project();
			fieldWorkProject.setName(project.getName());
			fieldWorkProject.setDescription(project.getDescription());
			fieldWorkProject.setDescriptionText(project.getDescriptionText());
			fieldWorkProject.setCategoryType(project.getCategoryType());
			fieldWorkProject.setMethodologyType(project.getMethodologyType());
			fieldWorkProject.setMethodologyDetails(project.getMethodologyDetails());
			fieldWorkProject.setStartDate(project.getStartDate());
			fieldWorkProject.setEndDate(project.getEndDate());
			fieldWorkProject.setCreationBy(project.getCreationBy());
			fieldWorkProject.setCreationDate(project.getCreationDate());
			fieldWorkProject.setModifiedBy(project.getModifiedBy());
			fieldWorkProject.setModifiedDate(project.getModifiedDate());
			fieldWorkProject.setBudgetYear(project.getBudgetYear());
			//TODO: Need to update the Budget Location
			
			for(Integer em : SynchroGlobal.getEndMarkets().keySet())
			{
				if(SynchroGlobal.getEndMarkets().get(em).equals(migrateProjectBean.getBudgetLocation()))
				{
					fieldWorkProject.setBudgetLocation(em);
				}
			}
	
			
			//fieldWorkProject.setBudgetLocation(fundingMarket.getFundingMarketID().intValue());
			
			String projectManagerName = "";
			try
			{
				projectManagerName = userManager.getUser(project.getProjectOwner()).getName();
			}
			catch(Exception e)
			{
				LOG.info("User Not Found ===>"+project.getProjectOwner());
				
			}
			fieldWorkProject.setProjectManagerName(projectManagerName);
			fieldWorkProject.setFieldWorkStudy(1);
			
			//fieldWorkProject.setTotalCost(new BigDecimal("0"));
			
			fieldWorkProject.setTotalCost(migrateProjectBean.getTotalCost());
			int defaultCurrency = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1);
			fieldWorkProject.setTotalCostCurrency(new Long(defaultCurrency));
			
			fieldWorkProject.setBriefCreator(project.getBriefCreator());
			fieldWorkProject.setProjectType(SynchroGlobal.ProjectType.ENDMARKET.getId());
			fieldWorkProject.setProcessType(SynchroGlobal.ProjectProcessType.END_MARKET_FIELDWORK.getId());
			
			fieldWorkProject.setMethWaiverReq(project.getMethWaiverReq());
			
			fieldWorkProject.setBrandSpecificStudy(project.getBrandSpecificStudy());
			fieldWorkProject.setBrandSpecificStudyType(project.getBrandSpecificStudyType());
			fieldWorkProject.setBrand(project.getBrand());
			fieldWorkProject.setRefSynchroCode(migrateProjectBean.getRefSynchroCode());
			//fieldWorkProject.setStatus(new Long(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal()));
			// Status will also be picked from the excel
			Integer projectStatus= 1;
			if(migrateProjectBean.getStatus().equals(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.getValue()))
			{
				projectStatus = SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal();
			}
			if(migrateProjectBean.getStatus().equals(SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.getValue()))
			{
				projectStatus = SynchroGlobal.ProjectStatusNew.PROPOSAL_IN_PLANNING.ordinal();
			}
			if(migrateProjectBean.getStatus().equals(SynchroGlobal.ProjectStatusNew.IN_PROGRESS.getValue()))
			{
				projectStatus = SynchroGlobal.ProjectStatusNew.IN_PROGRESS.ordinal();
			}
			if(migrateProjectBean.getStatus().equals(SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.getValue()))
			{
				projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_REPORT.ordinal();
			}
			if(migrateProjectBean.getStatus().equals(SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.getValue()))
			{
				projectStatus = SynchroGlobal.ProjectStatusNew.COMPLETE_PROJECT_EVAL.ordinal();
			}
			if(migrateProjectBean.getStatus().equals(SynchroGlobal.ProjectStatusNew.CLOSE.getValue()))
			{
				projectStatus = SynchroGlobal.ProjectStatusNew.CLOSE.ordinal();
			}
			
			fieldWorkProject.setStatus(projectStatus.longValue());
			fieldWorkProject = synchroProjectManagerNew.updateProjectMigrate(fieldWorkProject);
			
			// Save the EndMarket Details for fieldworkProject
			EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(fieldWorkProject.getProjectID(), fieldWorkProject.getBudgetLocation().longValue());
			endMarketInvestmentDetail.setCreationBy(project.getCreationBy());
            endMarketInvestmentDetail.setModifiedBy(project.getModifiedBy());
            synchroProjectManagerNew.saveEndMarketDetail(endMarketInvestmentDetail);
            
            updateGrailProjectCostDetails(fieldWorkProject, migrateProjectBean);
			
		
    }
    
    public void updateGrailProjectMM_OLDA(Project project, List<EndMarketInvestmentDetail> endMarketDetails, List<ProjectInitiation> aboveMarketPIBInitiationList,
    		List<ProposalInitiation> aboveMarketProposalInitiationList,List<ProjectSpecsInitiation> aboveMarketPSInitiationList, MigrateProjectBean migrateProjectBean) throws Exception
	{
		StringBuilder updateProjectsql= new StringBuilder("");
		try
		{
			List<FundingInvestment> fundingInvestments = this.synchroProjectManager.getProjectInvestments(project.getProjectID());
			
			Integer budgetLocation = new Integer("0");
			Integer globalEndMarketFunding = new Integer("0");
			
			boolean isGlobalProject = false;
			boolean isRegionalProject = false;
			boolean isEndMarketProject = false;
			
			// For these fundingEnd Markets we need to create Fieldwork projects
			List<Long> fundingEndMarkets = new ArrayList<Long>();
			
			List<FundingInvestment> fundingEndMarketList = new ArrayList<FundingInvestment>();
			
			Integer endMarketFunding = new Integer("2");
			
			if(fundingInvestments!=null && fundingInvestments.size()>0)
			{
				for(FundingInvestment fi : fundingInvestments)
				{
					if(fi.getInvestmentType().intValue() == SynchroGlobal.InvestmentType.GlOBAL.getId())
					{
						isGlobalProject = true;
						budgetLocation = -1;
						globalEndMarketFunding = fi.getFundingMarketID().intValue();
					}
					if(fi.getInvestmentType().intValue() == SynchroGlobal.InvestmentType.REGION.getId())
					{
						isRegionalProject = true;
						budgetLocation = fi.getInvestmentTypeID().intValue() * -1;
						globalEndMarketFunding = fi.getFundingMarketID().intValue();
					}
					if(fi.getInvestmentType().intValue() == SynchroGlobal.InvestmentType.AREA.getId())
					{
						isRegionalProject = true;
					}
					if(fi.getInvestmentType().intValue() == SynchroGlobal.InvestmentType.COUNTRY.getId())
					{
						isEndMarketProject = true;
						
						if(fi.getEstimatedCost()!=null && fi.getEstimatedCost().doubleValue()>0)
						{
							fundingEndMarkets.add(fi.getFieldworkMarketID());
							fundingEndMarketList.add(fi);
							endMarketFunding = 1;
						}
						
						
					}
				}
			}
			
			if(isGlobalProject)
			{
				project.setProjectType(SynchroGlobal.ProjectType.GLOBAL.getId());
			}
			else if(isRegionalProject)
			{
				project.setProjectType(SynchroGlobal.ProjectType.REGIONAL.getId());
			}
			else
			{
				project.setProjectType(SynchroGlobal.ProjectType.ENDMARKET.getId());
			}
			
			project.setBudgetLocation(budgetLocation);
			project.setProcessType(SynchroUtils.getProjectProcessType(project.getProjectType(), globalEndMarketFunding.longValue(),globalEndMarketFunding));
			
			//List<ProjectSpecsInitiation> aboveMarketPSInitiationList = this.projectSpecsManager.getProjectSpecsInitiation(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
			//List<ProjectInitiation> aboveMarketPIBInitiationList = this.pibManager.getPIBDetails(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
			
			if(aboveMarketPSInitiationList!=null && aboveMarketPSInitiationList.size()>0 && aboveMarketPSInitiationList.get(0).getDeviationFromSM()==1)
			{
				project.setMethWaiverReq(1);
			}
			else if(aboveMarketPSInitiationList!=null && aboveMarketPSInitiationList.size()>0 && aboveMarketPSInitiationList.get(0).getDeviationFromSM()==0)
			{
				project.setMethWaiverReq(2);
			}
			else if(aboveMarketPIBInitiationList!=null && aboveMarketPIBInitiationList.size()>0 && aboveMarketPIBInitiationList.get(0).getDeviationFromSM()==1)
			{
				project.setMethWaiverReq(1);
			}
			else if(aboveMarketPIBInitiationList!=null && aboveMarketPIBInitiationList.size()>0 && aboveMarketPIBInitiationList.get(0).getDeviationFromSM()==0)
			{
				project.setMethWaiverReq(2);
			}
			else
			{
				project.setMethWaiverReq(-1);
			}
			
			
			// Take the Project Details from the latest stage
			if(aboveMarketPSInitiationList!=null && aboveMarketPSInitiationList.size()>0)
			{
				if(aboveMarketPSInitiationList.get(0).getBrand()!=null && SynchroGlobal.getAllBrands().get(aboveMarketPSInitiationList.get(0).getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(aboveMarketPSInitiationList.get(0).getBrand().intValue()).equals("Multi-Brand"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(1);
					project.setBrand(new Long("-1"));
				}
				else if(aboveMarketPSInitiationList.get(0).getBrand()!=null && SynchroGlobal.getAllBrands().get(aboveMarketPSInitiationList.get(0).getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(aboveMarketPSInitiationList.get(0).getBrand().intValue()).equals("Non-Brand Related"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(2);
					project.setBrand(new Long("-1"));
				}
				else
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(project.getBrand());
				}
				
				if(aboveMarketPSInitiationList.get(0).getProposedMethodology()!=null)
				{
					project.setMethodologyDetails(aboveMarketPSInitiationList.get(0).getProposedMethodology());
				}
				else
				{
					project.setMethodologyDetails(project.getProposedMethodology());
				}
				
				if(aboveMarketPSInitiationList.get(0).getMethodologyType()!=null)
				{
					project.setMethodologyType(aboveMarketPSInitiationList.get(0).getMethodologyType());
				}
				else
				{
					project.setMethodologyType(project.getMethodologyType());
				}
				
				if(aboveMarketPSInitiationList.get(0).getCategoryType()!=null && aboveMarketPSInitiationList.get(0).getCategoryType().size() > 0)
				{
					project.setCategoryType(aboveMarketPSInitiationList.get(0).getCategoryType());
				}
				else
				{
					project.setCategoryType(project.getCategoryType());
				}
				
				if(aboveMarketPSInitiationList.get(0).getStartDate()!=null)
				{
					project.setStartDate(aboveMarketPSInitiationList.get(0).getStartDate());
				}
				else
				{
					project.setStartDate(project.getStartDate());
				}
				
				if(aboveMarketPSInitiationList.get(0).getEndDate()!=null)
				{
					project.setEndDate(aboveMarketPSInitiationList.get(0).getEndDate());
				}
				else
				{
					project.setEndDate(project.getEndDate());
				}
			}
			else if(aboveMarketProposalInitiationList!=null && aboveMarketProposalInitiationList.size()>0)
			{
				if(aboveMarketProposalInitiationList.get(0).getBrand()!=null && SynchroGlobal.getAllBrands().get(aboveMarketProposalInitiationList.get(0).getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(aboveMarketProposalInitiationList.get(0).getBrand().intValue()).equals("Multi-Brand"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(1);
					project.setBrand(new Long("-1"));
				}
				else if(aboveMarketProposalInitiationList.get(0).getBrand()!=null && SynchroGlobal.getAllBrands().get(aboveMarketProposalInitiationList.get(0).getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(aboveMarketProposalInitiationList.get(0).getBrand().intValue()).equals("Non-Brand Related"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(2);
					project.setBrand(new Long("-1"));
				}
				else
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(project.getBrand());
				}
				
				if(aboveMarketProposalInitiationList.get(0).getProposedMethodology()!=null)
				{
					project.setMethodologyDetails(aboveMarketProposalInitiationList.get(0).getProposedMethodology());
				}
				else
				{
					project.setMethodologyDetails(project.getProposedMethodology());
				}
				
				if(aboveMarketProposalInitiationList.get(0).getMethodologyType()!=null)
				{
					project.setMethodologyType(aboveMarketProposalInitiationList.get(0).getMethodologyType());
				}
				else
				{
					project.setMethodologyType(project.getMethodologyType());
				}
				
				
				
				if(aboveMarketProposalInitiationList.get(0).getStartDate()!=null)
				{
					project.setStartDate(aboveMarketProposalInitiationList.get(0).getStartDate());
				}
				else
				{
					project.setStartDate(project.getStartDate());
				}
				
				if(aboveMarketProposalInitiationList.get(0).getEndDate()!=null)
				{
					project.setEndDate(aboveMarketProposalInitiationList.get(0).getEndDate());
				}
				else
				{
					project.setEndDate(project.getEndDate());
				}
			}
	
			else
			{
				if(project.getBrand()!=null && SynchroGlobal.getAllBrands().get(project.getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(project.getBrand().intValue()).equals("Multi-Brand"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(1);
					project.setBrand(new Long("-1"));
				}
				else if(project.getBrand()!=null && SynchroGlobal.getAllBrands().get(project.getBrand().intValue())!=null && SynchroGlobal.getAllBrands().get(project.getBrand().intValue()).equals("Non-Brand Related"))
				{
					
					project.setBrandSpecificStudy(2);
					project.setBrandSpecificStudyType(2);
					project.setBrand(new Long("-1"));
				}
				else
				{
					
					project.setBrandSpecificStudy(1);
					project.setBrandSpecificStudyType(-1);
					project.setBrand(project.getBrand());
				}
				
				project.setMethodologyDetails(project.getProposedMethodology());
				project.setMethodologyType(project.getMethodologyType());
			
				project.setCategoryType(project.getCategoryType());
				project.setStartDate(project.getStartDate());
				project.setEndDate(project.getEndDate());
			}
				
			
			
			//project.setBudgetLocation(endMarketDetails.get(0).getEndMarketID().intValue());
			
			String projectManagerName = "";
			try
			{
				projectManagerName = userManager.getUser(project.getProjectOwner()).getName();
			}
			catch(Exception e)
			{
				LOG.info("User Not Found ===>"+project.getProjectOwner());
			}
			
			project.setProjectManagerName(projectManagerName);
			project.setFieldWorkStudy(2);
			
			//updateProjectsql.append(" newsynchro=1,");
			project.setFundingMarkets(fundingEndMarkets);
			project.setEndMarketFunding(endMarketFunding);
			project.setEuMarketConfirmation(null);
		//	project.setTotalCost(endMarketDetails.get(0).getInitialCost());
			
			//Total Cost should not be blank otherwise it will impact other functionalities
		/*	if(endMarketDetails.get(0)!=null && endMarketDetails.get(0).getInitialCost()!=null)
			{
				project.setTotalCost(endMarketDetails.get(0).getInitialCost());
			}
			else
			{
				project.setTotalCost(new BigDecimal("0"));
			}
			project.setTotalCostCurrency(endMarketDetails.get(0).getInitialCostCurrency());*/
			
			project.setTotalCost(migrateProjectBean.getTotalCost());
			int defaultCurrency = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1);
			project.setTotalCostCurrency(new Long(defaultCurrency));
			
		//	project.setMethodologyDetails(project.getProposedMethodology());
	
			synchroProjectManagerNew.updateProjectMigrate(project);
			
			// Update the Funding Markets for the Project
			for(Long fundingMarketID : project.getFundingMarkets())
			{
	            if(fundingMarketID!=null)
	            {
	            	final EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(project.getProjectID(), fundingMarketID);
	            	endMarketInvestmentDetail.setIsFundingMarket(true);
	                synchroProjectManagerNew.updateFundingEndMarkets(endMarketInvestmentDetail);
	            }   
			}
			
			// Now create the Fieldwork Projects.
			
			for(FundingInvestment fundingMarket:fundingEndMarketList)
			{
				Project fieldWorkProject = new Project();
				fieldWorkProject.setName(project.getName());
				fieldWorkProject.setDescription(project.getDescription());
				fieldWorkProject.setDescriptionText(project.getDescriptionText());
				fieldWorkProject.setCategoryType(project.getCategoryType());
				fieldWorkProject.setMethodologyType(project.getMethodologyType());
				fieldWorkProject.setMethodologyDetails(project.getMethodologyDetails());
				fieldWorkProject.setStartDate(project.getStartDate());
				fieldWorkProject.setEndDate(project.getEndDate());
				fieldWorkProject.setCreationBy(project.getCreationBy());
				fieldWorkProject.setCreationDate(project.getCreationDate());
				fieldWorkProject.setModifiedBy(project.getModifiedBy());
				fieldWorkProject.setModifiedDate(project.getModifiedDate());
				fieldWorkProject.setBudgetYear(project.getBudgetYear());
				//TODO: Need to update the Budget Location
				fieldWorkProject.setBudgetLocation(fundingMarket.getFundingMarketID().intValue());
				fieldWorkProject.setProjectManagerName(projectManagerName);
				fieldWorkProject.setFieldWorkStudy(1);
				
				fieldWorkProject.setTotalCost(fundingMarket.getEstimatedCost());
				
				fieldWorkProject.setBriefCreator(project.getBriefCreator());
				fieldWorkProject.setProjectType(SynchroGlobal.ProjectType.ENDMARKET.getId());
				fieldWorkProject.setProcessType(SynchroGlobal.ProjectProcessType.END_MARKET_FIELDWORK.getId());
				
				fieldWorkProject.setMethWaiverReq(project.getMethWaiverReq());
				
				fieldWorkProject.setBrandSpecificStudy(project.getBrandSpecificStudy());
				fieldWorkProject.setBrandSpecificStudyType(project.getBrandSpecificStudyType());
				fieldWorkProject.setBrand(project.getBrand());
				fieldWorkProject.setRefSynchroCode(project.getProjectID());
				fieldWorkProject.setStatus(new Long(SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal()));
				
				
				fieldWorkProject = synchroProjectManagerNew.updateProjectMigrate(fieldWorkProject);
				
				// Save the EndMarket Details for fieldworkProject
				EndMarketInvestmentDetail endMarketInvestmentDetail = new EndMarketInvestmentDetail(fieldWorkProject.getProjectID(), fundingMarket.getFieldworkMarketID());
				endMarketInvestmentDetail.setCreationBy(project.getCreationBy());
	            endMarketInvestmentDetail.setModifiedBy(project.getModifiedBy());
	            synchroProjectManagerNew.saveEndMarketDetail(endMarketInvestmentDetail);
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
    		throw new Exception();
		}
			
	
	
		
		
}
  
    public List<ProjectCostDetailsBean> updateGrailProjectCostDetails(Project project, MigrateProjectBean migrateProjectBean)
    {
    	List<ProjectCostDetailsBean> projectCostDetails = new ArrayList<ProjectCostDetailsBean>();
    	
    	
    	List<MigrateProjectCostBean> migrateProjectCostBeanList = migrateProjectBean.getMigrateProjectCostBean();
    	
    	for(MigrateProjectCostBean migrateProjectCostBean : migrateProjectCostBeanList)
    	{
	    	
    		ProjectCostDetailsBean pcb = new ProjectCostDetailsBean();
        	pcb.setProjectId(project.getProjectID());
        	
    		// It should fetch the inactive agencies also
        	for(Integer i : SynchroGlobal.getAllResearchAgency().keySet())
	        {
	            // This is done as there are spaces in the Agency Names at the end.
    			//if(SynchroGlobal.getResearchAgency().get(i).equals(migrateProjectCostBean.getResearchAgency()))
    			if(SynchroGlobal.getAllResearchAgency().get(i).trim().equals(migrateProjectCostBean.getResearchAgency().trim()))
	            {
	            	pcb.setAgencyId(new Long(i));
	            }
	        }
	    	
	    	if(StringUtils.isNotBlank(migrateProjectCostBean.getCostComponent()))
	    	{
	    		if(migrateProjectCostBean.getCostComponent().equals("Coordination"))
		    	{
		    		pcb.setCostComponent(new Integer("1"));
		    	}
		    	else
		    	{
		    		pcb.setCostComponent(new Integer("2"));
		    	}
	    	}
	    	// This will be set as Unclassified Cost Component
	    	else
	    	{
	    		pcb.setCostComponent(new Integer("3"));
	    	}
	    	
	    	for(Integer i : SynchroGlobal.getCurrencies().keySet())
	        {
	            if(SynchroGlobal.getCurrencies().get(i).equals(migrateProjectCostBean.getCostCurrency()))
	            {
	            	pcb.setCostCurrency(i);
	            }
	        }
	    	
	    	pcb.setEstimatedCost(migrateProjectCostBean.getEstimatedCost());
	    	projectCostDetails.add(pcb);
    	}
    	
    	
    	return projectCostDetails;
    	//synchroProjectManagerNew.saveProjectCostDetails(projectCostDetails);
    	
    }
    
    public boolean updateGrailPIBDetails(Project project,List<ProjectInitiation> projectInitiationList,  MigrateProjectBean migrateProjectBean) throws Exception
    {
    	ProjectInitiation pibInitiation = new ProjectInitiation();
    	pibInitiation.setProjectID(projectInitiationList.get(0).getProjectID());
    	
    	boolean addBriefDummyAttachment = false;
    	
    	StringBuilder brief = new StringBuilder("");
    	
    	if(StringUtils.isNotBlank(projectInitiationList.get(0).getBizQuestion()))
    	{
    		brief.append("<b>Business Question:</b> " +projectInitiationList.get(0).getBizQuestion());
    	}
    	if(StringUtils.isNotBlank(projectInitiationList.get(0).getResearchObjective()))
    	{
    		brief.append("<b>Research Objective:</b> "+projectInitiationList.get(0).getResearchObjective());
    	}
    	if(StringUtils.isNotBlank(projectInitiationList.get(0).getActionStandard()))
    	{
    		brief.append("<b>Action Standard:</b> "+projectInitiationList.get(0).getActionStandard());
    	}
    	/*brief.append("RD: "+projectInitiationList.get(0).getResearchDesign());
    	brief.append("SP: "+projectInitiationList.get(0).getSampleProfile());
    	brief.append("SM: "+projectInitiationList.get(0).getStimulusMaterial());
    	brief.append("O: "+projectInitiationList.get(0).getOthers());
    	*/
    	pibInitiation.setBrief(brief.toString());
    	
    	StringBuilder briefText = new StringBuilder("");
    	briefText.append(projectInitiationList.get(0).getBizQuestionText());
    	briefText.append(" "+projectInitiationList.get(0).getResearchObjectiveText());
    	briefText.append(" "+projectInitiationList.get(0).getActionStandardText());
    	/*briefText.append(" "+projectInitiationList.get(0).getResearchDesignText());
    	briefText.append(" "+projectInitiationList.get(0).getSampleProfileText());
    	briefText.append(" "+projectInitiationList.get(0).getStimulusMaterialText());
    	briefText.append(" "+projectInitiationList.get(0).getOthersText());*/
    	
    	pibInitiation.setBriefText(briefText.toString());
    	
    	// This check box is not required to be migrated as part of http://redmine.nvish.com/redmine/issues/500
    	
    	/*if(projectInitiationList.get(0).getLegalApprovalNotReq())
    	{
    		pibInitiation.setLegalSignOffRequired(1);
    	}*/
    	
    	// Legal Approval date will be fetched from PibLegalApprovalDate Field
    	if(projectInitiationList.get(0).getPibLegalApprovalDate()!=null)
    	{
    		pibInitiation.setLegalApprovalDate(projectInitiationList.get(0).getPibLegalApprovalDate());
    		
    		migrateProjectBean.setOldPibDateOfRequestForLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    		migrateProjectBean.setOldPibDateOfLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    		
    		migrateProjectBean.setNewPibDateOfRequestForLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    		migrateProjectBean.setNewPibDateOfLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    	}
    	
    	try
    	{
	    	if(project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId())
	    	{
	    		pibInitiation.setLegalApprovalDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
	    		pibInitiation.setLegalApprovalStatus(2);
	    		pibInitiation.setBriefLegalApprover(dummyUserId);
	    	}
	    	if(project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId())
	    	{
	    		pibInitiation.setLegalApprovalDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
	    		pibInitiation.setLegalApprovalStatus(2);
	    		pibInitiation.setBriefLegalApproverOffline(dummyUserName);
	    		addDummyAttachmentEmail(project.getProjectID(), SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF_LEGAL_APPROVAL.getId().longValue());
	    	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new Exception();
    	}
    	
    	// Legal Approvers to be migrated Separately
    	// First that Legal Approver should be added in the Synchro Group as user and then be made Legal Approver.
    	/*if(projectInitiationList.get(0).getLegalApprover()!=null)
    	{
    		String oldUserName = projectInitiationList.get(0).getLegalApprover();
    		List<User> synchroUsers = getSynchroUtils().getSynchroGroupUsers();
    		for(User user : synchroUsers)
    		{
    			String firstName = user.getFirstName();
    			String lastName = user.getLastName();
    			String name = user.getName();
    			
    			if(name!=null && name.equalsIgnoreCase(oldUserName))
    			{
    				migrateProjectBean.setOldLAName(projectInitiationList.get(0).getLegalApprover());
    	    		migrateProjectBean.setNewLAName(name);
    	    		pibInitiation.setBriefLegalApprover(user.getID());
    			}
    			
    		}
    		
    	}*/
    	//pibInitiation.setLegalApprovalStatus(legalApprovalStatus)
    	
    	pibInitiation.setModifiedBy(projectInitiationList.get(0).getModifiedBy());
    	pibInitiation.setModifiedDate(projectInitiationList.get(0).getModifiedDate());
    	pibInitiation.setStatus(projectInitiationList.get(0).getStatus());
    	
    	pibManagerNew.updatePIBDetailsNew(pibInitiation);
    	
    	
    	//PIB Attachments
    	List<EndMarketInvestmentDetail> endMarketDetails = this.synchroProjectManager.getEndMarketDetails(project.getProjectID());
    	attachmentMap = this.pibManagerNew.getDocumentAttachment(project.getProjectID(),endMarketDetails.get(0).getEndMarketID()); 
    	
		List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId()));
		}
	/*	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId()));
		}*/
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
           	 pibManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId().longValue(), attachmentUser.get(ab.getID()));
            }
            catch(Exception e)
            {
                Log.error("Error while copying exception for PIB for Project --"+ project.getProjectID() + ab.getID() + "NAME --" + ab.getName());
                throw new Exception();
            }

        }
     	
	
		if(attchBeanList!=null && attchBeanList.size() >0)
		{
			
		}
		else
		{
			addBriefDummyAttachment = true;
		}
		
		
		// If Agency Waiver has been raised for the project in older synchro, then it is mandatory to have an attachment for Waiver Summary Field
		PIBMethodologyWaiver pibKantarMethodologyWaiver = pibManagerNew.getPIBKantarMethodologyWaiver(project.getProjectID(), endMarketDetails.get(0).getEndMarketID());
		if(pibKantarMethodologyWaiver!=null)
		{
			addDummyAttachment( project.getProjectID(),SynchroGlobal.SynchroAttachmentObject.AGENCY_WAIVER.getId().longValue());
		}
		
		return addBriefDummyAttachment;
		
    }
    
    public boolean updateGrailPIBDetailsMM(Project project,List<ProjectInitiation> projectInitiationList,  MigrateProjectBean migrateProjectBean) throws Exception
    {
    	ProjectInitiation pibInitiation = new ProjectInitiation();
    	pibInitiation.setProjectID(projectInitiationList.get(0).getProjectID());
    	
    	boolean addBriefDummyAttachment = false;
    	
    	StringBuilder brief = new StringBuilder("");
    	
    	if(StringUtils.isNotBlank(projectInitiationList.get(0).getBizQuestion()))
    	{
    		if(projectInitiationList.get(0).getBizQuestion().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			brief.append("<p>");
    		}
    		brief.append("<b>Business Question:</b> " +projectInitiationList.get(0).getBizQuestion());
    		if(brief.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			brief.append("</p>");
    		}
    		
    		
    	}
    	if(StringUtils.isNotBlank(projectInitiationList.get(0).getResearchObjective()))
    	{
    		if(projectInitiationList.get(0).getResearchObjective().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			brief.append("<p>");
    		}
    		brief.append("<b>Research Objective:</b> " +projectInitiationList.get(0).getResearchObjective());
    		if(brief.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			brief.append("</p>");
    		}
    	}
    	if(StringUtils.isNotBlank(projectInitiationList.get(0).getActionStandard()))
    	{
    		if(projectInitiationList.get(0).getActionStandard().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			brief.append("<p>");
    		}
    		brief.append("<b>Action Standard:</b> " +projectInitiationList.get(0).getActionStandard());
    		if(brief.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			brief.append("</p>");
    		}
    	}
    	
    	pibInitiation.setBrief(brief.toString());
    	
    	StringBuilder briefText = new StringBuilder("");
    	briefText.append(projectInitiationList.get(0).getBizQuestionText());
    	briefText.append(" "+projectInitiationList.get(0).getResearchObjectiveText());
    	briefText.append(" "+projectInitiationList.get(0).getActionStandardText());
    
    	
    	pibInitiation.setBriefText(briefText.toString());
    	
    	// This check box is not required to be migrated as part of http://redmine.nvish.com/redmine/issues/500
    	
    	/*if(projectInitiationList.get(0).getLegalApprovalNotReq())
    	{
    		pibInitiation.setLegalSignOffRequired(1);
    	}*/
    	
    	// Legal Approval date will be fetched from PibLegalApprovalDate Field
    	if(projectInitiationList.get(0).getPibLegalApprovalDate()!=null)
    	{
    		pibInitiation.setLegalApprovalDate(projectInitiationList.get(0).getPibLegalApprovalDate());
    		
    		migrateProjectBean.setOldPibDateOfRequestForLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    		migrateProjectBean.setOldPibDateOfLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    		
    		migrateProjectBean.setNewPibDateOfRequestForLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    		migrateProjectBean.setNewPibDateOfLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    	}
    	
    	try
    	{
	    	if(project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId())
	    	{
	    		pibInitiation.setLegalApprovalDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
	    		pibInitiation.setLegalApprovalStatus(2);
	    		pibInitiation.setBriefLegalApprover(dummyUserId);
	    	}
	    	if(project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId())
	    	{
	    		pibInitiation.setLegalApprovalDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
	    		pibInitiation.setLegalApprovalStatus(2);
	    		pibInitiation.setBriefLegalApproverOffline(dummyUserName);
	    		addDummyAttachmentEmail(project.getProjectID(), SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF_LEGAL_APPROVAL.getId().longValue());
	    	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new Exception();
    	}
    	
    
    	
    	pibInitiation.setModifiedBy(projectInitiationList.get(0).getModifiedBy());
    	pibInitiation.setModifiedDate(projectInitiationList.get(0).getModifiedDate());
    	pibInitiation.setStatus(projectInitiationList.get(0).getStatus());
    	
    	// This is done to remove all the other End Markets apart from Above End Market 
    	pibManagerNew.deletePIBEndMarket(project.getProjectID());
    	pibManagerNew.updatePIBDetailsNew(pibInitiation);
    	
    	
    	//PIB Attachments
    	List<EndMarketInvestmentDetail> endMarketDetails = this.synchroProjectManager.getEndMarketDetails(project.getProjectID());
    	attachmentMap = this.pibManagerNew.getDocumentAttachment(project.getProjectID(),SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID); 
    	
		List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId()));
		}
	
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
           	 pibManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId().longValue(), attachmentUser.get(ab.getID()));
            }
            catch(Exception e)
            {
                Log.error("Error while copying exception for PIB for Project --"+ project.getProjectID() + ab.getID() + "NAME --" + ab.getName());
                throw new Exception();
            }

        }
     	
	
		if(attchBeanList!=null && attchBeanList.size() >0)
		{
			
		}
		else
		{
			addBriefDummyAttachment = true;
		}
		
		
		// If Agency Waiver has been raised for the project in older synchro, then it is mandatory to have an attachment for Waiver Summary Field
		PIBMethodologyWaiver pibKantarMethodologyWaiver = pibManagerNew.getPIBKantarMethodologyWaiver(project.getProjectID(), endMarketDetails.get(0).getEndMarketID());
		if(pibKantarMethodologyWaiver!=null)
		{
			addDummyAttachment( project.getProjectID(),SynchroGlobal.SynchroAttachmentObject.AGENCY_WAIVER.getId().longValue());
		}
		
		return addBriefDummyAttachment;
		
    }
    
    public boolean createGrailPIBDetailsMM(Project project, Project newProject, List<ProjectInitiation> projectInitiationList,  MigrateProjectBean migrateProjectBean) throws Exception
    {
    	ProjectInitiation pibInitiation = projectInitiationList.get(0);
    	pibInitiation.setProjectID(newProject.getProjectID());
    	
    	boolean addBriefDummyAttachment = false;
    	
    	
    	// Legal Approval date will be fetched from PibLegalApprovalDate Field
    	if(projectInitiationList.get(0).getPibLegalApprovalDate()!=null)
    	{
    		pibInitiation.setLegalApprovalDate(projectInitiationList.get(0).getPibLegalApprovalDate());
    		
    		migrateProjectBean.setOldPibDateOfRequestForLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    		migrateProjectBean.setOldPibDateOfLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    		
    		migrateProjectBean.setNewPibDateOfRequestForLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    		migrateProjectBean.setNewPibDateOfLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    	}
    	
    	try
    	{
	    	if(newProject.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId())
	    	{
	    		pibInitiation.setLegalApprovalDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
	    		pibInitiation.setLegalApprovalStatus(2);
	    		pibInitiation.setBriefLegalApprover(dummyUserId);
	    	}
	    	if(newProject.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId())
	    	{
	    		pibInitiation.setLegalApprovalDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
	    		pibInitiation.setLegalApprovalStatus(2);
	    		pibInitiation.setBriefLegalApproverOffline(dummyUserName);
	    		addDummyAttachmentEmail(newProject.getProjectID(), SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF_LEGAL_APPROVAL.getId().longValue());
	    	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new Exception();
    	}
    	
    	pibInitiation.setCreationBy(getUser().getID());
    	pibInitiation.setCreationDate(System.currentTimeMillis());

    	
    	//pibInitiation.setModifiedBy(projectInitiationList.get(0).getModifiedBy());
    	//pibInitiation.setModifiedDate(projectInitiationList.get(0).getModifiedDate());
    	
    	pibInitiation.setModifiedBy(getUser().getID());
    	pibInitiation.setModifiedDate(System.currentTimeMillis());
          
    	pibInitiation.setStatus(projectInitiationList.get(0).getStatus());
    	
    	pibManagerNew.savePIBDetails(pibInitiation);
    	pibManagerNew.updatePIBDetailsNew(pibInitiation);
    	
    	
    	//PIB Attachments
    	
    	attachmentMap = this.pibManagerNew.getDocumentAttachment(project.getProjectID(), new Long("-1")); 
    	
		List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId()));
		}
		
	
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
           	 pibManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), newProject.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId().longValue(), attachmentUser.get(ab.getID()));
            }
            catch(Exception e)
            {
                Log.error("Error while copying exception for PIB for New Project --"+ newProject.getProjectID() + ab.getID() + "NAME --" + ab.getName());
                throw new Exception();
            }

        }
     	
	
		if(attchBeanList!=null && attchBeanList.size() >0)
		{
			
		}
		else
		{
			addBriefDummyAttachment = true;
		}
		
		
		// If Agency Waiver has been raised for the project in older synchro, then it is mandatory to have an attachment for Waiver Summary Field
	/*	PIBMethodologyWaiver pibKantarMethodologyWaiver = pibManagerNew.getPIBKantarMethodologyWaiver(project.getProjectID(), endMarketDetails.get(0).getEndMarketID());
		if(pibKantarMethodologyWaiver!=null)
		{
			addDummyAttachment( project.getProjectID(),SynchroGlobal.SynchroAttachmentObject.AGENCY_WAIVER.getId().longValue());
		}
		*/
		return addBriefDummyAttachment;
		
    }
    public void updateGrailPIBDetailsMMOLD(Project project,List<ProjectInitiation> projectInitiationList,  MigrateProjectBean migrateProjectBean) throws Exception
    {
    	
    	
    	ProjectInitiation pibInitiation = new ProjectInitiation();
    	pibInitiation.setProjectID(projectInitiationList.get(0).getProjectID());
    	
    	StringBuilder brief = new StringBuilder("");
    	    		
    	if(StringUtils.isNotBlank(projectInitiationList.get(0).getBizQuestion()))
    	{
    		if(projectInitiationList.get(0).getBizQuestion().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			brief.append("<p>");
    		}
    		brief.append("<b>Business Question:</b> " +projectInitiationList.get(0).getBizQuestion());
    		if(brief.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			brief.append("</p>");
    		}
    		
    		
    	}
    	if(StringUtils.isNotBlank(projectInitiationList.get(0).getResearchObjective()))
    	{
    		if(projectInitiationList.get(0).getResearchObjective().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			brief.append("<p>");
    		}
    		brief.append("<b>Research Objective:</b> " +projectInitiationList.get(0).getResearchObjective());
    		if(brief.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			brief.append("</p>");
    		}
    	}
    	if(StringUtils.isNotBlank(projectInitiationList.get(0).getActionStandard()))
    	{
    		if(projectInitiationList.get(0).getActionStandard().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			brief.append("<p>");
    		}
    		brief.append("<b>Action Standard:</b> " +projectInitiationList.get(0).getActionStandard());
    		if(brief.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			brief.append("</p>");
    		}
    	}
    	
   
    	pibInitiation.setBrief(brief.toString());
    	
    	StringBuilder briefText = new StringBuilder("");
    	briefText.append(projectInitiationList.get(0).getBizQuestionText());
    	briefText.append(" "+projectInitiationList.get(0).getResearchObjectiveText());
    	briefText.append(" "+projectInitiationList.get(0).getActionStandardText());
   
    	pibInitiation.setBriefText(briefText.toString());
    	
    	// This check box is not required to be migrated as part of http://redmine.nvish.com/redmine/issues/500
    	
    	/*if(projectInitiationList.get(0).getLegalApprovalNotReq())
    	{
    		pibInitiation.setLegalSignOffRequired(1);
    	}*/
    	
    	//pibInitiation.setLegalApprovalStatus(legalApprovalStatus)
    	
    	// Legal Approval date will be fetched from PibLegalApprovalDate Field
    	if(projectInitiationList.get(0).getPibLegalApprovalDate()!=null)
    	{
    		pibInitiation.setLegalApprovalDate(projectInitiationList.get(0).getPibLegalApprovalDate());
    		
    		migrateProjectBean.setOldPibDateOfRequestForLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    		migrateProjectBean.setOldPibDateOfLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    		
    		migrateProjectBean.setNewPibDateOfRequestForLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    		migrateProjectBean.setNewPibDateOfLA(projectInitiationList.get(0).getPibLegalApprovalDate());
    	}
    	
    	// Legal Approvers to be migrated Separately
    	if(projectInitiationList.get(0).getLegalApprover()!=null)
    	{
    		migrateProjectBean.setOldLAName(projectInitiationList.get(0).getLegalApprover());
    		migrateProjectBean.setNewLAName("TO BE DONE SEPARATELY");
    	}
    	
    	pibInitiation.setModifiedBy(projectInitiationList.get(0).getModifiedBy());
    	pibInitiation.setModifiedDate(projectInitiationList.get(0).getModifiedDate());
    	pibInitiation.setStatus(projectInitiationList.get(0).getStatus());
    	
    	pibManagerNew.updatePIBDetailsNew(pibInitiation);
    	
    	
    	//PIB Attachments
    	//attachmentMap = this.pibManagerNew.getDocumentAttachment(projectID,endMarketDetails.get(0).getEndMarketID()); 
    	attachmentMap = this.pibManagerNew.getDocumentAttachment(project.getProjectID(),SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    	
		List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.BUSINESS_QUESTION.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_OBJECTIVE.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.ACTION_STANDARD.getId()));
		}
		/*if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHER_REPORTING_REQUIREMENT.getId()));
		}*/
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
           	 pibManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId().longValue(), attachmentUser.get(ab.getID()));
            }
            catch(Exception e)
            {
                Log.error("Error while copying exception for PIB for Project --"+ project.getProjectID() + ab.getID() + "NAME --" + ab.getName());
                throw new Exception();
            }

        }
     	
    }
    
    public boolean  updateGrailProposalDetails(Project project,List<ProposalInitiation> proposalInitiationList) throws Exception
    {
    	ProposalInitiation proposalInitiation = new ProposalInitiation();
    	proposalInitiation.setProjectID(proposalInitiationList.get(0).getProjectID());
    	
    	boolean addProposalDummyAttachment = false;
    	
    	StringBuilder proposal = new StringBuilder("");
     	
    	if(StringUtils.isNotBlank(proposalInitiationList.get(0).getResearchDesign()))
    	{
    		if(proposalInitiationList.get(0).getResearchDesign().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("<p>");
    		}
    		proposal.append("<b>Methodology Approach and Research Design:</b> " +proposalInitiationList.get(0).getResearchDesign());
    		if(proposal.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("</p>");
    		}
    	}
    	if(StringUtils.isNotBlank(proposalInitiationList.get(0).getSampleProfile()))
    	{
    		if(proposalInitiationList.get(0).getSampleProfile().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("<p>");
    		}
    		proposal.append("<b>Sample Profile:</b> "+proposalInitiationList.get(0).getSampleProfile());
    		if(proposal.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("</p>");
    		}
    	}
    	
    	if(StringUtils.isNotBlank(proposalInitiationList.get(0).getOthers()))
    	{
    		if(proposalInitiationList.get(0).getOthers().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("<p>");
    		}
    		proposal.append("<b>Other Comments:</b> "+proposalInitiationList.get(0).getOthers());
    		if(proposal.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("</p>");
    		}
    	}
    	
    	if(StringUtils.isNotBlank(proposalInitiationList.get(0).getProposalCostTemplate()))
    	{
    		if(proposalInitiationList.get(0).getProposalCostTemplate().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("<p>");
    		}
    		proposal.append("<b>Proposal and Cost Template:</b> "+proposalInitiationList.get(0).getProposalCostTemplate());
    		if(proposal.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("</p>");
    		}
    	}
    	
    	
    	proposalInitiation.setProposal(proposal.toString());
    	
    	
    	StringBuilder proposalText = new StringBuilder("");
    	proposalText.append(proposalInitiationList.get(0).getResearchDesignText());
    	proposalText.append(" "+proposalInitiationList.get(0).getSampleProfileText());
    	proposalText.append(" "+proposalInitiationList.get(0).getProposalCostTemplate());
    	
    	proposalInitiation.setProposalText(proposalText.toString());
    	
    	
    	try
    	{
	    	if(project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId())
	    	{
	    		proposalInitiation.setLegalApprovalDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
	    		proposalInitiation.setLegalApprovalStatus(2);
	    		proposalInitiation.setProposalLegalApprover(dummyUserId);
	    	}
	    	if(project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId())
	    	{
	    		proposalInitiation.setLegalApprovalDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
	    		proposalInitiation.setLegalApprovalStatus(2);
	    		proposalInitiation.setProposalLegalApproverOffline(dummyUserName);
	    		addDummyAttachmentEmail(project.getProjectID(), SynchroGlobal.SynchroAttachmentObject.PROPOSAL_LEGAL_APPROVAL.getId().longValue());
	    	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new Exception();
    	}
    	
    	
    	proposalInitiation.setModifiedBy(proposalInitiationList.get(0).getModifiedBy());
    	proposalInitiation.setModifiedDate(proposalInitiationList.get(0).getModifiedDate());
    	
    	proposalInitiation.setStatus(proposalInitiationList.get(0).getStatus());
    	
    	proposalManagerNew.updateProposalDetailsNew(proposalInitiation);
    	
    	//Proposal Attachments
    	List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
    	endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(project.getProjectID());
    	 
		PIBStakeholderList pibStakeHolderList = this.pibManager.getPIBStakeholderList(project.getProjectID(), endMarketDetails.get(0).getEndMarketID());
		Long agencyId = pibStakeHolderList.getAgencyContact1();
		
		// Fetch only attachments related to Proposal Stage
		//attachmentMap = this.proposalManagerNew.getDocumentAttachment(project.getProjectID(),endMarketDetails.get(0).getEndMarketID(),agencyId);
		attachmentMap = this.proposalManager.getDocumentAttachment(project.getProjectID(),endMarketDetails.get(0).getEndMarketID(),agencyId);
		
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId()));
		}
		
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId()));
		}
		
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId()));
		}
    	
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
            	proposalManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId().longValue(), attachmentUser.get(ab.getID()), new Long("-1"));
            }
            catch(Exception e)
            {
            	Log.error("Error while copying exception for Proposal for Project --"+ project.getProjectID() + ab.getID() + "NAME --" + ab.getName());
            	throw new Exception();
            }

        }
		
		if(attchBeanList!=null && attchBeanList.size()>0)
		{
			
		}
		else
		{
			addProposalDummyAttachment = true;
		}
     	return addProposalDummyAttachment;
    }

    public boolean  updateGrailProposalDetailsMM(Project project,List<ProposalInitiation> proposalInitiationList) throws Exception
    {
    	ProposalInitiation proposalInitiation = new ProposalInitiation();
    	proposalInitiation.setProjectID(proposalInitiationList.get(0).getProjectID());
    	
    	boolean addProposalDummyAttachment = false;
    	
    	StringBuilder proposal = new StringBuilder("");
     	
    	if(StringUtils.isNotBlank(proposalInitiationList.get(0).getResearchDesign()))
    	{
    		if(proposalInitiationList.get(0).getResearchDesign().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("<p>");
    		}
    		proposal.append("<b>Methodology Approach and Research Design:</b> " +proposalInitiationList.get(0).getResearchDesign());
    		if(proposal.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("</p>");
    		}
    	}
    	if(StringUtils.isNotBlank(proposalInitiationList.get(0).getSampleProfile()))
    	{
    		if(proposalInitiationList.get(0).getSampleProfile().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("<p>");
    		}
    		proposal.append("<b>Sample Profile:</b> "+proposalInitiationList.get(0).getSampleProfile());
    		if(proposal.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("</p>");
    		}
    	}
    	
    	if(StringUtils.isNotBlank(proposalInitiationList.get(0).getOthers()))
    	{
    		if(proposalInitiationList.get(0).getOthers().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("<p>");
    		}
    		proposal.append("<b>Other Comments:</b> "+proposalInitiationList.get(0).getOthers());
    		if(proposal.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("</p>");
    		}
    	}
    	
    	if(StringUtils.isNotBlank(proposalInitiationList.get(0).getProposalCostTemplate()))
    	{
    		if(proposalInitiationList.get(0).getProposalCostTemplate().contains("<p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("<p>");
    		}
    		proposal.append("<b>Proposal and Cost Template:</b> "+proposalInitiationList.get(0).getProposalCostTemplate());
    		if(proposal.toString().contains("</p>"))
    		{
    			
    		}
    		else
    		{
    			proposal.append("</p>");
    		}
    	}
    	
    	
    	proposalInitiation.setProposal(proposal.toString());
    	
    	
    	StringBuilder proposalText = new StringBuilder("");
    	proposalText.append(proposalInitiationList.get(0).getResearchDesignText());
    	proposalText.append(" "+proposalInitiationList.get(0).getSampleProfileText());
    	proposalText.append(" "+proposalInitiationList.get(0).getProposalCostTemplate());
    	
    	proposalInitiation.setProposalText(proposalText.toString());
    	
    	
    	try
    	{
	    	if(project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId())
	    	{
	    		proposalInitiation.setLegalApprovalDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
	    		proposalInitiation.setLegalApprovalStatus(2);
	    		proposalInitiation.setProposalLegalApprover(dummyUserId);
	    	}
	    	if(project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId())
	    	{
	    		proposalInitiation.setLegalApprovalDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
	    		proposalInitiation.setLegalApprovalStatus(2);
	    		proposalInitiation.setProposalLegalApproverOffline(dummyUserName);
	    		addDummyAttachmentEmail(project.getProjectID(), SynchroGlobal.SynchroAttachmentObject.PROPOSAL_LEGAL_APPROVAL.getId().longValue());
	    	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new Exception();
    	}
    	
    	
    	proposalInitiation.setModifiedBy(proposalInitiationList.get(0).getModifiedBy());
    	proposalInitiation.setModifiedDate(proposalInitiationList.get(0).getModifiedDate());
    	
    	proposalInitiation.setStatus(proposalInitiationList.get(0).getStatus());
    	
    	proposalManagerNew.updateProposalDetailsNew(proposalInitiation);
    	
    	// This is done to remove all the other End Markets apart from Above End Market 
    	proposalManagerNew.removeAllAgency(project.getProjectID());
    	
    	//Proposal Attachments
    	List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
    	endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(project.getProjectID());
    	 
		PIBStakeholderList pibStakeHolderList = this.pibManager.getPIBStakeholderList(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
		Long agencyId = pibStakeHolderList.getAgencyContact1();
		
		// Fetch only attachments related to Proposal Stage
		//attachmentMap = this.proposalManagerNew.getDocumentAttachment(project.getProjectID(),endMarketDetails.get(0).getEndMarketID(),agencyId);
		attachmentMap = this.proposalManager.getDocumentAttachment(project.getProjectID(),SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID,agencyId);
		
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId()));
		}
		
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId()));
		}
		
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.OTHERS.getId()));
		}
    	
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
            	proposalManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId().longValue(), attachmentUser.get(ab.getID()), new Long("-1"));
            }
            catch(Exception e)
            {
            	Log.error("Error while copying exception for Proposal for Project --"+ project.getProjectID() + ab.getID() + "NAME --" + ab.getName());
            	throw new Exception();
            }

        }
		
		if(attchBeanList!=null && attchBeanList.size()>0)
		{
			
		}
		else
		{
			addProposalDummyAttachment = true;
		}
     	return addProposalDummyAttachment;
    }
    
    public boolean  createGrailProposalDetailsMM(Project project, Project newProject, List<ProposalInitiation> proposalInitiationList) throws Exception
    {
    	ProposalInitiation proposalInitiation = new ProposalInitiation();
    	proposalInitiation = proposalInitiationList.get(0); 
    	proposalInitiation.setProjectID(newProject.getProjectID());
    	
    	boolean addProposalDummyAttachment = false;
    	
    	
    	try
    	{
	    	if(newProject.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId())
	    	{
	    		proposalInitiation.setLegalApprovalDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
	    		proposalInitiation.setLegalApprovalStatus(2);
	    		proposalInitiation.setProposalLegalApprover(dummyUserId);
	    	}
	    	if(newProject.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId())
	    	{
	    		proposalInitiation.setLegalApprovalDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
	    		proposalInitiation.setLegalApprovalStatus(2);
	    		proposalInitiation.setProposalLegalApproverOffline(dummyUserName);
	    		addDummyAttachmentEmail(newProject.getProjectID(), SynchroGlobal.SynchroAttachmentObject.PROPOSAL_LEGAL_APPROVAL.getId().longValue());
	    	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw new Exception();
    	}
    	
    	
    	//proposalInitiation.setModifiedBy(proposalInitiationList.get(0).getModifiedBy());
    	//proposalInitiation.setModifiedDate(proposalInitiationList.get(0).getModifiedDate());
    	
    	proposalInitiation.setCreationBy(getUser().getID());
    	proposalInitiation.setCreationDate(System.currentTimeMillis());

    
    	proposalInitiation.setModifiedBy(getUser().getID());
    	proposalInitiation.setModifiedDate(System.currentTimeMillis());
          
    	
    	
    	proposalInitiation.setStatus(proposalInitiationList.get(0).getStatus());
    	
    	
    	proposalManagerNew.saveProposalDetails(proposalInitiation);
    	proposalManagerNew.updateProposalDetailsNew(proposalInitiation);
    	
    	//Proposal Attachments
    	List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
    
		// Fetch only attachments related to Proposal Stage
		
		attachmentMap = this.proposalManagerNew.getDocumentAttachment(project.getProjectID(),new Long("-1"), new Long("-1"));
		
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId()));
		}
		
    	
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
            	proposalManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), newProject.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId().longValue(), attachmentUser.get(ab.getID()), new Long("-1"));
            }
            catch(Exception e)
            {
            	Log.error("Error while copying exception for Proposal for Project --"+ newProject.getProjectID() + ab.getID() + "NAME --" + ab.getName());
            	throw new Exception();
            }

        }
		
		if(attchBeanList!=null && attchBeanList.size()>0)
		{
			
		}
		else
		{
			addProposalDummyAttachment = true;
		}
     	return addProposalDummyAttachment;
    }
    public void updateGrailProposalDetailsMMOLD(Project project,List<ProposalInitiation> proposalInitiationList) throws Exception
    {
    	ProposalInitiation proposalInitiation = new ProposalInitiation();
    	proposalInitiation.setProjectID(proposalInitiationList.get(0).getProjectID());
    	
    	StringBuilder proposal = new StringBuilder("");
    	
    	
    	if(StringUtils.isNotBlank(proposalInitiationList.get(0).getResearchDesign()))
    	{
    		proposal.append("<b>Methodology Approach and Research Design:</b> " +proposalInitiationList.get(0).getResearchDesign());
    	}
    	if(StringUtils.isNotBlank(proposalInitiationList.get(0).getSampleProfile()))
    	{
    		proposal.append("<b>Sample Profile:</b> "+proposalInitiationList.get(0).getSampleProfile());
    	}
    	if(StringUtils.isNotBlank(proposalInitiationList.get(0).getActionStandard()))
    	{
    		proposal.append("<b>Proposal and Cost Template:</b> "+proposalInitiationList.get(0).getActionStandard());
    	}
    	proposalInitiation.setProposal(proposal.toString());
    	
    	
    	StringBuilder proposalText = new StringBuilder("");
    	proposalText.append(proposalInitiationList.get(0).getResearchDesignText());
    	proposalText.append(" "+proposalInitiationList.get(0).getSampleProfileText());
    	proposalText.append(" "+proposalInitiationList.get(0).getProposalCostTemplate());
    	
    	proposalInitiation.setProposalText(proposalText.toString());
    	
    	
    	
    	
    	proposalInitiation.setModifiedBy(proposalInitiationList.get(0).getModifiedBy());
    	proposalInitiation.setModifiedDate(proposalInitiationList.get(0).getModifiedDate());
    	
    	proposalInitiation.setStatus(proposalInitiationList.get(0).getStatus());
    	
    	proposalManagerNew.updateProposalDetailsNew(proposalInitiation);
    	
    	//Proposal Attachments
    	List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
		
		PIBStakeholderList pibStakeHolderList = this.pibManager.getPIBStakeholderList(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
		Long agencyId = pibStakeHolderList.getAgencyContact1();
		attachmentMap = this.proposalManagerNew.getDocumentAttachment(project.getProjectID(),SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID,agencyId);
		
		
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.RESEARCH_DESIGN.getId()));
		}
		if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SAMPLE_PROFILE.getId()));
		}
		
		
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.PROP_COST_TEMPLATE.getId()));
		}
		
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
            	proposalManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.PROPOSAL.getId().longValue(), attachmentUser.get(ab.getID()), new Long("-1"));
            }
            catch(Exception e)
            {
            	Log.error("Error while copying exception for Proposal for Project --"+ projectID + ab.getID() + "NAME --" + ab.getName());
            	throw new Exception();
            }

        }
     	
    }
    public void updateGrailProjectSpecsDetails(Project project,List<ProjectSpecsInitiation> projecSpecsInitiationList) throws Exception
    {
    	ProjectSpecsInitiation projectSpecsInitiation = new ProjectSpecsInitiation();
    	projectSpecsInitiation.setProjectID(projecSpecsInitiationList.get(0).getProjectID());
    	
    	StringBuilder documentation = new StringBuilder("");
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getScreener()))
    	{
    		documentation.append("<p><b>Screener:</b></p><p>"+projecSpecsInitiationList.get(0).getScreener()+"</p>");
    	}
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getConsumerCCAgreement()))
    	{
    		documentation.append("<p><b>Consumer Contract and Confidentiality Agreement:</b></p><p>"+projecSpecsInitiationList.get(0).getConsumerCCAgreement()+"</p>");
    	}
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getQuestionnaire()))
    	{
    		documentation.append("<p><b>Questionnaire/Discussion guide:</b></p><p> "+projecSpecsInitiationList.get(0).getQuestionnaire()+"</p>");
    	}
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getDiscussionguide()))
    	{
    		documentation.append("<p><b>Actual Stimulus Material:</b></p><p> "+projecSpecsInitiationList.get(0).getDiscussionguide()+"</p>");
    	}
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getStimulusMaterial()))
    	{
    		documentation.append("<p><b>Stimulus Material:</b></p><p>"+projecSpecsInitiationList.get(0).getStimulusMaterial()+"</p>");
    	}
    	
    	if(projecSpecsInitiationList.get(0).getStimuliDate()!=null)
    	{
    		String dateStr = df.format(projecSpecsInitiationList.get(0).getStimuliDate());
      	  	documentation.append("<p><b>Date Stimuli Available (in Research Agency):</b></p><p>"+dateStr+"</p>");
    	}
    	
    	
    
    	
    	projectSpecsInitiation.setDocumentation(documentation.toString());
    	projectSpecsInitiation.setDocumentationText(documentation.toString());
    	projectSpecsInitiation.setModifiedBy(projecSpecsInitiationList.get(0).getModifiedBy());
    	projectSpecsInitiation.setModifiedDate(projecSpecsInitiationList.get(0).getModifiedDate());
    	
    	projectSpecsInitiation.setStatus(projecSpecsInitiationList.get(0).getStatus());
    	
    	projectSpecsManagerNew.updateProjectSpecsDetailsNew(projectSpecsInitiation);
    	
    	//Project Specs Attachments
    	List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
    	endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(project.getProjectID());
    	
		attachmentMap = this.projectSpecsManagerNew.getDocumentAttachment(project.getProjectID(),endMarketDetails.get(0).getEndMarketID());
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId()));
		}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId()));
		}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId()));
		}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId()));
		}
    	
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()));
		}
		
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
            	projectSpecsManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.DOCUMENTATION.getId().longValue(), attachmentUser.get(ab.getID()));
            }
            catch(Exception e)
            {
            	Log.error("Error while copying exception for Project Specs for Project --"+ project.getProjectID() + ab.getID() + "NAME --" + ab.getName());
            	throw new Exception();
            }

        }	
     	
    }

    public void updateGrailProjectSpecsDetailsMM(Project project,List<ProjectSpecsInitiation> projecSpecsInitiationList) throws Exception
    {
    	ProjectSpecsInitiation projectSpecsInitiation = new ProjectSpecsInitiation();
    	projectSpecsInitiation.setProjectID(projecSpecsInitiationList.get(0).getProjectID());
    	
    	StringBuilder documentation = new StringBuilder("");
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getScreener()))
    	{
    		documentation.append("<p><b>Screener:</b></p><p>"+projecSpecsInitiationList.get(0).getScreener()+"</p>");
    	}
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getConsumerCCAgreement()))
    	{
    		documentation.append("<p><b>Consumer Contract and Confidentiality Agreement:</b></p><p>"+projecSpecsInitiationList.get(0).getConsumerCCAgreement()+"</p>");
    	}
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getQuestionnaire()))
    	{
    		documentation.append("<p><b>Questionnaire/Discussion guide:</b></p><p> "+projecSpecsInitiationList.get(0).getQuestionnaire()+"</p>");
    	}
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getDiscussionguide()))
    	{
    		documentation.append("<p><b>Actual Stimulus Material:</b></p><p> "+projecSpecsInitiationList.get(0).getDiscussionguide()+"</p>");
    	}
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getStimulusMaterial()))
    	{
    		documentation.append("<p><b>Stimulus Material:</b></p><p>"+projecSpecsInitiationList.get(0).getStimulusMaterial()+"</p>");
    	}
    	
    	if(projecSpecsInitiationList.get(0).getStimuliDate()!=null)
    	{
    		String dateStr = df.format(projecSpecsInitiationList.get(0).getStimuliDate());
      	  	documentation.append("<p><b>Date Stimuli Available (in Research Agency):</b></p><p>"+dateStr+"</p>");
    	}
    	
    	projectSpecsInitiation.setDocumentation(documentation.toString());
    	projectSpecsInitiation.setDocumentationText(documentation.toString());
    	projectSpecsInitiation.setModifiedBy(projecSpecsInitiationList.get(0).getModifiedBy());
    	projectSpecsInitiation.setModifiedDate(projecSpecsInitiationList.get(0).getModifiedDate());
    	
    	projectSpecsInitiation.setStatus(projecSpecsInitiationList.get(0).getStatus());
    	
    	projectSpecsManagerNew.updateProjectSpecsDetailsNew(projectSpecsInitiation);
    	
    	// This is done to remove all the other End Markets apart from Above End Market 
    	projectSpecsManagerNew.deleteAllProjectSpecsDetails(project.getProjectID());
    	
    	//Project Specs Attachments
    	List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
    	endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(project.getProjectID());
    	
		attachmentMap = this.projectSpecsManagerNew.getDocumentAttachment(project.getProjectID(),SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId()));
		}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId()));
		}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId()));
		}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId()));
		}
    	
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()));
		}
		
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
            	projectSpecsManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.DOCUMENTATION.getId().longValue(), attachmentUser.get(ab.getID()));
            }
            catch(Exception e)
            {
            	Log.error("Error while copying exception for Project Specs for Project --"+ project.getProjectID() + ab.getID() + "NAME --" + ab.getName());
            	throw new Exception();
            }

        }	
     	
    }
    
    public void createGrailProjectSpecsDetailsMM(Project project, Project newProject, List<ProjectSpecsInitiation> projecSpecsInitiationList) throws Exception
    {
    	ProjectSpecsInitiation projectSpecsInitiation = new ProjectSpecsInitiation();
    	projectSpecsInitiation = projecSpecsInitiationList.get(0);
    	projectSpecsInitiation.setProjectID(newProject.getProjectID());
    	
    	projectSpecsInitiation.setCreationBy(getUser().getID());
    	projectSpecsInitiation.setCreationDate(System.currentTimeMillis());
    	projectSpecsInitiation.setModifiedBy(getUser().getID());
    	projectSpecsInitiation.setModifiedDate(System.currentTimeMillis());

    	//projectSpecsInitiation.setModifiedBy(projecSpecsInitiationList.get(0).getModifiedBy());
    	//projectSpecsInitiation.setModifiedDate(projecSpecsInitiationList.get(0).getModifiedDate());
    	
    	projectSpecsInitiation.setStatus(projecSpecsInitiationList.get(0).getStatus());
    	
    	projectSpecsManagerNew.saveProjectSpecsDetails(projectSpecsInitiation);
    	projectSpecsManagerNew.updateProjectSpecsDetailsNew(projectSpecsInitiation);
    	
    	//Project Specs Attachments
    	List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
    	
    	
		attachmentMap = this.projectSpecsManagerNew.getDocumentAttachment(project.getProjectID(),new Long("-1"));
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DOCUMENTATION.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DOCUMENTATION.getId()));
		}
    	
		
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
            	projectSpecsManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), newProject.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.DOCUMENTATION.getId().longValue(), attachmentUser.get(ab.getID()));
            }
            catch(Exception e)
            {
            	Log.error("Error while copying exception for Project Specs for Project --"+ newProject.getProjectID() + ab.getID() + "NAME --" + ab.getName());
            	throw new Exception();
            }

        }	
     	
    }
    
    public void updateGrailProjectSpecsDetailsMM_OLD(Project project,List<ProjectSpecsInitiation> projecSpecsInitiationList) throws Exception
    {
    	ProjectSpecsInitiation projectSpecsInitiation = new ProjectSpecsInitiation();
    	projectSpecsInitiation.setProjectID(projecSpecsInitiationList.get(0).getProjectID());
    	
    	StringBuilder documentation = new StringBuilder("");
    	
    	
    /*	documentation.append("Screener: "+projecSpecsInitiationList.get(0).getScreener());
    	documentation.append("Consumer Contract and Confidentiality Agreement: "+projecSpecsInitiationList.get(0).getConsumerCCAgreement());
    	documentation.append("Questionnaire/Discussion guide: "+projecSpecsInitiationList.get(0).getQuestionnaire());
    	documentation.append("Actual Stimulus Material: "+projecSpecsInitiationList.get(0).getDiscussionguide());
    	documentation.append("Stimulus Material: "+projecSpecsInitiationList.get(0).getStimulusMaterial());
    	
    	*/
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getScreener()))
    	{
    		documentation.append("<p><b>Screener:</b></p><p>"+projecSpecsInitiationList.get(0).getScreener()+"</p>");
    	}
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getConsumerCCAgreement()))
    	{
    		documentation.append("<p><b>Consumer Contract and Confidentiality Agreement:</b></p><p>"+projecSpecsInitiationList.get(0).getConsumerCCAgreement()+"</p>");
    	}
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getQuestionnaire()))
    	{
    		documentation.append("<p><b>Questionnaire/Discussion guide:</b></p><p> "+projecSpecsInitiationList.get(0).getQuestionnaire()+"</p>");
    	}
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getDiscussionguide()))
    	{
    		documentation.append("<p><b>Actual Stimulus Material:</b></p><p> "+projecSpecsInitiationList.get(0).getDiscussionguide()+"</p>");
    	}
    	if(StringUtils.isNotBlank(projecSpecsInitiationList.get(0).getStimulusMaterial()))
    	{
    		documentation.append("<p><b>Stimulus Material:</b></p><p>"+projecSpecsInitiationList.get(0).getStimulusMaterial()+"</p>");
    	}
    	
    	projectSpecsInitiation.setDocumentation(documentation.toString());
    	projectSpecsInitiation.setDocumentationText(documentation.toString());
    	projectSpecsInitiation.setModifiedBy(projecSpecsInitiationList.get(0).getModifiedBy());
    	projectSpecsInitiation.setModifiedDate(projecSpecsInitiationList.get(0).getModifiedDate());
    	
    	projectSpecsInitiation.setStatus(projecSpecsInitiationList.get(0).getStatus());
    	
    	projectSpecsManagerNew.updateProjectSpecsDetailsNew(projectSpecsInitiation);
    	
    	//Project Specs Attachments
    	List<AttachmentBean> attchBeanList = new ArrayList<AttachmentBean>();
		
		attachmentMap = this.projectSpecsManagerNew.getDocumentAttachment(project.getProjectID(),SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SCREENER.getId()));
		}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.CCC_AGREEMENT.getId()));
		}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.QUESTIONNAIRE.getId()));
		}
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.DISCUSSION_GUIDE.getId()));
		}
    	
    	if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId())!=null)
		{
			attchBeanList.addAll(attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.STIMULUS_MATERIAL.getId()));
		}
		
		attachmentUser = pibManagerNew.getAttachmentUser(attchBeanList);
		for(AttachmentBean ab:attchBeanList)
        {
            try
            {
            	projectSpecsManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                        ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.DOCUMENTATION.getId().longValue(), attachmentUser.get(ab.getID()));
            }
            catch(Exception e)
            {
            	Log.error("Error while copying exception for Project Specs for Project --"+ project.getProjectID() + ab.getID() + "NAME --" + ab.getName());
            	throw new Exception();
            }

        }	
     	
    }
    
    public void updateGrailReportSummaryDetails(Project project,List<ReportSummaryInitiation> reportSummaryInitiationList, MigrateProjectBean migrateProjectBean) throws Exception
    {
    	List<ReportSummaryDetails> reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
    	
    	List<ReportSummaryDetails> blankReportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
    	int reportOrderId = 0;
    	
    	// For now we have added 3 rows. One for Full Report, One for Top Line/Executive Presentation and one for IRIS Summary.
    	
    	ReportSummaryDetails rsdbean = new ReportSummaryDetails();
    	boolean blankReportType = true;
    	boolean blankIRISReportType = true;
     /*	rsdbean.setProjectID(projectID);
     	rsdbean.setReportOrderId(++reportOrderId);
     	rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
     	rsdbean.setReportType(1);
		reportSummaryDetailsList.add(rsdbean);
		
		rsdbean = new ReportSummaryDetails();
     	rsdbean.setProjectID(projectID);
     	rsdbean.setReportOrderId(++reportOrderId);
     	rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
     	rsdbean.setReportType(2);
		reportSummaryDetailsList.add(rsdbean);
		
		rsdbean = new ReportSummaryDetails();
     	rsdbean.setProjectID(projectID);
     	rsdbean.setReportOrderId(++reportOrderId);
     	rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
     	rsdbean.setReportType(4);
		reportSummaryDetailsList.add(rsdbean);
		
		this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);*/
		
		// Report Summary Attachment	
    	endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(project.getProjectID());
	   	 attachmentMap = this.reportSummaryManagerNew.getDocumentAttachment(project.getProjectID(), endMarketDetails.get(0).getEndMarketID());
	   	 reportOrderId = 0;
	   	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null)
	   	 {
	   		 	reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	           	rsdbean.setProjectID(project.getProjectID());
			      rsdbean.setReportOrderId(++reportOrderId);
			      if(reportSummaryInitiationList.get(0).getLegalApprover()!=null)
			      {
			    	  rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setOldReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setNewReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			      }
			      else
			      {
			    	  rsdbean.setLegalApprover("");
			      }
			      //rsdbean.setLegalApprover("Dummy User");
			      rsdbean.setReportType(1);
				  reportSummaryDetailsList.add(rsdbean);
				  
					// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
		   		   this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
				 
		   		   
	   		 	   List<AttachmentBean> attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId());
	               List<Long> attachmentIds = new ArrayList<Long>();
	               for(AttachmentBean ab:attchBeanList)
	               {
	                   try
	                   {
	                   	long attachId =  reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                               ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId().longValue(), getUserID());
	                   	attachmentIds.add(attachId);
	                   }
	                   catch(Exception e)
	                   {
	                       Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	                       throw new Exception();
	                   }
	
	               }
	   	         
	            reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	           	rsdbean.setProjectID(project.getProjectID());
	           	rsdbean.setReportOrderId(reportOrderId);
	           	rsdbean.setLegalApprover("");
	           	//rsdbean.setLegalApprover("Dummy User");
	           	rsdbean.setReportType(1);
	   		    rsdbean.setAttachmentId(attachmentIds);
	   		    reportSummaryDetailsList.add(rsdbean);
	   		    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
	   		    
	   		    blankReportType = false;
	   	        
	   	 }
	   	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_REPORT.getId())!=null)
	   	 {
	   		 	reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	   		 	rsdbean.setProjectID(project.getProjectID());
		        rsdbean.setReportOrderId(++reportOrderId);
		       // rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
		        
		        if(reportSummaryInitiationList.get(0).getLegalApprover()!=null)
			      {
			    	  rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setOldReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setNewReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			      }
			      else
			      {
			    	  rsdbean.setLegalApprover("");
			      }
		       // rsdbean.setLegalApprover("Dummy User");
		        rsdbean.setReportType(2);
			    reportSummaryDetailsList.add(rsdbean);
			  
				// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
		   		   this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
				 
		   		   
	   		 	List<AttachmentBean> attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_REPORT.getId());
	            List<Long> attachmentIds = new ArrayList<Long>();
	            for(AttachmentBean ab:attchBeanList)
	            {
	                try
	                {
	               	 long attachId = reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                            ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId().longValue(), getUserID());
	                	attachmentIds.add(attachId);
	                }
	                catch(Exception e)
	                {
	                    Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	                    throw new Exception();
	                }
	
	            }
		         
	            reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	        	rsdbean = new ReportSummaryDetails();
	        	rsdbean.setProjectID(project.getProjectID());
	        	rsdbean.setReportOrderId(reportOrderId);
	        	rsdbean.setLegalApprover("");
	        	//rsdbean.setLegalApprover("Dummy User");
	        	rsdbean.setReportType(2);
			    rsdbean.setAttachmentId(attachmentIds);
			    reportSummaryDetailsList.add(rsdbean);
			    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
			    
			    blankReportType = false;
	   	 }
	   	 
	   	if(blankReportType)
	   	 {
	   		rsdbean = new ReportSummaryDetails();
	   		rsdbean.setProjectID(project.getProjectID());
	     	rsdbean.setReportOrderId(++reportOrderId);
	     	if(reportSummaryInitiationList.get(0)!=null && reportSummaryInitiationList.get(0).getLegalApprover()!=null)
	     	{
	     		rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
	     		//rsdbean.setLegalApprover("");
	     		 migrateProjectBean.setOldReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
		    	  migrateProjectBean.setNewReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
	     	}
	     	else
	     	{
	     		rsdbean.setLegalApprover("");
	     	}
	     	rsdbean.setReportType(1);
	     	blankReportSummaryDetailsList.add(rsdbean);
			
			
	   	 }
	   	
	   	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId())!=null)
	   	 {
	   		 	reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	           	
	   		 	rsdbean.setProjectID(project.getProjectID());
	   		 	rsdbean.setReportOrderId(++reportOrderId);
	   		 //	rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
	   		 	
		   		 if(reportSummaryInitiationList.get(0).getLegalApprover()!=null)
			      {
			    	  rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setOldIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setNewIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
			      }
			      else
			      {
			    	  rsdbean.setLegalApprover("");
			      }
		   		 
	   		 	rsdbean.setReportType(4);
	   		 	reportSummaryDetailsList.add(rsdbean);
	   		
	   		 	// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
	   		   this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
			  
	   		 	List<AttachmentBean> attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId());
	            List<Long> attachmentIds = new ArrayList<Long>();
	            for(AttachmentBean ab:attchBeanList)
	            {
	                try
	                {
	               	 long attachId = reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                            ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId().longValue(), getUserID());
	                	attachmentIds.add(attachId);
	                }
	                catch(Exception e)
	                {
	                    Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	                    throw new Exception();
	                }
	
	            }
		         
	            reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	        	rsdbean = new ReportSummaryDetails();
	        	rsdbean.setProjectID(project.getProjectID());
	        	rsdbean.setReportOrderId(reportOrderId);
	        	//rsdbean.setLegalApprover("");
	        	rsdbean.setReportType(4);
			    rsdbean.setAttachmentId(attachmentIds);
			    reportSummaryDetailsList.add(rsdbean);
			    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
			    
			    blankIRISReportType = false;
	   	 }
	   	 
	   	 
	   	 
	   	 if(blankIRISReportType)
	   	 {
	   		rsdbean = new ReportSummaryDetails();
	     	rsdbean.setProjectID(project.getProjectID());
	     	rsdbean.setReportOrderId(++reportOrderId);
	     	if(reportSummaryInitiationList.get(0)!=null && reportSummaryInitiationList.get(0).getLegalApprover()!=null)
	     	{
	     		rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
	     		
	     		 migrateProjectBean.setOldIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
		    	 migrateProjectBean.setNewIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
	     	}
	     	else
	     	{
	     		rsdbean.setLegalApprover("");
	     	}
	     	rsdbean.setReportType(4);
	     	blankReportSummaryDetailsList.add(rsdbean);
	   	 }
	   	 
	   	 if(blankReportType || blankIRISReportType)
	   	 {
	   		this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(blankReportSummaryDetailsList);
	   	 }
	 	// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
	  // 	 this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
    }
    
    
    public void updateGrailReportSummaryDetailsMM(Project project,List<ReportSummaryInitiation> reportSummaryInitiationList, MigrateProjectBean migrateProjectBean) throws Exception
    {
    	List<ReportSummaryDetails> reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
    	
    	List<ReportSummaryDetails> blankReportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
    	int reportOrderId = 0;
    	
    	// For now we have added 3 rows. One for Full Report, One for Top Line/Executive Presentation and one for IRIS Summary.
    	
    	ReportSummaryDetails rsdbean = new ReportSummaryDetails();
    	boolean blankReportType = true;
    	boolean blankIRISReportType = true;
   
		
		// Report Summary Attachment	
    	 endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(project.getProjectID());
	   	 attachmentMap = this.reportSummaryManagerNew.getDocumentAttachment(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	   	 reportOrderId = 0;
	   	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null)
	   	 {
	   		 	reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	           	rsdbean.setProjectID(project.getProjectID());
			      rsdbean.setReportOrderId(++reportOrderId);
			      if(reportSummaryInitiationList.get(0).getLegalApprover()!=null)
			      {
			    	  rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setOldReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setNewReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			      }
			      else
			      {
			    	  rsdbean.setLegalApprover("");
			      }
			      //rsdbean.setLegalApprover("Dummy User");
			      rsdbean.setReportType(1);
				  reportSummaryDetailsList.add(rsdbean);
				  
					// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
		   		   this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
				 
		   		   
	   		 	   List<AttachmentBean> attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId());
	               List<Long> attachmentIds = new ArrayList<Long>();
	               for(AttachmentBean ab:attchBeanList)
	               {
	                   try
	                   {
	                   	long attachId =  reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                               ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId().longValue(), getUserID());
	                   	attachmentIds.add(attachId);
	                   }
	                   catch(Exception e)
	                   {
	                       Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	                       throw new Exception();
	                   }
	
	               }
	   	         
	            reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	           	rsdbean.setProjectID(project.getProjectID());
	           	rsdbean.setReportOrderId(reportOrderId);
	           	rsdbean.setLegalApprover("");
	           	//rsdbean.setLegalApprover("Dummy User");
	           	rsdbean.setReportType(1);
	   		    rsdbean.setAttachmentId(attachmentIds);
	   		    reportSummaryDetailsList.add(rsdbean);
	   		    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
	   		    
	   		    blankReportType = false;
	   	        
	   	 }
	   	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_REPORT.getId())!=null)
	   	 {
	   		 	reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	   		 	rsdbean.setProjectID(project.getProjectID());
		        rsdbean.setReportOrderId(++reportOrderId);
		       // rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
		        
		        if(reportSummaryInitiationList.get(0).getLegalApprover()!=null)
			      {
			    	  rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setOldReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setNewReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			      }
			      else
			      {
			    	  rsdbean.setLegalApprover("");
			      }
		       // rsdbean.setLegalApprover("Dummy User");
		        rsdbean.setReportType(2);
			    reportSummaryDetailsList.add(rsdbean);
			  
				// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
		   		   this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
				 
		   		   
	   		 	List<AttachmentBean> attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_REPORT.getId());
	            List<Long> attachmentIds = new ArrayList<Long>();
	            for(AttachmentBean ab:attchBeanList)
	            {
	                try
	                {
	               	 long attachId = reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                            ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId().longValue(), getUserID());
	                	attachmentIds.add(attachId);
	                }
	                catch(Exception e)
	                {
	                    Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	                    throw new Exception();
	                }
	
	            }
		         
	            reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	        	rsdbean = new ReportSummaryDetails();
	        	rsdbean.setProjectID(project.getProjectID());
	        	rsdbean.setReportOrderId(reportOrderId);
	        	rsdbean.setLegalApprover("");
	        	//rsdbean.setLegalApprover("Dummy User");
	        	rsdbean.setReportType(2);
			    rsdbean.setAttachmentId(attachmentIds);
			    reportSummaryDetailsList.add(rsdbean);
			    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
			    
			    blankReportType = false;
	   	 }
	   	 
	   	if(blankReportType)
	   	 {
	   		rsdbean = new ReportSummaryDetails();
	   		rsdbean.setProjectID(project.getProjectID());
	     	rsdbean.setReportOrderId(++reportOrderId);
	     	if(reportSummaryInitiationList.get(0)!=null && reportSummaryInitiationList.get(0).getLegalApprover()!=null)
	     	{
	     		rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
	     		//rsdbean.setLegalApprover("");
	     		 migrateProjectBean.setOldReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
		    	  migrateProjectBean.setNewReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
	     	}
	     	else
	     	{
	     		rsdbean.setLegalApprover("");
	     	}
	     	rsdbean.setReportType(1);
	     	blankReportSummaryDetailsList.add(rsdbean);
			
			
	   	 }
	   	
	   	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId())!=null)
	   	 {
	   		 	reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	           	
	   		 	rsdbean.setProjectID(project.getProjectID());
	   		 	rsdbean.setReportOrderId(++reportOrderId);
	   		 //	rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
	   		 	
		   		 if(reportSummaryInitiationList.get(0).getLegalApprover()!=null)
			      {
			    	  rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setOldIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setNewIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
			      }
			      else
			      {
			    	  rsdbean.setLegalApprover("");
			      }
		   		 
	   		 	rsdbean.setReportType(4);
	   		 	reportSummaryDetailsList.add(rsdbean);
	   		
	   		 	// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
	   		   this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
			  
	   		 	List<AttachmentBean> attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId());
	            List<Long> attachmentIds = new ArrayList<Long>();
	            for(AttachmentBean ab:attchBeanList)
	            {
	                try
	                {
	               	 long attachId = reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                            ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId().longValue(), getUserID());
	                	attachmentIds.add(attachId);
	                }
	                catch(Exception e)
	                {
	                    Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	                    throw new Exception();
	                }
	
	            }
		         
	            reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	        	rsdbean = new ReportSummaryDetails();
	        	rsdbean.setProjectID(project.getProjectID());
	        	rsdbean.setReportOrderId(reportOrderId);
	        	//rsdbean.setLegalApprover("");
	        	rsdbean.setReportType(4);
			    rsdbean.setAttachmentId(attachmentIds);
			    reportSummaryDetailsList.add(rsdbean);
			    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
			    
			    blankIRISReportType = false;
	   	 }
	   	 
	   	 
	   	 
	   	 if(blankIRISReportType)
	   	 {
	   		rsdbean = new ReportSummaryDetails();
	     	rsdbean.setProjectID(project.getProjectID());
	     	rsdbean.setReportOrderId(++reportOrderId);
	     	if(reportSummaryInitiationList.get(0)!=null && reportSummaryInitiationList.get(0).getLegalApprover()!=null)
	     	{
	     		rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
	     		
	     		 migrateProjectBean.setOldIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
		    	 migrateProjectBean.setNewIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
	     	}
	     	else
	     	{
	     		rsdbean.setLegalApprover("");
	     	}
	     	rsdbean.setReportType(4);
	     	blankReportSummaryDetailsList.add(rsdbean);
	   	 }
	   	 
	   	 if(blankReportType || blankIRISReportType)
	   	 {
	   		this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(blankReportSummaryDetailsList);
	   	 }
	   	 
	  // This is done to remove all the other End Markets apart from Above End Market 
	   	reportSummaryManagerNew.deleteAllReportSummaryDetails(project.getProjectID());
	    	
	 	// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
	  // 	 this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
    }
    
    public void createGrailReportSummaryDetailsMM(Project project, Project newProject, List<ReportSummaryInitiation> reportSummaryInitiationList, MigrateProjectBean migrateProjectBean) throws Exception
    {
    	ReportSummaryInitiation reportSummaryInitiation = reportSummaryInitiationList.get(0);
    	reportSummaryInitiation.setProjectID(newProject.getProjectID());
		reportSummaryInitiation.setCreationBy(getUser().getID());
		reportSummaryInitiation.setCreationDate(System.currentTimeMillis());
    	reportSummaryInitiation.setModifiedBy(getUser().getID());
    	reportSummaryInitiation.setModifiedDate(System.currentTimeMillis());
    	reportSummaryManagerNew.saveReportSummaryDetails(reportSummaryInitiation);
    	
    	List<ReportSummaryDetails> reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
    	
    	List<ReportSummaryDetails> blankReportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
    	int reportOrderId = 0;
    	
    	// For now we have added 3 rows. One for Full Report, One for Top Line/Executive Presentation and one for IRIS Summary.
    	
    	ReportSummaryDetails rsdbean = new ReportSummaryDetails();
    	boolean blankReportType = true;
    	boolean blankIRISReportType = true;
   
		
		// Report Summary Attachment	
    	 endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(project.getProjectID());
	   	 attachmentMap = this.reportSummaryManagerNew.getDocumentAttachment(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	   	 reportOrderId = 0;
	   	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null)
	   	 {
	   		 	reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	           	rsdbean.setProjectID(newProject.getProjectID());
			      rsdbean.setReportOrderId(++reportOrderId);
			      if(reportSummaryInitiationList.get(0).getLegalApprover()!=null)
			      {
			    	  rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setOldReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setNewReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			      }
			      else
			      {
			    	  rsdbean.setLegalApprover("");
			      }
			      //rsdbean.setLegalApprover("Dummy User");
			      rsdbean.setReportType(1);
				  reportSummaryDetailsList.add(rsdbean);
				  
					// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
		   		   this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
				 
		   		   
	   		 	   List<AttachmentBean> attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId());
	               List<Long> attachmentIds = new ArrayList<Long>();
	               for(AttachmentBean ab:attchBeanList)
	               {
	                   try
	                   {
	                   	long attachId =  reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                               ab.getContentType(), newProject.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId().longValue(), getUserID());
	                   	attachmentIds.add(attachId);
	                   }
	                   catch(Exception e)
	                   {
	                       Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	                       throw new Exception();
	                   }
	
	               }
	   	         
	            reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	           	rsdbean.setProjectID(newProject.getProjectID());
	           	rsdbean.setReportOrderId(reportOrderId);
	           	rsdbean.setLegalApprover("");
	           	//rsdbean.setLegalApprover("Dummy User");
	           	rsdbean.setReportType(1);
	   		    rsdbean.setAttachmentId(attachmentIds);
	   		    reportSummaryDetailsList.add(rsdbean);
	   		    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
	   		    
	   		    blankReportType = false;
	   	        
	   	 }
	   	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_REPORT.getId())!=null)
	   	 {
	   		 	reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	   		 	rsdbean.setProjectID(newProject.getProjectID());
		        rsdbean.setReportOrderId(++reportOrderId);
		       // rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
		        
		        if(reportSummaryInitiationList.get(0).getLegalApprover()!=null)
			      {
			    	  rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setOldReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setNewReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			      }
			      else
			      {
			    	  rsdbean.setLegalApprover("");
			      }
		       // rsdbean.setLegalApprover("Dummy User");
		        rsdbean.setReportType(2);
			    reportSummaryDetailsList.add(rsdbean);
			  
				// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
		   		   this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
				 
		   		   
	   		 	List<AttachmentBean> attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_REPORT.getId());
	            List<Long> attachmentIds = new ArrayList<Long>();
	            for(AttachmentBean ab:attchBeanList)
	            {
	                try
	                {
	               	 long attachId = reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                            ab.getContentType(), newProject.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId().longValue(), getUserID());
	                	attachmentIds.add(attachId);
	                }
	                catch(Exception e)
	                {
	                    Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	                    throw new Exception();
	                }
	
	            }
		         
	            reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	        	rsdbean = new ReportSummaryDetails();
	        	rsdbean.setProjectID(newProject.getProjectID());
	        	rsdbean.setReportOrderId(reportOrderId);
	        	rsdbean.setLegalApprover("");
	        	//rsdbean.setLegalApprover("Dummy User");
	        	rsdbean.setReportType(2);
			    rsdbean.setAttachmentId(attachmentIds);
			    reportSummaryDetailsList.add(rsdbean);
			    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
			    
			    blankReportType = false;
	   	 }
	   	 
	   	if(blankReportType)
	   	 {
	   		rsdbean = new ReportSummaryDetails();
	   		rsdbean.setProjectID(newProject.getProjectID());
	     	rsdbean.setReportOrderId(++reportOrderId);
	     	if(reportSummaryInitiationList.get(0)!=null && reportSummaryInitiationList.get(0).getLegalApprover()!=null)
	     	{
	     		rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
	     		//rsdbean.setLegalApprover("");
	     		 migrateProjectBean.setOldReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
		    	  migrateProjectBean.setNewReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
	     	}
	     	else
	     	{
	     		rsdbean.setLegalApprover("");
	     	}
	     	rsdbean.setReportType(1);
	     	blankReportSummaryDetailsList.add(rsdbean);
			
			
	   	 }
	   	
	   	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId())!=null)
	   	 {
	   		 	reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	           	
	   		 	rsdbean.setProjectID(newProject.getProjectID());
	   		 	rsdbean.setReportOrderId(++reportOrderId);
	   		 //	rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
	   		 	
		   		 if(reportSummaryInitiationList.get(0).getLegalApprover()!=null)
			      {
			    	  rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setOldIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setNewIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
			      }
			      else
			      {
			    	  rsdbean.setLegalApprover("");
			      }
		   		 
	   		 	rsdbean.setReportType(4);
	   		 	reportSummaryDetailsList.add(rsdbean);
	   		
	   		 	// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
	   		   this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
			  
	   		 	List<AttachmentBean> attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId());
	            List<Long> attachmentIds = new ArrayList<Long>();
	            for(AttachmentBean ab:attchBeanList)
	            {
	                try
	                {
	               	 long attachId = reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                            ab.getContentType(), newProject.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId().longValue(), getUserID());
	                	attachmentIds.add(attachId);
	                }
	                catch(Exception e)
	                {
	                    Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	                    throw new Exception();
	                }
	
	            }
		         
	            reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	        	rsdbean = new ReportSummaryDetails();
	        	rsdbean.setProjectID(newProject.getProjectID());
	        	rsdbean.setReportOrderId(reportOrderId);
	        	//rsdbean.setLegalApprover("");
	        	rsdbean.setReportType(4);
			    rsdbean.setAttachmentId(attachmentIds);
			    reportSummaryDetailsList.add(rsdbean);
			    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
			    
			    blankIRISReportType = false;
	   	 }
	   	 
	   	 
	   	 
	   	 if(blankIRISReportType)
	   	 {
	   		rsdbean = new ReportSummaryDetails();
	     	rsdbean.setProjectID(newProject.getProjectID());
	     	rsdbean.setReportOrderId(++reportOrderId);
	     	if(reportSummaryInitiationList.get(0)!=null && reportSummaryInitiationList.get(0).getLegalApprover()!=null)
	     	{
	     		rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
	     		
	     		 migrateProjectBean.setOldIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
		    	 migrateProjectBean.setNewIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
	     	}
	     	else
	     	{
	     		rsdbean.setLegalApprover("");
	     	}
	     	rsdbean.setReportType(4);
	     	blankReportSummaryDetailsList.add(rsdbean);
	   	 }
	   	 
	   	 if(blankReportType || blankIRISReportType)
	   	 {
	   		this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(blankReportSummaryDetailsList);
	   	 }
	 	// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
	  // 	 this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
    }
    public void updateGrailReportSummaryDetailsMM_OLD(Project project,List<ReportSummaryInitiation> reportSummaryInitiationList, MigrateProjectBean migrateProjectBean) throws Exception
    {
    	List<ReportSummaryDetails> reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
    	
    	List<ReportSummaryDetails> blankReportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
    	int reportOrderId = 0;
    	
    	// For now we have added 3 rows. One for Full Report, One for Top Line/Executive Presentation and one for IRIS Summary.
    	
    	ReportSummaryDetails rsdbean = new ReportSummaryDetails();
    	boolean blankReportType = true;
    	boolean blankIRISReportType = true;
     
		
		// Report Summary Attachment	
    	endMarketDetails = this.synchroProjectManagerNew.getEndMarketDetails(project.getProjectID());
	   	 attachmentMap = this.reportSummaryManagerNew.getDocumentAttachment(project.getProjectID(), SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
	   	 reportOrderId = 0;
	   	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId())!=null)
	   	 {
	   		 	reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	           	rsdbean.setProjectID(project.getProjectID());
			      rsdbean.setReportOrderId(++reportOrderId);
			      if(reportSummaryInitiationList.get(0).getLegalApprover()!=null)
			      {
			    	  rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setOldReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setNewReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			      }
			      else
			      {
			    	  rsdbean.setLegalApprover("");
			      }
			      rsdbean.setReportType(1);
				  reportSummaryDetailsList.add(rsdbean);
				  
					// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
		   		   this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
				 
		   		   
	   		 	   List<AttachmentBean> attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId());
	               List<Long> attachmentIds = new ArrayList<Long>();
	               for(AttachmentBean ab:attchBeanList)
	               {
	                   try
	                   {
	                   	long attachId =  reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                               ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId().longValue(), getUserID());
	                   	attachmentIds.add(attachId);
	                   }
	                   catch(Exception e)
	                   {
	                       Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	                       throw new Exception();
	                   }
	
	               }
	   	         
	            reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	           	rsdbean.setProjectID(project.getProjectID());
	           	rsdbean.setReportOrderId(reportOrderId);
	           	//rsdbean.setLegalApprover("");
	           	rsdbean.setReportType(1);
	   		    rsdbean.setAttachmentId(attachmentIds);
	   		    reportSummaryDetailsList.add(rsdbean);
	   		    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
	   		    
	   		    blankReportType = false;
	   	        
	   	 }
	   	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_REPORT.getId())!=null)
	   	 {
	   		 	reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	   		 	rsdbean.setProjectID(project.getProjectID());
		        rsdbean.setReportOrderId(++reportOrderId);
		       // rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
		        
		        if(reportSummaryInitiationList.get(0).getLegalApprover()!=null)
			      {
			    	  rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setOldReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setNewReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
			      }
			      else
			      {
			    	  rsdbean.setLegalApprover("");
			      }
		        
		        rsdbean.setReportType(2);
			    reportSummaryDetailsList.add(rsdbean);
			  
				// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
		   		   this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
				 
		   		   
	   		 	List<AttachmentBean> attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_REPORT.getId());
	            List<Long> attachmentIds = new ArrayList<Long>();
	            for(AttachmentBean ab:attchBeanList)
	            {
	                try
	                {
	               	 long attachId = reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                            ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.FULL_REPORT.getId().longValue(), getUserID());
	                	attachmentIds.add(attachId);
	                }
	                catch(Exception e)
	                {
	                    Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	                    throw new Exception();
	                }
	
	            }
		         
	            reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	        	rsdbean = new ReportSummaryDetails();
	        	rsdbean.setProjectID(project.getProjectID());
	        	rsdbean.setReportOrderId(reportOrderId);
	        	//rsdbean.setLegalApprover("");
	        	rsdbean.setReportType(2);
			    rsdbean.setAttachmentId(attachmentIds);
			    reportSummaryDetailsList.add(rsdbean);
			    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
			    
			    blankReportType = false;
	   	 }
	   	 
	   	if(blankReportType)
	   	 {
	   		rsdbean = new ReportSummaryDetails();
	   		rsdbean.setProjectID(project.getProjectID());
	     	rsdbean.setReportOrderId(++reportOrderId);
	     	if(reportSummaryInitiationList.get(0)!=null && reportSummaryInitiationList.get(0).getLegalApprover()!=null)
	     	{
	     		rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
	     		
	     		 migrateProjectBean.setOldReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
		    	  migrateProjectBean.setNewReportTypeLA(reportSummaryInitiationList.get(0).getLegalApprover());
	     	}
	     	else
	     	{
	     		rsdbean.setLegalApprover("");
	     	}
	     	rsdbean.setReportType(1);
	     	blankReportSummaryDetailsList.add(rsdbean);
			
			
	   	 }
	   	
	   	 if(attachmentMap!=null && attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId())!=null)
	   	 {
	   		 	reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	           	rsdbean = new ReportSummaryDetails();
	           	
	   		 	rsdbean.setProjectID(project.getProjectID());
	   		 	rsdbean.setReportOrderId(++reportOrderId);
	   		 //	rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
	   		 	
		   		 if(reportSummaryInitiationList.get(0).getLegalApprover()!=null)
			      {
			    	  rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setOldIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
			    	  migrateProjectBean.setNewIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
			      }
			      else
			      {
			    	  rsdbean.setLegalApprover("");
			      }
		   		 
	   		 	rsdbean.setReportType(4);
	   		 	reportSummaryDetailsList.add(rsdbean);
	   		
	   		 	// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
	   		   this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
			  
	   		 	List<AttachmentBean> attchBeanList = attachmentMap.get(SynchroGlobal.SynchroAttachmentObject.SUMMARY_FOR_IRIS.getId());
	            List<Long> attachmentIds = new ArrayList<Long>();
	            for(AttachmentBean ab:attchBeanList)
	            {
	                try
	                {
	               	 long attachId = reportSummaryManagerNew.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
	                            ab.getContentType(), project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.IRIS_SUMMARY_REPORT.getId().longValue(), getUserID());
	                	attachmentIds.add(attachId);
	                }
	                catch(Exception e)
	                {
	                    Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
	                    throw new Exception();
	                }
	
	            }
		         
	            reportSummaryDetailsList = new ArrayList<ReportSummaryDetails>();
	        	rsdbean = new ReportSummaryDetails();
	        	rsdbean.setProjectID(project.getProjectID());
	        	rsdbean.setReportOrderId(reportOrderId);
	        	//rsdbean.setLegalApprover("");
	        	rsdbean.setReportType(4);
			    rsdbean.setAttachmentId(attachmentIds);
			    reportSummaryDetailsList.add(rsdbean);
			    this.reportSummaryManagerNew.saveReportSummaryAttachment(reportSummaryDetailsList);
			    
			    blankIRISReportType = false;
	   	 }
	   	 
	   	 
	   	 
	   	 if(blankIRISReportType)
	   	 {
	   		rsdbean = new ReportSummaryDetails();
	     	rsdbean.setProjectID(project.getProjectID());
	     	rsdbean.setReportOrderId(++reportOrderId);
	     	if(reportSummaryInitiationList.get(0)!=null && reportSummaryInitiationList.get(0).getLegalApprover()!=null)
	     	{
	     		rsdbean.setLegalApprover(reportSummaryInitiationList.get(0).getLegalApprover());
	     		
	     		 migrateProjectBean.setOldIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
		    	 migrateProjectBean.setNewIRISSummaryLA(reportSummaryInitiationList.get(0).getLegalApprover());
	     	}
	     	else
	     	{
	     		rsdbean.setLegalApprover("");
	     	}
	     	rsdbean.setReportType(4);
	     	blankReportSummaryDetailsList.add(rsdbean);
	   	 }
	   	 
	   	 if(blankReportType || blankIRISReportType)
	   	 {
	   		this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(blankReportSummaryDetailsList);
	   	 }
	 	// The details in the grailprojectrepsummarydetails will be migrated/saved only for those Report Summary rows for whom there are attachments.
	  // 	 this.reportSummaryManagerNew.saveReportSummaryDetailsOnly(reportSummaryDetailsList);
    }
    
    public void updateGrailProjectEvaluationDetails(Project project,List<ProjectEvaluationInitiation> projecEvalInitiationList, Long endMarketId, MigrateProjectBean migrateProjectBean, List<ProjectCostDetailsBean> projectCostDetails)
    {
    	ProjectEvaluationInitiation projectEvaluationInitiation = new ProjectEvaluationInitiation();
    	projectEvaluationInitiation.setProjectID(project.getProjectID());
    	projectEvaluationInitiation.setEndMarketId(endMarketId);
    	projectEvaluationInitiation.setCreationBy(projecEvalInitiationList.get(0).getCreationBy());
		projectEvaluationInitiation.setCreationDate(projecEvalInitiationList.get(0).getCreationDate());
        
		projectEvaluationInitiation.setModifiedBy(projecEvalInitiationList.get(0).getModifiedBy());
		projectEvaluationInitiation.setModifiedDate(projecEvalInitiationList.get(0).getModifiedDate());
        
		StringBuilder agencyComments = new StringBuilder("");
		if(StringUtils.isNotBlank(projecEvalInitiationList.get(0).getBatCommentsFA()))
		{
			agencyComments.append(projecEvalInitiationList.get(0).getBatCommentsFA().replaceAll("\\<.*?\\>", "").replaceAll("&nbsp;", " ").replaceAll("&nbsp", " "));
		}
		
		if(StringUtils.isNotBlank(projecEvalInitiationList.get(0).getBatCommentsIM()))
		{
			agencyComments.append(" "+projecEvalInitiationList.get(0).getBatCommentsIM().replaceAll("\\<.*?\\>", "").replaceAll("&nbsp;", " ").replaceAll("&nbsp", " "));
		}
		
		if(StringUtils.isNotBlank(projecEvalInitiationList.get(0).getBatCommentsLM()))
		{
			agencyComments.append(" " +projecEvalInitiationList.get(0).getBatCommentsLM().replaceAll("\\<.*?\\>", "").replaceAll("&nbsp;", " ").replaceAll("&nbsp", " "));
		}
		
		Integer agencyRating = new Integer("0");
		
		if(projecEvalInitiationList.get(0).getAgencyPerfFA()!=null && projecEvalInitiationList.get(0).getAgencyPerfFA() > agencyRating)
		{
			agencyRating = projecEvalInitiationList.get(0).getAgencyPerfFA();
		}
		
		if(projecEvalInitiationList.get(0).getAgencyPerfIM()!=null && projecEvalInitiationList.get(0).getAgencyPerfIM() > agencyRating)
		{
			agencyRating = projecEvalInitiationList.get(0).getAgencyPerfIM();
		}
		
		if(projecEvalInitiationList.get(0).getAgencyPerfLM()!=null && projecEvalInitiationList.get(0).getAgencyPerfLM() > agencyRating)
		{
			agencyRating = projecEvalInitiationList.get(0).getAgencyPerfLM();
		}
		// TODO : For now it is hardcoded, but it will be the same what we have done for Project Cost Details Section
		//projectEvaluationInitiation.setAgencyId(new Long("1"));
		
		// Essentially all the unique Agencies should be given the ratings and comments for the migrated project 
		List<Long> uniqueAgencyId = new ArrayList<Long>();
		
		if(projectCostDetails!=null && projectCostDetails.size() >0)
		{
			for(ProjectCostDetailsBean pcb:projectCostDetails)
	    	{
	    		if(!uniqueAgencyId.contains(pcb.getAgencyId()))
	    		{
	    			uniqueAgencyId.add(pcb.getAgencyId());
	    		}
	    	}
	    
			projectEvaluationManagerNew.deleteProjectEvaluationInitiation(project.getProjectID());
			for(Long agencyId : uniqueAgencyId)
			{
				projectEvaluationInitiation.setAgencyId(agencyId);
				projectEvaluationInitiation.setAgencyComments(agencyComments.toString());
				projectEvaluationInitiation.setAgencyRating(agencyRating);
				projectEvaluationInitiation.setStatus(SynchroGlobal.StageStatus.PROJ_EVAL_COMPLETED.ordinal());
				projectEvaluationInitiation.setCreationBy(getUser().getID());
	    		projectEvaluationInitiation.setCreationDate(System.currentTimeMillis());
	            
	    		projectEvaluationInitiation.setModifiedBy(getUser().getID());
	    		projectEvaluationInitiation.setModifiedDate(System.currentTimeMillis());
	    		this.projectEvaluationManagerNew.saveProjectEvaluationDetails(projectEvaluationInitiation);
			}
		}
		
		/*for(Integer i : SynchroGlobal.getResearchAgency().keySet())
        {
            if(SynchroGlobal.getResearchAgency().get(i).trim().equals(migrateProjectBean.getMigrateProjectCostBean().get(0).getResearchAgency().trim()))
            {
            	projectEvaluationInitiation.setAgencyId(new Long(i));
            }
        }
    	
		projectEvaluationInitiation.setAgencyComments(agencyComments.toString());
		projectEvaluationInitiation.setAgencyRating(agencyRating);
		projectEvaluationInitiation.setStatus(SynchroGlobal.StageStatus.PROJ_EVAL_COMPLETED.ordinal());         	 
		
        this.projectEvaluationManagerNew.updateProjectMigration(projectEvaluationInitiation);*/
		
		
		
    }
    
    public void createGrailProjectEvaluationDetailsMM(Project project,List<ProjectEvaluationInitiation> projecEvalInitiationList, Long endMarketId, MigrateProjectBean migrateProjectBean, List<ProjectCostDetailsBean> projectCostDetails)
    {
    	
    	
    	for(ProjectCostDetailsBean pcb : projectCostDetails)
		{
    		ProjectEvaluationInitiation projectEvaluationInitiation = new ProjectEvaluationInitiation();
        	
        	projectEvaluationInitiation.setCreationBy(getUser().getID());
        	projectEvaluationInitiation.setCreationDate(System.currentTimeMillis());
        	projectEvaluationInitiation.setModifiedBy(getUser().getID());
        	projectEvaluationInitiation.setModifiedDate(System.currentTimeMillis());
        	
        	projectEvaluationInitiation.setProjectID(project.getProjectID());
        	projectEvaluationInitiation.setEndMarketId(new Long("0"));
        	
    		projectEvaluationInitiation.setAgencyId(pcb.getAgencyId());
			projectEvaluationInitiation.setAgencyComments(projecEvalInitiationList.get(0).getAgencyComments());
			projectEvaluationInitiation.setAgencyRating(projecEvalInitiationList.get(0).getAgencyRating());
			projectEvaluationInitiation.setStatus(SynchroGlobal.StageStatus.PROJ_EVAL_COMPLETED.ordinal());
			projectEvaluationInitiation.setCreationBy(getUser().getID());
    		projectEvaluationInitiation.setCreationDate(System.currentTimeMillis());
            
    		projectEvaluationInitiation.setModifiedBy(getUser().getID());
    		projectEvaluationInitiation.setModifiedDate(System.currentTimeMillis());
    		this.projectEvaluationManagerNew.saveProjectEvaluationDetails(projectEvaluationInitiation);
		}
   	
    }
    
    public void generateMigrationReport(String fileName) throws Exception
    {
    
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("MigrationReport");
        
        LOG.info("Generating Migration Report Start ==>");
        
        //FileOutputStream outputStream = new FileOutputStream("ProjectMigration.xls");
        FileOutputStream outputStream = new FileOutputStream(fileName);
        
        if(generateReportBean!=null && generateReportBean.size()>0)
        {
        	
        	LOG.info("Generating Migration Report Projects Size ==>" + generateReportBean.size());
        	HSSFRow reportTypeHeader = sheet.createRow(0);
	        HSSFCell reportTypeHeaderColumn = reportTypeHeader.createCell(0);
	        reportTypeHeaderColumn.setCellValue("Project Id");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(1);
	        reportTypeHeaderColumn.setCellValue("Old Status");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(2);
	        reportTypeHeaderColumn.setCellValue("New Status");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(3);
	        reportTypeHeaderColumn.setCellValue("Old Category Type");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(4);
	        reportTypeHeaderColumn.setCellValue("New Category Type");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(5);
	        reportTypeHeaderColumn.setCellValue("Old Methodology Group");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(6);
	        reportTypeHeaderColumn.setCellValue("New Methodology Group");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(7);
	        reportTypeHeaderColumn.setCellValue("Old Date of Request for Legal Approval (Brief)");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(8);
	        reportTypeHeaderColumn.setCellValue("Old Date of last Update/ Approval (Brief)");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(9);
	        reportTypeHeaderColumn.setCellValue("Old Legal Approver Name (Brief)");
	        
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(10);
	        reportTypeHeaderColumn.setCellValue("New Date of Request for Legal Approval (Brief)");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(11);
	        reportTypeHeaderColumn.setCellValue("New Date of last Update/ Approval (Brief)");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(12);
	        reportTypeHeaderColumn.setCellValue("New Legal Approver Name (Brief)");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(13);
	        reportTypeHeaderColumn.setCellValue("Old Legal Approval Provided By (On Reports)");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(14);
	        reportTypeHeaderColumn.setCellValue("New Legal Approval Provided By (On Reports)");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(15);
	        reportTypeHeaderColumn.setCellValue("Old Legal Approval Provided By (On IRIS Summary)");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(16);
	        reportTypeHeaderColumn.setCellValue("New Legal Approval Provided By (On IRIS Summary)");
        	
        	for(int i=0;i<generateReportBean.size();i++)
	        {
	        	reportTypeHeader = sheet.createRow(i+1);
		        reportTypeHeaderColumn = reportTypeHeader.createCell(0);
		        reportTypeHeaderColumn.setCellValue(generateReportBean.get(i).getProjectID());
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(1);
		        reportTypeHeaderColumn.setCellValue(generateReportBean.get(i).getOldProjectStatus());
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(2);
		        reportTypeHeaderColumn.setCellValue(generateReportBean.get(i).getNewProjectStatus());
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(3);
		        if(generateReportBean.get(i).getOldCategoryType()!=null && generateReportBean.get(i).getOldCategoryType().size() > 0)
		        {
		        	
		        	List<String> categoryNames = new ArrayList<String>();
			         for(Long categId: generateReportBean.get(i).getOldCategoryType())
			         {
			        	 if(SynchroGlobal.getProductTypes().get(categId.intValue())!=null)
			        	 {
			        		 categoryNames.add(SynchroGlobal.getProductTypes().get(categId.intValue()));
			        	 }
			         }
			         if(categoryNames!=null && categoryNames.size()>0)
			         {
			        	 reportTypeHeaderColumn.setCellValue(StringUtils.join(categoryNames, ","));
			         }
			         else
			         {
			        	 reportTypeHeaderColumn.setCellValue("");
			         }
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(4);
		        if(generateReportBean.get(i).getNewCategoryType()!=null && generateReportBean.get(i).getNewCategoryType().size() > 0)
		        {
		        	
		        	List<String> categoryNames = new ArrayList<String>();
			         for(Long categId: generateReportBean.get(i).getNewCategoryType())
			         {
			        	 if(SynchroGlobal.getProductTypes().get(categId.intValue())!=null)
			        	 {
			        		 categoryNames.add(SynchroGlobal.getProductTypes().get(categId.intValue()));
			        	 }
			         }
			         if(categoryNames!=null && categoryNames.size()>0)
			         {
			        	 reportTypeHeaderColumn.setCellValue(StringUtils.join(categoryNames, ","));
			         }
			         else
			         {
			        	 reportTypeHeaderColumn.setCellValue("");
			         }
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(5);
		        if(generateReportBean.get(i).getOldMethodologyGroup()!=null)
		        {
		        	reportTypeHeaderColumn.setCellValue(SynchroGlobal.getMethodologyGroups(true, new Long("-1")).get(generateReportBean.get(i).getOldMethodologyGroup().intValue()));
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        reportTypeHeaderColumn = reportTypeHeader.createCell(6);
		        if(generateReportBean.get(i).getNewMethodologyGroup()!=null)
		        {
		        	reportTypeHeaderColumn.setCellValue(SynchroGlobal.getMethodologyGroups(true, new Long("-1")).get(generateReportBean.get(i).getNewMethodologyGroup().intValue()));
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(7);
		        if(generateReportBean.get(i).getOldPibDateOfRequestForLA()!=null)
		        {
		        	  String dateStr = df.format(generateReportBean.get(i).getOldPibDateOfRequestForLA());
		        	  reportTypeHeaderColumn.setCellValue(dateStr);
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(8);
		        if(generateReportBean.get(i).getOldPibDateOfLA()!=null)
		        {
		        	  String dateStr = df.format(generateReportBean.get(i).getOldPibDateOfLA());
		        	  reportTypeHeaderColumn.setCellValue(dateStr);
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(9);
		        if(generateReportBean.get(i).getOldLAName()!=null)
		        {
		        	
		        	reportTypeHeaderColumn.setCellValue(generateReportBean.get(i).getOldLAName());
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(10);
		        if(generateReportBean.get(i).getNewPibDateOfRequestForLA()!=null)
		        {
		        	  String dateStr = df.format(generateReportBean.get(i).getNewPibDateOfRequestForLA());
		        	  reportTypeHeaderColumn.setCellValue(dateStr);
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(11);
		        if(generateReportBean.get(i).getNewPibDateOfLA()!=null)
		        {
		        	  String dateStr = df.format(generateReportBean.get(i).getNewPibDateOfLA());
		        	  reportTypeHeaderColumn.setCellValue(dateStr);
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(12);
		        if(generateReportBean.get(i).getNewLAName()!=null)
		        {
		        	
		        	reportTypeHeaderColumn.setCellValue(generateReportBean.get(i).getNewLAName());
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(13);
		        if(generateReportBean.get(i).getOldReportTypeLA()!=null)
		        {
		        	
		        	reportTypeHeaderColumn.setCellValue(generateReportBean.get(i).getOldReportTypeLA());
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(14);
		        if(generateReportBean.get(i).getNewReportTypeLA()!=null)
		        {
		        	
		        	reportTypeHeaderColumn.setCellValue(generateReportBean.get(i).getNewReportTypeLA());
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(15);
		        if(generateReportBean.get(i).getOldIRISSummaryLA()!=null)
		        {
		        	
		        	reportTypeHeaderColumn.setCellValue(generateReportBean.get(i).getOldIRISSummaryLA());
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(16);
		        if(generateReportBean.get(i).getNewIRISSummaryLA()!=null)
		        {
		        	
		        	reportTypeHeaderColumn.setCellValue(generateReportBean.get(i).getNewIRISSummaryLA());
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(" ");
		        }
	        }
        	 workbook.write(outputStream);
        }
       
    }
    
    public void generateMigrationExceptionReport(String fileName) throws Exception
    {
    
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("MigrationExceptionReport");
        
        LOG.info("Generating Exception Migration Report Projects Start ==>");
        //FileOutputStream outputStream = new FileOutputStream("ProjectMigration.xls");
        FileOutputStream outputStream = new FileOutputStream(fileName);
        
        if(generateErrorReportBean!=null && generateErrorReportBean.size()>0)
        {
        	LOG.info("Generating Exception Migration Report Projects Size ==>" + generateErrorReportBean.size());
        	HSSFRow reportTypeHeader = sheet.createRow(0);
	        HSSFCell reportTypeHeaderColumn = reportTypeHeader.createCell(0);
	        reportTypeHeaderColumn.setCellValue("Project Id");
	        
	        reportTypeHeaderColumn = reportTypeHeader.createCell(1);
	        reportTypeHeaderColumn.setCellValue("Migration Exception");
	        
	       
        	
        	for(int i=0;i<generateErrorReportBean.size();i++)
	        {
	        	reportTypeHeader = sheet.createRow(i+1);
		        reportTypeHeaderColumn = reportTypeHeader.createCell(0);
		        reportTypeHeaderColumn.setCellValue(generateErrorReportBean.get(i).getProjectID());
		        
		        reportTypeHeaderColumn = reportTypeHeader.createCell(1);
		        if(generateErrorReportBean.get(i).getMigrationException()!=null && generateErrorReportBean.get(i).getMigrationException().length() > 200)
		        {
		        	reportTypeHeaderColumn.setCellValue(generateErrorReportBean.get(i).getMigrationException().substring(0,200));
		        }
		        else
		        {
		        	reportTypeHeaderColumn.setCellValue(generateErrorReportBean.get(i).getMigrationException());
		        }
		        
		        
	        }
        	 workbook.write(outputStream);
        }
       
    }
    
    private BigDecimal calculateTotalCost(List<ProjectCostDetailsBean> projectCostList)
    {
    	BigDecimal totalCost = new BigDecimal(0);
    	for(ProjectCostDetailsBean pcb:projectCostList )
    	{
    		/*BigDecimal agencyCost = projectCost.getEstimatedCost();
    		if(projectCost.getCostCurrency()==JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1))
    		{
    			totalCost = totalCost.add(agencyCost);
    		}
    		else
    		{
    			BigDecimal gbpEstimatedCost = BigDecimal.valueOf((SynchroUtils.getCurrencyExchangeRate(projectCost.getCostCurrency())) * (projectCost.getEstimatedCost().doubleValue()));
    			totalCost = totalCost.add(gbpEstimatedCost);
    		}*/
    		
    		// Commenting this as this will be calculated from the Front End
    		if(pcb.getCostCurrency()!=null && pcb.getEstimatedCost()!=null)
    		{
        		//BigDecimal gbpEstimatedCost = BigDecimal.valueOf((SynchroUtils.getCurrencyExchangeRate(pcb.getCostCurrency())) * (pcb.getEstimatedCost().doubleValue()));
    			BigDecimal gbpEstimatedCost = SynchroUtils.getCurrencyExchangeRateBD(pcb.getCostCurrency()).multiply(pcb.getEstimatedCost());
        		totalCost = totalCost.add(gbpEstimatedCost);
        		//projectId = pcb.getProjectId();
    		}
    	}
    	return totalCost;
    }
    
    private void addDummyAttachment(Long projectId, Long fieldType)
    {
    	//File dummyFile = new File("D:\\DummyFile.doc");
    	File dummyFile = new File("/usr/local/jive/applications/sbs/application/DummyFile.docx");
		try
        {
       	/* pibManagerNew.addAttachment(dummyFile,dummyFile.getName(),
                    "application/vnd.ms-excel", project.getProjectID(), new Long("-1"), SynchroGlobal.SynchroAttachmentObject.PIB_BRIEF.getId().longValue(), getUser().getID());
                    */
			pibManagerNew.addAttachment(dummyFile,dummyFile.getName(),
                    "application/msword", projectId, new Long("-1"), fieldType, getUser().getID());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        	Log.error("Error while copying Dummy File exception for PIB for Project --"+ project.getProjectID() );
           // throw new Exception();
        }
    }
    
    private void addDummyAttachmentEmail(Long projectId, Long fieldType)
    {
    	File dummyFile = new File("/usr/local/jive/applications/sbs/application/Dummy-Approval-Email.eml");
		try
        {
       		pibManagerNew.addAttachment(dummyFile,dummyFile.getName(),
                    "message/rfc822", projectId, new Long("-1"), fieldType, getUser().getID());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        	Log.error("Error while copying Dummy File exception for PIB for Project --"+ project.getProjectID() );
        }
    }
    
    public void setSynchroProjectManagerNew(final ProjectManagerNew synchroProjectManagerNew) {
        this.synchroProjectManagerNew = synchroProjectManagerNew;
    }

    public void setPibManagerNew(final PIBManagerNew pibManagerNew) {
        this.pibManagerNew = pibManagerNew;
    }

    public ProjectInitiation getProjectInitiation() {
        return projectInitiation;
    }

    public void setProjectInitiation(final ProjectInitiation projectInitiation) {
        this.projectInitiation = projectInitiation;
    }

    public Long getProjectID() {
        return projectID;
    }

    public void setProjectID(final Long projectID) {
        this.projectID = projectID;
    }

    public boolean isSave() {
        return isSave;
    }

    public void setSave(final boolean save) {
        isSave = save;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(final Project project) {
        this.project = project;
    }



    public PIBReporting getPibReporting() {
        return pibReporting;
    }

    public void setPibReporting(final PIBReporting pibReporting) {
        this.pibReporting = pibReporting;
    }


    public boolean isEditStage() {
        return editStage;
    }

    public void setEditStage(boolean editStage) {
        this.editStage = editStage;
    }

    public String getNotificationTabId() {
        return notificationTabId;
    }

    public void setNotificationTabId(String notificationTabId) {
        this.notificationTabId = notificationTabId;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public String getApprove() {
        return approve;
    }

    public void setApprove(String approve) {
        this.approve = approve;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public List<SynchroStageToDoListBean> getStageToDoList() {
        return stageToDoList;
    }

    public void setStageToDoList(List<SynchroStageToDoListBean> stageToDoList) {
        this.stageToDoList = stageToDoList;
    }

    public Integer getStageId() {
        return stageId;
    }

    public void setStageId(Integer stageId) {
        this.stageId = stageId;
    }




    public StageManager getStageManager() {
        return stageManager;
    }

    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public Map<String, Map<String, String>> getStageApprovers() {
        return stageApprovers;
    }

    public void setStageApprovers(Map<String, Map<String, String>> stageApprovers) {
        this.stageApprovers = stageApprovers;
    }




    public Map<String, String> getApprovers() {
        return approvers;
    }

    public int getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(int attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public long[] getRemoveAttachID() {
        return removeAttachID;
    }

    public void setRemoveAttachID(long[] removeAttachID) {
        this.removeAttachID = removeAttachID;
    }

    public long[] getImageFile() {
        return imageFile;
    }

    public void setImageFile(long[] imageFile) {
        this.imageFile = imageFile;
    }

    public AttachmentHelper getAttachmentHelper() {
        return attachmentHelper;
    }

    public void setAttachmentHelper(AttachmentHelper attachmentHelper) {
        this.attachmentHelper = attachmentHelper;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenCookie() {
        return tokenCookie;
    }

    public void setTokenCookie(String tokenCookie) {
        this.tokenCookie = tokenCookie;
    }



    public PIBStakeholderList getPibStakeholderList() {
        return pibStakeholderList;
    }
    public void setPibStakeholderList(PIBStakeholderList pibStakeholderList) {
        this.pibStakeholderList = pibStakeholderList;
    }

    public File getAttachFile() {
        return attachFile;
    }

    public void setAttachFile(File attachFile) {
        this.attachFile = attachFile;
    }

    public String getAttachFileContentType() {
        return attachFileContentType;
    }

    public void setAttachFileContentType(String attachFileContentType) {
        this.attachFileContentType = attachFileContentType;
    }

    public String getAttachFileFileName() {
        return attachFileFileName;
    }

    public void setAttachFileFileName(String attachFileFileName) {
        this.attachFileFileName = attachFileFileName;
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(Long attachmentId) {
        this.attachmentId = attachmentId;
    }

    
    public Long getAttachmentFieldID() {
		return attachmentFieldID;
	}

	public void setAttachmentFieldID(Long attachmentFieldID) {
		this.attachmentFieldID = attachmentFieldID;
	}

	public Long getFieldCategoryId() {
        return fieldCategoryId;
    }

    public void setFieldCategoryId(Long fieldCategoryId) {
        this.fieldCategoryId = fieldCategoryId;
    }

    public Map<Integer, List<AttachmentBean>> getAttachmentMap() {
        return attachmentMap;
    }

    public void setAttachmentMap(Map<Integer, List<AttachmentBean>> attachmentMap) {
        this.attachmentMap = attachmentMap;
    }



    public Long getEndMarketId() {
        return endMarketId;
    }

    public void setEndMarketId(Long endMarketId) {
        this.endMarketId = endMarketId;
    }

    public Long getUpdatedSingleMarketId() {
        return updatedSingleMarketId;
    }

    public void setUpdatedSingleMarketId(Long updatedSingleMarketId) {
        this.updatedSingleMarketId = updatedSingleMarketId;
    }

    public PIBMethodologyWaiver getPibMethodologyWaiver() {
        return pibMethodologyWaiver;
    }

    public void setPibMethodologyWaiver(PIBMethodologyWaiver pibMethodologyWaiver) {
        this.pibMethodologyWaiver = pibMethodologyWaiver;
    }



    public List<EndMarketInvestmentDetail> getEndMarketDetails() {
        return endMarketDetails;
    }

    public void setEndMarketDetails(List<EndMarketInvestmentDetail> endMarketDetails) {
        this.endMarketDetails = endMarketDetails;
    }

    public Map<Long, Long> getAttachmentUser() {
        return attachmentUser;
    }

    public void setAttachmentUser(Map<Long, Long> attachmentUser) {
        this.attachmentUser = attachmentUser;
    }

    public Boolean getShowMandatoryFieldsError() {
        return showMandatoryFieldsError;
    }

    public void setShowMandatoryFieldsError(Boolean showMandatoryFieldsError) {
        this.showMandatoryFieldsError = showMandatoryFieldsError;
    }

    public Boolean getIsProposalAwarded() {
        return isProposalAwarded;
    }

    public void setIsProposalAwarded(Boolean isProposalAwarded) {
        this.isProposalAwarded = isProposalAwarded;
    }

    public ProposalManagerNew getProposalManagerNew() {
        return proposalManagerNew;
    }

    public void setProposalManagerNew(ProposalManagerNew proposalManagerNew) {
        this.proposalManagerNew = proposalManagerNew;
    }

    public String getSubjectSendToProjOwner() {
        return subjectSendToProjOwner;
    }

    public void setSubjectSendToProjOwner(String subjectSendToProjOwner) {
        this.subjectSendToProjOwner = subjectSendToProjOwner;
    }

    public String getMessageBodySendToProjOwner() {
        return messageBodySendToProjOwner;
    }

    public void setMessageBodySendToProjOwner(String messageBodySendToProjOwner) {
        this.messageBodySendToProjOwner = messageBodySendToProjOwner;
    }

    public String getSubjectSendToSPI() {
        return subjectSendToSPI;
    }

    public void setSubjectSendToSPI(String subjectSendToSPI) {
        this.subjectSendToSPI = subjectSendToSPI;
    }

    public String getMessageBodySendToSPI() {
        return messageBodySendToSPI;
    }

    public void setMessageBodySendToSPI(String messageBodySendToSPI) {
        this.messageBodySendToSPI = messageBodySendToSPI;
    }

    public EmailNotificationManager getEmailNotificationManager() {
        return emailNotificationManager;
    }

    public void setEmailNotificationManager(
            EmailNotificationManager emailNotificationManager) {
        this.emailNotificationManager = emailNotificationManager;
    }

    public String getSubjectAdminPIBComplete() {
        return subjectAdminPIBComplete;
    }

    public void setSubjectAdminPIBComplete(String subjectAdminPIBComplete) {
        this.subjectAdminPIBComplete = subjectAdminPIBComplete;
    }

    public String getMessageBodyAdminPIBComplete() {
        return messageBodyAdminPIBComplete;
    }

    public void setMessageBodyAdminPIBComplete(String messageBodyAdminPIBComplete) {
        this.messageBodyAdminPIBComplete = messageBodyAdminPIBComplete;
    }

    public String getAdminPIBCompleteRecipents() {
        return adminPIBCompleteRecipents;
    }

    public void setAdminPIBCompleteRecipents(String adminPIBCompleteRecipents) {
        this.adminPIBCompleteRecipents = adminPIBCompleteRecipents;
    }

    


    public File[] getMailAttachment() {
        return mailAttachment;
    }

    public void setMailAttachment(File[] mailAttachment) {
        this.mailAttachment = mailAttachment;
    }

    public String[] getMailAttachmentFileName() {
        return mailAttachmentFileName;
    }

    public void setMailAttachmentFileName(String[] mailAttachmentFileName) {
        this.mailAttachmentFileName = mailAttachmentFileName;
    }

    public String[] getMailAttachmentContentType() {
        return mailAttachmentContentType;
    }

    public void setMailAttachmentContentType(String[] mailAttachmentContentType) {
        this.mailAttachmentContentType = mailAttachmentContentType;
    }

    public PIBMethodologyWaiver getPibKantarMethodologyWaiver() {
        return pibKantarMethodologyWaiver;
    }

    public void setPibKantarMethodologyWaiver(
            PIBMethodologyWaiver pibKantarMethodologyWaiver) {
        this.pibKantarMethodologyWaiver = pibKantarMethodologyWaiver;
    }

    public String getKantarMethodologyWaiverAction() {
        return kantarMethodologyWaiverAction;
    }

    public void setKantarMethodologyWaiverAction(
            String kantarMethodologyWaiverAction) {
        this.kantarMethodologyWaiverAction = kantarMethodologyWaiverAction;
    }

    public String getOtherBATUsers() {
        return otherBATUsers;
    }

    public void setOtherBATUsers(String otherBATUsers) {
        this.otherBATUsers = otherBATUsers;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

	public String getPageRequest() {
		return pageRequest;
	}

	public void setPageRequest(String pageRequest) {
		this.pageRequest = pageRequest;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public List<ProjectCostDetailsBean> getProjectCostDetailsList() {
		return projectCostDetailsList;
	}

	public void setProjectCostDetailsList(
			List<ProjectCostDetailsBean> projectCostDetailsList) {
		this.projectCostDetailsList = projectCostDetailsList;
	}
	 public SynchroUtils getSynchroUtils() {
	        if(synchroUtils == null){
	            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
	        }
	        return synchroUtils;
	    }

	public Map<Long, List<User>> getEndMarketLegalApprovers() {
		return endMarketLegalApprovers;
	}

	public void setEndMarketLegalApprovers(
			Map<Long, List<User>> endMarketLegalApprovers) {
		this.endMarketLegalApprovers = endMarketLegalApprovers;
	}

	public List<ProjectCostDetailsBean> getProjectCostDetails() {
		return projectCostDetails;
	}

	public void setProjectCostDetails(
			List<ProjectCostDetailsBean> projectCostDetails) {
		this.projectCostDetails = projectCostDetails;
	}

	public List<Long> getEndMarketIds() {
		return endMarketIds;
	}

	public void setEndMarketIds(List<Long> endMarketIds) {
		this.endMarketIds = endMarketIds;
	}

	public Map<Integer, BigDecimal> getTotalCosts() {
		return totalCosts;
	}

	public void setTotalCosts(Map<Integer, BigDecimal> totalCosts) {
		this.totalCosts = totalCosts;
	}

	public ReportSummaryManagerNew getReportSummaryManagerNew() {
		return reportSummaryManagerNew;
	}

	public void setReportSummaryManagerNew(
			ReportSummaryManagerNew reportSummaryManagerNew) {
		this.reportSummaryManagerNew = reportSummaryManagerNew;
	}

	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}

	public ProjectSpecsManagerNew getProjectSpecsManagerNew() {
		return projectSpecsManagerNew;
	}

	public void setProjectSpecsManagerNew(
			ProjectSpecsManagerNew projectSpecsManagerNew) {
		this.projectSpecsManagerNew = projectSpecsManagerNew;
	}

	public PIBManager getPibManager() {
		return pibManager;
	}

	public void setPibManager(PIBManager pibManager) {
		this.pibManager = pibManager;
	}

	public ProjectManager getSynchroProjectManager() {
		return synchroProjectManager;
	}

	public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
		this.synchroProjectManager = synchroProjectManager;
	}

	public ProposalManager getProposalManager() {
		return proposalManager;
	}

	public void setProposalManager(ProposalManager proposalManager) {
		this.proposalManager = proposalManager;
	}

	public ProjectSpecsManager getProjectSpecsManager() {
		return projectSpecsManager;
	}

	public void setProjectSpecsManager(ProjectSpecsManager projectSpecsManager) {
		this.projectSpecsManager = projectSpecsManager;
	}

	public ReportSummaryManager getReportSummaryManager() {
		return reportSummaryManager;
	}

	public void setReportSummaryManager(ReportSummaryManager reportSummaryManager) {
		this.reportSummaryManager = reportSummaryManager;
	}

	public PIBManagerNew getPibManagerNew() {
		return pibManagerNew;
	}

	public ProjectManagerNew getSynchroProjectManagerNew() {
		return synchroProjectManagerNew;
	}

	public ProjectEvaluationManagerNew getProjectEvaluationManagerNew() {
		return projectEvaluationManagerNew;
	}

	public void setProjectEvaluationManagerNew(
			ProjectEvaluationManagerNew projectEvaluationManagerNew) {
		this.projectEvaluationManagerNew = projectEvaluationManagerNew;
	}

	public ProjectEvaluationManager getProjectEvaluationManager() {
		return projectEvaluationManager;
	}

	public void setProjectEvaluationManager(
			ProjectEvaluationManager projectEvaluationManager) {
		this.projectEvaluationManager = projectEvaluationManager;
	}

	public List<MigrateProjectBean> getGenerateReportBean() {
		return generateReportBean;
	}

	public void setGenerateReportBean(List<MigrateProjectBean> generateReportBean) {
		this.generateReportBean = generateReportBean;
	}

	
	
	    
}
