/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.action;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.proxy.ProxyUtils;
import com.jivesoftware.base.proxy.UserProxy;
import com.jivesoftware.community.ForumMessage;
import com.jivesoftware.community.ForumMessageNotFoundException;
import com.jivesoftware.community.ForumThread;
import com.jivesoftware.community.ForumThreadNotFoundException;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.ReadTrackerManager;
import com.jivesoftware.community.ResultFilter;
import com.jivesoftware.community.ThreadResultFilter;
import com.jivesoftware.community.TreeWalker;
import com.jivesoftware.community.UserContainer;
import com.jivesoftware.community.aaa.AnonymousUser;
import com.jivesoftware.community.action.util.Pageable;
import com.jivesoftware.community.action.util.Paginator;
import com.jivesoftware.community.analytics.impact.ImpactStatsActionHelper;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.places.rest.ContainerService;
import com.jivesoftware.community.places.rest.Place;
import com.jivesoftware.community.proxy.ForumMessageProxy;
import com.jivesoftware.community.util.AttachmentPermHelper;
import com.jivesoftware.community.util.CommunityPermHelper;
import com.jivesoftware.community.util.ThreadedViewModerationIterator;
import com.jivesoftware.community.util.collect.Maps;
import com.jivesoftware.community.web.JiveResourceResolver;
import com.jivesoftware.util.StringUtils;
import com.jivesoftware.util.URLUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.jivesoftware.community.places.rest.Place;

/**
 * <p>This class is a Struts action which encapsulates all the logic of loading and validating a ForumThread object. You
 * can get a list of messages from this action - the list is generated from the start parameter and the number of
 * messages in the list is based on the user's preference.</p> <p/> <p>Additionally, this action will tell the developer
 * what the next and previous threads are for this thread.</p>
 */
public class ForumThreadAction extends ThreadActionSupport implements Pageable, HasMetaKeywords, HasBadgeableAvatars {

    private static final Logger log = LogManager.getLogger(ForumThreadAction.class);

    private String threadMode;

    private boolean abuseReported;

    // Pageable
    private int start = 0;
    private Integer tstart;

    private ThreadResultFilter resultFilter;
    private boolean isPdf;
    protected boolean isDisplayFullThread = false;
    private ReadTrackerManager readTrackerManager;
    private String fromQuest;
    private int questStep;
    private boolean userReportedAbuse;

    private ImpactStatsActionHelper impactStatsActionHelper;
    
    private List<Place> parents;

    
    private ContainerService containerServiceImpl;

    /* TODO uncomment when implementing ajax calls to this action for inline message replies
    // Posting from and inline rte
    private boolean inlineMsgPost = false;
    public static final String SUCCESS_INLINE_MSG_POST = "success-inline-msg-post";*/

    // action methods

    /**
     * Only relevant when displaying the thread in flat mode, this property will force the thread to be displayed in
     * its entirety without pagination.
     *
     * @param displayFullThread true to display the thread in its entirety without pagination and false to paginate
     * after the configured number of messages have been shown.
     * @since 2.5.3 and 2.0.9
     */
    public void setDisplayFullThread(boolean displayFullThread) {
        this.isDisplayFullThread = displayFullThread;
    }

    public void setPdf(boolean pdf) {
        isPdf = pdf;
    }

    public void setReadTrackerManager(ReadTrackerManager readTrackerManager) {
        this.readTrackerManager = readTrackerManager;
    }

    public void setFromQ(String fromQuest) {
        this.fromQuest = fromQuest;
    }

    public String getFromQuest() {
        return this.fromQuest;
    }

    public void setQstep(int questStep) {
        this.questStep = questStep;
    }

    public int getQstep() {
        return questStep;
    }

    public void setHasUserReportedAbuse(boolean userReportedAbuse) {
        this.userReportedAbuse = userReportedAbuse;
    }

    public boolean isUserReportedAbuse() {
        return abuseManager.hasUserReportedAbuse(getJiveObject(), getUser());
    }

    /**
     * Convenience method for building a partial URL with communityID, threadID, messageID and reply specified.
     *
     * @return a partial URL with communityID, threadID, messageID and reply specified.
     */
    public String getPartialURL() {
        return getPartialURL(getThread(), getMessage(), getContainer());
    }

