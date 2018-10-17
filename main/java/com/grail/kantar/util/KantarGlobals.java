package com.grail.kantar.util;

import com.grail.GrailGlobals;
import com.grail.kantar.beans.KantarBriefTemplate;
import com.grail.kantar.objecttype.KantarAttachmentObjectType;
import com.grail.synchro.SynchroGlobal;
import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.user.profile.ProfileFieldValue;
import com.jivesoftware.community.user.profile.ProfileManager;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 10/29/14
 * Time: 12:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class KantarGlobals {

    private static ProfileManager profileManager;
    private static UserManager userManager;

    public static boolean isKantarAttachmentType(final Integer objectType) {
        Integer oType = KantarGlobals.buildKantarAttachmentObjectID("KantarDocuments", KantarAttachmentObjectType.KANTAR_ATTACHMENT_OBJECT_CODE.toString());
        if(oType.intValue() == objectType.intValue()) {
            return true;
        }
        return false;
    }

    public static boolean isKantarReportAttachmentType(final Integer objectType) {
        Integer oType = KantarGlobals.buildKantarAttachmentObjectID("KantarReportDocuments", KantarAttachmentObjectType.KANTAR_ATTACHMENT_OBJECT_CODE.toString());
        if(oType.intValue() == objectType.intValue()) {
            return true;
        }
        return false;
    }

    public static Integer buildKantarAttachmentObjectID(final String prefix, final String objectType) {
        StringBuilder idBuilder = new StringBuilder();
        if(StringUtils.isNotBlank(prefix)) {
            idBuilder.append(prefix).append("-");
        }
        idBuilder.append(objectType);
        return idBuilder.toString().hashCode();
    }

    public static enum KantarBriefTemplateStatusType {
        IN_PROGRESS(1, "In-Progress"),
        COMPLETED(2, "Completed"),
        CANCELLED(3, "Cancelled");

        private int id;
        private String name;

        private KantarBriefTemplateStatusType(final int id, final String name) {
            this.id = id;
            this.name = name;
        }

        public static KantarBriefTemplateStatusType getById(int id) {
            for (KantarBriefTemplateStatusType status : values()) {
                if (status.getId() == id) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Project Status");
        }



        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static enum KantarMethodologyType {
        QUALITATIVE(1, "Qualitative"),
        DESK_RESEARCH(2, "Desk Research"),
        OTHERS(-100, "Others");

        private static Map<Integer, String> methodologyTypes = new HashMap<Integer, String>();
        static {
            for(KantarMethodologyType kantarMethodologyType: values()) {
                methodologyTypes.put(kantarMethodologyType.getId(), kantarMethodologyType.getName());
            }
        }

        private int id;
        private String name;

        private KantarMethodologyType(final int id, final String name) {
            this.id = id;
            this.name = name;
        }

        public static String getNameById(int id) {
            if(methodologyTypes.containsKey(id)) {
                return methodologyTypes.get(id);
            } else {
                return "";
            }
        }

        public static Map<Integer, String> getMethodologyTypes() {
            return methodologyTypes;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static enum KantarReportType {
        TYPE_1(1, "Type 1"),
        TYPE_2(2, "Type 2"),
        TYPE_3(3, "Type 3"),
        OTHER(-100, "Other");

        private Integer id;
        private String name;

        private static Map<Integer, String> kantarReportTypes = new HashMap<Integer, String>();

        static {
            for(KantarReportType kantarReportType: values()) {
                kantarReportTypes.put(kantarReportType.getId(), kantarReportType.getName());
            }
        }

        private KantarReportType(final Integer id, final String name) {
            this.id = id;
            this.name = name;
        }

        public static String getNameById(int id) {
            if(kantarReportTypes.containsKey(id)) {
                return kantarReportTypes.get(id);
            } else {
                return "";
            }
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static enum BriefTemplateOutputType {

        PPT(1, "PPT"),
        EXCEL(2, "Excel"),
        WORD(3,"Word"),
        EMAIL(4,"Email");

        private Integer id;
        private String name;

        private BriefTemplateOutputType(final Integer id, final String name) {
            this.id = id;
            this.name = name;
        }
        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static String getById(final Integer id) {
            for(BriefTemplateOutputType sr: BriefTemplateOutputType.values()) {
                if(sr.getId().intValue() == id.intValue()) {
                    return sr.toString();
                }
            }
            return "";
        }

        @Override
        public String toString() {
            return this.name;
        }
    }


    public static List<Map<String, Object>> toKantarTemplatesMapList(final List<KantarBriefTemplate> templates) {
        List<Map<String, Object>> briefTemplatesMapList = new ArrayList<Map<String, Object>>();
        if(templates != null && templates.size() > 0) {
            for(KantarBriefTemplate template: templates) {
                briefTemplatesMapList.add(toKantarBriefTemplateMap(template, template.getSender()));
            }
        }
        return briefTemplatesMapList;
    }

    public static Map<String, Object> toKantarBriefTemplateMap(final KantarBriefTemplate template, final User user) {

        Map<String, Object> kantarBriefTemplateMap = new LinkedHashMap<String, Object>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        String researchNeedsPriorities = "", hypothesisBusinessNeed = "", markets = "", products = "",
                brands = "", categories = "", deliveryDate = "", outputFormat = "",
                userName = "", userEmail = "", userLocation = "", userRole = "", recipientEmail = "", batContact = "",
                dataSource = "", methodologyType = "", code = "";

        if(user != null) {
            userName = user.getFirstName() + " " + user.getLastName();
            userEmail = user.getEmail();
            Map<Long, ProfileFieldValue> profileFieldMap = getProfileManager().getProfile(user);
            if(profileFieldMap != null) {
                if(profileFieldMap.get(3L) != null) {
                    String addresses = profileFieldMap.get(3L).getValue();
                    if(addresses.matches("^.*,?country:.*$")) {
                        userLocation = addresses.replaceAll("^.*\\,?country:([^\\,]+).*$", "$1");
                    }
                }

                if(profileFieldMap.get(1L) != null) {
                    userRole = profileFieldMap.get(1L).getValue();
                }
            }
        }

        if(template != null) {
            if(template.getResearchNeedsPriorities() != null) {
                researchNeedsPriorities = template.getResearchNeedsPriorities();
            }

            if(template.getHypothesisBusinessNeed() != null) {
                hypothesisBusinessNeed = template.getHypothesisBusinessNeed();
            }

            if(template.getMarkets() != null && template.getMarkets() > 0) {
                if(SynchroGlobal.getEndMarkets().containsKey(template.getMarkets().intValue())) {
                    markets = SynchroGlobal.getEndMarkets().get(template.getMarkets().intValue());
                } else {
                    markets = "";
                }
            }

            if(template.getProducts() != null) {
                products = template.getProducts();
            }

            if(template.getBrands() != null) {
                brands = template.getBrands();
            }

            if(template.getCategories() != null) {
                categories = template.getCategories();
            }

            if(template.getDeliveryDate() != null) {
                deliveryDate = dateFormat.format(template.getDeliveryDate());
            }

            if(template.getOutputFormat() != null) {
                if(template.getOutputFormat().equals(-1)) {
                    outputFormat = "None";
                } else {
                    outputFormat = GrailGlobals.BriefTemplateOutputType.getById(template.getOutputFormat());
                }
            }

            if(template.getRecipientEmail() != null) {
                recipientEmail = template.getRecipientEmail();
            }

            if(template.getBatContact() != null && template.getBatContact() > 0) {
                try {
                    User batUser = getUserManager().getUser(template.getBatContact());
                    if(batUser != null) {
                        batContact = batUser.getFirstName() + " " + batUser.getLastName();
                    }
                } catch (UserNotFoundException e) {
                    batContact = "";
                }
            }

            if(template.getDataSource() != null) {
                dataSource = template.getDataSource();
            }

            if(template.getMethodologyType() != null) {
                methodologyType = KantarGlobals.KantarMethodologyType.getNameById(template.getMethodologyType().intValue());
            }

            if(template.getId() != null) {
                code = generateProjectCode(template.getId());
            }
        }
        kantarBriefTemplateMap.put("code", code);
        kantarBriefTemplateMap.put("userName", userName);
        kantarBriefTemplateMap.put("userEmail", userEmail);
        kantarBriefTemplateMap.put("userLocation", userLocation);
        kantarBriefTemplateMap.put("userRole", userRole);
        kantarBriefTemplateMap.put("researchNeedsPriorities", researchNeedsPriorities);
        kantarBriefTemplateMap.put("hypothesisBusinessNeed", hypothesisBusinessNeed);
        kantarBriefTemplateMap.put("dataSource", dataSource);
        kantarBriefTemplateMap.put("markets", markets);
        kantarBriefTemplateMap.put("products", products);
        kantarBriefTemplateMap.put("brands", brands);
        kantarBriefTemplateMap.put("categories", categories);
        kantarBriefTemplateMap.put("methodologyType", methodologyType);
        kantarBriefTemplateMap.put("deliveryDate", deliveryDate);
        kantarBriefTemplateMap.put("outputFormat", outputFormat);
        kantarBriefTemplateMap.put("recipientEmail", recipientEmail);
        kantarBriefTemplateMap.put("isDraft", template.getDraft());
        kantarBriefTemplateMap.put("batContact", batContact);

        return kantarBriefTemplateMap;
    }

    public static Map<String, Object> toKantarBriefTemplateMap(final KantarBriefTemplate template, final Long userid) {

        User user = null;
        try{
            user = getUserManager().getUser(userid);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }
        return toKantarBriefTemplateMap(template, user);
    }


    public static String generateProjectCode(final Long id) {
        if(id != null) {
            int maxDigits = 5;
            String prepend = "K";
            String result = "";
            int len = id.toString().length();
            if(id > 0 && len < maxDigits) {
                result += prepend;
                for(int i = 0; i < (maxDigits - len); i++) {
                    result += "0";
                }
                result += id.toString();
                return result;
            } else {
                return prepend + id.toString();
            }
        } else {
            return "";
        }
    }

    public static UserManager getUserManager() {
        if(userManager == null) {
            return JiveApplication.getContext().getSpringBean("userManager");
        }
        return userManager;

    }

    public static ProfileManager getProfileManager() {
        if(profileManager == null) {
            return JiveApplication.getContext().getSpringBean("profileManager");
        }
        return profileManager;

    }



}
