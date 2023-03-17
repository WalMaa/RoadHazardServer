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
    private long epochTime;
    private String jsonDateString;
    private String dangerType;
    private JSONObject obj = new JSONObject();

    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    String dateText = now.format(formatter);

    public WarningMessage(JSONObject obj) {
        nickName = obj.getString("nickname");
        longitude = obj.getDouble("longitude");
        latitude = obj.getDouble("latitude");
        dangerType = obj.getString("dangertype");
        jsonDateString = obj.getString("sent");

        // getting date as epoch
        sent = ZonedDateTime.parse(jsonDateString);
        System.out.println(sent);
        epochTime = sent.toInstant().toEpochMilli();


        this.obj = obj;
        obj.put("nickname", nickName);
        obj.put("longitude", longitude);
        obj.put("latitude", latitude);
        obj.put("dangertype", dangerType);
        obj.put("sent", epochTime);
    }
    
    void setSent() {
        sent = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC);
        System.out.println(sent);
    }

    public long dateAsInt() {
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC);
        return epochTime;
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
        return dangerType;
    }
    public void setDangertype(String dangertype) {
        this.dangerType = dangertype;
    }
    public ZonedDateTime getDate() {
        return sent;
    }
    public void setDate(ZonedDateTime sent) {
        this.sent = sent;
    }

    public JSONObject getJSONObject() {
        return obj;
    }
}
