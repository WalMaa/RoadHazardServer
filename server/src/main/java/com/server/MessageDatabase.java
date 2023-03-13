package com.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageDatabase {
    private Connection dbConnection = null;
    private static MessageDatabase dbInstance = null;
    private static final Logger log = LoggerFactory.getLogger(MessageDatabase.class);

    public static synchronized MessageDatabase getInstance() {
        if (null == dbInstance) {
            dbInstance = new MessageDatabase();
        }
        return dbInstance;
    }

    private MessageDatabase() {
        try {
            init();
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }

    private boolean init() throws SQLException {
        String dbName = "MyDatabase";
        String database = "jdbc:sqlite:C:\\sqlite\\sqlite-tools-win32-x86-3410000" + dbName;
        dbConnection = DriverManager.getConnection(database);

        if (dbConnection != null) {
            String createUserTable = "CREATE TABLE users (username varchar(50) NOT NULL, password varchar(128) NOT NULL, email varchar(50) NOT NULL)";
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createUserTable);
            createStatement.close();

            String createMsgTable = "CREATE TABLE data (username varchar(50) NOT NULL, usermessage varchar(500) NOT NULL)";
            createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createMsgTable);
            createStatement.close();
            log.info("Database successfully created");

            return true;
        }

        log.error("Database creation failed");
        return false;
    }

    public void setMessage(JSONObject message) throws SQLException {
        String setMessageString = "INSERT INTO data " + "VALUES('" + message.getString("message") + "')";
        Statement createStatement;
        createStatement = dbConnection.createStatement();
        createStatement.executeUpdate(setMessageString);
        createStatement.close();
    }

    public JSONArray getMessages() throws SQLException {
        Statement queryStatement = null;
        JSONArray messages = new JSONArray();
        String getMessagesString = "SELECT rowid, user, usermessage FROM data";

        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getMessagesString);

        while (rs.next()) {
            JSONObject message = new JSONObject();
            message.put("id", rs.getInt("rowid"));
            message.put("user", rs.getString("user"));
            message.put("usermessage", rs.getString("usermessage"));
            messages.put(message);
        }

        return messages;
    }

    // returns true if user was added succesfully
    public boolean addUser(JSONObject obj) throws SQLException {
        // if user exists the following method returns true;
        if (checkIfUserExists(obj.getString("username"))) {
            log.info("User already exists");
            return false;
        }

        // using preparedStatement for SQL injection safety
        PreparedStatement preparedStatement = null;
        String userInsert = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        preparedStatement = dbConnection.prepareStatement(userInsert);
        preparedStatement.setString(1, obj.getString("username"));
        preparedStatement.setString(2, obj.getString("password"));
        preparedStatement.setString(3, obj.getString("email"));
        preparedStatement.executeUpdate();
        preparedStatement.close();
        return true;
    }

    // returns true if user exists
    public boolean checkIfUserExists(String username) throws SQLException {
        log.info("Checking user.");
        PreparedStatement queryStatement = null;
        ResultSet rs;
        String userQuery = "SELECT username FROM users WHERE username = ?";

        // using preparedStatement for SQL injection safety
        queryStatement = dbConnection.prepareStatement(userQuery);
        queryStatement.setString(1, username);
        rs = queryStatement.executeQuery(userQuery);

        if (rs.next()) {
            // block entered if user exists
            queryStatement.close();
            return true;
        }
        queryStatement.close();
        return false;
    }

    // returns true if user is successfully authenticated
    public boolean authenticateUser(String username, String password) throws SQLException {
        log.info("Authenticating user.");
        PreparedStatement queryStatement = null;
        ResultSet rs;
        String userQuery = "SELECT username, password FROM users WHERE username = ?";

        // using prepared statement for SQL injection safety
        queryStatement = dbConnection.prepareStatement(userQuery);
        queryStatement.setString(1, username);
        rs = queryStatement.executeQuery(userQuery);

        boolean isAuthenticated = false;

        if (rs.next()) {
            String storedPassword = rs.getString("password");
            isAuthenticated = storedPassword.equals(password);
        }

        rs.close();
        queryStatement.close();
        return isAuthenticated;
    }
}
