package com.grail.synchro.action;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.InvitedUser;
import com.grail.synchro.beans.InvitedUserResultFilter;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/9/15
 * Time: 3:53 PM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class InvitedUsersAction extends JiveActionSupport {


    private InputStream downloadStream;
    private final String DOWNLOAD_REPORT = "downloadReport";
    private String downloadFilename = "Synchro_Invited_Users.xls";

    private ProjectManager synchroProjectManager;
    private UserManager userManager;

    private String successURL;

    @Override
    public String input() {
        if(getUser() == null) {
            return UNAUTHENTICATED;
        }
        if(!SynchroPermHelper.isSynchroUser(getUser())) {
            return UNAUTHORIZED;
        }

        return INPUT;
    }


    @Override
    public String execute() {
        if(getUser() == null) {
            return UNAUTHENTICATED;
        }
        if(!SynchroPermHelper.isSynchroUser(getUser())) {
            return UNAUTHORIZED;
        }

        return SUCCESS;
    }



    public String downloadReport() throws IOException {
        if(getUser() == null) {
            return UNAUTHENTICATED;
        }

        if(!SynchroPermHelper.isSynchroUser(getUser())) {
            return UNAUTHORIZED;
        }

        Calendar calendar = Calendar.getInstance();
        downloadFilename = "Synchro_Invited_Users_" +
                calendar.get(Calendar.YEAR) +
                "-" + ((calendar.get(Calendar.MONTH) + 1) < 10?"0":"") + (calendar.get(Calendar.MONTH) + 1) +
                "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10?"0":"") + calendar.get(Calendar.DAY_OF_MONTH) +
                "_" + (calendar.get(Calendar.HOUR_OF_DAY) < 10?"0":"") + calendar.get(Calendar.HOUR_OF_DAY) +
                "." + (calendar.get(Calendar.MINUTE) < 10?"0":"") + calendar.get(Calendar.MINUTE) +
                "." + (calendar.get(Calendar.SECOND) < 10?"0":"") + calendar.get(Calendar.SECOND) +
                ".csv";

        StringBuilder report = new StringBuilder();

        StringBuilder header = new StringBuilder();
        header.append("User Requested").append(",");
        header.append("Requested by").append(",");
        header.append("Date of Request");

        report.append(header).append("\n");

        InvitedUserResultFilter filter = new InvitedUserResultFilter();
//        if(!(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin(getUser())
//                || SynchroPermHelper.isSynchroSuperUser(getUser()))) {
//             filter.setInvitedBy(getUser().getID());
//        }

        if(request.getParameter("keyword") != null && !request.getParameter("keyword").equals("")) {
            filter.setKeyword(request.getParameter("keyword"));
        }

        List<InvitedUser> invitedUsers = synchroProjectManager.getInvitedUsers(filter);
        for(InvitedUser invitedUser : invitedUsers) {
            StringBuilder data = new StringBuilder();
            if(invitedUser.getEmail() != null) {
                data.append("\"").append(invitedUser.getEmail()).append("\"").append(",");
            } else {
                data.append(" ").append(",");
            }

            if(invitedUser.getInvitedBy() != null && invitedUser.getInvitedBy() > 0) {
                try {
                    User user = getUserManager().getUser(invitedUser.getInvitedBy());
                    if(user != null) {
                        data.append("\"").append(user.getEmail()).append("\"").append(",");
                    } else {
                        data.append(" ").append(",");
                    }
                } catch (UserNotFoundException e) {
                    data.append(" ").append(",");
                }
            } else {
                data.append(" ").append(",");
            }


            if(invitedUser.getInvitedDate() != null) {
                SimpleDateFormat dtFormat = new SimpleDateFormat();
                dtFormat.applyPattern("dd/MM/yyyy");
                data.append("\"").append(dtFormat.format(invitedUser.getInvitedDate())).append("\"");
            } else {
                data.append(" ");
            }
            report.append(data).append("\n");
        }

        downloadStream = new ByteArrayInputStream(report.toString().getBytes("utf-8"));
        return DOWNLOAD_REPORT;
    }

    public InputStream getDownloadStream() {
        return downloadStream;
    }

    public void setDownloadStream(InputStream downloadStream) {
        this.downloadStream = downloadStream;
    }

    public String getDownloadFilename() {
        return downloadFilename;
    }

    public void setDownloadFilename(String downloadFilename) {
        this.downloadFilename = downloadFilename;
    }

    public ProjectManager getSynchroProjectManager() {
        return synchroProjectManager;
    }

    public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
        this.synchroProjectManager = synchroProjectManager;
    }

    public String getSuccessURL() {
        return successURL;
    }

    public void setSuccessURL(String successURL) {
        this.successURL = successURL;
    }

    public UserManager getUserManager() {
        if (userManager == null) {
            userManager = JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;
    }


}
