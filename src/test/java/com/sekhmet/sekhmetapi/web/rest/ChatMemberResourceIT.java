package com.sekhmet.sekhmetapi.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sekhmet.sekhmetapi.IntegrationTest;
import com.sekhmet.sekhmetapi.domain.ChatMember;
import com.sekhmet.sekhmetapi.domain.enumeration.ChatMemberScope;
import com.sekhmet.sekhmetapi.repository.ChatMemberRepository;
import com.sekhmet.sekhmetapi.repository.search.ChatMemberSearchRepository;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ChatMemberResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ChatMemberResourceIT {

    private static final ChatMemberScope DEFAULT_SCOPE = ChatMemberScope.PARTICIPANT;
    private static final ChatMemberScope UPDATED_SCOPE = ChatMemberScope.ADMIN;

    private static final String ENTITY_API_URL = "/api/chat-members";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/chat-members";

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    /**
     * This repository is mocked in the com.sekhmet.sekhmetapi.repository.search test package.
     *
     * @see com.sekhmet.sekhmetapi.repository.search.ChatMemberSearchRepositoryMockConfiguration
     */
    @Autowired
    private ChatMemberSearchRepository mockChatMemberSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restChatMemberMockMvc;

    private ChatMember chatMember;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ChatMember createEntity(EntityManager em) {
        ChatMember chatMember = new ChatMember().scope(DEFAULT_SCOPE);
        return chatMember;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ChatMember createUpdatedEntity(EntityManager em) {
        ChatMember chatMember = new ChatMember().scope(UPDATED_SCOPE);
        return chatMember;
    }

    @BeforeEach
    public void initTest() {
        chatMember = createEntity(em);
    }

    @Test
    @Transactional
    void createChatMember() throws Exception {
        int databaseSizeBeforeCreate = chatMemberRepository.findAll().size();
        // Create the ChatMember
        restChatMemberMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(chatMember)))
            .andExpect(status().isCreated());

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeCreate + 1);
        ChatMember testChatMember = chatMemberList.get(chatMemberList.size() - 1);
        assertThat(testChatMember.getScope()).isEqualTo(DEFAULT_SCOPE);

        // Validate the ChatMember in Elasticsearch
        verify(mockChatMemberSearchRepository, times(1)).save(testChatMember);
    }

    @Test
    @Transactional
    void createChatMemberWithExistingId() throws Exception {
        // Create the ChatMember with an existing ID
        chatMemberRepository.saveAndFlush(chatMember);

        int databaseSizeBeforeCreate = chatMemberRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restChatMemberMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(chatMember)))
            .andExpect(status().isBadRequest());

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeCreate);

        // Validate the ChatMember in Elasticsearch
        verify(mockChatMemberSearchRepository, times(0)).save(chatMember);
    }

    @Test
    @Transactional
    void checkScopeIsRequired() throws Exception {
        int databaseSizeBeforeTest = chatMemberRepository.findAll().size();
        // set the field null
        chatMember.setScope(null);

        // Create the ChatMember, which fails.

        restChatMemberMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(chatMember)))
            .andExpect(status().isBadRequest());

        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllChatMembers() throws Exception {
        // Initialize the database
        chatMemberRepository.saveAndFlush(chatMember);

        // Get all the chatMemberList
        restChatMemberMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(chatMember.getId().toString())))
            .andExpect(jsonPath("$.[*].scope").value(hasItem(DEFAULT_SCOPE.toString())));
    }

    @Test
    @Transactional
    void getChatMember() throws Exception {
        // Initialize the database
        chatMemberRepository.saveAndFlush(chatMember);

        // Get the chatMember
        restChatMemberMockMvc
            .perform(get(ENTITY_API_URL_ID, chatMember.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(chatMember.getId().toString()))
            .andExpect(jsonPath("$.scope").value(DEFAULT_SCOPE.toString()));
    }

    @Test
    @Transactional
    void getNonExistingChatMember() throws Exception {
        // Get the chatMember
        restChatMemberMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewChatMember() throws Exception {
        // Initialize the database
        chatMemberRepository.saveAndFlush(chatMember);

        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().size();

        // Update the chatMember
        ChatMember updatedChatMember = chatMemberRepository.findById(chatMember.getId()).get();
        // Disconnect from session so that the updates on updatedChatMember are not directly saved in db
        em.detach(updatedChatMember);
        updatedChatMember.scope(UPDATED_SCOPE);

        restChatMemberMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedChatMember.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedChatMember))
            )
            .andExpect(status().isOk());

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);
        ChatMember testChatMember = chatMemberList.get(chatMemberList.size() - 1);
        assertThat(testChatMember.getScope()).isEqualTo(UPDATED_SCOPE);

        // Validate the ChatMember in Elasticsearch
        verify(mockChatMemberSearchRepository).save(testChatMember);
    }

    @Test
    @Transactional
    void putNonExistingChatMember() throws Exception {
        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().size();
        chatMember.setId(UUID.randomUUID());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restChatMemberMockMvc
            .perform(
                put(ENTITY_API_URL_ID, chatMember.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(chatMember))
            )
            .andExpect(status().isBadRequest());

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ChatMember in Elasticsearch
        verify(mockChatMemberSearchRepository, times(0)).save(chatMember);
    }

    @Test
    @Transactional
    void putWithIdMismatchChatMember() throws Exception {
        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().size();
        chatMember.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatMemberMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(chatMember))
            )
            .andExpect(status().isBadRequest());

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ChatMember in Elasticsearch
        verify(mockChatMemberSearchRepository, times(0)).save(chatMember);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamChatMember() throws Exception {
        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().size();
        chatMember.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatMemberMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(chatMember)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ChatMember in Elasticsearch
        verify(mockChatMemberSearchRepository, times(0)).save(chatMember);
    }

    @Test
    @Transactional
    void partialUpdateChatMemberWithPatch() throws Exception {
        // Initialize the database
        chatMemberRepository.saveAndFlush(chatMember);

        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().size();

        // Update the chatMember using partial update
        ChatMember partialUpdatedChatMember = new ChatMember();
        partialUpdatedChatMember.setId(chatMember.getId());

        restChatMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedChatMember.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedChatMember))
            )
            .andExpect(status().isOk());

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);
        ChatMember testChatMember = chatMemberList.get(chatMemberList.size() - 1);
        assertThat(testChatMember.getScope()).isEqualTo(DEFAULT_SCOPE);
    }

    @Test
    @Transactional
    void fullUpdateChatMemberWithPatch() throws Exception {
        // Initialize the database
        chatMemberRepository.saveAndFlush(chatMember);

        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().size();

        // Update the chatMember using partial update
        ChatMember partialUpdatedChatMember = new ChatMember();
        partialUpdatedChatMember.setId(chatMember.getId());

        partialUpdatedChatMember.scope(UPDATED_SCOPE);

        restChatMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedChatMember.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedChatMember))
            )
            .andExpect(status().isOk());

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);
        ChatMember testChatMember = chatMemberList.get(chatMemberList.size() - 1);
        assertThat(testChatMember.getScope()).isEqualTo(UPDATED_SCOPE);
    }

    @Test
    @Transactional
    void patchNonExistingChatMember() throws Exception {
        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().size();
        chatMember.setId(UUID.randomUUID());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restChatMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, chatMember.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(chatMember))
            )
            .andExpect(status().isBadRequest());

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ChatMember in Elasticsearch
        verify(mockChatMemberSearchRepository, times(0)).save(chatMember);
    }

    @Test
    @Transactional
    void patchWithIdMismatchChatMember() throws Exception {
        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().size();
        chatMember.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatMemberMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(chatMember))
            )
            .andExpect(status().isBadRequest());

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ChatMember in Elasticsearch
        verify(mockChatMemberSearchRepository, times(0)).save(chatMember);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamChatMember() throws Exception {
        int databaseSizeBeforeUpdate = chatMemberRepository.findAll().size();
        chatMember.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChatMemberMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(chatMember))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the ChatMember in the database
        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ChatMember in Elasticsearch
        verify(mockChatMemberSearchRepository, times(0)).save(chatMember);
    }

    @Test
    @Transactional
    void deleteChatMember() throws Exception {
        // Initialize the database
        chatMemberRepository.saveAndFlush(chatMember);

        int databaseSizeBeforeDelete = chatMemberRepository.findAll().size();

        // Delete the chatMember
        restChatMemberMockMvc
            .perform(delete(ENTITY_API_URL_ID, chatMember.getId().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ChatMember> chatMemberList = chatMemberRepository.findAll();
        assertThat(chatMemberList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the ChatMember in Elasticsearch
        verify(mockChatMemberSearchRepository, times(1)).deleteById(chatMember.getId());
    }

    @Test
    @Transactional
    void searchChatMember() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        chatMemberRepository.saveAndFlush(chatMember);
        when(mockChatMemberSearchRepository.search("id:" + chatMember.getId(), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(chatMember), PageRequest.of(0, 1), 1));

        // Search the chatMember
        restChatMemberMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + chatMember.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(chatMember.getId().toString())))
            .andExpect(jsonPath("$.[*].scope").value(hasItem(DEFAULT_SCOPE.toString())));
    }
}
