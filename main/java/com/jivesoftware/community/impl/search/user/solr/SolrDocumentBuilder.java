/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.impl.search.user.solr;

import com.jivesoftware.community.impl.search.user.ProfileSearchFieldsData;
import com.jivesoftware.community.search.user.impl.DefaultProfileSearchIndexField;
import com.jivesoftware.community.user.profile.ProfileField;
import org.apache.solr.common.SolrInputDocument;

/**
 * Provides utility methods around working with the format of documents stored in the Solr index used for the SBS
 * user/profile search index
 */
public interface SolrDocumentBuilder {

    /**
     * String that is appended to the end of any field that contains access control level information in the index
     */
    public static final String ACL_POSTFIX = "_ACL";

    /**
     * Name of the field used to store access control level for the {@link com.jivesoftware.community.search.user.impl.DefaultProfileSearchIndexField#CREATION_DATE}
     * field
     */
    public static final String CREATION_DATE_ACL_FIELD = DefaultProfileSearchIndexField.CREATION_DATE.getFieldName() + ACL_POSTFIX;

    /**
     * Name of the field used to store access control level for the {@link com.jivesoftware.community.search.user.impl.DefaultProfileSearchIndexField#MODIFICATION_DATE}
     * field
     */
    public static final String MODIFICATION_DATE_ACL_FIELD = DefaultProfileSearchIndexField.MODIFICATION_DATE.getFieldName() + ACL_POSTFIX;

    /**
     * Name of the field used to store access control level for the {@link com.jivesoftware.community.search.user.impl.DefaultProfileSearchIndexField#INITIAL_LOGIN_DATE}
     * field
     */
    public static final String INITIAL_LOGIN_DATE_ACL_FIELD = DefaultProfileSearchIndexField.INITIAL_LOGIN_DATE.getFieldName() + ACL_POSTFIX;

    /**
     * Name of the field used to store access control level for the {@link com.jivesoftware.community.search.user.impl.DefaultProfileSearchIndexField#NAME}
     * field.
     */
    public static final String NAME_ACL_FIELD = DefaultProfileSearchIndexField.NAME.getFieldName() + ACL_POSTFIX;

    /**
     * Name of the field used to store access control level for the {@link com.jivesoftware.community.search.user.impl.DefaultProfileSearchIndexField#EMAIL}
     * field.
     */
    public static final String EMAIL_ACL_FIELD = DefaultProfileSearchIndexField.EMAIL.getFieldName() + ACL_POSTFIX;

    /**
     * Name of the field used to store access control level for the {@link com.jivesoftware.community.search.user.impl.DefaultProfileSearchIndexField#LAST_LOGGED_IN}
     * field.
     */
    public static final String LAST_LOGGED_IN_DATE_ACL_FIELD = DefaultProfileSearchIndexField.LAST_LOGGED_IN.getFieldName() + ACL_POSTFIX;

    /**
     * Name of the field used to store access control level for the {@link com.jivesoftware.community.search.user.impl.DefaultProfileSearchIndexField#LAST_PROFILE_UPDATE}
     * field.
     */
    public static final String LAST_PROFILE_UPDATE_ACL_FIELD = DefaultProfileSearchIndexField.LAST_PROFILE_UPDATE.getFieldName() + ACL_POSTFIX;

    /**
     * Name of the field in the index that contains the a user's last name stored as a simple string that is not
     * tokenized or analyzed.  This field should be used for sorting.
     */
    public static final String SORTABLE_LAST_NAME = "lastNameSortable";

    /**
     * Name of the field in the index that contains the user's name stored as a simple string that is not tokenized or
     * analyzed.  This field should be used for sorting.
     */
    public static final String SORTABLE_NAME = "nameSortable";

    /**
     * Name of the field in the index that contains the user's name analyzed in a manner to allow phonetic searching.
     */
    public static final String PHONETIC_NAME = "namePhonetic";

    /**
     * Name of the field in the index that contains the user's name tokenized by ngrams which allows for a better partial matching than wildcards
     */
    public static final String NGRAM_NAME = "nameNGram";

