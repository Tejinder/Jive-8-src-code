package com.grail.synchro.manager.impl;

import com.grail.synchro.object.MyLibraryDocument;
import com.grail.synchro.dao.MyLibraryDAO;
import com.grail.synchro.manager.MyLibraryManager;
import com.grail.synchro.objecttype.MyLibraryDocumentObjectType;
import com.grail.synchro.search.filter.MyLibrarySearchFilter;
import com.jivesoftware.base.User;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentManager;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.attachments.AttachmentHelper;
import com.jivesoftware.community.impl.dao.AttachmentBean;
import com.jivesoftware.community.impl.dao.AttachmentDAO;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 */
public class MyLibraryManagerImpl implements MyLibraryManager {

    private MyLibraryDAO myLibraryDAO;
    private AttachmentHelper attachmentHelper;
    private AttachmentManager attachmentManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public MyLibraryDocument get(final Long id) {
        return myLibraryDAO.get(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean addDocument(final String title,final String description,final File attachment,
                               final String fileName, final String contentType) throws IOException, AttachmentException {
        boolean success = false;
        MyLibraryDocument mylib = new MyLibraryDocument();
        mylib.getBean().setTitle(title);
        mylib.getBean().setDescription(description);
        mylib.getBean().setUserId(getUser().getID());
        MyLibraryDocument myLibraryDocument = myLibraryDAO.addDocument(mylib);

        if(myLibraryDocument != null) {
            try {
                attachmentHelper.createAttachment(myLibraryDocument, fileName , contentType, attachment);
                success = true;
            } catch (IOException e) {
                throw new IOException(e.getMessage(), e);
            } catch (AttachmentException e) {
                throw new AttachmentException(e.getMessage(), e);
            }
        }

        return success;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean removeDocument(final Long id) throws AttachmentNotFoundException, AttachmentException, Exception {
        boolean success = false;
        MyLibraryDocument document = myLibraryDAO.get(id);
        if(document != null) {
            try {
                Attachment attachment = attachmentManager.getAttachment(document, document.getBean().getFileName());
                attachmentManager.deleteAttachment(attachment);
                success = myLibraryDAO.removeDocument(id);
            } catch (AttachmentNotFoundException e) {
                throw new AttachmentNotFoundException(e.getMessage());
            } catch (AttachmentException e) {
                throw new AttachmentException(e.getMessage());
            } catch (Exception e) {
                throw new Exception(e.getMessage(), e);
            }
        } else {
            success = false;
        }
        return success;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments(final MyLibrarySearchFilter filter) {
        if(filter.getUserId() == null) {
            filter.setUserId(getUser().getID());
        }
        return myLibraryDAO.getDocuments(filter);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments() {
        return getDocuments(null, getUser().getID(), null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments(final String keyword) {
        return getDocuments(keyword, getUser().getID(), null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments(final Integer start, final Integer limit) {
        return getDocuments(null, getUser().getID(),start, limit);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments(final String keyword, final Integer start, final Integer limit) {
        return getDocuments(keyword, getUser().getID(), start, limit);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments(final Long userId) {
        return getDocuments(null, userId, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments(final String keyword, final Long userId) {
        return getDocuments(keyword, userId, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments(final Long userId, final Integer start, final Integer limit) {
        return getDocuments(null, userId, start, limit);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments(final String keyword, final Long userId,
                                                final Integer start, final Integer limit) {
        return myLibraryDAO.getDocuments(keyword, userId, start, limit);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments(final User user) {
        return getDocuments(null, user.getID(), null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments(final String keyword, final User user) {
        return getDocuments(keyword, user.getID(), null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments(final User user, final Integer start, final Integer limit) {
        return getDocuments(null, user.getID(), start, limit);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public List<MyLibraryDocument> getDocuments(final String keyword, final User user,
                                                final Integer start, final Integer limit) {
        return getDocuments(keyword, user.getID(), start, limit);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public Long getTotalCount() {
        return getTotalCount(null);
    }

    @Override
    public Long getTotalCount(final String keyword) {
        return myLibraryDAO.getTotalCount(keyword, getUser().getID());
    }

    public MyLibraryDAO getMyLibraryDAO() {
        return myLibraryDAO;
    }

    public void setMyLibraryDAO(final MyLibraryDAO myLibraryDAO) {
        this.myLibraryDAO = myLibraryDAO;
    }

    public AttachmentHelper getAttachmentHelper() {
        return attachmentHelper;
    }

    public void setAttachmentHelper(final AttachmentHelper attachmentHelper) {
        this.attachmentHelper = attachmentHelper;
    }

    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public void setAttachmentManager(final AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    private static User getUser() {
        return JiveApplication.getContext().getAuthenticationProvider().getJiveUser();
    }
}
