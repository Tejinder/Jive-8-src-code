package com.grail.osp.action;


import java.util.ArrayList;
import java.util.List;

import com.grail.osp.beans.OSPTile;
import com.grail.osp.manager.OSPManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * User: Tejinder
 * Date: 11/20/17
 */
public class OracleHomeAction extends JiveActionSupport {

	private List<OSPTile> oracleTileList;
	private OSPManager ospManager;
	
    @Override
    public String execute() {
        if(!SynchroPermHelper.canAccessOSPOraclePortal(getUser())) {
            return UNAUTHORIZED;
        }
        oracleTileList = ospManager.getOracleTiles();
        return SUCCESS;
    }
	public List<OSPTile> getOracleTileList() {
		return oracleTileList;
	}
	public void setOracleTileList(List<OSPTile> oracleTileList) {
		this.oracleTileList = oracleTileList;
	}
	public OSPManager getOspManager() {
		return ospManager;
	}
	public void setOspManager(OSPManager ospManager) {
		this.ospManager = ospManager;
	}

}
