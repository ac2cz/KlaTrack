package com.g0kla.track.gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

/**
 * 
 * @author g0kla@arrl.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
