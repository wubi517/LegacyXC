package com.newlegacyxc.models;

/**
 * Created by krishanu on 06/11/17.
 */
public class Childsetget {

    String channel_id="",start="",end="",stream_icon="",title="",description="",parent_streamid="";

    public String getParent_streamid() {
        return parent_streamid;
    }

    public void setParent_streamid(String parent_streamid) {
        this.parent_streamid = parent_streamid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getStream_icon() {
        return stream_icon;
    }

    public void setStream_icon(String stream_icon) {
        this.stream_icon = stream_icon;
    }
}
