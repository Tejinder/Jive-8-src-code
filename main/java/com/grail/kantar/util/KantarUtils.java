package com.grail.kantar.util;

import com.grail.kantar.beans.KantarReportTypeBean;
import com.grail.synchro.SynchroGlobal;
import com.grail.synchro.beans.MetaField;
import com.grail.synchro.manager.SynchroMetaFieldManager;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.apache.commons.collections.map.HashedMap;
import org.openrdf.sail.rdbms.schema.LongIdSequence;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 12/11/14
 * Time: 3:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarUtils {

    private static SynchroMetaFieldManager synchroMetaFieldManager;
    private static Map<Integer, String> reportTypesMap = new LinkedHashMap<Integer, String>();

    private static Map<Long, String> methodologyTypeMap = new LinkedHashMap<Long, java.lang.String>();
    private static Map<Long, String> allMethodologyTypeMap = new LinkedHashMap<Long, java.lang.String>();



    public static String generateKantarCode(final Long id) {
        final int MAXDIGITS = 5;
        String prepend = "K";

        if(id != null && id > 0) {
            int length = id.toString().length();
            int prependLength = MAXDIGITS - length;

            if(length < MAXDIGITS) {
                for(int i=0; i<prependLength; i++) {
                    prepend = prepend + "0";
                }
            }
            return prepend + id.intValue();
        } else {
            return id == null?"":(prepend + id.toString());
        }

    }

    public static Map<Integer, String> getKantarReportTypes() {
        if(reportTypesMap == null || reportTypesMap.isEmpty()) {
            reportTypesMap = new LinkedHashMap<Integer, String>();
            List<MetaField> reportTypes = getSynchroMetaFieldManager().getKantarReportTypes();
            for(MetaField reportType: reportTypes) {
                reportTypesMap.put(reportType.getId().intValue(), reportType.getName());
            }
        }
        return reportTypesMap;
    }

    public static List<KantarReportTypeBean> getKantarReportTypeBeans() {
        List<KantarReportTypeBean> reportTypes = getSynchroMetaFieldManager().getKantarReportTypeBeans();
        return reportTypes;
    }

//    public static MetaField setKantarReportType(final String name) {
//        MetaField reportType = getKantarReportType(name);
//        if(reportType == null) {
//            Long id = getSynchroMetaFieldManager().setKantarReportType(name);
//            if(id != null) {
//                reportType = getKantarReportType(id);
//                reportTypesMap.clear();
//            }
//        }
//        return reportType;
//    }

    public static MetaField setKantarReportType(final String name, final boolean otherType) {
        MetaField reportType = getKantarReportType(name);
        if(reportType == null) {
            Long id = getSynchroMetaFieldManager().setKantarReportType(name, otherType);
            if(id != null) {
                reportType = getKantarReportType(id);
                reportTypesMap.clear();
            }
        }
        return reportType;
    }

    public static void updateKantarReportType(final Long id, final String name) {
        getSynchroMetaFieldManager().updateKantarReportType(id, name);
    }

    public static void deleteKantarReportType(final Long id) {
        getSynchroMetaFieldManager().deleteKantarReportType(id);
    }


    public static MetaField getKantarReportType(final String name) {
        return getSynchroMetaFieldManager().getKantarReportType(name);
    }

    public static MetaField getKantarReportType(final Long id) {
        return getSynchroMetaFieldManager().getKantarReportType(id);
    }

    public static Map<Long, String> getKantarButtomMethodologyTypes() {
        if(methodologyTypeMap == null || methodologyTypeMap.isEmpty()) {
            methodologyTypeMap = new LinkedHashMap<Long, String>();
            List<MetaField> methodologyTypes = getSynchroMetaFieldManager().getKantarButtonMethodologyTypes();
            for(MetaField methodologyType: methodologyTypes) {
                methodologyTypeMap.put(methodologyType.getId(), methodologyType.getName());
            }
        }
        return methodologyTypeMap;
    }

    public static Map<Long, String> getAllKantarButtomMethodologyTypes() {
        if(allMethodologyTypeMap == null || allMethodologyTypeMap.isEmpty()) {
            allMethodologyTypeMap = new LinkedHashMap<Long, String>();
            List<MetaField> methodologyTypes = getSynchroMetaFieldManager().getAllKantarButtonMethodologyTypes();
            for(MetaField methodologyType: methodologyTypes) {
                allMethodologyTypeMap.put(methodologyType.getId(), methodologyType.getName());
            }
        }
        return allMethodologyTypeMap;
    }

    public static MetaField setKantarButtonMethodologyType(final String name) {
        MetaField methodologyType = getKantarButtonMethodologyType(name);
        if(methodologyType == null) {
            Long id = getSynchroMetaFieldManager().setKantarButtonMethodologyType(name);
            if(id != null) {
                methodologyType = getKantarButtonMethodologyType(id);
            }
        }
        triggerKantarButtonMethodologyTypes();
        return methodologyType;
    }

    public static MetaField getKantarButtonMethodologyType(final String name) {
        return getSynchroMetaFieldManager().getKantarButtonMethodologyType(name);
    }

    public static MetaField getKantarButtonMethodologyType(final Long id) {
        return getSynchroMetaFieldManager().getKantarButtonMethodologyType(id);
    }




    public static SynchroMetaFieldManager getSynchroMetaFieldManager() {
        if(synchroMetaFieldManager==null)
        {
            synchroMetaFieldManager = JiveApplication.getContext().getSpringBean("synchroMetaFieldManager");
        }
        return synchroMetaFieldManager;

    }

    public static void triggerKantarReportTypes() {
        KantarUtils.reportTypesMap.clear();
    }

    public static void triggerKantarButtonMethodologyTypes() {
        KantarUtils.methodologyTypeMap.clear();
        KantarUtils.allMethodologyTypeMap.clear();
    }
}
