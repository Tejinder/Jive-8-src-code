package com.grail.synchro.rest;

import com.grail.synchro.beans.InvitedUser;
import com.grail.synchro.util.SynchroPermHelper;
import org.apache.log4j.Logger;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.beans.EmailNotificationDetailsBean;
import com.grail.synchro.manager.EmailNotificationManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.StageManager;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserTemplate;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailManager;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.community.mail.EmailMessage.EmailAddress;
import com.jivesoftware.community.mail.util.TemplateUtil;
import com.jivesoftware.util.LocaleUtils;
import com.jivesoftware.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * RESTful Action related to Invite User Track
 *
 * @author: Kanwar Grewal
 * @since: 1.0
 */
public class InviteUserUtil extends RemoteSupport  {

    private static final Logger LOG = Logger.getLogger(InviteUserUtil.class);

    private ProjectManager synchroProjectManager;
    private EmailManager emailManager;
    private UserManager userManager;
    private EmailNotificationManager emailNotificationManager;

    public EmailNotificationManager getEmailNotificationManager() {

        if (emailNotificationManager == null)
        {
            emailNotificationManager = JiveApplication.getContext().getSpringBean("emailNotificationManager");
        }
        return emailNotificationManager;

    }

    public ProjectManager getSynchroProjectManager() {
        if (synchroProjectManager == null)
        {
            synchroProjectManager = JiveApplication.getContext().getSpringBean("synchroProjectManager");
        }
        return synchroProjectManager;
    }

    public EmailManager getEmailManager() {
        if (emailManager == null)
        {
            emailManager = JiveApplication.getContext().getSpringBean("emailManager");
        }
        return emailManager;
    }

    public UserManager getUserManager() {
        if (userManager == null)
        {
            userManager = JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;
    }


    public Long addInvite(final String email) {
        if(StringUtils.isValidEmailAddress(email)) {
            Date date = new Date();
            return getSynchroProjectManager().addInvite(email, getUser(), date);
        } else {
            return 1L;
        }
    }

    public List<InvitedUser> getInvitedUsers(final boolean allUsers) {
        if(allUsers) {
            return getSynchroProjectManager().getInvitedUsers();
        } else {
            if(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin(getUser()) || SynchroPermHelper.isSynchroSuperUser(getUser())) {
                return getSynchroProjectManager().getInvitedUsers();
            } else {
                return getSynchroProjectManager().getInvitedUsers(getUser());
            }
        }
    }



    public void removeInvite(final String email)
    {
        if(StringUtils.isValidEmailAddress(email))
            getSynchroProjectManager().removeInvite(email);

    }

    public Long getInviteIdByEmail(final String email)
    {
        if(StringUtils.isValidEmailAddress(email))
            return getSynchroProjectManager().getInviteIdByEmail(email);
        else
            return 1L;
    }

    public Boolean sendNotification(Long senderID, String recipient, String subject, String messageBody, Long projectID, Integer stageId) {
        EmailMessage inviteMessage = new EmailMessage();
        User sender = null;
        try{
            if(senderID > 0)
            {
                sender = getUserManager().getUser(senderID);
                if(StringUtils.isValidEmailAddress(sender.getEmail()))
                {
                    inviteMessage.setSender(sender.getName(), sender.getEmail());
                }
            }

            if(StringUtils.isValidEmailAddress(recipient))
            {
                inviteMessage.addRecipient(new EmailAddress(recipient, recipient));
            }

            inviteMessage.setSubject(subject);
            inviteMessage.setHtmlBody(messageBody);

            getEmailManager().send(inviteMessage);

            //Email Notification TimeStamp Storage
            if(stageId!=null && projectID!=null)
            {
                EmailNotificationDetailsBean emailNotBean = new EmailNotificationDetailsBean();
                emailNotBean.setProjectID(projectID);
                emailNotBean.setStageID(stageId);
                emailNotBean.setEmailType(SynchroConstants.EMAIL_TYPE_NOTIFICATION);
                emailNotBean.setEmailDesc("Notification | New Stakeholder Requested");

                emailNotBean.setEmailSubject("Notification | New Stakeholder Requested");
                if(sender!=null && StringUtils.isValidEmailAddress(sender.getEmail()))
                {
                    emailNotBean.setEmailSender(sender.getEmail());
                }

                emailNotBean.setEmailRecipients(recipient);
                getEmailNotificationManager().saveDetails(emailNotBean);
            }


        } catch (com.jivesoftware.base.UserNotFoundException e) {
            // TODO Auto-generated catch block
            LOG.error("Error sending email notification to Project owner - User Not Found " + e.getMessage());
            return false;
        }
        return true;
    }

    public String validateEmail(final String email)
    {
        String error = "";
        if(email!=null)
        {
            if(StringUtils.isValidEmailAddress(email))
            {
                try{
                    UserTemplate userTemplate = new UserTemplate();
                    userTemplate.setEmail(email);
                    User user = getUserManager().getUser(userTemplate);
                    if(user != null && user.getID() > 0) {
                        error = "User email already exists.";
                    }
                } catch(Exception e){LOG.error("User not found with email " + email);
                }

                Long invitedUserId = null;
//                if(!(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin(getUser()) || SynchroPermHelper.isSynchroSuperUser(getUser()))) {
//                    invitedUserId = getSynchroProjectManager().getInvitedUser(email, getUser());
//                } else {
                invitedUserId = getSynchroProjectManager().getInvitedUser(email);
//                }
                if(invitedUserId != null && invitedUserId > 0) {
                    error = "User email already requested. Please check select from list.";
                }
            }
            else {
                error = "Please enter correct email format";
            }
        }
        return error;
    }


    public String validateEmailRegex(final String emails)
    {
        String result = "";
        if(!StringUtils.isNullOrEmpty(emails))
        {
            if(emails.indexOf(",")!=-1)
            {
                String emailList[] = emails.split(",");

                for(int i=0; i<emailList.length; i++)
                {
                    if(!StringUtils.isValidEmailAddress(emailList[i].trim()))
                        result = "Please enter correct email format";
                }
            }
            else
            {
                if(!StringUtils.isValidEmailAddress(emails.trim()))
                {
                    result = "Please enter correct email format";
                }
            }
        }
        else
        {
            result = "Please enter email";
        }


        return result;
    }


    public String getMessageSubject(Long user)
    {
        String subject = TemplateUtil.getTemplate("project.invite.user.subject", LocaleUtils.localeCodeToLocale("en"));
        return subject;
    }


    public String getMessageBody(Long user)
    {
        String body = TemplateUtil.getTemplate("project.invite.user.htmlBody", LocaleUtils.localeCodeToLocale("en"));
        return body;
    }

}
