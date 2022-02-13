package com.sekhmet.sekhmetapi.service;

import com.sekhmet.sekhmetapi.domain.Chat;
import com.sekhmet.sekhmetapi.repository.ChatRepository;
import com.sekhmet.sekhmetapi.repository.search.ChatSearchRepository;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Chat}.
 */
@Service
@Transactional
public class ChatService {

    private final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatRepository chatRepository;

    private final ChatSearchRepository chatSearchRepository;

    public ChatService(ChatRepository chatRepository, ChatSearchRepository chatSearchRepository) {
        this.chatRepository = chatRepository;
        this.chatSearchRepository = chatSearchRepository;
    }

    /**
     * Save a chat.
     *
     * @param chat the entity to save.
     * @return the persisted entity.
     */
    public Chat save(Chat chat) {
        log.debug("Request to save Chat : {}", chat);
        Chat result = chatRepository.save(chat);
        chatSearchRepository.save(result);
        return result;
    }

    /**
     * Partially update a chat.
     *
     * @param chat the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Chat> partialUpdate(Chat chat) {
        log.debug("Request to partially update Chat : {}", chat);

        return chatRepository
            .findById(chat.getId())
            .map(existingChat -> {
                if (chat.getIcon() != null) {
                    existingChat.setIcon(chat.getIcon());
                }
                if (chat.getName() != null) {
                    existingChat.setName(chat.getName());
                }

                return existingChat;
            })
            .map(chatRepository::save)
            .map(savedChat -> {
                chatSearchRepository.save(savedChat);

                return savedChat;
            });
    }

    /**
     * Get all the chats.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Chat> findAll(Pageable pageable) {
        log.debug("Request to get all Chats");
        return chatRepository.findAll(pageable);
    }

    /**
     * Get one chat by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Chat> findOne(UUID id) {
        log.debug("Request to get Chat : {}", id);
        return chatRepository.findById(id);
    }

    /**
     * Get one chat members
     *
     * @param user1
     * @param user2
     * @return
     */
    @Transactional(readOnly = true)
    public Optional<Chat> findChatByMembers(UUID user1, UUID user2) {
        log.debug("Request to get Chat : user1 {}, user2 {}", user1, user2);
        return chatRepository.findChatByMembers(user1, user2);
    }

    public Page<Chat> findAllWithUserMember(Pageable pageable, UUID uuid) {
        log.debug("Request to get All Chat : user {}", uuid);
        return chatRepository.findAllWithUserMember(uuid, pageable);
    }

    /**
     * Delete the chat by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        log.debug("Request to delete Chat : {}", id);
        chatRepository.deleteById(id);
        chatSearchRepository.deleteById(id);
    }

    /**
     * Search for the chat corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Chat> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Chats for query {}", query);
        return chatSearchRepository.search(query, pageable);
    }

    /**
     * Search for the chat corresponding to the query.
     *
     * @param query    the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Chat> search(String query, Pageable pageable, UUID id) {
        log.debug("Request to search for a page of Chats for query {} and user {} ", query, id);
        return chatSearchRepository.search(query, pageable);
    }
}
