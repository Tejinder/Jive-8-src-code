package com.grail.synchro;

import com.jivesoftware.community.JiveGlobals;

/**
 * All constants that are reference across Synchro customization should be defined here.
 *
 * @author: Vivek Kondur
 */
public class SynchroConstants {


    public static final String SYNCHRO_PROJECTS_CONTAINER = "grail.synchro.projects.containerID";
    public static final String SYNCHRO_WAIVERS_CONTAINER = "grail.synchro.waivers.containerID";
    public static final Integer SYNCHRO_PROJECT_CONTRIBUTE_PERM = 4;
    public static final Integer SYNCHRO_PROJECT_CREATE_PERM = 3;
    public static final Integer SYNCHRO_WAIVER_CREATE_PERM = 3;
    public static final Integer SYNCHRO_PROJECT_ADMINISTRATOR_PERM = 1;
    public static final Double SYNCHRO_DEFAULT_CURRENCY_VALUE = 1D;
    public static final String SYNCHRO_PROJECT_PARENT_CONTAINER = JiveGlobals.getJiveProperty(SYNCHRO_PROJECTS_CONTAINER);

    /**
     *  Project specific jive user group name format. This UG will be assigned to the community to restrict the access
     *  for other portal users. Description defines the purpose of this user group.
     *  SYNCHRO_PROJECT__<projectID>_STAKEHOLDERS
     */
    public static final String SYNCHRO_PROJECT_UG_NAME = "SYNCHRO_PROJECT_%s_STAKEHOLDERS";
    public static final String SYNCHRO_CURRENCY_VALUE = "grail.synchro.currency.%s";
    public static final String SYNCHRO_PROJECT_UG_DESC = "This user group contains all stakeholders specific to '%s' project";
    public static final String SYNCHRO_PROJECT_WIZARD_STATE_KEY = "WIZARD_STATE.%s";

    /**
     * Synchro System property for field work Tabs
     * TODO
     * public static final String SYNCHRO_PROJECT_FIELDWORK_TABS = "grail.synchro.fieldwork.tabs";
     */
    public static final String SYNCHRO_PROJECT_FIELDWORK_TABS = "4,5";
    
    // Synchro System Property for default permission matrix selections, values of stages with comma separated in case of multiples
    public static final String SYNCHRO_PROJECT_DEFAULT_PERMISSIONS = "grail.synchro.%s.permission.%s";
    /*
     * Marketing Approvers
     */
    //public static final String SYNCHRO_PROJECT_MARKETING_DEF_PERMISSIONS = "grail.synchro.marketing_approvers.permission.globally";
    //public static final String SYNCHRO_PROJECT_MARKETING_DEF_PERMISSIONS = "grail.synchro.marketing_approvers.permission.regional";
    //public static final String SYNCHRO_PROJECT_MARKETING_DEF_PERMISSIONS = "grail.synchro.marketing_approvers.permission.simple";
    
    //Other BAT Approvers
    //public static final String SYNCHRO_PROJECT_MARKETING_DEF_PERMISSIONS = "grail.synchro.other_approvers.permission.globally";
    //public static final String SYNCHRO_PROJECT_MARKETING_DEF_PERMISSIONS = "grail.synchro.other_approvers.permission.regional";
    //public static final String SYNCHRO_PROJECT_MARKETING_DEF_PERMISSIONS = "grail.synchro.other_approvers.permission.simple";
     
    //SPI Approvers
    //public static final String SYNCHRO_PROJECT_MARKETING_DEF_PERMISSIONS = "grail.synchro.spi_approvers.permission.globally";
    //public static final String SYNCHRO_PROJECT_MARKETING_DEF_PERMISSIONS = "grail.synchro.spi_approvers.permission.regional";
    //public static final String SYNCHRO_PROJECT_MARKETING_DEF_PERMISSIONS = "grail.synchro.spi_approvers.permission.simple";
    
