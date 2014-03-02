package com.flegler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

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

		ServerSocket inputSocket = null;
		try {
			inputSocket = new ServerSocket(port);
			LOG.info("Socket started successfully on port " + port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// instanciate the MemoryData store
		MemoryData.getInstance();

		while (true) {
			try {
				Socket connectionSocket = inputSocket.accept();
				InputThread input = new InputThread(connectionSocket);
				input.start();
			} catch (IOException e) {
				for (StackTraceElement element : e.getStackTrace()) {
					LOG.error(element.toString());
				}
				System.exit(-1);
			} catch (java.lang.OutOfMemoryError e) {
				for (StackTraceElement element : e.getStackTrace()) {
					LOG.error(element.toString());
				}
				System.gc();
			}
		}
	}
}