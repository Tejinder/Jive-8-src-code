package com.grail.synchro.beans;

import java.util.Map;

/**
 * @author Samee K.S
 * @version 1.0, Date: 5/24/13
 */
public class BeanObject {
    private Long creationBy;
    private Long modifiedBy;
    private Long creationDate;
    private Long modifiedDate;
    private Map<String, Object> properties;

    public Long getCreationBy() {
        return creationBy;
    }

    public void setCreationBy(final Long creationBy) {
        this.creationBy = creationBy;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Long creationDate) {
        this.creationDate = creationDate;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(final Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Long getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(final Long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(final Map<String, Object> properties) {
        this.properties = properties;
    }
}
