package com.grail.synchro.beans;



/**
 * @author Kanwar
 * @version 1.0
 */
public class ProjectWaiverApprover extends BeanObject {
	
		private Long waiverID;
		private Long approverID;
	    private String waiverName;
	    private String comments;
	    private Integer isApproved;
		public Long getWaiverID() {
			return waiverID;
		}
		public void setWaiverID(Long waiverID) {
			this.waiverID = waiverID;
		}
		public Long getApproverID() {
			return approverID;
		}
		public void setApproverID(Long approverID) {
			this.approverID = approverID;
		}
		public String getWaiverName() {
			return waiverName;
		}
		public void setWaiverName(String waiverName) {
			this.waiverName = waiverName;
		}
		public String getComments() {
			return comments;
		}
		public void setComments(String comments) {
			this.comments = comments;
		}
		public Integer getIsApproved() {
			return isApproved;
		}
		public void setIsApproved(Integer isApproved) {
			this.isApproved = isApproved;
		}
	   


}
