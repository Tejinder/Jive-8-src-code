package com.grail.synchro.dao;

import com.grail.synchro.beans.MyLibraryDocumentBean;
import com.grail.synchro.object.MyLibraryDocument;
import com.grail.synchro.search.filter.MyLibrarySearchFilter;
import com.jivesoftware.base.User;

import java.util.List;

/**
 *
 */
public interface MyLibraryDAO {
    MyLibraryDocument get(final Long id);
    MyLibraryDocument addDocument(final MyLibraryDocument obj);
    boolean removeDocument(final Long id);
    List<MyLibraryDocument> getDocuments(final MyLibrarySearchFilter filter);
    List<MyLibraryDocument> getDocuments();
    List<MyLibraryDocument> getDocuments(final Long userID);
    List<MyLibraryDocument> getDocuments(final Integer start, final Integer limit);
    List<MyLibraryDocument> getDocuments(final Long userID, final Integer start, final Integer limit);
    List<MyLibraryDocument> getDocuments(final String keyword, final Long userID, final Integer start, final Integer limit);
    Long getTotalCount(final Long userId);
    Long getTotalCount(final String keyword, final Long userId);
}
