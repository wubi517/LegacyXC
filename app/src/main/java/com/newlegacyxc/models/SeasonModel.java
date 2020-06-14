package com.newlegacyxc.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by RST on 2/25/2017.
 */

public class SeasonModel implements Serializable {
    @SerializedName("episode_count")
    private int total;
    @SerializedName("id")
    private String id;
    @SerializedName("cover")
    private String icon;
    @SerializedName("cover_big")
    private String icon_big;
    @SerializedName("name")
    private String name;
    @SerializedName("overview")
    private String overview;
    @SerializedName("season_number")
    private int season_number;
    @SerializedName("air_date")
    private String air_date;
    private List<EpisodeModel> episodeModels;

    private boolean is_watched=false;

    public SeasonModel(String name) {
        this.name = "Season "+name;
        season_number = Integer.valueOf(name);
    }

    public SeasonModel(int total, String icon, String name) {
        this.total = total;
        this.icon = icon;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String url) {
        this.icon = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String ext) {
        this.name = ext;
    }

    public List<EpisodeModel> getEpisodeModels() {
        return episodeModels;
    }

    public void setEpisodeModels(List<EpisodeModel> episodeModels) {
        this.episodeModels = episodeModels;
    }

    public String getIcon_big() {
        return icon_big;
    }

    public void setIcon_big(String icon_big) {
        this.icon_big = icon_big;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public int getSeason_number() {
        return season_number;
    }

    public void setSeason_number(int season_number) {
        this.season_number = season_number;
    }

    public String getAir_date() {
        return air_date;
    }

    public void setAir_date(String air_date) {
        this.air_date = air_date;
    }

    public boolean isIs_watched() {
        return is_watched;
    }

    public void setIs_watched(boolean is_watched) {
        this.is_watched = is_watched;
    }
}
