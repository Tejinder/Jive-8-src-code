/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.action.admin;

import com.google.common.collect.Maps;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.UserDepartment;
import com.grail.synchro.dwr.service.UserDepartmentsService;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroUserPropertiesUtil;
import java.util.*;

import com.jivesoftware.community.impl.search.user.ProfileSearchIndexManager;
import com.jivesoftware.community.lifecycle.JiveApplication;

import com.jivesoftware.base.Group;
import com.jivesoftware.base.GroupFilter;
import com.jivesoftware.base.GroupManager;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserTemplate;
import com.jivesoftware.base.util.UserPermHelper;
import com.jivesoftware.community.Avatar;
import com.jivesoftware.community.BlogManager;
import com.jivesoftware.community.BlogPost;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentResultFilter;
import com.jivesoftware.community.DocumentState;
import com.jivesoftware.community.ForumManager;
import com.jivesoftware.community.ForumMessage;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.ThreadResultFilter;
import com.jivesoftware.community.UserContainer;
import com.jivesoftware.community.UserContainerManager;
import com.jivesoftware.community.aaa.AnonymousUser;
import com.jivesoftware.community.aaa.sso.external.identity.ExternalIdentity;
import com.jivesoftware.community.aaa.sso.external.identity.ExternalIdentityManager;
import com.jivesoftware.community.action.EditProfileAction;
import com.jivesoftware.community.browse.sort.CreationDateSort;
import com.jivesoftware.community.content.blogs.BlogPostBrowseQueryBuilder;
import com.jivesoftware.community.entitlements.action.util.AdminConsoleAccessHelper;
import com.jivesoftware.community.impl.UserContainerTemplate;
import com.jivesoftware.community.license.JiveSbsInstanceType;
import com.jivesoftware.community.user.profile.security.ProfileSecurityLevel;
import com.jivesoftware.community.user.profile.security.ProfileSecurityLevelView;
import com.jivesoftware.community.user.profile.security.ProfileSecurityManager;
import com.jivesoftware.community.util.JiveContainerPermHelper;
import com.jivesoftware.community.web.struts.NoValidation;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An action for administrators to edit a user profile in the admin console
 */
public class EditUserProfile extends EditProfileAction {

    private static final long serialVersionUID = -4870414539262714923L;

    @SuppressWarnings("hiding")
    private static final Logger log = Logger.getLogger(EditUserProfile.class.getName());

    private Long userId;

    private int totalMessageCount;

    private int totalDocumentCount;

    private int totalBlogpostCount;

    private Date lastActivity;

    private List<Group> groups;
    private boolean displayGroups;

    private List<ExternalIdentity> externalIdentities;

    private Iterator<Avatar> userAvatars;

    private List<Long> avatarIDs;

    private boolean success;

    private Map<String, List<ProfileSecurityLevelView>> securityLevelOptions;

    private long nameSecurityLevelID;
    private long emailSecurityLevelID;

    private List<User.Type> userTypeOptions;

    protected BlogManager blogManager;
    protected DocumentManager documentManager;
    protected ForumManager forumManager;
    private ProfileSecurityManager profileSecurityManager;
    private ExternalIdentityManager externalIdentityManager;
    private UserContainerManager userContainerManager;
    
    private ProfileSearchIndexManager profileSearchIndexManager;
    private GroupManager groupManager;

    private Boolean fromSynchro = false;

    private Integer brand = -1;
    private Integer region = -1;
    private Integer country = -1;
    private Integer jobTitle = -1;
    private String agencyGroupName;
    private Map<Integer, String> allDepartments = new HashMap<Integer, String>();
    private Map<Integer, String> unselectedDepartments = new HashMap<Integer, String>();
    private Map<Integer, String> selectedDepartments = new HashMap<Integer, String>();
    private Map<Integer, String> unselectedBrands = new HashMap<Integer, String>();
    private Map<Integer, String> selectedBrands = new HashMap<Integer, String>();
    private Boolean globalAccessSuperUser = false;
    private Boolean regionalAccessSuperUser = false;
    private Boolean areaAccessSuperUser = false;
    private Boolean endmarketAccessSuperUser = false;
    private Map<Integer, String> unselectedRegions = new HashMap<Integer, String>();
    private Map<Integer, String> selectedRegions = new HashMap<Integer, String>();
    private Map<Integer, String> unselectedAreas = new HashMap<Integer, String>();
    private Map<Integer, String> selectedAreas = new HashMap<Integer, String>();
    private Map<Integer, String> unselectedCountries = new HashMap<Integer, String>();
    private Map<Integer, String> selectedCountries = new HashMap<Integer, String>();
    private Map<Integer, String> allRegions = new HashMap<Integer, String>();
    private Map<Integer, String> allAreas = new HashMap<Integer, String>();
    private Map<Integer, String> allCountries = new HashMap<Integer, String>();
    private boolean externalAgencyUser = false;
//    private String superUserAccessList;
    
    /**
     * Phase 5 Customizations
     * @author kanwardeep.grewal
     */
    private String legalUser = "-1";
    private String editRightsUser = "-1";
    private String waiverApproverUser = "-1";
    private String TPDUser = "-1";
    private String TPDUserEditView = "-1";
    private String currencyUser = "-1";
    

    
    public String getLegalUser() {
		return legalUser;
	}

	public void setLegalUser(String legalUser) {
		this.legalUser = legalUser;
	}

	public String getEditRightsUser() {
		return editRightsUser;
	}

	public void setEditRightsUser(String editRightsUser) {
		this.editRightsUser = editRightsUser;
	}

	public String getWaiverApproverUser() {
		return waiverApproverUser;
	}

	public void setWaiverApproverUser(String waiverApproverUser) {
		this.waiverApproverUser = waiverApproverUser;
	}

	public String getTPDUser() {
		return TPDUser;
	}

	public void setTPDUser(String tPDUser) {
		TPDUser = tPDUser;
	}

	public String getCurrencyUser() {
		return currencyUser;
	}

