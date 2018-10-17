/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.action;

import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.util.Either;
import com.jivesoftware.base.util.Option;
import com.jivesoftware.community.ApprovalStatus;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentVersion;
import com.jivesoftware.community.RejectedException;
import com.jivesoftware.community.aaa.AnonymousUser;
import com.jivesoftware.community.action.uiextensions.DocumentActionUIExtension;
import com.jivesoftware.community.action.util.ActionUtils;
import com.jivesoftware.community.action.util.RenderUtils;
import com.jivesoftware.community.app.App;
import com.jivesoftware.community.app.AppManager;
import com.jivesoftware.community.app.InstallTypeEnum;
import com.jivesoftware.community.app.view.AppLaunchContextView;
import com.jivesoftware.community.app.view.DashboardAppInstanceView;
import com.jivesoftware.community.app.view.helper.impl.DashboardViewHelper;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.comments.Comment;
import com.jivesoftware.community.comments.CommentDelegator;
import com.jivesoftware.community.eae.tile.action.AppErrorView;
import com.jivesoftware.community.integration.storage.file.ExStorageFile;
import com.jivesoftware.community.integration.storage.file.ExStorageFileManager;
import com.jivesoftware.community.integration.storage.fileVersion.ExStorageFileVersion;
import com.jivesoftware.community.integration.storage.fileVersion.ExStorageFileVersionManager;
import com.jivesoftware.community.integration.storage.ui.ExStorageInfoHelper;
import com.jivesoftware.community.license.annotations.RequireModule;
import com.jivesoftware.community.places.rest.ContainerService;
import com.jivesoftware.community.places.rest.Place;
import com.jivesoftware.community.renderer.impl.v2.TinyMCESupport;
import com.jivesoftware.community.util.DocumentPermHelper;
import com.jivesoftware.community.web.util.JSONUtil;
import com.jivesoftware.conversion.executor.ConversionError;
import com.jivesoftware.conversion.model.ConversionStatus;
import com.jivesoftware.conversion.struts.ConversionViewerAction;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;


import java.util.ArrayList;
import java.util.List;
import com.grail.synchro.SynchroGlobal;

import static com.jivesoftware.community.web.util.JSONUtil.safePut;

/**
 * Used to apply a document against the presenter for its document type.
 */
@RequireModule(product = "wiki")
public class DocumentAction extends DocumentActionSupport implements ConversionViewerAction, HasMetaKeywords, HasBadgeableAvatars {
    
    public static final String APPROVED = "approved";
    public static final String REJECTED = "rejected";
    public static final String REJECTED_ERROR = "rejected-error";
    public static final String DELETED = "doc-deleted";

    // constants for Grail
    public static final String GRAIL_COUNTRY = "grail.country";
    public static final String GRAIL_BRAND = "grail.brand";
    public static final String GRAIL_METHODOLOGY = "grail.methodology";
    public static final String GRAIL_MONTH = "grail.month";
    public static final String GRAIL_YEAR = "grail.year";
    public static final String GRAIL_PERIOD_LONG = "grail.period.long";
    
    private static final long serialVersionUID = 2453969902582569577L;

    public static final String SESSION_SEARCH_URL = "jive.search.url";

    // rejection notes are limited to 255 characters
    protected static final int MAX_REJECTION_NOTE_LENGTH = 255;

    public static final String DEFAULT_PREVIEW_APP_VIEW = "canvas";

    private String rejectionNotes = "";

    private boolean approved;
    private boolean rejected;
    private boolean abuseReported;
    private boolean userReportedAbuse;

    private boolean uploadSuccess;

    private String fromQuest;
    private int questStep;
    private Collection<DocumentActionUIExtension> documentActionUIExtensions = Collections.emptyList();

    private static Logger log = LogManager.getLogger(DocumentAction.class);

    protected AttachmentHelper attachmentHelper;
    protected ExStorageInfoHelper exStorageInfoHelper;

    private DashboardAppInstanceView appInstanceView;
    private AppManager appManager;
    private AppErrorView appErrorView;
    private AppLaunchContextView launchContext;
    private DashboardViewHelper dashboardViewHelper;
    private ExStorageFileManager exStorageFileManager;
    private ExStorageFileVersionManager exStorageFileVersionManager;
    
