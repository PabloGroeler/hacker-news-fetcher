package com.algolia.fetcher.services;

import com.algolia.fetcher.HackerNewsFetcherApplication;
import com.algolia.fetcher.config.MongoTestConfig;
import com.algolia.fetcher.dto.ArticleResponse;
import com.algolia.fetcher.entities.ArticleEntity;
import com.algolia.fetcher.repositories.ArticleRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {HackerNewsFetcherApplication.class, MongoTestConfig.class})
@Testcontainers
@ActiveProfiles("test")
class ArticleServiceIT {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        articleRepository.deleteAll();

        ArticleEntity article1 = ArticleEntity.builder()
                .id(new ObjectId().toString())
                .author("testAuthor")
                .title("Test Article 1")
                .url("http://example.com/1")
                .createdAt(Instant.now().toString())
                .tags(List.of("java", "spring"))
                .build();

        ArticleEntity article2 = ArticleEntity.builder()
                .id(new ObjectId().toString())
                .author("anotherAuthor")
                .title("Test Article 2")
                .url("http://example.com/2")
                .createdAt(Instant.now().toString())
                .tags(List.of("python", "django"))
                .build();

        articleRepository.saveAll(List.of(article1, article2));
    }

    @AfterEach
    void tearDown() {
        articleRepository.deleteAll();
    }

    @Test
    void getArticles_ShouldReturnFilteredResults() {
        Page<ArticleResponse> result = articleService.getArticles("testAuthor", null, null, null, 0);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getAuthor()).isEqualTo("testAuthor");
    }

    @Test
    void getArticles_ShouldFilterByTag() {
        Page<ArticleResponse> result = articleService.getArticles(null, null, "java", null, 0);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTags()).contains("java");
    }

    @Test
    void removeArticle_ShouldMarkAsRemoved() {
        String articleId = new ObjectId().toString();
        ArticleEntity article = ArticleEntity.builder()
                .id(articleId)
                .author("testAuthor")
                .title("Test Article for Deletion")
                .url("http://example.com/delete")
                .createdAt(Instant.now().toString())
                .tags(List.of("test"))
                .build();
        articleRepository.save(article);

        articleService.removeArticle(articleId);

        ArticleEntity removedArticle = articleRepository.findById(articleId).orElseThrow();
        assertThat(removedArticle.getRemovedAt()).isNotNull();
    }
}
