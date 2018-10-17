package com.grail.synchro.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.esotericsoftware.minlog.Log;
import com.google.caja.util.Sets;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.EndMarketInvestmentDetail;
import com.grail.synchro.beans.FundingInvestment;
import com.grail.synchro.beans.PIBReporting;
import com.grail.synchro.beans.PIBStakeholderList;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectEvaluationInitiation;
import com.grail.synchro.beans.ProjectInitiation;
import com.grail.synchro.beans.ProjectSpecsInitiation;
import com.grail.synchro.beans.ProposalInitiation;
import com.grail.synchro.beans.ReportSummaryInitiation;
import com.grail.synchro.beans.ResearchPONum;
import com.grail.synchro.beans.SynchroStageToDoListBean;
import com.grail.synchro.dao.PIBDAO;
import com.grail.synchro.dao.ProjectDAO;
import com.grail.synchro.dao.ProjectSpecsDAO;
import com.grail.synchro.dao.ProposalDAO;
import com.grail.synchro.dao.ReportSummaryDAO;
import com.grail.synchro.dao.StageDAO;
import com.grail.synchro.manager.ProjectEvaluationManager;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.manager.ProposalManager;
import com.grail.synchro.manager.StageManager;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.BATGlobal;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailManager;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.community.mail.util.TemplateUtil;


/**
 * Implementation
 *
 * @author: Tejinder
 */
public class StageManagerImpl implements StageManager {

    private static final Logger LOG = Logger.getLogger(StageManagerImpl.class);

    private StageDAO stageDAO;

    private SynchroUtils synchroUtils;
    private UserManager userManager;
    private EmailManager emailManager;
    DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    private ProjectDAO projectDAO;
    private PIBDAO pibDAO;
    private ProposalDAO proposalDAO;
    private ProjectSpecsDAO projectSpecsDAO;
    private ReportSummaryDAO reportSummaryDAO;
    //TODO projectDAO needs to be replaced as we are using synchroProjectManager
    private ProjectManager synchroProjectManager;
    private ProjectEvaluationManager projectEvaluationManager;
    private ProposalManager proposalManager;
    private AttachmentManager attachmentManager;


    private SynchroUtils getSynchroUtils(){
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }

    /**
     * This method will fetch the disabled ToDoListTabs
     * @param stageId
     * @return
     */
    @Override
    public List<SynchroStageToDoListBean> getDisabledToDoListTabs(long stageId)
    {

        List<SynchroStageToDoListBean> synchroStageToDoBean = stageDAO.getToDoListSequence(stageId);
        String lastAction="";
        int removeIndex=-1;
        for(int i=0;i<synchroStageToDoBean.size();i++)
        {
            SynchroStageToDoListBean bean = synchroStageToDoBean.get(i);
            if(lastAction.equals(bean.getToDoAction()))
            {
                removeIndex = i;

            }
            lastAction=bean.getToDoAction();
        }
        if(removeIndex!=-1)
        {
            synchroStageToDoBean.remove(removeIndex);
        }
        return synchroStageToDoBean;

    }
    @Override
    public List<SynchroStageToDoListBean> getDisabledToDoListTabs(User currentUser, long projectId, long stageId, String projectName, long endMarketId,String baseUrl)
    {
        List<SynchroStageToDoListBean> stageToDoList = getToDoListTabs(currentUser, projectId, stageId, projectName, endMarketId, baseUrl);
        for(SynchroStageToDoListBean stageList:stageToDoList)
        {
            stageList.setActive(false);
        }
        return stageToDoList;
    }
    /**
     * This method will fetch all the properties related to ToDoList Tabs for a particular document.
     * @param document
     * @return
     */
    private static Map<String,String> getDocumentProperties(Document document)
    {
        Map<String,String> documentProperties = new LinkedHashMap <String, String>();
        for(String key:document.getProperties().keySet())
        {
            if(key.contains(SynchroConstants.DOCUMENT_TODO_LIST_NAME))
            {
                documentProperties.put(key, document.getProperties().get(key));
            }
        }

        return documentProperties;
    }


    public EmailMessage populateNotificationEmail(String recipients, String subject, String messageBody, String htmlProp, String subProp)
    {
        if(recipients!=null && recipients.length()>0)
        {
            EmailMessage message = new EmailMessage();
            if(recipients.contains(","))
            {
                String[] splitUser = recipients.split(",");
                for(int i=0;i<splitUser.length;i++)
                {
                    message.addRecipient(splitUser[i], splitUser[i]);
                }
            }
            else
            {
                message.addRecipient(recipients, recipients);
            }
            //TODO Implement generic Email Templates which will send the notification emails.
            if(htmlProp!=null && subProp!=null)
            {
                message.setHtmlBodyProperty(htmlProp);
                message.setSubjectProperty(subProp);
                message.setLocale(JiveGlobals.getLocale());
            }
            else
            {
                message.setSubject(subject);
                message.setHtmlBody(messageBody);
            }
            return message;
        }
        return null;

    }

    /**
     * This method will return the approvers who has clicked the Approve button in 
     * the To Do List for a particular Stage
     * @param document
     * @return
     */
    public Map<String, String> getProjectStageApprovers(Document document) {
        Map<String, String> approverMap = new LinkedHashMap<String, String>();
        String approvers = document.getProperties().get(
                SynchroConstants.DOCUMENT_TODO_LIST_APPROVERS);
        if (approvers != null && !approvers.isEmpty()) {
            if (approvers.contains(",")) {
                String[] splitApp = approvers.split(",");
                for (int i = 0; i < splitApp.length; i++) {
                    try {
                        approverMap.put(
                                userManager.getUser(new Long(splitApp[i].split("~")[0]))
                                        .getName(), splitApp[i].split("~")[1]);
                    } catch (UserNotFoundException e) {
                        LOG.error("User Not Found -->"
                                + splitApp[i].split("~")[0]);
                    }
                }
            } else {
                try {
                    approverMap.put(userManager
                            .getUser(new Long(approvers.split("~")[0])).getName(),
                            approvers.split("~")[1]);
                } catch (UserNotFoundException e) {
                    LOG.error("User Not Found -->" + approvers.split("~")[0]);
                }
            }
        }
        return approverMap;
    }

    /**
     * This method will return the approvers who has clicked the Approve Funding button in 
     * the To Do List for a particular Stage (Proposal Stage)
     * @param document
     * @return
     */
    public Map<String, String> getProjectBudgetApprovers(Document document) {
        Map<String, String> approverMap = new LinkedHashMap<String, String>();
        String approvers = document.getProperties().get(
                SynchroConstants.DOCUMENT_TODO_LIST_APPROVEFUNDING);
        //String approvers = document.getProperties().get(type);
        if (approvers != null && !approvers.isEmpty()) {
            if (approvers.contains(",")) {
                String[] splitApp = approvers.split(",");
                for (int i = 0; i < splitApp.length; i++) {
                    try {
                        approverMap.put(
                                userManager.getUser(new Long(splitApp[i].split("~")[0]))
                                        .getName(), splitApp[i].split("~")[1]);
                    } catch (UserNotFoundException e) {
                        LOG.error("User Not Found -->"
                                + splitApp[i].split("~")[0]);
                    }
                }
            } else {
                try {
                    approverMap.put(userManager
                            .getUser(new Long(approvers.split("~")[0])).getName(),
                            approvers.split("~")[1]);
                } catch (UserNotFoundException e) {
                    LOG.error("User Not Found -->" + approvers.split("~")[0]);
                }
            }
        }
        return approverMap;
    }
    /**
     * This method will return the budget approvers along with their PO numbers
     * the To Do List for a particular Stage (Proposal Stage)
     * @param document
     * @return
     */
    public List<ResearchPONum> getProjectBudgetApproversPONum(Document document) {
        //Map<String, String> approverMap = new LinkedHashMap<String, String>();
        List<ResearchPONum> researchPONum = new ArrayList<ResearchPONum>();
        String approvers = document.getProperties().get(SynchroConstants.DOCUMENT_TODO_LIST_APPROVERPO_NUMBER);

        if (approvers != null && !approvers.isEmpty()) {
            if (approvers.contains(",")) {
                String[] splitApp = approvers.split(",");
                for (int i = 0; i < splitApp.length; i++) {
                    //approverMap.put(splitApp[i].split("~")[0],splitApp[i].split("~").length>1?splitApp[i].split("~")[1]:"");
                    ResearchPONum poNum = new ResearchPONum();
                    poNum.setBudgetApproverId(splitApp[i].split("~")[0]);
                    poNum.setPoCheckBox(splitApp[i].split("~").length>2?true:false);
                    poNum.setPoNum(splitApp[i].split("~").length>1?splitApp[i].split("~")[1]:null);
                    researchPONum.add(poNum);
                }
            } else {

                //approverMap.put(approvers.split("~")[0],approvers.split("~").length>1?approvers.split("~")[1]:"");
                ResearchPONum poNum = new ResearchPONum();
                poNum.setBudgetApproverId(approvers.split("~")[0]);
                poNum.setPoCheckBox(approvers.split("~").length>2?true:false);
                poNum.setPoNum(approvers.split("~").length>1?approvers.split("~")[1]:null);
                researchPONum.add(poNum);
            }
        }
        return researchPONum;
    }
    /**
     * This method will return all the approvers for a particular Stage
     * @param document
     * @param stageId
     * @param projectId
     * @return
     */

    public Map<String, String> getStageApprovers(Long stageId, Project project) {
        Map<String, String> approverMap = new LinkedHashMap<String, String>();
        if(stageId==1)
        {
            try
            {
                User user = userManager.getUser(project.getProjectOwner());
                Long approvalDate = stageDAO.getApprovalDate(project.getProjectID(), stageId, project.getProjectOwner());
                if(approvalDate!=null && approvalDate>0)
                {
                    approverMap.put(user.getName(), df.format(new Date(approvalDate)));
                }
                else
                {
                    approverMap.put(user.getName(), null);
                }
            }
            catch(UserNotFoundException ue)
            {
                LOG.error("Could not found user --" + project.getProjectOwner());
            }
        }
        return approverMap;
        /*Map<String, String> approverMap = getProjectStageApprovers(document);
          Map<String, String> finalApproverMap = new LinkedHashMap<String, String>();
          Set<User> stageApprovers = new HashSet<User>();
          stageApprovers.addAll((HashSet<User>) getUsersByRole(projectId,
                  SynchroConstants.APPROVERS,stageId));

          // Only those Approvers to be shown which are selected as the Approvers for that particular stage. The Approvers who have clicked on the
          // approve button for stage and are removed from the Administration section as approvers for that Stage should not be displayed.
          for (User user : stageApprovers) {
              if (!approverMap.containsKey(user.getName())) {
                  finalApproverMap.put(user.getName(), null);
              }
              else
              {
                  finalApproverMap.put(user.getName(), approverMap.get(user.getName()));
              }

          }

          return finalApproverMap;*/
    }

