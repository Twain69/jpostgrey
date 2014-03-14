package com.flegler.jpostgrey.dataFetcher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.flegler.jpostgrey.InputRecord;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;
import com.flegler.jpostgrey.interfaces.DataFetcher;

/**
 * Database creation:
 *****************************************************************************
 * -- Table: greylist
 * 
 * CREATE TABLE greylist
 * (
 *   clientaddress character varying(15),
 *   sender character varying(255),
 *   recipient character varying(255),
 *   connectcount bigint,
 *   firstconnect timestamp without time zone,
 *   lastconnect timestamp without time zone
 * )
 * WITH (
 *   OIDS=FALSE
 * );
 * ALTER TABLE greylist
 *   OWNER TO jpostgrey;
 * 
 * -- Index: idx
 * 
 * -- DROP INDEX idx;
 * 
 * CREATE INDEX idx
 *   ON greylist
 *   USING btree
 *   (clientaddress, sender, recipient);
 ******************************************************************************
 * 
 * @author oxmox
 *
 */
public class DatabaseFetcher implements DataFetcher {

	private static final Logger LOG = Logger.getLogger(DatabaseFetcher.class);

	private Connection connection;

	private final String type;
	private final String host;
	private final String dbName;
	private final String user;
	private final String password;

	public DatabaseFetcher(String type, String host, String dbName,
			String user, String password) {
		if (!type.equals("postgresql") && !type.equals("mysql")) {
			String errorMessage = "Wrong parameter provided for database type. "
					+ "Only 'postgresql' and 'mysql' is allowed.";
			LOG.error(errorMessage);
			System.err.println(errorMessage);
			System.exit(1);
		}

		this.type = type;
		this.host = host;
		this.dbName = dbName;
		this.user = user;
		this.password = password;

		try {
			connection = this.connect();
			LOG.info("Connection to database succeeded.");
		} catch (SQLException e) {
			LOG.error("An error occured during initialization of the database connection: "
					+ e.getLocalizedMessage());
			System.exit(1);
		}
	}

	@Override
	public int getDuration(InputRecord inputRecord)
			throws InputRecordNotFoundException {
		if (!checkWhiteList(inputRecord)) {
			try {
				getGreylistedData(inputRecord);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return 5;
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

	private boolean checkWhiteList(InputRecord inputRecord) {
		// TODO implementation missing
		return false;
	}

	private ResultSet getGreylistedData(InputRecord inputRecord)
			throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = connection
					.prepareStatement("SELECT firstconnect, lastconnect, connectcount FROM greylist WHERE clientaddres = ? AND sender = ? AND recipient = ?");
			stmt.setString(1, inputRecord.getClientAddress().getHostAddress());
			stmt.setString(2, inputRecord.getSender());
			stmt.setString(3, inputRecord.getRecipient());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ResultSet resultSet = stmt.executeQuery();
		return resultSet;
	}

}