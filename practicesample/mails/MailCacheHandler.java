/*
 * GaianMail 3.0 11 Dec, 2013
 * Copyright 2008, by Gaian Solutions. Gaian Consulting Services Inc.400 N.
 * Continental Blvd. Suite 330,El Segundo,CA 90245, USA All rights reserved.
 * This software is the confidential and proprietary information ofGaian
 * Consulting Services Inc. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Gaian.
 */
package com.gaian.interactiveservices.mails;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;

/**
 *
 * @author Lovababu Golthi (GSI-155)
 *
 * Implemented On: 11 Dec, 2013
 */
public final class MailCacheHandler {

    private static final MailCacheHandler instance;
    private final Cache cache;
    private static final String CACHE_NAME = "MailService";
    public static final Integer UNKONOWN_USER = -1;
    private Logger log = Logger.getLogger(getClass());

    static {
        instance = new MailCacheHandler();
    }

    public static MailCacheHandler getInstance() {
        return instance;
    }

    private MailCacheHandler() {
        cache = CacheManager.getInstance().getCache(CACHE_NAME);
        if (!cache.isKeyInCache(KEY_EMAIL_ID_ACTIVE_LIST))
            cache.put(new Element(KEY_EMAIL_ID_ACTIVE_LIST, new TreeSet<String>()));
        if (!cache.isKeyInCache(KEY_USER_EMAIL_INSTALL_MAP))
            cache.put(new Element(KEY_USER_EMAIL_INSTALL_MAP, new HashMap<String, Integer>()));
    }
    // keys
    public static final String KEY_EMAIL_ID_ACTIVE_LIST = "KEY_EMAIL_ID_ACTIVE_LIST";
    public static final String KEY_EMAIL_ID_SPECIFIC_STB_LIST = "KEY_EMAIL_ID_SPECIFIC_STB_LIST_";
    public static final String KEY_EMAIL_ID_AUTH_CREDITS = "KEY_EMAIL_ID_AUTH_CREDITS_";
    public static final String KEY_STB_SPECIFIC_USER = "KEY_STB_SPECIFIC_USER_";
    public static final String KEY_USER_PREFERENCE = "KEY_USER_PREFERENCE_";
    public static final String KEY_USER_PREFERENCE$ = "{userID:%d,installID:%d}";
    public static final String KEY_EMAIL_ID_SPEC_INSTALL_ID_LIST = "{emailID:%s}";
    public static final String KEY_USER_EMAIL_INSTALL_MAP = "KEY_USER_EMAIL_INSTALL_MAP";
    public static final String KEY_USER_ID_SPEC_MAIL_IDS = "KEY_USER_ID_SPEC_MAIL_IDS_";
    public static final String ENTRY_USER_EMAIL_INSTALL_MAP = "{userID:%d,emailID:%s}";

    public Set<String> getActiveEmailIDs() {
        return (Set<String>) cache.get(KEY_EMAIL_ID_ACTIVE_LIST).getObjectValue();
    }

    public void setAuthCredits(AuthCredentials authCredentials) {
        if (cache.isKeyInCache(KEY_EMAIL_ID_AUTH_CREDITS + authCredentials.getUsername())) {
            try {
                cache.acquireWriteLockOnKey(KEY_EMAIL_ID_AUTH_CREDITS + authCredentials.getUsername());
                AuthCredentials existingAC = (AuthCredentials) cache.get(KEY_EMAIL_ID_AUTH_CREDITS + authCredentials.getUsername()).getObjectValue();
                existingAC.setPassword(authCredentials.getPassword());
                existingAC.setLoginFailed(authCredentials.isLoginFailed());
            } catch (Exception ex) {
            } finally {
                cache.releaseWriteLockOnKey(KEY_EMAIL_ID_AUTH_CREDITS + authCredentials.getUsername());
            }
        } else {
            cache.put(new Element(KEY_EMAIL_ID_AUTH_CREDITS + authCredentials.getUsername(), authCredentials));
        }
    }

    public AuthCredentials getAuthCredits(String emailID) {
        try {
            return (AuthCredentials) cache.get(KEY_EMAIL_ID_AUTH_CREDITS + emailID).getObjectValue();
        } catch (NullPointerException npe) {
            return null;
        }
    }

    public void addSTBSerial(String emailID, String STBSerial) {
        if (cache.isKeyInCache(KEY_EMAIL_ID_SPECIFIC_STB_LIST + emailID)) {
            try {
                cache.acquireWriteLockOnKey(KEY_EMAIL_ID_SPECIFIC_STB_LIST + emailID);
                Set<String> STBSerials = (Set<String>) cache.get(KEY_EMAIL_ID_SPECIFIC_STB_LIST + emailID).getObjectValue();
                STBSerials.add(STBSerial);
            } catch (Exception ex) {
            } finally {
                cache.releaseWriteLockOnKey(KEY_EMAIL_ID_SPECIFIC_STB_LIST + emailID);
            }
        } else {
            cache.put(new Element(KEY_EMAIL_ID_SPECIFIC_STB_LIST + emailID, new HashSet<>(Collections.<String>singleton(STBSerial))));
        }
    }

    public Set<String> getSTBSerials(String emailID) {
        try {
            return (Set<String>) cache.get(KEY_EMAIL_ID_SPECIFIC_STB_LIST + emailID).getObjectValue();
        } catch (NullPointerException npe) {
            return new HashSet<>();
        }
    }

