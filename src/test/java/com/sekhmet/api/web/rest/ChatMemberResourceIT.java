package com.sekhmet.api.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.sekhmet.api.IntegrationTest;
import com.sekhmet.api.domain.ChatMember;
import com.sekhmet.api.domain.enumeration.ChatMemberScope;
import com.sekhmet.api.repository.ChatMemberRepository;
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
 * Integration tests for the {@link ChatMemberResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class ChatMemberResourceIT {

    private static final UUID DEFAULT_UID = UUID.randomUUID();
    private static final UUID UPDATED_UID = UUID.randomUUID();

    private static final ChatMemberScope DEFAULT_SCOPE = ChatMemberScope.PARTICIPANT;
    private static final ChatMemberScope UPDATED_SCOPE = ChatMemberScope.ADMIN;

    private static final String ENTITY_API_URL = "/api/chat-members";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private ChatMember chatMember;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ChatMember createEntity(EntityManager em) {
        ChatMember chatMember = new ChatMember().uid(DEFAULT_UID).scope(DEFAULT_SCOPE);
        return chatMember;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ChatMember createUpdatedEntity(EntityManager em) {
        ChatMember chatMember = new ChatMember().uid(UPDATED_UID).scope(UPDATED_SCOPE);
        return chatMember;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(ChatMember.class).block();
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
        chatMember = createEntity(em);
    }

    @Test
    void createChatMember() throws Exception {
        int databaseSizeBeforeCreate = chatMemberRepository.findAll().collectList().block().size();
        // Create the ChatMember
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(chatMember))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeCreate + 1);
        ChatMember testChatMember = chatMemberList.get(chatMemberList.size() - 1);
        assertThat(testChatMember.getUid()).isEqualTo(DEFAULT_UID);
        assertThat(testChatMember.getScope()).isEqualTo(DEFAULT_SCOPE);
    }

    @Test
    void createChatMemberWithExistingId() throws Exception {
        // Create the ChatMember with an existing ID
        chatMember.setId(1L);

        int databaseSizeBeforeCreate = chatMemberRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(chatMember))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkUidIsRequired() throws Exception {
        int databaseSizeBeforeTest = chatMemberRepository.findAll().collectList().block().size();
        // set the field null
        chatMember.setUid(null);

        // Create the ChatMember, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(chatMember))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllChatMembersAsStream() {
        // Initialize the database
        chatMemberRepository.save(chatMember).block();

        List<ChatMember> chatMemberList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(ChatMember.class)
            .getResponseBody()
            .filter(chatMember::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(chatMemberList).isNotNull();
        assertThat(chatMemberList).hasSize(1);
        ChatMember testChatMember = chatMemberList.get(0);
        assertThat(testChatMember.getUid()).isEqualTo(DEFAULT_UID);
        assertThat(testChatMember.getScope()).isEqualTo(DEFAULT_SCOPE);
    }

    @Test
    void getAllChatMembers() {
        // Initialize the database
        chatMemberRepository.save(chatMember).block();

        // Get all the chatMemberList
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
            .value(hasItem(chatMember.getId().intValue()))
            .jsonPath("$.[*].uid")
            .value(hasItem(DEFAULT_UID.toString()))
            .jsonPath("$.[*].scope")
            .value(hasItem(DEFAULT_SCOPE.toString()));
    }

    @Test
    void getChatMember() {
        // Initialize the database
        chatMemberRepository.save(chatMember).block();

        // Get the chatMember
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, chatMember.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(chatMember.getId().intValue()))
            .jsonPath("$.uid")
            .value(is(DEFAULT_UID.toString()))
            .jsonPath("$.scope")
            .value(is(DEFAULT_SCOPE.toString()));
    }

    @Test
    void getNonExistingChatMember() {
        // Get the chatMember
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewChatMember() throws Exception {
        // Initialize the database
        chatMemberRepository.save(chatMember).block();

        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().collectList().block().size();

        // Update the chatMember
        ChatMember updatedChatMember = chatMemberRepository.findById(chatMember.getId()).block();
        updatedChatMember.uid(UPDATED_UID).scope(UPDATED_SCOPE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedChatMember.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedChatMember))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);
        ChatMember testChatMember = chatMemberList.get(chatMemberList.size() - 1);
        assertThat(testChatMember.getUid()).isEqualTo(UPDATED_UID);
        assertThat(testChatMember.getScope()).isEqualTo(UPDATED_SCOPE);
    }

    @Test
    void putNonExistingChatMember() throws Exception {
        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().collectList().block().size();
        chatMember.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, chatMember.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(chatMember))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchChatMember() throws Exception {
        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().collectList().block().size();
        chatMember.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(chatMember))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamChatMember() throws Exception {
        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().collectList().block().size();
        chatMember.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(chatMember))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateChatMemberWithPatch() throws Exception {
        // Initialize the database
        chatMemberRepository.save(chatMember).block();

        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().collectList().block().size();

        // Update the chatMember using partial update
        ChatMember partialUpdatedChatMember = new ChatMember();
        partialUpdatedChatMember.setId(chatMember.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedChatMember.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedChatMember))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);
        ChatMember testChatMember = chatMemberList.get(chatMemberList.size() - 1);
        assertThat(testChatMember.getUid()).isEqualTo(DEFAULT_UID);
        assertThat(testChatMember.getScope()).isEqualTo(DEFAULT_SCOPE);
    }

    @Test
    void fullUpdateChatMemberWithPatch() throws Exception {
        // Initialize the database
        chatMemberRepository.save(chatMember).block();

        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().collectList().block().size();

        // Update the chatMember using partial update
        ChatMember partialUpdatedChatMember = new ChatMember();
        partialUpdatedChatMember.setId(chatMember.getId());

        partialUpdatedChatMember.uid(UPDATED_UID).scope(UPDATED_SCOPE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedChatMember.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedChatMember))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);
        ChatMember testChatMember = chatMemberList.get(chatMemberList.size() - 1);
        assertThat(testChatMember.getUid()).isEqualTo(UPDATED_UID);
        assertThat(testChatMember.getScope()).isEqualTo(UPDATED_SCOPE);
    }

    @Test
    void patchNonExistingChatMember() throws Exception {
        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().collectList().block().size();
        chatMember.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, chatMember.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(chatMember))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchChatMember() throws Exception {
        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().collectList().block().size();
        chatMember.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(chatMember))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamChatMember() throws Exception {
        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().collectList().block().size();
        chatMember.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(chatMember))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteChatMember() {
        // Initialize the database
        chatMemberRepository.save(chatMember).block();

        int databaseSizeBeforeDelete = chatMemberRepository.findAll().collectList().block().size();

        // Delete the chatMember
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, chatMember.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<ChatMember> chatMemberList = chatMemberRepository.findAll().collectList().block();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
