package com.flegler.jpostgrey;

public class OutputRecord {
	// action=DEFER_IF_PERMIT Greylistedaction=DEFER_IF_PERMIT Greylisted
	public static enum Action {
		DEFER_IF_PERMIT, PASS, DUNNO
	};

	public static enum Reason {
		NEW, EARLY_RETRY, TRIPLET_FOUND
	};

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
				.append(inputRecord.getClientAddress().toString()).append(" ")
				.append("sender=").append(inputRecord.getSender()).append(" ")
				.append("recipient=").append(inputRecord.getRecipient());
		return sb.toString();
	}
}
