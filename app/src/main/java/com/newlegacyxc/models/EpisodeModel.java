package com.newlegacyxc.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class EpisodeModel implements Serializable {
    @SerializedName("id")
    private String stream_id;
    @SerializedName("title")
    private String title;
    @SerializedName("container_extension")
    private String container_extension;
//    @SerializedName("info")
    private EpisodeInfoModel episodeInfoModel;
    @SerializedName("season")
    private int seasonId;

    private boolean is_watched=false;
    public String getStream_id() {
        return stream_id;
    }

    public void setStream_id(String stream_id) {
        this.stream_id = stream_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContainer_extension() {
        return container_extension;
    }

    public void setContainer_extension(String container_extension) {
        this.container_extension = container_extension;
    }

    public boolean isIs_watched() {
        return is_watched;
    }

    public void setIs_watched(boolean is_watched) {
        this.is_watched = is_watched;
    }

    public EpisodeInfoModel getEpisodeInfoModel() {
        return episodeInfoModel;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public void setEpisodeInfoModel(EpisodeInfoModel episodeInfoModel) {
        this.episodeInfoModel = episodeInfoModel;
    }
}
