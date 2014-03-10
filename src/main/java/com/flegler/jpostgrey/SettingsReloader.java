package com.flegler.jpostgrey;

import org.apache.log4j.Logger;

public class SettingsReloader extends Thread {

	private static final Logger LOG = Logger.getLogger(SettingsReloader.class);

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1 * 30 * 1000); // run every 5 minutes
				LOG.debug("Parsing config file again");
				Settings.getInstance().readConfig();
			} catch (InterruptedException e) {
			}
		}
	}
}
