/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.util;


import com.grail.GrailGlobals;
import com.grail.kantar.object.KantarAttachment;
import com.grail.kantar.objecttype.KantarAttachmentEntitlementCheckProvider;
import com.grail.kantar.util.KantarGlobals;
import com.grail.object.GrailAttachment;
import com.grail.object.GrailEmailQueryAttachment;
import com.grail.objecttype.GrailAttachmentEntitlementCheckProvider;
import com.grail.objecttype.GrailEmailQueryAttachmentEntitlementCheckProvider;
import com.grail.osp.object.OSPAttachment;
import com.grail.osp.objecttype.OSPAttachmentEntitlementCheckProvider;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.object.SynchroAttachment;
import com.grail.synchro.objecttype.SynchroAttachmentEntitlementCheckProvider;
import com.grail.synchro.util.SynchroLogUtils;

import com.google.common.net.HttpHeaders;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.aaa.JiveAuthentication;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentContentResource;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.BinaryBody;
import com.jivesoftware.community.BinaryBodyDownloadException;
import com.jivesoftware.community.BinaryBodyManager;
import com.jivesoftware.community.BlogPost;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.DocumentManager;
import com.jivesoftware.community.DocumentObjectNotFoundException;
import com.jivesoftware.community.ForumMessage;
import com.jivesoftware.community.ForumMessageNotFoundException;
import com.jivesoftware.community.ForumThreadNotFoundException;
import com.jivesoftware.community.Image;
import com.jivesoftware.community.ImageContentResource;
import com.jivesoftware.community.ImageManager;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveContext;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveHome;
import com.jivesoftware.community.JiveObject;
import com.jivesoftware.community.NotFoundException;
import com.jivesoftware.community.aaa.AccessManager;
import com.jivesoftware.community.aaa.authz.EntitlementTypeProvider;
import com.jivesoftware.community.action.util.ParamUtils;
import com.jivesoftware.community.app.App;
import com.jivesoftware.community.attachments.AttachmentManagerFactory;
import com.jivesoftware.community.comments.Comment;
import com.jivesoftware.community.event.AttachmentEvent;
import com.jivesoftware.community.event.DocumentEvent;
import com.jivesoftware.community.image.thumbnailer.ImageThumbnailManager;
import com.jivesoftware.community.impl.DatabaseObjectLoader;
import com.jivesoftware.community.impl.DbAttachment;
import com.jivesoftware.community.impl.DbAttachmentManagerAbstract;
import com.jivesoftware.community.impl.DbBinaryBody;
import com.jivesoftware.community.impl.DbDocumentManager;
import com.jivesoftware.community.impl.DbImage;
import com.jivesoftware.community.integration.storage.file.ExStorageFile;
import com.jivesoftware.community.integration.storage.file.ExStorageFileManager;
import com.jivesoftware.community.integration.storage.file.ExStorageFileResultFilter;
import com.jivesoftware.community.integration.tile.Tile;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.proxy.BinaryBodyProxy;
import com.jivesoftware.community.web.ServletUtils;
import com.jivesoftware.community.web.util.UserAgentUtil;
import com.jivesoftware.conversion.ConversionManager;
import com.jivesoftware.conversion.provider.ConvertibleObjectProvider;
import com.jivesoftware.eos.StorageProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.conn.EofSensorInputStream;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jivesoftware.community.JiveGlobals.JIVE_DEV_MODE_KEY;
import static com.jivesoftware.community.action.util.ActionUtils.getRemoteAddress;
import static com.jivesoftware.util.StringUtils.isEmpty;
import static java.lang.String.format;

/**
 * A servlet that is used to serve binary data such as attachments or images. This servlet should be registered to be
 * loaded at appserver startup. Below is a sample entry in a Servlet 2.3's webapp web.xml file:<p>
 * <pre>
 *  &lt;servlet
 *      servlet-name="JiveServlet"
 *      servlet-class="com.jivesoftware.community.util.JiveServlet"
 *  &gt;
 *      &lt;load-on-startup/&gt;1&lt;/load-on-startup/&gt;
 *  &lt;/servlet&gt;
 * </pre>
 */
public class JiveServlet extends HttpServlet {

    private static final Logger log = LogManager.getLogger(JiveServlet.class);

    private long startUp = 0;
    private AccessManager accessManager;

