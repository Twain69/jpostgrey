package com.flegler.jpostgrey.model;

import lombok.Data;

import java.util.Date;

@Data
public class Record {

    private InputRecord inputRecord;
    private Date firstHit;
    private Date lastHit;
    private int count;

    public void incrementCount() {
        this.count += 1;
    }

    @Override
    public String toString() {
        return "Record [inputRecord=" + inputRecord + ", firstHit=" + firstHit
                + ", lastHit=" + lastHit + ", count=" + count + "]";
    }

}
