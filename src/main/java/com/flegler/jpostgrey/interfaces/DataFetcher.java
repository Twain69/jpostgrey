package com.flegler.jpostgrey.interfaces;

import com.flegler.jpostgrey.Settings;
import com.flegler.jpostgrey.dataFetcher.FetcherResult;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;
import com.flegler.jpostgrey.model.InputRecord;

public interface DataFetcher {

    default FetcherResult getResult(InputRecord inputRecord) throws InputRecordNotFoundException {
        FetcherResult result = new FetcherResult();
        result.setFirstConnect(0L);
        return result;
    }

    void setUp(Settings settings);
}
