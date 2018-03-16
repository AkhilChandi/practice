/*
 * GaianMail 3.0 10 Dec, 2013
 * Copyright 2008, by Gaian Solutions. Gaian Consulting Services Inc.400 N.
 * Continental Blvd. Suite 330,El Segundo,CA 90245, USA All rights reserved.
 * This software is the confidential and proprietary information ofGaian
 * Consulting Services Inc. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Gaian.
 */
package com.gaian.interactiveservices.mails;

import java.util.HashMap;

/**
 *
 * @author Lovababu Golthi (GSI-155)
 *
 * Implemented On: 10 Dec, 2013
 */
public class EmailContext {

    private static int EmailServiceIterationInterval;
    private static String UserActivityJMSDestination;
    private static int EmailPastDays;
    private static int EmailMaxMessageLength;
    private static int EmailSocketConnectionTimeout;
    private static int EmailSocketIOTimeout;
    private static int EmailsLimit;
    private static boolean EmailDebug;
    private static int EmailAccountsPerThread;
    private static int EmailsThreadCorePoolSize;
    private static int EmailsThreadMaximumPoolSize;
    private static int EmailsThreadKeepAliveTimeInSec;
    private static int EmailsThreadBlockingQueueSize;
    private static String InactiveTopicName;
    private static HashMap<String, String> gaianSupportedMailServers;

    public static HashMap<String, String> getGaianSupportedMailServers() {
        return gaianSupportedMailServers;
    }

    public static void setGaianSupportedMailServers(HashMap<String, String> gaianSupportedMailServers) {
        EmailContext.gaianSupportedMailServers = gaianSupportedMailServers;
    }

    public static int getEmailServiceIterationInterval() {
        return EmailServiceIterationInterval;
    }

    public static void setEmailServiceIterationInterval(int EmailServiceIterationInterval) {
        EmailContext.EmailServiceIterationInterval = EmailServiceIterationInterval;
    }

    public static String getUserActivityJMSDestination() {
        return UserActivityJMSDestination;
    }

    public static void setUserActivityJMSDestination(String UserActivityJMSDestination) {
        EmailContext.UserActivityJMSDestination = UserActivityJMSDestination;
    }

    public static int getEmailPastDays() {
        return EmailPastDays;
    }

    public static void setEmailPastDays(int EmailPastDays) {
        EmailContext.EmailPastDays = EmailPastDays;
    }

    public static int getEmailMaxMessageLength() {
        return EmailMaxMessageLength;
    }

    public static void setEmailMaxMessageLength(int EmailMaxMessageLength) {
        EmailContext.EmailMaxMessageLength = EmailMaxMessageLength;
    }

    public static int getEmailSocketConnectionTimeout() {
        return EmailSocketConnectionTimeout;
    }

    public static void setEmailSocketConnectionTimeout(int EmailSocketConnectionTimeout) {
        EmailContext.EmailSocketConnectionTimeout = EmailSocketConnectionTimeout;
    }

    public static int getEmailSocketIOTimeout() {
        return EmailSocketIOTimeout;
    }

    public static void setEmailSocketIOTimeout(int EmailSocketIOTimeout) {
        EmailContext.EmailSocketIOTimeout = EmailSocketIOTimeout;
    }

    public static int getEmailsLimit() {
        return EmailsLimit;
    }

    public static void setEmailsLimit(int EmailsLimit) {
        EmailContext.EmailsLimit = EmailsLimit;
    }

    public static boolean isEmailDebug() {
        return EmailDebug;
    }

    public static void setEmailDebug(boolean EmailDebug) {
        EmailContext.EmailDebug = EmailDebug;
    }

    public static int getEmailAccountsPerThread() {
        return EmailAccountsPerThread;
    }

    public static void setEmailAccountsPerThread(int EmailAccountsPerThread) {
        EmailContext.EmailAccountsPerThread = EmailAccountsPerThread;
    }

    public static int getEmailsThreadCorePoolSize() {
        return EmailsThreadCorePoolSize;
    }

    public static void setEmailsThreadCorePoolSize(int EmailsThreadCorePoolSize) {
        EmailContext.EmailsThreadCorePoolSize = EmailsThreadCorePoolSize;
    }

    public static int getEmailsThreadMaximumPoolSize() {
        return EmailsThreadMaximumPoolSize;
    }

    public static void setEmailsThreadMaximumPoolSize(int EmailsThreadMaximumPoolSize) {
        EmailContext.EmailsThreadMaximumPoolSize = EmailsThreadMaximumPoolSize;
    }

    public static int getEmailsThreadKeepAliveTimeInSec() {
        return EmailsThreadKeepAliveTimeInSec;
    }

    public static void setEmailsThreadKeepAliveTimeInSec(int EmailsThreadKeepAliveTimeInSec) {
        EmailContext.EmailsThreadKeepAliveTimeInSec = EmailsThreadKeepAliveTimeInSec;
    }

    public static int getEmailsThreadBlockingQueueSize() {
        return EmailsThreadBlockingQueueSize;
    }

    public static void setEmailsThreadBlockingQueueSize(int EmailsThreadBlockingQueueSize) {
        EmailContext.EmailsThreadBlockingQueueSize = EmailsThreadBlockingQueueSize;
    }

    public static String getInactiveTopicName() {
        return InactiveTopicName;
    }

    public static void setInactiveTopicName(String InactiveTopicName) {
        EmailContext.InactiveTopicName = InactiveTopicName;
    }

}
