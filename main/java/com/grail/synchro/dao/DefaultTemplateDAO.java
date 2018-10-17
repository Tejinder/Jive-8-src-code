package com.grail.synchro.dao;

import com.grail.synchro.object.DefaultTemplate;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 7/1/14
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
public interface DefaultTemplateDAO {
    DefaultTemplate get(final Long id);
    DefaultTemplate getByAttachmentId(final Long attachmentId);
    List<DefaultTemplate> getAll();
    List<DefaultTemplate> getAllById(final Long id);
    boolean save(final DefaultTemplate defaultTemplate);
    boolean update(final DefaultTemplate defaultTemplate);
    boolean delete(final Long id);
}
