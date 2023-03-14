package com.server;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

public class WarningMessage {

    private String nickName;
    private double latitude;
    private double longitude;
    private ZonedDateTime sent;
    private String dangertype;
    private JSONObject obj = new JSONObject();

    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    String dateText = now.format(formatter);

    public WarningMessage(JSONObject obj) {
        nickName = obj.getString("nickname");
        longitude = obj.getDouble("longitude");
        latitude = obj.getDouble("latitude");
        dangertype = obj.getString("dangertype");
        //converting from string JSON to ZonedDateTime
        String dateString = obj.getString("sent");
        sent = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        setSent();
        dateAsInt();
        
        this.obj = obj;
        obj.put("nickname", nickName);
        obj.put("longitude", longitude);
        obj.put("latitude", latitude);
        obj.put("dangertype", dangertype);
        obj.put("sent", sent);
    }
    
    void setSent() {
        long epochTime = System.currentTimeMillis();
        sent = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC);
    }

    public long dateAsInt() {
        return sent.toInstant().toEpochMilli();
    }

    
    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nick) {
        this.nickName = nick;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public String getDangertype() {
        return dangertype;
    }
    public void setDangertype(String dangertype) {
        this.dangertype = dangertype;
    }
    public ZonedDateTime getSent() {
        return sent;
    }
    public void setSent(ZonedDateTime sent) {
        this.sent = sent;
    }

    public JSONObject getJSONObject() {
        return obj;
    }
}
