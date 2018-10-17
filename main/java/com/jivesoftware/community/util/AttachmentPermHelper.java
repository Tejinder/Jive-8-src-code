/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.util;

import static com.jivesoftware.community.aaa.authz.EntitlementTypeProvider.EntitlementType.CREATE_ATTACHMENT;
import static com.jivesoftware.community.aaa.authz.EntitlementTypeProvider.EntitlementType.CREATE_IMAGE;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.grail.kantar.object.KantarAttachment;
import com.grail.kantar.objecttype.KantarAttachmentEntitlementCheckProvider;
import com.grail.object.GrailAttachment;
import com.grail.object.GrailEmailQueryAttachment;
import com.grail.objecttype.GrailAttachmentEntitlementCheckProvider;
import com.grail.objecttype.GrailEmailQueryAttachmentEntitlementCheckProvider;
import com.grail.osp.object.OSPAttachment;
import com.grail.osp.objecttype.OSPAttachmentEntitlementCheckProvider;
import com.grail.synchro.object.SynchroAttachment;
import com.grail.synchro.objecttype.SynchroAttachmentEntitlementCheckProvider;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.util.UserPermHelper;
import com.jivesoftware.community.AttachmentContentResource;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.Blog;
import com.jivesoftware.community.BlogPost;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentVersion;
import com.jivesoftware.community.ForumMessage;
import com.jivesoftware.community.ForumThread;
import com.jivesoftware.community.ImageManager;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContainer;
import com.jivesoftware.community.JiveContentObject;
import com.jivesoftware.community.JiveContext;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.UserContainer;
import com.jivesoftware.community.aaa.authz.Entitlement;
import com.jivesoftware.community.aaa.authz.EntitlementTypeProvider;
import com.jivesoftware.community.comments.Comment;
import com.jivesoftware.community.comments.CommentContentResource;
import com.jivesoftware.community.entitlements.Mask;
import com.jivesoftware.community.extension.objecttype.ExtensionObjectType;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * Permissions abstraction for attachments associated with content objects.
 */
public class AttachmentPermHelper extends BasePermHelper {

    private static final Logger log = LogManager.getLogger(AttachmentPermHelper.class);


    private AttachmentPermHelper() {
        //no-op
    }

    /**
     * Indicates if a content object can accept image attachments.
     * @param contentObject the content object which is being checked to determine if the authenticated user can create
     * attachments
     * @return true if the content object can accept image attachments.
     */
    public static boolean getCanCreateImageAttachment(JiveObject contentObject) {
        if (contentObject == null) {
            return false;
        }

        switch (contentObject.getObjectType()) {
            case (JiveConstants.DOCUMENT) :
            case (JiveConstants.MESSAGE) :
            case (JiveConstants.THREAD) :
            case (JiveConstants.COMMENT):
            case (JiveConstants.BLOGPOST) :
                try {
                    return getCanCreateImageAttachment(contentObject, getContainerForObject(contentObject));
                }
                catch (UnauthorizedException e) {
                    return false;
                }
            case (JiveConstants.AVATAR) :
                //fall through to lower proxy layer
                return true;
            default :
                return getCanCreateAttachmentInContentType(contentObject, true);
        }
    }

    private static JiveContainer getContainerForObject(JiveObject obj) {
        if (obj instanceof Document) {
            return ((Document)obj).getJiveContainer();
        } else if (obj instanceof ForumMessage) {
            ForumMessage message = ((ForumMessage)obj);
            if (message.getJiveContainer() != null) {
                return message.getJiveContainer();
            }
            if (message.getForumThread() != null) {
                return message.getForumThread().getJiveContainer();
            }
        } else if (obj instanceof ForumThread) {
            return ((ForumThread)obj).getJiveContainer();
        } else if (obj instanceof BlogPost) {
            return ((BlogPost)obj).getBlog();
        } else if (obj instanceof Comment) {
            return getContainerForObject(((Comment)obj).getCommentContentResource());
        }
        return null;
    }

