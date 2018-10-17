package com.grail.util;

//import com.opensymphony.xwork2.util.TypeConversionException;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.util.StrutsTypeConverter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/31/14
 * Time: 5:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class SynchroLongListConverter extends StrutsTypeConverter {

    @Override
    public Object convertFromString(Map context, String[] values, Class toClass) {

        if (values == null || values.length == 0 || values[0].trim().length() == 0) {
            return null;
        }

        try {
            String [] strItems = values[0].split(",");
            List<Long> list = new ArrayList<Long>();
            for(String item: strItems) {
                if(item != null) {
                    list.add(Long.parseLong(item));
                }
            }
            return list;
        } catch (Exception e) {
            //throw new TypeConversionException("Unable to convert given items to list: " + values[0]);
        	e.printStackTrace();
        }
        return null;
    }

    @Override
    public String convertToString(Map context, Object o) {
        if (o != null && o instanceof List) {
            return StringUtils.join((List) o, ",");
        } else {
            return null;
        }
    }
}
