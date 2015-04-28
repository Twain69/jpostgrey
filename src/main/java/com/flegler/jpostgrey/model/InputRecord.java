package com.flegler.jpostgrey.model;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.flegler.jpostgrey.exception.BuilderNotCompleteException;

public class InputRecord {

	private static final Logger LOG = Logger.getLogger(InputRecord.class);

	private final Inet4Address clientAddress;
	private final String sender;
	private final String recipient;

	private InputRecord(InputRecordBuilder builder) {
		this.clientAddress = builder.clientAddress;
		this.sender = builder.sender;
		this.recipient = builder.recipient;
		LOG.debug("New InputRecord created: " + this.toString());
	}

	public static class InputRecordBuilder {
		private Inet4Address clientAddress;
		private String sender;
		private String recipient;

		public InputRecordBuilder() {
		}

		public InputRecordBuilder addRow(String input) throws UnknownHostException {
			if (input != null) {
				String[] tmp = input.split("=");
				String key = tmp[0];
				Object value;
				if (tmp.length > 1) {
					value = tmp[1];
				} else {
					value = new String("");
				}
				if (key != null) {
					switch (key.toLowerCase()) {
					case "client_address":
						this.clientAddress = (Inet4Address) Inet4Address
								.getByName(value.toString());
						break;
					case "sender":
						this.sender = (String) value;
						break;
					case "recipient":
						this.recipient = (String) value;
						break;
					default:
						return this;
					}
				}
			}
			return this;
		}

		public InputRecord build() throws BuilderNotCompleteException {
			if (this.clientAddress != null && this.recipient != null
					&& this.sender != null) {
				return new InputRecord(this);
			} else {
				throw new BuilderNotCompleteException(
						"Not all variables have been set");
			}
		}
	}

	public Inet4Address getClientAddress() {
		return clientAddress;
	}

	public String getSender() {
		return sender;
	}

	public String getRecipient() {
		return recipient;
	}

	@Override
	public String toString() {
    return "InputRecord [clientAddress=" + clientAddress.getHostAddress() + ", sender="
				+ sender + ", recipient=" + recipient + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clientAddress == null) ? 0 : clientAddress.hashCode());
		result = prime * result
				+ ((recipient == null) ? 0 : recipient.hashCode());
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
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
		InputRecord other = (InputRecord) obj;
		if (clientAddress == null) {
			if (other.clientAddress != null)
				return false;
		} else if (!clientAddress.equals(other.clientAddress))
			return false;
		if (recipient == null) {
			if (other.recipient != null)
				return false;
		} else if (!recipient.equals(other.recipient))
			return false;
		if (sender == null) {
			if (other.sender != null)
				return false;
		} else if (!sender.equals(other.sender))
			return false;
		return true;
	}

}
