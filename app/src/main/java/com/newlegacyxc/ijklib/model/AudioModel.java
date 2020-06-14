package com.newlegacyxc.ijklib.model;

import java.io.Serializable;

import tv.danmaku.ijk.media.player.misc.IMediaFormat;

public class AudioModel implements Serializable {
    String language,infoline;
    int trcktype;
    IMediaFormat iMediaFormat;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getInfoline() {
        return infoline;
    }

    public void setInfoline(String infoline) {
        this.infoline = infoline;
    }

    public int getTrcktype() {
        return trcktype;
    }

    public void setTrcktype(int trcktype) {
        this.trcktype = trcktype;
    }

    public IMediaFormat getiMediaFormat() {
        return iMediaFormat;
    }

    public void setiMediaFormat(IMediaFormat iMediaFormat) {
        this.iMediaFormat = iMediaFormat;
    }
}