    /**
     * This method will check whether role belongs to External Agency or not
     * @param role
     * @return
     */
    public boolean isExternalAgency(String role)
    {

        if(role.contains(SynchroConstants.JIVE_COAGENCY_GROUP_NAME)||role.contains(SynchroConstants.JIVE_COAGENCY_SUPPORT_GROUP_NAME)||role.contains(SynchroConstants.JIVE_FIELDWORK_GROUP_NAME))
        {
            return true;
        }
        else
        {
            return false;
        }

    }
    private Map<String,String> getSubjectandBody(Long projectId, String todoListTab, String projectName, String stageUrl, String userName, String stageName)
    {
        Map<String,String> emailSubBody = new HashMap<String, String>();
        String subject="";
        String messageBody="";
        if(todoListTab.equalsIgnoreCase("PIB Complete - Notify Agency(s)"))
        {
            //subject=String.format(SynchroGlobal.EmailNotification.PIB_COMPLETE_NOTIFY_AGENCY.getSubject(),projectId,projectName,"Proposal");
            /*	EmailMessage email = new EmailMessage();
               String tt = TemplateUtil.getTemplate("pib.complete.notifyAgency.htmlBody", JiveGlobals.getLocale());
               String tts = TemplateUtil.getTemplate("pib.complete.notifyAgency.subject", JiveGlobals.getLocale());
               String ttpp = TemplateUtil.getHtmlEscapedTemplate("pib.complete.notifyAgency.htmlBody", JiveGlobals.getLocale());
               String ttspp = TemplateUtil.getHtmlEscapedTemplate("pib.complete.notifyAgency.subject", JiveGlobals.getLocale());
               EmailTemplateBean bean = EmailTemplateUtil.buildBeanDynamically("pib.complete.notifyAgency");
               //Map<String, Object> con = bean.getContext();


               Map<String,String> ssss = TemplateUtil.getTemplates();
               System.out.print("ssss"+ssss);
               System.out.print("tttt"+tt);
               email.setHtmlBodyProperty("pib.complete.notifyAgency.htmlBody");
               email.setSubjectProperty("pib.complete.notifyAgency.subject");
               email.setLocale(JiveGlobals.getLocale());
               email.getContext().put("projectId", projectId);
               email.getContext().put ("projectName",projectName);
               email.getContext().put ("stageUrl",stageUrl);
               String t1 = TemplateUtil.getHtmlEscapedTemplate("pib.complete.notifyAgency", JiveGlobals.getLocale());
             */
            subject = TemplateUtil.getTemplate("pib.complete.notifyAgency.subject", JiveGlobals.getLocale());
            subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
            subject=subject.replaceAll("\\$\\{projectName\\}", projectName);
            messageBody = TemplateUtil.getHtmlEscapedTemplate("pib.complete.notifyAgency.htmlBody", JiveGlobals.getLocale());
            messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
            messageBody=messageBody.replaceAll("\\$\\{projectName\\}", projectName);
            messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
            //messageBody=String.format(SynchroGlobal.EmailNotification.PIB_COMPLETE_NOTIFY_AGENCY.getMessageBody(),projectName,stageUrl,stageUrl);
        }
        else if(todoListTab.equals("SEND FOR APPROVAL"))
        {
            if(stageName.equals("Project Specs"))
            {
                //subject=String.format(SynchroGlobal.EmailNotification.SEND_FOR_APPROVAL.getSubject(),projectName);
                //messageBody=String.format(SynchroGlobal.EmailNotification.SEND_FOR_APPROVAL.getMessageBody(),projectName,stageUrl,stageUrl);
                subject = TemplateUtil.getTemplate("send.for.approval.ps.subject", JiveGlobals.getLocale());
                subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
                subject=subject.replaceAll("\\$\\{projectName\\}", projectName);
                messageBody = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.ps.htmlBody", JiveGlobals.getLocale());
                messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
                messageBody=messageBody.replaceAll("\\$\\{projectName\\}", projectName);
                messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
            }
            else
            {
                //subject=String.format(SynchroGlobal.EmailNotification.SEND_FOR_APPROVAL.getSubject(),projectName);
                //messageBody=String.format(SynchroGlobal.EmailNotification.SEND_FOR_APPROVAL.getMessageBody(),projectName,stageUrl,stageUrl);
                subject = TemplateUtil.getTemplate("send.for.approval.reportSummary.subject", JiveGlobals.getLocale());
                subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
                subject=subject.replaceAll("\\$\\{projectName\\}", projectName);
                messageBody = TemplateUtil.getHtmlEscapedTemplate("send.for.approval.reportSummary.htmlBody", JiveGlobals.getLocale());
                messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
                messageBody=messageBody.replaceAll("\\$\\{projectName\\}", projectName);
                messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
            }
        }
        else if(todoListTab.equals("SEND TO PROJECT OWNER"))
        {
            // subject=String.format(SynchroGlobal.EmailNotification.SEND_TO_PROJECT_OWNER.getSubject(),projectName);
            // messageBody=String.format(SynchroGlobal.EmailNotification.SEND_TO_PROJECT_OWNER.getMessageBody(),projectName,stageUrl,stageUrl);
            subject = TemplateUtil.getTemplate("reportSummary.send.to.projectowner.subject", JiveGlobals.getLocale());
            subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
            subject=subject.replaceAll("\\$\\{projectName\\}", projectName);
            messageBody = TemplateUtil.getHtmlEscapedTemplate("reportSummary.send.to.projectowner.htmlBody", JiveGlobals.getLocale());
            messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
            messageBody=messageBody.replaceAll("\\$\\{projectName\\}", projectName);
            messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        }
        else if(todoListTab.equalsIgnoreCase("Request for Clarification"))
        {
            //subject=String.format(SynchroGlobal.EmailNotification.PROPOSAL_REQ_CLARIFICATION.getSubject(),projectName);
            //messageBody=String.format(SynchroGlobal.EmailNotification.PROPOSAL_REQ_CLARIFICATION.getMessageBody(),projectName,stageUrl,stageUrl);

            if(stageName.equals("Project Specs"))
            {
                subject = TemplateUtil.getTemplate("ps.request.clarification.subject", JiveGlobals.getLocale());
                subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
                subject=subject.replaceAll("\\$\\{projectName\\}", projectName);
                messageBody = TemplateUtil.getHtmlEscapedTemplate("ps.request.clarification.htmlBody", JiveGlobals.getLocale());
                messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
                messageBody=messageBody.replaceAll("\\$\\{projectName\\}", projectName);
                messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
            }
            else
            {
                subject = TemplateUtil.getTemplate("proposal.request.clarification.subject", JiveGlobals.getLocale());
                subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
                subject=subject.replaceAll("\\$\\{projectName\\}", projectName);
                messageBody = TemplateUtil.getHtmlEscapedTemplate("proposal.request.clarification.htmlBody", JiveGlobals.getLocale());
                messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
                messageBody=messageBody.replaceAll("\\$\\{projectName\\}", projectName);
                messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
            }
        }
        else if(todoListTab.equals("SEND FOR MARKETING REVIEW"))
        {
            subject=String.format(SynchroGlobal.EmailNotification.SEND_FOR_MARKETING_REVIEW.getSubject(),projectName);
            messageBody=String.format(SynchroGlobal.EmailNotification.SEND_FOR_MARKETING_REVIEW.getMessageBody(),projectName,stageUrl,stageUrl);
        }
        else if(todoListTab.equals("SEND FOR OTHER BAT REVIEW"))
        {
            subject=String.format(SynchroGlobal.EmailNotification.SEND_FOR_OTHER_BAT_REVIEW.getSubject(),projectName);
            messageBody=String.format(SynchroGlobal.EmailNotification.SEND_FOR_OTHER_BAT_REVIEW.getMessageBody(),projectName,stageUrl,stageUrl);
        }
        else if(todoListTab.equals("SEND FOR SP&I REVIEW"))
        {
            subject=String.format(SynchroGlobal.EmailNotification.SEND_FOR_SPI_REVIEW.getSubject(),projectName);
            messageBody=String.format(SynchroGlobal.EmailNotification.SEND_FOR_SPI_REVIEW.getMessageBody(),projectName,stageUrl,stageUrl);
        }
        else if(todoListTab.equals("SEND FOR LEGAL APPROVAL"))
        {
            subject=String.format(SynchroGlobal.EmailNotification.SEND_FOR_LEGAL_APPROVAL.getSubject(),projectName);
            messageBody=String.format(SynchroGlobal.EmailNotification.SEND_FOR_LEGAL_APPROVAL.getMessageBody(),projectName,stageUrl,stageUrl);
        }
        else if(todoListTab.equals("APPROVE"))
        {
            if(stageName.equals("Project Specs"))
            {
                // subject=String.format(SynchroGlobal.EmailNotification.APPROVE.getSubject(),projectName,userName);
                // messageBody=String.format(SynchroGlobal.EmailNotification.APPROVE.getMessageBody(),userName,projectName,stageUrl,stageUrl, stageName);
                subject = TemplateUtil.getTemplate("ps.approve.subject", JiveGlobals.getLocale());
                subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
                subject=subject.replaceAll("\\$\\{projectName\\}", projectName);
                subject=subject.replaceAll("\\$\\{userName\\}", userName);
                messageBody = TemplateUtil.getHtmlEscapedTemplate("ps.approve.htmlBody", JiveGlobals.getLocale());
                messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
                messageBody=messageBody.replaceAll("\\$\\{projectName\\}", projectName);
                messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                messageBody=messageBody.replaceAll("\\$\\{userName\\}", userName);
            }
            else
            {
                // subject=String.format(SynchroGlobal.EmailNotification.APPROVE.getSubject(),projectName,userName);
                // messageBody=String.format(SynchroGlobal.EmailNotification.APPROVE.getMessageBody(),userName,projectName,stageUrl,stageUrl, stageName);
                subject = TemplateUtil.getTemplate("reportSummary.approve.subject", JiveGlobals.getLocale());
                subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
                subject=subject.replaceAll("\\$\\{projectName\\}", projectName);
                subject=subject.replaceAll("\\$\\{userName\\}", userName);
                messageBody = TemplateUtil.getHtmlEscapedTemplate("reportSummary.approve.htmlBody", JiveGlobals.getLocale());
                messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
                messageBody=messageBody.replaceAll("\\$\\{projectName\\}", projectName);
                messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
                messageBody=messageBody.replaceAll("\\$\\{userName\\}", userName);
            }


        }
        else if(todoListTab.equals("NOTIFY EXTERNAL AGENCY"))
        {
            subject=String.format(SynchroGlobal.EmailNotification.NOTIFY_EXTERNAL_AGENCY.getSubject(),projectName);
            messageBody=String.format(SynchroGlobal.EmailNotification.NOTIFY_EXTERNAL_AGENCY.getMessageBody(),projectName,stageUrl,stageUrl);
        }
        else if(todoListTab.equals("NEEDS REVISION"))
        {
            // subject=String.format(SynchroGlobal.EmailNotification.NEEDS_REVISION.getSubject(),projectName);
            //  messageBody=String.format(SynchroGlobal.EmailNotification.NEEDS_REVISION.getMessageBody(),projectName,stageUrl,stageUrl);
            subject = TemplateUtil.getTemplate("needs.revision.reportSummary.subject", JiveGlobals.getLocale());
            subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
            subject=subject.replaceAll("\\$\\{projectName\\}", projectName);
            messageBody = TemplateUtil.getHtmlEscapedTemplate("needs.revision.reportSummary.htmlBody", JiveGlobals.getLocale());
            messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
            messageBody=messageBody.replaceAll("\\$\\{projectName\\}", projectName);
            messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        }
        else if(todoListTab.equals("SEND FOR PROCUREMENT REVIEW"))
        {
            subject=String.format(SynchroGlobal.EmailNotification.SEND_FOR_PROCUREMENT_REVIEW.getSubject(),projectName);
            messageBody=String.format(SynchroGlobal.EmailNotification.SEND_FOR_PROCUREMENT_REVIEW.getMessageBody(),projectName,stageUrl,stageUrl);
        }
        else if(todoListTab.equals("APPROVE FUNDING"))
        {
            subject=String.format(SynchroGlobal.EmailNotification.APPROVE_FUNDING.getSubject(),projectName,userName);
            //messageBody=String.format(SynchroGlobal.EmailNotification.APPROVE_FUNDING.getMessageBody(),projectName,stageUrl);
            messageBody=String.format(SynchroGlobal.EmailNotification.APPROVE_FUNDING.getMessageBody(),userName,stageUrl,stageUrl);
        }
        else if(todoListTab.equals("READY TO UPLOAD TO RKP"))
        {
            subject=String.format(SynchroGlobal.EmailNotification.READY_TO_UPLOAD_TO_RKP.getSubject(),projectName);
            messageBody=String.format(SynchroGlobal.EmailNotification.READY_TO_UPLOAD_TO_RKP.getMessageBody(),projectName,stageUrl,stageUrl);
        }
        else if(todoListTab.equals("UPLOAD TO IRIS"))
        {
            //subject=String.format(SynchroGlobal.EmailNotification.READY_TO_UPLOAD_TO_RKP.getSubject(),projectName);
            //messageBody=String.format(SynchroGlobal.EmailNotification.READY_TO_UPLOAD_TO_RKP.getMessageBody(),projectName,stageUrl,stageUrl);
            subject = TemplateUtil.getTemplate("reportSummary.upload.on.iris.subject", JiveGlobals.getLocale());
            subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
            subject=subject.replaceAll("\\$\\{projectName\\}", projectName);
            messageBody = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.iris.htmlBody", JiveGlobals.getLocale());
            messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
            messageBody=messageBody.replaceAll("\\$\\{projectName\\}", projectName);
            messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        }
        else if(todoListTab.equals("UPLOAD TO C-PSI DATABASE"))
        {
            subject = TemplateUtil.getTemplate("reportSummary.upload.on.c.psi.database.subject", JiveGlobals.getLocale());
            subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
            subject=subject.replaceAll("\\$\\{projectName\\}", projectName);
            messageBody = TemplateUtil.getHtmlEscapedTemplate("reportSummary.upload.on.c.psi.database.htmlBody", JiveGlobals.getLocale());
            messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
            messageBody=messageBody.replaceAll("\\$\\{projectName\\}", projectName);
            messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        }
        else if(todoListTab.equals("SUMMARY UPLOADED TO IRIS"))
        {
            subject = TemplateUtil.getTemplate("reportSummary.summary.upload.on.iris.subject", JiveGlobals.getLocale());
            subject=subject.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
            subject=subject.replaceAll("\\$\\{projectName\\}", projectName);
            messageBody = TemplateUtil.getHtmlEscapedTemplate("reportSummary.summary.upload.on.iris.htmlBody", JiveGlobals.getLocale());
            messageBody=messageBody.replaceAll("\\$\\{projectId\\}", SynchroUtils.generateProjectCode(projectId));
            messageBody=messageBody.replaceAll("\\$\\{projectName\\}", projectName);
            messageBody=messageBody.replaceAll("\\$\\{stageUrl\\}", stageUrl);
        }
        else
        {
            subject=String.format(SynchroGlobal.EmailNotification.SEND_FOR_APPROVAL_EXTERNALAGENCY.getSubject(),projectName);
            messageBody=String.format(SynchroGlobal.EmailNotification.SEND_FOR_APPROVAL_EXTERNALAGENCY.getMessageBody(),projectName,stageUrl,stageUrl);
        }
        emailSubBody.put(subject, messageBody);
        return emailSubBody;
    }


