package com.grail.kantar.action;

import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/6/15
 * Time: 6:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarReportTypesViewAction extends JiveActionSupport {

    private boolean canUploadDocument = false;

    @Override
    public String execute() {

        if(!SynchroPermHelper.canAccessDocumentRepositoryPortal(getUser())) {
            return UNAUTHORIZED;
        }

        canUploadDocument = (SynchroPermHelper.isDocumentRepositoryBATUser(getUser())
                || SynchroPermHelper.isDocumentRepositoryAgencyUser(getUser()));


        return SUCCESS;
    }

    public boolean isCanUploadDocument() {
        return canUploadDocument;
    }

    public void setCanUploadDocument(boolean canUploadDocument) {
        this.canUploadDocument = canUploadDocument;
    }
}
