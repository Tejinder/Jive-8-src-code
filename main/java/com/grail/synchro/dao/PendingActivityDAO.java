package com.grail.synchro.dao;

import com.grail.synchro.beans.PendingActivity;
import com.grail.synchro.beans.ProjectPendingActivityViewBean;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/20/14
 * Time: 3:14 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PendingActivityDAO {
    void save(final PendingActivity activity);
    void update(final PendingActivity activity);
    List<PendingActivity> get(final Long projectId, final Long endmarketId);
    List<PendingActivity> get(final Long projectId);
}
