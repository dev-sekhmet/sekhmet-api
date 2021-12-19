package com.sekhmet.sekhmetapi.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.sekhmet.sekhmetapi.domain.Chat;
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
 * Spring Data Elasticsearch repository for the {@link Chat} entity.
 */
public interface ChatSearchRepository extends ElasticsearchRepository<Chat, UUID>, ChatSearchRepositoryInternal {}

interface ChatSearchRepositoryInternal {
    Page<Chat> search(String query, Pageable pageable);
}

class ChatSearchRepositoryInternalImpl implements ChatSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;

    ChatSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public Page<Chat> search(String query, Pageable pageable) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        nativeSearchQuery.setPageable(pageable);
        List<Chat> hits = elasticsearchTemplate
            .search(nativeSearchQuery, Chat.class)
            .map(SearchHit::getContent)
            .stream()
            .collect(Collectors.toList());

        return new PageImpl<>(hits, pageable, hits.size());
    }
}
