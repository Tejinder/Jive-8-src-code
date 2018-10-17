/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.browse.rest.impl;

import com.google.common.collect.Sets;
import com.jivesoftware.community.JiveContentObject;

import java.util.Collection;

/**
 * A view bean around a JiveContentObject.
 *
 * @since Jive SBS 5.0
 */
public class JiveContentObjectItemBean extends AbstractItemBean {

    public static Collection<String> DEFAULT_CONTENT_OBJECT_BROWSE_VIEW_PROPERTIES = Sets.newHashSet(
            JiveObjectActivityInfoProviderImpl.PROPERTY_NAME,
            JiveObjectItemBeanFollowInfoProviderImpl.PROPERTY_NAME,
            JiveObjectShareInfoProviderImpl.PROPERTY_NAME,
            JiveObjectBookmarkInfoProviderImpl.PROPERTY_NAME,
            JiveObjectBodySnippetProviderImpl.PROPERTY_NAME,
            JiveContentObjectBinaryPreviewProviderImpl.PROPERTY_NAME,
            JiveObjectAttachmentProviderImpl.PROPERTY_NAME,
            JiveObjectPrivacyProviderImpl.PROPERTY_NAME,
            JiveObjectLastActivityDateProviderImpl.PROPERTY_NAME,
            DiscussionReadTrackProviderImpl.PROPERTY_NAME,
            JiveObjectViewCountProviderImpl.PROPERTY_NAME,
            JiveObjectAuthorProviderImpl.PROPERTY_NAME,
            JiveObjectPlaceProviderImpl.PROPERTY_NAME,
            JiveObjectIsOriginalAuthorProviderImpl.PROPERTY_NAME,
            JiveObjectIsVisibleToPartnerProviderImpl.PROPERTY_NAME,
            JiveContentObjectContentPlaceRelCountProviderImpl.PROPERTY_NAME,
            JiveObjectRatingInfoProviderImpl.PROPERTY_NAME
    );

    public JiveContentObjectItemBean(JiveContentObject jiveContentObject) {
        super(jiveContentObject);
    }
}
