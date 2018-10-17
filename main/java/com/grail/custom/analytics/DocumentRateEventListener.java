package com.grail.custom.analytics;

import com.grail.custom.analytics.dao.EventAnalyticsDAO;
import com.jivesoftware.base.User;
import com.jivesoftware.base.event.ContentEvent;
import com.jivesoftware.base.event.v2.BaseJiveEvent;
import com.jivesoftware.base.event.v2.EventListener;
import com.jivesoftware.community.*;
import com.jivesoftware.community.acclaim.objecttype.AcclaimRecentActivityInfoProvider;
import com.jivesoftware.community.activity.type.RecentActivityInfoProvider;
import com.jivesoftware.community.analytics.AnalyticsEventCode;
import com.jivesoftware.community.event.RateEvent;
import com.jivesoftware.community.impl.ActivityManagerImpl;
import com.jivesoftware.community.impl.DbDocument;
import com.jivesoftware.community.impl.activity.DefaultContentActivityEventHandlingStrategy;
import com.jivesoftware.community.impl.dao.DocumentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.objecttype.JiveObjectType;
import com.jivesoftware.community.objecttype.RecentActivityEnabledType;
import com.jivesoftware.community.objecttype.impl.ObjectTypeManagerImpl;
import com.jivesoftware.community.rating.Rating;
import com.jivesoftware.community.web.JiveResourceResolver;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Bhaskar
 * Date: 5/24/13
 * Time: 3:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentRateEventListener implements EventListener<ContentEvent> {

    private static Logger LOG = Logger.getLogger(DocumentRateEventListener.class);

    private ActivityManagerImpl activityManager;
    private ObjectTypeManagerImpl objectTypeManager;

    private EventAnalyticsDAO eventAnalyticsDAO;

    public void setActivityManager(ActivityManagerImpl activityManager) {
        this.activityManager = activityManager;
    }

    public void setObjectTypeManager(ObjectTypeManagerImpl objectTypeManager) {
        this.objectTypeManager = objectTypeManager;
    }

    public EventAnalyticsDAO getEventAnalyticsDAO() {
        return eventAnalyticsDAO;
    }

    public void setEventAnalyticsDAO(EventAnalyticsDAO eventAnalyticsDAO) {
        this.eventAnalyticsDAO = eventAnalyticsDAO;
    }

    @Override
    public void handle(ContentEvent e) {
        // pre
        JiveObject payload = (JiveObject) e.getPayload();
        if (payload == null) {
            // only handle content events with payloads
            return;
        }

        // pre
        JiveObjectType type = objectTypeManager.getObjectType(payload.getObjectType());
        if (!(type instanceof RecentActivityEnabledType)) {
            // only handle payloads which are recent activity enabled
            return;
        }

        // pre
        RecentActivityInfoProvider provider = ((RecentActivityEnabledType) type).getRecentActivityInfoProvider();
        if (provider == null || provider.eventsHandledExclusivelyByCustomListener()) {
            // if events are handled exclusively by some other mechanism, bypass this listener
            return;
        }

        ActivityEventHandlingStrategy strategy = provider.getLegacyEventHandlingStrategy();

        if (strategy == null) {
            strategy = new DefaultContentActivityEventHandlingStrategy();
        }

        // ratings use the acclaim api and we want to record ratings in the activity stream
        if (e instanceof RateEvent) {
            strategy = new AcclaimRecentActivityInfoProvider().getEventHandlingStrategy();
        }


        Map<String, Object> params = new HashMap<String, Object>();
        if (e.getContentModificationType().name().equals("Rate")) {
            final SessionEventBean eventBean = handleDocumentEvent(e, payload);
            String eventName =  "DOCUMENT_RATED";
            params.put("ratingUserID", eventBean.getActorID());
            params.put("ratingScore", e.getParams().get("Rating"));
            params.putAll(e.getParams());
            eventBean.setEventParams(params);
            //params.put("userSessionID", activeSessionID);
            params.putAll(e.getParams());
            eventBean.setName(eventName);
            eventBean.setUrl(getEventTargetUrl(e));

            eventBean.setEventDate(EventAnalyticsUtil.getCurrentLongTime(new Date()));
            Collection eventList = new ArrayList();
            eventList.add(eventBean);
            // Update the DB with the session details
            eventAnalyticsDAO.insertUserSessionEvents(eventList);
        }
    }

    private String getEventTargetUrl(BaseJiveEvent baseJiveEvent) {
        String eventUrl = "";
        JiveObject payloadObj = ((JiveObject) baseJiveEvent.getPayload());
        JiveObject loadedJiveObject = null;
        try {
            loadedJiveObject = JiveApplication.getEffectiveContext().getObjectLoader().getJiveObject(payloadObj.getObjectType(), payloadObj.getID());
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        eventUrl = JiveResourceResolver.getJiveObjectURL(loadedJiveObject, true);
        LOG.info("Event target url - " + eventUrl);
        return eventUrl;
    }

    private SessionEventBean handleDocumentEvent(ContentEvent contentEvent, JiveObject payloadObj) {
        SessionEventBean eventBean = new SessionEventBean();
        eventBean.setID(AnalyticsEventCode.RATE_OR_VOTE.getId());
        eventBean.setActorID(contentEvent.getActorID());
        eventBean.setDirectObjectID(payloadObj.getID());
        eventBean.setDirectObjectType(payloadObj.getObjectType());
        // Payload doesn't include the container details.
        // Explicitly loading the document object to fetch the container details
        try {
            Document doc = JiveApplication.getContext().getDocumentManager().getDocument(payloadObj.getID());
            eventBean.setContainerObjectID(doc.getContainerID());
            eventBean.setContainerObjectType(doc.getContainerType());
        } catch (NotFoundException e) {
            LOG.error("Unable to load the document object - " + payloadObj.getID());
        }

        return eventBean;
    }
}
