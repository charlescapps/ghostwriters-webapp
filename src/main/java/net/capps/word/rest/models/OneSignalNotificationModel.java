package net.capps.word.rest.models;

import java.util.List;

/**
 * Created by charlescapps on 4/9/15.
 */
public class OneSignalNotificationModel {
    private String app_id;
    private PushContentModel contents;
    private PushContentModel headings;
    private PushData data;
    private Boolean isAndroid;
    private Boolean isIos;
    private List<PushTagModel> tags;

    public OneSignalNotificationModel() {
    }

    public OneSignalNotificationModel(String app_id, PushContentModel contents, PushContentModel headings, List<PushTagModel> tags) {
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

    public PushContentModel getContents() {
        return contents;
    }

    public void setContents(PushContentModel contents) {
        this.contents = contents;
    }

    public PushContentModel getHeadings() {
        return headings;
    }

    public void setHeadings(PushContentModel headings) {
        this.headings = headings;
    }

    public PushData getData() {
        return data;
    }

    public void setData(PushData data) {
        this.data = data;
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

    public List<PushTagModel> getTags() {
        return tags;
    }

    public void setTags(List<PushTagModel> tags) {
        this.tags = tags;
    }
}
