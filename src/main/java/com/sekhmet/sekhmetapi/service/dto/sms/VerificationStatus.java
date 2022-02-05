package com.sekhmet.sekhmetapi.service.dto.sms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.twilio.converter.Promoter;

public enum VerificationStatus {
    PENDING("pending"),
    APPROVED("approved"),
    CANCELED("canceled");

    private final String value;

    private VerificationStatus(final String value) {
        this.value = value;
    }

    /**
     * Generate a Status from a string.
     *
     * @param value string value
     * @return generated Status
     */
    @JsonCreator
    public static VerificationStatus forValue(final String value) {
        return Promoter.enumFromString(value, values());
    }

    public String toString() {
        return value;
    }
}
