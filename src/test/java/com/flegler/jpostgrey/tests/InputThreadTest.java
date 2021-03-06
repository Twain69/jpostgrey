package com.flegler.jpostgrey.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.flegler.jpostgrey.InputThread;
import com.flegler.jpostgrey.Settings;
import com.flegler.jpostgrey.model.InputRecord;

public class InputThreadTest {

	InputThread inputThread;

	@Before
	public void setup() {
		// Override default greylisting time to 2 seconds
		Settings.INSTANCE.getConfig().setProperty("application.greylisting.time", "2");
	}

	@Test
	public void testFindTripletAndBuildOutputRecord() throws InterruptedException {
		inputThread = new InputThread(null, null);

		InputRecord inputRecord = InputRecordTest.createInputRecord();
		String result = inputThread.findTripletAndBuildOutputRecord(inputRecord);
		Assert.assertTrue(result.equals("action=" + InputThread.DEFER));

		Thread.sleep(1000);

		result = inputThread.findTripletAndBuildOutputRecord(inputRecord);
		Assert.assertTrue(result.startsWith("action=DEFER 4.2.0 Greylisted, early retry"));

		Thread.sleep(1100);

		result = inputThread.findTripletAndBuildOutputRecord(inputRecord);
		Assert.assertTrue(result.equals("action=" + InputThread.PASS));
	}
}
