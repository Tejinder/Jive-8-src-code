package com.jivesoftware.community.action;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.grail.synchro.SynchroConstants;
//import com.grail.synchro.SynchroGlobal;
import com.grail.util.BATConstants;
import com.grail.util.BATGlobal;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.util.CookieUtils;
import com.jivesoftware.community.web.struts.JiveRefererInterceptor;

/**
 *
 */
public class DisclaimerAction extends JiveActionSupport {
    private static final long serialVersionUID = 1L;
    private  String username;
    private String password;
    
    private String synchroRedirectUrl;
    private String synchroRedirectUrlRetained;
    //    private static final String RKP_ACCEPT_ACTION = "rkpAcceptAction";
//    private static final String SYNCHRO_ACCEPT_ACTION = "synchroAcceptAction";
//    private static final String GRAIL_ACCEPT_ACTION = "grailAcceptAction";
//    private static final String KANTAR_ACCEPT_ACTION = "kantarAcceptAction";
//    private static final String KANTAR_REPORT_ACCEPT_ACTION = "kantarReportAcceptAction";
    private String ACCEPT_ACTION = "acceptAction";
    private String DECLINE_ACTION = "declineAction";
    private String PORTAL_SELECTION_ERROR = "portalSelectionError";
    private boolean accept = false;
    private boolean decline = false;
    private String type = "";

    public static String DEBUG = "off";

    private String acceptURL;

    public String getAcceptURL() {
        return acceptURL;
    }

    public void setAcceptURL(String acceptURL) {
        this.acceptURL = acceptURL;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public boolean isDecline() {
        return decline;
    }

    public void setDecline(boolean decline) {
        this.decline = decline;
    }

    public String execute()
    {
        System.out.println("Inside Disclaimer Action ---"+ request.getQueryString());
        System.out.println("Inside Disclaimer Action Request URI---"+ request.getRequestURI());
        System.out.println("Inside Disclaimer Action Request URL---"+ request.getParameter("url"));
    	HttpSession session = getRequest().getSession();
        session.setAttribute("username", username);

        String qs = request.getQueryString();
        String requestURI = request.getRequestURI();
        
        synchroRedirectUrl =  request.getParameter("url");
        
        // If clicks on "I accept"
        if(accept) {
            DEBUG = "on";
            session.setAttribute(BATConstants.BAT_DISCLAIMER_ACCEPTED, true);
            System.out.println("Inside Disclaimer Action BAT_DISCLAIMER_ACCEPTED ---"+ session.getAttribute(BATConstants.BAT_DISCLAIMER_ACCEPTED));
//            if(session.getAttribute(BATConstants.GRAIL_REFERRER_URL) != null
//                    && !session.getAttribute(BATConstants.GRAIL_REFERRER_URL).toString().equals("")) {
//                acceptURL = session.getAttribute(BATConstants.GRAIL_REFERRER_URL).toString();
//            } else {
//                acceptURL = "/portal-options.jspa";
//            }
           
            if(StringUtils.isNotBlank(synchroRedirectUrlRetained))
            {
            	acceptURL = synchroRedirectUrlRetained;
            }
            else
            {
            	acceptURL = "/portal-options.jspa";
            }
            //acceptURL = "/portal-options.jspa";
            return ACCEPT_ACTION;
        }

        // If clicks on "I decline"
        if(decline) {
            session.removeAttribute(BATConstants.BAT_DISCLAIMER_ACCEPTED);
            return DECLINE_ACTION;
        }
        return SUCCESS;
    }

    public String input() {
        HttpSession session = getRequest().getSession();

//        SynchroGlobal.getAppProperties().remove(BATConstants.GRAIL_PORTAL_TYPE);
//        session.removeAttribute(BATConstants.GRAIL_PORTAL_TYPE);

        return INPUT;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public void printDetails(HttpServletRequest request) {
        HttpSession session = request.getSession();
        //Enumeration<String> e=nul
        Enumeration<String> e =request.getAttributeNames();
        printNameValues("Request --Attribute Names ",e, request);

        Enumeration<String> e1 = request.getParameterNames();
        printNameValues("Request --Parameter Names ",e1, request);

        Enumeration<String> e2= session.getAttributeNames();
        printNameValues("\n\n\nSession --Attribute Names ",e2, session);
    }

    private void  printNameValues(String caption, Enumeration <String> e, HttpServletRequest request) {
        DisclaimerAction.printOut(caption+"\n\n\n");
        while(e.hasMoreElements())
        {
            String elem = e.nextElement();
            if(caption.indexOf("Attribute") != -1)
                DisclaimerAction.printOut("name ="+elem + " value ="+request.getAttribute(elem));
            else
                DisclaimerAction.printOut("name ="+elem + " value ="+request.getParameter(elem));
        }
    }

    private void  printNameValues(String caption, Enumeration <String> e, HttpSession session) {
        DisclaimerAction.printOut(caption+"\n\n\n");
        while(e.hasMoreElements())
        {
            String elem = e.nextElement();
            DisclaimerAction.printOut("name ="+elem + " value ="+session.getAttribute(elem));

        }
    }

    public static void printOut(String str)
    {
        if (DEBUG.equals("on"))
            System.out.println(str);
    }

	public String getSynchroRedirectUrl() {
		return synchroRedirectUrl;
	}

	public void setSynchroRedirectUrl(String synchroRedirectUrl) {
		this.synchroRedirectUrl = synchroRedirectUrl;
	}

	public String getSynchroRedirectUrlRetained() {
		return synchroRedirectUrlRetained;
	}

	public void setSynchroRedirectUrlRetained(String synchroRedirectUrlRetained) {
		this.synchroRedirectUrlRetained = synchroRedirectUrlRetained;
	}





}
