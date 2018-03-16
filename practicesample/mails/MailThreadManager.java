/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gaian.interactiveservices.mails;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * This class creates the threads based on the no of users exist in the service.
 *
 * @author satish
 */
public class MailThreadManager extends Thread {

    Logger log = null;
    ThreadPoolExecutor threadPool = null;
    final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(500);

    @Override
    public void run() {
        log = Logger.getLogger(MailThreadManager.class);
        List<String> userList = null;
        List<String> subList = null;
        int threadCount = 1;
        int userCount = 0;
        int startIndex = 0;
        int endIndex = 0;
        threadPool = new ThreadPoolExecutor(EmailContext.getEmailsThreadCorePoolSize(), EmailContext.getEmailsThreadMaximumPoolSize(), EmailContext.getEmailsThreadKeepAliveTimeInSec(), TimeUnit.SECONDS, queue);
        while (true) {
            try {
                log.debug("Active tasks count that are running in threadpool:" + threadPool.getQueue().size());
                if (threadPool.getQueue().isEmpty()) {
                    userList = new ArrayList<>();
                    ArrayList<String> mailIdList = new ArrayList<>(MailCacheHandler.getInstance().getActiveEmailIDs());
                    log.debug("The mailIDs list is : " + mailIdList);
                    int targetSize = 0;
                    if (mailIdList.size() > EmailContext.getEmailsThreadMaximumPoolSize()) {
                        targetSize = EmailContext.getEmailsThreadMaximumPoolSize();
                    } else {
                        targetSize = mailIdList.size();
                    }
                    log.debug("Mail Ids List:" + mailIdList.toString());
                    log.debug("Obtaining the mailids from index 0 - " + targetSize);
                    userList.addAll(mailIdList.subList(0, targetSize));
//                        MailCacheHandler.getInstance().getProcessingMailsIds().removeAll(userList);
                    log.debug("Obtained mailids list:" + userList);

                    threadCount = 1;
                    userCount = userList.size();
                    startIndex = 0;
                    endIndex = 0;

                    if (userCount > EmailContext.getEmailAccountsPerThread()) {
                        BigDecimal value = new BigDecimal((double) userCount / EmailContext.getEmailAccountsPerThread());
                        value = value.setScale(0, RoundingMode.UP);
                        threadCount = value.intValue();
                    }
                    log.debug("Thread count for Mail service: " + threadCount);
                    for (int i = 0; i < threadCount; i++) {
                        startIndex = i * EmailContext.getEmailAccountsPerThread();
                        endIndex = startIndex + EmailContext.getEmailAccountsPerThread();
                        endIndex = endIndex > userCount ? userCount : endIndex;

                        subList = userList.subList(startIndex, endIndex);
                        MailDataExtractor maildataextractor = new MailDataExtractor(subList, "MailDataExtractor[Thread" + i + "]");
                        threadPool.execute(maildataextractor);
                        subList = null;
                    }
                }
                userList = null;
                log.info("Mail service is in sleeping mode and it will resume after [" + EmailContext.getEmailServiceIterationInterval() + "] min");
                Thread.sleep(EmailContext.getEmailServiceIterationInterval() * 60 * 1000);
            } catch (Exception e) {
                log.error(e, e);
            } finally {
                threadPool.purge();
            }
        }
    }
}