    public Integer setUser(String STBSerial, int userID) {
        Integer oldUserID = userID;
        if (cache.isKeyInCache(KEY_STB_SPECIFIC_USER + STBSerial)) {
            try {
                cache.acquireWriteLockOnKey(KEY_STB_SPECIFIC_USER + STBSerial);
                oldUserID = (Integer) cache.get(KEY_STB_SPECIFIC_USER + STBSerial).getObjectValue();
                cache.put(new Element(KEY_STB_SPECIFIC_USER + STBSerial, new Integer(userID)));
            } catch (Exception ex) {
                oldUserID = UNKONOWN_USER;
            } finally {
                cache.releaseWriteLockOnKey(KEY_STB_SPECIFIC_USER + STBSerial);
            }
        } else {
            cache.put(new Element(KEY_STB_SPECIFIC_USER + STBSerial, new Integer(userID)));
        }
        return oldUserID;
    }

    public Integer getUser(String STBSerial) {
        try {
            return (Integer) cache.get(KEY_STB_SPECIFIC_USER + STBSerial).getObjectValue();
        } catch (NullPointerException npe) {
            return UNKONOWN_USER;
        }
    }

    public synchronized Set<String> getEmailIDs(int userID) {
        if (!cache.isKeyInCache(KEY_USER_ID_SPEC_MAIL_IDS + userID))
            cache.put(new Element(KEY_USER_ID_SPEC_MAIL_IDS + userID, new HashSet<String>()));
        return (Set<String>) cache.get(KEY_USER_ID_SPEC_MAIL_IDS + userID).getObjectValue();
    }

    public void addEmailID(int userID, String emailID) {
        Set<String> emailIDs = getEmailIDs(userID);
        synchronized (emailIDs) {
            getEmailIDs(userID).add(emailID);
        }
    }

    public void delete(String STBSerial) {
        Integer userID = getUser(STBSerial);
        Set<String> emailIDs = new HashSet<>(getEmailIDs(userID));
        for (String emaiID : emailIDs) {
            Integer installID = getInstallID(userID, emaiID);
            clear(userID, installID);
        }
        cache.remove(KEY_USER_ID_SPEC_MAIL_IDS + userID);
    }

    // returns UserPreferences stored under this userID and installID
    public UserPreference getUserPreference(int userID, int installID) {
        try {
            return (UserPreference) cache.get(String.format(KEY_USER_PREFERENCE$, userID, installID))
                    .getObjectValue();
        } catch (NullPointerException npe) {
            return null;
        }
    }

    // Stores given UserPreference under {userID,installID}
    public void setUserPreference(UserPreference preference) {
        cache.put(new Element(String.format(KEY_USER_PREFERENCE$, preference.getUserID(), preference.getInstallID()), preference));
    }

    // returns the installIDs associated with this emailID
    public Set<Integer> getInstallIDs(String emailID) {
        if (emailID == null)
            return null;
        String key = String.format(KEY_EMAIL_ID_SPEC_INSTALL_ID_LIST, emailID);
        try {
            return (Set<Integer>) cache.get(key).getObjectValue();
        } catch (NullPointerException npe) {
            return null;
        }
    }

    //  adds given installID into the list of this emailID.
    public void addInstallID(String emailID, int installID) {
        Set<Integer> installIDList = getInstallIDs(emailID);
        if (installIDList == null) {
            String key = String.format(KEY_EMAIL_ID_SPEC_INSTALL_ID_LIST, emailID);
            cache.acquireWriteLockOnKey(key);
            cache.put(new Element(key, new TreeSet<>(Collections.<Integer>singleton(installID))));
            cache.releaseWriteLockOnKey(key);
        } else {
            synchronized (installIDList) {
                installIDList.add(installID);
            }
        }
    }

    //  clears cache related to this installID and userID
    public void clear(int userID, int installID) {
        UserPreference pref = getUserPreference(userID, installID);
        if (pref != null) {
            Set<Integer> installIDs = getInstallIDs(pref.getEmailID());
            if (installIDs != null) {
                synchronized (installIDs) {
                    installIDs.remove(installID);
                    if (installIDs.isEmpty()) {
                        Set<String> activeEmailIDs = getActiveEmailIDs();
                        synchronized (activeEmailIDs) {
                            activeEmailIDs.remove(pref.getEmailID());
                        }
                        cache.remove(String.format(KEY_EMAIL_ID_SPEC_INSTALL_ID_LIST, pref.getEmailID()));
                        cache.remove(KEY_EMAIL_ID_AUTH_CREDITS + pref.getEmailID());
                    }
                }
            }
            cache.remove(String.format(KEY_USER_PREFERENCE$, userID, installID));
            Set<String> emailIDs = getEmailIDs(userID);
            synchronized (emailIDs) {
                emailIDs.remove(pref.getEmailID());
                if (emailIDs.isEmpty()) {
                    cache.remove(KEY_USER_ID_SPEC_MAIL_IDS + userID);
                    Set<String> stbSerials = getSTBSerials(pref.getEmailID());
                    synchronized (stbSerials) {
                        stbSerials.remove(pref.getSTBSerial());
                        if (stbSerials.isEmpty())
                            cache.remove(KEY_EMAIL_ID_SPECIFIC_STB_LIST + pref.getEmailID());
                    }
                }
            }
        }
    }

    public synchronized Integer getInstallID(int userID, String emailID) {
        String entryKey = String.format(ENTRY_USER_EMAIL_INSTALL_MAP, userID, emailID);
        return ((Map<String, Integer>) cache.get(KEY_USER_EMAIL_INSTALL_MAP).getObjectValue()).get(entryKey);
    }

    public synchronized void setInstallID(int userID, String emailID, int installID) {
        String entryKey = String.format(ENTRY_USER_EMAIL_INSTALL_MAP, userID, emailID);
        ((Map<String, Integer>) cache.get(KEY_USER_EMAIL_INSTALL_MAP).getObjectValue())
                .put(entryKey, installID);
    }
}
