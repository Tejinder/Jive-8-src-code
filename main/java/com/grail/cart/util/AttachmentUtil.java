package com.grail.cart.util;

import com.grail.cart.GrailCartBean;
import com.jivesoftware.base.User;
import com.jivesoftware.community.*;
import com.jivesoftware.community.action.util.RenderUtils;
import com.jivesoftware.community.impl.DbAttachment;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.renderer.impl.JiveGlobalRenderManager;
import com.jivesoftware.community.renderer.impl.v2.PdfRenderUtil;
import com.jivesoftware.community.util.XMLUtils;
import com.jivesoftware.util.ClassUtilsStatic;
import com.jivesoftware.util.StringUtils;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

/**
 * An Util class which is used for all the attachment related operations
 * User: vivek
 * Date: Feb 17, 2012
 * Time: 3:32:41 PM
 */
public class AttachmentUtil {

    private static final Logger log = Logger.getLogger(AttachmentUtil.class);

    private DocumentManager documentManager;
    private AttachmentManager attachmentManager;
    private JiveGlobalRenderManager renderManager;
    private FileCleaningTracker fileCleaningTracker;

    protected AtomicInteger counter = new AtomicInteger(0);
    protected String tempDir;
    private String size = JiveGlobals.getJiveProperty("jive.pdf.paper.size", XMLUtils.PaperSize.ANSI_A.toString());

    public void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setRenderManager(JiveGlobalRenderManager renderManager) {
        this.renderManager = renderManager;
    }


    public void init() {
        fileCleaningTracker = new FileCleaningTracker();
    }

    /**
     * For a given JiveUser, all the downnloadable cart items will be written
     * to a directory under the %TEMP_DIR% with the following directory
     * structure.
     *
     * For example a JiveUser with userID - 2001, this is how the directory
     * structure would be organized.
     *
     * %TEMP_DIR%/2001/Content
     * %TEMP_DIR%/2001/Attachments
     *
     * @param jiveUser - a jiveUser
     * @param cartItems - List of cart items which are eligible for download
     * @return a List of absolute filePaths' of the files which have been
     *         written to FileSystem (FS)
     */
    public List<GrailCartBean>  writeCartItemsToFS(User jiveUser, List<GrailCartBean> cartItems) {
        //The pattern to write the attachments in the temp directory would be
        // to have %TEMP_DIR%/userID/CONTENT
        //                          /ATTACHMENT
        log.debug(":::       Writing the CartITems to FS     :::");
        String userID = String.valueOf(jiveUser.getID());
        Iterator<GrailCartBean> itr = cartItems.iterator();
        List<String> files = new ArrayList<String>();

        //createDirectories first
        createDirectories(userID);

        while (itr.hasNext()) {
            GrailCartBean cartItem = itr.next();
            if (cartItem.getType().equalsIgnoreCase("pdf")) {
                //Goes to the ContentDirectory
                handleContent(userID, files, cartItem);

            } else if (cartItem.getType().equalsIgnoreCase("attachment")) {
                String attachID = cartItem.getAttachmentID();
                try {
                    DbAttachment dbAttachment = (DbAttachment) attachmentManager.getAttachment(Long.parseLong(attachID));
                    if (dbAttachment != null) {
                        log.debug(" Name  " + dbAttachment.getName());
                        handleAttachment(userID, files, (Attachment) dbAttachment, cartItem);
                    }


                } catch (AttachmentNotFoundException e) {
                    log.error("Unable to find the attachment ID   ::: " + attachID);
                    e.printStackTrace();
                } catch (IOException e) {
                    log.error("Failed to write an CartItem for the User    ::: " + userID);
                    log.error("\n Unable to write the cartItem to File System ::: " + cartItem.getCartItemID());
                    e.printStackTrace();
                }
            }

        }//while
        log.debug("Successfully written the cart items to file system");
        return cartItems;

    }

    /**
     * Is responsible for the CartItems which are of type - 'content' and
     * would be downloaded as a PDF file.
     * The files would be written to a directory called <bold>Content</bold>
     *
     * @param userID a jive userID.
     * @param files A list containing the filePaths, where the PDFs have been
     *              generated successfully.
     * @param cartItem Specific cartItem which is being processed.
     * @return a List of absolute filePaths' of those PDFs which were successfully
     *         written to FileSystem.
     */
    protected List handleContent(String userID, List<String> files, GrailCartBean cartItem) {
        FileOutputStream outputStream = null;
        ByteArrayInputStream xHtml = null;
        InputStream pdfStream = null;
        DeferredFileOutputStream dfos = null;
        try {

            Document doc = this.documentManager.getDocument(Long.parseLong(cartItem.getDocumentID()));
            String rawHtml = RenderUtils.renderToCompleteHtml(doc, renderManager).replaceAll("__jive_macro_name:font;", "");
            xHtml = PdfRenderUtil.prepareHtmlForPdf(rawHtml, true);
            pdfStream = asPdf(JiveApplication.getContext(), xHtml, XMLUtils.PaperSize.parse(getSize()));
            if (pdfStream != null) {
                String filePath = getContentDir(userID) + File.separator + cartItem.getDocumentID() + ".pdf";
                File file = new File(filePath);
                dfos = new DeferredFileOutputStream((int) FileUtils.ONE_MB, file);
                outputStream = new FileOutputStream(file);
                IOUtils.copyLarge(pdfStream, outputStream);
                outputStream.close();
                files.add(filePath);
                cartItem.setDocumentLoc(filePath);
                // Since pdfStream object is a marker object for the file and pdfStream is closed once we are done with
                // the processing of file.
                // this.fileCleaningTracker.track(file, pdfStream);
            } else {
                log.info("Unable to generate PDFstream, therefore iterate with remaining cart items");
            }

        } catch (DocumentObjectNotFoundException e) {
            log.error("Unable to find the document --- " + cartItem.getDocumentID() + " Skipping the PDF generation ");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            log.error(" Unable to prepareHTML for PDF generation");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(xHtml);
            IOUtils.closeQuietly(pdfStream);
            IOUtils.closeQuietly(dfos);
        }
        return files;
    }