    //Track Permission default selection for Project Intitation
    public static final String SYNCHRO_PROJECT_WZ_STAGE_PERMISSIONS_SET = "grail.synchro.wz%s.default.permissions.%s";
    

    // Synchro project related progress bar colors will be stored in system property in lowercase
    public static final String SYNCHRO_PROJECT_GRAPH_COLOR = "grail.synchro.graph.color.%s";
    //public static final String SYNCHRO_PROJECT_GRAPH_COLOR = "grail.synchro.graph.color.concept";
    //public static final String SYNCHRO_PROJECT_GRAPH_COLOR = "grail.synchro.graph.color.planned";
    //public static final String SYNCHRO_PROJECT_GRAPH_COLOR = "grail.synchro.graph.color.inprogress";
    // Synchro project related documents will be store in community extended property
    public static final String SYNCHRO_PROJECT_DOCUMENT_PROPERTY = "grail.synchro.project.doc.%s";
    public static final String SYNCHRO_WAIVER_DOCUMENT_PROPERTY = "grail.synchro.waiver.doc";
    public static final String SYNCHRO_PROJECT_BUDGET_START_YEAR = "grail.synchro.project.budgetYear";
//    public static final String SYNCHRO_PROJECT_DOCUMENT_RIB = "grail.synchro.project.doc.rib";
//    public static final String SYNCHRO_PROJECT_DOCUMENT_PPF = "grail.synchro.project.doc.ppf";
//    public static final String SYNCHRO_PROJECT_DOCUMENT_RC = "grail.synchro.project.doc.rc";
//    public static final String SYNCHRO_PROJECT_DOCUMENT_SCF = "grail.synchro.project.doc.scf";
//    public static final String SYNCHRO_PROJECT_DOCUMENT_qdg = "grail.synchro.project.doc.qdg";
//    public static final String SYNCHRO_PROJECT_DOCUMENT_ps = "grail.synchro.project.doc.ps";

    //All are Community Extended Property Keys
    public static final String PROJECT_STAKEHOLDER_GROUP_KEY = "grail.synchro.project.stakeholder.groupId";
    public static final String BUDGET_APPROVERS_KEY = "grail.synchro.project.budget.users";
    public static final String MARKETING_APPROVERS_KEY = "grail.synchro.project.marketing.users";
    public static final String SPI_APPROVERS_KEY = "grail.synchro.project.spi.users";
    public static final String LEGAL_APPROVERS_KEY = "grail.synchro.project.legal.users";
    public static final String PROCUREMENT_APPROVERS_KEY = "grail.synchro.project.procurement.users";
    public static final String COORDINATION_AGENCY_KEY = "grail.synchro.project.coagency.users";
    public static final String COORDINATION_AGENCY_SUPPORT_KEY = "grail.synchro.project.coagency_support.users";
    public static final String FIELDWORK_AGENCY_SUPPORT_KEY = "grail.synchro.project.fw.users";
    public static final String COMMUNICATION_AGENCY_SUPPORT_KEY = "grail.synchro.project.communication.users";

  //System Properties for setting Quarters rqeuired in Financial details Tab
    public static final String SYNCHRO_PROJECT_QUARTER_LIST= "grail.synchro.project.quarter.range.%s";
    //public static final String SYNCHRO_PROJECT_QUARTER_LIST= "grail.synchro.project.quarter.range.q1";
    //public static final String SYNCHRO_PROJECT_QUARTER_LIST= "grail.synchro.project.quarter.range.q2";
    //public static final String SYNCHRO_PROJECT_QUARTER_LIST= "grail.synchro.project.quarter.range.q3";
    //public static final String SYNCHRO_PROJECT_QUARTER_LIST= "grail.synchro.project.quarter.range.q4";
    
    //Jive UserGroup Names
    public static final String JIVE_RKP_GROUP_NAME = "RKP";
    public static final String JIVE_SYNCHRO_GROUP_NAME = "SYNCHRO";
    public static final String JIVE_MARKETING_APPROVERS_GROUP_NAME = "MARKETING_APPROVERS";
    
