package com.flegler.jpostgrey;

import interfaces.DataFetcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import com.flegler.exception.InputRecordNotFoundException;

public class MemoryData implements DataFetcher {

	private final List<Record> recordList;
	private static MemoryData instance;

	private final Semaphore writeLock = new Semaphore(1);

	private static final Logger LOG = Logger.getLogger(MemoryData.class);

	private MemoryData() {
		recordList = new ArrayList<>();
	}

	public static MemoryData getInstance() {
		if (instance == null) {
			instance = new MemoryData();
		}

		return instance;
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
	public int getDuration(InputRecord inputRecord)
			throws InputRecordNotFoundException {
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
				throw new InputRecordNotFoundException();
			}

			Record record = recordList.get(index);

			LOG.trace("Found Object in MemoryData: " + record.toString());
			LOG.trace("Current count of objects in MemoryData: "
					+ recordList.size());

			Date now = new Date();
			Date firstHit = record.getFirstHit();

			record.setLastHit(new Date());
			record.incrementCount();

			recordList.set(index, record);

			return new Long((now.getTime() - firstHit.getTime()) / 1000)
					.intValue();
		} else {
			addNewRecord(inputRecord);
			throw new InputRecordNotFoundException();
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
}
