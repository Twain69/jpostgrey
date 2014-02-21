package com.flegler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.flegler.InputRecord.InputRecordBuilder;

/**
 * Hello world!
 * 
 */
public class JPostGrey {
	private static final int port = 9989;

	private static final Logger LOG = Logger.getLogger(JPostGrey.class);

	// TODO add config file parser. For now these values will be fixed
	// TODO add configs in /etc/jpostgrey ... for now I need logging.conf and
	// jpostgrey.conf (for ports etc.)
	// TODO Add programatic configured log4j syslog appender
	// TODO Add periodic config file parser for logging (own thread)

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String clientSentence;
		ServerSocket welcomeSocket = null;
		try {
			welcomeSocket = new ServerSocket(port);
			LOG.info("Socket started");
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (true) {
			try {
				Socket connectionSocket = welcomeSocket.accept();
				LOG.info("New incoming connection");
				// TODO put every new connection to its own thread
				BufferedReader inFromClient = new BufferedReader(
						new InputStreamReader(connectionSocket.getInputStream()));
				InputRecordBuilder builder = new InputRecord.InputRecordBuilder();
				do {
					clientSentence = inFromClient.readLine();
					LOG.debug("Got message '" + clientSentence + "'");
					if (clientSentence != null && !clientSentence.isEmpty()) {
						builder.addRow(clientSentence);

					}
				} while (clientSentence != null && !clientSentence.isEmpty());
				InputRecord inputRecord = null;
				try {
					inputRecord = builder.build();
				} catch (BuilderNotCompleteException e) {
					LOG.info("Not all records received ... have to reject");
				}
				LOG.debug("Incoming message finished ... now I have to calculate my response");
				LOG.debug(inputRecord.toString());
				OutputStreamWriter out = new OutputStreamWriter(
						connectionSocket.getOutputStream());
				out.write(inputRecord.toString());
				out.flush();
				out.close();
				// action=greylist, reason=early-retry (19s missing),
				// client_name=idefix.flegler.com, client_address=10.200.10.20,
				// sender=oxmox@idefix.flegler.com,
				// recipient=oxmox@oxmox-nb.flegler.com
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}