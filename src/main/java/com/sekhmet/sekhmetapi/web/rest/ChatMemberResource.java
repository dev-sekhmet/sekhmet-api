package com.sekhmet.sekhmetapi.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.sekhmet.sekhmetapi.domain.ChatMember;
import com.sekhmet.sekhmetapi.repository.ChatMemberRepository;
import com.sekhmet.sekhmetapi.repository.search.ChatMemberSearchRepository;
import com.sekhmet.sekhmetapi.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.sekhmet.sekhmetapi.domain.ChatMember}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class ChatMemberResource {

    private final Logger log = LoggerFactory.getLogger(ChatMemberResource.class);

    private static final String ENTITY_NAME = "chatMember";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ChatMemberRepository chatMemberRepository;

    private final ChatMemberSearchRepository chatMemberSearchRepository;

    public ChatMemberResource(ChatMemberRepository chatMemberRepository, ChatMemberSearchRepository chatMemberSearchRepository) {
        this.chatMemberRepository = chatMemberRepository;
        this.chatMemberSearchRepository = chatMemberSearchRepository;
    }

    /**
     * {@code POST  /chat-members} : Create a new chatMember.
     *
     * @param chatMember the chatMember to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new chatMember, or with status {@code 400 (Bad Request)} if the chatMember has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/chat-members")
    public ResponseEntity<ChatMember> createChatMember(@Valid @RequestBody ChatMember chatMember) throws URISyntaxException {
        log.debug("REST request to save ChatMember : {}", chatMember);
        if (chatMember.getId() != null) {
            throw new BadRequestAlertException("A new chatMember cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ChatMember result = chatMemberRepository.save(chatMember);
        chatMemberSearchRepository.save(result);
        return ResponseEntity
            .created(new URI("/api/chat-members/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
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
    public ResponseEntity<ChatMember> updateChatMember(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody ChatMember chatMember
    ) throws URISyntaxException {
        log.debug("REST request to update ChatMember : {}, {}", id, chatMember);
        if (chatMember.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, chatMember.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!chatMemberRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ChatMember result = chatMemberRepository.save(chatMember);
        chatMemberSearchRepository.save(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, chatMember.getId().toString()))
            .body(result);
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
    public ResponseEntity<ChatMember> partialUpdateChatMember(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody ChatMember chatMember
    ) throws URISyntaxException {
        log.debug("REST request to partial update ChatMember partially : {}, {}", id, chatMember);
        if (chatMember.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, chatMember.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!chatMemberRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ChatMember> result = chatMemberRepository
            .findById(chatMember.getId())
            .map(existingChatMember -> {
                if (chatMember.getScope() != null) {
                    existingChatMember.setScope(chatMember.getScope());
                }

                return existingChatMember;
            })
            .map(chatMemberRepository::save)
            .map(savedChatMember -> {
                chatMemberSearchRepository.save(savedChatMember);

                return savedChatMember;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, chatMember.getId().toString())
        );
    }

    /**
     * {@code GET  /chat-members} : get all the chatMembers.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of chatMembers in body.
     */
    @GetMapping("/chat-members")
    public ResponseEntity<List<ChatMember>> getAllChatMembers(Pageable pageable) {
        log.debug("REST request to get a page of ChatMembers");
        Page<ChatMember> page = chatMemberRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /chat-members/:id} : get the "id" chatMember.
     *
     * @param id the id of the chatMember to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the chatMember, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/chat-members/{id}")
    public ResponseEntity<ChatMember> getChatMember(@PathVariable UUID id) {
        log.debug("REST request to get ChatMember : {}", id);
        Optional<ChatMember> chatMember = chatMemberRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(chatMember);
    }

    /**
     * {@code DELETE  /chat-members/:id} : delete the "id" chatMember.
     *
     * @param id the id of the chatMember to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/chat-members/{id}")
    public ResponseEntity<Void> deleteChatMember(@PathVariable UUID id) {
        log.debug("REST request to delete ChatMember : {}", id);
        chatMemberRepository.deleteById(id);
        chatMemberSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/chat-members?query=:query} : search for the chatMember corresponding
     * to the query.
     *
     * @param query the query of the chatMember search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/chat-members")
    public ResponseEntity<List<ChatMember>> searchChatMembers(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of ChatMembers for query {}", query);
        Page<ChatMember> page = chatMemberSearchRepository.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
