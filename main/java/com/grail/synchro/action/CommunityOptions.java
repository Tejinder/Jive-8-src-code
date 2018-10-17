package com.grail.synchro.action;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.manager.PermissionManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.util.BATConstants;
import com.grail.util.PortalPermissionHelper;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.CookieUtils;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * An action which lists the RKP & Synchro Options. All users have access to RKP.
 * However, w.r.t Synchro the access is controlled based on Users who are part of Jive User Group - Synchro
 * @author: vivek
 * @since: 1.0
 */
public class CommunityOptions extends JiveActionSupport {

    private static final Logger LOG = Logger.getLogger(CommunityOptions.class);

    private PermissionManager permissionManager;
    protected String successURL;
    private boolean synchroEligible = false;
    private boolean hasAccessToSynchro = false;
    private boolean hasAccessToRkp = false;

    private boolean hasAccessToGrail = false;
    private boolean hasAccessToKantar = false;
    private boolean hasAccessToDocumentRepository = false;
    private boolean hasAccessToOracleDocuments = false;
    private boolean hasAccessToOSPOracle = false;
    private boolean hasAccessToOSPShare = false;



    public String input(){

        if(request.getParameter("unauthorized") != null && Boolean.parseBoolean(request.getParameter("unauthorized"))) {
//            String type = request.getParameter("type");
//            if(type != null) {
//                if(type.equals("synchro")) {
//                    //addActionError("You do not have permission to access Research Knowledge Portal.");
//                } else if(type.equals("rkp")) {
//                    //addActionError("You do not have permission to access RKP.");
//                }
//            } else {
//                //addActionError("You are not authorized to access portal.");
//            }
            return UNAUTHORIZED;
        }
        final User jiveUser = getUser();
        if(jiveUser != null) {
            synchroEligible = this.permissionManager.isSynchroUser(jiveUser);
            LOG.info("Is User - "+jiveUser.getUsername() +" - eligible for Synchro?  "+synchroEligible);
        }

        if(SynchroPermHelper.isSynchroUser(jiveUser)
                && (!SynchroPermHelper.isExternalAgencyUser(jiveUser))
                && (!SynchroPermHelper.isCommunicationAgencyUser(jiveUser))
                ) {
            hasAccessToGrail = true;
            hasAccessToKantar = true;
//            hasAccessToKantarReport = true;
            hasAccessToOracleDocuments = true;
        } else {
            hasAccessToGrail = false;
            hasAccessToOracleDocuments = false;
            if(SynchroPermHelper.isKantarAgencyUser(jiveUser)) {
                hasAccessToKantar = true;
            } else {
                hasAccessToKantar = false;
            }
//            if(SynchroPermHelper.isDocumentRepositoryAgencyUser(jiveUser)) {
//                hasAccessToKantarReport = true;
//            } else {
//                hasAccessToKantarReport = false;
//            }
        }

        if(SynchroPermHelper.canAccessDocumentRepositoryPortal(getUser())) {
            hasAccessToDocumentRepository = true;
        } else {
            hasAccessToDocumentRepository = false;
        }

        if(SynchroPermHelper.isOSPOracleUser(getUser())) {
        	hasAccessToOSPOracle = true;
        } else {
        	hasAccessToOSPOracle = false;
        }
        
        if(SynchroPermHelper.isOSPShareUser(getUser())) {
        	hasAccessToOSPShare = true;
        } else {
        	hasAccessToOSPShare = false;
        }
        
        HttpSession session = getRequest().getSession();
        session.removeAttribute(BATConstants.GRAIL_PORTAL_TYPE);

        //DELETES REMEMBER ME PORTAL COOKIE
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        Cookie rememberMePortalCookie = CookieUtils.getCookie(httpRequest, SynchroConstants.REMEMBER_ME_PORTAL_COOKIE);
        if(rememberMePortalCookie!=null)
            CookieUtils.deleteCookie(httpRequest, httpResponse, rememberMePortalCookie);

        this.hasAccessToSynchro = PortalPermissionHelper.hasSynchroAccess(getUser());
        this.hasAccessToRkp = PortalPermissionHelper.hasRKPAccess(getUser()); 
        
        //TODO Remove this as and when we migrate GRAIL, KANTAR, ORACLE DOCUMENTS etc to Jive 8
       /* hasAccessToRkp= false;
        hasAccessToDocumentRepository = false;
        hasAccessToKantar = false;
        hasAccessToGrail = false;
        hasAccessToOracleDocuments = false;
        */
        return INPUT;
    }



    public String getSuccessURL() {
        return successURL;
    }

    public void setSuccessURL(final String successURL) {
        this.successURL = successURL;
    }

    public boolean isSynchroEligible() {
        return synchroEligible;
    }

    public void setSynchroEligible(final boolean synchroEligible) {
        this.synchroEligible = synchroEligible;
    }

    public boolean isHasAccessToSynchro() {
        return hasAccessToSynchro;
    }

    public void setHasAccessToSynchro(boolean hasAccessToSynchro) {
        this.hasAccessToSynchro = hasAccessToSynchro;
    }

    public boolean isHasAccessToRkp() {
        return hasAccessToRkp;
    }

    public void setHasAccessToRkp(boolean hasAccessToRkp) {
        this.hasAccessToRkp = hasAccessToRkp;
    }

    @Required
    public void setPermissionManager(final PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public boolean isHasAccessToGrail() {
        return hasAccessToGrail;
    }

    public void setHasAccessToGrail(boolean hasAccessToGrail) {
        this.hasAccessToGrail = hasAccessToGrail;
    }

    public boolean isHasAccessToKantar() {
        return hasAccessToKantar;
    }

    public void setHasAccessToKantar(boolean hasAccessToKantar) {
        this.hasAccessToKantar = hasAccessToKantar;
    }

    public boolean isHasAccessToDocumentRepository() {
        return hasAccessToDocumentRepository;
    }

    public void setHasAccessToDocumentRepository(boolean hasAccessToDocumentRepository) {
        this.hasAccessToDocumentRepository = hasAccessToDocumentRepository;
    }

    public boolean isHasAccessToOracleDocuments() {
        return hasAccessToOracleDocuments;
    }

    public void setHasAccessToOracleDocuments(boolean hasAccessToOracleDocuments) {
        this.hasAccessToOracleDocuments = hasAccessToOracleDocuments;
    }



	public boolean isHasAccessToOSPOracle() {
		return hasAccessToOSPOracle;
	}



	public void setHasAccessToOSPOracle(boolean hasAccessToOSPOracle) {
		this.hasAccessToOSPOracle = hasAccessToOSPOracle;
	}



	public boolean isHasAccessToOSPShare() {
		return hasAccessToOSPShare;
	}



	public void setHasAccessToOSPShare(boolean hasAccessToOSPShare) {
		this.hasAccessToOSPShare = hasAccessToOSPShare;
	}
}
