package com.sekhmet.api.repository;

import com.sekhmet.api.domain.ChatMember;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the ChatMember entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChatMemberRepository extends R2dbcRepository<ChatMember, Long>, ChatMemberRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<ChatMember> findAll();

    @Override
    Mono<ChatMember> findById(Long id);

    @Override
    <S extends ChatMember> Mono<S> save(S entity);
}

interface ChatMemberRepositoryInternal {
    <S extends ChatMember> Mono<S> insert(S entity);
    <S extends ChatMember> Mono<S> save(S entity);
    Mono<Integer> update(ChatMember entity);

    Flux<ChatMember> findAll();
    Mono<ChatMember> findById(Long id);
    Flux<ChatMember> findAllBy(Pageable pageable);
    Flux<ChatMember> findAllBy(Pageable pageable, Criteria criteria);
}
