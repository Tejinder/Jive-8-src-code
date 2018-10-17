package com.grail.synchro.events;

import com.grail.synchro.util.SynchroReminderUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.EventListener;

/**
 * Created with IntelliJ IDEA.
 * User: Bhakar
 * Date: 3/4/15
 * Time: 12:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class SynchroReminderEventListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            SynchroReminderUtils.processReminders();
        } catch (Exception e) {

        }
    }
}
