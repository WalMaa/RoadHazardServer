package com.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

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
        String database = "jdbc:sqlite:sqlite-tools-win32-x86-3410000" + dbName;
        dbConnection = DriverManager.getConnection(database);

        if (dbConnection != null) {
            String createUserTable = "CREATE TABLE users (username varchar(50) NOT NULL PRIMARY KEY, password varchar(128) NOT NULL, email varchar(50) NOT NULL, nickname varchar(50))";
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createUserTable);
            createStatement.close();

            String createMsgTable = "CREATE TABLE messages (nickname varchar(50) NOT NULL, dangertype varchar(50) NOT NULL, latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL, sent INTEGER NOT NULL)";
            createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createMsgTable);
            createStatement.close();
            log.info("Database successfully created");

            return true;
        }

        log.error("Database creation failed");
        return false;
    }

    public void setMessage(WarningMessage message) throws SQLException {
        PreparedStatement preparedStatement = null;
        String setMessageString = "INSERT INTO messages (nickname, dangertype, latitude, longitude, sent)" +
                "VALUES (?, ?, ?, ?, ?)";
        preparedStatement = dbConnection.prepareStatement(setMessageString);

        preparedStatement.setString(1, message.getNickName());
        preparedStatement.setString(2, message.getDangertype());
        preparedStatement.setDouble(3, message.getLatitude());
        preparedStatement.setDouble(4, message.getLongitude());
        preparedStatement.setLong(5, message.dateAsInt());

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public JSONArray getMessages() throws SQLException {
        Statement queryStatement = null;
        JSONArray messages = new JSONArray();
        String getMessagesString = "SELECT nickname, dangertype, latitude, longitude, sent FROM messages";

        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getMessagesString);

        while (rs.next()) {
            JSONObject message = new JSONObject();
            message.put("nickname", rs.getString("nickname"));
            message.put("dangertype", rs.getString("dangertype"));
            message.put("latitude", rs.getDouble("latitude"));
            message.put("longitude", rs.getDouble("longitude"));

            long epochTime = rs.getLong("sent");
            ZonedDateTime sent = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC);
            message.put(("sent"), sent.toString());
            System.out.println(sent.toString());
            messages.put(message);
        }

        return messages;
    }

    // returns true if user was added succesfully
    public void addUser(JSONObject obj) throws SQLException {

        // using preparedStatement for SQL injection safety
        PreparedStatement preparedStatement = null;
        String userInsert = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        preparedStatement = dbConnection.prepareStatement(userInsert);
        preparedStatement.setString(1, obj.getString("username"));
        preparedStatement.setString(2, obj.getString("password"));
        preparedStatement.setString(3, obj.getString("email"));
        preparedStatement.executeUpdate();
        preparedStatement.close();
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
        rs = queryStatement.executeQuery();

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
        rs = queryStatement.executeQuery();

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
