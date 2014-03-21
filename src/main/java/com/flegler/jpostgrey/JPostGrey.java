package com.flegler.jpostgrey;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import javax.naming.ConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class JPostGrey {
	private static final Logger LOG = Logger.getLogger(JPostGrey.class);

	// TODO Add programatic configured log4j syslog appender

	@SuppressWarnings("resource")
	public static void main(String[] args) throws ConfigurationException {

		parseArguments(args);

		SettingsReloader settingsReloader = new SettingsReloader();
		settingsReloader.start();

		ServerSocket inputSocket = null;
		try {
			inputSocket = new ServerSocket(Settings.getInstance().getPort(), 0,
					Settings.getInstance().getBindAddress());
			LOG.info("Socket started successfully on port "
					+ Settings.getInstance().getPort());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		while (true) {
			try {
				Socket connectionSocket = inputSocket.accept();
				InputThread input = new InputThread(connectionSocket,
						UUID.randomUUID());
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

	private static void parseArguments(String[] args) {
		Options options = new Options();
		options.addOption("h", "help", false, "Print this description");
		options.addOption("c", "configFile", true, "Config file");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Error while parsing the arguments");
			System.exit(-1);
		}

		Boolean help = cmd.hasOption("h");
		if (help) {
			printHelp(options);
		}

		String configFile = cmd.getOptionValue("c");
		if (configFile == null) {
			System.err
					.println("You have to provide a config file parameter (-c)");
			System.err.println();
			printHelp(options);
		}

		Settings settings = Settings.getInstance();

		try {
			settings.setConfigFile(new File(configFile));
			settings.readConfig();
		} catch (NullPointerException e) {
			System.err.println("Error while parsing the config file");
			if (e.getMessage() != null) {
				System.err.println(e.getMessage());
			}
			System.exit(-1);
		}
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar jpostgrey.jar", options);
		System.exit(0);
	}
}