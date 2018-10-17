package com.grail.synchro.manager.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.SynchroGlobal.SynchroAttachmentObject;
import com.grail.synchro.beans.ProjectWaiver;
import com.grail.synchro.beans.ProjectWaiverApprover;
import com.grail.synchro.beans.ProjectWaiverEndMarket;
import com.grail.synchro.dao.ProcessWaiverDAONew;
import com.grail.synchro.manager.ProcessWaiverManagerNew;
import com.grail.synchro.object.SynchroAttachment;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.jivesoftware.base.User;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Tejinder
 * @version 1.0
 */
public class ProcessWaiverManagerImplNew implements ProcessWaiverManagerNew {

    private ProcessWaiverDAONew processWaiverDAONew;
    private AttachmentHelper attachmentHelper;
    private AttachmentManager attachmentManager;
    
	
    
    public AttachmentHelper getAttachmentHelper() {
		return attachmentHelper;
	}

	public void setAttachmentHelper(AttachmentHelper attachmentHelper) {
		this.attachmentHelper = attachmentHelper;
	}

	public AttachmentManager getAttachmentManager() {
		return attachmentManager;
	}

	public void setAttachmentManager(AttachmentManager attachmentManager) {
		this.attachmentManager = attachmentManager;
	}

    @Override
    public ProjectWaiver create(ProjectWaiver projectWaiver) {
        projectWaiver.setCreationBy(getUser().getID());
        projectWaiver.setCreationDate(new Date().getTime());
        projectWaiver.setModifiedBy(getUser().getID());
        projectWaiver.setModifiedDate(new Date().getTime());
        ProjectWaiver pw = processWaiverDAONew.create(projectWaiver);
        projectWaiver.setWaiverID(pw.getWaiverID());
        if(projectWaiver.getWaiverID() > 0) {
        	processWaiverDAONew.saveWaiverEndMarkets(projectWaiver);
        		if(projectWaiver.getApproverID()!=null && projectWaiver.getApproverID()>0)
        		{
        			processWaiverDAONew.saveWaiverApprover(projectWaiver);
        		}
        }

        return projectWaiver;
    }

    @Override
    public ProjectWaiver update(final ProjectWaiver projectWaiver) {
        projectWaiver.setModifiedBy(getUser().getID());
        projectWaiver.setModifiedDate(new Date().getTime());
        processWaiverDAONew.update(projectWaiver);
        if(projectWaiver.getWaiverID()>0)
    	{
        	processWaiverDAONew.updateWaiverApprover(projectWaiver);
        	 if(projectWaiver.getEndMarkets()!=null)
         	{
        		 processWaiverDAONew.saveWaiverEndMarkets(projectWaiver);
         	}
    	}
        
        return projectWaiver;
    }
    
    @Override
    public List<ProjectWaiverEndMarket> getWaiverEndMarkets(final Long waiverID) {
       return processWaiverDAONew.getWaiverEndMarkets(waiverID);
    }


    @Override
    public void saveWaiverEndMarkets(final Long waiverID, final List<Long> endMarkets) {

    }

    @Override
    public boolean deleteWaiverEndMarkets(final ProjectWaiver projectWaiver) {
        return false;
    }

    @Override
    public boolean deleteWaiverEndMarkets(final Long waiverID) {
        return false;
    }

    @Override
    public void saveWaiverEndMarkets(final ProjectWaiver projectWaiver) {
    }

    @Override
    public Long getWaiverApproverID(final Long waiverID) {
    	 return processWaiverDAONew.getWaiverApproverID(waiverID);
    }

    @Override
    public Long getWaiverApprover(final ProjectWaiver projectWaiver) {
    	 return processWaiverDAONew.getWaiverApprover(projectWaiver);
    }

    @Override
    public void saveWaiverApprover(final Long waiverID, final Long approverID) {
    }
    
    @Override
    public void updateWaiverApprover(final ProjectWaiver projectWaiver) {
    	processWaiverDAONew.updateWaiverApprover(projectWaiver);
    }


    @Override
    public void saveWaiverApprover(final ProjectWaiver projectWaiver) {
    }

    @Override
    public boolean deleteWaiverApprover(final ProjectWaiver projectWaiver) {
        return false;
    }

    @Override
    public boolean deleteWaiverApprover(final Long waiverID) {
        return false;
    }

    @Override
    public boolean approve(final Long waiverID, final Long approverID) {
        return false;
    }

    @Override
    public boolean approve(final ProjectWaiver projectWaiver) {
        return false;
    }

    @Override
    public boolean reject(final Long waiverID, final Long approverID) {
        return false;
    }

    @Override
    public boolean reject(final ProjectWaiver projectWaiver) {
        return false;
    }

