package com.jivesoftware.community.browse.rest.impl;

import com.jivesoftware.base.User;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.acclaim.Acclaim;
import com.jivesoftware.community.acclaim.AcclaimManager;
import com.jivesoftware.community.acclaim.impl.RateAcclaimType;
import com.jivesoftware.community.browse.rest.ItemBeanPropertyProvider;
import com.jivesoftware.community.objecttype.ObjectTypeManager;
import com.jivesoftware.community.rating.Rating;
import com.jivesoftware.community.rating.RatingManager;
import com.jivesoftware.community.rating.dao.RatingDAO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JiveObjectRatingInfoProviderImpl implements ItemBeanPropertyProvider<JiveObject, ItemRatingBean> {
    private static Logger LOG = Logger.getLogger(JiveObjectRatingInfoProviderImpl.class);

    public static final String PROPERTY_NAME = "ratingInfo";

    protected AcclaimManager acclaimManager;
    protected ObjectTypeManager objectTypeManager;
    private RatingManager ratingManager;
    @Required
    public void setAcclaimManager(AcclaimManager acclaimManager) {
        this.acclaimManager = acclaimManager;
    }

    @Required
    public void setObjectTypeManager(ObjectTypeManager objectTypeManager) {
        this.objectTypeManager = objectTypeManager;
    }

    public RatingManager getRatingManager() {
        return ratingManager;
    }

    public void setRatingManager(RatingManager ratingManager) {
        this.ratingManager = ratingManager;
    }

    @Required


    @Override
    public String propertyName() {
        return PROPERTY_NAME;
    }

    @Override
    public ItemRatingBean provideProperty(JiveObject jiveObject, User targetUser, Map<String, Object> additionalContext) {
        ItemRatingBean itemRatingBean = new ItemRatingBean();
        try {
            Acclaim acclaim = acclaimManager.getAcclaim(jiveObject, new RateAcclaimType());
            if(acclaim == null) {
                itemRatingBean.setMeanRating(0);
            } else {
                itemRatingBean.setMeanRating(acclaim.getScore());
            }

            if(ratingManager == null || ratingManager.getAvailableRatingCount() == 0) {
                itemRatingBean.setAvailableRatingCount(5);
            } else {
                itemRatingBean.setAvailableRatingCount(ratingManager.getAvailableRatingCount());
            }


        } catch (RuntimeException e) {
            LOG.info(e.getMessage(), e);
        }
        return itemRatingBean;
    }
}
