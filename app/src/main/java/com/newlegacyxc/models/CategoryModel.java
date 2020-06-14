package com.newlegacyxc.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RST on 3/2/2017.
 */

public class CategoryModel implements Serializable {
    @SerializedName("category_id")
    private String id;
    @SerializedName("category_name")
    private String name;
    @SerializedName("parent_id")
    private String url;

    public CategoryModel(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }
    private List<MovieModel> movieModels=new ArrayList<>();

    private List<SeriesModel> seriesModels = new ArrayList<>();

    public List<MovieModel> getMovieModels() {
        return movieModels;
    }

    public void setMovieModels(List<MovieModel> movieModels) {
        this.movieModels = movieModels;
    }

    public List<SeriesModel> getSeriesModels() {
        return seriesModels;
    }

    public void setSeriesModels(List<SeriesModel> seriesModels) {
        this.seriesModels = seriesModels;
    }

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