    /**
     * Name of the field in the index that contains the user's name synonyms (e.g. making bob find robert)
     */
    public static final String SYNONYM_NAME = "nameSynonym";

    /**
     * Name of the field in the index that contains the user's shared groups.  Only populated for partner users.
     */
    public static final String SHARED_GROUPS = "sharedGroups";

    /**
     * Prefix for user profile fields in the index that are of a long or integer type
     */
    public static final String PROFILE_LONG_FIELD_PREFIX = "pfl-";

    /**
     * Prefix for user profile fields in the index that are of a double type
     */
    public static final String PROFILE_DBL_FIELD_PREFIX = "pfd-";

    /**
     * Prefix for user profile fields in the index that are of a date type
     */
    public static final String PROFILE_DATE_FIELD_PREFIX = "pft-";

    /**
     * Prefix for user profile fields in the index that are of a boolean type
     */
    public static final String PROFILE_BOOL_FIELD_PREFIX = "pfb-";

    /**
     * Prefix for user profile fields in the index that are of a single value string type
     */
    public static final String PROFILE_STRING_FIELD_PREFIX = "pfs-";

    /**
     * Prefix for user profile fields in the index that are multi-value and of a string type
     */
    public static final String PROFILE_MULTI_FIELD_PREFIX = "pfm-";

    /**
     * Prefix for fields that contain keywords for a user record by security level
     */
    public static final String KEYWORDS_PREFIX = "keywords-L";

    /**
     * Prefix for fields in the index that hold access control level information for keywords fields
     */
    public static final String KEYWORDS_ACL_PREFIX = "key-L";

    /**
     * Prefix for fields that hold the prefix character for a user's name
     */
    public static final String NAME_PREFIX_FIELD = "namePrefix";

    /**
     * Prefix for fields that hold the prefix character for a user's last name
     */
    public static final String LASTNAME_PREFIX_FIELD = "lastNamePrefix";

    /**
     * Name of the field that holds the prefix character for a user's username
     */
    public static final String USERNAME_PREFIX_FIELD = "usernamePrefix";
    
    /**
    *
    */
   public static final String BRAND_PFB_FIELD = "pfb-brand";

   /**
    *
    */
   public static final String COUNTRY_PFB_FIELD = "pfb-country";

   /**
    *
    */
   public static final String ROLE_PFB_FIELD = "pfb-role";

   /**
    *
    */
   public static final String REGION_PFB_FIELD = "pfb-region";

   /**
    *
    */
   public static final String JOB_TITLE_PFB_FIELD = "pfb-jobtitle";
   
   /**
    * 
    */
   public static final String FROM_SYNCHRO_PFB_FIELD = "pfb-fromsynchro";

    /**
     * Converts a {@link com.jivesoftware.community.impl.search.user.ProfileSearchFieldsData} object into an appropriate
     * {@link org.apache.solr.common.SolrInputDocument} object that can be put into the index
     *
     * @param data object holding the data to be indexed
     * @return {@link org.apache.solr.common.SolrInputDocument} built from the data of the provided data object
     */
    SolrInputDocument buildDocument(ProfileSearchFieldsData data);

    /**
     * Extracts the ID of the SBS profile field from the index field with the given name
     *
     * @param indexFieldName the full name of the profile field in the index
     * @return profile field ID
     */
    long getProfileFieldIDFromIndexFieldName(String indexFieldName);

    /**
     * Returns the appropriate field name prefix for a profile field of the specified type with the specified ID
     *
     * @param type the type of the profile field
     * @param fieldID the ID of the profile field
     * @return name prefix to use for the field in the index
     */
    String getProfileFieldIndexFieldName(ProfileField.Type type, long fieldID);

    /**
     * Returns an appropriately formatted/translated value for the String value assigned to a profile field of the
     * specified type
     *
     * @param type the type of the profile field
     * @param value the raw value stored in SBS for the field
     * @return value as it should be stored in the index
     */
    String getProfileFieldIndexValue(ProfileField.Type type, String value);
}
