package com.grail.custom.analytics;

import com.jivesoftware.base.User;
import com.jivesoftware.base.event.DitransitiveEvent;
import com.jivesoftware.base.event.MetadataEvent;
import com.jivesoftware.base.event.UserEvent;
import com.jivesoftware.base.event.v2.BaseJiveEvent;
import com.jivesoftware.community.*;
import com.jivesoftware.community.aaa.AnonymousUser;
import com.jivesoftware.community.aaa.SystemUser;
import com.jivesoftware.community.analytics.AnalyticsEventCode;
import com.jivesoftware.community.analytics.dao.impl.AnalyticsEventBean;
import com.jivesoftware.community.eae.follows.FollowingBean;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Samee
 * Date: Apr 23, 2010
 * Time: 5:03:33 PM
 * 
 */
public class SessionEventBean {

    private Map<String, Object> eventParams;

    // FROM - AnalyticsEventBean
    private long eventDate;
    private long actorID;
    private int directObjectType;
    private long directObjectID;
    private int indirectObjectType;
    private long indirectObjectID;
    private int containerObjectType;
    private long containerObjectID;

    private int code;
    private final static int NO_ACTOR = -1;

    private int ID;
    private String name;
    private String url;

    
    public SessionEventBean(){
        
    }

    // FROM - AnalyticsEventBean
    public SessionEventBean(BaseJiveEvent bje, int code, Map<String, Object> params) {

        this.eventParams = params;
        
        //set date
        Date date = bje.getDate();
        this.eventDate =  EventAnalyticsUtil.getCurrentLongTime(date);  //new java.sql.Timestamp((date != null) ? date.getTime() : new Date().getTime());

        this.code = code;

        //set direct object
        Object directObject = bje.getPayload();

        if (directObject == null) {
            this.directObjectType = -1;
            this.directObjectID = -1L;
        }
        else {
//            if (directObject instanceof Watch) {
//                this.directObjectType = ((Watch) directObject).getObjectType();
//                this.directObjectID = ((Watch) directObject).getObjectID();
//            }
//            else if (directObject instanceof FollowingBean) {
//                this.directObjectType = ((FollowingBean) directObject).getContainerType();
//                this.directObjectID = ((FollowingBean) directObject).getContainerID();
//            }

            //NOTE: Using activity engine 'FollowingBean' (com.jivesoftware.community.eae.follows.FollowingBean) since report is related to user or document activity
            if(directObject instanceof FollowingBean) {
                this.directObjectType = ((FollowingBean) directObject).getObjectType();
                this.directObjectID = ((FollowingBean) directObject).getID();
            } else if (directObject instanceof JiveObject) {
                this.directObjectType = ((JiveObject) directObject).getObjectType();
                this.directObjectID = ((JiveObject) directObject).getID();
            }
            else {
                throw new IllegalArgumentException("Event payloads must be a JiveObject :" + bje);
            }
        }

        //set actor
        long aID = bje.getActorID();

        //if we have no actor ID or actor is system user and target is user or user container, use user's ID
        //NOTE: this is for login events and user creation events
        if ((this.code == AnalyticsEventCode.LOG_IN.getId() || (bje instanceof UserEvent
                && this.code == AnalyticsEventCode.CREATE.getId()))
                && (aID == SystemUser.SYSTEM_USER_ID || aID == NO_ACTOR) && (directObjectType == JiveConstants.USER
                || this.directObjectType == JiveConstants.USER_CONTAINER))
        {
            this.actorID = ((User) directObject).getID();
        }
        else {
            this.actorID = aID;
        }

        //set container
        if (directObject instanceof ContainerAware) {
            this.containerObjectID = ((ContainerAware) directObject).getContainerID();
            this.containerObjectType = ((ContainerAware) directObject).getContainerType();
        }
        else if (directObject instanceof JiveContainer) {
            JiveContainer parentContainer = ((JiveContainer) directObject).getParentContainer();
            if (parentContainer != null) {
                this.containerObjectID = parentContainer.getID();
                this.containerObjectType = parentContainer.getObjectType();
            }
            else {
                this.containerObjectID = -1;
                this.containerObjectType = -1;
            }
        }
        else {
            this.containerObjectID = -1;
            this.containerObjectType = -1;
        }

        if (bje instanceof DitransitiveEvent) {
            this.indirectObjectID = ((DitransitiveEvent) bje).getIndirectObjectID();
            this.indirectObjectType = ((DitransitiveEvent) bje).getIndirectObjectType();
        }
        else {
            this.indirectObjectID = -1;
            this.indirectObjectType = -1;
        }
    }
    
    public Map<String, Object> getEventParams() {
        return eventParams;
    }

    public void setEventParams(Map<String, Object> eventParams) {
        this.eventParams = eventParams;
    }

    
    public long getActorID() {
        return actorID;
    }

    public void setActorID(long actorID) {
        this.actorID = actorID;
    }

    public int getDirectObjectType() {
        return directObjectType;
    }

    public void setDirectObjectType(int directObjectType) {
        this.directObjectType = directObjectType;
    }

    public long getDirectObjectID() {
        return directObjectID;
    }

    public void setDirectObjectID(long directObjectID) {
        this.directObjectID = directObjectID;
    }

    public int getIndirectObjectType() {
        return indirectObjectType;
    }

    public void setIndirectObjectType(int indirectObjectType) {
        this.indirectObjectType = indirectObjectType;
    }

    public long getIndirectObjectID() {
        return indirectObjectID;
    }

    public void setIndirectObjectID(long indirectObjectID) {
        this.indirectObjectID = indirectObjectID;
    }

    public int getContainerObjectType() {
        return containerObjectType;
    }

    public void setContainerObjectType(int containerObjectType) {
        this.containerObjectType = containerObjectType;
    }

    public long getContainerObjectID() {
        return containerObjectID;
    }

    public void setContainerObjectID(long containerObjectID) {
        this.containerObjectID = containerObjectID;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getEventDate() {
        return eventDate;
    }

    public void setEventDate(long eventDate) {
        this.eventDate = eventDate;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("EventName: " + name).
                append("\nActorID:  " + actorID).
                append("\nEventID:  " + ID).
                append("\nObjectID: "+ directObjectID).
                append("\nObjectType : " + directObjectType).
                append("\nContainerID : " + containerObjectID).
                append("\nContainerType :" + containerObjectType).
                append("\nUrl  :" + url).
                append("\nEvent params  :" + eventParams).
                append("\nEvent Time :" + eventDate);
        return sb.toString();
    }

    public boolean isValidAnalyticsEvent() {
        // do not allow "anonymous user modifying a user object" scenario
        // this occurs during a log in because the system executor updates
        // the user's last login date but the original authorization is the anonymous user
        if (directObjectType == JiveConstants.USER && this.actorID == AnonymousUser.ANONYMOUS_ID) {
            return false;
        }
        if (directObjectType == JiveConstants.DOCUMENT && this.containerObjectType == JiveConstants.USER_CONTAINER) {
            return false;
        }

        //allow any user or anonymous user
        return this.actorID > 0 || this.actorID == AnonymousUser.ANONYMOUS_ID;
    }
}
