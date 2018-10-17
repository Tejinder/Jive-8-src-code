package com.grail.filter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.util.PortalPermissionHelper;
import com.grail.util.URLUtils;
import com.jivesoftware.base.UnauthorizedException;
import com.jivesoftware.base.User;
import com.jivesoftware.base.admin.UIComponent;
import com.jivesoftware.base.admin.UIComponents;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.grail.synchro.SynchroConstants;
import com.grail.util.BATConstants;
import com.grail.util.BATGlobal;
import com.jivesoftware.base.aaa.JiveAuthentication;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.util.CookieUtils;
import com.jivesoftware.community.util.spring.MergeableCollection;
import com.jivesoftware.community.web.component.ActionLink;

/**
 * @author Bhaskar Avulapati
 * @modified Kanwar Grewal
 * @version 1.0
 */

public class GrailPageViewFilter implements Filter {

    private boolean doFilter = true;
    private MergeableCollection<ActionLink> satelliteManageLinks;
    private MergeableCollection<ActionLink> satelliteSettingsLinks;
    private static PermissionManager permissionManager;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //No Operation
    }

    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        doFilter = true;

        final HttpSession session = ((HttpServletRequest)request).getSession();

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        User user = ((JiveAuthentication) authentication).getUser();




        String requestURI = ((HttpServletRequest)request).getRequestURI();

        String portalType = null;

        final HttpServletRequest servletRequest = (HttpServletRequest) request;


        if(requestURI.contains("index.jspa")) {
            doFilter = false;
            redirectToPage(session, response, SynchroConstants.COMMUNITY_OPTIONS_ACTION_URL);
        }
        
        if(requestURI.contains("ospa")) {
            doFilter = false;
           // redirectToPage(session, response, "/osp!input.jspa");
            
            ((HttpServletResponse)response).sendRedirect("/osp!input.jspa");
        }

        if(requestURI.contains("/portal-options")) {
            session.removeAttribute(BATConstants.GRAIL_PORTAL_TYPE);
            SynchroGlobal.getAppProperties().remove(BATConstants.GRAIL_PORTAL_TYPE);
        }

        if(requestURI.contains("/synchro/login.jspa") || requestURI.contains("/grail/login.jspa")
                || requestURI.contains("/kantar/login.jspa") || requestURI.contains("/kantar-report/login.jspa")
                || requestURI.contains("/oracledocuments/login.jspa")) {

            redirectToLogin(session, response);
        }

        boolean hasRKPAccess = getPermissionManager().isRKPUser(user);
        boolean hasSynchroAccess = SynchroPermHelper.isSynchroUser(user);
        boolean hasGrailAccess = (SynchroPermHelper.isSynchroUser(user)
                && (!SynchroPermHelper.isExternalAgencyUser(user))
                && (!SynchroPermHelper.isCommunicationAgencyUser(user)));

        boolean hasKantarAccess = (SynchroPermHelper.isSynchroUser(user) || SynchroPermHelper.isKantarAgencyUser(user)
                && (!SynchroPermHelper.isExternalAgencyUser(user))
                && (!SynchroPermHelper.isCommunicationAgencyUser(user)));

        boolean hasKantarReportAccess = (SynchroPermHelper.isSynchroUser(user) || SynchroPermHelper.isDocumentRepositoryAgencyUser(user)
                && (!SynchroPermHelper.isExternalAgencyUser(user))
                && (!SynchroPermHelper.isCommunicationAgencyUser(user)));
        
        boolean hasOSPOracleAccess = SynchroPermHelper.canAccessOSPOraclePortal(user);
        
        boolean hasOSPShareAccess = SynchroPermHelper.canAccessOSPSharePortal(user);

        if(!isStaticResource(servletRequest) && !igonableURI(requestURI) && !user.isAnonymous() && doFilter) {
//            if(!(requestURI.contains("/portal-options") || requestURI.contains("/disclaimer.jspa"))) {
//                session.setAttribute(BATConstants.GRAIL_REFERRER_URL, ((HttpServletRequest) request).getRequestURI());
//            }

            if(user == null || user.isAnonymous()) {
                redirectToLogin(session, response);
            }

            if(session.getAttribute(BATConstants.BAT_DISCLAIMER_ACCEPTED) == null
                    || !Boolean.parseBoolean(session.getAttribute(BATConstants.BAT_DISCLAIMER_ACCEPTED).toString())) {
                
            	//TODO - CHech why the session is coming new on each request remove this paramater
            	doFilter = false;
            	//redirectToPage(session, response, "/disclaimer.jspa");
            	
            	String qs = ((HttpServletRequest)request).getQueryString();
            	String url = "/disclaimer.jspa?url="+requestURI;
            	if(qs!=null)
            	{
            		url = url + "?" +qs;
            	}
            	redirectToPage(session, response, url);
            
            	
            }
            if(!(requestURI.contains("/portal-options") || requestURI.contains("/disclaimer.jspa"))) {

                if(requestURI.contains("/synchro/")) {
                    if(hasSynchroAccess) {
                        portalType = BATGlobal.PortalType.SYNCHRO.toString();
                    } else {
                        doFilter = false;
                        redirectToPage(session, response, SynchroConstants.COMMUNITY_OPTIONS_ACTION_URL);
                    }
                }
                if(requestURI.contains("/new-synchro/")) {
                    if(hasSynchroAccess) {
                        portalType = BATGlobal.PortalType.SYNCHRO.toString();
                    } else {
                        doFilter = false;
                        redirectToPage(session, response, SynchroConstants.COMMUNITY_OPTIONS_ACTION_URL);
                    }
                } 
                else if(requestURI.contains("/grail/")) {
                    if(hasGrailAccess) {
                        portalType = BATGlobal.PortalType.GRAIL.toString();
                    } else {
                        doFilter = false;
                        redirectToPage(session, response, SynchroConstants.COMMUNITY_OPTIONS_ACTION_URL);
                    }
                } else if(requestURI.contains("/kantar/")) {
                    if(hasKantarAccess) {
                        portalType = BATGlobal.PortalType.KANTAR.toString();
                    } else {
                        doFilter = false;
                        redirectToPage(session, response, SynchroConstants.COMMUNITY_OPTIONS_ACTION_URL);
                    }
                } else if(requestURI.contains("/kantar-report/")) {
                    if(hasKantarReportAccess) {
                        portalType = BATGlobal.PortalType.KANTAR_REPORT.toString();
                    } else {
                        doFilter = false;
                        redirectToPage(session, response, SynchroConstants.COMMUNITY_OPTIONS_ACTION_URL);
                    }
                } else if(requestURI.contains("/oracledocuments/")) {
                    if(hasSynchroAccess) {
                        portalType = BATGlobal.PortalType.ORACLE_DOCUMENTS.toString();
                    } else {
                        doFilter = false;
                        redirectToPage(session, response, SynchroConstants.COMMUNITY_OPTIONS_ACTION_URL);
                    }
                } else if(requestURI.contains("/oracle/")) {
                    if(hasOSPOracleAccess) {
                        portalType = BATGlobal.PortalType.OSP_ORACLE.toString();
                    } else {
                        doFilter = false;
                        redirectToPage(session, response, SynchroConstants.COMMUNITY_OPTIONS_ACTION_URL);
                    }
                }
                else if(requestURI.contains("/share/")) {
                    if(hasOSPShareAccess) {
                        portalType = BATGlobal.PortalType.OSP_SHARE.toString();
                    } else {
                        doFilter = false;
                        redirectToPage(session, response, SynchroConstants.COMMUNITY_OPTIONS_ACTION_URL);
                    }
                }
                 else  {
                    if(hasRKPAccess) {
                        portalType = BATGlobal.PortalType.RKP.toString();
                    } else {
                        doFilter = false;
                        redirectToPage(session, response, SynchroConstants.COMMUNITY_OPTIONS_ACTION_URL);
                    }
                }
            }

            if(portalType != null) {
                SynchroGlobal.getAppProperties().put(BATConstants.GRAIL_PORTAL_TYPE, portalType);
                session.setAttribute(BATConstants.GRAIL_PORTAL_TYPE, portalType);
            } else {
                SynchroGlobal.getAppProperties().remove(BATConstants.GRAIL_PORTAL_TYPE);
                session.removeAttribute(BATConstants.GRAIL_PORTAL_TYPE);
            }
        }

        if(doFilter) {
            chain.doFilter(request, response);
        }
    }

    private void redirectToLogin(HttpSession session, ServletResponse response) throws IOException, ServletException {
        doFilter = false;
        redirectToPage(session, response, "/login.jspa");
    }

    private void redirectToPage(HttpSession session, ServletResponse response, String page)
            throws IOException, ServletException {

        StringBuilder url =  new StringBuilder();
        if(SynchroGlobal.getAppProperties().get(BATConstants.BAT_BASE_URL) != null
                && !SynchroGlobal.getAppProperties().get(BATConstants.BAT_BASE_URL).equals("")) {
            url.append(SynchroGlobal.getAppProperties().get(BATConstants.BAT_BASE_URL));
        }
        if(url.toString().equals("") || (url.length() > 0 && !("/").toString().equals(url.toString().charAt(url.length() - 1)))) {
            url.append("/");
        }
        if(page.charAt(0) == '/') {
            page = page.replaceFirst("/","");
        }

        url.append(page);
        doFilter = false;
        ((HttpServletResponse)response).sendRedirect(url.toString());
    }

    protected boolean isStaticResource(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString().toLowerCase();
        return requestUrl.endsWith(".css") || requestUrl.endsWith(".js")
                || requestUrl.endsWith(".png") || requestUrl.endsWith(".gif")
                || requestUrl.endsWith(".jpg") || requestUrl.endsWith(".jpeg")
                || requestUrl.indexOf("/resources/") > -1
                || requestUrl.endsWith(".html") || requestUrl.endsWith(".ico")
                || requestUrl.endsWith(".dwr");
    }

    protected boolean igonableURI(String requestURI) {

        if(satelliteSettingsLinks != null && !satelliteSettingsLinks.isEmpty()) {
            for(ActionLink link : satelliteSettingsLinks) {
                if(link.getUrl().contains(requestURI)) {
                    return true;
                }
            }
        }
        return requestURI.contains("image-picker")
                || requestURI.contains("/admin/")
                || requestURI.contains("terms-and-conditions")
                || requestURI.contains("change-password")
                || requestURI.contains("user-autocomplete-modal")
                || requestURI.contains("content-picker")
                || requestURI.contains("servlet")
                || requestURI.contains("user-preferences")
                || requestURI.contains("profile")
                || requestURI.contains("help")
                || requestURI.contains("connects-preferences")
                || requestURI.contains("edit-profile")
                || requestURI.contains("avatar-display")
                || requestURI.contains("/people")
                || requestURI.contains("/file/download")
                || requestURI.contains("/login.jspa")
                || requestURI.contains("/login")
                || requestURI.contains("login")
                || requestURI.contains("/logout.jspa")
                || requestURI.contains("/logout")
                || requestURI.contains("logout")
                || requestURI.contains("/resetPassword.jspa")
                || requestURI.contains("/resetPassword")
                || requestURI.contains("/emailPasswordToken.jspa")
//                || requestURI.contains("/portal-options.jspa")
                || requestURI.contains("force-restart.jspa")
                || requestURI.contains("/synchro/help.jspa")
                || requestURI.contains("/disclaimer.jspa")
                || requestURI.contains("/tinymce-test.jspa")
                || requestURI.contains("/grail/email-queries")
                || requestURI.contains("/inbox")
                || requestURI.contains("/news")

                || (!requestURI.contains(".jspa") && requestURI.matches(".*\\..*"));

    }



    public void setSatelliteManageLinks(MergeableCollection<ActionLink> satelliteManageLinks) {
        this.satelliteManageLinks = satelliteManageLinks;
    }

    public void setSatelliteSettingsLinks(MergeableCollection<ActionLink> satelliteSettingsLinks) {
        this.satelliteSettingsLinks = satelliteSettingsLinks;
    }



    public static PermissionManager getPermissionManager() {
        if(permissionManager == null) {
            return JiveApplication.getContext().getSpringBean("permissionManager");
        }
        return permissionManager;
    }
}
