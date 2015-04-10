package net.capps.word.rest.models;

import java.util.List;

/**
 * Created by charlescapps on 4/9/15.
 */
public class OneSignalNotificationModel {
    private String app_id;
    private OneSignalContentModel contents;
    private OneSignalContentModel headings;
    private Boolean isAndroid;
    private Boolean isIos;
    private List<OneSignalTagModel> tags;

    public OneSignalNotificationModel() {
    }

    public OneSignalNotificationModel(String app_id, OneSignalContentModel contents, OneSignalContentModel headings, List<OneSignalTagModel> tags) {
        this.app_id = app_id;
        this.contents = contents;
        this.headings = headings;
        this.tags = tags;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public OneSignalContentModel getContents() {
        return contents;
    }

    public void setContents(OneSignalContentModel contents) {
        this.contents = contents;
    }

    public OneSignalContentModel getHeadings() {
        return headings;
    }

    public void setHeadings(OneSignalContentModel headings) {
        this.headings = headings;
    }

    public Boolean getIsAndroid() {
        return isAndroid;
    }

    public void setIsAndroid(Boolean isAndroid) {
        this.isAndroid = isAndroid;
    }

    public Boolean getIsIos() {
        return isIos;
    }

    public void setIsIos(Boolean isIos) {
        this.isIos = isIos;
    }

    public List<OneSignalTagModel> getTags() {
        return tags;
    }

    public void setTags(List<OneSignalTagModel> tags) {
        this.tags = tags;
    }
}
