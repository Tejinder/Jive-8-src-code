package com.grail.synchro.beans;

/**
 * @author: vivek
 * @since: 1.0
 */
public class PIBStimulus extends BeanObject {

    private Long projectID;
    private Long docID;
    private Boolean actualStick;
    private Boolean actualPack;
    private Boolean mockSticks;
    private Boolean mockPacks;
    private Boolean conceptBoards;
    private Boolean commBoards;
    private Boolean postMaterials;
    private Boolean digitalVisuals;

    public Long getProjectID() {
        return projectID;
    }

    public void setProjectID(final Long projectID) {
        this.projectID = projectID;
    }

    public Long getDocID() {
        return docID;
    }

    public void setDocID(final Long docID) {
        this.docID = docID;
    }

    public Boolean getActualStick() {
        return actualStick;
    }

    public void setActualStick(final Boolean actualStick) {
        this.actualStick = actualStick;
    }

    public Boolean getActualPack() {
        return actualPack;
    }

    public void setActualPack(final Boolean actualPack) {
        this.actualPack = actualPack;
    }

    public Boolean getMockSticks() {
        return mockSticks;
    }

    public void setMockSticks(final Boolean mockSticks) {
        this.mockSticks = mockSticks;
    }

    public Boolean getMockPacks() {
        return mockPacks;
    }

    public void setMockPacks(final Boolean mockPacks) {
        this.mockPacks = mockPacks;
    }

    public Boolean getConceptBoards() {
        return conceptBoards;
    }

    public void setConceptBoards(final Boolean conceptBoards) {
        this.conceptBoards = conceptBoards;
    }

    public Boolean getCommBoards() {
        return commBoards;
    }

    public void setCommBoards(final Boolean commBoards) {
        this.commBoards = commBoards;
    }

    public Boolean getPostMaterials() {
        return postMaterials;
    }

    public void setPostMaterials(final Boolean postMaterials) {
        this.postMaterials = postMaterials;
    }

    public Boolean getDigitalVisuals() {
        return digitalVisuals;
    }

    public void setDigitalVisuals(final Boolean digitalVisuals) {
        this.digitalVisuals = digitalVisuals;
    }
}