    // Begin for Grail
    private List<String> country = new ArrayList<String>();
    private List<String> brand = new ArrayList<String>();
    private List<String> methodology = new ArrayList<String>();
    private List<String> month = new ArrayList<String>();
    private List<String> year = new ArrayList<String>();
    // This will contain the return Url for the appropriate Stage from Manage Version page 
    private String backUrl;
    private Long projectID;
    private Integer activeTab = 1;
    private String versionView;
    private List<Place> parents;

    
    private ContainerService containerServiceImpl;
    
    
  
    
    
    public List<String> getBrand() {
        return brand;
    }

    public void setBrand(List<String> brand) {
        this.brand = brand;
    }

    public List<String> getCountry() {
        return country;
    }

    public void setCountry(List<String> country) {
        this.country = country;
    }

    public List<String> getMonth() {
        return month;
    }

    public void setMonth(List<String> month) {
        this.month = month;
    }

    public List<String> getYear() {
        return year;
    }

    public void setYear(List<String> year) {
        this.year = year;
    }

    public List<String> getMethodology() {
        return methodology;
    }

    public void setMethodology(List<String> methodology) {
        this.methodology = methodology;
    }
    // End for Informatica
    

    public String getBody() {
        return RenderUtils.renderToHtml(getDocument(), getGlobalRenderManager());
    }
    public String getMacroJavaScript() {
        return TinyMCESupport.getMacroJavaScript(getGlobalRenderManager(), getDocument(), getContainer());
    }
    
    /**
     * Returns whether this document has expired or not.
     *
     * @return true if this document has expired, false otherwise
     */
    public boolean isExpired() {
        return DocumentPermHelper.isExpired(getDocument());
    }

    public boolean isAllowedToReadComments() {
        return DocumentPermHelper.isAllowedToReadComments(getDocument());
    }

