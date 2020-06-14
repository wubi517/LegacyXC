package com.newlegacyxc.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by RST on 7/19/2017.
 */

public class FullModel implements Serializable {
    private String category_id,category_name;
    private List<EPGChannel> channels;
    private  int catchable_count=0;

//    public FullModel(String category_id, List<EPGChannel> channels, String category_name) {
//        this.category_id = category_id;
//        this.channels = channels;
//        this.category_name = category_name;
//    }

    public FullModel(String category_id, List<EPGChannel> channels, String category_name,int catchable_count) {
        this.category_id = category_id;
        this.channels = channels;
        this.category_name = category_name;
        this.catchable_count = catchable_count;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public List<EPGChannel> getChannels() {
        return channels;
    }

    public void setChannels(List<EPGChannel> channels) {
        this.channels = channels;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public int getCatchable_count() {
        return catchable_count;
    }

    public void setCatchable_count(int catchable_count) {
        this.catchable_count = catchable_count;
    }
}
