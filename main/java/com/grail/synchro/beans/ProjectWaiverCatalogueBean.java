package com.grail.synchro.beans;

import java.util.Comparator;
import java.util.List;

import com.jivesoftware.community.impl.dao.AttachmentBean;


/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
public class ProjectWaiverCatalogueBean extends BeanObject implements Comparable<ProjectWaiverCatalogueBean> {

    private Long waiverID;
    private String waiverName;
    private String initiator;
    private Integer year;
    private String region;
    private String country;
    private String brand;
    private String approver;
    private String status;
    private String waiverUrl;
    
    private List<Long> endMarkets;
    private String approverComments;
    private String summary;
    private Long brandPopUp = -1L;
    
    private List<AttachmentBean> attachments;

    public ProjectWaiverCatalogueBean() {
    }

    public ProjectWaiverCatalogueBean(Long waiverID, String waiverName, String initiator, Integer year,
                                      String region, String country, String brand, String approver, String status) {
        this.waiverID = waiverID;
        this.waiverName = waiverName;
        this.initiator = initiator;
        this.year = year;
        this.region = region;
        this.country = country;
        this.brand = brand;
        this.approver = approver;
        this.status = status;
    }

    public Long getWaiverID() {
        return waiverID;
    }

    public void setWaiverID(Long waiverID) {
        this.waiverID = waiverID;
    }

    public String getWaiverName() {
        return waiverName;
    }

    public void setWaiverName(String waiverName) {
        this.waiverName = waiverName;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    

	/*
     * Comparator implementation to Sort Order object based on waiver ID
     */
    public static class OrderByID implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o1.waiverID > o2.waiverID ? 1 : (o1.waiverID < o2.waiverID ? -1 : 0);
        }
    }
    
    public static class OrderByIDDesc implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o2.waiverID > o1.waiverID ? 1 : (o2.waiverID < o1.waiverID ? -1 : 0);
        }
    }

    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon waiver name.
     */
    public static class OrderByName implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o1.waiverName.toLowerCase().compareTo(o2.waiverName.toLowerCase());
        }
    }
    
    public static class OrderByNameDesc implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o2.waiverName.toLowerCase().compareTo(o1.waiverName.toLowerCase());
        }
    }
    
    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon waiver owner.
     */
    public static class OrderByOwner implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o1.initiator.toLowerCase().compareTo(o2.initiator.toLowerCase());
        }
    }
    
    public static class OrderByOwnerDesc implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o2.initiator.toLowerCase().compareTo(o1.initiator.toLowerCase());
        }
    }
    
    /*
    * Comparator implementation to Sort Order object based on waiver Year
    */
    
    public static class OrderByYear implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o1.year.compareTo(o2.year);
        }
    }
    
    public static class OrderByYearDesc implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o2.year.compareTo(o1.year);
        }
    }


    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon waiver owner.
     */
    public static class OrderByBrand implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o1.brand.toLowerCase().compareTo(o2.brand.toLowerCase());
        }
    }
    
    public static class OrderByBrandDesc implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o2.brand.toLowerCase().compareTo(o1.brand.toLowerCase());
        }
    }
    
    
    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon waiver approver
     */
    public static class OrderByApprover implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o1.status.toLowerCase().compareTo(o2.status.toLowerCase());
        }
    }
    
    public static class OrderByApproverDesc implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o2.approver.toLowerCase().compareTo(o1.approver.toLowerCase());
        }
    }
    
    
    /*
     * Comparator implementation or Comparator interface to sort list of Order object
     * based upon waiver status.
     */
    public static class OrderByStatus implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o1.status.toLowerCase().compareTo(o2.status.toLowerCase());
        }
    }
    
    public static class OrderByStatusDesc implements Comparator<ProjectWaiverCatalogueBean> {

        @Override
        public int compare(ProjectWaiverCatalogueBean o1, ProjectWaiverCatalogueBean o2) {
            return o2.status.toLowerCase().compareTo(o1.status.toLowerCase());
        }
    }
    
    
	@Override
	public int compareTo(ProjectWaiverCatalogueBean o) {
		return this.waiverID > o.waiverID ? 1 : (this.waiverID < o.waiverID ? -1 : 0);
	}

	public String getWaiverUrl() {
		return waiverUrl;
	}

	public void setWaiverUrl(String waiverUrl) {
		this.waiverUrl = waiverUrl;
	}

	public List<Long> getEndMarkets() {
		return endMarkets;
	}

	public void setEndMarkets(List<Long> endMarkets) {
		this.endMarkets = endMarkets;
	}

	public String getApproverComments() {
		return approverComments;
	}

	public void setApproverComments(String approverComments) {
		this.approverComments = approverComments;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Long getBrandPopUp() {
		return brandPopUp;
	}

	public void setBrandPopUp(Long brandPopUp) {
		this.brandPopUp = brandPopUp;
	}

	public List<AttachmentBean> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentBean> attachments) {
		this.attachments = attachments;
	}
}
