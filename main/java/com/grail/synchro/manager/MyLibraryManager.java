package com.grail.synchro.manager;

import com.grail.synchro.object.MyLibraryDocument;
import com.grail.synchro.search.filter.MyLibrarySearchFilter;
import com.jivesoftware.base.User;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public interface MyLibraryManager {
    MyLibraryDocument get(final Long id);
    boolean addDocument(final String title, final String description, final File attachment,
                          final String fileName, final String contentType) throws IOException, AttachmentException;
    boolean removeDocument(final Long id) throws AttachmentNotFoundException, AttachmentException, Exception;
    List<MyLibraryDocument> getDocuments(final MyLibrarySearchFilter filter);
    List<MyLibraryDocument> getDocuments();
    List<MyLibraryDocument> getDocuments(final String keyword);
    List<MyLibraryDocument> getDocuments(final Integer start, final Integer limit);
    List<MyLibraryDocument> getDocuments(final String keyword, final Integer start, final Integer limit);
    List<MyLibraryDocument> getDocuments(final Long userId);
    List<MyLibraryDocument> getDocuments(final String keyword, final Long userId);
    List<MyLibraryDocument> getDocuments(final Long userId, final Integer start, final Integer limit);
    List<MyLibraryDocument> getDocuments(final String keyword, final Long userId, final Integer start, final Integer limit);
    List<MyLibraryDocument> getDocuments(final User user);
    List<MyLibraryDocument> getDocuments(final String keyword, final User user);
    List<MyLibraryDocument> getDocuments(final User user, final Integer start, final Integer limit);
    List<MyLibraryDocument> getDocuments(final String keyword, final User user, final Integer start, final Integer limit);
    Long getTotalCount();
    Long getTotalCount(final String keyword);
}
