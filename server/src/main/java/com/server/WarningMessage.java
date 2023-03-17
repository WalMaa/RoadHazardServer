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
    private String dangerType;
    private String areacode;
    private String phonenumber;

    private long epochTime;
    private String jsonDateString;
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
        areacode = obj.optString("areacode");
        phonenumber = obj.optString("phonenumber");

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
        obj.put("areacode", areacode);
        obj.put("phonenumber", phonenumber);
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

    public JSONObject getJSONObject() {
        return obj;
    }

    public ZonedDateTime getSent() {
        return sent;
    }

    public void setSent(ZonedDateTime sent) {
        this.sent = sent;
    }

    public String getDangerType() {
        return dangerType;
    }

    public void setDangerType(String dangerType) {
        this.dangerType = dangerType;
    }

    public String getAreacode() {
        return areacode;
    }

    public void setAreacode(String areacode) {
        this.areacode = areacode;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public long getEpochTime() {
        return epochTime;
    }

    public void setEpochTime(long epochTime) {
        this.epochTime = epochTime;
    }

    public JSONObject getObj() {
        return obj;
    }

    public void setObj(JSONObject obj) {
        this.obj = obj;
    }
}
