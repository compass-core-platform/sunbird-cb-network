package org.sunbird.cb.hubservices.model;

import java.io.Serializable;

public class PushNotificationTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;
    private String data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PushNotificationConfig getConfig() {
        return config;
    }

    public void setConfig(PushNotificationConfig config) {
        this.config = config;
    }

    private String id;
    PushNotificationConfig config = new PushNotificationConfig();

}
