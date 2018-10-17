package com.grail.dwr.service;

import com.grail.cart.GrailCartBean;
import com.grail.cart.util.AttachmentUtil;
import com.grail.cart.util.CartUtil;
import com.grail.cart.util.GrailCartTransformer;
import com.grail.util.BATConstants;
import com.jivesoftware.base.*;
import com.jivesoftware.community.*;
import com.jivesoftware.community.action.util.JiveDWRUtils;
import com.jivesoftware.community.dwr.RemoteSupport;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: vivek
 * Date: Feb 10, 2012
 * Time: 5:34:22 PM
 */
public class DocumentCartService extends RemoteSupport {

    private static final Logger log = Logger.getLogger(DocumentCartService.class);

    private AttachmentUtil attachmentUtil;
    private AttachmentManager attachmentManager;
    private DocumentManager documentManager;

    public void setAttachmentUtil(AttachmentUtil attachmentUtil) {
        this.attachmentUtil = attachmentUtil;
    }

    public void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }


    public String addToCart(String documentID, String type, String documentTitle, String attachmentID) throws DocumentObjectNotFoundException {

        String dwrStatus = "Successfully added to the cart.";
        boolean isExceeded = false;
        int cartLength = 1;
        User jiveUser = JiveDWRUtils.getUser();
        Map userProps = jiveUser.getProperties();

        //if there is pointer
        if(userProps.containsKey(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY))
            cartLength = Integer.parseInt((String)userProps.get(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY));

        //TODO : Change to active Cart Length.
        int activeCartLength = CartUtil.getCartItemsForDownload(jiveUser).size();

        isExceeded = hasUserExceededCartLimit(jiveUser,documentID);
        if( !isExceeded )
        {
            if(!CartUtil.isAlreadyOnCart(jiveUser,documentID,"pdf",attachmentID)) {
                String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM_KEY + cartLength;
                String uxp = GrailCartTransformer.toUXP(documentID,String.valueOf(cartLength),"false",type,documentTitle,attachmentID);
                cartLength++;
                String updatedCartIndex = String.valueOf(cartLength);
                userProps.put(cartKey,uxp);
                userProps.put(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY, updatedCartIndex);
                dwrStatus = updateUXP(dwrStatus, jiveUser, userProps);
            } else {
                dwrStatus = "This document already added to cart!";
            }
        } else {
            if(type.equals("attachment") && canAttachmentAddToCart(jiveUser, documentID, attachmentID)) {
                String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM_KEY + cartLength;
                String uxp = GrailCartTransformer.toUXP(documentID,String.valueOf(cartLength),"false","attachment",documentTitle,attachmentID);
                cartLength++;
                String updatedCartIndex = String.valueOf(cartLength);
                userProps.put(cartKey,uxp);
                //userProps.put(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY, updatedCartIndex);
                dwrStatus = "Successfully added the attachment!";
            } else if(type.equals("pdf") && canDocumentAddToCart(jiveUser, documentID, attachmentID)) {
                String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM_KEY + cartLength;
                String uxp = GrailCartTransformer.toUXP(documentID,String.valueOf(cartLength),"false","pdf",documentTitle,attachmentID);
                cartLength++;
                String updatedCartIndex = String.valueOf(cartLength);
                userProps.put(cartKey,uxp);
                //userProps.put(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY, updatedCartIndex);
                dwrStatus = "Successfully added the pdf!";
            } else {
                int approvedCartLimit = JiveGlobals.getJiveIntProperty(BATConstants.GRAIL_BAT_CART_ITEM_LIMIT_KEY,5);
                dwrStatus = "Maximum cart limit exceeded. You can add upto "+approvedCartLimit+" studies (either summary/attachments or both) to cart at a time. Please download and clear the cart before adding new items.";
                log.info(dwrStatus  + " --- "+jiveUser.getUsername());
            }
        }

        return dwrStatus;
    }

    public String addContentAndAttachmentsToCart(String documentID, String documentTitle, String attachmentID) {

        String dwrStatus = "Successfully added to the cart.";
        boolean isExceeded = false;
        int cartLength = 1;
        User jiveUser = JiveDWRUtils.getUser();
        Map userProps = jiveUser.getProperties();

        //if there is pointer
        if(userProps.containsKey(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY))
            cartLength = Integer.parseInt((String)userProps.get(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY));

        /*int counter = 0;
        if(!CartUtil.isAlreadyOnCart(jiveUser,documentID,"pdf",attachmentID)){
            counter++;
        }
        try {
            for (Attachment a : JiveApplication.getContext().getAttachmentManager().getAttachments(this.documentManager.getDocument(Long.parseLong(documentID)))){
                if(!CartUtil.isAlreadyOnCart(jiveUser,documentID,"attachment",String.valueOf(a.getID()))){
                    counter++;
                }
            }
        } catch (DocumentObjectNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/

        int approvedCartLimit = JiveGlobals.getJiveIntProperty(BATConstants.GRAIL_BAT_CART_ITEM_LIMIT_KEY,5);
        List<GrailCartBean> items = CartUtil.getCartItemsForDownload(jiveUser);
        Set<String> documents = new HashSet<String>();
        for(GrailCartBean item : items){
            documents.add(item.getDocumentID());
        }
        log.info("No of documents - " + documents.size());
        int currentCartSize = documents.size();
        //If the document content/attachments are already part of the cart items then don't increment the cart size
        if(!documents.contains(documentID)){
            currentCartSize = currentCartSize + 1;
        }
        if( currentCartSize > approvedCartLimit){
            isExceeded = true;
        }
        //int cartLength = ((Integer)userProps.get(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER)).intValue();
        if( !isExceeded ){
            if(!CartUtil.isAlreadyOnCart(jiveUser,documentID,"pdf",attachmentID)){
                String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM_KEY + cartLength;
                String uxp = GrailCartTransformer.toUXP(documentID,String.valueOf(cartLength),"false","pdf",documentTitle,attachmentID);
                cartLength++;
                String updatedCartIndex = String.valueOf(cartLength);
                userProps.put(cartKey,uxp);
                userProps.put(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY, updatedCartIndex);
            } else {
                dwrStatus = "This document already added to cart!";
            }
            try {
                for (Attachment a : getDocumentAttachments(documentID)){
                    if(!CartUtil.isAlreadyOnCart(jiveUser,documentID,"attachment",String.valueOf(a.getID()))){
                        String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM_KEY + cartLength;
                        String uxp = GrailCartTransformer.toUXP(documentID,String.valueOf(cartLength),"false","attachment",documentTitle,String.valueOf(a.getID()));
                        cartLength++;
                        String updatedCartIndex = String.valueOf(cartLength);
                        userProps.put(cartKey,uxp);
                        userProps.put(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY, updatedCartIndex);
                        //dwrStatus = updateUXP(dwrStatus, jiveUser, userProps);
                    }
                }
            } catch (DocumentObjectNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            dwrStatus = updateUXP(dwrStatus, jiveUser, userProps);

        } else {
            dwrStatus = "Maximum cart limit exceeded. You can add upto "+approvedCartLimit+" studies (either summary/attachments or both) to cart at a time. Please download and clear the cart before adding new items.";
            log.info(dwrStatus  + " --- "+jiveUser.getUsername());
        }

        return dwrStatus;
    }

    private boolean isDocumentAttachmentsExistsOnCart(final User jiveUser, final String documentID) throws DocumentObjectNotFoundException {
        boolean isExits = false;
        for(Attachment a : getDocumentAttachments(documentID)) {
            if(CartUtil.isAlreadyOnCart(jiveUser,documentID,"attachment",String.valueOf(a.getID()))) {
                isExits = true;
                break;
            }
        }
        return isExits;
    }

    private Iterable<Attachment> getDocumentAttachments(final String documentID) throws DocumentObjectNotFoundException {
        return JiveApplication.getContext().getAttachmentManager().getAttachments(this.documentManager.getDocument(Long.parseLong(documentID)));
    }

    protected boolean hasUserExceededCartLimit(User jiveUser, String documentID) throws DocumentObjectNotFoundException {
        Map userProps = jiveUser.getProperties();
        boolean exceeded = false;

        int approvedCartLimit = JiveGlobals.getJiveIntProperty(BATConstants.GRAIL_BAT_CART_ITEM_LIMIT_KEY,5);

        List<GrailCartBean> items = CartUtil.getCartItemsForDownload(jiveUser);
        String archived = (String)userProps.get(BATConstants.GRAIL_BAT_DOC_CART_ITEMS_ARCHIVED_KEY);
        Set<String> documents = new HashSet<String>();
        for(GrailCartBean item : items){
            documents.add(item.getDocumentID());
        }
        log.info("No of documents - " + documents.size());
        int currentCartSize = documents.size();
//        if( currentCartSize >= approvedCartLimit){
//            //If documentID is empty, then the check is for search page. Hence currentCartSize will give the limit of the cart items.
//            //If documentID is not empty and it is in documents set then
//            // any further inclusion of attachments from those documents should not contribute in the limit exceed check.
////            if(documentID == ""){
////                exceeded = true;
////            }else if(documentID != "" && (documents.contains(documentID) || isDocumentAttachmentsExistsOnCart(jiveUser, documentID))){
////                exceeded = true;
////            }
//            exceeded = true;
//        }
        return (currentCartSize >= approvedCartLimit);
    }

    public String removeFromCart(String cartItemID) throws DocumentObjectNotFoundException {
        String dwrStatus = "Successfully removed from the cart.";
        User jiveUser = JiveDWRUtils.getUser();
        Map userProps = jiveUser.getProperties();
        GrailCartBean bean = null;
        /*Map userProps = jiveUser.getProperties();
        //The item which needs to be removed
        String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM + cartItemID;

        String uxpRemove = StringUtils.replaceFirst((String) userProps.get(cartKey),"false","true");
        log.debug(" Removed from the cart  :::: "+uxpRemove);
        userProps.put(cartKey, uxpRemove);*/
        //CartUtil.isAlreadyOnCart(jiveUser,documentID,"pdf",attachmentID)
        String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM_KEY + cartItemID;
        String uxp = (String)userProps.get(cartKey);
        if(uxp != null){
            bean = GrailCartTransformer.UXPToBean(uxp);
        }
        if(bean != null && CartUtil.isCartItemExits(cartItemID, jiveUser)
                &&
                ((bean.getType().equals("attachment") && !canUndoAttachment(jiveUser, bean)) ||
                        (bean.getType().equals("pdf") && !canUndoDocument(jiveUser, bean)))
                ) {
            userProps = toggleRemoveFlag(cartItemID,jiveUser,"false","true");
            dwrStatus = updateUXP(dwrStatus, jiveUser, userProps);
        } else {
            dwrStatus =  "Already removed from the cart.";
        }

        return dwrStatus;
    }

    public String undoFromCart(String cartItemID) throws DocumentObjectNotFoundException {
        String dwrStatus = "Successfully added back to the cart.";
        boolean isExceeded = false;
        User jiveUser = JiveDWRUtils.getUser();
        Map userProps = jiveUser.getProperties();
        GrailCartBean bean = null;
        //Handle Special Case:
        // If the User has removed the item, then we allow him to add one more item to the cart.
        //If the user tries to undo an item, then before we add the *ITEM* back to *CART* we have to ensure that the CART LIMIT isn't exceeded.

        //if there is pointer
        String documentID= "";
        String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM_KEY + cartItemID;
        String uxp = (String)userProps.get(cartKey);
        if(uxp != null){
            bean = GrailCartTransformer.UXPToBean(uxp);
            documentID = bean.getDocumentID();
        }
        isExceeded = hasUserExceededCartLimit(jiveUser,documentID);
        if(!isExceeded)
        {
            userProps = toggleRemoveFlag(cartItemID,jiveUser,"true","false");
            dwrStatus = updateUXP(dwrStatus, jiveUser, userProps);
        } else {
            if(bean != null
                    && ((bean.getType().equals("attachment") && canUndoAttachment(jiveUser, bean))
                    || (bean.getType().equals("pdf") && canUndoDocument(jiveUser, bean)))
                    ) {
                userProps = toggleRemoveFlag(cartItemID,jiveUser,"true","false");
                dwrStatus = updateUXP(dwrStatus, jiveUser, userProps);
            } else {
                int approvedCartLimit = JiveGlobals.getJiveIntProperty(BATConstants.GRAIL_BAT_CART_ITEM_LIMIT_KEY,5);
                dwrStatus = "Maximum cart limit exceeded. You can add upto "+approvedCartLimit+" " +
                        "studies (either summary/attachments or both) to cart at a time. Please download and clear " +
                        "the cart before adding new items.";
            }
        }
        return dwrStatus;
    }

    private boolean canAttachmentAddToCart(final User jiveUser, final String documentID, final String attachmentID) {
        return (CartUtil.isAlreadyOnCart(jiveUser,documentID,"pdf",attachmentID) && !CartUtil.isAlreadyOnCart(jiveUser,
                documentID,"attachment",attachmentID));
    }

    private boolean canUndoAttachment(final User jiveUser, final GrailCartBean bean) {
        boolean undo = false;
        if(CartUtil.isAlreadyOnCart(jiveUser,bean.getDocumentID(),"attachment",bean.getAttachmentID())
                && bean.getRemoved().equals("true")) {
            if(CartUtil.isAlreadyOnCart(jiveUser,bean.getDocumentID(),"pdf",bean.getAttachmentID())) {
                GrailCartBean cartBean = CartUtil.getCartItem(jiveUser, bean.getDocumentID(), "pdf", bean.getAttachmentID());
                if(cartBean.getRemoved().equals("false")) {
                    undo = true;
                }
            }
        }
        return undo;
    }

    private boolean canDocumentAddToCart(final User jiveUser, final String documentID, final String attachmentID)
            throws DocumentObjectNotFoundException {
        return (!CartUtil.isAlreadyOnCart(jiveUser,documentID,"pdf",attachmentID)
                && isDocumentAttachmentsExistsOnCart(jiveUser, documentID));
    }

    private boolean canUndoDocument(final User jiveUser, final GrailCartBean bean) throws DocumentObjectNotFoundException {
        boolean undo = false;
        if(CartUtil.isAlreadyOnCart(jiveUser,bean.getDocumentID(),"pdf",bean.getAttachmentID()) && bean.getRemoved().equals("true")) {
            if(isDocumentAttachmentsExistsOnCart(jiveUser, bean.getDocumentID())) {
                List<GrailCartBean> attachments = CartUtil.getDocumentAttachmentsOnCart(jiveUser, bean.getDocumentID());
                for(GrailCartBean attachment: attachments) {
                    if(attachment.getRemoved().equals("false")) {
                        undo = true;
                        break;
                    }
                }
            }
        }
        return undo;
    }

    protected Map toggleRemoveFlag(String cartItemID, User jiveUser, String oldValue, String newValue){
        Map userProps = jiveUser.getProperties();
        //The item which needs to be removed
        String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM_KEY + cartItemID;
        String uxpUndo = StringUtils.replaceFirst((String) userProps.get(cartKey),oldValue,newValue);
        //log.debug(" Added back to the cart  :::: "+uxpUndo);
        userProps.put(cartKey, uxpUndo);
        return userProps;
    }

    public String uncheckFromCart(String documentID, String type, String attachmentID){
        String dwrStatus = "Completely removed from the cart.";
        User jiveUser = JiveDWRUtils.getUser();
        if(CartUtil.isAlreadyOnCart(jiveUser,documentID,type,attachmentID)) {
            if(type.equalsIgnoreCase("pdf"))
                dwrStatus = removeCartItem(documentID,type);
            else if(type.equalsIgnoreCase("attachment"))
                dwrStatus =  removeCartItem(documentID,type, attachmentID);

        } else {
            dwrStatus =  "This document already removed from the cart.";
        }

        return dwrStatus;

    }

    public String uncheckContentAndAttachmentsFromCart(String document, String attachmentID){
        String dwrStatus = "Completely removed from the cart.";
        dwrStatus = removeCartItem(document, "pdf");
        try {
            for (Attachment a : JiveApplication.getContext().getAttachmentManager().getAttachments(this.documentManager.getDocument(Long.parseLong(document)))){
                dwrStatus =  removeCartItem(document,"attachment",String.valueOf(a.getID()));
            }
        } catch (DocumentObjectNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return dwrStatus;
    }

    protected String removeCartItem(String... args){
        String dwrStatus = "Completely removed from the cart.";
        User jiveUser = JiveDWRUtils.getUser();
        Map userProps = jiveUser.getProperties();
        int cartLength = Integer.parseInt((String)userProps.get(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY));
        String archived = (String)userProps.get(BATConstants.GRAIL_BAT_DOC_CART_ITEMS_ARCHIVED_KEY);
        StringBuilder sb;
        if(archived == null)
            sb  = new StringBuilder("");
        else
            sb = new StringBuilder(archived);

        for(int i = cartLength - 1; i >= 0; i--) {
            String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM_KEY+i;
            String uxp = (String)userProps.get(cartKey);
            if(uxp != null)
            {
                //Logic for removal:
                // 1) For Content Uncheck: If the documentID & type = pdf, then remove the cartItem
                // 2) For Attachment Uncheck: If the documentID & type = attachment & attachmentID matches, then remove the cartItem
                GrailCartBean cartBean = GrailCartTransformer.UXPToBean(uxp);
                if(cartBean.getDocumentID().equalsIgnoreCase(args[0]) && cartBean.getType().equalsIgnoreCase(args[1]) && args.length == 2 )
                {
                    userProps.remove(cartKey);
                    sb.append(i+"|");
                } else if(cartBean.getDocumentID().equalsIgnoreCase(args[0]) && cartBean.getType().equalsIgnoreCase(args[1]) && args.length == 3 && cartBean.getAttachmentID().equalsIgnoreCase(args[2]) ){
                    userProps.remove(cartKey);
                    sb.append(i+"|");
                }
            }
        }//for ends

        userProps.put(BATConstants.GRAIL_BAT_DOC_CART_ITEMS_ARCHIVED_KEY,sb.toString());
        return updateUXP(dwrStatus,jiveUser,userProps);
    }


    public String clearCart() {
        String dwrStatus = "Successfully cleared the cart.";
        User jiveUser = JiveDWRUtils.getUser();
        Map userProps = jiveUser.getProperties();
        int cartLength = Integer.parseInt((String)userProps.get(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY));

        for(int i = cartLength - 1; i >= 0; i--) {
            String cartKey = BATConstants.GRAIL_BAT_DOC_CART_ITEM_KEY+i;
            if(userProps.containsKey(cartKey))
                userProps.remove(cartKey);
        }
        this.attachmentUtil.cleanDirectories(String.valueOf(jiveUser.getID()));

        userProps.remove(BATConstants.GRAIL_BAT_DOC_CART_ITEMS_ARCHIVED_KEY);
        userProps.put(BATConstants.GRAIL_BAT_DOC_CART_DOWNLOAD_POINTER_KEY,String.valueOf(1));
        dwrStatus = updateUXP(dwrStatus, jiveUser, userProps);

        return dwrStatus;
    }

    protected String updateUXP(String dwrStatus, User jiveUser, Map userProps) {
        UserTemplate ut = new UserTemplate(jiveUser);
        //ut.setProperties(userProps);
        ut.getProperties().putAll(userProps);

        try {
            userManager.updateUser(ut);
        } catch (UserNotFoundException e) {
            dwrStatus = e.getMessage();
            e.printStackTrace();
        } catch (UserAlreadyExistsException e) {
            dwrStatus = e.getMessage();
            e.printStackTrace();
        }
        return dwrStatus;
    }

    public String hasDownloadCompleted(){
        String dwrStatus = "Download is in progress...";
       /* if(JiveDWRUtils.getSession().getAttribute("completed")!=null){
            String isCompleted = JiveDWRUtils.getSession().getAttribute("completed").toString();
            if(isCompleted.equalsIgnoreCase("YES")){
                JiveDWRUtils.getSession().removeAttribute("completed");
                dwrStatus = "Successfully downloaded the cart items.";
            }
        }*/
        return  dwrStatus;

    }

    public int getCartItemsCount(){
        User user = JiveDWRUtils.getUser();
        return CartUtil.getCartItemsCount(user);
    }
}
