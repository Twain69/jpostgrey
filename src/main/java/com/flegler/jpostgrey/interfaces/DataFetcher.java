package com.flegler.jpostgrey.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.flegler.jpostgrey.dataFetcher.FetcherResult;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;
import com.flegler.jpostgrey.model.InputRecord;
import com.flegler.jpostgrey.model.WhiteListEntry;

public interface DataFetcher {

	default FetcherResult getResult(InputRecord inputRecord) throws InputRecordNotFoundException {
		FetcherResult result = new FetcherResult();
		result.setFirstConnect(0L);
		return result;
	}

	void setUp();
}
