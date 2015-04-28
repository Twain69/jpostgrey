package com.flegler.jpostgrey;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

import javax.naming.ConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.flegler.jpostgrey.enums.RunMode;
import com.flegler.jpostgrey.model.WhiteListEntry;

public class JPostGrey {
	private static final Logger LOG = Logger.getLogger(JPostGrey.class);

	// TODO Add programatic configured log4j syslog appender

	@SuppressWarnings("resource")
	public static void main(String[] args) throws ConfigurationException {

		RunMode mode = parseArguments(args);
		switch (mode) {
		case DAEMON:
			// default mode ... nothing to be done here, just go on
			break;
		case ADD_WHITELIST:
			Settings.getInstance()
					.getDataFetcherInstance()
					.addEntryToWhitelist(
							Settings.getInstance().getAddDelWhiteListEntry());
			System.exit(0);
		case DEL_WHITELIST:
			Settings.getInstance()
					.getDataFetcherInstance()
					.removeEntryFromWhitelist(
							Settings.getInstance().getAddDelWhiteListEntry()
									.getPattern());
			System.exit(0);
		case LIST_WHITELIST:
			printWhitelistEntries(Settings.getInstance()
					.getDataFetcherInstance().getAllWhitelistEntries());
			System.exit(0);
		}
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
			System.exit(-1);
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

	private static RunMode parseArguments(String[] args) {

		Options options = new Options();
		options.addOption("h", "help", false, "Print this description");
		options.addOption("c", "configFile", true, "Config file");
		options.addOption("p", "pidfile", true, "pid file");
		options.addOption(
				"r",
				"runmode",
				true,
				"Run Mode (daemon, add [add whitelist entry], del [del whitelist entry], list [list all whitelist entries])");
		options.addOption(
				"m",
				"pattern",
				true,
				"When adding a new or deleting an existing whitelist entry, this is the pattern (e.g. flegler.com)");
		options.addOption(
				"k",
				"comment",
				true,
				"When adding a new whitelist entry, this is the comment (e.g. 'tired of waiting for mail to arrive')");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Error while parsing the arguments ("
					+ e.getLocalizedMessage() + ")");
			System.err.println(e);
			System.exit(-1);
		}

		Boolean help = cmd.hasOption("h");
		if (help) {
			printHelp(options);
		}

		String configFile = cmd.getOptionValue("c");
		if (configFile == null) {
			configFile = "/etc/jpostgrey.conf";
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

		String runmode = cmd.getOptionValue("r");
		if (runmode == null) {
			System.err.println("You have to provide a pidfile");
			System.err.println();
			printHelp(options);
		}

		String pattern = cmd.getOptionValue("m");

		String comment = cmd.getOptionValue("k");

		switch (runmode.toUpperCase()) {
		case "DAEMON":
			return RunMode.DAEMON;
		case "ADD":
			if (StringUtils.isEmpty(pattern) || StringUtils.isEmpty(comment)) {
				System.err
						.println("You have to provide a pattern and comment to add a new entry");
				printHelp(options);
				System.exit(-1);
			}
			settings.setAddDelWhiteListEntry(new WhiteListEntry(pattern,
					comment));
			return RunMode.ADD_WHITELIST;
		case "DEL":
			if (StringUtils.isEmpty(pattern)) {
				System.err
						.println("You have to provide a pattern to delete an existing entry");
				printHelp(options);
				System.exit(-1);
			}
			settings.setAddDelWhiteListEntry(new WhiteListEntry(pattern, null));
			return RunMode.DEL_WHITELIST;
		case "LIST":
			return RunMode.LIST_WHITELIST;
		default:
			System.err
					.println("Not supported or empty run mode. Only daemon, add, del and list are allowed.");
			System.exit(-1);
			// this return statement should never be executed ... wondering why
			// the java compiler does not recognize ???
			return null;
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

	private static void printWhitelistEntries(List<WhiteListEntry> entries) {
		System.out.println("Number of entries found: " + entries.size());
		int count = 1;
		for (WhiteListEntry entry : entries) {
			System.out.print(String.format(
					"%-3d: Pattern: %s%n     Comment: %s", count++,
					entry.getPattern(), entry.getComment()));

		}
	}
}