    /**
     * Convenience method for building a partial URL with communityID, threadID, messageID and reply specified.
     * Method is static to allow for code reuse by PostAction and ForumThreadAction
     *
     * @param thread
     * @param message
     * @param container
     *
     * @return a partial URL with communityID, threadID, messageID and reply specified.
     */
    public static String getPartialURL(ForumThread thread, ForumMessage message, JiveContainer container) {
        StringBuilder buf = new StringBuilder();
        long threadID = (thread == null) ? -1 : thread.getID();
        long messageID = (message == null) ? -1 : message.getID();

        buf.append("container=").append(container.getID()).append("&containerType=").append(container.getObjectType());
        if (threadID != -1L) {
            buf.append("&thread=").append(threadID);
        }
        if (messageID != -1L) {
            buf.append("&message=").append(messageID);
        }
        buf.append("&reply=true");
        return buf.toString();
    }

    private boolean hasDraft() {
        return null != draftHelper.getDraft(getUser(), JiveConstants.MESSAGE, JiveConstants.THREAD, message.getForumThreadID());
    }

    public boolean isMessageModerationOn() {
        return jiveObjectModerator.isModerationEnabled(getContainer(), JiveConstants.MESSAGE, getUser(),
                getVisibilityHelper().getVisibilityPolicy(getThread()) );
    }

    /**
     * Validates the community ID and either redirects to the SUCCESS, ERROR page if the id was not found or the login
     * page if the page user is not authorized to view the community.
     */
    public String execute() {
        if (message == null) {
            if (thread != null) {
                message = thread.getRootMessage();
                if (getUser() != null) {
                    readTrackerManager.markRead(getUser(), message);
                }
            }
            else {
                addActionError(getText("error.notfound.thread"));
                return NOTFOUND;
            }
        }
        // Adjust the value of the 'start' parameter if we're in flat mode and the
        // start param was not set. We do this because if given a messageID we'll want to
        // go to the right page in the thread. This branch can be disabled by setting
        // a Jive property which might be useful if you have a large number of messages
        // per thread.
        if (start <= 0 && !JiveGlobals.getJiveBooleanProperty("skin.default.disableMessageScan", false)) {
            // Quick check - if the message is the root of the thread, set start to 0
            if (message != null && message.getID() == thread.getRootMessage().getID()) {
                setStart(0);
            }
            else {
                int index = 0;
                int messageRange = JiveGlobals.getJiveIntProperty("skin.default.defaultMessagesPerPage", 15);

                String messageRangeProp = getMessageRangeProperty();
                if (messageRangeProp != null) {
                    // Update the value of messageRange from the user's profile (or guest's cookie):
                    try {
                        messageRange = Integer.parseInt(messageRangeProp);
                    }
                    catch (NumberFormatException nfe) {
                        log.debug(nfe);
                    }
                }

                // Determine the page this message is on by looking at all the messages with creation dates
                // before this message's creation date
                ThreadResultFilter filter = getResultFilter();
                filter.setNumResults(ThreadResultFilter.NULL_INT);
                filter.setCreationDateRangeMax(message.getCreationDate());

                // The subtraction of one is needed because our message will be included in
                // the count since the max creation date range is inclusive
                index = thread.getMessageCount(filter) - 1;

                // If the message was found, set the start index
                if (index > 0) {
                    setStart((index / messageRange) * messageRange);
                }
                setResultFilter(null); // so it's recreated
            }
        }

        if (isDraftEnabled() && hasDraft()) {
            setDraftExists(true);
        }

        if (isAbuseReported()) {
            addActionMessage(getText("abuse.reported.text"));
        }

        // check for notification parameter and set action message if exists
        if (request.getParameter("notification") != null) {
            addActionMessage(getText("send.noft.sent"));
        }

        if (isPdf && getThread().getMessageCount() <
                JiveGlobals.getJiveIntProperty("thread.allMessageActions.messageLimit", 200))
        {
            if (AnonymousUser.isAnonymousUser(getUserID())) {
                return UNAUTHENTICATED;
            }

            isDisplayFullThread = true;
            return "pdf";
        }

        /* TODO uncomment when implementing ajax calls to this action for inline message replies
        if (isInlineMsgPost())
            return SUCCESS_INLINE_MSG_POST;
        else
            return SUCCESS;*/
        parents = containerServiceImpl.getParentContainers(getContainer().getObjectType(), getContainer().getID());
       return SUCCESS;
    }

