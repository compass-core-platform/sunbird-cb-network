package org.sunbird.cb.hubservices.model;

import java.io.Serializable;

public class PushNotificationData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;
    private Integer priority;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public PushNotificationAction getAction() {
        return action;
    }

    public void setAction(PushNotificationAction action) {
        this.action = action;
    }

    PushNotificationAction action = new PushNotificationAction();
}
