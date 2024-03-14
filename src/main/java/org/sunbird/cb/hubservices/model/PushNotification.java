package org.sunbird.cb.hubservices.model;

import java.io.Serializable;
import java.util.ArrayList;

public class PushNotification implements Serializable {

    private static final long serialVersionUID = 1L;
    private FilterConfig filter = new FilterConfig();
    private String dataValue;
    private Boolean isScheduleNotification;

    public Boolean getIsScheduleNotification() {
        return isScheduleNotification;
    }

    public void setIsScheduleNotification(Boolean isScheduleNotification) {
        this.isScheduleNotification = isScheduleNotification;
    }

    ArrayList data = new ArrayList();

    public FilterConfig getFilter() {
        return filter;
    }

    public void setFilter(FilterConfig filter) {
        this.filter = filter;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    public ArrayList getData() {
        return data;
    }

    public void setData(ArrayList data) {
        this.data = data;
    }
}

