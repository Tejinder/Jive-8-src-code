package com.grail.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.jivesoftware.base.aaa.JiveAuthentication;
import com.jivesoftware.community.JiveContext;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.palette.SkinThemeUtils;
import com.jivesoftware.community.theme.ThemeManager;

/**
 * Created with IntelliJ IDEA.
 * User: Bhaskar
 * Date: 5/9/14
 * Time: 3:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailFileDownloadServlet extends HttpServlet {

    private static Logger LOG = Logger.getLogger(GrailFileDownloadServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if(requestURI.contains("/file/download/")) {
//            if(getRequestingUser() == null) {
//                try {
//                    response.sendRedirect(request.getContextPath() + "/login.jspa?authzFailed=true");
//                }
//                catch (IOException e) {
//                    LOG.trace(e.getMessage());
//                }
//            }
            PrintWriter out = response.getWriter();
            String fileName = requestURI.substring(requestURI.lastIndexOf("/") + 1, requestURI.length());
            //String filePath = getSkinThemeUtils().getActiveThemePath(request, response)+"/downloads/"+fileName;
            File themeFile = getSkinThemeUtils().getDirectoryForTheme(getThemeManager().getTheme(getSkinThemeUtils().getActiveThemeName(request)));
            String filePath = themeFile.getPath() + "/uploads/" + fileName;
            try {
                FileInputStream inputStream = new FileInputStream(filePath);
                response.setHeader("Content-type","application/octet-stream");
                response.setHeader("Content-Disposition","attachment; filename="+fileName);
                int i;
                while ((i = inputStream.read()) != -1) {
                    out.write(i);
                }
                inputStream.close();
                out.close();
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("Requested file not available");
            }
        } else if(requestURI.contains("/oracledocuments/download/")) {

            PrintWriter out = response.getWriter();
            String rootPath =  getServletContext().getRealPath("/").concat("oracledocuments/");
            String filePath = null;
            String fileName = requestURI.substring(requestURI.lastIndexOf("/") + 1, requestURI.length());
            if(requestURI.contains("/download/oraclemanuals")) {
                rootPath += "oraclemanuals";
                filePath = rootPath + URLDecoder.decode(requestURI.replace("/oracledocuments/download/oraclemanuals",""), "UTF-8");
            } else if(requestURI.contains("/download/oraclegovernance")) {
                rootPath += "oraclegovernance";
                filePath = rootPath + URLDecoder.decode(requestURI.replace("/oracledocuments/download/oraclegovernance",""),"UTF-8");
            }
            if(filePath != "") {
                try {
                    FileInputStream inputStream = new FileInputStream(filePath);
                    response.setHeader("Content-type","application/octet-stream");
                    response.setHeader("Content-Disposition","attachment; filename="+fileName);
                    int i;
                    while ((i = inputStream.read()) != -1) {
                        out.write(i);
                    }
                    inputStream.close();
                    out.close();
                } catch (FileNotFoundException e) {
                    throw new FileNotFoundException("Requested file not available");
                }
            }
        }
    }

    private String getRequestingUser() {
        JiveContext jiveContext = JiveApplication.getEffectiveContext();
        JiveAuthentication auth = jiveContext.getAuthenticationProvider().getAuthentication();
        return auth == null  ? null : (auth.isAnonymous() ? null : auth.getUser().getUsername());
    }

    private static SkinThemeUtils skinThemeUtils;

    public static SkinThemeUtils getSkinThemeUtils() {
        if(skinThemeUtils == null) {
            return JiveApplication.getContext().getSpringBean("skinThemeUtils");
        }
        return skinThemeUtils;
    }
    private static ThemeManager themeManager;

    public static ThemeManager getThemeManager() {
        if(themeManager == null) {
            return JiveApplication.getContext().getSpringBean("themeManager");
        }
        return themeManager;
    }


}
