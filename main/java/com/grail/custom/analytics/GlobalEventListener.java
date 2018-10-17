package com.grail.custom.analytics;

import com.google.common.collect.Lists;
import com.grail.custom.analytics.dao.EventAnalyticsDAO;
import com.jivesoftware.base.User;
import com.jivesoftware.base.event.ContainerEvent;
import com.jivesoftware.base.event.ContentEvent;
import com.jivesoftware.base.event.PresenceEvent;
import com.jivesoftware.base.event.UserEvent;
import com.jivesoftware.base.event.v2.BaseJiveEvent;
import com.jivesoftware.base.event.v2.EventFilter;
import com.jivesoftware.base.event.v2.EventFilterImpl;
import com.jivesoftware.base.event.v2.FilteredEventListener;
import com.jivesoftware.community.*;
import com.jivesoftware.community.aaa.AnonymousUser;
import com.jivesoftware.community.analytics.AnalyticsEventCode;
import com.jivesoftware.community.analytics.AnalyticsListenerConfiguration;
import com.jivesoftware.community.analytics.AnalyticsListenerConfiguration.ConfigurationEntry;
import com.jivesoftware.community.comments.events.CommentEvent;
import com.jivesoftware.community.event.DocumentEvent;
import com.jivesoftware.community.favorites.events.FavoriteEvent;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.rating.Rating;
import com.jivesoftware.community.web.JiveResourceResolver;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.*;

/**
 * @Author Samee
 * @Date Apr 19, 2010
 * @Time 3:52:28 PM
 * @Version 1.0
 */
public class GlobalEventListener implements FilteredEventListener, HttpSessionListener {

    private static final Logger log = LogManager.getLogger(GlobalEventListener.class);
    private Map<Enum, ConfigurationEntry> analyticsEventTypes;

    // Spring injection
    private EventAnalyticsDAO eventAnalyticsDAO;
    private AnalyticsListenerConfiguration analyticsListenerConfiguration;

    public void setAnalyticsListenerConfiguration(AnalyticsListenerConfiguration analyticsListenerConfiguration) {
        this.analyticsListenerConfiguration = analyticsListenerConfiguration;
    }

    public void setEventAnalyticsDAO(EventAnalyticsDAO eventAnalyticsDAO) {
        this.eventAnalyticsDAO = eventAnalyticsDAO;
    }

    public EventFilter getFilter() {
        // Duplicated the code from AnalyticsListener
        if (null == analyticsEventTypes) {
            analyticsEventTypes = analyticsListenerConfiguration.getMap();
        }
        return new EventFilterImpl(Lists.newArrayList(analyticsListenerConfiguration.getMapClasses()),
                Lists.newArrayList(analyticsEventTypes.keySet()));
    }

