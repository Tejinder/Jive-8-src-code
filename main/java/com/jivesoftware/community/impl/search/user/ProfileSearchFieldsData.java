/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.impl.search.user;

import com.jivesoftware.base.User;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * A consolidated data object that holds all the necessary information for a particular user that is needed to create a
 * record in the search index for that user including any security related information for the data to allow the search
 * index implementation to appropriately handle the security information when searches are performed on those fields.
 */
public interface ProfileSearchFieldsData {

    /**
     * Returns the ID of the user represented by this data object
     *
     * @return user ID
     */
    long getUserID();

    /**
     * Returns the username of the user represented by this data object
     *
     * @return username
     */
    String getUsername();

    /**
     * Returns whether the user represented by this object is enabled
     *
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Returns the type of the user
     *
     * @return type of user
     */
    User.Type getUserType();

    /**
     * Returns the name of the user represented by this object
     *
     * @return name
     */
    String getName();

    long getNameSecurityLevelID();

    /**
     * Returns the access control role setting for the name information for the user represented by this object
     *
     * @return name access control role
     */
    String getNameACL();

    /**
     * Returns the last name of the user represented by this object
     *
     * @return last name
     */
    String getLastName();

    /**
     * Returns the email of the user represented by this object
     *
     * @return email
     */
    String getEmail();

    long getEmailSecurityLevelID();

    /**
     * Returns the access control role setting for the email information for the user represented by this object
     *
     * @return email access control role
     */
    String getEmailACL();

    /**
     * Returns the creation date of the user represented by this object
     *
     * @return creation date
     */
    Date getCreationDate();

    /**
     * Returns the access control role setting for the creation date information for the user represented by this object
     *
     * @return creation date access control role
     */
    String getCreationDateACL();

    long getCreationDateSecurityLevelID();

    /**
     * Returns the last logged in date for the user represented by this object
     *
     * @return last logged in date
     */
    Date getLastLoggedInDate();

    /**
     * Returns the access control role setting for the last logged in date information for the user represented by this
     * object
     *
     * @return last logged in access control role
     */
    String getLastLoggedInDateACL();

    long getLastLoggedInDateSecurityLevelID();

    /**
     * Returns the last profile update date for the user represented by this object
     *
     * @return last profile update date
     */
    Date getLastProfileUpdate();

    /**
     * Returns the access control role setting for the last profile update date information for the user represented by this
     * object
     *
     * @return last profile update access control role
     */
    String getLastProfileUpdateACL();

    long getLastProfileUpdateSecurityLevelID();

    /**
    /**
     * Returns the modification date of the user represented by this object
     *
     * @return modification date
     */
    Date getModificationDate();

    /**
     * Returns the access control role setting for the modification date information for the user represented by this object
     *
     * @return modification date access control role
     */
    String getModificationDateACL();

    /**
    /**
     * Returns the initialLogin date of the user represented by this object
     *
     * @return initialLogin date
     */
    Date getInitialLoginDate();

    /**
     * Returns the access control role setting for the initialLogin date information for the user represented by this object
     *
     * @return initialLogin date access control role
     */
    String getInitialLoginDateACL();

    long getModificationDateSecurityLevelID();

    /**
     * Returns the total status level points that the user represented by this object has or -1L if status levels are
     * not enabled in the system
     *
     * @return total status level points or -1L if status level points disabled
     */
    long getTotalStatusPoints();

    /**
     * Returns a map of containers represented by {@link com.jivesoftware.community.EntityDescriptor}s to the status level
     * points that the user represented by this object has in that container
     *
     * @return Map of container descriptors to status level points or empty map if status level point disabled
     */
    //Map<EntityDescriptor, Long> getContainerStatusPointMap();

    /**
     * Returns a set of the IDs of the tags associated with the user represented by this object
     *
     * @return set of tag IDs
     */
    Set<Long> getTagIDs();

    /**
     * Returns the set of tag names of the tags that are associated with the user represented by this object
     *
     * @return set of tag names
     */
    Set<String> getTags();

    /**
     * Returns a list of expertise tags
     *
     * @return list of expertise tags
     */
    List<String> getExpertiseTags();

    /**
     * Returns a list of {@link com.jivesoftware.community.impl.search.user.UserProfileFieldSearchData} objects that hold
     * information about each profile field in the profile of the user represented by this object.
     *
     * @return user profile information
     */
    List<UserProfileFieldSearchData> getProfileFieldsData();

    /**
     * Returns a list of the {@link com.jivesoftware.community.impl.search.user.ProfileSearchKeywordsData} objects that
     * hold the different keywords strings that should be searchable for the user represented by this object broken
     * down by security level
     *
     * @return keywords
     */
    List<ProfileSearchKeywordsData> getKeywordsData();


    /**
     * Returns a set of IDs of shared groups for a partner user.
     *
     * @return set of IDs of shared groups.
     */
    Set<Long> getSharedGroups();
    
    /**
    *
    */
   String getBrand();

   /**
    *
    */
   String getRole();

   /**
    *
    */
   String getCountry();

   /**
    *
    */
   String getRegion();

   /**
    *
    */
   String getJobTitle();
   
   /**
    * 
    */
   String getFromSynchro();
}
