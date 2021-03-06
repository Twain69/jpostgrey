/**
 * Copyright (c) 2014 ADTECH. All rights reserved.
 * 
 **/
package com.flegler.jpostgrey.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.junit.Test;

import com.flegler.jpostgrey.exception.BuilderNotCompleteException;
import com.flegler.jpostgrey.model.InputRecord;
import com.flegler.jpostgrey.model.InputRecord.InputRecordBuilder;

/**
 * @author Oliver Flegler
 * 
 */
public class InputRecordTest {

	public static InputRecord createInputRecord() {
		InputRecord record = null;
		try {
			InputRecordBuilder builder = new InputRecord.InputRecordBuilder();
			builder.addRow("client_address=10.200.10.20")
					.addRow("sender=oxmox@idefix.flegler.com")
					.addRow("recipient=oxmox@oxmox-nb.flegler.com");
			record = builder.build();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("UnknownHostException occured");
		} catch (BuilderNotCompleteException e) {
			e.printStackTrace();
			fail("Not all parameters have been set!");
		}
		return record;
	}

	@Test
	public void builderTest() {
		try {
			InputRecord record = createInputRecord();

			assertEquals("", Inet4Address.getByName("10.200.10.20"),
					record.getClientAddress());
			assertEquals("", "oxmox@idefix.flegler.com", record.getSender());
			assertEquals("", "oxmox@oxmox-nb.flegler.com",
					record.getRecipient());

		} catch (UnknownHostException e) {
			e.printStackTrace();
			fail("UnknownHostException occured");
		}

	}

}
