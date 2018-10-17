package com.grail.cart.util;

import com.grail.cart.GrailCartBean;
import com.jivesoftware.util.StringUtils;


import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: vivek
 * Date: Feb 10, 2012
 * Time: 8:19:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrailCartTransformer {

    //private static final String GRAIL_CART_USER_XPROP_KEY = "grail.cart.item";

    public static String beanToUXP(GrailCartBean bean){
        StringBuilder sb = new StringBuilder();
        sb.append(bean.getDocumentID() +"|");
        sb.append(bean.getCartItemID() +"|");
        sb.append(bean.getRemoved() +"|");
        sb.append(bean.getType() +"|");
        sb.append(bean.getDocumentTitle() +"|");
        sb.append(bean.getAttachmentID());

        List<String> attachments =  bean.getAttachments();
            if(attachments != null && attachments.size() > 0)
            {
                for(String attachID : attachments)
                sb.append(attachID +":");
            }
        return sb.toString();
    }


    public static GrailCartBean UXPToBean(String uxp){
        //String to transform - 1011|1|false|pdf|Test Doc|10025

        String[] cartProps = StringUtils.split(uxp,"|");       
        GrailCartBean cart = new GrailCartBean();
        cart.setDocumentID(cartProps[0]);
        cart.setCartItemID(cartProps[1]);
        cart.setRemoved(cartProps[2]);          
        cart.setType(cartProps[3]);
        cart.setDocumentTitle(cartProps[4]);
        if(cartProps.length > 5)
        {
            String attachment = cartProps[5];
            cart.setAttachmentID(attachment);
            /*if( attachments != null )
            {
                String attachs[] = attachments.split(":");
                cart.setAttachments(Arrays.asList(attachs));
            }*/
        }
        return cart;
    }


    public static String toUXP(String docID, String cartItemID,String isRemoved, String type, String docTitle, String attachID){
        StringBuilder sb = new StringBuilder();
        sb.append(docID+"|");
        sb.append(cartItemID+"|");
        sb.append(isRemoved+"|");
        sb.append(type+"|");
        sb.append(docTitle+"|");
        sb.append(attachID);

        return sb.toString();
    }
}