    private String getMessageRangeProperty() {
        if (AnonymousUser.isAnonymousUser(getUserID())) {
            return getGuestProperty("jiveMessageRange");
        }
        else {
            return getUser().getProperties().get("jiveMessageRange");
        }
    }

    public String buildMetaKeywords() {
        if (message != null) {
            return new MetaKeywordBuilder(tagActionUtil).object(message.getForumThread()).build();
        }
        else {
            return "";
        }
    }

    public boolean isPdf() {
        return isPdf;
    }
    // Methods to return Jive objects //

    /**
     * Returns the maximum number of messages that can be in a thread before creating a doc from the thread will result
     * in a warning.
     *
     * @return the maximum number of messages that can be in a thread before creating a doc from the thread will result
     *         in a warning.
     */
    public int getMaxMessagesForDocCreation() {
        return JiveGlobals.getJiveIntProperty("jive.thread.to.doc.max.messages", 100);
    }

    /**
     * Returns true if the page user is the thread author.
     *
     * @return true if the page user is the thread author.
     */
    public boolean isThreadAuthor() {
        return isAuthor(getThread());
    }

    /**
     * Returns true if the User can mark this thread as question.
     *
     * @return true if User has permission to mark thread as question, false if not.
     */
    public boolean isCanMarkAsQuestion() {
        return (getQuestion()==null &&
                canMarkAsQuestion(getThread(), getUser()));
    }

    /**
     * Returns true if the User can unmark this thread as question.
     *
     * @return true if User has permission to unmark thread as question, false if not.
     */
    public boolean isCanUnmarkAsQuestion() {
        return (getQuestion()!=null && getQuestion().isOpen() &&
                canUnmarkAsQuestion(getJiveContext().getQuestionManager(), getThread(), getUser()));
    }

    public boolean isCanViewThreaded() {
        return getThread().getMessageCount() < JiveGlobals.getJiveIntProperty("thread.allMessageActions.messageLimit", 100);
    }

    public boolean isThreaded() {
        return isCanViewThreaded() && isDesiredThreaded();
    }

    public boolean isDesiredThreaded() {
        threadMode = ForumThreadAction.isThreadedInit(threadMode, THREAD_MODE_PROPERTY_NAME, DEFAULT_THREAD_MODE,
            getUser(), JiveConstants.USER_DISCUSSION_THREAD_MODE);

        return ForumThreadAction.isThreaded(threadMode);
    }

    /**
     * Util methods to reuse code between ForumThreadAction and ContentActionSupport
     *
     */

    public static String isThreadedInit(@Nullable String threadMode, String defaultThreadModeKey, String defaultThreadMode,
            User user, String userThreadModePropKey) {
        String returnVal = threadMode;
        if (returnVal == null) {
            returnVal = JiveGlobals.getJiveProperty(defaultThreadModeKey, defaultThreadMode);
            // See if there is a user setting for the thread mode
            if (user != null && JiveGlobals.getJiveBooleanProperty("skin.default.usersChooseThreadMode", true)) {
                if (user.getProperties().get(userThreadModePropKey) != null) {
                    returnVal = user.getProperties().get(userThreadModePropKey);
                }
            }
        }

        return returnVal;
    }

    public static boolean isThreaded(String threadMode) {
        return "threaded".equals(threadMode);
    }

    public TreeWalker getUnproxiedTreeWalker() {
        if (isThreaded()) {
            ForumThread dbThread = null;
            try {
                dbThread = JiveApplication.getContext().getForumManager().getForumThread(thread.getID());
            }
            catch (ForumThreadNotFoundException e) {
                log.warn("Failed to load thread with ID: " + thread.getID(), e);
            }

            if (dbThread != null) {
                return dbThread.getTreeWalker();
            }
            else {
                return null;
            }
        }
        else {
            return new DummyTreeWalker();
        }
    }

    public TreeWalker getTreeWalker() {
        if (isThreaded()) {
            return getThread().getTreeWalker();
        }
        else {
            return new DummyTreeWalker();
        }
    }

    /**
     * Helper method to proxy users in cases where macros in the FTL are given un-proxied content
     *
     * @param user to proxy
     * @return proxied user
     */
    public User getProxiedUser(User user) {
        return ProxyUtils.proxyObject(UserProxy.class, user, authProvider.getAuthToken());
    }

