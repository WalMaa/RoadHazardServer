package com.server;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(UserAuthenticator.class);
    private ArrayList<User> users = null;
    
    public UserAuthenticator(String realm) {
        super("warning");
        users = new ArrayList<User>();
    }

    //returns false if user already exists
    public boolean addUser(String userName, String password, String email) {
        for (User user : users) {
            if (user.getUsername().equals(userName)) {
                log.info("User already exists");
                return false;
            }
        }

        User registerUser = new User(userName, password, email);
        users.add(registerUser);
        log.info("User " + userName + " registered");
        return true;
    }

    //returns true if username and password correspond
    @Override
    public boolean checkCredentials(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                log.info("username and password correspond");
                return true;
            }
        }

        return false;
    }

} 
