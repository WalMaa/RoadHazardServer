package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandleWarnings implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(HandleWarnings.class);
    private ArrayList<WarningMessage> warningList = new ArrayList<>();
    UserAuthenticator userAuthenticator;
    
    public HandleWarnings(UserAuthenticator uAuth) {
        userAuthenticator = uAuth;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject obj = null;
        Headers headers = exchange.getRequestHeaders();
        String responseString = "";
        String contentType = "";
        int code = 200;
        
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            log.info("Entered POST.");
            if (headers.containsKey("Content-Type")) {
                contentType = headers.get("Content-Type").get(0);
                log.info("Content-Type available.");

                if (contentType.equalsIgnoreCase("application/json")) {
                    log.info("Content-type is application/json.");       
                    
                    InputStream inStream = exchange.getRequestBody();
                    String message = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                    inStream.close();
                    System.out.println(message.toString());
                    
                    try {
                        obj = new JSONObject(message);
                    } catch (JSONException e) {
                        log.error("Error creating JSONObject.", e);
                        e.printStackTrace();
                    }
                    userAuthenticator.checkCredentials(obj.optString("username"), obj.getString("password"));
                    WarningMessage warning = new WarningMessage(obj);
                    warningList.add(warning);
                    log.info("Warning added");
                    responseString = "Warning added.";
        
                    log.info("Writing POST response.");
                    byte[] bytes = responseString.getBytes("UTF-8");
                    exchange.sendResponseHeaders(code, bytes.length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(responseString.getBytes());
                    outputStream.flush();
                    outputStream.close();
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
            if (warningList.isEmpty()) {
                code = 204;
                } else {
                    JSONArray responsemessages = new JSONArray();
                    for (WarningMessage message : warningList) {
                        JSONObject warningJSON = message.getJSONObject();
                        responsemessages.put(warningJSON);
                    }

                    log.info("Writing GET response.");
                    byte[] bytes = responsemessages.toString().getBytes("UTF-8");
                    exchange.sendResponseHeaders(code, bytes.length);

                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(responsemessages.toString().getBytes());

                    outputStream.flush();
                    outputStream.close();
                }

        } else {
            responseString = "Not supported";
            byte[] bytes = responseString.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
    
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseString.getBytes());
    
            outputStream.flush();
            outputStream.close();
        }
    }
}
