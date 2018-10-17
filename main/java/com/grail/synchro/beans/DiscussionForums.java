package com.grail.synchro.beans;

import java.util.Comparator;

/**
 * @author Tejinder
 * @version 1.0, Date: 10/11/13
 */
public class DiscussionForums implements Comparable<DiscussionForums> {
	private Long projectId;
	private String discussionName;
	private String author;
	private String creationDate;
	private Long threadId;
	
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
	public String getDiscussionName() {
		return discussionName;
	}
	public void setDiscussionName(String discussionName) {
		this.discussionName = discussionName;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	public Long getThreadId() {
		return threadId;
	}
	public void setThreadId(Long threadId) {
		this.threadId = threadId;
	}
	/*
     * Comparator implementation to Sort Order object based on Project ID
     */
    public static class OrderByID implements Comparator<DiscussionForums> {

        @Override
        public int compare(DiscussionForums o1, DiscussionForums o2) {
            return o1.projectId > o2.projectId ? 1 : (o1.projectId < o2.projectId ? -1 : 0);
        }
    }
    
    public static class OrderByIDDesc implements Comparator<DiscussionForums> {

        @Override
        public int compare(DiscussionForums o1, DiscussionForums o2) {
            return o2.projectId > o1.projectId ? 1 : (o2.projectId < o1.projectId ? -1 : 0);
        }
    }
    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project name.
     */
    public static class OrderByName implements Comparator<DiscussionForums> {

        @Override
        public int compare(DiscussionForums o1, DiscussionForums o2) {
            return o1.discussionName.toLowerCase().compareTo(o2.discussionName.toLowerCase());
        }
    }
    
    public static class OrderByNameDesc implements Comparator<DiscussionForums> {

        @Override
        public int compare(DiscussionForums o1, DiscussionForums o2) {
            return o2.discussionName.toLowerCase().compareTo(o1.discussionName.toLowerCase());
        }
    }
    
    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon project owner.
     */
    public static class OrderByOwner implements Comparator<DiscussionForums> {

        @Override
        public int compare(DiscussionForums o1, DiscussionForums o2) {
            return o1.author.toLowerCase().compareTo(o2.author.toLowerCase());
        }
    }
    
    public static class OrderByOwnerDesc implements Comparator<DiscussionForums> {

        @Override
        public int compare(DiscussionForums o1, DiscussionForums o2) {
            return o2.author.toLowerCase().compareTo(o1.author.toLowerCase());
        }
    }
    /*
     * Comparator implementation to Sort Order object based on Project Year
     */
     
     public static class OrderByDate implements Comparator<DiscussionForums> {

         @Override
         public int compare(DiscussionForums o1, DiscussionForums o2) {
             return o1.creationDate.compareTo(o2.creationDate);
         }
     }
     
     public static class OrderByDateDesc implements Comparator<DiscussionForums> {

         @Override
         public int compare(DiscussionForums o1, DiscussionForums o2) {
             return o2.creationDate.compareTo(o1.creationDate);
         }
     }

    
    @Override
	public int compareTo(DiscussionForums o) {
		return this.projectId > o.projectId ? 1 : (this.projectId < o.projectId ? -1 : 0);
	}
}