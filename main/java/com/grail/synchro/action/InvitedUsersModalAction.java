package com.grail.synchro.action;

import com.grail.synchro.beans.InvitedUser;
import com.grail.synchro.beans.InvitedUserResultFilter;
import com.grail.synchro.manager.ProjectManager;
import com.grail.synchro.util.SynchroPermHelper;
import com.jivesoftware.community.action.JiveActionSupport;
import com.jivesoftware.community.action.util.Decorate;
import org.springframework.web.bind.ServletRequestDataBinder;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 2/10/15
 * Time: 11:26 AM
 * To change this template use File | Settings | File Templates.
 */
@Decorate(false)
public class InvitedUsersModalAction extends JiveActionSupport {

    private Integer page = 1;
    private Integer pages;
    private Integer start = 0;
    private Integer end;
    private Integer limit = 10;
    private String keyword;

    private ProjectManager synchroProjectManager;
    private List<InvitedUser> invitedUsers;

    @Override
    public String input() {
        setPagination(synchroProjectManager.getInvitedUsersTotalCount(getFilter()).intValue());
        updatePage();
        return INPUT;
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
        invitedUsers = synchroProjectManager.getInvitedUsers(getFilter());
    }

    private InvitedUserResultFilter getFilter() {
        InvitedUserResultFilter filter = new InvitedUserResultFilter();

        ServletRequestDataBinder binder = new ServletRequestDataBinder(filter);
        binder.bind(getRequest());

        filter.setStart(start);
        filter.setLimit(limit);

        if(filter.getKeyword() == null || filter.getKeyword().equals("")) {
            filter.setKeyword(keyword);
        }
        if(filter.getStart() == null) {
            filter.setStart(start);
        }
        if(filter.getLimit() == null) {
            filter.setLimit(limit);
        }

//        if(!(SynchroPermHelper.isSynchroAdmin(getUser()) || SynchroPermHelper.isSynchroMiniAdmin(getUser())
//                || SynchroPermHelper.isSynchroSuperUser(getUser()))) {
//            filter.setInvitedBy(getUser().getID());
//        }

        return filter;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
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

    public ProjectManager getSynchroProjectManager() {
        return synchroProjectManager;
    }

    public void setSynchroProjectManager(ProjectManager synchroProjectManager) {
        this.synchroProjectManager = synchroProjectManager;
    }

    public List<InvitedUser> getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(List<InvitedUser> invitedUsers) {
        this.invitedUsers = invitedUsers;
    }
}
