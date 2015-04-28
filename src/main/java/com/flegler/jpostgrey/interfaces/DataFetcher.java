package com.flegler.jpostgrey.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.flegler.jpostgrey.Settings;
import com.flegler.jpostgrey.dataFetcher.FetcherResult;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;
import com.flegler.jpostgrey.model.InputRecord;
import com.flegler.jpostgrey.model.WhiteListEntry;

public interface DataFetcher {

	default public FetcherResult getResult(InputRecord inputRecord)
			throws InputRecordNotFoundException {
		FetcherResult result = new FetcherResult();
		result.setFirstConnect(0L);
		return result;
	}

	public void setUp(Settings settings);

	default public void addEntryToWhitelist(WhiteListEntry whiteListEntry) {
		System.out
				.println("Not supported (yet) with the datafetcher you configured");
	}

	default public void removeEntryFromWhitelist(String pattern) {
		System.out
				.println("Not supported (yet) with the datafetcher you configured");
	}

	default public List<WhiteListEntry> getAllWhitelistEntries() {
		System.out
				.println("Not supported (yet) with the datafetcher you configured");
		return new ArrayList<WhiteListEntry>();
	}

}
