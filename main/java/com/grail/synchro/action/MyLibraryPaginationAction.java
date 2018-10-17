package com.grail.synchro.action;

import com.grail.synchro.SynchroConstants;
import com.grail.synchro.object.MyLibraryDocument;
import com.grail.synchro.beans.MyLibraryDocumentBean;
import com.grail.synchro.manager.MyLibraryManager;
import com.grail.synchro.search.filter.MyLibrarySearchFilter;
import com.grail.synchro.util.SynchroUtils;
import com.jivesoftware.base.User;
import com.jivesoftware.community.*;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Decorate(false)
public class MyLibraryPaginationAction extends JiveActionSupport {
    private static Logger LOG = Logger.getLogger(MyLibraryPaginationAction.class);

    private List<MyLibraryDocumentBean> myLibraryList = new ArrayList<MyLibraryDocumentBean>();
    private Integer page = 1;
    private Integer results;
    private Integer pages;
    private Integer start = 0;
    private Integer end;
    private Integer limit = JiveGlobals.getJiveIntProperty(SynchroConstants.MY_LIBRARY_PAGE_LIMIT, 10);
    private String keyword;
    private SynchroUtils synchroUtils;
    private MyLibraryManager myLibraryManager;
    private String sortField;
    private Integer ascendingOrder;
    private AttachmentManager attachmentManager;
    private MyLibrarySearchFilter searchFilter;

    public SynchroUtils getSynchroUtils() {
        if(synchroUtils == null){
            synchroUtils = JiveApplication.getContext().getSpringBean("synchroUtils");
        }
        return synchroUtils;
    }

    @Override
    public String execute() {
        setPagination(myLibraryManager.getTotalCount(keyword).intValue());
        updatePage();
        return SUCCESS;
    }

    public void setPagination(final Integer count) {
        if(count > limit) {
            double temp = count / (limit * 1.0);
            if(count%limit == 0) {
                  pages = (int) temp;
            } else {
                pages = (int) temp + 1;
            }
        } else {
            pages = 1;
        }

    }

    public void updatePage() {
        start = (page-1) * limit;
        end = start + limit;
        myLibraryList = this.toDocumentBeans(myLibraryManager.getDocuments(getSearchFilter()));
    }

    private MyLibrarySearchFilter getSearchFilter() {
        searchFilter = new MyLibrarySearchFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(searchFilter);
        binder.bind(getRequest());

        if(searchFilter.getKeyword() == null || searchFilter.getKeyword().equals("")) {
            searchFilter.setKeyword(keyword);
        }
        if(searchFilter.getStart() == null) {
            searchFilter.setStart(start);
        }
        if(searchFilter.getLimit() == null) {
            searchFilter.setLimit(limit);
        }
        if(searchFilter.getUserId() == null) {
            searchFilter.setUserId(getUser().getID());
        }
        return searchFilter;
    }

    private List<MyLibraryDocumentBean> toDocumentBeans(final List<MyLibraryDocument> myLibraryDocuments) {
        List<MyLibraryDocumentBean> beans = new ArrayList<MyLibraryDocumentBean>();
        for(MyLibraryDocument document: myLibraryDocuments) {
            MyLibraryDocumentBean bean = document.getBean();
            StringBuilder link = new StringBuilder();
            link.append("/servlet/JiveServlet/download/");
            link.append(bean.getId()).append("-").append(bean.getAttachmentId()).append("/").append(bean.getFileName());;
            bean.setFileDownloadLink(link.toString());
            beans.add(bean);
        }
        return beans;
    }



    private void setPaginationFilter(int page,int results) {
        start = (this.page-1)*limit;
        end = start + limit;
        end = end>=this.results?this.results:end;
    }

    public List<MyLibraryDocumentBean> getMyLibraryList() {
        return myLibraryList;
    }

    public void setMyLibraryList(List<MyLibraryDocumentBean> myLibraryList) {
        this.myLibraryList = myLibraryList;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getResults() {
        return results;
    }

    public void setResults(Integer results) {
        this.results = results;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public MyLibraryManager getMyLibraryManager() {
        return myLibraryManager;
    }

    public void setMyLibraryManager(MyLibraryManager myLibraryManager) {
        this.myLibraryManager = myLibraryManager;
    }


    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public Integer getAscendingOrder() {
        return ascendingOrder;
    }

    public void setAscendingOrder(Integer ascendingOrder) {
        this.ascendingOrder = ascendingOrder;
    }

    public AttachmentManager getAttachmentManager() {
        return attachmentManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

}
