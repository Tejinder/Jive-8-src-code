package com.grail.synchro.events;

import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.GroupManager;
import com.jivesoftware.base.GroupNotFoundException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.event.GroupEvent;
import com.jivesoftware.base.event.v2.EventListener;
import com.jivesoftware.community.impl.search.user.ProfileSearchIndexManager;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Map;

/**
 * A custom EventListener which listens to any member who is has been added or removed from the JiveGroup.
 * This event listener is necessary for keeping the Cached Groups - SynchroUtils.synchroUserGroupsMap - update at all times.
 * Following are the Groups which are supported by the Listener:
 * SYNCHRO
 * MARKETING_APPROVERS
 * SPI_APPROVERS
 * LEGAL_APPROVERS
 * PROCUREMENT_APPROVERS
 * COORDINATION_AGENCY
 * CO_AGENCY_SUPPORT
 * FIELDWORK_AGENCY
 * COMMUNICATION_AGENCY
 *
 * @author: Vivek Kondur
 */
public class SynchroGroupEventListener implements EventListener<GroupEvent> {

    private static final Logger log = Logger.getLogger(SynchroGroupEventListener.class);

    private GroupManager groupManager;
    private SynchroUtils synchroUtils;
    private ProfileSearchIndexManager profileSearchIndexManager;
    
    @Required
    public void setProfileSearchIndexManager(
			ProfileSearchIndexManager profileSearchIndexManager) {
		this.profileSearchIndexManager = profileSearchIndexManager;
	}

    @Required
    public void setSynchroUtils(final SynchroUtils synchroUtils) {
        this.synchroUtils = synchroUtils;
    }

    @Required
    public final void setGroupManager(GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @Override
    public void handle(final GroupEvent e) {


        log.debug("\n\n\n Synchro GroupEventHandler : " + e);

        switch (e.getType()) {
            case USER_DELETED:
                handleMemberModified(e, e.getMember(), true);
                break;
            case USER_ADDED:
                handleMemberModified(e, e.getMember(), false);
                break;
            case ADMINISTRATOR_ADDED:
                handleMemberModified(e, e.getAdministrator(), false);
                break;
            case ADMINISTRATOR_DELETED:
                handleMemberModified(e, e.getAdministrator(), true);
                break;
            case DELETING:
                log.error("Group was delete  " + e.toString());
                break;
        }

    }

    /**
     * Handles both the Add & Delete events of a member from a Group
     * @param event GroupEvent
     * @param user JiveUser
     * @param delete Defaults to false, which means it's a ADD event. For delete the handler needs to be set as TRUE.
     */
    protected void handleMemberModified(final GroupEvent event, final User user, final boolean delete) {
        log.debug("Handling group event:   -- " + event);
        final String name = getGroupName(event.getPayload().getID());
        if (StringUtils.isNotEmpty(name)) {
            Map<String, List<User>> userGroupCacheMap = getSynchroUtils().getSynchroUserGroupsMap();
            List<User> members = userGroupCacheMap.get(name);
            if( members != null ) {
                updateUsers(members, name, user, delete);
            }
        }
        profileSearchIndexManager.updateIndex(user);
    }

    /**
     * Updates the existing Cache Lists. Following are the UserGroups which are cached.
     *
     * @param userList List of users which needs to be updated
     * @param user - User to be added or deleted
     * @param delete - false for Add. true for Delete
     * @return
     */
    private List<User> updateUsers(List<User> userList, User user, boolean delete) {
        if (delete) {
            if (userList.contains(user)) {
                userList.remove(user);
            }
        } else {
            if (!userList.contains(user)) {
                userList.add(user);
            }
        }
        return userList;
    }

    /**
     * Updates the existing Cache Lists. Following are the UserGroups which are cached.
     *
     * @param userList List of users which needs to be updated
     * @param grpName - Group name
     * @param user - User to be added or deleted
     * @param delete - false for Add. true for Delete
     * @return
     */
    private List<User> updateUsers(List<User> userList, String grpName, User user, boolean delete) {
        if (delete) {
            if (userList.contains(user)) {
                userList.remove(user);
            }
        } else {
            if (!userList.contains(user)) {
                userList.add(user);
            }
        }
        getSynchroUtils().getSynchroUserGroupsMap().put(grpName, userList);
        return userList;
    }

    /**
     * Returns the name of a Jive UserGroup for a given ObjectID
     * @param groupID - Unique Group Identifier
     * @return String name of the group
     */
    private String getGroupName(final Long groupID) {
        String name = null;
        try {
            name = this.groupManager.getGroup(groupID).getName();
        } catch (GroupNotFoundException e) {
            log.error("Undefined Jive Group. Synchro features will not work properly. Please configure.");
        }
        return name;
    }

    private SynchroUtils getSynchroUtils(){
        if( synchroUtils == null)
            synchroUtils =  new SynchroUtils();

        return synchroUtils;
    }


}
