package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegistrationHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(RegistrationHandler.class);
    UserAuthenticator userAuthenticator;
    MessageDatabase db = MessageDatabase.getInstance();

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final int RESPONSE_OK = 200;
    private static final int RESPONSE_UNSUPPORTED_MEDIA_TYPE = 415;
    private static final int RESPONSE_INTERNAL_SERVER_ERROR = 500;
    private static final int RESPONSE_NO_CONTENT_TYPE = 411;
    private static final int RESPONSE_CREDENTIALS_REQUIRED = 407;
    private static final int RESPONSE_UNAUTHORIZED = 401;
    private static final int RESPONSE_USER_EXISTS = 400;

    RegistrationHandler(UserAuthenticator uAuth) {
        userAuthenticator = uAuth;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        final Headers headers = exchange.getRequestHeaders();

        String contentType = "";
        String responseString = "";
        int code = RESPONSE_OK;
        JSONObject obj = null;

        log.info("Request handled in thread " + Thread.currentThread().getId());
        try {

            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

                if (headers.containsKey("Content-Type")) {
                    contentType = headers.get("Content-Type").get(0);
                    log.info("Content type available");
                    if (contentType.equalsIgnoreCase(CONTENT_TYPE_JSON)) {
                        log.info("Content type is " + CONTENT_TYPE_JSON);

                        InputStream inStream = exchange.getRequestBody();
                        String newUser = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        inStream.close();
                        if (newUser == null || newUser.length() == 0) {

                            code = RESPONSE_CREDENTIALS_REQUIRED;
                            responseString = "no user credentials.";

                        } else {
                            // start of JSON handling
                            try {
                                obj = new JSONObject(newUser);
                            } catch (JSONException e) {
                                log.error("JSONException. ", e);
                            }
                            if (obj.getString("username").length() == 0 || obj.getString("password").length() == 0) {
                                code = RESPONSE_CREDENTIALS_REQUIRED;
                                responseString = "no proper user credentials.";
                            } else {
                                log.info("registering user " + obj.getString("username") + " "
                                        + obj.getString("password"));
                                Boolean result = db.checkIfUserExists(obj.getString("username"));
                                if (result == true) {
                                    code = RESPONSE_USER_EXISTS;
                                    responseString = "user already exists.";
                                } else {
                                    code = RESPONSE_OK;
                                    db.addUser(obj);
                                    responseString = "User registered.";
                                }
                            }
                        }

                    } else {
                        code = RESPONSE_UNSUPPORTED_MEDIA_TYPE;
                        responseString = "content type is not " + CONTENT_TYPE_JSON;
                    }
                } else {
                    code = RESPONSE_NO_CONTENT_TYPE;
                    responseString = "No content type in request.";
                    log.info("No content type available");
                }
            } else {
                code = RESPONSE_UNAUTHORIZED;
                responseString = "Not supported.";
            }

        } catch (Exception e) {
            log.error(responseString, e);
            code = RESPONSE_INTERNAL_SERVER_ERROR;
            responseString = "Could not handle request.";
        } finally {
            log.info("Writing response");
            writeResponse(exchange, code, responseString);
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