package com.github.ratelimiter4c.db;

import java.sql.Date;

public class SaveModel {
    private String appid;
    private String url;
    private int type;
    private Date time;
    private long count;

    public SaveModel(String appid, String url, int type, Date time, long count) {
        this.appid = appid;
        this.url = url;
        this.type = type;
        this.time = time;
        this.count = count;
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
}
