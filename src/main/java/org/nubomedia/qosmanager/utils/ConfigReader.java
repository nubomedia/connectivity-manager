package org.nubomedia.qosmanager.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by maa on 27.11.15.
 */
public class ConfigReader {

    private static Logger logger = LoggerFactory.getLogger(ConfigReader.class);

    public static Properties readProperties() throws IOException {
        Properties properties = new Properties();
        File prop = new File("/etc/qosmanager/qos.properties");

        if(!prop.exists()){
            logger.info("file not found using local one");
            properties.load(ConfigReader.class.getResourceAsStream("/qos.properties"));
        }
        else{
            logger.info("file found using etc local");
            properties.load(new FileInputStream(prop));
        }
        logger.info("properties loaded " + properties);

        return properties;
    }

}
