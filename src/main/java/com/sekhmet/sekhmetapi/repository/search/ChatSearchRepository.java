package com.sekhmet.sekhmetapi.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import com.sekhmet.sekhmetapi.domain.Chat;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Chat} entity.
 */
public interface ChatSearchRepository extends ElasticsearchRepository<Chat, Long>, ChatSearchRepositoryInternal {}

interface ChatSearchRepositoryInternal {
    Stream<Chat> search(String query);
}

class ChatSearchRepositoryInternalImpl implements ChatSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;

    ChatSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public Stream<Chat> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return elasticsearchTemplate.search(nativeSearchQuery, Chat.class).map(SearchHit::getContent).stream();
    }
}
