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
    private final ObjectMapper objectMapper;
    private final ApplicationProperties.SmsProperties.TwilioPreperties smsProps;

    public TwilioConversationService(UserService userService, ApplicationProperties applicationProperties) {
        this.smsProps = applicationProperties.getSms().getTwilio();
        this.userService = userService;
        objectMapper = new ObjectMapper();
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
                    updateTwilioUser(user);
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
                    .setAttributes(objectMapper.writeValueAsString(user))
                    .create();
            log.info("Conv twilio user created Sucessfully: {} - {}", userConTwilio.getIdentity(), userConTwilio.getFriendlyName());
        } catch (JsonProcessingException e) {
            log.info("Could not parse twilio attributs");
        }
    }

    private void updateTwilioUser(com.sekhmet.sekhmetapi.domain.User user) {
        User userConTwilio;
        try {
            if (user.getImageUrl() == null) {
                user.setImageUrl("https://i.pravatar.cc/300");
            }
            userConTwilio =
                User
                    .updater(user.getId().toString())
                    .setFriendlyName(user.getFirstName() + " " + user.getLastName())
                    .setAttributes(objectMapper.writeValueAsString(user))
                    .update();
            log.info("Conv twilio Updated created Sucessfully: {} - {}", userConTwilio.getIdentity(), userConTwilio.getFriendlyName());
        } catch (JsonProcessingException e) {
            log.info("Could not parse twilio attributs");
        }
    }

    private boolean isDual(List<UUID> ids) {
        return ids.size() == 1; // current user id plus one other user id
    }

    public Optional<Conversation> findOrCreateConversation(
        ConversationDto conversationDto,
        com.sekhmet.sekhmetapi.domain.User currentUser
    ) {
        if (isDual(conversationDto.getIds())) {
            return findOrCreateConversationDual(conversationDto.getIds().get(0), currentUser);
        } else {
            return createConversationGroup(conversationDto, currentUser);
        }
    }

    private Optional<Conversation> createConversationGroup(
        ConversationDto conversationDto,
        com.sekhmet.sekhmetapi.domain.User currentUser
    ) {
        Conversation conversation = null;
        try {
            conversation =
                Conversation
                    .creator()
                    .setUniqueName(buildGroupConversationId())
                    .setAttributes(objectMapper.writeValueAsString(Map.of("description", conversationDto.getDescription())))
                    .setFriendlyName(conversationDto.getFriendlyName())
                    .create();
        } catch (JsonProcessingException e) {
            log.info("Could not parse twilio attributs");
        }

        if (conversation != null) {
            Conversation finalConversation = conversation;
            createParticipant(currentUser, conversation, smsProps.getChannelAdminSid());
            conversationDto
                .getIds()
                .forEach(id -> {
                    var participant = userService
                        .getUserById(id)
                        .map(user -> createParticipant(user, finalConversation, smsProps.getChannelUserSid()));
                    if (participant.isPresent()) {
                        log.info(
                            "Conv twilio user {} added to conversation {}",
                            participant.get().getIdentity(),
                            finalConversation.getSid()
                        );
                    } else {
                        log.info("User {} already cannot be add to conversation {}", id, finalConversation.getSid());
                    }
                });
            log.info("twilio Conversation created Successfully: {} - {}", conversation.getUniqueName(), conversation.getFriendlyName());
        } else {
            log.error("Could not create conversation");
        }
        return Optional.ofNullable(conversation);
    }

    private Participant createParticipant(com.sekhmet.sekhmetapi.domain.User currentUser, Conversation finalConversation, String roleSid) {
        try {
            return Participant
                .creator(finalConversation.getSid())
                .setIdentity(currentUser.getId().toString())
                .setAttributes(objectMapper.writeValueAsString(currentUser))
                .setRoleSid(roleSid)
                .create();
        } catch (JsonProcessingException ex) {
            log.error("Could not parse twilio attributs: {}", ex.getMessage(), ex);
        }
        return null;
    }

    public Optional<Conversation> findOrCreateConversationDual(UUID userId, com.sekhmet.sekhmetapi.domain.User currentUser) {
        Optional<Conversation> conversation = findConversationDual(userId, currentUser.getId());
        if (conversation.isEmpty()) {
            conversation = findConversationDual(currentUser.getId(), userId);
            if (conversation.isEmpty()) {
                String pathSid = buildDualConversationId(userId, currentUser.getId());
                return Optional.of(createConversationDual(userId, currentUser, pathSid));
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

    private Conversation createConversationDual(UUID userId, com.sekhmet.sekhmetapi.domain.User currentUser, String pathSid) {
        Optional<com.sekhmet.sekhmetapi.domain.User> userOptional = userService.getUserById(userId);
        Conversation conversation = null;
        if (userOptional.isPresent()) {
            com.sekhmet.sekhmetapi.domain.User user = userOptional.get();
            Map<String, Object> conversationNames = buildConversationAttributes(user, currentUser);
            try {
                conversation =
                    Conversation
                        .creator()
                        .setUniqueName(pathSid)
                        .setAttributes(objectMapper.writeValueAsString(conversationNames))
                        .setFriendlyName(user.getFirstName() + "/" + currentUser.getFirstName())
                        .create();
            } catch (JsonProcessingException e) {
                log.info("Could not parse twilio attributs");
            }

            if (conversation != null) {
                // create first User
                Participant.creator(conversation.getSid()).setIdentity(userId.toString()).create();

                // create second User
                Participant.creator(conversation.getSid()).setIdentity(currentUser.getId().toString()).create();
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
        return Map.of(user.getId().toString(), buildUserAttributes(currentUser), currentUser.getId().toString(), buildUserAttributes(user));
    }

    private Map<String, String> buildUserAttributes(com.sekhmet.sekhmetapi.domain.User user) {
        return Map.of("friendlyName", user.getFirstName() + " " + user.getLastName(), "imageUrl", getImageUrl(user));
    }

    private String getImageUrl(com.sekhmet.sekhmetapi.domain.User user) {
        return user.getImageUrl() != null ? user.getImageUrl() : "";
    }

    private String buildDualConversationId(UUID id, UUID currentUser) {
        return String.format(DUAL_CONVERSATION_FORMAT_ID, id.toString(), currentUser.toString());
    }

    private String buildGroupConversationId() {
        return String.format(GROUP_CONVERSATION_FORMAT_ID, UUID.randomUUID().toString());
    }

    public void deleter() {
        Conversation.deleter("CH52cb0f47a1434304b92b6031d1078c0f").delete();
    }
}
