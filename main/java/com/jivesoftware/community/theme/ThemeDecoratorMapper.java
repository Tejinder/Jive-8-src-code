/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.theme;

import com.grail.util.BATConstants;
import com.grail.util.BATGlobal;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.DecoratorMapper;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.mapper.AbstractDecoratorMapper;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ognl.OgnlValueStack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Properties;

/**
 * A sitemesh decorator mapper that loads the correct theme for each request and creates
 * a <tt>ThemeDecorator</tt>. If a theme is not found, the default sitemesh decorator
 * mapper will be used.
 */
public class ThemeDecoratorMapper extends AbstractDecoratorMapper {
    private static final String IGNORE_PROPERTY_KEY = "decorators-ignore";
    private static String[] ignore = new String[0];

    public void init(Config config, Properties properties, DecoratorMapper parent)
            throws InstantiationException {

        String ignoreList = (String) properties.get(IGNORE_PROPERTY_KEY);
        if (ignoreList != null) {
            ignore = ignoreList.split(",");
        }

        super.init(config, properties, parent);
    }

    public Decorator getDecorator(HttpServletRequest request, Page page) {
		HttpSession session = request.getSession();       
		Decorator decorator = super.getDecorator(request, page);
        if (decorator == null || Boolean.FALSE.toString().equals(JiveGlobals.getLocalProperty("setup"))
                || request.getRequestURI()
                .contains("/admin/setup/"))
        {
            // no decorator found
            return decorator;
        }
        String name = decorator.getName().trim();
        if (!shouldLoad(name)) {
            return super.getDecorator(request, page);
        }

        List<Theme> themes = loadTheme(request);
        if (themes != null && themes.size() > 0) {
            //return createDecorator(themes, decorator);
			return createDecorator(themes, decorator, session, request.getRequestURI());
        }
        else {
            return decorator;
        }
    }

    private boolean shouldLoad(String name) {
        // do not attempt to load a theme for ignored decorators
        boolean loadTheme = true;
        for (String anIgnore : ignore) {
            if (anIgnore.trim().equals(name)) {
                loadTheme = false;
                break;
            }
        }
        return loadTheme;
    }

    private List<Theme> loadTheme(HttpServletRequest request) {
        // the action context is used to get the current locale and action for this request
        OgnlValueStack stack = (OgnlValueStack) request.getAttribute("struts.valueStack");
        if (stack == null) {
            return null;
        }
        return JiveApplication.getContext().getThemeManager().determineThemes(new ActionContext(stack.getContext()), request);
    }

    public Decorator getNamedDecorator(HttpServletRequest request, String name) {
         HttpSession session = request.getSession();
		Decorator decorator = super.getNamedDecorator(request, name);
        if (decorator == null || !shouldLoad(decorator.getName().trim())) {
            return decorator;
        }
        List<Theme> themes = loadTheme(request);
        if (themes != null && themes.size() > 0) {
           // return createDecorator(themes, decorator);
		    return createDecorator(themes, decorator, session, request.getRequestURI());
        }
        else {
            return decorator;
        }
    }

    private Decorator createDecorator(List<Theme> themes, Decorator decorator) {
        return new ThemeDecorator(decorator, decorator.getPage(), themes);
    }
	
	private Decorator createDecorator(List<Theme> themes, Decorator decorator, HttpSession session) {
        String page = decorator.getPage();
        if(session.getAttribute(BATConstants.GRAIL_PORTAL_TYPE) != null) {
            String portalType = session.getAttribute(BATConstants.GRAIL_PORTAL_TYPE).toString();
            if(portalType.equals(BATGlobal.PortalType.SYNCHRO.toString())) {
                if(page.contains("/default/template.ftl")) {
                    page = page.replace("default","synchro");
                }
            } else if(portalType.equals(BATGlobal.PortalType.GRAIL.toString())) {
                if(page.contains("/default/template.ftl")) {
                    page = page.replace("default","grail");
                }
            } else if(portalType.equals(BATGlobal.PortalType.KANTAR.toString())) {
                if(page.contains("/default/template.ftl")) {
                    page = page.replace("default","kantar");
                }
            } else if(portalType.equals(BATGlobal.PortalType.KANTAR_REPORT.toString())) {
                if(page.contains("/default/template.ftl")) {
                    page = page.replace("default","kantar");
                }
            }

        }
        return new ThemeDecorator(decorator, page, themes);
    }

    private Decorator createDecorator(List<Theme> themes, Decorator decorator, HttpSession session, String uri) {
        String page = decorator.getPage();
        if(!(uri.contains("/login.jspa") || uri.contains("/disclaimer.jspa") || uri.contains("/portal-options.jspa"))) {
            if(session.getAttribute(BATConstants.GRAIL_PORTAL_TYPE) != null) {
                String portalType = session.getAttribute(BATConstants.GRAIL_PORTAL_TYPE).toString();
                if(portalType.equals(BATGlobal.PortalType.SYNCHRO.toString())) {
                    if(page.contains("/default/template.ftl")) {
                        page = page.replace("default","synchro");
                    }
                } else if(portalType.equals(BATGlobal.PortalType.GRAIL.toString())) {
                    if(page.contains("/default/template.ftl")) {
                        page = page.replace("default","grail");
                    }
                } else if(portalType.equals(BATGlobal.PortalType.KANTAR.toString())) {
                    if(page.contains("/default/template.ftl")) {
                        page = page.replace("default","kantar");
                    }
                } else if(portalType.equals(BATGlobal.PortalType.KANTAR_REPORT.toString())) {
                    if(page.contains("/default/template.ftl")) {
                        page = page.replace("default","kantar");
                    }
                } else if(portalType.equals(BATGlobal.PortalType.ORACLE_DOCUMENTS.toString())) {
                    if(page.contains("/default/template.ftl")) {
                        page = page.replace("default","synchro");
                    }
                }
            }
        }
        return new ThemeDecorator(decorator, page, themes);
    }
}
