/* @(#)MailDataExtractor        1.01 Oct 29, 2010
 #
 # Copyright  2008, by Gaian Solutions.
 # Gaian Consulting Services Inc.400 N. Continental Blvd. Suite 330,El Segundo,CA 90245, USA
 # All rights reserved.
 # This software is the confidential and proprietary information ofGaian Consulting Services Inc.
 #("Confidential Information").  You shall not
 # disclose such Confidential Information and shall use it only in
 # accordance with the terms of the license agreement you entered into
 # with Gaian.
 */
package com.gaian.interactiveservices.mails;

import com.gaian.interactiveservices.GNCProperties;
import com.gaian.interactiveservices.db.DBConnection;
import com.gaian.interactiveservices.util.DateUtil;
import com.gaian.ss.gnc.GNCSender;
import com.sun.mail.pop3.POP3Store;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Security;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import org.apache.log4j.Logger;

/**
 * <pre>
 * #This gets the mail informtion of users stored in the MailContext and retrieves the Information and stores that in the mailinfo
 * # If the user specified to send new mails as notificiation send to STB
 *
 * @author Harini
 *
 * Modifications
 * 1) userID is added
 * done by Lovababu Golthi (GSI-155) on 10-12-2013
 * </pre>
 */
public class MailDataExtractor implements Runnable {

    Logger log = null;
    Map<String, Vector<MailInboxBean>> userMailInfo = null;
    List<String> userList = null;
    SimpleDateFormat format;

    public MailDataExtractor(List<String> userList, String name) {
        this.userList = userList;
        log = Logger.getLogger(name);
        format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    }

    @Override
    public void run() {
        processData();
    }

