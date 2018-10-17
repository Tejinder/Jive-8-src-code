/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community;

import com.jivesoftware.util.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Filters and sorts lists of {@link Document}s. This allows for a very rich set of possible queries that
 * can be run. Some examples are: "Show all documents in the community, sorted by their modification
 * date" or "Show all documents in the community created after xxx and with the extended property
 * 'test' having a value of '12345'".<p>
 * <p/>
 * The class also supports pagination of results with the setStartIndex(int) and setNumResults(int)
 * methods. If the start index is not set, it will begin at index 0 (the start of results). If the
 * number of results is not set, it will be unbounded and return as many results as available.<p>
 * <p/>
 * Factory methods to create common queries are provided for convenience.
 *
 * @javadoc api
 */
public class DocumentResultFilter extends ResultFilter {
    
    public static final int NOT_MODE = 20061115;
    
    private Date expirationDateRangeMin = null;
    private Date expirationDateRangeMax = null;
    private boolean includeUnSearchable = true;
    private boolean includeRecommended = true;
    private boolean includeNonRecommended = true;
    private List<DocumentType> documentTypes = new ArrayList<DocumentType>();
    private List<String> languages = new ArrayList<>();
    private List<DocumentState> documentStates = new ArrayList<DocumentState>();
    private int documentFieldMode = AND_MODE;
    private List<DocumentField> documentFields = new ArrayList<DocumentField>();
    private List<Object> documentFieldValues = new ArrayList<Object>();
    private DocumentField sortDocumentField = null;
    private boolean onlyIncludeCollaborativeDocuments = false;
    private int binaryBodyMode = OR_MODE;
    private List<String> binaryBodyContentType = new ArrayList<>();
    private boolean includeAuthorsInUserFilter;
    private boolean includeReviewersInUserFilter;
    private boolean restrictToLatestVersion;
    private boolean excludeOwner;
    private boolean retrieveContainerInfo;
    private Long exStorageFileVersionID;

    private boolean isFromQuickSearchWidget = false;
    private boolean isSearchBody = false;
    private String searchBodyParam;
    
    public boolean isView() {
        return isView;
    }

    public void setView(boolean view) {
        isView = view;
    }

    private boolean isView = false;

    public boolean isFromMostRatedWidget() {
        return isFromMostRatedWidget;
    }

    public void setFromMostRatedWidget(boolean fromMostRatedWidget) {
        isFromMostRatedWidget = fromMostRatedWidget;
    }

    private boolean isFromMostRatedWidget = false;

    public boolean isFromMostViewedWidget() {
        return isFromMostViewedWidget;
    }

    public void setFromMostViewedWidget(boolean fromMostViewedWidget) {
        isFromMostViewedWidget = fromMostViewedWidget;
    }

    private boolean isFromMostViewedWidget = false;
    
    /**
     * Creates a default document ResultFilter: no filtering with results sorted on the document's creation
     * date in descending order.
     */
    public static DocumentResultFilter createDefaultFilter() {
        DocumentResultFilter resultFilter = new DocumentResultFilter();
        resultFilter.setSortField(JiveConstants.CREATION_DATE);
        resultFilter.setSortOrder(DESCENDING);
        resultFilter.addDocumentState(DocumentState.PUBLISHED);
        long now = System.currentTimeMillis();
        Date expirationDate = new Date(now - (now % (JiveConstants.DAY)));
        resultFilter.setExpirationDateRangeMin(expirationDate);
        return resultFilter;
    }

    public static DocumentResultFilter createAllStatesFilter() {
        DocumentResultFilter resultFilter = new DocumentResultFilter();
        resultFilter.setSortField(JiveConstants.CREATION_DATE);
        resultFilter.setSortOrder(DESCENDING);
        long now = System.currentTimeMillis();
        Date expirationDate = new Date(now - (now % (JiveConstants.DAY)));
        resultFilter.setExpirationDateRangeMin(expirationDate);
        return resultFilter;
    }

