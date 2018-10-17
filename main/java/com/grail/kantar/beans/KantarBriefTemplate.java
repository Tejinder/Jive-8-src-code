package com.grail.kantar.beans;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/16/14
 * Time: 1:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarBriefTemplate {
    private Long id;
    private String researchNeedsPriorities;
    private String hypothesisBusinessNeed;
    private Long markets;
    private String products;
    private String brands;
    private String categories;
    private Date deliveryDate;
    private Integer outputFormat;
    private String recipientEmail;
    private Long sender;
    private Date capturedDate;
    private boolean isDraft = false;
    private Long batContact;
    private Integer status;
    private Long methodologyType;
    private BigDecimal finalCost;
    private Long finalCostCurrency = -1L;
    private String dataSource;
    private Long createdBy;
    private Date creationDate;
    private Long modifiedBy;
    private Date modificationDate;
    private String comments;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResearchNeedsPriorities() {
        return researchNeedsPriorities;
    }

    public void setResearchNeedsPriorities(String researchNeedsPriorities) {
        this.researchNeedsPriorities = researchNeedsPriorities;
    }

    public String getHypothesisBusinessNeed() {
        return hypothesisBusinessNeed;
    }

    public void setHypothesisBusinessNeed(String hypothesisBusinessNeed) {
        this.hypothesisBusinessNeed = hypothesisBusinessNeed;
    }

    public Long getMarkets() {
        return markets;
    }

    public void setMarkets(Long markets) {
        this.markets = markets;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public String getBrands() {
        return brands;
    }

    public void setBrands(String brands) {
        this.brands = brands;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Integer getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(Integer outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public Long getSender() {
        return sender;
    }

    public void setSender(Long sender) {
        this.sender = sender;
    }

    public Date getCapturedDate() {
        return capturedDate;
    }

    public void setCapturedDate(Date capturedDate) {
        this.capturedDate = capturedDate;
    }

    public boolean getDraft() {
        return isDraft;
    }

    public void setDraft(boolean draft) {
        isDraft = draft;
    }

    public Long getBatContact() {
        return batContact;
    }

    public void setBatContact(Long batContact) {
        this.batContact = batContact;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getMethodologyType() {
        return methodologyType;
    }

    public void setMethodologyType(Long methodologyType) {
        this.methodologyType = methodologyType;
    }

    public BigDecimal getFinalCost() {
        return finalCost;
    }

    public void setFinalCost(BigDecimal finalCost) {
        this.finalCost = finalCost;
    }

    public Long getFinalCostCurrency() {
        return finalCostCurrency;
    }

    public void setFinalCostCurrency(Long finalCostCurrency) {
        this.finalCostCurrency = finalCostCurrency;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
