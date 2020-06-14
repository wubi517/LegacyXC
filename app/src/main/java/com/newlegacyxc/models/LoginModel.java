package com.newlegacyxc.models;

import java.io.Serializable;

/**
 * Created by RST on 7/26/2017.
 */

public class LoginModel implements Serializable {
    private String user_name, password,exp_date;

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getExp_date(){return exp_date;}

    public void setExp_date(String exp_date){this.exp_date = exp_date;}
}