    /**
     * This method is provided so we can get all children of a node, regardless of a childs status (hidden). To do this,
     * we need to use the DbTreeWalker, to bypass proxies and return all children. This is important for rendering the
     * full tree, so we don't skip over the child messages of a hidden (parent) message.
     *
     * If this method is used, it's important for the caller of this method to deal with permissions / message status' appropriately.
     *
     *
     * @param message the parent message
     * @return a jive iterator of all the child messages of the provided parent message, bypassing message status / proxies.
     */
    public Iterable<ForumMessage> getChildren(ForumMessage message) {
        if (message == null) {
            return Collections.emptyList();
        }

        Iterable<ForumMessage> children;
        TreeWalker treeWalker = getUnproxiedTreeWalker();
        if (treeWalker == null) {
            treeWalker = getTreeWalker();
            children = treeWalker.getChildren(message);
        }
        else {
            children = Iterables.transform(treeWalker.getChildren(message), new Function<ForumMessage, ForumMessage>() {
                @Override
                public ForumMessage apply(@Nullable ForumMessage message) {
                    // skip the authz check (happens in ftl), but still proxy the comment for user vis purposes
                    return ProxyUtils.proxyObject(ForumMessageProxy.class, message, authProvider.getAuthToken());
                }
            });
        }

        if (getUser() != null) {
            return new ReadTrackingIterable(children);
        } else {
            return children;
        }
    }

    /**
     * Returns an Iterator of messages in this thread. This depends on the user preference for messages per page so the
     * number of pages in a thread might vary.<p>
     * <p/>
     *
     * @return an Iterator of messages in this thread.
     */
    public Iterable<ForumMessage> getMessages() {
        Iterable<ForumMessage> messages;

        if (isThreaded()) {
            messages = getTreeWalker().getRecursiveMessages();
            messages = new ThreadedViewModerationIterator(messages);
        }
        else {
            ThreadResultFilter resultFilter = getResultFilter();
            if (isDisplayFullThread) {
                resultFilter.setNumResults(ResultFilter.NULL_INT);
            }

            messages = getThread().getMessages(resultFilter);
        }

        if (getUser() != null) {
            messages = new ReadTrackingIterable(messages);
        }

        return messages;
    }

    public boolean hasPermissionsToUploadImages() {
        if (message != null) {
            return AttachmentPermHelper.getCanCreateImageAttachment(message);
        }
        else if (thread != null) {
            return AttachmentPermHelper.getCanCreateImageAttachment(thread);
        }
        else {
            return AttachmentPermHelper.getCanCreateImageAttachment(getContainer(), JiveConstants.THREAD);
        }
    }

    // pageable

    /**
     * Returns the starting index of messages in the list of thread. Only relevant for the flat thread mode.
     *
     * @return returns the starting index in the list of messages.
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the thread index hint for this thread.
     *
     * @param start the starting index in the list of messages.
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Returns the index hint for this thread.
     *
     * @return the index hint for this thread.
     */
    public int getTstart() {
        if (tstart == null) {
            return 0;
        }
        else {
            return tstart;
        }
    }

    /**
     * Sets the index hint for this thread.
     *
     * @param tstart the index hint for this thread.
     */
    public void setTstart(int tstart) {
        this.tstart = tstart;
    }

    /**
     * Returns the index hint for the next thread in the list of threads.
     *
     * @return the index hint for the next thread in the list of threads.
     */
    public int getNextTstart() {
        return getTstart() + 1;
    }

    /**
     * Returns the index hint for th previous thread in the list of threads.
     *
     * @return the index hint for th previous thread in the list of threads.
     */
    public int getPrevTstart() {
        return getTstart() - 1;
    }

    /**
     * Returns the total number of messages in the thread. This method implements part of the Pageable interface.
     *
     * @return the total number of messages in the thread.
     */
    public int getTotalItemCount() {
        return thread.getMessageCount(getResultFilter());
    }

    public boolean isAbuseReported() {
        return abuseReported;
    }

    public void setAbuseReported(boolean abuseReported) {
        this.abuseReported = abuseReported;
    }

