package com.flegler.jpostgrey.interfaces;

import com.flegler.jpostgrey.InputRecord;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;

public interface DataFetcher {

	public int getDuration(InputRecord inputRecord)
			throws InputRecordNotFoundException;

}
