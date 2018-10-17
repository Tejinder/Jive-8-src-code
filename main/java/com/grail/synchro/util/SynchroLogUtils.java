package com.grail.synchro.util;


import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.jfree.util.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.ActivityLog;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsEndMarketDetails;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProjectSpecsReporting;
import com.grail.synchro.beans.ProposalEndMarketDetails;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ProposalReporting;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.SynchroToIRIS;
import com.grail.synchro.manager.ActivityLogManager;
import com.grail.synchro.object.LogData;
import com.grail.synchro.object.LogFieldObject;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.community.lifecycle.JiveApplication;
/**
 * Kanwar Grewal
 *
 */
public class SynchroLogUtils{

    private static ActivityLogManager activityLogManager;
    private static UserManager userManager;

    private static final Logger LOGGER = Logger.getLogger(SynchroLogUtils.class);

    public static ActivityLogManager getActivityLogManager() {
        if(activityLogManager==null)
        {
            activityLogManager = JiveApplication.getContext().getSpringBean("activityLogManager");
        }
        return activityLogManager;
    }


    public static UserManager getUserManager() {

        if(userManager==null)
        {
            userManager = JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;
    }


    public static void addLog(final Project project, final Enum type, final int activity, final String text)
    {
        //Create Project Activity
        if(SynchroGlobal.LogActivity.PIT_CREATE.ordinal()==type.ordinal())
        {
            if(project!=null)
            {
                try{
                    ActivityLog activityLog =  new ActivityLog();
                    activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
                    activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
                    activityLog.setType(activity);
                    activityLog.setStage(SynchroGlobal.LogProjectStage.CREATE.getId());
                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    LogData object = getJSONObject(text);
                    String json = ow.writeValueAsString(object);
                    activityLog.setJsonValue(json);
                    activityLog.setProjectID(project.getProjectID());
                    activityLog.setProjectName(project.getName());
                    if(JSONNotEmpty(activityLog.getJsonValue()))
                        getActivityLogManager().saveActivityLog(activityLog);
                }catch(JsonGenerationException jgenex)
                {
                    jgenex.printStackTrace();
                    Log.error("Error while Saving Logs for Project Create activity " + jgenex.getMessage());
                }
                catch(JsonMappingException jmapex)
                {
                    jmapex.printStackTrace();
                    Log.error("Error while Saving Logs for Project Create activity" + jmapex.getMessage());
                }
                catch(IOException ioex)
                {
                    ioex.printStackTrace();
                    Log.error("Error while Saving Logs for Project Create activity" + ioex.getMessage());
                }
            }

        }

        //Project DRAFT Activity
        if(SynchroGlobal.LogActivity.PIT_DRAFT.ordinal()==type.ordinal())
        {
            if(project!=null)
            {
                try{
                    ActivityLog activityLog =  new ActivityLog();
                    activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
                    activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
                    activityLog.setType(activity);
                    activityLog.setStage(SynchroGlobal.LogProjectStage.DRAFT.getId());
                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    LogData object = getJSONObject(text);
                    String json = ow.writeValueAsString(object);
                    activityLog.setJsonValue(json);
                    activityLog.setProjectID(project.getProjectID());
                    activityLog.setProjectName(project.getName());
                    if(JSONNotEmpty(activityLog.getJsonValue()))
                        getActivityLogManager().saveActivityLog(activityLog);
                }catch(JsonGenerationException jgenex)
                {
                    jgenex.printStackTrace();
                    Log.error("Error while Saving Logs for DRAFT Project activity" + jgenex.getMessage());
                }
                catch(JsonMappingException jmapex)
                {
                    jmapex.printStackTrace();
                    Log.error("Error while Saving Logs for DRAFT Project activity" + jmapex.getMessage());
                }
                catch(IOException ioex)
                {
                    ioex.printStackTrace();
                    Log.error("Error while Saving Logs for DRAFT Project activity" + ioex.getMessage());
                }
            }

        }

    }

    /**
     * Add Log without endmarket
     * @param portalName
     * @param pageType
     * @param activityType
     * @param stage
     * @param text
     * @param projectName
     * @param projectID
     * @param userID
     * @param endmarketID
     */

    public static void addLog(final String portalName, final int pageType, final int activityType, final int stage, final String text, String projectName, Long projectID, final Long userID)
    {

        if(projectID==null)
            projectID = -1L;
        if(projectName==null)
            projectName = StringUtils.EMPTY;

        try{
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(portalName);
            activityLog.setPage(pageType);
            activityLog.setType(activityType);
            activityLog.setStage(stage);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            LogData object = getJSONObject(text);
            String json = ow.writeValueAsString(object);
            activityLog.setJsonValue(json);
            activityLog.setProjectID(projectID);
            activityLog.setProjectName(projectName);
            activityLog.setUserID(userID);
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Logs " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }


    }

    /**
     * Add Log WITH Endmarket
     * @param portalName
     * @param pageType
     * @param activityType
     * @param stage
     * @param text
     * @param projectName
     * @param projectID
     * @param userID
     * @param endmarketID
     */
    public static void addLog(final String portalName, final int pageType, final int activityType, final int stage, final String text, String projectName, Long projectID, final Long userID, final Long endmarketID)
    {

        if(projectID==null)
            projectID = -1L;
        if(projectName==null)
            projectName = StringUtils.EMPTY;

        try{
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(portalName);
            activityLog.setPage(pageType);
            activityLog.setType(activityType);
            activityLog.setStage(stage);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            LogData object = getJSONObject(text);
            String json = ow.writeValueAsString(object);
            activityLog.setJsonValue(json);
            activityLog.setProjectID(projectID);
            activityLog.setProjectName(projectName);
            activityLog.setUserID(userID);
            activityLog.setEndmarketID(endmarketID);
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Logs " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }


    }

    /**
     * addNotificationLog method 
     * @param portalName
     * @param pageType
     * @param activityType
     * @param stage
     * @param text
     * @param projectName
     * @param projectID
     * @param userID
     * @param userNameList
     * @param notificationName
     */
    public static void addNotificationLog(final String portalName, final int pageType, final int activityType, final int stage, final String description, final String projectName, final Long projectID, final Long userID, final List<String> userNameList)
    {
        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(portalName);
        activityLog.setPage(pageType);
        activityLog.setType(activityType);
        activityLog.setStage(stage);
        activityLog.setJsonValue(generateNotificationJSONObject(userNameList, description));
        activityLog.setProjectID(projectID);
        activityLog.setProjectName(projectName);
        activityLog.setUserID(userID);
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);
    }


    public static void PIBWaiverSave(final PIBMethodologyWaiver pibMethodologyWaiver_DB, final PIBMethodologyWaiver pibMethodologyWaiver, final Project project, final Integer stage)
    {
        //Save PIB Methodology Waiver Activity

        if(pibMethodologyWaiver!=null)
        {
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            if(pibMethodologyWaiver_DB==null)
            {
                activityLog.setType(SynchroGlobal.Activity.ADD.getId());
            }
            else
            {
                activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            }
            activityLog.setStage(stage);
            activityLog.setJsonValue(generateWaiverJSONObject(pibMethodologyWaiver_DB, pibMethodologyWaiver));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }


    }


    public static void PIBMultiWaiverSave(final PIBMethodologyWaiver pibMethodologyWaiver_DB, final PIBMethodologyWaiver pibMethodologyWaiver, final Project project, final Integer stage, final Long endmarketID)
    {
        //Save PIB Methodology Waiver Activity

        if(pibMethodologyWaiver!=null)
        {
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            if(pibMethodologyWaiver_DB==null)
            {
                activityLog.setType(SynchroGlobal.Activity.ADD.getId());
            }
            else
            {
                activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            }
            activityLog.setStage(stage);
            activityLog.setJsonValue(generateWaiverJSONObject(pibMethodologyWaiver_DB, pibMethodologyWaiver));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            activityLog.setEndmarketID(endmarketID);
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }


    }

    public static void PIBKantarWaiverSave(final PIBMethodologyWaiver pibKantarMethodologyWaiver_DB, final PIBMethodologyWaiver pibKantarMethodologyWaiver, final Project project)
    {
        if(pibKantarMethodologyWaiver!=null)
        {
            //Update PIB Non-Kantar Waiver Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            if(pibKantarMethodologyWaiver_DB==null)
            {
                activityLog.setType(SynchroGlobal.Activity.ADD.getId());
            }
            else
            {
                activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            }

            activityLog.setStage(SynchroGlobal.LogProjectStage.PIB.getId());
            activityLog.setJsonValue(generateKantarWaiverJSONObject(pibKantarMethodologyWaiver_DB, pibKantarMethodologyWaiver));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }


    }

    /**
     * PIB Log Util
     * @param projectInitiation_DB
     * @param projectInitiation
     * @param project_DB
     * @param project
     * @param pibReporting_DB
     * @param endMarketDetails_DB
     * @param pibStakeholderList_DB
     */
    public static void PIBSave(final ProjectInitiation projectInitiation_DB, final ProjectInitiation projectInitiation, final Project project_DB, final Project project, final PIBReporting pibReporting_DB, final List<EndMarketInvestmentDetail> endMarketDetails_DB, final PIBStakeholderList pibStakeholderList_DB)
    {
        if(projectInitiation==null || project == null)
            return;

        if(projectInitiation_DB!=null)
        {
            //Update PIB Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PIB.getId());
            activityLog.setJsonValue(generatePIBUpdateJSONObject(projectInitiation_DB, projectInitiation, project_DB, project, pibReporting_DB, endMarketDetails_DB, pibStakeholderList_DB, -1L, false));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }
        else
        {
            //Save Project Details
            ActivityLog projectActivityLog =  new ActivityLog();
            projectActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            projectActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            projectActivityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            projectActivityLog.setStage(SynchroGlobal.LogProjectStage.PIB.getId());
            projectActivityLog.setJsonValue(generateProjectUpdateJSONObject(projectInitiation, project_DB, project, endMarketDetails_DB, false));
            projectActivityLog.setProjectID(project.getProjectID());
            projectActivityLog.setProjectName(project.getName());
            if(JSONNotEmpty(projectActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(projectActivityLog);


            //Save PIB Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.ADD.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PIB.getId());
            activityLog.setJsonValue(generatePIBSaveJSONObject(projectInitiation, false));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }

        //Legal Approvers Check

        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
        activityLog.setType(SynchroGlobal.Activity.APPROVE.getId());
        activityLog.setStage(SynchroGlobal.LogProjectStage.PIB.getId());
        activityLog.setJsonValue(generatePIBLegalApproverJSONObject(projectInitiation, projectInitiation_DB));
        activityLog.setProjectID(project.getProjectID());
        activityLog.setProjectName(project.getName());
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);
    }

    public static void PITSave(final Project project_DB, final Project project, final EndMarketInvestmentDetail endMarketDetail_DB, final EndMarketInvestmentDetail endMarketDetail)
    {
        if(project_DB==null || project == null)
            return;

        //Update PIT Activity
        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
        activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
        activityLog.setStage(SynchroGlobal.LogProjectStage.VIEWEDITPIT.getId());
        activityLog.setJsonValue(generatePITUpdateJSONObject(project_DB, project, endMarketDetail_DB, endMarketDetail));
        activityLog.setProjectID(project.getProjectID());
        activityLog.setProjectName(project.getName());
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);
    }

    public static void PITMultiSave(final Project project_DB, final Project project)
    {
        if(project_DB==null || project == null)
            return;

        //Update PIT Activity
        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
        activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
        activityLog.setStage(SynchroGlobal.LogProjectStage.VIEWEDITPIT.getId());
        activityLog.setJsonValue(generatePITMultimarketUpdateJSONObject(project_DB, project));
        activityLog.setProjectID(project.getProjectID());
        activityLog.setProjectName(project.getName());
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);
    }

    /**
     * Multimarket PIB Save
     */
    public static void PIBMultiSave(final ProjectInitiation projectInitiation_DB, final ProjectInitiation projectInitiation, final Project project_DB, final Project project,
                                    final PIBReporting pibReporting_DB, final List<EndMarketInvestmentDetail> endMarketDetails_DB, final PIBStakeholderList pibStakeholderList_DB, final Long endmarketID)
    {
        if(projectInitiation==null || project == null)
            return;

        if(projectInitiation_DB!=null)
        {
            //Update PIB Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PIB.getId());
            activityLog.setJsonValue(generatePIBUpdateJSONObject(projectInitiation_DB, projectInitiation, project_DB, project, pibReporting_DB, endMarketDetails_DB, pibStakeholderList_DB, endmarketID, true));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            activityLog.setEndmarketID(endmarketID);
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);

            if(pibStakeholderList_DB==null && !isEqual(endmarketID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                //Add PIB Stakeholders Activity for Endmarkets
                ActivityLog activityStakeholdersLog =  new ActivityLog();
                activityStakeholdersLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
                activityStakeholdersLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
                activityStakeholdersLog.setType(SynchroGlobal.Activity.ADD.getId());
                activityStakeholdersLog.setStage(SynchroGlobal.LogProjectStage.PIB.getId());
                activityStakeholdersLog.setJsonValue(generateEndMarketStakeholdersAddJSONObject(projectInitiation));
                activityStakeholdersLog.setProjectID(project.getProjectID());
                activityStakeholdersLog.setProjectName(project.getName());
                activityStakeholdersLog.setEndmarketID(endmarketID);
                if(JSONNotEmpty(activityStakeholdersLog.getJsonValue()))
                    getActivityLogManager().saveActivityLog(activityStakeholdersLog);

            }
        }
        else
        {
            //Save Project Details
            ActivityLog projectActivityLog =  new ActivityLog();
            projectActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            projectActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            projectActivityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            projectActivityLog.setStage(SynchroGlobal.LogProjectStage.PIB.getId());
            projectActivityLog.setJsonValue(generateProjectUpdateJSONObject(projectInitiation, project_DB, project, endMarketDetails_DB, true));
            projectActivityLog.setProjectID(project.getProjectID());
            projectActivityLog.setProjectName(project.getName());
            projectActivityLog.setEndmarketID(endmarketID);
            if(JSONNotEmpty(projectActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(projectActivityLog);


            //Save PIB Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.ADD.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PIB.getId());
            activityLog.setJsonValue(generatePIBSaveJSONObject(projectInitiation, true));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            activityLog.setEndmarketID(endmarketID);
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }

        //Legal Approvers Check

        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
        activityLog.setType(SynchroGlobal.Activity.APPROVE.getId());
        activityLog.setStage(SynchroGlobal.LogProjectStage.PIB.getId());
        activityLog.setJsonValue(generatePIBLegalApproverJSONObject(projectInitiation, projectInitiation_DB));
        activityLog.setProjectID(project.getProjectID());
        activityLog.setProjectName(project.getName());
        activityLog.setEndmarketID(endmarketID);
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);
    }

    public static void IRISSave(final Project project, final SynchroToIRIS synchroToIRIS, final SynchroToIRIS synchroToIRIS_DB)
    {
        if(synchroToIRIS_DB==null || project == null)
            return;


        ActivityLog activityLogMain =  new ActivityLog();
        activityLogMain.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLogMain.setPage(SynchroGlobal.PageType.PROJECT.getId());
        activityLogMain.setType(SynchroGlobal.Activity.EDIT.getId());
        activityLogMain.setStage(SynchroGlobal.LogProjectStage.SYNCHRO2IRIS.getId());
        activityLogMain.setJsonValue(generateIRISCheckboxJSONObject(synchroToIRIS, synchroToIRIS_DB));
        activityLogMain.setProjectID(project.getProjectID());
        activityLogMain.setProjectName(project.getName());
        if(JSONNotEmpty(activityLogMain.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLogMain);

        //Logs for Summary for IRIS Option Rationale
        if(synchroToIRIS.getIrisSummaryRequired()!=null && synchroToIRIS.getIrisSummaryRequired().intValue()==2)
        {
            ActivityLog activityLogSummaryOptional =  new ActivityLog();
            activityLogSummaryOptional.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLogSummaryOptional.setPage(SynchroGlobal.PageType.PROJECT.getId());
            if(synchroToIRIS_DB==null || !isEqual(synchroToIRIS.getIrisSummaryRequired(), synchroToIRIS_DB.getIrisSummaryRequired()))
            {
                activityLogSummaryOptional.setType(SynchroGlobal.Activity.ADD.getId());
                activityLogSummaryOptional.setJsonValue(generateIRISSumryOptJSONObject(synchroToIRIS, synchroToIRIS_DB, false));
            }
            else
            {
                activityLogSummaryOptional.setType(SynchroGlobal.Activity.EDIT.getId());
                activityLogSummaryOptional.setJsonValue(generateIRISSumryOptJSONObject(synchroToIRIS, synchroToIRIS_DB, true));
            }

            activityLogSummaryOptional.setStage(SynchroGlobal.LogProjectStage.SYNCHRO2IRIS.getId());
            activityLogSummaryOptional.setProjectID(project.getProjectID());
            activityLogSummaryOptional.setProjectName(project.getName());
            if(JSONNotEmpty(activityLogSummaryOptional.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLogSummaryOptional);
        }


        if(synchroToIRIS_DB!=null && synchroToIRIS_DB.getTags()!=null)
        {
            //Update IRIS Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.SYNCHRO2IRIS.getId());
            activityLog.setJsonValue(generateIRISUpdateJSONObject(synchroToIRIS, synchroToIRIS_DB, true));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }
        else
        {
            //Update existing IRIS Details
            ActivityLog projectActivityLog =  new ActivityLog();
            projectActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            projectActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            projectActivityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            projectActivityLog.setStage(SynchroGlobal.LogProjectStage.SYNCHRO2IRIS.getId());
            projectActivityLog.setJsonValue(generateIRISUpdateJSONObject(synchroToIRIS, synchroToIRIS_DB, false));
            projectActivityLog.setProjectID(project.getProjectID());
            projectActivityLog.setProjectName(project.getName());
            if(JSONNotEmpty(projectActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(projectActivityLog);


            //Add new IRIS Details
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.ADD.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.SYNCHRO2IRIS.getId());
            activityLog.setJsonValue(generateIRISSaveJSONObject(synchroToIRIS));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }
    }


    /**
     * Multimarket PIB Endmarket Update
     */
    public static void PIBCountryUpdate(final Project project, final List<Long> endmarketIDs_DB, final List<Long> endmarketIDs)
    {
        //Save PIB Endmarket Activity
        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
        activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
        activityLog.setStage(SynchroGlobal.LogProjectStage.PIB.getId());
        activityLog.setJsonValue(generateCountryUpdateJSONObject(endmarketIDs_DB, endmarketIDs));
        activityLog.setProjectID(project.getProjectID());
        activityLog.setProjectName(project.getName());
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);
    }
    /**
     * Multimarket Project Specs Endmarket Update
     */

    public static void ProjectSpecsCountryUpdate(final Project project, final List<Long> endmarketIDs_DB, final List<Long> endmarketIDs)
    {
        //Save PIB Endmarket Activity
        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
        activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
        activityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId());
        activityLog.setJsonValue(generateCountryUpdateJSONObject(endmarketIDs_DB, endmarketIDs));
        activityLog.setProjectID(project.getProjectID());
        activityLog.setProjectName(project.getName());
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);
    }

    /**
     * Proposal Logs Util
     * @param projectInitiation_DB
     * @param projectInitiation
     * @param project_DB
     * @param project
     * @param pibReporting_DB
     * @param endMarketDetails_DB
     * @param pibStakeholderList_DB
     */
    public static void ProposalSave(final ProposalInitiation proposalInitiation_DB, final ProposalInitiation proposalInitiation, final Project project, final ProposalReporting proposalReporting_DB, final ProposalEndMarketDetails proposalEMDetails, final ProposalEndMarketDetails proposalEMDetails_DB)
    {
        if(proposalInitiation==null || project == null)
            return;

        if(proposalEMDetails_DB!=null)
        {
            //Update Proposal Stage Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PROPOSAL.getId());
            activityLog.setJsonValue(generateProposalAllUpdateJSONObject(proposalInitiation_DB, proposalInitiation, proposalReporting_DB, proposalEMDetails, proposalEMDetails_DB));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }
        else
        {
            //Update Proposal Stage Details
            ActivityLog projectActivityLog =  new ActivityLog();
            projectActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            projectActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            projectActivityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            projectActivityLog.setStage(SynchroGlobal.LogProjectStage.PROPOSAL.getId());
            projectActivityLog.setJsonValue(generateProposalUpdateJSONObject(proposalInitiation_DB, proposalInitiation, proposalReporting_DB));
            projectActivityLog.setProjectID(project.getProjectID());
            projectActivityLog.setProjectName(project.getName());
            if(JSONNotEmpty(projectActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(projectActivityLog);


            //ADD Proposal Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.ADD.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PROPOSAL.getId());
            activityLog.setJsonValue(generateProposalEMJSONObject(proposalInitiation, proposalEMDetails));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }
    }

    public static void ProposalMultiSave(final ProposalInitiation proposalInitiation_DB, final ProposalInitiation proposalInitiation, final Project project,
                                         final ProposalReporting proposalReporting_DB, final Map<Long, ProposalEndMarketDetails> proposalEMDetailsMap,
                                         final Map<Long, ProposalEndMarketDetails> proposalEMDetailsMap_DB)
    {
        if(proposalInitiation==null || project == null)
            return;

        if(proposalEMDetailsMap_DB!=null)
        {
            //On Second or more time Save of Proposal Stage
            //Update Proposal Stage Details for existing fields
            ActivityLog projectActivityLog =  new ActivityLog();
            projectActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            projectActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            projectActivityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            projectActivityLog.setStage(SynchroGlobal.LogProjectStage.PROPOSAL.getId());
            projectActivityLog.setJsonValue(generateProposalMultiMarketAllUpdateJSONObject(proposalInitiation_DB, proposalInitiation, proposalReporting_DB));
            projectActivityLog.setProjectID(project.getProjectID());
            projectActivityLog.setProjectName(project.getName());
            if(JSONNotEmpty(projectActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(projectActivityLog);

            //Separate piece of code for capturing each EDIT endmarket fields separately for endmarket based categorization
            if(proposalEMDetailsMap!=null)
            {
                for(Long proposalEMID : proposalEMDetailsMap.keySet())
                {
                    ProposalEndMarketDetails proposalEMDetails = proposalEMDetailsMap.get(proposalEMID);
                    ProposalEndMarketDetails proposalEMDetails_DB = null;
                    if(proposalEMDetailsMap_DB.containsKey(proposalEMID))
                    {
                        proposalEMDetails_DB = proposalEMDetailsMap_DB.get(proposalEMID);
                    }
                    ActivityLog activityLog =  new ActivityLog();
                    activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
                    activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
                    activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
                    activityLog.setStage(SynchroGlobal.LogProjectStage.PROPOSAL.getId());
                    activityLog.setJsonValue(generateProposalEMUpdateJSONObject(proposalEMDetails, proposalEMDetails_DB));
                    activityLog.setProjectID(project.getProjectID());
                    activityLog.setProjectName(project.getName());
                    activityLog.setEndmarketID(proposalEMID);
                    if(JSONNotEmpty(activityLog.getJsonValue()))
                        getActivityLogManager().saveActivityLog(activityLog);
                }
            }
        }
        else
        {
            //On first Save of Proposal Stage
            //Update Proposal Stage Details for existing fields
            ActivityLog projectActivityLog =  new ActivityLog();
            projectActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            projectActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            projectActivityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            projectActivityLog.setStage(SynchroGlobal.LogProjectStage.PROPOSAL.getId());
            projectActivityLog.setJsonValue(generateProposalUpdateJSONObject(proposalInitiation_DB, proposalInitiation, proposalReporting_DB));
            projectActivityLog.setProjectID(project.getProjectID());
            projectActivityLog.setProjectName(project.getName());
            if(JSONNotEmpty(projectActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(projectActivityLog);

            //ADD Proposal Activity && Proposal and Cost Template for new fields
            ActivityLog fieldActivityLog =  new ActivityLog();
            fieldActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            fieldActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            fieldActivityLog.setType(SynchroGlobal.Activity.ADD.getId());
            fieldActivityLog.setStage(SynchroGlobal.LogProjectStage.PROPOSAL.getId());
            fieldActivityLog.setJsonValue(generateProposalFieldAddJSONObject(proposalInitiation));
            fieldActivityLog.setProjectID(project.getProjectID());
            fieldActivityLog.setProjectName(project.getName());
            if(JSONNotEmpty(fieldActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(fieldActivityLog);


            if(proposalEMDetailsMap!=null)
            {
                for(Long proposalEMID : proposalEMDetailsMap.keySet())
                {
                    ProposalEndMarketDetails proposalEMDetails = proposalEMDetailsMap.get(proposalEMID);
                    ActivityLog activityLog =  new ActivityLog();
                    activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
                    activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
                    activityLog.setType(SynchroGlobal.Activity.ADD.getId());
                    activityLog.setStage(SynchroGlobal.LogProjectStage.PROPOSAL.getId());
                    activityLog.setJsonValue(generateProposalMultiMarketEMJSONObject(proposalEMDetails));
                    activityLog.setProjectID(project.getProjectID());
                    activityLog.setProjectName(project.getName());
                    activityLog.setEndmarketID(proposalEMID);
                    if(JSONNotEmpty(activityLog.getJsonValue()))
                        getActivityLogManager().saveActivityLog(activityLog);
                }
            }

        }
    }


    /**
     * Project Specs Audit Logs
     * @param proposalInitiation_DB
     * @param proposalInitiation
     * @param project
     * @param proposalReporting_DB
     * @param proposalEMDetails
     * @param proposalEMDetails_DB
     */
    public static void ProjectSpecsSave(final Project project, final ProjectSpecsInitiation projectSpecsInitiation_DB, final ProjectSpecsInitiation projectSpecsInitiation,
                                        final ProjectSpecsEndMarketDetails projectSpecsEMDetails_DB, final ProjectSpecsEndMarketDetails projectSpecsEMDetails,
                                        final ProjectSpecsReporting projectSpecsReporting_DB, final List<EndMarketInvestmentDetail> endMarketDetails_DB, final ProjectInitiation projectInitiation_DB)
    {
        if(projectSpecsInitiation==null || project == null)
            return;

        if(projectSpecsInitiation_DB.getPoNumber()!=null)
        {
            //Update Project Specs Stage Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId());
            activityLog.setJsonValue(generateProjectSpecsAllUpdateJSONObject(projectSpecsInitiation_DB, projectSpecsInitiation, projectSpecsEMDetails_DB, projectSpecsEMDetails, projectSpecsReporting_DB, endMarketDetails_DB, projectInitiation_DB));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }
        else
        {
            //Update Project Specs Stage Details
            ActivityLog projectActivityLog =  new ActivityLog();
            projectActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            projectActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            projectActivityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            projectActivityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId());
            projectActivityLog.setJsonValue(generateProjectSpecsUpdateJSONObject(projectSpecsInitiation_DB, projectSpecsInitiation, projectSpecsEMDetails_DB, projectSpecsEMDetails, projectSpecsReporting_DB,  endMarketDetails_DB, projectInitiation_DB));
            projectActivityLog.setProjectID(project.getProjectID());
            projectActivityLog.setProjectName(project.getName());
            if(JSONNotEmpty(projectActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(projectActivityLog);


            //ADD Project Specs Stage-Specific Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.ADD.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId());
            activityLog.setJsonValue(generateProjectSpecsSpecificJSONObject(projectSpecsInitiation));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }

        //Check for Legal Approvers		
        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
        activityLog.setType(SynchroGlobal.Activity.APPROVE.getId());
        activityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId());
        activityLog.setJsonValue(generateProjectSpecsLegalApproverJSONObject(projectSpecsInitiation, projectSpecsInitiation_DB));
        activityLog.setProjectID(project.getProjectID());
        activityLog.setProjectName(project.getName());
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);

    }

    /**
     * Multi Market ProjectSpecsMultiMarketSave
     * @param project_DB
     * @param project
     * @param projectSpecsInitiation_DB
     * @param projectSpecsInitiation
     * @param projectSpecsEMDetails_DB
     * @param projectSpecsEMDetails
     * @param projectSpecsReporting_DB
     * @param endMarketDetails_DB
     * @param projectInitiation_DB
     */
    public static void ProjectSpecsMultiMarketSave(final Project project,
                                                   final ProjectSpecsInitiation projectSpecsInitiation_DB, final ProjectSpecsInitiation projectSpecsInitiation,
                                                   final ProjectSpecsEndMarketDetails projectSpecsEMDetails_DB, final ProjectSpecsEndMarketDetails projectSpecsEMDetails,
                                                   final ProjectSpecsReporting projectSpecsReporting_DB, final List<EndMarketInvestmentDetail> endMarketDetails_DB,
                                                   final ProjectInitiation projectInitiation_DB, final Long endmarketID, final User user)
    {
        if(projectSpecsInitiation==null || project == null)
            return;

        if(projectSpecsInitiation_DB.getPoNumber()!=null)
        {
            //Update Project Specs Stage Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId());
            activityLog.setJsonValue(generateProjectSpecsMultiMarketAllUpdateJSONObject(projectSpecsInitiation_DB, projectSpecsInitiation, projectSpecsEMDetails_DB, projectSpecsEMDetails, projectSpecsReporting_DB, endMarketDetails_DB, projectInitiation_DB, endmarketID, user));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            activityLog.setEndmarketID(endmarketID);
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }
        else
        {
            //Update Project Specs Stage Details
            ActivityLog projectActivityLog =  new ActivityLog();
            projectActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            projectActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            projectActivityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            projectActivityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId());
            projectActivityLog.setJsonValue(generateProjectSpecsMultiMarketUpdateJSONObject(projectSpecsInitiation_DB, projectSpecsInitiation, projectSpecsEMDetails_DB, projectSpecsEMDetails, projectSpecsReporting_DB,  endMarketDetails_DB, projectInitiation_DB, endmarketID, user));
            projectActivityLog.setProjectID(project.getProjectID());
            projectActivityLog.setProjectName(project.getName());
            projectActivityLog.setEndmarketID(endmarketID);
            if(JSONNotEmpty(projectActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(projectActivityLog);


            //ADD Project Specs Stage-Specific Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.ADD.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId());
            activityLog.setJsonValue(generateProjectSpecsSpecificJSONObject(projectSpecsInitiation));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            activityLog.setEndmarketID(endmarketID);
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }

        //Check for Legal Approvers		
        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
        activityLog.setType(SynchroGlobal.Activity.APPROVE.getId());
        activityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId());
        activityLog.setJsonValue(generateProjectSpecsLegalApproverJSONObject(projectSpecsInitiation, projectSpecsInitiation_DB));
        activityLog.setProjectID(project.getProjectID());
        activityLog.setProjectName(project.getName());
        activityLog.setEndmarketID(endmarketID);
        activityLog.setEndmarketID(endmarketID);
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);

    }

    /** Single Market
     * Report Summary Logs
     * @param project
     * @param reportSummaryInitiation
     * @param reportSummaryInitiation_DB
     */
    public static void ReportSave(final Project project, final ReportSummaryInitiation reportSummaryInitiation, final ReportSummaryInitiation reportSummaryInitiation_DB)
    {
        if(reportSummaryInitiation==null || project == null)
            return;

        if(reportSummaryInitiation_DB!=null && reportSummaryInitiation_DB.getFullReport()!=null)
        {
            //Update Report Summary Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId());
            activityLog.setJsonValue(generateReportUpdateJSONObject(reportSummaryInitiation, reportSummaryInitiation_DB));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }
        else
        {
            //Update Project Specs Stage Details
            ActivityLog projectActivityLog =  new ActivityLog();
            projectActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            projectActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            projectActivityLog.setType(SynchroGlobal.Activity.ADD.getId());
            projectActivityLog.setStage(SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId());
            projectActivityLog.setJsonValue(generateReportJSONObject(reportSummaryInitiation));
            projectActivityLog.setProjectID(project.getProjectID());
            projectActivityLog.setProjectName(project.getName());
            if(JSONNotEmpty(projectActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(projectActivityLog);

        }

        //Check for Legal Approvers		
        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
        activityLog.setType(SynchroGlobal.Activity.APPROVE.getId());
        activityLog.setStage(SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId());
        activityLog.setJsonValue(generateRepSummaryLegalApproverJSONObject(reportSummaryInitiation, reportSummaryInitiation_DB));
        activityLog.setProjectID(project.getProjectID());
        activityLog.setProjectName(project.getName());
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);


    }


    /** Multi Market
     * Report Summary Logs
     * @param project
     * @param reportSummaryInitiation
     * @param reportSummaryInitiation_DB
     * @param endmarketID
     */
    public static void ReportMultiMarketSave(final Project project, final ReportSummaryInitiation reportSummaryInitiation, final ReportSummaryInitiation reportSummaryInitiation_DB, final Long endmarketID)
    {
        if(reportSummaryInitiation==null || project == null)
            return;

        if(reportSummaryInitiation_DB!=null && reportSummaryInitiation_DB.getFullReport()!=null)
        {
            //Update Report Summary Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId());
            activityLog.setJsonValue(generateReportUpdateJSONObject(reportSummaryInitiation, reportSummaryInitiation_DB));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            activityLog.setEndmarketID(endmarketID);
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }
        else
        {
            //Update Project Specs Stage Details
            ActivityLog projectActivityLog =  new ActivityLog();
            projectActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            projectActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            projectActivityLog.setType(SynchroGlobal.Activity.ADD.getId());
            projectActivityLog.setStage(SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId());
            projectActivityLog.setJsonValue(generateReportJSONObject(reportSummaryInitiation));
            projectActivityLog.setProjectID(project.getProjectID());
            projectActivityLog.setProjectName(project.getName());
            projectActivityLog.setEndmarketID(endmarketID);
            if(JSONNotEmpty(projectActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(projectActivityLog);

        }

        //Check for Legal Approvers		
        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
        activityLog.setType(SynchroGlobal.Activity.APPROVE.getId());
        activityLog.setStage(SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId());
        activityLog.setJsonValue(generateRepSummaryLegalApproverJSONObject(reportSummaryInitiation, reportSummaryInitiation_DB));
        activityLog.setProjectID(project.getProjectID());
        activityLog.setProjectName(project.getName());
        activityLog.setEndmarketID(endmarketID);
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);


    }

    /**
     * Single market Project Evaluation Audit logs
     * @param project
     * @param projectEvaluationInitiation
     * @param projectEvaluationInitiation_DB
     */
    public static void EvaluationSave(final Project project, final ProjectEvaluationInitiation projectEvaluationInitiation, final ProjectEvaluationInitiation projectEvaluationInitiation_DB)
    {
        if(projectEvaluationInitiation==null || project == null)
            return;

        if(projectEvaluationInitiation_DB!=null)
        {
            //Update Project Evaluation Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_EVALUATION.getId());
            activityLog.setJsonValue(generateEvaluationUpdateJSONObject(projectEvaluationInitiation, projectEvaluationInitiation_DB));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }
        else
        {
            //Add Project Evaluation Activity
            ActivityLog projectActivityLog =  new ActivityLog();
            projectActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            projectActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            projectActivityLog.setType(SynchroGlobal.Activity.ADD.getId());
            projectActivityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_EVALUATION.getId());
            projectActivityLog.setJsonValue(generateEvaluationJSONObject(projectEvaluationInitiation));
            projectActivityLog.setProjectID(project.getProjectID());
            projectActivityLog.setProjectName(project.getName());
            if(JSONNotEmpty(projectActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(projectActivityLog);

        }

        /*
		//Check for Legal Approvers		
		ActivityLog activityLog =  new ActivityLog();
		activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
		activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
		activityLog.setType(SynchroGlobal.Activity.APPROVE.getId());
		activityLog.setStage(SynchroGlobal.LogProjectStage.REPORT_SUMMARY.getId());
		activityLog.setJsonValue(generateRepSummaryLegalApproverJSONObject(reportSummaryInitiation, reportSummaryInitiation_DB));
		activityLog.setProjectID(project.getProjectID());
		activityLog.setProjectName(project.getName());
		getActivityLogManager().saveActivityLog(activityLog);
		*/

    }


    /**
     * Multi market Project Evaluation Audit logs
     * @param project
     * @param projectEvaluationInitiation
     * @param projectEvaluationInitiation_DB
     * @param endmarketID
     */
    public static void EvaluationMultiMarketSave(final Project project, final ProjectEvaluationInitiation projectEvaluationInitiation, final ProjectEvaluationInitiation projectEvaluationInitiation_DB, final Long endmarketID)
    {
        if(projectEvaluationInitiation==null || project == null)
            return;

        if(projectEvaluationInitiation_DB!=null)
        {
            //Update Project Evaluation Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_EVALUATION.getId());
            activityLog.setJsonValue(generateEvaluationUpdateJSONObject(projectEvaluationInitiation, projectEvaluationInitiation_DB));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            activityLog.setEndmarketID(endmarketID);
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }
        else
        {
            //Add Project Evaluation Activity
            ActivityLog projectActivityLog =  new ActivityLog();
            projectActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            projectActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            projectActivityLog.setType(SynchroGlobal.Activity.ADD.getId());
            projectActivityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_EVALUATION.getId());
            projectActivityLog.setJsonValue(generateEvaluationJSONObject(projectEvaluationInitiation));
            projectActivityLog.setProjectID(project.getProjectID());
            projectActivityLog.setProjectName(project.getName());
            projectActivityLog.setEndmarketID(endmarketID);
            if(JSONNotEmpty(projectActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(projectActivityLog);

        }

    }
    /**
     * Project Specs Change Project Fieldwork dates audit logs
     * @param project
     * @param projectSpecsEMDetails
     * @param projectSpecsEMDetails_DB
     */
    public static void ProjectFieldworkSave(final Project project, final ProjectSpecsEndMarketDetails projectSpecsEMDetails, final ProjectSpecsEndMarketDetails projectSpecsEMDetails_DB)
    {
        if(projectSpecsEMDetails==null || project == null)
            return;

        if(projectSpecsEMDetails!=null)
        {
            //Update Project Specs Fieldwork Activity
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.FWCHANGE.getId());
            activityLog.setJsonValue(generateFieldworkJSONObject(projectSpecsEMDetails, projectSpecsEMDetails_DB, false));
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());
            if(JSONNotEmpty(activityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(activityLog);
        }

    }


    public static void ProjectFieldworkMultimarketSave(final Project project, final List<ProjectSpecsEndMarketDetails> psEMList_DB, final List<ProjectSpecsEndMarketDetails> psEMList,
                                                       final BigDecimal aboveMarketFinalCost, final Integer aboveMarketFinalCostType, final BigDecimal aboveMarketFinalCost_DB, final Integer aboveMarketFinalCostType_DB)
    {
        if(psEMList==null || project == null)
            return;

        if(psEMList_DB!=null)
        {
            ActivityLog aboveMarketActivityLog =  new ActivityLog();
            aboveMarketActivityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            aboveMarketActivityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            aboveMarketActivityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            aboveMarketActivityLog.setStage(SynchroGlobal.LogProjectStage.FWCHANGE.getId());
            aboveMarketActivityLog.setJsonValue(generateFieldworkAboveMarketJSONObject(psEMList, psEMList_DB, aboveMarketFinalCost, aboveMarketFinalCostType, aboveMarketFinalCost_DB, aboveMarketFinalCostType_DB));
            aboveMarketActivityLog.setProjectID(project.getProjectID());
            aboveMarketActivityLog.setProjectName(project.getName());
            if(JSONNotEmpty(aboveMarketActivityLog.getJsonValue()))
                getActivityLogManager().saveActivityLog(aboveMarketActivityLog);

            //EDIT/Update Fieldwork
            ActivityLog activityLog =  new ActivityLog();
            activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
            activityLog.setPage(SynchroGlobal.PageType.PROJECT.getId());
            activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
            activityLog.setStage(SynchroGlobal.LogProjectStage.PROJECT_SPECS.getId());
            activityLog.setProjectID(project.getProjectID());
            activityLog.setProjectName(project.getName());

            for(ProjectSpecsEndMarketDetails psEM : psEMList)
            {
                ProjectSpecsEndMarketDetails psEM_DB = null;
                for(ProjectSpecsEndMarketDetails psEM_Itr : psEMList_DB)
                {
                    if(psEM.getEndMarketID().intValue()==psEM_Itr.getEndMarketID().intValue())
                    {
                        psEM_DB = psEM_Itr;
                        break;
                    }
                }
                activityLog.setJsonValue(generateFieldworkJSONObject(psEM, psEM_DB, true));
                activityLog.setEndmarketID(psEM.getEndMarketID());
                if(JSONNotEmpty(activityLog.getJsonValue()))
                    getActivityLogManager().saveActivityLog(activityLog);
            }


        }
        else
        {
            //ADD Fieldwork

        }
    }

    public static void stakeholdersSave(final String spi, final String legal, final String product, final String agency, final String projectName, final Long projectID)
    {
        //Add Stakeholders Activity
        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLog.setPage(SynchroGlobal.PageType.MYDASHBOARD.getId());
        activityLog.setType(SynchroGlobal.Activity.ADD.getId());
        activityLog.setStage(SynchroGlobal.LogProjectStage.CHNGCONTACTOWNERS.getId());
        activityLog.setJsonValue(generateAddStakeholdersJSONObject(spi, legal, product, agency));
        activityLog.setProjectID(projectID);
        activityLog.setProjectName(projectName);
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);
    }

    public static String generateAddStakeholdersJSONObject(final String spi, final String legal, final String product, final String agency)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(spi!=null && !spi.equals(""))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.SPICONTACT.getDescription()));
        }
        if(legal!=null && !legal.equals(""))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.LEGAL.getDescription()));
        }
        if(product!=null && !product.equals(""))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PRODUCT.getDescription()));
        }
        if(agency!=null && !agency.equals(""))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.AGENCY.getDescription()));
        }

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }



    public static void stakeholdersEdit(final String spi, final Long spi_DB, final String owner, final Long owner_DB, final String agency,
                                        final Long agency_DB, final String projectName, final Long projectID)
    {
        //Edit Stakeholders Activity
        ActivityLog activityLog =  new ActivityLog();
        activityLog.setPortal(SynchroGlobal.PortalType.SYNCHRO.getDescription());
        activityLog.setPage(SynchroGlobal.PageType.MYDASHBOARD.getId());
        activityLog.setType(SynchroGlobal.Activity.EDIT.getId());
        activityLog.setStage(SynchroGlobal.LogProjectStage.CHNGCONTACTOWNERS.getId());
        activityLog.setJsonValue(generateEditStakeholdersJSONObject(spi, spi_DB, owner, owner_DB, agency, agency_DB));
        activityLog.setProjectID(projectID);
        activityLog.setProjectName(projectName);
        if(JSONNotEmpty(activityLog.getJsonValue()))
            getActivityLogManager().saveActivityLog(activityLog);
    }

    public static String generateEditStakeholdersJSONObject(final String spi, final Long spi_DB, final String owner, final Long owner_DB, final String agency, final Long agency_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(spi!=null && !spi.equals("") && isEqual(spi, spi_DB))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.SPICONTACT.getDescription(),
                    getUserName(spi), getUserName(spi_DB)));
        }
        if(owner!=null && !owner.equals("") && !isEqual(owner, owner_DB))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(),
                    getUserName(owner), getUserName(owner_DB)));
        }
        if(agency!=null && !agency.equals("") && !isEqual(agency, agency_DB))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.AGENCY.getDescription(),
                    getUserName(agency), getUserName(agency_DB)));
        }

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }



    public static String generatePIBSaveJSONObject(final ProjectInitiation projectInitiation, final Boolean isMultimarket)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(projectInitiation!=null)
        {
            //Latest Estimate
            if(projectInitiation.getLatestEstimate()!=null)
            {
                String currency = "";
                if(projectInitiation.getLatestEstimateType()!=null && SynchroGlobal.getCurrencies().containsKey(projectInitiation.getLatestEstimateType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(projectInitiation.getLatestEstimateType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.LATESTESTM.getDescription(), ObjStr(projectInitiation.getLatestEstimate()), currency));
            }


            //Has Tendering Process
            if(projectInitiation.getHasTenderingProcess()!=null && projectInitiation.getHasTenderingProcess().intValue()==1)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.TENDERPROCESS.getDescription(),"Yes"));
            }

            //Fieldwork Cost
            if(projectInitiation.getHasTenderingProcess()!=null && projectInitiation.getHasTenderingProcess().intValue()==1)
            {
                if(projectInitiation.getFieldworkCost()!=null)
                {
                    String currency = "";
                    if(projectInitiation.getFieldworkCostCurrency()!=null && SynchroGlobal.getCurrencies().containsKey(projectInitiation.getFieldworkCostCurrency().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectInitiation.getFieldworkCostCurrency().intValue());
                    }
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.FWCOST.getDescription(), ObjStr(projectInitiation.getFieldworkCost()), currency));
                }
            }


            //NPI No
            if(!StringUtils.isBlank(projectInitiation.getNpiReferenceNo()))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.NPINO.getDescription()));
            }

            //Request for Methodology Waiver
            if(projectInitiation.getDeviationFromSM()!=null && projectInitiation.getDeviationFromSM()==1)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.REQMETHWAIVER.getDescription(),"Yes"));
            }

            //Business Question
            if(!StringUtils.isBlank(html2text(projectInitiation.getBizQuestion())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription()));
            }
            //Research Objectives
            if(!StringUtils.isBlank(html2text(projectInitiation.getResearchObjective())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESOBJ.getDescription()));
            }
            //Action Standard
            if(!StringUtils.isBlank(html2text(projectInitiation.getActionStandard())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTD.getDescription()));
            }
            //Methodology Approach and Research Design
            if(!StringUtils.isBlank(html2text(projectInitiation.getResearchDesign())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESDSGN.getDescription()));
            }
            //Sample Profile (Research)
            if(!StringUtils.isBlank(html2text(projectInitiation.getSampleProfile())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SMPLPROFILE.getDescription()));
            }
            //Stimulus Material
            if(!StringUtils.isBlank(html2text(projectInitiation.getStimulusMaterial())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription()));
            }
            //Other Comments
            if(!StringUtils.isBlank(html2text(projectInitiation.getOthers())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHER.getDescription()));
            }

            //Date Stimuli Available
            if(projectInitiation.getStimuliDate()!=null)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.DATESTIMULI.getDescription()));
            }

            /**
             * Report Summary
             */
            if(projectInitiation.getTopLinePresentation()!=null && projectInitiation.getTopLinePresentation())
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPTOPLINE.getDescription(), "Selected"));
            }
            if(projectInitiation.getPresentation()!=null && projectInitiation.getPresentation())
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPPRES.getDescription(), "Selected"));
            }
            if(projectInitiation.getFullreport()!=null && projectInitiation.getFullreport())
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPFULL.getDescription(), "Selected"));
            }
            if(isMultimarket)
            {
                if(projectInitiation.getGlobalSummary()!=null && projectInitiation.getGlobalSummary())
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.CBSUMIRIS.getDescription(), "Selected"));
                }
            }

            if(!StringUtils.isBlank(html2text(projectInitiation.getOtherReportingRequirements())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHERREP.getDescription()));
            }

            //* Kantar / Non-Kantar Fields
            if(projectInitiation.getNonKantar()!=null && projectInitiation.getNonKantar().intValue()==1)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.KANTAR.getDescription(), "Selected"));
            }
            if(projectInitiation.getNonKantar()!=null && projectInitiation.getNonKantar().intValue()==2)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.NONKANTAR.getDescription(), "Selected"));
            }


            /**
             * Stakeholders ADD
             * */
            //Agency Contact 1
            if(projectInitiation.getAgencyContact1()!=null && projectInitiation.getAgencyContact1()>0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.AGENCY.getDescription()));
            }
            //Agency Contact 2
            if(projectInitiation.getAgencyContact2()!=null && projectInitiation.getAgencyContact2()>0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.AGENCY.getDescription()));
            }
            //Global Legal Contact
            if(projectInitiation.getGlobalLegalContact()!=null && projectInitiation.getGlobalLegalContact()>0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.LEGAL.getDescription()));
            }
            //Product Contact
            if(projectInitiation.getProductContact()!=null && projectInitiation.getProductContact()>0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PRODUCT.getDescription()));
            }
            //Global ProcurementContact 
            if(projectInitiation.getGlobalProcurementContact()!=null && projectInitiation.getGlobalProcurementContact()>0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROCUR.getDescription()));
            }
            //Global Communication Agency
            if(projectInitiation.getGlobalCommunicationAgency()!=null && projectInitiation.getGlobalCommunicationAgency()>0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.COMMUN.getDescription()));
            }

        }

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    public static String generateIRISSaveJSONObject(final SynchroToIRIS synchroToIRIS)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(synchroToIRIS!=null)
        {
            if(synchroToIRIS.getIrisSummaryRequired().intValue()==1)
            {
                //FW Start Date
                if(synchroToIRIS.getFieldWorkStartDate()!=null)
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.IRISFWSTARTDATE.getDescription()));
                }
                //FW End Date
                if(synchroToIRIS.getFieldWorkEndDate()!=null)
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.IRISFWENDDATE.getDescription()));
                }
                //Sample Size
                if(synchroToIRIS.getSampleSize()!=null)
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SAMPLESIZE.getDescription()));
                }
                //Report Date
                if(synchroToIRIS.getReportDate()!=null)
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.REPORTDATE.getDescription()));
                }
                //Respondent Type
                if(synchroToIRIS.getRespondentType()!=null)
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESPONDENTTYPE.getDescription()));
                }

                //Tags
                if(!StringUtils.isBlank(html2text(synchroToIRIS.getTags())))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.IRISTAGS.getDescription()));
                }
                //Related Studies
                if(!StringUtils.isBlank(html2text(synchroToIRIS.getRelatedStudy())))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.IRISRELSTUD.getDescription()));
                }
                //Conslusions
                if(!StringUtils.isBlank(html2text(synchroToIRIS.getConclusions())))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.IRISCONCLNS.getDescription()));
                }
                //KeyFindings
                if(!StringUtils.isBlank(html2text(synchroToIRIS.getKeyFindings())))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.IRISKEYFINDS.getDescription()));
                }

                if(synchroToIRIS.getAllDocsEnglish()!=null && synchroToIRIS.getAllDocsEnglish())
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.IRISALLDOCSINENG.getDescription(), "Selected"));
                }

            }
            /*else
		{
			//Summary for IRIS not required
			
			//Summary for IRIS Option Rationale
			if(!StringUtils.isBlank(html2text(synchroToIRIS.getIrisOptionRationale())))
			{
				data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.IRISSUMMARYOPT.getDescription()));
			}
		}*/

        }

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    public static String generateIRISCheckboxJSONObject(final SynchroToIRIS synchroToIRIS, final SynchroToIRIS synchroToIRIS_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(synchroToIRIS!=null)
        {

            if(!isEqual(synchroToIRIS.getIrisSummaryRequired(), synchroToIRIS_DB.getIrisSummaryRequired()))
            {
                if(synchroToIRIS.getIrisSummaryRequired().intValue()==1)
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.IRISREQ.getDescription(),
                            "Selected"));
                }
                else
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.IRISNOTREQ.getDescription(),
                            "Selected"));
                }

            }


        }

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    public static String generateEndMarketStakeholdersAddJSONObject(final ProjectInitiation projectInitiation)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(projectInitiation!=null)
        {

            /**
             * Stakeholders ADD
             * */

            //Global Legal Contact
            if(projectInitiation.getGlobalLegalContact()!=null && projectInitiation.getGlobalLegalContact()>0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.LEGAL.getDescription()));
            }
            //Product Contact
            if(projectInitiation.getProductContact()!=null && projectInitiation.getProductContact()>0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PRODUCT.getDescription()));
            }
            //Global ProcurementContact 
            if(projectInitiation.getGlobalProcurementContact()!=null && projectInitiation.getGlobalProcurementContact()>0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROCUR.getDescription()));
            }
            //Global Communication Agency
            if(projectInitiation.getGlobalCommunicationAgency()!=null && projectInitiation.getGlobalCommunicationAgency()>0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.COMMUN.getDescription()));
            }

        }

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    public static String generateCountryUpdateJSONObject(final List<Long> endmarketIDs_DB, final List<Long> endmarketIDs)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        //Country Field
        if(!isEqual(endmarketIDs, endmarketIDs_DB))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.COUNTRY.getDescription(),
                    ObjStr(getCountryNames(endmarketIDs)),
                    ObjStr(getCountryNames(endmarketIDs_DB))));
        }

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    /**
     * Project Fields Update JSON
     * @param projectInitiation
     * @param project_DB
     * @param project
     * @param endMarketDetails_DB
     * @return
     */
    public static String generateProjectUpdateJSONObject(final ProjectInitiation projectInitiation, final Project project_DB, final Project project, final List<EndMarketInvestmentDetail> endMarketDetails_DB, final Boolean isMultimarket)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();


        /**
         * Project EDIT Fields
         *
         */
        //Brand / Non-Branded 
        if(project.getBrand()!=project_DB.getBrand())
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.BRAND.getDescription(),
                    SynchroGlobal.getBrands().get(project.getBrand().intValue()), SynchroGlobal.getBrands().get(project_DB.getBrand().intValue())));
        }

        //Country 
        if(!isMultimarket)
        {
            if(!isEqual(projectInitiation.getEndMarketID(), endMarketDetails_DB.get(0).getEndMarketID()))
            {

                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.COUNTRY.getDescription(),
                        SynchroGlobal.getEndMarkets().get(new Long(projectInitiation.getEndMarketID()).intValue()), SynchroGlobal.getEndMarkets().get(new Long(endMarketDetails_DB.get(0).getEndMarketID()).intValue())));
            }
        }

        //Methodology Type
        if(project.getMethodologyType()!=project_DB.getMethodologyType())
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHTYPE.getDescription(),
                    SynchroGlobal.getMethodologies().get(project.getMethodologyType().intValue()), SynchroGlobal.getMethodologies().get(project_DB.getMethodologyType().intValue())));
        }

        //Methodology Group
        if(project.getMethodologyGroup()!=project_DB.getMethodologyGroup())
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHGROUP.getDescription(),
                    SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(project.getMethodologyGroup().intValue()), SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(project_DB.getMethodologyGroup().intValue())));
        }


        //Project Owner
        if(!isEqual(project.getProjectOwner(), project_DB.getProjectOwner()))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(), getUserName(project.getProjectOwner()), getUserName(project_DB.getProjectOwner())));
        }

        //Project Contact/ SPI Contact
        if(!isEqual(projectInitiation.getSpiContact(), endMarketDetails_DB.get(0).getSpiContact()))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(), getUserName(projectInitiation.getSpiContact()), getUserName(endMarketDetails_DB.get(0).getSpiContact())));
        }


        //Project Start Date
        if(!isEqual(project.getStartDate(), project_DB.getStartDate()))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STARTDATE.getDescription(), ObjStr(project.getStartDate()), ObjStr(project_DB.getStartDate())));
        }

        //Project End Date
        if(!isEqual(project.getEndDate(), project_DB.getEndDate()))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.ENDDATE.getDescription(), ObjStr(project.getEndDate()), ObjStr(project_DB.getEndDate())));
        }

        //CAP Rating
        if(isMultimarket)
        {
            if(project.getCapRating()!=project_DB.getCapRating())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.CAPRATING.getDescription(),
                        SynchroGlobal.getProjectTypes().get(project.getCapRating().intValue()), SynchroGlobal.getProjectTypes().get(project_DB.getCapRating().intValue())));
            }
        }

        // Save Project related fields Audit when PIB is saved for the first time
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    /**
     * PIB generate JSON
     * @param projectInitiation_DB
     * @param projectInitiation
     * @param project_DB
     * @param project
     * @param pibReporting_DB
     * @param endMarketDetails_DB
     * @param pibStakeholderList_DB
     * @return
     */
    public static String generatePIBUpdateJSONObject(final ProjectInitiation projectInitiation_DB, final ProjectInitiation projectInitiation, final Project project_DB, final Project project, final PIBReporting pibReporting_DB, final List<EndMarketInvestmentDetail> endMarketDetails_DB, final PIBStakeholderList pibStakeholderList_DB, final Long endmarketID, final Boolean isMultimarket)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(projectInitiation_DB!=null && projectInitiation!=null)
        {

            //Brand / Non-Branded 
            if(project.getBrand()!=project_DB.getBrand())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.BRAND.getDescription(),
                        SynchroGlobal.getBrands().get(project.getBrand().intValue()), SynchroGlobal.getBrands().get(project_DB.getBrand().intValue())));
            }

            //Country
            if(!isMultimarket)
            {
                if(projectInitiation.getEndMarketID()!=projectInitiation_DB.getEndMarketID())
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.COUNTRY.getDescription(),
                            SynchroGlobal.getEndMarkets().get(new Long(projectInitiation.getEndMarketID()).intValue()), SynchroGlobal.getEndMarkets().get(new Long(projectInitiation_DB.getEndMarketID()).intValue())));
                }
            }

            //Methodology Type
            if(project.getMethodologyType()!=project_DB.getMethodologyType())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHTYPE.getDescription(),
                        SynchroGlobal.getMethodologies().get(project.getMethodologyType().intValue()), SynchroGlobal.getMethodologies().get(project_DB.getMethodologyType().intValue())));
            }

            //Methodology Group
            if(project.getMethodologyGroup()!=project_DB.getMethodologyGroup())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHGROUP.getDescription(),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(project.getMethodologyGroup().intValue()), SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(project_DB.getMethodologyGroup().intValue())));
            }


            //Project Owner
            if(!isEqual(project.getProjectOwner(), project_DB.getProjectOwner()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(), getUserName(project.getProjectOwner()), getUserName(project_DB.getProjectOwner())));
            }


            //Project Contact/ SPI Contact
            if(!isEqual(projectInitiation.getSpiContact(), endMarketDetails_DB.get(0).getSpiContact()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(), getUserName(projectInitiation.getSpiContact()), getUserName(projectInitiation_DB.getSpiContact())));
            }


            //Project Start Date
            if(!isEqual(project.getStartDate(), project_DB.getStartDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STARTDATE.getDescription(), ObjStr(project.getStartDate()), ObjStr(project_DB.getStartDate())));
            }

            //Project End Date
            if(!isEqual(project.getEndDate(), project_DB.getEndDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.ENDDATE.getDescription(), ObjStr(project.getEndDate()), ObjStr(project_DB.getEndDate())));
            }

            //Latest Estimate
            //isEqual(projectInitiation.getLatestEstimate(), projectInitiation_DB.getLatestEstimate()) 
            if(!isEqual(projectInitiation.getLatestEstimate(), projectInitiation_DB.getLatestEstimate()) || projectInitiation.getLatestEstimateType()!=projectInitiation_DB.getLatestEstimateType())
            {
                String currency = "";
                if(projectInitiation.getLatestEstimateType()!=null && SynchroGlobal.getCurrencies().containsKey(projectInitiation.getLatestEstimateType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(projectInitiation.getLatestEstimateType());
                }

                String currency_DB = "";
                if(projectInitiation_DB.getLatestEstimateType()!=null && SynchroGlobal.getCurrencies().containsKey(projectInitiation_DB.getLatestEstimateType()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(projectInitiation_DB.getLatestEstimateType());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.LATESTESTM.getDescription(), ObjStr(projectInitiation.getLatestEstimate()), currency, ObjStr(projectInitiation_DB.getLatestEstimate()), currency_DB));
            }

            //Has Tendering Process
            if(!isEqual(projectInitiation.getHasTenderingProcess(), projectInitiation_DB.getHasTenderingProcess()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.TENDERPROCESS.getDescription(),
                        (projectInitiation.getHasTenderingProcess().intValue()==0?"No":"Yes"), (projectInitiation_DB.getHasTenderingProcess().intValue()==0?"No":"Yes")));
            }

            //FW COST
            if(projectInitiation.getHasTenderingProcess()!=null && projectInitiation.getHasTenderingProcess().intValue()==1)
            {
                if(!isEqual(projectInitiation.getFieldworkCost(), projectInitiation_DB.getFieldworkCost()) || projectInitiation.getFieldworkCostCurrency()!=projectInitiation_DB.getFieldworkCostCurrency())
                {
                    String currency = "";
                    if(projectInitiation.getFieldworkCostCurrency()!=null && SynchroGlobal.getCurrencies().containsKey(projectInitiation.getFieldworkCostCurrency().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectInitiation.getFieldworkCostCurrency().intValue());
                    }

                    String currency_DB = "";
                    if(projectInitiation_DB.getFieldworkCostCurrency()!=null && SynchroGlobal.getCurrencies().containsKey(projectInitiation_DB.getFieldworkCostCurrency().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectInitiation_DB.getFieldworkCostCurrency().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.FWCOST.getDescription(), ObjStr(projectInitiation.getFieldworkCost()) ,currency, ObjStr(projectInitiation_DB.getFieldworkCost()), currency_DB));
                }
            }

            //NPI Number
            if(!StringUtils.equals(projectInitiation.getNpiReferenceNo(), projectInitiation_DB.getNpiReferenceNo()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.NPINO.getDescription(), ObjStr(projectInitiation.getNpiReferenceNo()), ObjStr(projectInitiation_DB.getNpiReferenceNo())));
            }

            if(!isMultimarket || isEqual(endmarketID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                //Request for Methodology Waiver
                if(!isEqual(projectInitiation.getDeviationFromSM(), projectInitiation_DB.getDeviationFromSM()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.REQMETHWAIVER.getDescription(),
                            (projectInitiation.getDeviationFromSM().intValue()==0?"No":"Yes"), (projectInitiation_DB.getDeviationFromSM().intValue()==0?"No":"Yes")));
                }
            }

            //CAP Rating
            if(isMultimarket)
            {
                if(!isEqual(project.getCapRating(),project_DB.getCapRating()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.CAPRATING.getDescription(),
                            SynchroGlobal.getProjectTypes().get(project.getCapRating().intValue()), SynchroGlobal.getProjectTypes().get(project_DB.getCapRating().intValue())));
                }
            }

            //Business Question
            if(!StringUtils.equals(html2text(projectInitiation.getBizQuestion()), html2text(projectInitiation_DB.getBizQuestion())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(), ObjStr(projectInitiation.getBizQuestion()), ObjStr(projectInitiation_DB.getBizQuestion())));
            }
            //Research Objectives
            if(!StringUtils.equals(html2text(projectInitiation.getResearchObjective()), html2text(projectInitiation_DB.getResearchObjective())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESOBJ.getDescription(), ObjStr(projectInitiation.getResearchObjective()), ObjStr(projectInitiation_DB.getResearchObjective())));
            }
            //Action Standards
            if(!StringUtils.equals(html2text(projectInitiation.getActionStandard()), html2text(projectInitiation_DB.getActionStandard())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTD.getDescription(), ObjStr(projectInitiation.getActionStandard()), ObjStr(projectInitiation_DB.getActionStandard())));
            }
            //Methodology Approach and Research Design
            if(!StringUtils.equals(html2text(projectInitiation.getResearchDesign()), html2text(projectInitiation_DB.getResearchDesign())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESDSGN.getDescription(), ObjStr(projectInitiation.getResearchDesign()), ObjStr(projectInitiation_DB.getResearchDesign())));
            }
            //Sample Profile (Research)
            if(!StringUtils.equals(html2text(projectInitiation.getSampleProfile()), html2text(projectInitiation_DB.getSampleProfile())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SMPLPROFILE.getDescription(), ObjStr(projectInitiation.getSampleProfile()), ObjStr(projectInitiation_DB.getSampleProfile())));
            }
            //Stimulus Material
            if(!StringUtils.equals(html2text(projectInitiation.getStimulusMaterial()), html2text(projectInitiation_DB.getStimulusMaterial())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(), ObjStr(projectInitiation.getStimulusMaterial()), ObjStr(projectInitiation_DB.getStimulusMaterial())));
            }
            //Other Comments
            if(!StringUtils.equals(html2text(projectInitiation.getOthers()), html2text(projectInitiation_DB.getOthers())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHER.getDescription(), ObjStr(projectInitiation.getOthers()), ObjStr(projectInitiation_DB.getOthers())));
            }

            //Date Stimuli
            if(!isEqual(projectInitiation.getStimuliDate(), projectInitiation_DB.getStimuliDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(), ObjStr(projectInitiation.getStimuliDate()), ObjStr(projectInitiation_DB.getStimuliDate())));
            }


            //PIB Reporting
            if(pibReporting_DB!=null)
            {
                //Textfield: Other Reporting Requirements
                if(!StringUtils.equals(html2text(projectInitiation.getOtherReportingRequirements()), html2text(pibReporting_DB.getOtherReportingRequirements())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHERREP.getDescription(), ObjStr(projectInitiation.getOtherReportingRequirements()), ObjStr(pibReporting_DB.getOtherReportingRequirements())));
                }
                //Checkbox Reporting Requirement:Full Report
                if(!isEqual(projectInitiation.getTopLinePresentation(), pibReporting_DB.getTopLinePresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPTOPLINE.getDescription(),getBoolean(projectInitiation.getTopLinePresentation())?"Selected":"Unselected"));
                }

                //Checkbox Reporting Requirement:Presentation
                if(!isEqual(projectInitiation.getPresentation(), pibReporting_DB.getPresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPPRES.getDescription(), getBoolean(projectInitiation.getPresentation())?"Selected":"Unselected"));
                }
                //Checkbox Reporting Requirement:Topline Presentation
                if(!isEqual(projectInitiation.getFullreport(), pibReporting_DB.getFullreport()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPFULL.getDescription(), getBoolean(projectInitiation.getFullreport())?"Selected":"Unselected"));
                }
                if(isMultimarket)
                {
                    //Checkbox Reporting Requirement:Topline Presentation
                    if(!isEqual(projectInitiation.getGlobalSummary(), pibReporting_DB.getGlobalSummary()))
                    {
                        data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.CBSUMIRIS.getDescription(), getBoolean(projectInitiation.getGlobalSummary())?"Selected":"Unselected"));
                    }
                }
            }

            //Kantar / Non- Kantar Selection
            if(!isEqual(projectInitiation.getNonKantar(), projectInitiation_DB.getNonKantar()))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(),
                        projectInitiation.getNonKantar().intValue()==1?SynchroGlobal.LogFields.KANTAR.getDescription():SynchroGlobal.LogFields.NONKANTAR.getDescription(),"Selected"));
            }

            /**
             * Stakeholders EDIT
             */
            if(pibStakeholderList_DB!=null)
            {
                //Update in case project is Single market OR if project is multimarket and TAB is Above Market only.
                if(!isMultimarket || (endmarketID!=null && isEqual(endmarketID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)))
                {
                    //Agency Contact 1	
                    if(!isEqual(projectInitiation.getAgencyContact1(), pibStakeholderList_DB.getAgencyContact1()))
                    {
                        data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.AGENCY.getDescription(),
                                getUserName(projectInitiation.getAgencyContact1()), getUserName(pibStakeholderList_DB.getAgencyContact1())));
                    }
                    //Agency Contact 2	
                    if(!isEqual(projectInitiation.getAgencyContact2(), pibStakeholderList_DB.getAgencyContact2()))
                    {
                        data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.AGENCY.getDescription(),
                                getUserName(projectInitiation.getAgencyContact2()), getUserName(pibStakeholderList_DB.getAgencyContact2())));
                    }
                }

                //Legal Contact	
                if(!isEqual(projectInitiation.getGlobalLegalContact(), pibStakeholderList_DB.getGlobalLegalContact()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.LEGAL.getDescription(),
                            getUserName(projectInitiation.getGlobalLegalContact()), getUserName(pibStakeholderList_DB.getGlobalLegalContact())));
                }
                //Product Contact	
                if(!isEqual(projectInitiation.getProductContact(), pibStakeholderList_DB.getProductContact()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PRODUCT.getDescription(),
                            getUserName(projectInitiation.getProductContact()), getUserName(pibStakeholderList_DB.getProductContact())));
                }
                //Procurement Contact	
                if(!isEqual(projectInitiation.getGlobalProcurementContact(), pibStakeholderList_DB.getGlobalProcurementContact()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROCUR.getDescription(),
                            getUserName(projectInitiation.getGlobalProcurementContact()), getUserName(pibStakeholderList_DB.getGlobalProcurementContact())));
                }
                //Communication Agency
                if(!isEqual(projectInitiation.getGlobalCommunicationAgency(), pibStakeholderList_DB.getGlobalCommunicationAgency()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.COMMUN.getDescription(),
                            getUserName(projectInitiation.getGlobalCommunicationAgency()), getUserName(pibStakeholderList_DB.getGlobalCommunicationAgency())));
                }

            }

        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    public static String generatePITUpdateJSONObject(final Project project_DB, final Project project,
                                                     final EndMarketInvestmentDetail endMarketDetail_DB, final EndMarketInvestmentDetail endMarketDetail)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(project_DB!=null && project!=null)
        {
            if(!StringUtils.equals(project.getName(), project_DB.getName()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PITPROJNAME.getDescription(),
                        ObjStr(project.getName()), ObjStr(project_DB.getName())));
            }
            if(!isEqual(project.getCategoryType(), project_DB.getCategoryType()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.PITCATEGORY.getDescription(),
                        ObjStr(getDataCategoryNames(project.getCategoryType())),
                        ObjStr(getDataCategoryNames(project_DB.getCategoryType()))));
            }
            if(!isEqual(project.getProposedMethodology(), project_DB.getProposedMethodology()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.PITPROPMETH.getDescription(),
                        ObjStr(getProposedMethodologyNames(project.getProposedMethodology())),
                        ObjStr(getProposedMethodologyNames(project_DB.getProposedMethodology()))));
            }
            //Estimated Cost
            if(endMarketDetail!=null && endMarketDetail_DB!=null)
            {
                //Estimated Cost
                if(!isEqual(endMarketDetail.getInitialCost(), endMarketDetail_DB.getInitialCost()) ||
                        !isEqual(endMarketDetail.getInitialCostCurrency(), endMarketDetail_DB.getInitialCostCurrency()))
                {
                    String currency = "";
                    if(endMarketDetail.getInitialCostCurrency()!=null && SynchroGlobal.getCurrencies().containsKey(endMarketDetail.getInitialCostCurrency().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(endMarketDetail.getInitialCostCurrency().intValue());
                    }

                    String currency_DB = "";
                    if(endMarketDetail_DB.getInitialCostCurrency()!=null && SynchroGlobal.getCurrencies().containsKey(endMarketDetail_DB.getInitialCostCurrency().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(endMarketDetail_DB.getInitialCostCurrency().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.ESTIMATEDCOST.getDescription(),
                            ObjStr(endMarketDetail.getInitialCost()), currency, ObjStr(endMarketDetail_DB.getInitialCost()), currency_DB));
                }
            }
            if(!isEqual(project.getBudgetYear(), project_DB.getBudgetYear()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.PITBUDGETYEAR.getDescription(),
                        project.getBudgetYear().toString(), project_DB.getBudgetYear().toString()));
            }
            if(!isEqual(project.getConfidential(), project_DB.getConfidential()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.PITCONF.getDescription(),
                        (project.getConfidential()?"Yes":"No"), (project_DB.getConfidential()?"Yes":"No")));
            }





        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }



    public static String generatePITMultimarketUpdateJSONObject(final Project project_DB, final Project project)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(project_DB!=null && project!=null)
        {
            if(!StringUtils.equals(project.getName(), project_DB.getName()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PITPROJNAME.getDescription(),
                        ObjStr(project.getName()), ObjStr(project_DB.getName())));
            }

            if(!isEqual(project.getCategoryType(), project_DB.getCategoryType()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PITCATEGORY.getDescription(),
                        ObjStr(getDataCategoryNames(project.getCategoryType())),
                        ObjStr(getDataCategoryNames(project_DB.getCategoryType()))));
            }
            if(!isEqual(project.getProposedMethodology(), project_DB.getProposedMethodology()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PITPROPMETH.getDescription(),
                        ObjStr(getProposedMethodologyNames(project.getProposedMethodology())),
                        ObjStr(getProposedMethodologyNames(project_DB.getProposedMethodology()))));
            }

            //Total Cost
            if(!isEqual(project.getTotalCost(), project_DB.getTotalCost()) ||
                    !isEqual(project.getTotalCostCurrency(), project_DB.getTotalCostCurrency()))
            {
                String currency = "";
                if(project.getTotalCostCurrency()!=null && SynchroGlobal.getCurrencies().containsKey(project.getTotalCostCurrency().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(project.getTotalCostCurrency().intValue());
                }

                String currency_DB = "";
                if(project_DB.getTotalCostCurrency()!=null && SynchroGlobal.getCurrencies().containsKey(project_DB.getTotalCostCurrency().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(project_DB.getTotalCostCurrency().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMTOTALCOST.getDescription(),
                        ObjStr(project.getTotalCost()), currency, ObjStr(project_DB.getTotalCost()), currency_DB));
            }

            if(!isEqual(project.getBudgetYear(), project_DB.getBudgetYear()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.PITBUDGETYEAR.getDescription(),
                        project.getBudgetYear().toString(), project_DB.getBudgetYear().toString()));
            }

            if(!isEqual(project.getConfidential(), project_DB.getConfidential()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.PITCONF.getDescription(),
                        (project.getConfidential()?"Yes":"No"), (project_DB.getConfidential()?"Yes":"No")));
            }

        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    /**
     * IRIS Update Object
     * @param synchroToIRIS
     * @param synchroToIRIS_DB
     * @return
     */
    public static String generateIRISUpdateJSONObject(final SynchroToIRIS synchroToIRIS, final SynchroToIRIS synchroToIRIS_DB, final Boolean isUpdate)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(synchroToIRIS_DB!=null && synchroToIRIS!=null && synchroToIRIS.getIrisSummaryRequired().intValue()==1)
        {

            //Brand / Non-Branded 
            if(synchroToIRIS.getBrand()!=synchroToIRIS_DB.getBrand())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.BRAND.getDescription(),
                        SynchroGlobal.getBrands().get(synchroToIRIS.getBrand().intValue()), SynchroGlobal.getBrands().get(synchroToIRIS_DB.getBrand().intValue())));
            }

            //Summary written By
            if(!isEqual(synchroToIRIS.getSummaryWrittenBy(), synchroToIRIS_DB.getSummaryWrittenBy()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.SUMMRAYWRITTENBY.getDescription(),
                        getUserName(synchroToIRIS.getSummaryWrittenBy()), getUserName(synchroToIRIS_DB.getSummaryWrittenBy())));
            }

            if(isUpdate)
            {
                //Fieldwork Start Date
                if(!isEqual(synchroToIRIS.getFieldWorkStartDate(), synchroToIRIS_DB.getFieldWorkStartDate()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.IRISFWSTARTDATE.getDescription(),
                            ObjStr(synchroToIRIS.getFieldWorkStartDate()), ObjStr(synchroToIRIS_DB.getFieldWorkStartDate())));
                }
                //Fieldwork End Date
                if(!isEqual(synchroToIRIS.getFieldWorkEndDate(), synchroToIRIS_DB.getFieldWorkEndDate()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.IRISFWENDDATE.getDescription(),
                            ObjStr(synchroToIRIS.getFieldWorkEndDate()), ObjStr(synchroToIRIS_DB.getFieldWorkEndDate())));
                }
                //Sample size
                if(!StringUtils.equals(synchroToIRIS.getSampleSize(), synchroToIRIS_DB.getSampleSize()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SAMPLESIZE.getDescription(),
                            ObjStr(synchroToIRIS.getSampleSize()), ObjStr(synchroToIRIS_DB.getSampleSize())));
                }
                //Report Date
                if(!isEqual(synchroToIRIS.getReportDate(), synchroToIRIS_DB.getReportDate()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.REPORTDATE.getDescription(),
                            ObjStr(synchroToIRIS.getReportDate()), ObjStr(synchroToIRIS_DB.getReportDate())));
                }
                //Respondent Type
                if(!StringUtils.equals(synchroToIRIS.getRespondentType(), synchroToIRIS_DB.getRespondentType()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESPONDENTTYPE.getDescription(),
                            ObjStr(synchroToIRIS.getRespondentType()), ObjStr(synchroToIRIS_DB.getRespondentType())));
                }
            }


            //Project Description
            if(!StringUtils.equals(html2text(synchroToIRIS.getProjectDesc()), html2text(synchroToIRIS_DB.getProjectDesc())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PROJDESC.getDescription(),
                        ObjStr(synchroToIRIS.getProjectDesc()), ObjStr(synchroToIRIS_DB.getProjectDesc())));
            }
            //Research Objectives
            if(!StringUtils.equals(html2text(synchroToIRIS.getResearchObjective()), html2text(synchroToIRIS_DB.getResearchObjective())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESOBJ.getDescription(),
                        ObjStr(synchroToIRIS.getResearchObjective()), ObjStr(synchroToIRIS_DB.getResearchObjective())));
            }
            //Business Question
            if(!StringUtils.equals(html2text(synchroToIRIS.getBizQuestion()), html2text(synchroToIRIS_DB.getBizQuestion())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(),
                        ObjStr(synchroToIRIS.getBizQuestion()), ObjStr(synchroToIRIS_DB.getBizQuestion())));
            }

            //Action Standards
            if(!StringUtils.equals(html2text(synchroToIRIS.getActionStandard()), html2text(synchroToIRIS_DB.getActionStandard())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTD.getDescription(),
                        ObjStr(synchroToIRIS.getActionStandard()), ObjStr(synchroToIRIS_DB.getActionStandard())));
            }
            if(isUpdate)
            {
                //Tags
                if(!StringUtils.equals(html2text(synchroToIRIS.getTags()), html2text(synchroToIRIS_DB.getTags())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.IRISTAGS.getDescription(),
                            ObjStr(synchroToIRIS.getTags()), ObjStr(synchroToIRIS_DB.getTags())));
                }
                //Related Studies
                if(!StringUtils.equals(html2text(synchroToIRIS.getRelatedStudy()), html2text(synchroToIRIS_DB.getRelatedStudy())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.IRISRELSTUD.getDescription(),
                            ObjStr(synchroToIRIS.getRelatedStudy()), ObjStr(synchroToIRIS_DB.getRelatedStudy())));
                }
                //Conclusions
                if(!StringUtils.equals(html2text(synchroToIRIS.getConclusions()), html2text(synchroToIRIS_DB.getConclusions())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.IRISCONCLNS.getDescription(),
                            ObjStr(synchroToIRIS.getConclusions()), ObjStr(synchroToIRIS_DB.getConclusions())));
                }
                //Key Findings
                if(!StringUtils.equals(html2text(synchroToIRIS.getKeyFindings()), html2text(synchroToIRIS_DB.getKeyFindings())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.IRISCONCLNS.getDescription(),
                            ObjStr(synchroToIRIS.getKeyFindings()), ObjStr(synchroToIRIS_DB.getKeyFindings())));
                }

                //All documents are in English
                if(!isEqual(synchroToIRIS.getAllDocsEnglish(), synchroToIRIS_DB.getAllDocsEnglish()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.IRISALLDOCSINENG.getDescription(),
                            getBoolean(synchroToIRIS.getAllDocsEnglish())?"Selected":"Unselected"));
                }
            }
        }
        /*else
	{
		//IRIS Summary Not Required checkbox selected
		
		if(isUpdate)
		{
			//Summary for IRIS Option Rationale *
			if(!StringUtils.equals(html2text(synchroToIRIS.getIrisOptionRationale()), html2text(synchroToIRIS_DB.getIrisOptionRationale())))
			{
				data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.IRISSUMMARYOPT.getDescription(),
						ObjStr(synchroToIRIS.getIrisOptionRationale()), ObjStr(synchroToIRIS_DB.getIrisOptionRationale())));
			}	
		}
	}*/
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    public static String generateIRISSumryOptJSONObject(final SynchroToIRIS synchroToIRIS, final SynchroToIRIS synchroToIRIS_DB, final Boolean isUpdate)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(synchroToIRIS!=null)
        {
            if(synchroToIRIS_DB!=null && isUpdate)
            {
                //Summary for IRIS Option Rationale
                if(!StringUtils.equals(html2text(synchroToIRIS.getIrisOptionRationale()), html2text(synchroToIRIS_DB.getIrisOptionRationale())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.IRISSUMMARYOPT.getDescription(),
                            ObjStr(synchroToIRIS.getIrisOptionRationale()), ObjStr(synchroToIRIS_DB.getIrisOptionRationale())));
                }
            }
            else
            {
                if(!StringUtils.isBlank(html2text(synchroToIRIS.getIrisOptionRationale())))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.IRISSUMMARYOPT.getDescription()));
                }
            }
        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    public static String generatePIBLegalApproverJSONObject(final ProjectInitiation projectInitiation, final ProjectInitiation projectInitiation_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(projectInitiation_DB!=null)
        {
            //Legal Approval
            //Checkbox Legal Approval for PIB received
            if(!isEqual(projectInitiation.getLegalApprovalRcvd(), projectInitiation_DB.getLegalApprovalRcvd()) && projectInitiation.getLegalApprovalRcvd())
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CBAPPROVE.getId(), SynchroGlobal.LogFields.PIBLEGALAPPRREC.getDescription()));
            }

            //Checkbox Legal Approval not required on PIB
            if(!isEqual(projectInitiation.getLegalApprovalNotReq(), projectInitiation_DB.getLegalApprovalNotReq()) && projectInitiation.getLegalApprovalNotReq())
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CBAPPROVE.getId(), SynchroGlobal.LogFields.PIBLEGALAPPRNOTREQ.getDescription()));
            }
        }
        else
        {
            if(projectInitiation.getLegalApprovalRcvd()!=null && projectInitiation.getLegalApprovalRcvd())
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CBAPPROVE.getId(), SynchroGlobal.LogFields.PIBLEGALAPPRREC.getDescription()));
            }
            if(projectInitiation.getLegalApprovalNotReq()!=null && projectInitiation.getLegalApprovalNotReq())
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CBAPPROVE.getId(), SynchroGlobal.LogFields.PIBLEGALAPPRNOTREQ.getDescription()));
            }
        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }



    /**
     * Proposal Update Log Audit
     * @param proposalInitiation_DB
     * @param proposalInitiation
     * @param proposalReporting_DB
     * @return
     */
    public static String generateProposalUpdateJSONObject(final ProposalInitiation proposalInitiation_DB, final ProposalInitiation proposalInitiation, final ProposalReporting proposalReporting_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(proposalInitiation_DB!=null && proposalInitiation!=null)
        {

            //Brand / Non-Branded 
            if(proposalInitiation.getBrand()!=proposalInitiation_DB.getBrand())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.BRAND.getDescription(),
                        SynchroGlobal.getBrands().get(proposalInitiation.getBrand().intValue()), SynchroGlobal.getBrands().get(proposalInitiation_DB.getBrand().intValue())));
            }

            //Country 
            if(proposalInitiation.getEndMarketID()!=proposalInitiation_DB.getEndMarketID())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.COUNTRY.getDescription(),
                        SynchroGlobal.getEndMarkets().get(new Long(proposalInitiation.getEndMarketID()).intValue()), SynchroGlobal.getEndMarkets().get(new Long(proposalInitiation_DB.getEndMarketID()).intValue())));
            }
            //Methodology Type
            if(proposalInitiation.getMethodologyType()!=proposalInitiation_DB.getMethodologyType())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHTYPE.getDescription(),
                        SynchroGlobal.getMethodologies().get(proposalInitiation.getMethodologyType().intValue()), SynchroGlobal.getMethodologies().get(proposalInitiation_DB.getMethodologyType().intValue())));
            }

            //Methodology Group
            if(proposalInitiation.getMethodologyGroup()!=proposalInitiation_DB.getMethodologyGroup())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHGROUP.getDescription(),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(proposalInitiation.getMethodologyGroup().intValue()),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(proposalInitiation_DB.getMethodologyGroup().intValue())));
            }


            //Project Owner
            if(!isEqual(proposalInitiation.getProjectOwner(), proposalInitiation_DB.getProjectOwner()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(), getUserName(proposalInitiation.getProjectOwner()), getUserName(proposalInitiation_DB.getProjectOwner())));
            }


            //Project Contact/ SPI Contact
            if(!isEqual(proposalInitiation.getSpiContact(), proposalInitiation_DB.getSpiContact()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(), getUserName(proposalInitiation.getSpiContact()), getUserName(proposalInitiation_DB.getSpiContact())));
            }


            //Project Start Date
            if(!isEqual(proposalInitiation.getStartDate(), proposalInitiation_DB.getStartDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STARTDATE.getDescription(), ObjStr(proposalInitiation.getStartDate()), ObjStr(proposalInitiation_DB.getStartDate())));
            }

            //Project End Date
            if(!isEqual(proposalInitiation.getEndDate(), proposalInitiation_DB.getEndDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.ENDDATE.getDescription(), ObjStr(proposalInitiation.getEndDate()), ObjStr(proposalInitiation_DB.getEndDate())));
            }


            //NPI Number
            if(!StringUtils.equals(proposalInitiation.getNpiReferenceNo(), proposalInitiation_DB.getNpiReferenceNo()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.NPINO.getDescription(), ObjStr(proposalInitiation.getNpiReferenceNo()), ObjStr(proposalInitiation_DB.getNpiReferenceNo())));
            }

            /* READONLY Field
		//Request for Methodology Waiver
		if(!isEqual(projectInitiation.getDeviationFromSM(), projectInitiation_DB.getDeviationFromSM()))
		{
			data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.REQMETHWAIVER.getDescription(), 
											(projectInitiation.getDeviationFromSM().intValue()==0?"No":"Yes"), (projectInitiation_DB.getDeviationFromSM().intValue()==0?"No":"Yes")));
		}
		*/
            //Business Question
            if(!StringUtils.equals(html2text(proposalInitiation.getBizQuestion()), html2text(proposalInitiation_DB.getBizQuestion())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(), ObjStr(proposalInitiation.getBizQuestion()), ObjStr(proposalInitiation_DB.getBizQuestion())));
            }
            //Research Objectives
            if(!StringUtils.equals(html2text(proposalInitiation.getResearchObjective()), html2text(proposalInitiation_DB.getResearchObjective())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESOBJ.getDescription(), ObjStr(proposalInitiation.getResearchObjective()), ObjStr(proposalInitiation_DB.getResearchObjective())));
            }
            //Action Standards
            if(!StringUtils.equals(html2text(proposalInitiation.getActionStandard()), html2text(proposalInitiation_DB.getActionStandard())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTD.getDescription(), ObjStr(proposalInitiation.getActionStandard()), ObjStr(proposalInitiation_DB.getActionStandard())));
            }
            //Methodology Approach and Research Design
            if(!StringUtils.equals(html2text(proposalInitiation.getResearchDesign()), html2text(proposalInitiation_DB.getResearchDesign())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESDSGN.getDescription(), ObjStr(proposalInitiation.getResearchDesign()), ObjStr(proposalInitiation_DB.getResearchDesign())));
            }
            //Sample Profile (Research)
            if(!StringUtils.equals(html2text(proposalInitiation.getSampleProfile()), html2text(proposalInitiation_DB.getSampleProfile())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SMPLPROFILE.getDescription(), ObjStr(proposalInitiation.getSampleProfile()), ObjStr(proposalInitiation_DB.getSampleProfile())));
            }
            //Stimulus Material
            if(!StringUtils.equals(html2text(proposalInitiation.getStimulusMaterial()), html2text(proposalInitiation_DB.getStimulusMaterial())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(), ObjStr(proposalInitiation.getStimulusMaterial()), ObjStr(proposalInitiation_DB.getStimulusMaterial())));
            }
            //Other Comments
            if(!StringUtils.equals(html2text(proposalInitiation.getOthers()), html2text(proposalInitiation_DB.getOthers())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHER.getDescription(), ObjStr(proposalInitiation.getOthers()), ObjStr(proposalInitiation_DB.getOthers())));
            }

            //Date Stimuli
            if(!isEqual(proposalInitiation.getStimuliDate(), proposalInitiation_DB.getStimuliDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(), ObjStr(proposalInitiation.getStimuliDate()), ObjStr(proposalInitiation_DB.getStimuliDate())));
            }


            //PIB Reporting
            if(proposalReporting_DB!=null)
            {
                //Textfield: Other Reporting Requirements
                if(!StringUtils.equals(html2text(proposalInitiation.getOtherReportingRequirements()), html2text(proposalReporting_DB.getOtherReportingRequirements())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHERREP.getDescription(), ObjStr(proposalInitiation.getOtherReportingRequirements()), ObjStr(proposalReporting_DB.getOtherReportingRequirements())));
                }
                //Checkbox Reporting Requirement:Full Report
                if(!isEqual(proposalInitiation.getTopLinePresentation(), proposalReporting_DB.getTopLinePresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPTOPLINE.getDescription(),getBoolean(proposalInitiation.getTopLinePresentation())?"Selected":"Unselected"));
                }

                //Checkbox Reporting Requirement:Presentation
                if(!isEqual(proposalInitiation.getPresentation(), proposalReporting_DB.getPresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPPRES.getDescription(), getBoolean(proposalInitiation.getPresentation())?"Selected":"Unselected"));
                }
                //Checkbox Reporting Requirement:Topline Presentation
                if(!isEqual(proposalInitiation.getFullreport(), proposalReporting_DB.getFullreport()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPFULL.getDescription(), getBoolean(proposalInitiation.getFullreport())?"Selected":"Unselected"));
                }
            }

            /*//Kantar / Non- Kantar Selection
		if(!isEqual(projectInitiation.getNonKantar(), projectInitiation_DB.getNonKantar()))
		{
			data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), 
					projectInitiation.getNonKantar().intValue()==1?SynchroGlobal.LogFields.KANTAR.getDescription():SynchroGlobal.LogFields.NONKANTAR.getDescription(),"Selected"));
		}*/


        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    public static String generateProjectSpecsUpdateJSONObject(final ProjectSpecsInitiation projectSpecsInitiation_DB,
                                                              final ProjectSpecsInitiation projectSpecsInitiation, final ProjectSpecsEndMarketDetails projectSpecsEMDetails_DB,
                                                              final ProjectSpecsEndMarketDetails projectSpecsEMDetails, final ProjectSpecsReporting projectSpecsReporting_DB,
                                                              final List<EndMarketInvestmentDetail> endMarketDetails_DB, final ProjectInitiation projectInitiation_DB)
    {

        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(projectSpecsInitiation_DB!=null && projectSpecsInitiation!=null)
        {

            //Brand / Non-Branded 
            if(projectSpecsInitiation.getBrand()!=projectSpecsInitiation_DB.getBrand())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.BRAND.getDescription(),
                        SynchroGlobal.getBrands().get(projectSpecsInitiation.getBrand().intValue()),
                        SynchroGlobal.getBrands().get(projectSpecsInitiation_DB.getBrand().intValue())));
            }

            //Methodology Type
            if(projectSpecsInitiation.getMethodologyType()!=projectSpecsInitiation_DB.getMethodologyType())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHTYPE.getDescription(),
                        SynchroGlobal.getMethodologies().get(projectSpecsInitiation.getMethodologyType().intValue()),
                        SynchroGlobal.getMethodologies().get(projectSpecsInitiation_DB.getMethodologyType().intValue())));
            }

            //Methodology Group
            if(projectSpecsInitiation.getMethodologyGroup()!=projectSpecsInitiation_DB.getMethodologyGroup())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHGROUP.getDescription(),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(projectSpecsInitiation.getMethodologyGroup().intValue()),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(projectSpecsInitiation_DB.getMethodologyGroup().intValue())));
            }


            //Project Owner
            if(!isEqual(projectSpecsInitiation.getProjectOwner(), projectSpecsInitiation_DB.getProjectOwner()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(),
                        getUserName(projectSpecsInitiation.getProjectOwner()), getUserName(projectSpecsInitiation_DB.getProjectOwner())));
            }


            //Project Contact/ SPI Contact
            if(!isEqual(projectSpecsInitiation.getSpiContact(), projectSpecsInitiation_DB.getSpiContact()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(),
                        getUserName(projectSpecsInitiation.getSpiContact()), getUserName(projectSpecsInitiation_DB.getSpiContact())));
            }


            //Project Start Date
            if(!isEqual(projectSpecsInitiation.getStartDate(), projectSpecsInitiation_DB.getStartDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STARTDATE.getDescription(),
                        ObjStr(projectSpecsInitiation.getStartDate()), ObjStr(projectSpecsInitiation_DB.getStartDate())));
            }

            //Project End Date
            if(!isEqual(projectSpecsInitiation.getEndDate(), projectSpecsInitiation_DB.getEndDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.ENDDATE.getDescription(),
                        ObjStr(projectSpecsInitiation.getEndDate()), ObjStr(projectSpecsInitiation_DB.getEndDate())));
            }

            //Check if users are not External Agency User
            if(!SynchroPermHelper.isExternalAgencyUser(projectSpecsInitiation.getProjectID(), endMarketDetails_DB.get(0).getEndMarketID()))
            {
                //Estimated Cost
                if(!isEqual(projectSpecsInitiation.getEstimatedCost(), endMarketDetails_DB.get(0).getInitialCost()) ||
                        !isEqual(projectSpecsInitiation.getEstimatedCostType(), endMarketDetails_DB.get(0).getInitialCostCurrency()))
                {
                    String currency = "";
                    if(projectSpecsInitiation.getEstimatedCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsInitiation.getEstimatedCostType()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsInitiation.getEstimatedCostType());
                    }

                    String currency_DB = "";
                    if(endMarketDetails_DB.get(0).getInitialCostCurrency()!=null && SynchroGlobal.getCurrencies().containsKey(endMarketDetails_DB.get(0).getInitialCostCurrency()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(endMarketDetails_DB.get(0).getInitialCostCurrency());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.ESTIMATEDCOST.getDescription(),
                            ObjStr(projectSpecsInitiation.getEstimatedCost()), currency, ObjStr(endMarketDetails_DB.get(0).getInitialCost()), currency_DB));
                }

                //Latest Cost
                if(!isEqual(projectSpecsInitiation.getLatestEstimate(), projectInitiation_DB.getLatestEstimate()) ||
                        projectSpecsInitiation.getLatestEstimateType()!=projectInitiation_DB.getLatestEstimateType())
                {
                    String currency = "";
                    if(projectSpecsInitiation.getLatestEstimateType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsInitiation.getLatestEstimateType()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsInitiation.getLatestEstimateType());
                    }

                    String currency_DB = "";
                    if(projectInitiation_DB.getLatestEstimateType()!=null && SynchroGlobal.getCurrencies().containsKey(projectInitiation_DB.getLatestEstimateType()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectInitiation_DB.getLatestEstimateType());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.LATESTESTM.getDescription(),
                            ObjStr(projectSpecsInitiation.getLatestEstimate()), currency, ObjStr(projectInitiation_DB.getLatestEstimate()), currency_DB));
                }
            }

            //NPI Number
            if(!StringUtils.equals(projectSpecsInitiation.getNpiReferenceNo(), projectSpecsInitiation_DB.getNpiReferenceNo()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.NPINO.getDescription(),
                        ObjStr(projectSpecsInitiation.getNpiReferenceNo()), ObjStr(projectSpecsInitiation_DB.getNpiReferenceNo())));
            }

            //Request for Methodology Waiver
            if(!isEqual(projectSpecsInitiation.getDeviationFromSM(), projectSpecsInitiation_DB.getDeviationFromSM()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.REQMETHWAIVER.getDescription(),
                        (projectSpecsInitiation.getDeviationFromSM().intValue()==0?"No":"Yes"), (projectSpecsInitiation_DB.getDeviationFromSM().intValue()==0?"No":"Yes")));
            }

            //Check if users are not External Agency User
            if(!SynchroPermHelper.isExternalAgencyUser(projectSpecsInitiation.getProjectID(), endMarketDetails_DB.get(0).getEndMarketID()))
            {
                //PO Number
                if(!StringUtils.equals(projectSpecsInitiation.getPoNumber(), projectSpecsInitiation_DB.getPoNumber()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PONUMBER.getDescription(),
                            ObjStr(projectSpecsInitiation.getPoNumber()), ObjStr(projectSpecsInitiation_DB.getPoNumber())));
                }

                //PO Number 2
                if(!StringUtils.equals(projectSpecsInitiation.getPoNumber1(), projectSpecsInitiation_DB.getPoNumber1()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PONUMBER.getDescription(),
                            ObjStr(projectSpecsInitiation.getPoNumber1()), ObjStr(projectSpecsInitiation_DB.getPoNumber1())));
                }
            }
            //Project Description
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getDescription()), html2text(projectSpecsInitiation_DB.getDescription())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(),
                        ObjStr(projectSpecsInitiation.getDescription()), ObjStr(projectSpecsInitiation_DB.getDescription())));
            }

            //Business Question
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getBizQuestion()), html2text(projectSpecsInitiation_DB.getBizQuestion())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(),
                        ObjStr(projectSpecsInitiation.getBizQuestion()), ObjStr(projectSpecsInitiation_DB.getBizQuestion())));
            }
            //Research Objectives
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getResearchObjective()), html2text(projectSpecsInitiation_DB.getResearchObjective())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESOBJ.getDescription(),
                        ObjStr(projectSpecsInitiation.getResearchObjective()), ObjStr(projectSpecsInitiation_DB.getResearchObjective())));
            }
            //Action Standards
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getActionStandard()), html2text(projectSpecsInitiation_DB.getActionStandard())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTD.getDescription(),
                        ObjStr(projectSpecsInitiation.getActionStandard()), ObjStr(projectSpecsInitiation_DB.getActionStandard())));

            }
            //Methodology Approach and Research Design
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getResearchDesign()), html2text(projectSpecsInitiation_DB.getResearchDesign())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESDSGN.getDescription(),
                        ObjStr(projectSpecsInitiation.getResearchDesign()), ObjStr(projectSpecsInitiation_DB.getResearchDesign())));
            }
            //Sample Profile (Research)
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getSampleProfile()), html2text(projectSpecsInitiation_DB.getSampleProfile())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SMPLPROFILE.getDescription(),
                        ObjStr(projectSpecsInitiation.getSampleProfile()), ObjStr(projectSpecsInitiation_DB.getSampleProfile())));
            }
            //Stimulus Material
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getStimulusMaterial()), html2text(projectSpecsInitiation_DB.getStimulusMaterial())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(),
                        ObjStr(projectSpecsInitiation.getStimulusMaterial()), ObjStr(projectSpecsInitiation_DB.getStimulusMaterial())));
            }

            //Stimulus Material to be shipped to
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getStimulusMaterialShipped()), html2text(projectSpecsInitiation_DB.getStimulusMaterialShipped())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMMATSHIPPED.getDescription(),
                        ObjStr(projectSpecsInitiation.getStimulusMaterialShipped()), ObjStr(projectSpecsInitiation_DB.getStimulusMaterialShipped())));
            }

            //Other Comments
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getOthers()), html2text(projectSpecsInitiation_DB.getOthers())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHER.getDescription(),
                        ObjStr(projectSpecsInitiation.getOthers()), ObjStr(projectSpecsInitiation_DB.getOthers())));
            }

            //Date Stimuli
            if(!isEqual(projectSpecsInitiation.getStimuliDate(), projectSpecsInitiation_DB.getStimuliDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(),
                        ObjStr(projectSpecsInitiation.getStimuliDate()), ObjStr(projectSpecsInitiation_DB.getStimuliDate())));
            }


            //PIB Reporting
            if(projectSpecsReporting_DB!=null)
            {
                //Textfield: Other Reporting Requirements
                if(!StringUtils.equals(html2text(projectSpecsInitiation.getOtherReportingRequirements()), html2text(projectSpecsReporting_DB.getOtherReportingRequirements())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHERREP.getDescription(),
                            ObjStr(projectSpecsInitiation.getOtherReportingRequirements()), ObjStr(projectSpecsInitiation_DB.getOtherReportingRequirements())));
                }
                //Checkbox Reporting Requirement:Full Report
                if(!isEqual(projectSpecsInitiation.getTopLinePresentation(), projectSpecsReporting_DB.getTopLinePresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPTOPLINE.getDescription(),
                            getBoolean(projectSpecsInitiation.getTopLinePresentation())?"Selected":"Unselected"));
                }

                //Checkbox Reporting Requirement:Presentation
                if(!isEqual(projectSpecsInitiation.getPresentation(), projectSpecsReporting_DB.getPresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPPRES.getDescription(),
                            getBoolean(projectSpecsInitiation.getPresentation())?"Selected":"Unselected"));
                }
                //Checkbox Reporting Requirement:Topline Presentation
                if(!isEqual(projectSpecsInitiation.getFullreport(), projectSpecsReporting_DB.getFullreport()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPFULL.getDescription(),
                            getBoolean(projectSpecsInitiation.getFullreport())?"Selected":"Unselected"));
                }
            }

            if(projectSpecsEMDetails_DB!=null)
            {
	            //End-Market fields
	            //Total Cost
	            if(!isEqual(projectSpecsEMDetails.getTotalCost(), projectSpecsEMDetails_DB.getTotalCost()) ||
	                    projectSpecsEMDetails.getTotalCostType()!=projectSpecsEMDetails_DB.getTotalCostType())
	            {
	                String currency = "";
	                if(projectSpecsEMDetails.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getTotalCostType().intValue()))
	                {
	                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getTotalCostType().intValue());
	                }
	
	                String currency_DB = "";
	                if(projectSpecsEMDetails_DB.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getTotalCostType().intValue()))
	                {
	                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getTotalCostType().intValue());
	                }
	
	                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMTOTALCOST.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getTotalCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getTotalCost()), currency_DB));
	            }
	
	
	            //International Management Cost - Research Hub Cost
	            if(!isEqual(projectSpecsEMDetails.getIntMgmtCost(), projectSpecsEMDetails_DB.getIntMgmtCost()) ||
	                    projectSpecsEMDetails.getIntMgmtCostType()!=projectSpecsEMDetails_DB.getIntMgmtCostType())
	            {
	                String currency = "";
	                if(projectSpecsEMDetails.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getIntMgmtCostType().intValue()))
	                {
	                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getIntMgmtCostType().intValue());
	                }
	
	                String currency_DB = "";
	                if(projectSpecsEMDetails_DB.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getIntMgmtCostType().intValue()))
	                {
	                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getIntMgmtCostType().intValue());
	                }
	
	                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMINTMGTCOST.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getIntMgmtCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getIntMgmtCost()), currency_DB));
	            }
	
	            //Local Management Cost
	            if(!isEqual(projectSpecsEMDetails.getLocalMgmtCost(), projectSpecsEMDetails_DB.getLocalMgmtCost()) ||
	                    projectSpecsEMDetails.getLocalMgmtCostType()!=projectSpecsEMDetails_DB.getLocalMgmtCostType())
	            {
	                String currency = "";
	                if(projectSpecsEMDetails.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getLocalMgmtCostType().intValue()))
	                {
	                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getLocalMgmtCostType().intValue());
	                }
	
	                String currency_DB = "";
	                if(projectSpecsEMDetails_DB.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getLocalMgmtCostType().intValue()))
	                {
	                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getLocalMgmtCostType().intValue());
	                }
	
	                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getLocalMgmtCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getLocalMgmtCost()), currency_DB));
	            }
	
	            //Fieldwork Cost
	            if(!isEqual(projectSpecsEMDetails.getFieldworkCost(), projectSpecsEMDetails_DB.getFieldworkCost()) ||
	                    projectSpecsEMDetails.getFieldworkCostType()!=projectSpecsEMDetails_DB.getFieldworkCostType())
	            {
	                String currency = "";
	                if(projectSpecsEMDetails.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getFieldworkCostType().intValue()))
	                {
	                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getFieldworkCostType().intValue());
	                }
	
	                String currency_DB = "";
	                if(projectSpecsEMDetails_DB.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getFieldworkCostType().intValue()))
	                {
	                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getFieldworkCostType().intValue());
	                }
	
	                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMFWCOST.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getFieldworkCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getFieldworkCost()), currency_DB));
	            }
	
	            //Operational Hub Cost
	            if(!isEqual(projectSpecsEMDetails.getOperationalHubCost(), projectSpecsEMDetails_DB.getOperationalHubCost()) ||
	                    projectSpecsEMDetails.getOperationalHubCostType()!=projectSpecsEMDetails_DB.getOperationalHubCostType())
	            {
	                String currency = "";
	                if(projectSpecsEMDetails.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getOperationalHubCostType().intValue()))
	                {
	                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getOperationalHubCostType().intValue());
	                }
	
	                String currency_DB = "";
	                if(projectSpecsEMDetails_DB.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getOperationalHubCostType().intValue()))
	                {
	                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getOperationalHubCostType().intValue());
	                }
	
	                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getOperationalHubCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getOperationalHubCost()), currency_DB));
	            }
	
	            //Other Cost
	            if(!isEqual(projectSpecsEMDetails.getOtherCost(), projectSpecsEMDetails_DB.getOtherCost()) ||
	                    projectSpecsEMDetails.getOtherCostType()!=projectSpecsEMDetails_DB.getOtherCostType())
	            {
	                String currency = "";
	                if(projectSpecsEMDetails.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getOtherCostType().intValue()))
	                {
	                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getOtherCostType().intValue());
	                }
	
	                String currency_DB = "";
	                if(projectSpecsEMDetails_DB.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getOtherCostType().intValue()))
	                {
	                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getOtherCostType().intValue());
	                }
	
	                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMOTHERCOST.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getOtherCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getOtherCost()), currency_DB));
	            }
	
	
	            //Name of Proposed Fieldwork Agencies
	            if(!StringUtils.equals(projectSpecsEMDetails.getProposedFWAgencyNames(), projectSpecsEMDetails_DB.getProposedFWAgencyNames()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMPROPFWAGNCY.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getProposedFWAgencyNames()), ObjStr(projectSpecsEMDetails_DB.getProposedFWAgencyNames())));
	            }
	
	            //Estimated Fieldwork Start
	            if(!isEqual(projectSpecsEMDetails.getFwStartDate(), projectSpecsEMDetails_DB.getFwStartDate()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWSTART.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getFwStartDate()), ObjStr(projectSpecsEMDetails_DB.getFwStartDate())));
	            }
	
	            //Estimated Fieldwork Completion
	            if(!isEqual(projectSpecsEMDetails.getFwEndDate(), projectSpecsEMDetails_DB.getFwEndDate()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWCOMP.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getFwEndDate()), ObjStr(projectSpecsEMDetails_DB.getFwEndDate())));
	            }
	            //Data Collection Methods
	            if(!isEqual(projectSpecsEMDetails.getDataCollectionMethod(), projectSpecsEMDetails_DB.getDataCollectionMethod()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMDCMEH.getDescription(),
	                        ObjStr(getDataCollectionNames(projectSpecsEMDetails.getDataCollectionMethod())),
	                        ObjStr(getDataCollectionNames(projectSpecsEMDetails_DB.getDataCollectionMethod()))));
	            }
	
	            // Quantitative-Total Number of Interviews		
	            if(!isEqual(projectSpecsEMDetails.getTotalNoInterviews(), projectSpecsEMDetails_DB.getTotalNoInterviews()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTINT.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getTotalNoInterviews()), ObjStr(projectSpecsEMDetails_DB.getTotalNoInterviews())));
	            }
	
	            //Quantitative-Total Number of Visits per Respondent
	            if(!isEqual(projectSpecsEMDetails.getTotalNoOfVisits(), projectSpecsEMDetails_DB.getTotalNoOfVisits()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTVIS.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getTotalNoOfVisits()), ObjStr(projectSpecsEMDetails_DB.getTotalNoOfVisits())));
	            }
	
	            //Quantitative-Average Interview Duration
	            if(!isEqual(projectSpecsEMDetails.getAvIntDuration(), projectSpecsEMDetails_DB.getAvIntDuration()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTAVGDUR.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getAvIntDuration()), ObjStr(projectSpecsEMDetails_DB.getAvIntDuration())));
	            }
            }
            //Quantitative-Geographical Spread-Non-National-Geography
            //TODO


        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    /**
     * MultiMarket
     * @param projectSpecsInitiation_DB
     * @param projectSpecsInitiation
     * @param projectSpecsEMDetails_DB
     * @param projectSpecsEMDetails
     * @param projectSpecsReporting_DB
     * @param endMarketDetails_DB
     * @param projectInitiation_DB
     * @param isMultimarket
     * @return
     */
    public static String generateProjectSpecsMultiMarketUpdateJSONObject(final ProjectSpecsInitiation projectSpecsInitiation_DB,
                                                                         final ProjectSpecsInitiation projectSpecsInitiation, final ProjectSpecsEndMarketDetails projectSpecsEMDetails_DB,
                                                                         final ProjectSpecsEndMarketDetails projectSpecsEMDetails, final ProjectSpecsReporting projectSpecsReporting_DB,
                                                                         final List<EndMarketInvestmentDetail> endMarketDetails_DB, final ProjectInitiation projectInitiation_DB, final Long endmarketID, final User user)
    {

        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(projectSpecsInitiation_DB!=null && projectSpecsInitiation!=null)
        {

            //Brand / Non-Branded 
            if(projectSpecsInitiation.getBrand()!=projectSpecsInitiation_DB.getBrand())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.BRAND.getDescription(),
                        SynchroGlobal.getBrands().get(projectSpecsInitiation.getBrand().intValue()),
                        SynchroGlobal.getBrands().get(projectSpecsInitiation_DB.getBrand().intValue())));
            }

            //Methodology Type
            if(projectSpecsInitiation.getMethodologyType()!=projectSpecsInitiation_DB.getMethodologyType())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHTYPE.getDescription(),
                        SynchroGlobal.getMethodologies().get(projectSpecsInitiation.getMethodologyType().intValue()),
                        SynchroGlobal.getMethodologies().get(projectSpecsInitiation_DB.getMethodologyType().intValue())));
            }

            //Methodology Group
            if(projectSpecsInitiation.getMethodologyGroup()!=projectSpecsInitiation_DB.getMethodologyGroup())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHGROUP.getDescription(),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(projectSpecsInitiation.getMethodologyGroup().intValue()),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(projectSpecsInitiation_DB.getMethodologyGroup().intValue())));
            }


            //Project Start Date
            if(!isEqual(projectSpecsInitiation.getStartDate(), projectSpecsInitiation_DB.getStartDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STARTDATE.getDescription(),
                        ObjStr(projectSpecsInitiation.getStartDate()), ObjStr(projectSpecsInitiation_DB.getStartDate())));
            }

            //Project End Date
            if(!isEqual(projectSpecsInitiation.getEndDate(), projectSpecsInitiation_DB.getEndDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.ENDDATE.getDescription(),
                        ObjStr(projectSpecsInitiation.getEndDate()), ObjStr(projectSpecsInitiation_DB.getEndDate())));
            }


            //NPI Number
            if(!StringUtils.equals(projectSpecsInitiation.getNpiReferenceNo(), projectSpecsInitiation_DB.getNpiReferenceNo()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.NPINO.getDescription(),
                        ObjStr(projectSpecsInitiation.getNpiReferenceNo()), ObjStr(projectSpecsInitiation_DB.getNpiReferenceNo())));
            }

            if(endmarketID!=null && isEqual(endmarketID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                //Request for Methodology Waiver
                if(!isEqual(projectSpecsInitiation.getDeviationFromSM(), projectSpecsInitiation_DB.getDeviationFromSM()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.REQMETHWAIVER.getDescription(),
                            (projectSpecsInitiation.getDeviationFromSM().intValue()==0?"No":"Yes"), (projectSpecsInitiation_DB.getDeviationFromSM().intValue()==0?"No":"Yes")));
                }
            }

            //PO Number
            if(!StringUtils.equals(projectSpecsInitiation.getPoNumber(), projectSpecsInitiation_DB.getPoNumber()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PONUMBER.getDescription(),
                        ObjStr(projectSpecsInitiation.getPoNumber()), ObjStr(projectSpecsInitiation_DB.getPoNumber())));
            }

            //PO Number 2
            if(!StringUtils.equals(projectSpecsInitiation.getPoNumber1(), projectSpecsInitiation_DB.getPoNumber1()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PONUMBER.getDescription(),
                        ObjStr(projectSpecsInitiation.getPoNumber1()), ObjStr(projectSpecsInitiation_DB.getPoNumber1())));
            }

            //Project Description
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getDescription()), html2text(projectSpecsInitiation_DB.getDescription())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(),
                        ObjStr(projectSpecsInitiation.getDescription()), ObjStr(projectSpecsInitiation_DB.getDescription())));
            }

            //Business Question
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getBizQuestion()), html2text(projectSpecsInitiation_DB.getBizQuestion())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(),
                        ObjStr(projectSpecsInitiation.getBizQuestion()), ObjStr(projectSpecsInitiation_DB.getBizQuestion())));
            }
            //Research Objectives
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getResearchObjective()), html2text(projectSpecsInitiation_DB.getResearchObjective())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESOBJ.getDescription(),
                        ObjStr(projectSpecsInitiation.getResearchObjective()), ObjStr(projectSpecsInitiation_DB.getResearchObjective())));
            }
            //Action Standards
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getActionStandard()), html2text(projectSpecsInitiation_DB.getActionStandard())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTD.getDescription(),
                        ObjStr(projectSpecsInitiation.getActionStandard()), ObjStr(projectSpecsInitiation_DB.getActionStandard())));

            }
            //Methodology Approach and Research Design
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getResearchDesign()), html2text(projectSpecsInitiation_DB.getResearchDesign())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESDSGN.getDescription(),
                        ObjStr(projectSpecsInitiation.getResearchDesign()), ObjStr(projectSpecsInitiation_DB.getResearchDesign())));
            }
            //Sample Profile (Research)
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getSampleProfile()), html2text(projectSpecsInitiation_DB.getSampleProfile())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SMPLPROFILE.getDescription(),
                        ObjStr(projectSpecsInitiation.getSampleProfile()), ObjStr(projectSpecsInitiation_DB.getSampleProfile())));
            }
            //Stimulus Material
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getStimulusMaterial()), html2text(projectSpecsInitiation_DB.getStimulusMaterial())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(),
                        ObjStr(projectSpecsInitiation.getStimulusMaterial()), ObjStr(projectSpecsInitiation_DB.getStimulusMaterial())));
            }

            //Stimulus Material to be shipped to
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getStimulusMaterialShipped()), html2text(projectSpecsInitiation_DB.getStimulusMaterialShipped())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMMATSHIPPED.getDescription(),
                        ObjStr(projectSpecsInitiation.getStimulusMaterialShipped()), ObjStr(projectSpecsInitiation_DB.getStimulusMaterialShipped())));
            }

            //Other Comments
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getOthers()), html2text(projectSpecsInitiation_DB.getOthers())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHER.getDescription(),
                        ObjStr(projectSpecsInitiation.getOthers()), ObjStr(projectSpecsInitiation_DB.getOthers())));
            }

            //Date Stimuli
            if(!isEqual(projectSpecsInitiation.getStimuliDate(), projectSpecsInitiation_DB.getStimuliDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(),
                        ObjStr(projectSpecsInitiation.getStimuliDate()), ObjStr(projectSpecsInitiation_DB.getStimuliDate())));
            }


            //PIB Reporting
            if(projectSpecsReporting_DB!=null && SynchroPermHelper.isSynchroAdmin(user))
            {
                //Textfield: Other Reporting Requirements
                if(!StringUtils.equals(html2text(projectSpecsInitiation.getOtherReportingRequirements()), html2text(projectSpecsReporting_DB.getOtherReportingRequirements())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHERREP.getDescription(),
                            ObjStr(projectSpecsInitiation.getOtherReportingRequirements()), ObjStr(projectSpecsInitiation_DB.getOtherReportingRequirements())));
                }
                //Checkbox Reporting Requirement:Full Report
                if(!isEqual(projectSpecsInitiation.getTopLinePresentation(), projectSpecsReporting_DB.getTopLinePresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPTOPLINE.getDescription(),
                            getBoolean(projectSpecsInitiation.getTopLinePresentation())?"Selected":"Unselected"));
                }

                //Checkbox Reporting Requirement:Presentation
                if(!isEqual(projectSpecsInitiation.getPresentation(), projectSpecsReporting_DB.getPresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPPRES.getDescription(),
                            getBoolean(projectSpecsInitiation.getPresentation())?"Selected":"Unselected"));
                }
                //Checkbox Reporting Requirement:Topline Presentation
                if(!isEqual(projectSpecsInitiation.getFullreport(), projectSpecsReporting_DB.getFullreport()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPFULL.getDescription(),
                            getBoolean(projectSpecsInitiation.getFullreport())?"Selected":"Unselected"));
                }
            }

            //End-Market fields
            if(endmarketID!=null && !isEqual(endmarketID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                //Total Cost
                if(!isEqual(projectSpecsEMDetails.getTotalCost(), projectSpecsEMDetails_DB.getTotalCost()) ||
                        projectSpecsEMDetails.getTotalCostType()!=projectSpecsEMDetails_DB.getTotalCostType())
                {
                    String currency = "";
                    if(projectSpecsEMDetails.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getTotalCostType().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getTotalCostType().intValue());
                    }

                    String currency_DB = "";
                    if(projectSpecsEMDetails_DB.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getTotalCostType().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getTotalCostType().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMTOTALCOST.getDescription(),
                            ObjStr(projectSpecsEMDetails.getTotalCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getTotalCost()), currency_DB));
                }


                //International Management Cost - Research Hub Cost
                if(!isEqual(projectSpecsEMDetails.getIntMgmtCost(), projectSpecsEMDetails_DB.getIntMgmtCost()) ||
                        projectSpecsEMDetails.getIntMgmtCostType()!=projectSpecsEMDetails_DB.getIntMgmtCostType())
                {
                    String currency = "";
                    if(projectSpecsEMDetails.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getIntMgmtCostType().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getIntMgmtCostType().intValue());
                    }

                    String currency_DB = "";
                    if(projectSpecsEMDetails_DB.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getIntMgmtCostType().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getIntMgmtCostType().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMINTMGTCOST.getDescription(),
                            ObjStr(projectSpecsEMDetails.getIntMgmtCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getIntMgmtCost()), currency_DB));
                }

                //Local Management Cost
                if(!isEqual(projectSpecsEMDetails.getLocalMgmtCost(), projectSpecsEMDetails_DB.getLocalMgmtCost()) ||
                        projectSpecsEMDetails.getLocalMgmtCostType()!=projectSpecsEMDetails_DB.getLocalMgmtCostType())
                {
                    String currency = "";
                    if(projectSpecsEMDetails.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getLocalMgmtCostType().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getLocalMgmtCostType().intValue());
                    }

                    String currency_DB = "";
                    if(projectSpecsEMDetails_DB.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getLocalMgmtCostType().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getLocalMgmtCostType().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(),
                            ObjStr(projectSpecsEMDetails.getLocalMgmtCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getLocalMgmtCost()), currency_DB));
                }

                //Fieldwork Cost
                if(!isEqual(projectSpecsEMDetails.getFieldworkCost(), projectSpecsEMDetails_DB.getFieldworkCost()) ||
                        projectSpecsEMDetails.getFieldworkCostType()!=projectSpecsEMDetails_DB.getFieldworkCostType())
                {
                    String currency = "";
                    if(projectSpecsEMDetails.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getFieldworkCostType().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getFieldworkCostType().intValue());
                    }

                    String currency_DB = "";
                    if(projectSpecsEMDetails_DB.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getFieldworkCostType().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getFieldworkCostType().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMFWCOST.getDescription(),
                            ObjStr(projectSpecsEMDetails.getFieldworkCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getFieldworkCost()), currency_DB));
                }

                //Operational Hub Cost
                if(!isEqual(projectSpecsEMDetails.getOperationalHubCost(), projectSpecsEMDetails_DB.getOperationalHubCost()) ||
                        projectSpecsEMDetails.getOperationalHubCostType()!=projectSpecsEMDetails_DB.getOperationalHubCostType())
                {
                    String currency = "";
                    if(projectSpecsEMDetails.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getOperationalHubCostType().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getOperationalHubCostType().intValue());
                    }

                    String currency_DB = "";
                    if(projectSpecsEMDetails_DB.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getOperationalHubCostType().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getOperationalHubCostType().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(),
                            ObjStr(projectSpecsEMDetails.getOperationalHubCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getOperationalHubCost()), currency_DB));
                }

                //Other Cost
                if(!isEqual(projectSpecsEMDetails.getOtherCost(), projectSpecsEMDetails_DB.getOtherCost()) ||
                        projectSpecsEMDetails.getOtherCostType()!=projectSpecsEMDetails_DB.getOtherCostType())
                {
                    String currency = "";
                    if(projectSpecsEMDetails.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getOtherCostType().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getOtherCostType().intValue());
                    }

                    String currency_DB = "";
                    if(projectSpecsEMDetails_DB.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getOtherCostType().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getOtherCostType().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMOTHERCOST.getDescription(),
                            ObjStr(projectSpecsEMDetails.getOtherCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getOtherCost()), currency_DB));
                }


                //Name of Proposed Fieldwork Agencies
                if(!StringUtils.equals(projectSpecsEMDetails.getProposedFWAgencyNames(), projectSpecsEMDetails_DB.getProposedFWAgencyNames()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMPROPFWAGNCY.getDescription(),
                            ObjStr(projectSpecsEMDetails.getProposedFWAgencyNames()), ObjStr(projectSpecsEMDetails_DB.getProposedFWAgencyNames())));
                }

                //Estimated Fieldwork Start
                if(!isEqual(projectSpecsEMDetails.getFwStartDate(), projectSpecsEMDetails_DB.getFwStartDate()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWSTART.getDescription(),
                            ObjStr(projectSpecsEMDetails.getFwStartDate()), ObjStr(projectSpecsEMDetails_DB.getFwStartDate())));
                }

                //Estimated Fieldwork Completion
                if(!isEqual(projectSpecsEMDetails.getFwEndDate(), projectSpecsEMDetails_DB.getFwEndDate()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWCOMP.getDescription(),
                            ObjStr(projectSpecsEMDetails.getFwEndDate()), ObjStr(projectSpecsEMDetails_DB.getFwEndDate())));
                }
                //Data Collection Methods
                if(!isEqual(projectSpecsEMDetails.getDataCollectionMethod(), projectSpecsEMDetails_DB.getDataCollectionMethod()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMDCMEH.getDescription(),
                            ObjStr(getDataCollectionNames(projectSpecsEMDetails.getDataCollectionMethod())),
                            ObjStr(getDataCollectionNames(projectSpecsEMDetails_DB.getDataCollectionMethod()))));
                }

                // Quantitative-Total Number of Interviews		
                if(!isEqual(projectSpecsEMDetails.getTotalNoInterviews(), projectSpecsEMDetails_DB.getTotalNoInterviews()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTINT.getDescription(),
                            ObjStr(projectSpecsEMDetails.getTotalNoInterviews()), ObjStr(projectSpecsEMDetails_DB.getTotalNoInterviews())));
                }

                //Quantitative-Total Number of Visits per Respondent
                if(!isEqual(projectSpecsEMDetails.getTotalNoOfVisits(), projectSpecsEMDetails_DB.getTotalNoOfVisits()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTVIS.getDescription(),
                            ObjStr(projectSpecsEMDetails.getTotalNoOfVisits()), ObjStr(projectSpecsEMDetails_DB.getTotalNoOfVisits())));
                }

                //Quantitative-Average Interview Duration
                if(!isEqual(projectSpecsEMDetails.getAvIntDuration(), projectSpecsEMDetails_DB.getAvIntDuration()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTAVGDUR.getDescription(),
                            ObjStr(projectSpecsEMDetails.getAvIntDuration()), ObjStr(projectSpecsEMDetails_DB.getAvIntDuration())));
                }

                //Quantitative-Geographical Spread-Non-National-Geography
                //TODO
            }

        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    /**
     * Single Market
     * @param proposalInitiation_DB
     * @param proposalInitiation
     * @param proposalReporting_DB
     * @param proposalEMDetails
     * @param proposalEMDetails_DB
     * @return
     */
    public static String generateProposalAllUpdateJSONObject(final ProposalInitiation proposalInitiation_DB, final ProposalInitiation proposalInitiation, final ProposalReporting proposalReporting_DB, final ProposalEndMarketDetails proposalEMDetails, final ProposalEndMarketDetails proposalEMDetails_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(proposalInitiation_DB!=null && proposalInitiation!=null)
        {

            //Brand / Non-Branded 
            if(proposalInitiation.getBrand()!=proposalInitiation_DB.getBrand())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.BRAND.getDescription(),
                        SynchroGlobal.getBrands().get(proposalInitiation.getBrand().intValue()), SynchroGlobal.getBrands().get(proposalInitiation_DB.getBrand().intValue())));
            }

            //Country 
            if(proposalInitiation.getEndMarketID()!=proposalInitiation_DB.getEndMarketID())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.COUNTRY.getDescription(),
                        SynchroGlobal.getEndMarkets().get(new Long(proposalInitiation.getEndMarketID()).intValue()), SynchroGlobal.getEndMarkets().get(new Long(proposalInitiation_DB.getEndMarketID()).intValue())));
            }
            //Methodology Type
            if(proposalInitiation.getMethodologyType()!=proposalInitiation_DB.getMethodologyType())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHTYPE.getDescription(),
                        SynchroGlobal.getMethodologies().get(proposalInitiation.getMethodologyType().intValue()), SynchroGlobal.getMethodologies().get(proposalInitiation_DB.getMethodologyType().intValue())));
            }

            //Methodology Group
            if(proposalInitiation.getMethodologyGroup()!=proposalInitiation_DB.getMethodologyGroup())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHGROUP.getDescription(),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(proposalInitiation.getMethodologyGroup().intValue()),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(proposalInitiation_DB.getMethodologyGroup().intValue())));
            }


            //Project Owner
            if(!isEqual(proposalInitiation.getProjectOwner(), proposalInitiation_DB.getProjectOwner()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(), getUserName(proposalInitiation.getProjectOwner()), getUserName(proposalInitiation_DB.getProjectOwner())));
            }


            //Project Contact/ SPI Contact
            if(!isEqual(proposalInitiation.getSpiContact(), proposalInitiation.getSpiContact()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(), getUserName(proposalInitiation.getSpiContact()), getUserName(proposalInitiation_DB.getSpiContact())));
            }


            //Project Start Date
            if(!isEqual(proposalInitiation.getStartDate(), proposalInitiation_DB.getStartDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STARTDATE.getDescription(), ObjStr(proposalInitiation.getStartDate()), ObjStr(proposalInitiation_DB.getStartDate())));
            }

            //Project End Date
            if(!isEqual(proposalInitiation.getEndDate(), proposalInitiation_DB.getEndDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.ENDDATE.getDescription(), ObjStr(proposalInitiation.getEndDate()), ObjStr(proposalInitiation_DB.getEndDate())));
            }


            //NPI Number
            if(!StringUtils.equals(proposalInitiation.getNpiReferenceNo(), proposalInitiation_DB.getNpiReferenceNo()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.NPINO.getDescription(), ObjStr(proposalInitiation.getNpiReferenceNo()), ObjStr(proposalInitiation_DB.getNpiReferenceNo())));
            }

            /* READONLY Field
		//Request for Methodology Waiver
		if(!isEqual(projectInitiation.getDeviationFromSM(), projectInitiation_DB.getDeviationFromSM()))
		{
			data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.REQMETHWAIVER.getDescription(), 
											(projectInitiation.getDeviationFromSM().intValue()==0?"No":"Yes"), (projectInitiation_DB.getDeviationFromSM().intValue()==0?"No":"Yes")));
		}*/

            //Business Question
            if(!StringUtils.equals(html2text(proposalInitiation.getBizQuestion()), html2text(proposalInitiation_DB.getBizQuestion())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(), ObjStr(proposalInitiation.getBizQuestion()), ObjStr(proposalInitiation_DB.getBizQuestion())));
            }
            //Research Objectives
            if(!StringUtils.equals(html2text(proposalInitiation.getResearchObjective()), html2text(proposalInitiation_DB.getResearchObjective())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESOBJ.getDescription(), ObjStr(proposalInitiation.getResearchObjective()), ObjStr(proposalInitiation_DB.getResearchObjective())));
            }
            //Action Standards
            if(!StringUtils.equals(html2text(proposalInitiation.getActionStandard()), html2text(proposalInitiation_DB.getActionStandard())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTD.getDescription(), ObjStr(proposalInitiation.getActionStandard()), ObjStr(proposalInitiation_DB.getActionStandard())));
            }
            //Methodology Approach and Research Design
            if(!StringUtils.equals(html2text(proposalInitiation.getResearchDesign()), html2text(proposalInitiation_DB.getResearchDesign())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESDSGN.getDescription(), ObjStr(proposalInitiation.getResearchDesign()), ObjStr(proposalInitiation_DB.getResearchDesign())));
            }
            //Sample Profile (Research)
            if(!StringUtils.equals(html2text(proposalInitiation.getSampleProfile()), html2text(proposalInitiation_DB.getSampleProfile())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SMPLPROFILE.getDescription(), ObjStr(proposalInitiation.getSampleProfile()), ObjStr(proposalInitiation_DB.getSampleProfile())));
            }
            //Stimulus Material
            if(!StringUtils.equals(html2text(proposalInitiation.getStimulusMaterial()), html2text(proposalInitiation_DB.getStimulusMaterial())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(), ObjStr(proposalInitiation.getStimulusMaterial()), ObjStr(proposalInitiation_DB.getStimulusMaterial())));
            }
            //Other Comments
            if(!StringUtils.equals(html2text(proposalInitiation.getOthers()), html2text(proposalInitiation_DB.getOthers())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHER.getDescription(), ObjStr(proposalInitiation.getOthers()), ObjStr(proposalInitiation_DB.getOthers())));
            }

            //Date Stimuli
            if(!isEqual(proposalInitiation.getStimuliDate(), proposalInitiation_DB.getStimuliDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(), ObjStr(proposalInitiation.getStimuliDate()), ObjStr(proposalInitiation_DB.getStimuliDate())));
            }


            //PIB Reporting
            if(proposalReporting_DB!=null)
            {
                //Textfield: Other Reporting Requirements
                if(!StringUtils.equals(html2text(proposalInitiation.getOtherReportingRequirements()), html2text(proposalReporting_DB.getOtherReportingRequirements())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHERREP.getDescription(), ObjStr(proposalInitiation.getOtherReportingRequirements()), ObjStr(proposalReporting_DB.getOtherReportingRequirements())));
                }
                //Checkbox Reporting Requirement:Full Report
                if(!isEqual(proposalInitiation.getTopLinePresentation(), proposalReporting_DB.getTopLinePresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPTOPLINE.getDescription(),getBoolean(proposalInitiation.getTopLinePresentation())?"Selected":"Unselected"));
                }

                //Checkbox Reporting Requirement:Presentation
                if(!isEqual(proposalInitiation.getPresentation(), proposalReporting_DB.getPresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPPRES.getDescription(), getBoolean(proposalInitiation.getPresentation())?"Selected":"Unselected"));
                }
                //Checkbox Reporting Requirement:Topline Presentation
                if(!isEqual(proposalInitiation.getFullreport(), proposalReporting_DB.getFullreport()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPFULL.getDescription(), getBoolean(proposalInitiation.getFullreport())?"Selected":"Unselected"));
                }
            }

            //END-Market Fields
            //Stimulus Material to be shipped to
            if(!StringUtils.equals(html2text(proposalInitiation.getStimulusMaterialShipped()), html2text(proposalInitiation_DB.getStimulusMaterialShipped())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMMATSHIPPED.getDescription(), ObjStr(proposalInitiation.getStimulusMaterialShipped()), ObjStr(proposalInitiation_DB.getStimulusMaterialShipped())));
            }

            //Total Cost
            if(!isEqual(proposalEMDetails.getTotalCost(), proposalEMDetails_DB.getTotalCost()) || proposalEMDetails.getTotalCostType()!=proposalEMDetails_DB.getTotalCostType())
            {
                String currency = "";
                if(proposalEMDetails.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getTotalCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getTotalCostType().intValue());
                }

                String currency_DB = "";
                if(proposalEMDetails_DB.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails_DB.getTotalCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(proposalEMDetails_DB.getTotalCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMTOTALCOST.getDescription(), ObjStr(proposalEMDetails.getTotalCost()) ,currency, ObjStr(proposalEMDetails_DB.getTotalCost()), currency_DB));
            }


            //International Management Cost - Research Hub Cost
            if(!isEqual(proposalEMDetails.getIntMgmtCost(), proposalEMDetails_DB.getIntMgmtCost()) || proposalEMDetails.getIntMgmtCostType()!=proposalEMDetails_DB.getIntMgmtCostType())
            {
                String currency = "";
                if(proposalEMDetails.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getIntMgmtCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getIntMgmtCostType().intValue());
                }

                String currency_DB = "";
                if(proposalEMDetails_DB.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails_DB.getIntMgmtCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(proposalEMDetails_DB.getIntMgmtCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMINTMGTCOST.getDescription(), ObjStr(proposalEMDetails.getIntMgmtCost()) ,currency, ObjStr(proposalEMDetails_DB.getIntMgmtCost()), currency_DB));
            }

            //Local Management Cost
            if(!isEqual(proposalEMDetails.getLocalMgmtCost(), proposalEMDetails_DB.getLocalMgmtCost()) || proposalEMDetails.getLocalMgmtCostType()!=proposalEMDetails_DB.getLocalMgmtCostType())
            {
                String currency = "";
                if(proposalEMDetails.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getLocalMgmtCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getLocalMgmtCostType().intValue());
                }

                String currency_DB = "";
                if(proposalEMDetails_DB.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails_DB.getLocalMgmtCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(proposalEMDetails_DB.getLocalMgmtCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(), ObjStr(proposalEMDetails.getLocalMgmtCost()) ,currency, ObjStr(proposalEMDetails_DB.getLocalMgmtCost()), currency_DB));
            }

            //Fieldwork Cost
            if(!isEqual(proposalEMDetails.getFieldworkCost(), proposalEMDetails_DB.getFieldworkCost()) || proposalEMDetails.getFieldworkCostType()!=proposalEMDetails_DB.getFieldworkCostType())
            {
                String currency = "";
                if(proposalEMDetails.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getFieldworkCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getFieldworkCostType().intValue());
                }

                String currency_DB = "";
                if(proposalEMDetails_DB.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails_DB.getFieldworkCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(proposalEMDetails_DB.getFieldworkCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMFWCOST.getDescription(), ObjStr(proposalEMDetails.getFieldworkCost()) ,currency, ObjStr(proposalEMDetails_DB.getFieldworkCost()), currency_DB));
            }

            //Operational Hub Cost
            if(!isEqual(proposalEMDetails.getOperationalHubCost(), proposalEMDetails_DB.getOperationalHubCost()) || proposalEMDetails.getOperationalHubCostType()!=proposalEMDetails_DB.getOperationalHubCostType())
            {
                String currency = "";
                if(proposalEMDetails.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getOperationalHubCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getOperationalHubCostType().intValue());
                }

                String currency_DB = "";
                if(proposalEMDetails_DB.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails_DB.getOperationalHubCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(proposalEMDetails_DB.getOperationalHubCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(), ObjStr(proposalEMDetails.getOperationalHubCost()) ,currency, ObjStr(proposalEMDetails_DB.getOperationalHubCost()), currency_DB));
            }

            //Other Cost
            if(!isEqual(proposalEMDetails.getOtherCost(), proposalEMDetails_DB.getOtherCost()) || proposalEMDetails.getOtherCostType()!=proposalEMDetails_DB.getOtherCostType())
            {
                String currency = "";
                if(proposalEMDetails.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getOtherCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getOtherCostType().intValue());
                }

                String currency_DB = "";
                if(proposalEMDetails_DB.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails_DB.getOtherCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(proposalEMDetails_DB.getOtherCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMOTHERCOST.getDescription(), ObjStr(proposalEMDetails.getOtherCost()) ,currency, ObjStr(proposalEMDetails_DB.getOtherCost()), currency_DB));
            }


            //Name of Proposed Fieldwork Agencies
            if(!StringUtils.equals(proposalEMDetails.getProposedFWAgencyNames(), proposalEMDetails_DB.getProposedFWAgencyNames()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMPROPFWAGNCY.getDescription(), ObjStr(proposalEMDetails.getProposedFWAgencyNames()), ObjStr(proposalEMDetails_DB.getProposedFWAgencyNames())));
            }

            //Estimated Fieldwork Start
            if(!isEqual(proposalEMDetails.getFwStartDate(), proposalEMDetails_DB.getFwStartDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWSTART.getDescription(), ObjStr(proposalEMDetails.getFwStartDate()), ObjStr(proposalEMDetails_DB.getFwStartDate())));
            }

            //Estimated Fieldwork Completion
            if(!isEqual(proposalEMDetails.getFwEndDate(), proposalEMDetails_DB.getFwEndDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWCOMP.getDescription(), ObjStr(proposalEMDetails.getFwEndDate()), ObjStr(proposalEMDetails_DB.getFwEndDate())));
            }
            //Data Collection Methods
            if(!isEqual(proposalEMDetails.getDataCollectionMethod(), proposalEMDetails_DB.getDataCollectionMethod()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMDCMEH.getDescription(), ObjStr(getDataCollectionNames(proposalEMDetails.getDataCollectionMethod())), ObjStr(getDataCollectionNames(proposalEMDetails_DB.getDataCollectionMethod()))));
            }

            // Quantitative-Total Number of Interviews		
            if(!isEqual(proposalEMDetails.getTotalNoInterviews(), proposalEMDetails_DB.getTotalNoInterviews()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTINT.getDescription(), ObjStr(proposalEMDetails.getTotalNoInterviews()), ObjStr(proposalEMDetails_DB.getTotalNoInterviews())));
            }

            //Quantitative-Total Number of Visits per Respondent
            if(!isEqual(proposalEMDetails.getTotalNoOfVisits(), proposalEMDetails_DB.getTotalNoOfVisits()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTVIS.getDescription(), ObjStr(proposalEMDetails.getTotalNoOfVisits()), ObjStr(proposalEMDetails_DB.getTotalNoOfVisits())));
            }

            //Quantitative-Average Interview Duration
            if(!isEqual(proposalEMDetails.getAvIntDuration(), proposalEMDetails_DB.getAvIntDuration()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTAVGDUR.getDescription(), ObjStr(proposalEMDetails.getAvIntDuration()), ObjStr(proposalEMDetails_DB.getAvIntDuration())));
            }

            //Quantitative-Geographical Spread-Non-National-Geography
            //TODO

            //Proposal and Cost Template
            if(!StringUtils.equals(html2text(proposalInitiation.getProposalCostTemplate()), html2text(proposalInitiation_DB.getProposalCostTemplate())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PROPCOSTTEMP.getDescription(), ObjStr(proposalInitiation.getProposalCostTemplate()), ObjStr(proposalInitiation_DB.getProposalCostTemplate())));
            }


        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    public static String generateProposalEMUpdateJSONObject(final ProposalEndMarketDetails proposalEMDetails, final ProposalEndMarketDetails proposalEMDetails_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(proposalEMDetails!=null && proposalEMDetails_DB!=null)
        {

            //Total Cost
            if(!isEqual(proposalEMDetails.getTotalCost(), proposalEMDetails_DB.getTotalCost()) || proposalEMDetails.getTotalCostType()!=proposalEMDetails_DB.getTotalCostType())
            {
                String currency = "";
                if(proposalEMDetails.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getTotalCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getTotalCostType().intValue());
                }

                String currency_DB = "";
                if(proposalEMDetails_DB.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails_DB.getTotalCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(proposalEMDetails_DB.getTotalCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMTOTALCOST.getDescription(), ObjStr(proposalEMDetails.getTotalCost()) ,currency, ObjStr(proposalEMDetails_DB.getTotalCost()), currency_DB));
            }


            //International Management Cost - Research Hub Cost
            if(!isEqual(proposalEMDetails.getIntMgmtCost(), proposalEMDetails_DB.getIntMgmtCost()) || proposalEMDetails.getIntMgmtCostType()!=proposalEMDetails_DB.getIntMgmtCostType())
            {
                String currency = "";
                if(proposalEMDetails.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getIntMgmtCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getIntMgmtCostType().intValue());
                }

                String currency_DB = "";
                if(proposalEMDetails_DB.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails_DB.getIntMgmtCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(proposalEMDetails_DB.getIntMgmtCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMINTMGTCOST.getDescription(), ObjStr(proposalEMDetails.getIntMgmtCost()) ,currency, ObjStr(proposalEMDetails_DB.getIntMgmtCost()), currency_DB));
            }

            //Local Management Cost
            if(!isEqual(proposalEMDetails.getLocalMgmtCost(), proposalEMDetails_DB.getLocalMgmtCost()) || proposalEMDetails.getLocalMgmtCostType()!=proposalEMDetails_DB.getLocalMgmtCostType())
            {
                String currency = "";
                if(proposalEMDetails.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getLocalMgmtCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getLocalMgmtCostType().intValue());
                }

                String currency_DB = "";
                if(proposalEMDetails_DB.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails_DB.getLocalMgmtCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(proposalEMDetails_DB.getLocalMgmtCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(), ObjStr(proposalEMDetails.getLocalMgmtCost()) ,currency, ObjStr(proposalEMDetails_DB.getLocalMgmtCost()), currency_DB));
            }

            //Fieldwork Cost
            if(!isEqual(proposalEMDetails.getFieldworkCost(), proposalEMDetails_DB.getFieldworkCost()) || proposalEMDetails.getFieldworkCostType()!=proposalEMDetails_DB.getFieldworkCostType())
            {
                String currency = "";
                if(proposalEMDetails.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getFieldworkCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getFieldworkCostType().intValue());
                }

                String currency_DB = "";
                if(proposalEMDetails_DB.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails_DB.getFieldworkCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(proposalEMDetails_DB.getFieldworkCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMFWCOST.getDescription(), ObjStr(proposalEMDetails.getFieldworkCost()) ,currency, ObjStr(proposalEMDetails_DB.getFieldworkCost()), currency_DB));
            }

            //Operational Hub Cost
            if(!isEqual(proposalEMDetails.getOperationalHubCost(), proposalEMDetails_DB.getOperationalHubCost()) || proposalEMDetails.getOperationalHubCostType()!=proposalEMDetails_DB.getOperationalHubCostType())
            {
                String currency = "";
                if(proposalEMDetails.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getOperationalHubCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getOperationalHubCostType().intValue());
                }

                String currency_DB = "";
                if(proposalEMDetails_DB.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails_DB.getOperationalHubCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(proposalEMDetails_DB.getOperationalHubCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(), ObjStr(proposalEMDetails.getOperationalHubCost()) ,currency, ObjStr(proposalEMDetails_DB.getOperationalHubCost()), currency_DB));
            }

            //Other Cost
            if(!isEqual(proposalEMDetails.getOtherCost(), proposalEMDetails_DB.getOtherCost()) || proposalEMDetails.getOtherCostType()!=proposalEMDetails_DB.getOtherCostType())
            {
                String currency = "";
                if(proposalEMDetails.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getOtherCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getOtherCostType().intValue());
                }

                String currency_DB = "";
                if(proposalEMDetails_DB.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails_DB.getOtherCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(proposalEMDetails_DB.getOtherCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMOTHERCOST.getDescription(), ObjStr(proposalEMDetails.getOtherCost()) ,currency, ObjStr(proposalEMDetails_DB.getOtherCost()), currency_DB));
            }


            //Name of Proposed Fieldwork Agencies
            if(!StringUtils.equals(proposalEMDetails.getProposedFWAgencyNames(), proposalEMDetails_DB.getProposedFWAgencyNames()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMPROPFWAGNCY.getDescription(), ObjStr(proposalEMDetails.getProposedFWAgencyNames()), ObjStr(proposalEMDetails_DB.getProposedFWAgencyNames())));
            }

            //Estimated Fieldwork Start
            if(!isEqual(proposalEMDetails.getFwStartDate(), proposalEMDetails_DB.getFwStartDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWSTART.getDescription(), ObjStr(proposalEMDetails.getFwStartDate()), ObjStr(proposalEMDetails_DB.getFwStartDate())));
            }

            //Estimated Fieldwork Completion
            if(!isEqual(proposalEMDetails.getFwEndDate(), proposalEMDetails_DB.getFwEndDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWCOMP.getDescription(), ObjStr(proposalEMDetails.getFwEndDate()), ObjStr(proposalEMDetails_DB.getFwEndDate())));
            }
            //Data Collection Methods
            if(!isEqual(proposalEMDetails.getDataCollectionMethod(), proposalEMDetails_DB.getDataCollectionMethod()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMDCMEH.getDescription(), ObjStr(getDataCollectionNames(proposalEMDetails.getDataCollectionMethod())), ObjStr(getDataCollectionNames(proposalEMDetails_DB.getDataCollectionMethod()))));
            }

            // Quantitative-Total Number of Interviews		
            if(!isEqual(proposalEMDetails.getTotalNoInterviews(), proposalEMDetails_DB.getTotalNoInterviews()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTINT.getDescription(), ObjStr(proposalEMDetails.getTotalNoInterviews()), ObjStr(proposalEMDetails_DB.getTotalNoInterviews())));
            }

            //Quantitative-Total Number of Visits per Respondent
            if(!isEqual(proposalEMDetails.getTotalNoOfVisits(), proposalEMDetails_DB.getTotalNoOfVisits()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTVIS.getDescription(), ObjStr(proposalEMDetails.getTotalNoOfVisits()), ObjStr(proposalEMDetails_DB.getTotalNoOfVisits())));
            }

            //Quantitative-Average Interview Duration
            if(!isEqual(proposalEMDetails.getAvIntDuration(), proposalEMDetails_DB.getAvIntDuration()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTAVGDUR.getDescription(), ObjStr(proposalEMDetails.getAvIntDuration()), ObjStr(proposalEMDetails_DB.getAvIntDuration())));
            }

            //Quantitative-Geographical Spread-Non-National-Geography
            //TODO

        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    public static String generateProposalMultiMarketAllUpdateJSONObject(final ProposalInitiation proposalInitiation_DB, final ProposalInitiation proposalInitiation,
                                                                        final ProposalReporting proposalReporting_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(proposalInitiation_DB!=null && proposalInitiation!=null)
        {
            //Brand / Non-Branded 
            if(proposalInitiation.getBrand()!=proposalInitiation_DB.getBrand())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.BRAND.getDescription(),
                        SynchroGlobal.getBrands().get(proposalInitiation.getBrand().intValue()), SynchroGlobal.getBrands().get(proposalInitiation_DB.getBrand().intValue())));
            }

            //Country 
            if(proposalInitiation.getEndMarketID()!=proposalInitiation_DB.getEndMarketID())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.COUNTRY.getDescription(),
                        SynchroGlobal.getEndMarkets().get(new Long(proposalInitiation.getEndMarketID()).intValue()), SynchroGlobal.getEndMarkets().get(new Long(proposalInitiation_DB.getEndMarketID()).intValue())));
            }
            //Methodology Type
            if(proposalInitiation.getMethodologyType()!=proposalInitiation_DB.getMethodologyType())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHTYPE.getDescription(),
                        SynchroGlobal.getMethodologies().get(proposalInitiation.getMethodologyType().intValue()), SynchroGlobal.getMethodologies().get(proposalInitiation_DB.getMethodologyType().intValue())));
            }

            //Methodology Group
            if(proposalInitiation.getMethodologyGroup()!=proposalInitiation_DB.getMethodologyGroup())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHGROUP.getDescription(),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(proposalInitiation.getMethodologyGroup().intValue()),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(proposalInitiation_DB.getMethodologyGroup().intValue())));
            }


            //Project Owner
            if(!isEqual(proposalInitiation.getProjectOwner(), proposalInitiation_DB.getProjectOwner()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(), getUserName(proposalInitiation.getProjectOwner()), getUserName(proposalInitiation_DB.getProjectOwner())));
            }


            //Project Contact/ SPI Contact
            if(!isEqual(proposalInitiation.getSpiContact(), proposalInitiation.getSpiContact()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(), getUserName(proposalInitiation.getSpiContact()), getUserName(proposalInitiation_DB.getSpiContact())));
            }


            //Project Start Date
            if(!isEqual(proposalInitiation.getStartDate(), proposalInitiation_DB.getStartDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STARTDATE.getDescription(), ObjStr(proposalInitiation.getStartDate()), ObjStr(proposalInitiation_DB.getStartDate())));
            }

            //Project End Date
            if(!isEqual(proposalInitiation.getEndDate(), proposalInitiation_DB.getEndDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.ENDDATE.getDescription(), ObjStr(proposalInitiation.getEndDate()), ObjStr(proposalInitiation_DB.getEndDate())));
            }


            //NPI Number
            if(!StringUtils.equals(proposalInitiation.getNpiReferenceNo(), proposalInitiation_DB.getNpiReferenceNo()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.NPINO.getDescription(), ObjStr(proposalInitiation.getNpiReferenceNo()), ObjStr(proposalInitiation_DB.getNpiReferenceNo())));
            }

            /* READONLY Field
		//Request for Methodology Waiver
		if(!isEqual(projectInitiation.getDeviationFromSM(), projectInitiation_DB.getDeviationFromSM()))
		{
			data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.REQMETHWAIVER.getDescription(), 
											(projectInitiation.getDeviationFromSM().intValue()==0?"No":"Yes"), (projectInitiation_DB.getDeviationFromSM().intValue()==0?"No":"Yes")));
		}*/

            //Business Question
            if(!StringUtils.equals(html2text(proposalInitiation.getBizQuestion()), html2text(proposalInitiation_DB.getBizQuestion())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(), ObjStr(proposalInitiation.getBizQuestion()), ObjStr(proposalInitiation_DB.getBizQuestion())));
            }
            //Research Objectives
            if(!StringUtils.equals(html2text(proposalInitiation.getResearchObjective()), html2text(proposalInitiation_DB.getResearchObjective())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESOBJ.getDescription(), ObjStr(proposalInitiation.getResearchObjective()), ObjStr(proposalInitiation_DB.getResearchObjective())));
            }
            //Action Standards
            if(!StringUtils.equals(html2text(proposalInitiation.getActionStandard()), html2text(proposalInitiation_DB.getActionStandard())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTD.getDescription(), ObjStr(proposalInitiation.getActionStandard()), ObjStr(proposalInitiation_DB.getActionStandard())));
            }
            //Methodology Approach and Research Design
            if(!StringUtils.equals(html2text(proposalInitiation.getResearchDesign()), html2text(proposalInitiation_DB.getResearchDesign())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESDSGN.getDescription(), ObjStr(proposalInitiation.getResearchDesign()), ObjStr(proposalInitiation_DB.getResearchDesign())));
            }
            //Sample Profile (Research)
            if(!StringUtils.equals(html2text(proposalInitiation.getSampleProfile()), html2text(proposalInitiation_DB.getSampleProfile())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SMPLPROFILE.getDescription(), ObjStr(proposalInitiation.getSampleProfile()), ObjStr(proposalInitiation_DB.getSampleProfile())));
            }
            //Stimulus Material
            if(!StringUtils.equals(html2text(proposalInitiation.getStimulusMaterial()), html2text(proposalInitiation_DB.getStimulusMaterial())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(), ObjStr(proposalInitiation.getStimulusMaterial()), ObjStr(proposalInitiation_DB.getStimulusMaterial())));
            }

            //Stimulus Material to be shipped to
            if(!StringUtils.equals(html2text(proposalInitiation.getStimulusMaterialShipped()), html2text(proposalInitiation_DB.getStimulusMaterialShipped())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMMATSHIPPED.getDescription(),
                        ObjStr(proposalInitiation.getStimulusMaterialShipped()), ObjStr(proposalInitiation_DB.getStimulusMaterialShipped())));
            }

            //Other Comments
            if(!StringUtils.equals(html2text(proposalInitiation.getOthers()), html2text(proposalInitiation_DB.getOthers())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHER.getDescription(), ObjStr(proposalInitiation.getOthers()), ObjStr(proposalInitiation_DB.getOthers())));
            }

            //Date Stimuli
            if(!isEqual(proposalInitiation.getStimuliDate(), proposalInitiation_DB.getStimuliDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(), ObjStr(proposalInitiation.getStimuliDate()), ObjStr(proposalInitiation_DB.getStimuliDate())));
            }


            //PIB Reporting
            if(proposalReporting_DB!=null)
            {
                //Textfield: Other Reporting Requirements
                if(!StringUtils.equals(html2text(proposalInitiation.getOtherReportingRequirements()), html2text(proposalReporting_DB.getOtherReportingRequirements())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHERREP.getDescription(), ObjStr(proposalInitiation.getOtherReportingRequirements()), ObjStr(proposalReporting_DB.getOtherReportingRequirements())));
                }
                //Checkbox Reporting Requirement:Full Report
                if(!isEqual(proposalInitiation.getTopLinePresentation(), proposalReporting_DB.getTopLinePresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPTOPLINE.getDescription(),getBoolean(proposalInitiation.getTopLinePresentation())?"Selected":"Unselected"));
                }

                //Checkbox Reporting Requirement:Presentation
                if(!isEqual(proposalInitiation.getPresentation(), proposalReporting_DB.getPresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPPRES.getDescription(), getBoolean(proposalInitiation.getPresentation())?"Selected":"Unselected"));
                }
                //Checkbox Reporting Requirement:Topline Presentation
                if(!isEqual(proposalInitiation.getFullreport(), proposalReporting_DB.getFullreport()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPFULL.getDescription(), getBoolean(proposalInitiation.getFullreport())?"Selected":"Unselected"));
                }
            }

            //END-Market Fields
            //Stimulus Material to be shipped to
            if(!StringUtils.equals(html2text(proposalInitiation.getStimulusMaterialShipped()), html2text(proposalInitiation_DB.getStimulusMaterialShipped())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMMATSHIPPED.getDescription(), ObjStr(proposalInitiation.getStimulusMaterialShipped()), ObjStr(proposalInitiation_DB.getStimulusMaterialShipped())));
            }

            //Proposal and Cost Template
            if(!StringUtils.equals(html2text(proposalInitiation.getProposalCostTemplate()), html2text(proposalInitiation_DB.getProposalCostTemplate())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PROPCOSTTEMP.getDescription(), ObjStr(proposalInitiation.getProposalCostTemplate()), ObjStr(proposalInitiation_DB.getProposalCostTemplate())));
            }


        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    /**
     * Project Specs Audit Logs
     * @param proposalInitiation_DB
     * @param proposalInitiation
     * @param proposalReporting_DB
     * @param proposalEMDetails
     * @param proposalEMDetails_DB
     * @return
     */
    public static String generateProjectSpecsAllUpdateJSONObject(final ProjectSpecsInitiation projectSpecsInitiation_DB,
                                                                 final ProjectSpecsInitiation projectSpecsInitiation, final ProjectSpecsEndMarketDetails projectSpecsEMDetails_DB,
                                                                 final ProjectSpecsEndMarketDetails projectSpecsEMDetails, final ProjectSpecsReporting projectSpecsReporting_DB,
                                                                 final List<EndMarketInvestmentDetail> endMarketDetails_DB, final ProjectInitiation projectInitiation_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(projectSpecsInitiation_DB!=null && projectSpecsInitiation!=null)
        {

            //Brand / Non-Branded 
            if(projectSpecsInitiation.getBrand()!=projectSpecsInitiation_DB.getBrand())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.BRAND.getDescription(),
                        SynchroGlobal.getBrands().get(projectSpecsInitiation.getBrand().intValue()),
                        SynchroGlobal.getBrands().get(projectSpecsInitiation_DB.getBrand().intValue())));
            }

            //Country 
            if(projectSpecsInitiation.getEndMarketID()!=projectSpecsInitiation_DB.getEndMarketID())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.COUNTRY.getDescription(),
                        SynchroGlobal.getEndMarkets().get(new Long(projectSpecsInitiation.getEndMarketID()).intValue()),
                        SynchroGlobal.getEndMarkets().get(new Long(projectSpecsInitiation_DB.getEndMarketID()).intValue())));
            }
            //Methodology Type
            if(projectSpecsInitiation.getMethodologyType()!=projectSpecsInitiation_DB.getMethodologyType())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHTYPE.getDescription(),
                        SynchroGlobal.getMethodologies().get(projectSpecsInitiation.getMethodologyType().intValue()),
                        SynchroGlobal.getMethodologies().get(projectSpecsInitiation_DB.getMethodologyType().intValue())));
            }

            //Methodology Group
            if(projectSpecsInitiation.getMethodologyGroup()!=projectSpecsInitiation_DB.getMethodologyGroup())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHGROUP.getDescription(),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(projectSpecsInitiation.getMethodologyGroup().intValue()),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(projectSpecsInitiation_DB.getMethodologyGroup().intValue())));
            }


            //Project Owner
            if(!isEqual(projectSpecsInitiation.getProjectOwner(), projectSpecsInitiation_DB.getProjectOwner()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(),
                        getUserName(projectSpecsInitiation.getProjectOwner()), getUserName(projectSpecsInitiation_DB.getProjectOwner())));
            }


            //Project Contact/ SPI Contact
            if(!isEqual(projectSpecsInitiation.getSpiContact(), projectSpecsInitiation_DB.getSpiContact()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.PROJECTOWNER.getDescription(),
                        getUserName(projectSpecsInitiation.getSpiContact()), getUserName(projectSpecsInitiation_DB.getSpiContact())));
            }


            //Project Start Date
            if(!isEqual(projectSpecsInitiation.getStartDate(), projectSpecsInitiation_DB.getStartDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STARTDATE.getDescription(),
                        ObjStr(projectSpecsInitiation.getStartDate()), ObjStr(projectSpecsInitiation_DB.getStartDate())));
            }

            //Project End Date
            if(!isEqual(projectSpecsInitiation.getEndDate(), projectSpecsInitiation_DB.getEndDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.ENDDATE.getDescription(),
                        ObjStr(projectSpecsInitiation.getEndDate()), ObjStr(projectSpecsInitiation_DB.getEndDate())));
            }

            //Check if users are not External Agency User
            if(!SynchroPermHelper.isExternalAgencyUser(projectSpecsInitiation.getProjectID(), endMarketDetails_DB.get(0).getEndMarketID()))
            {
                //Estimated Cost
                if(!isEqual(projectSpecsInitiation.getEstimatedCost(), endMarketDetails_DB.get(0).getInitialCost()) ||
                        !isEqual(projectSpecsInitiation.getEstimatedCostType(), endMarketDetails_DB.get(0).getInitialCostCurrency()))
                {
                    String currency = "";
                    if(projectSpecsInitiation.getEstimatedCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsInitiation.getEstimatedCostType()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsInitiation.getEstimatedCostType());
                    }

                    String currency_DB = "";
                    if(endMarketDetails_DB.get(0).getInitialCostCurrency()!=null && SynchroGlobal.getCurrencies().containsKey(endMarketDetails_DB.get(0).getInitialCostCurrency().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(endMarketDetails_DB.get(0).getInitialCostCurrency().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.ESTIMATEDCOST.getDescription(),
                            ObjStr(projectSpecsInitiation.getEstimatedCost()), currency, ObjStr(endMarketDetails_DB.get(0).getInitialCost()), currency_DB));
                }

                //Latest Cost
                if(!isEqual(projectSpecsInitiation.getLatestEstimate(), projectInitiation_DB.getLatestEstimate()) ||
                        projectSpecsInitiation.getLatestEstimateType()!=projectInitiation_DB.getLatestEstimateType())
                {
                    String currency = "";
                    if(projectSpecsInitiation.getLatestEstimateType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsInitiation.getLatestEstimateType()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsInitiation.getLatestEstimateType());
                    }

                    String currency_DB = "";
                    if(projectInitiation_DB.getLatestEstimateType()!=null && SynchroGlobal.getCurrencies().containsKey(projectInitiation_DB.getLatestEstimateType()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectInitiation_DB.getLatestEstimateType());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.LATESTESTM.getDescription(),
                            ObjStr(projectSpecsInitiation.getLatestEstimate()), currency, ObjStr(projectInitiation_DB.getLatestEstimate()), currency_DB));
                }
            }

            //NPI Number
            if(!StringUtils.equals(projectSpecsInitiation.getNpiReferenceNo(), projectSpecsInitiation_DB.getNpiReferenceNo()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.NPINO.getDescription(),
                        ObjStr(projectSpecsInitiation.getNpiReferenceNo()), ObjStr(projectSpecsInitiation_DB.getNpiReferenceNo())));
            }

            //Request for Methodology Waiver
            if(!isEqual(projectSpecsInitiation.getDeviationFromSM(), projectSpecsInitiation_DB.getDeviationFromSM()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.REQMETHWAIVER.getDescription(),
                        (projectSpecsInitiation.getDeviationFromSM().intValue()==0?"No":"Yes"), (projectSpecsInitiation_DB.getDeviationFromSM().intValue()==0?"No":"Yes")));
            }

            //Check if users are not External Agency User
            if(!SynchroPermHelper.isExternalAgencyUser(projectSpecsInitiation.getProjectID(), endMarketDetails_DB.get(0).getEndMarketID()))
            {
                //PO Number
                if(!StringUtils.equals(projectSpecsInitiation.getPoNumber(), projectSpecsInitiation_DB.getPoNumber()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PONUMBER.getDescription(),
                            ObjStr(projectSpecsInitiation.getPoNumber()), ObjStr(projectSpecsInitiation_DB.getPoNumber())));
                }

                //PO Number 2
                if(!StringUtils.equals(projectSpecsInitiation.getPoNumber1(), projectSpecsInitiation_DB.getPoNumber1()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PONUMBER.getDescription(),
                            ObjStr(projectSpecsInitiation.getPoNumber1()), ObjStr(projectSpecsInitiation_DB.getPoNumber1())));
                }
            }
            //Project Description
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getDescription()), html2text(projectSpecsInitiation_DB.getDescription())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(),
                        ObjStr(projectSpecsInitiation.getDescription()), ObjStr(projectSpecsInitiation_DB.getDescription())));
            }

            //Business Question
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getBizQuestion()), html2text(projectSpecsInitiation_DB.getBizQuestion())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(),
                        ObjStr(projectSpecsInitiation.getBizQuestion()), ObjStr(projectSpecsInitiation_DB.getBizQuestion())));
            }
            //Research Objectives
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getResearchObjective()), html2text(projectSpecsInitiation_DB.getResearchObjective())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESOBJ.getDescription(),
                        ObjStr(projectSpecsInitiation.getResearchObjective()), ObjStr(projectSpecsInitiation_DB.getResearchObjective())));
            }
            //Action Standards
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getActionStandard()), html2text(projectSpecsInitiation_DB.getActionStandard())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTD.getDescription(),
                        ObjStr(projectSpecsInitiation.getActionStandard()), ObjStr(projectSpecsInitiation_DB.getActionStandard())));

            }
            //Methodology Approach and Research Design
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getResearchDesign()), html2text(projectSpecsInitiation_DB.getResearchDesign())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESDSGN.getDescription(),
                        ObjStr(projectSpecsInitiation.getResearchDesign()), ObjStr(projectSpecsInitiation_DB.getResearchDesign())));
            }
            //Sample Profile (Research)
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getSampleProfile()), html2text(projectSpecsInitiation_DB.getSampleProfile())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SMPLPROFILE.getDescription(),
                        ObjStr(projectSpecsInitiation.getSampleProfile()), ObjStr(projectSpecsInitiation_DB.getSampleProfile())));
            }
            //Stimulus Material
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getStimulusMaterial()), html2text(projectSpecsInitiation_DB.getStimulusMaterial())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(),
                        ObjStr(projectSpecsInitiation.getStimulusMaterial()), ObjStr(projectSpecsInitiation_DB.getStimulusMaterial())));
            }

            //Stimulus Material to be shipped to
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getStimulusMaterialShipped()), html2text(projectSpecsInitiation_DB.getStimulusMaterialShipped())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMMATSHIPPED.getDescription(),
                        ObjStr(projectSpecsInitiation.getStimulusMaterialShipped()), ObjStr(projectSpecsInitiation_DB.getStimulusMaterialShipped())));
            }

            //Other Comments
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getOthers()), html2text(projectSpecsInitiation_DB.getOthers())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHER.getDescription(),
                        ObjStr(projectSpecsInitiation.getOthers()), ObjStr(projectSpecsInitiation_DB.getOthers())));
            }

            //Date Stimuli
            if(!isEqual(projectSpecsInitiation.getStimuliDate(), projectSpecsInitiation_DB.getStimuliDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(),
                        ObjStr(projectSpecsInitiation.getStimuliDate()), ObjStr(projectSpecsInitiation_DB.getStimuliDate())));
            }


            //PIB Reporting
            if(projectSpecsReporting_DB!=null)
            {
                //Textfield: Other Reporting Requirements
                if(!StringUtils.equals(html2text(projectSpecsInitiation.getOtherReportingRequirements()), html2text(projectSpecsReporting_DB.getOtherReportingRequirements())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHERREP.getDescription(),
                            ObjStr(projectSpecsInitiation.getOtherReportingRequirements()), ObjStr(projectSpecsInitiation_DB.getOtherReportingRequirements())));
                }
                //Checkbox Reporting Requirement:Full Report
                if(!isEqual(projectSpecsInitiation.getTopLinePresentation(), projectSpecsReporting_DB.getTopLinePresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPTOPLINE.getDescription(),
                            getBoolean(projectSpecsInitiation.getTopLinePresentation())?"Selected":"Unselected"));
                }

                //Checkbox Reporting Requirement:Presentation
                if(!isEqual(projectSpecsInitiation.getPresentation(), projectSpecsReporting_DB.getPresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPPRES.getDescription(),
                            getBoolean(projectSpecsInitiation.getPresentation())?"Selected":"Unselected"));
                }
                //Checkbox Reporting Requirement:Topline Presentation
                if(!isEqual(projectSpecsInitiation.getFullreport(), projectSpecsReporting_DB.getFullreport()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPFULL.getDescription(),
                            getBoolean(projectSpecsInitiation.getFullreport())?"Selected":"Unselected"));
                }
            }

            //Screener
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getScreener()), html2text(projectSpecsInitiation_DB.getScreener())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SCREENER.getDescription(),
                        ObjStr(projectSpecsInitiation.getScreener()), ObjStr(projectSpecsInitiation_DB.getScreener())));
            }

            //Consumer Contract and Confidentiality Agreement
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getConsumerCCAgreement()), html2text(projectSpecsInitiation_DB.getConsumerCCAgreement())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.CONSUMERCONTRACTAGREEMENT.getDescription(),
                        ObjStr(projectSpecsInitiation.getConsumerCCAgreement()), ObjStr(projectSpecsInitiation_DB.getConsumerCCAgreement())));
            }
            //Questionnaire/Discussion guide
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getQuestionnaire()), html2text(projectSpecsInitiation_DB.getQuestionnaire())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.QUESTIONNAIRE.getDescription(),
                        ObjStr(projectSpecsInitiation.getQuestionnaire()), ObjStr(projectSpecsInitiation_DB.getQuestionnaire())));
            }
            //Actual Stimulus Material
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getDiscussionguide()), html2text(projectSpecsInitiation_DB.getDiscussionguide())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTIMMAT.getDescription(),
                        ObjStr(projectSpecsInitiation.getDiscussionguide()), ObjStr(projectSpecsInitiation_DB.getDiscussionguide())));
            }

            if(projectSpecsEMDetails_DB!=null)
            {
	            //Total Cost
	            if(!isEqual(projectSpecsEMDetails.getTotalCost(), projectSpecsEMDetails_DB.getTotalCost()) ||
	                    projectSpecsEMDetails.getTotalCostType()!=projectSpecsEMDetails_DB.getTotalCostType())
	            {
	                String currency = "";
	                if(projectSpecsEMDetails.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getTotalCostType().intValue()))
	                {
	                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getTotalCostType().intValue());
	                }
	
	                String currency_DB = "";
	                if(projectSpecsEMDetails_DB.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getTotalCostType().intValue()))
	                {
	                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getTotalCostType().intValue());
	                }
	
	                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMTOTALCOST.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getTotalCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getTotalCost()), currency_DB));
	            }
	
	
	            //International Management Cost - Research Hub Cost
	            if(!isEqual(projectSpecsEMDetails.getIntMgmtCost(), projectSpecsEMDetails_DB.getIntMgmtCost()) ||
	                    projectSpecsEMDetails.getIntMgmtCostType()!=projectSpecsEMDetails_DB.getIntMgmtCostType())
	            {
	                String currency = "";
	                if(projectSpecsEMDetails.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getIntMgmtCostType().intValue()))
	                {
	                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getIntMgmtCostType().intValue());
	                }
	
	                String currency_DB = "";
	                if(projectSpecsEMDetails_DB.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getIntMgmtCostType().intValue()))
	                {
	                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getIntMgmtCostType().intValue());
	                }
	
	                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMINTMGTCOST.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getIntMgmtCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getIntMgmtCost()), currency_DB));
	            }
	
	            //Local Management Cost
	            if(!isEqual(projectSpecsEMDetails.getLocalMgmtCost(), projectSpecsEMDetails_DB.getLocalMgmtCost()) ||
	                    projectSpecsEMDetails.getLocalMgmtCostType()!=projectSpecsEMDetails_DB.getLocalMgmtCostType())
	            {
	                String currency = "";
	                if(projectSpecsEMDetails.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getLocalMgmtCostType().intValue()))
	                {
	                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getLocalMgmtCostType().intValue());
	                }
	
	                String currency_DB = "";
	                if(projectSpecsEMDetails_DB.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getLocalMgmtCostType().intValue()))
	                {
	                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getLocalMgmtCostType().intValue());
	                }
	
	                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getLocalMgmtCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getLocalMgmtCost()), currency_DB));
	            }
	
	            //Fieldwork Cost
	            if(!isEqual(projectSpecsEMDetails.getFieldworkCost(), projectSpecsEMDetails_DB.getFieldworkCost()) ||
	                    projectSpecsEMDetails.getFieldworkCostType()!=projectSpecsEMDetails_DB.getFieldworkCostType())
	            {
	                String currency = "";
	                if(projectSpecsEMDetails.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getFieldworkCostType().intValue()))
	                {
	                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getFieldworkCostType().intValue());
	                }
	
	                String currency_DB = "";
	                if(projectSpecsEMDetails_DB.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getFieldworkCostType().intValue()))
	                {
	                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getFieldworkCostType().intValue());
	                }
	
	                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMFWCOST.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getFieldworkCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getFieldworkCost()), currency_DB));
	            }
	
	            //Operational Hub Cost
	            if(!isEqual(projectSpecsEMDetails.getOperationalHubCost(), projectSpecsEMDetails_DB.getOperationalHubCost()) ||
	                    projectSpecsEMDetails.getOperationalHubCostType()!=projectSpecsEMDetails_DB.getOperationalHubCostType())
	            {
	                String currency = "";
	                if(projectSpecsEMDetails.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getOperationalHubCostType().intValue()))
	                {
	                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getOperationalHubCostType().intValue());
	                }
	
	                String currency_DB = "";
	                if(projectSpecsEMDetails_DB.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getOperationalHubCostType().intValue()))
	                {
	                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getOperationalHubCostType().intValue());
	                }
	
	                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getOperationalHubCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getOperationalHubCost()), currency_DB));
	            }
	
	            //Other Cost
	            if(!isEqual(projectSpecsEMDetails.getOtherCost(), projectSpecsEMDetails_DB.getOtherCost()) ||
	                    projectSpecsEMDetails.getOtherCostType()!=projectSpecsEMDetails_DB.getOtherCostType())
	            {
	                String currency = "";
	                if(projectSpecsEMDetails.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getOtherCostType().intValue()))
	                {
	                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getOtherCostType().intValue());
	                }
	
	                String currency_DB = "";
	                if(projectSpecsEMDetails_DB.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getOtherCostType().intValue()))
	                {
	                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getOtherCostType().intValue());
	                }
	
	                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMOTHERCOST.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getOtherCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getOtherCost()), currency_DB));
	            }
	
	
	            //Name of Proposed Fieldwork Agencies
	            if(!StringUtils.equals(projectSpecsEMDetails.getProposedFWAgencyNames(), projectSpecsEMDetails_DB.getProposedFWAgencyNames()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMPROPFWAGNCY.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getProposedFWAgencyNames()), ObjStr(projectSpecsEMDetails_DB.getProposedFWAgencyNames())));
	            }
	
	            //Estimated Fieldwork Start
	            if(!isEqual(projectSpecsEMDetails.getFwStartDate(), projectSpecsEMDetails_DB.getFwStartDate()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWSTART.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getFwStartDate()), ObjStr(projectSpecsEMDetails_DB.getFwStartDate())));
	            }
	
	            //Estimated Fieldwork Completion
	            if(!isEqual(projectSpecsEMDetails.getFwEndDate(), projectSpecsEMDetails_DB.getFwEndDate()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWCOMP.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getFwEndDate()), ObjStr(projectSpecsEMDetails_DB.getFwEndDate())));
	            }
	            //Data Collection Methods
	            if(!isEqual(projectSpecsEMDetails.getDataCollectionMethod(), projectSpecsEMDetails_DB.getDataCollectionMethod()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMDCMEH.getDescription(),
	                        ObjStr(getDataCollectionNames(projectSpecsEMDetails.getDataCollectionMethod())),
	                        ObjStr(getDataCollectionNames(projectSpecsEMDetails_DB.getDataCollectionMethod()))));
	            }
	
	            // Quantitative-Total Number of Interviews		
	            if(!isEqual(projectSpecsEMDetails.getTotalNoInterviews(), projectSpecsEMDetails_DB.getTotalNoInterviews()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTINT.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getTotalNoInterviews()), ObjStr(projectSpecsEMDetails_DB.getTotalNoInterviews())));
	            }
	
	            //Quantitative-Total Number of Visits per Respondent
	            if(!isEqual(projectSpecsEMDetails.getTotalNoOfVisits(), projectSpecsEMDetails_DB.getTotalNoOfVisits()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTVIS.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getTotalNoOfVisits()), ObjStr(projectSpecsEMDetails_DB.getTotalNoOfVisits())));
	            }
	
	            //Quantitative-Average Interview Duration
	            if(!isEqual(projectSpecsEMDetails.getAvIntDuration(), projectSpecsEMDetails_DB.getAvIntDuration()))
	            {
	                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTAVGDUR.getDescription(),
	                        ObjStr(projectSpecsEMDetails.getAvIntDuration()), ObjStr(projectSpecsEMDetails_DB.getAvIntDuration())));
	            }
            }

            //Quantitative-Geographical Spread-Non-National-Geography
            //TODO

            /*Not Available
		//Proposal and Cost Template
		if(!StringUtils.equals(html2text(projectSpecsEMDetails.getProposalCostTemplate()), html2text(projectSpecsEMDetails_DB.getProposalCostTemplate())))
		{
			data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PROPCOSTTEMP.getDescription(), 
					ObjStr(projectSpecsEMDetails.getProposalCostTemplate()), ObjStr(projectSpecsEMDetails_DB.getProposalCostTemplate())));
		}
		*/

        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    public static String generateProjectSpecsMultiMarketAllUpdateJSONObject(final ProjectSpecsInitiation projectSpecsInitiation_DB, final ProjectSpecsInitiation projectSpecsInitiation,
                                                                            final ProjectSpecsEndMarketDetails projectSpecsEMDetails_DB, final ProjectSpecsEndMarketDetails projectSpecsEMDetails,
                                                                            final ProjectSpecsReporting projectSpecsReporting_DB, final List<EndMarketInvestmentDetail> endMarketDetails_DB,
                                                                            final ProjectInitiation projectInitiation_DB, final Long endmarketID, final User user)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(projectSpecsInitiation_DB!=null && projectSpecsInitiation!=null)
        {

            //Brand / Non-Branded 
            if(projectSpecsInitiation.getBrand()!=projectSpecsInitiation_DB.getBrand())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.BRAND.getDescription(),
                        SynchroGlobal.getBrands().get(projectSpecsInitiation.getBrand().intValue()),
                        SynchroGlobal.getBrands().get(projectSpecsInitiation_DB.getBrand().intValue())));
            }

            //Methodology Type
            if(projectSpecsInitiation.getMethodologyType()!=projectSpecsInitiation_DB.getMethodologyType())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHTYPE.getDescription(),
                        SynchroGlobal.getMethodologies().get(projectSpecsInitiation.getMethodologyType().intValue()),
                        SynchroGlobal.getMethodologies().get(projectSpecsInitiation_DB.getMethodologyType().intValue())));
            }

            //Methodology Group
            if(projectSpecsInitiation.getMethodologyGroup()!=projectSpecsInitiation_DB.getMethodologyGroup())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.METHGROUP.getDescription(),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(projectSpecsInitiation.getMethodologyGroup().intValue()),
                        SynchroGlobal.getMethodologyGroups(true, new Long(1)).get(projectSpecsInitiation_DB.getMethodologyGroup().intValue())));
            }

            //Project Start Date
            if(!isEqual(projectSpecsInitiation.getStartDate(), projectSpecsInitiation_DB.getStartDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STARTDATE.getDescription(),
                        ObjStr(projectSpecsInitiation.getStartDate()), ObjStr(projectSpecsInitiation_DB.getStartDate())));
            }

            //Project End Date
            if(!isEqual(projectSpecsInitiation.getEndDate(), projectSpecsInitiation_DB.getEndDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.ENDDATE.getDescription(),
                        ObjStr(projectSpecsInitiation.getEndDate()), ObjStr(projectSpecsInitiation_DB.getEndDate())));
            }

            //NPI Number
            if(!StringUtils.equals(projectSpecsInitiation.getNpiReferenceNo(), projectSpecsInitiation_DB.getNpiReferenceNo()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.NPINO.getDescription(),
                        ObjStr(projectSpecsInitiation.getNpiReferenceNo()), ObjStr(projectSpecsInitiation_DB.getNpiReferenceNo())));
            }

            if(endmarketID!=null && isEqual(endmarketID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                //Above Market Tab
                //Request for Methodology Waiver
                if(!isEqual(projectSpecsInitiation.getDeviationFromSM(), projectSpecsInitiation_DB.getDeviationFromSM()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.REQMETHWAIVER.getDescription(),
                            (projectSpecsInitiation.getDeviationFromSM().intValue()==0?"No":"Yes"), (projectSpecsInitiation_DB.getDeviationFromSM().intValue()==0?"No":"Yes")));
                }
            }

            //PO Number
            if(!StringUtils.equals(projectSpecsInitiation.getPoNumber(), projectSpecsInitiation_DB.getPoNumber()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PONUMBER.getDescription(),
                        ObjStr(projectSpecsInitiation.getPoNumber()), ObjStr(projectSpecsInitiation_DB.getPoNumber())));
            }

            //PO Number 2
            if(!StringUtils.equals(projectSpecsInitiation.getPoNumber1(), projectSpecsInitiation_DB.getPoNumber1()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PONUMBER.getDescription(),
                        ObjStr(projectSpecsInitiation.getPoNumber1()), ObjStr(projectSpecsInitiation_DB.getPoNumber1())));
            }

            //Project Description
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getDescription()), html2text(projectSpecsInitiation_DB.getDescription())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(),
                        ObjStr(projectSpecsInitiation.getDescription()), ObjStr(projectSpecsInitiation_DB.getDescription())));
            }

            //Business Question
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getBizQuestion()), html2text(projectSpecsInitiation_DB.getBizQuestion())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.BIZQS.getDescription(),
                        ObjStr(projectSpecsInitiation.getBizQuestion()), ObjStr(projectSpecsInitiation_DB.getBizQuestion())));
            }
            //Research Objectives
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getResearchObjective()), html2text(projectSpecsInitiation_DB.getResearchObjective())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESOBJ.getDescription(),
                        ObjStr(projectSpecsInitiation.getResearchObjective()), ObjStr(projectSpecsInitiation_DB.getResearchObjective())));
            }
            //Action Standards
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getActionStandard()), html2text(projectSpecsInitiation_DB.getActionStandard())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTD.getDescription(),
                        ObjStr(projectSpecsInitiation.getActionStandard()), ObjStr(projectSpecsInitiation_DB.getActionStandard())));

            }
            //Methodology Approach and Research Design
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getResearchDesign()), html2text(projectSpecsInitiation_DB.getResearchDesign())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.RESDSGN.getDescription(),
                        ObjStr(projectSpecsInitiation.getResearchDesign()), ObjStr(projectSpecsInitiation_DB.getResearchDesign())));
            }
            //Sample Profile (Research)
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getSampleProfile()), html2text(projectSpecsInitiation_DB.getSampleProfile())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SMPLPROFILE.getDescription(),
                        ObjStr(projectSpecsInitiation.getSampleProfile()), ObjStr(projectSpecsInitiation_DB.getSampleProfile())));
            }
            //Stimulus Material
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getStimulusMaterial()), html2text(projectSpecsInitiation_DB.getStimulusMaterial())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(),
                        ObjStr(projectSpecsInitiation.getStimulusMaterial()), ObjStr(projectSpecsInitiation_DB.getStimulusMaterial())));
            }

            //Stimulus Material to be shipped to
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getStimulusMaterialShipped()), html2text(projectSpecsInitiation_DB.getStimulusMaterialShipped())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMMATSHIPPED.getDescription(),
                        ObjStr(projectSpecsInitiation.getStimulusMaterialShipped()), ObjStr(projectSpecsInitiation_DB.getStimulusMaterialShipped())));
            }

            //Other Comments
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getOthers()), html2text(projectSpecsInitiation_DB.getOthers())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHER.getDescription(),
                        ObjStr(projectSpecsInitiation.getOthers()), ObjStr(projectSpecsInitiation_DB.getOthers())));
            }

            //Date Stimuli
            if(!isEqual(projectSpecsInitiation.getStimuliDate(), projectSpecsInitiation_DB.getStimuliDate()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.STIMULUS.getDescription(),
                        ObjStr(projectSpecsInitiation.getStimuliDate()), ObjStr(projectSpecsInitiation_DB.getStimuliDate())));
            }


            //PIB Reporting
            if(projectSpecsReporting_DB!=null && SynchroPermHelper.isSynchroAdmin(user))
            {
                //Textfield: Other Reporting Requirements
                if(!StringUtils.equals(html2text(projectSpecsInitiation.getOtherReportingRequirements()), html2text(projectSpecsReporting_DB.getOtherReportingRequirements())))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.OTHERREP.getDescription(),
                            ObjStr(projectSpecsInitiation.getOtherReportingRequirements()), ObjStr(projectSpecsInitiation_DB.getOtherReportingRequirements())));
                }
                //Checkbox Reporting Requirement:Full Report
                if(!isEqual(projectSpecsInitiation.getTopLinePresentation(), projectSpecsReporting_DB.getTopLinePresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPTOPLINE.getDescription(),
                            getBoolean(projectSpecsInitiation.getTopLinePresentation())?"Selected":"Unselected"));
                }

                //Checkbox Reporting Requirement:Presentation
                if(!isEqual(projectSpecsInitiation.getPresentation(), projectSpecsReporting_DB.getPresentation()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPPRES.getDescription(),
                            getBoolean(projectSpecsInitiation.getPresentation())?"Selected":"Unselected"));
                }
                //Checkbox Reporting Requirement:Topline Presentation
                if(!isEqual(projectSpecsInitiation.getFullreport(), projectSpecsReporting_DB.getFullreport()))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.REPFULL.getDescription(),
                            getBoolean(projectSpecsInitiation.getFullreport())?"Selected":"Unselected"));
                }
            }

            //Screener
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getScreener()), html2text(projectSpecsInitiation_DB.getScreener())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SCREENER.getDescription(),
                        ObjStr(projectSpecsInitiation.getScreener()), ObjStr(projectSpecsInitiation_DB.getScreener())));
            }

            //Consumer Contract and Confidentiality Agreement
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getConsumerCCAgreement()), html2text(projectSpecsInitiation_DB.getConsumerCCAgreement())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.CONSUMERCONTRACTAGREEMENT.getDescription(),
                        ObjStr(projectSpecsInitiation.getConsumerCCAgreement()), ObjStr(projectSpecsInitiation_DB.getConsumerCCAgreement())));
            }
            //Questionnaire/Discussion guide
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getQuestionnaire()), html2text(projectSpecsInitiation_DB.getQuestionnaire())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.QUESTIONNAIRE.getDescription(),
                        ObjStr(projectSpecsInitiation.getQuestionnaire()), ObjStr(projectSpecsInitiation_DB.getQuestionnaire())));
            }
            //Actual Stimulus Material
            if(!StringUtils.equals(html2text(projectSpecsInitiation.getDiscussionguide()), html2text(projectSpecsInitiation_DB.getDiscussionguide())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTIMMAT.getDescription(),
                        ObjStr(projectSpecsInitiation.getDiscussionguide()), ObjStr(projectSpecsInitiation_DB.getDiscussionguide())));
            }

            //Endmarket fields
            if(endmarketID!=null && !isEqual(endmarketID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                //Total Cost
                if(!isEqual(projectSpecsEMDetails.getTotalCost(), projectSpecsEMDetails_DB.getTotalCost()) ||
                        projectSpecsEMDetails.getTotalCostType()!=projectSpecsEMDetails_DB.getTotalCostType())
                {
                    String currency = "";
                    if(projectSpecsEMDetails.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getTotalCostType().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getTotalCostType().intValue());
                    }

                    String currency_DB = "";
                    if(projectSpecsEMDetails_DB.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getTotalCostType().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getTotalCostType().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMTOTALCOST.getDescription(),
                            ObjStr(projectSpecsEMDetails.getTotalCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getTotalCost()), currency_DB));
                }


                //International Management Cost - Research Hub Cost
                if(!isEqual(projectSpecsEMDetails.getIntMgmtCost(), projectSpecsEMDetails_DB.getIntMgmtCost()) ||
                        projectSpecsEMDetails.getIntMgmtCostType()!=projectSpecsEMDetails_DB.getIntMgmtCostType())
                {
                    String currency = "";
                    if(projectSpecsEMDetails.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getIntMgmtCostType().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getIntMgmtCostType().intValue());
                    }

                    String currency_DB = "";
                    if(projectSpecsEMDetails_DB.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getIntMgmtCostType().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getIntMgmtCostType().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMINTMGTCOST.getDescription(),
                            ObjStr(projectSpecsEMDetails.getIntMgmtCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getIntMgmtCost()), currency_DB));
                }

                //Local Management Cost
                if(!isEqual(projectSpecsEMDetails.getLocalMgmtCost(), projectSpecsEMDetails_DB.getLocalMgmtCost()) ||
                        projectSpecsEMDetails.getLocalMgmtCostType()!=projectSpecsEMDetails_DB.getLocalMgmtCostType())
                {
                    String currency = "";
                    if(projectSpecsEMDetails.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getLocalMgmtCostType().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getLocalMgmtCostType().intValue());
                    }

                    String currency_DB = "";
                    if(projectSpecsEMDetails_DB.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getLocalMgmtCostType().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getLocalMgmtCostType().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(),
                            ObjStr(projectSpecsEMDetails.getLocalMgmtCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getLocalMgmtCost()), currency_DB));
                }

                //Fieldwork Cost
                if(!isEqual(projectSpecsEMDetails.getFieldworkCost(), projectSpecsEMDetails_DB.getFieldworkCost()) ||
                        projectSpecsEMDetails.getFieldworkCostType()!=projectSpecsEMDetails_DB.getFieldworkCostType())
                {
                    String currency = "";
                    if(projectSpecsEMDetails.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getFieldworkCostType().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getFieldworkCostType().intValue());
                    }

                    String currency_DB = "";
                    if(projectSpecsEMDetails_DB.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getFieldworkCostType().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getFieldworkCostType().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMFWCOST.getDescription(),
                            ObjStr(projectSpecsEMDetails.getFieldworkCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getFieldworkCost()), currency_DB));
                }

                //Operational Hub Cost
                if(!isEqual(projectSpecsEMDetails.getOperationalHubCost(), projectSpecsEMDetails_DB.getOperationalHubCost()) ||
                        projectSpecsEMDetails.getOperationalHubCostType()!=projectSpecsEMDetails_DB.getOperationalHubCostType())
                {
                    String currency = "";
                    if(projectSpecsEMDetails.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getOperationalHubCostType().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getOperationalHubCostType().intValue());
                    }

                    String currency_DB = "";
                    if(projectSpecsEMDetails_DB.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getOperationalHubCostType().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getOperationalHubCostType().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(),
                            ObjStr(projectSpecsEMDetails.getOperationalHubCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getOperationalHubCost()), currency_DB));
                }

                //Other Cost
                if(!isEqual(projectSpecsEMDetails.getOtherCost(), projectSpecsEMDetails_DB.getOtherCost()) ||
                        projectSpecsEMDetails.getOtherCostType()!=projectSpecsEMDetails_DB.getOtherCostType())
                {
                    String currency = "";
                    if(projectSpecsEMDetails.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getOtherCostType().intValue()))
                    {
                        currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getOtherCostType().intValue());
                    }

                    String currency_DB = "";
                    if(projectSpecsEMDetails_DB.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getOtherCostType().intValue()))
                    {
                        currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getOtherCostType().intValue());
                    }

                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMOTHERCOST.getDescription(),
                            ObjStr(projectSpecsEMDetails.getOtherCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getOtherCost()), currency_DB));
                }


                //Name of Proposed Fieldwork Agencies
                if(!StringUtils.equals(projectSpecsEMDetails.getProposedFWAgencyNames(), projectSpecsEMDetails_DB.getProposedFWAgencyNames()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMPROPFWAGNCY.getDescription(),
                            ObjStr(projectSpecsEMDetails.getProposedFWAgencyNames()), ObjStr(projectSpecsEMDetails_DB.getProposedFWAgencyNames())));
                }

                //Estimated Fieldwork Start
                if(!isEqual(projectSpecsEMDetails.getFwStartDate(), projectSpecsEMDetails_DB.getFwStartDate()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWSTART.getDescription(),
                            ObjStr(projectSpecsEMDetails.getFwStartDate()), ObjStr(projectSpecsEMDetails_DB.getFwStartDate())));
                }

                //Estimated Fieldwork Completion
                if(!isEqual(projectSpecsEMDetails.getFwEndDate(), projectSpecsEMDetails_DB.getFwEndDate()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWCOMP.getDescription(),
                            ObjStr(projectSpecsEMDetails.getFwEndDate()), ObjStr(projectSpecsEMDetails_DB.getFwEndDate())));
                }
                //Data Collection Methods
                if(!isEqual(projectSpecsEMDetails.getDataCollectionMethod(), projectSpecsEMDetails_DB.getDataCollectionMethod()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMDCMEH.getDescription(),
                            ObjStr(getDataCollectionNames(projectSpecsEMDetails.getDataCollectionMethod())),
                            ObjStr(getDataCollectionNames(projectSpecsEMDetails_DB.getDataCollectionMethod()))));
                }

                // Quantitative-Total Number of Interviews		
                if(!isEqual(projectSpecsEMDetails.getTotalNoInterviews(), projectSpecsEMDetails_DB.getTotalNoInterviews()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTINT.getDescription(),
                            ObjStr(projectSpecsEMDetails.getTotalNoInterviews()), ObjStr(projectSpecsEMDetails_DB.getTotalNoInterviews())));
                }

                //Quantitative-Total Number of Visits per Respondent
                if(!isEqual(projectSpecsEMDetails.getTotalNoOfVisits(), projectSpecsEMDetails_DB.getTotalNoOfVisits()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTVIS.getDescription(),
                            ObjStr(projectSpecsEMDetails.getTotalNoOfVisits()), ObjStr(projectSpecsEMDetails_DB.getTotalNoOfVisits())));
                }

                //Quantitative-Average Interview Duration
                if(!isEqual(projectSpecsEMDetails.getAvIntDuration(), projectSpecsEMDetails_DB.getAvIntDuration()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTAVGDUR.getDescription(),
                            ObjStr(projectSpecsEMDetails.getAvIntDuration()), ObjStr(projectSpecsEMDetails_DB.getAvIntDuration())));
                }
            }


            //Quantitative-Geographical Spread-Non-National-Geography
            //TODO

        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    public static String generateFieldworkJSONObject(final ProjectSpecsEndMarketDetails projectSpecsEMDetails, final ProjectSpecsEndMarketDetails projectSpecsEMDetails_DB, final Boolean multimarket)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(projectSpecsEMDetails_DB!=null)
        {

            if(!multimarket)
            {
                //FW Estimated Project End Date
                if(!isEqual(projectSpecsEMDetails.getProjectEndDateLatest(), projectSpecsEMDetails_DB.getProjectEndDateLatest()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.FWESTENDDATE.getDescription(),
                            ObjStr(projectSpecsEMDetails.getProjectEndDateLatest()), ObjStr(projectSpecsEMDetails_DB.getProjectEndDateLatest())));
                }
            }

            //Latest Fieldwork Estimate Begin Date
            if(!isEqual(projectSpecsEMDetails.getFwStartDateLatest(), projectSpecsEMDetails_DB.getFwStartDateLatest()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.FWBGNDATE.getDescription(),
                        ObjStr(projectSpecsEMDetails.getFwStartDateLatest()), ObjStr(projectSpecsEMDetails_DB.getFwStartDateLatest())));
            }
            //Latest Fieldwork Estimate End Date
            if(!isEqual(projectSpecsEMDetails.getFwEndDateLatest(), projectSpecsEMDetails_DB.getFwEndDateLatest()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.FWENDDATE.getDescription(),
                        ObjStr(projectSpecsEMDetails.getFwEndDateLatest()), ObjStr(projectSpecsEMDetails_DB.getFwEndDateLatest())));
            }
            //Fieldwork Change Comments
            if(!StringUtils.equals(html2text(projectSpecsEMDetails.getLatestFWComments()), html2text(projectSpecsEMDetails_DB.getLatestFWComments())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.FWCHGCOMM.getDescription(),
                        ObjStr(projectSpecsEMDetails.getLatestFWComments()), ObjStr(projectSpecsEMDetails_DB.getLatestFWComments())));
            }
            //Final Cost
            if(!isEqual(projectSpecsEMDetails.getFinalCost(), projectSpecsEMDetails_DB.getFinalCost()) ||
                    projectSpecsEMDetails.getFinalCostType()!=projectSpecsEMDetails_DB.getFinalCostType())
            {
                String currency = "";
                if(projectSpecsEMDetails.getFinalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails.getFinalCostType().intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails.getFinalCostType().intValue());
                }

                String currency_DB = "";
                if(projectSpecsEMDetails_DB.getFinalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(projectSpecsEMDetails_DB.getFinalCostType().intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(projectSpecsEMDetails_DB.getFinalCostType().intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.FWFINALCOST.getDescription(),
                        ObjStr(projectSpecsEMDetails.getFinalCost()) ,currency, ObjStr(projectSpecsEMDetails_DB.getFinalCost()), currency_DB));
            }
            //Cost change comments
            if(!StringUtils.equals(html2text(projectSpecsEMDetails.getFinalCostComments()), html2text(projectSpecsEMDetails_DB.getFinalCostComments())))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.FWCOSCHGCOMM.getDescription(),
                        ObjStr(projectSpecsEMDetails.getFinalCostComments()), ObjStr(projectSpecsEMDetails_DB.getFinalCostComments())));
            }
        }

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    public static String generateFieldworkAboveMarketJSONObject(final List<ProjectSpecsEndMarketDetails> projectSpecsEMDetails, final List<ProjectSpecsEndMarketDetails> projectSpecsEMDetails_DB,
                                                                final BigDecimal aboveMarketFinalCost, final Integer aboveMarketFinalCostType, final BigDecimal aboveMarketFinalCost_DB, final Integer aboveMarketFinalCostType_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(projectSpecsEMDetails_DB!=null)
        {

            Date projectEndDateLatest = null;
            Date projectEndDateLatest_DB = null;
            for(ProjectSpecsEndMarketDetails projectSpecsEndMarketDetail :projectSpecsEMDetails)
            {
                projectEndDateLatest = projectSpecsEndMarketDetail.getProjectEndDateLatest();

            }
            for(ProjectSpecsEndMarketDetails projectSpecsEndMarketDetail_DB :projectSpecsEMDetails_DB)
            {
                projectEndDateLatest_DB = projectSpecsEndMarketDetail_DB.getProjectEndDateLatest();
            }

            //Above Market : Estimated Project End Date
            if(!isEqual(projectEndDateLatest, projectEndDateLatest_DB))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.FWESTENDDATE.getDescription(),
                        ObjStr(projectEndDateLatest), ObjStr(projectEndDateLatest_DB)));
            }

            //Estimated Project End Date : Final Cost
            if(!isEqual(aboveMarketFinalCost, aboveMarketFinalCost_DB) || aboveMarketFinalCostType!=aboveMarketFinalCostType_DB)
            {
                String currency = "";
                if(aboveMarketFinalCostType!=null && SynchroGlobal.getCurrencies().containsKey(aboveMarketFinalCostType.intValue()))
                {
                    currency = SynchroGlobal.getCurrencies().get(aboveMarketFinalCostType.intValue());
                }

                String currency_DB = "";
                if(aboveMarketFinalCostType_DB!=null && SynchroGlobal.getCurrencies().containsKey(aboveMarketFinalCostType_DB.intValue()))
                {
                    currency_DB = SynchroGlobal.getCurrencies().get(aboveMarketFinalCostType_DB.intValue());
                }

                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.FWFINALCOST.getDescription(),
                        ObjStr(aboveMarketFinalCost) ,currency, ObjStr(aboveMarketFinalCost_DB), currency_DB));
            }
        }

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    /**
     * Single Market Proposal End market Details
     * @param proposalInitiation
     * @param proposalEMDetails
     * @return
     */
    public static String generateProposalEMJSONObject(final ProposalInitiation proposalInitiation, final ProposalEndMarketDetails proposalEMDetails)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(proposalEMDetails!=null)
        {

            //Stimulus Material to be shipped to
            if(!StringUtils.isBlank(html2text(proposalInitiation.getStimulusMaterialShipped())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMMATSHIPPED.getDescription()));
            }

            //Total Cost
            if(proposalEMDetails.getTotalCost()!=null)
            {
                String currency = "";
                if(proposalEMDetails.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getTotalCostType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getTotalCostType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMTOTALCOST.getDescription(), ObjStr(proposalEMDetails.getTotalCost()), currency));
            }

            //International Management Cost - Research Hub Cost
            if(proposalEMDetails.getIntMgmtCost()!=null)
            {
                String currency = "";
                if(proposalEMDetails.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getIntMgmtCostType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getIntMgmtCostType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMINTMGTCOST.getDescription(), ObjStr(proposalEMDetails.getIntMgmtCost()), currency));
            }
            //Local Management Cost
            if(proposalEMDetails.getLocalMgmtCost()!=null)
            {
                String currency = "";
                if(proposalEMDetails.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getLocalMgmtCostType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getLocalMgmtCostType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(), ObjStr(proposalEMDetails.getLocalMgmtCost()), currency));
            }
            //Fieldwork Cost
            if(proposalEMDetails.getFieldworkCost()!=null)
            {
                String currency = "";
                if(proposalEMDetails.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getFieldworkCostType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getFieldworkCostType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMFWCOST.getDescription(), ObjStr(proposalEMDetails.getFieldworkCost()), currency));
            }
            //Operational Hub Cost
            if(proposalEMDetails.getOperationalHubCost()!=null)
            {
                String currency = "";
                if(proposalEMDetails.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getOperationalHubCostType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getOperationalHubCostType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMHUBCOST.getDescription(), ObjStr(proposalEMDetails.getOperationalHubCost()), currency));
            }
            //Other Cost
            if(proposalEMDetails.getOtherCost()!=null)
            {
                String currency = "";
                if(proposalEMDetails.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getOtherCostType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getOtherCostType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMOTHERCOST.getDescription(), ObjStr(proposalEMDetails.getOtherCost()), currency));
            }

            //Name of Proposed Fieldwork Agencies
            if(!StringUtils.isBlank(proposalEMDetails.getProposedFWAgencyNames()))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMPROPFWAGNCY.getDescription()));
            }

            //Estimated Fieldwork Start
            if(proposalEMDetails.getFwStartDate()!=null)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWSTART.getDescription()));
            }
            //Estimated Fieldwork Completion
            if(proposalEMDetails.getFwEndDate()!=null)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWCOMP.getDescription()));
            }
            //Data Collection Methods
            if(proposalEMDetails.getDataCollectionMethod()!=null && proposalEMDetails.getDataCollectionMethod().size() > 0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMDCMEH.getDescription()));
            }

            // Quantitative-Total Number of Interviews
            if(proposalEMDetails.getTotalNoInterviews()!=null)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTINT.getDescription()));
            }

            //Quantitative-Total Number of Visits per Respondent
            if(proposalEMDetails.getTotalNoOfVisits()!=null)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTVIS.getDescription()));
            }

            //Quantitative-Average Interview Duration
            if(proposalEMDetails.getAvIntDuration()!=null)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTAVGDUR.getDescription()));
            }

            //Quantitative-Geographical Spread-Non-National-Geography
            //TODO

            //Proposal and Cost Template
            if(!StringUtils.isBlank(html2text(proposalInitiation.getProposalCostTemplate())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PROPCOSTTEMP.getDescription()));
            }

        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    /**
     * Multimarket Proposal End market details
     * @param proposalEMDetails
     * @return
     */
    public static String generateProposalMultiMarketEMJSONObject(final ProposalEndMarketDetails proposalEMDetails)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(proposalEMDetails!=null)
        {

            //Total Cost
            if(proposalEMDetails.getTotalCost()!=null)
            {
                String currency = "";
                if(proposalEMDetails.getTotalCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getTotalCostType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getTotalCostType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMTOTALCOST.getDescription(), ObjStr(proposalEMDetails.getTotalCost()), currency));
            }

            //International Management Cost - Research Hub Cost
            if(proposalEMDetails.getIntMgmtCost()!=null)
            {
                String currency = "";
                if(proposalEMDetails.getIntMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getIntMgmtCostType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getIntMgmtCostType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMINTMGTCOST.getDescription(), ObjStr(proposalEMDetails.getIntMgmtCost()), currency));
            }
            //Local Management Cost
            if(proposalEMDetails.getLocalMgmtCost()!=null)
            {
                String currency = "";
                if(proposalEMDetails.getLocalMgmtCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getLocalMgmtCostType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getLocalMgmtCostType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMLCLMGTCOST.getDescription(), ObjStr(proposalEMDetails.getLocalMgmtCost()), currency));
            }
            //Fieldwork Cost
            if(proposalEMDetails.getFieldworkCost()!=null)
            {
                String currency = "";
                if(proposalEMDetails.getFieldworkCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getFieldworkCostType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getFieldworkCostType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMFWCOST.getDescription(), ObjStr(proposalEMDetails.getFieldworkCost()), currency));
            }
            //Operational Hub Cost
            if(proposalEMDetails.getOperationalHubCost()!=null)
            {
                String currency = "";
                if(proposalEMDetails.getOperationalHubCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getOperationalHubCostType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getOperationalHubCostType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMHUBCOST.getDescription(), ObjStr(proposalEMDetails.getOperationalHubCost()), currency));
            }
            //Other Cost
            if(proposalEMDetails.getOtherCost()!=null)
            {
                String currency = "";
                if(proposalEMDetails.getOtherCostType()!=null && SynchroGlobal.getCurrencies().containsKey(proposalEMDetails.getOtherCostType()))
                {
                    currency = SynchroGlobal.getCurrencies().get(proposalEMDetails.getOtherCostType());
                }
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CURRENCY.getId(), SynchroGlobal.LogFields.EMOTHERCOST.getDescription(), ObjStr(proposalEMDetails.getOtherCost()), currency));
            }

            //Name of Proposed Fieldwork Agencies
            if(!StringUtils.isBlank(proposalEMDetails.getProposedFWAgencyNames()))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMPROPFWAGNCY.getDescription()));
            }

            //Estimated Fieldwork Start
            if(proposalEMDetails.getFwStartDate()!=null)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWSTART.getDescription()));
            }
            //Estimated Fieldwork Completion
            if(proposalEMDetails.getFwEndDate()!=null)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.DATE.getId(), SynchroGlobal.LogFields.EMFWCOMP.getDescription()));
            }
            //Data Collection Methods
            if(proposalEMDetails.getDataCollectionMethod()!=null && proposalEMDetails.getDataCollectionMethod().size() > 0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMDCMEH.getDescription()));
            }

            // Quantitative-Total Number of Interviews
            if(proposalEMDetails.getTotalNoInterviews()!=null)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTINT.getDescription()));
            }

            //Quantitative-Total Number of Visits per Respondent
            if(proposalEMDetails.getTotalNoOfVisits()!=null)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTVIS.getDescription()));
            }

            //Quantitative-Average Interview Duration
            if(proposalEMDetails.getAvIntDuration()!=null)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EMQUANTAVGDUR.getDescription()));
            }

            //Quantitative-Geographical Spread-Non-National-Geography
            //TODO

        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    public static String generateProposalFieldAddJSONObject(final ProposalInitiation proposalInitiation)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(proposalInitiation!=null)
        {

            //Stimulus Material to be shipped to
            if(!StringUtils.isBlank(html2text(proposalInitiation.getStimulusMaterialShipped())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.STIMMATSHIPPED.getDescription()));
            }

            //Proposal and Cost Template
            if(!StringUtils.isBlank(html2text(proposalInitiation.getProposalCostTemplate())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.PROPCOSTTEMP.getDescription()));
            }

        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    /**
     * Single Market
     * @param projectSpecsInitiation
     * @return
     */
    public static String generateProjectSpecsSpecificJSONObject(final ProjectSpecsInitiation projectSpecsInitiation)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(projectSpecsInitiation!=null)
        {
            //Screener
            if(!StringUtils.isBlank(html2text(projectSpecsInitiation.getScreener())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.SCREENER.getDescription()));
            }

            //Consumer Contract and Confidentiality Agreement
            if(!StringUtils.isBlank(html2text(projectSpecsInitiation.getConsumerCCAgreement())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.CONSUMERCONTRACTAGREEMENT.getDescription()));
            }
            //Questionnaire/Discussion guide
            if(!StringUtils.isBlank(html2text(projectSpecsInitiation.getQuestionnaire())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.QUESTIONNAIRE.getDescription()));
            }
            //Actual Stimulus Material
            if(!StringUtils.isBlank(html2text(projectSpecsInitiation.getStimulusMaterial())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.ACTSTIMMAT.getDescription()));
            }

        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    public static String generateProjectSpecsLegalApproverJSONObject(final ProjectSpecsInitiation projectSpecsInitiation, final ProjectSpecsInitiation projectSpecsInitiation_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(projectSpecsInitiation!=null && projectSpecsInitiation_DB!=null)
        {
            //Legal Approval - Stimulus Material from
            if(!isEqual(projectSpecsInitiation.getLegalApprovalStimulus(), projectSpecsInitiation_DB.getLegalApprovalStimulus()) && projectSpecsInitiation.getLegalApprovalStimulus())
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CBAPPROVE.getId(), SynchroGlobal.LogFields.LEGALSTIM.getDescription()));
            }

            //Legal Approval - Screener from
            if(!isEqual(projectSpecsInitiation.getLegalApprovalScreener(), projectSpecsInitiation_DB.getLegalApprovalScreener()) && projectSpecsInitiation.getLegalApprovalScreener())
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CBAPPROVE.getId(), SynchroGlobal.LogFields.LEGALSCREEN.getDescription()));
            }

            //Legal Approval - Consumer Contract and Confidentiality Agreement from
            if(!isEqual(projectSpecsInitiation.getLegalApprovalCCCA(), projectSpecsInitiation_DB.getLegalApprovalCCCA()) && projectSpecsInitiation.getLegalApprovalCCCA())
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CBAPPROVE.getId(), SynchroGlobal.LogFields.LEGALAGREEM.getDescription()));
            }

            //Legal Approval - Questionnaire from
            if(!isEqual(projectSpecsInitiation.getLegalApprovalQuestionnaire(), projectSpecsInitiation_DB.getLegalApprovalQuestionnaire()) && projectSpecsInitiation.getLegalApprovalQuestionnaire())
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CBAPPROVE.getId(), SynchroGlobal.LogFields.LEGALQUEST.getDescription()));
            }

            //Legal Approval - Discussion guide from
            if(!isEqual(projectSpecsInitiation.getLegalApprovalDG(), projectSpecsInitiation_DB.getLegalApprovalDG()) && projectSpecsInitiation.getLegalApprovalDG())
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CBAPPROVE.getId(), SynchroGlobal.LogFields.LEGALDISGUIDE.getDescription()));
            }

        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    /** Report Summary
     *
     */
    public static String generateRepSummaryLegalApproverJSONObject(final ReportSummaryInitiation reportSummaryInitiation, final ReportSummaryInitiation reportSummaryInitiation_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(reportSummaryInitiation!=null && reportSummaryInitiation_DB!=null)
        {
            //Legal Approval - Stimulus Material from
            if(!isEqual(reportSummaryInitiation.getLegalApproval(), reportSummaryInitiation_DB.getLegalApproval()))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.CBREPORTLEGALAPPROVER.getDescription(),
                        getBoolean(reportSummaryInitiation_DB.getLegalApproval())?"Selected":"Unselected"));
            }
        }
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    public static String generateWaiverJSONObject(final PIBMethodologyWaiver pibMethodologyWaiver_DB, final PIBMethodologyWaiver pibMethodologyWaiver)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(pibMethodologyWaiver!=null)
        {
            if(pibMethodologyWaiver_DB!=null)
            {
                //Update Methodology Waiver for PIB
                if(!StringUtils.equals(pibMethodologyWaiver.getMethodologyDeviationRationale(), pibMethodologyWaiver_DB.getMethodologyDeviationRationale()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.METHDEVRNL.getDescription(), ObjStr(pibMethodologyWaiver.getMethodologyDeviationRationale()), ObjStr(pibMethodologyWaiver_DB.getMethodologyDeviationRationale())));
                }
                if(pibMethodologyWaiver.getMethodologyApprover().intValue()!=pibMethodologyWaiver_DB.getMethodologyApprover().intValue())
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.METHAPR.getDescription(), getUserName(pibMethodologyWaiver.getMethodologyApprover()), getUserName(pibMethodologyWaiver_DB.getMethodologyApprover())));
                }
                if(!StringUtils.equals(pibMethodologyWaiver.getMethodologyApproverComment(), pibMethodologyWaiver_DB.getMethodologyApproverComment()))
                {
                    data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.METHAPRCOMM.getDescription(), ObjStr(pibMethodologyWaiver.getMethodologyApproverComment()), ObjStr(pibMethodologyWaiver_DB.getMethodologyApproverComment())));
                }
            }
            else
            {
                if(!StringUtils.isBlank(html2text(pibMethodologyWaiver.getMethodologyDeviationRationale())))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.METHDEVRNL.getDescription()));
                }
                if(pibMethodologyWaiver.getMethodologyApprover()!=null && pibMethodologyWaiver.getMethodologyApprover()>0)
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.METHAPR.getDescription()));
                }
                if(!StringUtils.isBlank(html2text(pibMethodologyWaiver.getMethodologyApproverComment())))
                {
                    data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.METHAPRCOMM.getDescription()));
                }

            }

        }

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }



    public static String generateKantarWaiverJSONObject(final PIBMethodologyWaiver pibKantarMethodologyWaiver_DB, final PIBMethodologyWaiver pibKantarMethodologyWaiver)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        if(pibKantarMethodologyWaiver_DB!=null)
        {
            //Update existing PIB Non-Kantar Waiver Activity
            if(!StringUtils.equals(pibKantarMethodologyWaiver.getMethodologyDeviationRationale(), pibKantarMethodologyWaiver_DB.getMethodologyDeviationRationale()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.KANTARDEVRNL.getDescription(), ObjStr(pibKantarMethodologyWaiver.getMethodologyDeviationRationale()), ObjStr(pibKantarMethodologyWaiver_DB.getMethodologyDeviationRationale())));
            }
            if(pibKantarMethodologyWaiver.getMethodologyApprover().intValue()!=pibKantarMethodologyWaiver_DB.getMethodologyApprover().intValue())
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.KANTARAPR.getDescription(), getUserName(pibKantarMethodologyWaiver.getMethodologyApprover()), getUserName(pibKantarMethodologyWaiver_DB.getMethodologyApprover())));
            }
            if(!StringUtils.equals(pibKantarMethodologyWaiver.getMethodologyApproverComment(), pibKantarMethodologyWaiver_DB.getMethodologyApproverComment()))
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.KANTARAPRCOMM.getDescription(), ObjStr(pibKantarMethodologyWaiver.getMethodologyApproverComment()), ObjStr(pibKantarMethodologyWaiver_DB.getMethodologyApproverComment())));
            }
        }
        else
        {
            //Add new PIB Non-Kantar Waiver Activity
            if(!StringUtils.isBlank(html2text(pibKantarMethodologyWaiver.getMethodologyDeviationRationale())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.KANTARDEVRNL.getDescription()));
            }
            if(pibKantarMethodologyWaiver.getMethodologyApprover()!=null && pibKantarMethodologyWaiver.getMethodologyApprover()>0)
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.USER.getId(), SynchroGlobal.LogFields.KANTARAPR.getDescription()));
            }
            if(!StringUtils.isBlank(html2text(pibKantarMethodologyWaiver.getMethodologyApproverComment())))
            {
                data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.KANTARAPRCOMM.getDescription()));
            }

        }


        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    public static String generateReportUpdateJSONObject(final ReportSummaryInitiation reportSummaryInitiation, final ReportSummaryInitiation reportSummaryInitiation_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        //Full Report Checkbox
        if(!isEqual(reportSummaryInitiation.getFullReport(), reportSummaryInitiation_DB.getFullReport()))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.CBFULLREPORT.getDescription(),
                    getBoolean(reportSummaryInitiation.getFullReport())?"Selected":"Unselected"));
        }

        //Summary Report Checkbox
        if(!isEqual(reportSummaryInitiation.getSummaryReport(), reportSummaryInitiation_DB.getSummaryReport()))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.CBSUMREPORT.getDescription(),
                    getBoolean(reportSummaryInitiation.getSummaryReport())?"Selected":"Unselected"));
        }
        //Summary for IRIS Checkbox
        if(!isEqual(reportSummaryInitiation.getSummaryForIRIS(), reportSummaryInitiation_DB.getSummaryForIRIS()))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.CBSUMIRIS.getDescription(),
                    getBoolean(reportSummaryInitiation.getSummaryForIRIS())?"Selected":"Unselected"));
        }
        //Report Comments
        if(!StringUtils.equals(html2text(reportSummaryInitiation.getComments()), html2text(reportSummaryInitiation_DB.getComments())))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.REPORTCOMMENT.getDescription(),
                    ObjStr(reportSummaryInitiation.getComments()), ObjStr(reportSummaryInitiation_DB.getComments())));
        }

        // Save Project related fields Audit when PIB is saved for the first time
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }


    public static String generateReportJSONObject(final ReportSummaryInitiation reportSummaryInitiation)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        //Full Report checkbox
        if(reportSummaryInitiation.getFullReport()!=null && reportSummaryInitiation.getFullReport())
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.CBFULLREPORT.getDescription(), "Selected"));
        }
        //Summary Report checkbox
        if(reportSummaryInitiation.getSummaryReport()!=null && reportSummaryInitiation.getSummaryReport())
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.CBSUMREPORT.getDescription(), "Selected"));
        }
        //Summary for IRIS checkbox
        if(reportSummaryInitiation.getSummaryForIRIS()!=null && reportSummaryInitiation.getSummaryForIRIS())
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.CHECKBOX.getId(), SynchroGlobal.LogFields.CBSUMIRIS.getDescription(), "Selected"));
        }

        //Report Comments
        if(!StringUtils.isBlank(html2text(reportSummaryInitiation.getComments())))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.REPORTCOMMENT.getDescription()));
        }

        // Save Project related fields Audit when PIB is saved for the first time
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    /** TODO
     * Project Evaluation
     * @param projectEvaluationInitiation
     * @param projectEvaluationInitiation_DB
     * @return
     */
    public static String generateEvaluationUpdateJSONObject(final ProjectEvaluationInitiation projectEvaluationInitiation, final ProjectEvaluationInitiation projectEvaluationInitiation_DB)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        //International Management-Ratings
        if(projectEvaluationInitiation.getAgencyPerfIM()!=projectEvaluationInitiation_DB.getAgencyPerfIM())
        {
            if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation_DB.getAgencyPerfIM()!=null)
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.EVALIMRATING.getDescription(),
                        SynchroGlobal.AgencyPerformance.getById(projectEvaluationInitiation.getAgencyPerfIM()).getDescription(),
                        SynchroGlobal.AgencyPerformance.getById(projectEvaluationInitiation_DB.getAgencyPerfIM()).getDescription()));
            }
        }

        //International Management-BAT Comments
        if(!StringUtils.equals(html2text(projectEvaluationInitiation.getBatCommentsIM()), html2text(projectEvaluationInitiation_DB.getBatCommentsIM())))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EVALIMBAT.getDescription(),
                    ObjStr(projectEvaluationInitiation.getBatCommentsIM()), ObjStr(projectEvaluationInitiation_DB.getBatCommentsIM())));
        }

        //International Management-Agency Comments
        if(!StringUtils.equals(html2text(projectEvaluationInitiation.getAgencyCommentsIM()), html2text(projectEvaluationInitiation_DB.getAgencyCommentsIM())))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EVALIMAGNCY.getDescription(),
                    ObjStr(projectEvaluationInitiation.getAgencyCommentsIM()), ObjStr(projectEvaluationInitiation_DB.getAgencyCommentsIM())));
        }

        //Local Management-Ratings
        if(projectEvaluationInitiation.getAgencyPerfLM()!=projectEvaluationInitiation_DB.getAgencyPerfLM())
        {
            if(projectEvaluationInitiation.getAgencyPerfLM()!=null && projectEvaluationInitiation_DB.getAgencyPerfLM()!=null)
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.EVALLMRATING.getDescription(),
                        SynchroGlobal.AgencyPerformance.getById(projectEvaluationInitiation.getAgencyPerfLM()).getDescription(),
                        SynchroGlobal.AgencyPerformance.getById(projectEvaluationInitiation_DB.getAgencyPerfLM()).getDescription()));
            }
        }
        //Local Management-BAT Comments
        if(!StringUtils.equals(html2text(projectEvaluationInitiation.getBatCommentsLM()), html2text(projectEvaluationInitiation_DB.getBatCommentsLM())))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EVALLMBAT.getDescription(),
                    ObjStr(projectEvaluationInitiation.getBatCommentsLM()), ObjStr(projectEvaluationInitiation_DB.getBatCommentsLM())));
        }

        //Local Management-Agency Comments
        if(!StringUtils.equals(html2text(projectEvaluationInitiation.getAgencyCommentsLM()), html2text(projectEvaluationInitiation_DB.getAgencyCommentsLM())))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EVALLMAGNCY.getDescription(),
                    ObjStr(projectEvaluationInitiation.getAgencyCommentsLM()), ObjStr(projectEvaluationInitiation_DB.getAgencyCommentsLM())));
        }

        //Fieldwork Agencies-Ratings
        if(projectEvaluationInitiation.getAgencyPerfFA()!=projectEvaluationInitiation_DB.getAgencyPerfFA())
        {
            if(projectEvaluationInitiation.getAgencyPerfFA()!=null && projectEvaluationInitiation_DB.getAgencyPerfFA()!=null)
            {
                data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.EVALFARATING.getDescription(),
                        SynchroGlobal.AgencyPerformance.getById(projectEvaluationInitiation.getAgencyPerfFA()).getDescription(),
                        SynchroGlobal.AgencyPerformance.getById(projectEvaluationInitiation_DB.getAgencyPerfFA()).getDescription()));
            }
        }
        //Fieldwork Agencies-BAT Comments
        if(!StringUtils.equals(html2text(projectEvaluationInitiation.getBatCommentsFA()), html2text(projectEvaluationInitiation_DB.getBatCommentsFA())))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EVALFABAT.getDescription(),
                    ObjStr(projectEvaluationInitiation.getBatCommentsFA()), ObjStr(projectEvaluationInitiation_DB.getBatCommentsFA())));
        }

        //Fieldwork Agencies-Agency Comments
        if(!StringUtils.equals(html2text(projectEvaluationInitiation.getAgencyCommentsFA()), html2text(projectEvaluationInitiation_DB.getAgencyCommentsFA())))
        {
            data.addLogFieldObject(LogFieldObject.getLogEditedField(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EVALFAAGNCY.getDescription(),
                    ObjStr(projectEvaluationInitiation.getAgencyCommentsFA()), ObjStr(projectEvaluationInitiation_DB.getAgencyCommentsFA())));
        }


        // Save Project related fields Audit when PIB is saved for the first time
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    /**
     * Project Evaluation 
     * @param projectEvaluationInitiation
     * @return
     */
    public static String generateEvaluationJSONObject(final ProjectEvaluationInitiation projectEvaluationInitiation)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        //International Management - Agency Performance for Project
        if(projectEvaluationInitiation.getAgencyPerfIM()!=null && projectEvaluationInitiation.getAgencyPerfIM()>0)
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.EVALIMRATING.getDescription()));
        }
        //International Management- BAT Comments
        if(!StringUtils.isBlank(html2text(projectEvaluationInitiation.getBatCommentsIM())))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EVALIMBAT.getDescription()));
        }
        //International Management- Agency Comments
        if(!StringUtils.isBlank(html2text(projectEvaluationInitiation.getAgencyCommentsIM())))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EVALIMBAT.getDescription()));
        }

        //Local Management - Agency Performance for Project
        if(projectEvaluationInitiation.getAgencyPerfLM()!=null && projectEvaluationInitiation.getAgencyPerfLM()>0)
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.EVALLMRATING.getDescription()));
        }
        //International Management- BAT Comments
        if(!StringUtils.isBlank(html2text(projectEvaluationInitiation.getBatCommentsLM())))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EVALLMBAT.getDescription()));
        }
        //International Management- Agency Comments
        if(!StringUtils.isBlank(html2text(projectEvaluationInitiation.getAgencyCommentsLM())))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EVALLMBAT.getDescription()));
        }

        //Fieldwork Agencies - Agency Performance for Project
        if(projectEvaluationInitiation.getAgencyPerfFA()!=null && projectEvaluationInitiation.getAgencyPerfFA()>0)
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.SELECT.getId(), SynchroGlobal.LogFields.EVALFARATING.getDescription()));
        }
        //Fieldwork Agencies - BAT Comments
        if(!StringUtils.isBlank(html2text(projectEvaluationInitiation.getBatCommentsFA())))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EVALFABAT.getDescription()));
        }
        //Fieldwork Agencies- Agency Comments
        if(!StringUtils.isBlank(html2text(projectEvaluationInitiation.getAgencyCommentsFA())))
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.TEXT.getId(), SynchroGlobal.LogFields.EVALFABAT.getDescription()));
        }

        // Save Project related fields Audit when PIB is saved for the first time
        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    /**
     * Notification JSON
     * @param usernames
     * @param description
     * @return
     */
    public static String generateNotificationJSONObject(List<String> usernames, String description)
    {
        String json = StringUtils.EMPTY;
        LogData data = new LogData();

        for(String username: usernames)
        {
            data.addLogFieldObject(new LogFieldObject(SynchroGlobal.LogFieldType.NOTIFICATION.getId(), "", description.replaceAll("user", username)));
        }

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(data);
        }catch(JsonGenerationException jgenex)
        {
            jgenex.printStackTrace();
            Log.error("Error while Saving Log trail " + jgenex.getMessage());
        }
        catch(JsonMappingException jmapex)
        {
            jmapex.printStackTrace();
            Log.error("Error while Saving Logs " + jmapex.getMessage());
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
            Log.error("Error while Saving Logs " + ioex.getMessage());
        }

        return json;
    }

    public static List<String> parseJSONToList(String json, Integer stage, Integer activityType)
    {
        List<String> logs = new ArrayList<String>();

        if(json==null)
            return logs;

        JSONObject jsonObject = null;
        try
        {
            jsonObject = (JSONObject)new JSONParser().parse(json);
        }catch(org.json.simple.parser.ParseException pexc)
        {
            LOGGER.error("Error while parsing json data " + pexc.getStackTrace());
        }

        Boolean multiple = (Boolean) jsonObject.get("multiple");
        if(multiple)
        {
            // Multiple log entries
            JSONArray logData = (JSONArray) jsonObject.get("data");

            Iterator itr = logData.iterator();
            while (itr.hasNext())
            {
                JSONObject innerObj = (JSONObject) itr.next();
                Long fieldType = (Long) innerObj.get("fieldType");
                if(fieldType!=null && fieldType>0)
                {
                    if(activityType.intValue() == SynchroGlobal.Activity.ADD.getId())
                    {
                        if(fieldType.intValue() == SynchroGlobal.LogFieldType.TEXT.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            String description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldName;
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                        else if(fieldType.intValue() == SynchroGlobal.LogFieldType.CURRENCY.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            String fieldValue = (String) innerObj.get("fieldValue");
                            String currency = (String) innerObj.get("currency");
                            String description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldName + " ( " + fieldValue + " " + currency +" ) ";
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                        else if(fieldType.intValue() == SynchroGlobal.LogFieldType.SELECT.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            String description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldName;
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                        else if(fieldType.intValue() == SynchroGlobal.LogFieldType.DATE.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            String description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldName;
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                        else if(fieldType.intValue() == SynchroGlobal.LogFieldType.CHECKBOX.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            String fieldValue = (String) innerObj.get("fieldValue");
                            String description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldName + " " + fieldValue;
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                        else if(fieldType.intValue() == SynchroGlobal.LogFieldType.USER.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            String description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldName;
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                        else if(fieldType.intValue() == SynchroGlobal.LogFieldType.STAKEHOLDER.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            String description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " +
                                    SynchroGlobal.LogFieldType.STAKEHOLDER.getDescription() + "(" +fieldName + ")";
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                    }
                    else  if(activityType.intValue() == SynchroGlobal.Activity.EDIT.getId())
                    {
                        if(fieldType.intValue() == SynchroGlobal.LogFieldType.TEXT.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            String fieldValue = (String) innerObj.get("fieldValue");
                            String fieldValue_Prev = (String) innerObj.get("fieldValue_Prev");
                            String description = StringUtils.EMPTY;
                            description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldName  + " Changed from " + (fieldValue_Prev==null?"":fieldValue_Prev);
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                        else if(fieldType.intValue() == SynchroGlobal.LogFieldType.CURRENCY.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            String fieldValue = (String) innerObj.get("fieldValue");
                            String currency = (String) innerObj.get("currency");
                            String fieldValue_Prev = (String) innerObj.get("fieldValue_Prev");
                            String currency_Prev = (String) innerObj.get("currency_Prev");
                            String description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- Changed from " + fieldName + "(" + (fieldValue_Prev==null?"":fieldValue_Prev) + " " + (currency_Prev==null?"":currency_Prev) +") to " + fieldName + "(" + (fieldValue==null?"":fieldValue) + " " + (currency==null?"":currency) +" )";
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                        else if(fieldType.intValue() == SynchroGlobal.LogFieldType.SELECT.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            String fieldValue = (String) innerObj.get("fieldValue");
                            String fieldValue_Prev = (String) innerObj.get("fieldValue_Prev");
                            String description = StringUtils.EMPTY;
                            description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- Changed from " + fieldName + "(" +  (fieldValue_Prev==null?"":fieldValue_Prev) + ") to "+ fieldName + "(" + (fieldValue==null?"":fieldValue) + ")";
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                        else if(fieldType.intValue() == SynchroGlobal.LogFieldType.DATE.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            String fieldValue = (String) innerObj.get("fieldValue");
                            String fieldValue_Prev = (String) innerObj.get("fieldValue_Prev");
                            String description = StringUtils.EMPTY;
                            if(StringUtils.isBlank(fieldValue_Prev))
                            {
                                description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldName + " changed to " + fieldValue;
                            }
                            else
                            {
                                description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- Changed from " + fieldName + "(" +  fieldValue_Prev + ") to "+ fieldName + "(" + fieldValue + ")";
                            }
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                        else if(fieldType.intValue() == SynchroGlobal.LogFieldType.CHECKBOX.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            String fieldValue = (String) innerObj.get("fieldValue");
                            String description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldName + " " + fieldValue;
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                        else
                        {
                            //TODO
                            String fieldName = (String) innerObj.get("fieldName");
                            String fieldValue = (String) innerObj.get("fieldValue");
                            String fieldValue_Prev = (String) innerObj.get("fieldValue_Prev");
                            String description = StringUtils.EMPTY;
                            if(StringUtils.isBlank(fieldValue_Prev))
                            {
                                description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldName + " changed to " + fieldValue;
                            }
                            else
                            {
                                description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- Changed from " + fieldName + "(" +  fieldValue_Prev + ") to "+ fieldName + "(" + fieldValue + ")";
                            }

                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                    }
                    else if(activityType.intValue() == SynchroGlobal.Activity.NOTIFICATION.getId())
                    {
                        String fieldValue = (String) innerObj.get("fieldValue");
                        String description = StringUtils.EMPTY;
                        description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldValue;
                        if(!StringUtils.isBlank(description))
                            logs.add(description);
                    }
                    else if(activityType.intValue() == SynchroGlobal.Activity.APPROVE.getId())
                    {
                        if(fieldType.intValue() == SynchroGlobal.LogFieldType.CBAPPROVE.getId())
                        {
                            String fieldName = (String) innerObj.get("fieldName");
                            //String fieldValue = (String) innerObj.get("fieldValue");
                            String description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldName;
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }
                        else
                        {
                            //TODO
                            String fieldName = (String) innerObj.get("fieldName");
                            String fieldValue = (String) innerObj.get("fieldValue");
                            String description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + fieldName + " : " + fieldValue;
                            if(!StringUtils.isBlank(description))
                                logs.add(description);
                        }

                    }
                }

            }
        }
        else
        {
            //Single Log Entry like Project Create, Draft, Button Click, Approve etc....
            String description = StringUtils.EMPTY;

            if(stage>SynchroGlobal.LogProjectStage.CREATE.getId())
            {
                if(stage>0)
                {
                    description = SynchroGlobal.LogProjectStage.getById(stage).getDescription() + "- " + (String) jsonObject.get("description");
                }
                else
                {
                    description = (String) jsonObject.get("description");
                }

            }
            else
            {
                description = (String) jsonObject.get("description");
            }


            if(!StringUtils.isBlank(description))
                logs.add(description);
        }

        return logs;
    }


    public static Boolean JSONNotEmpty(String json)
    {
        Boolean hasChanged = false;

        if(json==null)
            return false;

        JSONObject jsonObject = null;

        try
        {
            jsonObject = (JSONObject)new JSONParser().parse(json);
        }catch(org.json.simple.parser.ParseException pexc)
        {
            LOGGER.error("Error while parsing json data " + pexc.getStackTrace());
        }

        Boolean multiple = (Boolean) jsonObject.get("multiple");
        if(multiple)
        {

            JSONArray logData = (JSONArray) jsonObject.get("data");
            if(logData!=null && !logData.isEmpty())
                hasChanged = true;
        }
        else
        {
            String description = StringUtils.EMPTY;
            description = (String) jsonObject.get("description");
            if(!StringUtils.isBlank(description))
                hasChanged = true;
        }

        return hasChanged;
    }


    public static LogData getJSONObject(final String description)
    {
        LogData data = new LogData(description);
        return data;
    }

    public static String html2text(String html) {
        if(StringUtils.isBlank(html))
            return StringUtils.EMPTY;

        return html.replaceAll("\\<.*?>","");
        //return Jsoup.parse(html).text();
    }

    public static String ObjStr(final Long value)
    {
        if(value==null)
            return null;
        return value.toString();
    }
    public static String ObjStr(final Integer value)
    {
        if(value==null)
            return null;
        return value.toString();
    }
    public static String ObjStr(final String value)
    {
        if(value==null)
            return null;

        return html2text(value.toString());
    }

    public static String ObjStr(final BigDecimal value)
    {
        if(value==null)
            return null;
        return value.toString();
    }
    public static String ObjStr(final Date value)
    {
        if(value==null)
            return null;
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(value);
    }

    public static String ObjStr(final List<String> value)
    {
        if(value==null)
            return null;

        return StringUtils.join(value, ',');
    }


    public static Boolean getBoolean(final Boolean value)
    {
        if(value == null)
            return false;
        return value;
    }

    public static Boolean isEqual(BigDecimal value1, BigDecimal value2)
    {
        // null, 1
        if(value1== null && value2 == null)
            return true;
        else if(value1 == null || value2 == null)
            return false;
        else
        {

            if(value1.compareTo(value2)==1)
            {
                return false;
            }
            else
                return true;
        }

    }

    public static Boolean isEqual(Date value1, Date value2)
    {
        // null, 1
        if(value1== null && value2 == null)
            return true;
        else if(value1 == null || value2 == null)
            return false;
        else
        {
            if(value1.compareTo(value2)==0)
            {
                return true;
            }
            else
                return false;
        }
    }

    public static Boolean isEqual(Boolean value1, Boolean value2)
    {
        if(value1== null && value2 == null)
            return true;
        else if(value1 == null || value2 == null)
            return false;
        else
        {
            if(value1 == value2)
            {
                return true;
            }
            else
                return false;
        }
    }

    public static Boolean isEqual(Long value1, Long value2)
    {
        value1 = (value1!=null && value1>0)?value1:null;
        value2 = (value2!=null && value2>0)?value2:null;
        if(value1== null && value2 == null)
            return true;
        else if(value1 == null || value2 == null)
            return false;
        else
        {
            if(value1.intValue() == value2.intValue())
            {
                return true;
            }
            else
                return false;
        }
    }

    public static Boolean isEqual(String value1, Long value2)
    {
        value1 = (value1!=null && !value1.equals("") && Integer.parseInt(value1)>0)?value1:null;
        value2 = (value2!=null  && !value2.equals("") && value2>0)?value2:null;

        if(value1== null && value2 == null)
            return true;
        else if(value1 == null || value2 == null)
            return false;
        else
        {
            if(Integer.parseInt(value1) == value2.intValue())
            {
                return true;
            }
            else
                return false;
        }
    }


    public static Boolean isEqual(Integer value1, Integer value2)
    {
        if(value1== null && value2 == null)
            return true;
        else if(value1 == null || value2 == null)
            return false;
        else
        {
            if(value1.intValue() == value2.intValue())
            {
                return true;
            }
            else
                return false;
        }
    }

    public static Boolean isEqual(Integer value1, Long value2)
    {
        if(value1== null && value2 == null)
            return true;
        else if(value1 == null || value2 == null)
            return false;
        else
        {
            if(value1.intValue() == value2.intValue())
            {
                return true;
            }
            else
                return false;
        }
    }

    public static Boolean isEqual(List<Long> value1, List<Long> value2)
    {
        Boolean unequal = false;
        if(value1== null && value2 == null)
            return true;
        else if(value1 == null || value2 == null)
            return false;
        else
        {
            if(value1.size()!=value2.size())
                return false;

            for(Long val : value1)
            {
                if(!value2.contains(val))
                {
                    unequal = true;
                }
                if(unequal)
                    break;
            }
        }
        if(unequal)
            return false;
        else
            return true;
    }

    public static List<String> getDataCollectionNames(List<Long> value)
    {
        List<String> collection =  new ArrayList<String>();
        if(value==null)
            return null;

        for(Long val : value)
        {
            if(val!=null && val>0 && SynchroGlobal.getDataCollections().get(val.intValue())!=null)
            {
                collection.add(SynchroGlobal.getDataCollections().get(val.intValue()));
            }
        }
        return collection;

    }


    public static List<String> getDataCategoryNames(List<Long> value)
    {
        List<String> collection =  new ArrayList<String>();
        if(value==null)
            return null;

        for(Long val : value)
        {
            if(val!=null && val>0 && SynchroGlobal.getProductTypes().get(val.intValue())!=null)
            {
                collection.add(SynchroGlobal.getProductTypes().get(val.intValue()));
            }
        }
        return collection;

    }

    public static List<String> getProposedMethodologyNames(List<Long> value)
    {
        List<String> collection =  new ArrayList<String>();
        if(value==null)
            return null;

        for(Long val : value)
        {
            if(val!=null && val>0 && SynchroGlobal.getMethodologies().get(val.intValue())!=null)
            {
                collection.add(SynchroGlobal.getMethodologies().get(val.intValue()));
            }
        }
        return collection;

    }

    public static List<String> getCountryNames(List<Long> value)
    {
        List<String> collection =  new ArrayList<String>();
        if(value==null)
            return null;

        for(Long val : value)
        {
            if(val!=null && val>0 && SynchroGlobal.getEndMarkets().get(val.intValue())!=null)
            {
                collection.add(SynchroGlobal.getEndMarkets().get(val.intValue()));
            }
        }
        return collection;

    }


    public static String getUserName(final Long userID)
    {
        String username = StringUtils.EMPTY;
        if(userID!=null && userID>0)
        {
            try
            {
                User user = getUserManager().getUser(userID);
                if(user!=null)
                    username = user.getName();
            } catch (com.jivesoftware.base.UserNotFoundException e) {
                Log.error("User Not found for ID ==>> " + userID);
                e.printStackTrace();
            }
        }
        return username;
    }


    public static String getUserName(final String userID)
    {
        String username = StringUtils.EMPTY;

        if(userID!=null && !userID.equals("") && Integer.parseInt(userID)>0)
        {
            try
            {
                User user = getUserManager().getUser(Long.parseLong(userID));
                if(user!=null)
                    username = user.getName();
            } catch (com.jivesoftware.base.UserNotFoundException e) {
                Log.error("User Not found for ID ==>> " + userID);
                e.printStackTrace();
            }
        }
        return username;
    }

    public static String getCurrentTableName() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return "grailactivitylog_"+month+"_"+year;
    }

}
