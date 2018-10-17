package com.grail.dao;

import com.grail.beans.GrailEmailQuery;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/4/14
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GrailEmailQueriesDAO {
    public Long saveQuery(final GrailEmailQuery emailQuery);
    public Long updateQuery(final GrailEmailQuery emailQuery);
    public void updateAttachment(final Long id, final Long attachmentId);
    public void updateEmailStatus(final Long id, final Boolean status);
}
