package com.grail.cart;

import java.io.Serializable;
import java.util.List;

/**
 * A POJO to hold the CartItem details
 *
 * User: vivek
 * Date: Feb 10, 2012
 * Time: 8:01:48 PM
 *
 */
public class GrailCartBean implements Serializable {

    String documentID;
    String documentTitle;
    String type; //pdf | attachments
    List<String> attachments;
    String attachmentID;
    String cartItemID;
    String isRemoved;
    String documentLoc;

    public GrailCartBean(){

    }

    public GrailCartBean(String documentID, String documentTitle, String type, String attachmentID, String cartItemID, String isRemoved, String filename)
    {
        this.documentID = documentID;
        this.documentTitle = documentTitle;
        this.type = type;
        this.attachmentID = attachmentID;
        this.cartItemID = cartItemID;
        this.isRemoved = isRemoved;
        this.documentLoc = documentLoc;
    }

    public String getRemoved() {
        return isRemoved;
    }

    public void setRemoved(String removed) {
        isRemoved = removed;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public String getCartItemID() {
        return cartItemID;
    }

    public void setCartItemID(String cartItemID) {
        this.cartItemID = cartItemID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAttachmentID() {
        return attachmentID;
    }

    public void setAttachmentID(String attachmentID) {
        this.attachmentID = attachmentID;
    }

    public String getDocumentLoc() {
        return documentLoc;
    }

    public void setDocumentLoc(String documentLoc) {
        this.documentLoc = documentLoc;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(" documentID : "+documentID);
        sb.append(" cartItemID : "+cartItemID);
        sb.append(" documentTitle : "+documentTitle);
        sb.append(" type : "+type);
        sb.append(" isRemoved : "+isRemoved);
        sb.append(" attachmentID : "+attachmentID);
        sb.append(" documentLoc : "+documentLoc);

        return sb.toString();
    }
}
