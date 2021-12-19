package com.sekhmet.sekhmetapi.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sekhmet.sekhmetapi.domain.Chat;
import com.sekhmet.sekhmetapi.domain.ChatMember;
import com.sekhmet.sekhmetapi.domain.Message;
import com.sekhmet.sekhmetapi.domain.User;
import com.sekhmet.sekhmetapi.repository.ChatMemberRepository;
import com.sekhmet.sekhmetapi.repository.ChatRepository;
import com.sekhmet.sekhmetapi.repository.MessageRepository;
import com.sekhmet.sekhmetapi.repository.UserRepository;
import com.sekhmet.sekhmetapi.repository.search.ChatMemberSearchRepository;
import com.sekhmet.sekhmetapi.repository.search.ChatSearchRepository;
import com.sekhmet.sekhmetapi.repository.search.MessageSearchRepository;
import com.sekhmet.sekhmetapi.repository.search.UserSearchRepository;
import io.micrometer.core.annotation.Timed;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javax.persistence.ManyToMany;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ElasticsearchIndexService {

    private static final Lock reindexLock = new ReentrantLock();

    private final Logger log = LoggerFactory.getLogger(ElasticsearchIndexService.class);

    private final ChatRepository chatRepository;

    private final ChatSearchRepository chatSearchRepository;

    private final ChatMemberRepository chatMemberRepository;

    private final ChatMemberSearchRepository chatMemberSearchRepository;

    private final MessageRepository messageRepository;

    private final MessageSearchRepository messageSearchRepository;

    private final UserRepository userRepository;

    private final UserSearchRepository userSearchRepository;

    private final IndicesClient indicesClient;
    private final ElasticsearchTemplate elasticsearchTemplate = null;

    public ElasticsearchIndexService(
        UserRepository userRepository,
        UserSearchRepository userSearchRepository,
        ChatRepository chatRepository,
        ChatSearchRepository chatSearchRepository,
        ChatMemberRepository chatMemberRepository,
        ChatMemberSearchRepository chatMemberSearchRepository,
        MessageRepository messageRepository,
        MessageSearchRepository messageSearchRepository,
        IndicesClient indicesClient
    ) {
        this.userRepository = userRepository;
        this.userSearchRepository = userSearchRepository;
        this.chatRepository = chatRepository;
        this.chatSearchRepository = chatSearchRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.chatMemberSearchRepository = chatMemberSearchRepository;
        this.messageRepository = messageRepository;
        this.messageSearchRepository = messageSearchRepository;
        this.indicesClient = indicesClient;
    }

    @Async
    @Timed
    public void reindexAll() {
        if (reindexLock.tryLock()) {
            try {
                reindexForClass(Chat.class, chatRepository, chatSearchRepository);
                reindexForClass(ChatMember.class, chatMemberRepository, chatMemberSearchRepository);
                reindexForClass(Message.class, messageRepository, messageSearchRepository);
                reindexForClass(User.class, userRepository, userSearchRepository);

                log.info("Elasticsearch: Successfully performed reindexing");
            } finally {
                reindexLock.unlock();
            }
        } else {
            log.info("Elasticsearch: concurrent reindexing attempt");
        }
    }

    @SuppressWarnings("unchecked")
    private <T, ID extends Serializable> void reindexForClass(
        Class<T> entityClass,
        JpaRepository<T, ID> jpaRepository,
        ElasticsearchRepository<T, ID> elasticsearchRepository
    ) {
        String indexName = getIndexname(entityClass);
        try {
            if (indexName != null) {
                indicesClient.delete(Requests.deleteIndexRequest(indexName), RequestOptions.DEFAULT);
                indicesClient.create(new CreateIndexRequest(indexName), RequestOptions.DEFAULT);
            } else {
                log.warn("Index for class : {} does not exist", entityClass);
            }
        } catch (IOException e) {
            // Do nothing. Index was already concurrently recreated by some other service.
        }
        try {
            indicesClient.putMapping(new PutMappingRequest(indexName), RequestOptions.DEFAULT);
        } catch (IOException e) {
            // Do nothing. Index was already concurrently recreated by some other service.
        }
        if (jpaRepository.count() > 0) {
            // if a JHipster entity field is the owner side of a many-to-many relationship, it should be loaded manually
            List<Method> relationshipGetters = Arrays
                .stream(entityClass.getDeclaredFields())
                .filter(field -> field.getType().equals(Set.class))
                .filter(field -> field.getAnnotation(ManyToMany.class) != null)
                .filter(field -> field.getAnnotation(ManyToMany.class).mappedBy().isEmpty())
                .filter(field -> field.getAnnotation(JsonIgnore.class) == null)
                .map(field -> {
                    try {
                        return new PropertyDescriptor(field.getName(), entityClass).getReadMethod();
                    } catch (IntrospectionException e) {
                        log.error(
                            "Error retrieving getter for class {}, field {}. Field will NOT be indexed",
                            entityClass.getSimpleName(),
                            field.getName(),
                            e
                        );
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            int size = 100;
            for (int i = 0; i <= jpaRepository.count() / size; i++) {
                Pageable page = PageRequest.of(i, size);
                log.info("Indexing page {} of {}, size {}", i, jpaRepository.count() / size, size);
                Page<T> results = jpaRepository.findAll(page);
                results.map(result -> {
                    // if there are any relationships to load, do it now
                    relationshipGetters.forEach(method -> {
                        try {
                            // eagerly load the relationship set
                            ((Set) method.invoke(result)).size();
                        } catch (Exception ex) {
                            log.error(ex.getMessage());
                        }
                    });
                    return result;
                });
                elasticsearchRepository.saveAll(results.getContent());
            }
        }
        log.info("Elasticsearch: Indexed all rows for {}", entityClass.getSimpleName());
    }

    private <T> String getIndexname(Class<T> entityClass) {
        Document document = entityClass.getAnnotation(Document.class);
        return document != null ? document.indexName() : null;
    }
}
