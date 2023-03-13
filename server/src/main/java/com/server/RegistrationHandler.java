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

    RegistrationHandler(UserAuthenticator uAuth) {
        userAuthenticator = uAuth;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        Headers headers = exchange.getRequestHeaders();
        String contentType = "";
        String responseString = "";
        int code = 200;
        JSONObject obj = null;
        MessageDatabase db = MessageDatabase.getInstance();

        try {

            InputStream inStream = exchange.getRequestBody();
            String newUser = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            inStream.close();

            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

                if (headers.containsKey("Content-Type")) {
                    contentType = headers.get("Content-Type").get(0);
                    log.info("Content type available");
                } else {
                    code = 411;
                    responseString = "No content type in request";
                    log.info("No content type available");
                }
                if (contentType.equalsIgnoreCase("application/json")) {
                    log.info("Content type is application/json");
                    if (newUser == null || newUser.length() == 0) {

                        code = 412;
                        responseString = "no user credentials";

                    } else {

                        try {
                            obj = new JSONObject(newUser);
                        } catch (JSONException e) {
                            log.error("Error creating JSONObject", e);
                        }
                        if (obj.getString("username").length() == 0 || obj.getString("password").length() == 0) {
                            code = 413;
                            responseString = "no proper user credentials";
                        } else {
                            log.info("registering user " + obj.getString("username") + " " + obj.getString("password"));
                            Boolean result = db.checkIfUserExists(obj.getString("username"));
                            if (result == false) {
                                code = 405;
                                responseString = "user already exist";
                            } else {
                                code = 200;
                                responseString = "User registered";
                            }
                        }
                    }

                } else {
                    code = 407;
                    responseString = "content type is not application/json";
                }
            } else {
                code = 401;
                responseString = "Not supported";
            }

            log.info("Writing response");
            byte[] bytes = responseString.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);

            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseString.getBytes());

            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            code = 500;
            responseString = "Could not handle request.";
            log.error(responseString, e);
            byte[] bytes = responseString.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);

            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseString.getBytes());

            outputStream.flush();
            outputStream.close();

            throw new JSONException(responseString);
        }
    }
}