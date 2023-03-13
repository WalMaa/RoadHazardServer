package com.server;

import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(UserAuthenticator.class);
    private MessageDatabase db = null;
    
    public UserAuthenticator(String realm) {
        super("warning");
        db = MessageDatabase.getInstance();
    }

    //returns true if username and password correspond
    @Override
    public boolean checkCredentials(String username, String password) throws SQLException {
        if (db.checkIfUserExists(username)) {
            
        }

        return false;
    }

} 
