package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.lang.StringBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandleWarnings implements HttpHandler {
    
    private StringBuilder textDump = new StringBuilder("Dumped text: ");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            InputStream stream = exchange.getRequestBody();
            String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
            .collect(Collectors.joining("\n"));
            textDump.append(text);
            stream.close();
            String responseString = "OK";
            byte[] bytes = responseString.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseString.getBytes());

            outputStream.flush();
            outputStream.close();
            
            
        } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            String responseString;
            if (textDump.length() <= "Dumped text: ".length()) {
                responseString = "No messages";
            } else {
                responseString = textDump.toString();
            }

            byte[] bytes = responseString.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseString.getBytes());

            outputStream.flush();
            outputStream.close();
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