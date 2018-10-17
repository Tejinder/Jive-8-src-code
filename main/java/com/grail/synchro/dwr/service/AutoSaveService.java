package com.grail.synchro.dwr.service;

import com.grail.synchro.beans.AutoSaveDetailsBean;
import com.grail.synchro.exceptions.AutoSaveDetailsNotFoundException;
import com.grail.synchro.exceptions.AutoSavePersistException;
import com.grail.synchro.manager.AutoSaveManager;
import com.jivesoftware.community.dwr.RemoteSupport;
import org.apache.log4j.Logger;

/**
 * @author Bhaskar Avulapati
 * @version 1.0
 */
public class AutoSaveService extends RemoteSupport {
    private static final Logger LOG = Logger.getLogger(AutoSaveService.class);

    private AutoSaveManager autoSaveManager;

    public boolean saveDetails(final String objectType, final Long objectID, final String data) {
        try{
            autoSaveManager.saveDetails(objectType, objectID, data);
            return true;
        } catch (AutoSavePersistException e) {
            e.printStackTrace();
            return false;
        } catch (AutoSaveDetailsNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getDetails(final String objectType, final Long objectID) {
        AutoSaveDetailsBean autoSaveDetailsBean = null;
        try{
            autoSaveDetailsBean = autoSaveManager.getDetails(objectType, objectID);
        } catch (AutoSaveDetailsNotFoundException e) {
            LOG.error("Auto save details not found...");
        }
        if(autoSaveDetailsBean != null) {
            return autoSaveDetailsBean.getDetails().toString();
        }
        return null;
    }

    public boolean deleteDetails(final String objectType, final Long objectID) {
         return autoSaveManager.deleteDetails(objectType, objectID);
    }



    public AutoSaveManager getAutoSaveManager() {
        return autoSaveManager;
    }

    public void setAutoSaveManager(AutoSaveManager autoSaveManager) {
        this.autoSaveManager = autoSaveManager;
    }
}
