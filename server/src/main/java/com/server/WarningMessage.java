package com.server;

import org.json.JSONObject;

public class WarningMessage {

    private String nick;
    private String latitude;
    private String longitude;
    private String dangertype;

    private JSONObject obj = new JSONObject();

    public WarningMessage(JSONObject obj) {
        nick = obj.getString("nickname");
        longitude = obj.getString("longitude");
        latitude = obj.getString("latitude");
        dangertype = obj.getString("dangertype");

        this.obj = obj;
        obj.put("nickname", nick);
        obj.put("longitude", longitude);
        obj.put("latitude", latitude);
        obj.put("dangertype", dangertype);
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
    public String getLatitude() {
        return latitude;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public String getLongitude() {
        return longitude;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    public String getDangertype() {
        return dangertype;
    }
    public void setDangertype(String dangertype) {
        this.dangertype = dangertype;
    }
}
