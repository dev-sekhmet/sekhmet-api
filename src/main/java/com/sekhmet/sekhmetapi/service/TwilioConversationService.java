package com.sekhmet.sekhmetapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sekhmet.sekhmetapi.config.ApplicationProperties;
import com.twilio.exception.ApiException;
import com.twilio.rest.conversations.v1.Conversation;
import com.twilio.rest.conversations.v1.User;
import com.twilio.rest.conversations.v1.conversation.Participant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioConversationService {

    public static final String DUAL_CONVERSATION_FORMAT_ID = "DUAL_%s_%s";
    private final UserService userService;
    private final ApplicationProperties.SmsProperties.TwilioPreperties smsProps;

    public TwilioConversationService(UserService userService, ApplicationProperties applicationProperties) {
        this.smsProps = applicationProperties.getSms().getTwilio();
        this.userService = userService;
    }

    public void createAllUsers() {
        userService
            .findAll()
            .forEach(user -> {
                log.info("Try Creating conv twilio user {} ...", user.getId());
                User userConTwilio = null;
                try {
                    userConTwilio = User.fetcher(user.getId().toString()).fetch();
                    log.info("Twilio user {} - {} Already exists", userConTwilio.getIdentity(), userConTwilio.getFriendlyName());
                } catch (ApiException ex) {
                    if (ex.getMessage().contains("not found")) {
                        try {
                            user.setImageUrl("https://i.pravatar.cc/300");
                            userConTwilio =
                                User
                                    .creator(user.getId().toString())
                                    .setFriendlyName(user.getFirstName() + " " + user.getLastName())
                                    .setAttributes(new ObjectMapper().writeValueAsString(user))
                                    .create();
                            log.info(
                                "Conv twilio user created Sucessfully: {} - {}",
                                userConTwilio.getIdentity(),
                                userConTwilio.getFriendlyName()
                            );
                        } catch (JsonProcessingException e) {
                            log.info("Could not parse twilio attributs");
                        }
                    } else {
                        throw ex;
                    }
                }
            });
    }

    public Optional<Conversation> findOrCreateConversationDual(UUID userId, UUID currentUserId) {
        String pathSid = buildDualConversationId(userId, currentUserId);
        Conversation conversation = null;
        try {
            conversation = Conversation.fetcher(pathSid).fetch();
            log.info("Twilio Conversation {} - {} Already exists", conversation.getUniqueName(), conversation.getFriendlyName());
            return Optional.of(conversation);
        } catch (ApiException ex) {
            if (ex.getMessage().contains("not found")) {
                Optional<com.sekhmet.sekhmetapi.domain.User> userOptional = userService.getUserById(userId);
                Optional<com.sekhmet.sekhmetapi.domain.User> currentUserOptional = userService.getUserWithAuthorities();

                if (userOptional.isPresent() && currentUserOptional.isPresent()) {
                    com.sekhmet.sekhmetapi.domain.User user = userOptional.get();
                    com.sekhmet.sekhmetapi.domain.User currentUser = currentUserOptional.get();
                    Map<String, String> conversationNames = buildConversationNames(user, currentUser);
                    try {
                        conversation =
                            Conversation
                                .creator()
                                .setUniqueName(pathSid)
                                .setAttributes(new ObjectMapper().writeValueAsString(conversationNames))
                                .setFriendlyName(user.getFirstName() + "/" + currentUser.getFirstName())
                                .create();
                    } catch (JsonProcessingException e) {
                        log.info("Could not parse twilio attributs");
                    }

                    Participant.creator(conversation.getSid()).setIdentity(userId.toString()).create();

                    Participant.creator(conversation.getSid()).setIdentity(currentUserId.toString()).create();
                    log.info(
                        "twilio Conversation created Successfully: {} - {}",
                        conversation.getUniqueName(),
                        conversation.getFriendlyName()
                    );
                }
                return Optional.of(conversation);
            } else {
                throw ex;
            }
        }
    }

    private Map<String, String> buildConversationNames(
        com.sekhmet.sekhmetapi.domain.User user,
        com.sekhmet.sekhmetapi.domain.User currentUser
    ) {
        HashMap<String, String> convNames = new HashMap<>();
        convNames.put(user.getId().toString(), currentUser.getFirstName() + " " + currentUser.getLastName());
        convNames.put(currentUser.getId().toString(), user.getFirstName() + " " + user.getLastName());
        return convNames;
    }

    public String buildDualConversationId(UUID id, UUID currentUser) {
        return String.format(DUAL_CONVERSATION_FORMAT_ID, id.toString(), currentUser.toString());
    }
}
