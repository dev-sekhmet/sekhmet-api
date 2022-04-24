package com.sekhmet.sekhmetapi.service;

import com.sekhmet.sekhmetapi.config.ApplicationProperties;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.ChatGrant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.jhipster.config.JHipsterProperties;

@Service
@Slf4j
public class ConversationService {

    private final ApplicationProperties.SmsProperties.TwilioPreperties twilioPreperties;

    public ConversationService(ApplicationProperties applicationProperties, JHipsterProperties jHipsterProperties) {
        this.twilioPreperties = applicationProperties.getSms().getTwilio();
    }

    public String generateAccessToken(UUID userId) {
        ChatGrant grant = new ChatGrant();
        grant.setServiceSid(twilioPreperties.getConversationSid());

        AccessToken token = new AccessToken.Builder(
            twilioPreperties.getAccountSid(),
            twilioPreperties.getApiSid(),
            twilioPreperties.getApiSecret()
        )
            .identity(userId.toString())
            .grant(grant)
            //.ttl(30)
            .ttl(86400) // 24 hours
            .build();
        return token.toJwt();
    }
}
