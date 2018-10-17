/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.community.mail.outgoing;

import com.google.common.collect.ImmutableSet;
import com.jivesoftware.base.event.JivePropertyEvent;
import com.jivesoftware.base.event.v2.EventDispatcher;
import com.jivesoftware.base.event.v2.EventListener;
import com.jivesoftware.base.event.v2.EventListenerRegistry;
import com.jivesoftware.base.event.v2.EventSink;
import com.jivesoftware.community.JiveConstants;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.JiveManager;
import com.jivesoftware.community.cloudalytics.util.AnalyticsActivityPropertyResolver;
import com.jivesoftware.community.lifecycle.ApplicationState;
import com.jivesoftware.community.lifecycle.ApplicationStateChangeEvent;
import com.jivesoftware.community.lifecycle.JiveApplication;
import com.jivesoftware.community.mail.EmailManager;
import com.jivesoftware.community.mail.EmailMessage;
import com.jivesoftware.community.mail.event.EmailEvent;
import com.jivesoftware.community.mail.jmx.EmailManagerMBean;
import com.jivesoftware.community.mail.util.EmailValidationHelper;
import com.jivesoftware.community.util.SkinUtils;
import com.jivesoftware.community.util.concurrent.BlockingDeque;
import com.jivesoftware.community.util.concurrent.LinkedBlockingDeque;
import com.jivesoftware.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import com.grail.synchro.SynchroGlobal;
import com.grail.util.BATConstants;
import com.grail.util.BATGlobal;


import com.jivesoftware.community.theme.Theme;
import com.jivesoftware.community.theme.ThemeManager;

/**
 * Provides ability to send email messages.
 *
 * @see com.jivesoftware.community.mail.EmailMessage
 */
@ManagedResource(
        objectName = "com.jivesoftware.community.mail.outgoing:type=EmailManager",
        description = "Jive email manager information"
)
public class EmailManagerImpl implements JiveManager, EmailManager, EmailManagerMBean, EventSink {

    protected static final Logger log = LogManager.getLogger(EmailManagerImpl.class);
    protected static final int MAX_MAILS_PER_SESSION = 25;
    protected static final int QUEUE_TRIGGER_LIMIT = 100;
    protected static final int NUMBER_CONNECTIONS = 5;
    protected static final int QUEUE_WARNING_SIZE = 5000;
    protected static final int QUEUE_SIZE_WARNING_FREQUENCY = 60; // 1 minute
    protected static final int MESSAGING_EXCEPTION_FREQUENCY = 600; // 10 minutes
    protected static final int CONSECUTIVE_FAILURE_THRESHOLD = 5;
    protected static final int FAILURE_THRESHOLD_SLEEP_SECONDS = 30;

    protected static boolean initialized = false;
    public static final String MAIL_SMTP_WORKER_SLEEP_PERIOD = "mail.smtp.workerSleepPeriod";

    protected boolean debug = JiveGlobals.getJiveBooleanProperty("mail.debug");

    protected ArrayList<EmailHandlerThread> handlers;

    private AtomicLong messagesQueued = new AtomicLong();
    private AtomicLong messagesSent = new AtomicLong();

    private AtomicLong lastSizeWarningMessage = new AtomicLong(0);
    private AtomicLong lastExceptionMessage = new AtomicLong(0);

    private AtomicBoolean failing = new AtomicBoolean(false);
    private AtomicInteger consecutiveFailureCount = new AtomicInteger(0);
    private AtomicLong lastFailureTimestamp = new AtomicLong(0);
    private int consecutiveFailureThreshold = 5;
    private long failureThresholdSleepSeconds = 60;

