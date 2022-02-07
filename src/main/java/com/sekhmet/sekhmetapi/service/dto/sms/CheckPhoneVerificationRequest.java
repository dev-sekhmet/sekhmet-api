package com.sekhmet.sekhmetapi.service.dto.sms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CheckPhoneVerificationRequest {

    @NotNull
    private String phoneNumber;

    @NotNull
    private String token;

    @Size(min = 2, max = 10)
    private String langKey;

    private String locale;

    public CheckPhoneVerificationRequest() {}
}
