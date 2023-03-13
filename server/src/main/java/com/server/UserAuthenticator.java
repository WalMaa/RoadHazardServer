package com.server;

import java.sql.SQLException;
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
    public boolean checkCredentials(String username, String password) {
        log.info("Checking credentials for user " + username);
        try {
            if (db.checkIfUserExists(username)) {
                if (db.authenticateUser(username, password)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
        return false;
    }
} 
