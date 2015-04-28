package com.flegler.jpostgrey.model;

import com.flegler.jpostgrey.enums.Action;
import com.flegler.jpostgrey.enums.Reason;

public class OutputRecord {

	private Action action;
	private Reason reason;

	public OutputRecord() {
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Reason getReason() {
		return reason;
	}

	public void setReason(Reason reason) {
		this.reason = reason;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("action=").append(action).append(" ")
				.append("4.2.0 Greylisted, please come back later");
		return sb.toString();
	}
}
