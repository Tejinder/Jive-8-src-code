package com.grail.cart.util;

import com.grail.cart.GrailCartBean;
import com.grail.util.BATConstants;
import com.jivesoftware.base.User;
import com.jivesoftware.community.Attachment;
import com.jivesoftware.community.AttachmentNotFoundException;
import com.jivesoftware.community.Document;
import com.jivesoftware.community.lifecycle.JiveApplication;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: vivek
 * Date: Feb 14, 2012
 * Time: 6:49:43 PM
 *
 */
public class CartUtil {

    private static final Logger log = Logger.getLogger(CartUtil.class);


    public static List<GrailCartBean> getCurrentCartItems(User jiveUser) {
        Map userProps = jiveUser.getProperties();
        List<GrailCartBean> cartItems = new ArrayList<GrailCartBean>();
        if(userProps.containsKey(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY))
        {
            //fetch all the cart items
            int cartLength = Integer.parseInt((String)userProps.get(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY));

            for(int i = 1; i < cartLength; i++)
            {
                String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM_KEY+i;
                String uxp = (String)userProps.get(cartKey);
                if(uxp != null)
                    cartItems.add(GrailCartTransformer.UXPToBean(uxp));

            }
            log.info("Total number of Cart Items for the User :::  "+jiveUser.getUsername()+ " is "+cartItems.size());

        }   else
        {
            //User's cart is empty
            log.info("Cart is empty for the User :::  "+jiveUser.getUsername());
            cartItems = Collections.emptyList();
        }

        return cartItems;
    }

    public static boolean isAlreadyOnCart(User jiveUser, String documentID, String type, String attachmentID)
    {
        boolean doesExists = false;

        List<GrailCartBean> items = getCurrentCartItems(jiveUser);
        if(items != null && items.size() > 0 )
        {
            Iterator<GrailCartBean> itr = items.iterator();
            while(itr.hasNext()){
                GrailCartBean bean = itr.next();
                if(type.equalsIgnoreCase("attachment") && type.equalsIgnoreCase(bean.getType()) )
                {
                    if( bean.getAttachmentID().equalsIgnoreCase(attachmentID) ){
                        doesExists = true;
                        break;
                    }

                } else if(type.equalsIgnoreCase("pdf") && type.equalsIgnoreCase(bean.getType()))
                {
                    if( bean.getDocumentID().equalsIgnoreCase(documentID) ){
                        doesExists = true;
                        break;
                    }
                }
            }
        }
        log.info(" Does the item --- "+documentID +" --- exists on cart :::        "+type );
        return doesExists;
    }

    public static GrailCartBean getCartItem(User jiveUser, String documentID, String type, String attachmentID) {
        List<GrailCartBean> items = getCurrentCartItems(jiveUser);
        if(items != null && items.size() > 0 )
        {
            Iterator<GrailCartBean> itr = items.iterator();
            while(itr.hasNext()){
                GrailCartBean bean = itr.next();
                if(type.equalsIgnoreCase("attachment") && type.equalsIgnoreCase(bean.getType())) {
                    if( bean.getAttachmentID().equalsIgnoreCase(attachmentID) ){
                        return bean;
                    }

                } else if(type.equalsIgnoreCase("pdf") && type.equalsIgnoreCase(bean.getType())) {
                    if( bean.getDocumentID().equalsIgnoreCase(documentID) ) {
                        return bean;
                    }
                }
            }
        }
        return null;
    }

    public static List<GrailCartBean> getDocumentAttachmentsOnCart(User jiveUser, String documentID) {
        List<GrailCartBean> items = getCurrentCartItems(jiveUser);
        List<GrailCartBean> attachments = new ArrayList<GrailCartBean>();
        if(items != null && items.size() > 0 )
        {
            Iterator<GrailCartBean> itr = items.iterator();
            while(itr.hasNext()){
                GrailCartBean bean = itr.next();
                if(bean.getType().equalsIgnoreCase("attachment") && bean.getDocumentID().equalsIgnoreCase(documentID)) {
                    attachments.add(bean);
                }
            }
        }
        return attachments;
    }

    public static boolean isContentOrAttachmentsAlreadyOnCart(User jiveUser, String documentID, Document document){
        boolean doesExists = true;
        if(!isAlreadyOnCart(jiveUser,documentID,"pdf","")){
            return false;
        }
        for (Attachment a : JiveApplication.getContext().getAttachmentManager().getAttachments(document)){
            if(!isAlreadyOnCart(jiveUser,documentID,"attachment",String.valueOf(a.getID()))){
                doesExists = false;
                break;
            }
        }
        log.info(" Does the item --- "+documentID +" --- and attachments exists on cart :::        ");
        return doesExists;
    }


    public static List<GrailCartBean> getCartItemsForDownload(User jiveUser){
        Map userProps = jiveUser.getProperties();
        List<GrailCartBean> cartItems = new ArrayList<GrailCartBean>();
        if(userProps.containsKey(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY))
        {
            //fetch all the cart items
            int cartLength = Integer.parseInt((String)userProps.get(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY));

            for(int i = 1; i < cartLength; i++)
            {
                String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM_KEY+i;
                String uxp = (String)userProps.get(cartKey);
                if(uxp != null)
                {
                    GrailCartBean bean = GrailCartTransformer.UXPToBean(uxp);
                    if( bean.getRemoved().equalsIgnoreCase("false") )
                    {
                        cartItems.add(bean);
                    }
                }

            }
            log.debug("Total number of Cart Items to be exported for the user :::  "+jiveUser.getUsername()+ " is "+cartItems.size());

        }   else
        {
            //User's cart is empty
            log.debug("Cart is empty for the User :::  "+jiveUser.getUsername());
            cartItems = Collections.emptyList();
        }
        return cartItems;
    }

    public static Attachment getAttachment(String attachmentID){
        long attachID = Long.parseLong(attachmentID);
        log.info("Attachment ID   ::: "+attachmentID);
        Attachment attach = null;
        try {
            attach = JiveApplication.getContext().getAttachmentManager().getAttachment(attachID);
            log.debug(" Attachment Details  "+attach.toString());

        } catch (AttachmentNotFoundException e) {
            e.printStackTrace();
            log.error("Attachment not found .... "+attachmentID);
        }
        //log.debug(" Attachment Details  "+attach.toString());

        return attach;
    }

    public static int getCartItemsCount(User jiveUser){
        List<GrailCartBean> items = getCartItemsForDownload(jiveUser);
        Set<String> documents = new HashSet<String>();
        for(GrailCartBean item : items){
            documents.add(item.getDocumentID());
        }
        log.info("No of documents - " + documents.size());
        return documents.size();
    }

    public static boolean isCartItemExits(String cartItemID, User jiveUser){
        List<GrailCartBean> items = getCartItemsForDownload(jiveUser);
        boolean isExists = false;
        for(GrailCartBean item: items) {
            if(item.getCartItemID().endsWith(cartItemID)) {
                isExists = true;
            }
        }
        return isExists;
    }

}
