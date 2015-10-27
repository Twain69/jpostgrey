package com.flegler.jpostgrey;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aeonbits.owner.ConfigFactory;
import org.apache.log4j.Logger;

import com.flegler.jpostgrey.interfaces.Conf;
import com.flegler.jpostgrey.interfaces.DataFetcher;
import com.flegler.jpostgrey.model.WhiteListEntry;

import lombok.Getter;

@Getter
public enum Settings {
	INSTANCE;

	private Logger LOG;
	
	Settings() {
		LOG = Logger.getLogger(Settings.class);
		loadDataClass();
	}

	private Class<DataFetcher> dataClass;
	private DataFetcher dataFetcherInstance;

	private String pidFileName;

	private Conf config = ConfigFactory.create(Conf.class);

	private WhiteListEntry addDelWhiteListEntry;

	@SuppressWarnings("unchecked")
	public void loadDataClass() {
		try {
			LOG.debug("Setting data class to " + config.dataClassName());
			this.dataClass = (Class<DataFetcher>) Class.forName(config.dataClassName());
			Constructor<DataFetcher> constructor = dataClass.getConstructor();
			this.dataFetcherInstance = constructor.newInstance();
			Method method = dataClass.getMethod("setUp", Settings.class);
			method.invoke(this.dataFetcherInstance, Settings.INSTANCE);
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | InstantiationException e) {
			if (getDataFetcherInstance() != null) {
				LOG.error("Could not load data class. Ignoring configuration change!");
			} else {
				System.err.println("Could not load data class " + config.dataClassName());
				System.exit(-1);
			}
		}
	}

	public void setPidFileName(String pidFileName) {
		this.pidFileName = pidFileName;
	}

	public void setAddDelWhiteListEntry(WhiteListEntry addDelWhiteListEntry) {
		this.addDelWhiteListEntry = addDelWhiteListEntry;
	}

}
