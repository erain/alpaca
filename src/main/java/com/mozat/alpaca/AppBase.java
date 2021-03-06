package com.mozat.alpaca;

import java.io.IOException;
import java.util.Properties;

public class AppBase {

	private static final AppBase INSTANCE = new AppBase();

	public static AppBase getInstance() {
		return INSTANCE;
	}

	public static Properties properties;

	private AppBase() {
		properties = new Properties();
		try {
			properties.load(AppBase.class
					.getResourceAsStream("/alpaca.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
