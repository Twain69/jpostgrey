package com.flegler.jpostgrey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.flegler.jpostgrey.InputRecord.InputRecordBuilder;
import com.flegler.jpostgrey.dataFetcher.FetcherResult;
import com.flegler.jpostgrey.exception.BuilderNotCompleteException;
import com.flegler.jpostgrey.interfaces.DataFetcher;

public class InputThread extends Thread {

	private static final Logger LOG = Logger.getLogger(InputThread.class);

	private final Socket socket;
	private final UUID uuid;

	@SuppressWarnings("unused")
	private InputThread() {
		throw new ExceptionInInitializerError("This is not ment to be called!");
	}

	public InputThread(Socket socket, UUID uuid) {
		this.socket = socket;
		this.uuid = uuid;
	}

	@Override
	public void run() {
		String inputString;
		BufferedReader inFromClient = null;
		OutputStreamWriter out = null;

		try {
			log(Priority.INFO_INT, "New incoming connection");
			inFromClient = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			InputRecordBuilder builder = new InputRecord.InputRecordBuilder();
			do {
				inputString = inFromClient.readLine();
				log(Priority.DEBUG_INT, "Got message '" + inputString + "'");
				if (inputString != null && !inputString.isEmpty()) {
					builder.addRow(inputString);

				}
			} while (inputString != null && !inputString.isEmpty());
			InputRecord inputRecord = null;
			try {
				inputRecord = builder.build();
			} catch (BuilderNotCompleteException e) {
				log(Priority.INFO_INT,
						"Not all records received ... have to reject");
			}
			out = new OutputStreamWriter(socket.getOutputStream());
			out.write(findTripletAndBuildOutputRecord(inputRecord).toString());
		} catch (IOException e) {
			for (StackTraceElement element : e.getStackTrace()) {
				log(Priority.ERROR_INT, element.toString());
			}
		} finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					for (StackTraceElement element : e.getStackTrace()) {
						log(Priority.ERROR_INT, element.toString());
					}
				}
			}
		}
	}

	public OutputRecord findTripletAndBuildOutputRecord(InputRecord inputRecord) {
		log(Priority.INFO_INT,
				String.format(
						"Performing search in backend for sender: '%s', recipient: '%s', clientAddress: '%s' ",
						inputRecord.getSender(), inputRecord.getRecipient(),
						inputRecord.getClientAddress().getHostAddress()));
		OutputRecord outputRecord = new OutputRecord(inputRecord);

		try {
			Settings settings = Settings.getInstance();
			DataFetcher fetcher = settings.getDataFetcherInstance();

			FetcherResult result = fetcher.getResult(inputRecord);

			if (result.getWhitelisted()) {
				outputRecord.setReason(Reason.WHITELIST);
				outputRecord.setAction(Action.PASS);
				log(Priority.INFO_INT, "InputRecord is whitelisted");
			} else {
				if (result.getFirstConnect() == 0L) {
					outputRecord.setReason(Reason.NEW);
					outputRecord.setAction(Action.DEFER_IF_PERMIT);
					log(Priority.INFO_INT, "This is a new InputRecord");
				} else {
					int duration = new Long(
							((new Date()).getTime() - result.getFirstConnect()) / 1000)
							.intValue();

					if (duration >= Settings.getInstance().getGreylistingTime()) {
						outputRecord.setReason(Reason.TRIPLET_FOUND);
						outputRecord.setAction(Action.PASS);
					} else {
						outputRecord.setReason(Reason.EARLY_RETRY);
						outputRecord.setAction(Action.DEFER_IF_PERMIT);
					}
					log(Priority.INFO_INT,
							String.format(
									"InputRecord found in backend. Duration since the first connect is '%d'. Current min duration: '%d'. Action: '%s'",
									duration, Settings.getInstance()
											.getGreylistingTime(), outputRecord
											.getAction().toString()));
				}
			}

		} catch (NullPointerException e) {
			log(Priority.ERROR_INT,
					"Something went really bad here! The datafetcher was missing.");
			outputRecord.setReason(Reason.ERROR);
			outputRecord.setAction(Action.DUNNO);
		}

		return outputRecord;
	}

	private void log(int priority, String message) {
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(uuid).append("] ").append(message);
		message = sb.toString();
		switch (priority) {
		case Priority.ALL_INT:
			LOG.fatal(message);
			break;
		case Priority.DEBUG_INT:
			LOG.debug(message);
			break;
		case Priority.ERROR_INT:
			LOG.error(message);
			break;
		case Priority.FATAL_INT:
			LOG.fatal(message);
			break;
		case Priority.INFO_INT:
			LOG.info(message);
			break;
		case Priority.OFF_INT:
			break;
		case Priority.WARN_INT:
			LOG.warn(message);
			break;
		}
	}

}
