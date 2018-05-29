package com.flegler.jpostgrey;

import com.flegler.jpostgrey.interfaces.Conf;
import com.flegler.jpostgrey.interfaces.DataFetcher;
import lombok.Getter;
import org.aeonbits.owner.ConfigFactory;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Getter
public enum Settings {
    INSTANCE;

    private Logger LOG;
    private Class<DataFetcher> dataClass;
    private DataFetcher dataFetcherInstance;
    private Conf config = ConfigFactory.create(Conf.class);

    Settings() {
        LOG = Logger.getLogger(Settings.class);
        loadDataClass();
    }

    @SuppressWarnings("unchecked")
    public void loadDataClass() {
        try {
            LOG.debug("Setting data class to " + config.dataClassName());
            this.dataClass = (Class<DataFetcher>) Class.forName(config.dataClassName());
            Constructor<DataFetcher> constructor = dataClass.getConstructor();
            this.dataFetcherInstance = constructor.newInstance();
            Method method = dataClass.getMethod("setUp");
            method.invoke(this.dataFetcherInstance);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException | InstantiationException e) {
            if (getDataFetcherInstance() != null) {
                LOG.error("Could not load data class. Ignoring configuration change!", e);
            } else {
                LOG.error("Could not load data class " + config.dataClassName(), e);
                System.exit(-1);
            }
        }
    }
}
