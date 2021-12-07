package com.sekhmet.api.repository;

import com.sekhmet.api.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Message entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MessageRepository extends R2dbcRepository<Message, Long>, MessageRepositoryInternal {
    Flux<Message> findAllBy(Pageable pageable);

    // just to avoid having unambigous methods
    @Override
    Flux<Message> findAll();

    @Override
    Mono<Message> findById(Long id);

    @Override
    <S extends Message> Mono<S> save(S entity);
}

interface MessageRepositoryInternal {
    <S extends Message> Mono<S> insert(S entity);
    <S extends Message> Mono<S> save(S entity);
    Mono<Integer> update(Message entity);

    Flux<Message> findAll();
    Mono<Message> findById(Long id);
    Flux<Message> findAllBy(Pageable pageable);
    Flux<Message> findAllBy(Pageable pageable, Criteria criteria);
}
