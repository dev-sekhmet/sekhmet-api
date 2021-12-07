package com.sekhmet.api.service;

import com.sekhmet.api.domain.ChatMember;
import com.sekhmet.api.repository.ChatMemberRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link ChatMember}.
 */
@Service
@Transactional
public class ChatMemberService {

    private final Logger log = LoggerFactory.getLogger(ChatMemberService.class);

    private final ChatMemberRepository chatMemberRepository;

    public ChatMemberService(ChatMemberRepository chatMemberRepository) {
        this.chatMemberRepository = chatMemberRepository;
    }

    /**
     * Save a chatMember.
     *
     * @param chatMember the entity to save.
     * @return the persisted entity.
     */
    public Mono<ChatMember> save(ChatMember chatMember) {
        log.debug("Request to save ChatMember : {}", chatMember);
        return chatMemberRepository.save(chatMember);
    }

    /**
     * Partially update a chatMember.
     *
     * @param chatMember the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<ChatMember> partialUpdate(ChatMember chatMember) {
        log.debug("Request to partially update ChatMember : {}", chatMember);

        return chatMemberRepository
            .findById(chatMember.getId())
            .map(existingChatMember -> {
                if (chatMember.getUid() != null) {
                    existingChatMember.setUid(chatMember.getUid());
                }
                if (chatMember.getScope() != null) {
                    existingChatMember.setScope(chatMember.getScope());
                }

                return existingChatMember;
            })
            .flatMap(chatMemberRepository::save);
    }

    /**
     * Get all the chatMembers.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<ChatMember> findAll() {
        log.debug("Request to get all ChatMembers");
        return chatMemberRepository.findAll();
    }

    /**
     * Returns the number of chatMembers available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return chatMemberRepository.count();
    }

    /**
     * Get one chatMember by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<ChatMember> findOne(Long id) {
        log.debug("Request to get ChatMember : {}", id);
        return chatMemberRepository.findById(id);
    }

    /**
     * Delete the chatMember by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete ChatMember : {}", id);
        return chatMemberRepository.deleteById(id);
    }
}
