package com.sekhmet.sekhmetapi.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.sekhmet.sekhmetapi.domain.Message;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Message} entity.
 */
public interface MessageSearchRepository extends ElasticsearchRepository<Message, UUID>, MessageSearchRepositoryInternal {}

interface MessageSearchRepositoryInternal {
    Page<Message> search(String query, Pageable pageable);
}

class MessageSearchRepositoryInternalImpl implements MessageSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;

    MessageSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public Page<Message> search(String query, Pageable pageable) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        nativeSearchQuery.setPageable(pageable);
        List<Message> hits = elasticsearchTemplate
            .search(nativeSearchQuery, Message.class)
            .map(SearchHit::getContent)
            .stream()
            .collect(Collectors.toList());

        return new PageImpl<>(hits, pageable, hits.size());
    }
}
