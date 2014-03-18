package com.flegler.jpostgrey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;

import com.flegler.jpostgrey.InputRecord.InputRecordBuilder;
import com.flegler.jpostgrey.exception.BuilderNotCompleteException;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;
import com.flegler.jpostgrey.interfaces.DataFetcher;

public class InputThread extends Thread {

	private static final Logger LOG = Logger.getLogger(InputThread.class);

	private final Socket socket;

	@SuppressWarnings("unused")
	private InputThread() {
		throw new ExceptionInInitializerError("This is not ment to be called!");
	}

	public InputThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		String inputString;
		BufferedReader inFromClient = null;
		OutputStreamWriter out = null;

		try {
			LOG.info("New incoming connection");
			inFromClient = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			InputRecordBuilder builder = new InputRecord.InputRecordBuilder();
			do {
				inputString = inFromClient.readLine();
				LOG.debug("Got message '" + inputString + "'");
				if (inputString != null && !inputString.isEmpty()) {
					builder.addRow(inputString);

				}
			} while (inputString != null && !inputString.isEmpty());
			InputRecord inputRecord = null;
			try {
				inputRecord = builder.build();
			} catch (BuilderNotCompleteException e) {
				LOG.info("Not all records received ... have to reject");
			}
			out = new OutputStreamWriter(socket.getOutputStream());
			out.write(findTripletAndBuildOutputRecord(inputRecord).toString());
		} catch (IOException e) {
			for (StackTraceElement element : e.getStackTrace()) {
				LOG.error(element.toString());
			}
		} finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					for (StackTraceElement element : e.getStackTrace()) {
						LOG.error(element.toString());
					}
				}
			}
		}
	}

	public OutputRecord findTripletAndBuildOutputRecord(InputRecord inputRecord) {
		OutputRecord outputRecord = new OutputRecord(inputRecord);

		try {
			Settings settings = Settings.getInstance();
			DataFetcher fetcher = settings.getDataFetcherInstance();
			Timestamp lastConnect = fetcher.getTimestamp(inputRecord);
			int duration = new Long(
					((new Date()).getTime() - lastConnect.getTime()) / 1000)
					.intValue();

			if (duration > Settings.getInstance().getGreylistingTime()) {
				outputRecord.setReason(OutputRecord.Reason.TRIPLET_FOUND);
				outputRecord.setAction(OutputRecord.Action.PASS);
			} else {
				LOG.info("OutputRecord: " + outputRecord + " // Duration: "
						+ duration);
				outputRecord.setReason(OutputRecord.Reason.EARLY_RETRY);
				outputRecord.setAction(OutputRecord.Action.DEFER_IF_PERMIT);
			}

		} catch (InputRecordNotFoundException e) {
			outputRecord.setReason(OutputRecord.Reason.NEW);
			outputRecord.setAction(OutputRecord.Action.DEFER_IF_PERMIT);
		} catch (NullPointerException e) {
			LOG.error("Something went really bad here! The datafetcher was missing.");
			outputRecord.setReason(OutputRecord.Reason.ERROR);
			outputRecord.setAction(OutputRecord.Action.DUNNO);
		}

		return outputRecord;
	}

}
