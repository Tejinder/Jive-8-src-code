package com.grail.action;

import com.grail.synchro.util.SynchroPermHelper;
import com.grail.util.GrailMakeRequestUtils;
import com.jivesoftware.community.action.JiveActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 6/27/14
 * Time: 2:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailHomeAction extends JiveActionSupport {

    @Override
    public String input() {
      /*  if((!SynchroPermHelper.isSynchroUser(getUser()))
                || SynchroPermHelper.isExternalAgencyUser(getUser())
                || SynchroPermHelper.isCommunicationAgencyUser(getUser())
                ) {
            return UNAUTHORIZED;
        }*/
        GrailMakeRequestUtils.processClicks(ServletActionContext.getServletContext(), 0, 0,1);
        return INPUT;
    }

    @Override
    public String execute() {
        GrailMakeRequestUtils.processClicks(ServletActionContext.getServletContext(), 0, 1,0);
        return SUCCESS;
    }
}
