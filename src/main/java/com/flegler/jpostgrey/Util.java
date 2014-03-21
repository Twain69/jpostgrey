package com.flegler.jpostgrey;

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

}
