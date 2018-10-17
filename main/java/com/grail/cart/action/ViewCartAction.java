package com.grail.cart.action;

import com.grail.cart.GrailCartBean;
import com.grail.cart.util.CartUtil;
import com.grail.cart.util.GrailCartTransformer;
import com.grail.util.BATConstants;
import com.jivesoftware.base.User;
import com.jivesoftware.community.action.JiveActionSupport;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Action lists all the items which have been added by the user
 * for downloading the attachments or PDFs.
 *
 * The user can then decide to remove, add, undo, download, clear the cart.
 *
 * User: vivek
 * Date: Feb 13, 2012
 * Time: 1:05:17 PM
 *
 */
public class ViewCartAction extends JiveActionSupport {

    private static final Logger log = Logger.getLogger(ViewCartAction.class);

    private List<GrailCartBean> cartItems;

    @Override
    public String input()
    {
        log.info(" Fetch the User CartItems from the UXP");


        return INPUT;

    }

    public List<GrailCartBean> getCartItems() {

        //We first check the pointer reference. If this property has been set, then
        //we proceed further in fetching all the items. Else we conclude that there are
        //no items in the cart.        

        cartItems = CartUtil.getCurrentCartItems(getUser());
        return cartItems;
    }

    /*protected List<GrailCartBean> getCurrentCartItems(User jiveUser) {
        Map userProps = jiveUser.getProperties();
        if(userProps.containsKey(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY))
        {
            //fetch all the cart items
            int cartLength = Integer.parseInt((String)userProps.get(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY));
            cartItems = new ArrayList<GrailCartBean>();
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
    }*/
}
