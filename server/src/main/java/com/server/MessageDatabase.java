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
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.SecureRandom;

public class MessageDatabase {
    private Connection dbConnection = null;
    private static MessageDatabase dbInstance = null;
    private static final Logger log = LoggerFactory.getLogger(MessageDatabase.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

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
            String createUserTable = "CREATE TABLE users (" +
                    " username varchar(50) NOT NULL PRIMARY KEY," +
                    " password varchar(128) NOT NULL," +
                    " email varchar(50) NOT NULL);";

            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createUserTable);
            createStatement.close();

            String createMsgTable = "CREATE TABLE messages (" +
                    " nickname varchar(50) NOT NULL," +
                    " latitude DOUBLE NOT NULL," +
                    " longitude DOUBLE NOT NULL," +
                    " sent INTEGER NOT NULL," +
                    " dangertype varchar(50) NOT NULL," +
                    " areacode varchar(4)," +
                    " phonenumber varchar(10)," +
                    " updatereason varchar(100)," +
                    " modified INTEGER," +
                    " users_username varchar(50)," +
                    " FOREIGN KEY(users_username) REFERENCES users(username));";

            createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createMsgTable);
            createStatement.close();
            log.info("Database successfully created");

            return true;
        }

        log.error("Database creation failed");
        return false;
    }

    public void setMessage(WarningMessage message, String username) throws SQLException {
        PreparedStatement preparedStatement = null;
        String setMessageString = "INSERT INTO messages (nickname, latitude, longitude, sent, dangertype, areacode, phonenumber, users_username)"
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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
        preparedStatement.setString(8, username);

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public JSONArray getMessages() throws SQLException {
        Statement queryStatement = null;
        JSONArray messages = new JSONArray();
        String getMessagesString = "SELECT rowid,  nickname, latitude, longitude, sent, dangertype, areacode, phonenumber, modified, updatereason FROM messages";

        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getMessagesString);

        while (rs.next()) {
            JSONObject message = new JSONObject();
            long epochTime = rs.getLong("sent");
            ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC);
            date.format(formatter);

            message.put("id", rs.getInt("rowid"));
            message.put("nickname", rs.getString("nickname"));
            message.put("latitude", rs.getDouble("latitude"));
            message.put("longitude", rs.getDouble("longitude"));
            message.put(("sent"), date.toString());
            message.put("dangertype", rs.getString("dangertype"));
            // checking optional values
            try {
                if (rs.getString("areacode") != null && rs.getString("phonenumber") != null) {
                    message.put("areacode", rs.getString("areacode"));
                    message.put("phonenumber", rs.getString("phonenumber"));
                }
            } catch (SQLException | JSONException e) {
                log.error("Phone number data not available", e);
            }
            try {
                if (rs.getString("updatereason") != null && rs.getInt("modified") != 0) {
                    message.put("updatereason", rs.getString("updatereason"));
                    long modifiedEpochTime = rs.getInt("modified");
                    ZonedDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(modifiedEpochTime),
                            ZoneOffset.UTC);
                    String newDateString = modifiedDate.toString();
                    message.put("modified", newDateString);
                }
            } catch (SQLException | JSONException e) {
                log.error("No updates available", e);
            }
            messages.put(message);
        }
        return messages;
    }

    public JSONArray queryByNickName(String nickName) throws SQLException {
        JSONArray messages = new JSONArray();
        Statement queryStatement = null;
        String getMessagesString = "SELECT nickname, latitude, longitude, sent, dangertype, areacode, phonenumber FROM messages WHERE nickname = "
                + nickName;
        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getMessagesString);

        while (rs.next()) {
            JSONObject message = new JSONObject();
            long epochTime = rs.getLong("sent");
            ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC);
            date.format(formatter);

            message.put("nickname", rs.getString("nickname"));
            message.put("latitude", rs.getDouble("latitude"));
            message.put("longitude", rs.getDouble("longitude"));
            message.put(("sent"), date.toString());
            message.put("dangertype", rs.getString("dangertype"));
            try {
                if (rs.getString("areacode") != null && rs.getString("phonenumber") != null) {
                    message.put("areacode", rs.getString("areacode"));
                    message.put("phonenumber", rs.getString("phonenumber"));
                }
            } catch (SQLException | JSONException e) {
                log.error("Phone number data not available", e);
            }
            messages.put(message);
        }
        rs.close();
        return messages;
    }

    public JSONObject updateMessage(JSONObject obj, String username) throws SQLException {
        
        String updateStatement = "UPDATE messages" +
                " SET nickname = ?," +
                " longitude = ?," +
                " latitude = ?," +
                " modified = ?," +
                " dangertype = ?," +
                " areacode = ?," +
                " phonenumber = ?," +
                " updatereason = ?" +
                " WHERE rowid = ? AND users_username = ?";
        PreparedStatement statement = dbConnection.prepareStatement(updateStatement);
        if (obj.has("nickname")) {
            statement.setString(1, obj.getString("nickname"));
        }

        if (obj.has("longitude")) {
            statement.setDouble(2, obj.getDouble("longitude"));
        }

        if (obj.has("latitude")) {
            statement.setDouble(3, obj.getDouble("latitude"));
        }

        String newDateString = obj.getString("sent");
        ZonedDateTime newSent = ZonedDateTime.parse(newDateString);
        long newEpochMilli = newSent.toInstant().toEpochMilli();
        statement.setLong(4, newEpochMilli);

        if (obj.has("dangertype")) {
            statement.setString(5, obj.getString("dangertype"));
        }

        if (obj.has("areacode")) {
            statement.setString(6, obj.getString("areacode"));
        }

        if (obj.has("phonenumber")) {
            statement.setString(7, obj.getString("phonenumber"));
        }

        if (obj.has("updatereason")) {
            statement.setString(8, obj.getString("updatereason"));
        }

        statement.setInt(9, obj.getInt("id"));
        statement.setString(10, username);
        statement.executeUpdate();

        // returning updated warning
        String query = "SELECT rowid, nickname, longitude, latitude, sent, dangertype, areacode, phonenumber, updatereason, modified FROM messages WHERE rowid = ?";
        JSONObject updatedMessage = new JSONObject();
        statement = null;
        statement = dbConnection.prepareStatement(query);
        statement.setInt(1, obj.getInt("id"));
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            // converting epoch to date format
            long epochTime = Long.parseLong(rs.getString("sent"));
            ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC);
            String dateString = date.toString();

            updatedMessage.put("sent", dateString);
            updatedMessage.put("nickname", rs.getString("nickname"));
            updatedMessage.put("latitude", rs.getDouble("latitude"));
            updatedMessage.put("longitude", rs.getDouble("longitude"));
            updatedMessage.put("id", rs.getInt("rowid"));
            updatedMessage.put("dangertype", rs.getString("dangertype"));
            try {
                if (rs.getString("areacode") != null && rs.getString("phonenumber") != null) {
                    updatedMessage.put("areacode", rs.getString("areacode"));
                    updatedMessage.put("phonenumber", rs.getString("phonenumber"));
                }
            } catch (Exception e) {
                log.error("Phone number data not available", e);
            }

            epochTime = rs.getLong("modified");
            ZonedDateTime modifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC);
            modifiedDate.format(formatter);
            updatedMessage.put("modified", obj.getString("sent"));
            updatedMessage.put("updatereason", rs.getString("updatereason"));
        }
        rs.close();
        return updatedMessage;
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
        rs.close();
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

        rs.close();
        return isAuthenticated;
    }
}
