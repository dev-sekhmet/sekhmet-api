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
        Optional<Conversation> conversation = findConversationDual(userId, currentUserId);
        if (conversation.isEmpty()) {
            conversation = findConversationDual(currentUserId, userId);
            if (conversation.isEmpty()) {
                String pathSid = buildDualConversationId(userId, currentUserId);
                return Optional.of(createConversationDual(userId, currentUserId, pathSid));
            }
            return conversation;
        }
        return conversation;
    }

    public Optional<Conversation> findConversationDual(UUID userId, UUID currentUserId) {
        Conversation conversation = null;
        String pathSid = buildDualConversationId(userId, currentUserId);
        try {
            conversation = Conversation.fetcher(pathSid).fetch();
            log.info("Twilio Conversation {} - {} Already exists", conversation.getUniqueName(), conversation.getFriendlyName());
            return Optional.of(conversation);
        } catch (ApiException ex) {
            if (ex.getMessage().contains("not found")) {
                return Optional.empty();
            } else {
                throw ex;
            }
        }
    }

    private Conversation createConversationDual(UUID userId, UUID currentUserId, String pathSid) {
        Optional<com.sekhmet.sekhmetapi.domain.User> userOptional = userService.getUserById(userId);
        Optional<com.sekhmet.sekhmetapi.domain.User> currentUserOptional = userService.getUserWithAuthorities();
        Conversation conversation = null;
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

            if (conversation != null) {
                // create first User
                Participant.creator(conversation.getSid()).setIdentity(userId.toString()).create();

                // create second User
                Participant.creator(conversation.getSid()).setIdentity(currentUserId.toString()).create();
            } else {
                log.warn("Could not create conversation");
            }
            log.info("twilio Conversation created Successfully: {} - {}", conversation.getUniqueName(), conversation.getFriendlyName());
        }
        return conversation;
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
