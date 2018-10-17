package com.grail.util;

/**
 * Created by IntelliJ IDEA.
 * User: Anantha
 * Date: 10/17/11
 * Time: 5:09 PM
 * To define all the constants defined for BAT customization - [Change password]
 */
public class BATConstants {

    // To store number days password will be valid before requesting it to change
    public static final String GRAIL_BAT_PASSWORD_VALID_DAYS = "grail.bat.pwd.valid.days";

    // To store number days before password expires so that notification can be sent to the user
    public static final String GRAIL_BAT_PASSWORD_NOTIFICATION_DAYS = "grail.bat.pwd.notification.days";

    // To send multiple email notifications to the user regarding changing password
    public static final String GRAIL_BAT_SEND_MULTIPLE_PASSWORD_NOTIFICATIONS = "grail.bat.pwd.multiple.notifications";

    // To store last password modified date - extended user property
    public static final String GRAIL_BAT_PASSWORD_LAST_MODIFIED_DATE = "user.last.pwd.modified.date";

    // To store password notification sent flag - extended user property
    public static final String GRAIL_BAT_PASSWORD_CHANGE_NOTIFICATION = "user.last.pwd.change.notification";

    //Maintains the pointer to track the password history
    public static final String GRAIL_BAT_PASSWORD_HISTORY_POINTER = "grail.bat.passwordHistory.pointer";

    //Maintains the pointer to track the document cart items for every user
    public static final String GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY = "grail.bat.cart.download.pointer";

    //The cart item property key for every download the user adds
    public static String GRAIL_BAT_DOC_CART_ITEM_KEY = "grail.bat.cart.item.";

    public static String GRAIL_BAT_DOC_CART_ITEMS_ARCHIVED_KEY = "grail.bat.cart.items.archived";

    public static String GRAIL_BAT_CART_ITEM_LIMIT_KEY = "grail.bat.cart.items.limit";

    public static String GRAIL_DOWNLOAD_ZIP_NAME_KEY = "grail.bat.cart.download.name";

    public static String GRAIL_BAT_CONCURRENT_SESSION_ERROR_MSG_KEY = "grail.bat.concurrent.error.msg";

    public static final String GRAIL_PORTAL_TYPE = "grail.portal.type";
    public static final String GRAIL_RKP_DISCLAIMER_ACCEPTED = "grail.rkp.disclaimer.accepted";
    public static final String GRAIL_SYNCHRO_DISCLAIMER_ACCEPTED = "grail.synchro.disclaimer.accepted";
    public static final String GRAIL_PORTAL_DISCLAIMER_ACCEPTED = "grail.portal.disclaimer.accepted";
    public static final String KANTAR_PORTAL_DISCLAIMER_ACCEPTED = "kantar.portal.disclaimer.accepted";
    public static final String KANTAR_REPORT_PORTAL_DISCLAIMER_ACCEPTED = "kantar.report.portal.disclaimer.accepted";
    public static final String BAT_DISCLAIMER_ACCEPTED = "bat.disclaimer.accepted";
    public static final String GRAIL_FORCE_PORTAL_OPTIONS = "grail.portal.options.force";

    public static final String DISCLAIMER_URL = "/disclaimer.jspa";

    public static final String JIVE_RKP_GROUP_NAME = "RKP";
    
    public static final String JIVE_SYNCHRO_GROUP_NAME = "SYNCHRO";

    public static final String BAT_BASE_URL = "grail.bat.baseURL";

    public static final String GRAIL_REFERRER_URL = "grail.referrer.url";

}
