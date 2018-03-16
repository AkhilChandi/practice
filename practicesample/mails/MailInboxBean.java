/**
 * @(#)MailInboxBean 1.01 Apr 26, 2011
 *
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
 * @author Harini
 */
public class MailInboxBean implements Serializable {

    private String msgBody;
    private String subject;
    private String msgDate;
    private String msgFrom;
    private int viewingStatus = 0;

    public String getMsgDate() {
        return msgDate;
    }

    public void setMsgDate(String msgDate) {
        this.msgDate = msgDate;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public String getMsgFrom() {
        return msgFrom;
    }

    public void setMsgFrom(String msgFrom) {
        this.msgFrom = msgFrom;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getViewingStatus() {
        return viewingStatus;
    }

    public void setViewingStatus(int viewingStatus) {
        this.viewingStatus = viewingStatus;
    }
}
