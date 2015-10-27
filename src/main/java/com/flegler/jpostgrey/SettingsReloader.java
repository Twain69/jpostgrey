package com.flegler.jpostgrey;

import org.apache.log4j.Logger;

public class SettingsReloader extends Thread {

	private static final Logger LOG = Logger.getLogger(SettingsReloader.class);

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1 * 30 * 1000); // run every 30 seconds
				LOG.debug("Parsing config file again");
				Settings.INSTANCE.readConfig();
			} catch (InterruptedException e) {
			}
		}
	}
}
