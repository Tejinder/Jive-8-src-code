package com.grail.synchro.dao;

import com.grail.synchro.beans.AutoSaveDetailsBean;
import com.grail.synchro.exceptions.AutoSaveDetailsNotFoundException;
import com.grail.synchro.exceptions.AutoSavePersistException;

/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
public interface AutoSaveDAO {
    void saveDetails(final AutoSaveDetailsBean bean) throws AutoSavePersistException;
    void updateDetails(final AutoSaveDetailsBean bean) throws AutoSavePersistException;
    public AutoSaveDetailsBean getDetails(final Long objectType, final Long objectID,
                                          final Long userID) throws AutoSaveDetailsNotFoundException;
    AutoSaveDetailsBean getDraftDetails(final Long userID, final boolean isDraft) throws AutoSaveDetailsNotFoundException;
    boolean deleteDetails(final Long objectType, final Long objectID, final Long userID);

}