    public boolean isApprovalRequired() {
        return DocumentPermHelper.isApprovalRequired(getDocument()) && !isUserContainer();
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public int getQstep() {
        return questStep;
    }

    public void setQstep(int questStep) {
        this.questStep = questStep;
    }

    public String getFromQuest() {
        return fromQuest;
    }

    public void setFromQ(String fromQuest) {
        this.fromQuest = fromQuest;
    }

    /**
     * Returns true if the page user is an administrator or the author of the document.
     *
     * @return true if the page user is an administrator or the author of the document.
     */
    public boolean isAllowedToCreateComments() {
        return DocumentPermHelper.isAllowedToCreateComments(getDocument());
    }

    public boolean previousVersionExists() {
        return ActionUtils.previousVersionExists(getDocument(), getUser());
    }

    public boolean isLatestVersion() {
        DocumentVersion publishedVersion = getDocument().getVersionManager().getPublishedDocumentVersion();
        return publishedVersion == null || getDocument().getDocumentVersion().getVersionNumber() >= publishedVersion
                .getVersionNumber();
    }

    public int getVersion() {
        return getDocument().getDocumentVersion().getVersionNumber();
    }

    public String getRejectionNotes() {
        return rejectionNotes;
    }

    public void setRejectionNotes(String rejectionNotes) {
        this.rejectionNotes = rejectionNotes;
    }

    public String execute() {
        if (AnonymousUser.isAnonymousUser(getUserID()) && getDocument() == null) {
            //assuming session timout or access attempt to doc which requires login
            return UNAUTHORIZED;
        }

        //CS-8638 fix only the owner or moderator/admin can see a document awaiting moderation.
        if (!DocumentPermHelper.getCanViewDocument(getDocument()))
        {
            return UNAUTHORIZED;
        }

        final Document doc = getDocument();
        if (doc == null) {
            addActionError(getText("error.notfound.document"));
            return ERROR;
        }

        if (isApproved()) {
            addActionMessage(getText("doc.state.youApprPublicatn.text"));
        }
        else if (isRejected()) {
            addActionMessage(getText("doc.state.youRjctPublicatn.text"));
        }

        if (isAbuseReported()) {
            addActionMessage(getText("abuse.reported.text"));
        }

        // check for notification parameter and set action message if exists
        if (request.getParameter("notification") != null) {
            addActionMessage(getText("send.noft.sent"));
        }
        
        /*if (request.getParameter("missingCollaborators") != null) {
            missingCollaborators = true;
        }*/
        // check for file upload confirmation param and set action message if it exists
        if (request.getParameter("uploadSuccess") != null) {
            uploadSuccess = true;
        }

        //non-viewable versions should not be viewable unless you have edit permission
        if (!versionIsViewable(getVersion()) ) {
            if (!isAllowedToEdit()) {
                return UNAUTHORIZED;
            }
        }
        //else 

        if (isDocVerseEnabled()) {
            //if doc is eligible for DV, but has not gone through workflow, convert it!
            boolean isConvertable = getConversionManager().isConvertable(doc);
         
            if (isConvertable && isAllowedToReconvert()) {

               // TODO - converting zipped docs?
               // BinaryBody binaryBody = doc.getBinaryBody();
               //                 String name = binaryBody.getName();

               /*   if (attachmentHelper.isJiveZippedFile(new EntityDescriptor(binaryBody))) {
                    // special case jive zipped - CS-13332. These files will be stored zipped so
                    // we must compare the original unzipped files is the same extension
                    name = attachmentHelper.getUnzippedFilename(binaryBody);
                } */

                    log.info("Starting document conversion for document " + doc);
                    this.getConversionManager().convert(doc);
                
            }
            
        }

        if (isExternalFile(doc)) {
            setupPreviewApp(doc);
        }

        // check to see if the extended property for grail document exists and set them for display in the edit form
        if(getDocument().getProperties().get(GRAIL_COUNTRY) != null) {
            if(getDocument().getProperties().get(GRAIL_COUNTRY).contains("NA")) {
                country.add("NA");
            }else {
                country = convertCSVToList(getDocument().getProperties().get(GRAIL_COUNTRY));
            }
        }else {
            country.add("NA");
        }

        if(getDocument().getProperties().get(GRAIL_BRAND) != null) {
            if(getDocument().getProperties().get(GRAIL_BRAND).contains("NA")) {
                brand.add("NA");
            }else {
                brand = convertCSVToList(getDocument().getProperties().get(GRAIL_BRAND));
            }
        }else {
            brand.add("NA");
        }

        if(getDocument().getProperties().get(GRAIL_METHODOLOGY) != null) {
            if(getDocument().getProperties().get(GRAIL_METHODOLOGY).contains("NA")) {
                methodology.add("NA");
            }else {
                methodology = convertCSVToList(getDocument().getProperties().get(GRAIL_METHODOLOGY));
            }
        }else {
            methodology.add("NA");
        }

        if(getDocument().getProperties().get(GRAIL_MONTH) != null) {
            if(getDocument().getProperties().get(GRAIL_MONTH).contains("NA")) {
                month.add("NA");
            }else {
                month = convertCSVToList(getDocument().getProperties().get(GRAIL_MONTH));
                // Commented as part of Jive 8 Upgradation
               // month = getMonth(month);
            }
        }else {
            month.add("NA");
        }

        if(getDocument().getProperties().get(GRAIL_YEAR) != null) {
            if(getDocument().getProperties().get(GRAIL_YEAR).contains("NA")) {
                year.add("NA");
            }else {
                year = convertCSVToList(getDocument().getProperties().get(GRAIL_YEAR));
            }
        }else {
            year.add("NA");
        }
        if(projectID!=null && activeTab!=null)
        {
        	if(versionView!=null)
        	{
        		backUrl = "/synchro/"+projectID+"/"+activeTab+"/docs/"+getDocument().getDocumentID()+"/version";
        	}
        	else
        	{
	        	String projectStage = SynchroGlobal.getProjectActivityMethod().get(activeTab);
	        	backUrl = "/synchro/activity!"+projectStage+".jspa?projectID="+projectID;
        	}
        }
        parents = containerServiceImpl.getParentContainers(getContainer().getObjectType(), getContainer().getID());
        
        
        
        return SUCCESS;
    }
    
    /**
     * Returns the month corresponding to the given month value in int
     * @param month
     * @return
     */
    private List<String> getMonth(List<String> month) {
        List<String> monthValue = new ArrayList<String>();
        if(month.get(0).equals("0")) {
            monthValue.add("January");
        }else if(month.get(0).equals("1")){
            monthValue.add("February");
        }else if(month.get(0).equals("2")){
            monthValue.add("March");
        }else if(month.get(0).equals("3")){
            monthValue.add("April");
        }else if(month.get(0).equals("4")){
            monthValue.add("May");
        }else if(month.get(0).equals("5")){
            monthValue.add("June");
        }else if(month.get(0).equals("6")){
            monthValue.add("July");
        }else if(month.get(0).equals("7")){
            monthValue.add("August");
        }else if(month.get(0).equals("8")){
            monthValue.add("September");
        }else if(month.get(0).equals("9")){
            monthValue.add("October");
        }else if(month.get(0).equals("10")){
            monthValue.add("November");
        }else if(month.get(0).equals("11")){
            monthValue.add("December");
        }else {
            monthValue.add("NA");
        }
        return monthValue;
    }

    /**
     * Convert a CSV string values to a List<String>
     * @param csvString
     * @return List<String>
     */
    private List<String> convertCSVToList(String csvString) {

        String[] stripComma = csvString.split(",");
        List<String> result = new ArrayList<String>();
        for(int i=0; i<stripComma.length; i++) {
            result.add(stripComma[i]);
        }
        return result;
    }
    @Override
    public boolean isUnsupported() {
        return getConversionManager().isDisabled(getDocument()); 
    }
    
    @Override
    public boolean isExceedingSizeLimit() {
        return getConversionManager().isExceedingFileSizeLimit(getDocument()); 
    }

    public boolean isAllowedToReconvert() {
        return super.isAllowedToEdit();
    }

    public String doDeleted() {
        addActionError(getText("doc.unauth.doc_deleted.text"));
        return DELETED;
    }

    public String approveDocument() {
        User user = getUser();
        Document doc = getDocument();

        if (user.getID() == AnonymousUser.ANONYMOUS_ID) {
            return LOGIN;
        }
        if (doc == null) {
            return ERROR;
        }
        doc.approve(user);
        return APPROVED;
    }


    public String rejectDocument() {
        if (StringUtils.isEmpty(rejectionNotes)) {
            addFieldError("rejectionNotes", getText("content.validation.required"));
            return REJECTED_ERROR;
        } else if (rejectionNotes.length() >= getMaxRejectionNoteLength()) {
            addFieldError("rejectionNotes", getText("doc.apprvl.error.rejectRsn.length", new String[] {Integer.toString(255)}));
            return REJECTED_ERROR;
        }

        User user = getUser();
        Document doc = getDocument();
        if (user.isAnonymous()) {
            return LOGIN;
        }
        if (doc == null) {
            return ERROR;
        }

        doc.reject(user, rejectionNotes);
        CommentDelegator commentManager = doc.getAuthorCommentDelegator();
        try {
            Comment comment = commentManager.createComment(user, filterDocumentHelper.getFilteredDocument(rejectionNotes, doc));
            commentManager.addComment(comment);
        }
        catch (RejectedException e) {
            log.error(e.getMessage());
        }
        catch (UnauthorizedException e) {
            log.debug("User: " + user.getID() + " is not authorized to create a comment "
                    + "while rejecting the document. Their rejection notes will be ignored.", e);
        }
        return REJECTED;
    }

    /**
     * Returns the upload success message for binary doc uploads.
     *
     * @return
     */
    public boolean getUploadSuccess() {
        return uploadSuccess;
    }

    public boolean isNeedsUserApproval() {
        if (!AnonymousUser.isAnonymousUser(getUserID())) {
            Collection<ApprovalStatus> approvalStatus = getDocument().getApprovalStatus();
            if (!approvalStatus.isEmpty()) {
                for (ApprovalStatus status : approvalStatus) {
                    if (status.getUser().getID() == getUserID() && !status.isApproved() && !status.isRejected()) {
                        return true;
                    }
                }
            }
        }
        return false;        
    }

    public int getViewerPageCutoff() {
        return getConversionManager().getViewerPageCutoff();
    }
    
    @Override
    public boolean isReconvertable() {
        ConversionStatus conversionStatus = getConversionStatus();
        
        boolean isReconvertable  = true;
        if (conversionStatus != null && conversionStatus.isError()) {
            ConversionError conversionError = ConversionError.getConversionError(conversionStatus.getError());
            isReconvertable = conversionError != null && conversionError.isReconvertable();
        }
        
        return isReconvertable;           
    }

    @Override
    public String getConversionError() {                      
        String error = getText("officeintegration.viewer.failed");
        ConversionStatus conversionStatus = getConversionStatus();
        if (conversionStatus != null && conversionStatus.isError()) {
            ConversionError conversionError = ConversionError.getConversionError(conversionStatus.getError());
            if (conversionError != null) {
                error = getText("officeintegration.viewer.failed.errorCode." + conversionError.getErrorCode());
            }
        }        
        return error;           
    }

    public void setAttachmentHelper(AttachmentHelper attachmentHelper) {
        this.attachmentHelper = attachmentHelper;
    }

    public void setExStorageInfoHelper(ExStorageInfoHelper exStorageInfoHelper) {
        this.exStorageInfoHelper = exStorageInfoHelper;
    }

    public ExStorageInfoHelper.ExStorageInfoBean getExStorageInfo(Document document) {
        final Long exStorageFileID = document.getExStorageFileID();
        if (exStorageFileID != null)
            return exStorageInfoHelper.getByFileID(exStorageFileID);

        return null;
    }


    public boolean isAbuseReported() {
        return abuseReported;
    }

    public void setAbuseReported(boolean abuseReported) {
        this.abuseReported = abuseReported;
    }

    public void setHasUserReportedAbuse(boolean userReportedAbuse) {
        this.userReportedAbuse = userReportedAbuse;
    }

    public boolean isUserReportedAbuse() {
        return abuseManager.hasUserReportedAbuse(getJiveObject(), getUser());
    }

    public int getMaxRejectionNoteLength() {
        return MAX_REJECTION_NOTE_LENGTH;
    }

    @Override
    public String buildMetaKeywords() {
        return new MetaKeywordBuilder(tagActionUtil).object(getDocument()).build();
    }

    public Collection<DocumentActionUIExtension> getDocumentActionUIExtensions() {
        return documentActionUIExtensions;
    }

    @Autowired
    public void setDocumentActionUIExtensions(Collection<DocumentActionUIExtension> documentActionUIExtensions) {
        this.documentActionUIExtensions = documentActionUIExtensions;
    }

    public boolean getHasContentPlaceRelationships() {
        return hasContentPlaceRelationships(getDocument());
    }

    public void setAppManager(AppManager appManager) {
        this.appManager = appManager;
    }

    public void setExStorageFileManager(ExStorageFileManager exStorageFileManager) {
        this.exStorageFileManager = exStorageFileManager;
    }

    public void setExStorageFileVersionManager(ExStorageFileVersionManager exStorageFileVersionManager) {
        this.exStorageFileVersionManager = exStorageFileVersionManager;
    }

    public void setDashboardViewHelper(DashboardViewHelper dashboardViewHelper) {
        this.dashboardViewHelper = dashboardViewHelper;
    }

    public DashboardAppInstanceView getAppInstance() {
        return appInstanceView;
    }

    public AppErrorView getAppErrorView() {
        return appErrorView;
    }

    public AppLaunchContextView getLaunchContext() {
        return launchContext;

    }

    private void setupPreviewApp(Document doc) {

        // If there is a preview app then configure it
        final Either<AppErrorView, App> previewApp = getPreviewApp(doc);
        for (AppErrorView aev : previewApp.left()) {
            appErrorView = aev;
            return;
        }

        for (App app : previewApp.right()) {

            // app context is not required
            final Either<AppErrorView, JSONObject> previewAppContextEither = getPreviewAppContext(app, doc);
            // Set the error and return if there is one
            for (AppErrorView aev : previewAppContextEither.left()) {
                appErrorView = aev;
                return;
            }

            final JSONObject previewAppContext = buildFullContext(app, doc, previewAppContextEither.right());

            launchContext = new AppLaunchContextView();
            launchContext.setContext(previewAppContext);
            launchContext.setID(doc.getID());
            launchContext.setCoreAPIType(getCoreAPIType());

            String view = getPreviewAppView(previewAppContext);
            for (DashboardAppInstanceView daiv : getPreviewAppInstance(app, view)) {
                appInstanceView = daiv;
                if ( !appInstanceView.getSuppportedViews().contains(view) ) {
                    appErrorView = error(app, AppErrorView.ERROR_TYPE_UNSUPPORTED_VIEW).setAppView(view);
                    return;
                }
            }

        }

    }

    private boolean isExternalFile(final Document doc) {
        return doc.getExStorageFileID() != null && doc.getExStorageFileID() > 0;
    }

    private Either<AppErrorView, App> getPreviewApp(final Document doc) {
        final Either<AppErrorView, UUID> previewAppID = getPreviewAppID(doc);
        if (previewAppID.isLeft()) {
            return Either.left(previewAppID.left());
        }
        for (final UUID appUUID : previewAppID.right()) {
            App app = appManager.getApp(appUUID);

            if ( app != null ) {
                return Either.right(app);
            }
            else {
                // in this case, the app is not available
                return Either.left(new AppErrorView().setType(AppErrorView.ERROR_TYPE_APP_REMOVED).setAppUUID(appUUID.toString()));
            }
        }
        return Either.right(Option.<App>none());
    }

    private JSONObject buildFullContext(App app, Document doc, Option<JSONObject> extraContext) {
        final JSONObject context = extraContext.getOrElse(new JSONObject());
        final ExStorageInfoHelper.ExStorageInfoBean storageInfo = getExStorageInfo(doc);
        safePut(context, "displayName", storageInfo.getDisplayName());
        safePut(context, "iconUrl", storageInfo.getIconUrl());
        safePut(context, "externalUrl", storageInfo.getExternalUrl());
        safePut(context, "maxFileSize", storageInfo.getMaxFileSize());
        safePut(context, "uploadResource", storageInfo.getUploadResource());
        safePut(context, "appUUID", app.getAppUUID());
        return context;
    }

    private String getPreviewAppView(JSONObject context) {
        return JSONUtil.safeGetString(context, "view").getOrElse(DEFAULT_PREVIEW_APP_VIEW);
    }

    private Option<DashboardAppInstanceView> getPreviewAppInstance(App app, String view) {
        return Option.some(dashboardViewHelper.findUserAppInstance(getUser(), app.getAppUUID(), view, ""));
    }

    private Either<AppErrorView, UUID> getPreviewAppID(final Document doc) {
        final ExStorageFile exStorageFile = exStorageFileManager.get(doc.getExStorageFileID());
        if (exStorageFile != null) {
            try {
                final String previewAppID = exStorageFile.getPreviewAppID();
                if (previewAppID != null) {
                    return Either.right(UUID.fromString(previewAppID));
                }
            }
            catch (IllegalArgumentException e) {
                final AppErrorView appErrorView = new AppErrorView();
                appErrorView.setType(AppErrorView.ERROR_TYPE_BAD_APP_UUID);
                return Either.left(appErrorView);
            }
        }
        return Either.right(Option.<UUID>none());
    }

    private Either<AppErrorView, JSONObject> getPreviewAppContext(final App app, final Document doc) {
        final ExStorageFileVersion ver = exStorageFileVersionManager.get(doc.getExStorageFileVersionID());
        if (ver != null) {
            final String previewAppContext = ver.getPreviewAppContext();
            if (previewAppContext != null) {
                try {
                    return Either.right(new JSONObject(previewAppContext));
                }
                catch (JSONException e) {
                    error(app, AppErrorView.ERROR_TYPE_INVALID_CONTEXT).setAppView(DEFAULT_PREVIEW_APP_VIEW);
                    return Either.left(new AppErrorView().setType(AppErrorView.ERROR_TYPE_BAD_APP_DATA));
                }
            }
        }
        return Either.right(Option.<JSONObject>none());
    }

    private AppErrorView error(final App app, final String type ) {
        final AppErrorView appErrorView = new AppErrorView();
        appErrorView.setType(type);
        appErrorView.setAppUUID(app.getAppUUID().toString());
        appErrorView.setAppName( app.getDisplayName() );
        appErrorView.setPreinstalled( app.getInstallType() == InstallTypeEnum.PRE_INSTALL );
        return appErrorView;
    }
    
	public String getBackUrl() {
		return backUrl;
	}

	public void setBackUrl(String backUrl) {
		this.backUrl = backUrl;
	}

	public Long getProjectID() {
		return projectID;
	}

	public void setProjectID(Long projectID) {
		this.projectID = projectID;
	}

	public Integer getActiveTab() {
		return activeTab;
	}

	public void setActiveTab(Integer activeTab) {
		this.activeTab = activeTab;
	}

	public String getVersionView() {
		return versionView;
	}

	public void setVersionView(String versionView) {
		this.versionView = versionView;
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
