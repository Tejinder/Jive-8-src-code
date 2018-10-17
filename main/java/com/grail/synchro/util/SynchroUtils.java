package com.grail.synchro.util;


import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.springframework.validation.ObjectError;
import org.subethamail.smtp.util.EmailUtils;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Joiner;
import com.grail.kantar.manager.KantarReportManager;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.Currency;
import com.grail.synchro.beans.CurrencyExchangeRate;
import com.grail.synchro.beans.EndMarketField;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.MetaField;
import com.grail.synchro.beans.MetaFieldMapping;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsEndMarketDetails;
import com.grail.synchro.beans.ProposalEndMarketDetails;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.UserDepartment;
import com.grail.synchro.manager.PIBManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProjectSpecsManager;
import com.grail.synchro.manager.ProposalManager;
import com.grail.synchro.manager.ReportSummaryManager;
import com.grail.synchro.manager.SynchroMetaFieldManager;
import com.grail.synchro.manager.UserDepartmentsManager;
import com.jivesoftware.base.Group;
import com.jivesoftware.base.GroupManager;
import com.jivesoftware.base.GroupMemberResultFilter;
import com.jivesoftware.base.GroupNotFoundException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.UserTemplate;
import com.jivesoftware.community.CommunityManager;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.entitlements.SystemPermissionManager;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.user.profile.ProfileFieldValue;
import com.jivesoftware.community.user.profile.ProfileManager;
import com.jivesoftware.community.util.JiveContainerPermHelper;
import com.jivesoftware.util.DateUtils;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * Synchro Utility class that does helps in looking up the Jive UserGroup.
 *
 * @author: Vivek Kondur
 *
 */
public class SynchroUtils {

    private static final Logger LOG = Logger.getLogger(SynchroUtils.class);

    //TODO Inject ProjectManager once we have the implementation

    private static Map<String, List<User>> synchroUserGroupsMap = new ConcurrentHashMap<String, List<User>>();

    private CommunityManager communityManager;

    private static GroupManager groupManager = null;

    private ProjectManager synchroProjectManager;

    private static ProfileManager profileManager;

    private static SynchroMetaFieldManager synchroMetaFieldManager;

    private static UserManager userManager = null;

    private PIBManager pibManager;
    private ProposalManager proposalManager;

    //    private static UserManager sUserManager;
//    private static GroupManager sGroupManager;
    private ReportSummaryManager reportSummaryManager;

    private ProjectSpecsManager projectSpecsManager;

    private static ProjectManager sSynchroProjectManager;

    private SystemPermissionManager systemPermissionManager;

    private static KantarReportManager kantarReportManager;


    public static ProjectManager getsSynchroProjectManager() {

        if(sSynchroProjectManager==null)
        {
            sSynchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return sSynchroProjectManager;
    }

    public void setProjectSpecsManager(ProjectSpecsManager projectSpecsManager) {
        this.projectSpecsManager = projectSpecsManager;
    }


    public static GroupManager getGroupManager() {
        return groupManager;
    }

    public void setGroupManager(final GroupManager groupManager) {
        SynchroUtils.groupManager = groupManager;
    }

    public SystemPermissionManager getSystemPermissionManager() {
        if(systemPermissionManager == null) {
            systemPermissionManager = JiveApplication.getContext().getSpringBean("systemPermissionManager");
        }
        return systemPermissionManager;
    }

    public static UserManager getUserManager() {
        return userManager;

    }

    public void setUserManager(final UserManager userManager) {
        SynchroUtils.userManager = userManager;
    }

//    public void setUserManager(final UserManager userManager) {
//        SynchroUtils.userManager = userManager;
//    }

    public static SynchroMetaFieldManager getSynchroMetaFieldManager() {
        if(synchroMetaFieldManager==null)
        {
            synchroMetaFieldManager = JiveApplication.getContext().getSpringBean("synchroMetaFieldManager");
        }
        return synchroMetaFieldManager;

    }

    public static KantarReportManager getKantarReportManager() {

        if(kantarReportManager==null)
        {
            kantarReportManager = JiveApplication.getContext().getSpringBean("kantarReportManager");
        }
        return kantarReportManager;
    }

    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    public void setCommunityManager(final CommunityManager communityManager) {
        this.communityManager = communityManager;
    }


    /**
     * Initialize all the global synchro user groups. The map acts as a cache, instead of making calls to groupManager.
     */
    protected void init(){
        updateSynchroUserGroupMap();
    }


    public void updateSynchroUserGroupMap() {
        synchroUserGroupsMap.put(SynchroConstants.JIVE_SYNCHRO_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_SYNCHRO_GROUP_NAME));
        //synchroUserGroupsMap.put(SynchroConstants.JIVE_MARKETING_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_MARKETING_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME));

        //synchroUserGroupsMap.put(SynchroConstants.JIVE_SPI_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_SPI_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME));

        //synchroUserGroupsMap.put(SynchroConstants.JIVE_LEGAL_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_LEGAL_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_GLOBAL_LEGAL_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_LEGAL_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_END_MARKET_LEGAL_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_LEGAL_APPROVERS_GROUP_NAME));

        //synchroUserGroupsMap.put(SynchroConstants.JIVE_PROCUREMENT_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_PROCUREMENT_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_GLOBAL_PROCUREMENT_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_PROCUREMENT_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_END_MARKET_PROCUREMENT_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_PROCUREMENT_APPROVERS_GROUP_NAME));

        synchroUserGroupsMap.put(SynchroConstants.JIVE_COAGENCY_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_COAGENCY_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_COAGENCY_SUPPORT_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_COAGENCY_SUPPORT_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_FIELDWORK_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_FIELDWORK_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_COMMUNICATION_AGECNY_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_COMMUNICATION_AGECNY_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_ORACLE_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_ORACLE_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_METHODOLOGY_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_METHODOLOGY_APPROVERS_GROUP_NAME));

