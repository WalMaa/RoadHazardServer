package com.viikko1;

import com.sun.net.httpserver.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.lang.StringBuilder;

public class Server implements HttpHandler {

    StringBuilder textDump = new StringBuilder("Dumped text: ");

    private Server() {
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            InputStream stream = exchange.getRequestBody();
            String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.joining("\n"));
            textDump.append(text);
            exchange.sendResponseHeaders(200, -1);
            stream.close();

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

    public static void main(String[] args) throws Exception {
        // create the http server to port 8001 with default logger
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        // create context that defines path for the resource, in this case a "help"
        server.createContext("/warning", new Server());
        // creates a default executor
        server.setExecutor(null);
        server.start();
    }
}