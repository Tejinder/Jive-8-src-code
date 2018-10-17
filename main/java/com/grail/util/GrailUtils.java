package com.grail.util;

import com.grail.synchro.beans.MetaField;
import com.grail.synchro.manager.SynchroMetaFieldManager;
import com.jivesoftware.community.lifecycle.JiveApplication;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 1/19/15
 * Time: 11:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class GrailUtils {

    private static SynchroMetaFieldManager synchroMetaFieldManager;

    private static Map<Long, String> methodologyTypeMap = new LinkedHashMap<Long, java.lang.String>();
    private static Map<Long, String> allMethodologyTypeMap = new LinkedHashMap<Long, java.lang.String>();

    public static Map<Long, String> getGrailButtomMethodologyTypes() {
        if(methodologyTypeMap == null || methodologyTypeMap.isEmpty()) {
            methodologyTypeMap = new LinkedHashMap<Long, String>();
            List<MetaField> methodologyTypes = getSynchroMetaFieldManager().getGrailButtonMethodologyTypes();
            for(MetaField methodologyType: methodologyTypes) {
                methodologyTypeMap.put(methodologyType.getId(), methodologyType.getName());
            }
        }
        return methodologyTypeMap;
    }

    public static Map<Long, String> getAllGrailButtomMethodologyTypes() {
        if(allMethodologyTypeMap == null || allMethodologyTypeMap.isEmpty()) {
            allMethodologyTypeMap = new LinkedHashMap<Long, String>();
            List<MetaField> methodologyTypes = getSynchroMetaFieldManager().getAllGrailButtonMethodologyTypes();
            for(MetaField methodologyType: methodologyTypes) {
                allMethodologyTypeMap.put(methodologyType.getId(), methodologyType.getName());
            }
        }
        return allMethodologyTypeMap;
    }

    public static MetaField setGrailButtonMethodologyType(final String name) {
        MetaField methodologyType = getGrailButtonMethodologyType(name);
        if(methodologyType == null) {
            Long id = getSynchroMetaFieldManager().setKantarButtonMethodologyType(name);
            if(id != null) {
                methodologyType = getGrailButtonMethodologyType(id);
            }
        }
        triggerGrailButtonMethodologyTypes();
        return methodologyType;
    }

    public static MetaField getGrailButtonMethodologyType(final String name) {
        return getSynchroMetaFieldManager().getGrailButtonMethodologyType(name);
    }

    public static MetaField getGrailButtonMethodologyType(final Long id) {
        return getSynchroMetaFieldManager().getGrailButtonMethodologyType(id);
    }

    public static SynchroMetaFieldManager getSynchroMetaFieldManager() {
        if(synchroMetaFieldManager==null)
        {
            synchroMetaFieldManager = JiveApplication.getContext().getSpringBean("synchroMetaFieldManager");
        }
        return synchroMetaFieldManager;

    }

    public static void triggerGrailButtonMethodologyTypes() {
        GrailUtils.methodologyTypeMap.clear();
        GrailUtils.allMethodologyTypeMap.clear();
    }
}
