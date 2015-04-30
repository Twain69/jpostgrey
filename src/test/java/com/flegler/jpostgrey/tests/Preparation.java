package com.flegler.jpostgrey.tests;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.junit.Assert;

import com.flegler.jpostgrey.Settings;
import com.flegler.jpostgrey.dataFetcher.MemoryDataFetcher;

public class Preparation {

	public static void createMemoryFetcherSettings() {
		System.out.println("Creating new Settings class");
		Settings settings = Settings.getInstance();
		settings.setConfigFile(createConfigFile());
		settings.readConfig();
		System.out.println("Creation of Settings class finished");
	}

	private static File createConfigFile() {
		String filePath = System.getProperty("java.io.tmpdir") + "/"
				+ System.nanoTime() + "_jpostgrey.conf";

		File configFile = new File(filePath);

		System.out.println("Creating new config file '"
				+ configFile.getAbsolutePath() + "'");

		configFile.deleteOnExit();

		Writer writer = null;

		try {
			writer = new FileWriter(configFile);
			writer.write("application.main.port=9989\n");
			writer.write("application.main.address=0.0.0.0\n");
			writer.write("application.greylisting.time=2\n");
			writer.write("application.datafetcher.type="
					+ MemoryDataFetcher.class.getCanonicalName());
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		printFile(configFile);

		return configFile;
	}

	private static void printFile(File file) {
		Reader reader = null;
		try {
			reader = new FileReader(file);
			char[] buf = new char[4096];
			while (reader.read(buf) != -1) {
				System.out.println(buf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
