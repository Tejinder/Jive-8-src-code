/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.impl.search.user.solr;

import com.google.common.base.Joiner;
import com.jivesoftware.community.impl.search.user.ProfileSearchFieldsData;
import com.jivesoftware.community.impl.search.user.ProfileSearchKeywordsData;
import com.jivesoftware.community.impl.search.user.UserProfileFieldSearchData;
import com.jivesoftware.community.search.user.impl.DefaultProfileSearchIndexField;
import com.jivesoftware.community.user.profile.ProfileField;
import com.jivesoftware.util.DateUtils;
import com.jivesoftware.util.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import java.text.ParseException;
import java.util.Date;

public class SolrDocumentBuilderImpl implements SolrDocumentBuilder {

    public SolrInputDocument buildDocument(ProfileSearchFieldsData data) {
        if (data == null) {
            return null;
        }

        SolrInputDocument doc = new SolrInputDocument();
        doc.addField(DefaultProfileSearchIndexField.USER_ID.getFieldName(), data.getUserID());
        doc.addField(DefaultProfileSearchIndexField.USERNAME.getFieldName(), data.getUsername());
        doc.addField(USERNAME_PREFIX_FIELD, data.getUsername().substring(0, 1).toLowerCase());
        doc.addField(DefaultProfileSearchIndexField.CREATION_DATE.getFieldName(), data.getCreationDate().getTime());
        doc.addField(CREATION_DATE_ACL_FIELD, data.getCreationDateACL());
        doc.addField(DefaultProfileSearchIndexField.MODIFICATION_DATE.getFieldName(), data.getModificationDate().getTime());
        doc.addField(MODIFICATION_DATE_ACL_FIELD, data.getModificationDateACL());

        if (data.getInitialLoginDate() != null && data.getInitialLoginDate().getTime() > 0l) {
            doc.addField(DefaultProfileSearchIndexField.INITIAL_LOGIN_DATE.getFieldName(), data.getInitialLoginDate().getTime());
        }
        doc.addField(INITIAL_LOGIN_DATE_ACL_FIELD, data.getInitialLoginDateACL());

        doc.addField(DefaultProfileSearchIndexField.LAST_PROFILE_UPDATE.getFieldName(), data.getLastProfileUpdate().getTime());
        doc.addField(LAST_PROFILE_UPDATE_ACL_FIELD, data.getLastProfileUpdateACL());

        // Last name will only be populated when setup is configured to use first and last name
        // vs. full name.  If the last name exists, index it for sorting on the profile pages.
        String lastNameSortableValue;
        if (StringUtils.isNotBlank(data.getLastName())) {
            lastNameSortableValue = StringUtils.convertNonAscii(data.getLastName().toLowerCase());
            doc.addField(LASTNAME_PREFIX_FIELD, data.getLastName().substring(0, 1).toLowerCase());
        }
        //If no last name specified,consider the name to be the last name
        else if (StringUtils.isNotBlank(data.getName())) {
            lastNameSortableValue = StringUtils.convertNonAscii(data.getName().toLowerCase());
        }
        //if no name specified, fall back to username (though this case shouldn't happen -- name shouldn't be
        // visible if it has never been specified)
        else {
            lastNameSortableValue = data.getUsername().toLowerCase();
        }

        if (StringUtils.isNotBlank(lastNameSortableValue)) {
            doc.addField(SORTABLE_LAST_NAME, lastNameSortableValue);
        }

        if (StringUtils.isNotBlank(data.getName())) {
            doc.addField(DefaultProfileSearchIndexField.NAME.getFieldName(), data.getName());
            doc.addField(PHONETIC_NAME, data.getName(), 0.6f);
            doc.addField(NGRAM_NAME, data.getName());
            doc.addField(SYNONYM_NAME, data.getName());
            doc.addField(SORTABLE_NAME, data.getName());
            doc.addField(NAME_ACL_FIELD, data.getNameACL());
            doc.addField(NAME_PREFIX_FIELD, data.getName().substring(0, 1).toLowerCase());
        }

        if (StringUtils.isNotBlank(data.getEmail())) {
            doc.addField(DefaultProfileSearchIndexField.EMAIL.getFieldName(), data.getEmail());
            doc.addField(EMAIL_ACL_FIELD, data.getEmailACL());
        }

        //log.info("user " + data.getUserID() + " points " + data.getTotalStatusPoints());
        if (data.getTotalStatusPoints() > -1L) {
            doc.addField(DefaultProfileSearchIndexField.STATUS_LEVEL.getFieldName(), data.getTotalStatusPoints());
        }


        /* GM 11/18/2011 Removed from indexing (this class) and querying (SolrProfileSearchQueryBuilderImpl) */
        /*
        for (Map.Entry<EntityDescriptor, Long> containerPoints : data.getContainerStatusPointMap().entrySet()) {
            String fieldName = new StringBuilder(DefaultProfileSearchIndexField.STATUS_LEVEL.getFieldName())
                    .append("-").append(containerPoints.getKey().getObjectType())
                    .append("-").append(containerPoints.getKey().getID())
                    .toString();
            doc.addField(fieldName, containerPoints.getValue());
        }
        */

        String tagIDs = Joiner.on(" ").join(data.getTagIDs());
        if (!tagIDs.isEmpty()) {
            doc.addField(DefaultProfileSearchIndexField.TAG_ID.getFieldName(), tagIDs);
        }

        String tags = Joiner.on(" ").join(data.getExpertiseTags());
        if (!tags.isEmpty()) {
            doc.addField(DefaultProfileSearchIndexField.TAGS.getFieldName(), tags);
        }

        String sharedGroups = Joiner.on(" ").join(data.getSharedGroups());
        if (!sharedGroups.isEmpty()) {
            doc.addField(SHARED_GROUPS, sharedGroups);
        }

        // add user profile information
        for (UserProfileFieldSearchData field : data.getProfileFieldsData()) {
            if (field.getData().size() > 0) {
                String fieldName = getProfileFieldIndexFieldName(field.getType(), field.getProfileFieldID());
                String fieldAcl = new StringBuilder(fieldName).append(ACL_POSTFIX).toString();

                doc.addField(fieldAcl, field.getFieldACL());
                for (String value : field.getData()) {
                    doc.addField(fieldName, getProfileFieldIndexValue(field.getType(), value));
                }
            }
        }

        for (ProfileSearchKeywordsData keywordsData : data.getKeywordsData()) {
            //store a security column for relative keywords with userIDs
            String keywordsACLName = new StringBuilder(KEYWORDS_ACL_PREFIX).append(keywordsData.getSecurityLevelID())
                    .append(ACL_POSTFIX).toString();
            doc.addField(keywordsACLName, keywordsData.getKeywordsACL());

            String keywordsName = new StringBuilder(KEYWORDS_PREFIX).append(keywordsData.getSecurityLevelID()).toString();
            doc.addField(keywordsName, keywordsData.getKeywords());
        }

        //store enabled value
        doc.addField(DefaultProfileSearchIndexField.ENABLED.getFieldName(), data.isEnabled() ? 1 : 0);

        //store user type value
        doc.addField(DefaultProfileSearchIndexField.USER_TYPE.getFieldName(), data.getUserType().getId());

        //store lastLoggedIn value
        doc.addField(LAST_LOGGED_IN_DATE_ACL_FIELD, data.getLastLoggedInDateACL());
        long time = data.getLastLoggedInDate() != null ? data.getLastLoggedInDate().getTime() : 0L;
        doc.addField(DefaultProfileSearchIndexField.LAST_LOGGED_IN.getFieldName(), time);
        
        if(StringUtils.isNotBlank(data.getBrand())) {
            doc.addField(SolrDocumentBuilder.BRAND_PFB_FIELD, data.getBrand());
        }

        if(StringUtils.isNotBlank(data.getRegion())) {
            doc.addField(SolrDocumentBuilder.REGION_PFB_FIELD, data.getRegion());
        }

        if(StringUtils.isNotBlank(data.getCountry())) {
            doc.addField(SolrDocumentBuilder.COUNTRY_PFB_FIELD, data.getCountry());
        }

        if(StringUtils.isNotBlank(data.getRole())) {
            doc.addField(SolrDocumentBuilder.ROLE_PFB_FIELD, data.getRole());
        }

        if(StringUtils.isNotBlank(data.getJobTitle())) {
            doc.addField(SolrDocumentBuilder.JOB_TITLE_PFB_FIELD, data.getJobTitle());
        }
        
        if(StringUtils.isNotBlank(data.getFromSynchro())) {
            doc.addField(SolrDocumentBuilder.FROM_SYNCHRO_PFB_FIELD, data.getFromSynchro());
        }
        return doc;
    }

