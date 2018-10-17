package com.grail.kantar.action;

import com.grail.kantar.util.KantarMakeRequestUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.util.GrailMakeRequestUtils;
import com.jivesoftware.community.action.JiveActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/21/14
 * Time: 6:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarHomeAction extends JiveActionSupport {
    @Override
    public String input() {
        if((!SynchroPermHelper.isSynchroUser(getUser()))
                || SynchroPermHelper.isExternalAgencyUser(getUser())
                || SynchroPermHelper.isCommunicationAgencyUser(getUser())
                ) {
            return UNAUTHORIZED;
        }
        KantarMakeRequestUtils.processClicks(ServletActionContext.getServletContext(), 0, 0, 1);
        return INPUT;
    }

    @Override
    public String execute() {
        KantarMakeRequestUtils.processClicks(ServletActionContext.getServletContext(), 0, 1, 0);
        return SUCCESS;
    }
}
