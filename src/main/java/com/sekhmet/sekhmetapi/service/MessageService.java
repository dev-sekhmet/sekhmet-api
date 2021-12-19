package com.sekhmet.sekhmetapi.service;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.sekhmet.sekhmetapi.domain.Message;
import com.sekhmet.sekhmetapi.repository.MessageRepository;
import com.sekhmet.sekhmetapi.repository.search.MessageSearchRepository;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Message}.
 */
@Service
@Transactional
public class MessageService {

    private final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;

    private final MessageSearchRepository messageSearchRepository;

    public MessageService(MessageRepository messageRepository, MessageSearchRepository messageSearchRepository) {
        this.messageRepository = messageRepository;
        this.messageSearchRepository = messageSearchRepository;
    }

    /**
     * Save a message.
     *
     * @param message the entity to save.
     * @return the persisted entity.
     */
    public Message save(Message message) {
        log.debug("Request to save Message : {}", message);
        Message result = messageRepository.save(message);
        messageSearchRepository.save(result);
        return result;
    }

    /**
     * Partially update a message.
     *
     * @param message the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Message> partialUpdate(Message message) {
        log.debug("Request to partially update Message : {}", message);

        return messageRepository
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
    }

    /**
     * Get all the messages.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Message> findAll(Pageable pageable) {
        log.debug("Request to get all Messages");
        return messageRepository.findAll(pageable);
    }

    /**
     * Get one message by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Message> findOne(UUID id) {
        log.debug("Request to get Message : {}", id);
        return messageRepository.findById(id);
    }

    /**
     * Delete the message by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        log.debug("Request to delete Message : {}", id);
        messageRepository.deleteById(id);
        messageSearchRepository.deleteById(id);
    }

    /**
     * Search for the message corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Message> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Messages for query {}", query);
        return messageSearchRepository.search(query, pageable);
    }
}
