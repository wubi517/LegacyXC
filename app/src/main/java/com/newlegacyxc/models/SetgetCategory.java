package com.newlegacyxc.models;

import java.util.ArrayList;

/**
 * Created by krishanu on 19/12/17.
 */
public class SetgetCategory {

    String category_name="",category_id="";
    String show_flag = "true";

    public String getShow_flag() {
        return show_flag;
    }

    public void setShow_flag(String show_flag) {
        this.show_flag = show_flag;
    }

    ArrayList<Parentsetget> channels;

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public ArrayList<Parentsetget> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<Parentsetget> channels) {
        this.channels = channels;
    }
}