    /**
     * Indicates if a content object can accept image attachments.
     * @param contentObject the content object which is being checked to determine if the authenticated user can create
     * attachments
     * @param container where the contentobject exists and the attachment will be created
     * @return true if the content object can accept image attachments.
     */
    public static boolean getCanCreateImageAttachment(JiveObject contentObject, JiveContainer container) {
        if (contentObject == null || container == null) {
            return false;
        }

        final JiveContext effectiveContext = getEffectiveContext();
        final ImageManager im = effectiveContext.getImageManager();

        if (!getImagesEnabled(im)) {
            return false;
        }

        if (JiveContainerPermHelper.isBannedFromPosting()) {
            return false;
        }

        switch (contentObject.getObjectType()) {
            case (JiveConstants.DOCUMENT) : {
                Document doc = (Document)contentObject;
                return getCanCreateImageInDocument(doc, container, im);
            }
            case (JiveConstants.MESSAGE) : {
                ForumMessage message = (ForumMessage)contentObject;
                ForumThread thread = message.getForumThread();

                return getCanCreateImageInThread(message, thread, container, im);
            }
            case (JiveConstants.THREAD) : {
                ForumThread thread = (ForumThread)contentObject;
                return getCanCreateImageInThread(null, thread, container, im);
            }
            case (JiveConstants.BLOGPOST) : {
                BlogPost post = (BlogPost)contentObject;
                return getCanCreateImageInBlogPost(post, post.getBlog(), im);
            }
            case (JiveConstants.AVATAR) : {
                //fall through to lower proxy layer
                return true;
            }
            case (JiveConstants.COMMENT): {
                Comment comment = (Comment)contentObject;
                return getCanCreateImageInComment(comment, container, im) ||
                        isEntitled(getEffectiveUser(), getEntitledContainer(container, JiveConstants.COMMENT),
                        JiveConstants.COMMENT, getImageMask());
            }
            default : {
                return getCanCreateAttachmentInContentType(contentObject, container, true);
            }
        }
    }

    private static boolean getCanCreateImageInComment(Comment comment, JiveContainer container, ImageManager im) {
        if (im.getMaxImagesPerObject() <= 0) {
            return false;
        }

        if (comment.getImageCount() >= im.getMaxImagesPerObject()) {
            return false;
        }

        CommentContentResource ccr = comment.getCommentContentResource();
        if (ccr.getObjectType() == JiveConstants.DOCUMENT || ccr.getObjectType() == JiveConstants.POLL) {
            if (container instanceof UserContainer) {
                UserContainer uc = (UserContainer)container;
                return (getEffectiveUser().getID() == uc.getUserID() || (ccr instanceof JiveContentObject && isEntitled(getEffectiveUser(), (JiveContentObject)ccr, Entitlement.WRITE)));
            } else {
                return isEntitled(getEffectiveUser(), getEntitledContainer(container, ccr.getObjectType()),
                            ccr.getObjectType(), getImageMask()) || (ccr instanceof JiveContentObject && isEntitled(getEffectiveUser(), (JiveContentObject) ccr, getImageMask() ) );
            }
        }else{
            return getCanCreateImageAttachment(ccr, container);
        }
    }

    public static boolean isAllowedToView(AttachmentContentResource attachmentContentResource) {
        if (attachmentContentResource != null) {

            switch (attachmentContentResource.getObjectType()) {
                case JiveConstants.MESSAGE:
                    return MessagePermHelper.getCanViewMessage((ForumMessage) attachmentContentResource);
                case JiveConstants.DOCUMENT:
                    return DocumentPermHelper.getCanViewDocument((Document) attachmentContentResource);
                case JiveConstants.BLOGPOST:
                    return BlogPermHelper.getCanViewBlogPost((BlogPost) attachmentContentResource);
                case JiveConstants.USER:
                    User user = (User) attachmentContentResource;
                    if (UserPermHelper.isUserAdmin(user)) {
                        return true;
                    }
                    break;
                default:
                    //if we don't yet know what the attachment is being attached to, it is being attached to new content in the
                    //process of being created
                    return true;
            }
        }

        //if we don't yet know what the attachment is being attached to, it is being attached to new content in the
        //process of being created
        return true;
    }