    /**
     * This method will update the status of the stage
     * @param projectId
     * @param stage
     * @param status
     */
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateStageStatus(long projectId, long endMarketId, int stageId, int status, User user, Map<Integer, List<AttachmentBean>> attachmentMap) {
        if(stageId==1)
        {
            pibDAO.updatePIBStatus(projectId,status);
            pibDAO.updatePIBCompletionDate(projectId, new Date(System.currentTimeMillis()));
            
        }
        if(stageId==2)
        {
            Project project = synchroProjectManager.get(projectId);
            PIBStakeholderList projectStakeholder = pibDAO.getPIBStakeholderList(projectId,endMarketId).get(0);
            List<ProjectInitiation> piList =  pibDAO.getProjectInitiation(projectId);
            List<PIBReporting> pibReportingList =  pibDAO.getPIBReporting(projectId);
            List<ProposalInitiation> proposalInitiationList = new ArrayList<ProposalInitiation>();
            List<EndMarketInvestmentDetail> endMarketDetails = synchroProjectManager.getEndMarketDetails(projectId);

            // https://www.svn.sourcen.com/issues/18051
            //  proposalDAO.removeAgency(projectId, projectStakeholder.getEndMarketID());

            if(projectStakeholder.getAgencyContact1()!=null && projectStakeholder.getAgencyContact1()>0)
            {
                ProposalInitiation proposalInitiation = new ProposalInitiation();
                proposalInitiation.setProjectID(projectId);
                proposalInitiation.setEndMarketID(projectStakeholder.getEndMarketID());
                proposalInitiation.setAgencyID(projectStakeholder.getAgencyContact1());
                proposalInitiation.setStatus(status);
                proposalInitiation.setCreationBy(user.getID());
                proposalInitiation.setCreationDate(System.currentTimeMillis());

                proposalInitiation.setModifiedBy(user.getID());
                proposalInitiation.setModifiedDate(System.currentTimeMillis());

                // project Specific fields
                proposalInitiation.setBrand(project.getBrand());
                proposalInitiation.setProjectOwner(project.getProjectOwner());
                //proposalInitiation.setSpiContact(project.getProjectOwner());
                if(endMarketDetails != null && endMarketDetails.size() > 0) {
                    proposalInitiation.setSpiContact(endMarketDetails.get(0).getSpiContact());
                } else {
                    proposalInitiation.setSpiContact(-1L);
                }

                proposalInitiation.setProposedMethodology(project.getProposedMethodology());
                proposalInitiation.setMethodologyGroup(project.getMethodologyGroup());
                proposalInitiation.setMethodologyType(project.getMethodologyType());
                proposalInitiation.setStartDate(project.getStartDate());
                proposalInitiation.setEndDate(project.getEndDate());

                // PIB Open text fields to be copied to Proposal Section
                proposalInitiation.setNpiReferenceNo(piList.get(0).getNpiReferenceNo());
                proposalInitiation.setBizQuestion(piList.get(0).getBizQuestion());
                proposalInitiation.setResearchObjective(piList.get(0).getResearchObjective());
                proposalInitiation.setActionStandard(piList.get(0).getActionStandard());
                proposalInitiation.setResearchDesign(piList.get(0).getResearchDesign());
                proposalInitiation.setSampleProfile(piList.get(0).getSampleProfile());
                proposalInitiation.setStimulusMaterial(piList.get(0).getStimulusMaterial());
                proposalInitiation.setOthers(piList.get(0).getOthers());

                if(piList.get(0).getStimuliDate()!=null)
                {
                    proposalInitiation.setStimuliDate(piList.get(0).getStimuliDate());
                }
                proposalInitiation.setOtherReportingRequirements(pibReportingList.get(0).getOtherReportingRequirements());

                proposalInitiation.setTopLinePresentation(pibReportingList.get(0).getTopLinePresentation());
                proposalInitiation.setPresentation(pibReportingList.get(0).getPresentation());
                proposalInitiation.setFullreport(pibReportingList.get(0).getFullreport());

                proposalInitiationList.add(proposalInitiation);
                if(attachmentMap!=null && attachmentMap.size()>0)
                {
                    //copyAttachments(attachmentMap,project.getProjectID(), endMarketId, projectStakeholder.getAgencyContact1(),projectStakeholder.getAgencyContact1() );
                    //https://www.svn.sourcen.com/issues/18710
                    copyAttachments(attachmentMap,project.getProjectID(), endMarketId, user.getID(),projectStakeholder.getAgencyContact1() );
                }

                //Text Fields Copy
                proposalInitiation.setBizQuestionText(piList.get(0).getBizQuestionText());
                proposalInitiation.setResearchObjectiveText(piList.get(0).getResearchObjectiveText());
                proposalInitiation.setActionStandardText(piList.get(0).getActionStandardText());
                proposalInitiation.setResearchDesignText(piList.get(0).getResearchDesignText());
                proposalInitiation.setSampleProfileText(piList.get(0).getSampleProfileText());
                proposalInitiation.setStimulusMaterialText(piList.get(0).getStimulusMaterialText());
                proposalInitiation.setOthersText(piList.get(0).getOthersText());
                proposalInitiation.setOtherReportingRequirementsText(pibReportingList.get(0).getOtherReportingRequirementsText());
                
                proposalInitiation.setProposalSaveDate(new Date(System.currentTimeMillis()));
            }
            if(projectStakeholder.getAgencyContact2()!=null && projectStakeholder.getAgencyContact2()>0)
            {
                ProposalInitiation proposalInitiation = new ProposalInitiation();
                proposalInitiation.setProjectID(projectId);
                proposalInitiation.setEndMarketID(projectStakeholder.getEndMarketID());
                proposalInitiation.setAgencyID(projectStakeholder.getAgencyContact2());
                proposalInitiation.setStatus(status);
                proposalInitiation.setCreationBy(user.getID());
                proposalInitiation.setCreationDate(System.currentTimeMillis());

                proposalInitiation.setModifiedBy(user.getID());
                proposalInitiation.setModifiedDate(System.currentTimeMillis());

                // project Specific fields
                proposalInitiation.setBrand(project.getBrand());
                proposalInitiation.setProjectOwner(project.getProjectOwner());
                //proposalInitiation.setSpiContact(project.getProjectOwner());
                if(endMarketDetails != null && endMarketDetails.size() > 0) {
                    proposalInitiation.setSpiContact(endMarketDetails.get(0).getSpiContact());
                } else {
                    proposalInitiation.setSpiContact(-1L);
                }
                proposalInitiation.setProposedMethodology(project.getProposedMethodology());
                proposalInitiation.setMethodologyGroup(project.getMethodologyGroup());
                proposalInitiation.setMethodologyType(project.getMethodologyType());
                proposalInitiation.setStartDate(project.getStartDate());
                proposalInitiation.setEndDate(project.getEndDate());

                // PIB Open text fields to be copied to Proposal Section
                proposalInitiation.setNpiReferenceNo(piList.get(0).getNpiReferenceNo());
                proposalInitiation.setBizQuestion(piList.get(0).getBizQuestion());
                proposalInitiation.setResearchObjective(piList.get(0).getResearchObjective());
                proposalInitiation.setActionStandard(piList.get(0).getActionStandard());
                proposalInitiation.setResearchDesign(piList.get(0).getResearchDesign());
                proposalInitiation.setSampleProfile(piList.get(0).getSampleProfile());
                proposalInitiation.setStimulusMaterial(piList.get(0).getStimulusMaterial());
                proposalInitiation.setOthers(piList.get(0).getOthers());
                if(piList.get(0).getStimuliDate()!=null)
                {
                    proposalInitiation.setStimuliDate(piList.get(0).getStimuliDate());
                }
                proposalInitiation.setOtherReportingRequirements(pibReportingList.get(0).getOtherReportingRequirements());
                proposalInitiation.setTopLinePresentation(pibReportingList.get(0).getTopLinePresentation());
                proposalInitiation.setPresentation(pibReportingList.get(0).getPresentation());
                proposalInitiation.setFullreport(pibReportingList.get(0).getFullreport());


                proposalInitiationList.add(proposalInitiation);
                if(attachmentMap!=null && attachmentMap.size()>0)
                {
                    //copyAttachments(attachmentMap,project.getProjectID(), endMarketId, projectStakeholder.getAgencyContact2(),projectStakeholder.getAgencyContact2() );
                    //https://www.svn.sourcen.com/issues/18710
                    copyAttachments(attachmentMap,project.getProjectID(), endMarketId, user.getID(),projectStakeholder.getAgencyContact2() );
                }

                //Text Fields Copy
                proposalInitiation.setBizQuestionText(piList.get(0).getBizQuestionText());
                proposalInitiation.setResearchObjectiveText(piList.get(0).getResearchObjectiveText());
                proposalInitiation.setActionStandardText(piList.get(0).getActionStandardText());
                proposalInitiation.setResearchDesignText(piList.get(0).getResearchDesignText());
                proposalInitiation.setSampleProfileText(piList.get(0).getSampleProfileText());
                proposalInitiation.setStimulusMaterialText(piList.get(0).getStimulusMaterialText());
                proposalInitiation.setOthersText(piList.get(0).getOthersText());
                proposalInitiation.setOtherReportingRequirementsText(pibReportingList.get(0).getOtherReportingRequirementsText());
                
                proposalInitiation.setProposalSaveDate(new Date(System.currentTimeMillis()));
            }
            if(projectStakeholder.getAgencyContact3()!=null && projectStakeholder.getAgencyContact3()>0)
            {
                ProposalInitiation proposalInitiation = new ProposalInitiation();
                proposalInitiation.setProjectID(projectId);
                proposalInitiation.setEndMarketID(projectStakeholder.getEndMarketID());
                proposalInitiation.setAgencyID(projectStakeholder.getAgencyContact3());
                proposalInitiation.setStatus(status);
                proposalInitiation.setCreationBy(user.getID());
                proposalInitiation.setCreationDate(System.currentTimeMillis());

                proposalInitiation.setModifiedBy(user.getID());
                proposalInitiation.setModifiedDate(System.currentTimeMillis());

                // project Specific fields
                proposalInitiation.setBrand(project.getBrand());
                proposalInitiation.setProjectOwner(project.getProjectOwner());
                //proposalInitiation.setSpiContact(project.getProjectOwner());
                if(endMarketDetails != null && endMarketDetails.size() > 0) {
                    proposalInitiation.setSpiContact(endMarketDetails.get(0).getSpiContact());
                } else {
                    proposalInitiation.setSpiContact(-1L);
                }
                proposalInitiation.setProposedMethodology(project.getProposedMethodology());
                proposalInitiation.setMethodologyGroup(project.getMethodologyGroup());
                proposalInitiation.setMethodologyType(project.getMethodologyType());
                proposalInitiation.setStartDate(project.getStartDate());
                proposalInitiation.setEndDate(project.getEndDate());

                // PIB Open text fields to be copied to Proposal Section
                proposalInitiation.setNpiReferenceNo(piList.get(0).getNpiReferenceNo());
                proposalInitiation.setBizQuestion(piList.get(0).getBizQuestion());
                proposalInitiation.setResearchObjective(piList.get(0).getResearchObjective());
                proposalInitiation.setActionStandard(piList.get(0).getActionStandard());
                proposalInitiation.setResearchDesign(piList.get(0).getResearchDesign());
                proposalInitiation.setSampleProfile(piList.get(0).getSampleProfile());
                proposalInitiation.setStimulusMaterial(piList.get(0).getStimulusMaterial());
                proposalInitiation.setOthers(piList.get(0).getOthers());
                if(piList.get(0).getStimuliDate()!=null)
                {
                    proposalInitiation.setStimuliDate(piList.get(0).getStimuliDate());
                }
                proposalInitiation.setOtherReportingRequirements(pibReportingList.get(0).getOtherReportingRequirements());
                proposalInitiation.setTopLinePresentation(pibReportingList.get(0).getTopLinePresentation());
                proposalInitiation.setPresentation(pibReportingList.get(0).getPresentation());
                proposalInitiation.setFullreport(pibReportingList.get(0).getFullreport());

                proposalInitiationList.add(proposalInitiation);
                if(attachmentMap!=null && attachmentMap.size()>0)
                {
                    //copyAttachments(attachmentMap,project.getProjectID(), endMarketId, projectStakeholder.getAgencyContact3(),projectStakeholder.getAgencyContact3() );
                    //https://www.svn.sourcen.com/issues/18710
                    copyAttachments(attachmentMap,project.getProjectID(), endMarketId, user.getID(),projectStakeholder.getAgencyContact3() );
                }

                //Text Fields Copy
                proposalInitiation.setBizQuestionText(piList.get(0).getBizQuestionText());
                proposalInitiation.setResearchObjectiveText(piList.get(0).getResearchObjectiveText());
                proposalInitiation.setActionStandardText(piList.get(0).getActionStandardText());
                proposalInitiation.setResearchDesignText(piList.get(0).getResearchDesignText());
                proposalInitiation.setSampleProfileText(piList.get(0).getSampleProfileText());
                proposalInitiation.setStimulusMaterialText(piList.get(0).getStimulusMaterialText());
                proposalInitiation.setOthersText(piList.get(0).getOthersText());
                proposalInitiation.setOtherReportingRequirementsText(pibReportingList.get(0).getOtherReportingRequirementsText());
                proposalInitiation.setProposalSaveDate(new Date(System.currentTimeMillis()));
            }
            proposalDAO.updateProposalStatus(projectId,proposalInitiationList,status);
        }

    }

