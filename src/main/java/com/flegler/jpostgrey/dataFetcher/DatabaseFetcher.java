package com.flegler.jpostgrey.dataFetcher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.flegler.jpostgrey.InputRecord;
import com.flegler.jpostgrey.Settings;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;
import com.flegler.jpostgrey.interfaces.DataFetcher;

/**
 * Database creation:
 ***************************************************************************** 
 * -- Table: greylist
 * 
 * CREATE TABLE greylist ( clientaddress character varying(15), sender character
 * varying(255), recipient character varying(255), connectcount bigint,
 * firstconnect timestamp without time zone, lastconnect timestamp without time
 * zone ) WITH ( OIDS=FALSE ); ALTER TABLE greylist OWNER TO jpostgrey;
 * 
 * -- Index: idx
 * 
 * -- DROP INDEX idx;
 * 
 * CREATE INDEX idx ON greylist USING btree (clientaddress, sender, recipient);
 ****************************************************************************** 
 * 
 * @author oxmox
 * 
 */
public class DatabaseFetcher implements DataFetcher {

	private static final Logger LOG = Logger.getLogger(DatabaseFetcher.class);

	private Connection connection;

	private String type;
	private String host;
	private String dbName;
	private String user;
	private String password;

	public DatabaseFetcher() {
	}

	@Override
	public Timestamp getTimestamp(InputRecord inputRecord)
			throws InputRecordNotFoundException {
		try {
			if (!isInWhiteList(inputRecord)) {
				return getGreylistedData(inputRecord);
			} else {
				return new Timestamp(0L);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new InputRecordNotFoundException();
	}

	private Connection connect() throws SQLException {
		// set up connection to database
		StringBuffer sb = new StringBuffer();
		sb.append("jdbc:").append(type).append("://").append(host).append("/")
				.append(dbName);
		Properties props = new Properties();
		props.setProperty("user", user);
		props.setProperty("password", password);
		return DriverManager.getConnection(sb.toString(), props);
	}

	private boolean isInWhiteList(InputRecord inputRecord) throws SQLException {
		// TODO implementation missing
		PreparedStatement stmt = connection
				.prepareStatement("SELECT pattern, comment FROM whitelist");
		ResultSet result = stmt.executeQuery();
		if (result != null) {
			while (result.next()) {
				String pattern = result.getString("pattern");
				String comment = result.getString("comment");

				if (pattern.matches(".*" + inputRecord.getRecipient() + ".*")
						|| pattern.matches(".*" + inputRecord.getSender()
								+ ".*")) {
					LOG.info("Whitelist found for pattern '" + pattern
							+ "' and comment '" + comment + "'");
					return true;
				}
			}
		}
		return false;
	}

	private Timestamp getGreylistedData(InputRecord inputRecord)
			throws SQLException, InputRecordNotFoundException {
		PreparedStatement stmt = connection
				.prepareStatement("SELECT firstconnect FROM greylist WHERE clientaddress = ? AND sender = ? AND recipient = ?");
		stmt.setString(1, inputRecord.getClientAddress().getHostAddress());
		stmt.setString(2, inputRecord.getSender());
		stmt.setString(3, inputRecord.getRecipient());
		ResultSet result = stmt.executeQuery();

		boolean recordFound = false;

		if (result != null) {
			while (result.next()) {
				recordFound = true;
				Timestamp firstConnect = result.getTimestamp("firstconnect");

				updateRecordInDatabase(inputRecord);

				return firstConnect;
			}
		}

		if (!recordFound) {
			insertInputRecord(inputRecord);
			throw new InputRecordNotFoundException();
		}

		throw new InputRecordNotFoundException();
	}

	private void insertInputRecord(InputRecord inputRecord) throws SQLException {
		PreparedStatement stmt = connection
				.prepareStatement("INSERT INTO greylist (firstconnect, lastconnect, connectcount, clientaddress, sender, recipient) VALUES (?, ?, ?, ?, ?, ?)");
		Timestamp now = new Timestamp(new GregorianCalendar().getTimeInMillis());
		stmt.setTimestamp(1, now);
		stmt.setTimestamp(2, now);
		stmt.setInt(3, 1);
		stmt.setString(4, inputRecord.getClientAddress().getHostAddress());
		stmt.setString(5, inputRecord.getSender());
		stmt.setString(6, inputRecord.getRecipient());
		stmt.executeUpdate();
	}

	private void updateRecordInDatabase(InputRecord inputRecord)
			throws SQLException {
		PreparedStatement stmt = connection
				.prepareStatement("UPDATE greylist SET lastconnect = ?, connectcount = connectcount+1 WHERE clientaddress = ? AND sender = ? AND recipient = ?");
		Timestamp now = new Timestamp(new GregorianCalendar().getTimeInMillis());
		stmt.setTimestamp(1, now);
		stmt.setString(2, inputRecord.getClientAddress().getHostAddress());
		stmt.setString(3, inputRecord.getSender());
		stmt.setString(4, inputRecord.getRecipient());
		stmt.executeUpdate();
	}

	@Override
	public void setUp(Settings settings) {
		if (!settings.getDataFetcherDBType().equals("postgresql")
				&& !settings.getDataFetcherDBType().equals("mysql")) {
			String errorMessage = "Wrong parameter provided for database type. "
					+ "Only 'postgresql' and 'mysql' is allowed.";
			LOG.error(errorMessage);
			System.err.println(errorMessage);
			System.exit(1);
		}

		type = settings.getDataFetcherDBType();
		host = settings.getDataFetcherDBHost();
		dbName = settings.getDataFetcherDBName();
		user = settings.getDataFetcherDBUser();
		password = settings.getDataFetcherDBPassword();

		try {
			connection = connect();
			LOG.info("Connection to database succeeded.");
		} catch (SQLException e) {
			LOG.error("An error occured during initialization of the database connection: "
					+ e.getLocalizedMessage());
			System.exit(1);
		}
	}
}