    public static boolean isAllowedToEdit(AttachmentContentResource attachmentContentResource, long userID) {
        if (attachmentContentResource != null) {

            switch (attachmentContentResource.getObjectType()) {
                case JiveConstants.MESSAGE:
                    return canCreateMessageAttachment((ForumMessage) attachmentContentResource);
                case JiveConstants.DOCUMENT:
                    return canCreateDocumentAttachment((Document) attachmentContentResource);
                case JiveConstants.BLOGPOST:
                    return canCreateBlogPostAttachment(userID, (BlogPost) attachmentContentResource);
                case JiveConstants.USER:
                    User user = (User) attachmentContentResource;
                    if (UserPermHelper.isUserAdmin(user)) {
                        return true;
                    }
                    break;
                default:
                    //if we don't yet know what the attachment is being attached to, it is being attached to new content in the
                    //process of being created
                    return true;
            }
        }

        //if we don't yet know what the attachment is being attached to, it is being attached to new content in the
        //process of being created
        return true;
    }

    protected static boolean canCreateMessageAttachment(ForumMessage message) {
        return AttachmentPermHelper.getCanCreateAttachment(message);
    }

    protected static boolean canCreateDocumentAttachment(Document document) {
        return AttachmentPermHelper.getCanCreateAttachment(document);
    }

    protected static boolean canCreateBlogPostAttachment(long userID, BlogPost post) {
        if (BasePermHelper.isSystemAdmin() || post.getUserID() == userID) {
            return true;
        } else {
            return getCanCreateAttachment(post);
        }
    }

    /**
     * Returns true if the effective user can create an attachment on the supplied object.
     *
     * @param contentObject the object to test
     * @return true if the effective user can create an attachment on the supplied object.
     */
    public static boolean getCanCreateAttachment(JiveObject contentObject) {
        return getCanCreateAttachment(contentObject, true);
    }

    /**
     * Returns true if the effective user can create an attachment on the supplied object.
     *
     * @param contentObject the object to test
     * @param ignoreMax set to true to avoid configured max attachments
     * @return true if the effective user can create an attachment on the supplied object.
     */
    public static boolean getCanCreateAttachment(JiveObject contentObject, boolean ignoreMax) {
        if (contentObject == null) {
            return false;
        }

        //use the container associated with the object
        switch (contentObject.getObjectType()) {
            case (JiveConstants.DOCUMENT): {
                return getCanCreateAttachment(contentObject, ((Document)contentObject).getJiveContainer(), ignoreMax);
            }
            case (JiveConstants.DOCUMENT_VERSION): {
                Document document = ((DocumentVersion) contentObject).getDocument();
                return getCanCreateAttachment(document, document.getJiveContainer(), true);
            }
            case (JiveConstants.MESSAGE) : {
                ForumMessage message = (ForumMessage)contentObject;
                ForumThread thread = message.getForumThread();
                JiveContainer container = JiveApplication.getContext().getJiveContainerManager().getJiveContainerFor(thread);
                if (container == null && thread != null) {
                    container = thread.getJiveContainer();
                }

                return getCanCreateAttachment(contentObject, container, ignoreMax);
            }
            case (JiveConstants.THREAD) : {
                JiveContainer container = JiveApplication.getContext().getJiveContainerManager().getJiveContainerFor(contentObject);
                return getCanCreateAttachment(contentObject, container, ignoreMax);
            }
            case (JiveConstants.BLOGPOST) : {
                Blog blog = ((BlogPost)contentObject).getBlog();
                return getCanCreateAttachment(contentObject, blog.getJiveContainer(), ignoreMax) && BlogPermHelper.getCanEditBlogPost((BlogPost)contentObject);
            }
            case (JiveConstants.AVATAR) : {
                //fall through to lower proxy layer
                return true;
            }
            case (JiveConstants.VIDEO) : {
                return true;
            }
            case (JiveConstants.USER) : {
                //users can save attachments to themselves
                return getEffectiveUser().getID() == contentObject.getID();
            }
            default : {
                if (contentObject.getObjectType() == ExtensionObjectType.EXTENSION_TYPE_ID) {
                    return true;
                }
                if(contentObject instanceof SynchroAttachment) {
                    return getCanCreateSychroAttachment((SynchroAttachment)contentObject);
                } else if(contentObject instanceof KantarAttachment) {
                    return getCanCreateKantarAttachment((KantarAttachment) contentObject);
                } else if(contentObject instanceof GrailEmailQueryAttachment) {
                    return getCanCreateGrailEmailQueryAttachment((GrailEmailQueryAttachment) contentObject);
                } else if(contentObject instanceof GrailAttachment) {
                    return getCanCreateGrailAttachment((GrailAttachment) contentObject);
                } else if(contentObject instanceof OSPAttachment) {
                    return getCanCreateOSPAttachment((OSPAttachment) contentObject);
                }
                else {
                    return getCanCreateAttachmentInContentType(contentObject, false);
                }

               // return getCanCreateAttachmentInContentType(contentObject, false);
            }
        }
    }

