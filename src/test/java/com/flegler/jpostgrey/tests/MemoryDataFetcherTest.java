package com.flegler.jpostgrey.tests;

import org.junit.Before;
import org.junit.Test;

import com.flegler.jpostgrey.Settings;
import com.flegler.jpostgrey.dataFetcher.MemoryDataFetcher;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;
import com.flegler.jpostgrey.interfaces.DataFetcher;
import com.flegler.jpostgrey.model.InputRecord;

public class MemoryDataFetcherTest {

	DataFetcher fetcher = null;
	Settings settings = null;

	@Before
	public void setup() {
		fetcher = new MemoryDataFetcher();
		Preparation.createMemoryFetcherSettings();
		settings = Settings.getInstance();
		fetcher.setUp(settings);
	}

	@Test(expected = InputRecordNotFoundException.class)
	public void testGetResultNotFound() throws InputRecordNotFoundException {

		InputRecord inputRecord = InputRecordTest.createInputRecord();
		fetcher.getResult(inputRecord);
	}

}
