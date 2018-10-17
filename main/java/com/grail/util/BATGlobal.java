package com.grail.util;

/**
 * Created with IntelliJ IDEA.
 * User: Bhaskar
 * Date: 8/29/13
 * Time: 12:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class BATGlobal {

    public enum PortalType {
        RKP("rkp"),
        SYNCHRO("synchro"),
        GRAIL("grail"),
        KANTAR("kantar"),
        KANTAR_REPORT("kantar_report"),
        ORACLE_DOCUMENTS("oracle_documents"),
        OSP_ORACLE("oracle"),
        OSP_SHARE("share");

        private String displayName;

        PortalType(String displayName) {
            this.displayName = displayName;
        }

        public static PortalType getById(int id) {
            for (PortalType region : values()) {
                if (region.ordinal() == id) {
                    return region;
                }
            }
            throw new IllegalArgumentException("Specified id does not relate to a valid Region");
        }
        public String displayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }

}