    @Override
    public ProjectWaiver get(final Long id) {
    	ProjectWaiver projectWaiver =  processWaiverDAONew.get(id);
    	ProjectWaiverApprover projectWaiverApprover = processWaiverDAONew.getWaiverApprover(id);
    	if(projectWaiverApprover!=null)
    	{
    		projectWaiver.setApproverID(projectWaiverApprover.getApproverID());
    		projectWaiver.setApproverComments(projectWaiverApprover.getComments());
    	}
    
    	 List<Long> endMarkets = processWaiverDAONew.getWaiverEndMarketsIDs(id);
         if(endMarkets!=null)
         {
         	projectWaiver.setEndMarkets(endMarkets);
         }
    	return projectWaiver;
    }

    @Override
    public ProjectWaiver get(final Long id, final Long userID) {
        return processWaiverDAONew.get(id, userID);
    }

    @Override
    public ProjectWaiver get(final String name) {
        return processWaiverDAONew.get(name);
    }

    @Override
    public List<ProjectWaiver> getAll() {
        return getAll(getUser().getID(), null, null);
    }

    @Override
    public List<ProjectWaiver> getAllByResultFilter(final ProjectResultFilter projectResultFilter) {
        List<ProjectWaiver> projectWaivers = processWaiverDAONew.getAllByResultFilter(projectResultFilter);
        if(projectWaivers != null && !projectWaivers.isEmpty()) {
            for(ProjectWaiver projectWaiver: projectWaivers) {              
            	 //projectWaiver.setApproverID(Long.parseLong(processWaiverDAONew.getWaiverApproverID(projectWaiver.getWaiverID()).toString()));
            	 ProjectWaiverApprover approverDetails = processWaiverDAONew.getWaiverApprover(projectWaiver.getWaiverID());
            	 if(approverDetails!=null)
            	 {
            		 projectWaiver.setApproverID(approverDetails.getApproverID());
            		 projectWaiver.setApproverComments(approverDetails.getComments());
            	 }
            	 
            	 projectWaiver.setEndMarkets(processWaiverDAONew.getWaiverEndMarketsIDs(projectWaiver.getWaiverID()));
            }
        }
        return projectWaivers;
    }
    
    @Override
    public List<ProjectWaiver> getAllByResultFilter(final User user, final ProjectResultFilter projectResultFilter) {
        List<ProjectWaiver> projectWaivers = processWaiverDAONew.getAllByResultFilter(user, projectResultFilter);
        if(projectWaivers != null && !projectWaivers.isEmpty()) {
            for(ProjectWaiver projectWaiver: projectWaivers) {              
            	// projectWaiver.setApproverID(Long.parseLong(processWaiverDAONew.getWaiverApproverID(projectWaiver.getWaiverID()).toString()));
            	// projectWaiver.setEndMarkets(processWaiverDAONew.getWaiverEndMarketsIDs(projectWaiver.getWaiverID()));
            	 
            	 ProjectWaiverApprover approverDetails = processWaiverDAONew.getWaiverApprover(projectWaiver.getWaiverID());
            	 if(approverDetails!=null)
            	 {
            		 projectWaiver.setApproverID(approverDetails.getApproverID());
            		 projectWaiver.setApproverComments(approverDetails.getComments());
            	 }
            	 
            	 projectWaiver.setEndMarkets(processWaiverDAONew.getWaiverEndMarketsIDs(projectWaiver.getWaiverID()));
            }
        }
        return projectWaivers;
    }
    
    @Override
    public List<ProjectWaiver> getAll(final Integer start, final Integer limit) {
        return getAll(getUser().getID(), start, limit);
    }

    @Override
    public List<ProjectWaiver> getAll(final Long userID) {
        return getAll(userID, null, null);
    }

    @Override
    public List<ProjectWaiver> getAll(Long userID, final Integer start, final Integer limit) {
        List<ProjectWaiver> projectWaivers = processWaiverDAONew.getAll(userID, start, limit);
        if(projectWaivers != null && !projectWaivers.isEmpty()) {
            for(ProjectWaiver projectWaiver: projectWaivers) {
                getEndMarkets(projectWaiver);
                getApprover(projectWaiver);
            }
        }
        return projectWaivers;
    }

    
    private void getEndMarkets(final ProjectWaiver projectWaiver) {
       projectWaiver.setEndMarkets(processWaiverDAONew.getWaiverEndMarketsIDs(projectWaiver.getWaiverID()));
    }

    private void getApprover(final ProjectWaiver projectWaiver) {
       projectWaiver.setApproverID(Long.parseLong(processWaiverDAONew.getWaiverApproverID(projectWaiver.getWaiverID()).toString()));
    }

    @Override
    public Long getTotalCount() {
        return getTotalCount(getUser().getID());
    }

    @Override
    public Long getTotalCount(final Long userID) {
        return processWaiverDAONew.getTotalCount(userID);
    }

    @Override
    @Transactional
    public Long getPendingActivityTotalCount(final User user, final ProjectResultFilter filter) {
        return processWaiverDAONew.getPendingActivityTotalCount(user,filter);
    }

