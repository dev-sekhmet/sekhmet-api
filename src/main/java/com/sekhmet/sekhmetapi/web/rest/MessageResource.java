package com.sekhmet.sekhmetapi.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.sekhmet.sekhmetapi.domain.Message;
import com.sekhmet.sekhmetapi.repository.MessageRepository;
import com.sekhmet.sekhmetapi.repository.search.MessageSearchRepository;
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
 * REST controller for managing {@link com.sekhmet.sekhmetapi.domain.Message}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class MessageResource {

    private final Logger log = LoggerFactory.getLogger(MessageResource.class);

    private static final String ENTITY_NAME = "message";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MessageRepository messageRepository;

    private final MessageSearchRepository messageSearchRepository;

    public MessageResource(MessageRepository messageRepository, MessageSearchRepository messageSearchRepository) {
        this.messageRepository = messageRepository;
        this.messageSearchRepository = messageSearchRepository;
    }

    /**
     * {@code POST  /messages} : Create a new message.
     *
     * @param message the message to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new message, or with status {@code 400 (Bad Request)} if the message has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/messages")
    public ResponseEntity<Message> createMessage(@Valid @RequestBody Message message) throws URISyntaxException {
        log.debug("REST request to save Message : {}", message);
        if (message.getId() != null) {
            throw new BadRequestAlertException("A new message cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Message result = messageRepository.save(message);
        messageSearchRepository.save(result);
        return ResponseEntity
            .created(new URI("/api/messages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /messages/:id} : Updates an existing message.
     *
     * @param id the id of the message to save.
     * @param message the message to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated message,
     * or with status {@code 400 (Bad Request)} if the message is not valid,
     * or with status {@code 500 (Internal Server Error)} if the message couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/messages/{id}")
    public ResponseEntity<Message> updateMessage(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody Message message
    ) throws URISyntaxException {
        log.debug("REST request to update Message : {}, {}", id, message);
        if (message.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, message.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!messageRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Message result = messageRepository.save(message);
        messageSearchRepository.save(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, message.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /messages/:id} : Partial updates given fields of an existing message, field will ignore if it is null
     *
     * @param id the id of the message to save.
     * @param message the message to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated message,
     * or with status {@code 400 (Bad Request)} if the message is not valid,
     * or with status {@code 404 (Not Found)} if the message is not found,
     * or with status {@code 500 (Internal Server Error)} if the message couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/messages/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Message> partialUpdateMessage(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody Message message
    ) throws URISyntaxException {
        log.debug("REST request to partial update Message partially : {}, {}", id, message);
        if (message.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, message.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!messageRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Message> result = messageRepository
            .findById(message.getId())
            .map(existingMessage -> {
                if (message.getText() != null) {
                    existingMessage.setText(message.getText());
                }
                if (message.getCreatedAt() != null) {
                    existingMessage.setCreatedAt(message.getCreatedAt());
                }
                if (message.getImage() != null) {
                    existingMessage.setImage(message.getImage());
                }
                if (message.getVideo() != null) {
                    existingMessage.setVideo(message.getVideo());
                }
                if (message.getAudio() != null) {
                    existingMessage.setAudio(message.getAudio());
                }
                if (message.getSystem() != null) {
                    existingMessage.setSystem(message.getSystem());
                }
                if (message.getSent() != null) {
                    existingMessage.setSent(message.getSent());
                }
                if (message.getReceived() != null) {
                    existingMessage.setReceived(message.getReceived());
                }
                if (message.getPending() != null) {
                    existingMessage.setPending(message.getPending());
                }

                return existingMessage;
            })
            .map(messageRepository::save)
            .map(savedMessage -> {
                messageSearchRepository.save(savedMessage);

                return savedMessage;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, message.getId().toString())
        );
    }

    /**
     * {@code GET  /messages} : get all the messages.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of messages in body.
     */
    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getAllMessages(Pageable pageable) {
        log.debug("REST request to get a page of Messages");
        Page<Message> page = messageRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /messages/:id} : get the "id" message.
     *
     * @param id the id of the message to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the message, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/messages/{id}")
    public ResponseEntity<Message> getMessage(@PathVariable UUID id) {
        log.debug("REST request to get Message : {}", id);
        Optional<Message> message = messageRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(message);
    }

    /**
     * {@code DELETE  /messages/:id} : delete the "id" message.
     *
     * @param id the id of the message to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID id) {
        log.debug("REST request to delete Message : {}", id);
        messageRepository.deleteById(id);
        messageSearchRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/messages?query=:query} : search for the message corresponding
     * to the query.
     *
     * @param query the query of the message search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/messages")
    public ResponseEntity<List<Message>> searchMessages(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Messages for query {}", query);
        Page<Message> page = messageSearchRepository.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
