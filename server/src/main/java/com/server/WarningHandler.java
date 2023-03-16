package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class WarningHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(WarningHandler.class);
    UserAuthenticator userAuthenticator;
    MessageDatabase db = MessageDatabase.getInstance();

    public WarningHandler(UserAuthenticator uAuth) {
        userAuthenticator = uAuth;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject obj = null;
        Headers headers = exchange.getRequestHeaders();
        String responseString = "";
        String contentType = "";
        int code = 200;
        try {

            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                log.info("Entered POST.");

                if (headers.containsKey("Content-Type")) {
                    contentType = headers.get("Content-Type").get(0);
                    log.info("Content-Type available.");

                    if (contentType.equalsIgnoreCase("application/json")) {
                        log.info("Content-type is application/json.");
                        InputStream inStream = exchange.getRequestBody();
                        String message = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        inStream.close();
                        System.out.println(message.toString());

                        try {
                            obj = new JSONObject(message);
                        } catch (JSONException e) {
                            log.error("JSONException" + e, e);
                        }
                        JSONChecker(obj);
                        WarningMessage warning = new WarningMessage(obj);
                        try {
                            db.setMessage(warning);
                            log.info("Warning added");
                            responseString = "Warning added.";
                        } catch (SQLException e) {
                            log.error("SQLException", e);
                            responseString = "Could not add message.";
                        }

                        log.info("Writing POST response.");
                    } else {
                        log.info("Content-Type is not application/json.");
                        code = 415;
                        responseString = "Content type is not application/json";
                    }
                } else {
                    log.info("No content type.");
                    code = 411;
                    responseString = "No content type in request.";
                }
                
            } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                log.info("Entered GET.");
                JSONArray responsemessages = new JSONArray();
                responsemessages = db.getMessages();
                responseString = responsemessages.toString();
                System.out.println(responsemessages);
                log.info("Writing GET response.");
            } else {
                log.info("Other than GET error.");
                code = 405;
                responseString = "Not supported";
            }
        } catch (Exception e) {
            log.error("Error: " + e, e);
            
            code = 500;
            responseString = "Could not handle request.";
        } finally {
            writeResponse(exchange, code, responseString);
        }
    }

    public void JSONChecker(JSONObject obj) throws JSONException {

        if (!obj.has("nickname")) {
            throw new JSONException("Message must include nickname.");
        }

        if (!obj.has("latitude") && !obj.has("longitude")) {
            throw new JSONException("Message must include coordinates.");
        }

        double latitude = obj.getDouble("latitude");
        double longitude = obj.getDouble("longitude");

        // checks whether longitude and latitude are numbers
        if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
            throw new JSONException("Coordinates must be double.");
        }

        if (!obj.has("sent")) {
            throw new JSONException("Message must include date.");
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            LocalTime.parse(obj.getString("sent"), formatter);
        } catch (Exception e) {
            throw new JSONException("Wrong time format.");
        }

        if (!obj.has("dangertype")) {
            throw new JSONException("Dangertype is missing.");
        }

        switch (obj.getString("dangertype")) {
            case "Moose":
                break;
            case "Reindeer":
                break;
            case "Deer":
                break;
            default:
                throw new JSONException("Dangertype not supported");
        }
    }

    public void writeResponse(HttpExchange exchange, int code, String responseString) throws IOException {
        byte[] bytes = responseString.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(responseString.getBytes());
        stream.close();
    }
}
