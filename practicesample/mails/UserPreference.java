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

import java.io.Serializable;

/**
 *
 * @author Lovababu Golthi (GSI-155)
 *
 * Implemented On: 11 Dec, 2013
 */
public class UserPreference implements Serializable {

    private int installID;
    private int subID;
    private int userID;
    private String STBSerial;
    private byte alert = 1;
    private String emailID;

    public String getEmailID() {
        return emailID;
    }

    public UserPreference setEmailID(String emailID) {
        this.emailID = emailID;
        return this;
    }

    public UserPreference(int installID, int subID, int userID, String STBSerial) {
        this.installID = installID;
        this.subID = subID;
        this.userID = userID;
        this.STBSerial = STBSerial;
    }

    public UserPreference() {
    }

    public int getInstallID() {
        return installID;
    }

    public UserPreference setInstallID(int installID) {
        this.installID = installID;
        return this;
    }

    public int getSubID() {
        return subID;
    }

    public UserPreference setSubID(int subID) {
        this.subID = subID;
        return this;
    }

    public int getUserID() {
        return userID;
    }

    public UserPreference setUserID(int userID) {
        this.userID = userID;
        return this;
    }

    public String getSTBSerial() {
        return STBSerial;
    }

    public UserPreference setSTBSerial(String STBSerial) {
        this.STBSerial = STBSerial;
        return this;
    }

    public byte getAlert() {
        return alert;
    }

    public UserPreference setAlert(byte alert) {
        this.alert = alert;
        return this;
    }

    @Override
    public String toString() {
        return "UserPreference{" + "installID=" + installID + ", subID=" + subID + ", userID=" + userID + ", STBSerial=" + STBSerial + ", alert=" + alert + '}';
    }
}
