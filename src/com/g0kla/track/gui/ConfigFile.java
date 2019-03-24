package com.g0kla.track.gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

public class ConfigFile {
	public Properties properties; // Java properties file for user defined values
	public String propertiesFileName = "";
	MainWindow mainWindow;

	public ConfigFile(MainWindow win, String name) {
		mainWindow = win;
		properties = new Properties();
		propertiesFileName = name;
		init();
	}
	
	public void init() {
		load();
	}
	
	public void set(String key, String value) {
		properties.setProperty(key, value);
		//store();
	}
	
	public String get(String key) {
		return properties.getProperty(key);
	}
	
	public void set(String sat, String fieldName, String key, String value) {
		properties.setProperty(sat + fieldName + key, value);
		//store();
	}
	
	public String get(String sat, String fieldName, String key) {
		return properties.getProperty(sat + fieldName + key);
	}
	
	public void set(String key, int value) {
		properties.setProperty(key, Integer.toString(value));
		//store();
	}

	public void set(String sat, String fieldName, String key, int value) {
		properties.setProperty(sat +  fieldName + key, Integer.toString(value));
		//store();
	}

	public void set(String sat, String fieldName, String key, long value) {
		properties.setProperty(sat +  fieldName + key, Long.toString(value));
		//store();
	}
	public void set(String key, boolean value) {
		properties.setProperty(key, Boolean.toString(value));
		//store();
	}
	public void set(String sat, String fieldName, String key, boolean value) {
		properties.setProperty(sat +  fieldName + key, Boolean.toString(value));
		//store();
	}
	
	public int getInt(String key) {
		try {
			return Integer.parseInt(properties.getProperty(key));
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	public int getInt(String sat, String fieldName, String key) {
		try {
			return Integer.parseInt(properties.getProperty(sat +  fieldName + key));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public long getLong(String sat, String fieldName, String key) {
		try {
			return Long.parseLong(properties.getProperty(sat +  fieldName + key));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public boolean getBoolean(String key) {
		try {
			return Boolean.parseBoolean(properties.getProperty(key));
		} catch (NumberFormatException e) {
			return false;
		}
	}
	public boolean getBoolean(String sat, String fieldName, String key) {
		try {
			return Boolean.parseBoolean(properties.getProperty(sat +  fieldName + key));
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public void save() {
		try {
			FileOutputStream fos = new FileOutputStream(propertiesFileName);
			properties.store(fos, "KLA Track Ground Station Properties");
			fos.close();
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(mainWindow,"Could not write properties file. Check permissions on directory or on the file\n"+e1,"ERROR", JOptionPane.ERROR_MESSAGE) ;
			e1.printStackTrace();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(mainWindow,"Could not write properties file. Check permissions on directory or on the file\n"+e1,"ERROR", JOptionPane.ERROR_MESSAGE) ;
			e1.printStackTrace();
		}

	}
	
	public void load() {
		// try to load the properties from a file
		try {
			FileInputStream fis = new FileInputStream(propertiesFileName);
			properties.load(fis);
			fis.close();
		} catch (IOException e) {
			save();
		}
	}
	
	private String getProperty(String key) {
		String value = properties.getProperty(key);
		if (value == null) throw new NullPointerException();
		return value;
	}
	

}
