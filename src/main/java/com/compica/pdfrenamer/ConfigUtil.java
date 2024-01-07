package com.compica.pdfrenamer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;


public class ConfigUtil {
    public Config getConfig() {
        Logger logger = Logger.getLogger(ConfigUtil.class.getName());
        //add slf4j logger        

        // Create a Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        // Get the JSON file from the resources directory
        
        File configFile = new File(System.getProperty("user.dir"), "config.json");

        // Parse the JSON content
        Config config;
        try {
            config = mapper.readValue(configFile, Config.class);
        } catch (IOException e) {
            logger.info("Config file not found, creating new one " + e.getMessage());
            return null;
        }

        return config;
    }

    public static void saveConfig(Config config) {
        // Create a Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        // Convert the config object to JSON string
        String json;
        try {
            json = mapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }

        // Write the JSON string to the config file
        try (FileWriter writer = new FileWriter("config.json")) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
