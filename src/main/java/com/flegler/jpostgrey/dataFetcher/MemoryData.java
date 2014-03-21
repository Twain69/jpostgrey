package com.flegler.jpostgrey.dataFetcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import com.flegler.jpostgrey.InputRecord;
import com.flegler.jpostgrey.Record;
import com.flegler.jpostgrey.Settings;
import com.flegler.jpostgrey.exception.InputRecordNotFoundException;
import com.flegler.jpostgrey.interfaces.DataFetcher;

public class MemoryData implements DataFetcher {

	private final List<Record> recordList;

	private final Semaphore writeLock = new Semaphore(1);

	private final Logger LOG = Logger.getLogger(MemoryData.class);

	private MemoryData() {
		recordList = new ArrayList<>();
	}

	/**
	 * Finds the Record in the safed elements and returns the duration since the
	 * first try
	 * 
	 * Throws {@link InputRecordNotFoundException} when the record was not found
	 * (so this must be the first try)
	 * 
	 * @param inputRecord
	 * @return
	 * @throws InputRecordNotFoundException
	 */
	@Override
	public FetcherResult getResult(InputRecord inputRecord) {
		FetcherResult result = new FetcherResult();
		if (recordList != null && !recordList.isEmpty()) {
			int index = -1;
			for (int i = 0; i < recordList.size(); i++) {
				if (recordList.get(i).getInputRecord().equals(inputRecord)) {
					index = i;
					break;
				}
			}

			if (index == -1) {
				addNewRecord(inputRecord);
				result.setFirstConnect(0L);
				return result;
			}

			Record record = recordList.get(index);

			LOG.trace("Found Object in MemoryData: " + record.toString());
			LOG.trace("Current count of objects in MemoryData: "
					+ recordList.size());

			Date firstHit = record.getFirstHit();

			record.setLastHit(new Date());
			record.incrementCount();

			recordList.set(index, record);

			result.setFirstConnect(firstHit.getTime());
			return result;
		} else {
			result.setFirstConnect(0L);
			addNewRecord(inputRecord);
			return result;
		}

	}

	private void addNewRecord(InputRecord inputRecord) {
		Record record = new Record();
		record.setInputRecord(inputRecord);
		record.setFirstHit(new Date());
		record.setLastHit(new Date());
		record.setCount(0);

		LOG.trace("Adding new object to MemoryData: " + record.toString());
		LOG.trace("Current count of objects in MemoryData: "
				+ recordList.size());

		writeLock.acquireUninterruptibly();
		recordList.add(record);
		writeLock.release();
	}

	@Override
	public void setUp(Settings settings) {
		// this is left intentionally, since it is not needed here
	}

}
