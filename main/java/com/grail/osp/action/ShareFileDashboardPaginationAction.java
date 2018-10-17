package com.grail.osp.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.RegionUtil;
import org.springframework.web.bind.ServletRequestDataBinder;

import com.google.common.base.Joiner;
import com.grail.osp.beans.OSPFile;
import com.grail.osp.beans.OSPFolder;
import com.grail.osp.beans.OSPTile;
import com.grail.osp.manager.OSPManager;
import com.grail.synchro.SynchroConstants;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.PIBMethodologyWaiver;
import com.grail.synchro.beans.Project;
import com.grail.synchro.beans.ProjectStage;
import com.grail.synchro.dao.util.SynchroDAOUtil;
import com.grail.synchro.manager.PIBManagerNew;
import com.grail.synchro.manager.ProjectManagerNew;
import com.grail.synchro.search.filter.ProjectResultFilter;
import com.grail.synchro.util.SynchroLogUtils;
import com.grail.synchro.util.SynchroPermHelper;
import com.grail.synchro.util.SynchroRawExtractUtil;
import com.grail.synchro.util.SynchroUtils;
import com.grail.util.DateUtils;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.util.StringUtils;

@Decorate(false)
public class ShareFileDashboardPaginationAction extends JiveActionSupport {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ShareFileDashboardPaginationAction.class);
    private Integer page = 1;
    private static Integer LIMIT = 10;
    private Integer results;
    private Integer pages = 0;
    private Integer start;
    private Integer end;
  
    ProjectResultFilter projectResultFilter = new ProjectResultFilter();
    private Integer  pageLimit = JiveGlobals.getJiveIntProperty(SynchroConstants.PROJECT_DASHBOARD_PAGE_LIMIT, LIMIT);
    private String keyword;
    
   
    private List<OSPFile> shareFileList;
	private OSPManager ospManager;
	private Long tileID;
	private Long folderID;
	private OSPTile ospTile;
    
    
    private String sortField;
    private Integer ascendingOrder;

    private Integer plimit;
    
    
   
    private List<String> selectedFilters = new ArrayList<String>(); 
    
    public String getSortField() {
        return sortField;
    }

    public Integer getAscendingOrder() {
        return ascendingOrder;
    }

   
    public Integer getPages() {
        return pages;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public void setResults(Integer results) {
        this.results = results;
    }

    public Integer getResults() {
        return results;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String execute()
    {
//        if(!(getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)!=null && ((String)getSession().get(SynchroConstants.SYNCHRO_DISCLAIMER_KEY)).equals("yes")))
//        {
//            return "disclaimer-error";
//        }
        ProjectResultFilter filter = getSearchFilter();
        setPagination(ospManager.getShareFilesTotalCount(tileID, folderID).intValue());
        //setPagination(10);
        updatePage();
        ospTile = ospManager.getShareTile(tileID);
      //  synchroProjectManagerNew.updatePendingActivityViews(SynchroReminderUtils.getPendingActivitySearchFilter(), getUser().getID());
        return SUCCESS;

    }

  
    
    public void setPagination(final Integer count) {
    	if(plimit!=null && plimit > 0)
    	{
    		LIMIT=plimit;
    	}
    	
    	if(count > LIMIT) {
            double temp = count / (LIMIT * 1.0);
            if(count%LIMIT == 0) {
                pages = (int) temp;
            } else {
                pages = (int) temp + 1;
            }
        } else {
            pages = 1;
        }
    }

    public void updatePage() {
        start = (page-1) * LIMIT;
        end = start + LIMIT;
        //oracleFolderList = ospManager.getOracleFolders(new Long("2"));
        shareFileList = ospManager.getShareFiles(tileID,folderID, getSearchFilter());
        //pibMethodologyWaiverList = pibManagerNew.getPIBMethodologyWaivers(getSearchFilter());
        //pibMethodologyWaiverList = toMethodologyWaiverBeans(pibMethodologyWaiverList);
    }

    
    
    

    private ProjectResultFilter getSearchFilter() {
        projectResultFilter = new ProjectResultFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(projectResultFilter);
        binder.bind(getRequest());

        if(projectResultFilter.getProjectStatusFields() == null
                || projectResultFilter.getProjectStatusFields().size() == 0) {
            
        }

        if(projectResultFilter.getKeyword() == null || projectResultFilter.getKeyword().equals("")) {
            projectResultFilter.setKeyword(keyword);
        }
        if(projectResultFilter.getStart() == null) {
            projectResultFilter.setStart(start);
        }
      /* if(projectResultFilter.getLimit() == null) {
            projectResultFilter.setLimit(LIMIT);
        }
        */
        if(plimit!=null && plimit > 0)
        {
        	projectResultFilter.setLimit(plimit);
        }
        else
        {
        	projectResultFilter.setLimit(LIMIT);
        }
        
      
        return projectResultFilter;
    }

	
	
	public Integer getPlimit() {
		return plimit;
	}

	public void setPlimit(Integer plimit) {
		this.plimit = plimit;
	}

	
	public OSPManager getOspManager() {
		return ospManager;
	}

	public void setOspManager(OSPManager ospManager) {
		this.ospManager = ospManager;
	}

	public Long getTileID() {
		return tileID;
	}

	public void setTileID(Long tileID) {
		this.tileID = tileID;
	}

	public OSPTile getOspTile() {
		return ospTile;
	}

	public void setOspTile(OSPTile ospTile) {
		this.ospTile = ospTile;
	}

	public List<OSPFile> getShareFileList() {
		return shareFileList;
	}

	public void setOracleFileList(List<OSPFile> shareFileList) {
		this.shareFileList = shareFileList;
	}

	public Long getFolderID() {
		return folderID;
	}

	public void setFolderID(Long folderID) {
		this.folderID = folderID;
	}

	
   

}
