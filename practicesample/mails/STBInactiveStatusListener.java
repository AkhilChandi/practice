package com.gaian.interactiveservices.mails;

import com.gaian.interactiveservices.GNCProperties;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

/**
 *
 * @author Patan Manzoor
 */
public class STBInactiveStatusListener extends Thread implements MessageListener, ExceptionListener {

    private static STBInactiveStatusListener jmsSubscriber;
    private final Logger log;
    private String brokerURL;
    private String topicName;
    private ActiveMQConnectionFactory activeMQConnectionFactory;
    private TopicConnection topicConnection;
    private TopicSession topicSession;
    private Topic topic;
    private TopicSubscriber subscriber;
    private boolean needsToShutDownJMS = false;

    private STBInactiveStatusListener() {
        log = Logger.getLogger(this.getClass());
    }

    public static STBInactiveStatusListener getInstance() {
        if (jmsSubscriber == null) {
            jmsSubscriber = new STBInactiveStatusListener();
        }
        return jmsSubscriber;
    }

    public void setNeedsToShutDownJMS(boolean needsToShutDownJMS) {
        this.needsToShutDownJMS = needsToShutDownJMS;
    }

    public boolean getNeedsToShutDown() {
        return needsToShutDownJMS;
    }

    @Override
    public void run() {
        do {
            log.info("Initiating JMSSubscriber to process InactiveSTB sessions..");
            try {
                initiate();
            } catch (Exception ex) {
                log.error(ex.getMessage());
            } finally {
                if (topicConnection != null) {
                    log.info("Starting JMSSubscriber..");
                    startConsuming();
                    log.info("JMSSubscriber is now ready to process InactiveSTB sessions expiry...");
                    break;
                } else {
                    try {
                        Thread.sleep(1000 * 10);
                    } catch (InterruptedException ipe) {
                        log.error(ipe, ipe);
                    }
                }
            }
        } while (!needsToShutDownJMS);
    }

    public TopicConnection getAMQConnection() {
        return topicConnection;
    }

    public void initiate() throws JMSException {

        brokerURL = GNCProperties.getGNCServerURL();
        topicName = EmailContext.getInactiveTopicName();
        log.debug("Initiating JMSConnection [for BrokerURL : " + brokerURL + ", TopicName : " + topicName + "] as a DurableSubscriber..");
        activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerURL);
        topicConnection = activeMQConnectionFactory.createTopicConnection();
        topicConnection.setClientID("Mail");
        topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        topic = topicSession.createTopic(topicName);
        subscriber = topicSession.createDurableSubscriber(topic, topicConnection.getClientID());
        topicConnection.setExceptionListener(this);
        subscriber.setMessageListener(this);
        log.debug("Successfully initiated JMSConnection.");
    }

    public void stopConsuming() {
        try {
            log.debug("Stopping JMSConnection..");
            topicConnection.stop();
            log.debug("Stopped.");
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
        }
    }

    public void shutdown() {
        try {
            log.debug("ShuttingDown JMSConnection..");
            if (topicConnection != null) {
                topicConnection.close();
            }
            log.debug("Shutdowned.");
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
        } finally {
            jmsSubscriber = null;
        }
    }

    public void startConsuming() {
        try {
            log.debug("Starting JMSConnection..");
            topicConnection.start();
            log.debug("Started.");
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
        }
    }

    @Override
    synchronized public void onException(JMSException ex) {
        log.error(ex, ex);
        log.info("JMS Exception occured.  Shutting down client.");
    }

    @Override
    public void onMessage(Message message) {
        try {
            String sourceId = message.getStringProperty("sourceId");
            log.debug("################################## Got the call from " + sourceId + " #########################################");
            String stb_serial = message.getStringProperty("stbSerial");
            String status = message.getStringProperty("stbStatus");//STB STatus.
            log.info("Status of :" + status + " STB : " + stb_serial + "[ACTIVE, INACTIVE]");
            log.debug("The stbSerial is : " + stb_serial);
            if (status.equalsIgnoreCase("INACTIVE")) {
                log.debug("inside the InactiveMethod...");
                MailCacheHandler.getInstance().delete(stb_serial);
            }
        } catch (Exception ex) {
            log.error(ex, ex);
        } finally {
            log.info("########################### End of Onmessage handler ################################");
        }
    }
}