    /**
     * Is responsible for the CartItems which are of type 'attachment'.
     * The files would be written to a directory called <bold>Attachments</bold>
     * and saved as PDF file.
     *
     * @param userID  a jive UserId
     * @param files A list containing the filePaths, where the PDFs have been
     *              generated successfully.
     * @param attachment A JiveAttachment object which will be written to the FileSystem
     * @return  a List of absolute filePaths' of those "Attachments" which were
     *          successfully written to FileSystem (FS)
     * @throws IOException
     */
    public List handleAttachment(String userID, List<String> files, Attachment attachment, GrailCartBean cartItem) throws IOException {

        long contentLength = attachment.getSize();
        InputStream in = null;
        FileOutputStream out = null;
        try {
            InputStream raw = attachment.getUnfilteredData();
            in = new BufferedInputStream(raw);
            byte[] data = new byte[(int) contentLength];
            int bytesRead = 0;
            int offset = 0;
            if (in != null) {
                while (offset < contentLength) {
                    bytesRead = in.read(data, offset, data.length - offset);
                    if (bytesRead == -1)
                        break;
                    offset += bytesRead;
                }
                String filePath = getAttachmentDir(userID) + File.separator + attachment.getName();
                boolean fileExists = new File(filePath).exists();
                log.debug("Does file --- " + filePath + "    already exists in the tempDir  --- " + fileExists);
                if (!fileExists) {
                    out = new FileOutputStream(filePath);
                    out.write(data);
                } else {
                    log.info(" File exists, just add the entry ");
                }

                files.add(filePath);
                cartItem.setDocumentLoc(filePath);
            }
        } finally {

            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }

        return files;
    }

    /**
     * A wrapper method to generate an PDF for the Document which are marked for
     * 'Content' download.
     *
     * @param jiveContext - Jivecontext
     * @param stream - Document as Stream
     * @param size - The dimension of paper
     * @return - PDF object as stream
     */
    public static InputStream asPdf(JiveContext jiveContext, ByteArrayInputStream stream, XMLUtils.PaperSize size) {
        InputStream xslStream = null;
        try {
            xslStream = getDocumentXSL(size == null ? XMLUtils.PaperSize.ANSI_A : size);
            return XMLUtils.generatePDF(jiveContext, stream, xslStream);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            stream.reset();
            try {
                log.error(IOUtils.toString(stream));
            } catch (Exception ex) {
                // Ignore.
            }
        } finally {
            IOUtils.closeQuietly(xslStream);
        }
        return null;
    }

    /**
     * A simple util method to get the last directory name from the
     * Absolute filePath.
     *
     * For ex: dirPath = /usr/local/temp/2001/content
     * Will fetch the directory name 'content
     *
     * @param dirPath - An absolute directory path
     * @return String which is directory name
     */
    public static String getLastElement(String dirPath) {

        int i = dirPath.lastIndexOf("\\");
        String justDir = dirPath.substring(i + 1, dirPath.length());
        return justDir;
    }