        synchroUserGroupsMap.put(SynchroConstants.JIVE_KANTAR_METHODOLOGY_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_KANTAR_METHODOLOGY_APPROVERS_GROUP_NAME));

        //synchroUserGroupsMap.put(SynchroConstants.JIVE_OTHER_BAT_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_OTHER_BAT_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME));

        synchroUserGroupsMap.put(SynchroConstants.JIVE_GLOBAL_SUPPORT_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_SUPPORT_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_REGIONAL_SUPPORT_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_SUPPORT_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_END_MARKET_SUPPORT_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_SUPPORT_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_SYNCHRO_MINI_ADMIN_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_SYNCHRO_MINI_ADMIN_GROUP_NAME));

        synchroUserGroupsMap.put(SynchroConstants.JIVE_PROCESS_WAIVER_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_PROCESS_WAIVER_APPROVERS_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.JIVE_COMMUNICATION_AGENCY_ADMIN_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_COMMUNICATION_AGENCY_ADMIN_GROUP_NAME));
        synchroUserGroupsMap.put(SynchroConstants.SYNCHRO_SYSTEM_OWNER_GROUP_NAME, getJiveGroupMembers(SynchroConstants.SYNCHRO_SYSTEM_OWNER_GROUP_NAME));
        
        synchroUserGroupsMap.put(SynchroConstants.OSP_ORACLE_GROUP_NAME, getJiveGroupMembers(SynchroConstants.OSP_ORACLE_GROUP_NAME));
        
        synchroUserGroupsMap.put(SynchroConstants.OSP_SHARE_GROUP_NAME, getJiveGroupMembers(SynchroConstants.OSP_SHARE_GROUP_NAME));
        
    }

    public Map<String, List<User>> getSynchroUserGroupsMap() {
        return synchroUserGroupsMap;
    }

    public List<User> getSynchroGroupUsers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_SYNCHRO_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_SYNCHRO_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_SYNCHRO_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_SYNCHRO_GROUP_NAME);
    }


    public List<User> getKantarAgencyGroupUsers() {
        if(!synchroUserGroupsMap.containsKey(SynchroConstants.JIVE_KANTAR_AGENCY_GROUP_NAME) || synchroUserGroupsMap.get(SynchroConstants.JIVE_KANTAR_AGENCY_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_KANTAR_AGENCY_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_KANTAR_AGENCY_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_KANTAR_AGENCY_GROUP_NAME);

    }

    public List<User> getDocumentRepositoryAgencyGroupUsers() {
        if(!synchroUserGroupsMap.containsKey(SynchroConstants.JIVE_DOCUMENT_REPOSITORY_AGENCY_GROUP_NAME) || synchroUserGroupsMap.get(SynchroConstants.JIVE_DOCUMENT_REPOSITORY_AGENCY_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_DOCUMENT_REPOSITORY_AGENCY_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_DOCUMENT_REPOSITORY_AGENCY_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_DOCUMENT_REPOSITORY_AGENCY_GROUP_NAME);

    }

    public List<User> getDocumentRepositoryBATGroupUsers() {
        if(!synchroUserGroupsMap.containsKey(SynchroConstants.JIVE_DOCUMENT_REPOSITORY_BAT_GROUP_NAME) || synchroUserGroupsMap.get(SynchroConstants.JIVE_DOCUMENT_REPOSITORY_BAT_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_DOCUMENT_REPOSITORY_BAT_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_DOCUMENT_REPOSITORY_BAT_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_DOCUMENT_REPOSITORY_BAT_GROUP_NAME);

    }

    public List<User> getSynchroOracleApprovers() {
        List<User> oracleUsers = Collections.emptyList();
        if(synchroUserGroupsMap.containsKey(SynchroConstants.JIVE_ORACLE_APPROVERS_GROUP_NAME)){
            oracleUsers = synchroUserGroupsMap.get(SynchroConstants.JIVE_ORACLE_APPROVERS_GROUP_NAME);
        }
        return oracleUsers;
    }

    public List<User> getSynchroMethodologyApprovers() {
        List<User> methodologyUsers = Collections.emptyList();
        if(synchroUserGroupsMap.containsKey(SynchroConstants.JIVE_METHODOLOGY_APPROVERS_GROUP_NAME)){
            methodologyUsers = synchroUserGroupsMap.get(SynchroConstants.JIVE_METHODOLOGY_APPROVERS_GROUP_NAME);
        }
        return methodologyUsers;
    }

    public List<User> getSynchroKantarMethodologyApprovers() {
        List<User> methodologyUsers = Collections.emptyList();
        if(synchroUserGroupsMap.containsKey(SynchroConstants.JIVE_KANTAR_METHODOLOGY_APPROVERS_GROUP_NAME)){
            methodologyUsers = synchroUserGroupsMap.get(SynchroConstants.JIVE_KANTAR_METHODOLOGY_APPROVERS_GROUP_NAME);
        }
        return methodologyUsers;
    }

    /**
     * This will return the Process Waiver Approvers
     * @return
     */
    public List<User> getSynchroProcessWaiverApprovers() {
        List<User> processWaiverUsers = Collections.emptyList();
        if(synchroUserGroupsMap.containsKey(SynchroConstants.JIVE_PROCESS_WAIVER_APPROVERS_GROUP_NAME)){
            processWaiverUsers = synchroUserGroupsMap.get(SynchroConstants.JIVE_PROCESS_WAIVER_APPROVERS_GROUP_NAME);
        }
        return processWaiverUsers;
    }

    /**
     * This will return the Kantar Waiver Approvers
     * @return
     */
    public List<User> getSynchroKantarWaiverApprovers() {
        List<User> kantarWaiverUsers = Collections.emptyList();
        if(synchroUserGroupsMap.containsKey(SynchroConstants.JIVE_KANTAR_METHODOLOGY_APPROVERS_GROUP_NAME)){
            kantarWaiverUsers = synchroUserGroupsMap.get(SynchroConstants.JIVE_KANTAR_METHODOLOGY_APPROVERS_GROUP_NAME);
        }
        return kantarWaiverUsers;
    }

    /**
     * This method will return the Mini Admin Users.
     * @return
     */
    public List<User> getSynchroMiniAdminUsers() {
        List<User> miniAdminUsers = Collections.emptyList();
        if(synchroUserGroupsMap.containsKey(SynchroConstants.JIVE_SYNCHRO_MINI_ADMIN_GROUP_NAME)){
            miniAdminUsers = synchroUserGroupsMap.get(SynchroConstants.JIVE_SYNCHRO_MINI_ADMIN_GROUP_NAME);
        }
        return miniAdminUsers;
    }
    /* public List<User> getMarketingGroupApprovers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_MARKETING_APPROVERS_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_MARKETING_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_MARKETING_APPROVERS_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_MARKETING_APPROVERS_GROUP_NAME);
    }*/
    /**
     * This method will fetch all the Marketing Approvers users
     * @return
     */
    public List<User> getMarketingGroupApprovers(){
        List<User> user = new ArrayList<User>();
        user.addAll(getGlobalMarketingGroupApprovers());
        user.addAll(getRegionalMarketingGroupApprovers());
        user.addAll(getEndMarketMarketingGroupApprovers());
        return user;
    }
    public List<User> getMarketingGroupApprovers_NEW(){
        List<User> user = new ArrayList<User>();
        user.addAll(getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME));
        user.addAll(getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME));
        user.addAll(getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME));
        return user;
    }
    public List<User> getGlobalMarketingGroupApprovers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME);
    }

    public List<User> getRegionalMarketingGroupApprovers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME);
    }
    public List<User> getEndMarketMarketingGroupApprovers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME);
    }

    /* public List<User> getOtherBATApprovers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_OTHER_BAT_APPROVERS_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_OTHER_BAT_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_OTHER_BAT_APPROVERS_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_OTHER_BAT_APPROVERS_GROUP_NAME);
    }*/
    /**
     * This method will fetch all the Other BAT Approvers Users
     * @return
     */
    public List<User> getOtherBATApprovers(){
        List<User> user = new ArrayList<User>();
        user.addAll(getGlobalOtherBATApprovers());
        user.addAll(getRegionalOtherBATApprovers());
        user.addAll(getEndMarketOtherBATApprovers());
        return user;
    }

    public List<User> getOtherBATApprovers_NEW(){
        List<User> user = new ArrayList<User>();
        user.addAll(getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME));
        user.addAll(getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME));
        user.addAll(getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME));
        return user;
    }
    public List<User> getGlobalOtherBATApprovers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME);
    }
    public List<User> getRegionalOtherBATApprovers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME);
    }
    public List<User> getEndMarketOtherBATApprovers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME);
    }

    /* public List<User> getSPIGroupApprovers(){
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_SPI_APPROVERS_GROUP_NAME);
    }*/

    /**
     * This method will fetch all the SPI Approvers
     * @return
     */
    public List<User> getSPIGroupApprovers(){
        List<User> user = new ArrayList<User>();
        user.addAll(getGlobalSPIGroupApprovers());
        user.addAll(getRegionalSPIGroupApprovers());
        user.addAll(getEndMarketSPIGroupApprovers());
        return user;
    }
    public List<User> getSPIGroupApprovers_NEW(){
        List<User> user = new ArrayList<User>();
        user.addAll(getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME));
        user.addAll(getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME));
        user.addAll(getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME));
        return user;
    }
    public List<User> getGlobalSPIGroupApprovers(){
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME);
    }
    public List<User> getRegionalSPIGroupApprovers(){
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME);
    }
    public List<User> getEndMarketSPIGroupApprovers(){
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME);
    }

    public List<User> getProjectOwnerUsers() {
        List<User> users = new ArrayList<User>();

        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_SPI_APPROVERS_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME));

        // BAT GROUP
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_OTHER_BAT_APPROVERS_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME));

        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_SUPPORT_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_SUPPORT_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_SUPPORT_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_SUPPORT_GROUP_NAME));

        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_MARKETING_APPROVERS_GROUP_NAME));

        return users;
    }

    public List<User> getSPIGroupUsers() {
        List<User> users = new ArrayList<User>();
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_SPI_APPROVERS_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME));
        return users;
    }

    public List<User> getAgencyGroupUsers() {
        List<User> users = new ArrayList<User>();
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_COAGENCY_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_FIELDWORK_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_COAGENCY_SUPPORT_GROUP_NAME));
        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_COMMUNICATION_AGENCY_ADMIN_GROUP_NAME));
        return users;
    }

    public List<UserDepartment> getAgencyNames() {
        List<UserDepartment> departments = new ArrayList<UserDepartment>();
        UserDepartmentsManager userDepartmentsManager = JiveApplication.getContext().getSpringBean("userDepartmentsManager");
        if(userDepartmentsManager != null) {
            departments = userDepartmentsManager.getAll();
        }
        return departments;
    }

    /* public List<User> getLegalGroupApprovers(){
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_LEGAL_APPROVERS_GROUP_NAME);
    }
    */
    /**
     * This method will fetch all the Legal Approver Users
     * @return
     */
    public List<User> getLegalGroupApprovers(){
        List<User> user = new ArrayList<User>();
        user.addAll(getGlobalLegalGroupApprovers());
        user.addAll(getEndMarketLegalGroupApprovers());
        return user;
    }
    public List<User> getGlobalLegalGroupApprovers(){
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_GLOBAL_LEGAL_APPROVERS_GROUP_NAME);
    }
    public List<User> getEndMarketLegalGroupApprovers(){
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_END_MARKET_LEGAL_APPROVERS_GROUP_NAME);
    }

    /* public List<User> getProcurementGroupApprovers() {
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_PROCUREMENT_APPROVERS_GROUP_NAME);
    }*/
    /**
     * This method will fetch all the Procurement Approvers
     * @return
     */
    public List<User> getProcurementGroupApprovers(){
        List<User> user = new ArrayList<User>();
        user.addAll(getGlobalProcurementGroupApprovers());
        user.addAll(getEndMarketProcurementGroupApprovers());
        return user;
    }
    public List<User> getGlobalProcurementGroupApprovers() {
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_GLOBAL_PROCUREMENT_APPROVERS_GROUP_NAME);
    }
    public List<User> getEndMarketProcurementGroupApprovers() {
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_END_MARKET_PROCUREMENT_APPROVERS_GROUP_NAME);
    }

    public List<User> getCoordinationGroupAgencyUsers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_COAGENCY_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_COAGENCY_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_COAGENCY_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_COAGENCY_GROUP_NAME);
    }

    public List<User> getCoordinationAgencySupportGroupUsers() {
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_COAGENCY_SUPPORT_GROUP_NAME);
    }

    public List<User> getFieldWorkAgencyGroupUsers() {
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_FIELDWORK_GROUP_NAME);
    }

    public List<User> getCommunicationAgencyGroupUsers() {
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_COMMUNICATION_AGECNY_GROUP_NAME);
    }

    public List<User> getCommunicationAgencyAdminGroupUsers() {
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_COMMUNICATION_AGENCY_ADMIN_GROUP_NAME);
    }

    public List<User> getSupportGroupUsers(){
        List<User> user = new ArrayList<User>();
        user.addAll(getGlobalSupportUsers());
        user.addAll(getRegionalSupportUsers());
        user.addAll(getEndMarketSupportUsers());
        return user;
    }
    public List<User> getSupportGroupUsers_NEW(){
        List<User> user = new ArrayList<User>();
        user.addAll(getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_SUPPORT_GROUP_NAME));
        user.addAll(getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_SUPPORT_GROUP_NAME));
        user.addAll(getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_SUPPORT_GROUP_NAME));
        return user;
    }
    public List<User> getGlobalSupportUsers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_GLOBAL_SUPPORT_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_GLOBAL_SUPPORT_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_GLOBAL_SUPPORT_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_GLOBAL_SUPPORT_GROUP_NAME);
    }
    public List<User> getRegionalSupportUsers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_REGIONAL_SUPPORT_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_REGIONAL_SUPPORT_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_REGIONAL_SUPPORT_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_REGIONAL_SUPPORT_GROUP_NAME);
    }
    public List<User> getEndMarketSupportUsers() {
        if(synchroUserGroupsMap.get(SynchroConstants.JIVE_END_MARKET_SUPPORT_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.JIVE_END_MARKET_SUPPORT_GROUP_NAME, getJiveGroupMembers(SynchroConstants.JIVE_END_MARKET_SUPPORT_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.JIVE_END_MARKET_SUPPORT_GROUP_NAME);
    }

    
    public List<User> getSynchroSystemOwnerUsers() {
        if(synchroUserGroupsMap.get(SynchroConstants.SYNCHRO_SYSTEM_OWNER_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.SYNCHRO_SYSTEM_OWNER_GROUP_NAME, getJiveGroupMembers(SynchroConstants.SYNCHRO_SYSTEM_OWNER_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.SYNCHRO_SYSTEM_OWNER_GROUP_NAME);
    }
    
    public static String getSynchroSystemOwnerName()
    {
     	 if(synchroUserGroupsMap!=null && synchroUserGroupsMap.size()>0 && synchroUserGroupsMap.get(SynchroConstants.SYNCHRO_SYSTEM_OWNER_GROUP_NAME)!=null && synchroUserGroupsMap.get(SynchroConstants.SYNCHRO_SYSTEM_OWNER_GROUP_NAME).size()>0)
    	 {
    		return synchroUserGroupsMap.get(SynchroConstants.SYNCHRO_SYSTEM_OWNER_GROUP_NAME).get(0).getName();
    	 }
    	 return "";
    	 
    }

    /**
     * Returns the Users who have been chosen as Stakeholders in the Project Wizard process.
     * Note: The Jive UserGroupID related to all the Stakeholders need to be persisted as Community Extended property
     * @param projectID - Long ProjectID
     * @return List of Users
     */
    public List<User> getProjectStakeHolders(final long projectID) {

        List<User> stakeHolders = new ArrayList<User>();
        Long ownerID = getSynchroProjectManager().get(projectID).getProjectOwner();
        try{
            //TODO Kanwar
            if(!SynchroUtils.isReferenceID(ownerID))
                stakeHolders.add(getUserManager().getUser(ownerID));
        }catch(UserNotFoundException ue){ LOG.error("Not able to find project owner user while fetching stakeholders of projecr" + projectID, ue);}

        stakeHolders.addAll(getSPIUsers(projectID));

        List<Long> eids = getSynchroProjectManager().getEndMarketIDs(projectID);
        for(Long id : eids)
        {
            stakeHolders.addAll(getLegalUsers(projectID, id));
            stakeHolders.addAll(getProcurementUsers(projectID, id));
            stakeHolders.addAll(getExternalAgencyUsers(projectID, id));
            stakeHolders.addAll(getCommunicationAgencyUsers(projectID, id));
        }

        return stakeHolders;
    }

    /**
     * Project Stakeholders than can create project and are part of current project as stakeholder in Initiate a Project Wizard
     * @param projectID
     * @param user
     * @return
     */
    public List<User> getProjectStakeHoldersEligibleOwners(final long projectID, final User user) {

        try {
            final String projectGroupName = String.format(SynchroConstants.SYNCHRO_PROJECT_UG_NAME, projectID);
            final Group stakeHolderGroup = groupManager.getGroup(projectGroupName);
            List<User> stakeHolders = new ArrayList<User>(stakeHolderGroup.getMembers());
            List<User> eligibleStakeHolders = new ArrayList<User>(getMarketingGroupApprovers());
            eligibleStakeHolders.addAll(getOtherBATApprovers());
            eligibleStakeHolders.addAll(getSPIGroupApprovers());
            eligibleStakeHolders.addAll(getSPIGroupApprovers());

            List<User> supportUsers = getSupportGroupUsers();
            for(User sUser : supportUsers)
            {
                if(sUser.getID() == user.getID())
                {
                    eligibleStakeHolders.add(sUser);
                    break;
                }
            }

            // Returns all members irrespective of roles
            stakeHolders.addAll(stakeHolderGroup.getAdministrators());
            stakeHolders.retainAll(eligibleStakeHolders);
            return stakeHolders;
        } catch (GroupNotFoundException e) {
            LOG.error("Unable to load Stakeholder JiveUserGroup for the project  " + projectID, e);
        }
        return Collections.emptyList();
    }

    /**
     * Project Stakeholders than can create project and are part of current project as stakeholder in Project Administration
     * @param projectID
     * @param user
     * @return
     */
    public List<User> getProjectStakeHoldersEligibleOwnersAdministration(final long projectID, final User user) {

        try {
            final String projectGroupName = String.format(SynchroConstants.SYNCHRO_PROJECT_UG_NAME, projectID);
            final Group stakeHolderGroup = groupManager.getGroup(projectGroupName);
            List<User> stakeHolders = new ArrayList<User>(stakeHolderGroup.getMembers());
            List<User> eligibleStakeHolders = new ArrayList<User>(getMarketingGroupApprovers());
            eligibleStakeHolders.addAll(getOtherBATApprovers());
            eligibleStakeHolders.addAll(getSPIGroupApprovers());
            eligibleStakeHolders.addAll(getSPIGroupApprovers());

            List<User> supportUsers = getSupportGroupUsers();
            try{/*
            	Long creatorID = getSynchroProjectManager().getProjectCreator(projectID);            	
                for(User sUser : supportUsers)
                {
                	if(sUser.getID() == creatorID)
                	{
                		eligibleStakeHolders.add(sUser);
                		break;
                	}
                }
            */}catch(Exception e){LOG.error("Error while fetching Project creator in populating owners list in Administration console");}


            // Returns all members irrespective of roles
            stakeHolders.addAll(stakeHolderGroup.getAdministrators());
            stakeHolders.retainAll(eligibleStakeHolders);
            return stakeHolders;
        } catch (GroupNotFoundException e) {
            LOG.error("Unable to load Stakeholder JiveUserGroup for the project  " + projectID, e);
        }
        return Collections.emptyList();
    }


    /**
     * Returns all Marketing Users for the specified project (Global + Regional + End Market)
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getMarketUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> allMarketingUsers = getMarketingGroupApprovers();
        final Collection<User> projectMarketingUsers = new HashSet<User>( allMarketingUsers );
        projectMarketingUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n All-Marketing-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",allMarketingUsers,stakeHolders,projectMarketingUsers));
        return projectMarketingUsers;
    }

    /**
     * Returns all Global Marketing Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getGlobalMarketUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> globalMarketingUsers = getGlobalMarketingGroupApprovers();
        final Collection<User> projectMarketingUsers = new HashSet<User>( globalMarketingUsers );
        projectMarketingUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n Global-Marketing-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",globalMarketingUsers,stakeHolders,projectMarketingUsers));
        return projectMarketingUsers;
    }

    public Collection<User> getProjectMarketUsers(final List<User> users) {
        final List<User> globalMarketingUsers = getMarketingGroupApprovers();
        final Collection<User> projectMarketingUsers = new HashSet<User>( globalMarketingUsers );
        projectMarketingUsers.retainAll(users);
        LOG.debug(String.format("%n%n Global-Marketing-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",globalMarketingUsers,users,projectMarketingUsers));
        return projectMarketingUsers;
    }

    public Collection<User> getProjectGLMarketUsers(final List<User> users) {
        final List<User> marketingUsers = getGlobalMarketingGroupApprovers();
        final Collection<User> projectMarketingUsers = new HashSet<User>( marketingUsers );
        projectMarketingUsers.retainAll(users);
        LOG.debug(String.format("%n%n Global-Marketing-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",projectMarketingUsers,users,projectMarketingUsers));
        return projectMarketingUsers;
    }

    public Collection<User> getProjectRGMarketUsers(final List<User> users) {
        final List<User> marketingUsers = getRegionalMarketingGroupApprovers();
        final Collection<User> projectMarketingUsers = new HashSet<User>( marketingUsers );
        projectMarketingUsers.retainAll(users);
        LOG.debug(String.format("%n%n Global-Marketing-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",projectMarketingUsers,users,projectMarketingUsers));
        return projectMarketingUsers;
    }

    public Collection<User> getProjectEMMarketUsers(final List<User> users) {
        final List<User> marketingUsers = getEndMarketMarketingGroupApprovers();
        final Collection<User> projectMarketingUsers = new HashSet<User>( marketingUsers );
        projectMarketingUsers.retainAll(users);
        LOG.debug(String.format("%n%n Global-Marketing-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",projectMarketingUsers,users,projectMarketingUsers));
        return projectMarketingUsers;
    }

    public Collection<User> getProjectOtherBATUsers(final List<User> users) {
        final List<User> allOtherBATUsers = getOtherBATApprovers();
        final Collection<User> projectOtherBATUsers = new HashSet<User>( allOtherBATUsers );
        projectOtherBATUsers.retainAll(users);
        LOG.debug(String.format("%n%n All-OtherBAT-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",allOtherBATUsers,users,projectOtherBATUsers));
        return projectOtherBATUsers;
    }

    public Collection<User> getProjectGLOtherBATUsers(final List<User> users) {
        final List<User> allOtherBATUsers = getGlobalOtherBATApprovers();
        final Collection<User> projectOtherBATUsers = new HashSet<User>( allOtherBATUsers );
        projectOtherBATUsers.retainAll(users);
        LOG.debug(String.format("%n%n All-OtherBAT-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",allOtherBATUsers,users,projectOtherBATUsers));
        return projectOtherBATUsers;
    }

    public Collection<User> getProjectRGOtherBATUsers(final List<User> users) {
        final List<User> allOtherBATUsers = getRegionalOtherBATApprovers();
        final Collection<User> projectOtherBATUsers = new HashSet<User>( allOtherBATUsers );
        projectOtherBATUsers.retainAll(users);
        LOG.debug(String.format("%n%n All-OtherBAT-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",allOtherBATUsers,users,projectOtherBATUsers));
        return projectOtherBATUsers;
    }

    public Collection<User> getProjectEMOtherBATUsers(final List<User> users) {
        final List<User> allOtherBATUsers = getEndMarketOtherBATApprovers();
        final Collection<User> projectOtherBATUsers = new HashSet<User>( allOtherBATUsers );
        projectOtherBATUsers.retainAll(users);
        LOG.debug(String.format("%n%n All-OtherBAT-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",allOtherBATUsers,users,projectOtherBATUsers));
        return projectOtherBATUsers;
    }

    public Collection<User> getProjectSPIUsers(final List<User> users) {
        final List<User> allSPIApprovers = getSPIGroupApprovers();
        final Collection<User> projectSPIUsers = new HashSet<User>( allSPIApprovers );
        projectSPIUsers.retainAll(users);
        LOG.debug(String.format("%n%n All-SPI-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",allSPIApprovers,users,projectSPIUsers));
        return projectSPIUsers;
    }

    public Collection<User> getProjectGLSPIUsers(final List<User> users) {
        final List<User> allSPIApprovers = getGlobalSPIGroupApprovers();
        final Collection<User> projectSPIUsers = new HashSet<User>( allSPIApprovers );
        projectSPIUsers.retainAll(users);
        LOG.debug(String.format("%n%n All-SPI-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",allSPIApprovers,users,projectSPIUsers));
        return projectSPIUsers;
    }

    public Collection<User> getProjectRGSPIUsers(final List<User> users) {
        final List<User> allSPIApprovers = getRegionalSPIGroupApprovers();
        final Collection<User> projectSPIUsers = new HashSet<User>( allSPIApprovers );
        projectSPIUsers.retainAll(users);
        LOG.debug(String.format("%n%n All-SPI-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",allSPIApprovers,users,projectSPIUsers));
        return projectSPIUsers;
    }

    public Collection<User> getProjectEMSPIUsers(final List<User> users) {
        final List<User> allSPIApprovers = getEndMarketSPIGroupApprovers();
        final Collection<User> projectSPIUsers = new HashSet<User>( allSPIApprovers );
        projectSPIUsers.retainAll(users);
        LOG.debug(String.format("%n%n All-SPI-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",allSPIApprovers,users,projectSPIUsers));
        return projectSPIUsers;
    }

    public Collection<User> getProjectLegalUsers(final List<User> users) {
        final List<User> allLegalApprovers = getLegalGroupApprovers();
        final Collection<User> projectLegalUsers = new HashSet<User>( allLegalApprovers );
        projectLegalUsers.retainAll(users);
        LOG.debug(String.format("%n%n All-Legal-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",allLegalApprovers,users,projectLegalUsers));
        return projectLegalUsers;
    }

    public Collection<User> getProjectGlobalLegalUsers(final List<User> users) {
        final List<User> globalLegalApprovers = getGlobalLegalGroupApprovers();
        final Collection<User> projectLegalUsers = new HashSet<User>( globalLegalApprovers );
        projectLegalUsers.retainAll(users);
        LOG.debug(String.format("%n%n Global-Legal-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",globalLegalApprovers,users,projectLegalUsers));
        return projectLegalUsers;
    }

    public Collection<User> getProjectEndMarketLegalUsers(final List<User> users) {
        final List<User> endMarketLegalApprovers = getEndMarketLegalGroupApprovers();
        final Collection<User> projectLegalUsers = new HashSet<User>( endMarketLegalApprovers );
        projectLegalUsers.retainAll(users);
        LOG.debug(String.format("%n%n End-Market-Legal-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",endMarketLegalApprovers,users,projectLegalUsers));
        return projectLegalUsers;
    }

    /**
     * Returns all Regional Marketing Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getRegionalMarketUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> regionalMarketingUsers = getRegionalMarketingGroupApprovers();
        final Collection<User> projectMarketingUsers = new HashSet<User>( regionalMarketingUsers );
        projectMarketingUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n Regional-Marketing-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",regionalMarketingUsers,stakeHolders,projectMarketingUsers));
        return projectMarketingUsers;
    }

    /**
     * Returns all End Market Marketing Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getEndMarketMarketingUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> endMarketMarketingUsers = getEndMarketMarketingGroupApprovers();
        final Collection<User> projectMarketingUsers = new HashSet<User>( endMarketMarketingUsers );
        projectMarketingUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n End-Market-Marketing-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",endMarketMarketingUsers,stakeHolders,projectMarketingUsers));
        return projectMarketingUsers;
    }

    /**
     * Returns all Other BAT Users for the specified project (Global + Regional + End Market)
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getOtherBATUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> allOtherBATUsers = getOtherBATApprovers();
        final Collection<User> projectOtherBATUsers = new HashSet<User>( allOtherBATUsers );
        projectOtherBATUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n All-OtherBAT-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",allOtherBATUsers,stakeHolders,projectOtherBATUsers));
        return projectOtherBATUsers;
    }

    /**
     * Returns Global Other BAT Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getGlobalOtherBATUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> globalOtherBATUsers = getGlobalOtherBATApprovers();
        final Collection<User> projectOtherBATUsers = new HashSet<User>( globalOtherBATUsers );
        projectOtherBATUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n Global-OtherBAT-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",globalOtherBATUsers,stakeHolders,projectOtherBATUsers));
        return projectOtherBATUsers;
    }
    /**
     * Returns Regional Other BAT Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getRegionalOtherBATUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> regionalOtherBATUsers = getRegionalOtherBATApprovers();
        final Collection<User> projectOtherBATUsers = new HashSet<User>( regionalOtherBATUsers );
        projectOtherBATUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n Regional-OtherBAT-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",regionalOtherBATUsers,stakeHolders,projectOtherBATUsers));
        return projectOtherBATUsers;
    }
    /**
     * Returns End Market Other BAT Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getEndMarketOtherBATUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> endMarketOtherBATUsers = getEndMarketOtherBATApprovers();
        final Collection<User> projectOtherBATUsers = new HashSet<User>( endMarketOtherBATUsers );
        projectOtherBATUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n End-Market-OtherBAT-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",endMarketOtherBATUsers,stakeHolders,projectOtherBATUsers));
        return projectOtherBATUsers;
    }
    public Collection<User> getSPIUsers(final long projectID) {
        List<EndMarketInvestmentDetail> endMarketDetails = synchroProjectManager.getEndMarketDetails(projectID);
        Collection<User> projectSPIUsers = new HashSet<User>();
        for(EndMarketInvestmentDetail emd:endMarketDetails)
        {
            try
            {
                projectSPIUsers.add(getUserManager().getUser(emd.getSpiContact()));
            }
            catch(UserNotFoundException e)
            {
                LOG.error("User Not found while fetching in SPI Contact "+ emd.getSpiContact());
            }
        }
        return projectSPIUsers;
    }



    /**
     * Returns Global SPI Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getGlobalSPIUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> globalSPIApprovers = getGlobalSPIGroupApprovers();
        final Collection<User> projectSPIUsers = new HashSet<User>( globalSPIApprovers );
        projectSPIUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n Global-SPI-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",globalSPIApprovers,stakeHolders,projectSPIUsers));
        return projectSPIUsers;
    }
    /**
     * Returns Regional SPI Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getRegionalSPIUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> regionalSPIApprovers = getRegionalSPIGroupApprovers();
        final Collection<User> projectSPIUsers = new HashSet<User>( regionalSPIApprovers );
        projectSPIUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n Regional-SPI-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",regionalSPIApprovers,stakeHolders,projectSPIUsers));
        return projectSPIUsers;
    }
    /**
     * Returns End Market SPI Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getEndMarketSPIUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> endMarketSPIApprovers = getEndMarketSPIGroupApprovers();
        final Collection<User> projectSPIUsers = new HashSet<User>( endMarketSPIApprovers );
        projectSPIUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n End-Market-SPI-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",endMarketSPIApprovers,stakeHolders,projectSPIUsers));
        return projectSPIUsers;
    }

    /**
     * Returns all Legal Users for the specified project (Global + End Market)
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getLegalUsers(final long projectID, final long endMarketID) {
        /*final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> allLegalApprovers = getLegalGroupApprovers();
        final Collection<User> projectLegalUsers = new HashSet<User>( allLegalApprovers );
        projectLegalUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n All-Legal-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",allLegalApprovers,stakeHolders,projectLegalUsers));
        return projectLegalUsers;*/

        PIBStakeholderList pibStakeholders = pibManager.getPIBStakeholderList(projectID, endMarketID);
        Collection<User> projectLegalUsers = new HashSet<User>();
        try
        {
            if(pibStakeholders!=null)
            {
                projectLegalUsers.add(getUserManager().getUser(pibStakeholders.getGlobalLegalContact()));
            }
        }
        catch(UserNotFoundException e)
        {
            LOG.error("User Not found while fetching in Legal Contact "+ pibStakeholders.getGlobalLegalContact());
        }
        return projectLegalUsers;
    }

    /**
     * Returns Global Legal Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getGlobalLegalUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> globalLegalApprovers = getGlobalLegalGroupApprovers();
        final Collection<User> projectLegalUsers = new HashSet<User>( globalLegalApprovers );
        projectLegalUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n Global-Legal-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",globalLegalApprovers,stakeHolders,projectLegalUsers));
        return projectLegalUsers;
    }
    /**
     * Returns End Market Legal Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getEndMarketLegalUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> endMarketLegalApprovers = getEndMarketLegalGroupApprovers();
        final Collection<User> projectLegalUsers = new HashSet<User>( endMarketLegalApprovers );
        projectLegalUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n End-Market-Legal-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",endMarketLegalApprovers,stakeHolders,projectLegalUsers));
        return projectLegalUsers;
    }

    /* Returns all Procurement Users for the specified project (Global + End Market)
    * @param projectID - Long - ProjectID
    * @return List of Users
    */
    public Collection<User> getProcurementUsers(final long projectID, final long endMarketID) {
        PIBStakeholderList pibStakeholders = pibManager.getPIBStakeholderList(projectID, endMarketID);
        Collection<User> projectProcurementUsers = new HashSet<User>();
        try
        {
            if(pibStakeholders!=null)
            {
                projectProcurementUsers.add(getUserManager().getUser(pibStakeholders.getGlobalProcurementContact()));
            }
        }
        catch(UserNotFoundException e)
        {
            LOG.error("User Not found while fetching Procurement Contact "+ pibStakeholders.getGlobalProcurementContact());
        }
        return projectProcurementUsers;
    }
    /**
     * Returns all External Agency Users for the specified project (CoordinationAgency + CoAgencySupport + Fieldwork Agency)
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getExternalAgencyUsers(final long projectID, final long endMarketID) {
        //PIBStakeholderList pibStakeholders = endMarketID > 0?pibManager.getPIBStakeholderList(projectID, endMarketID):pibManager.getPIBStakeholderList(projectID);
        PIBStakeholderList pibStakeholders = pibManager.getPIBStakeholderList(projectID, endMarketID);
        Collection<User> externalAgecnyUsers = new HashSet<User>();
        if(pibStakeholders!=null)
        {
            try
            {
                if(pibStakeholders.getAgencyContact1()!=null && pibStakeholders.getAgencyContact1()>0)
                {
                    externalAgecnyUsers.add(getUserManager().getUser(pibStakeholders.getAgencyContact1()));
                }
                if(pibStakeholders.getAgencyContact1Optional()!=null && pibStakeholders.getAgencyContact1Optional()>0)
                {
                    externalAgecnyUsers.add(getUserManager().getUser(pibStakeholders.getAgencyContact1Optional()));
                }
                if(pibStakeholders.getAgencyContact2()!=null && pibStakeholders.getAgencyContact2()>0)
                {
                    externalAgecnyUsers.add(getUserManager().getUser(pibStakeholders.getAgencyContact2()));
                }
                if(pibStakeholders.getAgencyContact2Optional()!=null && pibStakeholders.getAgencyContact2Optional()>0)
                {
                    externalAgecnyUsers.add(getUserManager().getUser(pibStakeholders.getAgencyContact2Optional()));
                }
                if(pibStakeholders.getAgencyContact3()!=null && pibStakeholders.getAgencyContact3()>0)
                {
                    externalAgecnyUsers.add(getUserManager().getUser(pibStakeholders.getAgencyContact3()));
                }

                if(pibStakeholders.getAgencyContact3Optional()!=null && pibStakeholders.getAgencyContact3Optional()>0)
                {
                    externalAgecnyUsers.add(getUserManager().getUser(pibStakeholders.getAgencyContact3Optional()));
                }
            }
            catch(UserNotFoundException e)
            {
                LOG.error("User Not found while fetching Agency Contact ");
            }
        }
        return externalAgecnyUsers;
    }
    /**
     * Returns the agency who is awarded the proposal
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getAwardedExternalAgencyUsers(final long projectID, final long endMarketID) {
        List<ProposalInitiation> proposalInitiationList = proposalManager.getProposalDetails(projectID);
        Collection<User> awardedExAgecnyUsers = new HashSet<User>();
        if(proposalInitiationList!=null && proposalInitiationList.size()>0)
        {
            try
            {
                for(ProposalInitiation pi: proposalInitiationList)
                {
                    if(pi.getIsAwarded())
                    {
                        awardedExAgecnyUsers.add(getUserManager().getUser(pi.getAgencyID()));
                        // https://www.svn.sourcen.com/issues/18872 - This is done so that the optional Agency user can also be considered as 
                        // Awarded Agency user

                        //PIBStakeholderList pibStakeholders = pibManager.getPIBStakeholderList(projectID, endMarketID);
                        // This is done to handle the scenario when the end market is changed on Proposal tab.
                        PIBStakeholderList pibStakeholders = null;
                        Project project = synchroProjectManager.get(projectID);
                        if(project.getMultiMarket())
                        {
                            pibStakeholders = pibManager.getPIBStakeholderList(projectID, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
                        }
                        else
                        {
                            pibStakeholders = pibManager.getPIBStakeholderList(projectID);
                        }

                        if(pibStakeholders.getAgencyContact1()==pi.getAgencyID() && pibStakeholders.getAgencyContact1Optional()!=null && pibStakeholders.getAgencyContact1Optional()>0)
                        {
                            awardedExAgecnyUsers.add(getUserManager().getUser(pibStakeholders.getAgencyContact1Optional()));
                        }
                        if(pibStakeholders.getAgencyContact2()==pi.getAgencyID() && pibStakeholders.getAgencyContact2Optional()!=null && pibStakeholders.getAgencyContact2Optional()>0)
                        {
                            awardedExAgecnyUsers.add(getUserManager().getUser(pibStakeholders.getAgencyContact2Optional()));
                        }
                        if(pibStakeholders.getAgencyContact3()==pi.getAgencyID() && pibStakeholders.getAgencyContact3Optional()!=null && pibStakeholders.getAgencyContact3Optional()>0)
                        {
                            awardedExAgecnyUsers.add(getUserManager().getUser(pibStakeholders.getAgencyContact3Optional()));
                        }
                    }
                }
            }
            catch(UserNotFoundException e)
            {
                LOG.error("User Not found while fetching Awarded External Agency Contact ");
            }
        }
        return awardedExAgecnyUsers;
    }

    /**
     * This method will return the Awarded External Agency ID
     * @param projectID
     * @param endMarketID
     * @return
     */
    public Long getAwardedExternalAgencyUserID(final long projectID, final long endMarketID) {
        List<ProposalInitiation> proposalInitiationList = proposalManager.getProposalDetails(projectID);

        if(proposalInitiationList!=null && proposalInitiationList.size()>0)
        {
            for(ProposalInitiation pi: proposalInitiationList)
            {
                if(pi.getIsAwarded())
                {
                    return pi.getAgencyID();

                }
            }
        }
        return null;
    }
    /**
     /**
     * Returns Global Procurement Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getGlobalProcurementUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> globalProcurementApprovers = getGlobalProcurementGroupApprovers();
        final Collection<User> projectProcurementUsers = new HashSet<User>( globalProcurementApprovers );
        projectProcurementUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n Global-Procurement-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",globalProcurementApprovers,stakeHolders,projectProcurementUsers));
        return projectProcurementUsers;
    }

    /**
     * Returns End Market Procurement Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getEndMarketProcurementUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> endMarketProcurementApprovers = getEndMarketProcurementGroupApprovers();
        final Collection<User> projectProcurementUsers = new HashSet<User>( endMarketProcurementApprovers );
        projectProcurementUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n End-Market-Procurement-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",endMarketProcurementApprovers,stakeHolders,projectProcurementUsers));
        return projectProcurementUsers;
    }

    /**
     * Returns all Co-ordination Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getCoAgencyUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> allCoordinationUsers = getCoordinationGroupAgencyUsers();
        final Collection<User> projectCoordUsers = new HashSet<User>( allCoordinationUsers );
        projectCoordUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n All-Coordination-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",allCoordinationUsers,stakeHolders,projectCoordUsers));
        return projectCoordUsers;
    }

    /**
     * Returns all Co-ordination Agency Support Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getCoAgencySupportUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> allSupportUser = getCoordinationAgencySupportGroupUsers();
        final Collection<User> projectSupportUsers = new HashSet<User>( allSupportUser );
        projectSupportUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n All-Coordination-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",allSupportUser,stakeHolders,projectSupportUsers));
        return projectSupportUsers;
    }

    /**
     * Returns all FieldWork Agency Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getFieldWorkAgencyUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> allFWUsers = getFieldWorkAgencyGroupUsers();
        final Collection<User> projectFWUsers = new HashSet<User>( allFWUsers );
        projectFWUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n All-Coordination-Approvers:%s%n%nStakeHolders:%s%n%nSPI-Users:%s%n%n",allFWUsers,stakeHolders,projectFWUsers));
        return projectFWUsers;
    }


    /* Returns all Communication Agency Users for the specified project
    * @param projectID - Long - ProjectID
    * @return List of Users
    */
    public Collection<User> getCommunicationAgencyUsers(final long projectID,final long endMarketID) {
        PIBStakeholderList pibStakeholders = pibManager.getPIBStakeholderList(projectID, endMarketID);
        Collection<User> communicationAgecnyUsers = new HashSet<User>();
        try
        {
            if(pibStakeholders!=null && pibStakeholders.getGlobalCommunicationAgency()!=null && pibStakeholders.getGlobalCommunicationAgency()>0)
            {
                communicationAgecnyUsers.add(getUserManager().getUser(pibStakeholders.getGlobalCommunicationAgency()));
            }
        }
        catch(UserNotFoundException e)
        {
            LOG.error("User Not found while fetching Communication Agency Contact "+ pibStakeholders.getGlobalCommunicationAgency());
        }
        return communicationAgecnyUsers;
    }

    /**
     * Returns all Support Users for the specified project (Global + Regional + End Market)
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getSupportUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> allSupportUsers = getSupportGroupUsers();
        final Collection<User> projectSupportUsers = new HashSet<User>( allSupportUsers );
        projectSupportUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n All-Support Users:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",allSupportUsers,stakeHolders,projectSupportUsers));
        return projectSupportUsers;
    }

    /**
     * Returns all Global Support Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getGlobalSupportUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> globalSupportUsers = getGlobalSupportUsers();
        final Collection<User> projectGlobalUsers = new HashSet<User>( globalSupportUsers );
        projectGlobalUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n Global-Support-Approvers:%s%n%nStakeHolders:%s%n%nProject-Marketing-Users:%s%n%n",globalSupportUsers,stakeHolders,projectGlobalUsers));
        return projectGlobalUsers;
    }

    /**
     * Returns all Regional Support Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getRegionalSupportUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> regionalSupportUsers = getRegionalSupportUsers();
        final Collection<User> projectRegionalUsers = new HashSet<User>( regionalSupportUsers );
        projectRegionalUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n Regional-Support-Approvers:%s%n%nStakeHolders:%s%n%nProject-Regional Support-Users:%s%n%n",regionalSupportUsers,stakeHolders,projectRegionalUsers));
        return projectRegionalUsers;
    }

    /**
     * Returns all End Market Support Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getEndMarketSupportUsers(final long projectID) {
        final List<User> stakeHolders = getProjectStakeHolders(projectID);
        final List<User> endMarketSupportUsers = getEndMarketSupportUsers();
        final Collection<User> projectEndMarketUsers = new HashSet<User>( endMarketSupportUsers );
        projectEndMarketUsers.retainAll(stakeHolders);
        LOG.debug(String.format("%n%n End-Market-Support-Approvers:%s%n%nStakeHolders:%s%n%nProject-End Market Support-Users:%s%n%n",endMarketSupportUsers,stakeHolders,projectEndMarketUsers));
        return projectEndMarketUsers;
    }

    /**
     * Returns a List of UserIDs for a Collection of Jive Users
     * @param users Collection of Jive Users
     * @return List of UserIDs
     */
    public static final List<Long> getJiveUserIds(final Collection<User> users) {
        List<Long> userIDList = new ArrayList<Long>();
        if( users != null && users.size() > 0){
            for(User user : users){
                userIDList.add(user.getID());
            }
        }
        return userIDList;
    }

    /**
     * Helper method which returns Jive UserGroup for the specified Group Name
     * @param groupName - String - Jive UserGroup name
     * @return Group
     */
    public final Group getJiveGroup(final String groupName){
        Group jiveGroup = null;
        try {
            jiveGroup = this.groupManager.getGroup(groupName);
        } catch (GroupNotFoundException e) {
            LOG.error("Undefined Jive Group "+groupName);
        }
        return jiveGroup;

    }

    /**
     * Helper method which returns UserGroup members for the specified Group Name
     *
     * Note: We had to go via this approach as the DbGroup.getMembers() returns an unmodifiable list. It does support
     * add() / addAll() operations.
     *
     * @param groupName - String - Jive UserGroup name
     * @return List of Jive Users
     */
    /*public List<User> getJiveGroupMembers(final String groupName) {
        List<User> members = new ArrayList<User>();
        final GroupMemberResultFilter filter = new GroupMemberResultFilter();
        filter.setStartIndex(0);
        filter.setNumResults(getUserManager().getTotalUserCount() + 100);
        Group jiveGroup = null;
        try {
            jiveGroup = getGroupManager().getGroup(groupName);
            if( jiveGroup != null){
                filter.setGroupID(jiveGroup.getID());
                final Iterable<User> iterable = getGroupManager().getGroupMembers(filter);
                
                if(iterable!=null && iterable.iterator()!=null)
                {
                	
                	Iterator<User> userIter = iterable.iterator();
                	while(userIter.hasNext()){
                		try
                		{
                			User user = (User) userIter.next();
                		}
                		catch(E)
                	}
                	
                	while(iterable.iterator().hasNext())
                	{
                		
                	}
                }
                CollectionUtils.addAll(members, iterable.iterator());
                LOG.debug("Members of the Group  "+ jiveGroup.getName() +"  :::  "+members);
            }

        } catch (GroupNotFoundException e) {
            LOG.error("Undefined Jive Group. Synchro features will not work properly. Please configure - "+groupName);
            members = Collections.emptyList();
            e.printStackTrace();
        }
        return members;

    }*/

    public List<User> getJiveGroupMembers(final String groupName) {
        List<User> members = new ArrayList<User>();
        final GroupMemberResultFilter filter = new GroupMemberResultFilter();
        filter.setStartIndex(0);
        filter.setNumResults(getUserManager().getTotalUserCount() + 200);
        Group jiveGroup = null;
        try {
            jiveGroup = getGroupManager().getGroup(groupName);
            if( jiveGroup != null){
                filter.setGroupID(jiveGroup.getID());
                final Iterable<User> iterable = getGroupManager().getGroupMembers(filter);
                
                if(groupName.equalsIgnoreCase("END_MARKET_SPI_APPROVERS"))
                {
                	 LOG.error("USER COUNT FOR END_MARKET_SPI_APPROVERS ==>"+ getUserManager().getTotalUserCount());
					 if(iterable!=null)
                     {
                     	
                     	LOG.error("INSIDE END_MARKET_SPI_APPROVERS GROUP");
						Iterator<User> userIter = iterable.iterator();
                     	
                     	if(userIter!=null)
						{
							try
							{
								while(userIter.hasNext())
								{
									User user = null;
									try
									{
										user = (User) userIter.next();
									}
									catch(Exception e)
									{
										LOG.error("Excpetion for USER GROUP "+ jiveGroup.getName());
										e.printStackTrace();
									}
									catch(Throwable e)
									{
										LOG.error("Throwable Excpetion for USER GROUP "+ jiveGroup.getName());
										e.printStackTrace();
									}
									finally
									{
										//LOG.error("INSIDE END_MARKET_SPI_APPROVERS GROUP FINALLY BLOCK");
										if(user!=null)
										{
											LOG.error("END_MARKET_SPI_APPROVERS GROUP FINALLY BLOCK ==> User Id"+ user.getID());
											members.add(user);
										}
									}
									
								}
							}
							catch(Exception e)
							{
								LOG.error("Exception while iterating END_MARKET_SPI_APPROVERS GROUP");
								e.printStackTrace();
							}
						}
                     	
                     	
                     }
                }
                else
                {
                
	                if(iterable!=null)
					{
						CollectionUtils.addAll(members, iterable.iterator());
						LOG.debug("Members of the Group  "+ jiveGroup.getName() +"  :::  "+members);
					}
                }
            }

        } catch (GroupNotFoundException e) {
            LOG.error("Undefined Jive Group. Synchro features will not work properly. Please configure - "+groupName);
            members = Collections.emptyList();
        }
		catch (Exception e) {
            LOG.error("Undefined Jive Group Exception. Synchro features will not work properly. Please configure - "+groupName);
            members = Collections.emptyList();
			e.printStackTrace();
        }
		if(groupName.equalsIgnoreCase("END_MARKET_SPI_APPROVERS"))
        {
        	LOG.debug("Members of the Group  "+ jiveGroup.getName() +"  :::  "+members);
        	LOG.info("Members of the Group  "+ jiveGroup.getName() +"  :::  "+members);
        	LOG.error("Members of the Group  "+ jiveGroup.getName() +"  :::  "+members);
        	System.out.println("Members of the Group  "+ jiveGroup.getName() +"  :::  "+members);
			LOG.error("SIZE of Members of the Group  "+ jiveGroup.getName() +"  :::  "+members.size());
        	System.out.println(" SIZE of Members of the Group  "+ jiveGroup.getName() +"  :::  "+members.size());
        }
        return members;

    }

    public static void printBindingErrors(List<ObjectError> errors){
        for(ObjectError error: errors){
            LOG.info(error.toString());
        }
    }

    public static String getGraphBarColor(final String projectStatus) {
        return JiveGlobals.getJiveProperty(String.format(SynchroConstants.SYNCHRO_PROJECT_GRAPH_COLOR, projectStatus), SynchroConstants.PROJECT_DASHBOARD_PROGRESSBAR_DEFCOLOR);
    }

    /**
     * Returns Middle Year for Project Dashboard Screen in Gant view
     * @return
     */
    public static Integer getYearDashboard()
    {
        Calendar now = Calendar.getInstance();
        int currentyear = now.get(Calendar.YEAR);
//    	if(currentyear>2013)
//    		return currentyear;
//    	else
        return currentyear;
    }

    /**
     * Returns Current Yearly Quarter
     * @return
     */
    public static int getCurrentQuarter()
    {
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH)+1;

        String Q1 = "1,3";
        String Q2 = "4,6";
        String Q3 = "7,9";
        String Q4 = "10,12";
        String keyQ1 = String.format(SynchroConstants.SYNCHRO_PROJECT_QUARTER_LIST, "q1");
        String keyQ2 = String.format(SynchroConstants.SYNCHRO_PROJECT_QUARTER_LIST, "q2");
        String keyQ3 = String.format(SynchroConstants.SYNCHRO_PROJECT_QUARTER_LIST, "q3");
        String keyQ4 = String.format(SynchroConstants.SYNCHRO_PROJECT_QUARTER_LIST, "q4");
        String[] Q1Months = JiveGlobals.getJiveProperty(keyQ1,Q1).split(",");
        String[] Q2Months = JiveGlobals.getJiveProperty(keyQ2,Q2).split(",");
        String[] Q3Months = JiveGlobals.getJiveProperty(keyQ3,Q3).split(",");
        String[] Q4Months = JiveGlobals.getJiveProperty(keyQ4,Q4).split(",");
        try
        {
            if(currentMonth >= Integer.parseInt(Q1Months[0].trim()) && currentMonth <= Integer.parseInt(Q1Months[1].trim()))
            {
                return 1;
            }
            else if(currentMonth >= Integer.parseInt(Q2Months[0].trim()) && currentMonth <= Integer.parseInt(Q2Months[1].trim()))
            {
                return 2;
            }
            else if(currentMonth >= Integer.parseInt(Q3Months[0].trim()) && currentMonth <= Integer.parseInt(Q3Months[1].trim()))
            {
                return 3;
            }
            else if(currentMonth >= Integer.parseInt(Q4Months[0].trim()) && currentMonth <= Integer.parseInt(Q4Months[1].trim()))
            {
                return 4;
            }
        }catch(Exception e)
        {
            Log.error("Error occured while fetching System Properties for Quarter defining months. Please use format "+ "System Property for Q1 = \"1,3\";System Property for Q2 = \"4,6\";System Property for Q3 = \"7,9\";System Property for Q4 = \"10,12\""+e.getMessage());
            return 0;
        }
        return 0;
    }


    /**
     * Checks if user can Create Project
     * @param user
     * @return
     */
    public boolean canCreateProject(final User user){
        boolean canCreate = false;
        // If anonymous then return false

        if(!user.isAnonymous()){
            try {
                if(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin())
                    return true;
                final List<User> marketingApprovers = getMarketingGroupApprovers();
                final List<User> batApprovers = getOtherBATApprovers();
                final List<User> spiApprovers = getSPIGroupApprovers();
                final List<User> supportGroupUsers = getSupportGroupUsers();
                if(marketingApprovers != null){
                    canCreate = getJiveUserIds(marketingApprovers).contains(user.getID());
                }
                if(!canCreate && batApprovers != null){
                    canCreate = getJiveUserIds(batApprovers).contains(user.getID());
                }
                if(!canCreate && spiApprovers != null){
                    canCreate = getJiveUserIds(spiApprovers).contains(user.getID());
                }
                if(!canCreate && supportGroupUsers != null){
                    canCreate = getJiveUserIds(supportGroupUsers).contains(user.getID());
                }
            }catch (Exception e) {
                // do-nothing
                LOG.debug("## There is NO group by name associated with this user.", e);
            }
            if(!canCreate)
            {
                LOG.debug("## User " + user.getUsername() + " doesn't have access to this project.");
            }
        }
        return canCreate;
    }

    /**
     * Checks if user can Create Project by fetching from DB
     * @param user
     * @return
     */
    public boolean canCreateProject_NEW(final User user){
        boolean canCreate = false;
        // If anonymous then return false

        if(!user.isAnonymous()){
            try {
                if(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin())
                    return true;
                
                // Synchro System Owner can initiate New Project . http://redmine.nvish.com/redmine/issues/499
                if(SynchroPermHelper.isSynchroSystemOwner())
                {
                    return true;
                }
                
                // Legal User should not be able to initiate Project. http://redmine.nvish.com/redmine/issues/212
                if(SynchroPermHelper.isLegaUserType())
                {
                	return false;
                }
                if(SynchroPermHelper.isGlobalUserType())
                {
                	
                    // Only a user if having User Profile Edit rights will be able to initiate project
                	if(SynchroPermHelper.isUserTypeEditRights())
                    {
                    	return true;
                    }
                	
                }
                if(SynchroPermHelper.isRegionalUserType())
                {
                	if(SynchroPermHelper.isUserTypeEditRights())
                    {
                    	return true;
                    }
                }
                if(SynchroPermHelper.isEndMarketUserType())
                {
                	if(SynchroPermHelper.isUserTypeEditRights())
                    {
                    	return true;
                    }
                }
               
            }catch (Exception e) {
                // do-nothing
                LOG.debug("## There is NO group by name associated with this user.", e);
            }
            if(!canCreate)
            {
                LOG.debug("## User " + user.getUsername() + " doesn't have access to this project.");
            }
        }
        return canCreate;
    }
    /**
     * Checks if user can access Project Waivers 
     * @param user
     * @return
     */
    public boolean canAccessProjectWaiver(final User user) {
        boolean canAccess = false;
        // If anonymous then return false

        if(!user.isAnonymous()){
            try {
                if(JiveContainerPermHelper.isGlobalAdmin(user.getID()) || SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin())
                    return true;

                final List<User> marketingApprovers = getMarketingGroupApprovers();
                final List<User> batApprovers = getOtherBATApprovers();
                final List<User> spiApprovers = getSPIGroupApprovers();
                final List<User> supportGroupUsers = getSupportGroupUsers();
                final List<User> processWaiverGroupUsers = getSynchroProcessWaiverApprovers();
                final List<User> kantarWaiverGroupUsers = getSynchroKantarWaiverApprovers();

                if(marketingApprovers != null){
                    canAccess = getJiveUserIds(marketingApprovers).contains(user.getID());
                }
                if(!canAccess && batApprovers != null){
                    canAccess = getJiveUserIds(batApprovers).contains(user.getID());
                }
                if(!canAccess && spiApprovers != null){
                    canAccess = getJiveUserIds(spiApprovers).contains(user.getID());
                }
                if(!canAccess && supportGroupUsers != null){
                    canAccess = getJiveUserIds(supportGroupUsers).contains(user.getID());
                }
                if(!canAccess && processWaiverGroupUsers != null){
                    canAccess = getJiveUserIds(processWaiverGroupUsers).contains(user.getID());
                }
                if(!canAccess && kantarWaiverGroupUsers != null){
                    canAccess = getJiveUserIds(kantarWaiverGroupUsers).contains(user.getID());
                }

            }catch (Exception e) {
                // do-nothing
                LOG.debug("## There is NO group by name associated with this user.", e);
            }
            if(!canAccess)
            {
                LOG.debug("## User " + user.getUsername() + " doesn't have access to project waiver.");
            }
        }
        return canAccess;
    }

    /**
     * Checks if user can create new Waiver
     * @param user
     * @return
     */
    public boolean canIniateProjectWaiver(final User user) {
        boolean canAccess = false;
        // If anonymous then return false

        if(!user.isAnonymous()){
            try {
                if(JiveContainerPermHelper.isGlobalAdmin(user.getID()) || SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin())
                    return true;
                final List<User> marketingApprovers = getMarketingGroupApprovers();
                final List<User> batApprovers = getOtherBATApprovers();
                final List<User> spiApprovers = getSPIGroupApprovers();
                final List<User> supportGroupUsers = getSupportGroupUsers();

                if(marketingApprovers != null){
                    canAccess = getJiveUserIds(marketingApprovers).contains(user.getID());
                }
                if(!canAccess && batApprovers != null){
                    canAccess = getJiveUserIds(batApprovers).contains(user.getID());
                }
                if(!canAccess && spiApprovers != null){
                    canAccess = getJiveUserIds(spiApprovers).contains(user.getID());
                }
                if(!canAccess && supportGroupUsers != null){
                    canAccess = getJiveUserIds(supportGroupUsers).contains(user.getID());
                }

            }catch (Exception e) {
                // do-nothing
                LOG.debug("## There is NO group by name associated with this user.", e);
            }
            if(!canAccess)
            {
                LOG.debug("## User " + user.getUsername() + " doesn't have access to project waiver.");
            }
        }
        return canAccess;
    }

    /**
     * Returns Eligible list of Stakeholders that can initiate a waiver in Waiver Catalogue
     * @return
     */
    public List<User> getWaiverEligibleInitiators() {

        List<User> initiators = new ArrayList<User>();
        final List<User> marketingApprovers = getMarketingGroupApprovers();
        final List<User> batApprovers = getOtherBATApprovers();
        final List<User> spiApprovers = getSPIGroupApprovers();
        final List<User> supportGroupUsers = getSupportGroupUsers();
        initiators.addAll(marketingApprovers);
        initiators.addAll(batApprovers);
        initiators.addAll(spiApprovers);
        initiators.addAll(supportGroupUsers);

        return initiators;
    }


    /**
     * Checks if User can change Status of projects
     * @param user
     * @return
     */
    public boolean canChangeProjectStatus(final User user){
        boolean canCreate = false;
        // If anonymous then return false

        if(!user.isAnonymous()){
            try {
                if(JiveContainerPermHelper.isGlobalAdmin(user.getID()) || SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroMiniAdmin())
                    return true;
                final List<User> marketingApprovers = getMarketingGroupApprovers();
                final List<User> batApprovers = getOtherBATApprovers();
                final List<User> spiApprovers = getSPIGroupApprovers();
                if(marketingApprovers != null){
                    canCreate = getJiveUserIds(marketingApprovers).contains(user.getID());
                }
                if(!canCreate && batApprovers != null){
                    canCreate = getJiveUserIds(batApprovers).contains(user.getID());
                }
                if(!canCreate && spiApprovers != null){
                    canCreate = getJiveUserIds(spiApprovers).contains(user.getID());
                }
            }catch (Exception e) {
                // do-nothing
                LOG.debug("## There is NO group by name associated with this user.", e);
            }
            if(!canCreate)
            {
                LOG.debug("## User " + user.getUsername() + " doesn't have access to this project.");
            }
        }
        return canCreate;
    }

    /**
     * User - Group Map
     * @param marketingApprovers
     * @param groupPostfix
     * @return
     */
    public static Map<String,Object> getUserGroupMap(Collection<User> marketingApprovers, String groupPostfix)
    {
        Map<String,Object> userGroupMap = new HashMap<String,Object>();
        if(marketingApprovers != null && marketingApprovers.size() > 0 )
        {
            for(User user : marketingApprovers)
            {
                userGroupMap.put(String.valueOf(user.getID()), SynchroPermHelper.getGroupIDByGroupPostFix(groupPostfix, user));
            }
        }
        return userGroupMap;
    }

    /**
     * Get Unique User object List
     * @param users
     * @return
     */
    public static List<User> getUniqueUserIds(List<User> users)
    {
        List<Long> userIds = new ArrayList<Long>();
        List<User> userList = new ArrayList<User>();
        for(User user : users)
        {
            if(userIds!= null && !userIds.contains(user.getID()))
            {
                userIds.add(user.getID());
                userList.add(user);
            }
        }
        return userList;
    }

    /**
     * Converts String comma separated values to Array of Strings
     * @param values
     * @return
     */
    public static ArrayList<Integer> stringToArray(final String values)
    {
        String[] valuesArray = values.split(",");
        ArrayList<Integer> intList = new ArrayList<Integer>();
        for(int i = 0; i < valuesArray.length; i++) {
            intList.add(Integer.parseInt(valuesArray[i]));
        }
        return intList;
    }


    public static List<String>  getEndMarketsList(List<Long> endMarkets)
    {
        List<String> endMarketsList = new ArrayList<String>();
        for(Long endMarket : endMarkets)
        {
            if(!endMarketsList.contains(SynchroGlobal.getEndMarkets().get(Integer.parseInt(endMarket.toString()))))
            {
                endMarketsList.add(SynchroGlobal.getEndMarkets().get(Integer.parseInt(endMarket.toString())));
            }
        }
        return endMarketsList;
    }

    public static List<String>  getRegionsList(List<Long> endMarkets)
    {
        List<String> regionsList = new ArrayList<String>();
        Map<Integer, Map<Integer, String>> map = SynchroGlobal.getRegionEndMarketsMapping();
        for(Integer region : map.keySet())
        {

        }
        return regionsList;
    }

    public static List<String>  getUniqueSupplierList(List<Integer> fwSuppliers,  List<Integer> coSuppliers)
    {
        List<String> suppliersList = new ArrayList<String>();
        Set<Integer> uniqueSet = new HashSet<Integer>();
        if(fwSuppliers.size()>0)
            uniqueSet.addAll(fwSuppliers);
        if(coSuppliers.size()>0)
            uniqueSet.addAll(coSuppliers);
        uniqueSet.remove(null);
        for(Integer id : uniqueSet)
        {
            suppliersList.add(SynchroGlobal.getSupplierGroup().get(id));
        }
        return suppliersList;
    }
    /**
     * Returns all Synchro Admin Users
     * @return List of Users
     */
    public Collection<User> getSynchroAdminUsers() {

        //TODO - Check the name of the Synchro Admin Users
        final List<User> allAdminUsers = getJiveGroupMembers("admin");
        final Collection<User> synchroAdminUsers = new HashSet<User>( allAdminUsers );
        return synchroAdminUsers;
    }

    public static String getUserCountry(User user)
    {
        String country ="";
        Map<Long, ProfileFieldValue> userProfileMap = profileManager.getProfile(user);
        try{
            ProfileFieldValue fieldvalue = userProfileMap.get(new Long(3));
            String addressField = fieldvalue.getValue();
            for(String field : addressField.split(","))
            {
                if(StringUtils.startsWith(field, "country:"))
                {
                    country =  StringUtils.substringAfter(field, ":");
                }
            }
        }catch(Exception e){/*do nothing */}
        return country;
    }


    public ProjectManager getSynchroProjectManager() {
        if (synchroProjectManager == null)
        {
            synchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return synchroProjectManager;

    }

    public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
        this.synchroProjectManager = synchroProjectManager;
    }

    public static String getDocumentURL(final Long projectID, final int stageID)
    {
        switch(stageID)
        {
            case 1:
                return "/synchro/pib-details!input.jspa?projectID="+projectID;
            case 2:
                return "/synchro/proposal-details!input.jspa?projectID="+projectID;
            case 3:
                return "/synchro/project-specsinput.jspa?projectID="+projectID;
            case 4:
                return "/synchro/report-summary!input.jspa?projectID="+projectID;
            case 5:
                return "/synchro/project-eval!input.jspa?projectID="+projectID;

            default : 	return "/synchro/pib-details!input.jspa?projectID="+projectID;
        }
    }

    public static String getDocumentURLText(final int stageID)
    {
        switch(stageID)
        {
            case 1:
                return SynchroGlobal.SynchroAttachmentStage.PIB.toString();
            case 2:
                return SynchroGlobal.SynchroAttachmentStage.PROPOSAL.toString();
            case 3:
                return SynchroGlobal.SynchroAttachmentStage.PROJECT_SPECS.toString();
            case 4:
                return SynchroGlobal.SynchroAttachmentStage.REPORT_SUMMARY.toString();
            case 5:
                return SynchroGlobal.SynchroAttachmentStage.PROJECT_EVALUATION.toString();

            default : return SynchroGlobal.SynchroAttachmentStage.PIB.toString();
        }
    }

    /**
     * This method will fetch the user role for a particular user
     * @param user
     */
    public List<String> getUserRole(User user)
    {
        List<String> userRole = new ArrayList<String>();
        Iterable<Group> userGroups = groupManager.getUserGroups(user);
        while(userGroups.iterator().hasNext())
        {
            Group userGroup = userGroups.iterator().next();
            if(userGroup.getName().equals(SynchroConstants.JIVE_GLOBAL_MARKETING_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_REGIONAL_MARKETING_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_COAGENCY_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_GLOBAL_SPI_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_REGIONAL_SPI_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_END_MARKET_SPI_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_GLOBAL_LEGAL_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_END_MARKET_LEGAL_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_GLOBAL_PROCUREMENT_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_END_MARKET_PROCUREMENT_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_GLOBAL_OTHER_BAT_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_REGIONAL_OTHER_BAT_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_END_MARKET_OTHER_BAT_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_COAGENCY_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_COAGENCY_SUPPORT_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_FIELDWORK_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_COMMUNICATION_AGECNY_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_METHODOLOGY_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_KANTAR_METHODOLOGY_APPROVERS_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_GLOBAL_SUPPORT_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_REGIONAL_SUPPORT_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_END_MARKET_SUPPORT_GROUP_NAME)
                    || userGroup.getName().equals(SynchroConstants.JIVE_SYNCHRO_ADMIN_GROUP_NAME))
            {
                userRole.add(userGroup.getName());
            }
        }
        return userRole;
    }
    public static Double getExchangeRate(Integer currencyID)
    {
        Double exchangeRate = 1D;

        String currencyName = SynchroGlobal.getCurrencies().get(currencyID);
        String currencyKey = String.format(SynchroConstants.SYNCHRO_CURRENCY_VALUE, currencyName);
        String currencyValue = JiveGlobals.getJiveProperty(currencyKey.toLowerCase(), SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_VALUE.toString());
        Double currDoubleVal = Double.parseDouble(currencyValue);

        String exchangeName = SynchroGlobal.getCurrencies().get(SynchroConstants.EXCHANGE_RATE_CURRENCY_ID);
        String exchangeKey = String.format(SynchroConstants.SYNCHRO_CURRENCY_VALUE, exchangeName);
        String exchangeValue = JiveGlobals.getJiveProperty(exchangeKey.toLowerCase(), SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_VALUE.toString());
        Double exchangeDoubleVal = Double.parseDouble(exchangeValue);

        exchangeRate = currDoubleVal/exchangeDoubleVal;
        return (double) Math.round(exchangeRate * 100) / 100;
    }

    public static Double getCurrencyExchangeRate(Long currencyID) {
        String currencyName = SynchroGlobal.getCurrencies().get(currencyID.intValue());
        String currencyKey = String.format(SynchroConstants.SYNCHRO_CURRENCY_VALUE, currencyName);
        String currencyValue = JiveGlobals.getJiveProperty(currencyKey.toLowerCase(), SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_VALUE.toString());
        return Double.parseDouble(currencyValue);
    }

    public static Double getCurrencyExchangeRate(Integer currencyID) {
        String currencyName = SynchroGlobal.getCurrencies().get(currencyID.intValue());
        String currencyKey = String.format(SynchroConstants.SYNCHRO_CURRENCY_VALUE, currencyName);
        String currencyValue = JiveGlobals.getJiveProperty(currencyKey.toLowerCase(), SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_VALUE.toString());
        return Double.parseDouble(currencyValue);
    }

    public static String getCurrencyExchangeRateString(Integer currencyID) {
        String currencyName = SynchroGlobal.getCurrencies().get(currencyID.intValue());
        String currencyKey = String.format(SynchroConstants.SYNCHRO_CURRENCY_VALUE, currencyName);
        String currencyValue = JiveGlobals.getJiveProperty(currencyKey.toLowerCase(), SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_VALUE.toString());
        return currencyValue;
    }
    
    public static BigDecimal getCurrencyExchangeRateBD(Integer currencyID) {
        String currencyName = SynchroGlobal.getCurrencies().get(currencyID.intValue());
        String currencyKey = String.format(SynchroConstants.SYNCHRO_CURRENCY_VALUE, currencyName);
        String currencyValue = JiveGlobals.getJiveProperty(currencyKey.toLowerCase(), SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_VALUE.toString());
        BigDecimal bd = new BigDecimal(currencyValue);
        return bd;
    }
    
    public static int getQuarterByMonth(int month)
    {
        if(month>0 && month <=3)
        {
            return 1;
        }
        else if(month>3 && month <=6)
        {
            return 2;
        }
        else if(month>6 && month <=9)
        {
            return 3;
        }
        else if(month>9 && month <=12)
        {
            return 4;
        }
        return -1;
    }

    public void setExchangeRate(Integer currencyID)
    {
        //TODO Permissions only for Jive/Synchro admin
        try{
            getSynchroProjectManager().saveExchangeRate(currencyID);
        }catch(Exception e){LOG.error("Error saving the exchange rate information for currency ID "+currencyID);}
    }

    public void updateExchangeRate(Integer currencyID)
    {
        //TODO Permissions only for Jive/Synchro admin
        try{
            getSynchroProjectManager().updateExchangeRate(currencyID);
        }catch(Exception e){LOG.error("Error saving the exchange rate information for currency ID "+currencyID);}

    }

    public static String getJiveURL()
    {
        return JiveGlobals.getJiveProperty("jiveURL","");
    }

    public static Map<Integer, String> getEndMarketFields()
    {
        Map<Integer, String> availableEndMarkets = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getEndMarketFields();
        for(MetaField field : fields)
        {
            availableEndMarkets.put(field.getId().intValue(), field.getName());
        }
        return availableEndMarkets;
    }
    
    public static Map<Integer, String> getAllEndMarketFields()
    {
        Map<Integer, String> availableEndMarkets = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getAllEndMarketFields();
        for(MetaField field : fields)
        {
            availableEndMarkets.put(field.getId().intValue(), field.getName());
        }
        return availableEndMarkets;
    }

    public static Map<Integer, String> getMethodologyFields()
    {
        Map<Integer, String> methodologies = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getMethodologyFields();
        for(MetaField field : fields)
        {
            methodologies.put(field.getId().intValue(), field.getName());
        }
        return methodologies;
    }
    
    public static Map<Integer, MetaField> getMethodologyFieldProperties()
    {
        Map<Integer, MetaField> methodologies = new LinkedHashMap<Integer, MetaField>();
        List<MetaField> fields = getSynchroMetaFieldManager().getMethodologyFields();
        for(MetaField field : fields)
        {
            methodologies.put(field.getId().intValue(), field);
        }
        return methodologies;
    }

    
    public static Map<Integer, MetaField> getAllMethodologyFieldProperties()
    {
        Map<Integer, MetaField> methodologies = new LinkedHashMap<Integer, MetaField>();
        List<MetaField> fields = getSynchroMetaFieldManager().getAllMethodologyFields();
        for(MetaField field : fields)
        {
            methodologies.put(field.getId().intValue(), field);
        }
        return methodologies;
    }
    
    public static Map<Integer, String> getAllMethodologyFields()
    {
        Map<Integer, String> methodologies = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getAllMethodologyFields();
        for(MetaField field : fields)
        {
            methodologies.put(field.getId().intValue(), field.getName());
        }
        return methodologies;
    }

    public static boolean isMethodologyTypeActive(final Long id) {

        return getSynchroMetaFieldManager().isMethodologyTypeActive(id);
    }

    public static Map<Integer, String> getSelectedInActiveMethodologyFields(List<Long> ids) {
        Map<Integer, String> methodologies = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getSelectedInactiveMethodologyFields(ids);
        for(MetaField field : fields)
        {
            methodologies.put(field.getId().intValue(), field.getName());
        }
        return methodologies;
    }

    public static Map<Integer, String> getMethodologyGroupFields()
    {
        Map<Integer, String> methodologyGroups = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getMethodologyGroupFields();
        for(MetaField field : fields)
        {
            methodologyGroups.put(field.getId().intValue(), field.getName());
        }
        return methodologyGroups;
    }
    
    /**
     * Synchro Phase 5
     * @param id
     * @return
     */
    public static Map<Integer, MetaField> getMethodologyOtherProperties()
    {
        Map<Integer, MetaField> methodologyOthers = new LinkedHashMap<Integer, MetaField>();
        List<MetaField> fields = getSynchroMetaFieldManager().getMethodologyFields();
        for(MetaField field : fields)
        {
        	methodologyOthers.put(field.getId().intValue(), field);
        }
        return methodologyOthers;
    }
    
    public static List<MetaField> getMethodologyByGroup(Integer id)
    {
        return  getSynchroMetaFieldManager().getMethodologiesByGroup(new Long(id.toString()));
    }

    public static List<MetaField> getAllMethodologyByGroup(Integer id)
    {
        return  getSynchroMetaFieldManager().getAllMethodologiesByGroup(new Long(id.toString()));
    }

    public static List<MetaField> getMethodologyByType(Integer id)
    {
        return  getSynchroMetaFieldManager().getMethodologiesByType(new Long(id.toString()));
    }

    public static List<MetaField> getUnselectedMethodologiesForMethType()
    {
        return  getSynchroMetaFieldManager().getUnselectedMethodologiesForMethType();
    }

    public static List<MetaField> getUnselectedMethodologyGroupsForType()
    {
        return  getSynchroMetaFieldManager().getUnselectedMethodologyGroupsForType();
    }



    public static List<MetaField> getMethodologiesByType(final Long id)
    {
        return  getSynchroMetaFieldManager().getMethodologiesByType(id);
    }

    public static List<MetaField> getMethodologyGrpsByType(final Long id)
    {
        return  getSynchroMetaFieldManager().getMethodologyGrpsByType(id);
    }




    public static List<MetaField> getDataCollections(Integer type)
    {
        return  getSynchroMetaFieldManager().getDataCollectionFields(new Long(type.toString()));
    }

    public static Long getMethodologyTypeByProposedMethodology(final Long id)
    {
        return getSynchroMetaFieldManager().getMethodologyTypeByProposedMethodology(id);
    }

    public static Set<Long> getMethodologyTypesByProposedMethodologies(final List<Long> ids) {
        return getSynchroMetaFieldManager().getMethodologyTypesByProposedMethodologies(ids);
    }

    public static Long getMethodologyTypeByGroup(final Long id)
    {
        return getSynchroMetaFieldManager().getMethodologyTypeByGroup(id);
    }



    public static Map<Integer, String> getProductFields()
    {
        Map<Integer, String> products = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getProductFields();
        for(MetaField field : fields)
        {
            products.put(field.getId().intValue(), field.getName());
        }
        return products;
    }


    public static Map<Integer, String> getCurrencyFields()
    {
        Map<Integer, String> currencies = new LinkedHashMap<Integer, String>();
        List<Currency> fields = getSynchroMetaFieldManager().getCurrencyFields();
        for(Currency field : fields)
        {
            currencies.put(field.getId().intValue(), field.getName());
        }
        return currencies;
    }
    
    
    public static Map<Integer, Integer> getCurrencyGlobalFields()
    {
        Map<Integer, Integer> currencies = new LinkedHashMap<Integer, Integer>();
        List<Currency> fields = getSynchroMetaFieldManager().getCurrencyFields();
        for(Currency field : fields)
        {
            currencies.put(field.getId().intValue(), (field.isGlobal()?1:0));
        }
        return currencies;
    }
    
    public static Map<Integer, Currency> getAllCurrencyFields()
    {
        Map<Integer, Currency> currencies = new LinkedHashMap<Integer, Currency>();
        List<Currency> fields = getSynchroMetaFieldManager().getCurrencyFields();
        for(Currency field : fields)
        {
            currencies.put(field.getId().intValue(), field);
        }
        return currencies;
    }

    public static Map<Integer, Currency> getAllCurrencyFieldsNew(User user)
    {
        
        Map<Integer, Currency> currencies = new LinkedHashMap<Integer, Currency>();
        
        Map<Integer, Currency> allCurrencies = getAllCurrencyFields(); 
        List<Currency> globalCurrency = getSynchroMetaFieldManager().getGlobalCurrencyFields();
        List<Currency> nonGlobalCurrency = getSynchroMetaFieldManager().getNonGlobalCurrencyFields();
       
        
        for(Currency field : globalCurrency)
        {
            currencies.put(field.getId().intValue(), field);
        }
        Integer userLocalCurrency = getUserLocalCurrency(user);
        if(userLocalCurrency!=null && userLocalCurrency!=-1)
        {
        	currencies.put(userLocalCurrency, allCurrencies.get(userLocalCurrency));
        }
        for(Currency field : nonGlobalCurrency)
        {
            if(!currencies.containsKey(field.getId()))
            {	
            	currencies.put(field.getId().intValue(), field);
            }
        }
        return currencies;
    }
    
    public static Map<Integer, String> getCurrencyDescriptionFields()
    {
        Map<Integer, String> currencyDescriptions = new HashMap<Integer, String>();
        List<Currency> fields = getSynchroMetaFieldManager().getCurrencyFields();
        for(Currency field : fields)
        {
            currencyDescriptions.put(field.getId().intValue(), field.getDescription());
        }
        return currencyDescriptions;
    }

    public static List<CurrencyExchangeRate> getCurrencyRates() {
        return getSynchroMetaFieldManager().getCurrencyExchangeRates();
    }

    public static CurrencyExchangeRate getCurrencyRate(final Long id) {
        return getSynchroMetaFieldManager().getCurrencyExchangeRate(id);
    }

    public static Map<Integer, String> getSupplierFields()
    {
        Map<Integer, String> suppliers = new HashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getSupplierFields();
        for(MetaField field : fields)
        {
            suppliers.put(field.getId().intValue(), field.getName());
        }
        return suppliers;
    }

    public static Map<Integer, String> getSupplierGroupFields()
    {
        Map<Integer, String> supplierGroups = new HashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getSupplierGroupFields();
        for(MetaField field : fields)
        {
            supplierGroups.put(field.getId().intValue(), field.getName());
        }
        return supplierGroups;
    }

    public static Map<Integer, String> getFwSupplierGroupFields()
    {
        Map<Integer, String> supplierGroups = new HashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getFwSupplierGroupFields();
        for(MetaField field : fields)
        {
            supplierGroups.put(field.getId().intValue(), field.getName());
        }
        return supplierGroups;
    }

    public static Map<Integer, String> getTAgencyFields()
    {
        Map<Integer, String> tAgency = new HashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getTAgencyFields();
        for(MetaField field : fields)
        {
            tAgency.put(field.getId().intValue(), field.getName());
        }
        return tAgency;
    }

    /**
     * Data Collections
     * @return
     */
    public static Map<Integer, String> getDataCollectionFields()
    {
        Map<Integer, String> dataCollection = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getDataCollectionFields();
        for(MetaField field : fields)
        {
            dataCollection.put(field.getId().intValue(), field.getName());
        }
        return dataCollection;
    }

    /**
     * Methodology Type
     * @return
     */
    public static Map<Integer, String> getMethodologyTypeFields()
    {
        Map<Integer, String> methodologyTypes = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getMethodologyTypeFields();
        for(MetaField field : fields)
        {
            methodologyTypes.put(field.getId().intValue(), field.getName());
        }
        return methodologyTypes;
    }

    public static List<Long> getSupplierFields(Integer groupId)
    {
        List<Long> suppliers = new ArrayList<Long>();
        List<MetaField> fields = getSynchroMetaFieldManager().getSupplierFields(new Long(groupId.toString()));
        for(MetaField field : fields)
        {
            suppliers.add(field.getId());
        }
        return suppliers;
    }


    public static  Map<Integer, String> getFwSupplierFields()
    {
        Map<Integer, String> fwSuppliers = new HashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getFwSupplierFields();
        for(MetaField field : fields)
        {
            fwSuppliers.put(field.getId().intValue(), field.getName());
        }
        return fwSuppliers;
    }


    public static List<Long> getFwSupplierFields(Integer groupId)
    {
        List<Long> suppliers = new ArrayList<Long>();
        List<MetaField> fields = getSynchroMetaFieldManager().getFwSupplierFields(new Long(groupId.toString()));
        for(MetaField field : fields)
        {
            suppliers.add(field.getId());
        }
        return suppliers;
    }

    public static Map<Integer, String> getBrandFields()
    {
        Map<Integer, String> brands = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getBrandFields();
        for(MetaField field : fields)
        {
            brands.put(field.getId().intValue(), field.getName());
        }
        return brands;
    }
    
    //This method will pull the Brand Type for each Brand
    public static Map<Integer, String> getBrandBrandTypeFields()
    {
        Map<Integer, String> brands = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getAllBrandFields();
        for(MetaField field : fields)
        {
            if(field.getBrandType()!=null && field.getBrandType().intValue()==1)
            {
            	brands.put(field.getId().intValue(), "GDB");
            }
            else if(field.getBrandType()!=null && field.getBrandType().intValue()==2)
            {
            	brands.put(field.getId().intValue(), "Non-GDB");
            }
            else
            {
            	brands.put(field.getId().intValue(), " ");
            }
        	
        }
        return brands;
    }
    
    //This method will pull the Brand Type for each Brand
    public static Map<Integer, Integer> getAllBrandBrandTypeFields()
    {
        Map<Integer, Integer> brands = new LinkedHashMap<Integer, Integer>();
        List<MetaField> fields = getSynchroMetaFieldManager().getAllBrandFields();
        for(MetaField field : fields)
        {
            if(field.getBrandType()!=null && field.getBrandType()==1)
            {
            	brands.put(field.getId().intValue(), field.getBrandType());
            }
            else if(field.getBrandType()!=null && field.getBrandType()==2)
            {
            	brands.put(field.getId().intValue(), field.getBrandType());
            }
            else
            {
            	//brands.put(field.getId().intValue(), " ");
            }
        	
        }
        return brands;
    }

    public static Map<Integer, String> getBrandAllFields()
    {
        Map<Integer, String> brands = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getAllBrandFields();
        for(MetaField field : fields)
        {
            brands.put(field.getId().intValue(), field.getName());
        }
        return brands;
    }
    
    public static Integer getBrandId(final String name) {
        return getSynchroMetaFieldManager().getBrandId(name);
    }

    public static List<MetaField> getEndMarketFieldsMapping()
    {
        return  getSynchroMetaFieldManager().getEndMarketFields();
    }

    public static List<MetaField> getBrandsByProduct(Integer id)
    {
        return  getSynchroMetaFieldManager().getBrandFields(new Long(id.toString()));
    }


   public static void triggerEndMarkets()
    {
        SynchroGlobal.getEndMarkets().clear();
        SynchroGlobal.getRegions().clear();
        SynchroGlobal.getRegionEndMarketsMapping().clear();
        SynchroGlobal.getAreas().clear();
        SynchroGlobal.getAreaEndMarketsMapping().clear();
        SynchroGlobal.getEndMarketRegionMap().clear();
        SynchroGlobal.getEndMarketAreaMap().clear();
        SynchroGlobal.getT20T40().clear();
        SynchroGlobal.getT20T40EndMarketsMapping().clear();
        SynchroGlobal.getEndmarketApprovalTypeMap().clear();
        SynchroGlobal.getEndmarketMarketTypeMap().clear();
        SynchroGlobal.getEndmarketT20_T40_TypeMap().clear();
        }

    public static void triggerJobTitles()
    {
        SynchroGlobal.getJobTitles().clear();
    }

    public static void triggerMethodology()
    {
        SynchroGlobal.getMethodologies().clear();
        SynchroGlobal.getMethodologyGroups(true, new Long(1)).clear();
        SynchroGlobal.getMethodologyMapping().clear();
        SynchroGlobal.getAllMethodologyMapping().clear();
        SynchroGlobal.getAllMethodologies().clear();
        SynchroGlobal.getMethodologyProperties().clear();
        SynchroGlobal.getAllMethodologyProperties().clear();
        SynchroGlobal.getMethodologyTypeMapping().clear();
        

    }


    public static void triggerProduct()
    {
        SynchroGlobal.getProductTypes().clear();
        SynchroGlobal.getBrands(true, new Long(1)).clear();
        SynchroGlobal.getProductBrandMapping().clear();
    }

    public static void triggerBrand()
    {
        SynchroGlobal.getProductBrandMapping().clear();
        SynchroGlobal.getBrands(true, new Long(1)).clear();
        SynchroGlobal.getProductTypes().clear();
    }

    public static void triggerSuppliers()
    {
        SynchroGlobal.getSuppliers().clear();
        SynchroGlobal.getSupplierGroup().clear();
        SynchroGlobal.getSupplierGroupSupplierMapping().clear();
    }
    
    public static void triggerGrailResearchAgency()
    {
    	//TODO
        SynchroGlobal.getGrailResearchAgency().clear();
        SynchroGlobal.getGrailResearchAgencyGroup().clear();
        SynchroGlobal.getGrailResearchAgencyGroupMapping().clear();
        
        SynchroGlobal.getResearchAgencyMapping().clear();
        SynchroGlobal.getAllResearchAgencyMapping().clear();
        SynchroGlobal.getResearchAgencyGroup().clear();
        SynchroGlobal.getResearchAgency().clear();
		SynchroGlobal.getAllResearchAgency().clear();
    }

    public static void triggerFwSuppliers()
    {
        SynchroGlobal.getFieldWorkSuppliers().clear();
        SynchroGlobal.getFieldWorkSupplierGroup().clear();
        SynchroGlobal.getFieldWorkSupplierGroupSupplierMapping().clear();
    }

    public static void triggerCurrency()
    {
        SynchroGlobal.getCurrencies().clear();
        SynchroGlobal.getCurrencyDescriptions().clear();
        SynchroGlobal.getCountryCurrencyMap().clear();
        SynchroGlobal.getCurrencyGlobalFields().clear();
    }

    public static void triggerTAgency()
    {
        SynchroGlobal.getTenderingAgency().clear();
    }

    /**
     * Data Collection clear trigger
     * @return
     */
    public static void triggerDataCollection()
    {
        SynchroGlobal.getDataCollections().clear();
        SynchroGlobal.getProjectIsMapping().clear();
        SynchroGlobal.getCollectionMapping().clear();
    }

    /**
     * Methodology Type Fields
     * @return
     */
    public static void triggerMethodologyType() {
        SynchroGlobal.getDataCollections().clear();
        SynchroGlobal.getProjectIsMapping().clear();
        SynchroGlobal.getCollectionMapping().clear();
        SynchroGlobal.getMethodologyTypeMapping().clear();

    }


    public static List<MetaField> getProductByBrandInclusion(Long id)
    {
        return  getSynchroMetaFieldManager().getProductByBrandInclusion(id);

    }


    /**
     * Returns User object List from User ID List
     * @param userIDs
     * @return
     */
    public List<User> getUserObjectList(final List<Long> userIDs)
    {
        List<User> users = new ArrayList<User>();

        for(Long userID : userIDs)
        {
            try{
                users.add(getUserManager().getUser(userID));
            }catch(UserNotFoundException e){LOG.error("User Not found while fetching in getUserObjectList ");}
        }
        return users;
    }


    /**
     * Get List of End-Market Ids from Object List
     * @param endMarkets
     * @return
     */
    public static List<Long> getEndMarketIds(List<EndMarketField> endMarkets){
        List<Long> endMarketsId = new ArrayList<Long>();
        for(EndMarketField endMarketDetail : endMarkets)
        {
            endMarketsId.add(endMarketDetail.getId());
        }
        return endMarketsId;
    }




    public PIBManager getPibManager() {
        return pibManager;
    }

    public void setPibManager(PIBManager pibManager) {
        this.pibManager = pibManager;
    }

    public ProposalManager getProposalManager() {
        return proposalManager;
    }

    public void setProposalManager(ProposalManager proposalManager) {
        this.proposalManager = proposalManager;
    }


    public void processUser(User user)
    {
        if(user!=null && user.getID()>0)
        {
            String email = user.getEmail();
            if(EmailUtils.isValidEmailAddress(email))
            {
                Long referenceID = getSynchroProjectManager().getInviteIdByEmail(email);
                if(referenceID > 0)
                {
                    getSynchroProjectManager().replaceInviteReferences(referenceID, user.getID());
                    getSynchroProjectManager().removeInvite(email);
                }
            }
        }

    }

    public static String getUserDisplayName(final Long userID)
    {
        if(userID!=null && userID>0)
        {
            if(isReferenceID(userID))
            {
                return "Stakeholder Requested";
            }
            else
            {
                try
                {
                    return getUserManager().getUser(userID).getName();
                }catch(UserNotFoundException une)
                {
                    LOG.error("User Not found in Database with ID " + userID);
                }
            }
        }
        return "";
    }

    public static String getUserEmail(final Long userID)
    {
        if(userID!=null && userID>0)
        {
            if(!isReferenceID(userID))
            {
                try
                {
                    return getUserManager().getUser(userID).getEmail();
                }catch(UserNotFoundException une)
                {
                    LOG.error("User Not found in Database with ID " + userID);
                }
            }
        }
        return "";
    }

    public static Boolean isReferenceID(final Long userID)
    {
        if(StringUtils.length(userID.toString()) > 5)
        {
            return true;
        }
        return false;
    }

    public static List<User> getUserList(final List<Long> userIDs)
    {
        List<User> users = new ArrayList<User>();

        for(Long userID : userIDs)
        {
            try
            {
                users.add(getUserManager().getUser(userID));
            }catch(UserNotFoundException une)
            {
                LOG.error("User Not found in Database with ID " + userID);
            }
        }
        return users;
    }

    /**
     * This method will update the status of the project based on new modifications
     * https://www.svn.sourcen.com/issues/17847
     */
    public void updateProjectStatus(Project project)
    {
        //Project project = synchroProjectManager.get(projectId);
        List<ProjectInitiation> pibList = pibManager.getPIBDetails(project.getProjectID());

        List<ProposalInitiation> proposalList = this.proposalManager.getProposalDetails(project.getProjectID());
        Date fieldWorkStartDate= null;
        Date fieldWorkEndDate= null;
        if(proposalList!=null && proposalList.size()>0)
        {
            for(ProposalInitiation pi:proposalList)
            {
                if(pi.getIsAwarded())
                {
                    ProposalEndMarketDetails propEMDetails = this.proposalManager.getProposalEMDetails(project.getProjectID(), pi.getEndMarketID(), pi.getAgencyID());
                    if(propEMDetails != null) {
                        fieldWorkStartDate = propEMDetails.getFwStartDate();
                        fieldWorkEndDate = propEMDetails.getFwEndDate();
                    }
                }
            }
        }
        Date todayDate = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        /*	if(pibList!=null && pibList.size()>0 && pibList.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal() && project.getStatus()==SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal() && fieldWorkStartDate!=null)
          {

              //TODO : This is before the fieldwork start date has reached. Fieldwork start date of the Awarded Agency to be considered
              SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
              if(todayDate.before(fieldWorkStartDate) || dateFormatter.format(todayDate).equals(dateFormatter.format(fieldWorkStartDate)))
              {
                  synchroProjectManager.updateProjectStatus(project.getProjectID(), SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal());
              }
          }*/

        if(fieldWorkStartDate!=null && fieldWorkEndDate!=null && todayDate.after(fieldWorkStartDate) && todayDate.before(fieldWorkEndDate) && project.getStatus()==SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal())
        {
            synchroProjectManager.updateProjectStatus(project.getProjectID(), SynchroGlobal.Status.INPROGRESS_FIELDWORK.ordinal());
        }
        if(fieldWorkStartDate!=null && dateFormatter.format(todayDate).equals(dateFormatter.format(fieldWorkStartDate)) && project.getStatus()==SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal())
        {
            synchroProjectManager.updateProjectStatus(project.getProjectID(), SynchroGlobal.Status.INPROGRESS_FIELDWORK.ordinal());
        }
        if(fieldWorkEndDate!=null && dateFormatter.format(todayDate).equals(dateFormatter.format(fieldWorkEndDate)) && project.getStatus()==SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal())
        {
            synchroProjectManager.updateProjectStatus(project.getProjectID(), SynchroGlobal.Status.INPROGRESS_FIELDWORK.ordinal());
        }
        if(fieldWorkEndDate!=null && todayDate.after(fieldWorkEndDate) && project.getStatus()==SynchroGlobal.Status.INPROGRESS_FIELDWORK.ordinal())
        {
            synchroProjectManager.updateProjectStatus(project.getProjectID(), SynchroGlobal.Status.INPROGRESS_ANALYSIS.ordinal());
        }

        //https://www.svn.sourcen.com/issues/19152
        if(fieldWorkEndDate!=null && todayDate.after(fieldWorkEndDate) && project.getStatus()==SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal())
        {
            synchroProjectManager.updateProjectStatus(project.getProjectID(), SynchroGlobal.Status.INPROGRESS_ANALYSIS.ordinal());
        }

        List<ReportSummaryInitiation> reportSummaryList = this.reportSummaryManager.getReportSummaryInitiation(project.getProjectID());
        if(reportSummaryList!=null && reportSummaryList.size()>0 && reportSummaryList.get(0).getIsSPIApproved() && reportSummaryList.get(0).getSpiApprovalDate()!=null && project.getStatus()==SynchroGlobal.Status.INPROGRESS_ANALYSIS.ordinal())
        {
            synchroProjectManager.updateProjectStatus(project.getProjectID(), SynchroGlobal.Status.INPROGRESS_IRIS.ordinal());
        }
    }

    public ReportSummaryManager getReportSummaryManager() {
        return reportSummaryManager;
    }

    public void setReportSummaryManager(ReportSummaryManager reportSummaryManager) {
        this.reportSummaryManager = reportSummaryManager;
    }


    /**
     * Investment and Funding Utils
     */

    public static Boolean isAboveMarket(Integer investmentType)
    {
        if(investmentType!=null)
        {
            return investmentType==SynchroGlobal.InvestmentType.COUNTRY.getId()?false:true;
        }
        return false;
    }

    public static BigDecimal formatDate(String date)
    {
        date = date.replaceAll(",", "");
        return new BigDecimal(date);
    }

    /**
     * Region
     */
    public static Map<Integer, String> getRegionFields()
    {
        Map<Integer, String> regions = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getRegionFields();
        for(MetaField field : fields)
        {
            regions.put(field.getId().intValue(), field.getName());
        }
        return regions;
    }

    public static Map<Integer, String> getAllRegionFields()
    {
        Map<Integer, String> regions = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getAllRegionFields();
        for(MetaField field : fields)
        {
            regions.put(field.getId().intValue(), field.getName());
        }
        return regions;
    }
    
    public static List<MetaField> getEndMarketsByRegion(Integer id)
    {
        return  getSynchroMetaFieldManager().getEndMarketFieldsByRegion(new Long(id.toString()));
    }

    public static Integer getApprovalByEndmarket(Integer id)
    {
        return getSynchroMetaFieldManager().getApprovalByEndmarket(new Long(id.toString()));
    }
    
    public static Integer getMarketByEndmarket(Integer id)
    {
    	
    	return getSynchroMetaFieldManager().getMarketByEndmarket(new Long(id.toString()));
    }
    
    public static Integer getT20_T40_ByEndmarket(Integer id)
    {
        return getSynchroMetaFieldManager().getT20_T40_ByEndmarket(new Long(id.toString()));
    }
    
    public static List<MetaField> getEndMarketsByRegion(Long id)
    {
        return  getSynchroMetaFieldManager().getEndMarketFieldsByRegion(id);
    }

    /**
     * Area
     */
    public static Map<Integer, String> getAreaFields()
    {
        Map<Integer, String> areas = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getAreaFields();
        for(MetaField field : fields)
        {
            areas.put(field.getId().intValue(), field.getName());
        }
        return areas;
    }

    /*
    * Job Titles
    */
    public static Map<Integer, String> getJobTitles()
    {
        Map<Integer, String> jobTitles = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getJobTitles();
        for(MetaField field : fields)
        {
            jobTitles.put(field.getId().intValue(), field.getName());
        }
        return jobTitles;
    }

    public static List<MetaField> getEndMarketsByArea(Integer id)
    {
        return  getSynchroMetaFieldManager().getEndMarketFieldsByArea(new Long(id.toString()));
    }

/**
 * Grail Phase 5 code
 * @author kanwardeep.grewal
 */
    
    public static List<MetaField> getEndMarketsByT20T40(Integer tid)
    {
        return  getSynchroMetaFieldManager().getEndMarketFieldsByT20T40(new Long(tid.toString()));
    }
    
    /**
     * Returns Country Currency Mappings
     */
    public static  List<MetaFieldMapping> getCountryCurrencyMapping()
    {
        return  getSynchroMetaFieldManager().getCountryCurrencyMapping();
    }

    /**
     * Returns all Above Market Project Contact Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getAboveMarketProjectContact(final long projectID) {

        final Collection<User> aboveMarketProjectContacts = new HashSet<User>();
        List<FundingInvestment> fundingInvestment = getSynchroProjectManager().getProjectInvestments(projectID);
        try
        {
            for(FundingInvestment fd:fundingInvestment )
            {
                if(fd.getAboveMarket())
                {
                    aboveMarketProjectContacts.add(getUserManager().getUser(fd.getProjectContact()));
                }
            }
        }
        catch(UserNotFoundException ue)
        {
            LOG.error("Not able to find project owner user while fetching Above Market Project Contact for projecr" + projectID, ue);
        }
        return aboveMarketProjectContacts;
    }

    /**
     * Returns all Country Project Contact Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getCountryProjectContact(final long projectID) {

        final Collection<User> countryProjectContacts = new HashSet<User>();
        List<FundingInvestment> fundingInvestment = getSynchroProjectManager().getProjectInvestments(projectID);
        try
        {
            for(FundingInvestment fd:fundingInvestment )
            {
                if(!fd.getAboveMarket())
                {
                    countryProjectContacts.add(getUserManager().getUser(fd.getProjectContact()));
                }
            }
        }
        catch(UserNotFoundException ue)
        {
            LOG.error("Not able to find project owner user while fetching Country Project Contact for projecr" + projectID, ue);
        }
        return countryProjectContacts;
    }

    /**
     * Returns all End Market Project Contact + SPI Contact Users for the specified project
     * @param projectID - Long - ProjectID
     * @return List of Users
     */
    public Collection<User> getEndMarketContacts(final long projectID) {

        final Collection<User> countryProjectContacts = new HashSet<User>();
        List<FundingInvestment> fundingInvestment = getSynchroProjectManager().getProjectInvestments(projectID);
        try
        {
            for(FundingInvestment fd:fundingInvestment )
            {
                if(!fd.getAboveMarket())
                {
                    countryProjectContacts.add(getUserManager().getUser(fd.getProjectContact()));
                    countryProjectContacts.add(getUserManager().getUser(fd.getSpiContact()));
                }
            }
        }
        catch(UserNotFoundException ue)
        {
            LOG.error("Not able to find project owner user while fetching Country Project Contact for projecr" + projectID, ue);
        }
        return countryProjectContacts;
    }

    public Boolean isFieldWorkCompleted(final Long projectID, final Long endMarketId)
    {
        ProjectSpecsEndMarketDetails  specsEndmarketDetails = projectSpecsManager.getProjectSpecsEMDetails(projectID, endMarketId);
        if(specsEndmarketDetails!=null)
        {
            Date fwStartDate = specsEndmarketDetails.getFwStartDateLatest();
            Date fwEndDate = specsEndmarketDetails.getFwEndDateLatest();
            Date now = DateUtils.now();
            if(fwStartDate!=null && fwEndDate!=null)
            {
                if(DateUtils.compare(now, fwEndDate)>=0)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<MetaFieldMapping> getEndMarketRegionMapping()
    {
        return  getSynchroMetaFieldManager().getEndMarketRegionMapping();
    }

    public static List<MetaFieldMapping> getEndMarketAreaMapping()
    {
        return  getSynchroMetaFieldManager().getEndMarketAreaMapping();
    }


    public static String generateProjectCode(final Long id)
    {
        final int MAXDIGITS = 5;
        String prepend = "";

        if(id!=null && id>0)
        {
            int length = id.toString().length();
            int prependLength = MAXDIGITS - length;

            if(length<MAXDIGITS)
            {
                for(int i=0; i<prependLength; i++)
                {
                    prepend = prepend + "0";
                }
            }
            return prepend + id.intValue();
        }
        else
        {
            return "";
        }

    }

    public static List<String> fetchAgencyUsers(final String recipients)
    {
        List<String> agencyUserEmailList = new ArrayList<String>();
        if(StringUtils.isNotBlank(recipients))
        {
            if(recipients.contains(","))
            {
                String[] splitUserArray = recipients.split(",");
                for(int i=0; i<splitUserArray.length; i++)
                {
                    if(StringUtils.isNotBlank(splitUserArray[i]) && EmailUtils.isValidEmailAddress(splitUserArray[i].trim()))
                    {
                        if(isAgencyUser(splitUserArray[i].trim()))
                            agencyUserEmailList.add(splitUserArray[i].trim());
                    }
                }
            }
            else
            {
                if(EmailUtils.isValidEmailAddress(recipients.trim()))
                {
                    if(isAgencyUser(recipients.trim()))
                        agencyUserEmailList.add(recipients.trim());
                }
            }
        }
        return agencyUserEmailList;
    }

    public static List<String> fetchUserNames(final String recipients)
    {
        List<String> agencyUserEmailList = new ArrayList<String>();
        if(StringUtils.isNotBlank(recipients))
        {
            if(recipients.contains(","))
            {
                String[] splitUserArray = recipients.split(",");
                for(int i=0; i<splitUserArray.length; i++)
                {
                    if(StringUtils.isNotBlank(splitUserArray[i]) && EmailUtils.isValidEmailAddress(splitUserArray[i].trim()))
                    {
                        UserTemplate userTemplate = new UserTemplate();
                        userTemplate.setEmail(splitUserArray[i].trim());
                        User user = getUserManager().getUser(userTemplate);
                        if(user!=null)
                            agencyUserEmailList.add(user.getUsername());
                    }
                }
            }
            else
            {
                if(EmailUtils.isValidEmailAddress(recipients.trim()))
                {
                    UserTemplate userTemplate = new UserTemplate();
                    userTemplate.setEmail(recipients);
                    User user = getUserManager().getUser(userTemplate);
                    if(user!=null)
                        agencyUserEmailList.add(user.getUsername());
                }
            }
        }
        return agencyUserEmailList;
    }

    public static String removeAgencyUsersFromRecipients(final String recipients)
    {
        //Setting default return value to input value
        String recipientsExcludingAgency = recipients;

        if(StringUtils.isNotBlank(recipients))
        {
            if(recipients.contains(","))
            {
                String[] splitUserArray = recipients.split(",");

                //Empty the return value so that it can be build excluding Agency emails
                recipientsExcludingAgency = "";

                for(int i=0; i<splitUserArray.length; i++)
                {
                    if(StringUtils.isNotBlank(splitUserArray[i]) && EmailUtils.isValidEmailAddress(splitUserArray[i].trim()))
                    {
                        if(!isAgencyUser(splitUserArray[i].trim()))
                        {
                            if(StringUtils.isNotBlank(recipientsExcludingAgency))
                            {
                                recipientsExcludingAgency = recipientsExcludingAgency + ",";
                            }
                            recipientsExcludingAgency = recipientsExcludingAgency + splitUserArray[i].trim();
                        }
                    }
                }
            }
            else
            {
                if(EmailUtils.isValidEmailAddress(recipients.trim()))
                {
                    if(isAgencyUser(recipients.trim()))
                        recipientsExcludingAgency = "";
                }
            }
        }

        return recipientsExcludingAgency;
    }

    private static Boolean isAgencyUser(String email)
    {
        Boolean isAgencyUser = false;
        UserTemplate userTemplate = new UserTemplate();
        userTemplate.setEmail(email.trim());
        try
        {
            User user = getUserManager().getUser(userTemplate);
            if(user!=null && user.getID()>0)
            {
                Iterable<Group> groupIterable = getGroupManager().getUserGroups(user);
                if(groupIterable!=null)
                {
                    Iterator<Group> groups = groupIterable.iterator();
                    while(groups.hasNext())
                    {
                        Group group = groups.next();
                        String name = (group!=null)?group.getName():"";

                        if(StringUtils.equalsIgnoreCase(name, SynchroConstants.JIVE_COAGENCY_SUPPORT_GROUP_NAME) ||
                                StringUtils.equalsIgnoreCase(name, SynchroConstants.JIVE_COAGENCY_GROUP_NAME)
                                || StringUtils.equalsIgnoreCase(name, SynchroConstants.JIVE_FIELDWORK_GROUP_NAME)
                                || StringUtils.equalsIgnoreCase(name, SynchroConstants.JIVE_COMMUNICATION_AGECNY_GROUP_NAME))
                        {
                            isAgencyUser = true;
                            break;
                        }
                    }
                }
            }
        }
        catch(Exception unexception)
        {
            LOG.error("Error while sending email to stakeholders, filtering Agency users from the recipients in 'removeAgencyUsersFromRecipients' method. Error while getting User with email " + email.trim() + ". Error Details - " + unexception.getMessage());
        }

        if(isAgencyUser)
            return true;

        return false;
    }

    public static int getDefaultCurrencyByProject(Long projectID)
    {
        int defaultCurrencyByProject = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1);
        if(projectID!=null && projectID>0)
        {
            Project project = getsSynchroProjectManager().get(projectID);
            if(project!=null)
            {
                if(project.getMultiMarket())
                {
                    if(project.getTotalCostCurrency()!=null && project.getTotalCostCurrency()>0)
                    {
                        defaultCurrencyByProject = project.getTotalCostCurrency().intValue();
                    }
                }
                else
                {
                    List<EndMarketInvestmentDetail> endMarketDetails = getsSynchroProjectManager().getEndMarketDetails(projectID);
                    if(endMarketDetails!=null && endMarketDetails.size()>0)
                    {
                        if(endMarketDetails.get(0).getInitialCostCurrency()!=null && endMarketDetails.get(0).getInitialCostCurrency()>0)
                        {
                            defaultCurrencyByProject = endMarketDetails.get(0).getInitialCostCurrency().intValue();
                        }
                    }
                }

            }

        }

        return defaultCurrencyByProject;
    }

    public static int getDefaultCurrencyByUser(User user)
    {
        int defaultCurrencyByProject = JiveGlobals.getJiveIntProperty(SynchroConstants.SYNCHRO_DEFAULT_CURRENCY_ID, -1);

        if(user!=null && user.getID() > 0)
        {
            if(user.getProperties().containsKey(SynchroUserPropertiesUtil.COUNTRY))
            {
                int propCountryid = 0;
                String propCountryName = user.getProperties().get(SynchroUserPropertiesUtil.COUNTRY);
                if(StringUtils.isNotBlank(propCountryName))
                {
                    try{
                        Map<Integer, String> countryMap = SynchroGlobal.getEndMarkets();
                        if(countryMap.containsValue(getDecodedString(propCountryName)))
                        {
                            for(Integer countryid: countryMap.keySet())
                            {
                                if(countryMap.get(countryid).equalsIgnoreCase(getDecodedString(propCountryName)))
                                {
                                    propCountryid = countryid.intValue();
                                    break;
                                }
                            }
                        }
                        if(propCountryid>0)
                        {
                            Map<String, String> countryCurrencyMap = SynchroGlobal.getCountryCurrencyMap();
                            if(countryCurrencyMap!=null && countryCurrencyMap.containsKey(propCountryid+""))
                            {
                                defaultCurrencyByProject = Integer.parseInt( countryCurrencyMap.get(propCountryid+""));
                            }
                        }
                    }catch(Exception nfex)
                    {
                        LOG.error("Number format exception while converting string property of country id to Integer in Project Create Actions for getting default currency id " + nfex.getMessage());
                    }
                }
            }
        }

        return defaultCurrencyByProject;
    }

    public static int getCurrencyByUser(final User user) {
        Integer defaultCountry = SynchroUtils.getCountryByUser(user);
        Map<String, String> countryCurrencyMap = SynchroGlobal.getCountryCurrencyMap();
        Integer currencyId = -1;
        if(defaultCountry != null && defaultCountry > 0 && countryCurrencyMap.containsKey(defaultCountry.toString())) {
            try{
                currencyId = Integer.parseInt(countryCurrencyMap.get(defaultCountry.toString()));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return currencyId;
    }

    public static int getCountryByUser(User user)
    {
        int defaultCountry = -1;

        if(user!=null && user.getID() > 0)
        {
            if(user.getProperties().containsKey(SynchroUserPropertiesUtil.COUNTRY))
            {
                String propCountryName = user.getProperties().get(SynchroUserPropertiesUtil.COUNTRY);
                if(StringUtils.isNotBlank(propCountryName))
                {
                    try{
                        Map<Integer, String> countryMap = SynchroGlobal.getEndMarkets();
                        if(countryMap.containsValue(getDecodedString(propCountryName)))
                        {
                            for(Integer countryid: countryMap.keySet())
                            {
                                if(countryMap.get(countryid).equalsIgnoreCase(getDecodedString(propCountryName)))
                                {
                                    defaultCountry = countryid.intValue();
                                    break;
                                }
                            }
                        }
                    }catch(Exception nfex)
                    {
                        LOG.error("Number format exception while converting string property of country id to Integer in Project Create Actions for getting default currency id " + nfex.getMessage());
                    }
                }
            }
        }

        return defaultCountry;
    }



    private static String getEncodedText(String str)
    {
        String encoded_text = str.replaceAll(" ", "%20A");
        return encoded_text;
    }

    public static String getDecodedString(String str)
    {
        return str.replaceAll("%20A", " ");
    }

    // This function is for fixing https://svn.sourcen.com/issues/19652
    public static String fixBulletPoint(String str)
    {
        if(str!=null)
        {

            if(str.contains("</ul><p") || str.contains("</ul>"+System.getProperty("line.separator")+"<p"))
            {
                return str.replaceAll("</ul><p>&nbsp;</p>(.*)<p>&nbsp;</p>", "</ul><p>&nbsp;</p>");
            }
            else
            {
                return str.replaceFirst("</ul>", "</ul><p>&nbsp;</p>");
            }
        }
        return str;
    }



    /**
     * Returns Eligible list of user that can initiate a project in kantar button
     * @return
     */
    public List<User> getKantarButtonInitiators() {
        final List<User> agencyGroupUsers = getAgencyGroupUsers();
        final List<User> allUsers = userManager.getUsers(0, userManager.getTotalUserCount());
        allUsers.removeAll(agencyGroupUsers);
        return allUsers;
    }

    public List<User> getDocumentRepositoryInitiators() {
        List<User> users = new ArrayList<User>();

//        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_SYNCHRO_MINI_ADMIN_GROUP_NAME));
//        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_DOCUMENT_REPOSITORY_BAT_GROUP_NAME));
//        users.addAll(getJiveGroupMembers(SynchroConstants.JIVE_DOCUMENT_REPOSITORY_AGENCY_GROUP_NAME));

        List<Long> userIds = getKantarReportManager().getAuthors();
        if(userIds != null && userIds.size() > 0) {
            for(Long uId : userIds) {
                User user = null;
                try {
                    user = userManager.getUser(uId);
                } catch (UserNotFoundException e) {

                }
                if(user != null) {
                    users.add(user);
                }
            }
        }

        return users;
    }


    /**
     * Returns Eligible list of user that can initiate a project in kantar button
     * @return
     */
    public List<User> getKantarButtonBATContacts() {
        final List<User> agencyGroupUsers = getAgencyGroupUsers();
        final List<User> allUsers = userManager.getUsers(0, userManager.getTotalUserCount());
        allUsers.removeAll(agencyGroupUsers);
        return allUsers;
    }
    public static Map<Integer, String> getResearchAgencyFields()
    {
        Map<Integer, String> researchAgencies = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getResearchAgencyFields();
        for(MetaField field : fields)
        {
        	researchAgencies.put(field.getId().intValue(), field.getName());
        }
        return researchAgencies;
    }
    
    public static Map<Integer, String> getAllResearchAgencyFields()
    {
        Map<Integer, String> researchAgencies = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getAllResearchAgencyFields();
        for(MetaField field : fields)
        {
        	researchAgencies.put(field.getId().intValue(), field.getName());
        }
        return researchAgencies;
    }
    
    public static Map<Integer, String> getResearchAgencyGroupFields()
    {
        Map<Integer, String> researchAgencyGroups = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getResearchAgencyGroupFields();
        for(MetaField field : fields)
        {
        	researchAgencyGroups.put(field.getId().intValue(), field.getName());
        }
        return researchAgencyGroups;
    }
    public static List<MetaField> getResearchAgencyByGroup(Integer id)
    {
        return  getSynchroMetaFieldManager().getResearchAgencyByGroup(new Long(id.toString()));
    }
    
    public static List<MetaField> getAllResearchAgencyByGroup(Integer id)
    {
        return  getSynchroMetaFieldManager().getAllResearchAgencyByGroup(new Long(id.toString()));
    }
    
    
    
    
    
 /** Synchro Phase 5
     * @author kanwardeep.grewal
     * 
     * Retrieve T20/T40 Areas
     */
    public static Map<Integer, String> getT20T40()
    {
        Map<Integer, String> t20T40 = new LinkedHashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getT20T40Fields();
        for(MetaField field : fields)
        {
        	t20T40.put(field.getId().intValue(), field.getName());
        }
        return t20T40;
    }
    
    /*
     * This method will return the ProcessType for a project on the basis of ProjectType(GLOBAL/REGIONAL/ENDMARKET)
     */
    public static Integer getProjectProcessType(Integer projectType, Long endMarketId, Integer budgetLocation) {
    	if(projectType==SynchroGlobal.ProjectType.GLOBAL.getId())
    	{
    		Integer approvalType = getSynchroMetaFieldManager().getApprovalByEndmarket(endMarketId);
    		Integer endMarketType = getSynchroMetaFieldManager().getMarketByEndmarket(endMarketId);
    		if(endMarketType==1)
    		{
    			if(approvalType==1)
    			{
    				return SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId();
    			}
    			else if (approvalType==2)
    			{
    				return SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId();
    			}
    			else if(approvalType==3)
    			{
    				
    			}
    		}
    		else if(endMarketType==2)
    		{
    			return SynchroGlobal.ProjectProcessType.GLOBAL_NON_EU.getId();
    		}
    	}
    	else if(projectType==SynchroGlobal.ProjectType.REGIONAL.getId())
    	{

    		Integer approvalType = getSynchroMetaFieldManager().getApprovalByEndmarket(endMarketId);
    		Integer endMarketType = getSynchroMetaFieldManager().getMarketByEndmarket(endMarketId);
    		if(endMarketType==1)
    		{
    			if(approvalType==1)
    			{
    				return SynchroGlobal.ProjectProcessType.GLOBAL_EU_ONLINE.getId();
    			}
    			else if (approvalType==2)
    			{
    				return SynchroGlobal.ProjectProcessType.GLOBAL_EU_OFFLINE.getId();
    			}
    			else if(approvalType==3)
    			{
    				
    			}
    		}
    		else if(endMarketType==2)
    		{
    			return SynchroGlobal.ProjectProcessType.GLOBAL_NON_EU.getId();
    		}
    	
    		
    	}
    	else if(projectType==SynchroGlobal.ProjectType.ENDMARKET.getId())
    	{
    		//Integer approvalType = getSynchroMetaFieldManager().getApprovalByEndmarket(endMarketId);
    		//Integer endMarketType = getSynchroMetaFieldManager().getMarketByEndmarket(endMarketId);
    		
    		// In case of End Market Projects the project type will be defined from the Budget Location and not from End Market
    		// http://redmine.nvish.com/redmine/issues/333
    		if(budgetLocation!=null)
    		{
	    		Integer approvalType = getSynchroMetaFieldManager().getApprovalByEndmarket(budgetLocation.longValue());
	    		Integer endMarketType = getSynchroMetaFieldManager().getMarketByEndmarket(budgetLocation.longValue());
	    		if(endMarketType==1)
	    		{
	    			if(approvalType==1)
	    			{
	    				return SynchroGlobal.ProjectProcessType.END_MARKET_EU_ONLINE.getId();
	    			}
	    			else if (approvalType==2)
	    			{
	    				return SynchroGlobal.ProjectProcessType.END_MARKET_EU_OFFLINE.getId();
	    			}
	    			else if(approvalType==3)
	    			{
	    				
	    			}
	    		}
	    		else if(endMarketType==2)
	    		{
	    			return SynchroGlobal.ProjectProcessType.END_MARKET_NON_EU.getId();
	    		}
    		}
    	}
    	return null;
        
    }
    
    /**
     * This method will return the budget location end Markets for a User
     * @return
     */
    public static List<Long>  getBudgetLocationEndMarkets(User user)
    {
        List<Long> endMarketsList = new ArrayList<Long>();
        Map<String,String> userProperties = user.getProperties();
        if(userProperties != null && !userProperties.isEmpty()) {
            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)
                    && userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST) != null
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).equals("")) {
                String endmarketAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST);
                String [] endmarketAccessList = endmarketAccessListStr.split(",");
                if(endmarketAccessList.length > 0) {
                    for(int i=0;i<endmarketAccessList.length;i++) {
                        
                        endMarketsList.add(Long.parseLong(endmarketAccessList[i]));
                    }
                }
            }
        }
        return endMarketsList;
    }
    
    /**
     * This method will return the budget location Regions for a User
     * @return
     */
    public static List<Long>  getBudgetLocationRegions(User user)
    {
        List<Long> regionsList = new ArrayList<Long>();
        Map<String,String> userProperties = user.getProperties();
        if(userProperties != null && !userProperties.isEmpty()) {
            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST)
                    && userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST) != null
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST).equals("")) {
                String regionsAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST);
                String [] regionsAccessList = regionsAccessListStr.split(",");
                if(regionsAccessList.length > 0) {
                    for(int i=0;i<regionsAccessList.length;i++) {
                        
                    	regionsList.add(Long.parseLong(regionsAccessList[i]));
                    }
                }
            }
        }
        return regionsList;
    }
    
    /**
     * This method will return the budget location EndMarkets  for a User Region
     * @return
     */
    public static List<Long>  getBudgetLocationRegionsEndMarkets(User user)
    {
        List<Long> regionsList = new ArrayList<Long>();
        Map<String,String> userProperties = user.getProperties();
        if(userProperties != null && !userProperties.isEmpty()) {
            if(userProperties.containsKey(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST)
                    && userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST) != null
                    && !userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST).equals("")) {
                String regionsAccessListStr = userProperties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST);
                String [] regionsAccessList = regionsAccessListStr.split(",");
                if(regionsAccessList.length > 0) {
                    for(int i=0;i<regionsAccessList.length;i++) {
                        
                    	regionsList.add(Long.parseLong(regionsAccessList[i]));
                    }
                }
            }
        }
        List<Long> endMarketsList = new ArrayList<Long>();
        for(Long region:regionsList)
        {
        	List<MetaField> endMarkets = getEndMarketsByRegion(region);
        	for(MetaField endMarket:endMarkets)
        	{
        		endMarketsList.add(endMarket.getId());
        	}
        }
        return endMarketsList;
        
        
    }
    
    
    /*
     * This method will fetch the Budget Location Filters for a particular user
     */
    public static Map<Integer, String> getBudgetLocationFilter(User user)
    {
    	Map<Integer, String> budgetLocationFilter = new LinkedHashMap<Integer, String>();
    	
    	Map<Integer, String> regions = SynchroGlobal.getRegions();
		Map<Integer, String> endMarkets = SynchroGlobal.getEndMarkets();
		
    	if(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroSystemOwner() || SynchroPermHelper.isGlobalUserType())
        {
    		budgetLocationFilter.put(-1, "Global");
    		
    		
    		for(Integer reg: regions.keySet())
    		{
    			
    			if(regions.get(reg).equals("Global"))
    			{
    				
    			}
    			else
    			{
    				budgetLocationFilter.put(reg, regions.get(reg));
    			}
    			
    		}
    		
    		for(Integer em: endMarkets.keySet())
    		{
    			if(endMarkets.get(em).equals("Global"))
    			{
    				
    			}
    			else
    			{
    				budgetLocationFilter.put(em, endMarkets.get(em));
    			}
    		}
    		
        }
    	else if (SynchroPermHelper.isRegionalUserType())
    	{
    		List<Long> userRegions =  getBudgetLocationRegions(user);
    		List<Long> userRegionEms = getBudgetLocationRegionsEndMarkets(user);
    		
    		if(userRegions!=null && userRegions.size()>0)
    		{
    			for(Long rId : userRegions)
    			{
    				budgetLocationFilter.put(rId.intValue(), regions.get(rId.intValue()));
    			}
    		}
    		
    		if(userRegionEms!=null && userRegionEms.size()>0)
    		{
    			for(Long emId : userRegionEms)
    			{
    				budgetLocationFilter.put(emId.intValue(), endMarkets.get(emId.intValue()));
    			}
    		}
    	}
    	else if (SynchroPermHelper.isEndMarketUserType())
    	{
    		List<Long>  userEMs = getBudgetLocationEndMarkets(user);
    		if(userEMs!=null && userEMs.size()>0)
    		{
    			for(Long emId : userEMs)
    			{
    				budgetLocationFilter.put(emId.intValue(), endMarkets.get(emId.intValue()));
    			}
    		}
    	}
    	
    	return budgetLocationFilter;
    }
    
    /*
     * This method will fetch the Budget Location Filters for a particular user for TPD Dashboard
     */
    public static Map<Integer, String> getBudgetLocationTPDDashboardFilter(User user)
    {
    	Map<Integer, String> budgetLocationFilter = new LinkedHashMap<Integer, String>();
    	
    	Map<Integer, String> regions = SynchroGlobal.getRegions();
		Map<Integer, String> endMarkets = SynchroGlobal.getEndMarkets();
		
    	if(SynchroPermHelper.isSynchroAdmin(user) || SynchroPermHelper.isSynchroSystemOwner() || SynchroPermHelper.isGlobalUserType())
        {
    		budgetLocationFilter.put(-1, "Global");
    		
    		
    		for(Integer reg: regions.keySet())
    		{
    			
    			if(regions.get(reg).equals("Global"))
    			{
    				
    			}
    			else
    			{
    				budgetLocationFilter.put(reg, regions.get(reg));
    			}
    			
    		}
    		
    		for(Integer em: endMarkets.keySet())
    		{
    			if(endMarkets.get(em).equals("Global"))
    			{
    				
    			}
    			else
    			{
    				budgetLocationFilter.put(em, endMarkets.get(em));
    			}
    		}
    		
        }
    	else if (SynchroPermHelper.isRegionalUserType())
    	{
    		List<Long> userRegions =  getBudgetLocationRegions(user);
    		List<Long> userRegionEms = getBudgetLocationRegionsEndMarkets(user);
    		
    		budgetLocationFilter.put(-1, "Global");
    		
    		if(userRegions!=null && userRegions.size()>0)
    		{
    			for(Long rId : userRegions)
    			{
    				budgetLocationFilter.put(rId.intValue(), regions.get(rId.intValue()));
    			}
    		}
    		
    		if(userRegionEms!=null && userRegionEms.size()>0)
    		{
    			for(Long emId : userRegionEms)
    			{
    				budgetLocationFilter.put(emId.intValue(), endMarkets.get(emId.intValue()));
    			}
    		}
    	}
    	else if (SynchroPermHelper.isEndMarketUserType())
    	{
    		budgetLocationFilter.put(-1, "Global");
    		for(Integer reg: regions.keySet())
    		{
    			
    			if(regions.get(reg).equals("Global"))
    			{
    				
    			}
    			else
    			{
    				budgetLocationFilter.put(reg, regions.get(reg));
    			}
    			
    		}
    		
    		List<Long>  userEMs = getBudgetLocationEndMarkets(user);
    		if(userEMs!=null && userEMs.size()>0)
    		{
    			for(Long emId : userEMs)
    			{
    				budgetLocationFilter.put(emId.intValue(), endMarkets.get(emId.intValue()));
    			}
    		}
    	}
    	
    	return budgetLocationFilter;
    }
    
    /**
     * This method will fetch the Legal Approvers for the end Markets for Synchro Phase 5
     */
    public Map<Long, List<User>>  getLegalApprovers()
    {
    	List<User> synchroUsers =  getSynchroGroupUsers();
    	Map<Long, List<User>> endMarketLegalApprovers = new HashMap<Long, List<User>>();
    	for(User user:synchroUsers)
    	{
    		if(user.getProperties()!=null && user.getProperties().containsKey(SynchroUserPropertiesUtil.LEGAL_USER) && user.getProperties().get(SynchroUserPropertiesUtil.LEGAL_USER).equals("1"))
    		{
    			List<Long> endMarketsList =  getBudgetLocationEndMarkets(user);
    			for(Long endMarketId: endMarketsList)
    			{
    				if(endMarketLegalApprovers.containsKey(endMarketId))
    				{
    					List<User> userList = endMarketLegalApprovers.get(endMarketId);
    					userList.add(user);
    					endMarketLegalApprovers.put(endMarketId, userList);
    				}
    				else
    				{
    					List<User> userList = new ArrayList<User>();
    					userList.add(user);
    					endMarketLegalApprovers.put(endMarketId, userList);
    				}
    			}
    		}
    	}
    	return endMarketLegalApprovers;
    }
    
    
    /**
     * grail Phase 5
     * 
     */
    public static Map<Integer, String> getGrailResearchAgencyFields()
    {
        Map<Integer, String> agencies = new HashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getGrailResearchAgencyFields();
        for(MetaField field : fields)
        {
        	agencies.put(field.getId().intValue(), field.getName());
        }
        return agencies;
    }

    public static Map<Integer, String> getGrailResearchAgencyGroupFields()
    {
        Map<Integer, String> agencyGroups = new HashMap<Integer, String>();
        List<MetaField> fields = getSynchroMetaFieldManager().getGrailResearchAgencyGroupFields();
        for(MetaField field : fields)
        {
        	agencyGroups.put(field.getId().intValue(), field.getName());
        }
        return agencyGroups;
    }
    

    public static List<Long> getGrailResearchAgencyFields(Integer groupId)
    {
        List<Long> agencies = new ArrayList<Long>();
        List<MetaField> fields = getSynchroMetaFieldManager().getGrailResearchAgencyFields(new Long(groupId.toString()));
        for(MetaField field : fields)
        {
        	agencies.add(field.getId());
        }
        return agencies;
    }
    
    public static Integer getUserLocalCurrency(User user)
    {
    	Integer userLocalCurrency = -1;

        if(user!=null && user.getID() > 0)
        {
            if(user.getProperties().containsKey(SynchroUserPropertiesUtil.CURRENCY_USER))
            {
            	try
            	{
            		userLocalCurrency = Integer.parseInt(user.getProperties().get(SynchroUserPropertiesUtil.CURRENCY_USER));
            	}
            	catch(Exception e)
            	{
            		
            	}
            	
            }
        }

        return userLocalCurrency;
    }
    
    public static String getDateString(Long timestamp)
    {
    	return new SimpleDateFormat("yyyy-MM-dd").format(new Date(timestamp));
    }
    
    /**
     * This method will return the budget location 
     * @return
     */
    public static String  getBudgetLocationName(Integer budgetLocation)
    {
       String budgetLocationName = "";
       if(budgetLocation!=null)
       {
	       if(budgetLocation == -1)
	       {
	    	   budgetLocationName = "Global";
	       }
	       else if(getRegionFields().get(budgetLocation)!=null)
	       {
	    	   budgetLocationName = getRegionFields().get(budgetLocation);
	       }
	       else if(getEndMarketFields().get(budgetLocation)!=null)
	       {
	    	   budgetLocationName = getEndMarketFields().get(budgetLocation);
	       }
       }
       return budgetLocationName;
    }
    
    /**
     * This method will return the budget location and will include Inactive End Markets as well
     * @return
     */
    public static String  getAllBudgetLocationName(Integer budgetLocation)
    {
       String budgetLocationName = "";
       if(budgetLocation!=null)
       {
	       if(budgetLocation == -1)
	       {
	    	   budgetLocationName = "Global";
	       }
	       else if(getRegionFields().get(budgetLocation)!=null)
	       {
	    	   budgetLocationName = getAllRegionFields().get(budgetLocation);
	       }
	       else if(getAllEndMarketFields().get(budgetLocation)!=null)
	       {
	    	   budgetLocationName = getAllEndMarketFields().get(budgetLocation);
	       }
       }
       return budgetLocationName;
    }
    
    public static String  getBudgetLocationNames(List<Long> budgetLocation)
    {
       List<String> budgetLocationName = new ArrayList<String>();
       if(budgetLocation!=null && budgetLocation.size() > 0)
       {
	       for(Long bl:budgetLocation)
	       {
	    	   budgetLocationName.add(getBudgetLocationName(bl.intValue()));
	       }
    	  
       }
       
       return StringUtils.join(budgetLocationName, ",");
    }
    
    public static String  getBrandNames(List<Long> brands)
    {
       List<String> brandName = new ArrayList<String>();
       if(brands!=null && brands.size() > 0)
       {
	       for(Long brand:brands)
	       {
	    	   brandName.add(getBrandAllFields().get(brand.intValue()));
	       }
    	  
       }
       
       return StringUtils.join(brandName, ",");
    }
    
    public static String  getProjectStatusName(List<Long> projectStatus)
    {
       List<String> projectStatusName = new ArrayList<String>();
       if(projectStatus!=null && projectStatus.size() > 0)
       {
	       for(Long ps:projectStatus)
	       {
	    	   if(ps.intValue()==1)
	    	   {
	    		   projectStatusName.add("On Track");
	    	   }
	    	   if(ps.intValue()==2)
	    	   {
	    		   projectStatusName.add("Not On Track");
	    	   }
	       }
    	  
       }
       
       return StringUtils.join(projectStatusName, ",");
    }
    
    public static String  getProjectStageName(List<Long> projectStage)
    {
       List<String> projectStageName = new ArrayList<String>();
       if(projectStage!=null && projectStage.size() > 0)
       {
	       for(Long ps:projectStage)
	       {
	    	   if(ps.intValue()==1)
	    	   {
	    		   projectStageName.add("In-Planning");
	    	   }
	    	   if(ps.intValue()==2)
	    	   {
	    		   projectStageName.add("In-Progress");
	    	   }
	    	   if(ps.intValue()==3)
	    	   {
	    		   projectStageName.add("Awaiting Results");
	    	   }
	    	   if(ps.intValue()==4)
	    	   {
	    		   projectStageName.add("Closed");
	    	   }
	    	   if(ps.intValue()==5)
	    	   {
	    		   projectStageName.add("Cancelled");
	    	   }
	       }
    	  
       }
       
       return StringUtils.join(projectStageName, ",");
    }
    
    public static String  getCategoryNames(List<Long> categories)
    {
       List<String> categoryName = new ArrayList<String>();
       if(categories!=null && categories.size() > 0)
       {
	       for(Long category:categories)
	       {
	    	   categoryName.add(getProductFields().get(category.intValue()));
	       }
    	  
       }
       
       return StringUtils.join(categoryName, ",");
    }
    
    public static String  getEndMarketNames(List<Long> endMarkets)
    {
       List<String> endMarketName = new ArrayList<String>();
       if(endMarkets!=null && endMarkets.size() > 0)
       {
	       for(Long em:endMarkets)
	       {
	    	   endMarketName.add(getEndMarketFields().get(em.intValue()));
	       }
    	  
       }
       
       return StringUtils.join(endMarketName, ",");
    }
    
    public static String  getAgencyNames(List<Long> agencies)
    {
       List<String> agencyName = new ArrayList<String>();
       if(agencies!=null && agencies.size() > 0)
       {
	       for(Long agency:agencies)
	       {
	    	   agencyName.add(getResearchAgencyFields().get(agency.intValue()));
	       }
    	  
       }
       
       return StringUtils.join(agencyName, ",");
    }
    
    public static String  getMethodologyNames(List<Long> methodologies)
    {
       List<String> methName = new ArrayList<String>();
       if(methodologies!=null && methodologies.size() > 0)
       {
	       for(Long meth:methodologies)
	       {
	    	   methName.add(getMethodologyFields().get(meth.intValue()));
	       }
    	  
       }
       
       return StringUtils.join(methName, ",");
    }
    
    public static String  getMethodologyTypeNames(List<Long> methodologyTypes)
    {
       List<String> methTypeName = new ArrayList<String>();
       if(methodologyTypes!=null && methodologyTypes.size() > 0)
       {
	       for(Long methType:methodologyTypes)
	       {
	    	   methTypeName.add(getMethodologyTypeFields().get(methType.intValue()));
	       }
    	  
       }
       
       return StringUtils.join(methTypeName, ",");
    }
    
    public static String  getProcessWaiverStatusName(List<Long> processWaiverStatus)
    {
       List<String> processWaiverStatusName = new ArrayList<String>();
       if(processWaiverStatus!=null && processWaiverStatus.size() > 0)
       {
	       for(Long pws:processWaiverStatus)
	       {
	    	   if(pws.intValue()==2)
	    	   {
	    		   processWaiverStatusName.add("Pending");
	    	   }
	    	   if(pws.intValue()==3)
	    	   {
	    		   processWaiverStatusName.add("Approved");
	    	   }
	    	   if(pws.intValue()==4)
	    	   {
	    		   processWaiverStatusName.add("Rejected");
	    	   }
	       }
    	  
       }
       
       return StringUtils.join(processWaiverStatusName, ",");
    }
    
    public static String  getTPDStatusName(List<Long> tpdStatus)
    {
       List<String> tpdStatusName = new ArrayList<String>();
       if(tpdStatus!=null && tpdStatus.size() > 0)
       {
	       for(Long tpdS:tpdStatus)
	       {
	    	   if(tpdS.intValue()==1)
	    	   {
	    		   tpdStatusName.add("May have to be TPD Submitted");
	    	   }
	    	   if(tpdS.intValue()==2)
	    	   {
	    		   tpdStatusName.add("Doesn't have to be TPD Submitted");
	    	   }
	    	   if(tpdS.intValue()==3)
	    	   {
	    		   tpdStatusName.add("Pending");
	    	   }
	    	   
	       }
    	  
       }
       
       return StringUtils.join(tpdStatusName, ",");
    }
    
    public static String  getCostComponentName(List<Long> costComponents)
    {
       List<String> costComponentName = new ArrayList<String>();
       if(costComponents!=null && costComponents.size() > 0)
       {
	       for(Long cc:costComponents)
	       {
	    	   if(cc.intValue()==1)
	    	   {
	    		   costComponentName.add("Coordination");
	    	   }
	    	   if(cc.intValue()==2)
	    	   {
	    		   costComponentName.add("Fieldwork");
	    	   }
	    	   
	       }
    	  
       }
       
       return StringUtils.join(costComponentName, ",");
    }
    
    public static String  getPendingActionName(List<Long> pendingAction)
    {
       List<String> pendingActionName = new ArrayList<String>();
       if(pendingAction!=null && pendingAction.size() > 0)
       {
	       for(Long pa:pendingAction)
	       {
	    	   if(pa.intValue()==1)
	    	   {
	    		   pendingActionName.add("Brief Approval");
	    	   }
	    	   if(pa.intValue()==2)
	    	   {
	    		   pendingActionName.add("Proposal Approval");
	    	   }
	    	  
	       }
    	  
       }
       
       return StringUtils.join(pendingActionName, ",");
    }
    
    /**
     * This method will return the area Name
     * @return
     */
    public static String  getAreaName(Integer budgetLocation)
    {
       String areaName = "";
       if(budgetLocation!=null)
       {
	       if(budgetLocation == -1)
	       {
	    	   //budgetLocationName = "Global";
	       }
	       else
	       {
		       for(Integer area : SynchroGlobal.getAreas().keySet())
	           {
		    	   Map<Integer, String> areaMapping = SynchroGlobal.getAreaEndMarketsMapping().get(area);
	               if(areaMapping!=null && areaMapping.get(budgetLocation)!=null)
	               {
	            	   areaName = SynchroGlobal.getAreas().get(area);
	               }
	           }
	       }
       }
       return areaName;
    }
    
    /**
     * This method will return the Region Name
     * @return
     */
    public static String  getRegionName(Integer budgetLocation)
    {
       String regionName = "";
       if(budgetLocation!=null)
       {
	       if(budgetLocation == -1)
	       {
	    	   //budgetLocationName = "Global";
	       }
	       else
	       {
		       for(Integer region : SynchroGlobal.getRegions().keySet())
	           {
		    	   Map<Integer, String> regionMapping = SynchroGlobal.getRegionEndMarketsMapping().get(region);
	               if(regionMapping!=null && regionMapping.get(budgetLocation)!=null)
	               {
	            	   regionName = SynchroGlobal.getRegions().get(region);
	               }
	           }
	       }
       }
       return regionName;
    }
    
    /**
     * This method will return the area Name
     * @return
     */
    public static String  getT20_T40Name(Integer budgetLocation)
    {
       String t20_40Name = "";
       if(budgetLocation!=null)
       {
	       if(budgetLocation == -1)
	       {
	    	   //budgetLocationName = "Global";
	       }
	       else if(SynchroGlobal.getEndmarketT20_T40_TypeMap().get(budgetLocation)!=null)
	       {
	    	   t20_40Name = SynchroGlobal.getT20_T40_Types().get(SynchroGlobal.getEndmarketT20_T40_TypeMap().get(budgetLocation));
	       }
	       
	      
       }
       return t20_40Name;
    }
    
    public static Boolean hasDateMet(Date date)
    {
    	Date todayDate = new Date(); 
    	if(date!=null)
    	{
	    	if(date.compareTo(todayDate) <= 0)
	    	{
	    		return true;
	    	}
    	}
    	return false;
    }
    
    public static HSSFCell populateCost(BigDecimal dataCost, HSSFCell dataCell, HSSFCellStyle costFormatStyle, HSSFCellStyle costDecimalFormatStyle )
	{

		
		  String totalCostString = dataCost.toPlainString();
		  if(totalCostString.contains("."))
		  {
			  String splitCost = totalCostString.split("\\.")[0];
			  if(splitCost.length()>14)
			  {
				  dataCell.setCellStyle(costFormatStyle);
				  dataCell.setCellValue(dataCost.toPlainString());
			  }
			  else
			  {
				  dataCell.setCellStyle(costFormatStyle);
				  dataCell.setCellValue(dataCost.doubleValue());
			  }
		  }
		  else
		  {
			  if(totalCostString.length()>14)
			  {
				  dataCell.setCellStyle(costFormatStyle);
				  dataCell.setCellValue(totalCostString);
			  }
			  else
			  {
				  dataCell.setCellStyle(costFormatStyle);
				  dataCell.setCellValue(dataCost.doubleValue());
			  }
		  }
		  return dataCell; 
		   
	   
	}
    
    public static HSSFCell populateSpendByProjectTotalCost(BigDecimal dataCost, HSSFCell dataCell, HSSFCellStyle costFormatStyle, HSSFCellStyle costDecimalFormatStyle )
   	{

   		if(dataCost==null)
   		{
   			dataCell.setCellValue("-");
   			return dataCell; 
   		}
   		  String totalCostString = dataCost.toPlainString();
   		  if(totalCostString.contains("."))
   		  {
   			  String splitCost = totalCostString.split("\\.")[0];
   			  if(splitCost.length()>14)
   			  {
   				  dataCell.setCellStyle(costFormatStyle);
   				  dataCell.setCellValue(dataCost.toPlainString());
   			  }
   			  else
   			  {
   				  if(Double.valueOf(splitCost).longValue() > 0)
   				  {
   					dataCell.setCellStyle(costFormatStyle);
   					  dataCell.setCellValue(dataCost.doubleValue());
   				  }
   				  else
   				  {
   					dataCell.setCellValue("NA");
   				  }
   				  
   			  }
   		  }
   		  else
   		  {
   			  if(totalCostString.length()>14)
   			  {
   				  dataCell.setCellStyle(costFormatStyle);
   				  dataCell.setCellValue(totalCostString);
   			  }
   			  else
   			  {
   				  //dataCell.setCellValue(dataCost.doubleValue());
   				 if(dataCost.longValue() > 0)
   				 {
   					 dataCell.setCellStyle(costFormatStyle);
   					 dataCell.setCellValue(dataCost.doubleValue());
 				  }
 				  else
 				  {
 					dataCell.setCellValue("NA");
 				  }
 				  
   			  }
   		  }
   		  return dataCell; 
   		   
   	   
   	}
    
    public static String displayCostType(BigDecimal dataCost)
   	{
    	/*if(dataCost==null)
   		{
   			return null; 
   		} */ 
    	String displayCost = "";
   		
   		  String totalCostString = dataCost.toPlainString();
   		  if(totalCostString.contains("."))
   		  {
   			  String splitCost = totalCostString.split("\\.")[0];
   			  if(splitCost.length()>14)
   			  {
   				 
   			  }
   			  else
   			  {
   				  if(Double.valueOf(splitCost).longValue() > 0)
   				  {
   				
   				  }
   				  else
   				  {
   					displayCost = "NA";
   				  }
   				  
   			  }
   		  }
   		  else
   		  {
   			  if(totalCostString.length()>14)
   			  {
   				  
   			  }
   			  else
   			  {
   				  //dataCell.setCellValue(dataCost.doubleValue());
   				 if(dataCost.longValue() > 0)
   				 {
 					
 				  }
 				  else
 				  {
 					 displayCost = "NA";
 				  }
 				  
   			  }
   		  }
   		  return displayCost; 
   		   
   	   
   	}
    
    public static HSSFWorkbook createExcelImage(HSSFWorkbook workbook, HSSFSheet sheet)
    {
    	 try
	       {
          		 InputStream inputStream = new FileInputStream("/usr/local/jive/applications/sbs/application/SynchroLogo.png");
          		// InputStream inputStream = new FileInputStream("D:\\SynchroLogo.png");
	            
	             byte[] bytes = IOUtils.toByteArray(inputStream);
	             
	             int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);       
	             inputStream.close();
	             
	             CreationHelper helper = workbook.getCreationHelper();
	             
	             Drawing drawing = sheet.createDrawingPatriarch();
	
	             //Create an anchor that is attached to the worksheet
	             ClientAnchor anchor = helper.createClientAnchor();
	             //set top-left corner for the image
	             anchor.setCol1(0);
	             anchor.setRow1(0);
	             Picture pict = drawing.createPicture(anchor, pictureIdx);
	             //Reset the image to the original size
	             pict.resize();
	
	      }
    	 catch(Exception e)
	      {
    		 e.printStackTrace();
	           
	     }
    	 return workbook;
    }
    
    //This method will calculate the Total Cost of project in his preferred currency
    public static BigDecimal getPreferredCurrenyTotalCost(Project project, User user)
    {
    	BigDecimal totalCost = project.getTotalCost();
    	BigDecimal preferredCurrenctTotalCost = new BigDecimal("0");
    	
    	int userLocalCurrency = getUserLocalCurrency(user);
    	if(userLocalCurrency!=-1)
    	{
    		Double exchangeRate = 1/SynchroUtils.getCurrencyExchangeRate(userLocalCurrency);
			if(totalCost!=null)
			{
				preferredCurrenctTotalCost = (BigDecimal.valueOf(totalCost.doubleValue() * exchangeRate).setScale(2, BigDecimal.ROUND_HALF_EVEN));
				return preferredCurrenctTotalCost;
			}
			
    	}
    	return null;
    	
    	
    }
    
    public static String removeQuotes(String variable)
    {
    	return variable.replace("'", "''");
    }
    
    /*
     * This method is for displaying the date in Month dd, Year format especially for OSP
     */
    public static String getModifiedDate(Date date)
    {
    	try {
        	SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");  
            String strDate = formatter.format(date);  
            return strDate;
            
        	
        } catch (Exception e) {
            
        }
    	return "";
    }
    
    
    public List<User> getOSPOracleUsers() {
        if(synchroUserGroupsMap.get(SynchroConstants.OSP_ORACLE_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.OSP_ORACLE_GROUP_NAME, getJiveGroupMembers(SynchroConstants.OSP_ORACLE_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.OSP_ORACLE_GROUP_NAME);
    }
    
    public List<User> getOSPShareUsers() {
        if(synchroUserGroupsMap.get(SynchroConstants.OSP_SHARE_GROUP_NAME).size() == 0){
            synchroUserGroupsMap.put(SynchroConstants.OSP_SHARE_GROUP_NAME, getJiveGroupMembers(SynchroConstants.OSP_SHARE_GROUP_NAME));
        }
        return synchroUserGroupsMap.get(SynchroConstants.OSP_SHARE_GROUP_NAME);
    }
}