    /**
     * Returns a date that represents the lower boundry for documents to be selected by the result
     * filter. If this value is not set it will return null and the results filter will return only
     * documents that have not yet expired.
     *
     * @return a date representing the lower value of the expiration date to be selected.
     */
    public Date getExpirationDateRangeMin() {
        return DateUtils.newInstance(expirationDateRangeMin);
    }

    /**
     * Sets a date that represents the lower boundry for documents to be selected by the result
     * filter. If this value is not set the results filter will return only documents that have not
     * yet expired.
     * <p/>
     * Setting a date range for a ResultFilter is a potential performance bottleneck. For example,
     * if the argument for the date range is "new Date()" then the corresponding database query will
     * map to an accuracy of a particular millisecond in time. This means that the results can't be
     * cached. A better solution is to round dates to the nearest minute, hour, etc (whatever
     * accuracy you need).
     *
     * @param expirationDateRangeMin a date representing the highest value of the expiration date
     * range to be selected.
     */
    public void setExpirationDateRangeMin(Date expirationDateRangeMin) {
        this.expirationDateRangeMin = DateUtils.newInstance(expirationDateRangeMin);
    }

    /**
     * Returns a date that represents the upper boundry for documents to be selected by the result
     * filter. If this value is not set it will return null and the results filter will be unbounded
     * for the latest expiration date selected.
     *
     * @return a date representing the highest value of the expiration date to be selected.
     */
    public Date getExpirationDateRangeMax() {
        return DateUtils.newInstance(expirationDateRangeMax);
    }

    /**
     * Sets a date that represents the upper boundry for documents to be selected by the result
     * filter. If this value is not set the results filter will be unbounded for the latest
     * expiration date selected.
     * <p/>
     * Setting a date range for a ResultFilter is a potential performance bottleneck. For example,
     * if the argument for the date range is "new Date()" then the corresponding database query will
     * map to an accuracy of a particular millisecond in time. This means that the results can't be
     * cached. A better solution is to round dates to the nearest minute, hour, etc (whatever
     * accuracy you need).
     *
     * @param expirationDateRangeMax a date representing the highest value of the expiration date
     * range to be selected.
     */
    public void setExpirationDateRangeMax(Date expirationDateRangeMax) {
        this.expirationDateRangeMax = DateUtils.newInstance(expirationDateRangeMax);
    }

    /**
     * Returns whether the result to be selected by the result filter should include documents with
     * a searchable attribute set to false. The default value is true, or to include unsearchable
     * documents.
     *
     * @return whether the result to be selected by the result filter should include searchable
     *         documents or not.
     */
    public boolean isIncludeUnSearchableDocuments() {
        return includeUnSearchable;
    }

    /**
     * Sets whether the result to be selected by the result filter should include documents with a
     * searchable attribute set to false. The default value is true, or to include unsearchable
     * documents.
     *
     * @param include true to include searchable documents, false otherwise.
     */
    public void setIncludeUnSearchableDocuments(boolean include) {
        this.includeUnSearchable = include;
    }

    /**
     * Returns whether the result to be selected by the result filter should include documents with
     * a recommended attribute set to true. The default value is true, or to include recommended
     * documents.
     *
     * @return whether the result to be selected by the result filter should include recommended
     *         documents or not.
     */
    public boolean isIncludeRecommendedDocuments() {
        return includeRecommended;
    }

    /**
     * Sets whether the result to be selected by the result filter should include documents with a
     * recommended attribute set to true. The default value is true, or to include recommended
     * documents.
     *
     * @param include true to include recommended documents, false otherwise.
     */
    public void setIncludeRecommendedDocuments(boolean include) {
        this.includeRecommended = include;
    }

    /**
     * Returns whether the result to be selected by the result filter should include documents with
     * a recommended attribute set to false. The default value is true, or to include unrecommended
     * documents.
     *
     * @return whether the result to be selected by the result filter should include unrecommended
     *         documents or not.
     */
    public boolean isIncludeNonRecommendedDocuments() {
        return includeNonRecommended;
    }

