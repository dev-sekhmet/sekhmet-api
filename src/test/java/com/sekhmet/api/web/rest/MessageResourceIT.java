package com.sekhmet.api.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.sekhmet.api.IntegrationTest;
import com.sekhmet.api.domain.Message;
import com.sekhmet.api.repository.MessageRepository;
import com.sekhmet.api.service.EntityManager;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link MessageResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class MessageResourceIT {

    private static final UUID DEFAULT_UID = UUID.randomUUID();
    private static final UUID UPDATED_UID = UUID.randomUUID();

    private static final LocalDate DEFAULT_CREATED_AT = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CREATED_AT = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_IMAGE = "AAAAAAAAAA";
    private static final String UPDATED_IMAGE = "BBBBBBBBBB";

    private static final String DEFAULT_VIDEO = "AAAAAAAAAA";
    private static final String UPDATED_VIDEO = "BBBBBBBBBB";

    private static final String DEFAULT_AUDIO = "AAAAAAAAAA";
    private static final String UPDATED_AUDIO = "BBBBBBBBBB";

    private static final Boolean DEFAULT_SYSTEM = false;
    private static final Boolean UPDATED_SYSTEM = true;

    private static final Boolean DEFAULT_SENT = false;
    private static final Boolean UPDATED_SENT = true;

    private static final Boolean DEFAULT_RECEIVED = false;
    private static final Boolean UPDATED_RECEIVED = true;

    private static final Boolean DEFAULT_PENDING = false;
    private static final Boolean UPDATED_PENDING = true;

    private static final String ENTITY_API_URL = "/api/messages";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Message message;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Message createEntity(EntityManager em) {
        Message message = new Message()
            .uid(DEFAULT_UID)
            .createdAt(DEFAULT_CREATED_AT)
            .image(DEFAULT_IMAGE)
            .video(DEFAULT_VIDEO)
            .audio(DEFAULT_AUDIO)
            .system(DEFAULT_SYSTEM)
            .sent(DEFAULT_SENT)
            .received(DEFAULT_RECEIVED)
            .pending(DEFAULT_PENDING);
        return message;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Message createUpdatedEntity(EntityManager em) {
        Message message = new Message()
            .uid(UPDATED_UID)
            .createdAt(UPDATED_CREATED_AT)
            .image(UPDATED_IMAGE)
            .video(UPDATED_VIDEO)
            .audio(UPDATED_AUDIO)
            .system(UPDATED_SYSTEM)
            .sent(UPDATED_SENT)
            .received(UPDATED_RECEIVED)
            .pending(UPDATED_PENDING);
        return message;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Message.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        message = createEntity(em);
    }

    @Test
    void createMessage() throws Exception {
        int databaseSizeBeforeCreate = messageRepository.findAll().collectList().block().size();
        // Create the Message
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(message))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeCreate + 1);
        Message testMessage = messageList.get(messageList.size() - 1);
        assertThat(testMessage.getUid()).isEqualTo(DEFAULT_UID);
        assertThat(testMessage.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testMessage.getImage()).isEqualTo(DEFAULT_IMAGE);
        assertThat(testMessage.getVideo()).isEqualTo(DEFAULT_VIDEO);
        assertThat(testMessage.getAudio()).isEqualTo(DEFAULT_AUDIO);
        assertThat(testMessage.getSystem()).isEqualTo(DEFAULT_SYSTEM);
        assertThat(testMessage.getSent()).isEqualTo(DEFAULT_SENT);
        assertThat(testMessage.getReceived()).isEqualTo(DEFAULT_RECEIVED);
        assertThat(testMessage.getPending()).isEqualTo(DEFAULT_PENDING);
    }

    @Test
    void createMessageWithExistingId() throws Exception {
        // Create the Message with an existing ID
        message.setId(1L);

        int databaseSizeBeforeCreate = messageRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(message))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkUidIsRequired() throws Exception {
        int databaseSizeBeforeTest = messageRepository.findAll().collectList().block().size();
        // set the field null
        message.setUid(null);

        // Create the Message, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(message))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkCreatedAtIsRequired() throws Exception {
        int databaseSizeBeforeTest = messageRepository.findAll().collectList().block().size();
        // set the field null
        message.setCreatedAt(null);

        // Create the Message, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(message))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllMessages() {
        // Initialize the database
        messageRepository.save(message).block();

        // Get all the messageList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(message.getId().intValue()))
            .jsonPath("$.[*].uid")
            .value(hasItem(DEFAULT_UID.toString()))
            .jsonPath("$.[*].createdAt")
            .value(hasItem(DEFAULT_CREATED_AT.toString()))
            .jsonPath("$.[*].image")
            .value(hasItem(DEFAULT_IMAGE))
            .jsonPath("$.[*].video")
            .value(hasItem(DEFAULT_VIDEO))
            .jsonPath("$.[*].audio")
            .value(hasItem(DEFAULT_AUDIO))
            .jsonPath("$.[*].system")
            .value(hasItem(DEFAULT_SYSTEM.booleanValue()))
            .jsonPath("$.[*].sent")
            .value(hasItem(DEFAULT_SENT.booleanValue()))
            .jsonPath("$.[*].received")
            .value(hasItem(DEFAULT_RECEIVED.booleanValue()))
            .jsonPath("$.[*].pending")
            .value(hasItem(DEFAULT_PENDING.booleanValue()));
    }

    @Test
    void getMessage() {
        // Initialize the database
        messageRepository.save(message).block();

        // Get the message
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, message.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(message.getId().intValue()))
            .jsonPath("$.uid")
            .value(is(DEFAULT_UID.toString()))
            .jsonPath("$.createdAt")
            .value(is(DEFAULT_CREATED_AT.toString()))
            .jsonPath("$.image")
            .value(is(DEFAULT_IMAGE))
            .jsonPath("$.video")
            .value(is(DEFAULT_VIDEO))
            .jsonPath("$.audio")
            .value(is(DEFAULT_AUDIO))
            .jsonPath("$.system")
            .value(is(DEFAULT_SYSTEM.booleanValue()))
            .jsonPath("$.sent")
            .value(is(DEFAULT_SENT.booleanValue()))
            .jsonPath("$.received")
            .value(is(DEFAULT_RECEIVED.booleanValue()))
            .jsonPath("$.pending")
            .value(is(DEFAULT_PENDING.booleanValue()));
    }

    @Test
    void getNonExistingMessage() {
        // Get the message
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewMessage() throws Exception {
        // Initialize the database
        messageRepository.save(message).block();

        int databaseSizeBeforeUpdate = messageRepository.findAll().collectList().block().size();

        // Update the message
        Message updatedMessage = messageRepository.findById(message.getId()).block();
        updatedMessage
            .uid(UPDATED_UID)
            .createdAt(UPDATED_CREATED_AT)
            .image(UPDATED_IMAGE)
            .video(UPDATED_VIDEO)
            .audio(UPDATED_AUDIO)
            .system(UPDATED_SYSTEM)
            .sent(UPDATED_SENT)
            .received(UPDATED_RECEIVED)
            .pending(UPDATED_PENDING);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedMessage.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedMessage))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);
        Message testMessage = messageList.get(messageList.size() - 1);
        assertThat(testMessage.getUid()).isEqualTo(UPDATED_UID);
        assertThat(testMessage.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testMessage.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(testMessage.getVideo()).isEqualTo(UPDATED_VIDEO);
        assertThat(testMessage.getAudio()).isEqualTo(UPDATED_AUDIO);
        assertThat(testMessage.getSystem()).isEqualTo(UPDATED_SYSTEM);
        assertThat(testMessage.getSent()).isEqualTo(UPDATED_SENT);
        assertThat(testMessage.getReceived()).isEqualTo(UPDATED_RECEIVED);
        assertThat(testMessage.getPending()).isEqualTo(UPDATED_PENDING);
    }

    @Test
    void putNonExistingMessage() throws Exception {
        int databaseSizeBeforeUpdate = messageRepository.findAll().collectList().block().size();
        message.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, message.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(message))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchMessage() throws Exception {
        int databaseSizeBeforeUpdate = messageRepository.findAll().collectList().block().size();
        message.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(message))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamMessage() throws Exception {
        int databaseSizeBeforeUpdate = messageRepository.findAll().collectList().block().size();
        message.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(message))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateMessageWithPatch() throws Exception {
        // Initialize the database
        messageRepository.save(message).block();

        int databaseSizeBeforeUpdate = messageRepository.findAll().collectList().block().size();

        // Update the message using partial update
        Message partialUpdatedMessage = new Message();
        partialUpdatedMessage.setId(message.getId());

        partialUpdatedMessage.audio(UPDATED_AUDIO).system(UPDATED_SYSTEM).pending(UPDATED_PENDING);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedMessage.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedMessage))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);
        Message testMessage = messageList.get(messageList.size() - 1);
        assertThat(testMessage.getUid()).isEqualTo(DEFAULT_UID);
        assertThat(testMessage.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testMessage.getImage()).isEqualTo(DEFAULT_IMAGE);
        assertThat(testMessage.getVideo()).isEqualTo(DEFAULT_VIDEO);
        assertThat(testMessage.getAudio()).isEqualTo(UPDATED_AUDIO);
        assertThat(testMessage.getSystem()).isEqualTo(UPDATED_SYSTEM);
        assertThat(testMessage.getSent()).isEqualTo(DEFAULT_SENT);
        assertThat(testMessage.getReceived()).isEqualTo(DEFAULT_RECEIVED);
        assertThat(testMessage.getPending()).isEqualTo(UPDATED_PENDING);
    }

    @Test
    void fullUpdateMessageWithPatch() throws Exception {
        // Initialize the database
        messageRepository.save(message).block();

        int databaseSizeBeforeUpdate = messageRepository.findAll().collectList().block().size();

        // Update the message using partial update
        Message partialUpdatedMessage = new Message();
        partialUpdatedMessage.setId(message.getId());

        partialUpdatedMessage
            .uid(UPDATED_UID)
            .createdAt(UPDATED_CREATED_AT)
            .image(UPDATED_IMAGE)
            .video(UPDATED_VIDEO)
            .audio(UPDATED_AUDIO)
            .system(UPDATED_SYSTEM)
            .sent(UPDATED_SENT)
            .received(UPDATED_RECEIVED)
            .pending(UPDATED_PENDING);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedMessage.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedMessage))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);
        Message testMessage = messageList.get(messageList.size() - 1);
        assertThat(testMessage.getUid()).isEqualTo(UPDATED_UID);
        assertThat(testMessage.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testMessage.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(testMessage.getVideo()).isEqualTo(UPDATED_VIDEO);
        assertThat(testMessage.getAudio()).isEqualTo(UPDATED_AUDIO);
        assertThat(testMessage.getSystem()).isEqualTo(UPDATED_SYSTEM);
        assertThat(testMessage.getSent()).isEqualTo(UPDATED_SENT);
        assertThat(testMessage.getReceived()).isEqualTo(UPDATED_RECEIVED);
        assertThat(testMessage.getPending()).isEqualTo(UPDATED_PENDING);
    }

    @Test
    void patchNonExistingMessage() throws Exception {
        int databaseSizeBeforeUpdate = messageRepository.findAll().collectList().block().size();
        message.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, message.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(message))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchMessage() throws Exception {
        int databaseSizeBeforeUpdate = messageRepository.findAll().collectList().block().size();
        message.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(message))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamMessage() throws Exception {
        int databaseSizeBeforeUpdate = messageRepository.findAll().collectList().block().size();
        message.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(message))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteMessage() {
        // Initialize the database
        messageRepository.save(message).block();

        int databaseSizeBeforeDelete = messageRepository.findAll().collectList().block().size();

        // Delete the message
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, message.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Message> messageList = messageRepository.findAll().collectList().block();
        assertThat(messageList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
