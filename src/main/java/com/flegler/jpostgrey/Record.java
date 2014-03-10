package com.flegler.jpostgrey;

import java.util.Date;

public class Record {

	private InputRecord inputRecord;
	private Date firstHit;
	private Date lastHit;
	private int count;

	public InputRecord getInputRecord() {
		return inputRecord;
	}

	public void setInputRecord(InputRecord inputRecord) {
		this.inputRecord = inputRecord;
	}

	public Date getFirstHit() {
		return firstHit;
	}

	public void setFirstHit(Date firstHit) {
		this.firstHit = firstHit;
	}

	public Date getLastHit() {
		return lastHit;
	}

	public void setLastHit(Date lastHit) {
		this.lastHit = lastHit;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void incrementCount() {
		this.count += 1;
	}

	@Override
	public String toString() {
		return "Record [inputRecord=" + inputRecord + ", firstHit=" + firstHit
				+ ", lastHit=" + lastHit + ", count=" + count + "]";
	}

}
