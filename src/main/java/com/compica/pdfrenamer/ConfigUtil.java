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
        Logger logger = Logger.getLogger(ConfigUtil.class.getName());

        // Create a Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        // Convert the config object to JSON string
        String json;
        try {
            //writerWithDefaultPrettyPrinter() is used to format the JSON output
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
        } catch (JsonProcessingException e) {
            logger.info(new LogMessage("Error converting config to JSON", e.getMessage()).toString());
            return;
        }
//make sure that the json file is pretty. I would like each key to be on a new line
        try (FileWriter writer = new FileWriter("config.json")) {
            writer.write(json);
        } catch (IOException e) {
            logger.info(new LogMessage("Error writing config file", e.getMessage()).toString());        }
    }

}