    /**
     * Sets whether the result to be selected by the result filter should include documents with a
     * recommended attribute set to false. The default value is true, or to include unrecommended
     * documents.
     *
     * @param include true to include unrecommended documents, false otherwise.
     */
    public void setIncludeNonRecommendedDocuments(boolean include) {
        this.includeNonRecommended = include;
    }

    /**
     * Returns the number of document types that results will be filtered on.
     *
     * @return the number of document types that results will be filtered on.
     */
    public int getDocumentTypesCount() {
        return documentTypes.size();
    }

    /**
     * Returns the list of document types to filter results selected by the result filter. If no
     * document types were specified filtering will include all document types.
     *
     * @return the list of document types to filter results selected by the result filter.
     */
    public Iterator<DocumentType> getDocumentTypes() {
        return Collections.unmodifiableList(documentTypes).iterator();
    }

    /**
     * Sets the document type to filter results selected by the result filter. If this value is not
     * set the results filter will not be filtered to any specific document type.
     *
     * @param type the document type to filter results selected by the result filter.
     */
    public void addDocumentType(DocumentType type) {
        if (!documentTypes.contains(type)) {
            this.documentTypes.add(type);
        }
    }

    /**
     * Add a document type to the list of document states that will be filtered upon.
     *
     * @param type a document type
     */
    public void deleteDocumentType(DocumentType type) {
        if (documentTypes.contains(type)) {
            this.documentTypes.remove(type);
        }
    }

    /**
     * Returns the list of languages to filter results selected by the result filter. If no
     * languages were specified filtering will not occur based upon languages.
     *
     * @return the list of languages to filter results selected by the result filter.
     */
    public List<String> getLanguages() {
        return languages;
    }

    /**
     * Add a language to the list of languages that will be filtered upon.
     *
     * @param language an ISO-639 language code.
     */
    public void addLanguage(String language) {
        if (!languages.contains(language)) {
            languages.add(language);
        }
    }

    /**
     * Deletes a language from the list of languages that will be filtered upon.
     *
     * @param language an ISO-639 language code.
     */
    public void deleteLanguage(String language) {
        if (languages.contains(language)) {
            languages.remove(language);
        }
    }

    /**
     * Returns true if users who have been added as authors to a document
     * should be included when filtering by user.
     *
     * @return true if users who have been added as authors to a document
     * should be included when filtering by user.
     */
    public boolean isIncludeAuthorsInUserFilter() {
        return includeAuthorsInUserFilter;
    }

    /**
     * Sets whether users who have been added as authors to a document
     * should be included when filtering by user.
     *
     * @param includeAuthorsInUserFilter true when users who have been added as authors to a document
     * should be included when filtering by user.
     */
    public void setIncludeAuthorsInUserFilter(boolean includeAuthorsInUserFilter) {
        this.includeAuthorsInUserFilter = includeAuthorsInUserFilter;
    }

    /**
     * Returns true if users who have been added as reviewers to a document
     * should be included when filtering by user.
     *
     * @return true if users who have been added as reviewers to a document
     * should be included when filtering by user.
     */
    public boolean isIncludeReviewersInUserFilter() {
        return includeReviewersInUserFilter;
    }

    /**
     * Sets whether users who have been added as reviewers to a document
     * should be included when filtering by user.
     *
     * @param includeReviewersInUserFilter true when users who have been added as reviewers to a document
     * should be included when filtering by user.
     */
    public void setIncludeReviewersInUserFilter(boolean includeReviewersInUserFilter) {
        this.includeReviewersInUserFilter = includeReviewersInUserFilter;
    }

    /**
     * Returns the number of document states that results will be filtered on.
     *
     * @return the number of document states that results will be filtered on.
     */
    public int getDocumentStateCount() {
        return documentStates.size();
    }

