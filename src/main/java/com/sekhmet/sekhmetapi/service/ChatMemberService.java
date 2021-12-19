package com.sekhmet.sekhmetapi.service;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.sekhmet.sekhmetapi.domain.ChatMember;
import com.sekhmet.sekhmetapi.repository.ChatMemberRepository;
import com.sekhmet.sekhmetapi.repository.search.ChatMemberSearchRepository;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ChatMember}.
 */
@Service
@Transactional
public class ChatMemberService {

    private final Logger log = LoggerFactory.getLogger(ChatMemberService.class);

    private final ChatMemberRepository chatMemberRepository;

    private final ChatMemberSearchRepository chatMemberSearchRepository;

    public ChatMemberService(ChatMemberRepository chatMemberRepository, ChatMemberSearchRepository chatMemberSearchRepository) {
        this.chatMemberRepository = chatMemberRepository;
        this.chatMemberSearchRepository = chatMemberSearchRepository;
    }

    /**
     * Save a chatMember.
     *
     * @param chatMember the entity to save.
     * @return the persisted entity.
     */
    public ChatMember save(ChatMember chatMember) {
        log.debug("Request to save ChatMember : {}", chatMember);
        ChatMember result = chatMemberRepository.save(chatMember);
        chatMemberSearchRepository.save(result);
        return result;
    }

    /**
     * Partially update a chatMember.
     *
     * @param chatMember the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ChatMember> partialUpdate(ChatMember chatMember) {
        log.debug("Request to partially update ChatMember : {}", chatMember);

        return chatMemberRepository
            .findById(chatMember.getId())
            .map(existingChatMember -> {
                if (chatMember.getScope() != null) {
                    existingChatMember.setScope(chatMember.getScope());
                }

                return existingChatMember;
            })
            .map(chatMemberRepository::save)
            .map(savedChatMember -> {
                chatMemberSearchRepository.save(savedChatMember);

                return savedChatMember;
            });
    }

    /**
     * Get all the chatMembers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ChatMember> findAll(Pageable pageable) {
        log.debug("Request to get all ChatMembers");
        return chatMemberRepository.findAll(pageable);
    }

    /**
     * Get one chatMember by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ChatMember> findOne(UUID id) {
        log.debug("Request to get ChatMember : {}", id);
        return chatMemberRepository.findById(id);
    }

    /**
     * Delete the chatMember by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        log.debug("Request to delete ChatMember : {}", id);
        chatMemberRepository.deleteById(id);
        chatMemberSearchRepository.deleteById(id);
    }

    /**
     * Search for the chatMember corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ChatMember> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of ChatMembers for query {}", query);
        return chatMemberSearchRepository.search(query, pageable);
    }
}
