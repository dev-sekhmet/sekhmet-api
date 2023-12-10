package com.sekhmet.sekhmetapi.web.rest;

import com.sekhmet.sekhmetapi.domain.User;
import com.sekhmet.sekhmetapi.security.AuthoritiesConstants;
import com.sekhmet.sekhmetapi.service.TwilioConversationService;
import com.sekhmet.sekhmetapi.service.UserService;
import com.sekhmet.sekhmetapi.service.dto.ConversationDto;
import com.twilio.rest.conversations.v1.Conversation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TwilioConversationResource {

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserService userService;
    private final TwilioConversationService conversationUserService;

    /**
     * {@code GET  /conversations/:id} : get the "id" chat.
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

    /**
     * {@code GET  /conversations} : get all chat.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the chat, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/conversations")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<Conversation>> getAllConversations(Pageable pageable) {
        log.info("REST request to get all Chat");
        var page = conversationUserService.getAllConversations(pageable);
        var headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * {@code GET  /conversations} : get all chat.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the chat, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/conversations/{sid}")
    public ResponseEntity<Void> deleteConversation(String sid) {
        log.info("REST request to delete Chat");
        conversationUserService.deleteConversation(sid);
        return ResponseEntity.noContent().headers(HeaderUtil.createAlert(applicationName, "conversation.deleted", sid)).build();
    }
}
