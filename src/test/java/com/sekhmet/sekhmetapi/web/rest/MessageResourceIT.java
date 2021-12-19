package com.sekhmet.sekhmetapi.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.sekhmet.sekhmetapi.IntegrationTest;
import com.sekhmet.sekhmetapi.domain.Message;
import com.sekhmet.sekhmetapi.domain.User;
import com.sekhmet.sekhmetapi.repository.MessageRepository;
import com.sekhmet.sekhmetapi.repository.UserRepository;
import com.sekhmet.sekhmetapi.repository.search.MessageSearchRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Integration tests for the {@link MessageResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class MessageResourceIT {

    private static final String DEFAULT_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_TEXT = "BBBBBBBBBB";

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
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/messages";

    @Autowired
    private MessageRepository messageRepository;

    /**
     * This repository is mocked in the com.sekhmet.sekhmetapi.repository.search test package.
     *
     * @see com.sekhmet.sekhmetapi.repository.search.MessageSearchRepositoryMockConfiguration
     */
    @Autowired
    private MessageSearchRepository mockMessageSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMessageMockMvc;

    private Message message;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Message createEntity(EntityManager em) {
        Message message = new Message()
            .text(DEFAULT_TEXT)
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
            .text(UPDATED_TEXT)
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

    @BeforeEach
    public void initTest() {
        message = createEntity(em);
        setUser();
    }

    @Test
    @Transactional
    void createMessage() throws Exception {
        int databaseSizeBeforeCreate = messageRepository.findAll().size();
        // Create the Message
        restMessageMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(message)))
            .andExpect(status().isCreated());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeCreate + 1);
        Message testMessage = messageList.get(messageList.size() - 1);
        assertThat(testMessage.getText()).isEqualTo(DEFAULT_TEXT);
        assertThat(testMessage.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testMessage.getImage()).isEqualTo(DEFAULT_IMAGE);
        assertThat(testMessage.getVideo()).isEqualTo(DEFAULT_VIDEO);
        assertThat(testMessage.getAudio()).isEqualTo(DEFAULT_AUDIO);
        assertThat(testMessage.getSystem()).isEqualTo(DEFAULT_SYSTEM);
        assertThat(testMessage.getSent()).isEqualTo(DEFAULT_SENT);
        assertThat(testMessage.getReceived()).isEqualTo(DEFAULT_RECEIVED);
        assertThat(testMessage.getPending()).isEqualTo(DEFAULT_PENDING);

        // Validate the Message in Elasticsearch
        verify(mockMessageSearchRepository, times(1)).save(testMessage);
    }

    @Test
    @Transactional
    void createMessageWithExistingId() throws Exception {
        // Create the Message with an existing ID
        messageRepository.saveAndFlush(message);

        int databaseSizeBeforeCreate = messageRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restMessageMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(message)))
            .andExpect(status().isBadRequest());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeCreate);

        // Validate the Message in Elasticsearch
        verify(mockMessageSearchRepository, times(0)).save(message);
    }

    @Test
    @Transactional
    void getAllMessages() throws Exception {
        // Initialize the database
        messageRepository.saveAndFlush(message);

        // Get all the messageList
        restMessageMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(message.getId().toString())))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].image").value(hasItem(DEFAULT_IMAGE)))
            .andExpect(jsonPath("$.[*].video").value(hasItem(DEFAULT_VIDEO)))
            .andExpect(jsonPath("$.[*].audio").value(hasItem(DEFAULT_AUDIO)))
            .andExpect(jsonPath("$.[*].system").value(hasItem(DEFAULT_SYSTEM.booleanValue())))
            .andExpect(jsonPath("$.[*].sent").value(hasItem(DEFAULT_SENT.booleanValue())))
            .andExpect(jsonPath("$.[*].received").value(hasItem(DEFAULT_RECEIVED.booleanValue())))
            .andExpect(jsonPath("$.[*].pending").value(hasItem(DEFAULT_PENDING.booleanValue())));
    }

    @Test
    @Transactional
    void getMessage() throws Exception {
        // Initialize the database
        messageRepository.saveAndFlush(message);

        // Get the message
        restMessageMockMvc
            .perform(get(ENTITY_API_URL_ID, message.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(message.getId().toString()))
            .andExpect(jsonPath("$.text").value(DEFAULT_TEXT))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.image").value(DEFAULT_IMAGE))
            .andExpect(jsonPath("$.video").value(DEFAULT_VIDEO))
            .andExpect(jsonPath("$.audio").value(DEFAULT_AUDIO))
            .andExpect(jsonPath("$.system").value(DEFAULT_SYSTEM.booleanValue()))
            .andExpect(jsonPath("$.sent").value(DEFAULT_SENT.booleanValue()))
            .andExpect(jsonPath("$.received").value(DEFAULT_RECEIVED.booleanValue()))
            .andExpect(jsonPath("$.pending").value(DEFAULT_PENDING.booleanValue()));
    }

    @Test
    @Transactional
    void getNonExistingMessage() throws Exception {
        // Get the message
        restMessageMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewMessage() throws Exception {
        // Initialize the database
        messageRepository.saveAndFlush(message);

        int databaseSizeBeforeUpdate = messageRepository.findAll().size();

        // Update the message
        Message updatedMessage = messageRepository.findById(message.getId()).get();
        // Disconnect from session so that the updates on updatedMessage are not directly saved in db
        em.detach(updatedMessage);
        updatedMessage
            .text(UPDATED_TEXT)
            .createdAt(UPDATED_CREATED_AT)
            .image(UPDATED_IMAGE)
            .video(UPDATED_VIDEO)
            .audio(UPDATED_AUDIO)
            .system(UPDATED_SYSTEM)
            .sent(UPDATED_SENT)
            .received(UPDATED_RECEIVED)
            .pending(UPDATED_PENDING);

        restMessageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedMessage.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedMessage))
            )
            .andExpect(status().isOk());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);
        Message testMessage = messageList.get(messageList.size() - 1);
        assertThat(testMessage.getText()).isEqualTo(UPDATED_TEXT);
        assertThat(testMessage.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testMessage.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(testMessage.getVideo()).isEqualTo(UPDATED_VIDEO);
        assertThat(testMessage.getAudio()).isEqualTo(UPDATED_AUDIO);
        assertThat(testMessage.getSystem()).isEqualTo(UPDATED_SYSTEM);
        assertThat(testMessage.getSent()).isEqualTo(UPDATED_SENT);
        assertThat(testMessage.getReceived()).isEqualTo(UPDATED_RECEIVED);
        assertThat(testMessage.getPending()).isEqualTo(UPDATED_PENDING);

        // Validate the Message in Elasticsearch
        verify(mockMessageSearchRepository).save(testMessage);
    }

    @Test
    @Transactional
    void putNonExistingMessage() throws Exception {
        int databaseSizeBeforeUpdate = messageRepository.findAll().size();
        message.setId(UUID.randomUUID());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMessageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, message.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(message))
            )
            .andExpect(status().isBadRequest());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Message in Elasticsearch
        verify(mockMessageSearchRepository, times(0)).save(message);
    }

    @Test
    @Transactional
    void putWithIdMismatchMessage() throws Exception {
        int databaseSizeBeforeUpdate = messageRepository.findAll().size();
        message.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(message))
            )
            .andExpect(status().isBadRequest());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Message in Elasticsearch
        verify(mockMessageSearchRepository, times(0)).save(message);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMessage() throws Exception {
        int databaseSizeBeforeUpdate = messageRepository.findAll().size();
        message.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(message)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Message in Elasticsearch
        verify(mockMessageSearchRepository, times(0)).save(message);
    }

    @Test
    @Transactional
    void partialUpdateMessageWithPatch() throws Exception {
        // Initialize the database
        messageRepository.saveAndFlush(message);

        int databaseSizeBeforeUpdate = messageRepository.findAll().size();

        // Update the message using partial update
        Message partialUpdatedMessage = new Message();
        partialUpdatedMessage.setId(message.getId());

        partialUpdatedMessage.audio(UPDATED_AUDIO).system(UPDATED_SYSTEM).pending(UPDATED_PENDING);

        restMessageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMessage.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMessage))
            )
            .andExpect(status().isOk());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);
        Message testMessage = messageList.get(messageList.size() - 1);
        assertThat(testMessage.getText()).isEqualTo(DEFAULT_TEXT);
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
    @Transactional
    void fullUpdateMessageWithPatch() throws Exception {
        // Initialize the database
        messageRepository.saveAndFlush(message);

        int databaseSizeBeforeUpdate = messageRepository.findAll().size();

        // Update the message using partial update
        Message partialUpdatedMessage = new Message();
        partialUpdatedMessage.setId(message.getId());

        partialUpdatedMessage
            .text(UPDATED_TEXT)
            .createdAt(UPDATED_CREATED_AT)
            .image(UPDATED_IMAGE)
            .video(UPDATED_VIDEO)
            .audio(UPDATED_AUDIO)
            .system(UPDATED_SYSTEM)
            .sent(UPDATED_SENT)
            .received(UPDATED_RECEIVED)
            .pending(UPDATED_PENDING);

        restMessageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMessage.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedMessage))
            )
            .andExpect(status().isOk());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);
        Message testMessage = messageList.get(messageList.size() - 1);
        assertThat(testMessage.getText()).isEqualTo(UPDATED_TEXT);
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
    @Transactional
    void patchNonExistingMessage() throws Exception {
        int databaseSizeBeforeUpdate = messageRepository.findAll().size();
        message.setId(UUID.randomUUID());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMessageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, message.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(message))
            )
            .andExpect(status().isBadRequest());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Message in Elasticsearch
        verify(mockMessageSearchRepository, times(0)).save(message);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMessage() throws Exception {
        int databaseSizeBeforeUpdate = messageRepository.findAll().size();
        message.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(message))
            )
            .andExpect(status().isBadRequest());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Message in Elasticsearch
        verify(mockMessageSearchRepository, times(0)).save(message);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMessage() throws Exception {
        int databaseSizeBeforeUpdate = messageRepository.findAll().size();
        message.setId(UUID.randomUUID());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMessageMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(message)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Message in Elasticsearch
        verify(mockMessageSearchRepository, times(0)).save(message);
    }

    @Test
    @Transactional
    void deleteMessage() throws Exception {
        // Initialize the database
        messageRepository.saveAndFlush(message);

        int databaseSizeBeforeDelete = messageRepository.findAll().size();

        // Delete the message
        restMessageMockMvc
            .perform(delete(ENTITY_API_URL_ID, message.getId().toString()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Message in Elasticsearch
        verify(mockMessageSearchRepository, times(1)).deleteById(message.getId());
    }

    @Test
    @Transactional
    void searchMessage() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        messageRepository.saveAndFlush(message);
        when(mockMessageSearchRepository.search("id:" + message.getId(), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(message), PageRequest.of(0, 1), 1));

        // Search the message
        restMessageMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + message.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(message.getId().toString())))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].image").value(hasItem(DEFAULT_IMAGE)))
            .andExpect(jsonPath("$.[*].video").value(hasItem(DEFAULT_VIDEO)))
            .andExpect(jsonPath("$.[*].audio").value(hasItem(DEFAULT_AUDIO)))
            .andExpect(jsonPath("$.[*].system").value(hasItem(DEFAULT_SYSTEM.booleanValue())))
            .andExpect(jsonPath("$.[*].sent").value(hasItem(DEFAULT_SENT.booleanValue())))
            .andExpect(jsonPath("$.[*].received").value(hasItem(DEFAULT_RECEIVED.booleanValue())))
            .andExpect(jsonPath("$.[*].pending").value(hasItem(DEFAULT_PENDING.booleanValue())));
    }

    private void setUser() {
        User user = userRepository.saveAndFlush(UserResourceIT.initTestUser());
        message.setUser(user);
    }
}