    /**
     * Checks attachment permission for a content object including the
     * max attachment limitation.
     *
     * @param contentObject the content object which is being checked
     * @param container the container which is being checked for the given permission
     * @return true if the user can create attachments in the given container on the given content object.
     */
    public static boolean getCanCreateAttachment(JiveObject contentObject, JiveContainer container) {
        return getCanCreateAttachment(contentObject, container, false);
    }

    /**
     * Indicates of a content object can accept an attachment.
     * This method explicitly requires the container in which the
     * content object will live due to the fact that certain
     * object workfolows in the system have not attached the
     * container by the time the permissions checks are needed.
     *
     * The ignore max param indicates if the check should ignore max attachments
     * which is usually the case when checking for delete.
     *
     * @param contentObject the content type which is being checked to determine if it supports attachments
     * @param container the container which is checked to determine if it supports attachments.
     * @param ignoreMax if the maximum number of attachments should be ignored in this check.
     * @return true if the container can accept an attachment for the given content object in the given container.
     */
    public static boolean getCanCreateAttachment(JiveObject contentObject, JiveContainer container, boolean ignoreMax) {
        if (contentObject == null)  {
            return false;
        }

        final JiveContext effectiveContext = getEffectiveContext();
        final AttachmentManager am = effectiveContext.getAttachmentManager();

        if (!getAttachmentsEnabled(am)) {
            return false;
        }

        if (JiveContainerPermHelper.isBannedFromPosting()) {
            return false;
        }

        switch (contentObject.getObjectType()) {
            case (JiveConstants.DOCUMENT): {
                Document doc = (Document) contentObject;
                return getCanCreateAttachmentInDocument(doc, container, ignoreMax, am);
            }
            case (JiveConstants.MESSAGE) : {
                ForumMessage message = (ForumMessage)contentObject;
                return getCanCreateAttachmentInThread(message, message.getForumThread(), container, ignoreMax, am);
            }
            case (JiveConstants.THREAD) : {
                ForumThread thread = (ForumThread)contentObject;
                return getCanCreateAttachmentInThread(null, thread, container, ignoreMax, am);
            }
            case (JiveConstants.BLOGPOST) : {
                BlogPost blogPost = (BlogPost)contentObject;
                return getCanCreateAttachmentInBlogPost(blogPost, container, ignoreMax, am);
            }
            default: {
               // return getCanCreateAttachmentInContentType(contentObject, container, false);
            	 if(contentObject instanceof SynchroAttachment) {
                     return getCanCreateSychroAttachment((SynchroAttachment)contentObject);
                 } else if(contentObject instanceof KantarAttachment) {
                     return getCanCreateKantarAttachment((KantarAttachment) contentObject);
                 } else if(contentObject instanceof GrailEmailQueryAttachment) {
                     return getCanCreateGrailEmailQueryAttachment((GrailEmailQueryAttachment) contentObject);
                 } else if(contentObject instanceof GrailAttachment) {
                     return getCanCreateGrailAttachment((GrailAttachment) contentObject);
                 } else {
                     return getCanCreateAttachmentInContentType(contentObject, container, false);
                 }
            }
        }
    }

