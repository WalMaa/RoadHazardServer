package com.server;

import java.util.Hashtable;
import java.util.Map;

public class UserAuthenticator extends com.sun.net.httpserver.BasicAuthenticator {
    private Map<String, String> users = null;
    
    public UserAuthenticator(String realm) {
        super("warning");
        users = new Hashtable<String, String>();
        users.put("dummy", "passwd");
    }

    public boolean addUser(String userName, String password) {
        if (users.containsKey(userName)) {
            //username already exists
            return false;
        } else {
            users.put(userName, password);
            return true;
        }
    }


    @Override
    public boolean checkCredentials(String username, String password) {
        if ( (username.contains(username)) && (password.contains(password)) ) {
            return true;
        }
        else {
            return false;
        }
    }

} 