    private AnalyticsActivityPropertyResolver analyticsActivityPropertyResolver;
    private EventDispatcher dispatcher;
    private final ImmutableSet<String> authorizedActivities = new ImmutableSet.Builder<String>() //InboxEntry
        //put("jive.eae.actionqueue.item.externalActivityEntry", null); //ExternalActivityEntry
        .add("actionqueue.item.new.subject") // NewUserRegistrationApprovalEntry - UserRelationshipApprovalEntry - InvitationUserRelationshipSuggestionEntry
        .add("action.outcome.pending.subject") // OutcomeNotificationEntry
        .add("notification.mention.subject") //Mention
        .add("notification.direct.message.subject") //DM
        .add("notification.send.subject") // Share
        .add("collaborator.notification.email.subject") //Document
        .add("watches.email.document.subject") //Document creation
        .add("watches.email.document_comment.subject") //Comment on a document
        .add("watches.email.thread.subject") //Thread
        .add("watches.email.blog.subject") //Blog
        .add("blogs.comment.created.subject") // Comment on a Blog
        .build();
    /**
     * This task should never reach a queue of size 10000 so it is probably a safe number
     */
    private BlockingQueue<EmailMessage> emailQueue = new LinkedBlockingQueue<EmailMessage>();

    private EmailValidationHelper emailValidationHelper;
    public static final String EMAIL_PREVIEW_KEY_DELIM = "---___---";

    public void setEmailValidationHelper(EmailValidationHelper emailValidationHelper) {
        this.emailValidationHelper = emailValidationHelper;
    }

    /**
     * Used to send an email message. This will add the email
     * message to an outbound email message queue that will be
     * processed by the first available worker.
     *
     * @param message message to be sent
     */
    public void send(EmailMessage message) {
        send(message, null);
    }

