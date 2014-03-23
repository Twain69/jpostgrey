package com.flegler.jpostgrey;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
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
		writePid();

		Util util = new Util();
		LOG.info("jPostgrey started. Version: " + util.getVersion());


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
		options.addOption("p", "pidfile", true, "pid file");

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

		String pidfile = cmd.getOptionValue("p");
		if (pidfile == null) {
			System.err.println("You have to provide a pidfile");
			System.err.println();
			printHelp(options);
		}

		Settings settings = Settings.getInstance();

		try {
			settings.setConfigFile(new File(configFile));
			settings.readConfig();
			settings.setPidFileName(pidfile);
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

	private static void writePid() {
		FileWriter fw = null;
		try {
			Integer pid = new Integer(ManagementFactory.getRuntimeMXBean()
					.getName().split("@")[0]);
			File pidFile = new File(Settings.getInstance().getPidFileName());
			if (!pidFile.exists()) {
				pidFile.createNewFile();
			}
			if (pidFile.canWrite()) {
				fw = new FileWriter(pidFile);
				fw.write(pid.toString());
			} else {
				throw new IOException(String.format(
						"Can not write to pidFile '%s'", Settings.getInstance()
								.getPidFileName()));
			}
		} catch (NumberFormatException e) {
			System.err.println("Could not determine the process id");
			System.exit(1);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
	}
}