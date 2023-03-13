package com.server;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

public class WarningMessage {

    private String nick;
    private double latitude;
    private double longitude;
    private ZonedDateTime sent;
    private String dangertype;
    private JSONObject obj = new JSONObject();

    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    String dateText = now.format(formatter);

    public WarningMessage(JSONObject obj) {
        nick = obj.getString("nickname");
        longitude = obj.getDouble("longitude");
        latitude = obj.getDouble("latitude");
        dangertype = obj.getString("dangertype");
        //converting from string JSON to ZonedDateTime
        String dateString = obj.getString("sent");
        sent = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME);


        this.obj = obj;
        obj.put("nickname", nick);
        obj.put("longitude", longitude);
        obj.put("latitude", latitude);
        obj.put("dangertype", dangertype);
        obj.put("sent", sent);
    }

    public JSONObject getJSONObject() {
        System.out.println("getJSONOBject: " + obj);
        return obj;
    }

    public String getNick() {
        return nick;
    }
    public void setNick(String nick) {
        this.nick = nick;
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
}
