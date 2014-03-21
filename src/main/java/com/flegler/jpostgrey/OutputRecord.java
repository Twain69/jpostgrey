package com.flegler.jpostgrey;

public class OutputRecord {

	private Action action;
	private Reason reason;
	private final InputRecord inputRecord;

	@SuppressWarnings("unused")
	private OutputRecord() {
		throw new ExceptionInInitializerError("This is not ment to be called!");
	}

	public OutputRecord(InputRecord inputRecord) {
		this.inputRecord = inputRecord;
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
		sb.append("action=").append(action).append(" ").append("reason=")
				.append(reason).append(" ").append("client_address=")
        .append(inputRecord.getClientAddress().getHostAddress().toString()).append(" ")
				.append("sender=").append(inputRecord.getSender()).append(" ")
				.append("recipient=").append(inputRecord.getRecipient());
		return sb.toString();
	}
}
