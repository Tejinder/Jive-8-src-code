package com.grail.custom.analytics.util;


import com.jivesoftware.util.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Leo
 * Date: May 26, 2010
 * Time: 1:28:50 AM
 * To change this template use File | Settings | File Templates.
 */
 public class ReportingMap extends HashMap implements Comparable {
        private boolean ascending = true;
        private String sortType;

        /*
        * Returns a negative integer, a positive integer, or zero as the builder has
        * judged the "left-hand" side as less than, greater than, or equal to the "right-hand" side.
        */

        public int compareTo(Object bar) {
            int result;
            if (bar == null || !(bar instanceof ReportingMap)) {
                result = -1;
            }
            ReportingMap _rhs = (ReportingMap) bar;
			result = new CompareToBuilder().append(get(sortType), _rhs.get(sortType)).toComparison();
/*
            String lhsStr = (String)get(sortType);
            String rhsStr = (String)_rhs.get(sortType);

            if(StringUtils.isNullOrEmpty(lhsStr)){
                lhsStr = "0";                
            }
            if(StringUtils.isNullOrEmpty(lhsStr)){
                rhsStr = "0";
            }


            if("RATING_COUNT".equals(sortType))
            {
                Float flhs = Float.valueOf(lhsStr);
                Float frhs = Float.valueOf(rhsStr);
                result = new CompareToBuilder().append(flhs,frhs ).toComparison();
                System.out.println("ITS Rating SORT " + flhs + " -- " + frhs + " -- " + result);
            }
            else {
                Integer lhs = Integer.valueOf(lhsStr);
                Integer rhs = Integer.valueOf(rhsStr);
                result = new CompareToBuilder().append(lhs,rhs ).toComparison();
                System.out.println("ITS " + sortType +" SORT " + lhs + " -- " + rhs + " -- " + result);
            }
*/
            return (ascending ? result : -result);
        }

        public void setAscending(boolean asc) {
            ascending = asc;
        }

        public void setSortType(String sortType) {
            this.sortType = sortType;
        }
    }