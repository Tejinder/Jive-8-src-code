/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.action;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.util.SynchroUserPropertiesUtil;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;

import com.jivesoftware.base.Group;
import com.jivesoftware.base.GroupManager;

import com.jivesoftware.base.UserTemplate;
import com.jivesoftware.base.event.UserEvent;
import com.jivesoftware.base.event.v2.EventDispatcher;
import com.jivesoftware.base.util.UserPermHelper;
import com.jivesoftware.community.ApprovalWorkflowView;
import com.jivesoftware.community.AvatarManager;
import com.jivesoftware.community.Blog;
import com.jivesoftware.community.BlogManager;
import com.jivesoftware.community.BlogPost;
import com.jivesoftware.community.Community;
import com.jivesoftware.community.ContainerAware;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.EntityDescriptor;
import com.jivesoftware.community.ForumMessage;
import com.jivesoftware.community.ForumThread;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.ResultFilter;
import com.jivesoftware.community.UserContainerManager;
import com.jivesoftware.community.UserStatus;
import com.jivesoftware.community.aaa.AnonymousUser;
import com.jivesoftware.community.action.util.Pageable;
import com.jivesoftware.community.action.util.Paginator;
import com.jivesoftware.community.app.view.ActionLinkView;
import com.jivesoftware.community.browse.QueryFilterDef;
import com.jivesoftware.community.browse.filter.BrowseFilter;
import com.jivesoftware.community.browse.filter.MemberFilter;
import com.jivesoftware.community.browse.filter.ObjectTypeFilter;
import com.jivesoftware.community.browse.rest.impl.ItemsViewBean;
import com.jivesoftware.community.comments.Comment;
import com.jivesoftware.community.content.polls.Poll;
import com.jivesoftware.community.eae.ActivityManager;
import com.jivesoftware.community.eae.RecommendationManager;
import com.jivesoftware.community.eae.impl.ActivityContainer;
import com.jivesoftware.community.eae.mail.NotificationSettingsManager;
import com.jivesoftware.community.favorites.FavoriteProfileManager;
import com.jivesoftware.community.impl.ApprovalManagerImpl;
import com.jivesoftware.community.inbox.InboxEntry;
import com.jivesoftware.community.inbox.entry.NewUserRegistrationApprovalEntry;
import com.jivesoftware.community.invitation.Invitation;
import com.jivesoftware.community.invitation.impl.InvitationHelper;
import com.jivesoftware.community.mail.incoming.processors.create.CreateMailProcessorHelper;
import com.jivesoftware.community.microblogging.WallEntry;
import com.jivesoftware.community.microblogging.WallEntryManager;
import com.jivesoftware.community.moderation.JiveObjectModerator;
import com.jivesoftware.community.moderation.impl.ModerationServiceImpl;
import com.jivesoftware.community.project.Project;
import com.jivesoftware.community.project.Task;
import com.jivesoftware.community.socialgroup.SocialGroup;
import com.jivesoftware.community.socialgroup.SocialGroupMemberResultFilter;
import com.jivesoftware.community.socialgroup.action.YourSocialGroupRowDisplay;
import com.jivesoftware.community.statuslevel.StatusLevelManager;
import com.jivesoftware.community.user.profile.ProfileField;
import com.jivesoftware.community.user.profile.ProfileFieldValue;
import com.jivesoftware.community.user.profile.ProfileImage;
import com.jivesoftware.community.user.profile.UserProfile;
import com.jivesoftware.community.user.profile.security.ProfileSecurityManager;
import com.jivesoftware.community.user.rest.UserItemBean;
import com.jivesoftware.community.user.rest.UserService;
import com.jivesoftware.community.util.SkinUtils;
import com.jivesoftware.community.util.SocialGroupPermHelper;
import com.jivesoftware.conversion.ConversionManager;
import com.jivesoftware.eae.constants.StreamConstants;
import com.jivesoftware.util.LocaleUtils;
import com.jivesoftware.util.StringUtils;
import com.opensymphony.xwork2.Preparable;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.jivesoftware.community.moderation.JiveObjectModerator.Type;

