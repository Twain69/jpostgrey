package com.flegler.jpostgrey;

import java.lang.management.ManagementFactory;

public class Util {
	public String getVersion() {
		String version = getClass().getPackage().getImplementationVersion();
		if (version == null) {
			version = "n/a";
		}
		return version;
	}

	public String getTitle() {
		String title = getClass().getPackage().getImplementationTitle();
		if (title == null) {
			title = "n/a";
		}
		return title;
	}

	public static Integer getPid() {
		return Integer.valueOf(ManagementFactory.getRuntimeMXBean().getName()
				.split("@")[0]);
	}
}
