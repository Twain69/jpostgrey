package com.flegler.jpostgrey.dataFetcher;

import com.flegler.jpostgrey.Settings;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;
import com.flegler.jpostgrey.interfaces.DataFetcher;
import com.flegler.jpostgrey.model.InputRecord;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.GregorianCalendar;
import java.util.Properties;

/**
 * @author Oliver Flegler
 */
public class DatabaseDataFetcher implements DataFetcher {

    private static final Logger LOG = Logger.getLogger(DatabaseDataFetcher.class);

    private Connection connection;

    private String type;
    private String host;
    private String dbName;
    private String user;
    private String password;

    public DatabaseDataFetcher() {
        LOG.info("Instantiating new databasefetcher");
    }

    @Override
    public FetcherResult getResult(InputRecord inputRecord) throws InputRecordNotFoundException {
        FetcherResult result = new FetcherResult();
        try {
            if (isInWhiteList(inputRecord)) {
                result.setWhitelisted(true);
                return result;
            } else {
                result.setFirstConnect(getGreylistedData(inputRecord));
                return result;
            }
        } catch (SQLException e) {
            throw new InputRecordNotFoundException("Record not found");
        }
    }

    private Connection connect() throws SQLException {
        // set up connection to database
        StringBuffer sb = new StringBuffer();
        sb.append("jdbc:").append(type).append("://").append(host).append("/").append(dbName);
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        return DriverManager.getConnection(sb.toString(), props);
    }

    private boolean isInWhiteList(InputRecord inputRecord) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT pattern, comment FROM whitelist");
        ResultSet result = stmt.executeQuery();
        if (result != null) {
            while (result.next()) {
                String pattern = ".*" + result.getString("pattern") + ".*";

                if (inputRecord.getRecipient().matches(pattern) || inputRecord.getSender().matches(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Long getGreylistedData(InputRecord inputRecord) throws SQLException, InputRecordNotFoundException {
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT firstconnect FROM greylist WHERE clientaddress = ? AND sender = ? AND recipient = ?");
        stmt.setString(1, inputRecord.getClientAddress().getHostAddress());
        stmt.setString(2, inputRecord.getSender().toLowerCase());
        stmt.setString(3, inputRecord.getRecipient().toLowerCase());
        ResultSet result = stmt.executeQuery();

        boolean recordFound = false;

        if (result != null) {
            while (result.next()) {
                recordFound = true;
                Timestamp firstConnect = result.getTimestamp("firstconnect");

                updateRecordInDatabase(inputRecord);

                return firstConnect.getTime();
            }
        }

        if (!recordFound) {
            insertInputRecord(inputRecord);
            throw new InputRecordNotFoundException();
        }

        return 0L;
    }

    private void insertInputRecord(InputRecord inputRecord) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO greylist (firstconnect, lastconnect, connectcount, clientaddress, sender, recipient) VALUES (?, ?, ?, ?, ?, ?)");
        Timestamp now = new Timestamp(new GregorianCalendar().getTimeInMillis());
        stmt.setTimestamp(1, now);
        stmt.setTimestamp(2, now);
        stmt.setInt(3, 1);
        stmt.setString(4, inputRecord.getClientAddress().getHostAddress());
        stmt.setString(5, inputRecord.getSender().toLowerCase());
        stmt.setString(6, inputRecord.getRecipient().toLowerCase());
        stmt.executeUpdate();
    }

    private void updateRecordInDatabase(InputRecord inputRecord) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "UPDATE greylist SET lastconnect = ?, connectcount = connectcount+1 WHERE clientaddress = ? AND sender = ? AND recipient = ?");
        Timestamp now = new Timestamp(new GregorianCalendar().getTimeInMillis());
        stmt.setTimestamp(1, now);
        stmt.setString(2, inputRecord.getClientAddress().getHostAddress());
        stmt.setString(3, inputRecord.getSender().toLowerCase());
        stmt.setString(4, inputRecord.getRecipient().toLowerCase());
        stmt.executeUpdate();
    }

    @Override
    public void setUp(Settings settings) {
        LOG.info("Setting up databasefetcher");
        if (!settings.getConfig().dataFetcherDBType().equals("postgresql")
                && !settings.getConfig().dataFetcherDBType().equals("mysql")) {
            String errorMessage = "Wrong parameter provided for database type. "
                    + "Only 'postgresql' and 'mysql' is allowed.";
            LOG.error(errorMessage);
            System.err.println(errorMessage);
            System.exit(1);
        }

        type = settings.getConfig().dataFetcherDBType();
        host = settings.getConfig().dataFetcherDBHost();
        dbName = settings.getConfig().dataFetcherDBName();
        user = settings.getConfig().dataFetcherDBUser();
        password = settings.getConfig().dataFetcherDBPassword();

        try {
            connection = connect();
            LOG.info("Connection to database succeeded.");
        } catch (SQLException e) {
            LOG.error("An error occured during initialization of the database connection: " + e.getLocalizedMessage());
            System.exit(1);
        }
    }
}