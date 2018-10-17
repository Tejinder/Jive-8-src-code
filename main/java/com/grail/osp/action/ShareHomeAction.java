package com.grail.osp.action;


import java.util.ArrayList;
import java.util.List;

import com.grail.osp.beans.OSPTile;
import com.grail.osp.manager.OSPManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;

/**
 * User: Tejinder
 * Date: 12/08/17
 */
public class ShareHomeAction extends JiveActionSupport {

	private List<OSPTile> shareTileList;
	private OSPManager ospManager;
	
    @Override
    public String execute() {
        if(!SynchroPermHelper.canAccessOSPSharePortal(getUser())) {
            return UNAUTHORIZED;
        }
        shareTileList = ospManager.getShareTiles();
        return SUCCESS;
    }
	
	public OSPManager getOspManager() {
		return ospManager;
	}
	public void setOspManager(OSPManager ospManager) {
		this.ospManager = ospManager;
	}

	public List<OSPTile> getShareTileList() {
		return shareTileList;
	}

	public void setShareTileList(List<OSPTile> shareTileList) {
		this.shareTileList = shareTileList;
	}

}
