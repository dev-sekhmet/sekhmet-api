package com.sekhmet.api.service;

import com.sekhmet.api.domain.Message;
import com.sekhmet.api.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Message}.
 */
@Service
@Transactional
public class MessageService {

    private final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Save a message.
     *
     * @param message the entity to save.
     * @return the persisted entity.
     */
    public Mono<Message> save(Message message) {
        log.debug("Request to save Message : {}", message);
        return messageRepository.save(message);
    }

    /**
     * Partially update a message.
     *
     * @param message the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Message> partialUpdate(Message message) {
        log.debug("Request to partially update Message : {}", message);

        return messageRepository
            .findById(message.getId())
            .map(existingMessage -> {
                if (message.getUid() != null) {
                    existingMessage.setUid(message.getUid());
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
            .flatMap(messageRepository::save);
    }

    /**
     * Get all the messages.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Message> findAll(Pageable pageable) {
        log.debug("Request to get all Messages");
        return messageRepository.findAllBy(pageable);
    }

    /**
     * Returns the number of messages available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return messageRepository.count();
    }

    /**
     * Get one message by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<Message> findOne(Long id) {
        log.debug("Request to get Message : {}", id);
        return messageRepository.findById(id);
    }

    /**
     * Delete the message by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Message : {}", id);
        return messageRepository.deleteById(id);
    }
}