    public String getProfileFieldIndexFieldName(ProfileField.Type type, long fieldID) {
        String prefix;
        switch (type) {
            case NUMBER:
                prefix = PROFILE_LONG_FIELD_PREFIX;
                break;
            case DECIMAL:
                prefix = PROFILE_DBL_FIELD_PREFIX;
                break;
            case BOOLEAN:
                prefix = PROFILE_BOOL_FIELD_PREFIX;
                break;
            case DATE:
                prefix = PROFILE_DATE_FIELD_PREFIX;
                break;
            case MULTILIST:
                prefix = PROFILE_MULTI_FIELD_PREFIX;
                break;
            default:
                prefix = PROFILE_STRING_FIELD_PREFIX;
        }

        return new StringBuilder(prefix).append(fieldID).toString();
    }

    public String getProfileFieldIndexValue(ProfileField.Type type, String value) {
        String parsed;
        switch (type) {
            case DATE:
                try {
                    Date date = DateUtils.parseFromDefaultDate(value);
                    parsed = Long.toString(date.getTime());
                }
                catch (ParseException e) {
                    parsed = null;
                }
                break;
            default:
                parsed = value;
        }

        return parsed;
    }

    public long getProfileFieldIDFromIndexFieldName(String indexFieldName) {
        // Profile field index field names are like <prefix>-<id>
        int firstDash = indexFieldName.indexOf("-");

        if (firstDash > -1) {
            return Long.parseLong(indexFieldName.substring(firstDash + 1));
        }

        return -1L;
    }
}
