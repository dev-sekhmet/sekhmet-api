package com.sekhmet.sekhmetapi.service;

import com.sekhmet.sekhmetapi.domain.Message;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
public class ChatLiveService {

    private static final Logger log = LoggerFactory.getLogger(ChatLiveService.class);
    private final UserService userService;
    private final MessageService messageService;
    private final SimpMessageSendingOperations messagingTemplate;

    public ChatLiveService(UserService userService, MessageService messageService, SimpMessageSendingOperations messagingTemplate) {
        this.userService = userService;
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
    }

    public Message forwardMessageToChat(UUID chatId, Message message) {
        message.setUser(userService.getUserWithAuthorities().get());
        message.createdAt(LocalDateTime.now());
        message = messageService.save(message);
        log.debug("Sending user chat data {}", message);
        messagingTemplate.convertAndSend("/chat/" + chatId, message);
        return message;
    }
}
