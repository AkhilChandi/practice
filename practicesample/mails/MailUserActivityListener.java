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

import com.gaian.iptv.jms.AppMessageProperties;
import com.gaian.iptv.jms.AuthCredentials;
import com.gaian.iptv.jms.MessageCategory;
import com.gaian.iptv.jms.UserActivityCallbackAdapter;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.jms.Message;
import org.apache.log4j.Logger;

/**
 *
 * @author Lovababu Golthi (GSI-155)
 *
 * Implemented On: 10 Dec, 2013
 */
public class MailUserActivityListener extends UserActivityCallbackAdapter {

    private Logger log = Logger.getLogger(getClass());
    public static final byte CHOICE_YES = 1;
    public static final byte CHOICE_NO = 0;

    @Override
    public void onLogon(AppMessageProperties properties, Message message) {
        log.debug("onLogon");
        try (EmailCredentialsDAO dao = new EmailCredentialsDAO()) {
            Map<com.gaian.interactiveservices.mails.AuthCredentials, UserPreference> creditAndPreferences
                    = dao.getCreditAndPreferences(properties.getSTBSerial(), properties.getUserID());
            log.debug("creditAndPreferences : " + creditAndPreferences);
            if (creditAndPreferences != null && !creditAndPreferences.isEmpty()) {
                for (com.gaian.interactiveservices.mails.AuthCredentials credit : creditAndPreferences.keySet()) {
                    UserPreference pref = creditAndPreferences.get(credit);
                    properties.setInstallID(pref.getInstallID())
                            .setShowIMOnTV(pref.getAlert() == CHOICE_YES);
                    insert(properties, AuthCredentials.decode(credit.getUsername() + ';' + credit.getPassword())[0]);
                }
            }
        } catch (SQLException sqe) {
            log.error("Database error : " + sqe.getMessage(), sqe);
        } catch (Exception ex) {
            log.error("Unknown error : " + ex.getMessage(), ex);
        }
    }

    @Override
    public void onLogout(AppMessageProperties properties, Message message) {
        log.debug("onLogout");
        MailCacheHandler cacheHandler = MailCacheHandler.getInstance();
        Set<String> emailIDs = new HashSet<>(cacheHandler.getEmailIDs(properties.getUserID()));
        for (String emailID : emailIDs) {
            cacheHandler.clear(properties.getUserID(), cacheHandler.getInstallID(properties.getUserID(), emailID));
        }
    }

    @Override
    public void onInsert(AppMessageProperties properties, Message message) {
        if (properties.getCategory() == MessageCategory.EMAIL) {
            log.debug("onInsert");
            AuthCredentials[] authCredentials = properties.getAuthCredentials();
            if (authCredentials != null && authCredentials.length > 0)
                insert(properties, authCredentials[0]);
        }
    }

    protected void insert(AppMessageProperties properties, AuthCredentials credit) {
        MailCacheHandler cacheHandler = MailCacheHandler.getInstance();
        cacheHandler.setAuthCredits(new com.gaian.interactiveservices.mails.AuthCredentials(credit.getUsername(), credit.getPassword()));
        cacheHandler.setUserPreference(new UserPreference()
                .setAlert(properties.isShowIMOnTV() ? CHOICE_YES : CHOICE_NO)
                .setEmailID(credit.getUsername())
                .setInstallID(properties.getInstallID())
                .setSTBSerial(properties.getSTBSerial())
                .setSubID(properties.getSubscriberID())
                .setUserID(properties.getUserID()));
        cacheHandler.addSTBSerial(credit.getUsername(), properties.getSTBSerial());
        cacheHandler.setInstallID(properties.getUserID(), credit.getUsername(), properties.getInstallID());
        cacheHandler.addInstallID(credit.getUsername(), properties.getInstallID());
        Set<String> activeEmailIDs = cacheHandler.getActiveEmailIDs();
        synchronized (activeEmailIDs) {
            activeEmailIDs.add(credit.getUsername());
        }
        cacheHandler.addEmailID(properties.getUserID(), credit.getUsername());
        cacheHandler.setUser(properties.getSTBSerial(), properties.getUserID());
    }

    @Override
    public void onUpdate(AppMessageProperties properties, Message message) {
        if (properties.getCategory() == MessageCategory.EMAIL) {
            log.debug("onUpdate");
            MailCacheHandler cacheHandler = MailCacheHandler.getInstance();
            UserPreference pref = cacheHandler.getUserPreference(properties.getUserID(), properties.getInstallID());
            AuthCredentials[] authCredentials = properties.getAuthCredentials();
            if (authCredentials != null) {
                AuthCredentials credit = authCredentials[0];
                // emailID might be changed
                if (!credit.getUsername().equals(pref.getEmailID())) {
                    // clear old pref
                    cacheHandler.clear(properties.getUserID(), properties.getInstallID());
                    // add new pref
                    insert(properties, credit);
                } else {
                    // update existing pref
                    cacheHandler.setAuthCredits(new com.gaian.interactiveservices.mails.AuthCredentials(credit.getUsername(), credit.getPassword()));
                    cacheHandler.setUserPreference(new UserPreference()
                            .setAlert(properties.isShowIMOnTV() ? CHOICE_YES : CHOICE_NO)
                            .setEmailID(credit.getUsername())
                            .setInstallID(properties.getInstallID())
                            .setSTBSerial(properties.getSTBSerial())
                            .setSubID(properties.getSubscriberID())
                            .setUserID(properties.getUserID()));
                }
            }
        }
    }

    @Override
    public void onDelete(AppMessageProperties properties, Message message) {
        switch (properties.getCategory()) {
            case MessageCategory.EMAIL:
                log.debug("onDelete");
                AuthCredentials[] credits = properties.getAuthCredentials();
                if (credits != null && credits.length == 1) {
                    MailCacheHandler.getInstance().clear(properties.getUserID(), properties.getInstallID());
                }
                break;
            case MessageCategory.WIDGET:
                if ("email".equalsIgnoreCase(properties.getApplicationName())) {
                    log.debug("Email widget is delete by the user " + properties.getUserID() + ", hence clearing info associated with the user.");
                    MailCacheHandler.getInstance().delete(properties.getSTBSerial());
                }
                break;
        }
    }
}
