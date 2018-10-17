package com.grail.synchro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.grail.kantar.objecttype.KantarAttachmentObjectType;
import com.grail.kantar.util.KantarGlobals;
import com.grail.osp.objecttype.OSPAttachmentObjectType;
import com.grail.synchro.beans.Currency;
import com.grail.synchro.beans.MetaField;
import com.grail.synchro.beans.MetaFieldMapping;
import com.grail.synchro.beans.UserDepartment;
import com.grail.synchro.dwr.service.UserDepartmentsService;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUserPropertiesUtil;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.Group;
import com.jivesoftware.base.GroupNotFoundException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.JiveGlobals;

/**
 * @author Kanwar Grewal
 * @version 1.0, Date: 09/02/13
 */
public class SynchroGlobal { //implements InitializingBean {
    private static Map<Integer, String> productTypes = new LinkedHashMap<Integer, String>();
    private static Map<Integer, String> brands = new LinkedHashMap<Integer, String>();
    
    private static Map<Integer, String> allBrands = new LinkedHashMap<Integer, String>();
    
    private static Map<Integer, Integer> allBrandType = new LinkedHashMap<Integer, Integer>();
    
    private final static Map<Integer, String> projectTypes = new LinkedHashMap<Integer, String>();
    private static Map<Integer, String> currencies = new LinkedHashMap<Integer, String>();
    private static Map<Integer, String> currencyDescriptions = new HashMap<Integer, String>();
    private static Map<Integer, Currency> allCurrencyFields = new LinkedHashMap<Integer, Currency>();
    private static Map<Integer, String> suppliers = new HashMap<Integer, String>();
    private static Map<Integer, String> methodologies = new LinkedHashMap<Integer, String>();
    private static Map<Integer, MetaField> methodologyProperties = new LinkedHashMap<Integer, MetaField>();
    
    private static Map<Integer, MetaField> allMethodologyProperties = new LinkedHashMap<Integer, MetaField>();
    
    private static Map<Integer, String> allMethodologies = new LinkedHashMap<Integer, String>();
    private static Map<Integer, String> methodologyGroups = new LinkedHashMap<Integer, String>();
    private static Map<Integer, Map<Integer, String>> methodologyMapping = new LinkedHashMap<Integer, Map<Integer, String>>();
    
    private static Map<Integer, Map<Integer, String>> allMethodologyMapping = new LinkedHashMap<Integer, Map<Integer, String>>();
    
    private static Map<Integer, Map<Integer, String>> methodologyTypeMapping = new LinkedHashMap<Integer, Map<Integer, String>>();
    private static Map<Integer, Map<Integer, String>> collectionMapping = new LinkedHashMap<Integer, Map<Integer, String>>();
    private final static Map<Integer, String> budgetHolderLocations = new HashMap<Integer, String>();
    private final static Map<Integer, String> budgetHolderFunctions = new HashMap<Integer, String>();

    private final static Map<String, String[]> stageApproverGroup = new HashMap<String, String[]>();
    private final static Map<Integer, String> t15_t40EndMarket = new HashMap<Integer, String>();
    private static Map<Integer, Object[]> productBrandMapping = new LinkedHashMap<Integer, Object[]>();
    private static Map<Integer, String> tenderingAgency = new HashMap<Integer, String>();
    private static Map<Integer, String> supplierGroup = new HashMap<Integer, String>();
    private static Map<Integer, String> fieldWorkSuppliers = new HashMap<Integer, String>();
    private static Map<Integer, String> fieldWorkSupplierGroup = new HashMap<Integer, String>();
    private static Map<Integer, String> dataCollection = new LinkedHashMap<Integer, String>();
    private static Map<Integer, String> supplierGroupMapping = new HashMap<Integer, String>();
    private static Map<Integer, String> fwSupplierGroupMapping = new HashMap<Integer, String>();
    private static Map<Integer, String> projectIsMapping = new LinkedHashMap<Integer, String>();
    private static Map<Integer, String> waiverStatusList = new HashMap<Integer, String>();
    private static Map<String, String> pendingActivityList = new HashMap<String, String>();

    private static Map<Integer, String> availableEndMarkets = new LinkedHashMap<Integer, String>();
    private static Map<Integer, String> allavailableEndMarkets = new LinkedHashMap<Integer, String>();
    
    private static Map<Integer, String> departments = new LinkedHashMap<Integer, String>();
    private static Map<Integer, String> regions = new LinkedHashMap<Integer, String>();
    private static Map<Integer, String> allregions = new LinkedHashMap<Integer, String>();
    
    private static Map<Integer, Map<Integer, String>> regionEndMarketsMapping = new LinkedHashMap<Integer, Map<Integer, String>>();
    private static Map<Integer, String> areas = new LinkedHashMap<Integer, String>();
    private static Map<Integer, String> jobTitles = new LinkedHashMap<Integer, String>();
    private static Map<Integer, Map<Integer, String>> areaEndMarketsMapping = new LinkedHashMap<Integer, Map<Integer, String>>();

    private final static Map<String, Integer> projectActivityTab = new HashMap<String, Integer>();
    private final static Map<Integer, String> projectActivityMethod = new HashMap<Integer, String>();
    private final static Map<String, String> projectActivityMapping = new HashMap<String, String>();
    private final static Map<String, String> projectActivityName = new HashMap<String, String>();

    private static Map<String, String> endMarketRegionMap = new LinkedHashMap<String, String>();
    private static Map<String, String> endMarketAreaMap = new LinkedHashMap<String, String>();

    private static Map<String, String> countryCurrencyMap = new LinkedHashMap<String, String>();

    private static Map<String, Object> appProperties = new LinkedHashMap<String, Object>();

    private static Map<Integer, String> researchAgencies = new LinkedHashMap<Integer, String>();
    private static Map<Integer, String> allResearchAgencies = new LinkedHashMap<Integer, String>();
    
    private static Map<Integer, String> researchAgencyGroups = new LinkedHashMap<Integer, String>();
    private static Map<Integer, Map<Integer, String>> researchAgencyMapping = new LinkedHashMap<Integer, Map<Integer, String>>();
    private static Map<Integer, Map<Integer, String>> allResearchAgencyMapping = new LinkedHashMap<Integer, Map<Integer, String>>();

    /** Synchro Phase 5
     * @author kanwardeep.grewal
     * @return
     */
    private static Map<Integer, Map<Integer, String>> t20t40EndMarketsMapping = new LinkedHashMap<Integer, Map<Integer, String>>();
    private static Map<Integer, String> t20T40 = new LinkedHashMap<Integer, String>();
    
    private static Map<Integer, String> emApprovalMap = new LinkedHashMap<Integer, String>();
    private static Map<Integer, String> emTypeMap = new LinkedHashMap<Integer, String>();

    private static Map<Integer, Integer> endmarketApprovalTypeMap = new LinkedHashMap<Integer, Integer>();
    private static Map<Integer, Integer> endmarketMarketTypeMap = new LinkedHashMap<Integer, Integer>();
    
    private static Map<Integer, Integer> endmarketT20_T40_TypeMap = new LinkedHashMap<Integer, Integer>();
    
    private static Map<Integer, MetaField> methodologyDetailsMap = new LinkedHashMap<Integer, MetaField>();
    
    private static Map<Integer, String> grailResearchAgencies = new HashMap<Integer, String>();
    private static Map<Integer, String> grailResearchAgencyGroup = new HashMap<Integer, String>();
    private static Map<Integer, String> grailResearchAgencyGroupMapping = new HashMap<Integer, String>();
    
    private static Map<Integer, Integer> currencyGlobalMap = new LinkedHashMap<Integer, Integer>();
    
    private final static Map<Integer, String> agencyRatings = new HashMap<Integer, String>();
    
    private final static Map<Integer, String> spendForOptions = new HashMap<Integer, String>();
    
    //TODO Need to merge following two different maps-projectActivityTab, projectActivityMethod to one Object
    public static Map<String, Integer> getProjectActivityTab(){
        projectActivityTab.put("input", 1);
        projectActivityTab.put("proposal", 2);
        projectActivityTab.put("research", 3);
        projectActivityTab.put("screener", 4);
        projectActivityTab.put("projectEvaluation", 5);
        // projectActivityTab.put("presentation", 6);
        return projectActivityTab;
    }

    public static Map<Integer, String> getProjectActivityMethod(){
        projectActivityMethod.put(1, "input");
        projectActivityMethod.put(2, "proposal");
        projectActivityMethod.put(3, "research");
        projectActivityMethod.put(4, "screener");
        projectActivityMethod.put(5, "projectEvaluation");
        // projectActivityMethod.put(6, "presentation");
        return projectActivityMethod;
    }

    public static Map<String, String> getProjectActivityMapping(){
        projectActivityMapping.put("rib", "input");
        projectActivityMapping.put("ppf", "proposal");
        projectActivityMapping.put("rc", "research");
        projectActivityMapping.put("scf", "screener");
        projectActivityMapping.put("qdg", "questionaire");
        projectActivityMapping.put("ps", "presentation");
        return projectActivityMapping;
    }

    public static Map<String, String> getProjectActivityName(){
        projectActivityName.put("1","rib");
        projectActivityName.put("2","ppf");
        projectActivityName.put("3","rc");
        projectActivityName.put("4","scf");
        projectActivityName.put("5","qdg");
        projectActivityName.put("6","ps");
        return projectActivityName;
    }

    public static enum ProjectDocument {
        // Research Initiation Brief
        // Project Proposal and Funding
        // Research Commissioning
        // Screener/Consent Forms
        // Questionnaire/Discussion Guide
        // Presentation/Summary
        RESEARCH_INITIATION_BRIEF("rib", "Research Initiation Brief"),
        PROJECT_PROPOSAL_FUNDING("ppf", "Proposal"),
        RESEARCH_COMMISSIONING("rc", "Project Specs"),
        SCREENER_CONSENT_FORMS("scf", "Report Summary"),
        QUESTIONNAIRE_DISCUSSION_GUIDE("qdg", "Questionnaire/Discussion Guide"),
        PRESENTATION_SUMMARY("ps", "Presentation/Summary");

        final String code;
        final String description;

        ProjectDocument(final String key, final String desc) {
            this.code = key;
            this.description = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
        public static ProjectDocument getByCode(String code) {
            for (ProjectDocument projDocument : values()) {
                if (projDocument.code.equals(code)) {
                    return projDocument;
                }
            }
            throw new IllegalArgumentException("Specified Code does not relate to a valid Project Document");
        }
    };



    public static Map<Integer, String> getProjectIsMapping(){
        if(projectIsMapping==null || projectIsMapping.size()<1)
        {
            projectIsMapping = SynchroUtils.getMethodologyTypeFields();
            return projectIsMapping;
        }
        else
        {
            return projectIsMapping;
        }

    }

    public static enum ProjectStatus {
        DRAFT, PIT, PIB, INPROGRESS, INPROGRESS_PLANNING, INPROGRESS_FIELDWORK, INPROGRESS_ANALYSIS, INPROGRESS_IRIS, COMPLETED, DELETED;

