package com.server;

import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WeatherService {
    String responseText;
    
    
    
    public String getWeather(WarningMessage warningMessage) {
        String responseText = null;
        try {

            CloseableHttpClient client = HttpClients.createDefault();
            String url = "https://localhost:4001/weather";
            ContentType contentType = ContentType.create("application/xml", "UTF-8");

            double latitude = warningMessage.getLatitude();
            double longitude = warningMessage.getLongitude();
            String xml = "<coordinates> <latitude>" + latitude + "</latitude> <longitude>" + longitude + "</longitude> </coordinates>";

            ProcessBuilder builder = new ProcessBuilder(
                    "curl",
                    "-X",
                    "POST",
                    "-H",
                    "Content-Type: " + contentType,
                    "-d",
                    xml,
                    url
            );
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }
            
            // Wait for curl process to finish
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("curl exited with error code " + exitCode);
            }

            responseText = responseBuilder.toString().trim();
            System.out.println("API Response: " + responseText);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    return responseText;
    }
}