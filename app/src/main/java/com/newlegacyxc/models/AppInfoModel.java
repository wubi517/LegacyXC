package com.newlegacyxc.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AppInfoModel implements Serializable {
    @SerializedName("app_info")
    private AppInfoEntity appInfo;
    @SerializedName("result")
    private List<String> result;
    @SerializedName("success")
    private boolean success;
    @SerializedName("expired_date")
    private String expiredDate;

    public AppInfoEntity getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppInfoEntity appInfo) {
        this.appInfo = appInfo;
    }

    public List<String> getResult() {
        return result;
    }

    public void setResult(List<String> result) {
        this.result = result;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(String expiredDate) {
        this.expiredDate = expiredDate;
    }

    public static class AppInfoEntity implements Serializable {
        @SerializedName("slider_time")
        private int sliderTime;
        @SerializedName("slider_on_off")
        private int slider_on_off;
//        @SerializedName("message_time")
//        private String messageTime;
//        @SerializedName("message_on_off")
//        private String messageOnOff;
        @SerializedName("version")
        private String version;
        @SerializedName("pin_4")
        private String pin4;
        @SerializedName("pin_3")
        private String pin3;
        @SerializedName("pin_2")
        private String pin2;
//        @SerializedName("message")
//        private String message;
        @SerializedName("app_url")
        private String appUrl;
        @SerializedName("vpn_ip")
        private String vpnIp;
        @SerializedName("slider")
        private List<SliderEntity> slider;
        @SerializedName("logo")
        private String logo;
        @SerializedName("img_url")
        private String img_url;
//        @SerializedName("image_urls")
//        private List<String> image_urls;

        public int getSliderTime() {
            return sliderTime;
        }

        public void setSliderTime(int sliderTime) {
            this.sliderTime = sliderTime;
        }

//        public String getMessageTime() {
//            return messageTime;
//        }
//
//        public void setMessageTime(String messageTime) {
//            this.messageTime = messageTime;
//        }
//
//        public String getMessageOnOff() {
//            return messageOnOff;
//        }
//
//        public void setMessageOnOff(String messageOnOff) {
//            this.messageOnOff = messageOnOff;
//        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getPin4() {
            return pin4;
        }

        public void setPin4(String pin4) {
            this.pin4 = pin4;
        }

        public String getPin3() {
            return pin3;
        }

        public void setPin3(String pin3) {
            this.pin3 = pin3;
        }

        public String getPin2() {
            return pin2;
        }

        public void setPin2(String pin2) {
            this.pin2 = pin2;
        }

//        public String getMessage() {
//            return message;
//        }
//
//        public void setMessage(String message) {
//            this.message = message;
//        }

        public String getAppUrl() {
            return appUrl;
        }

        public void setAppUrl(String appUrl) {
            this.appUrl = appUrl;
        }

        public String getVpnIp() {
            return vpnIp;
        }

        public void setVpnIp(String vpnIp) {
            this.vpnIp = vpnIp;
        }

        public List<SliderEntity> getSlider() {
            return slider;
        }

        public void setSlider(List<SliderEntity> slider) {
            this.slider = slider;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

//        public List<String> getImage_urls() {
//            return image_urls;
//        }
//
//        public void setImage_urls(List<String> image_urls) {
//            this.image_urls = image_urls;
//        }

        public String getImg_url() {
            return img_url;
        }

        public void setImg_url(String img_url) {
            this.img_url = img_url;
        }

        public int getSlider_on_off() {
            return slider_on_off;
        }

        public void setSlider_on_off(int slider_on_off) {
            this.slider_on_off = slider_on_off;
        }
    }

    public static class SliderEntity implements Serializable {
        @SerializedName("imageUrl")
        private String imageurl;
        @SerializedName("body")
        private String body;
        @SerializedName("header")
        private String header;

        public String getImageurl() {
            return imageurl;
        }

        public void setImageurl(String imageurl) {
            this.imageurl = imageurl;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }
    }
}