    /**
     * # Retrieve the mails for the given Email IDS: # If the email and pwd
     * credientials are wrong update the status as auth failed into the db
     * tables mailid and mailinfo # Also insert an entry with the above mail id
     * into the mailinfo indicating that auth is failed # # If the credentials
     * are right then retrieve the information of the mail ans store the it in
     * the mailinfo table # If the user sets the settings as to show email
     * notifications send all the retrieved messages (without body) # to STB
     * including total unread mails for the user # This JSON message is to be
     * sent to STB via PIServer/GNC based on the configuration # Also update the
     * status of email "email_alert_status =1 " in the table mailinbox
     */
    private void processData() {
        log.debug("Started the Data Extracter service...");
        Connection con = null;
        try {
            userMailInfo = new HashMap<>();
//            log.debug("Context Mail id information: " + MailContext.getUserMailInfo().keySet().toString());
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

            // Get a Properties object
            Properties props = new Properties();
            log.debug("from the new properies");
            props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.pop3.socketFactory.fallback", "false");
            props.setProperty("mail.pop3.port", "995");
            props.setProperty("mail.pop3.socketFactory.port", "995");
            props.setProperty("mail.store.protocol", "pop3");
            props.setProperty("mail.pop3.connectiontimeout", EmailContext.getEmailSocketConnectionTimeout() + "");
            props.setProperty("mail.pop3.timeout", EmailContext.getEmailSocketIOTimeout() + "");
            props.setProperty("mail.pop3.ssl", "true");
            props.setProperty("mail.debug", EmailContext.isEmailDebug() + "");

            log.debug("The usersList is : " + userList);
            for (String mailId : userList) {
                /*
                 Read the mail and insert the mail data into the Database table ,If the user has the setting as
                 to send the notification it returns the JSON Object of unread emails els it returns null;
                 */
                AuthCredentials authCredits = MailCacheHandler.getInstance().getAuthCredits(mailId);
                Set<String> stbList = MailCacheHandler.getInstance().getSTBSerials(mailId);
                if (authCredits != null && stbList.size() > 0) {
                    log.info("############################# Searching for mails of id:" + mailId + " #######################################");
                    log.debug("# Email Session established ");
                    Vector<MailInboxBean> readMail = readMail(authCredits, props);
                    log.debug("readMail : " + readMail);
                    userMailInfo.put(mailId, readMail);
                    log.debug("# scanning of Inbox folder was finished for id:" + mailId);
                    log.info("###################################################################################################");
                }
            }
            con = new DBConnection().getConnectionObj();
            log.debug("Got db connection for maildataextractor:" + con);
            updateGaianMailInbox(con);
            Integer userID;
            Integer installID;
            UserPreference userPreference;
            for (String mailId : userList) {
                Set<String> stbSerialsSet = new HashSet<>(MailCacheHandler.getInstance().getSTBSerials(mailId));
                for (String STBSerial : stbSerialsSet) {
                    userID = MailCacheHandler.getInstance().getUser(STBSerial);
                    installID = MailCacheHandler.getInstance().getInstallID(userID, mailId);
                    userPreference = MailCacheHandler.getInstance().getUserPreference(userID, installID);
                    if (userPreference == null)
                        continue;
                    String json = getNewMailInfo(userPreference.getSubID(), userID, userPreference.getInstallID(), con, log);
                    log.info("Notification Message of id " + mailId + ":" + json);
                    if (json != null && json.length() > 5) {
                        String domain = mailId.substring(mailId.indexOf("@") + 1, mailId.lastIndexOf("."));
                        json = "({\"msgType\":\"EMAIL\",\"domain\":\"" + domain.toUpperCase() + "\",\"uid\":\"" + mailId.substring(0, mailId.lastIndexOf("@")) + "\",\"totalUnreadCnt\":" + getDomainSpecificUnreadMailCount(con, domain.toUpperCase()) + ",\"unReadCnt\":" + getUnreadMailCount(con, userPreference.getInstallID()) + ", \"sa\":" + userPreference.getAlert() + ",\"msgs\":[ "
                                + json + "],\"user_id\":" + userID + ",\"error\":0})";
                        new GNCSender(GNCProperties.getGNCServerURL(), GNCProperties.getGNCMessageQueueName()).sendMessage(STBSerial, json, GNCProperties.getGNCEmailCategory(), 3);
                    }
                }
            }

        } catch (Exception ex) {
            log.error("Unknown error: " + ex.getLocalizedMessage());
            log.error(ex, ex);
        } finally {
            userMailInfo = null;
            if (con != null) {
                try {
                    log.debug("Closing & Removing the DBConnection from pool.");
                    con.close();
                } catch (SQLException ex) {
                    log.error(ex, ex);
                }
            }
            log.info("End of MailDataExtractor ");
        }
    }

