package com.flegler.jpostgrey.interfaces;

import com.flegler.jpostgrey.InputRecord;
import com.flegler.jpostgrey.Settings;
import com.flegler.jpostgrey.dataFetcher.FetcherResult;

public interface DataFetcher {

	public FetcherResult getResult(InputRecord inputRecord);

	public void setUp(Settings settings);

}
