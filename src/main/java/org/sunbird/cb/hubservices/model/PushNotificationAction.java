package org.sunbird.cb.hubservices.model;

import java.io.Serializable;

public class PushNotificationAction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;
    private String category;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public PushNotificationTemplate getTemplate() {
        return template;
    }

    public void setTemplate(PushNotificationTemplate template) {
        this.template = template;
    }

    PushNotificationTemplate template = new PushNotificationTemplate();
}
