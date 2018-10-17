package com.grail.synchro.dao;

import com.grail.synchro.beans.EmailNotificationDetailsBean;

/**
 * @author tejinder
 * @version 1.0
 */
public interface EmailNotificationDAO {
    void saveDetails(final EmailNotificationDetailsBean bean);
}
