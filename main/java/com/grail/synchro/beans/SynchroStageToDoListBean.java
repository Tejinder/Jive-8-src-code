package com.grail.synchro.beans;


/**
 * Representation of a grailstagetodolist in the DAO.
 *
 * @author: Tejinder
 */
public class SynchroStageToDoListBean extends BeanObject {

    private long id;
    private long stageId;
    private String toDoAction;
    private String role;
    //private List<String> toDoActionList;
    private boolean isActive;
    private String notificationRecipients;
    private String subject;
    private String messageBody;
    

    //Default
    public SynchroStageToDoListBean() {
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getStageId() {
		return stageId;
	}

	public void setStageId(long stageId) {
		this.stageId = stageId;
	}

	public String getToDoAction() {
		return toDoAction;
	}

	public void setToDoAction(String toDoAction) {
		this.toDoAction = toDoAction;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

/*	public List<String> getToDoActionList() {
		return toDoActionList;
	}

	public void setToDoActionList(List<String> toDoActionList) {
		this.toDoActionList = toDoActionList;
	}
*/
	public String getNotificationRecipients() {
		return notificationRecipients;
	}

	public void setNotificationRecipients(String notificationRecipients) {
		this.notificationRecipients = notificationRecipients;
	}
	/*@Override
	public boolean equals(Object obj)
	{
		if(obj==null)
		{
			return false;
		}
		if(obj==this)
		{
			return true;
		}
		SynchroStageToDoListBean bean = (SynchroStageToDoListBean)obj;
		if(this.toDoAction.equals(bean.toDoAction))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
*/

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}
    
}