    /**
     * Returns the result filter used to create the list of messages in this thread. From a ResultFilter you can get the
     * starting index in the list, the number of results per page, moderation values, etc. See the JavaDoc for the
     * ResultFilter class for the full list of properties. <p>
     * <p/>
     * This method is only intended for a "flat" list of messages (non-threaded).
     *
     * @return the ResultFilter used to create the list of messages in this thread.
     */
    public ThreadResultFilter getResultFilter() {
        if (resultFilter == null) {
            resultFilter = ThreadResultFilter.createDefaultMessageFilter();
            resultFilter.setStartIndex(getStart());
            resultFilter.setNumResults(getNumResults());
            if (CommunityPermHelper.getCanModerateCommunity(getContainer())) {
                resultFilter.setStatus(JiveContentObject.Status.ABUSE_HIDDEN,
                    JiveContentObject.Status.ABUSE_VISIBLE, JiveContentObject.Status.PUBLISHED,
                        JiveContentObject.Status.PENDING_APPROVAL);
            }
            else {
                resultFilter.setStatus(JiveContentObject.Status.PUBLISHED);
            }
        }
        return resultFilter;
    }

    public int getNumResults() {
        return JiveGlobals.getJiveIntProperty("skin.default.defaultMessagesPerPage", 15);
    }

    /**
     * Sets the result filter used to create the list of messages in this thread. From a ResultFilter you can get the
     * starting index in the list, the number of results per page, moderation values, etc. See the JavaDoc for the
     * ResultFilter class for the full list of properties. <p>
     * <p/>
     * This method is only intended for a "flat" list of messages (non-threaded).
     *
     * @param filter the ResultFilter used to create the list of messages in this thread.
     */
    public void setResultFilter(ThreadResultFilter filter) {
        resultFilter = filter;
    }

    public Paginator getNewPaginator() {
        return new Paginator(this);
    }

    public JiveContainer getContainer() {
        return container;
    }

    public User getUserContainerOwner() {
        if ( !isUserContainer() ) {
            return null;
        }

        JiveContainer container = getContainer();
        if ( container instanceof UserContainer ) {
            UserContainer userContainer = (UserContainer) container;
            Long ownerId = userContainer.getUserID();
            try {
                return userManager.getUser(ownerId);
            }
            catch (UserNotFoundException e) {
                throw new IllegalStateException( "Could not find user container owner " + ownerId );
            }
        } else {
            throw new IllegalStateException( "Could not find user container owner for container ID " + container.getID() );
        }
    }

    /* TODO uncomment when implementing ajax calls to this action for inline message replies
    public boolean isInlineMsgPost() {
        return inlineMsgPost;
    }

    public void setInlineMsgPost(boolean inlineMsgPost) {
        this.inlineMsgPost = inlineMsgPost;
    }*/

    class ReadTrackingIterable implements Iterable<ForumMessage>, Iterator<ForumMessage> {

        private final Iterator<ForumMessage> messages;
        private final ReadTrackerManager readTrackerManager;

        private ReadTrackingIterable(Iterable<ForumMessage> messages) {
            this.messages = messages.iterator();
            readTrackerManager = getJiveContext().getReadTrackerManager();
        }

        public Iterator<ForumMessage> iterator() {
            return this;
        }

        public boolean hasNext() {
            return messages.hasNext();
        }

        public ForumMessage next() {
            ForumMessage nextMessage = messages.next();
            readTrackerManager.markRead(getUser(), nextMessage);
            return nextMessage;
        }

        public void remove() {
            messages.remove();
        }
    }

    public String getCanonicalURL() {
        return JiveResourceResolver.getJiveObjectURL(getThread(), true);
    }

    public String getNextURL() {
        Paginator paginator = getNewPaginator();
        if (paginator.getNextPage()) {
            return URLUtils.appendQueryParameters(JiveResourceResolver.getJiveObjectURL(getThread(), true),
                    Maps.newHashMap("start", Integer.toString(paginator.getNextPageStart()), "tstart", Integer.toString(getTstart())));
        }
        return null;
    }

    public String getPreviousURL() {
        Paginator paginator = getNewPaginator();
        if (paginator.getPreviousPage()) {
            if (paginator.getPreviousPageStart() == 0) {
                return JiveResourceResolver.getJiveObjectURL(getThread(), true);
            }
            else if (paginator.getPreviousPageStart() > 0) {
                return URLUtils.appendQueryParameters(JiveResourceResolver.getJiveObjectURL(getThread(), true),
                        Maps.newHashMap("start", Integer.toString(paginator.getPreviousPageStart()), "tstart", Integer.toString(getTstart())));

            }
        }
        return null;
    }


