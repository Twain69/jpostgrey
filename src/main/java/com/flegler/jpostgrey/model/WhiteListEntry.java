package com.flegler.jpostgrey.model;

public class WhiteListEntry {

	private final String pattern;
	private final String comment;

	public WhiteListEntry(final String pattern, final String comment) {
		this.pattern = pattern;
		this.comment = comment;
	}

	public String getPattern() {
		return pattern;
	}

	public String getComment() {
		return comment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WhiteListEntry other = (WhiteListEntry) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (pattern == null) {
			if (other.pattern != null)
				return false;
		} else if (!pattern.equals(other.pattern))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WhiteListEntry [pattern=" + pattern + ", comment=" + comment
				+ "]";
	}

}