	public void setCurrencyUser(String currencyUser) {
		this.currencyUser = currencyUser;
	}

	public void setUserId(long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setBlogManager(BlogManager blogManager) {
        this.blogManager = blogManager;
    }

    public void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    public void setForumManager(ForumManager forumManager) {
        this.forumManager = forumManager;
    }

    public void setProfileSecurityManager(ProfileSecurityManager profileSecurityManager) {
        this.profileSecurityManager = profileSecurityManager;
    }

    public void setExternalIdentityManager(ExternalIdentityManager externalIdentityManager) {
        this.externalIdentityManager = externalIdentityManager;
    }

    public void setUserContainerManager(UserContainerManager userContainerManager) {
        this.userContainerManager = userContainerManager;
    }

    @Override
    public UserTemplate getTargetUser() {
        if (userId == null) {
            log.debug("Edit profile for unspecified user.");
        }

        if (targetUser == null) {
            if (!loadTargetUser()) {
                return null;
            }
        }

        // Admin is always allowed to set these for a user
        UserTemplate usert = new UserTemplate(targetUser);
        usert.setSetUsernameSupported(true);
        usert.setSetEmailSupported(true);
        usert.setSetNameSupported(true);
        return usert;
    }

    private boolean loadTargetUser() {
        // Load the user by ID or by username
        try {
            if (userId != null && userId > 0) {
                targetUser = userManager.getUser(userId.longValue());
            }
            else if (!StringUtils.isBlank(this.getUsername())) {
                targetUser = userManager.getUser(this.getUsername());
            }
        }
        catch (Exception e) {
            if (!StringUtils.isBlank(this.getUsername())) {
                try {
                    targetUser = userManager.getUser(this.getUsername());
                }
                catch(Exception ex) {
                    log.warn("Could not load user object for id: " + userId);
                    return false;
                }
            }
            else {
                log.warn("Could not load user object for id: " + userId);
                return false;
            }
        }
        return true;
    }

    @Override
    public long getTargetUserID() {
        if (userId != null && userId > 0) {
            return userId;
        }

        if (targetUser == null) {
            if (loadTargetUser()) {
                return targetUser.getID();
            }
        }

        return AnonymousUser.ANONYMOUS_ID;
    }

    // Required ParamsPrepareParamsStack to load parameters before prepare
    @Override
    public void prepare() {
        if (!new AdminConsoleAccessHelper().checkPageAccess(getUser(), AdminConsoleAccessHelper.MANAGE_SYSTEM,
                AdminConsoleAccessHelper.MANAGE_USERS))
        {
            throw new UnauthorizedException("You don't have admin privileges to perform this operation.");
        }

        if(SynchroPermHelper.isExternalAgencyUser(getTargetUser())) {
            externalAgencyUser = true;
        } else {
            externalAgencyUser = false;
        }

        
        if (userId == null) {
            String targetId = request.getParameter("userId");
            if (null != targetId) {
                try {
                    userId = Long.parseLong(targetId);
                }
                catch(Exception ex) {
                    addActionError("Invalid userId.");
                }
            }
        }

        if (getTargetUser() != null) {

        	
    	    allRegions = SynchroGlobal.getRegions();
            allAreas = SynchroGlobal.getAreas();
            allCountries = SynchroGlobal.getEndMarkets();

            // load user message count
            totalMessageCount = forumManager.getUserMessageCount(getTargetUser());
            totalDocumentCount = documentManager.getUserDocumentCount(getTargetUser(), new DocumentState[0]);

            BlogPostBrowseQueryBuilder blogPostBrowseQueryBuilder = browseQueryBuilderFactory.getBlogPostBrowseQueryBuilder();
            blogPostBrowseQueryBuilder.setAuthorID(getTargetUserID());
            totalBlogpostCount = blogPostBrowseQueryBuilder.getObjectCount();

            List<Date> dates = new ArrayList<Date>();
            // load last message by user
            ThreadResultFilter filter = new ThreadResultFilter();
            filter.setNumResults(1);
            filter.setSortField(JiveConstants.CREATION_DATE);
            filter.setSortOrder(ThreadResultFilter.DESCENDING);
            Iterator<ForumMessage> userMessages = forumManager.getUserMessages(getTargetUser(), filter).iterator();
            if (userMessages.hasNext()) {
                dates.add(userMessages.next().getCreationDate());
            }
            // load last document by user
            DocumentResultFilter docfilter = DocumentResultFilter.createDefaultFilter();
            docfilter.setNumResults(1);
            docfilter.setSortField(JiveConstants.CREATION_DATE);
            docfilter.setSortOrder(ThreadResultFilter.DESCENDING);
            docfilter.setUserID(getTargetUserID());
            docfilter.setRecursive(true);
            Iterable<Document> userDocuments = documentManager.getDocumentVersions(communityManager.getRootCommunity(), docfilter);
            if (userDocuments.iterator().hasNext()) {
                dates.add(userDocuments.iterator().next().getCreationDate());
            }
            // load last blogpost by user
            blogPostBrowseQueryBuilder.setStatuses(JiveContentObject.Status.PUBLISHED, JiveContentObject.Status.ABUSE_VISIBLE);
            Iterator<BlogPost> userPosts = blogPostBrowseQueryBuilder.getContentIterator(0, 1, new CreationDateSort());
            if (userPosts.hasNext()) {
                dates.add(userPosts.next().getPublishDate());
            }

            if (dates.size() > 1) {
                Collections.sort(dates);
            }

            if (dates.size() > 0) {
                lastActivity = dates.get(dates.size() - 1);
            }

            // load user groups
            displayGroups = true;
            groups = new ArrayList<Group>();
            try {
                GroupManager groupManager = getJiveContext().getGroupManager();
                groupManager.clearGroupMemberCacheForUser(getTargetUser());
                GroupFilter groupFilter = new GroupFilter.Builder().setFilterGenerated(true).setUserID(getTargetUserID()).build();
                Iterable<Group> usergroups = groupManager.getGroups(0, Integer.MAX_VALUE, groupFilter, "name");
                for (Group g : usergroups) {
                    groups.add(g);
                }
            } catch (UnauthorizedException e) {
                displayGroups = false;
            }

            // Load user external identities
            externalIdentities = externalIdentityManager.getIdentities(getTargetUser());

            // load user avatars
            userAvatars = avatarManager.getAvatars(getTargetUser()).iterator();

            super.prepare();

            securityLevelOptions = Maps.newHashMap();

            //levels for name
            List<ProfileSecurityLevel> nameAvailableLevels = profileSecurityManager
                    .getNonProfileFieldAvailableSecurityLevels(
                            ProfileSecurityManager.NAME_PROFILE_SECURITY_LEVEL_OPTIONS, getUser());
            securityLevelOptions.put("name", createProfileSecurityLevelViews(nameAvailableLevels));

            //levels for email
            List<ProfileSecurityLevel> emailAvailableLevels = profileSecurityManager
                    .getNonProfileFieldAvailableSecurityLevels(
                            ProfileSecurityManager.EMAIL_PROFILE_SECURITY_LEVEL_OPTIONS, getUser());
            securityLevelOptions.put("email", createProfileSecurityLevelViews(emailAvailableLevels));

            userTypeOptions = new ArrayList<User.Type>();

            userTypeOptions.add(getTargetUser().getType());
            if (getTargetUser().getType().equals(User.Type.PARTNER)) {
                userTypeOptions.add(User.Type.REGULAR);
            }

            try {
                User u = userManager.getUser(getTargetUserID());    //get a fresh user copy so the props are up to date
                this.nameSecurityLevelID = profileSecurityManager
                        .getNonProfileFieldSecurityLevelID(User.NAME_PROFILE_SECURITY_LEVEL, u);
                this.emailSecurityLevelID = profileSecurityManager
                        .getNonProfileFieldSecurityLevelID(User.EMAIL_PROFILE_SECURITY_LEVEL, u);
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String input() {
        if (AnonymousUser.isAnonymousUser(getTargetUserID()) || getTargetUser() == null) {
            addActionError("The user: " + getTargetUserID() + " could not be loaded.");
            return ERROR;
        }

        // todo: temp fix for admin console submitting user param
        if (getFieldErrors().size() > 0 && getFieldErrors().get("user") != null) {
            Map fieldErrors = getFieldErrors();
            fieldErrors.remove("user");
            setFieldErrors(fieldErrors);
        }

        User user = getTargetUser();
        Iterable<Group> groups = groupManager.getUserGroups(user);
        for(Group group : groups)
        {
            Map<String, String> groupProps = group.getProperties();

            if(groupProps.containsKey(SynchroConstants.SYNCHRO_GROUP_PROP))
            {
                if(groupProps.get(SynchroConstants.SYNCHRO_GROUP_PROP).equalsIgnoreCase("true"))
                {
                    fromSynchro = true;
                    break;
                }
            }
        }
        Map<String, String> properties = user.getProperties();
        if(fromSynchro)
        {

            if(properties != null && properties.size() > 0) {

                if(properties.containsKey(SynchroUserPropertiesUtil.BRAND)) {
                    String brandName = properties.get(SynchroUserPropertiesUtil.BRAND);
                    Map<Integer, String> brands = SynchroGlobal.getBrands();
                    for(Integer id : brands.keySet())
                    {
                        if(brands.get(id).equalsIgnoreCase(getDecodedString(brandName)))
                        {
                            brand = id;
                        }
                    }
                }

                if(properties.containsKey(SynchroUserPropertiesUtil.REGION)) {
                    String regionName = properties.get(SynchroUserPropertiesUtil.REGION);
                    Map<Integer, String> regions = SynchroGlobal.getRegions();
                    for(Integer id : regions.keySet())
                    {
                        if(regions.get(id).equalsIgnoreCase(getDecodedString(regionName)))
                        {
                            region = id;
                        }
                    }
                }

                if(properties.containsKey(SynchroUserPropertiesUtil.COUNTRY)) {
                    String countryName = properties.get(SynchroUserPropertiesUtil.COUNTRY);
                    Map<Integer, String> countries = SynchroGlobal.getEndMarkets();
                    for(Integer id : countries.keySet())
                    {
                        if(countries.get(id).equalsIgnoreCase(getDecodedString(countryName)))
                        {
                            country = id;
                        }
                    }
                }

                if(properties.containsKey(SynchroUserPropertiesUtil.JOB_TITLE)) {
                    String jobTitleName = properties.get(SynchroUserPropertiesUtil.JOB_TITLE);
                    Map<Integer, String> jobTitles = SynchroGlobal.getJobTitles();
                    for(Integer id : jobTitles.keySet())
                    {
                        if(jobTitles.get(id).equalsIgnoreCase(getDecodedString(jobTitleName)))
                        {
                            jobTitle = id;
                        }
                    }
                }
                
                /**
                 * Grail Phase 5
                 * @author kanwardeep.grewal
                 */
                
                if(properties.containsKey(SynchroUserPropertiesUtil.LEGAL_USER)) {
                    this.legalUser = properties.get(SynchroUserPropertiesUtil.LEGAL_USER);                    
                }
                
                if(properties.containsKey(SynchroUserPropertiesUtil.EDIT_RIGHT_USER)) {
                    this.editRightsUser = properties.get(SynchroUserPropertiesUtil.EDIT_RIGHT_USER);                    
                }
                
                if(properties.containsKey(SynchroUserPropertiesUtil.WAIVER_APPROVER_USER)) {
                    this.waiverApproverUser = properties.get(SynchroUserPropertiesUtil.WAIVER_APPROVER_USER);
                }
                
                if(properties.containsKey(SynchroUserPropertiesUtil.TPD_USER)) {
                    this.TPDUser = properties.get(SynchroUserPropertiesUtil.TPD_USER);                    
                }
                
                if(properties.containsKey(SynchroUserPropertiesUtil.TPD_USER_MANAGING_RIGHTS)) {
                    this.TPDUserEditView = properties.get(SynchroUserPropertiesUtil.TPD_USER_MANAGING_RIGHTS);                    
                }
                
                if(properties.containsKey(SynchroUserPropertiesUtil.CURRENCY_USER)) {
                    
                	String currencyid = properties.get(SynchroUserPropertiesUtil.CURRENCY_USER);
                    Map<Integer, String> currencies = SynchroGlobal.getCurrencies();
                    
                    try{
                    	if(currencyid!=null && currencyid.trim()!="")
                        {
                        	int cid = Integer.parseInt(currencyid);
                        	if(cid > 0 && currencies.containsKey(cid))
                        	{
                        		this.currencyUser = currencyid;
                        	}
                        }
                    }catch(Exception e){
                    	System.out.println("Exception while fetching currency from user props " + e.getStackTrace());
                    	}
                }
            }


            allDepartments = SynchroGlobal.getDepartments();

            if(properties.containsKey(SynchroUserPropertiesUtil.AGENCY_GROUP_NAME)) {
                agencyGroupName = properties.get(SynchroUserPropertiesUtil.AGENCY_GROUP_NAME);
            }

            if(properties.containsKey(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_DEPARTMENTS)) {
                String departments = properties.get(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_DEPARTMENTS);
                if(departments != null && !departments.equals("")) {
                    String [] depIds = departments.split(",");
                    unselectedDepartments = new HashMap<Integer, String>();
                    selectedDepartments = new HashMap<Integer, String>();
                    Set<Integer> departmentKeySet = allDepartments.keySet();
                    for(Integer departmentKey: departmentKeySet) {
                        boolean exists = false;
                        for(String depId : depIds) {
                            if(Integer.parseInt(depId) == departmentKey) {
                                exists = true;
                                break;
                            }
                        }
                        if(exists) {
                            selectedDepartments.put(departmentKey, allDepartments.get(departmentKey));
                        } else {
                            unselectedDepartments.put(departmentKey, allDepartments.get(departmentKey));
                        }
                    }
                } else {
                    selectedDepartments = new HashMap<Integer, String>();
                    unselectedDepartments = allDepartments;
                }
            } else {
                selectedDepartments = new HashMap<Integer, String>();
                unselectedDepartments = allDepartments;
            }

            Map<Integer, String> brandsMap = SynchroGlobal.getBrands();
            if(properties.containsKey(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_BRANDS)) {
                String brands = properties.get(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_BRANDS);
                if(brands != null && !brands.equals("")) {
                    String [] bids = brands.split(",");
                    Set<Integer> brandsKeySet = brandsMap.keySet();
                    unselectedBrands = new HashMap<Integer, String>();
                    selectedBrands = new HashMap<Integer, String>();
                    for(Integer brandKey : brandsKeySet) {
                        boolean exists = false;
                        for(String bid : bids) {
                            if(brandKey == Integer.parseInt(bid)) {
                                exists = true;
                            }
                        }
                        if(exists) {
                            selectedBrands.put(brandKey, brandsMap.get(brandKey));
                        } else {
                            unselectedBrands.put(brandKey, brandsMap.get(brandKey));
                        }
                    }
                } else {
                    selectedBrands = new HashMap<Integer, String>();
                    unselectedBrands = brandsMap;
                }
            } else {
                selectedBrands = new HashMap<Integer, String>();
                unselectedBrands = brandsMap;
            }


            if(properties.containsKey(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER)
                    && properties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER) != null
                    && !properties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER).equals("")) {
                globalAccessSuperUser = Boolean.parseBoolean(properties.get(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER));
            }

            if(properties.containsKey(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER)
                    && properties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER) != null
                    && !properties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER).equals("")) {
                regionalAccessSuperUser = Boolean.parseBoolean(properties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER));
            }

            if(properties.containsKey(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER)
                    && properties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER) != null
                    && !properties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER).equals("")) {
                areaAccessSuperUser = Boolean.parseBoolean(properties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER));
            }
            
            if(properties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_SUPER_USER)
                    && properties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_SUPER_USER) != null
                    && !properties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_SUPER_USER).equals("")) {
            	endmarketAccessSuperUser = Boolean.parseBoolean(properties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_SUPER_USER));
            }

            if(properties.containsKey(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST)
                    && properties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST) != null
                    && !properties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST).equals("")) {
                String regionalAccessList = properties.get(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST);
                String [] regionalAccessIds = regionalAccessList.split(",");
                Set<Integer> regionsKeySet = allRegions.keySet();
                for(Integer regionKey : regionsKeySet) {
                    boolean exists = false;
                    for(String regionalAccessId : regionalAccessIds) {
                        if(regionKey == Integer.parseInt(regionalAccessId)) {
                            exists = true;
                            break;
                        }
                    }
                    if(exists) {
                        selectedRegions.put(regionKey, allRegions.get(regionKey));
                    } else {
                        unselectedRegions.put(regionKey, allRegions.get(regionKey));
                    }
                }
            } else {
                unselectedRegions = allRegions;
            }

            if(properties.containsKey(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_LIST)
                    && properties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_LIST) != null
                    && !properties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_LIST).equals("")) {
                String areaAccessList = properties.get(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_LIST);
                String [] areasAccessIds = areaAccessList.split(",");
                Set<Integer> areaKeySet = allAreas.keySet();
                for(Integer areaKey : areaKeySet) {
                    boolean exists = false;
                    for(String areaAccessId : areasAccessIds) {
                        if(areaKey == Integer.parseInt(areaAccessId)) {
                            exists = true;
                            break;
                        }
                    }
                    if(exists) {
                        selectedAreas.put(areaKey, allAreas.get(areaKey));
                    } else {
                        unselectedAreas.put(areaKey, allAreas.get(areaKey));
                    }
                }
            } else {
                unselectedAreas = allAreas;
            }

            if(properties.containsKey(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST)
                    && properties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST) != null
                    && !properties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST).equals("")) {
                String endmarketAccessList = properties.get(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST);
                String [] endmarketsAccessIds = endmarketAccessList.split(",");
                Set<Integer> endmarketsKeySet = allCountries.keySet();
                for(Integer endmarketKey : endmarketsKeySet) {
                    boolean exists = false;
                    for(String endmarketAccessId : endmarketsAccessIds) {
                        if(endmarketKey == Integer.parseInt(endmarketAccessId)) {
                            exists = true;
                            break;
                        }
                    }
                    if(exists) {
                        selectedCountries.put(endmarketKey, allCountries.get(endmarketKey));
                    } else {
                        unselectedCountries.put(endmarketKey, allCountries.get(endmarketKey));
                    }
                }
            } else {
                unselectedCountries = allCountries;
            }
        }




