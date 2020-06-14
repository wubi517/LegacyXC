package com.newlegacyxc.models;

import java.io.Serializable;

/**
 * Created by RST on 7/19/2017.
 */

public class CategoryModelSeries implements Serializable {
    String id, name, type, url, group;
    int featured, parent_control;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getFeatured() {
        return featured;
    }

    public void setFeatured(int featured) {
        this.featured = featured;
    }

    public int getParent_control() {
        return parent_control;
    }

    public void setParent_control(int parent_control) {
        this.parent_control = parent_control;
    }
}
