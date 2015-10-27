package com.flegler.jpostgrey.model;

import com.flegler.jpostgrey.enums.Action;
import com.flegler.jpostgrey.enums.Reason;

import lombok.Data;

@Data
public class OutputRecord {

	private Action action;
	private Reason reason;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("action=").append(action).append(" ")
				.append("4.2.0 Greylisted, please come back later");
		return sb.toString();
	}
}
