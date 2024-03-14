package org.sunbird.cb.hubservices.model;

import java.io.Serializable;

public class FilterConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userId;
}