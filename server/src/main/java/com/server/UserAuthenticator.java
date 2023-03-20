package com.server;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Base64;

import javax.naming.AuthenticationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpExchange;

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

    // fetches the usename from the Authorization header
    public String getUsername(HttpExchange exchange) throws AuthenticationException {
        String authHeader =  exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), Charset.forName("UTF-8"));
            final String[] values = credentials.split(":", 2);
            final String username = values[0];
            return username;
        } else {
            throw new AuthenticationException();
        }
    }
} 