@SuppressWarnings({"unchecked"})
public class ViewProfile extends ProfileAction
        implements Pageable, YourSocialGroupRowDisplay, YourStuffDisplay, Preparable,
        HasMetaKeywords, HasMetaDescription
{

    protected static final Logger log = LogManager.getLogger(ViewProfile.class);

    public static final String VIEW_BIO = "profile";        //bio
    public static final String VIEW_ACTIVITY = "activity";       //activity
    public static final String VIEW_CONTENT = "content";        //content
    public static final String VIEW_PLACES = "places";         //places
    public static final String VIEW_PEOPLE = "people";         //people
    public static final String VIEW_TASKS = "tasks";
    public static final String VIEW_BOOKMARKS = "bookmarks";
    public static final String VIEW_DEVICES = "devices";

    //TODO: determine which of these are still necessary
    public static final String VIEW_APPROVALS = "approvals";
    public static final String VIEW_FOLLOWING = "following";
    public static final String VIEW_VCARD = "vcard";
    public static final String VIEW_UPDATES = "updates";
    public static final String VIEW_PRIVATE_MESSAGES = "pm";

    public static final String FILTER_ALL = "all";

    public static final String SUCCESS_VCARD = "success-vcard";
    public static final String SUCCESS_REDIRECT = "refresh";
    public static final String SUCCESS_UPDATES = "success-updates";

    protected String view = JiveGlobals.getJiveProperty("profile.view.default", VIEW_BIO);
    protected String filter = FILTER_ALL;
    protected long blogID = -1;

    protected String username;
    protected long userID;
    protected String uri;
    protected Map<Long, ProfileFieldValue> profile;
    protected List<ProfileField> fields;
    protected List<User> escalationChain;

    protected User targetUser;
    protected String targetUserDisplayName;
    protected WallEntry targetWallEntry;

    // profile view
    protected Iterator<ActivityContainer> userActivity;

    // Overview View
    protected Iterator<Comment> contentComments;

    // Tasks View
    protected Task task;

    // Documents View
    protected Iterable<Document> documentApproval;
    protected Iterable<Document> reviewDocuments;

    // Counts
    protected int pendingApprovalCount = 0;
    protected int approvalItemCount = 0;

    protected Blog personalBlog;

    protected Collection<ApprovalWorkflowView> approvalQueue;
    private List<Invitation> invitations;

    //Social groups
    Iterator<SocialGroup> socialGroups;

    // status updates
    protected List<UserStatus> updates;
    protected boolean filterStatus = false;

    // user blog
    protected Blog blog;
    protected int blogPostCount;
    protected boolean canCreatePersonalBlog;

    // Pageable
    protected int start;
    protected int numResults = 15;
    protected int totalItemCount;

    // Review pageable
    protected int reviewDocumentStart;
    protected int reviewDocumentNumResults = 5;

    //social group pagination sort
    protected String socialGroupSort = SortField.activity.name();
    protected int numSocialGroupResults = 5;

    // favorites
    protected boolean showBookmarkletAnnouncement;
    protected boolean showBookmarklet;
    protected String communityName;

    protected AvatarManager avatarManager;
    protected BlogManager blogManager;
    protected DocumentManager documentManager;
    protected WallEntryManager wallEntryManager;
    protected ActivityManager activityManager;
    protected UserProfile userProfile;
    protected UserContainerManager userContainerManager;
    protected JiveObjectModerator jiveObjectModerator;
    protected RecommendationManager recommendationManager;
    protected EventDispatcher eventDispatcher;
    protected FavoriteProfileManager favoriteProfileManager;
    //protected MediaManager mediaManager;
    protected InvitationHelper invitationHelper;
    protected NotificationSettingsManager notificationSettingsManager;

    protected ProfileSecurityManager profileSecurityManager;

    protected StatusLevelManager statusLevelManager;

    protected String viewProfileContentTypeURL;
    protected int objectTypeID;

    private com.jivesoftware.util.DateUtils dateUtils;
    private List<Invitation> newInvitations;
    private List<Invitation> fullfilledInvitations;

    boolean profileImageEnabled;
    private Collection<ProfileImage> profileImages;
    boolean statusLevelEnabled;

    protected ConversionManager conversionManager;
    private UserService userService;

    private int socialGroupsMembershipCount;

    private GroupManager groupManager;
    private Boolean fromSynchro = false;
    
    private Integer brand = -1;
    private Integer region = -1;
    private Integer country = -1;    
    private Integer jobTitle = -1;
    
    public final void setConversionManager(ConversionManager conversionManager) {
        this.conversionManager = conversionManager;
    }

    public final JiveContainer getContainer() {
        return communityManager.getRootCommunity();
    }

    public final void setAvatarManager(AvatarManager avatarManager) {
        this.avatarManager = avatarManager;
    }

    public final void setBlogManager(BlogManager blogManager) {
        this.blogManager = blogManager;
    }

    public final void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    public final void setWallEntryManager(WallEntryManager wallEntryManager) {
        this.wallEntryManager = wallEntryManager;
    }

    public final void setUserServiceImpl(UserService userServiceImpl) {
        this.userService = userServiceImpl;
    }

    public final void setActivityManager(ActivityManager activityManager) {
        this.activityManager = activityManager;
    }

    public final void setUserContainerManager(UserContainerManager userContainerManager) {
        this.userContainerManager = userContainerManager;
    }

    public final void setJiveObjectModerator(JiveObjectModerator jiveObjectModerator) {
        this.jiveObjectModerator = jiveObjectModerator;
    }

    public final void setFavoriteProfileManager(FavoriteProfileManager favoriteProfileManager) {
        this.favoriteProfileManager = favoriteProfileManager;
    }

    public final void setProfileSecurityManager(ProfileSecurityManager profileSecurityManager) {
        this.profileSecurityManager = profileSecurityManager;
    }

    public final void setStatusLevelManager(StatusLevelManager statusLevelManager) {
        this.statusLevelManager = statusLevelManager;
    }

    public final void setJiveEventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public final InvitationHelper getInvitationHelper() {
        return invitationHelper;
    }

    public final void setInvitationHelper(InvitationHelper invitationHelper) {
        this.invitationHelper = invitationHelper;
    }

    public final void setNotificationSettingsManager(NotificationSettingsManager notificationSettingsManager) {
        this.notificationSettingsManager = notificationSettingsManager;
    }

    public int getSocialGroupsMembershipCount() {
        return socialGroupsMembershipCount;
    }

    public void prepare() {
        dateUtils = new com.jivesoftware.util.DateUtils(getRequest(), getUser());
        profileImageEnabled = profileManager.isProfileImageEnabled();
        statusLevelEnabled = statusLevelManager.isStatusLevelsEnabled();
    }

    public String execute() {
        if (AnonymousUser.isAnonymousUser(getUserID()) && !profileManager.isAnonymousBrowsingPeoplePermitted()) {
            log.info("User " + getUserID() + " is not authorized to view the profile of " + targetUser);
            return UNAUTHENTICATED;
        }

        loadTargetUser();

        if (targetUser == null) {
            return userNotFound();
        }

        if (!isViewingSelf() && !targetUser.isAnonymous()) {
            targetUserDisplayName = SkinUtils.getDisplayName(targetUser);
        }
        else {
            targetUserDisplayName = "";
        }

        try {
            targetWallEntry = (WallEntry)wallEntryManager.getCurrentStatus(targetUser);
        }
        catch (UnauthorizedException e) {
            // Ignore, means user is not able to view status updates at all
        }

        fields = profileFieldManager.getProfileFields();

        profile = profileManager.getProfile(targetUser);
        long profileImageSecurityLevelID = profileSecurityManager
                .getNonProfileFieldSecurityLevelID(User.IMAGE_PROFILE_SECURITY_LEVEL, getTargetUser());
        profileImageEnabled = profileImageEnabled && profileSecurityManager
                .isValidViewer(getUser(), getTargetUser(), profileImageSecurityLevelID);

        profileImages = new ArrayList<ProfileImage>();
        if (profileImageEnabled) {
            profileImages = profileImageManager.getProfileImagesByIndex(getTargetUser());
        }

        //we set this attribute so that other actions involved in this page render can access it
        request.setAttribute("targetUser", getTargetUser());

        //fire event before any SUCCESS returned, but after any errors returned
        if (eventDispatcher != null && (VIEW_ACTIVITY.equals(view) || VIEW_BIO.equals(view))) {
            Map<String, String> params = new HashMap<>();
            params.put(UserEvent.PROFILE_VIEW_TYPE, "full-" + view);
            eventDispatcher.fire(new UserEvent(UserEvent.Type.PROFILE_VIEWED, new UserTemplate(targetUser), params));
        }

        // for displaying the profile tabs
        if (isViewingSelf()) {
            approvalItemCount = approvalManager.getPendingCount(getUser(), ApprovalManagerImpl.UNLIMITED_APPROVAL_MANAGER_ITEMS, Type.APPROVAL);
            newInvitations = getInvitationHelper().getUnreadInvitations(getUser());
        }

        //handle legacy "overview" view gracefully
        if ("overview".equals(view)) {
            view = VIEW_ACTIVITY;
        }

        if (VIEW_ACTIVITY.equals(view)) {
            // TODO[TW] - remove?
            userActivity = new ArrayList<ActivityContainer>().iterator();
        }

        //show people if not anonymous
        if ((VIEW_PEOPLE.equals(view) || VIEW_FOLLOWING.equals(view)) && (AnonymousUser.isAnonymousUser(getUserID())
                ||
                //or approvals are not enabled or we can view lables
                ((userRelationshipManager.getDefaultMeshRelationshipGraph().isApprovalsEnabled()
                        && !userRelationshipManager.canViewRelationshipLists(targetUser))
                        //or if orgcharting is enabled
                        && !userRelationshipManager.isOrgChartingEnabled())))
        {
            log.info("Unauthorized for friends view");
            return UNAUTHORIZED;
        }

        //load escalation chain on bio view
        if (VIEW_BIO.equals(view) && userRelationshipManager.isOrgChartingEnabled()) {
            try {
                escalationChain = userRelationshipManager.getEscalationChain(targetUser, userRelationshipManager.getDefaultHierarchicalRelationshipGraph());
            }
            catch (UnauthorizedException e) {
                //ignore
            }
        }

        // load content
        Community root = communityManager.getRootCommunity();

         if (VIEW_VCARD.equals(view)) {
            return SUCCESS_VCARD;
        }
        else if (VIEW_UPDATES.equals(view) && (wallEntryManager.isEnabled() || wallEntryManager.isPlacesEnabled())) {
            updates = wallEntryManager.getRecentStatusUpdates(getTargetUser());
            return SUCCESS_UPDATES;
        }/*
        else if (VIEW_BOOKMARKS.equals(view) && isFavoritingEnabled()) {
            this.showBookmarkletAnnouncement = getUser().equals(targetUser)
                    && favoriteProfileManager.isBookmarkletAnnouncementVisible(getUser());
            this.showBookmarklet = getUser().equals(targetUser)
                    && favoriteProfileManager.isBookmarkletEnabled();
            this.communityName = root.getName();
            return SUCCESS_BOOKMARKS;
        }*/
        else {
            // we don't know the view (could be a plugin) so do nothing
        }

        if (targetUser.isPartner() && !getUser().isAnonymous() && !getUser().isPartner()) {
            socialGroupsMembershipCount = browseManager.getObjectCount(ImmutableSet.<BrowseFilter>of(new ObjectTypeFilter(new Integer(JiveConstants.SOCIAL_GROUP)), new MemberFilter().getBoundInstance(targetUser.getID())),
                                                                 QueryFilterDef.Archetype.Container);
        }
        return SUCCESS;
    }

    protected UserManager getUserManager() {
        return userManager;
    }

    @Override
    public String getCoreAPIType() {
        return "osapi.jive.core.User";
    }

    public long getCoreAPIID() {
        return targetUser == null ? -1L : targetUser.getID();
    }

    @Override
    public JiveObject getJiveObject() {
        return targetUser;
    }

    protected void loadTargetUser() {
        try {
            if (uri != null) {
                final JiveObject object = jiveObjectProvider.object(uri);
                if (object != null && object instanceof User) {
                    targetUser = (User) object;
                }
            }
            if (targetUser == null && username != null) {
                targetUser = getUserManager().getUser(new UserTemplate(username));
            }

            if (targetUser == null) {
                targetUser = getUserManager().getUser(userID);
            }

            if (targetUser != null) {
                // prevent profile access to users who are not visible or are external
                if (!targetUser.isVisible() || targetUser.isExternal()) {
                    targetUser = null;
                }
            }
        }
        catch (NotFoundException e) {
            targetUser = null;
        }
    }

    Collection<ActionLinkView> profileAppActionLinks;
    public Collection<ActionLinkView> getProfileAppActionLinks() {
        if (profileAppActionLinks == null) {
            profileAppActionLinks = getActionLinkHelper().get(Arrays.asList("jive/actions/profile"), getAuthenticationProvider().getJiveUser(),
                    LocaleUtils.getCurrentUserLocale(), targetUser);
        }
        return profileAppActionLinks;
    }


    public void setUserID(long userID) {
        this.userID = userID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public Map<Long, ProfileFieldValue> getProfile() {
        return profile;
    }

    public List<ProfileField> getFields() {
        return fields;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public long getBlogID() {
        return blogID;
    }

    public void setBlogID(long blogID) {
        this.blogID = blogID;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getNumResults() {
        return numResults;
    }

    public int getTotalItemCount() {
        return totalItemCount;
    }

    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }

    public int getReviewDocumentStart() {
        return reviewDocumentStart;
    }

    public void setReviewDocumentStart(int reviewDocumentStart) {
        this.reviewDocumentStart = reviewDocumentStart;
    }

    public int getReviewDocumentNumResults() {
        return reviewDocumentNumResults;
    }

    public void setReviewDocumentNumResults(int reviewDocumentNumResults) {
        this.reviewDocumentNumResults = reviewDocumentNumResults;
    }

    public Iterable<Document> getDocumentApproval() {
        return documentApproval;
    }

    public Iterable<Document> getReviewDocuments() {
        return reviewDocuments;
    }

    public List<UserStatus> getUpdates() {
        return updates;
    }

    public Iterator<Comment> getContentComments() {
        return contentComments;
    }

    public Iterator<SocialGroup> getSocialGroups() {
        return socialGroups;
    }

    public UserProfile getProfileObject() {
        if (userProfile == null) {
            userProfile = userProfileProvider.get(getTargetUser());
        }
        return userProfile;
    }

    public List<User> getEscalationChain() {
        return escalationChain;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getSocialGroupSort() {
        return socialGroupSort;
    }

    public void setSocialGroupSort(String socialGroupSort) {
        this.socialGroupSort = socialGroupSort;
    }

    public int getNumSocialGroupResults() {
        return numSocialGroupResults;
    }

    public void setNumSocialGroupResults(int numSocialGroupResults) {
        this.numSocialGroupResults = numSocialGroupResults;
    }

    public SocialGroup getSocialGroup(long socialGroupID) {
        SocialGroup socialGroup = null;
        try {
            socialGroup = socialGroupManager.getSocialGroup(socialGroupID);
        }
        catch (NotFoundException e) {
            log.error("Failed to load social group with id: " + socialGroupID);
        }
        return socialGroup;
    }

    public Blog getPersonalBlog() {
        if (personalBlog == null) {
            try {
                personalBlog = blogManager.getBlog(userContainerManager.getUserContainer(getTargetUser()));
            }
            catch (UnauthorizedException ue) {
                // user cannot see blog therefore we should treat it as if they do not have a blog
                // http://jira.jivesoftware.com/browse/CS-16134
                personalBlog = null;
            }
        }

        return personalBlog;
    }

    public boolean isPersonalExists() {
        return getPersonalBlog() != null;
    }

    public boolean isCanCreatePersonalBlog() {
        return canCreatePersonalBlog;
    }

    public Paginator getNewPaginator() {
        return new Paginator(this);
    }

    public boolean isUserAdmin() {
        return UserPermHelper.isGlobalUserAdmin() && UserPermHelper.getCanManageUser(targetUser);
    }

    public boolean isViewingSelf() {
        return !AnonymousUser.isAnonymousUser(getUserID()) && getUserID() == targetUser.getID();
    }

    public boolean isCurrentStatusValid() {
        UserStatus userStatus = null;
        try {
            userStatus = wallEntryManager.getCurrentStatus(targetUser);
        }
        catch (UnauthorizedException e) {
            // Ignore, means user is not able to view status updates at all
        }
        return (userStatus != null && userStatus.getStatusText() != null
                && userStatus.getStatusText().trim().length() != 0);
    }

    public String getCurrentStatus() {
        UserStatus userStatus;
        try {
            userStatus = wallEntryManager.getCurrentStatus(targetUser);
        }
        catch (UnauthorizedException e) {
            // Ignore, means user is not able to view status updates at all
            return null;
        }

        return userStatus.getStatusText();
    }

    public Collection<ProfileImage> getProfileImages() {
        return profileImages;
    }

    public boolean isProfileImageEnabled() {
        return profileImageEnabled;
    }

    public boolean isStatusLevelEnabled() {
        return statusLevelEnabled;
    }

    /**
     * Returns true if the current user can edit avatars.
     *
     * @return true if the current user can edit avatars.
     */
    public boolean isAvatarEditEnabled() {
        return (avatarManager.isAvatarsEnabled() && UserPermHelper.getCanEditAvatar());
    }

    public boolean isEditProfileEnabled() {
        return !userManager.isCreationSupported();
    }

    public Iterator<ActivityContainer> getUserActivity() {
        return userActivity;
    }

    public Document getDocument(long docId) {
        try {
            return getJiveContext().getDocumentManager().getDocument(docId);
        }
        catch (Throwable t) {
            log.warn("Problem retrieving document - " + docId, t);
            return null;
        }
    }

    public ForumThread getForumThread(long threadId) {
        try {
            return getJiveContext().getForumManager().getForumThread(threadId);
        }
        catch (Throwable t) {
            log.warn("Problem retrieving thread - " + threadId, t);
            return null;
        }
    }

    public ForumMessage getMessage(long messageId) {
        try {
            return getJiveContext().getForumManager().getMessage(messageId);
        }
        catch (Throwable t) {
            log.warn("Problem retrieving message - " + messageId, t);
            return null;
        }
    }

    public BlogPost getBlogPost(long postId) {
        try {
            return getJiveContext().getBlogManager().getBlogPost(postId);
        }
        catch (Throwable t) {
            log.warn("Problem retrieving blog post - " + postId, t);
            return null;
        }
    }

    public Poll getPoll(long pollId) {
        try {
            return getJiveContext().getPollManager().get(pollId);
        }
        catch (Throwable t) {
            log.warn("Problem retrieving poll - " + pollId, t);
            return null;
        }
    }

    public User getUser(long userId) {
        try {
            return jiveObjectLoader.getUser(userId);
        }
        catch (Throwable t) {
            log.warn("Problem retrieving user - " + userId, t);
            return null;
        }
    }

    public int getPendingApprovalCount() {
        return pendingApprovalCount;
    }

    public Collection<ApprovalWorkflowView> getApprovalQueue() {
        return approvalQueue;
    }

    public Collection<Invitation> getInvitations() {
        return invitations;
    }

    public int getInvitationCount() {
        return invitations != null ? invitations.size() : 0;
    }

    public int getUnreadInvitationCount() {
        return newInvitations != null ? newInvitations.size() : 0;
    }

    public boolean isUnreadInvitation(Invitation invitation) {
        return invitation.getState() == Invitation.State.sent;
    }

    public String getTimeZoneString(TimeZone tz) {
        for (String[] item : LocaleUtils.getTimeZoneList()) {
            if (tz.getID().equals(item[0])) {
                return item[1];
            }
        }
        return tz.getDisplayName(JiveGlobals.getLocale());
    }

    protected <T extends ResultFilter> T setIndexes(T filter) {
        if (start > 0) {
            filter.setStartIndex(start);
        }
        if (numResults > 0) {
            filter.setNumResults(numResults);
        }
        return filter;
    }

    protected User getUserForContext() {
        if (isViewingSelf()) {
            return getUser();
        }
        else {
            return targetUser;
        }
    }

    protected String userNotFound() {
        log.debug("Could not find user with username: '" + username + "' or userID: " + userID);
        return NOTFOUND;
    }

    public boolean isSocialGroupImageAvailable(SocialGroup socialGroup) {
        return socialGroup != null && socialGroup.getContainerImage(0) != null;
    }

    public int getTotalUserCount(SocialGroup socialGroup) {
        if (socialGroup != null) {
            return socialGroupManager.getTotalUsersInGroup(socialGroup);
        }

        return 0;
    }

    public boolean isSocialGroupOwner(SocialGroup socialGroup) {
        return socialGroup != null && socialGroupManager.isSocialGroupOwner(socialGroup, getUserForContext());
    }

    public boolean isViewerSocialGroupOwner(SocialGroup socialGroup) {
        return socialGroup != null && socialGroupManager.isSocialGroupOwner(socialGroup, getUser());
    }

    public boolean isOnlyOneSocialGroupOwner(SocialGroup socialGroup) {
        return 1 == socialGroupManager
                .getTotalMemberships(SocialGroupMemberResultFilter.createOwnerFilter(socialGroup));
    }

    public boolean getCanLeaveSocialGroup(SocialGroup socialGroup) {
        return SocialGroupPermHelper.getCanLeaveSocialGroup(socialGroup, getUser());
    }

    public boolean getCanViewSocialGroupContent(SocialGroup socialGroup) {
        return SocialGroupPermHelper.getCanViewSocialGroupContent(socialGroup, getUser());
    }

    public Blog getBlog() {
        if (blog == null) {
            blog = blogManager.getBlog(userContainerManager.getUserContainer(getTargetUser()));
        }
        return blog;
    }

    public int getBlogPostCount() {
        return getBlog() != null ? getBlog().getBlogPostCount() : 0;
    }

    public int getApprovalItemCount() {
        return approvalItemCount;
    }

    public String getViewProfileContentTypeURL() {
        return viewProfileContentTypeURL;
    }

    public void setViewProfileContentTypeURL(String viewProfileContentTypeURL) {
        this.viewProfileContentTypeURL = viewProfileContentTypeURL;
    }

    public int getObjectTypeID() {
        return objectTypeID;
    }

    public void setObjectTypeID(int objectTypeID) {
        this.objectTypeID = objectTypeID;
    }

    public boolean isShowGuestLoginOrRegisterMessage() {
        return isGuest();
    }

    public boolean isAccountCreationEnabled() {
        return registrationManager.isNewAccountCreationEnabled();
    }

    public boolean isShowBookmarkletAnnouncement() {
        return showBookmarkletAnnouncement;
    }

    public boolean isShowBookmarklet() {
        return showBookmarklet;
    }

    public String getCommunityName() {
        return communityManager.getRootCommunity().getName();
    }

    public String displayHumanReadable(Date date) {
        return getDateUtils().displayFriendly(date);
    }

    public com.jivesoftware.util.DateUtils getDateUtils() {
        return dateUtils;
    }

    public String getPersonalCreateContentEmail(int contentObjectType) {
        return CreateMailProcessorHelper
                .getPersonalContentEmail(contentObjectType, userContainerManager.getUserContainer(getUser()));
    }

    public boolean isProfileImageModerationEnabled() {
        return jiveObjectModerator
                .isModerationEnabled(new EntityDescriptor(JiveConstants.PROFILE_IMAGE, -1), getTargetUser());
    }

    public boolean isRecommendationsEnabled() {
        return recommendationManager.isEnabled();
    }

    public void setRecommendationManager(RecommendationManager recommendationManager) {
        this.recommendationManager = recommendationManager;
    }

    public boolean isInvitaionToPrivateContent(Invitation invitation) {
        JiveObject entity = invitation.getJiveObject();
        try {
            JiveObject object = getObjectTypeManager().getObjectType(entity.getObjectType()).getObjectFactory()
                    .loadObject(entity.getID());
            if (object instanceof ContainerAware) {
                return ((ContainerAware) object).getContainerType() == JiveConstants.USER_CONTAINER;
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public String getTargetUserDisplayName() {
        return targetUserDisplayName;
    }

    public WallEntry getTargetWallEntry() {
        return targetWallEntry;
    }

    public boolean isFollowed(Project project) {
        return streamManager.isAssociated(getUser(), new EntityDescriptor(project));
    }

    public boolean isFollowed(Community community) {
        return streamManager.isAssociated(getUser(), new EntityDescriptor(community));
    }

    public int getStreamsAssociatedCount(Project project) {
        return getStreamsAssociatedCount(new EntityDescriptor(project));
    }

    public int getStreamsAssociatedCount(Community community) {
        return getStreamsAssociatedCount(new EntityDescriptor(community));
    }

    public boolean isTracked() {
        return streamManager.isAssociated(getUser(), new EntityDescriptor(targetUser),
                StreamConstants.StreamSource.communications);
    }

    public boolean isWatching() {
        return isWatching(targetUser);
    }

    public boolean isWatchingEnabled() {
        return notificationSettingsManager.isNotificationsEnabled();
    }

    public boolean isFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(boolean filterStatus) {
        this.filterStatus = filterStatus;
    }

    public long getApprovalEntryID() {
        InboxEntry inboxEntry = inboxManager.get(NewUserRegistrationApprovalEntry.code, getUserID(),
                targetUser.getObjectType(), targetUser.getID(), InboxEntry.State.awaiting_action);
        if (inboxEntry != null) {
            return inboxEntry.getEntryID();
        }
        return -1;
    }

    @Override
    public String buildMetaKeywords() {
        if (VIEW_PEOPLE.equals(view)) {
            // hard-coded values copied (in desperation) from ProfilePeopleBrowseAction. Strings & ints are not good API
            ItemsViewBean<UserItemBean> people = userService
                    .getUsers(String.valueOf(getTargetUser().getID()), 0, 0, "profilePeople",
                            Collections.singletonList("following"), "following~firstNameAsc", 1,
                            0, 10, null, 0L, null, null);
            Iterable<String> names = Iterables.transform(people.getItems(), new Function<UserItemBean, String>() {
                @Override
                public String apply(UserItemBean input) {
                    return input.getDisplayName();
                }
            });
            return new MetaKeywordBuilder(tagActionUtil)
                    .tagNames(names)
                    .overflow("and others")
                    .build();
        }
        return new MetaKeywordBuilder(tagActionUtil)
                .object(getProfileObject())
                .build();
    }

    @Override
    public String buildMetaDescription() {
        User targetUser = getTargetUser();
        String displayName = "";
        String targetUsername = null;
        if (targetUser != null) {
            displayName = SkinUtils.getDisplayName(targetUser);
            targetUsername = targetUser.getUsername();
        }
        displayName = StringUtils.trimToEmpty(displayName);
        if ("".equals(displayName) || displayName.equals(targetUsername)) {
            displayName = getText("profile.meta.description.target_user");
        }

        final String i18nKey = String.format("profile.meta.description.%s", view);
        return getText(i18nKey, "", new String[]{displayName, getCommunityName()});
    }

    public int getModerationNoteMaxLength() {
        return ModerationServiceImpl.MAX_NOTE_LENGTH;
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

	public void setGroupManager(GroupManager groupManager) {
		this.groupManager = groupManager;
	}
}
