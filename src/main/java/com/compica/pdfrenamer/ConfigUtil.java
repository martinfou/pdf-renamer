package com.compica.pdfrenamer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;


public class ConfigUtil {
    public static Config getConfig() {
        // Create a Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        // Get the JSON file from the resources directory
        InputStream jsonStream = ConfigUtil.class.getResourceAsStream("/config.json");

        // Parse the JSON content
        Config config;
        try {
            config = mapper.readValue(jsonStream, Config.class);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return config;
    }
}
