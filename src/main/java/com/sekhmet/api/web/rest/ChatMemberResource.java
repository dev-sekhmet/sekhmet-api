package com.sekhmet.api.web.rest;

import com.sekhmet.api.domain.ChatMember;
import com.sekhmet.api.repository.ChatMemberRepository;
import com.sekhmet.api.service.ChatMemberService;
import com.sekhmet.api.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.sekhmet.api.domain.ChatMember}.
 */
@RestController
@RequestMapping("/api")
public class ChatMemberResource {

    private final Logger log = LoggerFactory.getLogger(ChatMemberResource.class);

    private static final String ENTITY_NAME = "chatMember";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ChatMemberService chatMemberService;

    private final ChatMemberRepository chatMemberRepository;

    public ChatMemberResource(ChatMemberService chatMemberService, ChatMemberRepository chatMemberRepository) {
        this.chatMemberService = chatMemberService;
        this.chatMemberRepository = chatMemberRepository;
    }

    /**
     * {@code POST  /chat-members} : Create a new chatMember.
     *
     * @param chatMember the chatMember to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new chatMember, or with status {@code 400 (Bad Request)} if the chatMember has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/chat-members")
    public Mono<ResponseEntity<ChatMember>> createChatMember(@Valid @RequestBody ChatMember chatMember) throws URISyntaxException {
        log.debug("REST request to save ChatMember : {}", chatMember);
        if (chatMember.getId() != null) {
            throw new BadRequestAlertException("A new chatMember cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return chatMemberService
            .save(chatMember)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/chat-members/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /chat-members/:id} : Updates an existing chatMember.
     *
     * @param id the id of the chatMember to save.
     * @param chatMember the chatMember to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated chatMember,
     * or with status {@code 400 (Bad Request)} if the chatMember is not valid,
     * or with status {@code 500 (Internal Server Error)} if the chatMember couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/chat-members/{id}")
    public Mono<ResponseEntity<ChatMember>> updateChatMember(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ChatMember chatMember
    ) throws URISyntaxException {
        log.debug("REST request to update ChatMember : {}, {}", id, chatMember);
        if (chatMember.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, chatMember.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return chatMemberRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return chatMemberService
                    .save(chatMember)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /chat-members/:id} : Partial updates given fields of an existing chatMember, field will ignore if it is null
     *
     * @param id the id of the chatMember to save.
     * @param chatMember the chatMember to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated chatMember,
     * or with status {@code 400 (Bad Request)} if the chatMember is not valid,
     * or with status {@code 404 (Not Found)} if the chatMember is not found,
     * or with status {@code 500 (Internal Server Error)} if the chatMember couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/chat-members/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<ChatMember>> partialUpdateChatMember(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ChatMember chatMember
    ) throws URISyntaxException {
        log.debug("REST request to partial update ChatMember partially : {}, {}", id, chatMember);
        if (chatMember.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, chatMember.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return chatMemberRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<ChatMember> result = chatMemberService.partialUpdate(chatMember);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /chat-members} : get all the chatMembers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of chatMembers in body.
     */
    @GetMapping("/chat-members")
    public Mono<List<ChatMember>> getAllChatMembers() {
        log.debug("REST request to get all ChatMembers");
        return chatMemberService.findAll().collectList();
    }

    /**
     * {@code GET  /chat-members} : get all the chatMembers as a stream.
     * @return the {@link Flux} of chatMembers.
     */
    @GetMapping(value = "/chat-members", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ChatMember> getAllChatMembersAsStream() {
        log.debug("REST request to get all ChatMembers as a stream");
        return chatMemberService.findAll();
    }

    /**
     * {@code GET  /chat-members/:id} : get the "id" chatMember.
     *
     * @param id the id of the chatMember to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the chatMember, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/chat-members/{id}")
    public Mono<ResponseEntity<ChatMember>> getChatMember(@PathVariable Long id) {
        log.debug("REST request to get ChatMember : {}", id);
        Mono<ChatMember> chatMember = chatMemberService.findOne(id);
        return ResponseUtil.wrapOrNotFound(chatMember);
    }

    /**
     * {@code DELETE  /chat-members/:id} : delete the "id" chatMember.
     *
     * @param id the id of the chatMember to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/chat-members/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteChatMember(@PathVariable Long id) {
        log.debug("REST request to delete ChatMember : {}", id);
        return chatMemberService
            .delete(id)
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                    .build()
            );
    }
}
