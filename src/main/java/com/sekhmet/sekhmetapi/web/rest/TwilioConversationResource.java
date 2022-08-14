package com.sekhmet.sekhmetapi.web.rest;

import com.sekhmet.sekhmetapi.domain.User;
import com.sekhmet.sekhmetapi.service.TwilioConversationService;
import com.sekhmet.sekhmetapi.service.UserService;
import com.sekhmet.sekhmetapi.service.dto.ConversationDto;
import com.twilio.rest.conversations.v1.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TwilioConversationResource {

    private final UserService userService;
    private final TwilioConversationService conversationUserService;

    /**
     * {@code GET  /chats/:id} : get the "id" chat.
     *
     * @param conversationDto information on the chat to be created.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the chat, or with status {@code 404 (Not Found)}.
     */
    @PostMapping("/conversations")
    public ResponseEntity<Conversation> findOrCreateConversationDual(@RequestBody ConversationDto conversationDto) {
        log.info("REST request to create or get Chat : {}", conversationDto);
        User user = userService.getUserWithAuthorities().get();
        var conversationOptional = conversationUserService.findOrCreateConversation(conversationDto, user);
        return ResponseUtil.wrapOrNotFound(conversationOptional);
    }
}
