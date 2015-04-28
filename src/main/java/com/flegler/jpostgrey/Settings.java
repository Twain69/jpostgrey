package com.flegler.jpostgrey;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.flegler.jpostgrey.interfaces.DataFetcher;
import com.flegler.jpostgrey.model.WhiteListEntry;

public class Settings {

	private static final Logger LOG = Logger.getLogger(Settings.class);

	private static Settings instance;
	private Integer port;
	private InetAddress bindAddress;
	private Long configFileLastModified;
	private File configFile;

	private String dataClassName;
	private Class<DataFetcher> dataClass;
	private DataFetcher dataFetcherInstance;

	private Integer greylistingTime;

	private String dataFetcherDBType;
	private String dataFetcherDBHost;
	private String dataFetcherDBName;
	private String dataFetcherDBUser;
	private String dataFetcherDBPassword;
	
	private String redisHost;
	private Integer redisPort;

	private String pidFileName;

	private WhiteListEntry addDelWhiteListEntry;

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
					+ this.configFile.getAbsolutePath());
		}

		// only set this at the first settings load run. this can not be changed
		// during the running process
		if (this.getPort() == null && this.getBindAddress() == null) {
			try {
				this.setPort(new Integer(bundle.getString(
						"application.main.port").trim()));
				setBindAddress(InetAddress.getByName(bundle
						.getString("application.main.address")));

			} catch (NumberFormatException e) {
				String errorMessage = "Wrong input for application.main.port: '"
						+ bundle.getString("application.main.port") + "'";
				LOG.error(errorMessage);
				System.err.println(errorMessage);
				System.exit(1);
			} catch (UnknownHostException e) {
				String errorMessage = "Wrong input for application.main.address: '"
						+ bundle.getString("application.main.address") + "'";
				LOG.error(errorMessage);
				System.err.println(errorMessage);
				System.exit(1);
			}
		}

		try {
			setGreylistingTime(new Integer(bundle.getString(
					"application.greylisting.time").trim()));
		} catch (NumberFormatException e) {
			LOG.error("Wrong input for application.greylisting.time: '"
					+ bundle.getString("application.greylisting.time") + "'");
		}

		try {
			this.dataFetcherDBType = bundle
					.getString("application.datafetcher.database.type");
			this.dataFetcherDBHost = bundle
					.getString("application.datafetcher.database.host");
			this.dataFetcherDBUser = bundle
					.getString("application.datafetcher.database.user");
			this.dataFetcherDBPassword = bundle
					.getString("application.datafetcher.database.password");
			this.dataFetcherDBName = bundle
					.getString("application.datafetcher.database.name");
		} catch (MissingResourceException e) {
			if (bundle.getString("application.datafetcher.type").equals(
					"com.flegler.jpostgrey.dataFetcher.DatabaseFetcher")) {
				String errorMessage = "There is some configuration missing: "
						+ e.getLocalizedMessage();
				LOG.error(errorMessage);
				System.err.println(errorMessage);
				System.exit(1);
			}
		}
		
		try {
			this.redisHost = bundle.getString("application.datafetcher.redis.host");
			this.redisPort = Integer.valueOf(bundle.getString("application.datafetcher.redis.port"));
		} catch(NumberFormatException e) {
			LOG.error("RedisPort in wrong format");
			LOG.error(e);
			System.exit(1);
		} catch(MissingResourceException e) {
			if (bundle.getString("application.datafetcher.type").equals(
					"com.flegler.jpostgrey.dataFetcher.RedisFetcher")) {
				String errorMessage = "There is some configuration missing: "
						+ e.getLocalizedMessage();
				LOG.error(errorMessage);
				System.err.println(errorMessage);
				System.exit(1);
			}
		}

		// this has always to be called at the very end of this method.
		// otherwise some information could be missing
		this.setDataClass(bundle.getString("application.datafetcher.type")
				.trim());

	}

	public Integer getPort() {
		return port;
	}

	private void setPort(Integer port) {
		LOG.info("Setting port to '" + port + "'.");
		this.port = port;
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
				Constructor<DataFetcher> constructor = dataClass
						.getConstructor();
				this.dataFetcherInstance = constructor.newInstance();
				Method method = dataClass.getMethod("setUp", Settings.class);
				method.invoke(this.dataFetcherInstance, Settings.getInstance());
			} catch (ClassNotFoundException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException
					| InstantiationException e) {
				if (getDataFetcherInstance() != null) {
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

	public void setBindAddress(InetAddress bindAddress) {
		LOG.info("Setting bindAddress to '" + bindAddress.getHostAddress()
				+ "'.");
		this.bindAddress = bindAddress;
	}

	public InetAddress getBindAddress() {
		return bindAddress;
	}

	public DataFetcher getDataFetcherInstance() {
		return dataFetcherInstance;
	}

	public String getDataFetcherDBType() {
		return dataFetcherDBType;
	}

	public String getDataFetcherDBHost() {
		return dataFetcherDBHost;
	}

	public String getDataFetcherDBUser() {
		return dataFetcherDBUser;
	}

	public String getDataFetcherDBPassword() {
		return dataFetcherDBPassword;
	}

	public String getDataFetcherDBName() {
		return dataFetcherDBName;
	}

	public void setPidFileName(String pidFileName) {
		this.pidFileName = pidFileName;
	}

	public String getPidFileName() {
		return pidFileName;
	}
	
	public String getRedisHost() {
		return this.redisHost;
	}
	
	public Integer getRedisPort() {
		return this.redisPort;
	}

	public WhiteListEntry getAddDelWhiteListEntry() {
		return addDelWhiteListEntry;
	}

	public void setAddDelWhiteListEntry(WhiteListEntry addDelWhiteListEntry) {
		this.addDelWhiteListEntry = addDelWhiteListEntry;
	}

}
