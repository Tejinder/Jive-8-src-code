package com.grail.synchro.util;

import com.grail.synchro.beans.Quarter;
import com.jivesoftware.community.JiveGlobals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 5/14/14
 * Time: 7:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class QuarterRangeUtil {
    public static String q1RangeStr = JiveGlobals.getJiveProperty("synchro.projectcosts.reports.Q1.range", "1-3");
    public static String q2RangeStr = JiveGlobals.getJiveProperty("synchro.projectcosts.reports.Q2.range","4-6");
    public static String q3RangeStr = JiveGlobals.getJiveProperty("synchro.projectcosts.reports.Q3.range","7-9");
    public static String q4RangeStr = JiveGlobals.getJiveProperty("synchro.projectcosts.reports.Q4.range","10-12");

    public static Quarter getQuarter(final Integer range) {
        Quarter quarter = new Quarter();
        String [] qRange = null;
        if(range == 1) {
           qRange =  q1RangeStr.split("-");
        } else if(range == 2) {
            qRange =  q2RangeStr.split("-");
        } else if(range == 3) {
            qRange =  q3RangeStr.split("-");
        } else if(range == 4) {
            qRange =  q4RangeStr.split("-");
        }

        if(qRange != null && qRange.length > 0) {
            quarter.setStartMonth(Integer.parseInt(qRange[0]));
            quarter.setEndMonth(Integer.parseInt(qRange[1]));
            Calendar dateTime = Calendar.getInstance();
            quarter.setStartYear(dateTime.get(Calendar.YEAR));
            quarter.setEndYear(dateTime.get(Calendar.YEAR));
        }
        return quarter;
    }

    public static Quarter getCurrentQuarter() {

        Quarter quarter = new Quarter();

        Calendar dateTime = Calendar.getInstance();

        String[] q1Range = q1RangeStr.split("-");
        String[] q2Range = q2RangeStr.split("-");
        String[] q3Range = q3RangeStr.split("-");
        String[] q4Range = q4RangeStr.split("-");

        int month = dateTime.get(Calendar.MONTH) + 1;
        if(month >= Integer.parseInt(q1Range[0])
                && month <= Integer.parseInt(q1Range[1])) {
            quarter.setStartMonth(Integer.parseInt(q1Range[0]));
            quarter.setEndMonth(Integer.parseInt(q1Range[1]));
        }

        if(month >= Integer.parseInt(q2Range[0])
                && month <= Integer.parseInt(q2Range[1])) {
            quarter.setStartMonth(Integer.parseInt(q2Range[0]));
            quarter.setEndMonth(Integer.parseInt(q2Range[1]));
        }

        if(month >= Integer.parseInt(q3Range[0])
                && month <= Integer.parseInt(q3Range[1])) {
            quarter.setStartMonth(Integer.parseInt(q3Range[0]));
            quarter.setEndMonth(Integer.parseInt(q3Range[1]));
        }

        if(month >= (Integer.parseInt(q4Range[0]))
                && month <= Integer.parseInt(q4Range[1])) {
            quarter.setStartMonth(Integer.parseInt(q4Range[0]));
            quarter.setEndMonth(Integer.parseInt(q4Range[1]));
        }

        quarter.setStartYear(dateTime.get(Calendar.YEAR));
        quarter.setEndYear(dateTime.get(Calendar.YEAR));

        return quarter;
    }
}
