package com.flegler.jpostgrey.model;

import com.flegler.jpostgrey.exception.BuilderNotCompleteException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;

@Getter
@EqualsAndHashCode
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

    @Override
    public String toString() {
        return "InputRecord [clientAddress=" + clientAddress.getHostAddress() + ", sender="
                + sender + ", recipient=" + recipient + "]";
    }

}