    /**
     * @param baseJiveEvent Logic for handling events
     *                      if(eventType == LOGIN){
     *                      log into usersession table if the event is of type LOGIN
     *                      Add user session details to a MAP
     *                      }
     *                      if(eventType == LOGOUT){
     *                      Update the usersession table with
     *                      Update the user session details in MAP
     *                      }
     *                      else {
     *                      handle other events
     *                      }
     */
    public void handle(BaseJiveEvent baseJiveEvent) {
        SessionEventBean eventBean = new SessionEventBean();
        List<String> validEvents = new ArrayList<String>(){
            {
                add("DOCUMENT_VIEWED");
                add("DOCUMENT_DELETED");
                add("DOCUMENT_RATED");
                add("FAVORITE_added");
                add("COMMENT_ADDED");
                add("COMMENT_DELETED");
                add("FAVORITE_deleted");
            }
        };
        // Payload - JiveObject
        // Actor - Event triggering user

        String eventName = "";
        long activeSessionID = -1;
        Map<String, Object> params = new HashMap<String, Object>();
        SessionEventBean bean = new SessionEventBean(baseJiveEvent, getTypeForEvent(baseJiveEvent).getId(), null);
        int eventID = getTypeForEvent(baseJiveEvent).getId();
        if (baseJiveEvent instanceof UserEvent || baseJiveEvent instanceof PresenceEvent) {
            // Create a new record for user LOGIN / ONLINE events
            if (eventID == AnalyticsEventCode.ONLINE.getId() ||
                    (eventID == AnalyticsEventCode.LOG_IN.getId() && baseJiveEvent.getActorID() != AnonymousUser.ANONYMOUS_ID)) {
                bean.setActorID(baseJiveEvent.getActorID());
                activeSessionID = eventAnalyticsDAO.insertUserSession(bean);
            }
            // Update the record for user OFFLINE / LOG_OUT events
            if (eventID == AnalyticsEventCode.OFFLINE.getId() ||
                    (eventID == AnalyticsEventCode.LOG_OUT.getId() && baseJiveEvent.getActorID() != AnonymousUser.ANONYMOUS_ID)) {
                eventAnalyticsDAO.updateUserSession(bean);
            }
        } else {
            // Get friendly Event name
            eventName = getFriendlyEventName(baseJiveEvent);
            // ignore system level events ... and
            // additional check whether the events needs to log or not[ to ignore other events ]
            if (bean.isValidAnalyticsEvent() && validEvents.contains(eventName)) {
                JiveObject payloadObj = (JiveObject) baseJiveEvent.getPayload();
                if (payloadObj == null) {
                    eventBean.setDirectObjectID(-1);
                    eventBean.setDirectObjectType(-1);
                    log.info("Payload object is null.");
                }else{
                    // Track events only for DOCUMENTS content type
                    if (baseJiveEvent instanceof DocumentEvent) {
                        eventBean = handleDocumentEvent(baseJiveEvent, payloadObj);
                    }
                    if (baseJiveEvent instanceof CommentEvent) {
                        eventBean = handleCommentEvent(baseJiveEvent, payloadObj);
                    }
                    if (baseJiveEvent instanceof FavoriteEvent) {
                        eventBean = handleFavoriteEvent(baseJiveEvent, payloadObj);
                    }

                    eventBean.setID(bean.getCode());
                    if(eventBean.getDirectObjectType() == JiveConstants.DOCUMENT){
                        if (baseJiveEvent instanceof DocumentEvent && eventName.equals("DOCUMENT_RATED")) {
                            // Event params - "User", "Rating", "PreviousRating"
                            params.put("ratingUserID", ((User) baseJiveEvent.getParams().get("User")).getID());
                            params.put("ratingScore", ((Rating) baseJiveEvent.getParams().get("Rating")).getScore());
                        } else {
                            params.put("ratingUserID", eventBean.getActorID());
                            params.put("ratingScore", 0);
                        }
                        // If its a Bookmark remove event/ Document delete/ Comment delete.
                        // Then update the existing record in userSessionEvents. So that its reflected in next
                        // report query results
                        if(StringUtils.containsAnyOf(eventName, "FAVORITE_deleted", "DOCUMENT_DELETED", "COMMENT_DELETED")){
                            params.putAll(baseJiveEvent.getParams());
                            eventBean.setEventParams(params);
                            //if(baseJiveEvent instanceof FavoriteEvent && eventName.equals("FAVORITE_deleted")){
                            eventAnalyticsDAO.updateUserSessionEventForEvent(eventBean, eventName);
//                            }else if(baseJiveEvent instanceof DocumentEvent && eventName.equals("DOCUMENT_DELETED")){
//                                eventAnalyticsDAO.updateUserSessionEventForEvent(eventBean, eventName);
//                            }else if(baseJiveEvent instanceof CommentEvent && eventName.equals("COMMENT_DELETED")){
//                                eventAnalyticsDAO.updateUserSessionEventForEvent(eventBean, eventName);
//                            }
                        }else{
                            params.put("userSessionID", activeSessionID);
                            params.putAll(baseJiveEvent.getParams());
                            eventBean.setName(eventName);
                            eventBean.setUrl(getEventTargetUrl(baseJiveEvent));
                            eventBean.setEventParams(params);
                            //Date currentDate = new Date(Calendar.YEAR,Calendar.DAY_OF_MONTH,Calendar.DATE,0,0);
                            eventBean.setEventDate(EventAnalyticsUtil.getCurrentLongTime(new Date()));
                            Collection eventList = new ArrayList();
                            eventList.add(eventBean);
                            // Update the DB with the session details
                            eventAnalyticsDAO.insertUserSessionEvents(eventList);
                        }
                    }
                }
            }
        }
    }

    private SessionEventBean handleDocumentEvent(BaseJiveEvent baseJiveEvent, JiveObject payloadObj) {
        SessionEventBean eventBean = new SessionEventBean();
        eventBean.setActorID(baseJiveEvent.getActorID());
        eventBean.setDirectObjectID(payloadObj.getID());
        eventBean.setDirectObjectType(payloadObj.getObjectType());
        if (payloadObj instanceof ContainerAware) {
            eventBean.setContainerObjectID(((ContainerAware) payloadObj).getContainerID());
            eventBean.setContainerObjectType(((ContainerAware) payloadObj).getContainerType());
        } else {
            log.info("Event is not a associated with container aware Jive Object.");
            eventBean.setContainerObjectID(-1);
            eventBean.setContainerObjectType(-1);
        }
        return eventBean;
    }