//            if(properties.containsKey(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_SUPER_USER_ACCESS_LIST)) {
//                if(accessType != null && accessType > 0) {
//                    String superUserAccessList = properties.get(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_SUPER_USER_ACCESS_LIST);
//                    unselectedAreas = new HashMap<Integer, String>();
//                    selectedAreas = new HashMap<Integer, String>();
//                    unselectedRegions = new HashMap<Integer, String>();
//                    selectedRegions = new HashMap<Integer, String>();
//                    unselectedCountries = new HashMap<Integer, String>();
//                    selectedCountries = new HashMap<Integer, String>();
//                    if(superUserAccessList != null && !superUserAccessList.equals("")) {
//                        String [] userAccessIds = superUserAccessList.split(",");
//                        if(accessType == SynchroGlobal.InvestmentType.GlOBAL.getId()) {
//                            selectedRegions = new HashMap<Integer, String>();
//                            unselectedRegions = allRegions;
//
//                            selectedAreas = new HashMap<Integer, String>();
//                            unselectedAreas = allAreas;
//
//                            selectedCountries = new HashMap<Integer, String>();
//                            unselectedCountries = allCountries;
//                        } else if(accessType == SynchroGlobal.InvestmentType.REGION.getId()) {
//                            selectedRegions = new HashMap<Integer, String>();
//                            unselectedRegions = new HashMap<Integer, String>();
//
//                            Set<Integer> regionsKeySet = allRegions.keySet();
//                            for(Integer regionKey : regionsKeySet) {
//                                boolean exists = false;
//                                for(String userAccessId : userAccessIds) {
//                                    if(regionKey == Integer.parseInt(userAccessId)) {
//                                        exists = true;
//                                        break;
//                                    }
//                                }
//                                if(exists) {
//                                    selectedRegions.put(regionKey, allRegions.get(regionKey));
//                                } else {
//                                    unselectedRegions.put(regionKey, allRegions.get(regionKey));
//                                }
//                            }
//
//
//                            selectedAreas = new HashMap<Integer, String>();
//                            unselectedAreas = allAreas;
//
//                            selectedCountries = new HashMap<Integer, String>();
//                            unselectedCountries = allCountries;
//                        } else if(accessType == SynchroGlobal.InvestmentType.AREA.getId()) {
//                            selectedRegions = new HashMap<Integer, String>();
//                            unselectedRegions = allRegions;
//
//                            selectedAreas = new HashMap<Integer, String>();
//                            unselectedAreas = new HashMap<Integer, String>();
//                            Set<Integer> areasKeySet = allAreas.keySet();
//                            for(Integer areaKey : areasKeySet) {
//                                boolean exists = false;
//                                for(String userAccessId : userAccessIds) {
//                                    if(areaKey == Integer.parseInt(userAccessId)) {
//                                        exists = true;
//                                        break;
//                                    }
//                                }
//                                if(exists) {
//                                    selectedAreas.put(areaKey, allAreas.get(areaKey));
//                                } else {
//                                    unselectedAreas.put(areaKey, allAreas.get(areaKey));
//                                }
//                            }
//
//                            selectedCountries = new HashMap<Integer, String>();
//                            unselectedCountries = allCountries;
//                        } else if(accessType == SynchroGlobal.InvestmentType.COUNTRY.getId()) {
//                            selectedRegions = new HashMap<Integer, String>();
//                            unselectedRegions = allRegions;
//
//                            selectedAreas = new HashMap<Integer, String>();
//                            unselectedAreas = allAreas;
//
//                            selectedCountries = new HashMap<Integer, String>();
//                            unselectedCountries = new HashMap<Integer, String>();
//                            Set<Integer> countriesKeySet = allCountries.keySet();
//                            for(Integer countryKey : countriesKeySet) {
//                                boolean exists = false;
//                                for(String userAccessId : userAccessIds) {
//                                    if(countryKey == Integer.parseInt(userAccessId)) {
//                                        exists = true;
//                                        break;
//                                    }
//                                }
//                                if(exists) {
//                                    selectedCountries.put(countryKey, allCountries.get(countryKey));
//                                } else {
//                                    unselectedCountries.put(countryKey, allCountries.get(countryKey));
//                                }
//                            }
//                        }
//                    } else {
//                        selectedRegions = new HashMap<Integer, String>();
//                        unselectedRegions = allRegions;
//
//                        selectedAreas = new HashMap<Integer, String>();
//                        unselectedAreas = allCountries;
//
//                        selectedCountries = new HashMap<Integer, String>();
//                        unselectedCountries = allCountries;
//                    }
//                } else {
//                    selectedRegions = new HashMap<Integer, String>();
//                    unselectedRegions = allRegions;
//
//                    selectedAreas = new HashMap<Integer, String>();
//                    unselectedAreas = allAreas;
//
//                    selectedCountries = new HashMap<Integer, String>();
//                    unselectedCountries = allCountries;
//                }
//            } else {
//                selectedRegions = new HashMap<Integer, String>();
//                unselectedRegions = allRegions;
//
//                selectedAreas = new HashMap<Integer, String>();
//                unselectedAreas = allAreas;
//
//                selectedCountries = new HashMap<Integer, String>();
//                unselectedCountries = allCountries;
//            }
//        }


        return super.input();
    }

    @Override
    public String execute() {
        if (AnonymousUser.isAnonymousUser(getTargetUserID())) {
            addActionError("The user: " + getTargetUserID() + " could not be loaded.");
            return ERROR;
        }

        try {
            User u = userManager.getUser(getTargetUserID());
            profileSecurityManager
                    .setNonProfileFieldSecurityLevelID(User.NAME_PROFILE_SECURITY_LEVEL, u, nameSecurityLevelID);
            profileSecurityManager
                    .setNonProfileFieldSecurityLevelID(User.EMAIL_PROFILE_SECURITY_LEVEL, u, emailSecurityLevelID);
            userManager.updateUser(u);
            if (u.isEnabled()) {
                UserContainer userContainer = userContainerManager.getUserContainer(u);
                if (userContainer == null) {
                    if (!(u.isExternal()) && u.isVisible()) {
                        UserContainerTemplate template = new UserContainerTemplate();
                        template.setUserID(u.getID());
                        template.setStatus(JiveContainer.Status.ACTIVE);
                        userContainerManager.createUserContainer(new UserContainerTemplate(template));
                    }
                }
            }
        }
        catch (Exception e) {
            log.warn("Couldn't persist security levels for user " + getTargetUser(), e);
        }

        UserTemplate user = getTargetUser();
        Boolean userChanged = false;
        Iterable<Group> groups = groupManager.getUserGroups(user);
        for(Group group : groups)
        {
            Map<String, String> groupProps = group.getProperties();

            if(groupProps.containsKey(SynchroConstants.SYNCHRO_GROUP_PROP))
            {
                if(groupProps.get(SynchroConstants.SYNCHRO_GROUP_PROP).equalsIgnoreCase("true"))
                {
                    fromSynchro = true;
                    break;
                }
            }
        }

        Map<String, String> properties = user.getProperties();
        if(fromSynchro)
        {

            if(properties != null && properties.size() > 0) {

                if(brand!=null)
                {
                    Map<Integer, String> brands = SynchroGlobal.getBrands();
                    if(brand<0)
                    {
                        properties.remove(SynchroUserPropertiesUtil.BRAND);
                        userChanged = true;
                    }
                    else if(brands.containsKey(brand))
                    {
                        String brandName = getEncodedText(brands.get(brand));
                        properties.put(SynchroUserPropertiesUtil.BRAND, brandName);
                        userChanged = true;
                    }
                }

                if(region!=null)
                {
                    Map<Integer, String> regions = SynchroGlobal.getRegions();
                    if(region<0)
                    {
                        properties.remove(SynchroUserPropertiesUtil.REGION);
                        userChanged = true;
                    }
                    else if(regions.containsKey(region))
                    {
                        String regionName = getEncodedText(regions.get(region));
                        properties.put(SynchroUserPropertiesUtil.REGION, regionName);
                        userChanged = true;
                    }
                }

                if(country!=null)
                {
                    Map<Integer, String> countries = SynchroGlobal.getEndMarkets();
                    if(country<0)
                    {
                        properties.remove(SynchroUserPropertiesUtil.COUNTRY);
                        userChanged = true;
                    }
                    else if(countries.containsKey(country))
                    {
                        String countryName = getEncodedText(countries.get(country));
                        properties.put(SynchroUserPropertiesUtil.COUNTRY, countryName);
                        userChanged = true;
                    }
                }

                if(jobTitle!=null)
                {
                    Map<Integer, String> jobTitles = SynchroGlobal.getJobTitles();
                    if(jobTitle<0)
                    {
                        properties.remove(SynchroUserPropertiesUtil.JOB_TITLE);
                        userChanged = true;
                    }
                    else if(jobTitles.containsKey(jobTitle))
                    {
                        String jobTitleName = getEncodedText(jobTitles.get(jobTitle));
                        properties.put(SynchroUserPropertiesUtil.JOB_TITLE, jobTitleName);
                        userChanged = true;
                    }
                }

                
                /**
                 * Synchro Phase 5
                 * @author kanwardeep.grewal
                 * 
                 * private String legalUser = "-1";
				    private String editRightsUser = "-1";
				    private String waiverApproverUser = "-1";
				    private String TPDUser = "-1";
				    private String currencyUser = "-1";
                 */
                if(legalUser!="-1")
                {
                	 properties.put(SynchroUserPropertiesUtil.LEGAL_USER, legalUser);
                     userChanged = true;
                }
                
                if(editRightsUser!="-1")
                {
                	 properties.put(SynchroUserPropertiesUtil.EDIT_RIGHT_USER, editRightsUser);
                     userChanged = true;
                }
                
                if(waiverApproverUser!="-1")
                {
                	 properties.put(SynchroUserPropertiesUtil.WAIVER_APPROVER_USER, waiverApproverUser);
                     userChanged = true;
                }
                
                if(TPDUser!="-1")
                {
                	 properties.put(SynchroUserPropertiesUtil.TPD_USER, TPDUser);
                     userChanged = true;
                }
                
                if(TPDUserEditView!="-1")
                {
                	 properties.put(SynchroUserPropertiesUtil.TPD_USER_MANAGING_RIGHTS, TPDUserEditView);
                     userChanged = true;
                }
                
                if(currencyUser!="-1")
                {
                	 properties.put(SynchroUserPropertiesUtil.CURRENCY_USER, currencyUser);
                     userChanged = true;
                }
                
                if(agencyGroupName != null) {
                    properties.put(SynchroUserPropertiesUtil.AGENCY_GROUP_NAME, agencyGroupName);
                    userChanged = true;
                } else {
                    properties.remove(SynchroUserPropertiesUtil.AGENCY_GROUP_NAME);
                    userChanged = true;
                }

                String departments = request.getParameter("departments");
                if(departments != null && !departments.equals("")) {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_DEPARTMENTS, departments);
                    userChanged = true;
                } else {
                    properties.remove(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_DEPARTMENTS);
                    userChanged = true;
                }
                String brands = request.getParameter("brands");
                if(brands != null && !brands.equals("")) {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_BRANDS, brands);
                    userChanged = true;
                } else {
                    properties.remove(SynchroUserPropertiesUtil.GRAIL_USER_PROFILE_BRANDS);
                    userChanged = true;
                }

                String globalAccessSuperUser = request.getParameter("globalAccessSuperUser");
                if(globalAccessSuperUser != null && !globalAccessSuperUser.equals("")) {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER, globalAccessSuperUser.toString());
                    userChanged = true;
                } else {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_GLOBAL_ACCESS_SUPER_USER, "false");
                    userChanged = true;
                }

                String regionalAccessSuperUser = request.getParameter("regionalAccessSuperUser");
                if(regionalAccessSuperUser != null && !regionalAccessSuperUser.equals("")) {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER, regionalAccessSuperUser.toString());
                    userChanged = true;
                } else {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_SUPER_USER, "false");
                    userChanged = true;
                }

                String regionalAccessList = request.getParameter("regionalAccessList");
                if(regionalAccessList != null && !regionalAccessList.equals("")) {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST, regionalAccessList.toString());
                    userChanged = true;
                } else {
                    properties.remove(SynchroUserPropertiesUtil.GRAIL_REGIONAL_ACCESS_LIST);
                    userChanged = true;
                }

                String areaAccessSuperUser = request.getParameter("areaAccessSuperUser");
                if(areaAccessSuperUser != null && !areaAccessSuperUser.equals("")) {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER, areaAccessSuperUser.toString());
                    userChanged = true;
                } else {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_SUPER_USER, "false");
                    userChanged = true;
                }
                
                
                String areaAccessList = request.getParameter("areaAccessList");
                if(areaAccessList != null && !areaAccessList.equals("")) {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_LIST, areaAccessList.toString());
                    userChanged = true;
                } else {
                    properties.remove(SynchroUserPropertiesUtil.GRAIL_AREA_ACCESS_LIST);
                    userChanged = true;
                }

                /**
                 * Synchro Phase 5
                 * @author kanwardeep.grewal
                 */
                String endmarketAccessSuperUser = request.getParameter("endmarketAccessSuperUser");
                if(endmarketAccessSuperUser != null && !endmarketAccessSuperUser.equals("")) {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_SUPER_USER, endmarketAccessSuperUser.toString());
                    userChanged = true;
                } else {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_SUPER_USER, "false");
                    userChanged = true;
                }

                
                String endmarketAccessList = request.getParameter("endmarketAccessList");
                if(endmarketAccessList != null && !endmarketAccessList.equals("")) {
                    properties.put(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST, endmarketAccessList.toString());
                    userChanged = true;
                } else {
                    properties.remove(SynchroUserPropertiesUtil.GRAIL_ENDMARKET_ACCESS_LIST);
                    userChanged = true;
                }

                try{
                    if (userChanged) {
                        user.setLastProfileUpdate(new Date());
                        userManager.updateUser(user);
                        profileSearchIndexManager.updateIndex(user);
                        //profileSearchIndexManager.rebuildIndex();
                    }

                } catch(Exception e){
                	log.error("User already exists exception custom Edit Profile Action " + e.getMessage());
                }
            }
        }


        return super.execute();
    }

    @NoValidation
    public String doDeleteAvatars() {
        if (AnonymousUser.isAnonymousUser(getTargetUserID())) {
            addActionError("The user: " + getTargetUserID() + " could not be loaded.");
            return ERROR;
        }

        if (avatarIDs != null && avatarIDs.size() > 0) {
            Avatar avatar;
            for (Long avatarID : avatarIDs) {
                avatar = avatarManager.getAvatar(avatarID);
                avatarManager.deleteAvatar(avatar);
            }
        }

        return SUCCESS;
    }

    public int getTotalMessageCount() {
        return totalMessageCount;
    }

    public int getTotalDocumentCount() {
        return totalDocumentCount;
    }

    public int getTotalBlogpostCount() {
        return totalBlogpostCount;
    }

    public Date getLastActivity() {
        return lastActivity;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public Iterator<Avatar> getUserAvatars() {
        return userAvatars;
    }

    public List<Long> getAvatarIDs() {
        return avatarIDs;
    }

    public void setAvatarIDs(List<Long> avatarIDs) {
        this.avatarIDs = avatarIDs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isAdmin() {
        return true;
    }

    public boolean isUpdateSupported() {
        return userManager.isCreationSupported();
    }

    public Map<String, List<ProfileSecurityLevelView>> getSecurityLevelOptions() {
        return securityLevelOptions;
    }

    public List<User.Type> getUserTypeOptions() {
        return userTypeOptions;
    }

    public long getNameSecurityLevelID() {
        return nameSecurityLevelID;
    }

    public void setNameSecurityLevelID(long nameSecurityLevelID) {
        this.nameSecurityLevelID = nameSecurityLevelID;
    }

    public long getEmailSecurityLevelID() {
        return emailSecurityLevelID;
    }

    public void setEmailSecurityLevelID(long emailSecurityLevelID) {
        this.emailSecurityLevelID = emailSecurityLevelID;
    }

    public boolean getCanChangePassword() {
        if (targetUser == null || targetUser.isAnonymous()) {
            return false;
        }

        // If admin (current user) is a sys-admin he can change the pwd.
        if (JiveContainerPermHelper.isGlobalAdmin()) {
            return true;
        }

        // A user-admin cannot change the password of a sys-admin.
        if (JiveContainerPermHelper.isGlobalUserAdmin()) {
            if (JiveContainerPermHelper.isGlobalAdmin(targetUser.getID())) {
                return false;
            }
        }
        return true;
    }

    public boolean isDisplayEditControls() {
        return UserPermHelper.getCanManageUser(getTargetUser());
    }

    public List<ExternalIdentity> getExternalIdentities() {
        return externalIdentities;
    }

    public boolean isDisplayGroups() {
        return displayGroups;
    }

    public boolean getDevInstallation() {
        JiveSbsInstanceType instanceType = licenseManager.getInstanceType();
        return instanceType == JiveSbsInstanceType.developer;
    }
    
    public ProfileSearchIndexManager getProfileSearchIndexManager() {
        return profileSearchIndexManager;
    }

    public void setProfileSearchIndexManager(
            ProfileSearchIndexManager profileSearchIndexManager) {
        this.profileSearchIndexManager = profileSearchIndexManager;
    }

    public Boolean getFromSynchro() {
        return fromSynchro;
    }

    public void setFromSynchro(Boolean fromSynchro) {
        this.fromSynchro = fromSynchro;
    }

    public Integer getBrand() {
        return brand;
    }

    public void setBrand(Integer brand) {
        this.brand = brand;
    }

    public Integer getRegion() {
        return region;
    }

    public void setRegion(Integer region) {
        this.region = region;
    }

    public Integer getCountry() {
        return country;
    }

    public void setCountry(Integer country) {
        this.country = country;
    }

    public Integer getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(Integer jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getAgencyGroupName() {
        return agencyGroupName;
    }

    public void setAgencyGroupName(String agencyGroupName) {
        this.agencyGroupName = agencyGroupName;
    }

    public Map<Integer, String> getAllDepartments() {
        return allDepartments;
    }

    public void setAllDepartments(Map<Integer, String> allDepartments) {
        this.allDepartments = allDepartments;
    }

    public Map<Integer, String> getUnselectedDepartments() {
        return unselectedDepartments;
    }

    public void setUnselectedDepartments(Map<Integer, String> unselectedDepartments) {
        this.unselectedDepartments = unselectedDepartments;
    }

    public Map<Integer, String> getSelectedDepartments() {
        return selectedDepartments;
    }

    public void setSelectedDepartments(Map<Integer, String> selectedDepartments) {
        this.selectedDepartments = selectedDepartments;
    }

    public Map<Integer, String> getUnselectedBrands() {
        return unselectedBrands;
    }

    public void setUnselectedBrands(Map<Integer, String> unselectedBrands) {
        this.unselectedBrands = unselectedBrands;
    }

    public Map<Integer, String> getSelectedBrands() {
        return selectedBrands;
    }

    public void setSelectedBrands(Map<Integer, String> selectedBrands) {
        this.selectedBrands = selectedBrands;
    }

    public Boolean getRegionalAccessSuperUser() {
        return regionalAccessSuperUser;
    }

    public void setRegionalAccessSuperUser(Boolean regionalAccessSuperUser) {
        this.regionalAccessSuperUser = regionalAccessSuperUser;
    }

    public Boolean getAreaAccessSuperUser() {
        return areaAccessSuperUser;
    }

    public void setAreaAccessSuperUser(Boolean areaAccessSuperUser) {
        this.areaAccessSuperUser = areaAccessSuperUser;
    }

    public Boolean getGlobalAccessSuperUser() {
        return globalAccessSuperUser;
    }

    public void setGlobalAccessSuperUser(Boolean globalAccessSuperUser) {
        this.globalAccessSuperUser = globalAccessSuperUser;
    }

    public Boolean getEndmarketAccessSuperUser() {
		return endmarketAccessSuperUser;
	}

	public void setEndmarketAccessSuperUser(Boolean endmarketAccessSuperUser) {
		this.endmarketAccessSuperUser = endmarketAccessSuperUser;
	}

	public Map<Integer, String> getUnselectedRegions() {
        return unselectedRegions;
    }

    public void setUnselectedRegions(Map<Integer, String> unselectedRegions) {
        this.unselectedRegions = unselectedRegions;
    }

    public Map<Integer, String> getSelectedRegions() {
        return selectedRegions;
    }

    public void setSelectedRegions(Map<Integer, String> selectedRegions) {
        this.selectedRegions = selectedRegions;
    }

    public Map<Integer, String> getUnselectedAreas() {
        return unselectedAreas;
    }

    public void setUnselectedAreas(Map<Integer, String> unselectedAreas) {
        this.unselectedAreas = unselectedAreas;
    }

    public Map<Integer, String> getSelectedAreas() {
        return selectedAreas;
    }

    public void setSelectedAreas(Map<Integer, String> selectedAreas) {
        this.selectedAreas = selectedAreas;
    }

    public Map<Integer, String> getUnselectedCountries() {
        return unselectedCountries;
    }

    public void setUnselectedCountries(Map<Integer, String> unselectedCountries) {
        this.unselectedCountries = unselectedCountries;
    }

    public Map<Integer, String> getSelectedCountries() {
        return selectedCountries;
    }

    public void setSelectedCountries(Map<Integer, String> selectedCountries) {
        this.selectedCountries = selectedCountries;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    public Map<Integer, String> getAllRegions() {
        return allRegions;
    }

    public void setAllRegions(Map<Integer, String> allRegions) {
        this.allRegions = allRegions;
    }

    public Map<Integer, String> getAllAreas() {
        return allAreas;
    }

    public void setAllAreas(Map<Integer, String> allAreas) {
        this.allAreas = allAreas;
    }

    public Map<Integer, String> getAllCountries() {
        return allCountries;
    }

    public void setAllCountries(Map<Integer, String> allCountries) {
        this.allCountries = allCountries;
    }

    public boolean getExternalAgencyUser() {
        return externalAgencyUser;
    }

    public void setExternalAgencyUser(boolean externalAgencyUser) {
        externalAgencyUser = externalAgencyUser;
    }

    private String getEncodedText(String str)
    {
        String encoded_text = str.replaceAll(" ", "%20A");
        return encoded_text;
    }

    private String getDecodedString(String str)
    {
        return str.replaceAll("%20A", " ");
    }

	public String getTPDUserEditView() {
		return TPDUserEditView;
	}

	public void setTPDUserEditView(String tPDUserEditView) {
		TPDUserEditView = tPDUserEditView;
	}
}
