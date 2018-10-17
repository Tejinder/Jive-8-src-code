package com.grail.community.web.struts.mapping;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.dispatcher.mapper.ActionMapping;

import com.jivesoftware.community.web.struts.mapping.AbstractURLMapping;
//import com.jivesoftware.community.web.struts.mapping.HttpStatusViewResultHelper;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;

/**
 * @author: vivek
 * @since: 1.0
 */
@SuppressWarnings({"unchecked"})
public class SynchroURLMapping extends AbstractURLMapping {

    public static final String SYNCHRO_URL_PREFIX = "/synchro";
   // private HttpStatusViewResultHelper statusViewResultHelper;

    private Container container;

    @Inject
    public void setContainer(Container container) {
        this.container = container;
       // statusViewResultHelper = new HttpStatusViewResultHelper(this.container);
    }



    @Override
    public void process(final String uri, final ActionMapping mapping) {
  System.out.println("INSIDE process SynchroURLMapping --");       
	   String[] uriElements = uri.split("/");
        Map params = mapping.getParams();
        if (null == params) {
            params = new HashMap();
        }
        //Supports the following URL mappings
        //1. synchro/synchro
        //2. synchro/dashboard
// mapping.setName("profile");
 
        
        if (uriElements.length == 3) {
        	 System.out.println("INSIDE process SynchroURLMapping length == 3 --");     
        	if(uriElements[2].equals("home")) {
                mapping.setName("home");
            } else if(uriElements[2].equals("dashboard")){
                mapping.setName("dashboard");
            }
			
        }
        // URL Mappings for viewing the Discussion Threads
		else if(uriElements.length >= 4 && uriElements[2].equals("viewDiscussionThread"))
        {
        	mapping.setNamespace(getNamespace());
        	mapping.setName("viewDiscussionThread");
        	params.put("thread", uriElements[3]);
        	// This check is for creating the back URL in case the discussion view is clicked through View All Discussions page.
        	if(uriElements.length>4)
        	{
        		params.put("backURL", uriElements[4]);
        	}
        
        }
        // URL Mappings for the Stage Documents
		else if(uriElements.length > 4 && uriElements[4].equals("docs"))
        {
			mapping.setNamespace(getNamespace());
			params.put("document", uriElements[5]);
			params.put("projectID", uriElements[2]);
    		params.put("activeTab", uriElements[3]);
			if(uriElements.length==7 && "version".equals(uriElements[6]))
			{
				mapping.setName("synchro-docVersion");
			}
			else if(uriElements.length==8 && "version".equals(uriElements[6]))
			{
				params.put("versionView", "versionView");
				params.put("version", uriElements[7]);
				mapping.setName("synchro-docView");
			}
			else if (uriElements.length==7 && uriElements[6].equals("diff")) {
                mapping.setName("synchro-docDiff");
            }
			else if (uriElements.length==7 && uriElements[6].equals("restore")) {
                mapping.setName("synchro-docRestore");
            } 
			else if (uriElements.length==7 && uriElements[6].equals("deleteVersion")) {
                mapping.setName("synchro-docVersionDelete");
            }
			else
			{
				params.put("versionView", "versionView");
				mapping.setName("synchro-docView");
			}
	    }
		// This else block is not required as all its conditions are met in the upper block
		/*else if(uriElements.length >= 4 && uriElements[2].equals("docs"))
        {
			mapping.setNamespace(getNamespace());
			params.put("document", uriElements[3]);
			if(uriElements.length==5 && "version".equals(uriElements[4]))
			{
				mapping.setName("synchro-docVersion");
	    		
			}
			else if (uriElements.length==6 && "version".equals(uriElements[4])) 
			{
                params.put("version", uriElements[5]);
            	mapping.setName("synchro-docView");
            }
			else if (uriElements.length==5 && uriElements[4].equals("diff")) {
                mapping.setName("synchro-docDiff");
            }
			else if (uriElements.length==5 && uriElements[4].equals("restore")) {
                mapping.setName("synchro-docRestore");
            } 
			else if (uriElements.length==5 && uriElements[4].equals("deleteVersion")) {
                mapping.setName("synchro-docVersionDelete");
            }
			else
			{
				mapping.setName("synchro-docView");
			}
        
        }*/
     // URL Mappings for the Stage Documents Edit
		else if(uriElements.length == 6 && uriElements[3].equals("doc-edit"))
		{
				mapping.setNamespace(getNamespace());
        		mapping.setName("doc-edit");
        		mapping.setMethod("input");
        		params.put("document", uriElements[4]);
        		params.put("projectID", uriElements[2]);
        		params.put("activeTab", uriElements[5]);
		}
     
		else if (uriElements.length >= 5) {
			 // URL Mappings for viewing the Discussion Forums for a project 
			if(uriElements.length == 5 && uriElements[2].equals("discussionForum")){
	        	//mapping.setName("place-content");
				mapping.setNamespace(getNamespace());
				mapping.setName("discussionForum");
				params.put("projectID", uriElements[3]);
	            params.put("containerType", "14");
	            params.put("container", uriElements[4]);
	            params.put("filterID", "contentstatus[published]~objecttype~objecttype[thread]");
	           // params.put("objecttype", "thread");
	            params.put("itemView","detail");
	            params.put("view", "content");
	        }
			// URL Mappings for editing the discussion
			if (uriElements[4].equals("synchro-editDiscussion")) {
            	mapping.setNamespace(getNamespace());
            	mapping.setName("synchro-editDiscussion");
                mapping.setMethod("default");
                params.put("message", uriElements[3]);
             // This check is for creating the back URL in case the discussion view is clicked through View All Discussions page.
            	if(uriElements.length>5)
            	{
            		params.put("backURL", uriElements[5]);
            	}
            }
			 /* if (uriElements[2].equals("project") && uriElements[3].equals("docs-edit")) {
        		mapping.setNamespace(getNamespace());
        		mapping.setName("synchro-doc-edit");
        		mapping.setMethod("input");
        		params.put("document", uriElements[4]);
            }
           // This mapping is for managing the Document Versions.
            if (uriElements[2].equals("docs") && uriElements[4].equals("version")) {
        		mapping.setNamespace(getNamespace());
        		mapping.setName("synchro-docVersion");
        		params.put("document", uriElements[3]);
            }
        	
            else if(uriElements[2].equals("project") && uriElements[3].equals("docs"))
        	{
        		mapping.setNamespace(getNamespace());
        		mapping.setName("synchro-doc-view");        		
        		params.put("document", uriElements[4]);        		
        	} */  
        }
		else if (uriElements.length == 4) {
        	if(uriElements[2].equals("people"))
        	{
        		String username = uriElements[3];
        		mapping.setName("profile");
        		params.put("username", username);
        		mapping.setNamespace("");
        	}
        }
        else {
            //statusViewResultHelper.notFoundMapping(mapping);
        }


        mapping.setParams(params);
    }

    public String getNamespace() {
        return SYNCHRO_URL_PREFIX;
    }
}