    /**
     * A convenience method to return the reply message subject.
     *
     * @return the reply message subject.
     */
    public String getReplySubject() {
        String replySubject = message.getUnfilteredSubject();
        if (!replySubject.startsWith("Re:")) {
            replySubject = "Re: " + replySubject;
        }
        return StringUtils.chop(replySubject, 250);
    }

    /**
     * Building an actual TreeWalker is an expensive operation and only needed when a thread is being viewed in
     * "threaded" mode.  However, the view layer has some macros that need some TreeWalker object even if they
     * don't use it.  So, this dummy implementation is provided for that case.
     */
    class DummyTreeWalker implements TreeWalker {

        public DummyTreeWalker() {
        }

        public ForumMessage getRoot() {
            return null;
        }

        public boolean hasParent(ForumMessage child) {
            return false;
        }

        public ForumMessage getParent(ForumMessage child) throws ForumMessageNotFoundException {
            return null;
        }

        public ForumMessage getChild(ForumMessage parent, int index) throws ForumMessageNotFoundException {
            return null;
        }

        public Iterable<ForumMessage> getChildren(ForumMessage parent) {
            return null;
        }

        public Iterable<ForumMessage> getRecursiveMessages() {
            return null;
        }

        public Iterable<ForumMessage> getRecursiveChildren(ForumMessage parent) {
            return null;
        }

        public int getMessageDepth(ForumMessage message) {
            return 0;
        }

        public int getChildCount(ForumMessage parent) {
            return 0;
        }

        public int getRecursiveChildCount(ForumMessage parent) {
            return 0;
        }

        public int getIndexOfChild(ForumMessage parent, ForumMessage child) {
            return 0;
        }

        public boolean isLeaf(ForumMessage node) {
            return false;
        }
    }

    public Map<String, String> getFromQueryData() {
        return Maps.newHashMap(
                "query", request.getParameter("q"),
                "referer", request.getHeader("referer"));
    }

    public Map<String, Object> getImpactStatsSoyModel() {
        return impactStatsActionHelper.getImpactStatsSoyModel(getThread(), getTimeZone(), getUser());
    }

    public boolean isShowImpactStats() {
        return extendedInvitationHelper.isNotExtendedAuthor(getThread(), getUser())
                && impactStatsActionHelper.isShowImpactStats(JiveConstants.THREAD, getThread(), getUser());
    }

    public void setImpactStatsActionHelper(ImpactStatsActionHelper impactStatsActionHelper) {
        this.impactStatsActionHelper = impactStatsActionHelper;
    }

    @Override
    public String getMetaInfo() {
        String body = "";

        if (getThreadMode().equalsIgnoreCase(THREAD_FLAT) && getStart() > 0) {
            ForumThread forumThread = getThread();

            // TODO: body should only be the root meesage's body when there is no pagination or we are on page 1
            body = forumThread.getRootMessage().getPlainBody();

            // body should only be the root meesage's body when there is no pagination or we are on page 1
            ThreadResultFilter resultFilter = ThreadResultFilter.createDefaultMessageFilter();
            resultFilter.setStartIndex(getStart());
            Iterable<ForumMessage> tempIterable = forumThread.getMessages(resultFilter);
            Iterator messageIterator = tempIterable.iterator();
            if (messageIterator.hasNext()) {
                body = ((ForumMessage)messageIterator.next()).getPlainBody();
            }

            final int MAX_DESCRIPTON_LENGTH = JiveGlobals.getJiveIntProperty("meta.description.max-length", 155);
            if (body.length() > MAX_DESCRIPTON_LENGTH) {
                body = StringUtils.chopAtWord(body, MAX_DESCRIPTON_LENGTH);
            }
            if (body.length() == 0)
            {
                body = forumThread.getPlainSubject();
            }
        }
        else {
            body = super.getMetaInfo();
        }

        return body;
    }

    public boolean getHasContentPlaceRelationships() {
        return hasContentPlaceRelationships(getThread());
    }

	public List<Place> getParents() {
		return parents;
	}

	public void setParents(List<Place> parents) {
		this.parents = parents;
	}

	public ContainerService getContainerServiceImpl() {
		return containerServiceImpl;
	}

	public void setContainerServiceImpl(ContainerService containerServiceImpl) {
		this.containerServiceImpl = containerServiceImpl;
	}
}