    /**
     * This method will update the status of the stage in case of multi market
     * @param projectId
     * @param stage
     * @param status
     */
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateMultiMarketStageStatus(long projectId, long endMarketId, int stageId, int status, User user, Map<Integer, List<AttachmentBean>> attachmentMap) {
        if(stageId==1)
        {
            pibDAO.updatePIBStatus(projectId,status);
        }
        if(stageId==2)
        {
            Project project = synchroProjectManager.get(projectId);
            PIBStakeholderList projectStakeholder = pibDAO.getPIBStakeholderList(projectId,endMarketId).get(0);
            List<ProjectInitiation> piList =  pibDAO.getProjectInitiation(projectId);
            List<PIBReporting> pibReportingList =  pibDAO.getPIBReporting(projectId, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);
            List<ProposalInitiation> proposalInitiationList = new ArrayList<ProposalInitiation>();
            List<EndMarketInvestmentDetail> endMarketDetails = synchroProjectManager.getEndMarketDetails(projectId);

            // https://www.svn.sourcen.com/issues/18051
            //  proposalDAO.removeAgency(projectId, projectStakeholder.getEndMarketID());

            if(projectStakeholder.getAgencyContact1()!=null && projectStakeholder.getAgencyContact1()>0)
            {
                for(ProjectInitiation pi:piList)
                {
                    boolean isEndMarketEnable = true;
                    Integer endMarketStatus = synchroProjectManager.getEndMarketStatus(projectId, pi.getEndMarketID());
                    if(pi.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID && (endMarketStatus == SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal() || endMarketStatus == SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal() || endMarketStatus == SynchroGlobal.ProjectActivationStatus.DELETED.ordinal()))
                    {
                        isEndMarketEnable= false;
                    }
                    if(isEndMarketEnable)
                    {
                        ProposalInitiation proposalInitiation = new ProposalInitiation();
                        proposalInitiation.setProjectID(projectId);
                        proposalInitiation.setEndMarketID(pi.getEndMarketID());
                        proposalInitiation.setAgencyID(projectStakeholder.getAgencyContact1());
                        proposalInitiation.setStatus(status);
                        proposalInitiation.setCreationBy(user.getID());
                        proposalInitiation.setCreationDate(System.currentTimeMillis());

                        proposalInitiation.setModifiedBy(user.getID());
                        proposalInitiation.setModifiedDate(System.currentTimeMillis());

                        // project Specific fields
                        proposalInitiation.setBrand(project.getBrand());
                        proposalInitiation.setProjectOwner(project.getProjectOwner());
                        //proposalInitiation.setSpiContact(project.getProjectOwner());
                        if(endMarketDetails != null && endMarketDetails.size() > 0) {
                            proposalInitiation.setSpiContact(endMarketDetails.get(0).getSpiContact());
                        } else {
                            proposalInitiation.setSpiContact(-1L);
                        }

                        proposalInitiation.setProposedMethodology(project.getProposedMethodology());
                        proposalInitiation.setMethodologyGroup(project.getMethodologyGroup());
                        proposalInitiation.setMethodologyType(project.getMethodologyType());
                        proposalInitiation.setStartDate(project.getStartDate());
                        proposalInitiation.setEndDate(project.getEndDate());

                        // PIB Open text fields to be copied to Proposal Section
                        proposalInitiation.setNpiReferenceNo(pi.getNpiReferenceNo());
                        proposalInitiation.setBizQuestion(pi.getBizQuestion());
                        proposalInitiation.setResearchObjective(pi.getResearchObjective());
                        proposalInitiation.setActionStandard(pi.getActionStandard());
                        proposalInitiation.setResearchDesign(pi.getResearchDesign());
                        proposalInitiation.setSampleProfile(pi.getSampleProfile());
                        proposalInitiation.setStimulusMaterial(pi.getStimulusMaterial());
                        proposalInitiation.setOthers(pi.getOthers());
                        if(pi.getStimuliDate()!=null)
                        {
                            proposalInitiation.setStimuliDate(pi.getStimuliDate());
                        }
                        proposalInitiation.setOtherReportingRequirements(pibReportingList.get(0).getOtherReportingRequirements());
                        proposalInitiation.setTopLinePresentation(pibReportingList.get(0).getTopLinePresentation());
                        proposalInitiation.setPresentation(pibReportingList.get(0).getPresentation());
                        proposalInitiation.setFullreport(pibReportingList.get(0).getFullreport());
                        proposalInitiation.setGlobalSummary(pibReportingList.get(0).getGlobalSummary());

                        proposalInitiation.setCapRating(project.getCapRating());
                        proposalInitiationList.add(proposalInitiation);
                        if(attachmentMap!=null && attachmentMap.size()>0)
                        {
                            //copyAttachments(attachmentMap,project.getProjectID(), pi.getEndMarketID(), projectStakeholder.getAgencyContact1(),projectStakeholder.getAgencyContact1() );
                            //https://www.svn.sourcen.com/issues/18710
                            copyAttachments(attachmentMap,project.getProjectID(), pi.getEndMarketID(), user.getID(),projectStakeholder.getAgencyContact1() );
                        }

                        //Text Fields Copy
                        proposalInitiation.setBizQuestionText(pi.getBizQuestionText());
                        proposalInitiation.setResearchObjectiveText(pi.getResearchObjectiveText());
                        proposalInitiation.setActionStandardText(pi.getActionStandardText());
                        proposalInitiation.setResearchDesignText(pi.getResearchDesignText());
                        proposalInitiation.setSampleProfileText(pi.getSampleProfileText());
                        proposalInitiation.setStimulusMaterialText(pi.getStimulusMaterialText());
                        proposalInitiation.setOthersText(pi.getOthersText());
                        proposalInitiation.setOtherReportingRequirementsText(pibReportingList.get(0).getOtherReportingRequirementsText());
                        proposalInitiation.setProposalSaveDate(new Date(System.currentTimeMillis()));
                    }
                }
            }
            if(projectStakeholder.getAgencyContact2()!=null && projectStakeholder.getAgencyContact2()>0)
            {
                for(ProjectInitiation pi:piList)
                {
                    boolean isEndMarketEnable = true;
                    Integer endMarketStatus = synchroProjectManager.getEndMarketStatus(projectId, pi.getEndMarketID());
                    if(pi.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID && (endMarketStatus == SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal() || endMarketStatus == SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal() || endMarketStatus == SynchroGlobal.ProjectActivationStatus.DELETED.ordinal()))
                    {
                        isEndMarketEnable= false;
                    }
                    if(isEndMarketEnable)
                    {
                        ProposalInitiation proposalInitiation = new ProposalInitiation();
                        proposalInitiation.setProjectID(projectId);
                        proposalInitiation.setEndMarketID(pi.getEndMarketID());
                        proposalInitiation.setAgencyID(projectStakeholder.getAgencyContact2());
                        proposalInitiation.setStatus(status);
                        proposalInitiation.setCreationBy(user.getID());
                        proposalInitiation.setCreationDate(System.currentTimeMillis());

                        proposalInitiation.setModifiedBy(user.getID());
                        proposalInitiation.setModifiedDate(System.currentTimeMillis());

                        // project Specific fields
                        proposalInitiation.setBrand(project.getBrand());
                        proposalInitiation.setProjectOwner(project.getProjectOwner());
                        //proposalInitiation.setSpiContact(project.getProjectOwner());
                        if(endMarketDetails != null && endMarketDetails.size() > 0) {
                            proposalInitiation.setSpiContact(endMarketDetails.get(0).getSpiContact());
                        } else {
                            proposalInitiation.setSpiContact(-1L);
                        }
                        proposalInitiation.setProposedMethodology(project.getProposedMethodology());
                        proposalInitiation.setMethodologyGroup(project.getMethodologyGroup());
                        proposalInitiation.setMethodologyType(project.getMethodologyType());
                        proposalInitiation.setStartDate(project.getStartDate());
                        proposalInitiation.setEndDate(project.getEndDate());

                        // PIB Open text fields to be copied to Proposal Section
                        proposalInitiation.setNpiReferenceNo(pi.getNpiReferenceNo());
                        proposalInitiation.setBizQuestion(pi.getBizQuestion());
                        proposalInitiation.setResearchObjective(pi.getResearchObjective());
                        proposalInitiation.setActionStandard(pi.getActionStandard());
                        proposalInitiation.setResearchDesign(pi.getResearchDesign());
                        proposalInitiation.setSampleProfile(pi.getSampleProfile());
                        proposalInitiation.setStimulusMaterial(pi.getStimulusMaterial());
                        proposalInitiation.setOthers(pi.getOthers());
                        if(pi.getStimuliDate()!=null)
                        {
                            proposalInitiation.setStimuliDate(pi.getStimuliDate());
                        }
                        proposalInitiation.setOtherReportingRequirements(pibReportingList.get(0).getOtherReportingRequirements());
                        proposalInitiation.setTopLinePresentation(pibReportingList.get(0).getTopLinePresentation());
                        proposalInitiation.setPresentation(pibReportingList.get(0).getPresentation());
                        proposalInitiation.setFullreport(pibReportingList.get(0).getFullreport());
                        proposalInitiation.setGlobalSummary(pibReportingList.get(0).getGlobalSummary());

                        proposalInitiation.setCapRating(project.getCapRating());

                        proposalInitiationList.add(proposalInitiation);
                        if(attachmentMap!=null && attachmentMap.size()>0)
                        {
                            //copyAttachments(attachmentMap,project.getProjectID(), endMarketId, projectStakeholder.getAgencyContact2(),projectStakeholder.getAgencyContact2() );

                            //https://www.svn.sourcen.com/issues/18710
                            copyAttachments(attachmentMap,project.getProjectID(), pi.getEndMarketID(), user.getID(),projectStakeholder.getAgencyContact2() );
                        }

                        //Text Fields Copy
                        proposalInitiation.setBizQuestionText(piList.get(0).getBizQuestionText());
                        proposalInitiation.setResearchObjectiveText(piList.get(0).getResearchObjectiveText());
                        proposalInitiation.setActionStandardText(piList.get(0).getActionStandardText());
                        proposalInitiation.setResearchDesignText(piList.get(0).getResearchDesignText());
                        proposalInitiation.setSampleProfileText(piList.get(0).getSampleProfileText());
                        proposalInitiation.setStimulusMaterialText(piList.get(0).getStimulusMaterialText());
                        proposalInitiation.setOthersText(piList.get(0).getOthersText());
                        proposalInitiation.setOtherReportingRequirementsText(pibReportingList.get(0).getOtherReportingRequirementsText());
                        proposalInitiation.setProposalSaveDate(new Date(System.currentTimeMillis()));
                    }
                }
            }
            if(projectStakeholder.getAgencyContact3()!=null && projectStakeholder.getAgencyContact3()>0)
            {
                for(ProjectInitiation pi:piList)
                {
                    boolean isEndMarketEnable = true;
                    Integer endMarketStatus = synchroProjectManager.getEndMarketStatus(projectId, pi.getEndMarketID());
                    if(pi.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID && (endMarketStatus == SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal() || endMarketStatus == SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal() || endMarketStatus == SynchroGlobal.ProjectActivationStatus.DELETED.ordinal()))
                    {
                        isEndMarketEnable= false;
                    }
                    if(isEndMarketEnable)
                    {

                        ProposalInitiation proposalInitiation = new ProposalInitiation();
                        proposalInitiation.setProjectID(projectId);
                        proposalInitiation.setEndMarketID(pi.getEndMarketID());
                        proposalInitiation.setAgencyID(projectStakeholder.getAgencyContact3());
                        proposalInitiation.setStatus(status);
                        proposalInitiation.setCreationBy(user.getID());
                        proposalInitiation.setCreationDate(System.currentTimeMillis());

                        proposalInitiation.setModifiedBy(user.getID());
                        proposalInitiation.setModifiedDate(System.currentTimeMillis());

                        // project Specific fields
                        proposalInitiation.setBrand(project.getBrand());
                        proposalInitiation.setProjectOwner(project.getProjectOwner());
                        //proposalInitiation.setSpiContact(project.getProjectOwner());
                        if(endMarketDetails != null && endMarketDetails.size() > 0) {
                            proposalInitiation.setSpiContact(endMarketDetails.get(0).getSpiContact());
                        } else {
                            proposalInitiation.setSpiContact(-1L);
                        }
                        proposalInitiation.setProposedMethodology(project.getProposedMethodology());
                        proposalInitiation.setMethodologyGroup(project.getMethodologyGroup());
                        proposalInitiation.setMethodologyType(project.getMethodologyType());
                        proposalInitiation.setStartDate(project.getStartDate());
                        proposalInitiation.setEndDate(project.getEndDate());

                        // PIB Open text fields to be copied to Proposal Section
                        proposalInitiation.setNpiReferenceNo(pi.getNpiReferenceNo());
                        proposalInitiation.setBizQuestion(pi.getBizQuestion());
                        proposalInitiation.setResearchObjective(pi.getResearchObjective());
                        proposalInitiation.setActionStandard(pi.getActionStandard());
                        proposalInitiation.setResearchDesign(pi.getResearchDesign());
                        proposalInitiation.setSampleProfile(pi.getSampleProfile());
                        proposalInitiation.setStimulusMaterial(pi.getStimulusMaterial());
                        proposalInitiation.setOthers(pi.getOthers());
                        if(pi.getStimuliDate()!=null)
                        {
                            proposalInitiation.setStimuliDate(pi.getStimuliDate());
                        }

                        proposalInitiation.setOtherReportingRequirements(pibReportingList.get(0).getOtherReportingRequirements());
                        proposalInitiation.setTopLinePresentation(pibReportingList.get(0).getTopLinePresentation());
                        proposalInitiation.setPresentation(pibReportingList.get(0).getPresentation());
                        proposalInitiation.setFullreport(pibReportingList.get(0).getFullreport());
                        proposalInitiation.setGlobalSummary(pibReportingList.get(0).getGlobalSummary());

                        proposalInitiation.setCapRating(project.getCapRating());

                        proposalInitiationList.add(proposalInitiation);
                        if(attachmentMap!=null && attachmentMap.size()>0)
                        {
                            //copyAttachments(attachmentMap,project.getProjectID(), pi.getEndMarketID(), projectStakeholder.getAgencyContact3(),projectStakeholder.getAgencyContact3() );
                            //https://www.svn.sourcen.com/issues/18710
                            copyAttachments(attachmentMap,project.getProjectID(), pi.getEndMarketID(), user.getID(),projectStakeholder.getAgencyContact3() );
                        }
                        //Text Fields Copy
                        proposalInitiation.setBizQuestionText(piList.get(0).getBizQuestionText());
                        proposalInitiation.setResearchObjectiveText(piList.get(0).getResearchObjectiveText());
                        proposalInitiation.setActionStandardText(piList.get(0).getActionStandardText());
                        proposalInitiation.setResearchDesignText(piList.get(0).getResearchDesignText());
                        proposalInitiation.setSampleProfileText(piList.get(0).getSampleProfileText());
                        proposalInitiation.setStimulusMaterialText(piList.get(0).getStimulusMaterialText());
                        proposalInitiation.setOthersText(piList.get(0).getOthersText());
                        proposalInitiation.setOtherReportingRequirementsText(pibReportingList.get(0).getOtherReportingRequirementsText());
                        proposalInitiation.setProposalSaveDate(new Date(System.currentTimeMillis()));
                    }
                }
            }
            proposalDAO.updateProposalStatus(projectId,proposalInitiationList,status);
        }

    }
    /**
     * This method will update the status of the stage in case of multi market
     * @param projectId
     * @param stage
     * @param status
     */
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void updateActivatedProposalEM(long projectId, long endMarketId, int stageId, int status, User user, Map<Integer, List<AttachmentBean>> attachmentMap) {

        Project project = synchroProjectManager.get(projectId);
        PIBStakeholderList projectStakeholder = pibDAO.getPIBStakeholderList(projectId,endMarketId).get(0);
        List<ProjectInitiation> piList =  pibDAO.getProjectInitiation(projectId);
        List<PIBReporting> pibReportingList =  pibDAO.getPIBReporting(projectId, SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID);

        List<EndMarketInvestmentDetail> endMarketDetails = synchroProjectManager.getEndMarketDetails(projectId);

        List<Long> activeEndMarketIds = new ArrayList<Long>();
        List<ProposalInitiation> proposalAgencyList = this.proposalManager.getProposalDetails(projectId);

        for(ProposalInitiation pi:proposalAgencyList)
        {
            if(pi.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID)
            {
                activeEndMarketIds.add(pi.getEndMarketID());
            }
        }

        if(projectStakeholder.getAgencyContact1()!=null && projectStakeholder.getAgencyContact1()>0)
        {
            for(ProjectInitiation pi:piList)
            {
                boolean isEndMarketEnable = true;
                Integer endMarketStatus = synchroProjectManager.getEndMarketStatus(projectId, pi.getEndMarketID());
                if(pi.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID && (endMarketStatus == SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal() || endMarketStatus == SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal() || endMarketStatus == SynchroGlobal.ProjectActivationStatus.DELETED.ordinal() ))
                {
                    isEndMarketEnable= false;
                }
                if(isEndMarketEnable && pi.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID  && !activeEndMarketIds.contains(pi.getEndMarketID()))
                {
                    ProposalInitiation proposalInitiation = new ProposalInitiation();
                    proposalInitiation.setProjectID(projectId);
                    proposalInitiation.setEndMarketID(pi.getEndMarketID());
                    proposalInitiation.setAgencyID(projectStakeholder.getAgencyContact1());
                    proposalInitiation.setStatus(status);
                    proposalInitiation.setCreationBy(user.getID());
                    proposalInitiation.setCreationDate(System.currentTimeMillis());

                    proposalInitiation.setModifiedBy(user.getID());
                    proposalInitiation.setModifiedDate(System.currentTimeMillis());

                    // project Specific fields
                    proposalInitiation.setBrand(project.getBrand());
                    proposalInitiation.setProjectOwner(project.getProjectOwner());
                    //proposalInitiation.setSpiContact(project.getProjectOwner());
                    if(endMarketDetails != null && endMarketDetails.size() > 0) {
                        proposalInitiation.setSpiContact(endMarketDetails.get(0).getSpiContact());
                    } else {
                        proposalInitiation.setSpiContact(-1L);
                    }

                    proposalInitiation.setProposedMethodology(project.getProposedMethodology());
                    proposalInitiation.setMethodologyGroup(project.getMethodologyGroup());
                    proposalInitiation.setMethodologyType(project.getMethodologyType());
                    proposalInitiation.setStartDate(project.getStartDate());
                    proposalInitiation.setEndDate(project.getEndDate());

                    // PIB Open text fields to be copied to Proposal Section
                    proposalInitiation.setNpiReferenceNo(pi.getNpiReferenceNo());
                    proposalInitiation.setBizQuestion(pi.getBizQuestion());
                    proposalInitiation.setResearchObjective(pi.getResearchObjective());
                    proposalInitiation.setActionStandard(pi.getActionStandard());
                    proposalInitiation.setResearchDesign(pi.getResearchDesign());
                    proposalInitiation.setSampleProfile(pi.getSampleProfile());
                    proposalInitiation.setStimulusMaterial(pi.getStimulusMaterial());
                    proposalInitiation.setOthers(pi.getOthers());
                    if(pi.getStimuliDate()!=null)
                    {
                        proposalInitiation.setStimuliDate(pi.getStimuliDate());
                    }
                    proposalInitiation.setOtherReportingRequirements(pibReportingList.get(0).getOtherReportingRequirements());
                    proposalInitiation.setTopLinePresentation(pibReportingList.get(0).getTopLinePresentation());
                    proposalInitiation.setPresentation(pibReportingList.get(0).getPresentation());
                    proposalInitiation.setFullreport(pibReportingList.get(0).getFullreport());
                    proposalInitiation.setGlobalSummary(pibReportingList.get(0).getGlobalSummary());

                    proposalInitiation.setCapRating(project.getCapRating());
                    // proposalInitiationList.add(proposalInitiation);
                    
                    proposalInitiation.setProposalSaveDate(new Date(System.currentTimeMillis()));
                    
                    proposalDAO.save(proposalInitiation);
                    proposalDAO.saveProposalReporting(proposalInitiation);

                    proposalDAO.updateProposalSubimtted(projectId,null);

                    if(attachmentMap!=null && attachmentMap.size()>0)
                    {
                        //copyAttachments(attachmentMap,project.getProjectID(), pi.getEndMarketID(), projectStakeholder.getAgencyContact1(),projectStakeholder.getAgencyContact1() );
                        //https://www.svn.sourcen.com/issues/18710
                        copyAttachments(attachmentMap,project.getProjectID(), pi.getEndMarketID(), user.getID(),projectStakeholder.getAgencyContact1() );
                    }
                }
            }
        }
        if(projectStakeholder.getAgencyContact2()!=null && projectStakeholder.getAgencyContact2()>0)
        {
            for(ProjectInitiation pi:piList)
            {
                boolean isEndMarketEnable = true;
                Integer endMarketStatus = synchroProjectManager.getEndMarketStatus(projectId, pi.getEndMarketID());
                if(pi.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID && (endMarketStatus == SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal() || endMarketStatus == SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal() || endMarketStatus == SynchroGlobal.ProjectActivationStatus.DELETED.ordinal()))
                {
                    isEndMarketEnable= false;
                }
                if(isEndMarketEnable && pi.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID  && !activeEndMarketIds.contains(pi.getEndMarketID()))
                {
                    ProposalInitiation proposalInitiation = new ProposalInitiation();
                    proposalInitiation.setProjectID(projectId);
                    proposalInitiation.setEndMarketID(pi.getEndMarketID());
                    proposalInitiation.setAgencyID(projectStakeholder.getAgencyContact2());
                    proposalInitiation.setStatus(status);
                    proposalInitiation.setCreationBy(user.getID());
                    proposalInitiation.setCreationDate(System.currentTimeMillis());

                    proposalInitiation.setModifiedBy(user.getID());
                    proposalInitiation.setModifiedDate(System.currentTimeMillis());

                    // project Specific fields
                    proposalInitiation.setBrand(project.getBrand());
                    proposalInitiation.setProjectOwner(project.getProjectOwner());
                    //proposalInitiation.setSpiContact(project.getProjectOwner());
                    if(endMarketDetails != null && endMarketDetails.size() > 0) {
                        proposalInitiation.setSpiContact(endMarketDetails.get(0).getSpiContact());
                    } else {
                        proposalInitiation.setSpiContact(-1L);
                    }
                    proposalInitiation.setProposedMethodology(project.getProposedMethodology());
                    proposalInitiation.setMethodologyGroup(project.getMethodologyGroup());
                    proposalInitiation.setMethodologyType(project.getMethodologyType());
                    proposalInitiation.setStartDate(project.getStartDate());
                    proposalInitiation.setEndDate(project.getEndDate());

                    // PIB Open text fields to be copied to Proposal Section
                    proposalInitiation.setNpiReferenceNo(pi.getNpiReferenceNo());
                    proposalInitiation.setBizQuestion(pi.getBizQuestion());
                    proposalInitiation.setResearchObjective(pi.getResearchObjective());
                    proposalInitiation.setActionStandard(pi.getActionStandard());
                    proposalInitiation.setResearchDesign(pi.getResearchDesign());
                    proposalInitiation.setSampleProfile(pi.getSampleProfile());
                    proposalInitiation.setStimulusMaterial(pi.getStimulusMaterial());
                    proposalInitiation.setOthers(pi.getOthers());
                    if(pi.getStimuliDate()!=null)
                    {
                        proposalInitiation.setStimuliDate(pi.getStimuliDate());
                    }
                    proposalInitiation.setOtherReportingRequirements(pibReportingList.get(0).getOtherReportingRequirements());
                    proposalInitiation.setTopLinePresentation(pibReportingList.get(0).getTopLinePresentation());
                    proposalInitiation.setPresentation(pibReportingList.get(0).getPresentation());
                    proposalInitiation.setFullreport(pibReportingList.get(0).getFullreport());
                    proposalInitiation.setGlobalSummary(pibReportingList.get(0).getGlobalSummary());

                    proposalInitiation.setCapRating(project.getCapRating());
                    proposalInitiation.setProposalSaveDate(new Date(System.currentTimeMillis()));

                    //proposalInitiationList.add(proposalInitiation);
                    proposalDAO.save(proposalInitiation);
                    proposalDAO.saveProposalReporting(proposalInitiation);

                    proposalDAO.updateProposalSubimtted(projectId,null);

                    if(attachmentMap!=null && attachmentMap.size()>0)
                    {
                        //copyAttachments(attachmentMap,project.getProjectID(), endMarketId, projectStakeholder.getAgencyContact2(),projectStakeholder.getAgencyContact2() );
                        //https://www.svn.sourcen.com/issues/18710
                        copyAttachments(attachmentMap,project.getProjectID(), pi.getEndMarketID(), user.getID(),projectStakeholder.getAgencyContact2() );
                    }
                }
            }
        }
        if(projectStakeholder.getAgencyContact3()!=null && projectStakeholder.getAgencyContact3()>0)
        {
            for(ProjectInitiation pi:piList)
            {
                boolean isEndMarketEnable = true;
                Integer endMarketStatus = synchroProjectManager.getEndMarketStatus(projectId, pi.getEndMarketID());
                if(pi.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID && (endMarketStatus == SynchroGlobal.ProjectActivationStatus.CANCEL.ordinal() || endMarketStatus == SynchroGlobal.ProjectActivationStatus.ONHOLD.ordinal() || endMarketStatus == SynchroGlobal.ProjectActivationStatus.DELETED.ordinal()))
                {
                    isEndMarketEnable= false;
                }
                if(isEndMarketEnable && pi.getEndMarketID()!=SynchroConstants.ABOVE_MARKET_MULTI_MARKET_ID  && !activeEndMarketIds.contains(pi.getEndMarketID()))
                {

                    ProposalInitiation proposalInitiation = new ProposalInitiation();
                    proposalInitiation.setProjectID(projectId);
                    proposalInitiation.setEndMarketID(pi.getEndMarketID());
                    proposalInitiation.setAgencyID(projectStakeholder.getAgencyContact3());
                    proposalInitiation.setStatus(status);
                    proposalInitiation.setCreationBy(user.getID());
                    proposalInitiation.setCreationDate(System.currentTimeMillis());

                    proposalInitiation.setModifiedBy(user.getID());
                    proposalInitiation.setModifiedDate(System.currentTimeMillis());

                    // project Specific fields
                    proposalInitiation.setBrand(project.getBrand());
                    proposalInitiation.setProjectOwner(project.getProjectOwner());
                    //proposalInitiation.setSpiContact(project.getProjectOwner());
                    if(endMarketDetails != null && endMarketDetails.size() > 0) {
                        proposalInitiation.setSpiContact(endMarketDetails.get(0).getSpiContact());
                    } else {
                        proposalInitiation.setSpiContact(-1L);
                    }
                    proposalInitiation.setProposedMethodology(project.getProposedMethodology());
                    proposalInitiation.setMethodologyGroup(project.getMethodologyGroup());
                    proposalInitiation.setMethodologyType(project.getMethodologyType());
                    proposalInitiation.setStartDate(project.getStartDate());
                    proposalInitiation.setEndDate(project.getEndDate());

                    // PIB Open text fields to be copied to Proposal Section
                    proposalInitiation.setNpiReferenceNo(pi.getNpiReferenceNo());
                    proposalInitiation.setBizQuestion(pi.getBizQuestion());
                    proposalInitiation.setResearchObjective(pi.getResearchObjective());
                    proposalInitiation.setActionStandard(pi.getActionStandard());
                    proposalInitiation.setResearchDesign(pi.getResearchDesign());
                    proposalInitiation.setSampleProfile(pi.getSampleProfile());
                    proposalInitiation.setStimulusMaterial(pi.getStimulusMaterial());
                    proposalInitiation.setOthers(pi.getOthers());
                    if(pi.getStimuliDate()!=null)
                    {
                        proposalInitiation.setStimuliDate(pi.getStimuliDate());
                    }

                    proposalInitiation.setOtherReportingRequirements(pibReportingList.get(0).getOtherReportingRequirements());
                    proposalInitiation.setTopLinePresentation(pibReportingList.get(0).getTopLinePresentation());
                    proposalInitiation.setPresentation(pibReportingList.get(0).getPresentation());
                    proposalInitiation.setFullreport(pibReportingList.get(0).getFullreport());
                    proposalInitiation.setGlobalSummary(pibReportingList.get(0).getGlobalSummary());

                    proposalInitiation.setCapRating(project.getCapRating());

                    proposalInitiation.setProposalSaveDate(new Date(System.currentTimeMillis()));
                    //proposalInitiationList.add(proposalInitiation);

                    proposalDAO.save(proposalInitiation);
                    proposalDAO.saveProposalReporting(proposalInitiation);

                    proposalDAO.updateProposalSubimtted(projectId,null);

                    if(attachmentMap!=null && attachmentMap.size()>0)
                    {
                        //copyAttachments(attachmentMap,project.getProjectID(), pi.getEndMarketID(), projectStakeholder.getAgencyContact3(),projectStakeholder.getAgencyContact3() );
                        //https://www.svn.sourcen.com/issues/18710
                        copyAttachments(attachmentMap,project.getProjectID(), pi.getEndMarketID(), user.getID(),projectStakeholder.getAgencyContact3() );
                    }
                }
            }
        }
        //proposalDAO.updateProposalSubimtted(projectId,null);


    }
    /**
     * This method will copy the attachments from PIB stage to Proposal stage
     * @param attachmentMap
     */
    public void copyAttachments(Map<Integer, List<AttachmentBean>> attachmentMap, Long projectId, Long endMarketId,
                                 Long userId, Long agencyId)
    {
        if(attachmentMap!=null && attachmentMap.size()>0)
        {
            for(Integer key: attachmentMap.keySet())
            {
                List<AttachmentBean> attchBeanList = attachmentMap.get(key);
                for(AttachmentBean ab:attchBeanList)
                {
                    try
                    {
                        proposalManager.addAttachment(attachmentManager.getAttachment(ab.getID()).getUnfilteredData(),ab.getName(),
                                ab.getContentType(), projectId, endMarketId, key.longValue(), userId, agencyId);
                    }
                    catch(Exception e)
                    {
                        Log.error("Error while copying exception --"+ ab.getID() + "NAME --" + ab.getName());
                    }

                }

            }
        }

    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void saveAgency(Long projectId, Long endMarketId, Long agencyId, int status, User user, Map<Integer, List<AttachmentBean>> attachmentMap) {

        Project project = synchroProjectManager.get(projectId);
        //   PIBStakeholderList projectStakeholder = pibDAO.getPIBStakeholderList(projectId,endMarketId).get(0);
        List<ProjectInitiation> piList =  pibDAO.getProjectInitiation(projectId);
        List<PIBReporting> pibReportingList =  pibDAO.getPIBReporting(projectId);
        List<ProposalInitiation> proposalInitiationList = new ArrayList<ProposalInitiation>();
        List<EndMarketInvestmentDetail> endMarketDetails = synchroProjectManager.getEndMarketDetails(projectId);

        // https://www.svn.sourcen.com/issues/18051
        //  proposalDAO.removeAgency(projectId, projectStakeholder.getEndMarketID());

        if(agencyId!=null && agencyId>0)
        {
            ProposalInitiation proposalInitiation = new ProposalInitiation();
            proposalInitiation.setProjectID(projectId);
            proposalInitiation.setEndMarketID(endMarketId);
            proposalInitiation.setAgencyID(agencyId);
            proposalInitiation.setStatus(status);
            proposalInitiation.setCreationBy(user.getID());
            proposalInitiation.setCreationDate(System.currentTimeMillis());

            proposalInitiation.setModifiedBy(user.getID());
            proposalInitiation.setModifiedDate(System.currentTimeMillis());

            // project Specific fields
            proposalInitiation.setBrand(project.getBrand());
            proposalInitiation.setProjectOwner(project.getProjectOwner());
            //proposalInitiation.setSpiContact(project.getProjectOwner());
            if(endMarketDetails != null && endMarketDetails.size() > 0) {
                proposalInitiation.setSpiContact(endMarketDetails.get(0).getSpiContact());
            } else {
                proposalInitiation.setSpiContact(-1L);
            }


            proposalInitiation.setProposedMethodology(project.getProposedMethodology());
            proposalInitiation.setMethodologyGroup(project.getMethodologyGroup());
            proposalInitiation.setMethodologyType(project.getMethodologyType());
            proposalInitiation.setStartDate(project.getStartDate());
            proposalInitiation.setEndDate(project.getEndDate());

            // PIB Open text fields to be copied to Proposal Section
            proposalInitiation.setNpiReferenceNo(piList.get(0).getNpiReferenceNo());
            proposalInitiation.setBizQuestion(piList.get(0).getBizQuestion());
            proposalInitiation.setResearchObjective(piList.get(0).getResearchObjective());
            proposalInitiation.setActionStandard(piList.get(0).getActionStandard());
            proposalInitiation.setResearchDesign(piList.get(0).getResearchDesign());
            proposalInitiation.setSampleProfile(piList.get(0).getSampleProfile());
            proposalInitiation.setStimulusMaterial(piList.get(0).getStimulusMaterial());
            proposalInitiation.setOthers(piList.get(0).getOthers());
            
            proposalInitiation.setBizQuestionText(piList.get(0).getBizQuestionText());
            proposalInitiation.setResearchObjectiveText(piList.get(0).getResearchObjectiveText());
            proposalInitiation.setActionStandardText(piList.get(0).getActionStandardText());
            proposalInitiation.setResearchDesignText(piList.get(0).getResearchDesignText());
            proposalInitiation.setSampleProfileText(piList.get(0).getSampleProfileText());
            proposalInitiation.setStimulusMaterialText(piList.get(0).getStimulusMaterialText());
            proposalInitiation.setOthersText(piList.get(0).getOthersText());
            
            if(piList.get(0).getStimuliDate()!=null)
            {
                proposalInitiation.setStimuliDate(piList.get(0).getStimuliDate());
            }
            proposalInitiation.setOtherReportingRequirements(pibReportingList.get(0).getOtherReportingRequirements());
            proposalInitiation.setOtherReportingRequirementsText(pibReportingList.get(0).getOtherReportingRequirementsText());
            proposalInitiation.setTopLinePresentation(pibReportingList.get(0).getTopLinePresentation());
            proposalInitiation.setPresentation(pibReportingList.get(0).getPresentation());
            proposalInitiation.setFullreport(pibReportingList.get(0).getFullreport());
            proposalInitiation.setGlobalSummary(pibReportingList.get(0).getGlobalSummary());

            proposalInitiation.setCapRating(project.getCapRating());

            proposalInitiationList.add(proposalInitiation);
            if(attachmentMap!=null && attachmentMap.size()>0)
            {
                copyAttachments(attachmentMap,project.getProjectID(), endMarketId, agencyId,agencyId );
            }
        }

        // proposalDAO.updateProposalStatus(projectId,proposalInitiationList,status);
        proposalDAO.saveAgency(projectId,proposalInitiationList);


    }


    /**
     * This method will get the pending Activity Type and Pending activity link for Pending activities section
     */
    public Map<String, String> getPendingActivity(Project project)
    {
        Map<String, String> pendingActivites = new HashMap<String, String>();
        if(project.getStatus()==SynchroGlobal.Status.PIB_OPEN.ordinal())
        {
            List<ProjectInitiation> piList = pibDAO.getProjectInitiation(project.getProjectID());
            if(piList!=null && piList.size()>0)
            {
                //https://www.svn.sourcen.com/issues/17842
                if(!piList.get(0).getLegalApprovalNotReq() && !piList.get(0).getLegalApprovalRcvd())
                {
                    pendingActivites.put("PIB - Legal approval is not received.", JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID="+project.getProjectID());
                }
                if(piList.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_SAVED.ordinal())
                {
                    pendingActivites.put("PIB Complete - Notify Agency - Pending", JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID="+project.getProjectID());
                }
                /* if(piList.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_METH_WAIV_APP_PENDING.ordinal())
                {
                    pendingActivites.put("Methodology Waiver - Approval Pending", JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID="+project.getProjectID());
                }
                if(piList.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_METH_WAIV_MORE_INFO_REQ.ordinal())
                {
                    pendingActivites.put("Methodology Waiver - More Information requested", JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID="+project.getProjectID());
                }*/
            }
        }
        if(project.getStatus()==SynchroGlobal.Status.INPROGRESS_PLANNING.ordinal() || project.getStatus()==SynchroGlobal.Status.INPROGRESS_FIELDWORK.ordinal() || project.getStatus()==SynchroGlobal.Status.INPROGRESS_ANALYSIS.ordinal() || project.getStatus()==SynchroGlobal.Status.INPROGRESS_IRIS.ordinal())
        {
            List<ProjectInitiation> piList = pibDAO.getProjectInitiation(project.getProjectID());
            if(piList!=null && piList.size()>0)
            {
                //https://www.svn.sourcen.com/issues/17842
                if(!piList.get(0).getLegalApprovalNotReq() && !piList.get(0).getLegalApprovalRcvd())
                {
                    pendingActivites.put("PIB - Legal approval is not received.", JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID="+project.getProjectID());
                }
            }
            List<ProposalInitiation> propIniList = proposalDAO.getProposalInitiation(project.getProjectID());
            if(propIniList!=null && propIniList.size()>0)
            {
                if(propIniList.get(0).getStatus()==SynchroGlobal.StageStatus.PROPOSAL_SUBMITTED.ordinal())
                {
                    pendingActivites.put("Proposal - Award Agency - Pending", JiveGlobals.getDefaultBaseURL()+"/synchro/proposal-details!input.jspa?projectID="+project.getProjectID());
                }
            }
            List<ProjectSpecsInitiation> projSpecsList = projectSpecsDAO.getProjectSpecsInitiation(project.getProjectID());
            if(projSpecsList!=null && projSpecsList.size()>0)
            {
                if(projSpecsList.get(0).getStatus()==SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal())
                {
                    if(!(projSpecsList.get(0).getIsQDGApproved()==0 || projSpecsList.get(0).getIsQDGApproved()==0))
                    {
                        pendingActivites.put("Project Specs - Legal Approval Pending", JiveGlobals.getDefaultBaseURL()+"/synchro/project-specs!input.jspa?projectID="+project.getProjectID());
                    }
                    else if(!(projSpecsList.get(0).getIsApproved()==0))
                    {
                        pendingActivites.put("Project Specs - Approval Pending", JiveGlobals.getDefaultBaseURL()+"/synchro/project-specs!input.jspa?projectID="+project.getProjectID());
                    }
                }
            }
            List<ReportSummaryInitiation> repSummaryList = reportSummaryDAO.getReportSummaryInitiation(project.getProjectID());
            if(repSummaryList!=null && repSummaryList.size()>0)
            {
                if(repSummaryList.get(0).getStatus()==SynchroGlobal.StageStatus.REPORT_SUMMARY_STARTED.ordinal())
                {
                    if(!repSummaryList.get(0).getIsSPIApproved())
                    {
                        pendingActivites.put("Report/Summary - SPI Approval Pending", JiveGlobals.getDefaultBaseURL()+"/synchro/report-summary!input.jspa?projectID="+project.getProjectID());
                    }
                    /*  if(!repSummaryList.get(0).getIsLegalApproved())
                    {
                        pendingActivites.put("Report/Summary - Legal Approval Pending", JiveGlobals.getDefaultBaseURL()+"/synchro/report-summary!input.jspa?projectID="+project.getProjectID());
                    }*/
                }
            }
        }
        if(project.getStatus()==SynchroGlobal.Status.COMPLETED.ordinal())
        {
            List<ProjectInitiation> piList = pibDAO.getProjectInitiation(project.getProjectID());
            if(piList!=null && piList.size()>0)
            {
                //https://www.svn.sourcen.com/issues/17842
                if(!piList.get(0).getLegalApprovalNotReq() && !piList.get(0).getLegalApprovalRcvd())
                {
                    pendingActivites.put("PIB - Legal approval is not received.", JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!input.jspa?projectID="+project.getProjectID());
                }
            }


            // Project Evaluation will shown in Project Evaluation if they are not completed
            List<ProjectEvaluationInitiation> initiationList = projectEvaluationManager.getProjectEvaluationInitiation(project.getProjectID());
            if(initiationList!=null && initiationList.size()>0 && initiationList.get(0).getStatus()==SynchroGlobal.StageStatus.PROJ_EVAL_COMPLETED.ordinal())
            {

            }
            else
            {
                pendingActivites.put("Project Evaluation - Pending", JiveGlobals.getDefaultBaseURL()+"/synchro/project-eval!input.jspa?projectID="+project.getProjectID());
            }


        }
        return pendingActivites;
    }

    /**
     * This method will be triggered when the user click on any of the ToDoList Tab 
     * @param notificationTab
     * @param document
     * @param user
     * @param projectId
     * @param approver
     */
    public void sendNotification(User user, EmailMessage message)
    {

        /*SynchroStageToDoListBean synchroStageToDoBean = stageDAO
                  .getToDoList(notificationTab);
          LOG.info("NOTIFICATION RECIPIENTS -->"+ synchroStageToDoBean.getNotificationRecipients());*/
        //EmailMessage message = populateNotificationEmail(synchroStageToDoBean,projectId);
        if(message!=null)
        {
            message.getContext().put("portalType", BATGlobal.PortalType.SYNCHRO.toString());
            message.setSender(user.getName(),user.getEmail());
            emailManager.send(message);
        }
        /*if (approver != null && approver.trim().equals("approve")) {
              updateNotifcationProperties(projectId, synchroStageToDoBean.getStageId(),user);

          }
          else if (approver != null && approver.trim().equals("approve-funding")) {
              updateNotifcationProperties(projectId, synchroStageToDoBean.getStageId(),user);

          }*/

    }
    
    public void sendNotificationNew(String senderName, String senderEmail, EmailMessage message)
    {
        if(message!=null)
        {
            message.getContext().put("portalType", BATGlobal.PortalType.SYNCHRO.toString());
            message.setSender(senderName,senderEmail);
            emailManager.send(message);
        }

    }

    /**
     * This method will get the To Do List tabs for user
     * @param currentUser
     * @param projectId
     * @param document
     * @param stageId
     */
    @Override
    public List<SynchroStageToDoListBean> getToDoListTabs(User currentUser, long projectId, long stageId, String projectName, long endMarketId, String baseUrl)
    {

        List<SynchroStageToDoListBean> synchroStageToDoBean = stageDAO.getToDoListSequence(stageId);
        synchroStageToDoBean=populateActiveTabs(synchroStageToDoBean, currentUser, projectId,projectName,endMarketId, baseUrl);
        return synchroStageToDoBean;

        /*List<SynchroStageToDoListBean> synchroStageToDoBean = stageDAO.getToDoListSequence(stageId);
          Map<String,String> documentProperties = getDocumentProperties(document);
          if(documentProperties.containsKey(SynchroConstants.DOCUMENT_TODO_LIST_APPROVERS+".Completed") && documentProperties.get(SynchroConstants.DOCUMENT_TODO_LIST_APPROVERS+".Completed").equals("yes"))
          {
              synchroStageToDoBean=populateAfterApproveActiveTabs(synchroStageToDoBean, currentUser, projectId,projectName,document.getID(),documentProperties);
                 return synchroStageToDoBean;
          }
          else
          {
              synchroStageToDoBean=populateActiveTabs(synchroStageToDoBean, currentUser, projectId,documentProperties,projectName,document.getID());
                 return synchroStageToDoBean;
          }*/

    }

    /**
     * This method will set the Active ToDoList Tabs.
     * @param toDoList
     * @param currentUser
     * @param projectId
     * @param documentProperties
     * @return
     */
    private List<SynchroStageToDoListBean> populateActiveTabs(List<SynchroStageToDoListBean> toDoList,User currentUser, long projectId,String projectName, long endMarketId, String baseUrl)
    {
        List<SynchroStageToDoListBean> synchroStageToDoBean = toDoList;
        List<SynchroStageToDoListBean> populatedBean = new ArrayList<SynchroStageToDoListBean>();
        for(int i=0;i<synchroStageToDoBean.size();i++)
        {

            SynchroStageToDoListBean bean = synchroStageToDoBean.get(i);
            if(checkRole(bean.getRole(),projectId,currentUser,bean.getStageId(), endMarketId))
            {
                //	if(isTabActive(bean.getStageId(), projectId, currentUser.getID()) || !bean.getToDoAction().equals("APPROVE"))
                if(isTabActive(bean.getStageId(), projectId, currentUser.getID()))
                {
                    bean.setActive(true);

                    // This will set the Email Ids of the Notification Receipents from their Roles

                    //EmailNotification#6 Fetch Recipients for TO Do List Actions based on project, action button type, stage and current user
                    bean.setNotificationRecipients(getNotificationRecipients(bean.getNotificationRecipients(),projectId,endMarketId));
                    String activeStage = SynchroGlobal.getProjectActivityMethod().get(new Integer(bean.getStageId()+""));
                    String stageUrl=null;
                    // This is done as the Action for PIB Stage is different.
                    if(bean.getStageId()==1)
                    {
                        //stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!" + activeStage+ ".jspa?projectID=" + projectId;
                        stageUrl = baseUrl+"/synchro/pib-details!" + activeStage+ ".jspa?projectID=" + projectId;
                    }
                    else if(bean.getStageId()==3)
                    {
                        // stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/project-specs!input.jspa?projectID=" + projectId;
                        stageUrl = baseUrl+"/synchro/project-specs!input.jspa?projectID=" + projectId;
                    }
                    else if(bean.getStageId()==4)
                    {
                        //stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/report-summary!input.jspa?projectID=" + projectId;
                        stageUrl = baseUrl+"/synchro/report-summary!input.jspa?projectID=" + projectId;
                    }
                    else
                    {
                        // stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/activity!" + activeStage+ ".jspa?projectID=" + projectId;
                        stageUrl = baseUrl+"/synchro/activity!" + activeStage+ ".jspa?projectID=" + projectId;
                    }
                    // This check is for External Agency
                    Map<String,String> emailSubBoby = null;
                    String stageName = SynchroGlobal.ProjectDocument.getByCode(SynchroGlobal.getProjectActivityName().get(bean.getStageId()+"")).getDescription();
                    if(isExternalAgency(bean.getRole()))
                    {
                        emailSubBoby = getSubjectandBody(projectId,"",projectName,stageUrl,currentUser.getName(),stageName);
                    }
                    else
                    {
                        emailSubBoby = getSubjectandBody(projectId,bean.getToDoAction(),projectName,stageUrl,currentUser.getName(),stageName);
                    }
                    //emailSubBoby = getSubjectandBody(bean.getToDoAction(),projectName,stageUrl);
                    for(String key:emailSubBoby.keySet())
                    {
                        bean.setSubject(key);
                        bean.setMessageBody(emailSubBoby.get(key));
                    }
                }
                //https://www.svn.sourcen.com/issues/17738 --
                //only the users will applicable role will see the appropriate button it will be hidden for other irrelevant users
                populatedBean.add(bean);
            }


        }

        //return synchroStageToDoBean;
        return populatedBean;

        /*List<SynchroStageToDoListBean> synchroStageToDoBean = toDoList;
          String lastAction="";
          int removeIndex=-1;
          for(int i=0;i<synchroStageToDoBean.size();i++)
          {

              SynchroStageToDoListBean bean = synchroStageToDoBean.get(i);
              if(checkRole(bean.getRole(),projectId,currentUser,bean.getStageId()))
              {
                  if(bean.getToDoAction().equals("APPROVE FUNDING")&& documentProperties.get(SynchroConstants.DOCUMENT_TODO_LIST_APPROVEFUNDING)!=null && documentProperties.get(SynchroConstants.DOCUMENT_TODO_LIST_APPROVEFUNDING).contains(currentUser.getID()+""))
                  {
                      bean.setActive(false);
                  }
                  else if(bean.getToDoAction().equals("APPROVE FUNDING"))
                  {
                      // Approve funding tab will be enabled only when the Pre Plan and Co plan sections are
                      // completed on the Financial Details Section.
                      if (isTabActive(2, docId, projectId)) {

                          bean.setActive(true);
                          // This will set the Email Ids of the Notification
                          // Receipents from their Roles
                          bean.setNotificationRecipients(getNotificationRecipients(
                                  bean.getNotificationRecipients(), projectId,
                                  bean.getStageId()));
                          String activeStage = SynchroGlobal
                                  .getProjectActivityMethod().get(
                                          new Integer(bean.getStageId() + ""));
                          String stageUrl = JiveGlobals.getDefaultBaseURL()
                                  + "/synchro/activity!" + activeStage
                                  + ".jspa?projectID=" + projectId;
                          String stageName = SynchroGlobal.ProjectDocument.getByCode(SynchroGlobal.getProjectActivityName().get(bean.getStageId()+"")).getDescription();
                          Map<String, String> emailSubBoby = getSubjectandBody(
                                  bean.getToDoAction(), projectName, stageUrl,
                                  currentUser.getName(),stageName);
                          for (String key : emailSubBoby.keySet()) {
                              bean.setSubject(key);
                              bean.setMessageBody(emailSubBoby.get(key));
                          }
                      } else {
                          bean.setActive(false);
                      }
                  }
                  else if(bean.getToDoAction().equals("APPROVE")&& documentProperties.get(SynchroConstants.DOCUMENT_TODO_LIST_APPROVERS)!=null && documentProperties.get(SynchroConstants.DOCUMENT_TODO_LIST_APPROVERS).contains(currentUser.getID()+""))
                  {
                      bean.setActive(false);
                  }

                  else if(lastAction.equals("APPROVE"))
                  {
                      bean.setActive(false);
                  }
                  else
                  {
                      bean.setActive(true);
                      // This will set the Email Ids of the Notification Receipents from their Roles
                      bean.setNotificationRecipients(getNotificationRecipients(bean.getNotificationRecipients(),projectId,bean.getStageId()));
                      String activeStage = SynchroGlobal.getProjectActivityMethod().get(new Integer(bean.getStageId()+""));
                      String stageUrl=null;
                      // This is done as the Action for PIB Stage is different.
                      if(bean.getStageId()==1)
                      {
                          stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/pib-details!" + activeStage+ ".jspa?projectID=" + projectId;
                      }
                      else
                      {
                          stageUrl = JiveGlobals.getDefaultBaseURL()+"/synchro/activity!" + activeStage+ ".jspa?projectID=" + projectId;
                      }
                      // This check is for External Agency
                      Map<String,String> emailSubBoby = null;
                      String stageName = SynchroGlobal.ProjectDocument.getByCode(SynchroGlobal.getProjectActivityName().get(bean.getStageId()+"")).getDescription();
                      if(isExternalAgency(bean.getRole()))
                      {
                          emailSubBoby = getSubjectandBody("",projectName,stageUrl,currentUser.getName(),stageName);
                      }
                      else
                      {
                          emailSubBoby = getSubjectandBody(bean.getToDoAction(),projectName,stageUrl,currentUser.getName(),stageName);
                      }
                      //emailSubBoby = getSubjectandBody(bean.getToDoAction(),projectName,stageUrl);
                      for(String key:emailSubBoby.keySet())
                      {
                          bean.setSubject(key);
                          bean.setMessageBody(emailSubBoby.get(key));
                      }
                  }
              }
              if(lastAction.equals(bean.getToDoAction()))
              {
                  if(bean.isActive())
                  {
                      //synchroStageToDoBean.remove(i-1);
                      removeIndex = i-1;
                  }
                  else
                  {
                      //synchroStageToDoBean.remove(bean);
                      removeIndex = i;
                  }
              }
              lastAction=bean.getToDoAction();
              // This check is required for making the Approve tab enabled/disabled based on the
              // conditions from other screens/Tabs.
              if(bean.getToDoAction().equals("APPROVE")&& bean.isActive())
              {
                  //bean.setActive(isTabActive(bean.getStageId(),docId,projectId));
                  // This check as per issue https://www.svn.sourcen.com/issues/16630. for the 'Deviation from Methodology Waiver' field on
                  // Project Details
                  bean.setActive(isTabActive(bean.getStageId(),docId,projectId) && checkPartialMethodologyValidation(synchroProjectManager.get(projectId)));
              }
          }
          if(removeIndex!=-1)
          {
              synchroStageToDoBean.remove(removeIndex);
          }
          return synchroStageToDoBean;*/
    }

    /**
     * This method will check whether the current user belongs to a particular role or not
     * @param role
     * @param projectId
     * @param currentuser
     * @return
     */
    private boolean checkRole(String role, Long projectId, User currentuser, long stageId, long endMarketId)
    {
        if (role == null)
        {
            return false;
        }
        else if(role.contains(","))
        {
            String[] splitRole=role.split(",");
            for(int i=0;i<splitRole.length;i++)
            {
                HashSet<User> users = (HashSet<User>)getUsersByRole(projectId,splitRole[i],endMarketId);
                if(users.contains(currentuser))
                {
                    return true;
                }

            }
        }
        else
        {
            HashSet<User> users = (HashSet<User>)getUsersByRole(projectId,role,endMarketId);
            if(users.contains(currentuser))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * This method with check whether a particular tab specifically 'APPROVE' tab is active or not based
     * on the relevant information required from other screens/tabs.
     * @param stageId
     * @return
     */
    public boolean isTabActive(long stageId,long projectId,long approverId)
    {
        if(stageId==1)
        {

            List<ProjectInitiation> piList = pibDAO.getProjectInitiation(projectId);
            if(piList.isEmpty())
            {
                //return false;
            	return true;
            }

            List<ProposalInitiation> iniList = this.proposalManager.getProposalDetails(projectId);
            boolean isProposalAwarded = false;
            if(iniList!=null && iniList.size()>0)
            {
                for(ProposalInitiation pi:iniList)
                {
                    if(pi.getIsAwarded())
                    {
                        isProposalAwarded=true;
                    }
                }

            }

            // PIB tabs Notify Agency will be active only when all the fields on PIB are saved and the Proposal is not awarded.
            //https://www.svn.sourcen.com/issues/18051
           /* if(piList.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_SAVED.ordinal())
            {
                return true;
            }
            else if(piList.get(0).getStatus()==SynchroGlobal.StageStatus.PIB_COMPLETED.ordinal() && !isProposalAwarded)
            {
                return true;
            }
            else
            {
                return false;
            }*/
            
            // We need to keep Notify Agency Button Enabled all the time
            return true;
            
        }

        else if(stageId==3)
        {

            List<ProjectSpecsInitiation> projectSpecsList = projectSpecsDAO.getProjectSpecsInitiation(projectId);
            if(projectSpecsList.isEmpty())
            {
                return false;
            }
            // Project Specs tabs are enabled only when all the fields on Project Specs tab are filled.
          
            // This fix has been done for Quick Fix 
            // if(projectSpecsList.get(0).getStatus()==SynchroGlobal.StageStatus.PROJECT_SPECS_SAVED.ordinal())
            if(projectSpecsList.get(0).getStatus()==SynchroGlobal.StageStatus.PROJECT_SPECS_SAVED.ordinal() || projectSpecsList.get(0).getStatus()==SynchroGlobal.StageStatus.PROJECT_SPECS_STARTED.ordinal())
            {
                return true;
            }
            else
            {
                return false;
            }

        }
        else if(stageId==4)
        {

            List<ReportSummaryInitiation>  reportSummaryList = reportSummaryDAO.getReportSummaryInitiation(projectId);
            if(reportSummaryList.isEmpty())
            {
                return false;
            }
            // Report Summary tabs are enabled only when all the fields on Report Summary tab are filled.
            /*if(reportSummaryList.get(0).getStatus()==SynchroGlobal.StageStatus.REPORT_SUMMARY_SAVED.ordinal())
            {
                return true;
            }
            else
            {
                return false;
            }*/
            return true;

        }

        return true;
    }

    /**
     * This method will fetch all the notification receipents for particular roles.
     * @param notificationRoles
     * @param projectId
     * @param stageId
     * @return
     */
    @Override
    public String getNotificationRecipients(String notificationRoles,long projectId,long endMarketId)
    {
        StringBuffer notificationRecipients = new StringBuffer();
        Set<User> notificationUsers = new HashSet<User>();
        if (notificationRoles.contains(",")) {
            String[] splitRole = notificationRoles.split(",");

            for (int j = 0; j < splitRole.length; j++) {
                notificationUsers.addAll((HashSet<User>) getUsersByRole(projectId,
                        splitRole[j],endMarketId));
            }
        } else {
            notificationUsers = (HashSet<User>) getUsersByRole(projectId,
                    notificationRoles,endMarketId);
        }
        if(notificationUsers!=null && notificationUsers.size()>0)
        {
            int i=0;
            for(User recipient:notificationUsers)
            {
                notificationRecipients.append(recipient.getEmail());
                i++;
                if(i!=notificationUsers.size())
                {
                    notificationRecipients.append(",");
                }

            }


        }
        return notificationRecipients.toString();

    }
    /**
     * This method will return all the relevant users for a particular project for a particular role.
     * @param projectId
     * @param role
     * @return
     */
    public Collection<User> getUsersByRole(long projectId,String role, long endMarketId)
    {
        if(role.equals(SynchroConstants.JIVE_MARKETING_APPROVERS_GROUP_NAME))
        {
            return synchroUtils.getMarketUsers(projectId);

        }
        else if(role.equals(SynchroConstants.JIVE_OTHER_BAT_APPROVERS_GROUP_NAME))
        {
            return synchroUtils.getOtherBATUsers(projectId);

        }
        else if(role.equals(SynchroConstants.JIVE_SPI_APPROVERS_GROUP_NAME))
        {
            return synchroUtils.getSPIUsers(projectId);

        }
        else if(role.equals(SynchroConstants.JIVE_LEGAL_APPROVERS_GROUP_NAME))
        {
            return synchroUtils.getLegalUsers(projectId,endMarketId);

        }
        else if(role.equals(SynchroConstants.JIVE_PROCUREMENT_APPROVERS_GROUP_NAME))
        {
            return synchroUtils.getProcurementUsers(projectId,endMarketId);

        }

        else if(role.equals(SynchroConstants.JIVE_EXTERNAL_AGENCY_GROUP_NAME))
        {
            return synchroUtils.getExternalAgencyUsers(projectId,endMarketId);

        }
        /*
       * This will return only the agency who 's proposal is awarded
       */
        else if(role.equals(SynchroConstants.AWARDED_EXTERNAL_AGENCY_ROLE))
        {
            return synchroUtils.getAwardedExternalAgencyUsers(projectId,endMarketId);

        }
        else if(role.equals(SynchroConstants.JIVE_COMMUNICATION_AGECNY_GROUP_NAME))
        {
            return synchroUtils.getCommunicationAgencyUsers(projectId,endMarketId);

        }

        else if(role.equals(SynchroConstants.PROJECT_OWNER_ROLE))
        {
            long ownerId = projectDAO.get(projectId).getProjectOwner();
            try
            {
                User user = userManager.getUser(ownerId);
                return Sets.newHashSet(user);
            }
            catch(UserNotFoundException ue)
            {
                LOG.error("Could not found User with ID -->"+ownerId);
            }

        }
        else if(role.equals(SynchroConstants.JIVE_SUPPORT_GROUP_NAME))
        {
            return synchroUtils.getSupportUsers(projectId);

        }
        // This will fetch the approvers for a particular stage and project.Please note that Approvers
        // and Project Stakeholders are different roles.
        else if(role.equals(SynchroConstants.APPROVERS))
        {
            /*String[] groups = SynchroGlobal.getApproverGroup().get(stageId+"");
               int i=1;
               StringBuilder groupIds = new StringBuilder();
               for(String group:groups)
               {
                   groupIds.append(synchroUtils.getJiveGroup(group).getID());
                   if(i<groups.length)
                   {
                       groupIds.append(",");
                   }
                   i++;
               }
                  return Sets.newHashSet(stageDAO.getStageApprovers(projectId, stageId, groupIds.toString()));	*/
        }
        // This will fetch the Synchro Admin Users
        else if(role.equals(SynchroConstants.JIVE_SYNCHRO_ADMIN_GROUP_NAME))
        {
            return synchroUtils.getSynchroAdminUsers();

        }
        else if(role.equals(SynchroConstants.SYNCHRO_GLOBAL_PROJECT_CONTACT_GROUP_NAME))
        {
            List<FundingInvestment> fundingInvestmentList = projectDAO.getProjectInvestments(projectId);
            if(fundingInvestmentList!=null && fundingInvestmentList.size()>0)
            {
                try
                {
                    for(FundingInvestment fi : fundingInvestmentList)
                    {
                        if(fi.getInvestmentType().intValue()==SynchroGlobal.InvestmentType.GlOBAL.getId())
                        {
                            User user = userManager.getUser(fi.getProjectContact());
                            return Sets.newHashSet(user);
                        }
                    }
                }
                catch(UserNotFoundException ue)
                {
                    LOG.error("Could not found Global Project Contact for  -->"+projectId);
                }
            }
        }

        return new HashSet<User>();
    }


    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }


    public void setSynchroUtils(final SynchroUtils synchroUtils) {
        this.synchroUtils = synchroUtils;
    }

    public ProjectDAO getProjectDAO() {
        return projectDAO;
    }

    public void setProjectDAO(ProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    public StageDAO getStageDAO() {
        return stageDAO;
    }

    public void setStageDAO(StageDAO stageDAO) {
        this.stageDAO = stageDAO;
    }
    public EmailManager getEmailManager() {
        return emailManager;
    }
    public void setEmailManager(EmailManager emailManager) {
        this.emailManager = emailManager;
    }

    public PIBDAO getPibDAO() {
        return pibDAO;
    }
    public void setPibDAO(PIBDAO pibDAO) {
        this.pibDAO = pibDAO;
    }
    public ProjectManager getSynchroProjectManager() {
        return synchroProjectManager;
    }
    public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
        this.synchroProjectManager = synchroProjectManager;
    }
    public ProposalDAO getProposalDAO() {
        return proposalDAO;
    }
    public void setProposalDAO(ProposalDAO proposalDAO) {
        this.proposalDAO = proposalDAO;
    }
    public ProjectSpecsDAO getProjectSpecsDAO() {
        return projectSpecsDAO;
    }
    public void setProjectSpecsDAO(ProjectSpecsDAO projectSpecsDAO) {
        this.projectSpecsDAO = projectSpecsDAO;
    }
    public ReportSummaryDAO getReportSummaryDAO() {
        return reportSummaryDAO;
    }
    public void setReportSummaryDAO(ReportSummaryDAO reportSummaryDAO) {
        this.reportSummaryDAO = reportSummaryDAO;
    }

    public ProjectEvaluationManager getProjectEvaluationManager() {
        return projectEvaluationManager;
    }

    public void setProjectEvaluationManager(
            ProjectEvaluationManager projectEvaluationManager) {
        this.projectEvaluationManager = projectEvaluationManager;
    }

    public ProposalManager getProposalManager() {
        return proposalManager;
    }

    public void setProposalManager(ProposalManager proposalManager) {
        this.proposalManager = proposalManager;
    }

    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

}