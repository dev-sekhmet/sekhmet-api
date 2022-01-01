package com.sekhmet.sekhmetapi.web.rest;

import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sekhmet.sekhmetapi.domain.Message;
import com.sekhmet.sekhmetapi.repository.MessageRepository;
import com.sekhmet.sekhmetapi.service.ChatLiveService;
import com.sekhmet.sekhmetapi.service.MessageService;
import com.sekhmet.sekhmetapi.service.S3Service;
import com.sekhmet.sekhmetapi.service.UserService;
import com.sekhmet.sekhmetapi.web.rest.errors.BadRequestAlertException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.sekhmet.sekhmetapi.domain.Message}.
 */
@RestController
@RequestMapping("/api")
public class MessageResource {

    private final Logger log = LoggerFactory.getLogger(MessageResource.class);

    private static final String ENTITY_NAME = "message";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MessageService messageService;
    private final ChatLiveService chatLiveService;
    private final S3Service s3Service;
    private final UserService userService;

    private final MessageRepository messageRepository;

    public MessageResource(
        MessageService messageService,
        ChatLiveService chatLiveService,
        S3Service s3Service,
        UserService userService,
        MessageRepository messageRepository
    ) {
        this.messageService = messageService;
        this.chatLiveService = chatLiveService;
        this.s3Service = s3Service;
        this.userService = userService;
        this.messageRepository = messageRepository;
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
        Message result = messageService.save(message);
        return ResponseEntity
            .created(new URI("/api/messages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code POST  /messages} : Create a new message with media
     *
     * @param messageStr the message to create.
     * @param file       media file
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new message, or with status {@code 400 (Bad Request)} if the message has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/messages/media")
    public ResponseEntity<Message> createMessageWithMedia(@RequestParam("message") String messageStr, @RequestParam MultipartFile file)
        throws URISyntaxException, JsonProcessingException {
        log.debug("REST request to save Message : {}", messageStr);
        Message message = new ObjectMapper().readValue(messageStr, Message.class);
        if (message.getId() != null) {
            throw new BadRequestAlertException("A new message cannot already have an ID", ENTITY_NAME, "idexists");
        }
        // save to have image id
        message.setUser(userService.getUserWithAuthorities().get());
        Message result = messageService.save(message);

        // save media file to s3
        UUID chatId = result.getChat().getId();
        S3Service.PutResult putResult = s3Service.putMedia(chatId.toString(), file);

        // set url to message en forward message
        result.setMedia(putResult.getFileType(), putResult.getKey());
        result = chatLiveService.forwardMessageToChat(chatId, result);
        return ResponseEntity
            .created(new URI("/api/messages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /messages/:messageId/:fileType/:fileId} : get the file.
     *
     * @param fileId the id of the message media to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the message, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/messages/media/chat-content/{chatId}/{fileType}/{fileId}")
    public ResponseEntity<byte[]> getMessageMedia(@PathVariable String chatId, @PathVariable String fileType, @PathVariable String fileId) {
        log.debug("REST request to get Media : {}", fileId);

        S3Object media = s3Service.getMedia(chatId, fileType, fileId);
        try (InputStream in = media.getObjectContent()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(in, baos);
            byte[] fileBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaTypes(media.getObjectMetadata().getContentType()).get(0));
            headers.setContentLength(fileBytes.length);
            return ResponseUtil.wrapOrNotFound(Optional.of(fileBytes), headers);
        } catch (IOException e) {
            log.error("IO error ", e);
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
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

        Message result = messageService.save(message);
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

        Optional<Message> result = messageService.partialUpdate(message);

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
        Page<Message> page = messageService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /messages} : get all the messages by chat
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of messages in body.
     */
    @GetMapping("/messages/chat/{chatId}")
    public ResponseEntity<List<Message>> getAllByChatMessages(
        @PathVariable("chatId") UUID chatId,
        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("REST request to get a page of Messages");
        Page<Message> page = messageService.findAll(chatId, pageable);
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
        Optional<Message> message = messageService.findOne(id);
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
        messageService.delete(id);
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
        Page<Message> page = messageService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
