package com.grail.synchro.action;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.docx4j.openpackaging.exceptions.Docx4JException;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.jivesoftware.community.action.JiveActionSupport;


/**
 * @author: kanwarg
 * @since: 1.0
 * PDFGenerateAction generates PDF from the screenshot taken by Canvas2HTML jQuery plugin.
 * Action does following:
 * - Reads Byte data into iText Objects
 * - Creates PDF Templates for cropping image on more than one pages of PDF documents
 * - Scaling of the inserted images 
 * - Generate loader cookie used in FTL files for removing loading overlay
 */
public class PDFGenerateAction extends JiveActionSupport{

    private static final Logger LOG = Logger.getLogger(PDFGenerateAction.class);
    private String pdfImageDataURL;
    private String projectId;
    private String pdfFileName;
    private String redirectURL;
    private String token;
   	private String tokenCookie;
   	private final float PDF_DOCUMENT_HEIGHT = 1500;
   	private final float PDF_DOCUMENT_WIDTH = 1200;
   	private static final String DEFAULT_PDF_FILENAME = "PIBPDF";
    public String execute(){
    	
    	//Incase no PDF file name is provided
    	if(StringUtils.isBlank(pdfFileName))
    	{
    		pdfFileName = DEFAULT_PDF_FILENAME;
    	}
    	
    	try {
    	    
    		//Get Byte array from the screenshot captured by Canvas2HTML Plugin
    	    pdfImageDataURL = pdfImageDataURL.substring("data:image/png;base64,".length());
    	    
    	 //   LOG.error("pdfImageDataURL-->"+ pdfImageDataURL);
    	    
    		byte[] btDataFile = new sun.misc.BASE64Decoder().decodeBuffer(pdfImageDataURL);
    		
    		LOG.error("btDataFile length -->"+ btDataFile.length);
    		
    		//Set Response type as PDF attachment, auto downloaded in browser 
    		//response.setContentType("application/pdf");
   		 	//response.setContentLength(btDataFile.length);
   		 	//response.addHeader("Content-Disposition", "attachment; filename="+pdfFileName+".pdf");
   		 	
   		 	//Generate loader cookie user is FTL files for removing loading overlay
            //generateTokenCookie();  
            
            //Initiates iText document Object for PDF
            Document document = new Document();
            
            //Get iText PDFWriter for the document and PDF type output stream 
           // PdfWriter writer
             // = PdfWriter.getInstance(document, response.getOutputStream());
            
            String root = ServletActionContext.getServletContext().getRealPath("/"); 
        	
        	File path = new File(root + "/pdfexport");

            if (!path.exists()) {
                path.mkdirs();
            }
           // File generated = new File(root + "/PIBDoc.docx");
            File generated = new File(path + File.separator  +pdfFileName+".pdf");
            
            PdfWriter writer =  PdfWriter.getInstance(document, new FileOutputStream(generated));
            
            //Open the iText PDF document Object
            document.open();
            
            //Reads Byte array and generates iText image obj 
            Image img = Image.getInstance(btDataFile);
            
            //Get image height and width
            float imageWidth = img.getScaledWidth();
            float imageHeight = img.getScaledHeight();
            
        	//Calculates number of pages required to be generated in resultant PDF output 
        	double pages = (imageHeight/PDF_DOCUMENT_HEIGHT);        	
        	int pageCount = (int)pages + 1;
        	
        	//Insert cropped image one by one in all the PDF document pages
        	for(int i=0; i<pageCount; i++)
        	{
        		//Skip creation of new page when image is inserted in first Page of the doc 
        		if(i>0)
        		{
        			//Creates new PDF document page
        			document.newPage();
        		}

        		//Generates PDF template with max width and height
        		PdfTemplate t = writer.getDirectContent().createTemplate(PDF_DOCUMENT_WIDTH, PDF_DOCUMENT_HEIGHT);
        		
        		//Draw rectangle of specific size in the PDF Template
                t.rectangle(0, 0, PDF_DOCUMENT_WIDTH, PDF_DOCUMENT_HEIGHT);
                //Clip the rectangle in the PDF Template
                t.clip();
                t.newPath();
                
                //Add cropped image with specific X-Y coordinates
                t.addImage(img, imageWidth, 0, 0, imageHeight, 0, -(imageHeight - (i+1)*PDF_DOCUMENT_HEIGHT));
                
                //Get clipped image instance
                Image clipped = Image.getInstance(t);
                
                //Set image width scaling parameters for generated PDF document
                int indentation = 0;
                float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                        - document.rightMargin() - indentation) / img.getWidth()) * 100;
                
                LOG.error("scaler length -->"+ scaler);
                
                clipped.scalePercent(scaler);
                
                //Add clipped image to the document page
                document.add(clipped);
        	}
        
        	//Closes the document. otherwise file will show corrupted error
            document.close();
            
            
            try
            {
            	
            	response.setContentType("application/pdf");
       		 	response.addHeader("Content-Disposition", "attachment; filename="+pdfFileName+".pdf");
                
                FileInputStream fis = new FileInputStream(generated);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                try {
                    for (int readNum; (readNum = fis.read(buf)) != -1;) {
                        bos.write(buf, 0, readNum);
                    }
                    // System.out.println( buf.length);

                } catch (IOException ex) {
                }
                byte[] bytes = bos.toByteArray();
                response.getOutputStream().write(bytes);

            } catch (Exception e) {
                e.printStackTrace();
            }

           
        }
        catch (Exception e) {
        	e.printStackTrace();
            LOG.error("Error while generating PDF for " + pdfFileName +". Error  details: " + e.getMessage());
        }
    	
    	return null;
    }

	public String getPdfImageDataURL() {
		return pdfImageDataURL;
	}

	public void setPdfImageDataURL(String pdfImageDataURL) {
		this.pdfImageDataURL = pdfImageDataURL;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getPdfFileName() {
		return pdfFileName;
	}

	public void setPdfFileName(String pdfFileName) {
		this.pdfFileName = pdfFileName;
	}

	private void generateTokenCookie()
    {
    	if(token!=null && tokenCookie!=null)
    	{
    		Cookie cookie = new Cookie(tokenCookie, token);
    		cookie.setMaxAge(1000*60*10);    	
    		response.addCookie(cookie);
    	}
    }

	public String getRedirectURL() {
		return redirectURL;
	}

	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}
	public void setTokenCookie(String tokenCookie) {
   		this.tokenCookie = tokenCookie;
   	}

   	public void setToken(String token) {
   		this.token = token;
   	}
	
};