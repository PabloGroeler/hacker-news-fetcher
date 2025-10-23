package com.algolia.fetcher.services;

import com.algolia.fetcher.dto.ArticleResponse;
import com.algolia.fetcher.entities.ArticleEntity;
import com.algolia.fetcher.mapper.ArticleMapper;
import com.algolia.fetcher.model.HitsResult;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ArticleService articleService;

    private final String articleId = "507f1f77bcf86cd799439011";
    private final String author = "testAuthor";
    private final String title = "Test Title";
    private final List<String> tags = List.of("java", "spring");

    @Test
    void getArticles_WithFilters_ShouldReturnFilteredResults() {
        String tag = "java";
        String month = "2023-10";
        int page = 0;
        
        ArticleEntity article = ArticleEntity.builder()
                .id(articleId)
                .author(author)
                .title(title)
                .tags(tags)
                .createdAt(Instant.now().toString())
                .build();

        when(mongoTemplate.count(any(Query.class), eq(ArticleEntity.class))).thenReturn(1L);
        
        when(mongoTemplate.find(any(Query.class), eq(ArticleEntity.class)))
                .thenAnswer(invocation -> {
                    Query query = invocation.getArgument(0);
                    String queryStr = query.toString();

                    assertThat(queryStr).contains("\"author\" : \"" + author + "\"");
                    assertThat(queryStr).contains("\"title\" : { \"$regularExpression\" : { \"pattern\" : \"" + title + "\"");
                    assertThat(queryStr).contains("\"createdAt\" : { \"$regularExpression\" : { \"pattern\" : \"^" + month);
                    assertThat(queryStr).contains("Sort: { \"createdAt\" : -1}");
                    
                    return List.of(article);
                });

        Page<ArticleResponse> result = articleService.getArticles(author, title, tag, month, page);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getAuthor()).isEqualTo(author);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo(title);
        
        verify(mongoTemplate).count(any(Query.class), eq(ArticleEntity.class));
        verify(mongoTemplate).find(any(Query.class), eq(ArticleEntity.class));
    }

    @Test
    void removeArticle_ShouldSetRemovedAtTimestamp() {
        Query expectedQuery = new Query(Criteria.where("id").is(new ObjectId(articleId)));
        Instant now = Instant.now();
        articleService.removeArticle(articleId);
        
        verify(mongoTemplate).updateFirst(
            eq(expectedQuery),
            argThat(update -> {
                Map<String, Object> updateObject = update.getUpdateObject();
                @SuppressWarnings("unchecked")
                Map<String, Object> setMap = (Map<String, Object>) updateObject.get("$set");
                Instant removedAt = (Instant) setMap.get("removedAt");
                return removedAt != null && 
                       !removedAt.isBefore(now) && 
                       !removedAt.isAfter(Instant.now());
            }),
            eq(ArticleEntity.class)
        );
    }

    @Test
    void saveArticle_ShouldSaveEntity() {
        String objectId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        String createdAtString = now.toString();

        HitsResult hitsResult = HitsResult.builder()
                .objectID(objectId)
                .author(author)
                .title(title)
                .tags(tags.toArray(new String[0]))
                .createdAt(createdAtString)
                .build();

        articleService.saveArticle(hitsResult);

        ArgumentCaptor<ArticleEntity> articleCaptor = ArgumentCaptor.forClass(ArticleEntity.class);
        verify(mongoTemplate).save(articleCaptor.capture());

        ArticleEntity savedArticle = articleCaptor.getValue();
        assertThat(savedArticle)
                .isNotNull()
                .satisfies(article -> {
                    assertThat(article.getId()).isEqualTo(objectId);
                    assertThat(article.getAuthor()).isEqualTo(author);
                    assertThat(article.getTitle()).isEqualTo(title);
                    assertThat(article.getTags()).isEqualTo(tags);
                    assertThat(article.getCreatedAt()).isEqualTo(createdAtString);
                    assertThat(article.getLastSyncAt())
                            .isNotNull()
                            .isAfterOrEqualTo(now);
                });
    }
}