    public static boolean getCanCreateSychroAttachment(SynchroAttachment contentObject) {
        SynchroAttachmentEntitlementCheckProvider entitlementCheckProvider = JiveApplication.getContext().getSpringBean("synchroAttachmentEntitlementCheckProvider");
        if(entitlementCheckProvider != null && entitlementCheckProvider.isUserEntitled(contentObject, getEffectiveUser())) {
            return true;
        }
        return false;
    }

    public static boolean getCanCreateKantarAttachment(KantarAttachment contentObject) {
        KantarAttachmentEntitlementCheckProvider entitlementCheckProvider = JiveApplication.getContext().getSpringBean("kantarAttachmentEntitlementCheckProvider");
        if(entitlementCheckProvider != null && entitlementCheckProvider.isUserEntitled(contentObject, getEffectiveUser())) {
            return true;
        }
        return false;
    }

    public static boolean getCanCreateGrailEmailQueryAttachment(GrailEmailQueryAttachment contentObject) {
        GrailEmailQueryAttachmentEntitlementCheckProvider entitlementCheckProvider = JiveApplication.getContext().getSpringBean("grailEmailQueryAttachmentEntitlementCheckProvider");
        if(entitlementCheckProvider != null && entitlementCheckProvider.isUserEntitled(contentObject, getEffectiveUser())) {
            return true;
        }
        return false;
    }

    public static boolean getCanCreateGrailAttachment(GrailAttachment contentObject) {
        GrailAttachmentEntitlementCheckProvider entitlementCheckProvider = JiveApplication.getContext().getSpringBean("grailAttachmentEntitlementCheckProvider");
        if(entitlementCheckProvider != null && entitlementCheckProvider.isUserEntitled(contentObject, getEffectiveUser())) {
            return true;
        }
        return false;
    }
    
    public static boolean getCanCreateOSPAttachment(OSPAttachment contentObject) {
        OSPAttachmentEntitlementCheckProvider entitlementCheckProvider = JiveApplication.getContext().getSpringBean("ospAttachmentEntitlementCheckProvider");
        if(entitlementCheckProvider != null && entitlementCheckProvider.isUserEntitled(contentObject, getEffectiveUser())) {
            return true;
        }
        return false;
    }
    
    /**
     * Returns true if a particular container can accept image attachments.
     *
     * @param container the container which is being checked to determine if it can handle attachments.
     * @param contentTypeID the content type which is being checked.
     * @return true if a particular container can accept image attachments.
     */
    public static boolean getCanCreateImageAttachment(JiveContainer container, int contentTypeID) {
        if (container == null) {
            return false;
        }

        if (!getAttachmentsEnabled()) {
            return false;
        }

        if (JiveContainerPermHelper.isBannedFromPosting()) {
            return false;
        }

        if (!JiveGlobals.getJiveBooleanProperty("image.enabled", true)) {
            return false;
        }


        return isEntitled(getEffectiveUser(), getEntitledContainer(container, contentTypeID),
                contentTypeID, getImageMask());
    }