    public static final String JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME = "GLOBAL_MARKETING_APPROVERS";
    public static final String JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME = "REGIONAL_MARKETING_APPROVERS";
    public static final String JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME = "END_MARKET_MARKETING_APPROVERS";
    
    public static final String JIVE_SPI_APPROVERS_GROUP_NAME = "SPI_APPROVERS";
    public static final String JIVE_GLOBAL_GROUP_PREFIX = "GLOBAL_";
    public static final String JIVE_REGIONAL_GROUP_PREFIX = "REGIONAL_";
    public static final String JIVE_END_MARKET_GROUP_PREFIX = "END_MARKET_";
    public static final String JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME = JIVE_GLOBAL_GROUP_PREFIX + "SPI_APPROVERS";
    public static final String JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME = JIVE_REGIONAL_GROUP_PREFIX + "SPI_APPROVERS";
    public static final String JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME = JIVE_END_MARKET_GROUP_PREFIX + "SPI_APPROVERS";
    
    public static final String JIVE_LEGAL_APPROVERS_GROUP_NAME = "LEGAL_APPROVERS";
    public static final String JIVE_GLOBAL_LEGAL_APPROVERS_GROUP_NAME = "GLOBAL_LEGAL_APPROVERS";
    public static final String JIVE_END_MARKET_LEGAL_APPROVERS_GROUP_NAME = "END_MARKET_LEGAL_APPROVERS";
    
    public static final String JIVE_PROCUREMENT_APPROVERS_GROUP_NAME = "PROCUREMENT_APPROVERS";
    public static final String JIVE_GLOBAL_PROCUREMENT_APPROVERS_GROUP_NAME = "GLOBAL_PROCUREMENT_APPROVERS";
    public static final String JIVE_END_MARKET_PROCUREMENT_APPROVERS_GROUP_NAME = "END_MARKET_PROCUREMENT_APPROVERS";
    
    public static final String JIVE_OTHER_BAT_APPROVERS_GROUP_NAME = "OTHER_BAT_APPROVERS";
    public static final String JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME = "GLOBAL_OTHER_BAT_APPROVERS";
    public static final String JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME = "REGIONAL_OTHER_BAT_APPROVERS";
    public static final String JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME = "END_MARKET_OTHER_BAT_APPROVERS";
    
    public static final String JIVE_COAGENCY_GROUP_NAME = "COORDINATION_AGENCY";
    public static final String JIVE_COAGENCY_SUPPORT_GROUP_NAME = "CO_AGENCY_SUPPORT";
    public static final String JIVE_FIELDWORK_GROUP_NAME = "FIELDWORK_AGENCY";
    public static final String JIVE_COMMUNICATION_AGECNY_GROUP_NAME = "COMMUNICATION_AGENCY";
    public static final String JIVE_ORACLE_APPROVERS_GROUP_NAME = "ORACLE_APPROVERS";
    public static final String JIVE_METHODOLOGY_APPROVERS_GROUP_NAME = "METHODOLOGY_APPROVERS";
    
    public static final String JIVE_KANTAR_METHODOLOGY_APPROVERS_GROUP_NAME = "KANTAR_METHODOLOGY_APPROVERS";
    
    public static final String JIVE_PROCESS_WAIVER_APPROVERS_GROUP_NAME = "PROCESS_WAIVER_APPROVERS";
    
    public static final String JIVE_COMMUNICATION_AGENCY_ADMIN_GROUP_NAME = "COMMUNICATION_AGENCY_ADMIN";
   
    public static final String BUDGET_APPROVERS = "BUDGET_APPROVERS";
    
