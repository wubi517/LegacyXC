package com.newlegacyxc.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MovieModel implements Serializable {
    @SerializedName("num")
    private String num;
    @SerializedName("name")
    private String name;
    @SerializedName("stream_type")
    private String stream_type;
    @SerializedName("stream_id")
    private String stream_id;
    @SerializedName("stream_icon")
    private String stream_icon;
    @SerializedName("container_extension")
    private String extension;
    private String type;
    @SerializedName("rating_5based")
    private double rating;
    @SerializedName("category_id")
    private String category_id;
    @SerializedName("custom_sid")
    private String custom_sid;
    @SerializedName("info")
    private MovieInfoModel movieInfoModel;
    @SerializedName("added")
    private String added;
    private boolean is_locked=false;

    private boolean is_favorite=false;

    public boolean isIs_locked() {
        return is_locked;
    }

    public void setIs_locked(boolean is_locked) {
        this.is_locked = is_locked;
    }
    public String getNum(){
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStream_type() {
        return stream_type;
    }

    public void setStream_type(String stream_type) {
        this.stream_type = stream_type;
    }

    public String getStream_id() {
        return stream_id;
    }

    public void setStream_id(String stream_id) {
        this.stream_id = stream_id;
    }

    public String getStream_icon() {
        return stream_icon;
    }

    public void setStream_icon(String stream_icon) {
        this.stream_icon = stream_icon;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public MovieInfoModel getMovieInfoModel() {
        return movieInfoModel;
    }

    public void setMovieInfoModel(MovieInfoModel movieInfoModel) {
        this.movieInfoModel = movieInfoModel;
    }

    public boolean isIs_favorite() {
        return is_favorite;
    }

    public void setIs_favorite(boolean is_favorite) {
        this.is_favorite = is_favorite;
    }

    public String getCustom_sid() {
        return custom_sid;
    }

    public void setCustom_sid(String custom_sid) {
        this.custom_sid = custom_sid;
    }

    public String getAdded() {
        return added;
    }

    public void setAdded(String added) {
        this.added = added;
    }
}
