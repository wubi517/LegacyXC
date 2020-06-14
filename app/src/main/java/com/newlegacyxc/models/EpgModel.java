package com.newlegacyxc.models;

import java.io.Serializable;

public class EpgModel implements Serializable {
    private String id,ch_id,correct,name,descr,real_id,category,director,actor,t_time,t_time_to,start_timestamp,stop_timestamp;
    private int duration,mark_memo,mark_archive;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getCh_id() {
        return ch_id;
    }

    public void setCh_id(String ch_id) {
        this.ch_id = ch_id;
    }

    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getReal_id() {
        return real_id;
    }

    public void setReal_id(String real_id) {
        this.real_id = real_id;
    }

    public String getT_time() {
        return t_time;
    }

    public void setT_time(String t_time) {
        this.t_time = t_time;
    }

    public String getT_time_to() {
        return t_time_to;
    }

    public void setT_time_to(String t_time_to) {
        this.t_time_to = t_time_to;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMark_archive() {
        return mark_archive;
    }

    public void setMark_archive(int mark_archive) {
        this.mark_archive = mark_archive;
    }

    public int getMark_memo() {
        return mark_memo;
    }

    public void setMark_memo(int mark_memo) {
        this.mark_memo = mark_memo;
    }

    public String  getStart_timestamp() {
        return start_timestamp;
    }

    public void setStart_timestamp(String start_timestamp) {
        this.start_timestamp = start_timestamp;
    }

    public String getStop_timestamp() {
        return stop_timestamp;
    }

    public void setStop_timestamp(String stop_timestamp) {
        this.stop_timestamp = stop_timestamp;
    }
}

