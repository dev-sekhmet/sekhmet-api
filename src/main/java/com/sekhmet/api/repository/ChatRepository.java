package com.sekhmet.api.repository;

import com.sekhmet.api.domain.Chat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Chat entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChatRepository extends R2dbcRepository<Chat, Long>, ChatRepositoryInternal {
    Flux<Chat> findAllBy(Pageable pageable);

    // just to avoid having unambigous methods
    @Override
    Flux<Chat> findAll();

    @Override
    Mono<Chat> findById(Long id);

    @Override
    <S extends Chat> Mono<S> save(S entity);
}

interface ChatRepositoryInternal {
    <S extends Chat> Mono<S> insert(S entity);
    <S extends Chat> Mono<S> save(S entity);
    Mono<Integer> update(Chat entity);

    Flux<Chat> findAll();
    Mono<Chat> findById(Long id);
    Flux<Chat> findAllBy(Pageable pageable);
    Flux<Chat> findAllBy(Pageable pageable, Criteria criteria);
}
