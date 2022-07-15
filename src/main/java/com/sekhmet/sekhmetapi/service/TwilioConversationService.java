package com.sekhmet.sekhmetapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sekhmet.sekhmetapi.config.ApplicationProperties;
import com.sekhmet.sekhmetapi.service.dto.ConversationDto;
import com.twilio.exception.ApiException;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.ChatGrant;
import com.twilio.rest.conversations.v1.Conversation;
import com.twilio.rest.conversations.v1.User;
import com.twilio.rest.conversations.v1.conversation.Participant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TwilioConversationService {

    public static final String DUAL_CONVERSATION_FORMAT_ID = "DUAL_%s_%s";
    public static final String GROUP_CONVERSATION_FORMAT_ID = "GROUP_%s";
    private final UserService userService;
    private final ApplicationProperties.SmsProperties.TwilioPreperties smsProps;

    public TwilioConversationService(UserService userService, ApplicationProperties applicationProperties) {
        this.smsProps = applicationProperties.getSms().getTwilio();
        this.userService = userService;
    }

    public String generateAccessToken(UUID userId) {
        ChatGrant grant = new ChatGrant();
        grant.setServiceSid(smsProps.getConversationSid());

        AccessToken token = new AccessToken.Builder(smsProps.getAccountSid(), smsProps.getApiSid(), smsProps.getApiSecret())
            .identity(userId.toString())
            .grant(grant)
            //.ttl(30)
            .ttl(86400) // 24 hours
            .build();
        return token.toJwt();
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
                        createTwilioUser(user);
                    } else {
                        throw ex;
                    }
                }
            });
    }

    private void createTwilioUser(com.sekhmet.sekhmetapi.domain.User user) {
        User userConTwilio;
        try {
            if (user.getImageUrl() == null) {
                user.setImageUrl("https://i.pravatar.cc/300");
            }
            userConTwilio =
                User
                    .creator(user.getId().toString())
                    .setFriendlyName(user.getFirstName() + " " + user.getLastName())
                    .setAttributes(new ObjectMapper().writeValueAsString(user))
                    .create();
            log.info("Conv twilio user created Sucessfully: {} - {}", userConTwilio.getIdentity(), userConTwilio.getFriendlyName());
        } catch (JsonProcessingException e) {
            log.info("Could not parse twilio attributs");
        }
    }

    private boolean isDual(List<UUID> ids) {
        return ids.size() == 1; // current user id plus one other user id
    }

    public Optional<Conversation> findOrCreateConversation(ConversationDto conversationDto, UUID currentUserId) {
        if (isDual(conversationDto.getIds())) {
            return findOrCreateConversationDual(conversationDto.getIds().get(0), currentUserId);
        } else {
            return createConversationGroup(conversationDto, currentUserId);
        }
    }

    private Optional<Conversation> createConversationGroup(ConversationDto conversationDto, UUID currentUserId) {
        Conversation conversation = null;
        try {
            conversation =
                Conversation
                    .creator()
                    .setUniqueName(buildGroupConversationId())
                    .setAttributes(new ObjectMapper().writeValueAsString(Map.of("description", conversationDto.getDescription())))
                    .setFriendlyName(conversationDto.getFriendlyName())
                    .create();
        } catch (JsonProcessingException e) {
            log.info("Could not parse twilio attributs");
        }

        if (conversation != null) {
            Conversation finalConversation = conversation;

            Participant
                .creator(finalConversation.getSid())
                .setIdentity(currentUserId.toString())
                .setRoleSid(smsProps.getChannelAdminSid())
                .create();
            conversationDto
                .getIds()
                .forEach(id -> {
                    var participant = Participant
                        .creator(finalConversation.getSid())
                        .setIdentity(id.toString())
                        .setRoleSid(smsProps.getChannelUserSid())
                        .create();
                    log.info("Conv twilio user {} added to conversation {}", participant.getIdentity(), finalConversation.getSid());
                });
            log.info("twilio Conversation created Successfully: {} - {}", conversation.getUniqueName(), conversation.getFriendlyName());
        } else {
            log.error("Could not create conversation");
        }
        return Optional.ofNullable(conversation);
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
            Map<String, Object> conversationNames = buildConversationAttributes(user, currentUser);
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
                log.info("twilio Conversation created Successfully: {} - {}", conversation.getUniqueName(), conversation.getFriendlyName());
            } else {
                log.error("Could not create conversation");
            }
        }
        return conversation;
    }

    private Map<String, Object> buildConversationAttributes(
        com.sekhmet.sekhmetapi.domain.User user,
        com.sekhmet.sekhmetapi.domain.User currentUser
    ) {
        return Map.of(
            user.getId().toString(),
            Map.of("friendlyName", currentUser.getFirstName() + " " + currentUser.getLastName(), "imageUrl", getImageUrl(currentUser)),
            currentUser.getId().toString(),
            Map.of("friendlyName", user.getFirstName() + " " + user.getLastName(), "imageUrl", getImageUrl(user))
        );
    }

    private String getImageUrl(com.sekhmet.sekhmetapi.domain.User currentUser) {
        return currentUser.getImageUrl() != null ? currentUser.getImageUrl() : "";
    }

    private String buildDualConversationId(UUID id, UUID currentUser) {
        return String.format(DUAL_CONVERSATION_FORMAT_ID, id.toString(), currentUser.toString());
    }

    private String buildGroupConversationId() {
        return String.format(GROUP_CONVERSATION_FORMAT_ID, UUID.randomUUID().toString());
    }
}
