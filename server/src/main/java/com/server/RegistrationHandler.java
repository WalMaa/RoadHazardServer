package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegistrationHandler implements HttpHandler {
    UserAuthenticator userAuthenticator;

    RegistrationHandler(UserAuthenticator uAuth) {
        userAuthenticator = uAuth;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            InputStream stream = exchange.getRequestBody();
            String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
            if (text.startsWith(":")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String[] credentialArray = text.split(":");
            
            // addUser checks whether the user exists is the credentialArray; returning
            // false if it exists
            if (userAuthenticator.addUser(credentialArray[0], credentialArray[1]) && (credentialArray[0].length() > 0) && credentialArray[1].length() > 0) {
                exchange.sendResponseHeaders(200, -1);

            } else {
                String responseString = "User already exists";
                byte[] bytes = responseString.getBytes("UTF-8");
                exchange.sendResponseHeaders(403, bytes.length);
                
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(responseString.getBytes());
                outputStream.flush();
                outputStream.close();
            }
            stream.close();
            
        } else {
            String responseString = "Not supported";
            byte[] bytes = responseString.getBytes("UTF-8");
            exchange.sendResponseHeaders(400, bytes.length);

            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseString.getBytes());
            outputStream.flush();
            outputStream.close();
        }

    }
}