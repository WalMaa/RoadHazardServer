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
        int code = 200;
        
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            InputStream inStream = exchange.getRequestBody();
            String message = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            inStream.close();
            System.out.println(message.toString());
            
            log.info("Entered POST.");
            try {
                obj = new JSONObject(message);
            } catch (JSONException e) {
                log.error("Error creating JSONObject.", e);
                e.printStackTrace();
            }

            WarningMessage warning = new WarningMessage(obj);
            warningList.add(warning);
            log.info("Warning added");
            String responseString = "Warning added.";

            log.info("Writing POST response.");
            byte[] bytes = responseString.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseString.getBytes());
            outputStream.flush();
            outputStream.close();
            
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
                    System.out.println(responsemessages);
                    log.info("Writing GET response.");
                    byte[] bytes = responsemessages.toString().getBytes("UTF-8");
                    exchange.sendResponseHeaders(code, bytes.length);

                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(responsemessages.toString().getBytes());

                    outputStream.flush();
                    outputStream.close();
                }




        } else {
            String responseString = "Not supported";
            byte[] bytes = responseString.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
    
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseString.getBytes());
    
            outputStream.flush();
            outputStream.close();
        }
    }
}