    public static final String JIVE_GLOBAL_SUPPORT_GROUP_NAME = "GLOBAL_SUPPORT";
    public static final String JIVE_REGIONAL_SUPPORT_GROUP_NAME = "REGIONAL_SUPPORT";
    public static final String JIVE_END_MARKET_SUPPORT_GROUP_NAME = "END_MARKET_SUPPORT";
    public static final String JIVE_SUPPORT_GROUP_NAME = "SUPPORT";
    
    public static final String JIVE_EXTERNAL_AGENCY_GROUP_NAME = "EXTERNAL_AGENCY";
    public static final String AWARDED_EXTERNAL_AGENCY_ROLE = "AWARDED_EXTERNAL_AGENCY";

    public static final String JIVE_KANTAR_AGENCY_GROUP_NAME = "KANTAR_AGENCY";
    public static final String JIVE_DOCUMENT_REPOSITORY_AGENCY_GROUP_NAME = "DOCUMENT_REPOSITORY_AGENCY";
    public static final String JIVE_DOCUMENT_REPOSITORY_BAT_GROUP_NAME = "DOCUMENT_REPOSITORY_BAT";

    //Community Options Action URL
    public static final String COMMUNITY_OPTIONS_ACTION_URL ="/portal-options.jspa";
    public static final String RKP_DISCLIAMER_URL ="/disclaimer.jspa?type=rkp";
    public static final String SYNCHRO_DISCLIAMER_URL ="/disclaimer.jspa?type=synchro";

    public static final String PROJECT_ACTIVITY_ACTION_URL ="/synchro/activity";
    public static final String PROJECT_PIB_ACTION_URL ="/synchro/pib-details";
    
    // Properties for Dashboard Project progress Bar
    public static final int PROJECT_DASHBOARD_PROGRESSBAR_LEN = 720;
    public static final String PROJECT_DASHBOARD_PROGRESSBAR_DEFCOLOR ="#CBEDD8";
    public static final String PROJECT_DASHBOARD_PAGE_LIMIT = "grail.synchro.table.page.limit";
    public static final String LOG_DASHBOARD_PAGE_LIMIT = "grail.synchro.log.page.limit";
    
    //Session key for storing Project Activities and Document MAP used in Activity Tabs
    public static final String SYNCHRO_PROJECT_DOCUMENT_MAP= "PROJECT_DOC_MAP_%s";
    //Stage To Do List Document Properties
    public static final String DOCUMENT_TODO_LIST_NAME="grail.ToDoList";
    public static final String DOCUMENT_TODO_LIST_APPROVERS="grail.ToDoList.Approvers";
    public static final String DOCUMENT_TODO_LIST_APPROVEFUNDING="grail.ToDoList.ApproveFunding";
    public static final String DOCUMENT_TODO_LIST_APPROVERPO_NUMBER="grail.ToDoList.ApproveFunding.PONumbers";
    public static final String STAGE_STATUS="stage.status";
    public static final String PROJECT_OWNER_ROLE="PROJECT_OWNER";
    public static final String APPROVERS="APPROVERS";
    public static final String JIVE_SYNCHRO_ADMIN_GROUP_NAME = "SYNCHRO_ADMIN";
    public static final String JIVE_SYNCHRO_MINI_ADMIN_GROUP_NAME = "SYNCHRO_MINI_ADMIN";
    public static final String SYNCHRO_DISCLAIMER_KEY = "disclaimer.pass.status";
    public static final String SYNCHRO_GLOBAL_PROJECT_CONTACT_GROUP_NAME = "SYNCHRO_GLOBAL_PROJECT_CONTACT";
    public static final String SYNCHRO_SYSTEM_OWNER_GROUP_NAME = "SYNCHRO_SYSTEM_OWNER";
    public static final String OSP_ORACLE_GROUP_NAME = "OSP_ORACLE_GROUP";
    public static final String OSP_SHARE_GROUP_NAME = "OSP_SHARE_GROUP";

    
    //currencies
    public static final Integer EXCHANGE_RATE_CURRENCY_ID = 87;
    public static final String PROJECT_WAIVER_CATALOGUE_PAGE_LIMIT = "grail.synchro.table.page.limit";
    
