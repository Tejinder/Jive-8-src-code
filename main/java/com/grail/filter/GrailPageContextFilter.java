package com.grail.filter;

import com.grail.synchro.SynchroGlobal;
import com.grail.util.BATConstants;
import com.grail.util.URLUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 7/10/14
 * Time: 4:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailPageContextFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if(SynchroGlobal.getAppProperties().get(BATConstants.BAT_BASE_URL) == null || SynchroGlobal.getAppProperties().get(BATConstants.BAT_BASE_URL).equals("")) {
            SynchroGlobal.getAppProperties().put(BATConstants.BAT_BASE_URL, URLUtils.getBaseURL(((HttpServletRequest) request)));
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
