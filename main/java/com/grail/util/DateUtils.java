package com.grail.util;

import com.jivesoftware.community.action.util.JiveTextProvider;
import com.jivesoftware.community.action.util.JiveTextProviderFactory;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.TextProvider;
import org.apache.commons.lang.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Bhaskar
 * Date: 1/23/14
 * Time: 4:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class DateUtils {
    public DateUtils() {
    }


    public static Date parse(final String str) {
        if(StringUtils.isNotBlank(str)) {
            try {
                Date dt = getDateFormat().parse(str);
                dt.setHours(0);
                dt.setMinutes(0);
                dt.setSeconds(0);
                return dt;
            } catch (ParseException ex) {
                throw new IllegalArgumentException("Unable to parse ::" + ex.getMessage(), ex);
            }
        } else {
            return null;
        }
    }

    private static DateFormat getDateFormat() {
        DateFormat dateFormat = null;
        String format = getTextProvider().getText("synchro.date.format");
        if(StringUtils.isNotBlank(format)) {
            dateFormat = new SimpleDateFormat(format);
        } else {
            dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        }
        return dateFormat;
    }

    private static JiveTextProvider getTextProvider() {
        JiveTextProviderFactory jiveTextProviderFactory = JiveApplication.getContext().getSpringBean("jiveTextProviderFactory");
        return jiveTextProviderFactory.get();
    }

}
