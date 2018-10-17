/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.impl.search.user;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.util.SynchroUserPropertiesUtil;
import com.jivesoftware.base.Group;
import com.jivesoftware.base.GroupManager;
import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveContext;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.search.user.UserSearchInfo;
import com.jivesoftware.community.statuslevel.StatusLevelManager;
import com.jivesoftware.community.user.profile.ProfileField;
import com.jivesoftware.community.user.profile.ProfileFieldManager;
import com.jivesoftware.community.user.profile.ProfileFieldValue;
import com.jivesoftware.community.user.profile.security.ProfileSecurityManager;
import com.jivesoftware.util.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;
/**
 * Converts a {@link com.jivesoftware.community.search.user.UserSearchInfo} object into a {@link com.jivesoftware.community.impl.search.user.ProfileSearchFieldsData}
 * object by flattening out any necessary data and apply any necessary security rules to that data.
 *
 * <p>Implementations of profile/user search index should use this object to get the data to be put into the actual
 * index.</p>
 */
public class ProfileSearchFieldsDataCollator {

    private StatusLevelManager statusLevelManager;
    private ProfileSecurityManager profileSecurityManager;
    private ProfileFieldManager profileFieldManager;
    private GroupManager groupManager;
    
    @Required
    public void setGroupManager(GroupManager groupManager) {
		this.groupManager = groupManager;
	}

    @Required
    public void setStatusLevelManager(StatusLevelManager statusLevelManager) {
        this.statusLevelManager = statusLevelManager;
    }

    @Required
    public void setProfileSecurityManager(ProfileSecurityManager profileSecurityManager) {
        this.profileSecurityManager = profileSecurityManager;
    }

    @Required
    public void setProfileFieldManager(ProfileFieldManager profileFieldManager) {
        this.profileFieldManager = profileFieldManager;
    }