    /**
     * Returns true if a particular container can accept attachments.
     *
     * @param container the container which is being checked to determine if it can handle attachments.
     * @param contentTypeID the content type which is being checked.
     * @return true if a particular container can accept attachments.
     */
    public static boolean getCanCreateAttachment(JiveContainer container, int contentTypeID) {
        if (container == null) {
            return false;
        }

        if (!getAttachmentsEnabled()) {
            return false;
        }

        if (JiveContainerPermHelper.isBannedFromPosting()) {
            return false;
        }

        return isEntitled(getEffectiveUser(), getEntitledContainer(container, contentTypeID),
                contentTypeID, getAttachMask());
    }


    protected static boolean getCanCreateAttachmentInBlogPost(BlogPost blogPost, JiveContainer container,
            boolean ignoreMax, AttachmentManager am) {

        if (am.getMaxAttachmentsPerBlogPost() <= 0) {
                    return false;
        }

        if (am.getMaxAttachmentsPerBlogPost() <= 0) {
            return false;
        }


        if (!ignoreMax && (am.getAttachmentCount(blogPost) >= am.getMaxAttachmentsPerBlogPost())) {
            return false;
        }

        return isEntitled(getEffectiveUser(), getEntitledContainer(container, JiveConstants.BLOGPOST),
                    JiveConstants.BLOGPOST, getAttachMask());
    }


    protected static boolean getCanCreateAttachmentInThread(ForumMessage message, ForumThread thread, JiveContainer container,
            boolean ignoreMax, AttachmentManager am) {

        if (am.getMaxAttachmentsPerMessage() <= 0) {
            return false;
        }

        if (!ignoreMax && message != null)  {
            if (am.getAttachmentCount(message) >= am.getMaxAttachmentsPerMessage()) {
                return false;
            }
        }

        if (container instanceof UserContainer) {
            UserContainer uc = (UserContainer)container;
            return (getEffectiveUser().getID() == uc.getUserID() || (thread != null &&
                    isEntitled(getEffectiveUser(), thread, Entitlement.WRITE)));
        }
        else {
            return isEntitled(getEffectiveUser(), getEntitledContainer(container, JiveConstants.THREAD),
                        JiveConstants.THREAD, getAttachMask()) || isEntitled(getEffectiveUser(), thread, getAttachMask());
        }
    }


    protected static boolean getCanCreateAttachmentInDocument(Document doc, JiveContainer container,
            boolean ignoreMax, AttachmentManager am)
    {
        if (am.getMaxAttachmentsPerDocument() <= 0) {
            return false;
        }

        if (!ignoreMax && am.getAttachmentCount(doc) >= am.getMaxAttachmentsPerDocument()) {
            return false;
        }

        if (container instanceof UserContainer) {
            UserContainer uc = (UserContainer)container;
            return (getEffectiveUser().getID() == uc.getUserID() || isEntitled(getEffectiveUser(), doc, Entitlement.WRITE));
        }

        if (!isEntitled(getEffectiveUser(), getEntitledContainer(container, JiveConstants.DOCUMENT),
                JiveConstants.DOCUMENT, getAttachMask()))
        {
            return isEntitled(getEffectiveUser(), doc, getAttachMask());
        }

        return DocumentPermHelper.isAuthor(doc, getEffectiveUser()) || isContainerAdminOrModerator(container);
    }


    protected static boolean getCanCreateImageInBlogPost(BlogPost post, JiveContainer container, ImageManager im) {
        if (im.getMaxImagesPerObject() <= 0) {
            return false;
        }

        if (post.getImageCount() >= im.getMaxImagesPerObject()) {
            return false;
        }

        return isEntitled(getEffectiveUser(), getEntitledContainer(container, JiveConstants.BLOGPOST),
                    JiveConstants.BLOGPOST, getImageMask());
    }


    protected static boolean getCanCreateImageInThread(ForumMessage message, ForumThread thread, JiveContainer container, ImageManager im) {
        if (im.getMaxImagesPerObject() <= 0) {
            return false;
        }

        if (message != null && message.getImageCount() >= im.getMaxImagesPerObject()) {
            return false;
        }

        return isEntitled(getEffectiveUser(), getEntitledContainer(container, JiveConstants.THREAD),
                    JiveConstants.THREAD, getImageMask()) || isEntitled(getEffectiveUser(), thread, getImageMask());
    }


