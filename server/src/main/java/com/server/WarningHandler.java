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

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int RESPONSE_OK = 200;
    private static final int RESPONSE_UNSUPPORTED_MEDIA_TYPE = 415;
    private static final int RESPONSE_INTERNAL_SERVER_ERROR = 500;
    private static final int RESPONSE_NO_CONTENT_TYPE = 411;
    private static final int RESPONSE_NOT_ALLOWED = 405;
    private static final String MESSAGE_TYPE_MOOSE = "Moose";
    private static final String MESSAGE_TYPE_REINDEER = "Reindeer";
    private static final String MESSAGE_TYPE_DEER = "Deer";
    private static final String MESSAGE_TYPE_OTHER = "Other";

    public WarningHandler(UserAuthenticator uAuth) {
        userAuthenticator = uAuth;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        JSONObject obj = null;
        final Headers headers = exchange.getRequestHeaders();
        String responseString = "";
        String contentType = "";
        int code = RESPONSE_OK;

        log.info("Request handled in thread " + Thread.currentThread().getId());
        try {

            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                log.info("Entered POST.");

                if (headers.containsKey("Content-Type")) {
                    contentType = headers.get("Content-Type").get(0);
                    log.info("Content-Type available.");

                    if (contentType.equalsIgnoreCase(CONTENT_TYPE_JSON)) {
                        log.info("Content-type is " + CONTENT_TYPE_JSON);

                        InputStream inStream = exchange.getRequestBody();
                        String message = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        inStream.close();
                        try {
                            obj = new JSONObject(message);
                        } catch (JSONException e) {
                            log.error("JSONException" + e, e);
                        }
                        if (obj.has("query")) {
                            queryJSONChecker(obj);
                            responseString = fetchWarningByNick(obj);
                        } else if (obj.has("id")) {
                            String username = userAuthenticator.getUsername(exchange);
                            JSONObject updatedMessage = db.updateMessage(obj, username);
                            responseString = updatedMessage.toString();
                        } else {

                            JSONChecker(obj);
                            WarningMessage warning = new WarningMessage(obj);
                            try {
                                db.setMessage(warning, userAuthenticator.getUsername(exchange));
                                log.info("Warning added");
                                responseString = "Warning added:" + warning.getJSONObject();
                            } catch (SQLException e) {
                                log.error("SQLException", e);
                                responseString = "Could not add message.";
                            }
                        }
                        log.info("Writing POST response.");
                    } else {
                        log.error("Content-Type is not " + CONTENT_TYPE_JSON);
                        code = RESPONSE_UNSUPPORTED_MEDIA_TYPE;
                        responseString = "Content type is not " + CONTENT_TYPE_JSON;
                    }
                } else {
                    log.error("No content type.");
                    code = RESPONSE_NO_CONTENT_TYPE;
                    responseString = "No content type in request.";
                }

            } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                log.info("Entered GET.");
                JSONArray responsemessages = new JSONArray();
                responsemessages = db.getMessages();
                responseString = responsemessages.toString();
                log.info("Writing GET response.");
            } else {
                log.error("Other than GET error.");
                code = RESPONSE_NOT_ALLOWED;
                responseString = "Not supported";
            }
        } catch (Exception e) {
            log.error("Error: " + e, e);
            code = RESPONSE_INTERNAL_SERVER_ERROR;
            responseString = "Could not handle request.";
        } finally {
            log.info("Writing response");
            writeResponse(exchange, code, responseString);
        }
    }

    private void queryJSONChecker(JSONObject obj) {

        if (!obj.has("query")) {
            throw new JSONException("Query type must be specified.");
        }

        if (!obj.get("query").equals("user")) {
            throw new JSONException("Only userquery supported.");
        }

        if (!obj.has("nickname")) {
            throw new JSONException("Message must include nickname.");
        }

    }

    private String fetchWarningByNick(JSONObject obj) throws SQLException {
        String responseString;
        JSONArray responsemessages = new JSONArray();
        responsemessages = db.queryByNickName(obj.getString("nickname"));
        log.info("Warnings by user: " + obj.getString("nickname") + "fetched.");
        return responseString = responsemessages.toString();
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
            case MESSAGE_TYPE_MOOSE:
                break;
            case MESSAGE_TYPE_REINDEER:
                break;
            case MESSAGE_TYPE_DEER:
                break;
            case MESSAGE_TYPE_OTHER:

            default:
                throw new JSONException("Dangertype not supported");
        }
        // XOR gate checking phone number and area code
        if (obj.has("areacode") || obj.has("phonenumber")) {
            if ((obj.optString("areacode") != null && obj.optString("phonenumber").isEmpty())
                    || (obj.optString("areacode") == null && !obj.optString("phonenumber").isEmpty())) {
                throw new JSONException("Areacode must be accompanied by a phone number and vice versa");
            }
        }
    }

    public void writeResponse(HttpExchange exchange, int code, String responseString) throws IOException {
        byte[] bytes = responseString.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);

        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseString.getBytes());

        outputStream.flush();
        outputStream.close();
    }
}