    /**
     * Returns the list of document states to filter results selected by the result filter. If no
     * document states were specified filtering will only include documents with the state of {@link
     * DocumentState#PUBLISHED}.
     *
     * @return the list of document states to filter results selected by the result filter.
     */
    public Iterator<DocumentState> getDocumentStates() {
        return Collections.unmodifiableList(documentStates).iterator();
    }

    /**
     * Add a document state to the list of document states that will be filtered upon.
     *
     * @param state a document state
     */
    public void addDocumentState(DocumentState state) {
        if (!documentStates.contains(state)) {
            documentStates.add(state);
        }
    }

    /**
     * Deletes a document state from the list of document states that will be filtered upon.
     *
     * @param state a document state to remove
     */
    public void deleteDocumentState(DocumentState state) {
        if (documentStates.contains(state)) {
            documentStates.remove(state);
        }
    }

    /**
     * Returns the mode that will be used to select results if multiple document field values have
     * been specified. By default the mode is {@link #AND_MODE} which specifies that all document
     * field values must match for the object to be selected. {@link #OR_MODE} is useful if you want
     * objects to be returned if any of the document field values match.
     *
     * @return the mode that will be used to select results if multiple document field values have
     *         been specified.
     */
    public int getDocumentFieldMode() {
        return documentFieldMode;
    }

    /**
     * Sets the mode that will be used to select results if multiple document field values have been
     * specified. {@link #AND_MODE} (the default) specifies that all document field values must
     * match for the object to be selected. {@link #OR_MODE} is useful if you want objects to be
     * returned if any of the document field values match.
     *
     * @param documentFieldMode the mode that will be used to select results if multiple document
     * field values have been specified.
     */
    public void setDocumentFieldMode(int documentFieldMode) {
        if (documentFieldMode == AND_MODE || documentFieldMode == OR_MODE) {
            this.documentFieldMode = documentFieldMode;
        }
        else {
            throw new IllegalArgumentException(
                    "Unknown document field mode specified: " + documentFieldMode);
        }
    }

    public int getDocumentFieldCount() {
        return documentFields.size();
    }

    /**
     * Returns the document field at the specified index in the list of document fields to be
     * filtered on. If the index is invalid, null will be returned.
     *
     * @return the document field at the specified index in the document field filter list.
     */
    public DocumentField getDocumentField(int index) {
        if (index >= 0 && index < documentFields.size()) {
            return (DocumentField) documentFields.get(index);
        }
        else {
            return null;
        }
    }

    /**
     * Returns the optionID(s) at the specified index in the list of DocumentFields to be filtered
     * on. If the index is invalid, null will be returned. The returned object will either be a Long
     * or a list of Long values.<p>
     *
     * @return the optionID at the specified index in the document field filter list.
     */
    public Object getDocumentFieldValue(int index) {
        if (index >= 0 && index < documentFieldValues.size()) {
            return documentFieldValues.get(index);
        }
        else {
            return null;
        }
    }

    /**
     * Adds a document field option to the list of options that will be filtered upon
     *
     * @param field a document field with a {@link DocumentFieldType.DataType#LIST_SINGLE} or
     *      {@link DocumentFieldType.DataType#LIST_MULTI} data type
     * @param optionID an option from the field 
     */
    @SuppressWarnings("unchecked")
	public void addDocumentField(DocumentField field, long optionID) {
        // AND mode, Overwrite any existing entry in the documentField list with the same name.
        if (documentFields.contains(field) && documentFieldMode == AND_MODE) {
            int index = documentFields.indexOf(field);
            documentFields.remove(index);
            documentFieldValues.remove(index);
        }
        // OR mode, existing documentField, same matchType
        else if (documentFields.contains(field) && documentFieldMode == OR_MODE) {
            int index = documentFields.indexOf(field);
            Object o = documentFieldValues.get(index);
            List<Object> values;

            if (o instanceof List) {
                values = (List<Object>) o;
            }
            else {
                values = new ArrayList<Object>();
                values.add(o);
                values.add(new Long(optionID));
            }

            documentFieldValues.set(index, values);
            return;
        }

        documentFields.add(field);
        documentFieldValues.add(optionID);
    }