    /**
     * Mime types for office 2007 files.
     * @see <a href="http://www.therightstuff.de/2006/12/16/Office+2007+File+Icons+For+Windows+SharePoint+Services+20+And+SharePoint+Portal+Server+2003.aspx">Office 2007 File Icons for Windows SharePoint Services 2.0 and SharePoint Portal Server 2003</a>
     */
    private static final List<String> OFFICE_2007_MIME_TYPES = new ArrayList<String>() {{
        add("application/vnd.ms-word.document.macroEnabled.12");
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        add("application/vnd.ms-word.template.macroEnabled.12");
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        add("application/vnd.ms-powerpoint.template.macroEnabled.12");
        add("application/vnd.openxmlformats-officedocument.presentationml.template");
        add("application/vnd.ms-powerpoint.addin.macroEnabled.12");
        add("application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
        add("application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        add("application/vnd.ms-powerpoint.presentation.macroEnabled.12");
        add("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        add("application/vnd.ms-excel.addin.macroEnabled.12");
        add("application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        add("application/vnd.ms-excel.sheet.macroEnabled.12");
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        add("application/vnd.ms-excel.template.macroEnabled.12");
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.template");
    }};

    private static final String CACHE_SECURE_REVALIDATE_FLAG = "jive.cache.secure.must-revalidate";
    private static final String CACHE_SECURE_MAX_AGE_FLAG = "jive.cache.secure.max-age";
    private static final String HEADER_IE_CACHE_CONTROL = "X-JAL-IECC";

    JiveServletImageUtils jiveServletImageUtils;
    ImageThumbnailManager imageThumbnailManager;

    /**
     * This method will set the "jiveHome" property if its passed in as an init parameter to this servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        startUp = System.currentTimeMillis();
        jiveServletImageUtils = new JiveServletImageUtils();
        accessManager = JiveApplication.getContext().getAccessManager();
    }

    /**
     * Handles all incoming requests. Calling this servlet with no arguments results in the version of Jive being
     * printed out.
     */
    public void service(HttpServletRequest request, HttpServletResponse response) {

        // mimic the behavior in GuestAuthorizationInterceptor: don't allow anonymous requests if the jive.auth.disallowGuest
        /// jive property has been set
        if (!allowAnonymous() && isAnonymousRequest()) {
            try {
                response.sendRedirect(redirectUrl(request, false));
            }
            catch (IOException e) {
                log.debug(e.getMessage());
            }
            return;
        }
        if(request.getParameter("fromDocumentRepository") != null && Boolean.parseBoolean(request.getParameter("fromDocumentRepository"))) {
            SynchroLogUtils.addLog(SynchroGlobal.PortalType.DOCUMENT.getDescription(), SynchroGlobal.PageType.REPORTS.getId(),
                    SynchroGlobal.Activity.DOWNLOAD.getId(),0,
                    request.getParameter("fileName") != null?"File - "+request.getParameter("fileName"):"",
                    request.getParameter("objectName") != null?""+request.getParameter("objectName"):"",
                    -1L, JiveApplication.getEffectiveContext().getAuthenticationProvider().getAuthentication().getUserID());
        }
        /*
            the platform will attempt to cache resources based on content type, we want to make sure images that belong
            to a content object are never saved locally
         */
        response.setHeader("Cache-Control", "no-store, private");

        String pathInfo = request.getPathInfo();
        // Check if we need to handle attachment downloads:
        if ("true".equals(request.getParameter("attachImage"))) {
            try {
                handleAttachmentPreview(request, response);
            }
            catch (Exception e) {
                handleException(request, response, e);
            }
        }
        else if ("true".equals(request.getParameter("bodyImage"))) {
            try {
                handleBinaryBodyPreview(request, response);
            }
            catch (Exception e) {
                handleException(request, response, e);
            }
        }
        else if (pathInfo != null && pathInfo.contains("downloadImage")) {
            try {
                handleImageDownload(request, response);
            }
            catch (Exception e) {
                handleException(request, response, e);
            }
        }
        else if (pathInfo != null && pathInfo.contains("imagePreview")) {
            try {
                handleImagePreview(request, response);
            }
            catch (Exception e) {
                log.error("Error handling image preview: " + request.getPathInfo(), e);
                handleException(request, response, e);
            }
        }
        else if (pathInfo != null && pathInfo.contains("showImage")) {
            try {
                handleImageShow(request, response);
            }
            catch (Exception e) {
                log.error("Error handling image show: " + request.getPathInfo(), e);
                handleException(request, response, e);
            }
        }
        // binary body preview
        else if (pathInfo != null && pathInfo.contains("previewBody")) {
            try {
                handleBinaryBodyDownload(request, response, false);
            }
            catch (Exception e) {
                handleException(request, response, e);
            }
        }
        // binary body download
        else if (pathInfo != null && pathInfo.contains("externalDocument")) {
            try {
                handleExternalDocument(request, response, false);
            }
            catch (Exception e) {
                handleException(request, response, e);
            }
        }
        else if (pathInfo != null && pathInfo.contains("downloadBody")) {
            try {
                handleBinaryBodyDownload(request, response, true);
            }
            catch (Exception e) {
                handleException(request, response, e);
            }
        }
        // attachment file download
        else if (pathInfo != null && pathInfo.contains("download")) {
            try {
                handleAttachmentDownload(request, response);
            }
            catch (Exception e) {
                handleException(request, response, e);
            }
        } else if (pathInfo != null && pathInfo.contains("storageProvider")) {
            try {
                handleStorageProviderDownload(request, response);
            } catch (Exception e) {
                handleException(request, response, e);
            }
        } else if (pathInfo != null && pathInfo.contains("ediscoveryExportResult")) {
            try {
                handleEDiscoveryRequestDownload(request, response);
            } catch (Exception e) {
                handleException(request, response, e);
            }
        }
        else {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException ioe) {
                log.debug(ioe);
            }
        }
    }

    public static boolean isImage(String contentType) {
        return contentType != null && contentType.indexOf("image/") == 0;
    }

    public static boolean isPreviewable(HttpServletRequest request, String contentType) {
        if (contentType == null || UserAgentUtil.isIe(request)) {
            return false;
        }
        else if (contentType.equals("text/xml") || contentType.equals("image/svg+xml")) {
            return false;
        }
        if (contentType.indexOf("image/") == 0) {
            return true;
        }
        else if (contentType.indexOf("text/") == 0) {
            return !contentType.toLowerCase().contains("htm");
        }
        else if (contentType.indexOf("video/") == 0) {
            return true;
        }
        else if ("application/pdf".equals(contentType)) {
            return true;
        }
        // We can't allow flash to execute in the server domain as this is
        // a possible XSS vector. CS-13512
        else if ("application/x-shockwave-flash".equals(contentType)) {
            return false;
        }
        else if ("application/msword".equals(contentType)) {
            return true;
        }
        else if ("application/vnd.ms-powerpoint".equals(contentType)) {
            return true;
        }
        else if ("application/vnd.ms-excel".equals(contentType)) {
            return true;
        }
        else if (OFFICE_2007_MIME_TYPES.contains(contentType)) {
            return true;
        }

        return false;
    }

    /*
     * Handles an attachment image preview request.
     */
    private void handleAttachmentPreview(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JiveContext jiveContext = JiveApplication.getEffectiveContext();
        AttachmentManager attachManager = jiveContext.getAttachmentManager();
        ImageManager imageManager = jiveContext.getImageManager();
        boolean imagePreviewEnabled = attachManager.isImagePreviewEnabled();

        String contentType = request.getParameter("contentType");
        long attachmentID = ParamUtils.getLongParameter(request, "attachment", -1);
        int width = ParamUtils.getIntParameter(request, "width", attachManager.getImagePreviewMaxSize());
        int height = ParamUtils.getIntParameter(request, "height", attachManager.getImagePreviewMaxSize());

        // Determine the image that we'll show next to the attachment. It will
        // either be a representation of the mime type or a thumbnail if it's
        // an image attachment and thumbnail support is turned on.
        String imageName = DbAttachmentManagerAbstract.getThumbnailImage(contentType);

        File image = null;
        // If thumbnails are enabled and this is an image, get the thumbnail.
        if (imagePreviewEnabled && contentType != null && contentType.contains("image")) {
            Attachment attachment = DatabaseObjectLoader.getInstance()
                    .getJiveObject(JiveConstants.ATTACHMENT, attachmentID);

            if (attachment == null) {
                throw new AttachmentNotFoundException("Unable to load attachment with id " + attachmentID);
            }

            //make sure the image isn't dangerously large.  Attachment limits are ususally higher than image limits.
            if (attachment.getSize() <= JiveGlobals.getJiveIntProperty("attachment.thumbnailCutoff", 1024 * imageManager.getMaxImageSize())) {
                //validate that the effective user is authorized to view the attachment
                if (!(canViewAttachment(attachment))) {
                    log.warn(format("Unauthorized attachment request for attachmentID: %s by %s.", attachmentID,
                            getRemoteAddress(request)));
                    response.sendRedirect(redirectUrlForUnauthorized(request));
                    return;
                }

                try {
                    width = Math.min(width, attachManager.getImagePreviewMaxSize());
                    height = Math.min(height, attachManager.getImagePreviewMaxSize());
                    boolean preserveRatio = attachManager.isImagePreviewRatioEnabled();
                    image = getAttachmentThumbnail(attachment, width, height, preserveRatio);
                }
                catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                //Image not done thumbnailing
                if (image == null) {
                    image = new File(JiveHome.getAttachmentImageCache(), imageName);
                    writeInputStreamToResponseOutputStream(request, response, attachment.getData(), image.getName(), attachment.getSize(), false, getContentType(getImageType(image.getName())));
                    return;
                }
            }
        }

        // Otherwise, use the normal image.
        if (image == null) {
            image = new File(JiveHome.getAttachmentImageCache(), imageName);
        }

        if (!checkCacheHeaders(image.length(), request, response)) {
            writeFileToResponseOutputStream(request, response, image, false, getContentType(getImageType(image.getName())));
        }
    }

    /**
     * Shows an image.
     *
     * @param request the http request.
     * @param response the http response.
     * @throws Exception if there is an error.
     */
    private void handleImageShow(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Get parameters as extra path information.
        String pathInfo = request.getPathInfo();
        pathInfo = pathInfo.substring(pathInfo.indexOf("/showImage/") + "/showImage/".length());
        JiveContext jiveContext = JiveApplication.getEffectiveContext();
        Object[] o;

        try {
            o = loadImage(pathInfo, jiveContext);
        }
        catch (UnauthorizedException e) {
            log.warn(e.getMessage());
            response.sendRedirect(redirectUrlForUnauthorized(request));
            return;
        }
        catch (Exception e) {
            // busted url, log it
            log.error("Unable to parse image url '" + request.getPathInfo());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long imageID = (Long) o[0];
        Image image = (Image) o[1];

        if (image == null) {
            throw new NotFoundException("Image " + imageID + " not found.");
        }

        //validate that the effective user is authorized to view the attachment
        if (!(canViewImage(image))) {
            log.warn(format("Unauthorized image request for imageID: %s by %s.", imageID,
                    getRemoteAddress(request)));
            response.sendRedirect(redirectUrlForUnauthorized(request));
            return;
        }

        writeEtagHeaders(response);
        writeInputStreamToResponseOutputStream(request, response, image.getData(), contentType(image), image.getSize(), false,
                contentType(image));
    }

    private String contentType(Image image) {
        return jiveServletImageUtils.contentType(image);
    }

    /*
    * Download a image (images feature)
    */
    private void handleImageDownload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Get parameters as extra path information.
        String pathInfo = request.getPathInfo();
        Map<String, String[]> parameters = request.getParameterMap();
        pathInfo = pathInfo.substring(pathInfo.indexOf("/downloadImage/") + "/downloadImage/".length());
        JiveContext jiveContext = JiveApplication.getEffectiveContext();
        Object[] o = loadImage(pathInfo, jiveContext);

        long imageID = (Long) o[0];
        Image image = (Image) o[1];

        if (image == null) {
            throw new NotFoundException("Image " + imageID + " not found.");
        }

        //validate that the effective user is authorized to view the attachment
        if (!(canViewImage(image))) {
            log.warn(format("Unauthorized image request for imageID: %s by %s.", imageID, getRemoteAddress(request)));
            response.sendRedirect(redirectUrlForUnauthorized(request));
            return;
        }


        Optional<File> maybeThumbnail;
        try {
            maybeThumbnail = jiveServletImageUtils.getImage(pathInfo, parameters, image);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }

        if (!maybeThumbnail.isPresent()) {
            //Thumbnailing queued - return full size image
            writeInputStreamToResponseOutputStream(request, response, image.getData(), image.getName(), image.getSize(), false, contentType(image) );
        }
        else {
            File thumbnail = maybeThumbnail.get();
            if (!checkCacheHeaders(thumbnail.length(), request, response)) {
                writeFileToResponseOutputStream(request, response, thumbnail, false, contentType(image));
            }
        }
    }


    private String getContentType(String imgType) {
        return jiveServletImageUtils.getContentType(imgType);
    }

    private String getImageType(String imgType) {
        return jiveServletImageUtils.getImageType(imgType);
    }

    /*
     * Preview an image
     */
    protected void handleImagePreview(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Get parameters as extra path information.
        String pathInfo = request.getPathInfo();
        pathInfo = pathInfo.substring(pathInfo.indexOf("/imagePreview/") + "/imagePreview/".length());
        JiveContext jiveContext = JiveApplication.getEffectiveContext();
        Object[] o;

        try {
            o = loadImage(pathInfo, jiveContext);
        }
        catch (Exception e) {
            // busted url, log it
            log.error("Unable to parse image url '" + request.getPathInfo());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long imageID = (Long) o[0];
        Image image = (Image) o[1];

        if (image == null) {
            throw new NotFoundException("Image " + imageID + " not found.");
        }

        //validate that the effective user is authorized to view the attachment
        if (!(canViewImage(image))) {
            log.warn(format("Unauthorized image request for imageID: %s by %s.", imageID, getRemoteAddress(request)));
            response.sendRedirect(redirectUrlForUnauthorized(request));
            return;
        }

        ImageManager imageManager = jiveContext.getImageManager();
        int width = imageManager.getImagePreviewMaxSize();
        int height = imageManager.getImagePreviewMaxSize();

        // check to see if we were passed size info in the path
        if (pathInfo.indexOf('/') != pathInfo.lastIndexOf('/')) {
            String sizeInfo = pathInfo.substring(pathInfo.indexOf('/') + 1, pathInfo.lastIndexOf('/'));
            StringTokenizer tokens = new StringTokenizer(sizeInfo, "-");
            if (tokens.countTokens() == 2) {
                String widthParam = tokens.nextToken();
                String heightParam = tokens.nextToken();
                width = NumberUtils.isNumber(widthParam) ? Integer.parseInt(widthParam) : -1;
                height = NumberUtils.isNumber(heightParam) ? Integer.parseInt(heightParam) : -1;
            }
        }
        File thumbnail = null;

        try {
            int maxWidth = imageManager.getImagePreviewMaxSize();
            int maxHeight = imageManager.getImagePreviewMaxSize();
            width = Math.min(width, maxWidth);
            height = Math.min(height, maxHeight);
            thumbnail = jiveServletImageUtils.getImageThumbnail(image, width, height, true);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (thumbnail != null) {
            if (!checkCacheHeaders(thumbnail.length(), request, response)) {
                writeFileToResponseOutputStream(request, response, thumbnail, false, contentType(image));
            }
        }
        else {
            // so use the normal image.
            writeInputStreamToResponseOutputStream(request, response, image.getData(), image.getName(), image.getSize(), false,
                    contentType(image));
        }
    }

    /*
     * Download a file (binary body feature)
     */
    private void handleBinaryBodyDownload(HttpServletRequest request, HttpServletResponse response, boolean download)
    throws Exception
    {
        // Get parameters as extra path information. The encoding of the path
        // information is detailed in the getAttachmentURL method.
        String pathInfo = request.getPathInfo();
        pathInfo = pathInfo.substring(pathInfo.indexOf("/downloadBody/") + "/downloadBody/".length());
        long bodyID;
        int versionID;
        long docID;
        BinaryBody body;
        int index = pathInfo.indexOf("/");

        if (index < 0) {
            // busted url, log it
            log.error("Unable to parse binary document download url '" + request.getPathInfo() + "'");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String bodyInfo = pathInfo.substring(0, index);

        // If the body info contains dashes, it is a normal body download.
        // Otherwise, we're trying to download a temp body.

        Document doc;
        if (!bodyInfo.contains("-")) {
            bodyID = Long.parseLong(bodyInfo);

            JiveContext context = JiveApplication.getContext();
            DbDocumentManager documentManager = (DbDocumentManager) context.getDocumentManager();
            body = documentManager.getBinaryBody(bodyID);
            docID = body.getDocID();

            JiveContext jiveContext = JiveApplication.getEffectiveContext();
            doc = jiveContext.getDocumentManager().getDocument(docID);
        }
        else {
            StringTokenizer tokens = new StringTokenizer(pathInfo.substring(0, pathInfo.indexOf("/")), "-");
            if (tokens.countTokens() != 4) {
                log.error("Unable to parse binary document download url '" + request.getPathInfo() + "'");
            }

            docID = Long.parseLong(tokens.nextToken());
            int objectType = Integer.parseInt(tokens.nextToken());
            versionID = Integer.parseInt(tokens.nextToken());
            bodyID = Long.parseLong(tokens.nextToken());

            JiveContext jiveContext = JiveApplication.getEffectiveContext();

            // Load object
            if (objectType == JiveConstants.DOCUMENT) {
                doc = jiveContext.getDocumentManager().getDocument(docID);
                doc = doc.getVersionManager().getDocumentVersion(versionID).getDocument();
                body = doc.getBinaryBody();
            }
            else {
                log.error("Unsupported ObjectType supplied: " + objectType);
                return;
            }
        }

        if (body == null) {
            throw new DocumentObjectNotFoundException(JiveConstants.DOCUMENT_BODY, bodyID,
                    "Body " + bodyID + " not found.");
        }

        //validate that the effective user is authorized to view the binary body
        if (!(canViewBinaryBody(body))) {
            log.warn(format("Unauthorized binary body request for bodyID: %s by %s.", bodyID, getRemoteAddress(request)));
            response.sendRedirect(redirectUrlForUnauthorized(request));
            return;
        }

        InputStream in = null;
        long size = body.getSize();
        try {
            // If this user can view (enforced above), they can download. However, if they are anonymous
            // they they can't create a modified document with the Office-integration headers. JIVE-6924
            if (getRequestingUser() != null) {
                JiveContext jiveCtx = JiveApplication.getContext();
                ConversionManager conversionManager = jiveCtx.getSpringBean("conversionManager");

                // modify the downloading document body with the information necessary for office plugin
                // for linking
                if (conversionManager.isOfficeIntegrationLicensed() && conversionManager.isModifiableOnDownload(doc)) {
                    ConvertibleObjectProvider.SizedInputStream is = conversionManager.getModifiedStream(doc);

                    if (is != null) {
                        in = is.getInputStream();
                        size = is.getLength();
                    }
                }
            }

            // if stream wasn't taken from conversionManager (jive-for-office properties insertion) -
            // then get regular stream
            if (in == null) {
                in = body.getData();
            }

            // if no stream is found - throw an error
            if (in == null) {
                throw new IOException("Body data was null (Was the body deleted?)");
            }
        }
        catch (Exception e) {
            IOUtils.closeQuietly(in);

            log.error("Error writing data for body " + bodyID + ": " + e.getMessage(), e);

            fireDownloadIncomplete(doc);
            throw e;
        }

        try {
            if (!checkCacheHeaders(body.getSize(), request, response)) {
                writeInputStreamToResponseOutputStream(request, response, in, body.getName(), size,
                        download, body.getContentType());

                doc = JiveApplication.getEffectiveContext().getDocumentManager().getDocument(docID);
                Map<String, Object> map = new HashMap<>();
                map.put("downloadCompleted", Boolean.TRUE);
                map.put("remoteAddress", getRemoteAddress(request));
                if (!isAnonymousRequest()) {
                    map.put("downloadingUser", getRequestingUser());
                }
                JiveContext jiveContext = JiveApplication.getEffectiveContext();
                DocumentEvent event = new DocumentEvent(DocumentEvent.Type.BINARY_BODY_DOWNLOADED, doc,
                        doc.getJiveContainer(), map);
                jiveContext.getEventDispatcher().fire(event);
            }
        }
        catch (IOException ioe) {
            log.error("Error writing data for attachment " + bodyID + ": " + ioe.getMessage(), ioe);

            doc = JiveApplication.getEffectiveContext().getDocumentManager().getDocument(docID);
            Map<String, Object> map = new HashMap<>();
            map.put("downloadCompleted", Boolean.FALSE);
            JiveContext jiveContext = JiveApplication.getEffectiveContext();
            DocumentEvent event = new DocumentEvent(DocumentEvent.Type.BINARY_BODY_DOWNLOADED, doc, doc.getJiveContainer(),
                    map);
            jiveContext.getEventDispatcher().fire(event);
        }
    }

    private void fireDownloadIncomplete(Document doc)
    {
        JiveContext jiveContext = JiveApplication.getEffectiveContext();

        Map<String, Object> map = new HashMap<>();
        map.put("downloadCompleted", Boolean.FALSE);
        DocumentEvent event = new DocumentEvent(DocumentEvent.Type.BINARY_BODY_DOWNLOADED, doc, doc.getJiveContainer(),
                map);
        jiveContext.getEventDispatcher().fire(event);
    }

    /*
     * Handles an binary body image preview.
     */
    private void handleBinaryBodyPreview(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JiveContext jiveContext = JiveApplication.getEffectiveContext();
        AttachmentManager attachManager = jiveContext.getAttachmentManager();
        boolean imagePreviewEnabled = attachManager.isImagePreviewEnabled();

        String contentType = ParamUtils.getParameter(request, "contentType", "");
        long binaryBodyID = ParamUtils.getLongParameter(request, "binaryBodyID", -1);
        String imageName = DbAttachmentManagerAbstract.getThumbnailImage(contentType);

        // Determine the image that we'll show next to the attachment. It will
        // either be a representation of the mime type or a thumbnail if it's
        // an image attachment and thumbnail support is turned on.
        File image = null;

        // If thumbnails are enabled and this is an image, get the thumbnail.
        if (imagePreviewEnabled && contentType.contains("image")) {
            //BinaryBody body = (BinaryBody) DatabaseObjectLoader.getLongRowMapper().getJiveObject(JiveConstants.DOCUMENT_BODY, bodyID);

            //if (body == null) {
            //  throw new DocumentObjectNotFoundException("Unable to load document body with id " + bodyID);
            //}

            try {
                int maxHeight = ParamUtils.getIntParameter(request, "maxHeight", attachManager.getImagePreviewMaxSize());
                int maxWidth = ParamUtils.getIntParameter(request, "maxWidth", attachManager.getImagePreviewMaxSize());
                boolean preserveRatio = attachManager.isImagePreviewRatioEnabled();
                image = getBinaryBodyThumbnail(binaryBodyID, maxWidth, maxHeight, preserveRatio);
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            // thumbnail not thumbnailed yet (or error thumbnailing)
            if (image == null) {
                image = new File(JiveHome.getAttachmentImageCache(), imageName);
                JiveContext context = JiveApplication.getContext();
                DbDocumentManager documentManager = (DbDocumentManager) context.getDocumentManager();
                DbBinaryBody dbBody = (DbBinaryBody) documentManager.getBinaryBody(binaryBodyID);
                writeInputStreamToResponseOutputStream(request, response, dbBody.getData(), image.getName(), dbBody.getSize(), false, getContentType(getImageType(image.getName())));
                return;
            }
        }
        // Otherwise, use the normal image.
        else {
            image = new File(JiveHome.getAttachmentImageCache(), imageName);
        }

        response.addHeader(HttpHeaders.X_CONTENT_TYPE_OPTIONS, "nosniff");
        if (!checkCacheHeaders(image.length(), request, response)) {
            writeFileToResponseOutputStream(request, response, image, false, getContentType(getImageType(image.getName())));
        }
    }

    /*
     * Download an attachment
     */
    private void handleAttachmentDownload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Get parameters as extra path information. The encoding of the path
        // information is detailed in the getAttachmentURL method.
        String pathInfo = request.getPathInfo();
        pathInfo = pathInfo.substring(pathInfo.indexOf("/download/") + "/download/".length());
        JiveContext jiveContext = JiveApplication.getEffectiveContext();
        Object[] o;

        try {
            o = loadAttachment(pathInfo, jiveContext);
        }
        catch (Exception e) {
            // busted url, log it
            log.error("Unable to parse attachment download url '" + request.getPathInfo() + "'");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long attachmentID = (Long) o[0];
        Attachment attachment = (Attachment) o[1];

        //validate that the effective user is authorized to view the attachment
        if (!(canViewAttachment(attachment))) {
            log.warn(format("Unauthorized attachment request for attachmentID: %s by %s.", attachmentID,
                    getRemoteAddress(request)));
            response.sendRedirect(redirectUrlForUnauthorized(request));
            return;
        }

        if (attachment == null) {
            throw new AttachmentNotFoundException("Attachment " + attachmentID + " not found.");
        }

        boolean download = !attachment.getContentType().startsWith("image/");
        // check to see if we were passed size info in the path
        if (!download && pathInfo.indexOf('/') != pathInfo.lastIndexOf('/')) {
            String sizeInfo = pathInfo.substring(pathInfo.indexOf('/') + 1, pathInfo.lastIndexOf('/'));
            StringTokenizer tokens = new StringTokenizer(sizeInfo, "-");
            if (tokens.countTokens() == 2) {
                String widthParam = tokens.nextToken();
                String heightParam = tokens.nextToken();
                int width = NumberUtils.isNumber(widthParam) ? Integer.parseInt(widthParam) : -1;
                int height = NumberUtils.isNumber(heightParam) ? Integer.parseInt(heightParam) : -1;
                File thumbnail = null;

                try {
                    ImageManager imageManager = jiveContext.getImageManager();
                    int maxWidth = imageManager.getImageMaxWidth();
                    int maxHeight = imageManager.getImageMaxHeight();

                    width = Math.min(width, maxWidth);
                    height = Math.min(height, maxHeight);
                    thumbnail = getAttachmentThumbnail(attachment, width, height, true);
                }
                catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                if (thumbnail != null) {
                    if (!checkCacheHeaders(thumbnail.length(), request, response)) {
                        writeFileToResponseOutputStream(request, response, thumbnail, false, attachment.getContentType());
                    }

                    return;
                }
            }
        }

        if (!checkCacheHeaders(attachment.getSize(), request, response)) {
            writeInputStreamToResponseOutputStream(request, response, attachment.getData(), attachment.getName(),
                    attachment.getSize(), download, attachment.getContentType());

            // fire an attach downloaded event
            Map<String, Object> map = new HashMap<>();
            map.put("downloadCompleted", Boolean.TRUE);
            map.put("remoteAddress", getRemoteAddress(request));
            if (!isAnonymousRequest()) {
                map.put("downloadingUser", getRequestingUser());
            }
            JiveApplication.getContext().getEventDispatcher().fire(new AttachmentEvent(
                    AttachmentEvent.Type.DOWNLOADED, attachment.getAttachmentContentResource(), attachment, map));
        }
    }

    private void handleStorageProviderDownload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JiveContext context = JiveApplication.getEffectiveContext();
        Authentication auth = context.getAuthenticationProvider().getAuthentication();
        if (auth == null || !BasePermHelper.isSystemAdmin()) {
            throw new UnauthorizedException();
        }

        Pattern pattern = Pattern.compile("^/storageProvider/(.*)/(.*)$");
        Matcher matcher = pattern.matcher(request.getPathInfo());
        String key, fileName;
        if (matcher.matches() && matcher.groupCount() == 2) {
            key = matcher.group(1);
            fileName = matcher.group(2);
        } else {
            throw new IllegalArgumentException("Bad argument list -- path does not match '/storageProvider/{key}/{filename}'");
        }

        context = JiveApplication.getContext();
        StorageProvider storageProvider = context.getSpringBean("storageProvider");
        if (storageProvider.containsKey(key)) {
            writeInputStreamToResponseOutputStream(request, response, storageProvider.getStream(key), fileName, -1, true, "application/octet-stream");
        } else {
            throw new IllegalArgumentException(format("key:%s does not exist", key));
        }
    }

    private void handleEDiscoveryRequestDownload(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JiveContext context = JiveApplication.getEffectiveContext();
        Authentication auth = context.getAuthenticationProvider().getAuthentication();
        if (auth == null || !BasePermHelper.isFullAccessAdmin(context.getAuthenticationProvider().getJiveUser())) {
            throw new UnauthorizedException();
        }


        context = JiveApplication.getContext();

        String fileName =  request.getPathInfo().substring(request.getPathInfo().lastIndexOf("/") + 1);


        writeInputStreamToResponseOutputStream(request, response, context.getEdiscoveryExportManager().getResultStream(request.getRequestURI()),
                fileName, -1, true, "application/octet-stream");

    }

    private boolean isAnonymousRequest() {
        JiveContext jiveContext = JiveApplication.getEffectiveContext();
        JiveAuthentication auth = jiveContext.getAuthenticationProvider().getAuthentication();
        return auth == null || auth.isAnonymous();
    }


    private boolean allowAnonymous() {
        return accessManager.isGuestAccessAllowed();
    }

    private String getRequestingUser() {
        JiveContext jiveContext = JiveApplication.getEffectiveContext();
        JiveAuthentication auth = jiveContext.getAuthenticationProvider().getAuthentication();
        return auth == null  ? null : (auth.isAnonymous() ? null : auth.getUser().getUsername());
    }

    /*
     * Handles all errors.
     */
    private void handleException(HttpServletRequest request, HttpServletResponse response, Exception e) {
        if (!(e instanceof UnauthorizedException)) {
            log.error(e.getMessage(), e);
        }

        if (e instanceof UnauthorizedException) {
            try {
                response.sendRedirect(redirectUrlForUnauthorized(request));
            }
            catch (IOException ioe) {
                log.info("Unable to redirect unauth response.", ioe);
            }
            return;
        }

        try {
            PrintWriter out = response.getWriter();
            response.setContentType("text/html");
            out.println("<html><head><title>Jive SBS</title></head>");
            out.println("<body><font face=\"arial,helvetica,sans-serif\">");
            out.println("<b>Error</b><br><font size=\"-1\">");

            // Print out specific error message
            if (e instanceof ForumThreadNotFoundException) {
                out.println(ServletTextUtils.getText(request, response, "srvlt.jive.cannotLoadThrd.text"));
            }
            else if (e instanceof ForumMessageNotFoundException) {
                out.println(ServletTextUtils.getText(request, response, "srvlt.jive.cannotLoadMsg.text"));
            }
            else if (e instanceof DocumentObjectNotFoundException) {
                out.println(ServletTextUtils.getText(request, response, "srvlt.jive.cannotLoadDoc.text"));
            }
            else if (e instanceof UserNotFoundException) {
                out.println(ServletTextUtils.getText(request, response, "srvlt.jive.cannotLoadUser.text"));
            }
            else if (e instanceof AttachmentNotFoundException) {
                out.println(ServletTextUtils.getText(request, response, "srvlt.jive.cannotLoadAttch.text"));
            }
            else if (e instanceof BinaryBodyDownloadException && ((BinaryBodyDownloadException) e).getI18MessageKey() != null) {
                out.println(ServletTextUtils.getText(request, response, ((BinaryBodyDownloadException) e).getI18MessageKey()));
            }
            else {
                out.println(ServletTextUtils.getText(request, response, "srvlt.jive.general_error.text"));
            }

            out.println("</font></font></body></html>");
            out.close();
        }
        catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
        }
    }

    /**
     * Returns the file for an attachment thumbnail.
     *
     * @param attachment the attachment to return a thumbnail for
     * @param maxWidth the maximum width of the thumbnail
     * @param maxHeight the maximum height of the thumbnail
     * @param preserveRatio true if the aspect ratio of the image should be preserved when creating the thumbnail.
     * @return the thumbnail image file or <tt>null</tt> if a thumbnail can't be created.
     * @throws IOException if an error occurs creating the thumbnail.
     */
    public File getAttachmentThumbnail(Attachment attachment, int maxWidth, int maxHeight, boolean preserveRatio)
            throws IOException
    {
        File cacheDir = JiveHome.getAttachmentCache();
        if (!cacheDir.exists()) {
            boolean success = cacheDir.mkdirs();
            if (!success) {
                log.error("Unable to create cache directory: " + cacheDir.getAbsolutePath());
            }
        }

        String imgType = getImageType(attachment.getContentType());
        String thumbFileName = attachment.getID() + "_" + maxWidth + "_" + maxHeight + "_cache." + imgType;
        File thumbFile = new File(cacheDir, thumbFileName);

        // If the file already exists, return it. Note, we don't have any
        // kind of built-in cache expiration so this has to be handled in
        // the admin tool. Minimally, the cache should be expired whenever
        // the aspect ratio flag is toggled or if the max dimension changes.
        if (thumbFile.exists()) {
            return thumbFile;
        }

        // Otherwise, we need to generate the image.
        if (imageThumbnailManager == null) {
            imageThumbnailManager = JiveApplication.getContext().getSpringBean("imageThumbnailManager");
        }
        Future<File> thumbRequest = imageThumbnailManager.queueThumbnailGeneration(attachment, thumbFile, imgType, maxWidth, maxHeight, preserveRatio);
        try {
            return thumbRequest.get(2, TimeUnit.SECONDS);
        } catch (Exception ex) { /*timeout probably */ }
        return null;
    }

    /**
     * Returns the file for a binary body thumbnail.
     *
     * @param binaryBodyID the ID of the binary body to return a thumbnail for
     * @param maxWidth the maximum width of the thumbnail
     * @param maxHeight the maximum height of the thumbnail
     * @param preserveRatio true if the aspect ratio of the image should be preserved when creating the thumbnail.
     * @return the thumbnail image file or <tt>null</tt> if a thumbnail can't be created.
     * @throws IOException if an error occurs creating the thumbnail.
     */
    public File getBinaryBodyThumbnail(long binaryBodyID, int maxWidth, int maxHeight, boolean preserveRatio)
            throws IOException
    {
        final BinaryBodyManager binaryBodyManager = JiveApplication.getContext().getDefaultBinaryBodyManager();
        File cacheDir = new File(binaryBodyManager.getBinaryBodyDir(), "cache");
        if (!cacheDir.exists()) {
            boolean success = cacheDir.mkdirs();
            if (!success) {
                log.error("Unable to create cache directory: " + cacheDir.getAbsolutePath());
            }
        }

        String thumbFileName = binaryBodyID + "_" + maxWidth + "_" + maxHeight + "_cache.png";
        File thumbFile = new File(cacheDir, thumbFileName);

        // If the file already exists, return it. Note, we don't have any
        // kind of built-in cache expiration so this has to be handled in
        // the admin tool. Minimally, the cache should be expired whenever
        // the aspect ratio flag is toggled or if the max dimension changes.
        if (thumbFile.exists()) {
            return thumbFile;
        }

        // Otherwise, we need to generate the image.
        try {
            //load the binary body for this ID

            JiveContext context = JiveApplication.getContext();
            DbDocumentManager documentManager = (DbDocumentManager) context.getDocumentManager();
            DbBinaryBody dbBody = (DbBinaryBody) documentManager.getBinaryBody(binaryBodyID);
            Document document = documentManager.getDocument(dbBody.getDocID(), dbBody.getDocVersionID());
            if (imageThumbnailManager == null) {
                imageThumbnailManager = JiveApplication.getContext().getSpringBean("imageThumbnailManager");
            }
            imageThumbnailManager.queueThumbnailGeneration(document, binaryBodyID, "png", maxWidth, maxHeight, preserveRatio, thumbFile);
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Object[] loadAttachment(String pathInfo, JiveContext jiveContext) throws Exception {
        int index = pathInfo.indexOf("/");

        // if there is no /, then all the pathInfo has the information
        if (index < 0) {
            index = pathInfo.length();
        }

        // if index is 0 then there is no info
        if (index == 0) {
            throw new IllegalArgumentException("Unable to parse attachment download url '" + pathInfo + "'");
        }

        String attachmentInfo = pathInfo.substring(0, index);
        long attachmentID;
        Attachment attachment;

        // If the attachment info contains dashes, it is a normal attachment download.
        // Otherwise, we're trying to download a temp attachment.
        if (!attachmentInfo.contains("-")) {
            attachmentID = Long.parseLong(attachmentInfo);

            AttachmentManagerFactory attachmentManagerFactory = jiveContext.getAttachmentManagerFactory();

            AttachmentManager attachmentManager = attachmentManagerFactory.getAttachmentManager(attachmentID);

            DbAttachment dbAttachment = (DbAttachment) attachmentManager.getAttachment(attachmentID);
            if (dbAttachment.getObjectID() != -1) {
                throw new UnauthorizedException("Not allowed to read attachment.");
            }
            attachment = dbAttachment;
        }
        // Otherwise, normal attachment download.
        else {
            StringTokenizer tokens = new StringTokenizer(attachmentInfo, "-");
            if (tokens.countTokens() > 4) {
                log.error("Unable to parse attachment download url '" + pathInfo + "'");
            }

            JiveContext systemContext = JiveApplication.getContext();
            if (tokens.countTokens() == 2) {
                // ignore the first token - it's the object type
                Integer.parseInt(tokens.nextToken());
                attachmentID = Long.parseLong(tokens.nextToken());
                attachment = systemContext.getAttachmentManager().getAttachment(attachmentID);
            }
            // historic syntax - kept for backwards compatibility
            else if (tokens.countTokens() == 4) {
                // ignore communityID
                Long.parseLong(tokens.nextToken());
                // ignore threadID
                Long.parseLong(tokens.nextToken());
                // ignore messageID
                Long.parseLong(tokens.nextToken());
                attachmentID = Long.parseLong(tokens.nextToken());
                // Get the attachment
                attachment = systemContext.getAttachmentManager().getAttachment(attachmentID);
            }
            // user to load an attachment from one of a document's previous version
            else if (tokens.countTokens() == 3) {
                // ignore docID
                Long.parseLong(tokens.nextToken());
                // ignore versionID
                Long.parseLong(tokens.nextToken());
                attachmentID = Long.parseLong(tokens.nextToken());
                attachment = systemContext.getAttachmentManager().getAttachment(attachmentID);
            }
            else {
                throw new IllegalArgumentException("Unable to parse attachment download url '" + pathInfo + "'");
            }
        }

        return new Object[]{attachmentID, attachment};
    }

    public static Object[] loadImage(String pathInfo, JiveContext jiveContext) throws Exception {
        int index = pathInfo.indexOf("/");

        // if there is no /, then all the pathInfo has the information
        if (index < 0) {
            index = pathInfo.length();
        }

        // if index is 0 then there is no info
        if (index == 0) {
            throw new IllegalArgumentException("Unable to parse image download url '" + pathInfo + "'");
        }

        String imageInfo = pathInfo.substring(0, index);
        long imageID;
        Image image = null;

        // If the image info contains dashes, it is a normal image.
        // Otherwise, we're trying to preview a temp image.
        if (!imageInfo.contains("-")) {
            imageID = Long.parseLong(imageInfo);
            image = (DbImage) DatabaseObjectLoader.getInstance().getJiveObject(JiveConstants.IMAGE, imageID);
        }
        else {
            StringTokenizer tokens = new StringTokenizer(imageInfo, "-");

            if (tokens.countTokens() == 4) {
                // used to load an image from one of a document's previous version
                // objectType-docID-versionID-imageID
                tokens.nextToken(); // objectType
                long docID = Long.parseLong(tokens.nextToken());
                int versionID = Integer.parseInt(tokens.nextToken());
                imageID = Long.parseLong(tokens.nextToken());
                image = jiveContext.getDocumentManager().getDocument(docID, versionID).getImage(imageID);
            }
            else if (tokens.countTokens() == 3) {
                // objectType-objectID-imageID
                int objectType = Integer.parseInt(tokens.nextToken());
                long objectID = Long.parseLong(tokens.nextToken());
                imageID = Long.parseLong(tokens.nextToken());

                // attempt to load the image

                JiveObject jiveObject = DatabaseObjectLoader.getInstance().getJiveObject(objectType, objectID);
                if (jiveObject instanceof ImageContentResource) {
                    image = ((ImageContentResource) jiveObject).getImage(imageID);
                }
                else if (imageID > 0) {
                    image = jiveContext.getImageManager().getImage(imageID);
                }
                else {
                    log.error("Unsupported ObjectType supplied: " + objectType);
                }
            }
            else {
                throw new IllegalArgumentException("Unable to parse image download url '" + pathInfo + "'");
            }
        }

        return new Object[]{imageID, image};
    }

    private void writeFileToResponseOutputStream(HttpServletRequest request, HttpServletResponse response, File image, boolean download,
            String contentType) throws IOException
    {
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(image));
            writeInputStreamToResponseOutputStream(request, response, in, image.getName(), image.length(), download,
                    contentType);
        }
        catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void writeInputStreamToResponseOutputStream(HttpServletRequest request, HttpServletResponse response, InputStream in, String name,
            long size, boolean download, String contentType) throws IOException
    {
        if (in == null) {
            return;
        }
        // Write the content of the inputstream out
        // CS-11611 Don't allow IE to open any documents directly in the browser as this is a possible XSS
        // vector.
        // CS-13512 Don't allow flash files to be viewed in the server domain.
        if (download || !isPreviewable(request, contentType)) {
            String encodedFilename = URLEncoder.encode(name, JiveGlobals.getCharacterEncoding());
            //Convert + to %20, so we don't end up leaking + into the filename when the browser saves the file
            encodedFilename = encodedFilename.replaceAll("\\+", "%20");
            if (UserAgentUtil.isIe(request)) {
                // Internet Explorer
                response.setHeader("Content-disposition", "attachment; filename=" + encodedFilename);
            }
            else {
                // Firefox and Safari (see RFC 2231)
                response.setHeader("Content-disposition", "attachment; filename*=" + JiveGlobals.getCharacterEncoding() + "''" + encodedFilename);
            }
            // [CS-3756] Internet Explorer is unable to open Office documents from an SSL Web site if no-cache header is set
            //this is now done upstream, harder to manager at this level and there
            //response.setHeader("Pragma", "expires");
            //response.setHeader("Cache-Control", "private");
        }
        response.setContentType(contentType);
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();

            // Set the size
            response.setContentLength((int) size);

            // Use a 128K buffer.
            byte[] buf = new byte[128 * 1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        }
        catch(IOException e)
        {
            if (in instanceof EofSensorInputStream)
            {
                log.warn("Connection aborted.");

                EofSensorInputStream inAsEofSensor = (EofSensorInputStream) in;
                inAsEofSensor.abortConnection();
            }

            throw e;
        }
        finally
        {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    /*
    * Sets cache/status headers on the response object
    */
    private boolean checkCacheHeaders(long fileSize, HttpServletRequest request, HttpServletResponse response) {
        if (!JiveGlobals.getJiveBooleanProperty(JIVE_DEV_MODE_KEY, false) && !ServletUtils
                .isModified(request, fileSize, startUp))
        {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return true;
        }
        else {
            writeCacheControlHeader(response);
            writeEtagHeaders(response);
        }
        return false;
    }

    private void writeCacheControlHeader(HttpServletResponse response) {
        if (JiveGlobals.getJiveBooleanProperty(CACHE_SECURE_REVALIDATE_FLAG, false)) {
            response.setHeader(HEADER_IE_CACHE_CONTROL, format("max-age=%d, must-revalidate", JiveGlobals.getJiveIntProperty(CACHE_SECURE_MAX_AGE_FLAG, 1)));
        }
    }

    private void writeEtagHeaders(HttpServletResponse response) {
        response.setDateHeader("Last-Modified", startUp);
        //response.setHeader("Etag", ServletUtils.getEtag(fileSize));
    }

    private boolean canViewAttachment(Attachment attach) {
        if (attach == null) {
            return false;
        }

        AttachmentContentResource contentResource = attach.getAttachmentContentResource();
        DatabaseObjectLoader loader = DatabaseObjectLoader.getInstance();

        if (contentResource == null) {
            //this attachment belongs to content that hasn't yet been created. Usually happens when one content type
            //is moved to a different content type
            return true;
        }

        switch (contentResource.getObjectType()) {
            case (JiveConstants.DOCUMENT): {
                try {
                    Document doc = loader.getJiveObject(contentResource.getObjectType(), contentResource.getID());
                    return DocumentPermHelper.getCanViewDocument(doc);
                }
                catch (NotFoundException nfe) {
                    //no-op
                }
                break;
            }
            case (JiveConstants.BLOGPOST): {
                try {
                    BlogPost bp = loader.getJiveObject(contentResource.getObjectType(), contentResource.getID());
                    return BlogPermHelper.getCanViewBlogPost(bp);
                }
                catch (NotFoundException nfe) {
                    //no-op
                }
                break;
            }
            case (JiveConstants.MESSAGE): {
                try {
                    ForumMessage msg = loader.getJiveObject(contentResource.getObjectType(), contentResource.getID());
                    return MessagePermHelper.getCanViewMessage(msg);
                }
                catch (NotFoundException nfe) {
                    //no-op
                }
                break;
            }
            //TODO [NEB] - When you do perms
            case (JiveConstants.VIDEO) : {
                return true;
            }
            case (JiveConstants.BRIDGE) : {
                return true;
            }
            case (JiveConstants.BRIDGED_CONTENT_MESSAGE) : {
                return true;
            }
            default: {
            	 if(SynchroGlobal.isSynchroAttachmentObjectType(contentResource.getObjectType())) {
                     try {
                         SynchroAttachmentEntitlementCheckProvider entitlementCheckProvider = JiveApplication.getContext().getSpringBean("synchroAttachmentEntitlementCheckProvider");
                         SynchroAttachment synchroAttachment = new SynchroAttachment();
                         synchroAttachment.getBean().setObjectId(contentResource.getID());
                         synchroAttachment.getBean().setObjectType(contentResource.getObjectType());
                         return entitlementCheckProvider.isUserEntitled(synchroAttachment, EntitlementTypeProvider.EntitlementType.VIEW);
                     } catch (NullPointerException e) {

                     }
                 } if(KantarGlobals.isKantarAttachmentType(contentResource.getObjectType())
                         || KantarGlobals.isKantarReportAttachmentType(contentResource.getObjectType())) {
                     try {
                         KantarAttachmentEntitlementCheckProvider entitlementCheckProvider = JiveApplication.getContext().getSpringBean("kantarAttachmentEntitlementCheckProvider");
                         KantarAttachment kantarAttachment = new KantarAttachment();
                         kantarAttachment.getBean().setObjectId(contentResource.getID());
                         kantarAttachment.getBean().setObjectType(contentResource.getObjectType());
                         return entitlementCheckProvider.isUserEntitled(kantarAttachment, EntitlementTypeProvider.EntitlementType.VIEW);
                     } catch (NullPointerException e) {

                     }
                 } if(GrailGlobals.isGrailEmailQueriesAttachmentType(contentResource.getObjectType()) ) {
                     try {
                         GrailEmailQueryAttachmentEntitlementCheckProvider entitlementCheckProvider = JiveApplication.getContext().getSpringBean("grailEmailQueryAttachmentEntitlementCheckProvider");
                         GrailEmailQueryAttachment grailAttachment = new GrailEmailQueryAttachment();
                         grailAttachment.getBean().setObjectId(contentResource.getID());
                         grailAttachment.getBean().setObjectType(contentResource.getObjectType());
                         return entitlementCheckProvider.isUserEntitled(grailAttachment, EntitlementTypeProvider.EntitlementType.VIEW);
                     } catch (NullPointerException e) {

                     }
                 } if(GrailGlobals.isGrailAttachmentType(contentResource.getObjectType())) {
                     try {
                         GrailAttachmentEntitlementCheckProvider entitlementCheckProvider = JiveApplication.getContext().getSpringBean("grailAttachmentEntitlementCheckProvider");
                         GrailAttachment grailAttachment = new GrailAttachment();
                         grailAttachment.getBean().setObjectId(contentResource.getID());
                         grailAttachment.getBean().setObjectType(contentResource.getObjectType());
                         return entitlementCheckProvider.isUserEntitled(grailAttachment, EntitlementTypeProvider.EntitlementType.VIEW);
                     } catch (NullPointerException e) {

                     }
                 }
                 
                 if(SynchroGlobal.isOSPAttachmentType(contentResource.getObjectType())) {
                	 try
                	 {
                		 OSPAttachmentEntitlementCheckProvider entitlementCheckProvider = JiveApplication.getContext().getSpringBean("ospAttachmentEntitlementCheckProvider");
                		 OSPAttachment ospAttachment = new OSPAttachment();
                		 ospAttachment.getOspFile().setAttachmentId(contentResource.getID());
                		 //ospAttachment.getBean().setObjectType(bean.getObjectType());
                		 return entitlementCheckProvider.isUserEntitled(ospAttachment, EntitlementTypeProvider.EntitlementType.VIEW);
                	 }
                	 catch (NullPointerException e) 
                	 {

                     }
                	 
                   }
                 else
                 {
	            	try {
	                    JiveObject content = loader.getJiveObject(contentResource.getObjectType(), contentResource.getID());
	                    EntitlementTypeProvider entitlementTypeProvider = JiveApplication.getEffectiveContext().getEntitlementTypeProvider();
	                    return entitlementTypeProvider.isUserEntitled(content, EntitlementTypeProvider.EntitlementType.VIEW);
	                }
	                catch (NotFoundException e) {
	                    //no-op
	                }
                 }
                break;
            }
        }

        log.warn(format("Unable to determine content resource for %s/%s.", contentResource.getID(),
                contentResource.getObjectType()));
        return false;
    }

    public static boolean canViewImage(Image image) {
        if (image == null) {
            return false;
        }
        JiveObject jco = null;
        try {
            jco = image.getJiveContentObject();
        }
        catch(IllegalArgumentException e) {
            log.error("Couldn't find content object for iamge "+image.getID()+" it's probably ok.",e);
        }
        catch(NullPointerException e) {
            log.error("Couldn't find cotent object for iamge "+image.getID()+" it's probably ok.",e);
        }
        //this may be acceptable for image upload before a post/doc/thread has been
        //created
        if (jco == null) {
            log.debug("Image object " + image.getID() + " has no content object. ");
            return true;
        } else if (jco instanceof Comment) {
            jco = ((Comment) jco).getCommentContentResource();
        } else if (jco instanceof App) {
            return true;
        } else if (jco instanceof Tile) {
            return true;
        }

        return JiveApplication.getContext().getEntitlementTypeProvider()
                .isUserEntitled(jco, EntitlementTypeProvider.EntitlementType.VIEW);
    }

    private boolean canViewBinaryBody(BinaryBody body) {
        if (body == null) {
            return false;
        }

        DatabaseObjectLoader loader = DatabaseObjectLoader.getInstance();
        try {
            DbBinaryBody dbBody;
            if (body instanceof BinaryBodyProxy) {
                BinaryBodyProxy bbp = (BinaryBodyProxy) body;
                dbBody = (DbBinaryBody) bbp.getUnproxiedObject();
            }
            else {
                dbBody = (DbBinaryBody) body;
            }
            Document doc = loader.getJiveObject(JiveConstants.DOCUMENT, dbBody.getDocID());
            return DocumentPermHelper.getCanViewDocument(doc);
        }
        catch (NotFoundException nfe) {
            //no-op
        }
        log.warn(format("Unable to determine content resource for %s/%s.", body.getID(), body.getObjectType()));
        return false;
    }

    private String redirectUrlForUnauthorized(HttpServletRequest request) {
        return redirectUrl(request, true);
    }

    private String redirectUrl(HttpServletRequest request, boolean authzFailed) {
        String referer = isEmpty(request.getHeader("Referer")) ? request.getRequestURI() : request.getHeader("Referer");
        String url = format("%s/login.jspa?referer=%s", request.getContextPath(), referer);
        if (authzFailed) url = url + "&authzFailed=true";
        return url;
    }

    /*
     * Handle an external document (a document that is stored on an external stroage provider)
     */
    private void handleExternalDocument(HttpServletRequest request, HttpServletResponse response, boolean download)
    throws Exception
    {
        // Get parameters as extra path information. The encoding of the path
        // information is detailed in the getAttachmentURL method.
        String pathInfo = request.getPathInfo();
        pathInfo = pathInfo.substring(pathInfo.indexOf("/externalDocument/") + "/externalDocument/".length());

        int index = pathInfo.indexOf("/");
        if (index < 0) {
            // busted url, log it
            log.error("Unable to parse external document url '" + request.getPathInfo() + "'");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Pattern rePath = Pattern.compile("/externalDocument/(?<container>[^/]+)/(?<file>[^/]+)/?$");
        String requestUrl = request.getRequestURL().toString();

        Matcher pathMatcher = rePath.matcher(requestUrl);
        pathMatcher.find();

        long jiveContainerId = Long.parseLong(pathMatcher.group("container"));
        String espFileId = pathMatcher.group("file");

        ExStorageFile exFile = acquireExStorageFile(jiveContainerId, espFileId);
        if (exFile == null)
        {
            log.error("No external document exists for '" + request.getPathInfo() + "'");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        try
        {
            Document jiveDocument = acquireJiveDocument(exFile);
            String jiveDocumentPageUrl = format("%s/docs/%s/", JiveGlobals.getDefaultBaseURL(), jiveDocument.getDocumentID());
            response.sendRedirect(jiveDocumentPageUrl);
        }
        catch(DocumentObjectNotFoundException e)
        {
            log.error("No matching Jive document found for external document '" + request.getPathInfo() + "'");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private ExStorageFile acquireExStorageFile(long jiveContainerId, String espFileId)
            throws IOException
    {
        JiveContext context = JiveApplication.getEffectiveContext();
        ExStorageFileManager exFileManager = context.getSpringBean("exStorageFileManager");

        ExStorageFileResultFilter resultFilter = new ExStorageFileResultFilter();

        resultFilter.setContainerID(jiveContainerId);
        resultFilter.setEspFileID(espFileId);

        Iterable<ExStorageFile> exStorageFiles = exFileManager.get(resultFilter);

        Iterator<ExStorageFile> iterator = exStorageFiles.iterator();
        return iterator.next();
    }

    private Document acquireJiveDocument(ExStorageFile exFile)
            throws DocumentObjectNotFoundException
    {
        JiveContext context = JiveApplication.getEffectiveContext();
        DocumentManager documentManager = context.getDocumentManager();

        return documentManager.getDocumentByExStorageFileID(exFile.getID());
    }
}
