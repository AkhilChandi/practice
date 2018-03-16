package com.gaian.interactiveservices.mails;

/**
 * @(#)MailService 1.01 Jun 21, 2011
 *
 * Copyright 2008, by Gaian Solutions. Gaian Consulting Services Inc.400 N.
 * Continental Blvd. Suite 330,El Segundo,CA 90245, USA All rights reserved.
 * This software is the confidential and proprietary information ofGaian
 * Consulting Services Inc. ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with Gaian.
 */
import java.util.Hashtable;
import org.apache.log4j.Logger;

/**
 *
 * @author SaiSatish
 */
public class MailService extends Thread {

    private Logger log;

    @Override
    public void run() {
        MailThreadManager loadBalancer = null;
        log = Logger.getLogger(MailService.class);
        MailUtil.readMailServerInfo();
        log.debug("Lanching the Mail Service");

        while (true) {
            try {
                if (loadBalancer == null || !loadBalancer.isAlive()) {
                    loadBalancer = new MailThreadManager();
                    loadBalancer.setPriority(Thread.MIN_PRIORITY);
                    loadBalancer.setDaemon(true);
                    loadBalancer.start();
                } else if (loadBalancer.getState() == Thread.State.BLOCKED) {
                    loadBalancer.interrupt();
                }

            } catch (Exception e) {
                log.error(e, e);
            }
            try {
                log.debug("Mail Service will start after : [" + EmailContext.getEmailServiceIterationInterval() + "] mins.");
                Thread.sleep(EmailContext.getEmailServiceIterationInterval() * 60 * 1000);
            } catch (InterruptedException e) {
                log.error(e, e);
            }
        }
    }
}
