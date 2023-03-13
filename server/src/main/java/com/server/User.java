package com.server;

import org.json.JSONObject;

public class User {
    
    private String username;
    private String password;
    private String email;

    
    public User(JSONObject obj) {
        username = obj.getString("username");
        password = obj.getString("password");
        email = obj.getString("email");
    }


    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }


    public String getEmail() {
        return email;
    }

    
}
