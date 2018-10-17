package com.grail.synchro.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.caja.util.Sets;
import com.grail.beans.GrailBriefTemplate;
import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.beans.KantarReportBean;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectCostDetailsBean;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProjectEvaluationManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.manager.ProposalManager;
import com.grail.synchro.manager.ProposalManagerNew;
import com.grail.synchro.manager.ReportSummaryManager;
import com.jivesoftware.base.Group;
import com.jivesoftware.base.GroupManager;
import com.jivesoftware.base.GroupNotFoundException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.user.profile.ProfileFieldValue;
import com.jivesoftware.community.user.profile.ProfileManager;
import com.jivesoftware.community.util.BasePermHelper;
import com.jivesoftware.util.StringUtils;

/**
 * @author Samee K.S
 * @version 1.0, Date: 6/12/13
 */
public class SynchroPermHelper extends BasePermHelper {

    private static final Logger LOG = Logger.getLogger(SynchroPermHelper.class);
    private static GroupManager groupManager;
    private static SynchroUtils synchroUtils;
    private static ProjectManager synchroProjectManager;
    private static PIBManager pibManager;
    private static ProposalManager proposalManager;
    private static ProposalManagerNew proposalManagerNew;
    private static ProjectSpecsManager projectSpecsManager;
    private static ReportSummaryManager reportSummaryManager;
    private static ProjectEvaluationManager projectEvaluationManager;
    private static ProfileManager profileManager;
    private static UserManager userManager;
    
    private static PIBManagerNew pibManagerNew;
    private static ProjectManagerNew synchroProjectManagerNew;



    public static ProjectManager getSynchroProjectManager() {
        if(synchroProjectManager==null)
        {
            return JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return synchroProjectManager;

    }
    
    public static ProjectManagerNew getSynchroProjectManagerNew() {
        if(synchroProjectManagerNew==null)
        {
            return JiveApplication.getContext().getSpringBean("synchroProjectManagerNew");
        }
        return synchroProjectManagerNew;

    }
    public static PIBManager getPibManager() {
        if(pibManager==null)
        {
            return JiveApplication.getContext().getSpringBean("pibManager");
        }
        return pibManager;

    }
    public static PIBManagerNew getPibManagerNew() {
        if(pibManagerNew==null)
        {
            return JiveApplication.getContext().getSpringBean("pibManagerNew");
        }
        return pibManagerNew;

    }
    public static ProposalManager getProposalManager() {
        if(proposalManager==null)
        {
            return JiveApplication.getContext().getSpringBean("proposalManager");
        }
        return proposalManager;

    }
    public static ProposalManagerNew getProposalManagerNew() {
        if(proposalManagerNew==null)
        {
            return JiveApplication.getContext().getSpringBean("proposalManagerNew");
        }
        return proposalManagerNew;

    }
    public static ProjectSpecsManager getProjectSpecsManager() {
        if(projectSpecsManager==null)
        {
            return JiveApplication.getContext().getSpringBean("projectSpecsManager");
        }
        return projectSpecsManager;

    }
    public static ReportSummaryManager getReportSummaryManager() {
        if(reportSummaryManager==null)
        {
            return JiveApplication.getContext().getSpringBean("reportSummaryManager");
        }
        return reportSummaryManager;

    }
    public static ProjectEvaluationManager getProjectEvaluationManager() {
        if(projectEvaluationManager==null)
        {
            return JiveApplication.getContext().getSpringBean("projectEvaluationManager");
        }
        return projectEvaluationManager;

    }
    public static ProfileManager getProfileManager() {
        if(profileManager == null) {
            return JiveApplication.getContext().getSpringBean("profileManager");
        }
        return profileManager;

    }
    public static UserManager getUserManager() {
        if(userManager == null) {
            return JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;

    }
    public static boolean isOracleUser(final Long userID){
        boolean isOracleUser = false;
        try {
            final Group oracleGroup = getGroupManager().getGroup(SynchroConstants.JIVE_ORACLE_APPROVERS_GROUP_NAME);
            if(oracleGroup != null){
                isOracleUser =  oracleGroup.getMemberIds().contains(userID);
                // Check if the member is a Group Admin permission
                if(!isOracleUser){
                    final UserManager userManager = JiveApplication.getContext().getUserManager();
                    try {
                        isOracleUser = oracleGroup.isAdministrator(userManager.getUser(userID));
                    } catch (UserNotFoundException e) {
                        // do-nothing
                    }
                }
            }
        } catch (GroupNotFoundException e) {
            LOG.debug("## There is 'ORACLE' user group defined in system.", e);
        }
        LOG.debug("## Is logged in user member of Oracle Group ? " + isOracleUser);
        return isOracleUser;
    }

    public static boolean hasProjectAccess_OLD(final Long projectID){
        boolean isMember = false;
        // If anonymous then return false

        if(!getEffectiveUser().isAnonymous()){
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin())
            {
                return true;
            }
            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return false;
            }


            final String projectStakeholderGroupName = String.format(SynchroConstants.SYNCHRO_PROJECT_UG_NAME, projectID);
            final User targetUser = getEffectiveUser();
            try {
                final Group projectStakeHolders = getGroupManager().getGroup(projectStakeholderGroupName);
                if(projectStakeHolders != null){
                    isMember = projectStakeHolders.getMemberIds().contains(targetUser.getID());
                    // Check if the member is a Group Admin permission
                    if(!isMember){
                        isMember = projectStakeHolders.isAdministrator(targetUser);
                    }
                }
            } catch (GroupNotFoundException e) {
                // do-nothing
                LOG.debug("## There is NO group by name '" + projectStakeholderGroupName +"'associated with this project.", e);
            }
            LOG.debug("## User " + targetUser.getUsername() + " doesn't have access to this project.");
        }
        return isMember;
    }
    
     public static boolean hasProjectAccess(final Long projectID){


        if(!getEffectiveUser().isAnonymous()){
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isMethodologyApproverUser() || SynchroPermHelper.isSynchroCommunicationAgencyAdmin())
            {
                return true;
            }
            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return false;
            }
            // Project Owner , SPI Owner, Legal, Procurement users and External Agency users will have access to a project.
            Long projectOwner = project.getProjectOwner();
            if(projectOwner==getEffectiveUser().getID())
            {
                return true;
            }
            Long projectCreator = project.getBriefCreator();
            //Project creator can also access the project. https://svn.sourcen.com/issues/17556
            if(projectCreator==getEffectiveUser().getID())
            {
                return true;
            }
            PIBStakeholderList pibStakeholders = getPibManager().getPIBStakeholderList(projectID);
            if(pibStakeholders!=null && pibStakeholders.getGlobalLegalContact()!=null && pibStakeholders.getGlobalLegalContact().equals(getEffectiveUser().getID()))
            {
                return true;
            }
            if(pibStakeholders!=null && pibStakeholders.getGlobalProcurementContact()!=null && pibStakeholders.getGlobalProcurementContact().equals(getEffectiveUser().getID()))
            {
                return true;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact1()!=null && pibStakeholders.getAgencyContact1().equals(getEffectiveUser().getID()))
            {
                return true;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact1Optional()!=null && pibStakeholders.getAgencyContact1Optional()==getEffectiveUser().getID())
            {
                return true;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact2()!=null && pibStakeholders.getAgencyContact2().equals(getEffectiveUser().getID()))
            {
                return true;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact2Optional()!=null && pibStakeholders.getAgencyContact2Optional()==getEffectiveUser().getID())
            {
                return true;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact3()!=null && pibStakeholders.getAgencyContact3().equals(getEffectiveUser().getID()))
            {
                return true;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact3Optional()!=null && pibStakeholders.getAgencyContact3Optional()==getEffectiveUser().getID())
            {
                return true;
            }
            if(pibStakeholders!=null && pibStakeholders.getGlobalCommunicationAgency()!=null && pibStakeholders.getGlobalCommunicationAgency().equals(getEffectiveUser().getID()))
            {
                return true;
            }
            if(pibStakeholders!=null && pibStakeholders.getProductContact()!=null && pibStakeholders.getProductContact().equals(getEffectiveUser().getID()))
            {
                return true;
            }
            List<EndMarketInvestmentDetail> endMarketDetails = getSynchroProjectManager().getEndMarketDetails(projectID);
            if(endMarketDetails!=null && endMarketDetails.size()>0)
            {
                if(endMarketDetails.get(0).getSpiContact()!=null && endMarketDetails.get(0).getSpiContact().equals(getEffectiveUser().getID()))
                {
                    return true;
                }
                // Other SPI Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherSPIContact(projectID, endMarketDetails.get(0).getEndMarketID()))
                {
                    return true;
                }
                // Other Legal Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherLegalContact(projectID, endMarketDetails.get(0).getEndMarketID()))
                {
                    return true;
                }
                // Other Product Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherProductContact(projectID, endMarketDetails.get(0).getEndMarketID()))
                {
                    return true;
                }
                // Other Agency Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherAgencyContact(projectID, endMarketDetails.get(0).getEndMarketID()))
                {
                    return true;
                }
            }


        }
        return false;
    }
    public static boolean hasProjectAccessNew(final Long projectID){


        if(!getEffectiveUser().isAnonymous()){
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin())
            {
                return true;
            }
            if(SynchroGlobal.ProjectStatusNew.CANCEL.ordinal()==status)
            {
                return false;
            }
            // Project Owner , SPI Owner, Legal, Procurement users and External Agency users will have access to a project.
            Long projectOwner = project.getProjectOwner();
            if(projectOwner==getEffectiveUser().getID())
            {
                return true;
            }
            Long projectCreator = project.getBriefCreator();
            //Project creator can also access the project. https://svn.sourcen.com/issues/17556
            if(projectCreator==getEffectiveUser().getID())
            {
                return true;
            }
          


        }
        return false;
    }

    public static boolean hasReadOnlyProjectAccess(final Long projectID){
        if(!getEffectiveUser().isAnonymous()){
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isMethodologyApproverUser())
            {
                return false;
            }
            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return true;
            }
            // Project Owner , SPI Owner, Legal, Procurement users and External Agency users will have access to a project.
            Long projectOwner = project.getProjectOwner();
            if(projectOwner==getEffectiveUser().getID())
            {
                return false;
            }
            Long projectCreator = project.getBriefCreator();
            //Project creator can also access the project. https://svn.sourcen.com/issues/17556
            if(projectCreator==getEffectiveUser().getID())
            {
                return false;
            }
            PIBStakeholderList pibStakeholders = getPibManager().getPIBStakeholderList(projectID);
            if(pibStakeholders!=null && pibStakeholders.getGlobalLegalContact()!=null && pibStakeholders.getGlobalLegalContact().equals(getEffectiveUser().getID()))
            {
                return false;
            }
            if(pibStakeholders!=null && pibStakeholders.getGlobalProcurementContact()!=null && pibStakeholders.getGlobalProcurementContact().equals(getEffectiveUser().getID()))
            {
                return false;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact1()!=null && pibStakeholders.getAgencyContact1().equals(getEffectiveUser().getID()))
            {
                return false;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact1Optional()!=null && pibStakeholders.getAgencyContact1Optional()==getEffectiveUser().getID())
            {
                return false;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact2()!=null && pibStakeholders.getAgencyContact2().equals(getEffectiveUser().getID()))
            {
                return false;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact2Optional()!=null && pibStakeholders.getAgencyContact2Optional()==getEffectiveUser().getID())
            {
                return false;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact3()!=null && pibStakeholders.getAgencyContact3().equals(getEffectiveUser().getID()))
            {
                return false;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact3Optional()!=null && pibStakeholders.getAgencyContact3Optional()==getEffectiveUser().getID())
            {
                return false;
            }
            if(pibStakeholders!=null && pibStakeholders.getGlobalCommunicationAgency()!=null && pibStakeholders.getGlobalCommunicationAgency().equals(getEffectiveUser().getID()))
            {
                return false;
            }
            if(pibStakeholders!=null && pibStakeholders.getProductContact()!=null && pibStakeholders.getProductContact().equals(getEffectiveUser().getID()))
            {
                return true;
            }
            List<EndMarketInvestmentDetail> endMarketDetails = getSynchroProjectManager().getEndMarketDetails(projectID);
            if(endMarketDetails!=null && endMarketDetails.size()>0)
            {
                if(endMarketDetails.get(0).getSpiContact()!=null && endMarketDetails.get(0).getSpiContact().equals(getEffectiveUser().getID()))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Method for checking access for Multi Market projects
     * @param projectID
     * @return
     */
    public static boolean hasProjectAccessMultiMarket(final Long projectID){


        if(!getEffectiveUser().isAnonymous()){
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isMethodologyApproverUser() || SynchroPermHelper.isSynchroCommunicationAgencyAdmin())
            {
                return true;
            }
            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return false;
            }
            // Project Owner , SPI Owner, Legal, Procurement users and External Agency users will have access to a project.
            Long projectOwner = project.getProjectOwner();
            if(projectOwner==getEffectiveUser().getID())
            {
                return true;
            }
            Long projectCreator = project.getBriefCreator();
            //Project creator can also access the project. https://svn.sourcen.com/issues/17556
            if(projectCreator==getEffectiveUser().getID())
            {
                return true;
            }
            //TODO : Make changes for All the End Markets
            List<PIBStakeholderList> pibStakeholdersList= getPibManager().getPIBStakeholderListMultiMarket(projectID);
            if(pibStakeholdersList!=null && pibStakeholdersList.size()>0)
            {
                for(PIBStakeholderList pibStakeholders: pibStakeholdersList)
                {
                    if(pibStakeholders!=null && pibStakeholders.getGlobalLegalContact()!=null && pibStakeholders.getGlobalLegalContact().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }
                    if(pibStakeholders!=null && pibStakeholders.getGlobalProcurementContact()!=null && pibStakeholders.getGlobalProcurementContact().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }
                    if(pibStakeholders!=null && pibStakeholders.getAgencyContact1()!=null && pibStakeholders.getAgencyContact1().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }
                    if(pibStakeholders!=null && pibStakeholders.getAgencyContact2()!=null && pibStakeholders.getAgencyContact2().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }
                    if(pibStakeholders!=null && pibStakeholders.getAgencyContact3()!=null && pibStakeholders.getAgencyContact3().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }

                    if(pibStakeholders!=null && pibStakeholders.getAgencyContact1Optional()!=null && pibStakeholders.getAgencyContact1Optional().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }
                    if(pibStakeholders!=null && pibStakeholders.getAgencyContact2Optional()!=null && pibStakeholders.getAgencyContact2Optional().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }
                    if(pibStakeholders!=null && pibStakeholders.getAgencyContact3Optional()!=null && pibStakeholders.getAgencyContact3Optional().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }

                    if(pibStakeholders!=null && pibStakeholders.getGlobalCommunicationAgency()!=null && pibStakeholders.getGlobalCommunicationAgency().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }

                    if(pibStakeholders!=null && pibStakeholders.getProductContact()!=null && pibStakeholders.getProductContact().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }

                }
            }

            // https://www.svn.sourcen.com/issues/18899
            if(isAboveMarketProjectContact(projectID) || isRegionalProjectContact(projectID) || isAreaProjectContact(projectID) || isCountryProjectContact(projectID) || isCountrySPIContact(projectID))
            {
                return true;
            }

            // Other SPI Contacts will have read only access to the PIB stage
            if(SynchroPermHelper.isOtherSPIContact(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                return true;
            }
            // Other Legal Contacts will have read only access to the PIB stage
            if(SynchroPermHelper.isOtherLegalContact(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                return true;
            }
            // Other Product Contacts will have read only access to the PIB stage
            if(SynchroPermHelper.isOtherProductContact(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                return true;
            }
            // Other Agency Contacts will have read only access to the PIB stage
            if(SynchroPermHelper.isOtherAgencyContact(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                return true;
            }
            List<Long> endMarkets =  getSynchroProjectManager().getEndMarketIDs(projectID);
            for(Long emId:endMarkets)
            {
                // Other SPI Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherSPIContact(projectID, emId))
                {
                    return true;
                }
                // Other Legal Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherLegalContact(projectID, emId))
                {
                    return true;
                }
                // Other Product Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherProductContact(projectID, emId))
                {
                    return true;
                }
                // Other Agency Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherAgencyContact(projectID, emId))
                {
                    return true;
                }
            }

        }
        return false;
    }

    public static boolean hasReadOnlyProjectAccessMultiMarket(final Long projectID){

        if(!getEffectiveUser().isAnonymous())
        {
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isMethodologyApproverUser())
            {
                return false;
            }
            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return true;
            }
            // Project Owner , SPI Owner, Legal, Procurement users and External Agency users will have access to a project.
            Long projectOwner = project.getProjectOwner();
            if(projectOwner==getEffectiveUser().getID())
            {
                return false;
            }
            Long projectCreator = project.getBriefCreator();
            //Project creator can also access the project. https://svn.sourcen.com/issues/17556
            if(projectCreator==getEffectiveUser().getID())
            {
                return false;
            }

            List<PIBStakeholderList> pibStakeholdersList= getPibManager().getPIBStakeholderListMultiMarket(projectID);
            if(pibStakeholdersList!=null && pibStakeholdersList.size()>0)
            {
                for(PIBStakeholderList pibStakeholders: pibStakeholdersList)
                {
                    if(pibStakeholders!=null && pibStakeholders.getGlobalLegalContact()!=null && pibStakeholders.getGlobalLegalContact().equals(getEffectiveUser().getID()))
                    {
                        return false;
                    }
                    if(pibStakeholders!=null && pibStakeholders.getGlobalProcurementContact()!=null && pibStakeholders.getGlobalProcurementContact().equals(getEffectiveUser().getID()))
                    {
                        return false;
                    }
                    if(pibStakeholders!=null && pibStakeholders.getAgencyContact1()!=null && pibStakeholders.getAgencyContact1().equals(getEffectiveUser().getID()))
                    {
                        return false;
                    }
                    if(pibStakeholders!=null && pibStakeholders.getAgencyContact2()!=null && pibStakeholders.getAgencyContact2().equals(getEffectiveUser().getID()))
                    {
                        return false;
                    }
                    if(pibStakeholders!=null && pibStakeholders.getAgencyContact3()!=null && pibStakeholders.getAgencyContact3().equals(getEffectiveUser().getID()))
                    {
                        return false;
                    }

                    if(pibStakeholders!=null && pibStakeholders.getAgencyContact1Optional()!=null && pibStakeholders.getAgencyContact1Optional().equals(getEffectiveUser().getID()))
                    {
                        return false;
                    }
                    if(pibStakeholders!=null && pibStakeholders.getAgencyContact2Optional()!=null && pibStakeholders.getAgencyContact2Optional().equals(getEffectiveUser().getID()))
                    {
                        return false;
                    }
                    if(pibStakeholders!=null && pibStakeholders.getAgencyContact3Optional()!=null && pibStakeholders.getAgencyContact3Optional().equals(getEffectiveUser().getID()))
                    {
                        return false;
                    }

                    if(pibStakeholders!=null && pibStakeholders.getGlobalCommunicationAgency()!=null && pibStakeholders.getGlobalCommunicationAgency().equals(getEffectiveUser().getID()))
                    {
                        return false;
                    }
                }
                /*Iterate stakeholders list to check if it product contact in of the market, if yes then it is readonly */
                for(PIBStakeholderList pibStakeholders: pibStakeholdersList)
                {
                    if(pibStakeholders!=null && pibStakeholders.getProductContact()!=null && pibStakeholders.getProductContact().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }
                }
            }

            // https://www.svn.sourcen.com/issues/18899
            if(isAboveMarketProjectContact(projectID) || isRegionalProjectContact(projectID) || isAreaProjectContact(projectID) || isCountryProjectContact(projectID) || isCountrySPIContact(projectID))
            {
                return false;
            }

        }
        return true;
    }


    public static boolean hasPIBAccessNew(final Long projectID){


        if(!getEffectiveUser().isAnonymous()){
            Project project = getSynchroProjectManagerNew().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin())
            {
                return true;
            }
            //if(SynchroGlobal.ProjectStatusNew.CANCEL.ordinal()==status)
            if(project.getIsCancel())
            {
                return false;
            }
           
            Long projectCreator = project.getBriefCreator();
            if(projectCreator==getEffectiveUser().getID())
            {
                return true;
            }
            if(isBriefLegalUser(projectID))
            {
            	return true;
            }
            if(isSynchroSystemOwner())
            {
            	return true;
            }
            
           
           
        }
        return false;
    }
    
    public static boolean hasProposalAccessNew(final Long projectID){


        if(!getEffectiveUser().isAnonymous()){
            Project project = getSynchroProjectManagerNew().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin())
            {
                return true;
            }
           // if(SynchroGlobal.ProjectStatusNew.CANCEL.ordinal()==status)
            if(project.getIsCancel())
            {
                return false;
            }
           
            Long projectCreator = project.getBriefCreator();
            if(projectCreator==getEffectiveUser().getID())
            {
                return true;
            }
            if(isProposalLegalUser(projectID))
            {
            	return true;
            }
            if(isSynchroSystemOwner())
            {
            	return true;
            }
            
           
        }
        return false;
    }
    
    /**
     * This method is used for checking Accessing for New Synchro Projects for Project Specs, Report Summary, Project Evaluation and Project Close Stages
     * @param projectID
     * @return
     */
    public static boolean hasProjectAccessNew1(final Long projectID){


        if(!getEffectiveUser().isAnonymous()){
            Project project = getSynchroProjectManagerNew().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin() || isSynchroSystemOwner())
            {
                return true;
            }
           // if(SynchroGlobal.ProjectStatusNew.CANCEL.ordinal()==status)
            if(project.getIsCancel())
            {
                return false;
            }
           
            Long projectCreator = project.getBriefCreator();
            if(projectCreator==getEffectiveUser().getID())
            {
                return true;
            }
            
            if(isSynchroSystemOwner())
            {
            	return true;
            }
            
           
        }
        return false;
    }
    
  public static boolean userTypeAccess(final Long projectID)
  {
	  Project project = getSynchroProjectManagerNew().get(projectID);
	  if(isGlobalUserType())
      {
      	return true;
      }
      if(isRegionalUserType())
      {
      	boolean regionAccess = false;
      	
      	//http://redmine.nvish.com/redmine/issues/448
      	List<Long> regionBudgetLocations = getSynchroUtils().getBudgetLocationRegions(getEffectiveUser());
      	List<Long> userRegionsEM = SynchroUtils.getBudgetLocationRegionsEndMarkets(getEffectiveUser());
  		List<Long> userEM = SynchroUtils.getBudgetLocationEndMarkets(getEffectiveUser());
  		List<Long> allEM = new ArrayList<Long>();
  		
  		allEM.addAll(regionBudgetLocations);
  		allEM.addAll(userRegionsEM);
  		allEM.addAll(userEM);
  		
      	for(Long budgetLocation : allEM)
      	{
      		if(project.getBudgetLocation() == budgetLocation.intValue())
      		{
      			regionAccess=true;
      		}
      	}
      	if(regionAccess)
      	{
      		return true;
      	}
      }
      if(isEndMarketUserType())
      {
      	boolean endMarketAccess = false;
      //	List<Long> emBudgetLocations = getSynchroUtils().getBudgetLocationEndMarkets(getEffectiveUser());
      	
      //http://redmine.nvish.com/redmine/issues/448
      	List<Long> userRegionsEM = SynchroUtils.getBudgetLocationRegionsEndMarkets(getEffectiveUser());
  		List<Long> userEM = SynchroUtils.getBudgetLocationEndMarkets(getEffectiveUser());
  		List<Long> allEM = new ArrayList<Long>();
  		allEM.addAll(userRegionsEM);
  		allEM.addAll(userEM);
  		
      	for(Long budgetLocation : allEM)
      	{
      		if(project.getBudgetLocation() == budgetLocation.intValue())
      		{
      			endMarketAccess=true;
      		}
      	}
      	if(endMarketAccess)
      	{
      		return true;
      	}
      }
      return false;
  }

  
 
  
