package com.flegler.jpostgrey.dataFetcher;

import com.flegler.jpostgrey.Settings;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;
import com.flegler.jpostgrey.interfaces.DataFetcher;
import com.flegler.jpostgrey.model.InputRecord;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;

/**
 * <pre>
 * Redis bases backend fetcher. Data in redis is stored in maps.
 * Key: recipientMail
 * Field: senderMail|senderIp
 * Value: firstConnect|lastConnect (Long fields)
 * </pre>
 *
 * @author Oliver Flegler
 */
public class RedisDataFetcher implements DataFetcher {

    private static final Logger LOG = Logger.getLogger(RedisDataFetcher.class);
    private static final String REDISFIELD_SEPARATOR = "^";

    private Jedis jedis;

    @Override
    public FetcherResult getResult(InputRecord inputRecord) {
        FetcherResult result = new FetcherResult();
        try {

            result.setFirstConnect(getGreylistedData(inputRecord));
            return result;
        } catch (InputRecordNotFoundException | SQLException e) {
            result.setFirstConnect(0L);
            return result;
        }
    }

    @Override
    public void setUp() {
        if (StringUtils.isEmpty(Settings.INSTANCE.getConfig().redisHost())
                || Settings.INSTANCE.getConfig().redisPort() == null
                || Settings.INSTANCE.getConfig().redisPort() <= 0) {
            LOG.error("RedisFetcher not set up properly");
            System.exit(1);
        }
        jedis = new Jedis(Settings.INSTANCE.getConfig().redisHost(), Settings.INSTANCE.getConfig().redisPort());
    }

    private Long getGreylistedData(InputRecord inputRecord)
            throws SQLException, InputRecordNotFoundException {

        String key = inputRecord.getRecipient().toLowerCase();
        String field = inputRecord.getSender().toLowerCase()
                + REDISFIELD_SEPARATOR
                + inputRecord.getClientAddress().getHostAddress();
        String result = jedis.hget(key, field);

        if (!StringUtils.isEmpty(result)) {
            RedisFetcherJSON valueObject = new Gson().fromJson(result,
                    RedisFetcherJSON.class);
            updateRecordInDatabase(inputRecord);
            return Long.valueOf(valueObject.getFirstConnect());
        } else {
            insertInputRecord(inputRecord);
            throw new InputRecordNotFoundException();
        }

    }

    private void insertInputRecord(InputRecord inputRecord) throws SQLException {
        String key = inputRecord.getRecipient().toLowerCase();
        String field = inputRecord.getSender().toLowerCase()
                + REDISFIELD_SEPARATOR
                + inputRecord.getClientAddress().getHostAddress();

        RedisFetcherJSON valueObject = new RedisFetcherJSON();
        valueObject.setFirstConnect(System.currentTimeMillis());
        valueObject.setLastConnect(System.currentTimeMillis());

        jedis.hset(key, field, new Gson().toJson(valueObject));
    }

    private void updateRecordInDatabase(InputRecord inputRecord)
            throws SQLException {
        String result = jedis.hget(inputRecord.getRecipient().toLowerCase(),
                inputRecord.getSender().toLowerCase() + REDISFIELD_SEPARATOR
                        + inputRecord.getClientAddress().getHostAddress());

        RedisFetcherJSON valueObject = new Gson().fromJson(result,
                RedisFetcherJSON.class);
        valueObject.setLastConnect(System.currentTimeMillis());

        String key = inputRecord.getRecipient().toLowerCase();
        String field = inputRecord.getSender().toLowerCase()
                + REDISFIELD_SEPARATOR
                + inputRecord.getClientAddress().getHostAddress();

        jedis.hset(key, field, new Gson().toJson(valueObject));
    }

}
