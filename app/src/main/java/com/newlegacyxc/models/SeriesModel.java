package com.newlegacyxc.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SeriesModel implements Serializable {
    @SerializedName("num")
    private String num;
    @SerializedName("name")
    private String name;
    @SerializedName("")
    private String stream_type;
    @SerializedName("series_id")
    private String series_id;
    @SerializedName("cover")
    private String stream_icon;
    @SerializedName("youtube_trailer")
    private String youtube;
    @SerializedName("plot")
    private String plot;
    @SerializedName("cast")
    private String cast;
    @SerializedName("director")
    private String director;
    @SerializedName("genre")
    private String genre;
    @SerializedName("releaseDate")
    private String releaseDate;
    @SerializedName("rating")
    private String rating;
    @SerializedName("rating_5based")
    private float rating_5based;
    @SerializedName("category_id")
    private String category_id;
    @SerializedName("backdrop_path")
    private List<String> backdrop_path;//List<String>
    @SerializedName("last_modified")
    private String last_modified;

    private List<SeasonModel> seasonModels;

    private boolean is_watched = false;

    private boolean is_favorite = false;

    public String getNum() {
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

    public String getSeries_id() {
        return series_id;
    }

    public void setSeries_id(String series_id) {
        this.series_id = series_id;
    }

    public String getStream_icon() {
        return stream_icon;
    }

    public void setStream_icon(String stream_icon) {
        this.stream_icon = stream_icon;
    }

    public String getYoutube() {
        return youtube;
    }

    public void setYoutube(String youtube) {
        this.youtube = youtube;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public float getRating_5based() {
        return rating_5based;
    }

    public void setRating_5based(float rating_5based) {
        this.rating_5based = rating_5based;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public List<SeasonModel> getSeasonModels() {
        return seasonModels;
    }

    public void setSeasonModels(List<SeasonModel> seasonModels) {
        this.seasonModels = seasonModels;
    }

    public boolean isIs_favorite() {
        return is_favorite;
    }

    public void setIs_favorite(boolean is_favorite) {
        this.is_favorite = is_favorite;
    }

    public boolean isIs_watched() {
        return is_watched;
    }

    public void setIs_watched(boolean is_watched) {
        this.is_watched = is_watched;
    }

    public List<String> getBackdrop_path() {
        return backdrop_path;
    }

    public void setBackdrop_path(List<String> backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public String getLast_modified() {
        return last_modified;
    }

    public void setLast_modified(String last_modified) {
        this.last_modified = last_modified;
    }
}
