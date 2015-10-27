package com.flegler.jpostgrey;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

	public static final String DEFAULT_CONFIG_FILE = "/etc/jpostgrey.conf";

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
			Settings.INSTANCE.getDataFetcherInstance().addEntryToWhitelist(Settings.INSTANCE.getAddDelWhiteListEntry());
			System.exit(0);
		case DEL_WHITELIST:
			Settings.INSTANCE.getDataFetcherInstance()
					.removeEntryFromWhitelist(Settings.INSTANCE.getAddDelWhiteListEntry().getPattern());
			System.exit(0);
		case LIST_WHITELIST:
			printWhitelistEntries(Settings.INSTANCE.getDataFetcherInstance().getAllWhitelistEntries());
			System.exit(0);
		}
		writePid();

		Util util = new Util();
		LOG.info("jPostgrey started. Version: " + util.getVersion());

		ServerSocket inputSocket = null;
		try {
			inputSocket = new ServerSocket(Settings.INSTANCE.getConfig().port(), 0,
					Settings.INSTANCE.getConfig().bindAddress());
			LOG.info("Socket started successfully on port " + Settings.INSTANCE.getConfig().port());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		while (true) {
			try {
				Socket connectionSocket = inputSocket.accept();
				InputThread input = new InputThread(connectionSocket, UUID.randomUUID());
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
		options.addOption("p", "pidfile", true, "pid file");
		options.addOption("r", "runmode", true,
				"Run Mode (daemon, add [add whitelist entry], del [del whitelist entry], list [list all whitelist entries])");
		options.addOption("m", "pattern", true,
				"When adding a new or deleting an existing whitelist entry, this is the pattern (e.g. flegler.com)");
		options.addOption("k", "comment", true,
				"When adding a new whitelist entry, this is the comment (e.g. 'tired of waiting for mail to arrive')");

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Error while parsing the arguments (" + e.getLocalizedMessage() + ")");
			System.err.println(e);
			System.exit(-1);
		}

		Boolean help = cmd.hasOption("h");
		String pidfile = cmd.getOptionValue("p");
		String runmode = cmd.getOptionValue("r");
		String pattern = cmd.getOptionValue("m");
		String comment = cmd.getOptionValue("k");
		RunMode rm;

		if (help) {
			printHelp(options);
		}

		try {
			Settings.INSTANCE.setPidFileName(pidfile);
		} catch (NullPointerException e) {
			System.err.println("Error while parsing the config file");
			if (e.getMessage() != null) {
				System.err.println(e.getMessage());
			}
			System.exit(-1);
		}

		if (runmode == null) {
			System.err.println("You have to provide a pidfile");
			System.err.println();
			printHelp(options);
		}

		switch (runmode.toUpperCase()) {
		case "DAEMON":
			rm = RunMode.DAEMON;
			break;
		case "ADD":
			if (StringUtils.isEmpty(pattern) || StringUtils.isEmpty(comment)) {
				System.err.println("You have to provide a pattern and comment to add a new entry");
				printHelp(options);
				System.exit(-1);
			}
			Settings.INSTANCE.setAddDelWhiteListEntry(new WhiteListEntry(pattern, comment));
			rm = RunMode.ADD_WHITELIST;
			break;
		case "DEL":
			if (StringUtils.isEmpty(pattern)) {
				System.err.println("You have to provide a pattern to delete an existing entry");
				printHelp(options);
				System.exit(-1);
			}
			Settings.INSTANCE.setAddDelWhiteListEntry(new WhiteListEntry(pattern, null));
			rm = RunMode.DEL_WHITELIST;
			break;
		case "LIST":
			rm = RunMode.LIST_WHITELIST;
			break;
		default:
			System.err.println("Not supported or empty run mode. Only daemon, add, del and list are allowed.");
			System.exit(-1);
			// this return statement should never be executed ... wondering why
			// the java compiler does not recognize ???
			rm = null;
			break;
		}

		if (rm == RunMode.DAEMON && pidfile == null) {
			System.err.println("You have to provide a pidfile");
			System.err.println();
			printHelp(options);
		}

		return rm;
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar jpostgrey.jar", options);
		System.exit(0);
	}

	private static void writePid() {
		FileWriter fw = null;
		try {
			Integer pid = Util.getPid();
			File pidFile = new File(Settings.INSTANCE.getPidFileName());
			if (!pidFile.exists()) {
				pidFile.createNewFile();
			}
			if (pidFile.canWrite()) {
				fw = new FileWriter(pidFile);
				fw.write(pid.toString());
			} else {
				throw new IOException(
						String.format("Can not write to pidFile '%s'", Settings.INSTANCE.getPidFileName()));
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
			System.out.print(String.format("%-3d: Pattern: %s%n     Comment: %s", count++, entry.getPattern(),
					entry.getComment()));

		}
	}
}