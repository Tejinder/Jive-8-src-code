package com.grail.util;

import javax.servlet.http.HttpServletRequest;

import com.jivesoftware.community.JiveGlobals;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 7/3/14
 * Time: 6:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class URLUtils {

    public static String getBaseURL(final HttpServletRequest request) {
        URL requestUrl;
        try {
            requestUrl = new URL(request.getRequestURL().toString());
            String portString = requestUrl.getPort() == -1 ? "" : ":" + requestUrl.getPort();
            return requestUrl.getProtocol() + "://" + requestUrl.getHost() + portString + request.getContextPath();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public static String getRSATokenBaseURL(final HttpServletRequest request) {
        String baseUrl = getBaseURL(request);
        if(baseUrl.contains("synchro_rkp"))
        {
        	//baseUrl = JiveGlobals.getJiveProperty("rsaTokenURL","") + baseUrl;
        	baseUrl = JiveGlobals.getJiveProperty("rsaTokenURL","");
        }
        return baseUrl;
    }
    public static String getSecondBaseURL(String baseUrl) {
        
        String secondBaseUrl="";
    	if(baseUrl.contains("synchro_rkp"))
        {
       		secondBaseUrl = JiveGlobals.getJiveProperty("jiveURL","");
        }
    	else
    	{
    		secondBaseUrl = JiveGlobals.getJiveProperty("rsaTokenURL","");
    	}
        return secondBaseUrl;
    }
}