    @Override
    @Transactional
    public List<ProjectWaiver> getPendingApprovalWaivers(final User user, final ProjectResultFilter projectResultFilter) {
    	List<ProjectWaiver> projectWaivers = processWaiverDAONew.getPendingApprovalWaivers(user, projectResultFilter);
        if(projectWaivers != null && !projectWaivers.isEmpty()) {
            for(ProjectWaiver projectWaiver: projectWaivers) {              
            	 projectWaiver.setApproverID(Long.parseLong(processWaiverDAONew.getWaiverApproverID(projectWaiver.getWaiverID()).toString()));
            	 projectWaiver.setEndMarkets(processWaiverDAONew.getWaiverEndMarketsIDs(projectWaiver.getWaiverID()));
            }
        }
        return projectWaivers;
    }

    private static User getUser() {
        return JiveApplication.getContext().getAuthenticationProvider().getJiveUser();
    }
    
    @Override
    public Boolean doesWaiverExists(final Long waiverID) {
        return processWaiverDAONew.doesWaiverExists(waiverID);
    }
    
    @Override
    public Long generateWaiverID() {
        return processWaiverDAONew.generateWaiverID();
    }
    
    @Override
    public Map<Integer, List<AttachmentBean>> getDocumentAttachment(final Long waiverID)
    {
    	Map<Integer, List<AttachmentBean>> attachmentMap = new HashMap<Integer, List<AttachmentBean>>();

    	List<AttachmentBean> attachments = processWaiverDAONew.getFieldAttachments(getSynchroAttachment(waiverID));
    	if(attachments != null && attachments.size() > 0) {
            attachmentMap.put(SynchroAttachmentObject.WAIVER_ATTACHMENT.getId(), attachments);
        }
      
    	return attachmentMap;
    }
    
    @Override
    public List<AttachmentBean> getFieldAttachments(final Long waiverID) {
        return processWaiverDAONew.getFieldAttachments(getSynchroAttachment(waiverID));  //To change body of implemented methods use File | Settings | File Templates.
    }
    
    @Override
    public boolean addAttachment(File attachment,String fileName, final String contentType, 
    		Long waiverID, Long userId) throws IOException, AttachmentException
    {
        boolean success = false;
        try
        {
        	Attachment att = attachmentHelper.createAttachment(
                    getSynchroAttachment(waiverID), fileName , contentType, attachment);
        	processWaiverDAONew.saveAttachmentUser(att.getID(), userId);
            success = true;
        }
        catch (IOException e) 
        {
            throw new IOException(e.getMessage(), e);
        }
        catch (AttachmentException e) 
        {
            throw new AttachmentException(e.getMessage(), e);
        }
        return success;
    }
    
    @Override
    public void updateDocumentAttachment(Long attachmentId, Long waiverID)
    {
    	long objectId = (waiverID).hashCode();
    	processWaiverDAONew.updateDocumentAttachment(attachmentId, objectId);
        
        
    }
    
    @Override
    public boolean removeAttachment(Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception {
        boolean success = false;
        try
        {
            Attachment attachment = attachmentManager.getAttachment(attachmentId);
            attachmentManager.deleteAttachment(attachment);
            processWaiverDAONew.deleteAttachmentUser(attachmentId);
            //pibDAO.removeDocumentAttachment(attachmentId);
            success=true;
        } 
        catch (AttachmentNotFoundException e) 
        {
            throw new AttachmentNotFoundException(e.getMessage());
        }
        catch (AttachmentException e) 
        {
        	throw new AttachmentException(e.getMessage());
        }
        catch (Exception e) 
        {
            throw new Exception(e.getMessage(), e);
        }
        return success;
    }
    
    private SynchroAttachment getSynchroAttachment(final Long waiverID) {
        SynchroAttachment synchroAttachment = new SynchroAttachment();
        synchroAttachment.getBean().setObjectId((waiverID).hashCode());
        Integer objectType = SynchroGlobal.buildSynchroAttachmentObjectID(SynchroGlobal.SynchroAttachmentStage.PROJECT_WAIVER.toString()
                , SynchroGlobal.SynchroAttachmentObject.WAIVER_ATTACHMENT.toString());
        synchroAttachment.getBean().setObjectType(objectType);
        return synchroAttachment;
    }
    
    @Override
    public Map<Long,Long> getAttachmentUser(List<AttachmentBean> attachmentBean)
    {
    	Map<Long,Long> attachmentUser = new HashMap<Long,Long>();
    	for(AttachmentBean ab:attachmentBean)
    	{
    		attachmentUser.put(ab.getID(), processWaiverDAONew.getAttachmentUser(ab.getID()));
    	}
    	return attachmentUser;
    }

	public ProcessWaiverDAONew getProcessWaiverDAONew() {
		return processWaiverDAONew;
	}

	public void setProcessWaiverDAONew(ProcessWaiverDAONew processWaiverDAONew) {
		this.processWaiverDAONew = processWaiverDAONew;
	}


}

