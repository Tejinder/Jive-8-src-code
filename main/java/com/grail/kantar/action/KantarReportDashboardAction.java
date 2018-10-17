package com.grail.kantar.action;

import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/30/14
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class KantarReportDashboardAction  extends JiveActionSupport {
    private boolean kantarReportDashboardCatalogue = false;
    private boolean canUploadDocument = true;

    public boolean isKantarReportDashboardCatalogue() {
        return kantarReportDashboardCatalogue;
    }

    @Override
    public String execute() {
        if(!SynchroPermHelper.canAccessDocumentRepositoryPortal(getUser())) {
            return UNAUTHORIZED;
        }

        canUploadDocument = (SynchroPermHelper.isDocumentRepositoryBATUser(getUser())
                || SynchroPermHelper.isDocumentRepositoryAgencyUser(getUser()));

        kantarReportDashboardCatalogue = true;
        return SUCCESS;
    }

    public boolean isCanUploadDocument() {
        return canUploadDocument;
    }

    public void setCanUploadDocument(boolean canUploadDocument) {
        this.canUploadDocument = canUploadDocument;
    }
}