    public ProfileSearchFieldsData collate(UserSearchInfo userInfo) {
        if (userInfo == null) {
            return null;
        }

        //build up a map of ACLs for this user
        Map<Long, String> aclMap = userInfo.userAclMap;

        long creationDateLevelID = profileSecurityManager.getNonProfileFieldSecurityLevelID(User.CREATION_DATE_PROFILE_SECURITY_LEVEL, userInfo.user);
        String creationDateACL = StringUtils.trimToEmpty(aclMap.get(creationDateLevelID));

        // Privacy for modification date is the same as for creation date
        long modificationDateLevelID = profileSecurityManager.getNonProfileFieldSecurityLevelID(User.CREATION_DATE_PROFILE_SECURITY_LEVEL, userInfo.user);
        String modificationDateACL = StringUtils.trimToEmpty(aclMap.get(modificationDateLevelID));

        // Privacy for modification date is the same as for creation date
        long initialLoginDateSecurityLevelID = profileSecurityManager.getNonProfileFieldSecurityLevelID(User.CREATION_DATE_PROFILE_SECURITY_LEVEL, userInfo.user);
        String initialLoginDateACL = StringUtils.trimToEmpty(aclMap.get(initialLoginDateSecurityLevelID));

        // Privacy for last profile update is the same as for creation date
        long lastProfileUpdateLevelID = profileSecurityManager.getNonProfileFieldSecurityLevelID(User.CREATION_DATE_PROFILE_SECURITY_LEVEL, userInfo.user);
        String lastProfileUpdateACL = StringUtils.trimToEmpty(aclMap.get(lastProfileUpdateLevelID));

        long nameLevelID = profileSecurityManager.getNonProfileFieldSecurityLevelID(User.NAME_PROFILE_SECURITY_LEVEL, userInfo.user);
        String nameACL = "";
        if (userInfo.user.getName() != null) {
            nameACL = StringUtils.trimToEmpty(aclMap.get(nameLevelID));
        }

        long emailLevelID = profileSecurityManager.getNonProfileFieldSecurityLevelID(User.EMAIL_PROFILE_SECURITY_LEVEL, userInfo.user);
        String emailACL = "";
        if (userInfo.user.getEmail() != null) {
            emailACL = StringUtils.trimToEmpty(aclMap.get(emailLevelID));
        }

        // add user profile information
        Map<Long, StringBuilder> keywordMap = new HashMap<Long, StringBuilder>();

        List<UserProfileFieldSearchData> profileFieldsData = Lists.newLinkedList();
        Map<Long, ProfileFieldValue> userProfile = userInfo.profile;
        for (ProfileFieldValue up : userProfile.values()) {
            ProfileField profileField = profileFieldManager.getProfileField(up.getFieldID());

            // ensure profile field exists. it could be null if the field was deleted but the user
            // values were not cleared.
            List<String> fieldData = Lists.newLinkedList();
            if (profileField != null) {
                boolean bigText = profileField.getType().equals(ProfileField.Type.LARGETEXT);

                boolean multiList = profileField.getType().equals(ProfileField.Type.MULTILIST);
                String keywordAddition = "";

                long securityLevelID = up.getEffectiveSecurityLevelID();

                if (bigText && up.getValues() != null && !up.getValues().isEmpty()) {
                    StringBuilder values = new StringBuilder();
                    for (String value : up.getValues()) {
                        if (StringUtils.isBlank(value)) {
                            continue;
                        }
                        values.append(value).append(" ");
                    }

                    //store a security column for relative keywords with userIDs
                    fieldData.add(values.toString());

                    if (profileField.isSearchable()) {
                        keywordAddition = values.toString();
                    }
                }
                else if (multiList && up.getValues() != null && !up.getValues().isEmpty()) {
                    StringBuilder values = new StringBuilder();
                    for (String value : up.getValues()) {
                        if (StringUtils.isBlank(value)) {
                            continue;
                        }
                        fieldData.add(value);
                        values.append(value).append(" ");
                    }

                    if (profileField.isSearchable()) {
                        keywordAddition = values.toString();
                    }
                }
                else if (StringUtils.isNotBlank(up.getValue())) {

                    if (profileField.getType() == ProfileField.Type.NUMBER) {
                        // CS-22602 -- strip all whitespace as numbers may be populated (incorrectly) via external sources
                        String val = StringUtils.stripWhitespace(up.getValue());
                        // CS-18569: trim before attempting to parse since the ProfileField framework doesn't take care
                        // of this elsewhere.
                        val = StringUtils.trim(val);
                        long value = NumberUtils.toLong(val, Long.MIN_VALUE);
                        if (value != Long.MIN_VALUE) {
                            fieldData.add(Long.toString(value));
                        }
                        if (profileField.isSearchable()) {
                            keywordAddition = new StringBuilder().append(up.getValue()).append(" ").toString();
                        }
                    }
                    else if (profileField.getType() == ProfileField.Type.DECIMAL) {
                        // CS-22602 -- strip all whitespace as numbers may be populated (incorrectly) via external sources
                        String val = StringUtils.stripWhitespace(up.getValue());
                        // CS-18569: trim before attempting to parse since the ProfileField framework doesn't take care
                        // of this elsewhere.
                        val = StringUtils.trim(val);
                        double value = NumberUtils.toDouble(val, Double.MIN_VALUE);
                        if (value != Double.MIN_VALUE) {
                            fieldData.add(Double.toString(value));
                        }
                    }

                    else if (profileField.getType() == ProfileField.Type.ADDRESS) {
                        if (profileField.isSearchable()) {
                            keywordAddition = StringUtils.stripAddressTags(up.getValue());
                        }
                    }

                    else if (profileField.getType() == ProfileField.Type.PHONE_NUMBER) {
                        if (profileField.isSearchable()) {
                            keywordAddition = StringUtils.stripPhoneNumbers(up.getValue());
                        }
                    }

                    else {
                        fieldData.add(up.getValue());
                        if (profileField.isSearchable()) {
                            keywordAddition = new StringBuilder().append(up.getValue()).append(" ").toString();
                        }
                    }
                }

                profileFieldsData.add(new UserProfileFieldSearchData(profileField.getID(), fieldData,
                        profileField.getType(), StringUtils.trimToEmpty(aclMap.get(securityLevelID)),
                        securityLevelID));

                if (profileField.isSearchable() && keywordAddition.length() > 0) {
                    //add keywords to security level map
                    StringBuilder keywords = keywordMap.get(securityLevelID);
                    if (keywords == null) {
                        keywords = new StringBuilder();
                        keywordMap.put(securityLevelID, keywords);
                    }
                    keywords.append(keywordAddition);
                }
                
                
            }
        }

        List<ProfileSearchKeywordsData> keywordsData = Lists.newLinkedList();
        for (Long levelID : keywordMap.keySet()) {
            keywordsData.add(new ProfileSearchKeywordsData(keywordMap.get(levelID).toString(),
                    StringUtils.trimToEmpty(aclMap.get(levelID)), levelID));
        }

        //store lastLoggedIn value
        long lastLoginLevelID = profileSecurityManager.getNonProfileFieldSecurityLevelID(User.LAST_LOGIN_PROFILE_SECURITY_LEVEL, userInfo.user);
        String lastLoggedInDateACL = StringUtils.trimToEmpty(aclMap.get(lastLoginLevelID));
        
        
        String brand = null;
        String region = null;
        String country = null;
        String role = null;
        String jobTitle = null;
        String fromSynchro = null;

        Map<String, String> properties = userInfo.user.getProperties();

        if(properties != null && properties.size() > 0) {
            if(properties.containsKey(SynchroUserPropertiesUtil.BRAND)) {
                brand = properties.get(SynchroUserPropertiesUtil.BRAND);
            }

            if(properties.containsKey(SynchroUserPropertiesUtil.REGION)) {
                region = properties.get(SynchroUserPropertiesUtil.REGION);
            }

            if(properties.containsKey(SynchroUserPropertiesUtil.COUNTRY)) {
                country = properties.get(SynchroUserPropertiesUtil.COUNTRY);
            }

            /*if(properties.containsKey(SynchroUserPropertiesUtil.ROLE)) {
                role = properties.get(SynchroUserPropertiesUtil.ROLE);
            }*/
            

            if(properties.containsKey(SynchroUserPropertiesUtil.JOB_TITLE)) {
                jobTitle = properties.get(SynchroUserPropertiesUtil.JOB_TITLE);
            }
        }
        
        //Check if user is Synchro user
       // GroupManager groupManager = (GroupManager) JiveApplication.getContext().getSpringBean("groupManager");
        
        Iterable<Group> groups = groupManager.getUserGroups(userInfo.user);
        boolean skipGroupCheck = false;
        boolean skipRoleCheck = false;
        for(Group group : groups)
        {
        	 Map<String, String> groupProps = group.getProperties();
        	 if(!skipGroupCheck)
        	 {
	        	 if(groupProps.containsKey(SynchroConstants.SYNCHRO_GROUP_PROP))
	        	 {
	        		 if(groupProps.get(SynchroConstants.SYNCHRO_GROUP_PROP).equalsIgnoreCase("true"))
	        		 {
	        			 fromSynchro = "true";
	        			 skipGroupCheck = true;
	        		 }
	        	 }
        	 }
        	 
        	 if(!skipRoleCheck)
        	 {
	        	 if(groupProps.containsKey(SynchroConstants.SYNCHRO_ROLE_TYPE_PROP))
	        	 {
	        		 if(groupProps.get(SynchroConstants.SYNCHRO_ROLE_TYPE_PROP).equalsIgnoreCase(SynchroGlobal.UserRole.SPI.name()))
	        		 {
	        			 skipRoleCheck = true;
	        			 role=SynchroGlobal.UserRole.SPI.name();
	        		 }
	        		 if(groupProps.get(SynchroConstants.SYNCHRO_ROLE_TYPE_PROP).equalsIgnoreCase(SynchroGlobal.UserRole.BAT.name()))
	        		 {
	        			 skipRoleCheck = true;
	        			 role=SynchroGlobal.UserRole.BAT.name();
	        		 }
	        		 if(groupProps.get(SynchroConstants.SYNCHRO_ROLE_TYPE_PROP).equalsIgnoreCase(SynchroGlobal.UserRole.MARKETING.name()))
	        		 {
	        			 skipRoleCheck = true;
	        			 role=SynchroGlobal.UserRole.MARKETING.name();
	        		 }
	        		 if(groupProps.get(SynchroConstants.SYNCHRO_ROLE_TYPE_PROP).equalsIgnoreCase(SynchroGlobal.UserRole.LEGAL.name()))
	        		 {
	        			 skipRoleCheck = true;
	        			 role=SynchroGlobal.UserRole.LEGAL.name();
	        		 }
	        		 
	        		 if(groupProps.get(SynchroConstants.SYNCHRO_ROLE_TYPE_PROP).equalsIgnoreCase(SynchroGlobal.UserRole.PROCUREMENT.name()))
	        		 {
	        			 skipRoleCheck = true;
	        			 role=SynchroGlobal.UserRole.PROCUREMENT.name();
	        		 }
	        		 if(groupProps.get(SynchroConstants.SYNCHRO_ROLE_TYPE_PROP).equalsIgnoreCase(SynchroGlobal.UserRole.EXTERNALAGENCY.name()))
	        		 {
	        			 skipRoleCheck = true;
	        			 role=SynchroGlobal.UserRole.EXTERNALAGENCY.name();
	        		 }
	        		 
	        		 if(groupProps.get(SynchroConstants.SYNCHRO_ROLE_TYPE_PROP).equalsIgnoreCase(SynchroGlobal.UserRole.COMMUNICATIONAGENCY.name()))
	        		 {
	        			 skipRoleCheck = true;
	        			 role=SynchroGlobal.UserRole.COMMUNICATIONAGENCY.name();
	        		 }
	        		 
	        		 if(groupProps.get(SynchroConstants.SYNCHRO_ROLE_TYPE_PROP).equalsIgnoreCase(SynchroGlobal.UserRole.SUPPORT.name()))
	        		 {
	        			 skipRoleCheck = true;
	        			 role=SynchroGlobal.UserRole.SUPPORT.name();
	        		 }
	        		 
	        	 }
        	 }
        }
        
        return new ProfileSearchFieldsDataImpl(userInfo, nameACL, nameLevelID, emailACL, emailLevelID, creationDateACL,
                creationDateLevelID, lastLoggedInDateACL, lastLoginLevelID,
                modificationDateACL, modificationDateLevelID,
                lastProfileUpdateACL, lastProfileUpdateLevelID,
                initialLoginDateACL, initialLoginDateSecurityLevelID,
                profileFieldsData, keywordsData,
                statusLevelManager.isStatusLevelsEnabled(), brand, region, country, role, jobTitle, fromSynchro);
    }
}