    protected void send(EmailMessage message, MessagingException messagingException) {
        try {

            // Do not send messages that are from forum threads that were created by outlook
            // This does not skip subsequent messages to the thread
            if (emailValidationHelper.shouldSkip(message)) {
                return;
            }

            if (messagingException == null && emailValidationHelper.isDomainRestrictionEnabled()) {
                // check recipients to ensure they're in the allowed domain
                for (EmailMessage.EmailAddress email : message.getRecipients()) {
                    if (!(emailValidationHelper.isApprovedDomain(email.getEmail()) ||
                            email.getEmail().endsWith("jivesoftware.com")))
                    {
                        log.warn("Sending message to non-strict email domain: " + email.getEmail() +
                                " is not in " + SkinUtils.getAllCompanyDomains());
                    }
                }
            }
            
            if(!message.getContext().containsKey("portalType")) {
                if(SynchroGlobal.getAppProperties().get(BATConstants.GRAIL_PORTAL_TYPE) != null) {
                    message.getContext().put("portalType", SynchroGlobal.getAppProperties().get(BATConstants.GRAIL_PORTAL_TYPE));
                } else {
                    message.getContext().put("portalType", "");
                }
            }

            String basePath = null;
            if((!message.getContext().containsKey("basePath")) || message.getContext().get("basePath") == null) {
                basePath = SynchroGlobal.getAppProperties().get(BATConstants.BAT_BASE_URL) != null ? SynchroGlobal.getAppProperties().get(BATConstants.BAT_BASE_URL).toString(): "";
                if(basePath == null || basePath.equals("")) {
                    String jiveUrl = JiveGlobals.getJiveProperty("jiveURL");
                    if(jiveUrl != null && !jiveUrl.equals("")) {
                        message.getContext().put("basePath", jiveUrl);
                        basePath = jiveUrl;
                    } else {
                        message.getContext().put("basePath", "");
                        basePath = "";
                    }

                } else {
                    message.getContext().put("basePath", basePath);
                }
            } else {
               basePath = message.getContext().get("basePath").toString();
            }

            if(basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.length()-1);
            }

            Theme theme = getThemeManager().getGlobalTheme();
            message.getContext().put("themeAbsPath", (basePath == null?"":basePath) + "/themes/" + theme.getName());

            
            getEmailQueue().put(message);
            messagesQueued.addAndGet(1);

            if (messagingException == null && analyticsActivityPropertyResolver.isAnalyticsActivityListenerEnabled()) {
                String subjectProperty  = message.getSubjectProperty();
                if (StringUtils.isEmpty(subjectProperty)) {
                    try {
                        message = message.createIfFactory();
                    } catch (Exception e) {
                        log.error("Unable to retrieve the message context");
                    }
                }

                if (authorizedActivities.contains(message.getSubjectProperty())) {
                    //dispatch EmailEvent
                    EmailEvent emailEvent = new EmailEvent(EmailEvent.Type.SENDING, null, message,
                                                           new HashMap<>());
                    try {
                        dispatcher.fireInline(emailEvent);
                    } catch (Exception e) {
                        log.error("Unable to retrieve the message context");
                    }
                }
            }
        }
        catch (InterruptedException e) {
            log.warn("Attempt to add message " + message + " to  queue failed", e);
        }
    }

    public EmailMessage.EmailAddress getAdminEmail() {
        String email = JiveGlobals.getJiveProperty(EmailManager.ADMINEMAIL, "admin@localhost");
        String name = JiveGlobals.getJiveProperty(EmailManager.ADMINNAME, "Jive Administrator");
        return new EmailMessage.EmailAddress(name, email);
    }

    public static String readTemplateKeyFromProperty(String templateProperty, String replacement) {
        String templateKey = null;

        if (templateProperty != null) {
            if (templateProperty.startsWith(TEST_EMAIL_SUBJECT_PREFIX)) {
                String[] parts = templateProperty.split(EMAIL_PREVIEW_KEY_DELIM);

                if (parts.length == 2) {
                    templateKey = parts[1];
                }
            }

            if (templateKey == null) {
                templateKey = templateProperty.replaceAll(replacement, "");
            }
        }

        return templateKey;
    }

    public void destroy() {
        log.info("Destroying email manager");
        if (handlers != null) {
            for (EmailHandlerThread handler : handlers) {
                handler.stopHandler();
            }
        }
    }

    @Override
    public void setRegistry(EventListenerRegistry registry) {
        registry.register(new EventListener<ApplicationStateChangeEvent>() {
            @Override
            public void handle(ApplicationStateChangeEvent e) {
                handleApplicationStateChangeEvent(e);
            }
        });

        registry.register(new EventListener<JivePropertyEvent>() {
            @Override
            public void handle(JivePropertyEvent e) {
                if (e.getName().startsWith("mail.smtp.")) {
                    reportEmailSenderSuccess();
                }
            }
        });
    }

    protected void handleApplicationStateChangeEvent(ApplicationStateChangeEvent e) {
        if (e.getNewState().equals(ApplicationState.RUNNING)) {
            initLazily();
        }
    }

    private synchronized void initLazily() {
        if (!initialized) {
            log.debug("Initializing email manager");

            int numberConnections = JiveGlobals.getJiveIntProperty("mail.smtp.maxConnections", NUMBER_CONNECTIONS);

            handlers = new ArrayList<EmailHandlerThread>(numberConnections);

            for (int i = 0; i < numberConnections; i++) {
                EmailHandlerThread handler = new EmailHandlerThread(i);
                handlers.add(handler);
                handler.start();
            }

            consecutiveFailureThreshold = JiveGlobals.getJiveIntProperty(
                    "mail.smtp.consecutiveFailureThreshold", CONSECUTIVE_FAILURE_THRESHOLD);
            failureThresholdSleepSeconds = JiveGlobals.getJiveIntProperty(
                    "mail.smtp.failureThresholdSleepSeconds", FAILURE_THRESHOLD_SLEEP_SECONDS);

            initialized = true;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    @ManagedOperation(description = "Returns the total number of messages added to the email queue (includes retries).")
    public long getTotalMessagesQueued() {
        return messagesQueued.get();
    }

    @Override
    @ManagedOperation(description = "Returns the total number of messages sent from the email queue.")
    public long getTotalMessagesSent() {
        return messagesSent.get();
    }

    @Override
    @ManagedOperation(description = "Returns the number of messages currently in the email queue.")
    public int getQueueDepth() {
        return getEmailQueue().size();
    }

    protected BlockingQueue<EmailMessage> getEmailQueue() {
        return emailQueue;
    }

    public void setEmailQueue(BlockingQueue<EmailMessage> emailQueue) {
        this.emailQueue = emailQueue;
    }

    protected InternetAddress[] getRecipients(EmailMessage message) {
        ArrayList<InternetAddress> list = new ArrayList<InternetAddress>();

        for (Object o : message.getRecipients()) {
            EmailMessage.EmailAddress address = (EmailMessage.EmailAddress) o;
            String name = org.apache.commons.lang.StringUtils.isEmpty(address.getName()) ? "" : address.getName();
            try {
                list.add(new InternetAddress(address.getEmail(), name));
            }
            catch (UnsupportedEncodingException e) {
                log.warn("trouble adding email address to email " + address, e);
            }
        }

        return list.toArray(new InternetAddress[list.size()]);
    }

    /**
     * Resets any consecutive failure stats.
     */
    private void reportEmailSenderSuccess() {
        consecutiveFailureCount.set(0);
        lastFailureTimestamp.set(0);
    }

    /**
     * Adds to consecutive failure stats.
     */
    private void reportEmailSenderFailure() {
        consecutiveFailureCount.incrementAndGet();
        lastFailureTimestamp.set(System.currentTimeMillis());
    }

    /**
     * Determines whether email senders are banned by inspecting consecutive failure stats.
     *
     * @return whether email senders are banned
     */
    private boolean emailSendersAreBanned() {
        if (consecutiveFailureCount.get() < consecutiveFailureThreshold) {
            return false;
        }

        long secondsElapsed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastFailureTimestamp.get());
        return secondsElapsed < failureThresholdSleepSeconds;
    }

    /**
     * Log at WARN if the queue size is greater than the configured threshold, but no more than once per
     * configured period.
     */
    private void warnOfSizeConditionally() {
        int queueWarningSize = JiveGlobals.getJiveIntProperty("mail.smtp.queueWarningSize", QUEUE_WARNING_SIZE);
        if (emailQueue.size() > queueWarningSize) {
            int queueSizeWarningFrequency = JiveGlobals
                    .getJiveIntProperty("mail.smtp.queueSizeWarningFrequency", QUEUE_SIZE_WARNING_FREQUENCY);
            long nextWarningTime =
                    lastSizeWarningMessage.get() + (queueSizeWarningFrequency * JiveConstants.SECOND);
            if (System.currentTimeMillis() > nextWarningTime) {
                log.warn("Email queue contains " + emailQueue.size() + " messages");
                lastSizeWarningMessage.set(System.currentTimeMillis());
            }
        }
    }

    /**
     * Log at ERROR if the most recent attempt to send emails threw a messaging exception, but only if the
     * sender has just started failing or the configured period has elapsed.
     *
     * @param messagingException the most recent messaging exception, or null if not failing
     */
    private void errorIfFailingConditionally(MessagingException messagingException) {
        if (messagingException != null) {
            if (!failing.getAndSet(true)) {
                log.error("Email senders are failing", messagingException);
                lastExceptionMessage.set(System.currentTimeMillis());
            }
            else {
                int messagingExceptionFrequency = JiveGlobals
                        .getJiveIntProperty("mail.smtp.messagingExceptionFrequency", MESSAGING_EXCEPTION_FREQUENCY);
                long nextExceptionTime =
                        lastExceptionMessage.get() + (messagingExceptionFrequency * JiveConstants.SECOND);
                if (System.currentTimeMillis() > nextExceptionTime) {
                    log.error("Email senders are still failing", messagingException);
                    lastExceptionMessage.set(System.currentTimeMillis());
                }
            }
        }
        else if (failing.getAndSet(false)) {
            // intentionally logged at error so that this message will always accompany the "failing" messages
            log.error("Email senders have recovered");
        }
    }

    private int maxEmailsPerSession() {
        return JiveGlobals
                .getJiveIntProperty("mail.smtp.maxMailsPerSession", EmailManagerImpl.MAX_MAILS_PER_SESSION);
    }

    private boolean hasTouchedHighWaterMark() {
        return emailQueue != null && !emailQueue.isEmpty() &&
                emailQueue.size() >= JiveGlobals
                        .getJiveIntProperty("mail.smtp.queueTriggerLimit", QUEUE_TRIGGER_LIMIT);
    }

    public void setDispatcher(EventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void setAnalyticsActivityPropertyResolver(
            AnalyticsActivityPropertyResolver analyticsActivityPropertyResolver)
    {
        this.analyticsActivityPropertyResolver = analyticsActivityPropertyResolver;
    }

    public class EmailHandlerThread extends Thread{
        private static final int DEFAULT_MAX_SLEEP_TIME = 10;
        private static final int DEFAULT_MIN_SLEEP_TIME = 2;

        private int id;
        private AtomicBoolean running = new AtomicBoolean(true);
        private AtomicLong lastRun = new AtomicLong(0);

        public EmailHandlerThread(int id) {
            this.id = id;
        }

        public void run() {
            log.info("#EmailSender:" + id + " running");

            while (running.get()) {
                if (shouldRun()) {
                    warnOfSizeConditionally();

                    EmailSender sender = getEmailHandler();
                    while (!emailQueue.isEmpty()) {
                        BlockingDeque<EmailMessage> messages = new LinkedBlockingDeque<EmailMessage>();
                        emailQueue.drainTo(messages, maxEmailsPerSession());
                        sender.setMessages(messages);
                        sender.run();
                        MessagingException messagingException = sender.getMessagingException().get();
                        if (messagingException != null) {
                            errorIfFailingConditionally(messagingException);
                            for (EmailMessage emailMessage : sender.getMessages()) {
                                send(emailMessage, messagingException);
                            }
                            sender.getMessagingException().set(null);

                            reportEmailSenderFailure();
                            if (emailSendersAreBanned()) {
                                break;
                            }
                        }
                        else if (!sender.getMessages().isEmpty()) {
                            errorIfFailingConditionally(null);
                            messagesSent.addAndGet(sender.getMessages().size());
                            reportEmailSenderSuccess();
                        }
                    }

                    lastRun.set(System.currentTimeMillis());

                }

                // Nap for a while
                try {
                    Thread.sleep(getSleepTime());
                }
                catch (InterruptedException e) {
                    // Ignore
                }
            }
        }

        private long getSleepTime() {
            long time = System.currentTimeMillis() - lastRun.get();
            long maxTime = JiveGlobals
                    .getJiveIntProperty("mail.smtp.workerSleepPeriod", DEFAULT_MAX_SLEEP_TIME) * JiveConstants.SECOND;
            if (hasTouchedHighWaterMark()) {
                return DEFAULT_MIN_SLEEP_TIME * JiveConstants.SECOND;
            }
            return (time > 0 && time < maxTime) ? time : maxTime;
        }

        private EmailSender getEmailHandler() {
            return JiveApplication.getContext().getSpringBean("emailSender");
        }

        public synchronized void stopHandler() {
            this.running.set(false);
            this.interrupt();
        }

        protected boolean shouldRun() {
            if (!JiveApplication.isInitialized() || emailSendersAreBanned()) {
                return false;
            }

            // If the queue has reached 100 emails or more we should run now.
            if (hasTouchedHighWaterMark()) {
                return true;
            }

            // If the last time we ran was more than 10 seconds ago, we should run.
            return System.currentTimeMillis() >= lastRun.get() + (JiveGlobals.getJiveIntProperty(
                    MAIL_SMTP_WORKER_SLEEP_PERIOD,
                    DEFAULT_MAX_SLEEP_TIME) * JiveConstants.SECOND);
        }

        public long getLastRun() {
            return lastRun.get();
        }
    }
        private static ThemeManager themeManager;

    public static ThemeManager getThemeManager() {
        if(themeManager == null) {
            return JiveApplication.getContext().getSpringBean("themeManager");
        }
        return themeManager;
    }
}
