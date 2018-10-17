/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.action;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroUserPropertiesUtil;
import com.jivesoftware.base.Group;
import com.jivesoftware.base.GroupManager;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserTemplate;
import com.jivesoftware.base.util.UserPermHelper;
import com.jivesoftware.community.action.util.AlwaysDisallowAnonymous;
import com.jivesoftware.community.web.JiveResourceResolver;
import com.opensymphony.xwork2.Preparable;
import org.apache.commons.lang.StringUtils;
import com.jivesoftware.community.impl.search.user.ProfileSearchIndexManager;

@AlwaysDisallowAnonymous
public class EditProfile extends EditProfileAction implements Preparable {

    protected UserTemplate targetUserTemplate;
    
    private GroupManager groupManager;
    private ProfileSearchIndexManager profileSearchIndexManager;
    private Boolean fromSynchro = false;

    private Integer brand = -1;
    private Integer region = -1;
    private Integer country = -1;
    private Integer jobTitle = -1;

    private String redirectURL;

    public UserTemplate getTargetUser() {
        if (targetUser!=null)
        {
            if (targetUserTemplate == null)
            {
                targetUserTemplate = new UserTemplate(targetUser);
            }
            return targetUserTemplate;
        }
        return new UserTemplate(getUser());
    }

    public void setTargetUser(User user) {
        targetUser = user;
    }

    public long getTargetUserID() {
        if (targetUser != null) {
            return targetUser.getID();
        }
        return getUserID();
    }

    public String input() {
        if (!isAuthenticated()) {
            return LOGIN;
        }

        if (!isAuthorized()) {
            return UNAUTHORIZED;
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
        if(fromSynchro)
        {
            Map<String, String> properties = user.getProperties();
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
            }
        }

        StringBuilder buffer = null;
        if(request.getParameter("from") != null && request.getParameter("from").equalsIgnoreCase("selective_screen")) {
            buffer = new StringBuilder();
            buffer.append("/portal-options.jspa");
        } else {
            buffer = new StringBuilder(JiveResourceResolver.getJiveObjectURL(this.getTargetUser()));
            buffer.append("?view=profile");
        }
        redirectURL = buffer.toString();
        return super.input();
    }

    public String execute() {
        if (!isAuthenticated()) {
            return LOGIN;
        }

        if (!isAuthorized()) {
            return UNAUTHORIZED;
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

        if(fromSynchro)
        {
            Map<String, String> properties = user.getProperties();
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
                try{
                    if (userChanged) {
                        user.setLastProfileUpdate(new Date());
                        userManager.updateUser(user);
                        profileSearchIndexManager.updateIndex(user);
                        //profileSearchIndexManager.rebuildIndex();
                    }

                }catch(Exception e)
                {
                	//LOG.error("User already exists exception custom Edit Profile Action ---" + e.getMessage());
                }
            }
        }
        
        //Audit Logs: Profile EDIT      
        String i18Text = getText("logger.profile.edit");
        SynchroLogUtils.addLog("", SynchroGlobal.PageType.PROFILE.getId(), SynchroGlobal.Activity.EDIT.getId(), 0, i18Text, "", -1L, getUser().getID());

        return super.execute();
    }

    public final String getRedirect() {
       /* StringBuilder buffer = new StringBuilder(JiveResourceResolver.getJiveObjectURL(this.getTargetUser()));
        buffer.delete(0,buffer.indexOf("/people"));
        buffer.append("?view=profile");
        if (StringUtils.isNotBlank(fromQuest)) {
            buffer.append("&fromQ=").append(fromQuest);
        }
        if (questProgress) {
            buffer.append("&qProg=true");
        }
        return buffer.toString();*/
    	 return redirectURL;
    }

    private boolean isAuthenticated() {
        if (getUser().isAnonymous()) {
            return false;
        }
        return true;
    }

    private boolean isAuthorized() {
        if (getTargetUserID() != getUserID()) {
            if (!UserPermHelper.isGlobalUserAdmin()) {
                return false;
            }
        }
        return true;
    }
    public Boolean getFromSynchro() {
        return fromSynchro;
    }

    public void setFromSynchro(Boolean fromSynchro) {
        this.fromSynchro = fromSynchro;
    }

    public void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    public void setProfileSearchIndexManager(
            ProfileSearchIndexManager profileSearchIndexManager) {
        this.profileSearchIndexManager = profileSearchIndexManager;
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

    private String getEncodedText(String str)
    {
        String encoded_text = str.replaceAll(" ", "%20A");
        return encoded_text;
    }

    private String getDecodedString(String str)
    {
        return str.replaceAll("%20A", " ");
    }
}