    private void updateGaianMailInbox(Connection con) {
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement("insert into mailbox(mid, sender, subject, body, recddate) "
                    + "values ((select record_id from mail_ids where mail_id=?),?,?,?,?)");
            //mid,sender,subject,body,recddate,sub_id,authfailed
            for (String mailID : userMailInfo.keySet()) {
                Vector<MailInboxBean> beans = userMailInfo.get(mailID);
                if (beans != null && beans.size() > 0) {
                    try {
                        for (MailInboxBean bean : beans) {
                            ps.setString(1, mailID);
                            ps.setString(2, bean.getMsgFrom());
                            ps.setString(3, bean.getSubject());
                            ps.setString(4, bean.getMsgBody());
                            ps.setString(5, bean.getMsgDate());
                            ps.addBatch();
                        }
                        int[] executeBatch = ps.executeBatch();
                        log.debug("Updated the row count is : " + executeBatch.length + " for mailID " + mailID);
                    } catch (Exception e) {
                        log.error(e, e);
                    } finally {
                        ps.clearBatch();
                    }
                }
            }
        } catch (Exception e) {
            log.error(e, e);

        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception ex) {
                log.error("Error while closing connection ");
            }
        }
    }

    private Vector<MailInboxBean> readMail(AuthCredentials credits, Properties props) {
        Vector<MailInboxBean> inboxMails = new Vector<>();
        String to = null;
        try {
            Session session = Session.getDefaultInstance(props);
            log.debug("The session is : " + session.toString());
            log.info("# Getting mails for the account [Uid:" + credits.getUsername() + ", Mid:" + credits.getUsername() + "]");
            POP3Store store = (POP3Store) session.getStore("pop3s");//new POP3Store(session, urln);
            store.connect(MailUtil.getMailServer(credits.getUsername()), credits.getUsername(), credits.getPassword());
            log.debug("# Connected to Mail server store");
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            log.debug("# Inbox folder opened");
            Message[] messages = inbox.getMessages();
            log.debug("# The Unread message count is : " + inbox.getUnreadMessageCount());
            log.debug("# Total Messages count of " + credits.getUsername() + ": " + messages.length);
            int count = 1;
            for (int i = messages.length - 1; i >= 0; i--) {
                try {
                    to = InternetAddress.toString(messages[i].getRecipients(Message.RecipientType.TO));
                } catch (Exception ex) {
                    log.error("# Error while getting to address of mail " + credits.getUsername());
                }
                log.debug("$$ subject : " + messages[i].getSubject() + "sentDate : " + messages[i].getSentDate() + ", pastDate : " + new java.util.Date(DateUtil.getPastDate(EmailContext.getEmailPastDays())));
//                if (messages[i].getSentDate().getTime() - DateUtil.getPastDate(EmailContext.getEmailPastDays()) >= 0) {
                if (true) {
                    log.debug("# Iterating the messages,recid: as it is new mail : " + i);
                    if (count > EmailContext.getEmailsLimit()) {
                        break;
                    }
                    String from = null;
                    from = ((InternetAddress) messages[i].getFrom()[0]).getAddress(); //get email id of sender
                    if (from.equalsIgnoreCase(credits.getUsername()) && to != null && !to.contains(from)) {
                        log.debug("# Found sent mail,so skipping it. From:" + from + " To:" + to + " uid:" + credits.getUsername());
                        continue;
                    }

                    from = ((InternetAddress) messages[i].getFrom()[0]).getPersonal() == null ? from : ((InternetAddress) messages[i].getFrom()[0]).getPersonal(); // gets name of sender
                    String subject = messages[i].getSubject();
                    if (subject != null) {
                        subject = subject.replaceAll("\\+", "");
                    }
                    log.debug("# Subject: " + subject);
                    String msgBody;

                    //-- Get the message part--
                    StringBuilder stringBuilder = new StringBuilder();
                    Part messagePart = messages[i];
                    Object content = messagePart.getContent();
                    //-- get the first body part of the message part--
                    if (content instanceof Multipart) {
                        messagePart = ((Multipart) content).getBodyPart(0);
                    } // -- Get the content type --
                    String contentType = messagePart.getContentType();
                    if (contentType.startsWith("text/plain") || contentType.startsWith("text/html")) {
                        InputStream is = messagePart.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        String thisLine = reader.readLine();
                        while (thisLine != null) {
                            stringBuilder.append(thisLine).append("\n");
                            thisLine = reader.readLine();
                        }
                    }
                    //--Here we can limit the size of the text by using substring--
                    msgBody = stringBuilder.toString();

                    if (msgBody.length() > EmailContext.getEmailMaxMessageLength()) {
                        msgBody = msgBody.substring(0, EmailContext.getEmailMaxMessageLength());
                    }
                    String msgDate = format.format(messages[i].getSentDate());
                    MailInboxBean inboxBean = new MailInboxBean();
                    inboxBean.setMsgFrom(from);
                    inboxBean.setSubject(subject);
                    inboxBean.setMsgBody(msgBody);
                    inboxBean.setMsgDate(msgDate);
                    inboxMails.add(inboxBean);
                    count++;
                    messages[i].setFlag(Flag.SEEN, true);
                }
            }
            inbox.close(true);
            store.close();
        } catch (AuthenticationFailedException ex) {
            log.error(ex, ex);
            log.error("# Error while reading the mail " + credits.getUsername() + " Please verify user credientials");
        } catch (NoSuchProviderException ex) {
            log.error(ex, ex);
            log.error("# Error while reading the mail " + credits.getUsername() + " Please verify the network and user credientials");
        } catch (MessagingException ex) {
            log.error(ex, ex);
            log.error("# Error while reading the mail [MessagingException]  " + credits.getUsername() + ex.getMessage() + " or varify your internet connection");
        } catch (Exception ex) {
            log.error("# unknown error: " + ex.getLocalizedMessage());
            log.error(ex, ex);
        }
        return inboxMails;
    }

    private String getNewMailInfo(int subId, int userID, int installID, Connection con, Logger log) {
        StringBuffer jsonObjet = new StringBuffer(1024);
        String midList = "";
        String value;
        try (
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT mb.record_id AS rcid,sender AS sender,SUBJECT AS sub,DATE_FORMAT(recddate,'%Y-%m-%d %H:%i:%s') AS DATE "
                        + "FROM mailbox mb RIGHT JOIN mail_ids m ON mb.mid=m.record_id "
                        + "RIGHT JOIN mail_ids_to_user mtu ON (m.record_id=mtu.mid AND mtu.record_id=" + installID + ") "
                        + "WHERE m.deleted=0 AND mb.deleted=0 AND mtu.deleted=0 AND mb.viewstatus=0 AND mb.email_alert_status=0");) {
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i
                        <= rsmd.getColumnCount(); i++) {
                    value = rs.getString(i);

                    if (i == 1) {
                        jsonObjet.append("{");
                        midList += value + ",";
                    }
                    if (i == rsmd.getColumnCount()) {
                        jsonObjet.append("\"").append(rsmd.getColumnLabel(i)).append("\":\"").append(value.trim()).append("\"},");

                    } else {
                        jsonObjet.append("\"").append(rsmd.getColumnLabel(i)).append("\":\"").append(value.trim()).append("\",");

                    }
                }

            }
            if (jsonObjet.length() > 1) {
                jsonObjet.deleteCharAt(jsonObjet.length() - 1);
            }

            if (!midList.equals("") && midList.length() > 1) {
                String qry = "update mailbox set email_alert_status = 1 where record_id in(" + midList.substring(0, midList.length() - 1) + ")";
                log.debug("Executing the status update query: " + qry);
                stmt.executeUpdate(qry);
                log.debug("Updated the email alert status for " + midList);
            } else {
                jsonObjet = null;
                log.debug("No mails were found to notify for the sub_id: " + subId + " mialid:" + installID);
            }
        } catch (Exception ex) {
            log.error("Error while reading the new mail " + subId + " ");
            log.error(ex, ex);
        }
        return (jsonObjet != null) ? jsonObjet.toString() : null;
    }

    private int getDomainSpecificUnreadMailCount(Connection con, String domain) {
        int count = 0;
        try (
                Statement ps = con.createStatement();
                ResultSet rs = ps.executeQuery("select count(record_id) from mailbox where deleted=0 and viewstatus=0 and mid in ( select record_id from mail_ids where upper((SUBSTRING_INDEX(SUBSTRING_INDEX(mail_id,'@',-1),'.',1)))='" + domain + "')");) {
            if (rs.next())
                count = rs.getInt(1);
        } catch (Exception e) {
            log.error(e, e);
        }
        return count;
    }

    private int getUnreadMailCount(Connection con, int mid) {
        int count = 0;
        try (
                Statement ps = con.createStatement();
                ResultSet rs = ps.executeQuery("select count(record_id) from mailbox where deleted = 0 and viewstatus=0 and  mid=" + mid + "");) {
            if (rs.next())
                count = rs.getInt(1);
        } catch (Exception e) {
            log.error(e, e);
        }
        return count;
    }
}