    /**
     * Returns the document field that will be sorted on. Returns null if sorting will not be done
     * on a document field.
     *
     * @return the document field that will be sorted on.
     */
    public DocumentField getSortDocumentField() {
        return sortDocumentField;
    }

    /**
     * Sets the document field to sort on. You must also call the setSortField(int) method with
     * JiveConstants.DOCUMENT_FIELD as an argument. Attempting to sort on a document field that
     * does not have the filterable attribute set to true will result in a IllegalArgumentException
     * being thrown.
     *
     * @param field the document field to sort on.
     * @throws IllegalArgumentException if the document field is unfilterable
     */
    public void setSortDocumentField(DocumentField field) {
        if (!field.getAttributes().isFilterable()) {
            throw new IllegalArgumentException(
                    "Field " + field.getName() + " cannot be sorted upon");
        }
        this.sortDocumentField = field;
    }

    /**
     * Returns true to only include collaborative documents (documents having textual bodies instead
     * of binary bodies) in the list of document to filter upon. Default is false.
     * 
     * @return true to only include collaborative documents, false to include both text and binary 
     *      body documents in the filtered results.
     */
    public boolean isOnlyIncludeCollaborativeDocuments() {
        return onlyIncludeCollaborativeDocuments;
    }

    /**
     * Set to true to only include collaborative documents (documents having textual bodies instead
     * of binary bodies) in the list of document to filter upon. Default is false.
     *  
     * @param onlyIncludeCollaborativeDocuments true to only include textual documents in the filtered
     *  results, false to include both text and binary body documents in the filtered results.
     */
    public void setOnlyIncludeCollaborativeDocuments(boolean onlyIncludeCollaborativeDocuments) {
        this.onlyIncludeCollaborativeDocuments = onlyIncludeCollaborativeDocuments;
    }

    /**
     * Returns the mode that will be used to select results if binary body content types have
     * been specified. By default the mode is {@link #OR_MODE} which specifies that any specified 
     * content type must match for the object to be selected. {@link #NOT_MODE} is useful if you want
     * objects to be returned if none of the specified content types match for the object to be 
     * selected.
     *
     * @return the mode that will be used to select results if binary body content types have
     *      been specified.
     */
    public int getBinaryBodyMode() {
        return binaryBodyMode;
    }

    /**
     * Sets the mode that will be used to select results if binary body content types have
     * been specified. {@link #OR_MODE} (the default) specifies that any specified 
     * content type must match for the object to be selected. {@link #NOT_MODE} is useful if you want
     * objects to be returned if none of the specified content types match for the object to be 
     * selected
     *
     * @param binaryBodyMode the mode that will be used to select results if binary body content types have
     *      been specified.
     */
    public void setBinaryBodyMode(int binaryBodyMode) {
        if (binaryBodyMode == OR_MODE || binaryBodyMode == NOT_MODE) {
            this.binaryBodyMode = binaryBodyMode;
        }
        else {
            throw new IllegalArgumentException(
                    "Unknown binary body mode specified: " + binaryBodyMode);
        }
    }
    
    /**
     * Returns the count of the content types that will be used to filter binary documents
     * 
     * @return the count of the content types that will be used to filter binary documents
     */
    public int getBinaryBodyContentTypeCount() {
        return binaryBodyContentType.size();
    }

    /**
     * Returns the binary body content type at the specified index. If the index is invalid, null 
     * will be returned.
     *  
     * @param index the index
     * @return the binary body content type at the specified index
     */
    public String getBinaryBodyContentType(int index) {
        if (index >= 0 && index < binaryBodyContentType.size()) {
            return binaryBodyContentType.get(index);
        }
        else {
            return null;
        }
    }