    private SessionEventBean handleCommentEvent(BaseJiveEvent baseJiveEvent, JiveObject payloadObj) {
        SessionEventBean eventBean = new SessionEventBean();
        eventBean.setActorID((Long) baseJiveEvent.getParams().get("userID"));
        eventBean.setDirectObjectID((Long) baseJiveEvent.getParams().get("commentResourceID"));
        eventBean.setDirectObjectType((Integer) baseJiveEvent.getParams().get("commentResourceType"));
        if (payloadObj instanceof ContainerAware) {
            eventBean.setContainerObjectID((Long) baseJiveEvent.getParams().get("containerID"));
            eventBean.setContainerObjectType((Integer) baseJiveEvent.getParams().get("containerType"));
        } else {
            log.info("Event is not a associated with container aware Jive Object.");
            eventBean.setContainerObjectID(-1);
            eventBean.setContainerObjectType(-1);
        }
        return eventBean;
    }

    private SessionEventBean handleFavoriteEvent(BaseJiveEvent baseJiveEvent, JiveObject payloadObj) {
        SessionEventBean eventBean = new SessionEventBean();
        // markedObj - JiveObject 
        // payLoadObj - Favourite obj
        EntityDescriptor entityDesc = (EntityDescriptor)baseJiveEvent.getActor();
        if(entityDesc != null){
            eventBean.setActorID(entityDesc.getID());
        }

        JiveObject markedObject = (JiveObject)baseJiveEvent.getParams().get("markedObject");
        if(markedObject != null){
            eventBean.setDirectObjectID(markedObject.getID());
            eventBean.setDirectObjectType(markedObject.getObjectType());
        }
        // Its not container aware event.
        // So put the favObj details as container details 
        eventBean.setContainerObjectID(-1);
        eventBean.setContainerObjectType(-1);
        return eventBean;
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
        log.info("Event target url - " + eventUrl);
        return eventUrl;
    }

    /**
     * Method returns a friendly Event Name
     *
     * @param baseJiveEvent
     * @return
     */
    private String getFriendlyEventName(BaseJiveEvent baseJiveEvent) {
        String eventName = "";
        boolean constructedEventName = false;
        // Track Comment event for Documents 
        if (baseJiveEvent instanceof CommentEvent && !constructedEventName) {
            log.info("CommentEvent : " + baseJiveEvent.getParams());
            eventName = "COMMENT_" + baseJiveEvent.getType().toString();
            constructedEventName = true;
        }
        // Track Read/Rated event for Documents
        if (baseJiveEvent instanceof DocumentEvent && !constructedEventName) {
            log.info("DocumentEvent : " + baseJiveEvent.getParams());
            eventName = "DOCUMENT_" + baseJiveEvent.getType().toString();
            constructedEventName = true;
        }
        // Track Bookmarked event for Documents
        if (baseJiveEvent instanceof FavoriteEvent && !constructedEventName) {
            log.info("FavoriteEvent : " + baseJiveEvent.getParams());
            eventName = "FAVORITE_" + baseJiveEvent.getType().toString();
            constructedEventName = true;
        }

        if (constructedEventName) {
            log.info("Event fired - " + eventName);
        } else {
            eventName = baseJiveEvent.getClass().getSimpleName();
            log.info("Event type not captured so set the class name to event name  " + eventName);
        }
        return eventName;
    }

    // From base - Analytics module - AnalyticsListener

    private AnalyticsEventCode getTypeForEvent(BaseJiveEvent bje) {

        // To make sure its initialized
        if (null == analyticsEventTypes) {
            analyticsEventTypes = analyticsListenerConfiguration.getMap();
        }
        ConfigurationEntry entry = null;
        if (bje instanceof ContentEvent) {
            ContentEvent ce = (ContentEvent) bje;
            if (ce.getContentModificationType() != null) {
                entry = analyticsEventTypes.get(ce.getContentModificationType());
            }
        } else if (bje instanceof ContainerEvent) {
            ContainerEvent ce = (ContainerEvent) bje;
            if (ce.getContainerModificationType() != null) {
                entry = analyticsEventTypes.get(ce.getContainerModificationType());
            }
        }
        if (entry == null) {
            entry = analyticsEventTypes.get(bje.getType());
        }

        return (entry != null && entry.isEnabled()) ? entry.getCode() : null;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // do-nothing
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        log.info("Session destroyed " + se.getSource().toString());
    }
}
