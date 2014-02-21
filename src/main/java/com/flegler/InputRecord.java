package com.flegler;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class InputRecord {

	private final Logger LOG = Logger.getLogger(InputRecord.class);

	// private final String protocol; // protocol_state=RCPT
	// private final String protocolName; // protocol_name=ESMTP
	// private final String clientName; // client_name=idefix.flegler.com
	// private final String reverseClientName; //
	// reverse_client_name=idefix.flegler.com
	// private final String heloName; // helo_name=idefix.flegler.com
	// private final Integer recipientCount; // recipient_count=0
	// queue_id=
	// instance=4705.5300a841.e358.0
	// size=1533
	// etrn_domain=
	// stress=
	// sasl_method=
	// sasl_username=
	// sasl_sender=
	// ccert_subject=
	// ccert_issuer=
	// ccert_fingerprint=
	// ccert_pubkey_fingerprint=
	// encryption_protocol=
	// encryption_cipher=
	// encryption_keysize=0
	private final Inet4Address clientAddress; // client_address=10.200.10.20
	private final String sender; // sender=oxmox@idefix.flegler.com
	private final String recipient; // recipient=oxmox@oxmox-nb.flegler.com

	private InputRecord(Builder builder) {
		this.clientAddress = builder.clientAddress;
		this.sender = builder.sender;
		this.recipient = builder.recipient;
	}

	public static class Builder {
		private Inet4Address clientAddress;
		private String sender;
		private String recipient;

		public Builder() {
		}

		public Builder addRow(String input) throws UnknownHostException {
			if (input != null) {
				String[] tmp = input.split("=");
				String key = tmp[0];
				Object value = tmp[1];
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
		return "InputRecord [clientAddress=" + clientAddress + ", sender="
				+ sender + ", recipient=" + recipient + "]";
	}
}
