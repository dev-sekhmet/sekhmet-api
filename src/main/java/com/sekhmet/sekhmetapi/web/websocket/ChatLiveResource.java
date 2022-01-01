package com.sekhmet.sekhmetapi.web.websocket;

import static com.sekhmet.sekhmetapi.config.WebsocketConfiguration.IP_ADDRESS;

import com.sekhmet.sekhmetapi.domain.Chat;
import com.sekhmet.sekhmetapi.domain.Message;
import com.sekhmet.sekhmetapi.domain.User;
import com.sekhmet.sekhmetapi.security.SecurityUtils;
import com.sekhmet.sekhmetapi.service.ChatLiveService;
import com.sekhmet.sekhmetapi.service.ChatService;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
public class ChatLiveResource implements ApplicationListener<SessionDisconnectEvent> {

    private static final Logger log = LoggerFactory.getLogger(ChatLiveResource.class);

    private final SimpMessageSendingOperations messagingTemplate;

    private final ChatService chatService;
    private final ChatLiveService chatLiveService;

    public ChatLiveResource(SimpMessageSendingOperations messagingTemplate, ChatService chatService, ChatLiveService chatLiveService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.chatLiveService = chatLiveService;
    }

    @SubscribeMapping("/chat/{chatId}")
    public void subscribe(@DestinationVariable("chatId") UUID chatId, StompHeaderAccessor stompHeaderAccessor, Principal principal) {
        String login = SecurityUtils.getCurrentUserLogin().orElse("anonymoususer");
        String ipAddress = stompHeaderAccessor.getSessionAttributes().get(IP_ADDRESS).toString();
        Chat chat = chatService.findOne(chatId).get();
        log.debug("User {} subscribed to Chat from IP {}", login, ipAddress);
        Message messageDTO = new Message();
        messageDTO.setUser(new User().login("system"));
        messageDTO.createdAt(LocalDateTime.now());
        messageDTO.setChat(chat);
        messageDTO.setText(login + " joined the chat");
        messagingTemplate.convertAndSend("/chat/" + messageDTO.getChat().getId(), messageDTO);
    }

    @MessageMapping("/chat/{chatId}/sent")
    public void sendChat(@DestinationVariable("chatId") UUID chatId, @Payload Message message) {
        chatLiveService.forwardMessageToChat(chatId, message);
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        // when the user disconnects, send a message saying that hey are leaving
        log.info("{} disconnected from the chat websockets", event.getUser().getName());
        /*       Message messageDTO = new Message();
        messageDTO.setUser(new User().login("system"));
        messageDTO.createdAt(LocalDateTime.now());
        messageDTO.setText(event.getUser().getName() + " left the chat");
        messagingTemplate.convertAndSend("/chat/" + messageDTO.getChat().getId(), messageDTO);*/
    }
}
