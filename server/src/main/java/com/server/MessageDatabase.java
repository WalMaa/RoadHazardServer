package com.server;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.time.ZoneOffset;

import org.apache.commons.codec.digest.Crypt;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.SecureRandom;

public class MessageDatabase {
    private Connection dbConnection = null;
    private static MessageDatabase dbInstance = null;
    private static final Logger log = LoggerFactory.getLogger(MessageDatabase.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    ;

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
            String createUserTable = "CREATE TABLE users (username varchar(50) NOT NULL PRIMARY KEY, password varchar(128) NOT NULL, email varchar(50) NOT NULL)";
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createUserTable);
            createStatement.close();

            String createMsgTable = "CREATE TABLE messages (nickname varchar(50) NOT NULL, latitude DOUBLE NOT NULL, longitude DOUBLE NOT NULL, sent INTEGER NOT NULL, dangertype varchar(50) NOT NULL, areacode varchar(4), phonenumber varchar(10))";
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
        String setMessageString = "INSERT INTO messages (nickname, latitude, longitude, sent, dangertype, areacode, phonenumber)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        preparedStatement = dbConnection.prepareStatement(setMessageString);
        preparedStatement.setString(1, message.getNickName());
        preparedStatement.setDouble(2, message.getLatitude());
        preparedStatement.setDouble(3, message.getLongitude());
        preparedStatement.setLong(4, message.dateAsInt());
        preparedStatement.setString(5, message.getDangertype());
        if (!message.getAreacode().isEmpty() && message.getPhonenumber() != null) {
            preparedStatement.setString(6, message.getAreacode());
            preparedStatement.setString(7, message.getPhonenumber());
        }
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public JSONArray getMessages() throws SQLException {
        Statement queryStatement = null;
        JSONArray messages = new JSONArray();
        String getMessagesString = "SELECT nickname, latitude, longitude, sent, dangertype, areacode, phonenumber FROM messages";

        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getMessagesString);

        while (rs.next()) {
            JSONObject message = new JSONObject();
            long epochTime = rs.getLong("sent");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC);
            date.format(formatter);

            message.put("nickname", rs.getString("nickname"));
            message.put("latitude", rs.getDouble("latitude"));
            message.put("longitude", rs.getDouble("longitude"));
            message.put(("sent"), date.toString());
            message.put("dangertype", rs.getString("dangertype"));
            try {
                if (!rs.getString("areacode").isEmpty() && rs.getString("phonenumber") != null) {
                    System.out.println(rs.getString("areacode"));
                    message.put("areacode", rs.getString("areacode"));
                    message.put("phonenumber", rs.getString("phonenumber"));
                }
            } catch (Exception e) {
                log.error("Phone number data not available", e);
            } 
            System.out.println(message);
            messages.put(message);
        }
        return messages;
    }

    // returns true if user was added succesfully
    public void addUser(JSONObject obj) throws SQLException {
        
        // hashing the provided password
        byte bytes[] = new byte[13];
        secureRandom.nextBytes(bytes);
        String saltBytes = new String(Base64.getEncoder().encode(bytes));
        String salt = "$6$" + saltBytes;
        String hashedPassWord = Crypt.crypt(obj.getString("password"), salt);
        // using preparedStatement for SQL injection safety
        PreparedStatement preparedStatement = null;
        String userInsert = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        log.info("Adding user.");

        preparedStatement = dbConnection.prepareStatement(userInsert);
        preparedStatement.setString(1, obj.getString("username"));
        preparedStatement.setString(2, hashedPassWord);
        preparedStatement.setString(3, obj.getString("email"));
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    // returns true if user exists
    public boolean checkIfUserExists(String username) throws SQLException {
        PreparedStatement queryStatement = null;
        ResultSet rs;
        String userQuery = "SELECT username FROM users WHERE username = ?";

        log.info("Checking user.");

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
        PreparedStatement queryStatement = null;
        ResultSet rs;
        String userQuery = "";
        boolean isAuthenticated = false;

        log.info("Authenticating user.");

        // using prepared statement for SQL injection safety
        userQuery = "SELECT password FROM users WHERE username = ?";
        queryStatement = dbConnection.prepareStatement(userQuery);
        queryStatement.setString(1, username);
        rs = queryStatement.executeQuery();

        String hashedPassWord = rs.getString("password");
        Crypt.crypt(password, hashedPassWord);

        if (hashedPassWord.equals(Crypt.crypt(password, hashedPassWord))) {
            isAuthenticated = true;
            return isAuthenticated;
        }

        return isAuthenticated;
    }
}
