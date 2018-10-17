package com.grail.synchro.manager;

import com.grail.synchro.beans.DefaultTemplateBean;
import com.grail.synchro.object.DefaultTemplate;
import com.jivesoftware.community.AttachmentException;
import com.jivesoftware.community.AttachmentNotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 6/30/14
 * Time: 6:40 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DefaultTemplateManager {
    DefaultTemplate get(final Long id);
    DefaultTemplate getByAttachmentId(final Long attachmentId);
    Map<Integer, List<DefaultTemplateBean>> getAll()  throws UnsupportedEncodingException;
    public boolean save(final DefaultTemplateBean defaultTemplateBean, final String fileName,
                        final String contentType, final File attachment) throws IOException, AttachmentException;
    boolean deleteByAttachmentId(final Long attachmentId) throws AttachmentNotFoundException, AttachmentException, Exception;
    boolean delete(final Long id) throws AttachmentNotFoundException, AttachmentException, Exception;
}
