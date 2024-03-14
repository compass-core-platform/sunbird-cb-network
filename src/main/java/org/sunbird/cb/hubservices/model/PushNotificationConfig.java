package org.sunbird.cb.hubservices.model;

import java.io.Serializable;

public class PushNotificationConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private String otp;
    private String topic;
    private String subject;
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
