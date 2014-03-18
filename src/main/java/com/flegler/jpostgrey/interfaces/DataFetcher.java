package com.flegler.jpostgrey.interfaces;

import java.sql.Timestamp;

import com.flegler.jpostgrey.InputRecord;
import com.flegler.jpostgrey.Settings;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;

public interface DataFetcher {

	public Timestamp getTimestamp(InputRecord inputRecord)
			throws InputRecordNotFoundException;

	public void setUp(Settings settings);

}
