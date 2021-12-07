package com.sekhmet.api.service;

import com.sekhmet.api.domain.Chat;
import com.sekhmet.api.repository.ChatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Chat}.
 */
@Service
@Transactional
public class ChatService {

    private final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    /**
     * Save a chat.
     *
     * @param chat the entity to save.
     * @return the persisted entity.
     */
    public Mono<Chat> save(Chat chat) {
        log.debug("Request to save Chat : {}", chat);
        return chatRepository.save(chat);
    }

    /**
     * Partially update a chat.
     *
     * @param chat the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<Chat> partialUpdate(Chat chat) {
        log.debug("Request to partially update Chat : {}", chat);

        return chatRepository
            .findById(chat.getId())
            .map(existingChat -> {
                if (chat.getGuid() != null) {
                    existingChat.setGuid(chat.getGuid());
                }
                if (chat.getIcon() != null) {
                    existingChat.setIcon(chat.getIcon());
                }
                if (chat.getName() != null) {
                    existingChat.setName(chat.getName());
                }

                return existingChat;
            })
            .flatMap(chatRepository::save);
    }

    /**
     * Get all the chats.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<Chat> findAll(Pageable pageable) {
        log.debug("Request to get all Chats");
        return chatRepository.findAllBy(pageable);
    }

    /**
     * Returns the number of chats available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return chatRepository.count();
    }

    /**
     * Get one chat by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<Chat> findOne(Long id) {
        log.debug("Request to get Chat : {}", id);
        return chatRepository.findById(id);
    }

    /**
     * Delete the chat by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete Chat : {}", id);
        return chatRepository.deleteById(id);
    }
}