    protected static boolean getCanCreateImageInDocument(Document doc, JiveContainer container, ImageManager im) {
        if (im.getMaxImagesPerObject() <= 0) {
            return false;
        }

        if (doc.getImageCount() >= im.getMaxImagesPerObject()) {
            return false;
        }

        if (container instanceof UserContainer) {
            UserContainer uc = (UserContainer)container;
            return (getEffectiveUser().getID() == uc.getUserID()|| isEntitled(getEffectiveUser(), doc, getImageMask()));
        }

        EntitlementTypeProvider typeProvider = JiveApplication.getEffectiveContext().getEntitlementTypeProvider();
        if (typeProvider.isUserEntitled(doc, CREATE_IMAGE)) {
            //first attempt to let the object type framework give us the entitlement check logic
            return true;
        }

        if (!isEntitled(getEffectiveUser(), getEntitledContainer(container, JiveConstants.DOCUMENT),
                JiveConstants.DOCUMENT, getImageMask())) {
            return false;
        }

        return DocumentPermHelper.isAuthor(doc, getEffectiveUser());
    }

    protected static boolean getCanCreateAttachmentInContentType(JiveObject contentObject, boolean imageAttachment) {

        JiveContainer container = null;

        if (contentObject instanceof JiveContentObject) {
            try {
                JiveContentObject jco = (JiveContentObject) contentObject;
                container = JiveApplication.getEffectiveContext().getJiveContainerManager()
                        .getJiveContainer(jco.getContainerType(), jco.getContainerID());
            }
            catch (NotFoundException e) {
                log.error("Couldn't load container for perm check", e);
            }
        }

        return getCanCreateAttachmentInContentType(contentObject, container, imageAttachment);
    }

    protected static boolean getCanCreateAttachmentInContentType(JiveObject contentObject, JiveContainer container, boolean imageAttachment) {
        //TODO add a generic way so specify a max number of attachments per content type

        EntitlementTypeProvider typeProvider = JiveApplication.getEffectiveContext().getEntitlementTypeProvider();

        if (typeProvider.isUserEntitled(contentObject, imageAttachment? CREATE_IMAGE : CREATE_ATTACHMENT)) {
            //first attempt to let the object type framework give us the entitlement check logic
            return true;
        }

        //if the entitlementprovider deems the user is not entitled, we still will directly check the
        //container entitlement which can supersede content object specific entitlement

        // first check is for the temp AttachmentProvider.AttachmentContentResource holder which should have had it's own type
        return (container == null && contentObject.getObjectType() == JiveConstants.ATTACHMENT) ||(container != null && isEntitled(getEffectiveUser(), getEntitledContainer(container, contentObject.getObjectType()),
                    contentObject.getObjectType(), imageAttachment? getImageMask() : getAttachMask()));
    }

    /**
     * Returns true if attachments are enabled in the application.
     *
     * @return true if attachments are enabled in the application.
     */
    public static boolean getAttachmentsEnabled() {
        return getAttachmentsEnabled(getEffectiveContext().getAttachmentManager());
    }

    public static boolean getAttachmentsEnabled(AttachmentManager am) {
        return am.isAttachmentsEnabled() && am.getMaxAttachmentSize() > 0;
    }

    public static boolean getImagesEnabled() {
        return getImagesEnabled(getEffectiveContext().getImageManager());
    }

    public static boolean getImagesEnabled(ImageManager im) {
        return im.isImagesEnabled() && im.getMaxImagesPerObject() > 0;
    }

    private static Mask getImageMask() {
        return getEntitlementCheckHelper().getImageMask();
    }

    private static Mask getAttachMask() {
        return getEntitlementCheckHelper().getAttachMask();
    }

}
