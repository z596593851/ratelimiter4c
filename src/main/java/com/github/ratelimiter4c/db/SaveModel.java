package com.github.ratelimiter4c.db;


import java.util.Date;

public class SaveModel {
    private String appid;
    private String url;
    private int type;
    private Date time;
    private long count;
    private String address;

    public SaveModel(){

    }

    public SaveModel(String appid, String url, int type, Date time, long count, String address) {
        this.appid = appid;
        this.url = url;
        this.type = type;
        this.time = time;
        this.count = count;
        this.address=address;

    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
