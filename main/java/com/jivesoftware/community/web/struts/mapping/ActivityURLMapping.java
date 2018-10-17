/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.web.struts.mapping;

import org.apache.struts2.dispatcher.mapper.ActionMapping;

public class ActivityURLMapping implements URLMapping {
    public void process(String uri, ActionMapping mapping) {
        
		System.out.println("Inside process() in  ActivityURLMapping -------");
		mapping.setName("news");
    }

}
