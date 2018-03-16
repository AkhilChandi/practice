/**
 * @(#)MailUtil 1.01 Jun 21, 2011
 *
 * Copyright 2008, by Gaian Solutions. Gaian Consulting Services Inc.400 N.
 * Continental Blvd. Suite 330,El Segundo,CA 90245, USA All rights reserved.
 * This software is the confidential and proprietary information ofGaian
 * Consulting Services Inc. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Gaian.
 */
package com.gaian.interactiveservices.mails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;

/**
 *
 * @author SaiSatish
 */
public class MailUtil {

    private static Logger log;
    public static Hashtable<String, Integer> domainSpeceficUnreadMailCount;

    static {
        log = Logger.getLogger(MailUtil.class);
    }

    public static String getMailServer(String mailId) {
        String mailServer;
        String domain;
        log.debug("Finding the mailserver address for userId: " + mailId);
        if (EmailContext.getGaianSupportedMailServers() == null) {
            log.fatal("Mail server Information was set");
            return null;
        }

        if (mailId == null) {
            log.error("Mailid was empty or incorrect");
            return null;
        }
        log.debug("The mail Id is : " + mailId);
        domain = mailId.substring(mailId.lastIndexOf("@") + 1, mailId.lastIndexOf(".co"));
        log.debug("Domin: " + domain);
        if ((mailServer = EmailContext.getGaianSupportedMailServers().get(domain)) == null) {
            log.error("Unable to identify the mail server,please enter service supported mailId");
        }
        log.debug("the mail Server is : " + mailServer);
        return mailServer;
    }

    public static void readMailServerInfo() {
        String key;
        HashMap<String, String> mailServerInfo = new HashMap<>();
        domainSpeceficUnreadMailCount = new Hashtable<>();
        domainSpeceficUnreadMailCount.put("totalCount", 0);
        try {
            ResourceBundle rb = ResourceBundle.getBundle("MailServerInfo");
            Enumeration<String> mailDomainList = rb.getKeys();
            while (mailDomainList.hasMoreElements()) {
                key = mailDomainList.nextElement();
                mailServerInfo.put(key, rb.getString(key));
                log.info("Added mail domain " + key + " to mail service");
            }
        } catch (Exception e) {
            log.error(e, e);
            mailServerInfo = null;
        }
        EmailContext.setGaianSupportedMailServers(mailServerInfo);
    }

    public static long getPastDate(int noofpastdays) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, -noofpastdays);

        return cal.getTime().getTime();
    }

//    public static void updateDomainSpecificUnreadMailInfo(int sub_id, int userID, String userid) {
//        Hashtable<String, Integer> mailunreadCount = MailCacheHandler.getInstance().getUnreadMailCount(sub_id, userID);
//        if (mailunreadCount == null) {
//            mailunreadCount = new Hashtable<String, Integer>();
//        }
//        int count = (mailunreadCount.get(userid) == null ? 0 : mailunreadCount.get(userid)) + 1;
//        mailunreadCount.put(userid, count);
//        count = (mailunreadCount.get("totalCount") == null ? 0 : mailunreadCount.get("totalCount")) + 1;
//        mailunreadCount.put("totalCount", count);
//        log.debug("mail unread count for subscriber " + sub_id + " " + mailunreadCount);
//        MailCacheHandler.getInstance().putUnreadMailCount(sub_id, userID, mailunreadCount);
//
//    }
    public static int getDomainSpecificUnreadCount(int sub_id, String loginID, int userID) {
//        Hashtable<String, Integer> unreadMails = MailCacheHandler.getInstance().getUnreadMailCount(sub_id, userID);
//        if (unreadMails != null || !unreadMails.isEmpty()) {
//            return unreadMails.get(loginID);
//        }
        return 0;
    }

    public static int getTotalUnreadCount(int sub_id, int userID) {
//        Hashtable<String, Integer> unreadMails = MailCacheHandler.getInstance().getUnreadMailCount(sub_id, userID);
//        if (unreadMails == null || unreadMails.isEmpty()) {
//            return 0;
//        }
//        return unreadMails.get("totalCount");
        return 0;
    }

    public static void getAllMailIds(Connection con) {
        try {
            String getAllUserMailIDS = "select mail_ids.record_id,stb_serial,stb_ip_address,mail_ids.sub_id,mail_id,mail_pwd,show_email_notification from mail_ids,"
                    + "stb_to_subscriber,stb where mail_ids.sub_id=stb_to_subscriber.sub_id and stb.record_id=stb_to_subscriber.stb_id"
                    + " and mail_ids.deleted=0 and stb_to_subscriber.deleted=0 and stb.deleted=0 and authfailed=0 "
                    + " order by stb_serial";
            PreparedStatement stmt = null;
            ResultSet rs = null;
            log.debug("Executing the query for updated mails: " + getAllUserMailIDS);
            stmt = con.prepareStatement(getAllUserMailIDS);
            rs = stmt.executeQuery();
            while (rs.next()) {
            }
        } catch (SQLException ex) {
            log.error(ex, ex);
        }
    }
}
