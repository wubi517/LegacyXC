package com.newlegacyxc.vpn.fastconnect.api;

public class AuthData {
    private String address;
    private String authType;
    private String password;
    private String username;
    private String vpnName;

    public AuthData(String address, String authType, String password, String username, String vpnName) {
        this.address = address;
        this.authType = authType;
        this.password = password;
        this.username = username;
        this.vpnName = vpnName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVpnName() {
        return vpnName;
    }

    public void setVpnName(String vpnName) {
        this.vpnName = vpnName;
    }
}
