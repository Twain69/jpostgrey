package com.flegler.jpostgrey.dataFetcher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.flegler.jpostgrey.Settings;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;
import com.flegler.jpostgrey.interfaces.DataFetcher;
import com.flegler.jpostgrey.model.InputRecord;
import com.flegler.jpostgrey.model.WhiteListEntry;

/**
 * @author Oliver Flegler
 * 
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
	}

	@Override
	public FetcherResult getResult(InputRecord inputRecord)
			throws InputRecordNotFoundException {
		FetcherResult result = new FetcherResult();
		try {
			if (isInWhiteList(inputRecord)) {
				result.setWhitelisted(true);
				return result;
			} else {
				result.setFirstConnect(getGreylistedData(inputRecord));
				return result;
			}
		} catch (InputRecordNotFoundException | SQLException e) {
			result.setFirstConnect(0L);
			return result;
		}
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
		PreparedStatement stmt = connection
				.prepareStatement("SELECT pattern, comment FROM whitelist");
		ResultSet result = stmt.executeQuery();
		if (result != null) {
			while (result.next()) {
				String pattern = ".*" + result.getString("pattern") + ".*";

				if (inputRecord.getRecipient().matches(pattern)
						|| inputRecord.getSender().matches(pattern)) {
					return true;
				}
			}
		}
		return false;
	}

	private Long getGreylistedData(InputRecord inputRecord)
			throws SQLException, InputRecordNotFoundException {
		PreparedStatement stmt = connection
				.prepareStatement("SELECT firstconnect FROM greylist WHERE clientaddress = ? AND sender = ? AND recipient = ?");
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
		PreparedStatement stmt = connection
				.prepareStatement("INSERT INTO greylist (firstconnect, lastconnect, connectcount, clientaddress, sender, recipient) VALUES (?, ?, ?, ?, ?, ?)");
		Timestamp now = new Timestamp(new GregorianCalendar().getTimeInMillis());
		stmt.setTimestamp(1, now);
		stmt.setTimestamp(2, now);
		stmt.setInt(3, 1);
		stmt.setString(4, inputRecord.getClientAddress().getHostAddress());
		stmt.setString(5, inputRecord.getSender().toLowerCase());
		stmt.setString(6, inputRecord.getRecipient().toLowerCase());
		stmt.executeUpdate();
	}

	private void updateRecordInDatabase(InputRecord inputRecord)
			throws SQLException {
		PreparedStatement stmt = connection
				.prepareStatement("UPDATE greylist SET lastconnect = ?, connectcount = connectcount+1 WHERE clientaddress = ? AND sender = ? AND recipient = ?");
		Timestamp now = new Timestamp(new GregorianCalendar().getTimeInMillis());
		stmt.setTimestamp(1, now);
		stmt.setString(2, inputRecord.getClientAddress().getHostAddress());
		stmt.setString(3, inputRecord.getSender().toLowerCase());
		stmt.setString(4, inputRecord.getRecipient().toLowerCase());
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

	@Override
	public List<WhiteListEntry> getAllWhitelistEntries() {
		PreparedStatement stmt;
		List<WhiteListEntry> whiteListEntries = new ArrayList<WhiteListEntry>();
		try {
			stmt = connection
					.prepareStatement("SELECT pattern, comment FROM whitelist");
			ResultSet result = stmt.executeQuery();
			if (result != null) {
				while (result.next()) {
					WhiteListEntry entry = new WhiteListEntry(
							result.getString("pattern"),
							result.getString("comment"));
					whiteListEntries.add(entry);
				}
			}
		} catch (SQLException e) {
			System.err.println("Fetching whitelist entries did not succeed");
			System.err.println(e);
			return whiteListEntries;
		}
		return whiteListEntries;
	}

	@Override
	public void addEntryToWhitelist(WhiteListEntry whiteListEntry) {
		PreparedStatement stmt;
		try {
			stmt = connection
					.prepareStatement("INSERT INTO whitelist (pattern, comment) VALUES ?, ?");
			stmt.setString(1, whiteListEntry.getPattern());
			stmt.setString(2, whiteListEntry.getComment());
			int resultCount = stmt.executeUpdate();

			if (resultCount == PreparedStatement.EXECUTE_FAILED) {
				System.err.println("Error while inserting whitelist entry");
			} else {
				if (resultCount > 0) {
					System.out.println("Whitelist entry successfully inserted");
				} else {
					System.out
							.println("No entry inserted (maybe the entry already existed)");
				}
			}
		} catch (SQLException e) {
			System.err.println("Creating whitelist entry did not succeed");
			System.err.println(e);
		}
	}

	@Override
	public void removeEntryFromWhitelist(String pattern) {
		PreparedStatement stmt;
		try {
			stmt = connection
					.prepareStatement("DELETE FROM whitelist WHERE pattern LIKE '?'");
			stmt.setString(1, pattern);
			int resultCount = stmt.executeUpdate();

			if (resultCount == PreparedStatement.EXECUTE_FAILED) {
				System.err.println("Error while deleting whitelist entry");
			} else {
				if (resultCount > 0) {
					System.out.println(resultCount
							+ " entries deleted from whitelist");
				} else {
					System.out.println("No entry deleted");
				}
			}
		} catch (SQLException e) {
			System.err.println("Deleting whitelist entry did not succeed");
			System.err.println(e);
		}
	}
}