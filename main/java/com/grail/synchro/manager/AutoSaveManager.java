package com.grail.synchro.manager;

import com.grail.synchro.beans.AutoSaveDetailsBean;
import com.grail.synchro.exceptions.AutoSaveDetailsNotFoundException;
import com.grail.synchro.exceptions.AutoSavePersistException;

/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
public interface AutoSaveManager {
    void saveDetails(final String objectType, final Long objectID, final String data)
            throws AutoSavePersistException, AutoSaveDetailsNotFoundException;
    void saveDetails(final AutoSaveDetailsBean bean)
            throws AutoSavePersistException, AutoSaveDetailsNotFoundException;
    AutoSaveDetailsBean getDetails(final String objectType, final Long objectID) throws AutoSaveDetailsNotFoundException;
    AutoSaveDetailsBean getDetails(final Long objectType, final Long objectID, final Long userID) throws AutoSaveDetailsNotFoundException;
    AutoSaveDetailsBean getDraftDetails(final Long userID) throws AutoSaveDetailsNotFoundException;
    boolean deleteDetails(final String objectType, final Long objectID);
    boolean deleteDetails(final Long objectType, final Long objectID, final Long userID);
}