    public static final String REMEMBER_ME_PORTAL_COOKIE = "REMEMBER_ME_PORTAL";
    public static final String REMEMBER_ME_SPRING_COOKIE = "SPRING_SECURITY_REMEMBER_ME_COOKIE";

    public static final String MY_LIBRARY_PAGE_LIMIT = "grail.synchro.table.page.limit";
    public static final String STANDARD_REPORT_PAGE_LIMIT = "grail.synchro.table.page.limit";
    
    public static final String SYNCHRO_GROUP_PROP = "grail.synchro.group";
    public static final String SYNCHRO_ROLE_TYPE_PROP = "grail.synchro.role";
    
    public static final String SYNCHRO_PROJECT_OWNER_FIELDNAME = "owner";
    public static final int SYNCHRO_PROJECT_OWNER_FIELDID = 20;
    // Above Market - End Market Id
    public static final Long ABOVE_MARKET_MULTI_MARKET_ID = new Long("-100");
    
    public static final String SYNCHRO_DEFAULT_CATEGORY_ID = "grail.synchro.default.category";
    public static final String SYNCHRO_DEFAULT_BRAND_ID = "grail.synchro.default.brand";
    public static final String SYNCHRO_DEFAULT_METHODOLOGYTYPE_ID = "grail.synchro.default.methodology";
    public static final String SYNCHRO_DEFAULT_METHOGOLOGYGROUP_ID = "grail.synchro.default.methodologygroup";
    public static final String SYNCHRO_DEFAULT_PROPOSEDMETHOGOLOGY_ID = "grail.synchro.default.proposedmethodology";
    public static final String SYNCHRO_DEFAULT_CURRENCY_ID = "grail.default.currency";

    public static final String SYNCHRO_PORTAL_ENABLE_PROPERTY = "synchro.portal.enable";
    
    
    // Variables for Email Notifications
    public static final int PIT_STAGE = 0;
    public static final int PIB_STAGE = 1;
    public static final int PROPOSAL_STAGE = 2;
    public static final int PS_STAGE = 3;
    public static final int RS_STAGE = 4;
    public static final int PE_STAGE = 5;
    public static final int CHANGE_STATUS_STAGE = 6;
    public static final int PROCESS_WAIVER_STAGE = 7;
    public static final int PENDING_ACTIONS_STAGE = 8;
    
    public static final int PROJECT_CLOSE_STAGE = 9;
    
    public static final int EMAIL_TYPE_ACTION = 1;
    public static final int EMAIL_TYPE_NOTIFICATION = 2;

    public static final String SYNCHRO_ATTACHMENT_SIZE_MB = "grail.email.attachment.maxsize.mb";
    public static final int MAX_ATTACHMENT_SIZE_MB = 10;

    public static final String SYNCHRO_SPEND_REPORT_YEAR_FILTER_MIN = "synchro.spend.report.year.filter.minimum";

    public static final String SYNCHRO_PROJECT_BUDGET_YEAR_START = "synchro.project.budget.year.start";
    public static final String SYNCHRO_PROJECT_BUDGET_YEAR_END = "synchro.project.budget.year.end";
    
    public static final String SYNCHRO_AGENCY_BAT_USER_FIELDNAME = "agencybat";
    public static final int SYNCHRO_AGENCY_BAT_USER_FIELDID = 21;
    
    public static final String SYNCHRO_USER_FIELDNAME = "SYNCHRO";
    
    public static final String SYNCHRO_PROJECT_ON_TRACK = "On Track";
    public static final String SYNCHRO_PROJECT_NOT_ON_TRACK = "Not On Track";
    
    public static final int MULTI_BRAND_UI_VALUE = 30;
    public static final int NON_BRAND_UI_VALUE = 1;
    
    public static final int MULTI_BRAND_DB_VALUE = 1;
    public static final int NON_BRAND_DB_VALUE = 2;

}
