package com.flegler.jpostgrey.model;

import lombok.Data;

@Data
public class WhiteListEntry {

	private final String pattern;
	private final String comment;

	public WhiteListEntry(final String pattern, final String comment) {
		this.pattern = pattern;
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "WhiteListEntry [pattern=" + pattern + ", comment=" + comment
				+ "]";
	}

}