    /**
     * Adds a content type to the list of content types that binary body documents will be filtered 
     * upon. Adding a binary body content type to the result filter will result in only 
     * documents with binary bodies being filtered - normal textual documents will not be included
     * in the results. This may conflict with the {@link #isOnlyIncludeCollaborativeDocuments()} 
     * setting, in which case the results are undefined.
     * 
     * <p>Adding multiple content types results in 'OR' type filtering since documents cannot have 
     * multiple content types.</p>
     * 
     * <p>Note that wildcards (*) are allowed such that adding a content type such as image/* will 
     * include all documents with an image based binary body being included in the filtered 
     * results.</p> 
     * 
     * @param contentType the binary body content type to add to the filter.
     */
    public void addBinaryBodyContentType(String contentType) {
        if (!this.binaryBodyContentType.contains(contentType) && contentType != null) {
            this.binaryBodyContentType.add(contentType.trim().toLowerCase());
        }
    }

    /**
     * Returns true if only the latest document version will be tested when filtering on document state.
     * @return true if only the latest document version will be tested when filtering on document state.
     */
    public boolean isRestrictToLatestVersion() {
        return restrictToLatestVersion;
    }

    /**
     * By default, all versions of a document will be tested when filtering on document state. Set to true
     * to restrict this test to only the lates document version.
     * @param restrictToLatestVersion set true to restrict this test to only the lates document version.
     */
    public void setRestrictToLatestVersion(boolean restrictToLatestVersion) {
        this.restrictToLatestVersion = restrictToLatestVersion;
    }

    /**
     * Returns true if the document owner should be excluded when filtering by user id.
     *
     * @return true if the document owner should be excluded when filtering by user id.
     */
    public boolean isExcludeOwnerInUserFilter() {
        return excludeOwner;
    }

    /**
    * Sets whether the document owner should be excluded when filtering by user id.
    *
    * @param excludeOwner true when the document owner should be excluded when filtering by user id.
    * @throws IllegalStateException if this filter is not set to include authors or reviewers when filtering
    * by user id.
    */
    public void setExcludeOwnerInUserFilter(boolean excludeOwner) {
        if (isIncludeAuthorsInUserFilter() || isIncludeReviewersInUserFilter()) {
            this.excludeOwner = excludeOwner;
        }
        else {
            throw new IllegalStateException(
                    "DocumentResultFilter must include authors or reviewers in user filter if owner is to be excluded");
        }
    }

    /**
     * Indicates whether the containerID and containerType should be selected
     * @return
     */
    public boolean isRetrieveContainerInfo() {
        return retrieveContainerInfo;
    }

    /**
     * Sets whether the containerID and containerType are also retrieved
     * @param retrieveContainerInfo true if container info should be retrieved
     */
    public void setRetrieveContainerInfo(boolean retrieveContainerInfo) {
        this.retrieveContainerInfo = retrieveContainerInfo;
    }

    /**
     * Returns the exStorageFileVersionID to filter the document versions by.
     * @return
     */
    public Long getExStorageFileVersionID() {
        return exStorageFileVersionID;
    }

    /**
     * Sets the exStorageFileVersionID to filter the document versions by.
     * @param exStorageFileVersionID
     */
    public void setExStorageFileVersionID(long exStorageFileVersionID) {
        this.exStorageFileVersionID = exStorageFileVersionID;
    }

	public boolean isFromQuickSearchWidget() {
		return isFromQuickSearchWidget;
	}

	public void setFromQuickSearchWidget(boolean isFromQuickSearchWidget) {
		this.isFromQuickSearchWidget = isFromQuickSearchWidget;
	}

	public boolean isSearchBody() {
		return isSearchBody;
	}

	public void setSearchBody(boolean isSearchBody) {
		this.isSearchBody = isSearchBody;
	}

	public String getSearchBodyParam() {
		return searchBodyParam;
	}

	public void setSearchBodyParam(String searchBodyParam) {
		this.searchBodyParam = searchBodyParam;
	}
}
