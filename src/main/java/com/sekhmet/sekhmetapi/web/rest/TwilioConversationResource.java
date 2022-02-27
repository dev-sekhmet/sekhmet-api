package com.sekhmet.sekhmetapi.web.rest;

import com.sekhmet.sekhmetapi.domain.User;
import com.sekhmet.sekhmetapi.service.TwilioConversationService;
import com.sekhmet.sekhmetapi.service.UserService;
import com.twilio.rest.conversations.v1.Conversation;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
     * @param id the id of the chat to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the chat, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/conversations/{id}/user")
    public ResponseEntity<Conversation> findOrCreateConversationDual(@PathVariable UUID id) {
        log.debug("REST request to get Chat : {}", id);
        User user = userService.getUserWithAuthorities().get();
        Optional<Conversation> conversationOptional = conversationUserService.findOrCreateConversationDual(id, user.getId());
        return ResponseUtil.wrapOrNotFound(conversationOptional);
    }
}