    /**
     * Generates a ZIP stream for a given User and the List of files
     *
     * @param userID - Jive UserId
     * @param items - List of files which will be added to the Zip
     * @return - ZIP as byte array
     * @throws IOException
     */
    public static byte[] getCartAsZip(String userID, List<GrailCartBean> items) throws IOException {
        log.debug(" Generate the ZIP archive for download ");
        log.info(" Generate the ZIP archive for download ");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(baos);
        InputStream fis = null;
        Set<String> dirs = new HashSet<String>();
        for(GrailCartBean item : items){
            dirs.add(item.getDocumentID());
        }
        log.info("No of directories - " + dirs.size());

        try {

            for(String dir : dirs){
                log.info("Add " + "DOC-"+ dir + "/" + " as a ZIP entry");
                zipOut.putNextEntry(new ZipEntry("DOC-"+ dir + "/"));
                zipOut.closeEntry();
            }
            /*zipOut.putNextEntry(new ZipEntry("attachments" + "/"));
            zipOut.closeEntry();
            zipOut.putNextEntry(new ZipEntry("content" + "/"));
            zipOut.closeEntry();*/

            for (GrailCartBean item : items) {
                String filePath = item.getDocumentLoc();
                if(filePath != null){
                    log.debug(" Zip -- filePath :::  " + filePath);
                    log.info(" Zip -- filePath :::  " + filePath);
                    File imageFile = new File(filePath);
                    try{
                        fis = new FileInputStream(imageFile);

                        int data;
                        String filename = imageFile.getName();
                        filename = "DOC-" + item.getDocumentID()+"\\" + getLastElement(filename);
                        /*if (filePath.contains("attachments")) {
                            //log.info(" Attachment entry      "+getLastElement(filename));
                            filename = "attachments\\" + getLastElement(filename);
                        } else if (filePath.contains("content")) {
                            filename = "content\\" + getLastElement(filename);
                        }*/
                        ZipEntry entry = new ZipEntry(filename);
                        entry.setMethod(ZipEntry.DEFLATED);
                        entry.setSize(imageFile.length());
                        //TODO: Handle Duplicate Entry, if any. We do not have a mechanism to handle Duplicates as of now.
                        zipOut.putNextEntry(entry);
                        while ((data = fis.read()) > -1) {
                            zipOut.write(data);
                        }
                        fis.close();
                        zipOut.closeEntry();
                    } catch (ZipException zipEx) {
                        log.error("Currently, we are not allowing duplicate attachment entries in the Cart --- " + zipEx.getMessage());
                        log.info("Duplicate item - Currently, we are not allowing duplicate attachment entries in the Cart --- ", zipEx);

                    } catch (FileNotFoundException ex) {
                        log.info("File not found to write into the zip --- ", ex);
                    }

                }

            }
        } finally {
            if (fis != null)
                IOUtils.closeQuietly(fis);

            IOUtils.closeQuietly(zipOut);
        }
        log.debug("\n\n :::      Successfully generated ZIP stream       ::::");
        return baos.toByteArray();
    }

    /**
     * Returns an User directory found in the created in the %TEMP_DIR%
     * @param userID - Jive userID
     * @return
     */
    public String getUserDir(String userID) {
        if (tempDir == null)
            tempDir = System.getProperty("java.io.tmpdir");

        String userDir = tempDir + File.separator + "%s";
        userDir = String.format(userDir, userID);
        log.info("\n\n  ##### User Directory path  ::: " + userDir);
        return userDir;
    }

    protected String getAttachmentDir(String userID) {
        return getUserDir(userID) + File.separator + "attachments";
    }

    protected String getContentDir(String userID) {
        return getUserDir(userID) + File.separator + "content";
    }

    public void cleanDirectories(String userID) {

        String userDir = getUserDir(userID);
        String attachDir = getAttachmentDir(userID);
        String contentDir = getContentDir(userID);

        if (directoryExists(userDir)) {
            // (new File(userDir)).delete();
            try {
                FileUtils.deleteDirectory(new File(userDir));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            log.info("userDir exists " + directoryExists(userDir));
        }
        /*if (!directoryExists(attachDir)) {
            (new File(attachDir)).delete();
            log.info("attachDir deleted  "+directoryExists(attachDir));
        }
        if (!directoryExists(contentDir)) {
            (new File(contentDir)).delete();
            log.info("contentDir deleted "+directoryExists(contentDir));
        }*/

    }

    protected void createDirectories(String userID) {

        fileCleaningTracker = new FileCleaningTracker();

        String userDir = getUserDir(userID);
        String attachDir = getAttachmentDir(userID);
        String contentDir = getContentDir(userID);

        if (!directoryExists(userDir)) {
            (new File(userDir)).mkdir();
            log.debug("userDir created ");
        }
        if (!directoryExists(attachDir)) {
            (new File(attachDir)).mkdir();
            log.debug("attachDir created ");
        }
        if (!directoryExists(contentDir)) {
            (new File(contentDir)).mkdir();
            log.debug("contentDir created ");
        }

    }

    protected boolean directoryExists(String dirName) {
        log.info("Directory exists check for ::::: " + dirName);
        return new File(dirName).exists();
    }

    public static InputStream getDocumentXSL(XMLUtils.PaperSize paper) throws IOException {
        InputStream stream = ClassUtilsStatic.getResourceAsStream("/fo-template.xsl");
        StringWriter writer = new StringWriter();
        IOUtils.copy(stream, writer);
        String xsl = writer.toString();
        Map<String, String> map = paper.dimensions();
        for (String key : map.keySet()) {
            xsl = StringUtils.replaceAll(xsl, "${" + key + "}", map.get(key));
        }
        xsl = StringUtils.replaceAll(xsl, "\n", "");
        xsl = StringUtils.replaceAll(xsl, "\r\n", "");
        xsl = StringUtils.replaceAll(xsl, "\t", " ");
        return new ByteArrayInputStream(xsl.getBytes());
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
