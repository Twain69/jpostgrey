package com.flegler.jpostgrey;

import interfaces.DataFetcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class Settings {

	private static final Logger LOG = Logger.getLogger(Settings.class);

	private static Settings instance;
	private Integer port;
	private Long configFileLastModified;
	private File configFile;
	private String dataClassName;
	private Class<DataFetcher> dataClass;
	private Integer greylistingTime;

	private Settings() {
	}

	public static Settings getInstance() {
		if (instance == null) {
			instance = new Settings();
		}

		return instance;
	}

	public void readConfig() {
		if (configFile == null) {
			LOG.error("Config file not set yet");
			return;
		}
		if (configFileLastModified != null
				&& configFileLastModified == configFile.lastModified()) {
			LOG.debug("Config file not changed. Skipping reload.");
			return;
		}

		configFileLastModified = configFile.lastModified();

		ResourceBundle bundle = null;
		try (FileInputStream fis = new FileInputStream(this.configFile)) {
			bundle = new PropertyResourceBundle(fis);
		} catch (IOException e) {
			LOG.error("Could not load config file '"
					+ this.configFile.getAbsolutePath() + "/"
					+ this.configFile.getName() + "'");
		}

		this.setDataClass(bundle.getString("application.greylisting.dataClass").trim());

		try {
			this.setPort(new Integer(bundle.getString("application.main.port")
					.trim()));
		} catch (NumberFormatException e) {
			LOG.error("Wrong input for application.main.port: '"
					+ bundle.getString("application.main.port") + "'");
		}
		
		try {
			setGreylistingTime(new Integer(bundle.getString("application.greylisting.time")
					.trim()));
		} catch (NumberFormatException e) {
			LOG.error("Wrong input for application.greylisting.time: '"
					+ bundle.getString("application.greylisting.time") + "'");
		}

	}

	public Integer getPort() {
		return port;
	}

	private void setPort(Integer port) {
		if (getPort() == null) {
			LOG.info("Setting port to '" + port + "'.");
			this.port = port;
		}
	}

	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}

	public Class<DataFetcher> getDataClass() {
		return this.dataClass;
	}

	@SuppressWarnings("unchecked")
	public void setDataClass(String dataClassName) {
		if (this.dataClassName == null
				|| !this.dataClassName.equals(dataClassName)) {

			this.dataClassName = dataClassName;

			try {
				LOG.debug("Setting data class to " + dataClassName);
				this.dataClass = (Class<DataFetcher>) Class
						.forName(dataClassName);
			} catch (ClassNotFoundException e) {
				if (getDataClass() != null) {
					LOG.error("Could not load data class. Ignoring configuration change!");
				} else {
					System.err.println("Could not load data class "
							+ dataClassName);
					System.exit(-1);
				}
			}

		} else {
			LOG.debug("DataClassName configuration not changed. Ignoring!");
		}
	}

	private void setGreylistingTime(Integer greylistingTime) {
		LOG.info("Setting greylistingTime to '" + greylistingTime + "' seconds");
		this.greylistingTime = greylistingTime;
	}

	public Integer getGreylistingTime() {
		return greylistingTime;
	}
}
