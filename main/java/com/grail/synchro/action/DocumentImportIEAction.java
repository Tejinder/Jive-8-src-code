package com.grail.synchro.action;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.converter.WordToHtmlUtils;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.struts2.ServletActionContext;
import org.docx4j.convert.out.html.AbstractHtmlExporter;
import org.docx4j.convert.out.html.AbstractHtmlExporter.HtmlSettings;
import org.docx4j.convert.out.html.HtmlExporterNG2;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.util.SynchroLogUtils;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;

/**
 * PIB Document Import Utility
 */

public class DocumentImportIEAction extends JiveActionSupport {

    private final static Logger LOG = Logger.getLogger(DocumentImportIEAction.class);

    private File attachPIBTemplateIE;
    private String fileTypeIE;
    private String importDocConfirmOptIE;
	private InputStream importDocStatus;
    private static final String IMPORT_DOC_RESPONSE = "importDocResponse";
    private Map<String, String> pibFieldMap = new HashMap<String, String>(); 
    private PermissionManager permissionManager;
    private String projectName;
    private Long projectID;
    private Long userID;

    @Override
    public String execute() {
        return UNAUTHORIZED;
    }

   
    public String importDocument() throws UnsupportedEncodingException {
    	//Authentication layer
    	final User jiveUser = getUser();
        if(jiveUser != null) {
            // This will check whether the user has accepted the Disclaimer or not.            

            if(!getPermissionManager().isSynchroUser(jiveUser))
            {
                return UNAUTHORIZED;
            }
        }
        
        Map<String, Object> result = new HashMap<String, Object>();
        JSONObject out = new JSONObject();
            try {
            	if(fileTypeIE.equalsIgnoreCase("docx") || fileTypeIE.equalsIgnoreCase("doc"))
            	{
            		convertWordToHtml(attachPIBTemplateIE);
            	}
            	/*else if(fileType.equalsIgnoreCase("doc"))
            	{
            		importFromDocument();
            	}*/
            	else
            	{
            		  result.put("success", false);
                      result.put("message", "File type not recognized");            		
            	}
            	
                result.put("success", true);
                result.put("message", "Successfully imported");
                result.put("fieldMap", pibFieldMap);
            }  catch (UnauthorizedException e) {
                result.put("success", false);
                result.put("message", "Unauthorized");
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", "Unknown error while importing");
            }
        try {
            out.put("data", result);
            importDocStatus = new ByteArrayInputStream(out.toString().getBytes("utf-8"));
            
          //Audit Logs: Import Doc to PIB
            if(projectID!=null && userID !=null)
            {
            	String description = SynchroGlobal.LogProjectStage.PIB.getDescription() + "- " + getText("logger.project.import.word"); 
                SynchroLogUtils.addLog(SynchroGlobal.PortalType.SYNCHRO.getDescription(), SynchroGlobal.PageType.PROJECT.getId(), SynchroGlobal.Activity.DOWNLOAD.getId(), 
        								SynchroGlobal.LogProjectStage.PIB.getId(), description, projectName, 
        										projectID, userID);
            }
            
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage());
            throw new UnsupportedEncodingException(e.getMessage());
        }
        return IMPORT_DOC_RESPONSE;

    }
    
    public void convertWordToHtmlOLD(File docFile) {

    	boolean officeXmlFile = false;
    	try {
        	  FileInputStream finStream=new FileInputStream(docFile.getAbsolutePath()); // file input stream with docFile
        	  HWPFDocumentCore doc = WordToHtmlUtils.loadDoc(finStream);
        	  WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                      DocumentBuilderFactory.newInstance().newDocumentBuilder()
                              .newDocument());
              wordToHtmlConverter.processDocument(doc);
              Document htmlDocument = wordToHtmlConverter.getDocument();
              DOMSource domSource = new DOMSource(htmlDocument);
    	      StringWriter stringWriter = new StringWriter();
    	      Transformer transformer = TransformerFactory.newInstance()
    	              .newTransformer();
    	      transformer.setOutputProperty( OutputKeys.INDENT, "no" );
    	      transformer.setOutputProperty( OutputKeys.ENCODING, "utf-8" );
    	      transformer.setOutputProperty( OutputKeys.METHOD, "html" );
    	    //  transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
    	      transformer.transform(
    	              new DOMSource( wordToHtmlConverter.getDocument() ),
    	              new StreamResult( stringWriter ) );


    	      String html = stringWriter.toString();
        	    
        	  String htmlInline = convertHTMLtoInline(html);
        	    
        	  pibFieldMap.put("description", parseHtml(htmlInline, "Project Description" , "Business Question"));
        	  pibFieldMap.put("bizQuestion", parseHtml(htmlInline, "Business Question" , "Research Objectives\\(s\\)"));
        	  pibFieldMap.put("researchObjective", parseHtml(htmlInline, "Research Objectives\\(s\\)" , "Action Standard\\(s\\)"));
        	  pibFieldMap.put("actionStandard", parseHtml(htmlInline, "Action Standard\\(s\\)" , "Methodology Approach and Research Design"));
        	  pibFieldMap.put("researchDesign", parseHtml(htmlInline, "Methodology Approach and Research Design" , "Sample Profile \\(Research\\)"));
        	  pibFieldMap.put("sampleProfile", parseHtml(htmlInline, "Sample Profile \\(Research\\)" , "Stimulus Material"));
        	  pibFieldMap.put("stimulusMaterial", parseHtml(htmlInline, "Stimulus Material" , "Other Comments"));
        	  pibFieldMap.put("others", parseHtml(htmlInline, "Other Comments" , "Other Reporting Requirements"));
        	  pibFieldMap.put("otherReportingRequirements", parseHtml(htmlInline, "Other Reporting Requirements" , ""));
        	    
        } 
    	 catch(OfficeXmlFileException oe)
         {
         	officeXmlFile = true;
         }
    	catch(Exception e) {
            e.printStackTrace();
        }
    	
    	if(officeXmlFile)
    	{
    		try  
            {  
                 
               XWPFDocument doc = new XWPFDocument(new FileInputStream(docFile.getAbsolutePath()));
              // XWPFParagraph[] paras = doc.getParagraphs(); //This list will hold the paragraphs
               Iterator<XWPFParagraph> iterator = doc.getParagraphsIterator();
               while(iterator.hasNext())
               {
            	   XWPFParagraph p = iterator.next();
            	   if(p.getText().contains("Project Description :"))
                   {
                  	// System.out.println("Project Description ---->"+ p.getText().replace("Project Description :", ""));
                  	 pibFieldMap.put("description", p.getText().replace("Project Description :", ""));
                   }
                   if(p.getText().contains("Business Question :"))
                   {
                  	// System.out.println("Business Question ---->"+ p.getText().replace("Business Question :", ""));
                  	 pibFieldMap.put("bizQuestion", p.getText().replace("Business Question :", ""));
                   }
                   if(p.getText().contains("Research Objective(s) :"))
                   {
                  	// System.out.println("Research Objective(s) ---->"+ p.getText().replace("Research Objective(s) :", ""));
                  	 pibFieldMap.put("researchObjective", p.getText().replace("Research Objective(s) :", ""));
                   }
                   if(p.getText().contains("Action Standard(s) :"))
                   {
                  	 //System.out.println("Action Standard(s) ---->"+ p.getText().replace("Action Standard(s) :", ""));
                  	 pibFieldMap.put("actionStandard", p.getText().replace("Action Standard(s) :", ""));
                   }
                   if(p.getText().contains("Methodology Approach and Research Design :"))
                   {
                  	 //System.out.println("Methodology Approach and Research Design ---->"+ p.getText().replace("Methodology Approach and Research Design :", ""));
                  	 pibFieldMap.put("researchDesign", p.getText().replace("Methodology Approach and Research Design :", ""));
                   }
                   if(p.getText().contains("Sample Profile(Research) :"))
                   {
                  	// System.out.println("Sample Profile(Research) ---->"+ p.getText().replace("Sample Profile(Research) :", ""));
                  	 pibFieldMap.put("sampleProfile", p.getText().replace("Sample Profile(Research) :", ""));
                   }
                   if(p.getText().contains("Stimulus Material :"))
                   {
                  	 //System.out.println("Stimulus Material ---->"+ p.getText().replace("Stimulus Material :", ""));
                  	 pibFieldMap.put("stimulusMaterial", p.getText().replace("Stimulus Material :", ""));
                   }
                   if(p.getText().contains("Other Comments :"))
                   {
                  	// System.out.println("Other Comments ---->"+ p.getText().replace("Other Comments :", ""));
                  	 pibFieldMap.put("others", p.getText().replace("Other Comments :", ""));
                   }
                   if(p.getText().contains("Other Reporting Requirements :"))
                   {
                  	 //System.out.println("Other Reporting Requirements ---->"+ p.getText().replace("Other Reporting Requirements :", ""));
                  	 pibFieldMap.put("otherReportingRequirements", p.getText().replace("Other Reporting Requirements :", ""));   
                   }
               }
               
           //    XWPFWordExtractor ex = new XWPFWordExtractor(doc);  //To get the words
            //   String words = ""; //This will hold all the text
              	  
               
           /*    for(XWPFParagraph p : paras){  //For each paragraph we retrieved from the document
                     //words += p.getText();    //Add the text we retrieve to the words string  
                     //System.out.println("Para- Text ---->"+ p.getParagraphText());
                   //  System.out.println("Para- Text ---->"+ p.getText());
                     if(p.getText().contains("Project Description :"))
                     {
                    	// System.out.println("Project Description ---->"+ p.getText().replace("Project Description :", ""));
                    	 pibFieldMap.put("description", p.getText().replace("Project Description :", ""));
                     }
                     if(p.getText().contains("Business Question :"))
                     {
                    	// System.out.println("Business Question ---->"+ p.getText().replace("Business Question :", ""));
                    	 pibFieldMap.put("bizQuestion", p.getText().replace("Business Question :", ""));
                     }
                     if(p.getText().contains("Research Objective(s) :"))
                     {
                    	// System.out.println("Research Objective(s) ---->"+ p.getText().replace("Research Objective(s) :", ""));
                    	 pibFieldMap.put("researchObjective", p.getText().replace("Research Objective(s) :", ""));
                     }
                     if(p.getText().contains("Action Standard(s) :"))
                     {
                    	 //System.out.println("Action Standard(s) ---->"+ p.getText().replace("Action Standard(s) :", ""));
                    	 pibFieldMap.put("actionStandard", p.getText().replace("Action Standard(s) :", ""));
                     }
                     if(p.getText().contains("Methodology Approach and Research Design :"))
                     {
                    	 //System.out.println("Methodology Approach and Research Design ---->"+ p.getText().replace("Methodology Approach and Research Design :", ""));
                    	 pibFieldMap.put("researchDesign", p.getText().replace("Methodology Approach and Research Design :", ""));
                     }
                     if(p.getText().contains("Sample Profile(Research) :"))
                     {
                    	// System.out.println("Sample Profile(Research) ---->"+ p.getText().replace("Sample Profile(Research) :", ""));
                    	 pibFieldMap.put("sampleProfile", p.getText().replace("Sample Profile(Research) :", ""));
                     }
                     if(p.getText().contains("Stimulus Material :"))
                     {
                    	 //System.out.println("Stimulus Material ---->"+ p.getText().replace("Stimulus Material :", ""));
                    	 pibFieldMap.put("stimulusMaterial", p.getText().replace("Stimulus Material :", ""));
                     }
                     if(p.getText().contains("Other Comments :"))
                     {
                    	// System.out.println("Other Comments ---->"+ p.getText().replace("Other Comments :", ""));
                    	 pibFieldMap.put("others", p.getText().replace("Other Comments :", ""));
                     }
                     if(p.getText().contains("Other Reporting Requirements :"))
                     {
                    	 //System.out.println("Other Reporting Requirements ---->"+ p.getText().replace("Other Reporting Requirements :", ""));
                    	 pibFieldMap.put("otherReportingRequirements", p.getText().replace("Other Reporting Requirements :", ""));   
                     }
                   }
                  // System.out.println("Para- SIZE ---->"+ paras.size());

                */
                   }
                  catch (IOException e)
               {System.out.println(e);}
    	}
    }  

    public void convertWordToHtml(File docFile) {

    	boolean officeXmlFile = false;
    	boolean isWordHTML = false;
    	try {
        	  FileInputStream finStream=new FileInputStream(docFile.getAbsolutePath()); // file input stream with docFile
        	  HWPFDocumentCore doc = WordToHtmlUtils.loadDoc(finStream);
        	  WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                      DocumentBuilderFactory.newInstance().newDocumentBuilder()
                              .newDocument());
              wordToHtmlConverter.processDocument(doc);
              Document htmlDocument = wordToHtmlConverter.getDocument();
              DOMSource domSource = new DOMSource(htmlDocument);
    	      StringWriter stringWriter = new StringWriter();
    	      Transformer transformer = TransformerFactory.newInstance()
    	              .newTransformer();
    	      transformer.setOutputProperty( OutputKeys.INDENT, "no" );
    	      transformer.setOutputProperty( OutputKeys.ENCODING, "utf-8" );
    	      transformer.setOutputProperty( OutputKeys.METHOD, "html" );
    	    //  transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
    	      transformer.transform(
    	              new DOMSource( wordToHtmlConverter.getDocument() ),
    	              new StreamResult( stringWriter ) );


    	      String html = stringWriter.toString();
        	    
        	  String htmlInline = convertHTMLtoInline(html);
        	    
        	  pibFieldMap.put("description", parseHtml(htmlInline, "Project Description" , "Business Question"));
        	  pibFieldMap.put("bizQuestion", parseHtml(htmlInline, "Business Question" , "Research Objectives\\(s\\)"));
        	  pibFieldMap.put("researchObjective", parseHtml(htmlInline, "Research Objectives\\(s\\)" , "Action Standard\\(s\\)"));
        	  pibFieldMap.put("actionStandard", parseHtml(htmlInline, "Action Standard\\(s\\)" , "Methodology Approach and Research Design"));
        	  pibFieldMap.put("researchDesign", parseHtml(htmlInline, "Methodology Approach and Research Design" , "Sample Profile \\(Research\\)"));
        	  pibFieldMap.put("sampleProfile", parseHtml(htmlInline, "Sample Profile \\(Research\\)" , "Stimulus Material"));
        	  pibFieldMap.put("stimulusMaterial", parseHtml(htmlInline, "Stimulus Material" , "Other Comments"));
        	  pibFieldMap.put("others", parseHtml(htmlInline, "Other Comments" , "Other Reporting Requirements"));
        	  pibFieldMap.put("otherReportingRequirements", parseHtml(htmlInline, "Other Reporting Requirements" , ""));
        	    
        } 
    	 catch(OfficeXmlFileException oe)
         {
         	officeXmlFile = true;
         }
    	catch(IOException io)
        {
        	io.printStackTrace();
        	isWordHTML = true;
        }
    	catch(Exception e) {
            e.printStackTrace();
        }
    	if(isWordHTML)
    	{
    		 BufferedReader br = null;
    	     String docxTemplate = "";
    	     try
    	     {
    	    	 br = new BufferedReader(new InputStreamReader(new FileInputStream(docFile.getAbsolutePath())));
    	         String temp;
    	         while( (temp = br.readLine()) != null)
    	                docxTemplate = docxTemplate + temp; 
    	         br.close();
	    	   
    	        String htmlInline = convertHTMLtoInline(docxTemplate);
  	      	    //System.out.println("Decription --> " + parseHtml(htmlInline, "Project Description" , "Business Question"));
  	      	    //System.out.println("bizQuestion --> " + parseHtml(htmlInline, "Business Question" , "Research Objectives\\(s\\)"));
  	      	    
	  	      	pibFieldMap.put("description", parseHtml(htmlInline, "Project Description" , "Business Question"));
	  	      	pibFieldMap.put("bizQuestion", parseHtml(htmlInline, "Business Question" , "Research Objectives\\(s\\)"));
	  	      	pibFieldMap.put("researchObjective", parseHtml(htmlInline, "Research Objectives\\(s\\)" , "Action Standard\\(s\\)"));
		      	pibFieldMap.put("actionStandard", parseHtml(htmlInline, "Action Standard\\(s\\)" , "Methodology Approach and Research Design"));
		      	pibFieldMap.put("researchDesign", parseHtml(htmlInline, "Methodology Approach and Research Design" , "Sample Profile \\(Research\\)"));
		      	pibFieldMap.put("sampleProfile", parseHtml(htmlInline, "Sample Profile \\(Research\\)" , "Stimulus Material"));
		      	pibFieldMap.put("stimulusMaterial", parseHtml(htmlInline, "Stimulus Material" , "Other Comments"));
		      	pibFieldMap.put("others", parseHtml(htmlInline, "Other Comments" , "Other Reporting Requirements"));
		      	pibFieldMap.put("otherReportingRequirements", parseHtml(htmlInline, "Other Reporting Requirements" , ""));
    	     } 
    	     catch(Exception e) 
    	     {
    	        e.printStackTrace();
    	     }
    	}
    	if(officeXmlFile)
    	{
    		try  
            {  
                 
    			  
    			// 1) Load DOCX into WordprocessingMLPackage
    			InputStream is = new FileInputStream(new File(
    					docFile.getAbsolutePath()));
    			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
    					.load(is);

    			// 2) Prepare HTML settings
    			HtmlSettings htmlSettings = new HtmlSettings();

    			// 3) Convert WordprocessingMLPackage to HTML
    			String root = ServletActionContext.getServletContext().getRealPath("/"); 
	        	
	        	File path = new File(root + "/pibwordimport");

	            if (!path.exists()) {
	                path.mkdirs();

	            }
	            File generated = new File(path + File.separator + "PIBImport.html");
		        
    			
    			//File generated = new File("C:\\Documents and Settings\\tejinder.ghatore.ATHERIO\\Desktop\\HelloWorld1.html");
    			OutputStream out = new FileOutputStream(generated);
    			AbstractHtmlExporter exporter = new HtmlExporterNG2();
    			StreamResult result = new StreamResult(out);
    			exporter.html(wordMLPackage, result, htmlSettings);

    			

    			
    		//	XWPFDocument document = new XWPFDocument(new FileInputStream("C:\\Documents and Settings\\tejinder.ghatore.ATHERIO\\Desktop\\test.docx"));
    			   
    		  //      XHTMLOptions options = XHTMLOptions.create();//;.indent( 4 );
    		       
    		        
    		        
    		        
    		       // OutputStream out = new FileOutputStream(generated);
    		//        OutputStream out = new FileOutputStream(generated);
    		  //      XHTMLConverter.getInstance().convert( document, out, options );
    		        //String html = out.toString();
    		       // System.out.println("html ---"+ html);
    		        StringBuffer html = new StringBuffer();
    		        
    		        BufferedReader br = null;
    		        
    				try {
    		 
    					String sCurrentLine;
    		 
    					br = new BufferedReader(new FileReader(generated));
    		 
    					while ((sCurrentLine = br.readLine()) != null) {
    						html.append(sCurrentLine);
    					}
    					html = html.replace(html.indexOf("<!--"), html.indexOf("-->"), "");
    					//html = html.replaceAll("<!--", "");
    					//html = html.replaceAll("-->", "");
    					//html = html.replaceAll("/\\*", "");
    					//html = html.replaceAll("\\*/", "");
    					System.out.println("HTML --> " + html.toString());
    				} catch (IOException e) {
    					e.printStackTrace();
    				} finally {
    					try {
    						if (br != null)br.close();
    					} catch (IOException ex) {
    						ex.printStackTrace();
    					}
    				}
    		 
    		        System.out.println("html ---"+ html);
    	            //response.getOutputStream().write(bytes);
    		        
    		        String htmlInline = convertHTMLtoInline(html.toString());
            	    //String para[] = htmlInline.split("</p>");
            	    
            	    pibFieldMap.put("description", parseHtml(htmlInline, "Project Description" , "Business Question"));
            	    pibFieldMap.put("bizQuestion", parseHtml(htmlInline, "Business Question" , "Research Objectives\\(s\\)"));
            	    pibFieldMap.put("researchObjective", parseHtml(htmlInline, "Research Objectives\\(s\\)" , "Action Standard\\(s\\)"));
            	    pibFieldMap.put("actionStandard", parseHtml(htmlInline, "Action Standard\\(s\\)" , "Methodology Approach and Research Design"));
            	    pibFieldMap.put("researchDesign", parseHtml(htmlInline, "Methodology Approach and Research Design" , "Sample Profile \\(Research\\)"));
            	    pibFieldMap.put("sampleProfile", parseHtml(htmlInline, "Sample Profile \\(Research\\)" , "Stimulus Material"));
            	    pibFieldMap.put("stimulusMaterial", parseHtml(htmlInline, "Stimulus Material" , "Other Comments"));
            	    pibFieldMap.put("others", parseHtml(htmlInline, "Other Comments" , "Other Reporting Requirements"));
            	    pibFieldMap.put("otherReportingRequirements", parseHtml(htmlInline, "Other Reporting Requirements" , ""));
    		        
                   }
                  catch (IOException e)
               {System.out.println(e);}
    		 catch (Exception e)
             {System.out.println(e);}
    	}
    	
    }  

    private String parseHtml(String html, String starttag, String endtag)
    {
    	String nodeHtml = "";
    	Pattern p = Pattern.compile("<p(.*)"+starttag+":(\\s*)</span></p>");
    	Matcher m = p.matcher(html);
    	int offsetstart = -1;
    	int offsetend = -1;
    	if (m.find())
    	{
    		offsetstart = m.end();
    	}
    	
    	if(endtag!="")
    	{
    		p = Pattern.compile("<p(.*)"+endtag+":(\\s*)</span></p>");
        	m = p.matcher(html);
        	 
        	if (m.find())
        	{
        		offsetend = m.start();
        	}
    	}
    	else
    	{
    		offsetend = html.length();
    	}
    	
    	if(offsetstart>0 && offsetend >0 && offsetstart<offsetend)
    	{
    		nodeHtml = html.substring(offsetstart, offsetend);
    	}

    	try
    	{
    		nodeHtml = trimEndTags(nodeHtml);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	return nodeHtml;
    }
    
    
    
    private String convertHTMLtoInline(String html)
    {
    	 
    	  final String style = "style";
    	  org.jsoup.nodes.Document doc = Jsoup.parse(html);
          Elements els = doc.select(style);// to get all the style elements
          for (Element e : els) {
              String styleRules = e.getAllElements().get(0).data().replaceAll(
                      "\n", "").trim(), delims = "{}";
              StringTokenizer st = new StringTokenizer(styleRules, delims);
              while (st.countTokens() > 1) {
                  String selector = st.nextToken(), properties = st.nextToken();
                  Elements selectedElements = doc.select(selector);
                  for (Element selElem : selectedElements) {
                      String oldProperties = selElem.attr(style);
                      selElem.attr(style,
                              oldProperties.length() > 0 ? concatenateProperties(
                                      oldProperties, properties) : properties);
                      
                  }
              }              
              e.remove();
          }

          return doc.toString();
    }
    
    private static String concatenateProperties(String oldProp, String newProp) {
        oldProp = oldProp.trim();
        if (!newProp.endsWith(";"))
           newProp += ";";
        return newProp + oldProp; // The existing (old) properties should take precedence.
    }
    
private static String trimEndTags(String html)
{
	org.jsoup.nodes.Document doc = Jsoup.parse(html);
	for (Element element : doc.select("*")) {

        if (!element.hasText() && element.isBlock()) {
            element.remove();
        }
    }
	
	html = doc.body().html();
	html = html.replace("</body>", "");
	html = html.replace("</html>", "");
	return html;
}
    
public PermissionManager getPermissionManager() {
    if(permissionManager == null){
        permissionManager = JiveApplication.getContext().getSpringBean("permissionManager");
    }
    return permissionManager;
}

public InputStream getImportDocStatus() {
	return importDocStatus;
}

public void setImportDocStatus(InputStream importDocStatus) {
	this.importDocStatus = importDocStatus;
}

public Map<String, String> getPibFieldMap() {
	return pibFieldMap;
}

public void setPibFieldMap(Map<String, String> pibFieldMap) {
	this.pibFieldMap = pibFieldMap;
}


public File getAttachPIBTemplateIE() {
	return attachPIBTemplateIE;
}


public void setAttachPIBTemplateIE(File attachPIBTemplateIE) {
	this.attachPIBTemplateIE = attachPIBTemplateIE;
}


public String getFileTypeIE() {
	return fileTypeIE;
}


public void setFileTypeIE(String fileTypeIE) {
	this.fileTypeIE = fileTypeIE;
}


public String getImportDocConfirmOptIE() {
	return importDocConfirmOptIE;
}


public void setImportDocConfirmOptIE(String importDocConfirmOptIE) {
	this.importDocConfirmOptIE = importDocConfirmOptIE;
}


public String getProjectName() {
	return projectName;
}


public void setProjectName(String projectName) {
	this.projectName = projectName;
}


public Long getProjectID() {
	return projectID;
}


public void setProjectID(Long projectID) {
	this.projectID = projectID;
}

/*
public Long getUserID() {
	return new Long(userID+"");
}
*/

public void setUserID(Long userID) {
	this.userID = userID;
}


}
