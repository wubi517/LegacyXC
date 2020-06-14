package com.newlegacyxc.models;

import java.io.Serializable;

/**
 * Created by RST on 7/19/2017.
 */

public class ChannelModel implements Serializable {
    private String num,name,stream_type,stream_id,stream_icon,epg_channel_id,added,category_id,custom_sid,tv_archive,direct_source,tv_archive_duration;
    private boolean  is_locked, is_favorite;
    private int cell;

    public int getCell(){return cell;}

    public void setCell(int cell){this.cell = cell;}

    public String getNum(){return num;}

    public void setNum(String num){this.num = num;}

    public String getName(){return name;}

    public void setName(String name){this.name = name;}

    public String getStream_type(){return stream_type;}

    public void setStream_type(String stream_type){this.stream_type = stream_type;}

    public String getStream_id(){return stream_id;}

    public void setStream_id(String stream_id){this.stream_id = stream_id;}

    public String getStream_icon(){return stream_icon;}

    public void setStream_icon(String stream_icon){this.stream_icon = stream_icon;}

    public String getEpg_channel_id(){return epg_channel_id;}

    public void setEpg_channel_id(String epg_channel_id){this.epg_channel_id = epg_channel_id;}


    public String getAdded(){return added;}

    public void setAdded(String added){this.added = added;}

    public String getCategory_id(){return category_id;}

    public void setCategory_id(String category_id){this.category_id = category_id;}

    public String getCustom_sid(){return custom_sid;}

    public void setCustom_sid(String custom_sid){this.custom_sid = custom_sid;}

    public String getTv_archive(){return tv_archive;}

    public void setTv_archive(String tv_archive){this.tv_archive = tv_archive;}

    public String getDirect_source(){return direct_source;}

    public void setDirect_source(String direct_source){this.direct_source = direct_source;}

    public String getTv_archive_duration(){return tv_archive_duration;}

    public void setTv_archive_duration(String tv_archive_duration){this.tv_archive_duration = tv_archive_duration;}

    public boolean is_locked() {
        return is_locked;
    }

    public void setIs_locked(boolean is_locked) {
        this.is_locked = is_locked;
    }

    public boolean is_favorite() {
        return is_favorite;
    }

    public void setIs_favorite(boolean is_favorite) {
        this.is_favorite = is_favorite;
    }

}