        public static String getName(int id) {
            for (ProjectStatus status : values()) {
                if (status.ordinal() == id) {
                    return status.name();
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Status");
        }
    };

    public static enum ProjectActivationStatus {
        OPEN, ONHOLD, CANCEL, DELETED;

        public static String getName(int id) {
            for (ProjectActivationStatus status : values()) {
                if (status.ordinal() == id) {
                    return status.name();
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Status");
        }
    };

    public static enum Status {
        DRAFT("PIT(Draft)"),
        PIT_OPEN("PIT"),
        PIT_ONHOLD("On Hold PIT"),
        PIT_CANCEL("Cancelled PIT"),
        PIB_OPEN("PIB"),
        PIB_ONHOLD("On Hold PIB"),
        PIB_CANCEL("Cancelled PIB"),
        INPROGRESS_OPEN("In-Progress"),
        INPROGRESS_PLANNING("In-Progress Planning"),
        INPROGRESS_FIELDWORK("In-Progress Fieldwork"),
        INPROGRESS_ANALYSIS("In-Progress Analysis & Report"),
        INPROGRESS_IRIS("In-Progress Upload to IRIS"),
        INPROGRESS_ONHOLD("On Hold In-Progress"),
        INPROGRESS_CANCEL("Cancelled In-Progress"),
        COMPLETED("Completed"),
        COMPLETED_PROJ_EVALUATION("Completed Project Evaluation"),
        DELETED("Deleted");

        private String value;

        private Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Status getById(int id) {
            for (Status status : values()) {
                if (status.ordinal() == id) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Status");
        }

        public static String getName(long id) {
            for (Status status : values()) {
                if (status.ordinal() == id) {
                    return status.toString();
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Workflow");
        }

        @Override
        public String toString() {
            return this.value;
        }
    };

    public static Map<Integer, String> getProjectStatuses(final boolean isAdmin) {
        Map<Integer, String> statuses = new HashMap<Integer, String>();
        statuses.put(Status.PIT_OPEN.ordinal(), Status.PIT_OPEN.toString());
        statuses.put(Status.PIT_ONHOLD.ordinal(), Status.PIT_ONHOLD.toString());
        statuses.put(Status.PIB_OPEN.ordinal(), Status.PIB_OPEN.toString());
        statuses.put(Status.PIB_ONHOLD.ordinal(), Status.PIB_ONHOLD.toString());
        statuses.put(Status.INPROGRESS_OPEN.ordinal(), Status.INPROGRESS_OPEN.toString());
        statuses.put(Status.INPROGRESS_PLANNING.ordinal(), Status.INPROGRESS_PLANNING.toString());
        statuses.put(Status.INPROGRESS_FIELDWORK.ordinal(), Status.INPROGRESS_FIELDWORK.toString());
        statuses.put(Status.INPROGRESS_ANALYSIS.ordinal(), Status.INPROGRESS_ANALYSIS.toString());
        statuses.put(Status.INPROGRESS_IRIS.ordinal(), Status.INPROGRESS_IRIS.toString());
        statuses.put(Status.INPROGRESS_ONHOLD.ordinal(), Status.INPROGRESS_ONHOLD.toString());
        statuses.put(Status.COMPLETED.ordinal(), Status.COMPLETED.toString());
        if(isAdmin) {
            statuses.put(Status.DRAFT.ordinal(), Status.DRAFT.toString());
            statuses.put(Status.PIT_CANCEL.ordinal(), Status.PIT_CANCEL.toString());
            statuses.put(Status.PIB_CANCEL.ordinal(), Status.PIB_CANCEL.toString());
            statuses.put(Status.INPROGRESS_CANCEL.ordinal(), Status.INPROGRESS_CANCEL.toString());
            statuses.put(Status.DELETED.ordinal(), Status.DELETED.toString());
        }
        return statuses;
    }


    public static enum ProjectStatusFilterAll
    {
        OPEN(1, "Open"),
        ONHOLD(2, "On Hold"),
        CANCEL(3, "Cancelled"),
        COMPLETED(4, "Completed"),
        DELETED(5, "Deleted");

        int id;
        String description;

        ProjectStatusFilterAll(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static ProjectStatusFilterAll getById(int id) {
            for (ProjectStatusFilterAll projectStatusFilter : values()) {
                if (projectStatusFilter.id == id) {
                    return projectStatusFilter;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Stage ID FilterAll");
        }
    };



    public static enum StageStatus {
        PIB_STARTED, PIB_SAVED, PIB_COMPLETED, PROPOSAL_STARTED,PROPOSAL_SAVED,PROPOSAL_SUBMITTED,PROPOASL_AWARDED,
        PROJECT_SPECS_STARTED,PROJECT_SPECS_SAVED,PROJECT_SPECS_METH_WAIV_APP_PENDING, PROJECT_SPECS_METH_WAIV_MORE_INFO_REQ,PROJECT_SPECS_COMPLETED,REPORT_SUMMARY_STARTED,REPORT_SUMMARY_SAVED,REPORT_SUMMARY_COMPLETED, PROJ_EVAL_COMPLETED};



    public static enum MethodologyWaiverStatus {
        PIB_METH_WAIV_APP_PENDING, PIB_METH_WAIV_MORE_INFO_REQ,PROJECT_SPECS_METH_WAIV_APP_PENDING, PROJECT_SPECS_METH_WAIV_MORE_INFO_REQ};


    public static enum EmailNotification {
        PIB_COMPLETE_NOTIFY_AGENCY("%s - %s has moved to %s stage", "Hi<br><br>This is to notify that the PIB for the following project is complete:<br><br>Project Name: %s<br><br>Stage: <a href=\\'%s\\'>%s </a> "),
        SEND_FOR_APPROVAL_EXTERNALAGENCY("Approval required on - %s", "Hi<br><br>Please provide you approval on the following project:<br><br>Project Name: %s<br><br>Stage: <a href=\\'%s\\'>%s </a> (click here to reach the stage where approval is required)"),
        SEND_FOR_APPROVAL("Approval required on - %s", "Hi<br><br>Please provide you approval on the following project:<br><br>Project Name: %s<br><br>Stage: <a href=\\'%s\\'>%s </a> (click here to reach the stage where approval is required).<br><br>Please respond within 2 days of receiving this notification."),
        SEND_FOR_MARKETING_REVIEW("Documents review required on - %s", "Hi<br><br>Please review the documents uploaded at the following project stage:<br><br>Project Name: %s<br><br>Stage: <a href=\\'%s\\'>%s </a>(click here to reach the stage where approval is required).<br><br>Please respond within 2 days of receiving this notification"),
        SEND_FOR_OTHER_BAT_REVIEW("Documents review required on - %s", "Hi<br><br>Please review the documents uploaded at the following project stage:<br><br>Project Name: %s<br><br>Stage: <a href=\\'%s\\'>%s </a> (click here to reach the stage where approval is required).<br><br>Please respond within 2 days of receiving this notification"),
        SEND_FOR_SPI_REVIEW("Documents review required on - %s", "Hi<br><br>Please review the documents uploaded at the following project stage:<br><br>Project Name: %s<br><br>Stage: <a href=\\'%s\\'>%s </a> (click here to reach the stage where approval is required).<br><br>Please respond within 2 days of receiving this notification"),
        SEND_FOR_LEGAL_APPROVAL("Legal Approval required on - %s", "Hi<br><br>Please approve the documents uploaded for the following project stage:\\nProject Name: %s<br><br>Stage: <a href=\\'%s\\'>%s </a> (click here to reach the stage where approval is required).<br><br>Please respond within 3 days of receiving this notification"),
        APPROVE("%s has been approved by %s", "Hi<br><br>This is to notify that %s has approved the following project stage:<br><br>Project Name: %s<br><br>Stage: <a href=\\'%s\\'>%s </a> (click here to reach the %s stage of the project)"),
        NOTIFY_EXTERNAL_AGENCY("Project Specs has been approved on - %s", "Hi<br><br>This is to notify that the Project Specs for the following project has been approved:<br><br>Project Name: %s<br><br>Stage: <a href=\\'%s\\'>%s </a> (click here to reach the Project Specs stage of the project)"),
        NEEDS_REVISION("Revision required on - %s", "Hi<br><br>Please upload the revised documents based on the feedback for the following project:<br><br>Project Name: %s<br><br>Stage: <a href=\\'%s\\'>%s </a> (click here to reach the stage where approval is required)"),
        SEND_FOR_PROCUREMENT_REVIEW("Procurement Review required on - %s", "Hi<br><br>Please review the documents uploaded at the following project stage:<br><br>Project Name: %s<br><br>Stage: <a href=\\'%s\\'>%s </a> (click here to reach the stage where approval is required)."),
        APPROVE_FUNDING("Funding for %s has been approved by %s", "Hi<br><br>This is to notify that %s has approved the funding for the following project:<br><br>Project Name: <a href=\\'%s\\'>%s </a> (click here to reach the Proposal stage of the project)"),
        READY_TO_UPLOAD_TO_RKP("Presentation/Summary has been approved on - %s", "Hi<br><br>This is to notify that the Presentation/Summary for the following project has been approved:<br><br>Project Name: %s<br><br>Stage: <a href=\\'%s\\'>%s </a> (click here to reach the Presentation/Summary stage of the project)"),
        PROJECT_ONHOLD("%s - %s is on-hold", "Hi,<br><br>Your project <a href=\\'%s\\'>%s </a> has been put 'On-Hold'.<br><br>You will not be able to take any actions on this project unless it is reopened."),
        PROJECT_CANCELLED("%s - %s has been cancelled", "Hi,<br><br>Your project <a href=\\'%s\\'>%s </a> has been 'Cancelled'.<br><br>You will not be able to take any actions on this project."),
        PROJECT_DELETED("%s - %s is deleted from Synchro", "Hi,<br><br>This is to notify that %s has been deleted from Synchro.<br><br>This project no longer exists in the system."),
        PROJECT_REOPENED("%s - %s has been reopened", "Hi,<br><br>This is to notify that <a href=\\'%s\\'>%s </a> has been reopened."),
        PROJECT_COMPLETED("%s - %s has completed", "Hi,<br><br>This is to notify that project - <a href=\\'%s\\'>%s </a> has completed. You can now provide Agency feedback by clicking on the following link:<br><br><a href=\\'%s\\'>Link to the Project Evaluation tab of the project</a>"),
        ADD_PROJECT_STAKEHOLDER("New Project on Synchro | %s - %s", "Hi,<br><br>You have been made a part of the following project:<br><br>Project Name: <a href=\\'%s\\'>%s </a> (click on the link to view the project details)<br><br>Project ID: %s"),
        ADD_PROJECT_OWNER("You own a new project | %s - %s", "Hi,<br><br>You have been added as a Project Owner of the following project:<br><br>Project Name: <a href=\\'%s\\'>%s </a> (click on the link to view the project details)<br><br>Project ID: %s"),
        ADD_PROJECT_CONTACT("You are a Project contact on a new project | %s - %s", "Hi,<br><br>You have been added as a Project Contact on the following project:<br><br>Project Name: <a href=\\'%s\\'>%s </a> (click on the link to view the project details)<br><br>Project ID: %s"),
        SEND_FOR_METHODOLOGY_APPROVER("Waiver Approval Required for Methodology Deviation", "Hi<br><br>This is to notify that a waiver is pending your approval for the following project:<br><br>Project Name: <a href=\\'%s\\'>%s </a> (click here to reach end-market details tab of the project)"),
        NEW_WAIVER_ADDED("%s - %s has been initiated", "Hi,<br><br>This is to notify that waiver - %s has been raised by %s and is pending your approval. Please click on the following link for further details:<br><br><a href=\\'%s\\'>Link to the Waiver Page</a>"),
        WAIVER_APPROVE("%s - %s has been %s", "Hi,<br><br>This is to notify that waiver - %s has been %s. Please click on the following link for further details:<br><br><a href=\\'%s\\'>Link to the Waiver Page</a>"),
        PROPOSAL_REQ_CLARIFICATION("Proposal Clarification Request for Project - %s", "Hi,<br><br>Please provide more clarification for Proposal for project:<br><br>Project Name: %s<br><br> <a href=\\'%s\\'>%s </a> (click on the link to view the project details)"),
        PIB_CHANGE_AFTER_COMPETION("PIB Change","Hi,<br><br> This is to notify that PIB has been changed after completion for project : %s"),
        PROPOSAL_SEND_TO_PROJECT_OWNER("Proposals are submitted for Project - %s", "Hi,<br><br>Proposals are submitted for project:<br><br>Project Name: %s<br><br> <a href=\\'%s\\'>%s </a> (click on the link to view the project details)"),
        PROPOSAL_SEND_TO_SPI("Proposal Feedback for Project - %s", "Hi,<br><br>Proposal feedback for project:<br><br>Project Name: %s<br><br> <a href=\\'%s\\'>%s </a> (click on the link to view the project details)"),
        SEND_TO_PROJECT_OWNER("Notify for Project - %s", "Hi,<br><br>Report Summary for project:<br><br>Project Name: %s<br><br> <a href=\\'%s\\'>%s </a> (click on the link to view the project details)");

        String subject;
        String messageBody;

        EmailNotification(final String subject, final String messageBody) {
            this.subject = subject;
            this.messageBody = messageBody;
        }

        public String getSubject() {
            return subject;
        }

        public String getMessageBody() {
            return messageBody;
        }

    };

    /*********** Helper method to get the list items *************/
    public static Map<Integer, String> getProductTypes(){

        if(productTypes==null || productTypes.size()<1)
        {
            productTypes = SynchroUtils.getProductFields();
            return productTypes;
        }
        else
        {
            return productTypes;
        }
    }

    public static Map<Integer, String> getCurrencies()
    {
        if(currencies==null || currencies.size()<1)
        {
            currencies = SynchroUtils.getCurrencyFields();
            return currencies;
        }
        else
        {
            return currencies;
        }
    }
    public static Map<Integer, Currency> getAllCurrencyFields()
    {
    	if(allCurrencyFields==null || allCurrencyFields.size()<1)
        {
    		allCurrencyFields = SynchroUtils.getAllCurrencyFields();
            return allCurrencyFields;
        }
        else
        {
            return allCurrencyFields;
        }
    }

    public static Map<Integer, String> getCurrencyDescriptions()
    {
        if(currencyDescriptions==null || currencyDescriptions.size()<1)
        {
            currencyDescriptions = SynchroUtils.getCurrencyDescriptionFields();
            return currencyDescriptions;
        }
        else
        {
            return currencyDescriptions;
        }
    }

    
    public static Map<Integer, Integer> getCurrencyGlobalFields()
    {
        if(currencyGlobalMap==null || currencyGlobalMap.size() < 1)
        {
        	currencyGlobalMap = SynchroUtils.getCurrencyGlobalFields();
            return currencyGlobalMap;
        }
        else
        {
            return currencyGlobalMap;
        }
    }

    public static Map<Integer, String> getBudgetHolderLocations(){
        budgetHolderLocations.put(1, "BHL_1");
        budgetHolderLocations.put(2, "BHL_2");
        budgetHolderLocations.put(3, "BHL_3");
        budgetHolderLocations.put(4, "BHL_4");
        return budgetHolderLocations;
    }

    public static Map<Integer, String> getBudgetHolderFunctions(){
        budgetHolderFunctions.put(1, "BHF_1");
        budgetHolderFunctions.put(2, "BHF_2");
        budgetHolderFunctions.put(3, "BHF_3");
        budgetHolderFunctions.put(4, "BHF_4");
        return budgetHolderFunctions;
    }


    public static Map<Integer, Object[]> getProductBrandMapping(){

        for(Integer productID : getProductTypes().keySet())
        {
            List<Integer> brandList = new ArrayList<Integer>();
            List<MetaField> brands = SynchroUtils.getBrandsByProduct(productID);
            for(MetaField field : brands)
            {
                brandList.add(field.getId().intValue());
            }
            productBrandMapping.put(productID, brandList.toArray());
        }

        return productBrandMapping;

    }
    public static Map<Integer, String> getBrands(){
        return SynchroUtils.getBrandFields();
    }
    
    public static Map<Integer, String> getAllBrands(){
    	 if(allBrands==null || allBrands.size()<1)
         {
    		 allBrands = SynchroUtils.getBrandAllFields();
         }
         return allBrands;
    	
    }
    
    
    public static Map<Integer, Integer> getAllBrandType(){
   	 if(allBrandType==null || allBrandType.size()<1)
        {
   		allBrandType = SynchroUtils.getAllBrandBrandTypeFields();
        }
        return allBrandType;
   	
   }
    public static Map<Integer, String> getBrands(final boolean fetchAll, final Long productType){

        if(brands==null || brands.size()<1)
        {
            brands = SynchroUtils.getBrandFields();
        }
        return brands;
    }

    public static Map<Integer, String> getBrandsByProduct()
    {
        Map<Integer,Object[]>  productBrands = getProductBrandMapping();
        Map<Integer, String> allListMap = new LinkedHashMap<Integer, String>();
        for(Integer product : productBrands.keySet())
        {
            String str="";
            Object[] brands = productBrands.get(product);
            for(Object obj : brands)
            {
                str = str + obj;
                str = str + ",";
            }
            allListMap.put(product, str.endsWith(",")?str.substring(0, str.length()-1):str);
        }
        return allListMap;
    }
    //regionEndMarket


    public static Map<String, String> getEndMarketRegionMap(){
        if(endMarketRegionMap==null || endMarketRegionMap.size()<1)
        {
            List<MetaFieldMapping> mappingList = SynchroUtils.getEndMarketRegionMapping();

            for(MetaFieldMapping mapping : mappingList)
            {
                endMarketRegionMap.put(mapping.getEid().toString(), mapping.getId().toString());
            }

            return endMarketRegionMap;
        }
        else
        {
            return endMarketRegionMap;
        }
    }

    public static Map<String, String> getEndMarketAreaMap(){
        if(endMarketAreaMap==null || endMarketAreaMap.size()<1)
        {
            List<MetaFieldMapping> mappingList = SynchroUtils.getEndMarketAreaMapping();

            for(MetaFieldMapping mapping : mappingList)
            {
                endMarketAreaMap.put(mapping.getEid().toString(), mapping.getId().toString());
            }

            return endMarketAreaMap;
        }
        else
        {
            return endMarketAreaMap;
        }
    }

    public static Map<Integer, String> getEndMarkets(){
        if(availableEndMarkets==null || availableEndMarkets.size()<1)
        {
            availableEndMarkets = SynchroUtils.getEndMarketFields();
            return availableEndMarkets;
        }
        else
        {
            return availableEndMarkets;
        }
    }

    public static Map<Integer, String> getAllEndMarkets(){
        if(allavailableEndMarkets==null || allavailableEndMarkets.size()<1)
        {
        	allavailableEndMarkets = SynchroUtils.getAllEndMarketFields();
            return allavailableEndMarkets;
        }
        else
        {
            return allavailableEndMarkets;
        }
    }
    
    public static Map<Integer, String> getNonEUEndMarkets(){
        
    	Map<Integer, String> nonEUEM = new HashMap<Integer, String>();
    	
    	 for(Integer eid : getEndmarketMarketTypeMap().keySet())
         {
         	Integer endMarketType = getEndmarketMarketTypeMap().get(eid);
         	if(endMarketType!=null && endMarketType==2)
         	{
         		nonEUEM.put(eid, availableEndMarkets.get(eid));
         	}
         } 
    	return nonEUEM;
    }
    
    public static Map<Integer, String> getGlobalEndMarket(){
        
    	Map<Integer, String> globalEM = new HashMap<Integer, String>();
    	
    	String eId = JiveGlobals.getJiveProperty("synchro.global.endmarketId");
    	globalEM.put(new Integer(eId), "Global");
    	
    	return globalEM;
    }
    
    public static Map<Integer, String> getEUEndMarkets(){
        
    	Map<Integer, String> nonEUEM = new HashMap<Integer, String>();
    	
    	 for(Integer eid : getEndmarketMarketTypeMap().keySet())
         {
         	Integer endMarketType = getEndmarketMarketTypeMap().get(eid);
         	if(endMarketType!=null && endMarketType==1)
         	{
         		nonEUEM.put(eid, availableEndMarkets.get(eid));
         	}
         } 
    	return nonEUEM;
    }

    public static Map<Integer, String> getEndMarkets(final User user){
        Map<Integer, String> endmarkets = getEndMarkets();
        boolean  globalSuperUser = SynchroPermHelper.isSynchroAdmin(user)
                || SynchroPermHelper.isSynchroMiniAdmin(user) || SynchroPermHelper.isSynchroGlobalSuperUser(user);
        if(globalSuperUser) {
            return endmarkets;
        } else  {
            Map<Integer, String> endmarketAccessList = new HashMap<Integer, String>();
            Set<Integer> enmarketIds = endmarkets.keySet();
            Map<String, String> userProperties = user.getProperties();
            if(userProperties != null && !userProperties.isEmpty()) {
                if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)
                        && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).equals("")) {
                    String endmarketAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST);
                    if(endmarketAccessListStr != null && !endmarketAccessListStr.equals("")) {
                        String [] endmarketAccessListArr = endmarketAccessListStr.split(",");
                        for(String endmarketIdStr : endmarketAccessListArr) {
                            Integer endmarketId = Integer.parseInt(endmarketIdStr);
                            if(enmarketIds.contains(endmarketId)) {
                                endmarketAccessList.put(endmarketId, endmarkets.get(endmarketId));
                            }
                        }
                    }
                }
            }

            return endmarketAccessList;
        }
    }

    public static Map<Integer, String> getProjectTypes(){
        projectTypes.put(1,"CAP1");
        projectTypes.put(2,"CAP2");
        projectTypes.put(3,"CAP3");
        projectTypes.put(4,"Not known");
        return projectTypes;
    }


    public static Map<Integer, String> getMethodologies(){
        if(methodologies==null || methodologies.size()<1)
        {
            methodologies = SynchroUtils.getMethodologyFields();
            return methodologies;
        }
        else
        {
            return methodologies;
        }

    }
    public static Map<Integer, MetaField> getMethodologyProperties(){
        if(methodologyProperties==null || methodologyProperties.size()<1)
        {
        	methodologyProperties = SynchroUtils.getMethodologyFieldProperties();
            return methodologyProperties;
        }
        else
        {
            return methodologyProperties;
        }

    }
    
    public static Map<Integer, MetaField> getAllMethodologyProperties(){
        if(allMethodologyProperties==null || allMethodologyProperties.size()<1)
        {
        	allMethodologyProperties = SynchroUtils.getAllMethodologyFieldProperties();
            return allMethodologyProperties;
        }
        else
        {
            return allMethodologyProperties;
        }

    }



    public static Map<Integer, String> getAllMethodologies(){
        if(allMethodologies==null || allMethodologies.size()<1)
        {
            allMethodologies = SynchroUtils.getAllMethodologyFields();
            return allMethodologies;
        }
        else
        {
            return allMethodologies;
        }

    }

    /**
     * This method will check whether the methodology is Active or not
     * @param methodologyId
     * @return
     */
    public static boolean isActiveMethodology(Integer methodologyId){
        
    	if(getMethodologies()!=null && getMethodologies().containsKey(methodologyId))
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
   
    
    public static boolean isMethodologyTypeActive(final Long id) {
        return SynchroUtils.isMethodologyTypeActive(id);
    }

    public static Map<Integer, String> getSelectedInActiveMethodologyFields(final List<Long> ids) {
        return SynchroUtils.getSelectedInActiveMethodologyFields(ids);
    }

    public static List<MetaField> getUnselectedMethodologiesForMethType() {
        return SynchroUtils.getUnselectedMethodologiesForMethType();
    }

    public static List<MetaField> getMethodologiesByType(final Long id) {
        return SynchroUtils.getMethodologiesByType(id);
    }

    public static Map<Integer, String> getMethodologiesMapByType(final Long id) {
        Map<Integer, String> map = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = SynchroUtils.getMethodologiesByType(id);
        if(fields != null && fields.size() > 0) {
            for(MetaField meth: fields) {
                map.put(meth.getId().intValue(), meth.getName());
            }
        }
        return map;

    }

    public static List<MetaField> getUnselectedMethodologyGroupsForType() {
        return SynchroUtils.getUnselectedMethodologyGroupsForType();
    }

    public static List<MetaField> getMethodologyGrpsByType(final Long id) {
        return SynchroUtils.getMethodologyGrpsByType(id);
    }

    public static Long getMethodologyTypeByProposedMethodology(final Long id) {
        return SynchroUtils.getMethodologyTypeByProposedMethodology(id);
    }

    public static Set<Long> getMethodologyTypesByProposedMethodologies(final List<Long> ids) {
        return SynchroUtils.getMethodologyTypesByProposedMethodologies(ids);
    }

    public static Long getMethodologyTypeByGroup(final Long id) {
        return SynchroUtils.getMethodologyTypeByGroup(id);
    }

    public static Long getMethodologyTypeByProsedMethodologies(final List<Long> ids) {
        Long methodologyType = -1L;
        Set<Long> mtIds = SynchroGlobal.getMethodologyTypesByProposedMethodologies(ids);
        if(mtIds != null && mtIds.size() > 0) {
            if((mtIds.contains(1L) && mtIds.contains(2L) && mtIds.contains(3L))
                    || (mtIds.contains(1L) && mtIds.contains(2L)) || (mtIds.contains(1L) && mtIds.contains(3L))
                    || (mtIds.contains(2L) && mtIds.contains(3L)) || mtIds.contains(3L)) {
                methodologyType = 3L;
            } else if(mtIds.contains(1L)) {
                methodologyType = 1L;
            } else if(mtIds.contains(2L)) {
                methodologyType = 2L;
            }
        }
        return methodologyType;
    }

    public static Map<Integer, String> getMethodologyGroups(final boolean fetchAll, final Long methodology){

        if(!fetchAll && methodology>0)
        {
            return getMethodologyMapping().get(methodology.intValue());
        }
        else
        {
            if(methodologyGroups==null || methodologyGroups.size()<1)
            {
                methodologyGroups = SynchroUtils.getMethodologyGroupFields();
                return methodologyGroups;
            }
            else
            {
                return methodologyGroups;
            }
        }
    }
    
	 // This method will fetch the methodology Group Name for a particular methodology
	    public static String getMethodologyGroupName(final Long methodology)
	    {
	
			String methGroup = "";
			if(methodology>0)
			{
				Map<Integer, Map<Integer, String>> methGroupMeth =  getMethodologyMapping();
				if(methGroupMeth!=null)
				{
					//Set<Integer> methGroups = methGroupMeth.keySet();
					for(Integer methGroups: methGroupMeth.keySet())
					{
						for(Integer meth: methGroupMeth.get(methGroups).keySet())
						{
							if(meth==methodology.intValue())
							{
								methGroup=methodologyGroups.get(methGroups);
							}
						}
					}
				}
			}
			return methGroup;
	}
	    
	    // This method will fetch the methodology Group Id for a particular methodology
	    public static Integer getMethodologyGroupId(final Long methodology)
	    {
	
			Integer methGroup = null;
			if(methodology>0)
			{
				Map<Integer, Map<Integer, String>> methGroupMeth =  getMethodologyMapping();
				if(methGroupMeth!=null)
				{
					//Set<Integer> methGroups = methGroupMeth.keySet();
					for(Integer methGroups: methGroupMeth.keySet())
					{
						for(Integer meth: methGroupMeth.get(methGroups).keySet())
						{
							if(meth==methodology.intValue())
							{
								methGroup=methGroups;
							}
						}
					}
				}
			}
			return methGroup;
	}

	    // This method will fetch the methodology Group Id for a particular methodology even for Inactive ones also
	    public static Integer getAllMethodologyGroupId(final Long methodology)
	    {
	
			Integer methGroup = null;
			if(methodology>0)
			{
				Map<Integer, Map<Integer, String>> methGroupMeth =  getAllMethodologyMapping();
				if(methGroupMeth!=null)
				{
					//Set<Integer> methGroups = methGroupMeth.keySet();
					for(Integer methGroups: methGroupMeth.keySet())
					{
						for(Integer meth: methGroupMeth.get(methGroups).keySet())
						{
							if(meth==methodology.intValue())
							{
								methGroup=methGroups;
							}
						}
					}
				}
			}
			return methGroup;
	}

	// This method will have the Region Id for each End Market Id    
	public static  Map<Integer, Integer> getRegionIdEndMarketsMapping()
    {
		 Map<Integer, Integer> endMarketMap = new LinkedHashMap<Integer, Integer>();
		
		if(getRegionEndMarketsMapping()!=null && getRegionEndMarketsMapping().size() > 0)
        {
            for(Integer region : getRegions().keySet())
            {
                List<MetaField> endmarkets = SynchroUtils.getEndMarketsByRegion(region);
               
                for(MetaField field : endmarkets)
                {
                    endMarketMap.put(field.getId().intValue(), region);
                }
               
            }
           
        }
        return endMarketMap;
    }
	
	// This method will have the Area Id for each End Market Id    
		public static  Map<Integer, Integer> getAreaIdEndMarketsMapping()
	    {
			Map<Integer, Integer> endMarketMap = new LinkedHashMap<Integer, Integer>();
			if(getAreaEndMarketsMapping()!=null && getAreaEndMarketsMapping().size() > 0)
	        {
	            for(Integer area : getAreas().keySet())
	            {
	                List<MetaField> endmarkets = SynchroUtils.getEndMarketsByArea(area);
	              
	                for(MetaField field : endmarkets)
	                {
	                    endMarketMap.put(field.getId().intValue(), area);
	                }
	                
	            }

	        }
		      
	        return endMarketMap;
	    }
    /**
     * Synchro Phase 5
     * @param methodology
     * @return
     */
    
    public static Map<Integer, MetaField> getMethodologyOtherProperties(){

    	if(methodologyDetailsMap==null || methodologyDetailsMap.size()<1)
        {
    		methodologyDetailsMap = SynchroUtils.getMethodologyOtherProperties();
            return methodologyDetailsMap;
        }
        else
        {
            return methodologyDetailsMap;
        }
        
    }
    
    public static Map<Integer, String> getMethodologiesByGroup(final boolean fetchAll, final Long methodologyGroup){

        if(!fetchAll && methodologyGroup>0)
        {
            return getMethodologyMapping().get(methodologyGroup.intValue());
        }
        else
        {
            if(methodologies==null || methodologies.size()<1)
            {
                methodologies = SynchroUtils.getMethodologyFields();
                return methodologies;
            }
            else
            {
                return methodologies;
            }
        }
    }

    public static  Map<Integer, Map<Integer, String>> getMethodologyMapping()
    {
        if(methodologyMapping==null || methodologyMapping.size()<1)
        {
            for(Integer methdologyGroup : getMethodologyGroups(true, new Long(1)).keySet())
            {
                List<MetaField> methdologies = SynchroUtils.getMethodologyByGroup(methdologyGroup);
                Map<Integer, String> methodologyMap = new LinkedHashMap<Integer, String>();
                for(MetaField field : methdologies)
                {
                    methodologyMap.put(field.getId().intValue(), field.getName());
                }
                methodologyMapping.put(methdologyGroup, methodologyMap);
            }
            return methodologyMapping;
        }
        else
        {
            return methodologyMapping;
        }
    }

    public static  Map<Integer, Map<Integer, String>> getAllMethodologyMapping()
    {
        if(allMethodologyMapping==null || allMethodologyMapping.size()<1)
        {
            for(Integer methdologyGroup : getMethodologyGroups(true, new Long(1)).keySet())
            {
                List<MetaField> methdologies = SynchroUtils.getAllMethodologyByGroup(methdologyGroup);
                Map<Integer, String> methodologyMap = new LinkedHashMap<Integer, String>();
                for(MetaField field : methdologies)
                {
                    methodologyMap.put(field.getId().intValue(), field.getName());
                }
                allMethodologyMapping.put(methdologyGroup, methodologyMap);
            }
            return allMethodologyMapping;
        }
        else
        {
            return allMethodologyMapping;
        }
    }
    
    public static  Map<Integer, Map<Integer, String>> getMethodologyTypeMapping()
    {
        if(methodologyTypeMapping==null || methodologyTypeMapping.size()<1)
        {
            Map<Integer, String> methTypsMap = SynchroUtils.getMethodologyTypeFields();
            Set<Integer>  keys = methTypsMap.keySet();
            for(Integer methTypeId : keys) {
                List<MetaField> methdologies = SynchroUtils.getMethodologyByType(methTypeId);
                Map<Integer, String> methodologyMap = new LinkedHashMap<Integer, String>();
                for(MetaField field : methdologies)
                {
                    methodologyMap.put(field.getId().intValue(), field.getName());
                }
                methodologyTypeMapping.put(methTypeId, methodologyMap);
            }

            return methodologyTypeMapping;
        }
        else
        {
            return methodologyTypeMapping;
        }
    }

    public static  Map<Integer, Map<Integer, String>> getCollectionMapping()
    {
        if(collectionMapping==null || collectionMapping.size()<1)
        {
            for(Integer type : getProjectIsMapping().keySet())
            {
                List<MetaField> dataCollections = SynchroUtils.getDataCollections(type);
                Map<Integer, String> collectionMap = new LinkedHashMap<Integer, String>();
                for(MetaField field : dataCollections)
                {
                    collectionMap.put(field.getId().intValue(), field.getName());
                }
                collectionMapping.put(type, collectionMap);
            }
            return collectionMapping;
        }
        else
        {
            return collectionMapping;
        }
    }


    public static Map<String, String[]> getApproverGroup()
    {
        stageApproverGroup.put("1", new String[]{SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_LEGAL_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_LEGAL_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME});
        stageApproverGroup.put("2", new String[]{SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME});
        stageApproverGroup.put("3", new String[]{SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME});
        stageApproverGroup.put("4", new String[]{SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_LEGAL_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_LEGAL_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME});
        stageApproverGroup.put("5", new String[]{SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_LEGAL_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_LEGAL_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME});
        stageApproverGroup.put("6", new String[]{SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_LEGAL_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_LEGAL_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME,SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME});
        return stageApproverGroup;
    }
    public static Long getMappingResearchType(final Long methodology) {
        return 1L;
    }

    public static Long getMappingMethodologyGroup(final Long methodology) {
        return 1L;
    }


    public static enum FinancialDetailsStatus {
        NONE, SAVED, FREEZE;

        public static String getName(int id) {
            for (FinancialDetailsStatus status : values()) {
                if (status.ordinal() == id) {
                    return status.name();
                }
            }
            throw new IllegalArgumentException(
                    "Specified id does not relate to a valid Financial Details Status");
        }
    };

    public static enum PIBBrandMappingType {
        REFERENCE_BRAND(1), SMOKER_GROUP(2);
        int id;
        PIBBrandMappingType(final int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }


        public static PIBBrandMappingType getById(int id) {
            for (PIBBrandMappingType mappingType : values()) {
                if (mappingType.id == id) {
                    return mappingType;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid PIB Mapping Type");
        }

    };

    // Map which contains which PIB fields to be hidden for a particular methodology type
    public static Map<Integer, String> getPIBFieldsByMethodology()
    {

        Map<Integer, String> pibMethodologyFields = new HashMap<Integer, String>();
        pibMethodologyFields.put(2, "actionStandard");
        //pibMethodologyFields.put(3, "userReferenceBrand,userSobBrand");
        pibMethodologyFields.put(11, "targetSegment,bizSteps,actionStandard,researchDesign,stimulus,reportingRequirements");
        pibMethodologyFields.put(12, "targetSegment,bizQuestion,bizSteps,researchObjective,actionStandard,researchDesign,stimulus,reportingRequirements,payArrangement");
        pibMethodologyFields.put(13, "targetSegment,bizSteps,actionStandard");
        pibMethodologyFields.put(14, "targetSegment,bizSteps,actionStandard");
        pibMethodologyFields.put(16, "actionStandard");
        pibMethodologyFields.put(19, "targetSegment");
        //pibMethodologyFields.put(17, "userReferenceBrand,userSobBrand");
        pibMethodologyFields.put(25, "actionStandard");
        pibMethodologyFields.put(26, "targetSegment,actionStandard");
        pibMethodologyFields.put(28, "targetSegment,actionStandard");
        pibMethodologyFields.put(30, "targetSegment,bizSteps,actionStandard");
        pibMethodologyFields.put(31, "actionStandard");
        pibMethodologyFields.put(32, "targetSegment,bizSteps,actionStandard");
        pibMethodologyFields.put(40, "targetSegment,bizSteps,actionStandard");
        pibMethodologyFields.put(42, "bizSteps,actionStandard");
        return pibMethodologyFields;

    }
    // Map which contains which End Market fields to be shown for a particular methodology type
    public static Map<Integer, String> getEndMarketsFieldsByMethodology()
    {
        Map<Integer, String> endMarketMethodologyFields = new HashMap<Integer, String>();
        endMarketMethodologyFields.put(1, "totalinterviews,cells");
        endMarketMethodologyFields.put(2, "totalinterviews,depthinterviews,focusGroups");
        endMarketMethodologyFields.put(3, "totalinterviews,cells");
        endMarketMethodologyFields.put(4, "totalinterviews,cells");
        endMarketMethodologyFields.put(5, "totalinterviews,cells");
        endMarketMethodologyFields.put(7, "totalinterviews,depthinterviews,focusGroups");
        endMarketMethodologyFields.put(8, "totalinterviews,depthinterviews,focusGroups");
        endMarketMethodologyFields.put(9, "totalinterviews,depthinterviews,focusGroups");
        endMarketMethodologyFields.put(10, "waves,totalinterviews");
        endMarketMethodologyFields.put(11, "totalinterviews");
        endMarketMethodologyFields.put(13, "waves,totalinterviews");
        endMarketMethodologyFields.put(14, "waves,totalinterviews");
        endMarketMethodologyFields.put(15, "waves,totalinterviews");
        endMarketMethodologyFields.put(16, "totalinterviews");
        endMarketMethodologyFields.put(17, "waves,totalinterviews");
        endMarketMethodologyFields.put(18, "totalinterviews,cells");
        endMarketMethodologyFields.put(19, "totalinterviews");
        endMarketMethodologyFields.put(20, "totalinterviews,cells");
        endMarketMethodologyFields.put(21, "totalinterviews,cells");
        endMarketMethodologyFields.put(22, "totalinterviews,cells");
        endMarketMethodologyFields.put(23, "totalinterviews,cells");
        endMarketMethodologyFields.put(24, "totalinterviews,cells");
        endMarketMethodologyFields.put(25, "totalinterviews,depthinterviews,focusGroups");
        endMarketMethodologyFields.put(26, "totalinterviews,cells");
        endMarketMethodologyFields.put(27, "totalinterviews");
        endMarketMethodologyFields.put(28, "totalinterviews,cells");
        endMarketMethodologyFields.put(29, "totalinterviews");
        endMarketMethodologyFields.put(30, "waves");
        endMarketMethodologyFields.put(31, "depthinterviews,focusGroups");
        endMarketMethodologyFields.put(32, "totalinterviews");
        endMarketMethodologyFields.put(33, "totalinterviews,depthinterviews,focusGroups");
        endMarketMethodologyFields.put(34, "totalinterviews,depthinterviews,focusGroups");
        endMarketMethodologyFields.put(35, "totalinterviews,depthinterviews,focusGroups");
        endMarketMethodologyFields.put(36, "totalinterviews,depthinterviews,focusGroups");
        endMarketMethodologyFields.put(37, "totalinterviews");
        endMarketMethodologyFields.put(38, "totalinterviews");
        endMarketMethodologyFields.put(39, "totalinterviews");
        endMarketMethodologyFields.put(40, "waves,totalinterviews");
        endMarketMethodologyFields.put(41, "totalinterviews");
        endMarketMethodologyFields.put(42, "waves,totalinterviews");
        return endMarketMethodologyFields;
    }

    public enum T15_T40 {

        NONE("None"),
        T15("T15"),
        T40("T40");

        private String displayName;

        T15_T40(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }

    //TODO- Need to repopulate the list again once it has been finalized by client.
    public static Map<Integer, String> getT15_T40_EndMarket(){
        t15_t40EndMarket.put(1, T15_T40.T15.displayName);
        t15_t40EndMarket.put(2, T15_T40.T15.displayName);
        t15_t40EndMarket.put(3, T15_T40.T15.displayName);
        t15_t40EndMarket.put(4, T15_T40.T15.displayName);
        t15_t40EndMarket.put(5, T15_T40.T15.displayName);
        t15_t40EndMarket.put(6, T15_T40.T15.displayName);
        t15_t40EndMarket.put(7, T15_T40.T15.displayName);
        t15_t40EndMarket.put(8, T15_T40.T15.displayName);
        t15_t40EndMarket.put(9, T15_T40.T15.displayName);
        t15_t40EndMarket.put(10, T15_T40.T15.displayName);
        t15_t40EndMarket.put(11, T15_T40.T15.displayName);
        t15_t40EndMarket.put(12, T15_T40.T15.displayName);
        t15_t40EndMarket.put(13, T15_T40.T15.displayName);
        t15_t40EndMarket.put(14, T15_T40.T15.displayName);
        t15_t40EndMarket.put(15, T15_T40.T15.displayName);
        t15_t40EndMarket.put(16, T15_T40.T15.displayName);
        t15_t40EndMarket.put(17, T15_T40.T15.displayName);
        t15_t40EndMarket.put(18, T15_T40.T15.displayName);
        t15_t40EndMarket.put(19, T15_T40.T15.displayName);
        t15_t40EndMarket.put(20, T15_T40.T15.displayName);
        t15_t40EndMarket.put(21, T15_T40.T15.displayName);
        t15_t40EndMarket.put(22, T15_T40.T15.displayName);
        t15_t40EndMarket.put(23, T15_T40.T15.displayName);
        t15_t40EndMarket.put(24, T15_T40.T15.displayName);
        t15_t40EndMarket.put(25, T15_T40.T15.displayName);
        t15_t40EndMarket.put(26, T15_T40.T15.displayName);
        t15_t40EndMarket.put(27, T15_T40.T15.displayName);
        t15_t40EndMarket.put(28, T15_T40.T15.displayName);
        t15_t40EndMarket.put(29, T15_T40.T15.displayName);
        t15_t40EndMarket.put(30, T15_T40.T15.displayName);
        t15_t40EndMarket.put(31, T15_T40.T15.displayName);
        t15_t40EndMarket.put(32, T15_T40.T15.displayName);
        t15_t40EndMarket.put(33, T15_T40.T15.displayName);
        t15_t40EndMarket.put(34, T15_T40.T15.displayName);
        t15_t40EndMarket.put(35, T15_T40.T15.displayName);
        t15_t40EndMarket.put(36, T15_T40.T15.displayName);
        t15_t40EndMarket.put(37, T15_T40.T15.displayName);
        t15_t40EndMarket.put(38, T15_T40.T15.displayName);
        t15_t40EndMarket.put(39, T15_T40.T15.displayName);
        t15_t40EndMarket.put(40, T15_T40.T15.displayName);
        t15_t40EndMarket.put(41, T15_T40.T15.displayName);
        t15_t40EndMarket.put(42, T15_T40.T15.displayName);
        t15_t40EndMarket.put(43, T15_T40.T15.displayName);
        t15_t40EndMarket.put(44, T15_T40.T15.displayName);
        t15_t40EndMarket.put(45, T15_T40.T15.displayName);
        t15_t40EndMarket.put(46, T15_T40.T15.displayName);
        t15_t40EndMarket.put(47, T15_T40.T15.displayName);
        t15_t40EndMarket.put(48, T15_T40.T15.displayName);
        t15_t40EndMarket.put(49, T15_T40.T15.displayName);
        t15_t40EndMarket.put(50, T15_T40.T15.displayName);
        t15_t40EndMarket.put(51, T15_T40.T15.displayName);
        t15_t40EndMarket.put(52, T15_T40.T15.displayName);
        t15_t40EndMarket.put(53, T15_T40.T15.displayName);
        t15_t40EndMarket.put(54, T15_T40.T15.displayName);
        t15_t40EndMarket.put(55, T15_T40.T15.displayName);
        t15_t40EndMarket.put(56, T15_T40.T15.displayName);
        t15_t40EndMarket.put(57, T15_T40.T15.displayName);
        t15_t40EndMarket.put(58, T15_T40.T15.displayName);
        t15_t40EndMarket.put(59, T15_T40.T15.displayName);
        t15_t40EndMarket.put(60, T15_T40.T15.displayName);
        t15_t40EndMarket.put(61, T15_T40.T15.displayName);
        t15_t40EndMarket.put(62, T15_T40.T15.displayName);
        t15_t40EndMarket.put(63, T15_T40.T15.displayName);
        t15_t40EndMarket.put(64, T15_T40.T15.displayName);
        t15_t40EndMarket.put(65, T15_T40.T15.displayName);
        t15_t40EndMarket.put(66, T15_T40.T15.displayName);
        t15_t40EndMarket.put(67, T15_T40.T15.displayName);
        t15_t40EndMarket.put(68, T15_T40.T15.displayName);
        t15_t40EndMarket.put(69, T15_T40.T15.displayName);
        t15_t40EndMarket.put(70, T15_T40.T15.displayName);
        t15_t40EndMarket.put(71, T15_T40.T15.displayName);
        t15_t40EndMarket.put(72, T15_T40.T15.displayName);
        t15_t40EndMarket.put(73, T15_T40.T15.displayName);
        t15_t40EndMarket.put(74, T15_T40.T15.displayName);
        t15_t40EndMarket.put(75, T15_T40.T15.displayName);
        t15_t40EndMarket.put(76, T15_T40.T15.displayName);
        t15_t40EndMarket.put(77, T15_T40.T15.displayName);
        t15_t40EndMarket.put(78, T15_T40.T15.displayName);
        t15_t40EndMarket.put(79, T15_T40.T15.displayName);
        t15_t40EndMarket.put(80, T15_T40.T15.displayName);
        t15_t40EndMarket.put(81, T15_T40.T15.displayName);
        t15_t40EndMarket.put(82, T15_T40.T15.displayName);
        t15_t40EndMarket.put(83, T15_T40.T15.displayName);
        t15_t40EndMarket.put(84, T15_T40.T15.displayName);
        t15_t40EndMarket.put(85, T15_T40.T15.displayName);
        t15_t40EndMarket.put(86, T15_T40.T15.displayName);
        t15_t40EndMarket.put(87, T15_T40.T15.displayName);
        t15_t40EndMarket.put(88, T15_T40.T15.displayName);
        t15_t40EndMarket.put(89, T15_T40.T15.displayName);
        t15_t40EndMarket.put(90, T15_T40.T15.displayName);
        t15_t40EndMarket.put(91, T15_T40.T15.displayName);
        t15_t40EndMarket.put(92, T15_T40.T15.displayName);
        t15_t40EndMarket.put(93, T15_T40.T15.displayName);
        t15_t40EndMarket.put(94, T15_T40.T15.displayName);
        t15_t40EndMarket.put(95, T15_T40.T15.displayName);
        t15_t40EndMarket.put(96, T15_T40.T15.displayName);
        t15_t40EndMarket.put(97, T15_T40.T15.displayName);
        t15_t40EndMarket.put(98, T15_T40.T15.displayName);
        t15_t40EndMarket.put(99, T15_T40.T15.displayName);
        t15_t40EndMarket.put(100, T15_T40.T15.displayName);
        t15_t40EndMarket.put(101, T15_T40.T40.displayName);
        t15_t40EndMarket.put(102, T15_T40.T40.displayName);
        t15_t40EndMarket.put(103, T15_T40.T40.displayName);
        t15_t40EndMarket.put(104, T15_T40.T40.displayName);
        t15_t40EndMarket.put(105, T15_T40.T40.displayName);
        t15_t40EndMarket.put(106, T15_T40.T40.displayName);
        t15_t40EndMarket.put(107, T15_T40.T40.displayName);
        t15_t40EndMarket.put(108, T15_T40.T40.displayName);
        t15_t40EndMarket.put(109, T15_T40.T40.displayName);
        t15_t40EndMarket.put(110, T15_T40.T40.displayName);
        t15_t40EndMarket.put(111, T15_T40.T40.displayName);
        t15_t40EndMarket.put(112, T15_T40.T40.displayName);
        t15_t40EndMarket.put(113, T15_T40.T40.displayName);
        t15_t40EndMarket.put(114, T15_T40.T40.displayName);
        t15_t40EndMarket.put(115, T15_T40.T40.displayName);
        t15_t40EndMarket.put(116, T15_T40.T40.displayName);
        t15_t40EndMarket.put(117, T15_T40.T40.displayName);
        t15_t40EndMarket.put(118, T15_T40.T40.displayName);
        t15_t40EndMarket.put(119, T15_T40.T40.displayName);
        t15_t40EndMarket.put(120, T15_T40.T40.displayName);
        t15_t40EndMarket.put(121, T15_T40.T40.displayName);
        t15_t40EndMarket.put(122, T15_T40.T40.displayName);
        t15_t40EndMarket.put(123, T15_T40.T40.displayName);
        t15_t40EndMarket.put(124, T15_T40.T40.displayName);
        t15_t40EndMarket.put(125, T15_T40.T40.displayName);
        t15_t40EndMarket.put(126, T15_T40.T40.displayName);
        t15_t40EndMarket.put(127, T15_T40.T40.displayName);
        t15_t40EndMarket.put(128, T15_T40.T40.displayName);
        t15_t40EndMarket.put(129, T15_T40.T40.displayName);
        t15_t40EndMarket.put(130, T15_T40.T40.displayName);
        t15_t40EndMarket.put(131, T15_T40.T40.displayName);
        t15_t40EndMarket.put(132, T15_T40.T40.displayName);
        t15_t40EndMarket.put(133, T15_T40.T40.displayName);
        t15_t40EndMarket.put(134, T15_T40.T40.displayName);
        t15_t40EndMarket.put(135, T15_T40.T40.displayName);
        t15_t40EndMarket.put(136, T15_T40.T40.displayName);
        t15_t40EndMarket.put(137, T15_T40.T40.displayName);
        t15_t40EndMarket.put(138, T15_T40.T40.displayName);
        t15_t40EndMarket.put(139, T15_T40.T40.displayName);
        t15_t40EndMarket.put(140, T15_T40.T40.displayName);
        t15_t40EndMarket.put(141, T15_T40.T40.displayName);
        t15_t40EndMarket.put(142, T15_T40.T40.displayName);
        t15_t40EndMarket.put(143, T15_T40.T40.displayName);
        t15_t40EndMarket.put(144, T15_T40.T40.displayName);
        t15_t40EndMarket.put(145, T15_T40.T40.displayName);
        t15_t40EndMarket.put(146, T15_T40.T40.displayName);
        t15_t40EndMarket.put(147, T15_T40.T40.displayName);
        t15_t40EndMarket.put(148, T15_T40.T40.displayName);
        t15_t40EndMarket.put(149, T15_T40.T40.displayName);
        t15_t40EndMarket.put(150, T15_T40.T40.displayName);
        t15_t40EndMarket.put(151, T15_T40.T40.displayName);
        t15_t40EndMarket.put(152, T15_T40.T40.displayName);
        t15_t40EndMarket.put(153, T15_T40.T40.displayName);
        t15_t40EndMarket.put(154, T15_T40.T40.displayName);
        t15_t40EndMarket.put(155, T15_T40.T40.displayName);
        t15_t40EndMarket.put(156, T15_T40.T40.displayName);
        t15_t40EndMarket.put(157, T15_T40.T40.displayName);
        t15_t40EndMarket.put(158, T15_T40.T40.displayName);
        t15_t40EndMarket.put(159, T15_T40.T40.displayName);
        t15_t40EndMarket.put(160, T15_T40.T40.displayName);
        t15_t40EndMarket.put(161, T15_T40.T40.displayName);
        t15_t40EndMarket.put(162, T15_T40.T40.displayName);
        t15_t40EndMarket.put(163, T15_T40.T40.displayName);
        t15_t40EndMarket.put(164, T15_T40.T40.displayName);
        t15_t40EndMarket.put(165, T15_T40.T40.displayName);
        t15_t40EndMarket.put(166, T15_T40.T40.displayName);
        t15_t40EndMarket.put(167, T15_T40.T40.displayName);
        t15_t40EndMarket.put(168, T15_T40.T40.displayName);
        t15_t40EndMarket.put(169, T15_T40.T40.displayName);
        t15_t40EndMarket.put(170, T15_T40.T40.displayName);
        t15_t40EndMarket.put(171, T15_T40.T40.displayName);
        t15_t40EndMarket.put(172, T15_T40.T40.displayName);
        t15_t40EndMarket.put(173, T15_T40.T40.displayName);
        t15_t40EndMarket.put(174, T15_T40.T40.displayName);
        t15_t40EndMarket.put(175, T15_T40.T40.displayName);
        t15_t40EndMarket.put(176, T15_T40.T40.displayName);
        t15_t40EndMarket.put(177, T15_T40.T40.displayName);
        t15_t40EndMarket.put(178, T15_T40.T40.displayName);
        t15_t40EndMarket.put(179, T15_T40.T40.displayName);
        t15_t40EndMarket.put(180, T15_T40.T40.displayName);
        t15_t40EndMarket.put(181, T15_T40.T40.displayName);
        t15_t40EndMarket.put(182, T15_T40.T40.displayName);
        t15_t40EndMarket.put(183, T15_T40.T40.displayName);
        t15_t40EndMarket.put(184, T15_T40.T40.displayName);
        t15_t40EndMarket.put(185, T15_T40.T40.displayName);
        t15_t40EndMarket.put(186, T15_T40.T40.displayName);
        t15_t40EndMarket.put(187, T15_T40.T40.displayName);
        t15_t40EndMarket.put(188, T15_T40.T40.displayName);
        t15_t40EndMarket.put(189, T15_T40.T40.displayName);
        t15_t40EndMarket.put(190, T15_T40.T40.displayName);
        t15_t40EndMarket.put(191, T15_T40.T40.displayName);
        t15_t40EndMarket.put(192, T15_T40.T40.displayName);
        t15_t40EndMarket.put(193, T15_T40.T40.displayName);
        t15_t40EndMarket.put(194, T15_T40.T40.displayName);
        t15_t40EndMarket.put(195, T15_T40.T40.displayName);
        t15_t40EndMarket.put(196, T15_T40.T40.displayName);
        t15_t40EndMarket.put(197, T15_T40.T40.displayName);
        t15_t40EndMarket.put(198, T15_T40.T40.displayName);
        t15_t40EndMarket.put(199, T15_T40.T40.displayName);
        t15_t40EndMarket.put(200, T15_T40.T40.displayName);
        t15_t40EndMarket.put(201, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(202, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(203, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(204, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(205, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(206, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(207, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(208, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(209, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(210, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(211, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(212, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(213, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(214, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(215, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(216, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(217, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(218, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(219, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(220, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(221, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(222, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(223, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(224, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(225, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(226, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(227, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(228, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(229, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(230, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(231, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(232, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(233, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(234, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(235, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(236, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(237, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(238, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(239, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(240, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(241, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(242, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(243, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(244, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(245, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(246, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(247, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(248, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(249, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(250, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(251, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(252, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(253, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(254, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(255, T15_T40.NONE.displayName);
        t15_t40EndMarket.put(256, T15_T40.NONE.displayName);
        return t15_t40EndMarket;
    }

    // The Mapping list for Tendering Agency, Supplier and FieldWork Supplier is same as per the mapping excel given by client.
    // Created separate Maps for them as there are chances that these can be separate in future.

    public static Map<Integer, String> getTenderingAgency(){

        if(tenderingAgency==null || tenderingAgency.size()<1)
        {
            tenderingAgency = SynchroUtils.getTAgencyFields();
            return tenderingAgency;
        }
        else
        {
            return tenderingAgency;
        }
    }

    // The Mapping list for Supplier Group and Field Work Supplier Group  is same as per the mapping excel given by client.
    // Created separate Maps for them as there are chances that these can be separate in future.

    public static Map<Integer, String> getSupplierGroup(){

        if(supplierGroup==null || supplierGroup.size()<1)
        {
            supplierGroup = SynchroUtils.getSupplierGroupFields();
            return supplierGroup;
        }
        else
        {
            return supplierGroup;
        }

    }
    public static Map<Integer, String> getSupplierGroupSupplierMapping(){

        if(supplierGroupMapping==null || supplierGroupMapping.size()<1)
        {
            for(Integer gid :getSupplierGroup().keySet())
            {
                List<Long> suppliers = SynchroUtils.getSupplierFields(gid);
                supplierGroupMapping.put(gid, StringUtils.join(suppliers, ','));
            }
            return supplierGroupMapping;
        }
        else
        {
            return supplierGroupMapping;
        }
    }

    public static Map<Integer, String> getFieldWorkSupplierGroupSupplierMapping() {

        if(fwSupplierGroupMapping==null || fwSupplierGroupMapping.size()<1)
        {
            for(Integer gid :getFieldWorkSupplierGroup().keySet())
            {
                List<Long> fwSuppliers = SynchroUtils.getFwSupplierFields(gid);
                fwSupplierGroupMapping.put(gid, StringUtils.join(fwSuppliers, ','));
            }
            return fwSupplierGroupMapping;
        }
        else
        {
            return fwSupplierGroupMapping;
        }

    }
    public static Map<Integer, String> getFieldWorkSupplierGroup(){

        if(fieldWorkSupplierGroup==null || fieldWorkSupplierGroup.size()<1)
        {
            fieldWorkSupplierGroup = SynchroUtils.getFwSupplierGroupFields();
            return fieldWorkSupplierGroup;
        }
        else
        {
            return fieldWorkSupplierGroup;
        }
    }

    // The Mapping list for Tendering Agency, Supplier and FieldWork Supplier is same as per the mapping excel given by client.
    // Created separate Maps for them as there are chances that these can be separate in future.

    public static Map<Integer, String> getSuppliers() {

        if(suppliers==null || suppliers.size()<1)
        {
            suppliers = SynchroUtils.getSupplierFields();
            return suppliers;
        }
        else
        {
            return suppliers;
        }
    }



    // The Mapping list for Tendering Agency, Supplier and FieldWork Supplier is same as per the mapping excel given by client.
    // Created separate Maps for them as there are chances that these can be separate in future.

    public static Map<Integer, String> getFieldWorkSuppliers() {
        if(fieldWorkSuppliers==null || fieldWorkSuppliers.size()<1)
        {
            fieldWorkSuppliers = SynchroUtils.getFwSupplierFields();
            return fieldWorkSuppliers;
        }
        else
        {
            return fieldWorkSuppliers;
        }

    }

    public static Map<Integer, String> getDataCollections(){

        if(dataCollection==null || dataCollection.size()<1)
        {
            dataCollection = SynchroUtils.getDataCollectionFields();
            return dataCollection;
        }
        else
        {
            return dataCollection;
        }

    }


    public static enum ProjectWaiverStatus {
        DRAFT(1L, "Draft"),
        PENDING_APPROVAL(2L, "Pending Approval"),
        APPROVED(3L, "Approved"),
        REJECTED(4L, "Rejected");
        private Long value;
        private String name;

        private ProjectWaiverStatus(final Long value, final String name) {
            this.value = value;
            this.name = name;
        }

        public Long value() {
            return this.value;
        }

        public static String getName(final Long id) {
            for(ProjectWaiverStatus status: values()) {
                if(status.value().equals(id)) {
                    return status.name;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.name;
        }
    };

    public static Map<Integer, String> getWaiverStatusList()
    {
        waiverStatusList.put(ProjectWaiverStatus.DRAFT.value.intValue(), ProjectWaiverStatus.DRAFT.toString());
        waiverStatusList.put(ProjectWaiverStatus.PENDING_APPROVAL.value.intValue(), ProjectWaiverStatus.PENDING_APPROVAL.toString());
        waiverStatusList.put(ProjectWaiverStatus.APPROVED.value.intValue(), ProjectWaiverStatus.APPROVED.toString());
        waiverStatusList.put(ProjectWaiverStatus.REJECTED.value.intValue(), ProjectWaiverStatus.REJECTED.toString());
        return waiverStatusList;

    }

    public static Map<String, String> getPendingActivityList() {
        pendingActivityList = new HashMap<String, String>();
        for(PendingActivityStatus status: PendingActivityStatus.values()) {
            pendingActivityList.put(status.getId().toString(),status.toString());
        }
        return pendingActivityList;
    }

    /**
     *
     */
    public enum SynchroAttachmentStage {
        PIB("PIB"),
        PROPOSAL("Proposal"),
        PROJECT_SPECS("Project Specs"),
        REPORT_SUMMARY("Report Summary"),
        PROJECT_EVALUATION("Project Evaluation"),
        PROJECT_WAIVER("Project Waiver");
        private String value;

        private SynchroAttachmentStage(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    /**
     *
     */
    public enum SynchroAttachmentObject {
        BUSINESS_QUESTION(1, "Business Question"),
        RESEARCH_OBJECTIVE(2, "Research Objective"),
        ACTION_STANDARD(3, "Action Standard"),
        RESEARCH_DESIGN(4, "Research Design"),
        SAMPLE_PROFILE(5, "Sample Profile"),
        STIMULUS_MATERIAL(6, "Stimulus Material"),
        OTHER_REPORTING_REQUIREMENT(7, "Other Reporting Requirement"),
        OTHERS(8, "Others"),
        // Proposal Fields
        STIMULUS_MATERIAL_SHIPPED(9, "Stimulus Material Shipped to"),
        PROP_COST_TEMPLATE(10, "Proposal and Cost templates"),
        // Project Specs Fields
        SCREENER(11, "Screener"),
        CCC_AGREEMENT(12, "Consumer contract and Confidentiality agreement"),
        QUESTIONNAIRE(13, "Questionnaires"),
        DISCUSSION_GUIDE(14, "Discussion guide"),
        // Report Summary fields
        COMMENTS(15, "Comments"),
        WAIVER_ATTACHMENT(16, "Waiver Attachment"),
        FULL_REPORT(17, "Full Report"),
        SUMMARY_REPORT(18, "Summary Report"),
        SUMMARY_FOR_IRIS(19, "Summary for IRIS"),
        //Waiver fields
        PIB_BRIEF(20, "Brief"),
        PIB_BRIEF_LEGAL_APPROVAL(21, "BriefLegalApproval"),
        AGENCY_WAIVER(22, "Agency Waiver"),
        PROPOSAL(23, "Proposal"),
        PROPOSAL_LEGAL_APPROVAL(24, "ProposalLegalApproval"),
        DOCUMENTATION(25, "Documentation"),
	TOP_LINE_REPORT(26, "Top Line Presentation Report"),
        EXECUTIVE_PRESENTATION_REPORT(27, "Executive Presentation Report"),
        IRIS_SUMMARY_REPORT(28, "IRIS Summary Report"),
        TPD_SUMMARY_REPORT(29, "TPD Summary Report"),
        TPD_SUMMARY_LEGAL_APPROVAL(30, "TPD Summary Legal Approval"),
        TPD_SUMMARY_SKU_DETAILS(31, "TPD Summary SKU DETAILS");
        
        private Integer id;
        private String value;

        private SynchroAttachmentObject(final Integer id, final String value) {
            this.id = id;
            this.value = value;
        }

        public Integer getId() {
            return id;
        }

        public static String getById(final Integer id) {
            for(SynchroAttachmentObject value:values()) {
                if(value.getId().equals(id)) {
                    return value.toString();
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static boolean isSynchroAttachmentObjectType(final int objecType) {
        SynchroAttachmentStage[] stages = SynchroAttachmentStage.values();
        SynchroAttachmentObject[] objects = SynchroAttachmentObject.values();
        for(SynchroAttachmentStage stage: stages) {
            for(SynchroAttachmentObject object: objects) {
                int value = buildSynchroAttachmentObjectID(stage.toString(),object.toString());
                if(objecType == value) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Integer buildSynchroAttachmentObjectID(final String prefix, final String objectType , final String suffix) {
        StringBuilder idBuilder = new StringBuilder();
        if(StringUtils.isNotBlank(prefix)) {
            idBuilder.append(prefix).append("-");
        }
        idBuilder.append(objectType);
        if(StringUtils.isNotBlank(suffix)) {
            idBuilder.append("-").append(suffix);
        }
        return idBuilder.toString().hashCode();
    }

    public static Integer buildSynchroAttachmentObjectID(final String prefix, final String objectType) {
        StringBuilder idBuilder = new StringBuilder();
        if(StringUtils.isNotBlank(prefix)) {
            idBuilder.append(prefix).append("-");
        }
        idBuilder.append(objectType);
        return idBuilder.toString().hashCode();
    }

    public static boolean isOSPAttachmentType(final Integer objectType) {
        
        if(OSPAttachmentObjectType.OSP_DOCUMENT_OBJECT_TYPE_ID == objectType.intValue()) {
            return true;
        }
        return false;
    }
    public static String getStageFieldObject(final String stage, final String attachmentObjectType) {
        StringBuilder str = new StringBuilder();
        str.append(stage).append("-").append(attachmentObjectType);
        return str.toString();
    }

    public enum AutoSavePage {
        PIT("PIT"),
        PIB("PIB"),
        PROPOSAL("Proposal"),
        PROJECT_SPECS("Project Specs"),
        REPORT_SUMMARY("Report Summary"),
        PROJECT_EVALUATION("Project Evaluation"),
        PROJECT_WAIVER("Project Waiver");

        private String value;

        private AutoSavePage(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    public static Map<Integer, String> getDepartments() {
        if(departments == null || departments.size() <= 0) {
            UserDepartmentsService service = JiveApplication.getContext().getSpringBean("userDepartmentsService");
            List<UserDepartment> userDepartments = service.getAll();
            for(UserDepartment userDepartment:userDepartments) {
                departments.put(userDepartment.getId().intValue(), userDepartment.getName());
            }
        }

        return departments;

    }

    private static Map<Integer, String> sortByValue(Map<Integer, String> unsortMap, final boolean order) {

        List<Map.Entry<Integer, String>> list = new LinkedList<Map.Entry<Integer, String>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<Integer, String>>()
        {
            public int compare(Map.Entry<Integer, String> o1,
                               Map.Entry<Integer, String> o2)
            {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());
                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<Integer, String> sortedMap = new LinkedHashMap<Integer, String>();
        for (Map.Entry<Integer, String> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

//    public static String getDepartmentNameById(final Integer id) {
//        if(departments != null && departments.size() > 0
//                && departments.containsKey(id)) {
//            return departments.get(id);
//        }
//        return "Not Defined";
//    }

    public static String getDepartmentNameById(final String id) {
        if(id != null && !id.equals("")) {
            if(id.matches("^\\d+$")) {
                getDepartments();
                if(departments != null && departments.size() > 0
                        && departments.containsKey(Integer.parseInt(id))) {
                    return departments.get(Integer.parseInt(id));
                }
            } else {
                return "NOT DEFINED";
            }
        }
        return "NOT DEFINED";
    }

    public static void updateUserDepartmentMap(final UserDepartment userDepartment, String action) {
        if(userDepartment != null) {
            getDepartments();
            if(action.equals("update")) {
                departments.put(userDepartment.getId().intValue(), userDepartment.getName());
            } else if(action.equals("remove")) {
                departments.remove(userDepartment.getId().intValue());
            }
        }
    }



    public static void updateUserDepartmentMap(final Long departmentId, String action) {
        if(departmentId != null && departmentId > 0) {
            getDepartments();
            if(action.equals("update")) {
                UserDepartmentsService service = JiveApplication.getContext().getSpringBean("userDepartmentsService");
                UserDepartment userDepartment = service.get(departmentId);
                departments.put(userDepartment.getId().intValue(), userDepartment.getName());
            } else if(action.equals("remove")) {
                departments.remove(departmentId.intValue());
            }
        }
    }


    public static Map<Integer, String> getRegions()
    {
        if(regions==null || regions.size()<1)
        {
            regions = SynchroUtils.getRegionFields();
            return regions;
        }
        else
        {
            return regions;
        }
    }

    public static Map<Integer, String> getAllRegions()
    {
        if(allregions==null || allregions.size()<1)
        {
        	allregions = SynchroUtils.getAllRegionFields();
            return allregions;
        }
        else
        {
            return allregions;
        }
    }
    
    public static Map<Integer, String> getRegions(final User user)
    {
        Map<Integer, String> regions = getRegions();
        boolean  globalSuperUser = SynchroPermHelper.isSynchroAdmin(user)
                || SynchroPermHelper.isSynchroMiniAdmin(user) || SynchroPermHelper.isSynchroGlobalSuperUser(user);
        if(globalSuperUser) {
            return regions;
        } else  {
            Map<Integer, String> regionAccessList = new HashMap<Integer, String>();
            Set<Integer> regionsIds = regions.keySet();
            Map<String, String> userProperties = user.getProperties();
            if(userProperties != null && !userProperties.isEmpty()) {
                if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER)
                        && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER).equals("")) {
                    String regionalAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST);
                    if(regionalAccessListStr != null && !regionalAccessListStr.equals("")) {
                        String [] regionalAccessListArr = regionalAccessListStr.split(",");
                        for(String regionIdStr : regionalAccessListArr) {
                            Integer regionId = Integer.parseInt(regionIdStr);
                            if(regionsIds.contains(regionId)) {
                                regionAccessList.put(regionId, regions.get(regionId));
                            }
                        }
                    }
                }
            }

            return regionAccessList;
        }
    }

    public static Map<Integer, String> getEndMarketsByRegion(final Long region){
        if(getRegionEndMarketsMapping().containsKey(region.intValue()))
        {
            return getRegionEndMarketsMapping().get(region.intValue());
        }
        else
        {
            return new LinkedHashMap<Integer, String>();
        }
    }

    public static  Map<Integer, Map<Integer, String>> getRegionEndMarketsMapping()
    {
        if(regionEndMarketsMapping==null || regionEndMarketsMapping.size()<1)
        {
            for(Integer region : getRegions().keySet())
            {
                List<MetaField> endmarkets = SynchroUtils.getEndMarketsByRegion(region);
                Map<Integer, String> endMarketMap = new LinkedHashMap<Integer, String>();
                for(MetaField field : endmarkets)
                {
                    endMarketMap.put(field.getId().intValue(), field.getName());
                }
                if(!regionEndMarketsMapping.containsKey(region))
                    regionEndMarketsMapping.put(region, endMarketMap);
            }
            return regionEndMarketsMapping;
        }
        else
        {
            return regionEndMarketsMapping;
        }
    }

    
    public static Map<Integer, Integer> getEndmarketApprovalTypeMap()
    {
        if(endmarketApprovalTypeMap==null || endmarketApprovalTypeMap.size()<1)
        {
            for(Integer eid : getEndMarkets().keySet())
            {
            	Integer approvalType = SynchroUtils.getApprovalByEndmarket(eid);
            	
                if(!endmarketApprovalTypeMap.containsKey(eid))
                	endmarketApprovalTypeMap.put(eid, approvalType);
            }
            return endmarketApprovalTypeMap;
        }
        else
        {
            return endmarketApprovalTypeMap;
        }
    }
    
    public static Map<Integer, Integer> getEndmarketMarketTypeMap()
    {
        if(endmarketMarketTypeMap==null || endmarketMarketTypeMap.size()<1)
        {
            for(Integer eid : getEndMarkets().keySet())
            {
            	Integer endMarketType = SynchroUtils.getMarketByEndmarket(eid);
            	
                if(!endmarketMarketTypeMap.containsKey(eid))
                	endmarketMarketTypeMap.put(eid, endMarketType);
            }
            return endmarketMarketTypeMap;
        }
        else
        {
            return endmarketMarketTypeMap;
        }
    }


    public static Map<Integer, Integer> getEndmarketT20_T40_TypeMap()
    {
        if(endmarketT20_T40_TypeMap==null || endmarketT20_T40_TypeMap.size()<1)
        {
            for(Integer eid : getEndMarkets().keySet())
            {
            	Integer approvalType = SynchroUtils.getT20_T40_ByEndmarket(eid);
            	
                if(!endmarketT20_T40_TypeMap.containsKey(eid))
                	endmarketT20_T40_TypeMap.put(eid, approvalType);
            }
            return endmarketT20_T40_TypeMap;
        }
        else
        {
            return endmarketT20_T40_TypeMap;
        }
    }
    
    public static Map<Integer, String> getAreas()
    {
        if(areas==null || areas.size()<1)
        {
            areas = SynchroUtils.getAreaFields();
            return areas;
        }
        else
        {
            return areas;
        }
    }

    public static Map<Integer, String> getEndMarketsByArea(final Long area){
        if(getAreaEndMarketsMapping().containsKey(area.intValue()))
        {
            return getAreaEndMarketsMapping().get(area.intValue());
        }
        else
        {
            return new LinkedHashMap<Integer, String>();
        }
    }

    public static  Map<Integer, Map<Integer, String>> getAreaEndMarketsMapping()
    {
        if(areaEndMarketsMapping==null || areaEndMarketsMapping.size()<1)
        {
            for(Integer area : getAreas().keySet())
            {
                List<MetaField> endmarkets = SynchroUtils.getEndMarketsByArea(area);
                Map<Integer, String> endMarketMap = new LinkedHashMap<Integer, String>();
                for(MetaField field : endmarkets)
                {
                    endMarketMap.put(field.getId().intValue(), field.getName());
                }
                if(!areaEndMarketsMapping.containsKey(area))
                    areaEndMarketsMapping.put(area, endMarketMap);
            }
            return areaEndMarketsMapping;
        }
        else
        {
            return areaEndMarketsMapping;
        }
    }


    public static  Map<String, String> getCountryCurrencyMap()
    {
        if(countryCurrencyMap==null || countryCurrencyMap.size()<1)
        {
            List<MetaFieldMapping> mappingList = SynchroUtils.getCountryCurrencyMapping();

            for(MetaFieldMapping mapping : mappingList)
            {
                countryCurrencyMap.put(mapping.getEid().intValue()+"", mapping.getId().intValue()+"");
            }
            return countryCurrencyMap;
        }
        else
        {
            return countryCurrencyMap;
        }
    }

    public static Map<Integer, String> getUserRoles()
    {
        Map<Integer, String> userRoles = new HashMap<Integer, String>();
        userRoles.put(UserRole.SPI.getId(), UserRole.SPI.getDescription());
        userRoles.put(UserRole.BAT.getId(), UserRole.BAT.getDescription());
        userRoles.put(UserRole.MARKETING.getId(), UserRole.MARKETING.getDescription());
        userRoles.put(UserRole.LEGAL.getId(), UserRole.LEGAL.getDescription());
        userRoles.put(UserRole.PROCUREMENT.getId(), UserRole.PROCUREMENT.getDescription());
        userRoles.put(UserRole.EXTERNALAGENCY.getId(), UserRole.EXTERNALAGENCY.getDescription());
        userRoles.put(UserRole.COMMUNICATIONAGENCY.getId(), UserRole.COMMUNICATIONAGENCY.getDescription());
        userRoles.put(UserRole.SUPPORT.getId(), UserRole.SUPPORT.getDescription());
        return userRoles;
    }

    public static Map<Integer, String> getProjectOwnerRoles()
    {
        Map<Integer, String> userRoles = new HashMap<Integer, String>();
        userRoles.put(UserRole.SPI.getId(), UserRole.SPI.getDescription());
        userRoles.put(UserRole.BAT.getId(), UserRole.BAT.getDescription());
        userRoles.put(UserRole.MARKETING.getId(), UserRole.MARKETING.getDescription());
        userRoles.put(UserRole.SUPPORT.getId(), UserRole.SUPPORT.getDescription());
        return userRoles;
    }

    public static enum UserRole {
        SPI(1, "SPI"),
        BAT(2, "BAT"),
        MARKETING(3, "Marketing"),
        LEGAL(4, "Legal"),
        PROCUREMENT(5, "Procurement"),
        EXTERNALAGENCY(6, "External Agency"),
        COMMUNICATIONAGENCY(7, "Communication Agency"),
        SUPPORT(8, "Support"),
        SYNCHRO(9, "Synchro");

        int id;
        String description;

        UserRole(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static UserRole getById(int id) {
            for (UserRole userRole : values()) {
                if (userRole.id == id) {
                    return userRole;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Stage Field");
        }
    };

    public static Map<Integer, String> getJobTitles()
    {
        /*   Map<Integer, String> jobTitles = new HashMap<Integer, String>();
        jobTitles.put(JobTitle.ENGINEER.getId(),JobTitle.ENGINEER.getDescription());
        jobTitles.put(JobTitle.DOCTOR.getId(), JobTitle.DOCTOR.getDescription());
        return jobTitles;
        */

        if(jobTitles==null || jobTitles.size()<1)
        {
            jobTitles = SynchroUtils.getJobTitles();
            return jobTitles;
        }
        else
        {
            return jobTitles;
        }

    }


    public static enum JobTitle {

        ENGINEER(1, "Er."),
        DOCTOR(2, "Dr.");

        int id;
        String description;

        JobTitle(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static JobTitle getById(int id) {
            for (JobTitle jobTitle : values()) {
                if (jobTitle.id == id) {
                    return jobTitle;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Stage Field");
        }
    };

    /*
     * 
     */
    public static enum InvestmentType {
        GlOBAL(1, "Global"),
        REGION(2, "Regional"),
        AREA(3, "Area"),
        COUNTRY(4, "Country");
        int id;
        String description;

        InvestmentType(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static InvestmentType getById(int id) {
            for (InvestmentType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Type of Investment");
        }
    };

    public static enum WaiverPreApproval {
        LOCALLY_APPROVED(1, "Locally Approved"),
        REGIONAL_APPROVED(2, "Regional Approved"),
        GLOBALLY_APPROVED(3, "Globally Approved");

        int id;
        String description;

        WaiverPreApproval(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static WaiverPreApproval getById(int id) {
            for (WaiverPreApproval type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Type of WaiverPreApproval");
        }
    };

    public static enum PendingActivityStatus {
        METHODOLOGY_WAIVER_APPROVAL_PENDING(1, "Methodology Waiver - Approval pending"),
        METHODOLOGY_WAIVER_MORE_INFORMATION_REQUESTED(2, "Methodology Waiver - More Information requested"),
        PIB_LEGAL_APPROVAL_PENDING(3, "PIB - Legal Approval pending"),
        PIB_COMPLETE_NOTIFY_AGENCY_PENDING(4, "PIB Complete - Notify Agency - Pending"),
        PIT_APPROVAL_ON_COST_PENDING(5, "Approval on cost - Pending"),
        PROPOSAL_CLARIFICATION_REQUESTED(6, "Proposal - Clarification requested"),
        PROPOSAL_AWARD_AGENCY_PENDING(7, "Proposal - Award Agency Pending"),
        PROJECT_SPECS_CLARIFICATION_REQUESTED(8, "Project specs - Clarification requested"),
        PROJECT_SPECS_LEGAL_APPROVAL_PENDING(9, "Project specs - Legal Approval pending"),
        PROJECT_SPECS_APPROVAL_PENDING(10, "Project specs - Approval pending"),
        REPORT_SUMMARY_CLARIFICATION_REQUESTED(11, "Report - Clarification requested"),
        REPORT_SUMMARY_LEGAL_APPROVAL_PENDING(12, "Report - Legal Approval pending"),
        REPORT_SUMMARY_APPROVAL_PENDING(13, "Report - Approval pending"),
        REPORT_SUMMARY_UPLOADED_TO_IRIS_PENDING(14, "Report - Summary Uploaded to IRIS - Pending"),
        PROJECT_EVALUATION_PENDING(15, "Project Evaluation - Pending");

        Integer id;
        String status;

        private PendingActivityStatus(final Integer id, final String status) {
            this.status = status;
            this.id = id;
        }

        public Integer getId() {
            return this.id;
        }

        public static String getById(final Integer id) {
            for(PendingActivityStatus s: PendingActivityStatus.values()) {
                if(s.getId() == id) {
                    return s.toString();
                }
            }
            return "";
        }

        @Override
        public String toString() {
            return this.status;
        }
    }

    public static enum PDFDocumentName {
        PIB(1, "PIBPDF"),
        PROPOSAL(2, "ProposalPDF"),
        PROJECT_SPECS(3, "ProjectSpecsPDF"),
        REPORT_SUMMARY(4, "ReportSummaryPDF");
        Integer id;
        String name;

        private PDFDocumentName(final Integer id, final String name) {
            this.name = name;
            this.id = id;
        }

        public Integer getId() {
            return this.id;
        }

        public static String getById(final Integer id) {
            for(PDFDocumentName s: PDFDocumentName.values()) {
                if(s.getId().intValue() == id.intValue()) {
                    return s.toString();
                }
            }
            return "";
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static enum DefaultTemplateType {
        RETAIL_AUDIT_TEMPLATE(1,"Retail audit template"),
        PRODUCT_TEMPLATE(2,"Product template"),
        COST_TEMPLATE(3,"Cost template"),
        STIMULI_TEMPLATE(4,"Stimuli Template");

        Integer id;
        String name;
        private DefaultTemplateType(final Integer id, final String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static String getById(final Integer id) {
            for(DefaultTemplateType d: DefaultTemplateType.values()) {
                if(d.getId().intValue() == id.intValue()) {
                    return d.toString();
                }
            }
            return "";
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static enum SpendReportType {
        TOTAL_SPEND(1, "Total Spend", "Total Spend"),
        SPEND_BY_METHODOLOGY(2, "Spend by Methodology", "Methodology"),
        SPEND_BY_BRANDED_NON_BRANDED(3,"Spend by Branded / Non-Branded","Branded / Non-Branded"),
        SPEND_BY_SUPPLIER_GROUP(4,"Spend by Supplier Group","Supplier Group"),
        SPEND_BY_PROJECTS(5,"Spend by Projects","All Projects (By EM within Global, By EM within Region)"),
        SPEND_BY_NON_BRANDED_PROJECTS(6,"Spend by Non-Branded Projects","Non-Branded Projects (By EM within Global, By EM within Region)"),
        SPEND_BY_GDB1_PROJECTS(7,"Spend by GDB 1 (Dunhill) Projects","GDB 1 (By EM within Global, By EM within Region)"),
        SPEND_BY_GDB2_PROJECTS(8,"Spend by GDB 2 (Kent) Projects","GDB 2 (By EM within Global, By EM within Region)"),
        SPEND_BY_GDB3_PROJECTS(9,"Spend by GDB 3 (Lucky Strike) Projects","GDB 3 (By EM within Global, By EM within Region)"),
        SPEND_BY_GDB4_PROJECTS(10,"Spend by GDB 4 (Pall Mall) Projects","GDB 4 (By EM within Global, By EM within Region)"),
        SPEND_BY_GDB5_PROJECTS(11,"Spend by GDB 5 (Rothmans) Projects","GDB 5 (By EM within Global, By EM within Region)"),
        SPEND_BY_GDB6_PROJECTS(12,"Spend by GDB 6 (Viceroy) Projects","GDB 6 (By EM within Global, By EM within Region)"),
        SPEND_BY_GDB7_PROJECTS(13,"Spend by GDB 7 (Vogue) Projects","GDB 7 (By EM within Global, By EM within Region)"),
        SPEND_BY_UPT_PROJECTS(14,"Spend by UPT Projects","UPT (By EM within Global, By EM within Region)"),
        SPEND_BY_BPT_PROJECTS(15,"Spend by BPT Projects","BPT (By EM within Global, By EM within Region)"),
        SPEND_BY_CAP1_PROJECTS(16,"Spend by CAP Rating (CAP 1) Projects","CAP Rating - CAP 1 (By EM within Global, By EM within Region)"),
        SPEND_BY_CAP2_PROJECTS(17,"Spend by CAP Rating (CAP 2) Projects","CAP Rating - CAP 2 (By EM within Global, By EM within Region)"),
        SPEND_BY_CAP3_PROJECTS(18,"Spend by CAP Rating (CAP 3) Projects","CAP Rating - CAP 3 (By EM within Global, By EM within Region)");

        private Integer id;
        private String name;
        private String description;

        private SpendReportType(final Integer id, final String name, final String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public static SpendReportType getById(final Integer id) {
            for(SpendReportType sr: SpendReportType.values()) {
                if(sr.getId().intValue() == id.intValue()) {
                    return sr;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.name;
        }

    }
    
    public static enum SpendReportTypeNew {
       /* SPEND_BY_PROJECTS(1, "Spend By Projects", "Projects"),
        SPEND_BY_BUDGET_LOCATION(2, "Spend By Budget Location", "Budget Location"),
        SPEND_BY_METHODOLOGY(3,"Spend By Methodology","Methodology"),
        SPEND_BY_BRANDED_NON_BRANDED(4,"Spend By Branded / Non-Branded","Branded / Non-Branded"),
        SPEND_BY_AGENCY(5,"Spend By Agency","Agency"),
        SPEND_BY_KANTAR_NONKANTAR(6,"Spend By Kantar / Non-Kantar","Kantar / Non-Kantar"),
        SPEND_BY_CATEGORY(7,"Spend By Category","Category");
        */
        SPEND_BY_PROJECTS(1, "Spend By Projects", "Projects"),
        SPEND_BY_METHODOLOGY(2,"Spend By Methodology","Methodology"),
        SPEND_BY_CATEGORY(3,"Spend By Category","Category"),
        SPEND_BY_BRANDED_NON_BRANDED(4,"Spend By Branded / Non-Branded","Branded / Non-Branded"),
        SPEND_BY_AGENCY(5,"Spend By Agency","Agency"),
        SPEND_BY_KANTAR_NONKANTAR(6,"Spend By Kantar / Non-Kantar","Kantar / Non-Kantar"),
        SPEND_BY_BUDGET_LOCATION(7, "Spend By Budget Location", "Budget Location");

        private Integer id;
        private String name;
        private String description;

        private SpendReportTypeNew(final Integer id, final String name, final String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public static SpendReportTypeNew getById(final Integer id) {
            for(SpendReportTypeNew sr: SpendReportTypeNew.values()) {
                if(sr.getId().intValue() == id.intValue()) {
                    return sr;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.name;
        }

    }

    public static enum SpendReportNotes {
        TOTAL_SPEND_NOTES("Notes:\n" +
                "- In the report above, the sum of end-markets spend could be lesser than the global spend, as in multi-market projects, the final cost of an end-market is only know once it is assigned to an agency.\n" +
                "- Exchange rates used are of QPR3 (budget rates).\n" +
                "- Latest project costs are used to prepare this report. If the final cost is not available, latest estimate is used to prepare the report.\n" +
                "- Deleted, Cancelled, and on-hold projects are not included in the report.\n" +
                "- In case of an area investment the cost is reflected into the corresponding region."),

        METHODOLOGY_NOTES("Notes:\n" +
                "- Exchange rates used are of QPR3 (budget rates).\n" +
                "- Latest project costs are used to prepare this report. If the final cost is not available, latest estimate is used to prepare the report.\n" +
                "- Deleted, Cancelled, and on-hold projects are not included in the report.\n" +
                "- In case a project include multiple methodologies, the total cost of the project will be attributed to all the methodologies."),

        REPORT_NOTES("Notes:\n" +
                "- Exchange rates used are of QPR3 (budget rates).\n" +
                "- Latest project costs are used to prepare this report. If the final cost is not available, latest estimate is used to prepare the report.\n" +
                "- Deleted, Cancelled, and on-hold projects are not included in the report.");


        String notes;
        private SpendReportNotes(final String notes) {
            this.notes = notes;
        }

        public String getName() {
            return notes;
        }

        @Override
        public String toString() {
            return notes;
        }
    }

    public static enum SpendReportBrandType {
        NON_BRANDED("Non-Brand Related"),
        DUNHILL("Dunhill"),
        KENT("Kent"),
        LUCKY_STRIKE("Lucky Strike"),
        PALL_MALL("Pall Mall"),
        ROTHMANS("Rothmans"),
        VICEROY("Viceroy"),
        VOGUE("Vogue");

        private String name;

        private SpendReportBrandType(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return this.name;
        }

    }

    public static enum SpendReportMethodologyType {
        BPT("Branded Product Test (BPT)"),
        BG4S("Branded Product Test (G4S)"),
        UPT("Unbranded Product Test (UPT)"),
        UG4S("Unbranded Product Test (G4S)");

        private String name;

        private SpendReportMethodologyType(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return this.name;
        }

    }

    public static enum PageType
    {
        HOME(1, "Home"),
        PROJECT(2, "Project"),
        MYDASHBOARD(3, "My Dashboard"),
        REPORTS(4, "Reports"),
        MYLIBRARY(5, "My Library"),
        SUPPORT(6, "Support"),
        PROFILE(7, "Profile"),
        ALERTSANDREMINDERS(8, "Alerts and Reminders"),
        PROCESS_WAIVER(9, "Project Waiver");


        int id;
        String description;

        PageType(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static PageType getById(int id) {
            for (PageType pageType : values()) {
                if (pageType.id == id) {
                    return pageType;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Page Type");
        }
    };
    
    public static Map<Integer, String> getPageTypes()
    {
        Map<Integer, String> pageTypes = new HashMap<Integer, String>();
        pageTypes.put(PageType.HOME.getId(), PageType.HOME.getDescription());
        pageTypes.put(PageType.PROJECT.getId(), PageType.PROJECT.getDescription());
        pageTypes.put(PageType.MYDASHBOARD.getId(), PageType.MYDASHBOARD.getDescription());
        pageTypes.put(PageType.REPORTS.getId(), PageType.REPORTS.getDescription());
        pageTypes.put(PageType.MYLIBRARY.getId(), PageType.MYLIBRARY.getDescription());
        pageTypes.put(PageType.SUPPORT.getId(), PageType.SUPPORT.getDescription());
        pageTypes.put(PageType.PROFILE.getId(), PageType.PROFILE.getDescription());
        pageTypes.put(PageType.ALERTSANDREMINDERS.getId(), PageType.ALERTSANDREMINDERS.getDescription());       
        return pageTypes;
    }
    
    public static enum Activity
    {
        ADD(1, "Add"),
        EDIT(2, "Edit"),
        APPEND(3, "Append"),
        NOTIFICATION(4, "Notification"),
        DOWNLOAD(5, "Download"),
        VIEW(6, "View"),
        UPLOAD(7, "Upload"),
        DELETE(8, "Delete"),
        APPROVE(9, "Approve"),
        REJECT(10, "Reject");

        int id;
        String description;

        Activity(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static Activity getById(int id) {
            for (Activity activity : values()) {
                if (activity.id == id) {
                    return activity;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Activity");
        }
    };
    
    public static Map<Integer, String> getActivityTypes()
    {
        Map<Integer, String> activityTypes = new HashMap<Integer, String>();
        activityTypes.put(Activity.ADD.getId(), Activity.ADD.getDescription());
        activityTypes.put(Activity.EDIT.getId(), Activity.EDIT.getDescription());
        activityTypes.put(Activity.APPEND.getId(), Activity.APPEND.getDescription());
        activityTypes.put(Activity.NOTIFICATION.getId(), Activity.NOTIFICATION.getDescription());
        activityTypes.put(Activity.DOWNLOAD.getId(), Activity.DOWNLOAD.getDescription());
        activityTypes.put(Activity.VIEW.getId(), Activity.VIEW.getDescription());
        activityTypes.put(Activity.UPLOAD.getId(), Activity.UPLOAD.getDescription());
        activityTypes.put(Activity.DELETE.getId(), Activity.DELETE.getDescription());
        activityTypes.put(Activity.APPROVE.getId(), Activity.APPROVE.getDescription());
               
        return activityTypes;
    }
    
    public static enum AgencyPerformance
    {
        NA(-1, "Not Applicable"),
        UNSATF(1, "1-Unsatisfactory"),
        POOR(2, "2-Poor"),
        SATF(3, "3-Satisfactory"),
        GOOD(4, "4-Good"),
        OUTSD(5, "5-Outstanding");        

        int id;
        String description;

        AgencyPerformance(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static AgencyPerformance getById(int id) {
            for (AgencyPerformance performance : values()) {
                if (performance.id == id) {
                    return performance;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Agency Performance");
        }
    };
    
    
    public static enum PortalType
    {
        SYNCHRO(1, "Synchro"),
        KANTAR(2, "Kantar"),
        DOCUMENT(3, "Document Repository"),
        GRAIL(4, "Grail"),
        IRIS(5, "IRIS");

        int id;
        String description;

        PortalType(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static PortalType getById(int id) {
            for (PortalType portalType : values()) {
                if (portalType.id == id) {
                    return portalType;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Portal Type");
        }
    };
    
    public static Map<Integer, String> getPortalTypes()
    {
        Map<Integer, String> portalTypes = new HashMap<Integer, String>();
        portalTypes.put(PortalType.SYNCHRO.getId(), PortalType.SYNCHRO.getDescription());
        portalTypes.put(PortalType.KANTAR.getId(), PortalType.KANTAR.getDescription());
        portalTypes.put(PortalType.DOCUMENT.getId(), PortalType.DOCUMENT.getDescription());        
        portalTypes.put(PortalType.GRAIL.getId(), PortalType.GRAIL.getDescription());
        portalTypes.put(PortalType.IRIS.getId(), PortalType.IRIS.getDescription());
        return portalTypes;
    }
    
    public static enum LogFieldType
    {
        CURRENCY(1, "Currency"),
        TEXT(2, "Text"),
        USER(3, "User"),
        SELECT(4, "Select"),
        DATE(5, "Date"),
        CHECKBOX(6, "Checkbox"),
        NOTIFICATION(7, "Notification"),
        CBAPPROVE(8, "Approve"),
        STAKEHOLDER(9, "Stakeholder");

        int id;
        String description;

        LogFieldType(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static LogFieldType getById(int id) {
            for (LogFieldType fieldType : values()) {
                if (fieldType.id == id) {
                    return fieldType;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Log Field Type");
        }
    };
    
    
    public static enum LogFields
    {
        LATESTESTM(1, "Latest Estimate"),
        NPINO(2, "NPI Number"),
        BIZQS(3, "Business Question"),
        RESOBJ(4, "Research Objectives"),
        BUSINESS(5, "Business"),
        ACTSTD(6, "Action Standards"),
        RESDSGN(7, "Methodology Approach and Research Design"),
        SMPLPROFILE(8, "Sample Profile (Research)"),
        STIMULUS(9, "Stimulus Material"),
        OTHER(10, "Other Comments"),
        DATESTIMULI(11, "Date Stimuli Available (in Research Agency)"),
        REPTOPLINE(12, "Reporting Requirement:Topline Presentation"),
        REPPRES(13, "Reporting Requirement:Presentation"),
        REPFULL(14, "Reporting Requirement:Full Report"),
        OTHERREP(15, "Other Reporting Requirements"),
        KANTAR(16, "Kantar"),
        NONKANTAR(17, "Non-Kantar"),
        AGENCY(18, "Agency Contact"),
        LEGAL(19, "Legal Contact"),
        PRODUCT(20, "Product Contact"),
        PROCUR(21, "Procurement Contact"),
        COMMUN(22, "Communication Agency"),
        FWCOST(23, "Fieldwork Cost"),
        METHDEVRNL(24, "Methodology Waiver-Rationale"),
        METHAPRCOMM(25, "Methodology Waiver-Approver's Comment"),
        METHAPR(26, "Methodology Waiver-Approver"),
        KANTARDEVRNL(27, "Non-Kantar-Initiate a Waiver-Rationale for using Non-Kantar Agency"),
        KANTARAPRCOMM(28, "Non-Kantar-Initiate a Waiver-Approver's Comment"),
        KANTARAPR(29, "Non-Kantar-Initiate a Waiver-Approver"),
        TENDERPROCESS(30, "Fieldwork has Tendering Process"),
        METHGROUP(31, "Methodology Group"),
        BRAND(32, "Brand / Non-Branded"),
        COUNTRY(33, "Country"),
        STARTDATE(34, "Estimated Project Start"),
        ENDDATE(35, "Estimated Project End"),
        METHTYPE(36, "Methodology Type"),
        REQMETHWAIVER(37, "Request for Methodology Waiver"),
        PROJECTOWNER(38, "Project Owner"),
        PROJECTCONTACT(39, "Product Contact"),
        PIBLEGALAPPRREC(40, "Legal Approval for PIB received"),
        PIBLEGALAPPRNOTREQ(41, "Legal Approval not required on PIB"),
        STIMMATSHIPPED(42, "Stimulus Material to be shipped to"),
        EMTOTALCOST(43, "Total Cost"),
        EMINTMGTCOST(44, "International Management Cost - Research Hub Cost"),
        EMLCLMGTCOST(45, "Local Management Cost"),
        EMFWCOST(46, "Fieldwork Cost"),
        EMHUBCOST(47, "Operational Hub Cost"),
        EMOTHERCOST(48, "Other Cost"),
        EMPROPFWAGNCY(49, "Name of Proposed Fieldwork Agencies"),
        EMFWSTART(50, "Estimated Fieldwork Start"),
        EMFWCOMP(51, "Estimated Fieldwork Completion"),
        EMDCMEH(52, "Data Collection Methods"),
        EMQUANTINT(53, "Quantitative-Total Number of Interviews"),
        EMQUANTVIS(54, "Quantitative-Total Number of Visits per Respondent"),
        EMQUANTAVGDUR(55, " Quantitative-Average Interview Duration"),
        EMQUANTGEOG(56, "Quantitative-Geographical Spread-Non-National-Geography"),
        PROPCOSTTEMP(57, "Proposal and Cost Template"),
        ESTIMATEDCOST(58, "Estimated Cost"),
        PONUMBER(59, "PO Number"),
        SCREENER(60, "Screener"),
        CONSUMERCONTRACTAGREEMENT(61, "Consumer Contract and Confidentiality Agreement"),
        QUESTIONNAIRE(62, "Questionnaire/Discussion guide"),
        ACTSTIMMAT(63, "Actual Stimulus Material"),
        FWESTENDDATE(64, "Estimated Project End Date"),
        FWBGNDATE(65, "Fieldwork Begin"),
        FWENDDATE(66, "Fieldwork End"),
        FWCHGCOMM(67, "Fieldwork change comments"),
        FWFINALCOST(68, "Final Cost"),
        FWCOSCHGCOMM(69, "Cost change comments"),
        LEGALSTIM(70, "Legal Approval - Stimulus Material"),
        LEGALSCREEN(71, "Legal Approval - Screener"),
        LEGALAGREEM(72, "Legal Approval - Consumer Contract and Confidentiality Agreement"),
        LEGALQUEST(73, "Legal Approval - Questionnaire"),
        LEGALDISGUIDE(74, "Legal Approval - Discussion guide"),
        CBFULLREPORT(75, "Full Report"),
        CBSUMREPORT(76, "Summary Report"),
        CBSUMIRIS(77, "Summary for IRIS"),
        REPORTCOMMENT(78, "Comments"),
        CBREPORTLEGALAPPROVER(79, "Legal Approval from"),
        EVALIMRATING(80, "International Management-Agency Performance for Project"),
        EVALLMRATING(81, "Local Management-Agency Performance for Project"),
        EVALFARATING(82, "Fieldwork Agencies-Agency Performance for Project"),
        EVALIMBAT(83, "International Management-BAT Comments"),
        EVALIMAGNCY(84, "International Management-Agency Comments"),
        EVALLMBAT(85, "Local Management-BAT Comments"),
        EVALLMAGNCY(86, "Local Management-Agency Comments"),
        EVALFABAT(87, "Fieldwork Agencies-BAT Comments"),
        EVALFAAGNCY(88, "Fieldwork Agencies-Agency Comments"),
        SPICONTACT(89, "SP&I Contact"),
        CAPRATING(90, "CAP Rating"),
        SUMMRAYWRITTENBY(91, "Summary Written By"),
        IRISFWSTARTDATE(92, "Fieldwork Start Date"),
        IRISFWENDDATE(93, "Fieldwork End Date"),
        SAMPLESIZE(94, "Sample Size"),
        REPORTDATE(95, "Report Date"),
        RESPONDENTTYPE(96, "Respondent Type"),
        PROJDESC(97, "Project Description"),
        IRISTAGS(98, "Tags"),
        IRISRELSTUD(98, "Related Studies"),
        IRISCONCLNS(100, "Conclusions"),
        IRISKEYFINDS(101, "Key Findings"),
        IRISALLDOCSINENG(102, "All documents are in English"),
        IRISREQ(103, "Summary For IRIS required"),
        IRISNOTREQ(104, "Summary For IRIS not required"),
        IRISSUMMARYOPT(105, "Summary for IRIS Option Rationale"),
        PITCATEGORY(106, "Category"),
        PITCONF(107, "Confidential Project"),
        PITESTCOST(108, "Estimated Cost"),
        PITPROJNAME(109, "Project Name"),
        PITPROPMETH(110, "Proposed Methodology"),
        PITBUDGETYEAR(111, "Budget Year"),
        // Synchro New Requirement Changes Start
        PIT_PROJECT_DESCRIPTION(112, "Project Descrition"),
        PIT_PROJECTMANAGER(113, "SP&I Contact"),
        PIT_METHODOLOGY_DETAILS(114, "Methodology Details"),
        PIT_WILL_METH_WAIVER_REQUIRED(115, "Will a methodology waiver be required"),
        PIT_IS_THIS_BRAND_SPECIFIC_STUDY(116, "Is this a brand specific study"),
        PIT_BRAND_SPECIFIC_STUDY_TYPE(117, "Brand specific study Type"),
        PIT_BUDGET_LOCATION(118, "Budget Location"),
        PIT_TOTAL_COST(119, "Total Cost"),
        PIB_WILL_METH_WAIVER_REQUIRED(120, "Will a methodology waiver be required"),
        PIB_BRIEF(121, "Brief (if any)"),
        PIB_BRIEF_LEGAL_APPROVER(122, "Legal Approver's Name"),
        PIB_BRIEF_LEGAL_APPROVAL_STATUS(123, "Legal Approval Status"),
        PIB_BRIEF_LEGAL_APPROVAL_DATE(124, "Date of Legal Approval"),
        PROPOSAL_WILL_METH_WAIVER_REQUIRED(125, "Will a methodology waiver be required"),
        PROPOSAL_PROPOSAL(126, "Proposal"),
        PROPOSAL_LEGAL_APPROVER(127, "Legal Approver's Name"),
        PROPOSAL_LEGAL_APPROVAL_STATUS(128, "Legal Approval Status"),
        PROPOSAL_LEGAL_APPROVAL_DATE(129, "Date of Legal Approval"),
        PROJECTSPECS_WILL_METH_WAIVER_REQUIRED(130, "Will a methodology waiver be required"),
        PROJECTSPECS_DOCUMENTATION(131, "Documentation"),
        PROJECTEVAL_AGENCY(132, "Agency"),
        PROJECTEVAL_RATING(133, "Rating"),
        PROJECTEVAL_COMMENTS(134, "Comments"),
        PROPOSAL_REF_SYCNHRO_CODE(135, "Reference Synchro Code"),
        TPD_SUMMARY_RESEARCH_DONE_ON(136, "Research Done On"),
        TPD_SUMMARY_PRODUCT_DESC(137, "Product Description"),
        TPD_SUMMARY_HAS_PROD_MODIFICATION(138, "Has the Product Modification Happened Yet"),
        TPD_SUMMARY_DATE_OF_MODIFICATION(139, "Date Of Modification"),
        TPD_SUMMARY_TAO_CODE(140, "TAO Code"),
        TPD_SUMMARY_LEGAL_STATUS(141, "Status"),
        TPD_SUMMARY_LEGAL_APPROVER(142, "Legal Approval Provided By"),
        TPD_SUMMARY_DATE_OF_LEGAL_APPROVAL(143, "Date Of Legal Approval"),
        PIT_RESEARCH_END_MARKET(144, "Research End Market"),
        WAIVER_NAME(145, "Waiver Name"),
        WAIVER_DESCRIPTION(146, "Waiver Description"),
        WAIVER_END_MARKETS(147, "Waiver End-markets");

        int id;
        String description;

        LogFields(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static LogFields getById(int id) {
            for (LogFields field : values()) {
                if (field.id == id) {
                    return field;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Log Fields");
        }
    };
    
    
    
    public static enum LogActivity {
        PIT_DRAFT, PIT_CREATE, PIB_SAVE;

        public static String getName(int id) {
            for (LogActivity activity : values()) {
                if (activity.ordinal() == id) {
                    return activity.name();
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Log Activity");
        }
    };
    
    public static enum LogProjectStage
    {
        DRAFT(1, "Draft"),
        CREATE(2, "Create"),
        PIB(3, "PIB"),
        PROPOSAL(4, "Proposal"),
        PROJECT_SPECS(5, "Project Specs"),
        REPORT_SUMMARY(6, "Report Summary"),
        PROJECT_EVALUATION(7, "Project Evaluation"),
        PROJECT_STATUS(8, "Change Project Status"),
        PROJECT_WAIVER(9, "Project Waiver"),
        DASHBOARD(10, "My Projects"),
        DASHBOARDPENDINGACTV(11, "My Pending Activities"),
        DASHBOARDWAIVER(12, "My Waivers"),
        CHNGCONTACTOWNERS(13, "Change Key Contacts and Owners"),
        RAWEXTRACT(14, "Raw Extract"),
        SPENDREPORTS(15, "Spend Reports"),
        DOCUMENT(16, "Documents"),
        SUPPORT(17, "Support"),
        HOMEPAGE(18, "Home Page"),
        PENDING_ACTIONS(19, "Pending Actions"),
        DASHBOARDDOCREP(20, "My Reports"),
        DASHBOARDKANTAR(21, "My Projects"),
        SYNCHRO2IRIS(22, "Synchro to IRIS"),
        VIEWEDITPIT(23, "View/Edit PIT"),
        FWCHANGE(24, " Change Fieldwork/Cost Status"),
        PROJECT_IN_PLANNING(25, "In Planning"),
        PROJECT_IN_PROGRESS(26, "In Progress"),
        PROJECT_COMPLETE(27, "Complete"),
        PROJECT_CLOSE(28, "Close"),
        TPD_SUMMARY(29, "TPD Submission");
        

        int id;
        String description;

        LogProjectStage(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static LogProjectStage getById(int id) {
            for (LogProjectStage stage : values()) {
                if (stage.id == id) {
                    return stage;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Log Project Stage ID");
        }
    };
    
    
    public static enum ProjectReminderCategoryType {

        WAIVERS(1, "Waivers"),
        DRAFT_PROJECT(2, "Draft Projects"),
        PROJECT_PENDING_ACTIVITY(3, "Pending Activities - SynchrO"),
        GRAIL_PENDING_ACTIVITY(4, "Pending Activities - Grail Button"),
        KANTAR_PENDING_ACTIVITY(5, "Pending Activities - Kantar Button");



        private static Map<Integer, String> categoryTypesMap = new HashMap<Integer, String>();
        static {
            for(ProjectReminderCategoryType categoryType: values()) {
                categoryTypesMap.put(categoryType.getId(), categoryType.getName());
            }
        }
        Integer id;
        String name;



        private ProjectReminderCategoryType(final Integer id, final String name) {
            this.id = id;
            this.name = name;
        }

        public static ProjectReminderCategoryType getById(int id) {
            for (ProjectReminderCategoryType status : values()) {
                if (status.getId() == id) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Status");
        }
        
        public Integer getId() {
            return id;
        }



        public String getName() {
            return name;
        }

        @Override
        public String toString() {

            return name;
        }
    }
    
    public static enum SynchroReminderType {

        PROJECT_REMINDER(1, "Project Reminder"),
        GENERAL_REMINDER(2, "General Reminder");


        private static Map<Integer, String> reminderTypesMap = new HashMap<Integer, String>();
        static {
            for(SynchroReminderType reminderType: values()) {
                reminderTypesMap.put(reminderType.getId(), reminderType.getName());
            }

        }
        
        Integer id;
        String name;

        private SynchroReminderType(final Integer id, final String name) {
            this.id = id;

            this.name = name;
        }


        public String getNameById(final Integer id) {
            if(reminderTypesMap.containsKey(id)) {
                return reminderTypesMap.get(id);
            } else {
                return "";
            }
        }

        public Integer getId() {
            return id;
        }

        public String getName() {

            return name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }

    public static enum ProjectReminderFrequencyType {
        DAILY(1, "Daily"),
        WEEKLY(2, "Weekly"),
        MONTHLY(3, "Monthly"),
        YEARLY(4, "Yearly");
        private static Map<Integer, String> frequencyTypesMap = new HashMap<Integer, String>();
        static {
            for(ProjectReminderFrequencyType frequencyType: values()) {
                frequencyTypesMap.put(frequencyType.getId(), frequencyType.getName());
            }
        }
        Integer id;
        String name;

        private ProjectReminderFrequencyType(final Integer id, final String name) {
            this.id = id;
            this.name = name;
        }

        public static ProjectReminderFrequencyType getById(int id) {
            for (ProjectReminderFrequencyType status : values()) {
                if (status.getId() == id) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Status");
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {

            return name;
        }
    }

    
    public static enum ProjectReminderWeekday {
        SUNDAY(1, "Sunday"),
        MONDAY(2, "Monday"),
        TUESDAY(3, "Tuesday"),
        WEDNESDAY(4, "Wednesday"),
        THURSDAY(5, "Thursday"),
        FRIDAY(6, "Friday"),
        SATURDAY(7, "Saturday");

        private static Map<Integer, String> weekdaysMap = new HashMap<Integer, String>();
        static {
            for(ProjectReminderWeekday weekday: values()) {
                weekdaysMap.put(weekday.getId(), weekday.getName());
            }

        }

        private Integer id;
        private String name;

        private ProjectReminderWeekday(final Integer id, final String name) {
            this.id = id;

            this.name = name;
        }

        public String getNameById(final Integer id) {
            if(weekdaysMap.containsKey(id)) {
                return weekdaysMap.get(id);
            } else {
                return "";
            }
        }

        public Integer getId() {
            return id;
        }

        public String getName() {

            return name;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    public static enum ProjectReminderMonth {
        JANUARY(1, "January"),
        FEBRUARY(2, "February"),
        MARCH(3, "March"),
        APRIL(4, "April"),
        MAY(5, "May"),
        JUNE(6, "June"),
        JULY(7, "July"),
        AUGUST(8, "August"),
        SEPTEMBER(9, "September"),
        OCTOBER(10, "October"),
        NOVEMBER(11, "November"),
        DECEMBER(12, "December");

        private static Map<Integer, String> monthsMap = new HashMap<Integer, String>();
        static {
            for(ProjectReminderMonth month: values()) {
                monthsMap.put(month.getId(), month.getName());
            }

        }

        private Integer id;
        private String name;

        private ProjectReminderMonth(final Integer id, final String name) {
            this.id = id;

            this.name = name;
        }


        public String getNameById(final Integer id) {
            if(monthsMap.containsKey(id)) {
                return monthsMap.get(id);
            } else {
                return "";
            }
        }

        public Integer getId() {
            return id;
        }

        public String getName() {

            return name;
        }

        @Override
        public String toString() {
            return name;
        }

    }
    public static Map<String, Object> getAppProperties() {
        return appProperties;
    }

    public static void setAppProperties(Map<String, Object> appProperties) {
        SynchroGlobal.appProperties = appProperties;
    
    }
    
    public static Map<Integer, String> getResearchAgency(){
        if(researchAgencies==null || researchAgencies.size()<1)
        {
        	researchAgencies = SynchroUtils.getResearchAgencyFields();
            return researchAgencies;
        }
        else
        {
            return researchAgencies;
        }

    }
       
    public static Map<Integer, String> getAllResearchAgency(){
        if(allResearchAgencies==null || allResearchAgencies.size()<1)
        {
        	allResearchAgencies = SynchroUtils.getAllResearchAgencyFields();
            return allResearchAgencies;
        }
        else
        {
            return allResearchAgencies;
        }

    }
       
       
    public static Map<Integer, String> getResearchAgencyGroup(){
        if(researchAgencyGroups==null || researchAgencyGroups.size()<1)
        {
        	researchAgencyGroups = SynchroUtils.getResearchAgencyGroupFields();
            return researchAgencyGroups;
        }
        else
        {
            return researchAgencyGroups;
        }

    }
    
    public static  Map<Integer, Map<Integer, String>> getResearchAgencyMapping()
    {
    	 if(researchAgencyMapping==null || researchAgencyMapping.size()<1)
         {
             for(Integer researchAgencyGroup : getResearchAgencyGroup().keySet())
             {
                 List<MetaField> researchAgencies = SynchroUtils.getResearchAgencyByGroup(researchAgencyGroup);
                 Map<Integer, String> researchAgencyMap = new LinkedHashMap<Integer, String>();
                 for(MetaField field : researchAgencies)
                 {
                	 researchAgencyMap.put(field.getId().intValue(), field.getName());
                 }
                 researchAgencyMapping.put(researchAgencyGroup, researchAgencyMap);
             }
             return researchAgencyMapping;
         }
         else
         {
             return researchAgencyMapping;
         }
    }
    
    public static  Map<Integer, Map<Integer, String>> getAllResearchAgencyMapping()
    {
    	 if(allResearchAgencyMapping==null || allResearchAgencyMapping.size()<1)
         {
             for(Integer researchAgencyGroup : getResearchAgencyGroup().keySet())
             {
                 List<MetaField> researchAgencies = SynchroUtils.getAllResearchAgencyByGroup(researchAgencyGroup);
                 Map<Integer, String> researchAgencyMap = new LinkedHashMap<Integer, String>();
                 for(MetaField field : researchAgencies)
                 {
                	 researchAgencyMap.put(field.getId().intValue(), field.getName());
                 }
                 allResearchAgencyMapping.put(researchAgencyGroup, researchAgencyMap);
             }
             return allResearchAgencyMapping;
         }
         else
         {
             return allResearchAgencyMapping;
         }
    }
    
    /**
     * This method will return the Research Agency Group for a particular Research Agency
     * @return
     */
    public static Integer getResearchAgencyGroupFromAgency(Integer agencyId )
    {
    	 Integer researchGroup = -1;
    	 //Map<Integer, Map<Integer, String>> agencyGroupMapping = getResearchAgencyMapping();
    	 Map<Integer, Map<Integer, String>> agencyGroupMapping = getAllResearchAgencyMapping();
    	 for(Integer researchGroups : agencyGroupMapping.keySet())
    	 {
    		 for(Integer researchAgency:agencyGroupMapping.get(researchGroups).keySet() )
    		 {
    			 if(researchAgency.intValue()==agencyId.intValue())
    			 {
    				 researchGroup=researchGroups;
    			 }
    		 }
    	 }
    	 return researchGroup;
    }
    /**
     * Synchro Phase 5 
     * @author kanwardeep.grewal
     */
        public static Map<Integer, String> getT20T40()
        {
            if(t20T40==null || t20T40.size()<1)
            {
            	t20T40 = SynchroUtils.getT20T40();
                return t20T40;
            }
            else
            {
                return t20T40;
            }
        }

        //TODO
        public static Map<Integer, String> getT20T40ByEndmarket(final Long tid){
            if(getT20T40EndMarketsMapping().containsKey(tid.intValue()))
            {
                return getAreaEndMarketsMapping().get(tid.intValue());
            }
            else
            {
                return new LinkedHashMap<Integer, String>();
            }
        }
   
        
        public static  Map<Integer, Map<Integer, String>> getT20T40EndMarketsMapping()
        {
            if(t20t40EndMarketsMapping==null || t20t40EndMarketsMapping.size()<1)
            {
                for(Integer tid : getT20T40().keySet())
                {
                    List<MetaField> endmarkets = SynchroUtils.getEndMarketsByT20T40(tid);
                    Map<Integer, String> endMarketMap = new LinkedHashMap<Integer, String>();
                    for(MetaField field : endmarkets)
                    {
                        endMarketMap.put(field.getId().intValue(), field.getName());
                    }
                    if(!t20t40EndMarketsMapping.containsKey(tid))
                    	t20t40EndMarketsMapping.put(tid, endMarketMap);
                }
                return t20t40EndMarketsMapping;
            }
            else
            {
                return t20t40EndMarketsMapping;
            }
        }

        public static Map<Integer, String> getEMApprovalTypes()
        {
            Map<Integer, String> endmarketApprovals = new HashMap<Integer, String>();
            endmarketApprovals.put(1, "Online");
            endmarketApprovals.put(2, "Offline");
            endmarketApprovals.put(3, "NA");
            return endmarketApprovals;
        }
        
        public static Map<Integer, String> getEUMarketTypes()
        {
            Map<Integer, String> marketTypes = new HashMap<Integer, String>();
            marketTypes.put(1, "Yes");
            marketTypes.put(2, "No");            
            return marketTypes;
        }
        
        
        public static Map<Integer, String> getT20_T40_Types()
        {
            Map<Integer, String> t20_t40_Types = new HashMap<Integer, String>();
            t20_t40_Types.put(1, "T20");
            t20_t40_Types.put(2, "T40");   
            t20_t40_Types.put(3, "Non-T40");   
            return t20_t40_Types;
        }
        
	public static enum ProjectStatusNew {
        DRAFT("PIT(Draft)"),
        BRIEF_IN_PLANNING("In-Planning"),
        PROPOSAL_IN_PLANNING("In-Planning"),
        IN_PROGRESS("In-Progress"),
        COMPLETE_REPORT("Awaiting Results"),
        COMPLETE_PROJECT_EVAL("Awaiting Results"),
        CLOSE("Closed"),
        CANCEL("Cancelled");

        private String value;

        private ProjectStatusNew(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ProjectStatusNew getById(int id) {
            for (ProjectStatusNew status : values()) {
                if (status.ordinal() == id) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Status");
        }

        public static String getName(long id) {
            for (ProjectStatusNew status : values()) {
                if (status.ordinal() == id) {
                    return status.toString();
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Status");
        }

        @Override
        public String toString() {
            return this.value;
        }
    };
    
  
    
    public static enum ProjectType
    {
        GLOBAL(1, "Global"),
        REGIONAL(2, "Regional"),
        ENDMARKET(3, "End Market");

        int id;
        String description;

        ProjectType(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static ProjectType getById(int id) {
            for (ProjectType projectType : values()) {
                if (projectType.id == id) {
                    return projectType;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Type");
        }
    };
    
    public static enum ProjectProcessType
    {
        END_MARKET_EU_ONLINE(1, "End Market EU Online"),
        END_MARKET_EU_OFFLINE(2, "End Market EU Offline"),
        END_MARKET_NON_EU(3, "End Market Non EU"),
        END_MARKET_FIELDWORK(4, "End Market Fieldwork"),
        GLOBAL_EU_ONLINE(5, "Global EU Online"),
        GLOBAL_EU_OFFLINE(6, "Global EU Offline"),
        GLOBAL_NON_EU(7, "Global Non EU");
        
        int id;
        String description;

        ProjectProcessType(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static ProjectProcessType getById(int id) {
            for (ProjectProcessType projectProcessType : values()) {
                if (projectProcessType.id == id) {
                    return projectProcessType;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Process Type");
        }
    };
    
    
    /**
     * Grail Phase 5
     * @return
     */
    public static Map<Integer, String> getGrailResearchAgency() {

        if(grailResearchAgencies==null || grailResearchAgencies.size()<1)
        {
        	grailResearchAgencies = SynchroUtils.getGrailResearchAgencyFields();
            return grailResearchAgencies;
        }
        else
        {
            return grailResearchAgencies;
        }
    }
    
    public static Map<Integer, String> getGrailResearchAgencyGroup(){

        if(grailResearchAgencyGroup==null || grailResearchAgencyGroup.size()<1)
        {
        	grailResearchAgencyGroup = SynchroUtils.getGrailResearchAgencyGroupFields();
            return grailResearchAgencyGroup;
        }
        else
        {
            return grailResearchAgencyGroup;
        }

    }
    
    public static Map<Integer, String> getGrailResearchAgencyGroupMapping(){

        if(grailResearchAgencyGroupMapping==null || grailResearchAgencyGroupMapping.size()<1)
        {
            for(Integer gid :getSupplierGroup().keySet())
            {
                List<Long> agencies = SynchroUtils.getGrailResearchAgencyFields(gid);
                grailResearchAgencyGroupMapping.put(gid, StringUtils.join(agencies, ','));
            }
            return grailResearchAgencyGroupMapping;
        }
        else
        {
            return grailResearchAgencyGroupMapping;
        }
    }
    
    public static enum ReportType
    {
    	FULL_REPORT_BLANK(0, "Full Report Blank"),
    	FULL_REPORT(1, "Full Report"),
        TOP_LINE_REPORT(2, "Top Line Report"),
        EXECUTIVE_PRESENTATION(3, "Executive Presentation"),
        IRIS_SUMMARY(4, "IRIS Summary"),
        TPD_SUMMARY(5, "TPD Summary");
        
        int id;
        String description;

        ReportType(final int id, final String desc) {
            this.id = id;
            this.description = desc;
        }

        public int getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public static ReportType getById(int id) {
            for (ReportType reportType : values()) {
                if (reportType.id == id) {
                    return reportType;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Report Type");
        }
    };
    
   
    public static Map<Integer, String> getAgencyRatings(){
        agencyRatings.put(1, "1-Poor");
        agencyRatings.put(2, "2-Unsatisfactory");
        agencyRatings.put(3, "3-Satisfactory");
        agencyRatings.put(4, "4-Good");
        agencyRatings.put(5, "5-Excellent");
        return agencyRatings;
    }
    
    public static Map<Integer, String> getSpendForOptions(){
    	spendForOptions.put(1, "COPLA");
    	spendForOptions.put(2, "QPR 1");
    	spendForOptions.put(3, "QPR 2");
    	spendForOptions.put(4, "QPR 3");
    	spendForOptions.put(5, "FULL YEAR");
        return spendForOptions;
    }
    
}

