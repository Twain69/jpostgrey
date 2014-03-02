package com.flegler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.flegler.InputRecord.InputRecordBuilder;
import com.flegler.exception.BuilderNotCompleteException;
import com.flegler.exception.InputRecordNotFoundException;

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
			out = new OutputStreamWriter(
					socket.getOutputStream());
			out.write(findTripletAndBuildOutputRecord(inputRecord).toString());
		} catch (IOException e) {
			for(StackTraceElement element: e.getStackTrace()) {
				LOG.error(element.toString());
			}
		} finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					for(StackTraceElement element: e.getStackTrace()) {
						LOG.error(element.toString());
					}
				}
			}
		}
	}

	public OutputRecord findTripletAndBuildOutputRecord(InputRecord inputRecord) {
		OutputRecord outputRecord = new OutputRecord(inputRecord);

		try {
			int duration = MemoryData.getInstance().getDuration(inputRecord);
			if (duration > 10) {
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
		}

		return outputRecord;
	}

}
