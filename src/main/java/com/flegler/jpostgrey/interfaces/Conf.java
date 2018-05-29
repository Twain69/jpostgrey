package com.flegler.jpostgrey.interfaces;

import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;

@HotReload(2)
@Sources({"file:" + Conf.DEFAULT_CONFIG_FILE})
public interface Conf extends Mutable {
    String DEFAULT_CONFIG_FILE = "/etc/jpostgrey/jpostgrey.conf";

    @Key("application.main.port")
    @DefaultValue("9989")
    Integer port();

    @Key("application.main.address")
    @DefaultValue("127.0.0.1")
    String bindAddress();

    @Key("application.datafetcher.type")
    @DefaultValue("com.flegler.jpostgrey.dataFetcher.DatabaseDataFetcher")
    String dataClassName();

    @Key("application.greylisting.time")
    @DefaultValue("120")
    Integer greylistingTime();

    @Key("application.datafetcher.database.type")
    String dataFetcherDBType();

    @Key("application.datafetcher.database.host")
    String dataFetcherDBHost();

    @Key("application.datafetcher.database.name")
    String dataFetcherDBName();

    @Key("application.datafetcher.database.user")
    String dataFetcherDBUser();

    @Key("application.datafetcher.database.password")
    String dataFetcherDBPassword();

    @Key("application.datafetcher.redis.host")
    String redisHost();

    @Key("application.datafetcher.redis.port")
    Integer redisPort();

}
