package com.newlegacyxc.models;

import java.io.Serializable;
import java.util.List;

public class SeriesFullModel implements Serializable {
    String category;
    List<MovieModel> channels;

    public SeriesFullModel(String category, List<MovieModel> channels) {
        this.category = category;
        this.channels = channels;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<MovieModel> getChannels() {
        return channels;
    }

    public void setChannels(List<MovieModel> channels) {
        this.channels = channels;
    }
}
