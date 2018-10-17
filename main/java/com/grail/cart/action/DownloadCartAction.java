package com.grail.cart.action;

import com.grail.cart.GrailCartBean;
import com.grail.cart.util.AttachmentUtil;
import com.grail.cart.util.CartUtil;
import com.grail.util.BATConstants;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.util.XMLUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * DownloadCartAction is responsible for downloading the cart items as ZIP file.
 * The return type of this action is an Stream object i.e. InputStream
 *
 * User: vivek
 * Date: Feb 17, 2012
 * Time: 3:08:25 PM
 *
 */
public class DownloadCartAction extends JiveActionSupport {

    private static final Logger log = Logger.getLogger(DownloadCartAction.class);
    private String size = JiveGlobals.getJiveProperty("jive.pdf.paper.size", XMLUtils.PaperSize.ANSI_A.toString());
    private InputStream zipStream;
    private String zipFilename;
    //The stream length in bytes. This should help us (the browser displays a progress bar).
    private String zipFileLength;

    private AttachmentUtil attachmentUtil;

    public void setAttachmentUtil(AttachmentUtil attachmentUtil) {
        this.attachmentUtil = attachmentUtil;
    }

    public InputStream getZipStream() {
        return zipStream;
    }

    public String getZipFilename() {
        return zipFilename;
    }

    public String getZipFileLength() {
        return zipFileLength;
    }

    public void setZipFileLength(String zipFileLength) {
        this.zipFileLength = zipFileLength;
    }

    @Override
    public String execute() {
        log.info("\n\n\n  Downloading.....please wait.......");
        getRequest().getSession().removeAttribute("completed");
        //Filter the cart to get only items which are eligible for download.
        //Write the items to file system
        //Zip the items written
        //Check if the stream is not null and then return it ;)

        List<GrailCartBean> items = CartUtil.getCartItemsForDownload(getUser());
        if(items.size() > 0)
        {
           // AttachmentUtil attachHelp = new AttachmentUtil();
            items = this.attachmentUtil.writeCartItemsToFS(getUser(),items);
            log.info(" \n\n\n  Writing the cart to file system ..... \n\n");
            //if no exceptions then proceed with generation of the ZIP
            try {
                log.info("\n\n\n  Generating ZIP file, please wait patiently....... \n\n");
                byte[] zip = this.attachmentUtil.getCartAsZip(String.valueOf(getUser().getID()),items) ;
                log.info("Received a zip byte stream");
                if( zip != null )
                {
                    zipStream = new ByteArrayInputStream(zip);
                    log.info(" ZipStream  Successful !!! ");
                    zipFileLength = String.valueOf(zip.length);
                    log.info("Zip file size  :::   "+zipFileLength+" bytes");
                    this.zipFilename = JiveGlobals.getJiveProperty(BATConstants.GRAIL_DOWNLOAD_ZIP_NAME_KEY,"download.zip");
                }
                else if (zipStream == null)
                {
                    log.error(" ZIP Stream NULL  - IOException occurred while generating the ZIP for the User    "+getUser().getEmail());
                    log.info(" ZIP Stream NULL  - IOException occurred while generating the ZIP for the User    "+getUser().getEmail());
                    return INPUT;
                }
            } catch (IOException e) {
                log.error(" IOException occurred while generating the ZIP for the User    "+getUser().getEmail());
                log.info(" IOException occurred while generating the ZIP for the User    ", e);
                e.printStackTrace();
                return INPUT;
            }
        } else {
                return INPUT;
        }
        // Flush the user temp directory created to generate the PDF file for cart download

        File userTmpDirectory = new File(this.attachmentUtil.getUserDir(String.valueOf(getUser().getID())));
        try {
            FileUtils.deleteDirectory(userTmpDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Session is set when the download process is completed and used for removing the spinner from the UI.

        getRequest().getSession().setAttribute("completed", "YES");
        return SUCCESS;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

}
