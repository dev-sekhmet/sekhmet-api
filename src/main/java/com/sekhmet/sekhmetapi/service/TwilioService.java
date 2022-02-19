package com.sekhmet.sekhmetapi.service;

import com.sekhmet.sekhmetapi.config.ApplicationProperties;
import com.sekhmet.sekhmetapi.service.dto.sms.CheckPhoneVerificationRequest;
import com.sekhmet.sekhmetapi.service.dto.sms.StartPhoneVerificationRequest;
import com.sekhmet.sekhmetapi.service.dto.sms.VerificationStatus;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioService {

    private final ApplicationProperties.SmsProperties.TwilioPreperties smsProps;
    private final TwilioConversationService conversationUserService;

    public TwilioService(ApplicationProperties applicationProperties, TwilioConversationService conversationUserService) {
        this.smsProps = applicationProperties.getSms().getTwilio();
        this.conversationUserService = conversationUserService;
        Twilio.init(smsProps.getAccountSid(), smsProps.getAuthToken());
    }

    public VerificationStatus sendVerificationCode(StartPhoneVerificationRequest request) {
        Verification verification = Verification
            .creator(
                smsProps.getVerifySid(),
                request.getPhoneNumber(), // concatenated with country code +33 or +237
                request.getChannel().toString()
            )
            .create();
        String status = verification.getStatus();
        log.info("Verification code send, params: {} - status: {}", request, status);
        return VerificationStatus.forValue(status);
    }

    public VerificationStatus checkVerificationCode(CheckPhoneVerificationRequest request) {
        VerificationCheck verificationCheck = VerificationCheck
            .creator(smsProps.getVerifySid(), request.getToken())
            .setTo(request.getPhoneNumber())
            .create();

        String status = verificationCheck.getStatus();
        log.info("Verification code send, params: {} - status: {}", request, status);
        return VerificationStatus.forValue(status);
    }

    @PostConstruct
    public void init() {
        conversationUserService.createAllUsers();
    }
}