public static boolean hasPIBAccess(final Long projectID){


        if(!getEffectiveUser().isAnonymous()){
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isMethodologyApproverUser() || SynchroPermHelper.isSynchroCommunicationAgencyAdmin() || SynchroPermHelper.isKantarMethodologyApproverUser())
            {
                return true;
            }
            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return false;
            }
            // Project Owner , SPI Owner, Legal, Procurement users and External Agency users will have access to a project.
            Long projectOwner = project.getProjectOwner();
            if(projectOwner==getEffectiveUser().getID())
            {
                return true;
            }
            Long projectCreator = project.getBriefCreator();
            //Project creator can also access the project. https://svn.sourcen.com/issues/17556
            if(projectCreator==getEffectiveUser().getID())
            {
                return true;
            }
            List<EndMarketInvestmentDetail> endMarketDetails = getSynchroProjectManager().getEndMarketDetails(projectID);
            if(endMarketDetails!=null && endMarketDetails.size()>0)
            {
                if(endMarketDetails.get(0).getSpiContact()!=null && endMarketDetails.get(0).getSpiContact()==getEffectiveUser().getID())
                {
                    return true;
                }
                // Other SPI Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherSPIContact(projectID, endMarketDetails.get(0).getEndMarketID()))
                {
                    return true;
                }
                // Other Legal Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherLegalContact(projectID, endMarketDetails.get(0).getEndMarketID()))
                {
                    return true;
                }
                // Other Product Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherProductContact(projectID, endMarketDetails.get(0).getEndMarketID()))
                {
                    return true;
                }
                // Other Agency Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherAgencyContact(projectID, endMarketDetails.get(0).getEndMarketID()))
                {
                    return true;
                }
            }
           
            PIBStakeholderList pibStakeholders = getPibManager().getPIBStakeholderList(projectID);
            //Whatsoever is been selected in the Agency Contact option should be able to run all the stages.
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact1()!=null && pibStakeholders.getAgencyContact1()==getEffectiveUser().getID())
            {
                return true;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact1Optional()!=null && pibStakeholders.getAgencyContact1Optional()==getEffectiveUser().getID())
            {
                return true;
            }
            
            //Stakeholders like Agency, Legal and Procuremnt user can only access the PIB stage once it is completed
            // https://www.svn.sourcen.com/issues/17732
            
            List<ProjectInitiation> pi = getPibManager().getPIBDetails(projectID);
            if(pi!=null && pi.size()>0 && pi.get(0).getStatus()!=null && pi.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
            {
                
                if(pibStakeholders!=null && pibStakeholders.getGlobalLegalContact()!=null && pibStakeholders.getGlobalLegalContact()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getGlobalProcurementContact()!=null && pibStakeholders.getGlobalProcurementContact()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact1()!=null && pibStakeholders.getAgencyContact1()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact1Optional()!=null && pibStakeholders.getAgencyContact1Optional()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact2()!=null && pibStakeholders.getAgencyContact2()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact2Optional()!=null && pibStakeholders.getAgencyContact2Optional()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact3()!=null && pibStakeholders.getAgencyContact3()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact3Optional()!=null && pibStakeholders.getAgencyContact3Optional()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getGlobalCommunicationAgency()!=null && pibStakeholders.getGlobalCommunicationAgency()==getEffectiveUser().getID())
                {
                    return true;
                }

                if(pibStakeholders!=null && pibStakeholders.getProductContact()!=null && pibStakeholders.getProductContact()==getEffectiveUser().getID())
                {
                    return true;
                }

            }
            else
            {
                return false;
            }
        }
        return false;
    }


    public static boolean hasReadonlyAccessToPIB(final Long projectID){


        if(!getEffectiveUser().isAnonymous()){
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isMethodologyApproverUser())
            {
                return false;
            }
            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return true;
            }
            // Project Owner , SPI Owner, Legal, Procurement users and External Agency users will have access to a project.
            Long projectOwner = project.getProjectOwner();
            if(projectOwner==getEffectiveUser().getID())
            {
                return false;
            }
            Long projectCreator = project.getBriefCreator();
            //Project creator can also access the project. https://svn.sourcen.com/issues/17556
            if(projectCreator==getEffectiveUser().getID())
            {
                return false;
            }
            List<EndMarketInvestmentDetail> endMarketDetails = getSynchroProjectManager().getEndMarketDetails(projectID);
            if(endMarketDetails!=null && endMarketDetails.size()>0)
            {
                if(endMarketDetails.get(0).getSpiContact()!=null && endMarketDetails.get(0).getSpiContact()==getEffectiveUser().getID())
                {
                    return false;
                }
            }
            //Stakeholders like Agency, Legal and Procuremnt user can only access the PIB stage once it is completed
            // https://www.svn.sourcen.com/issues/17732
            List<ProjectInitiation> pi = getPibManager().getPIBDetails(projectID);
            if(pi!=null && pi.size()>0 && pi.get(0).getStatus()!=null && pi.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
            {
                PIBStakeholderList pibStakeholders = getPibManager().getPIBStakeholderList(projectID);
                if(pibStakeholders!=null && pibStakeholders.getGlobalLegalContact()!=null && pibStakeholders.getGlobalLegalContact()==getEffectiveUser().getID())
                {
                    return false;
                }
                if(pibStakeholders!=null && pibStakeholders.getGlobalProcurementContact()!=null && pibStakeholders.getGlobalProcurementContact()==getEffectiveUser().getID())
                {
                    return false;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact1()!=null && pibStakeholders.getAgencyContact1()==getEffectiveUser().getID())
                {
                    return false;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact1Optional()!=null && pibStakeholders.getAgencyContact1Optional()==getEffectiveUser().getID())
                {
                    return false;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact2()!=null && pibStakeholders.getAgencyContact2()==getEffectiveUser().getID())
                {
                    return false;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact2Optional()!=null && pibStakeholders.getAgencyContact2Optional()==getEffectiveUser().getID())
                {
                    return false;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact3()!=null && pibStakeholders.getAgencyContact3()==getEffectiveUser().getID())
                {
                    return false;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact3Optional()!=null && pibStakeholders.getAgencyContact3Optional()==getEffectiveUser().getID())
                {
                    return false;
                }
                if(pibStakeholders!=null && pibStakeholders.getGlobalCommunicationAgency()!=null && pibStakeholders.getGlobalCommunicationAgency()==getEffectiveUser().getID())
                {
                    return false;
                }

                if(pibStakeholders!=null && pibStakeholders.getProductContact()!=null && pibStakeholders.getProductContact()==getEffectiveUser().getID())
                {
                    return true;
                }

            }
            else
            {
                return true;
            }
        }
        return true;
    }


    public static boolean hasPIBAccessMultiMarket(final Long projectID){


        if(!getEffectiveUser().isAnonymous()){
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isMethodologyApproverUser() || SynchroPermHelper.isSynchroCommunicationAgencyAdmin() || SynchroPermHelper.isKantarMethodologyApproverUser() )
            {
                return true;
            }
            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return false;
            }
            // Project Owner , SPI Owner, Legal, Procurement users and External Agency users will have access to a project.
            Long projectOwner = project.getProjectOwner();
            if(projectOwner==getEffectiveUser().getID())
            {
                return true;
            }
            Long projectCreator = project.getBriefCreator();
            //Project creator can also access the project. https://svn.sourcen.com/issues/17556
            if(projectCreator==getEffectiveUser().getID())
            {
                return true;
            }
            /*TODO Added by Kanwar to fix https://www.svn.sourcen.com/issues/18562 */
            //https://www.svn.sourcen.com/issues/18899
            if(isAboveMarketProjectContact(projectID) || isRegionalProjectContact(projectID) || isAreaProjectContact(projectID) || isCountryProjectContact(projectID) || isCountrySPIContact(projectID))
            {
                return true;
            }

            // Other SPI Contacts will have read only access to the PIB stage
            if(SynchroPermHelper.isOtherSPIContact(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                return true;
            }
            // Other Legal Contacts will have read only access to the PIB stage
            if(SynchroPermHelper.isOtherLegalContact(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                return true;
            }
            // Other Product Contacts will have read only access to the PIB stage
            if(SynchroPermHelper.isOtherProductContact(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                return true;
            }
            // Other Agency Contacts will have read only access to the PIB stage
            if(SynchroPermHelper.isOtherAgencyContact(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID))
            {
                return true;
            }
            List<Long> endMarkets =  getSynchroProjectManager().getEndMarketIDs(projectID);
            for(Long emId:endMarkets)
            {
                // Other SPI Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherSPIContact(projectID, emId))
                {
                    return true;
                }
                // Other Legal Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherLegalContact(projectID, emId))
                {
                    return true;
                }
                // Other Product Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherProductContact(projectID, emId))
                {
                    return true;
                }
                // Other Agency Contacts will have read only access to the PIB stage
                if(SynchroPermHelper.isOtherAgencyContact(projectID, emId))
                {
                    return true;
                }
            }
            //Stakeholders like Agency, Legal and Procuremnt user can only access the PIB stage once it is completed
            // https://www.svn.sourcen.com/issues/17732
            List<ProjectInitiation> pi = getPibManager().getPIBDetails(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
            PIBStakeholderList pibStakeholders = getPibManager().getPIBStakeholderList(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
            
            //Whatsoever is been selected in the Agency Contact option should be able to run all the stages.
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact1()!=null && pibStakeholders.getAgencyContact1()==getEffectiveUser().getID())
            {
                return true;
            }
            if(pibStakeholders!=null && pibStakeholders.getAgencyContact1Optional()!=null && pibStakeholders.getAgencyContact1Optional()==getEffectiveUser().getID())
            {
                return true;
            }
            
            if(pi!=null && pi.size()>0 && pi.get(0).getStatus()!=null && pi.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
            {
                //TODO : Make changes for All the End Markets
                //PIBStakeholderList pibStakeholders = getPibManager().getPIBStakeholderList(projectID);
               
                if(pibStakeholders!=null && pibStakeholders.getGlobalLegalContact()!=null && pibStakeholders.getGlobalLegalContact()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getGlobalProcurementContact()!=null && pibStakeholders.getGlobalProcurementContact()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact1()!=null && pibStakeholders.getAgencyContact1()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact2()!=null && pibStakeholders.getAgencyContact2()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact3()!=null && pibStakeholders.getAgencyContact3()==getEffectiveUser().getID())
                {
                    return true;
                }

                if(pibStakeholders!=null && pibStakeholders.getAgencyContact1Optional()!=null && pibStakeholders.getAgencyContact1Optional()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact2Optional()!=null && pibStakeholders.getAgencyContact2Optional()==getEffectiveUser().getID())
                {
                    return true;
                }
                if(pibStakeholders!=null && pibStakeholders.getAgencyContact3Optional()!=null && pibStakeholders.getAgencyContact3Optional()==getEffectiveUser().getID())
                {
                    return true;
                }

                if(pibStakeholders!=null && pibStakeholders.getGlobalCommunicationAgency()!=null && pibStakeholders.getGlobalCommunicationAgency()==getEffectiveUser().getID())
                {
                    return true;
                }


                List<PIBStakeholderList> allPIBStakeholders = getPibManager().getPIBStakeholderListMultiMarket(projectID);

                for(PIBStakeholderList PIBStakeholder : allPIBStakeholders)
                {
                    if(PIBStakeholder!=null && PIBStakeholder.getProductContact()!=null && PIBStakeholder.getProductContact().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }
                }
            }
            else
            {
                return false;
            }
        }
        return false;
    }

    /**
     * To check if contact is product contact and not any other higher user
     * returns true of it is only project contact but not any other higher access to this project
     * @param projectID
     * @return
     */
    public static boolean hasReadonlyAccessToPIBMultiMarket(final Long projectID){
        if(!getEffectiveUser().isAnonymous()){
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin() || SynchroPermHelper.isMethodologyApproverUser())
            {
                return false;
            }
            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return true;
            }
            // Project Owner , SPI Owner, Legal, Procurement users and External Agency users will have access to a project.
            Long projectOwner = project.getProjectOwner();
            if(projectOwner==getEffectiveUser().getID())
            {
                return false;
            }
            Long projectCreator = project.getBriefCreator();
            //Project creator can also access the project. https://svn.sourcen.com/issues/17556
            if(projectCreator==getEffectiveUser().getID())
            {
                return false;
            }

            if(isAboveMarketProjectContact(projectID) || isRegionalProjectContact(projectID) || isAreaProjectContact(projectID) || isCountryProjectContact(projectID) || isCountrySPIContact(projectID))
            {
                return false;
            }

            List<ProjectInitiation> pi = getPibManager().getPIBDetails(projectID,SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
            if(pi!=null && pi.size()>0 && pi.get(0).getStatus()!=null && pi.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal())
            {
                List<PIBStakeholderList> pibStakeholders = getPibManager().getPIBStakeholderListMultiMarket(projectID);
                for(PIBStakeholderList pibStakeholder : pibStakeholders)
                {
                    if(pibStakeholder!=null && pibStakeholder.getProductContact()!=null && pibStakeholder.getProductContact().equals(getEffectiveUser().getID()))
                    {
                        return true;
                    }
                }

            }
            else
            {
                return true;
            }
        }
        return true;
    }

    public static boolean isGlobalMember(){
        final User user = getEffectiveUser();
        for(final Group userGroup: getGroupManager().getUserGroups(user)){
            /* if(StringUtils.startsWith(userGroup.getName(), "Global ")){
                return true;
            }*/
            //TODO GLOBAL Prefix is not driven from, System Property
            if(StringUtils.startsWith(userGroup.getName(), "GLOBAL_")){
                return true;
            }
        }
        return false;
    }
    public static boolean isRegionalMember(){
        final User user = getEffectiveUser();
        for(final Group userGroup: getGroupManager().getUserGroups(user)){
            /*if(StringUtils.startsWith(userGroup.getName(), "Regional ")){
                return true;
            }*/
            //TODO REGIONAL Prefix is not driven from, System Property
            if(StringUtils.startsWith(userGroup.getName(), "REGIONAL_")){
                return true;
            }
        }
        return false;
    }
    public static boolean isAreaMember(){
        final User user = getEffectiveUser();
        for(final Group userGroup: getGroupManager().getUserGroups(user)){
            /*if(StringUtils.startsWith(userGroup.getName(), "End-Market ")){
                return true;
            }*/
            //TODO END_MARKET Prefix is not driven from, System Property
            if(StringUtils.startsWith(userGroup.getName(), "END_MARKET_")){
                return true;
            }
        }
        return false;
    }

    public static Long getGroupIDByGroupPostFix(final String groupPostFix, final User user){
        for(final Group userGroup: getGroupManager().getUserGroups(user)){

            if(StringUtils.endsWith(userGroup.getName(), groupPostFix)){
                return userGroup.getID();
            }
        }
        return new Long(-1);
    }


    static GroupManager getGroupManager() {
        if (groupManager != null) {
            return groupManager;
        }
        groupManager = JiveApplication.getContext().getGroupManager();
        return groupManager;
    }
    /**
     * This method will return whether a stage document can be edited or not.    
     * @param document
     * @return
     */
    public static boolean canEditStageDocument(Document document, long projectID) {
        /*   Project project = getSynchroProjectManager().get(projectID);
        // https://www.svn.sourcen.com/issues/16876 - Cancelled or Completed Project stages should not be Editable
        // https://www.svn.sourcen.com/issues/16876 - On Holde projects should also not be editable
        Integer status = project.getStatus().intValue();
        if(status==SynchroGlobal.Status.COMPLETED.ordinal() || status==SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()
                || status==SynchroGlobal.Status.INPROGRESS_ONHOLD.ordinal() || status==SynchroGlobal.Status.PIB_OPEN.ordinal()
                || status==SynchroGlobal.Status.PIB_ONHOLD.ordinal())
        {
            return false;
        }
        if (document.getProperties().containsKey(SynchroConstants.STAGE_STATUS)
                && document.getProperties().get(SynchroConstants.STAGE_STATUS)
                .equals(SynchroGlobal.StageStatus.STARTED.name())) {
            // Communication Agency user should not be able to edit the Stage Documents
            if (isCommunicationAgencyUser(projectID)) {
                return false;
            } else {
                return true;
            }
        }*/
        return false;
    }

    /**
     * This method will return whether a stage document can be viewed or not.
     * Only stage which is in STARTED state or has been completed can be accessed    
     * @param projectId
     * @param stageId
     * @return
     */
    public static boolean canViewStageNew(Long projectId, Integer stageId) {
        /*if (document.getProperties().containsKey(SynchroConstants.STAGE_STATUS)
                && (document.getProperties().get(SynchroConstants.STAGE_STATUS)
                .equals(SynchroGlobal.StageStatus.STARTED.name())||document.getProperties().get(SynchroConstants.STAGE_STATUS)
                .equals(SynchroGlobal.StageStatus.COMPLETED.name()))) {
            return true;
        }*/

        if(stageId==2)
        {
            List<ProposalInitiation> proposalInitiation = getProposalManager().getProposalDetails(projectId);
            if(proposalInitiation!=null && proposalInitiation.size()>0 && proposalInitiation.get(0).getStatus()>=SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal())
            {
                return true;
            }
        }
        if(stageId==3)
        {
            List<ProjectSpecsInitiation> projSpecsInitiation = getProjectSpecsManager().getProjectSpecsInitiation(projectId);
            if(projSpecsInitiation!=null && projSpecsInitiation.size()>0 && projSpecsInitiation.get(0).getStatus()>=SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal())
            {
                //https://www.svn.sourcen.com/issues/17741
                if(isExternalAgencyUser(projSpecsInitiation.get(0).getProjectID(),projSpecsInitiation.get(0).getEndMarketID()))
                {
                    if(isAwardedExternalAgencyUser(projSpecsInitiation.get(0).getProjectID(),projSpecsInitiation.get(0).getEndMarketID()))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    return true;
                }

            }
        }
        if(stageId==4)
        {
            List<ReportSummaryInitiation> repSummaryInitiation = getReportSummaryManager().getReportSummaryInitiation(projectId);
            if(repSummaryInitiation!=null && repSummaryInitiation.size()>0 && repSummaryInitiation.get(0).getStatus()>=SynchroGlobal.StageStatus.REPORT_SUMMARY_STARTED.ordinal())
            {
                //return true;
                //https://www.svn.sourcen.com/issues/17741
                if(isExternalAgencyUser(repSummaryInitiation.get(0).getProjectID(),repSummaryInitiation.get(0).getEndMarketID()))
                {
                    if(isAwardedExternalAgencyUser(repSummaryInitiation.get(0).getProjectID(),repSummaryInitiation.get(0).getEndMarketID()))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    return true;
                }
            }
        }
        if(stageId==5)
        {
            // Agency evaluation stage will not be accessible to communication agency roles
            if(isSynchroCommunicationAgencyAdmin())
            {
                return false;
            }
            List<EndMarketInvestmentDetail> endMarketInvestmentDetails = getSynchroProjectManager().getEndMarketDetails(projectId);
            if(endMarketInvestmentDetails != null && endMarketInvestmentDetails.size() > 0) {

                Project project = getSynchroProjectManager().get(projectId);
                Long endMarketId = null;
               /* if(project.getMultiMarket())
                {
                    endMarketId = SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID;
                }
                else
                {
                    endMarketId = getSynchroProjectManager().getEndMarketDetails(projectId).get(0).getEndMarketID();
                }*/
                endMarketId = getSynchroProjectManager().getEndMarketDetails(projectId).get(0).getEndMarketID();
                //  Long endMarketId = getSynchroProjectManager().getEndMarketDetails(projectId).get(0).getEndMarketID();
                if(isExternalAgencyUser(projectId,endMarketId))
                {
                    if(isAwardedExternalAgencyUser(projectId,endMarketId))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    return true;
                }
            } else {
                return false;
            }

        }

        return false;
    }
    
    /**
     * This method will return whether a stage document can be viewed or not.
     * Only stage which is in STARTED state or has been completed can be accessed    
     * @param projectId
     * @param stageId
     * @return
     */
    public static boolean canViewStage(Long projectId, Integer stageId) {
        /*if (document.getProperties().containsKey(SynchroConstants.STAGE_STATUS)
                && (document.getProperties().get(SynchroConstants.STAGE_STATUS)
                .equals(SynchroGlobal.StageStatus.STARTED.name())||document.getProperties().get(SynchroConstants.STAGE_STATUS)
                .equals(SynchroGlobal.StageStatus.COMPLETED.name()))) {
            return true;
        }*/

        if(stageId==2)
        {
            List<ProposalInitiation> proposalInitiation = getProposalManager().getProposalDetails(projectId);
            if(proposalInitiation!=null && proposalInitiation.size()>0 && proposalInitiation.get(0).getStatus()>=SynchroGlobal.StageStatus.PROPOSAL_STARTED.ordinal())
            {
                return true;
            }
        }
        if(stageId==3)
        {
            List<ProjectSpecsInitiation> projSpecsInitiation = getProjectSpecsManager().getProjectSpecsInitiation(projectId);
            if(projSpecsInitiation!=null && projSpecsInitiation.size()>0 && projSpecsInitiation.get(0).getStatus()>=SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal())
            {
                //https://www.svn.sourcen.com/issues/17741
                if(isExternalAgencyUser(projSpecsInitiation.get(0).getProjectID(),projSpecsInitiation.get(0).getEndMarketID()))
                {
                    if(isAwardedExternalAgencyUser(projSpecsInitiation.get(0).getProjectID(),projSpecsInitiation.get(0).getEndMarketID()))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    return true;
                }

            }
        }
        if(stageId==4)
        {
            List<ReportSummaryInitiation> repSummaryInitiation = getReportSummaryManager().getReportSummaryInitiation(projectId);
            if(repSummaryInitiation!=null && repSummaryInitiation.size()>0 && repSummaryInitiation.get(0).getStatus()>=SynchroGlobal.StageStatus.REPORT_SUMMARY_STARTED.ordinal())
            {
                //return true;
                //https://www.svn.sourcen.com/issues/17741
                if(isExternalAgencyUser(repSummaryInitiation.get(0).getProjectID(),repSummaryInitiation.get(0).getEndMarketID()))
                {
                    if(isAwardedExternalAgencyUser(repSummaryInitiation.get(0).getProjectID(),repSummaryInitiation.get(0).getEndMarketID()))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    return true;
                }
            }
        }
        if(stageId==5)
        {
            // Agency evaluation stage will not be accessible to communication agency roles
            if(isSynchroCommunicationAgencyAdmin())
            {
                return false;
            }
            List<EndMarketInvestmentDetail> endMarketInvestmentDetails = getSynchroProjectManager().getEndMarketDetails(projectId);
            if(endMarketInvestmentDetails != null && endMarketInvestmentDetails.size() > 0) {

                Project project = getSynchroProjectManager().get(projectId);
                Long endMarketId = null;
                if(project.getMultiMarket())
                {
                    endMarketId = SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID;
                }
                else
                {
                    endMarketId = getSynchroProjectManager().getEndMarketDetails(projectId).get(0).getEndMarketID();
                }
                //  Long endMarketId = getSynchroProjectManager().getEndMarketDetails(projectId).get(0).getEndMarketID();
                if(isExternalAgencyUser(projectId,endMarketId))
                {
                    if(isAwardedExternalAgencyUser(projectId,endMarketId))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    return true;
                }
            } else {
                return false;
            }

        }

        return false;
    }

    public static Boolean canEditproject(User user, long projectID)
    {
        long ownerID = -1L;
        if(BasePermHelper.isSystemAdmin(user))
            return true;
        if(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin())
            return true;
        ProjectManager synchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
        Project project = synchroProjectManager.get(projectID);
        Integer status = project.getStatus().intValue();
        if(status==SynchroGlobal.Status.DELETED.ordinal())
        {
            return false;
        }
        ownerID = project.getProjectOwner();
        if(ownerID==user.getID())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static Boolean canEditMultimarketProject(User user, long projectID)
    {
        long ownerID = -1L;
        if(BasePermHelper.isSystemAdmin(user))
            return true;

        if(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin())
            return true;

        ProjectManager synchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
        Project project = synchroProjectManager.get(projectID);
        Integer status = project.getStatus().intValue();
        if(status==SynchroGlobal.Status.DELETED.ordinal())
        {
            return false;
        }

        long projectOwner = -1;
        long globalProjectContact =-1;
        long regionalProjectContact=-1;
        long areaProjectContact=-1;

        List<FundingInvestment> fundingInvestments = synchroProjectManager.getProjectInvestments(projectID);
        for(FundingInvestment fundingInvestment : fundingInvestments)
        {
            if(fundingInvestment.getInvestmentType()==SynchroGlobal.InvestmentType.GlOBAL.getId())
                globalProjectContact = fundingInvestment.getProjectContact();
            else if(fundingInvestment.getInvestmentType()==SynchroGlobal.InvestmentType.REGION.getId())
                regionalProjectContact = fundingInvestment.getProjectContact();
            else if(fundingInvestment.getInvestmentType()==SynchroGlobal.InvestmentType.AREA.getId())
                areaProjectContact = fundingInvestment.getProjectContact();
        }

        if(globalProjectContact>0)
        {
            projectOwner = globalProjectContact;
        }
        else if(regionalProjectContact>0)
        {
            projectOwner = regionalProjectContact;
        }
        else if(areaProjectContact>0)
        {
            projectOwner = areaProjectContact;
        }
        else
        {
            projectOwner = project.getProjectOwner();
        }

        if(projectOwner==user.getID())
        {
            return true;
        }
        else
        {
            return false;
        }
        /* 
        ownerID = project.getProjectOwner();
        if(ownerID==user.getID())
        {
            return true;
        }
        else
        {
            return false;
        }*/
    }

    public static Boolean canChangeProjectStatusDashboard(User user, long projectID)
    {
        long ownerID = -1L;
        if(BasePermHelper.isSystemAdmin(user))
            return true;
        if(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin())
            return true;
        ProjectManager synchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
        Project project = synchroProjectManager.get(projectID);
        Integer status = project.getStatus().intValue();
        if(status==SynchroGlobal.Status.DELETED.ordinal())
        {
            return false;
        }
        /*   if(SynchroGlobal.Status.CONCEPT_CANCEL.ordinal()==status || SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()==status || SynchroGlobal.Status.PLANNED_CANCEL.ordinal()==status || SynchroGlobal.Status.COMPLETED.ordinal()==status || SynchroGlobal.Status.COMPLETED_PROJECT_EVALUATION.ordinal()==status)
        {
            return false;
        }*/
        ownerID = project.getProjectOwner();
        if(ownerID==user.getID())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static Boolean isSynchroAdmin(User user)
    {
        if(BasePermHelper.isSystemAdmin(user))
            return true;
        else
            return false;
    }
    /**
     * This method will check whether the user is Mini Admin user or not
     * @return
     */

    public static Boolean isSynchroMiniAdmin()
    {
        User user = getEffectiveUser();
        boolean isMiniAdmin = false;
        try {
            final Group miniAdminGroup = getGroupManager().getGroup(SynchroConstants.JIVE_SYNCHRO_MINI_ADMIN_GROUP_NAME);
            if(miniAdminGroup != null){
                isMiniAdmin =  miniAdminGroup.getMemberIds().contains(user.getID());
                // Check if the member is a Group Admin permission
                if(!isMiniAdmin){
                    final UserManager userManager = JiveApplication.getContext().getUserManager();
                    try {
                        isMiniAdmin = miniAdminGroup.isAdministrator(userManager.getUser(user.getID()));
                    } catch (UserNotFoundException e) {
                        // do-nothing
                    }
                }
            }
        } catch (GroupNotFoundException e) {
            LOG.debug("## There is 'SYNCHRO MINI ADMIN' user group defined in system.", e);
        }
        LOG.debug("## Is logged in user member of Oracle Group ? " + isMiniAdmin);
        return isMiniAdmin;
    }


    public static Boolean isSynchroMiniAdmin(final User user)
    {
        boolean isMiniAdmin = false;
        try {
            final Group miniAdminGroup = getGroupManager().getGroup(SynchroConstants.JIVE_SYNCHRO_MINI_ADMIN_GROUP_NAME);
            if(miniAdminGroup != null){
                isMiniAdmin =  miniAdminGroup.getMemberIds().contains(user.getID());
                // Check if the member is a Group Admin permission
                if(!isMiniAdmin){
                    final UserManager userManager = JiveApplication.getContext().getUserManager();
                    try {
                        isMiniAdmin = miniAdminGroup.isAdministrator(userManager.getUser(user.getID()));
                    } catch (UserNotFoundException e) {
                        // do-nothing
                    }
                }
            }
        } catch (GroupNotFoundException e) {
            LOG.debug("## There is 'SYNCHRO MINI ADMIN' user group defined in system.", e);
        }
        LOG.debug("## Is logged in user member of Oracle Group ? " + isMiniAdmin);
        return isMiniAdmin;
    }

    /**
     * This method will check whether the user is Communication Agency Admin user or not
     * @return
     */

    public static Boolean isSynchroCommunicationAgencyAdmin()
    {
        User user = getEffectiveUser();
        boolean isCommunicationAgencyAdmin = false;
        try {
            final Group communicationAgencyAdminGroup = getGroupManager().getGroup(SynchroConstants.JIVE_COMMUNICATION_AGENCY_ADMIN_GROUP_NAME);
            if(communicationAgencyAdminGroup != null){
                isCommunicationAgencyAdmin =  communicationAgencyAdminGroup.getMemberIds().contains(user.getID());
                // Check if the member is a Group Admin permission
                if(!isCommunicationAgencyAdmin){
                    final UserManager userManager = JiveApplication.getContext().getUserManager();
                    try {
                        isCommunicationAgencyAdmin = communicationAgencyAdminGroup.isAdministrator(userManager.getUser(user.getID()));
                    } catch (UserNotFoundException e) {
                        // do-nothing
                    }
                }
            }
        } catch (GroupNotFoundException e) {
            LOG.debug("## There is 'COMMUNICATION AGENCY ADMIN' user group defined in system.", e);
        }
        LOG.debug("## Is logged in user member of Oracle Group ? " + isCommunicationAgencyAdmin);
        return isCommunicationAgencyAdmin;
    }
    /**
     * This method will check whether logged in user is External Agency user or not.
     * @param projectId
     * @return
     */
    public static boolean isExternalAgencyUser(Long projectId, Long endMarketId){
        User user = getEffectiveUser();
        /*Set<User> coAgencySupportUsers = Sets.newHashSet(getSynchroUtils().getCoAgencySupportUsers(projectId));
        if(coAgencySupportUsers.contains(user))
        {
            return true;
        }
        Set<User> coAgencyUsers = Sets.newHashSet(getSynchroUtils().getCoAgencyUsers(projectId));
        if(coAgencyUsers.contains(user))
        {
            return true;
        }
        Set<User> fieldAgencyUsers = Sets.newHashSet(getSynchroUtils().getFieldWorkAgencyUsers(projectId));
        if(fieldAgencyUsers.contains(user))
        {
            return true;
        }*/
        Set<User> externalAgencyUsers = Sets.newHashSet(getSynchroUtils().getExternalAgencyUsers(projectId, endMarketId));
        if(externalAgencyUsers.contains(user))
        {
            return true;
        }
        if(isExternalAgencyUser(user)) {
            return true;
        }
        return false;

    }
    /**
     * This method will check whether logged in user is Awarded External Agency user or not.
     * @param projectId
     * @return
     */
    public static boolean isAwardedExternalAgencyUser(Long projectId, Long endMarketId){
        User user = getEffectiveUser();

        Set<User> externalAgencyUsers = Sets.newHashSet(getSynchroUtils().getAwardedExternalAgencyUsers(projectId, endMarketId));
        if(externalAgencyUsers.contains(user))
        {
            return true;
        }
        return false;

    }
    /**
     * This method will check whether logged in user is Project Owner or not.
     * @param projectId
     * @return
     */
    public static boolean isProjectOwner(Long projectId){
        User user = getEffectiveUser();
        Long projectOwner = getSynchroProjectManager().get(projectId).getProjectOwner();
        if(projectOwner!=null && user.getID()==projectOwner)
        {
            return true;
        }
        return false;

    }

    public static boolean canAccessActivityLogs(Long projectId){
        User user = getEffectiveUser();

        if(SynchroPermHelper.isSynchroAdmin(user))
            return true;
        
        if(SynchroPermHelper.isSynchroSystemOwner())
        {
        	return true;
        }
        if(projectId==null || projectId<=0)
            return false;

        Project project = getSynchroProjectManager().get(projectId);

        if(project == null)
            return false;

        //Check if user is Project Owner
        Long projectOwner = project.getProjectOwner();
        if(projectOwner!=null && user.getID()==projectOwner)
        {
            return true;
        }


        //Check if user is SPI Contact
        if(project!=null && !project.getMultiMarket())
        {
            //Single Market Project
            List<EndMarketInvestmentDetail> endMarketInvstDetails = getSynchroProjectManager().getEndMarketDetails(projectId);
            if(endMarketInvstDetails!=null && endMarketInvstDetails.size() > 0)
            {
                Long endmarketID = endMarketInvstDetails.get(0).getEndMarketID();
                if(endmarketID!=null && endmarketID > 0)
                {
                    if(isSPIContactUser(projectId, endmarketID))
                        return true;
                }
            }
        }
        else
        {
            //Multimarket Project
            if(isAboveMarketProjectContact(projectId))
                return true;
        }

        return false;

    }

    /**
     * This method will check whether logged in user is Project Creator/Author or not.
     * @param projectId
     * @return
     */
    public static boolean isProjectCreator(Long projectId){
        User user = getEffectiveUser();
        Long projectCreator = getSynchroProjectManager().get(projectId).getBriefCreator();
        if(projectCreator!=null && user.getID()==projectCreator)
        {
            return true;
        }
        return false;

    }
    
    /**
     * This method will check whether logged in user is Project Creator/Author or not.
     * @param projectId
     * @return
     */
    public static boolean isProjectCreatorNew(Long projectId){
        User user = getEffectiveUser();
        Long projectCreator = getSynchroProjectManagerNew().get(projectId).getBriefCreator();
        if(projectCreator!=null && user.getID()==projectCreator)
        {
            return true;
        }
        return false;

    }
    
    /**
     * This method will check whether logged in user is SPI Contact User or not.
     * @param projectId
     * @return
     */
    public static boolean isSPIContactUser(Long projectId,Long endMarketId){
        User user = getEffectiveUser();
        Long spiContactUser = getSynchroProjectManager().getEndMarketDetail(projectId, endMarketId).getSpiContact();
        if(spiContactUser!=null && user.getID()==spiContactUser)
        {
            return true;
        }
        return false;

    }

    /**
     * This method will check whether logged in user is Global Project Contact User or not.
     * @param projectId
     * @return
     */
    public static boolean isGlobalProjectContactUser(Long projectId){
        User user = getEffectiveUser();
        List<FundingInvestment> fundingInvestmentList = getSynchroProjectManager().getProjectInvestments(projectId);
        if(fundingInvestmentList!=null && fundingInvestmentList.size()>0)
        {
            for(FundingInvestment fi : fundingInvestmentList)
            {
                if(fi.getInvestmentType().intValue()==SynchroGlobal.InvestmentType.GlOBAL.getId())
                {
                    if(fi.getProjectContact()==user.getID())
                    {
                        return true;
                    }
                }
            }
        }return false;

    }
    /**
     * This method will check whether logged in user is Legal Contact User or not.
     * @param projectId
     * @return
     */
    public static boolean isLegalUser(Long projectId,Long endMarketId){
        User user = getEffectiveUser();
        PIBStakeholderList pibStakeholders = getPibManager().getPIBStakeholderList(projectId);
        if(pibStakeholders!=null && pibStakeholders.getGlobalLegalContact()!=null && pibStakeholders.getGlobalLegalContact()==user.getID())
        {
            return true;
        }

        return false;

    }
    /**
     * This method will check whether logged in user is Procurement User User or not.
     * @param projectId
     * @return
     */
    public static boolean isProcurementUser(Long projectId,Long endMarketId){
        User user = getEffectiveUser();
        PIBStakeholderList pibStakeholders = getPibManager().getPIBStakeholderList(projectId);
        if(pibStakeholders!=null && pibStakeholders.getGlobalProcurementContact()!=null && pibStakeholders.getGlobalProcurementContact()==user.getID())
        {
            return true;
        }

        return false;

    }
    /**
     * This method will check whether logged in user is Product User or not.
     * @param projectId
     * @return
     */
    public static boolean isProductUser(Long projectId,Long endMarketId){
        User user = getEffectiveUser();
        PIBStakeholderList pibStakeholders = getPibManager().getPIBStakeholderList(projectId);
        if(pibStakeholders!=null && pibStakeholders.getProductContact()!=null && pibStakeholders.getProductContact()==user.getID())
        {
            return true;
        }

        return false;

    }

    /**
     * This method will check whether logged in user is Communication Agency user or not.
     * @return true/false
     */
    public static boolean isExternalAgencyUser(){
        User user = getEffectiveUser();
        Set<User> coAgencySupportUsers = Sets.newHashSet(getSynchroUtils().getCoordinationAgencySupportGroupUsers());
        if(coAgencySupportUsers.contains(user))
        {
            return true;
        }
        Set<User> coAgencyUsers = Sets.newHashSet(getSynchroUtils().getCoordinationGroupAgencyUsers());
        if(coAgencyUsers.contains(user))
        {
            return true;
        }
        Set<User> fieldAgencyUsers = Sets.newHashSet(getSynchroUtils().getFieldWorkAgencyGroupUsers());
        if(fieldAgencyUsers.contains(user))
        {
            return true;
        }
        return false;
    }

    /**
     * This method will check whether logged in user is Legal User or not.
     * @return true/false
     */
    public static boolean isLegalUser(){
        User user = getEffectiveUser();
        Set<User> legalUsers = Sets.newHashSet(getSynchroUtils().getLegalGroupApprovers());
        if(legalUsers.contains(user))
        {
            return true;
        }

        return false;
    }

    /**
     * This method will check whether logged in user is Communication Agency user or not.
     * @return trye/false
     */
    public static boolean isExternalAgencyUser(final User user){
        Set<User> coAgencySupportUsers = Sets.newHashSet(getSynchroUtils().getCoordinationAgencySupportGroupUsers());
        if(coAgencySupportUsers.contains(user))
        {
            return true;
        }
        Set<User> coAgencyUsers = Sets.newHashSet(getSynchroUtils().getCoordinationGroupAgencyUsers());
        if(coAgencyUsers.contains(user))
        {
            return true;
        }
        Set<User> fieldAgencyUsers = Sets.newHashSet(getSynchroUtils().getFieldWorkAgencyGroupUsers());
        if(fieldAgencyUsers.contains(user))
        {
            return true;
        }
        return false;
    }

    /**
     * This method will check whether logged in user is Communication Agency user or not.
     * @return true/false
     */
    public static boolean isCommunicationAgencyUser(){
        User user = getEffectiveUser();
        Set<User> comAgencyUsers = Sets.newHashSet(getSynchroUtils().getCommunicationAgencyGroupUsers());
        if(comAgencyUsers.contains(user))
        {
            return true;
        }
        return false;

    }

    /**
     * This method will check whether logged in user is Communication Agency user or not.
     * @return true/false
     */
    public static boolean isCommunicationAgencyUser(final User user){
        Set<User> comAgencyUsers = Sets.newHashSet(getSynchroUtils().getCommunicationAgencyGroupUsers());
        if(comAgencyUsers.contains(user))
        {
            return true;
        }
        return false;

    }



    /**
     * This method will check whether logged in user is Communication Agency user or not.
     * @param projectId
     * @return
     */
    public static boolean isCommunicationAgencyUser(Long projectId, Long endMarketId){
        /*User user = getEffectiveUser();
        Set<User> comAgencyUsers = Sets.newHashSet(getSynchroUtils().getCommunicationAgencyUsers(projectId));
        if(comAgencyUsers.contains(user))
        {
            return true;
        }*/
        User user = getEffectiveUser();
        Set<User> commAgencyUsers = Sets.newHashSet(getSynchroUtils().getCommunicationAgencyUsers(projectId, endMarketId));
        if(commAgencyUsers.contains(user))
        {
            return true;
        }
        if(isCommunicationAgencyUser(user)) {
            return true;
        }
        return false;

    }

    /**
     * This method will check whether logged in user is Coordinating Agency user or not.
     * @return true/false
     */
    public static boolean isCoordinatingAgencyUser(){
        User user = getEffectiveUser();
        Set<User> comAgencyUsers = Sets.newHashSet(getSynchroUtils().getCoordinationGroupAgencyUsers());
        if(comAgencyUsers.contains(user))
        {
            return true;
        }
        return false;

    }

    /**
     * This method will check whether logged in user is Coordinating Agency user or not.
     * @return true/false
     */
    public static boolean isCoordinatingAgencyUser(final User user){
        Set<User> comAgencyUsers = Sets.newHashSet(getSynchroUtils().getCoordinationGroupAgencyUsers());
        if(comAgencyUsers.contains(user))
        {
            return true;
        }
        return false;

    }

    /**
     * This method will check whether logged in user is Coordinating Agency Support user or not.
     * @return true/false
     */
    public static boolean isCoordinatingAgencySupportUser(){
        User user = getEffectiveUser();
        Set<User> comAgencyUsers = Sets.newHashSet(getSynchroUtils().getCoordinationAgencySupportGroupUsers());
        if(comAgencyUsers.contains(user))
        {
            return true;
        }
        return false;

    }

    /**
     * This method will check whether logged in user is Coordinating Agency  Support user or not.
     * @return true/false
     */
    public static boolean isCoordinatingAgencySupportUser(final User user){
        Set<User> comAgencyUsers = Sets.newHashSet(getSynchroUtils().getCoordinationAgencySupportGroupUsers());
        if(comAgencyUsers.contains(user))
        {
            return true;
        }
        return false;

    }

    public static SynchroUtils getSynchroUtils() {
        if(synchroUtils==null)
        {
            return JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }

    public static Boolean isFWEnabled(Long projectID)
    {/*
		return getSynchroProjectManager().get(projectID).getFwEnabled();
	*/
        //dummy below
        return true;
    }

    public static boolean hasValidProjectStatus(Long projectID)
    {
        if(projectID!=null && projectID>0)
        {
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroGlobal.Status.DRAFT.ordinal()==status)
            {
                return false;
            }
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin())
            {
                return true;
            }
            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return false;
            }

            long ownerID = -1L;
            ownerID = project.getProjectOwner();
            if(ownerID==getEffectiveUser().getID())
            {
                return true;
            }

        }
        else
        {
            return false;
        }
        return true;
    }

    public static boolean canEditProjectByStatus(Long projectID)
    {
        if(projectID!=null && projectID>0)
        {
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();

            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin())
            {
                return true;
            }

            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return false;
            }

            if(SynchroGlobal.Status.PIT_CANCEL.ordinal()==status || SynchroGlobal.Status.PIB_CANCEL.ordinal()==status || SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()==status)
            {
                return false;
            }

            if(SynchroGlobal.Status.PIT_ONHOLD.ordinal()==status || SynchroGlobal.Status.PIB_ONHOLD.ordinal()==status || SynchroGlobal.Status.INPROGRESS_ONHOLD.ordinal()==status)
            {
                return false;
            }

            // if(SynchroGlobal.Status.COMPLETED.ordinal()==status || SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal()==status)
            if(SynchroGlobal.Status.COMPLETED.ordinal()==status )
            {
                return false;
            }
            // This is done as the status Completed Project Evaluation can be changed anytime
            if(SynchroGlobal.Status.COMPLETED_PROJ_EVALUATION.ordinal()==status )
            {
                List<ReportSummaryInitiation> rpList = getReportSummaryManager().getReportSummaryInitiation(projectID);
                if(rpList!=null && rpList.size()>0 && rpList.get(0).getStatus()==SynchroGlobal.StageStatus.REPORT_SUMMARY_COMPLETED.ordinal())
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }

        }
        else
        {
            return false;
        }
        return true;
    }
    public static boolean canEditProjectEvaluation(Long projectID)
    {
        if(projectID!=null && projectID>0)
        {
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();

            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin())
            {
                return true;
            }

            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return false;
            }

            if(SynchroGlobal.Status.PIT_CANCEL.ordinal()==status || SynchroGlobal.Status.PIB_CANCEL.ordinal()==status || SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()==status)
            {
                return false;
            }

            if(SynchroGlobal.Status.PIT_ONHOLD.ordinal()==status || SynchroGlobal.Status.PIB_ONHOLD.ordinal()==status || SynchroGlobal.Status.INPROGRESS_ONHOLD.ordinal()==status)
            {
                return false;
            }

            // Project Evaluation will be enabled if it is not completed
            /*   List<ProjectEvaluationInitiation> initiationList = getProjectEvaluationManager().getProjectEvaluationInitiation(projectID);
            if(initiationList!=null && initiationList.size()>0 && initiationList.get(0).getStatus()==SynchroGlobal.StageStatus.PROJ_EVAL_COMPLETED.ordinal())
            {
                return false;
            }
            else
            {
                return true;
            }*/

        }
        else
        {
            return false;
        }
        return true;

    }

    public static boolean canAdministerProject(Long projectID)
    {
        if(projectID!=null && projectID>0)
        {
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroGlobal.Status.DRAFT.ordinal()==status)
            {
                return false;
            }
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin())
            {
                return true;
            }
            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return false;
            }

            /* if(SynchroGlobal.Status.CONCEPT_CANCEL.ordinal()==status || SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()==status || SynchroGlobal.Status.PLANNED_CANCEL.ordinal()==status)
            {
                return false;
            }

            if(SynchroGlobal.Status.CONCEPT_ONHOLD.ordinal()==status || SynchroGlobal.Status.INPROGRESS_ONHOLD.ordinal()==status || SynchroGlobal.Status.PLANNED_ONHOLD.ordinal()==status)
            {
                return false;
            }*/
            long ownerID = -1L;
            ownerID = project.getProjectOwner();
            if(ownerID==getEffectiveUser().getID())
            {
                return true;
            }
        }
        else
        {
            return false;
        }
        return false;
    }

    public static boolean canChangeProjectStatus(Long projectID)
    {
        if(projectID!=null && projectID>0)
        {
            Project project = getSynchroProjectManager().get(projectID);
            Integer status = project.getStatus().intValue();
            if(SynchroGlobal.Status.DRAFT.ordinal()==status)
            {
                return false;
            }
            if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin())
            {
                return true;
            }
            if(SynchroGlobal.Status.DELETED.ordinal()==status)
            {
                return false;
            }
            /* if(SynchroGlobal.Status.CONCEPT_ONHOLD.ordinal()==status || SynchroGlobal.Status.INPROGRESS_ONHOLD.ordinal()==status || SynchroGlobal.Status.PLANNED_ONHOLD.ordinal()==status)
            {
                return true;
            }

            if(SynchroGlobal.Status.CONCEPT_CANCEL.ordinal()==status || SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()==status || SynchroGlobal.Status.PLANNED_CANCEL.ordinal()==status)
            {
                return false;
            }*/

            if(SynchroGlobal.Status.COMPLETED.ordinal()==status)
            {
                return false;
            }
        }
        else
        {
            return false;
        }
        return true;
    }

    public static boolean canAccessProject(final Long projectId) {
        Project project = getSynchroProjectManager().get(projectId);
        if(!project.getConfidential()) {
            User user = getEffectiveUser();
            Map<String,String> userProperties = user.getProperties();
            if(userProperties != null && !userProperties.isEmpty()) {
                boolean hasBrandAccess = false;
                boolean containsBrands = false;
                boolean hasDepartmentAccess = false;
                boolean containsDepartments = false;
                boolean isExternalAgency = SynchroPermHelper.isExternalAgencyUser(user);
                if(isExternalAgency) {
                    if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_DEPARTMENTS)
                            && userProperties.get(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_DEPARTMENTS) != null
                            && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_DEPARTMENTS).equals("")) {
                        containsDepartments = true;
                        String departments = userProperties.get(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_DEPARTMENTS);
                        String[] departmentsList = departments.split(",");
                        PIBStakeholderList pibStakeholderList = getPibManager().getPIBStakeholderList(projectId);
                        if(pibStakeholderList != null) {
                            // Agency1
                            if((!hasDepartmentAccess) && pibStakeholderList.getAgencyContact1() != null && pibStakeholderList.getAgencyContact1() > 0) {
                                if(agencyDepartmentExists(pibStakeholderList.getAgencyContact1(), departmentsList)) {
                                    hasDepartmentAccess = true;
                                }
                            }
                            if((!hasDepartmentAccess) && pibStakeholderList.getAgencyContact1Optional() != null && pibStakeholderList.getAgencyContact1Optional() > 0) {
                                if(agencyDepartmentExists(pibStakeholderList.getAgencyContact1Optional(), departmentsList)) {
                                    hasDepartmentAccess = true;
                                }
                            }

                            // Agency 2
                            if((!hasDepartmentAccess) && pibStakeholderList.getAgencyContact2() != null && pibStakeholderList.getAgencyContact2() > 0) {
                                if(agencyDepartmentExists(pibStakeholderList.getAgencyContact2(), departmentsList)) {
                                    hasDepartmentAccess = true;
                                }
                            }
                            if((!hasDepartmentAccess) && pibStakeholderList.getAgencyContact2Optional() != null && pibStakeholderList.getAgencyContact2Optional() > 0) {
                                if(agencyDepartmentExists(pibStakeholderList.getAgencyContact2Optional(), departmentsList)) {
                                    hasDepartmentAccess = true;
                                }
                            }

                            // Agency 3
                            if((!hasDepartmentAccess) && pibStakeholderList.getAgencyContact3() != null && pibStakeholderList.getAgencyContact3() > 0) {
                                if(agencyDepartmentExists(pibStakeholderList.getAgencyContact3(), departmentsList)) {
                                    hasDepartmentAccess = true;
                                }
                            }
                            if(pibStakeholderList.getAgencyContact3Optional() != null && pibStakeholderList.getAgencyContact3Optional() > 0) {
                                if(agencyDepartmentExists(pibStakeholderList.getAgencyContact3Optional(), departmentsList)) {
                                    hasDepartmentAccess = true;
                                }
                            }
                        }
                    }
                }
                if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_BRANDS)) {
                    String brandsString = userProperties.get(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_BRANDS);
                    if(brandsString != null && !brandsString.equals("")) {
                        containsBrands = true;
                        String[] brands = brandsString.split(",");
                        for(String brand: brands) {
                            if(Long.parseLong(brand) == project.getBrand()) {
                                hasBrandAccess = true;
                            }
                        }
                    }

                }

                StringBuilder accessTypeFiler = new StringBuilder();
                Boolean globalAccessSuperUser = false;
                if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER)
                        && userProperties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER) != null
                        && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER).equals("")) {
                    globalAccessSuperUser = Boolean.parseBoolean(userProperties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER));
                }


                if(globalAccessSuperUser) {
                    return deliverAccess(project, containsBrands, hasBrandAccess, containsDepartments, hasDepartmentAccess);
//                    if(isExternalAgency) {
//                        if(project.getStatus() >= SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()) {
//                            if(containsBrands && containsDepartments) {
//                                return hasBrandAccess && hasDepartmentAccess;
//                            } else if(containsDepartments) {
//                                return hasDepartmentAccess;
//                            } else {
//                                return false;
//                            }
//                        } else {
//                            return false;
//                        }
//                    } else {
//                        return containsBrands?hasBrandAccess:true;
//                    }
                } else {
                    if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)
                            && userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST) != null
                            && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).equals("")) {
                        List<EndMarketInvestmentDetail> endMarketInvestmentDetails = getSynchroProjectManager().getEndMarketDetails(projectId);
                        String endmarketAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST);
                        String [] endmarketAccessList = endmarketAccessListStr.split(",");
                        for(EndMarketInvestmentDetail endMarketInvestmentDetail : endMarketInvestmentDetails) {
                            for(String endmarketAccessId : endmarketAccessList) {
                                if(Long.parseLong(endmarketAccessId) == endMarketInvestmentDetail.getEndMarketID()) {
                                    return deliverAccess(project, containsBrands, hasBrandAccess, containsDepartments, hasDepartmentAccess);
//                                    if(isExternalAgency) {
//                                        if(project.getStatus() >= SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()) {
//                                            if(containsBrands && containsDepartments) {
//                                                return hasBrandAccess && hasDepartmentAccess;
//                                            } else if(containsDepartments) {
//                                                return hasDepartmentAccess;
//                                            } else {
//                                                return false;
//                                            }
//                                        } else {
//                                            return false;
//                                        }
//                                    } else {
//                                        return containsBrands?hasBrandAccess:true;
//                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean deliverAccess(final Project project,
                                         final boolean containsBrands, final boolean hasBrandAccess,
                                         final boolean containsDepartments, final boolean hasDepartmentAccess) {
        boolean isExternalAgency = SynchroPermHelper.isExternalAgencyUser(getEffectiveUser());
        if(isExternalAgency) {
            if(project.getStatus() >= SynchroGlobal.Status.INPROGRESS_OPEN.ordinal()) {
                if(containsBrands && containsDepartments) {
                    return hasBrandAccess && hasDepartmentAccess;
                } else if(containsDepartments) {
                    return hasDepartmentAccess;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return containsBrands?hasBrandAccess:true;
        }
    }

    private static boolean agencyDepartmentExists(final Long agencyId, String [] departmentsList) {
        boolean exists = false;
        Map<Long, ProfileFieldValue> profileFieldMap = null;
        try {
            profileFieldMap = getProfileManager().getProfile(getUserManager().getUser(agencyId));
        } catch (UserNotFoundException e) {
            LOG.error("Agency contact ::" + agencyId + " not found.");
        }
        if(profileFieldMap != null && profileFieldMap.get(2L) != null) {
            String val = profileFieldMap.get(2L).getValue();
            if(val != null && !val.equals("")) {
                for(String deptVal : departmentsList) {
                    if(deptVal.equals(val)) {
                        exists = true;
                        break;
                    }
                }
            }
        }

        return exists;
    }
    private boolean departmentExits(String [] departmentsList, String val) {
        boolean exists = false;
        for(String deptVal : departmentsList) {
            if(deptVal.equals(val)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    public static boolean isSynchroGlobalSuperUser(final User user) {
        boolean globalSuperUser = false;
        Map<String,String> userProperties = user.getProperties();
        if(userProperties != null && !userProperties.isEmpty()) {
            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER)
                    && userProperties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER) != null
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER).equals("")) {
                globalSuperUser = Boolean.parseBoolean(userProperties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER));
            }
        }
        return globalSuperUser;
    }

    public static boolean isSynchroRegionalSuperUser(final User user) {
        boolean superUser = false;
        Map<String,String> userProperties = user.getProperties();
        if(userProperties != null && !userProperties.isEmpty()) {
            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER)
                    && userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER) != null
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER).equals("")) {
                superUser = Boolean.parseBoolean(userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER));
            }
        }
        return superUser;
    }

    public static boolean isSynchroAreaSuperUser(final User user) {
        boolean superUser = false;
        Map<String,String> userProperties = user.getProperties();
        if(userProperties != null && !userProperties.isEmpty()) {
            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER)
                    && userProperties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER) != null
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER).equals("")) {
                superUser = Boolean.parseBoolean(userProperties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER));
            }
        }
        return superUser;
    }

    public static boolean isSynchroEndmarketSuperUser(final User user) {
        boolean superUser = false;
        Map<String,String> userProperties = user.getProperties();
        if(userProperties != null && !userProperties.isEmpty()) {
            if(!(isSynchroRegionalSuperUser(user) || isSynchroAreaSuperUser(user)) &&
                    (userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)
                            && userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST) != null
                            && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).equals(""))) {
                String endmarketAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST);
                String [] endmarketAccessList = endmarketAccessListStr.split(",");
                if(endmarketAccessList.length > 0) {
                    superUser = true;
                }
            }
        }
        return superUser;
    }

    /**
     * This method will check whether a particular user is EndMarket Super User for a particular End Market.
     * @param user
     * @param endMarketId
     * @return
     */
    public static boolean isSynchroEndmarketSuperUser(final User user, final Long endMarketId) {
        boolean superUser = false;
        if(endMarketId != null && endMarketId > 0) {
            Map<String,String> userProperties = user.getProperties();
            if(userProperties != null && !userProperties.isEmpty()) {
                if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)
                        && userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST) != null
                        && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).equals("")) {
                    String endmarketAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST);
                    String [] endmarketAccessList = endmarketAccessListStr.split(",");
                    if(endmarketAccessList.length > 0) {
                        for(int i=0;i<endmarketAccessList.length;i++) {
                            if(endMarketId.intValue()==Integer.parseInt(endmarketAccessList[i])) {
                                superUser = true;
                            }
                        }
                    }
                }
            }
        }
        return superUser;
    }

    public static boolean isSynchroSuperUser(final User user) {
        boolean superUser = false;
        Map<String,String> userProperties = user.getProperties();
        if(userProperties != null && !userProperties.isEmpty()) {
            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER)
                    && userProperties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER) != null
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER).equals("")) {
                superUser = Boolean.parseBoolean(userProperties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER));
            }

            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER)
                    && userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER) != null
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER).equals("")) {
                superUser = Boolean.parseBoolean(userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER));
            }

            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER)
                    && userProperties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER) != null
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER).equals("")) {
                superUser = Boolean.parseBoolean(userProperties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER));
            }

            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)
                    && userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST) != null
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).equals("")) {
                String endmarketAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST);
                String [] endmarketAccessList = endmarketAccessListStr.split(",");
                if(endmarketAccessList.length > 0) {
                    superUser = true;
                }
            }
        }
        return superUser;
    }

    public static boolean canViewProjectCancelMessage(Long projectID)
    {

        if(SynchroPermHelper.isSynchroAdmin(getEffectiveUser()) || SynchroPermHelper.isSynchroMiniAdmin())
        {
            return false;
        }

        if(hasProjectAccess(projectID))
        {

            Project project = getSynchroProjectManager().get(projectID);
            long ownerID = -1L;
            ownerID = project.getProjectOwner();
            if(ownerID==getEffectiveUser().getID())
            {
                return false;
            }
            Integer status = project.getStatus().intValue();
            /*  if(SynchroGlobal.Status.CONCEPT_CANCEL.ordinal()==status || SynchroGlobal.Status.INPROGRESS_CANCEL.ordinal()==status || SynchroGlobal.Status.PLANNED_CANCEL.ordinal()==status)
            {
                return true;
            }*/
        }
        return false;

    }
    public static boolean isMethodologyApproverUser(){
        User user = getEffectiveUser();
        boolean isMethodologyApprover = false;
        try {
            final Group methAppGroup = getGroupManager().getGroup(SynchroConstants.JIVE_METHODOLOGY_APPROVERS_GROUP_NAME);
            if(methAppGroup != null){
                isMethodologyApprover =  methAppGroup.getMemberIds().contains(user.getID());
                // Check if the member is a Group Admin permission
                if(!isMethodologyApprover){
                    final UserManager userManager = JiveApplication.getContext().getUserManager();
                    try {
                        isMethodologyApprover = methAppGroup.isAdministrator(userManager.getUser(user.getID()));
                    } catch (UserNotFoundException e) {
                        // do-nothing
                    }
                }
            }
        } catch (GroupNotFoundException e) {
            LOG.debug("## There is 'Methodology Approver ' user group defined in system.", e);
        }
        LOG.debug("## Is logged in user member of Methodology Approver Group ? " + isMethodologyApprover);
        return isMethodologyApprover;
    }

    /*
    * This method will check whether the logged in user is Kantar Methodology Approver user or not.
    */
    public static boolean isKantarMethodologyApproverUser(){
        User user = getEffectiveUser();
        boolean isMethodologyApprover = false;
        try {
            final Group methAppGroup = getGroupManager().getGroup(SynchroConstants.JIVE_KANTAR_METHODOLOGY_APPROVERS_GROUP_NAME);
            if(methAppGroup != null){
                isMethodologyApprover =  methAppGroup.getMemberIds().contains(user.getID());
                // Check if the member is a Group Admin permission
                if(!isMethodologyApprover){
                    final UserManager userManager = JiveApplication.getContext().getUserManager();
                    try {
                        isMethodologyApprover = methAppGroup.isAdministrator(userManager.getUser(user.getID()));
                    } catch (UserNotFoundException e) {
                        // do-nothing
                    }
                }
            }
        } catch (GroupNotFoundException e) {
            LOG.debug("## There is 'Kantar Methodology Approver ' user group defined in system.", e);
        }
        LOG.debug("## Is logged in user member of Kantar Methodology Approver Group ? " + isMethodologyApprover);
        return isMethodologyApprover;
    }

    public static boolean isSynchroUser(final User user) {
        final List<User> synchroUsers = getSynchroUtils().getSynchroGroupUsers();
        if(isSynchroMiniAdmin() || isSynchroAdmin(user))
            return true;
        if( synchroUsers != null){
            return synchroUsers.contains(user);
        }
        return false;
    }

    public static boolean canAccessKantarPortal(final User user) {
        if(isSynchroAdmin(user) || isSynchroMiniAdmin(user) || isSynchroGlobalSuperUser(user)
                || SynchroPermHelper.isKantarAgencyUser(user)
                || (isSynchroUser(user) && !SynchroPermHelper.isExternalAgencyUser(user))
                || (isSynchroUser(user) && !SynchroPermHelper.isCommunicationAgencyUser(user))) {
            return true;
        }

        return false;
    }

    public static boolean canAccessKantarProject(final KantarBriefTemplate kantarBriefTemplate, final User user) {
        if(user == null || !canAccessKantarPortal(user) || kantarBriefTemplate == null) {
            return false;
        }

        if(isSynchroAdmin(user) || isSynchroMiniAdmin(user) || isSynchroGlobalSuperUser(user)
                || (kantarBriefTemplate.getBatContact() != null && kantarBriefTemplate.getBatContact().equals(user.getID()))
                || (kantarBriefTemplate.getCreatedBy() != null && kantarBriefTemplate.getCreatedBy().equals(user.getID()))
                || isSynchroEndmarketSuperUser(user, kantarBriefTemplate.getMarkets())) {
            return true;
        }
        return false;
    }


    public static boolean canAccessDocumentRepositoryPortal(final User user) {
        if(isSynchroAdmin(user) || isSynchroMiniAdmin(user)
                || SynchroPermHelper.isDocumentRepositoryBATUser(user)
                || SynchroPermHelper.isDocumentRepositoryAgencyUser(user)
                || ((isSynchroUser(user) || isSynchroGlobalSuperUser(user) || isSynchroRegionalSuperUser(user) || isSynchroAreaSuperUser(user) || isSynchroEndmarketSuperUser(user)) &&
                !(SynchroPermHelper.isExternalAgencyUser(user)
                        || SynchroPermHelper.isCommunicationAgencyUser(user)
                        || SynchroPermHelper.isCoordinatingAgencyUser(user)
                        || SynchroPermHelper.isCoordinatingAgencySupportUser(user)))
                ) {
            return true;
        }

        return false;
    }

    public static boolean canAccessKantarReportProject(final KantarReportBean bean, final User user) {
        if(user == null || !canAccessDocumentRepositoryPortal(user) || bean == null) {
            return false;
        }

        if(isSynchroAdmin(user) || isSynchroMiniAdmin(user) || isSynchroGlobalSuperUser(user)
                || (bean.getCreatedBy() != null && bean.getCreatedBy().equals(user.getID()))
                || (bean.getModifiedBy() != null && bean.getModifiedBy().equals(user.getID()))
                || isSynchroEndmarketSuperUser(user, bean.getCountry())) {
            return true;
        }
        return false;
    }


    public static boolean canAccessGrailPortal(final User user) {
        if(isSynchroAdmin(user) || isSynchroMiniAdmin(user) || isSynchroGlobalSuperUser(user)
                || (isSynchroUser(user) && !SynchroPermHelper.isExternalAgencyUser(user))
                || (isSynchroUser(user) && !SynchroPermHelper.isCommunicationAgencyUser(user))) {
            return true;
        }

        return false;
    }


    public static boolean canAccessGrailProject(final GrailBriefTemplate grailBriefTemplate, final User user) {
        if(user == null || !canAccessGrailPortal(user) || grailBriefTemplate == null) {
            return false;
        }

        if(isSynchroAdmin(user) || isSynchroMiniAdmin(user) || isSynchroGlobalSuperUser(user)
                || (grailBriefTemplate.getBatContact() != null && grailBriefTemplate.getBatContact().equals(user.getID()))
                || (grailBriefTemplate.getCreatedBy() != null && grailBriefTemplate.getCreatedBy().equals(user.getID()))
                || isSynchroEndmarketSuperUser(user, grailBriefTemplate.getMarkets())) {
            return true;
        }
        return false;
    }

    public static boolean canCreateKantarProject(final User user) {
        if(user == null) {
            return false;
        }
        if(canAccessKantarPortal(user)) {
            return true;
        }
        return false;
    }

    public static boolean canCreateGrailProject(final User user) {
        if(user == null) {
            return false;
        }
        if(canAccessGrailPortal(user)) {
            return true;
        }
        return false;
    }

    public static boolean isKantarAgencyUser(final User user) {
        final List<User> kantarAgencyGroupUsers = getSynchroUtils().getKantarAgencyGroupUsers();
        if(isSynchroMiniAdmin() || isSynchroAdmin(user))
            return true;
        if(kantarAgencyGroupUsers != null){
            return kantarAgencyGroupUsers.contains(user);
        }
        return false;
    }

    public static boolean isDocumentRepositoryAgencyUser(final User user) {
        final List<User> documentRepositoryAgencyGroupUsers = getSynchroUtils().getDocumentRepositoryAgencyGroupUsers();
        if(isSynchroMiniAdmin() || isSynchroAdmin(user))
            return true;
        if(documentRepositoryAgencyGroupUsers != null){
            return documentRepositoryAgencyGroupUsers.contains(user);
        }
        return false;
    }

    public static boolean isDocumentRepositoryBATUser(final User user) {
        final List<User> documentRepositoryBATGroupUsers = getSynchroUtils().getDocumentRepositoryBATGroupUsers();
        if(isSynchroMiniAdmin() || isSynchroAdmin(user))
            return true;
        if(documentRepositoryBATGroupUsers != null){
            return documentRepositoryBATGroupUsers.contains(user);
        }
        return false;
    }

    public static boolean canUploadDocumentRepositoryDocument(final User user) {
        if(user == null) {
            return false;
        }
        if(isDocumentRepositoryBATUser(user) || isDocumentRepositoryAgencyUser(user)) {
            return true;
        }
        return false;
    }


    public static boolean canInitiateProject(final User user)
    {
        return getSynchroUtils().canCreateProject(user);
    }

    public static boolean canInitiateProject_NEW()
    {
        return getSynchroUtils().canCreateProject_NEW(getEffectiveUser());
    }

    public static boolean canInitiateWaiver(final User user)
    {
        return getSynchroUtils().canIniateProjectWaiver(user);
    }
    
    public static boolean canInitiateWaiver_NEW()
    {
    	 if(!isSynchroUser(getEffectiveUser()))
         {
             return false;
         }
         
         // Legal User should not be able to initiate Process Waiver. http://redmine.nvish.com/redmine/issues/491
         if(SynchroPermHelper.isLegaUserType())
         {
         	return false;
         }
      
         // http://redmine.nvish.com/redmine/issues/466 . Global users should not be able to initiate Process Waivers
         if(SynchroPermHelper.isGlobalUserType())
         {
         	if(!SynchroPermHelper.isEndMarketUserType())
         	{
         		return false;
         	}
         }
         
         if(SynchroPermHelper.isRegionalUserType())
         {
         	if(!SynchroPermHelper.isEndMarketUserType())
         	{
         		return false;
         	}
         }
         return true;
    }

    /**
     * This method will check whether logged in user is Above Market Project Contact or not.
     * @param projectId
     * @return
     */
    public static boolean isAboveMarketProjectContact(Long projectId){
        User user = getEffectiveUser();
        List<FundingInvestment> fundingInvestment = getSynchroProjectManager().getProjectInvestments(projectId);
        for(FundingInvestment fd:fundingInvestment )
        {
            if(fd.getAboveMarket() && user.getID()==fd.getProjectContact())
            {
                return true;
            }
        }
        return false;

    }

    /**
     * This method will check whether logged in user is Regional Project Contact or not.
     * @param projectId
     * @return
     */
    public static boolean isRegionalProjectContact(Long projectId){
        User user = getEffectiveUser();
        List<FundingInvestment> fundingInvestment = getSynchroProjectManager().getProjectInvestments(projectId);
        for(FundingInvestment fd:fundingInvestment )
        {
            if(fd.getInvestmentType().intValue()==SynchroGlobal.InvestmentType.REGION.getId() && user.getID()==fd.getProjectContact())
            {
                return true;
            }
        }
        return false;

    }
    /**
     * This method will check whether logged in user is Area Project Contact or not.
     * @param projectId
     * @return
     */
    public static boolean isAreaProjectContact(Long projectId){
        User user = getEffectiveUser();
        List<FundingInvestment> fundingInvestment = getSynchroProjectManager().getProjectInvestments(projectId);
        for(FundingInvestment fd:fundingInvestment )
        {
            if(fd.getInvestmentType().intValue()==SynchroGlobal.InvestmentType.AREA.getId() && user.getID()==fd.getProjectContact())
            {
                return true;
            }
        }
        return false;

    }
    /**
     * This method will check whether logged in user is Country Project Contact or not.
     * @param projectId
     * @return
     */
    public static boolean isCountryProjectContact(Long projectId){
        User user = getEffectiveUser();
        List<FundingInvestment> fundingInvestment = getSynchroProjectManager().getProjectInvestments(projectId);
        for(FundingInvestment fd:fundingInvestment )
        {
            if(fd.getInvestmentType().intValue()==SynchroGlobal.InvestmentType.COUNTRY.getId() && user.getID()==fd.getProjectContact())
            {
                return true;
            }
        }
        return false;

    }

    /**
     * This method will check whether logged in user is Country Project Contact or not.
     * @param projectId
     * @return
     */
    public static boolean isCountryProjectContact(Long projectId, Long endMarketId){
        User user = getEffectiveUser();
        List<FundingInvestment> fundingInvestment = getSynchroProjectManager().getProjectInvestments(projectId);
        for(FundingInvestment fd:fundingInvestment )
        {
            if(fd.getInvestmentType().intValue()==SynchroGlobal.InvestmentType.COUNTRY.getId() && user.getID()==fd.getProjectContact() && endMarketId.intValue()==fd.getFieldworkMarketID().intValue())
            {
                return true;
            }
        }
        return false;

    }


    /**
     * This method will check whether logged in user is Country SPI Contact or not.
     * @param projectId
     * @return
     */
    public static boolean isCountrySPIContact(Long projectId){
        User user = getEffectiveUser();
        List<FundingInvestment> fundingInvestment = getSynchroProjectManager().getProjectInvestments(projectId);
        for(FundingInvestment fd:fundingInvestment )
        {
            if(fd.getInvestmentType().intValue()==SynchroGlobal.InvestmentType.COUNTRY.getId() && user.getID()==fd.getSpiContact())
            {
                return true;
            }
        }
        return false;

    }

    /**
     * This method will check whether logged in user is Country SPI Contact or not.
     * @param projectId
     * @return
     */
    public static boolean isCountrySPIContact(Long projectId, Long endMarketId){
        User user = getEffectiveUser();
        List<FundingInvestment> fundingInvestment = getSynchroProjectManager().getProjectInvestments(projectId);
        for(FundingInvestment fd:fundingInvestment )
        {
            if(fd.getInvestmentType().intValue()==SynchroGlobal.InvestmentType.COUNTRY.getId() && user.getID()==fd.getSpiContact() && endMarketId.intValue()==fd.getFieldworkMarketID().intValue())
            {
                return true;
            }
        }
        return false;

    }

    public static boolean isMarketDeleted(Long projectId, Long marketid)
    {
        Integer status = getSynchroProjectManager().getEndMarketStatus(projectId, marketid);

        if(status>-1 && status == SynchroGlobal.ProjectActivationStatus.DELETED.ordinal())
        {
            return true;
        }

        return false;
    }

    public static boolean isBudgetApprover(Long projectID)
    {
        Boolean isBudgetApprover = false;
        List<FundingInvestment> fundingInvestments = getSynchroProjectManager().getProjectInvestments(projectID);
        final Long userID = getEffectiveUser().getID();
        for(FundingInvestment investment : fundingInvestments)
        {
            if(investment.getSpiContact()!=null && investment.getSpiContact().intValue() == userID.intValue())
            {
                isBudgetApprover = true;
            }

            if(investment.getProjectContact()!=null && investment.getProjectContact().intValue() == userID.intValue())
            {
                isBudgetApprover = true;
            }
        }
        return isBudgetApprover;
    }

    // TODO: Need to remove below method once production verification done
    public static boolean hasGenerateReportAccess(final User user) {
        boolean hasAccess = false;
        Long userId = user.getID();
        // 2520, 2528, 2517, 2523,2521, 2530, 2534
        if(userId.intValue() == 2520 || userId.intValue() == 2528 || userId.intValue() == 2517
                || userId.intValue() == 2523 || userId.intValue() == 2521
                || userId.intValue() == 2530 || userId.intValue() == 2534 || userId.intValue() == 2002) {
            hasAccess = true;
        }
        return hasAccess;
    }

    /**
     * This method will check whether logged in user is Other SPI Contact or not.
     * @param projectId
     * @return
     */
    public static boolean isOtherSPIContact(Long projectId, Long endMarketId){
        User user = getEffectiveUser();
        String otherSPIContacts = getPibManager().getOtherSPIContact(projectId, endMarketId);
        if(otherSPIContacts!=null && otherSPIContacts.contains(user.getID()+""))
        {
            return true;
        }
        return false;

    }

    /**
     * This method will check whether logged in user is Other Legal Contact or not.
     * @param projectId
     * @return
     */
    public static boolean isOtherLegalContact(Long projectId, Long endMarketId){
        User user = getEffectiveUser();
        String otherLegalContacts = getPibManager().getOtherLegalContact(projectId, endMarketId);
        if(otherLegalContacts!=null && otherLegalContacts.contains(user.getID()+""))
        {
            return true;
        }
        return false;

    }

    /**
     * This method will check whether logged in user is Other Product Contact or not.
     * @param projectId
     * @return
     */
    public static boolean isOtherProductContact(Long projectId, Long endMarketId){
        User user = getEffectiveUser();
        String otherProductContacts = getPibManager().getOtherProductContact(projectId, endMarketId);
        if(otherProductContacts!=null && otherProductContacts.contains(user.getID()+""))
        {
            return true;
        }
        return false;

    }

    /**
     * This method will check whether logged in user is Other Agency Contact or not.
     * @param projectId
     * @return
     */
    public static boolean isOtherAgencyContact(Long projectId, Long endMarketId){
        User user = getEffectiveUser();
        String otherAgencyContacts = getPibManager().getOtherAgencyContact(projectId, endMarketId);
        if(otherAgencyContacts!=null && otherAgencyContacts.contains(user.getID()+""))
        {
            return true;
        }
        return false;

    }
    
    /**
     * This method will check whether logged in user is selected Legal Contact User or not.
     * @param projectId
     * @return
     */
    public static boolean isBriefLegalUser(Long projectId){
        User user = getEffectiveUser();
        List<ProjectInitiation> projectInitiation= getPibManagerNew().getPIBDetailsNew(projectId);
        if(projectInitiation!=null && projectInitiation.size()>0 && projectInitiation.get(0).getBriefLegalApprover()!=null && projectInitiation.get(0).getBriefLegalApprover() == user.getID())
        
        {
            return true;
        }

        return false;

    }
    
    public static boolean isProposalLegalUser(Long projectId){
        User user = getEffectiveUser();
        List<ProposalInitiation> proposalInitiation= getProposalManagerNew().getProposalInitiationNew(projectId);
        if(proposalInitiation!=null && proposalInitiation.size()>0 && proposalInitiation.get(0).getProposalLegalApprover()!=null && proposalInitiation.get(0).getProposalLegalApprover() == user.getID())
        
        {
            return true;
        }

        return false;

    }
    
    public static boolean isGlobalUserType(){
        final User user = getEffectiveUser();
        Map<String,String> userProperties = user.getProperties();
        if(userProperties!=null && userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER) && userProperties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER).equalsIgnoreCase("true"))
        {
        	return true;
        }
        	
        return false;
       
    }
    public static boolean isRegionalUserType(){
        final User user = getEffectiveUser();
        Map<String,String> userProperties = user.getProperties();
        if(userProperties!=null && userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER) && userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER).equalsIgnoreCase("true"))
        {
        	return true;
        }
        	
        return false;
       
    }
    public static boolean isEndMarketUserType(){
        final User user = getEffectiveUser();
        Map<String,String> userProperties = user.getProperties();
        if(userProperties!=null && userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_SUPER_USER) && userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_SUPER_USER).equalsIgnoreCase("true"))
        {
        	return true;
        }
        	
        return false;
       
    }
    
    /**
     * This method will check whether logged in user is Synchro System Owner or not
     * @param projectId
     * @return
     */
    public static boolean isSynchroSystemOwner(){
        User user = getEffectiveUser();
        
        
        List<User> systemOwners = getSynchroUtils().getSynchroSystemOwnerUsers();
        if(systemOwners!=null && systemOwners.size()>0 && systemOwners.contains(user))
        {
        	return true;
        }
        return false;

    }
    
    public static List<String> getSynchroUserNames()
    {
    	List<String> synchroUserNames = new ArrayList<String>();
       
       
    	synchroUserNames = getSynchroProjectManagerNew().getSynchroUserNames();
        /*
        try {
            final Group synchroGroup = getGroupManager().getGroup(SynchroConstants.JIVE_SYNCHRO_GROUP_NAME);
            if(synchroGroup != null){
                List<User> synchroUsers =  synchroGroup.getMembers();
                for(User user : synchroUsers)
                {
                	synchroUserNames.add(user.getName());
                }
            }
        } catch (GroupNotFoundException e) {
            LOG.debug("## There is 'ORACLE' user group defined in system.", e);
        }*/
        return synchroUserNames;
    }
    
    public static boolean isLegaUserType(){
        final User user = getEffectiveUser();
        Map<String,String> userProperties = user.getProperties();
        if(userProperties!=null && userProperties.containsKey(SynchroUserPropertiesUtil.LEGAL_USER) && userProperties.get(SynchroUserPropertiesUtil.LEGAL_USER).equals("1"))       
        {
        	return true;
        }
        	
        return false;
       
    }
    
    public static boolean legaUserTypeCheck(Project project){
    	 Map<Integer, String> userEM = SynchroGlobal.getEndMarkets(getEffectiveUser()); 
    	 
    	 // For Global and Regional projects it will be on the basis of End Market values 
    	 if(project.getProjectType()!=null && (project.getProjectType().intValue()==1 || project.getProjectType().intValue()==2))
    	 {
    		 boolean flag = false;
    		 List<Long> endMarketIDs = getSynchroProjectManagerNew().getEndMarketIDs(project.getProjectID());
    		 for(Long eId:endMarketIDs)
    		 {
    			 if(userEM!=null && userEM.containsKey(eId.intValue()))
    			 {
    				 flag = true;
    			 }
    		 }
    		 return flag;
    	 }
    	 else
    	 {
	    	 Integer budgetLocation = project.getBudgetLocation();
			 if(userEM!=null && userEM.containsKey(budgetLocation))
			 {
				 return true;
			 }
			 else
			 {
				 return false;
			 }
    	 }
		 //return false;
    }
    
    public static String getSystemAdminEmail()
    {
       return JiveGlobals.getJiveProperty("system.adminuser.email", "assistance@batinsights.com");
    }
    
    public static String getSystemAdminName()
    {
       return JiveGlobals.getJiveProperty("system.adminuser.name", "System Administrator");
    }
    
    /**
     * This method is used for User Type Edit Rights as per new Synhro Requirements
     * @return
     */
    public static boolean isUserTypeEditRights()
    {
        final User user = getEffectiveUser();
        Map<String,String> userProperties = user.getProperties();
        if(userProperties!=null && userProperties.containsKey(SynchroUserPropertiesUtil.EDIT_RIGHT_USER) && userProperties.get(SynchroUserPropertiesUtil.EDIT_RIGHT_USER).equals("1"))       
        {
        	return true;
        }
        	
        return false;
       
    }
    
    /**
     * This method is used for User Type TPD Summary Manage Rights as per new Sycnhro Requirements
     * @return
     */
    public static boolean canViewTPDSubmission()
    {
        final User user = getEffectiveUser();
        Map<String,String> userProperties = user.getProperties();
        if(userProperties!=null && userProperties.containsKey(SynchroUserPropertiesUtil.TPD_USER) && userProperties.get(SynchroUserPropertiesUtil.TPD_USER).equals("1"))       
        {
        	return true;
        }
        	
        return false;
       
    }
    
    public static boolean canEditTPDSummary()
    {
        final User user = getEffectiveUser();
        
        if(isSynchroAdmin(user))
		{
    		return true;
		}
        
        if(isSynchroSystemOwner())
		{
    		return true;
		}
        
        Map<String,String> userProperties = user.getProperties();
        if(userProperties!=null && userProperties.containsKey(SynchroUserPropertiesUtil.TPD_USER_MANAGING_RIGHTS) && userProperties.get(SynchroUserPropertiesUtil.TPD_USER_MANAGING_RIGHTS).equals("1"))       
        {
        	return true;
        }
        	
        return false;
       
    }
    /*
     * This method will check whether a user can Edit a project or not on the basis of Synchro New Requirements
     */
    public static boolean canEditProject(Project project)
    {
        final User user = getEffectiveUser();
        
        if(isSynchroAdmin(user))
		{
    		return true;
		}
        
        // http://redmine.nvish.com/redmine/issues/499 : System Owner will have the similar access as of Admin User.
        if(isSynchroSystemOwner())
		{
    		return true;
		}
        if(!isUserTypeEditRights())
        {
        	return false;
        }
        
            	
    	// Global Users can edit only Global projects but can view All Global, Regional and End Market Projects
        if(isGlobalUserType())
        {
        	if(project.getProjectType()==SynchroGlobal.ProjectType.GLOBAL.getId())
        	{
        		return true;
        	}
        	
        }
     
        // Regional Users can edit only Regional projects but can view Regional and End Market Projects correspond to their Regions
        if(isRegionalUserType())
        {
        	if(project.getProjectType()==SynchroGlobal.ProjectType.REGIONAL.getId())
        	{
        		List<Long> userRegions = SynchroUtils.getBudgetLocationRegions(user);
        		boolean flag=false;
        		if(userRegions!=null && userRegions.size()>0)
        		{
        			for(Long region : userRegions)
        			{
        				if(region.intValue()==project.getBudgetLocation())
        				{
        					flag=true;
        				}
        			}
        		}
        		if(flag)
        		{
        			return true;
        		}
        	}
        	
        }
        
        // End Market Users can edit only End projects correspond to their End Markets
        if(isEndMarketUserType())
        {
        	if(project.getProjectType()==SynchroGlobal.ProjectType.ENDMARKET.getId())
        	{
        		// Only the users with End Market Access can edit their projects
        		// http://redmine.nvish.com/redmine/issues/448
        		
        		//List<Long> userRegionsEM = SynchroUtils.getBudgetLocationRegionsEndMarkets(user);
        		List<Long> userEM = SynchroUtils.getBudgetLocationEndMarkets(user);
        		List<Long> allEM = new ArrayList<Long>();
        		//allEM.addAll(userRegionsEM);
        		allEM.addAll(userEM);
        		
        		boolean flag=false;
        		
        		if(allEM!=null && allEM.size()>0)
        		{
        			for(Long em : allEM)
        			{
        				if(em.intValue()==project.getBudgetLocation())
        				{
        					flag=true;
        				}
        			}
        		}
        		if(flag)
        		{
        			return true;
        		}
        		
        	
        	}
        	
        }
    	return false;
    }

    /*
     * This method will fetch the latest Status for TPD Summary Stage
     */
    public static Integer getLatestTPDStatus(Long projectId)
    {
    	List<ProjectInitiation> pibList = getPibManagerNew().getPIBDetailsNew(projectId);
    	List<ProposalInitiation> proposalList = getProposalManagerNew().getProposalInitiationNew(projectId);
    	
    	Project project = getSynchroProjectManagerNew().get(projectId);
    	
    	if(proposalList!=null && proposalList.size()>0 && project.getStatus().intValue() > SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
    	{
    		//If Proposal Send For Approval has been sent and the approval is not received, then till that time the TPD Status should be Pending
    		if(proposalList.get(0).getSendForApproval()!=null && proposalList.get(0).getSendForApproval().intValue() == 1)
    		{
    			return -1;
    		}
    		if(proposalList.get(0).getLegalApprovalStatus()!=null && proposalList.get(0).getLegalApprovalStatus()>0)
    		{
    			// This is done in case Exception is selected on the Proposal stage then TPD Status should be Pending
    			if(proposalList.get(0).getLegalSignOffRequired()!=null && proposalList.get(0).getLegalSignOffRequired().intValue()==1)
    			{
    				return -1;
    			}
    			return proposalList.get(0).getLegalApprovalStatus();
    		}
    		
    		// This is done for the cases for which the status in brief stage is saved but not saved in proposal stage
    		if(pibList!=null && pibList.size()>0 && project.getStatus().intValue() >= SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
        	{
        		if(pibList.get(0).getLegalApprovalStatus()!=null && pibList.get(0).getLegalApprovalStatus()>0)
        		{
        			// This is done in case Exception is selected on the Brief stage then TPD Status should be Pending
        			if(pibList.get(0).getLegalSignOffRequired()!=null && pibList.get(0).getLegalSignOffRequired().intValue()==1)
        			{
        				return -1;
        			}
        			return pibList.get(0).getLegalApprovalStatus();
        		}
        	}
    	}
    	//else if(pibList!=null && pibList.size()>0 && project.getStatus().intValue() == SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
    	else if(pibList!=null && pibList.size()>0 && project.getStatus().intValue() >= SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
    	{
    		if(pibList.get(0).getLegalApprovalStatus()!=null && pibList.get(0).getLegalApprovalStatus()>0)
    		{
    			// This is done in case Exception is selected on the Brief stage then TPD Status should be Pending
    			if(pibList.get(0).getLegalSignOffRequired()!=null && pibList.get(0).getLegalSignOffRequired().intValue()==1)
    			{
    				return -1;
    			}
    			return pibList.get(0).getLegalApprovalStatus();
    		}
    	}
    	return -1;
    }
    
    /*
     * This method will fetch the latest Legal Approver Name for TPD Summary Stage
     */
    public static String getLatestTPDLegalApprover(Long projectId)
    {
    	List<ProjectInitiation> pibList = getPibManagerNew().getPIBDetailsNew(projectId);
    	List<ProposalInitiation> proposalList = getProposalManagerNew().getProposalInitiationNew(projectId);
    	
    	Project project = getSynchroProjectManagerNew().get(projectId);
    	
    	String legalApproverName="";
    	
    	if(proposalList!=null && proposalList.size()>0 && project.getStatus().intValue() > SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
    	{
    		
    		if(project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId())
    		{
    			if(proposalList.get(0).getProposalLegalApprover()!=null && proposalList.get(0).getProposalLegalApprover()>0)
        		{
    				try
    				{
    					legalApproverName = getUserManager().getUser(proposalList.get(0).getProposalLegalApprover()).getName();
    				}
    				catch(UserNotFoundException e)
    				{
    					
    				}
    				return legalApproverName;
    				
        		}
    		}
    		
    		if(project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId())
    		{
    			if(StringUtils.isNotBlank(proposalList.get(0).getProposalLegalApproverOffline()))
        		{
   					legalApproverName = proposalList.get(0).getProposalLegalApproverOffline();
        		}
    			return legalApproverName;
    		}
    		
    		
    		// This is done for the cases for which the status in brief stage is saved but not saved in proposal stage
    		if(pibList!=null && pibList.size()>0 && project.getStatus().intValue() >= SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
        	{
    			if(project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId())
        		{
        			if(pibList.get(0).getBriefLegalApprover()!=null && pibList.get(0).getBriefLegalApprover()>0)
            		{
        				try
        				{
        					legalApproverName = getUserManager().getUser(pibList.get(0).getBriefLegalApprover()).getName();
        				}
        				catch(UserNotFoundException e)
        				{
        					
        				}
        				return legalApproverName;
            		}
        		}
        		
        		if(project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId())
        		{
        			if(StringUtils.isNotBlank(pibList.get(0).getBriefLegalApproverOffline()))
            		{
       					legalApproverName = pibList.get(0).getBriefLegalApproverOffline();
            		}
        			return legalApproverName;
        		}
        	}
    	}
    	//else if(pibList!=null && pibList.size()>0 && project.getStatus().intValue() == SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
    	else if(pibList!=null && pibList.size()>0 && project.getStatus().intValue() >= SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
    	{

			if(project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId())
    		{
    			if(pibList.get(0).getBriefLegalApprover()!=null && pibList.get(0).getBriefLegalApprover()>0)
        		{
    				try
    				{
    					legalApproverName = getUserManager().getUser(pibList.get(0).getBriefLegalApprover()).getName();
    				}
    				catch(UserNotFoundException e)
    				{
    					
    				}
    				return legalApproverName;
        		}
    		}
    		
    		if(project.getProcessType()==SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId() || project.getProcessType()==SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId())
    		{
    			if(StringUtils.isNotBlank(pibList.get(0).getBriefLegalApproverOffline()))
        		{
   					legalApproverName = pibList.get(0).getBriefLegalApproverOffline();
        		}
    			return legalApproverName;
    		}
    	
    	}
    	return legalApproverName;
    }
    
    /*
     * This method will fetch the latest Legal Approval Date for TPD Summary Stage
     */
    public static Date getLatestTPDLegalApprovalDate(Long projectId)
    {
    	List<ProjectInitiation> pibList = getPibManagerNew().getPIBDetailsNew(projectId);
    	List<ProposalInitiation> proposalList = getProposalManagerNew().getProposalInitiationNew(projectId);
    	
    	Project project = getSynchroProjectManagerNew().get(projectId);
    	
    	if(proposalList!=null && proposalList.size()>0 && project.getStatus().intValue() > SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
    	{
    		if(proposalList.get(0).getLegalApprovalStatus()!=null && proposalList.get(0).getLegalApprovalStatus()>0)
    		{
    			if(proposalList.get(0).getLegalApprovalDate()!=null )
        		{
        			return proposalList.get(0).getLegalApprovalDate();
        		}
    			else
    			{
    				return null;
    			}
    		}
    			
    		
    		// This is done for the cases for which the status in brief stage is saved but not saved in proposal stage
    		if(pibList!=null && pibList.size()>0 && project.getStatus().intValue() >= SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
        	{
    			if(pibList.get(0).getLegalApprovalStatus()!=null && pibList.get(0).getLegalApprovalStatus()>0)
        		{
        			if(pibList.get(0).getLegalApprovalDate()!=null )
            		{
            			return pibList.get(0).getLegalApprovalDate();
            		}
        			else
        			{
        				return null;
        			}
        		}
    			
        	}
    	}
    	//else if(pibList!=null && pibList.size()>0 && project.getStatus().intValue() == SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
    	else if(pibList!=null && pibList.size()>0 && project.getStatus().intValue() >= SynchroGlobal.ProjectStatusNew.BRIEF_IN_PLANNING.ordinal())
    	{
    		if(pibList.get(0).getLegalApprovalDate()!=null )
    		{
    			return pibList.get(0).getLegalApprovalDate();
    		}
    	}
    	return null;
    }
    
    /**
     * This method is for checking the Budget Location Exception for End Market Non EU projects
     * @param projectId
     * @return
     */
    public static boolean budgetYearException(Long projectId)
    {
    	Project project = getSynchroProjectManagerNew().get(projectId);
    	boolean budgetYearException = false;
    	if(project.getProjectType()==SynchroGlobal.ProjectType.ENDMARKET.getId() && project.getProcessType() == SynchroGlobal.ProjectProcessType.END_MARKET_NON_EU.getId())
    	{
    		String exceptionBL = JiveGlobals.getJiveProperty("synchro.exception.budgetLocations");
    		
    		if(exceptionBL!=null && exceptionBL.contains(","))
    		{
    			String[] splitBL = exceptionBL.split(",");
    			for(int i=0; i<splitBL.length; i++)
    			{
    				Integer bl = new Integer(splitBL[i]);
    				if(project.getBudgetLocation().intValue()==bl.intValue())
    				{
    					budgetYearException = true;
    				}
    			}
    		}
    		else
    		{
    			Integer bl = new Integer(exceptionBL);
				if(project.getBudgetLocation().intValue()==bl.intValue())
				{
					budgetYearException = true;
				}
    		}
    		
    		
    	}
    	return budgetYearException;
    }
    
    /**
     * This method will check whether the Research Agency is Active or not
     * @param methodologyId
     * @return
     */
    public static boolean isActiveResearchAgency(Long projectId){
        
    	boolean activeResearchAgency = false;
    	
    	List<ProjectCostDetailsBean> pcdList = getSynchroProjectManagerNew().getProjectCostDetails(projectId);
    	if(pcdList!=null && pcdList.size()>0)
    	{
    		for(ProjectCostDetailsBean pcb : pcdList)
    		{
    			if(SynchroGlobal.getResearchAgency().containsKey(pcb.getAgencyId().intValue()))
    			{
    				
    			}
    			else
    			{
    				return false;
    			}
    		}
    	}
    	
    	return true;
    	
    }
    
    /**
     * This method will check whether the Methodology Details is Active or not
     * @param projectId
     * @return
     */
    public static boolean isActiveMethodologyDetails(List<Long> methodologyDetails){
        
    	boolean activeMethDetails = false;
    	
    	
    	if(methodologyDetails!=null && methodologyDetails.size()>0)
    	{
    		for(Long methId : methodologyDetails)
    		{
    			LOG.info("isActiveMethodologyDetails methId ==> "+methId);
				LOG.debug("isActiveMethodologyDetails methId ==> "+methId);
				
    			if(SynchroGlobal.getMethodologies().containsKey(methId.intValue()))
    			{
    				LOG.info("isActiveMethodologyDetails Contains MethId  ==> "+methId);
    				LOG.debug("isActiveMethodologyDetails Contains MethId ==> "+methId);
    			}
    			else
    			{
    				LOG.info("isActiveMethodologyDetails return false");
    				LOG.debug("isActiveMethodologyDetails return false");
    				return false;
    			}
    		}
    	}
    	
    	LOG.info("isActiveMethodologyDetails return true");
		LOG.debug("isActiveMethodologyDetails return true");
    	return true;
    	
    }
    
    public static boolean canAccessOSPOraclePortal(final User user) {
        if(isSynchroAdmin(user) || isOSPOracleUser(user)) {
            return true;
        }

        return false;
    }
    
    public static boolean isOSPOracleUser(final User user) {
        final List<User> ospOracleGroupUsers = getSynchroUtils().getOSPOracleUsers();
        if(isSynchroAdmin(user))
            return true;
        if(ospOracleGroupUsers != null){
            return ospOracleGroupUsers.contains(user);
        }
        return false;
    }
    
    public static boolean canEditOSPOraclePortal(final User user) {
        if(isSynchroAdmin(user)) {
            return true;
        }
        
        if(isOSPOracleUser(user))
        {
        	Map<String,String> userProperties = user.getProperties();
            if(userProperties!=null && userProperties.containsKey(SynchroUserPropertiesUtil.EDIT_OSP_ORACLE) && userProperties.get(SynchroUserPropertiesUtil.EDIT_OSP_ORACLE).equals("1"))       
            {
            	return true;
            }
            	
            return false;
        }

        return false;
    }
    
    public static boolean canAccessOSPSharePortal(final User user) {
        if(isSynchroAdmin(user) || isOSPShareUser(user)) {
            return true;
        }

        return false;
    }
    
    public static boolean isOSPShareUser(final User user) {
        final List<User> ospShareGroupUsers = getSynchroUtils().getOSPShareUsers();
        if(isSynchroAdmin(user))
            return true;
        if(ospShareGroupUsers != null){
            return ospShareGroupUsers.contains(user);
        }
        return false;
    }
    
    public static boolean canEditOSPSharePortal(final User user) {
        if(isSynchroAdmin(user)) {
            return true;
        }
        
        if(isOSPShareUser(user))
        {
        	Map<String,String> userProperties = user.getProperties();
            if(userProperties!=null && userProperties.containsKey(SynchroUserPropertiesUtil.EDIT_OSP_SHARE) && userProperties.get(SynchroUserPropertiesUtil.EDIT_OSP_SHARE).equals("1"))       
            {
            	return true;
            }
            	
            return false;
        }

        return false;
    }
}
