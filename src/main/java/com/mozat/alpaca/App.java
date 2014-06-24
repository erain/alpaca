package com.mozat.alpaca;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 * 
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static final App INSTANCE = new App();

    public static App getInstance() {
        return INSTANCE;
    }

    public static Configuration config;

    private App() {
        try {
            config = new PropertiesConfiguration("alpaca.properties");
        } catch (ConfigurationException e) {
            logger.error("Error when reading config: {}", e.getMessage());
        }
    }

	public static void main(String[] args) {
		System.out.println(App.config.getString("zookeeper.hosts"));
	}
}
