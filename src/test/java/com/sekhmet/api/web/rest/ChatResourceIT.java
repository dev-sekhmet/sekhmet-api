package com.sekhmet.api.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.sekhmet.api.IntegrationTest;
import com.sekhmet.api.domain.Chat;
import com.sekhmet.api.repository.ChatRepository;
import com.sekhmet.api.service.EntityManager;
import java.time.Duration;
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
 * Integration tests for the {@link ChatResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class ChatResourceIT {

    private static final UUID DEFAULT_GUID = UUID.randomUUID();
    private static final UUID UPDATED_GUID = UUID.randomUUID();

    private static final String DEFAULT_ICON = "AAAAAAAAAA";
    private static final String UPDATED_ICON = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/chats";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Chat chat;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Chat createEntity(EntityManager em) {
        Chat chat = new Chat().guid(DEFAULT_GUID).icon(DEFAULT_ICON).name(DEFAULT_NAME);
        return chat;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Chat createUpdatedEntity(EntityManager em) {
        Chat chat = new Chat().guid(UPDATED_GUID).icon(UPDATED_ICON).name(UPDATED_NAME);
        return chat;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Chat.class).block();
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
        chat = createEntity(em);
    }

    @Test
    void createChat() throws Exception {
        int databaseSizeBeforeCreate = chatRepository.findAll().collectList().block().size();
        // Create the Chat
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(chat))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeCreate + 1);
        Chat testChat = chatList.get(chatList.size() - 1);
        assertThat(testChat.getGuid()).isEqualTo(DEFAULT_GUID);
        assertThat(testChat.getIcon()).isEqualTo(DEFAULT_ICON);
        assertThat(testChat.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    void createChatWithExistingId() throws Exception {
        // Create the Chat with an existing ID
        chat.setId(1L);

        int databaseSizeBeforeCreate = chatRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(chat))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkGuidIsRequired() throws Exception {
        int databaseSizeBeforeTest = chatRepository.findAll().collectList().block().size();
        // set the field null
        chat.setGuid(null);

        // Create the Chat, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(chat))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllChats() {
        // Initialize the database
        chatRepository.save(chat).block();

        // Get all the chatList
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
            .value(hasItem(chat.getId().intValue()))
            .jsonPath("$.[*].guid")
            .value(hasItem(DEFAULT_GUID.toString()))
            .jsonPath("$.[*].icon")
            .value(hasItem(DEFAULT_ICON))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));
    }

    @Test
    void getChat() {
        // Initialize the database
        chatRepository.save(chat).block();

        // Get the chat
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, chat.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(chat.getId().intValue()))
            .jsonPath("$.guid")
            .value(is(DEFAULT_GUID.toString()))
            .jsonPath("$.icon")
            .value(is(DEFAULT_ICON))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME));
    }

    @Test
    void getNonExistingChat() {
        // Get the chat
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewChat() throws Exception {
        // Initialize the database
        chatRepository.save(chat).block();

        int databaseSizeBeforeUpdate = chatRepository.findAll().collectList().block().size();

        // Update the chat
        Chat updatedChat = chatRepository.findById(chat.getId()).block();
        updatedChat.guid(UPDATED_GUID).icon(UPDATED_ICON).name(UPDATED_NAME);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedChat.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedChat))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
        Chat testChat = chatList.get(chatList.size() - 1);
        assertThat(testChat.getGuid()).isEqualTo(UPDATED_GUID);
        assertThat(testChat.getIcon()).isEqualTo(UPDATED_ICON);
        assertThat(testChat.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    void putNonExistingChat() throws Exception {
        int databaseSizeBeforeUpdate = chatRepository.findAll().collectList().block().size();
        chat.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, chat.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(chat))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchChat() throws Exception {
        int databaseSizeBeforeUpdate = chatRepository.findAll().collectList().block().size();
        chat.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(chat))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamChat() throws Exception {
        int databaseSizeBeforeUpdate = chatRepository.findAll().collectList().block().size();
        chat.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(chat))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateChatWithPatch() throws Exception {
        // Initialize the database
        chatRepository.save(chat).block();

        int databaseSizeBeforeUpdate = chatRepository.findAll().collectList().block().size();

        // Update the chat using partial update
        Chat partialUpdatedChat = new Chat();
        partialUpdatedChat.setId(chat.getId());

        partialUpdatedChat.guid(UPDATED_GUID).name(UPDATED_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedChat.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedChat))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
        Chat testChat = chatList.get(chatList.size() - 1);
        assertThat(testChat.getGuid()).isEqualTo(UPDATED_GUID);
        assertThat(testChat.getIcon()).isEqualTo(DEFAULT_ICON);
        assertThat(testChat.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    void fullUpdateChatWithPatch() throws Exception {
        // Initialize the database
        chatRepository.save(chat).block();

        int databaseSizeBeforeUpdate = chatRepository.findAll().collectList().block().size();

        // Update the chat using partial update
        Chat partialUpdatedChat = new Chat();
        partialUpdatedChat.setId(chat.getId());

        partialUpdatedChat.guid(UPDATED_GUID).icon(UPDATED_ICON).name(UPDATED_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedChat.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedChat))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
        Chat testChat = chatList.get(chatList.size() - 1);
        assertThat(testChat.getGuid()).isEqualTo(UPDATED_GUID);
        assertThat(testChat.getIcon()).isEqualTo(UPDATED_ICON);
        assertThat(testChat.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    void patchNonExistingChat() throws Exception {
        int databaseSizeBeforeUpdate = chatRepository.findAll().collectList().block().size();
        chat.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, chat.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(chat))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchChat() throws Exception {
        int databaseSizeBeforeUpdate = chatRepository.findAll().collectList().block().size();
        chat.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(chat))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamChat() throws Exception {
        int databaseSizeBeforeUpdate = chatRepository.findAll().collectList().block().size();
        chat.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(chat))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Chat in the database
        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteChat() {
        // Initialize the database
        chatRepository.save(chat).block();

        int databaseSizeBeforeDelete = chatRepository.findAll().collectList().block().size();

        // Delete the chat
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, chat.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Chat> chatList = chatRepository.findAll().collectList().block();
        assertThat(chatList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
