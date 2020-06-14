package com.newlegacyxc.models;

import java.util.ArrayList;

/**
 * Created by krishanu on 06/11/17.
 */
public class Parentsetget {

    String name="",stream_icon="",stream_type="";
    int id;
    String categoryid="",container_extension="";
    String epg_channel_id="";
    ArrayList<Childsetget> childlist=new ArrayList<>();

    public String getContainer_extension() {
        return container_extension;
    }

    public void setContainer_extension(String container_extension) {
        this.container_extension = container_extension;
    }

    public String getEpg_channel_id() {
        return epg_channel_id;
    }

    public void setEpg_channel_id(String epg_channel_id) {
        this.epg_channel_id = epg_channel_id;
    }

    public String getStream_type() {
        return stream_type;
    }

    public void setStream_type(String stream_type) {
        this.stream_type = stream_type;
    }

    public ArrayList<Childsetget> getChildlist() {
        return childlist;
    }

    public void setChildlist(ArrayList<Childsetget> childlist) {
        this.childlist = childlist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStream_icon() {
        return stream_icon;
    }

    public void setStream_icon(String stream_icon) {
        this.stream_icon = stream_icon;
    }

    public String getCategoryid() {
        return categoryid;
    }

    public void setCategoryid(String categoryid) {
        this.categoryid = categoryid;
    }
}
