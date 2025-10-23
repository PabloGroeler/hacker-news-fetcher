package com.algolia.fetcher.services;

import com.algolia.fetcher.model.HitsResult;
import com.algolia.fetcher.model.NewsSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    @Mock
    private ExternalApiService externalApiService;

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private SchedulerService schedulerService;

    private final String testQuery = "java";
    private NewsSearchResult testSearchResult;

    @BeforeEach
    void setUp() {
        HitsResult hit = new HitsResult();
        hit.setTitle("Test Article");
        testSearchResult = new NewsSearchResult();
        testSearchResult.setHits(Collections.singletonList(hit));
    }

    @Test
    void triggerTaskManually_ShouldFetchAndSaveArticles() {
        when(externalApiService.searchArticlesByQuery(testQuery))
                .thenReturn(testSearchResult);
        doNothing().when(articleService).saveArticle(any(HitsResult.class));

        schedulerService.triggerTaskManually();

        verify(externalApiService).searchArticlesByQuery(testQuery);
        verify(articleService, times(1)).saveArticle(any(HitsResult.class));
    }

    @Test
    void triggerTaskManually_WhenNoResults_ShouldNotSaveArticles() {
        NewsSearchResult emptyResult = new NewsSearchResult();
        emptyResult.setHits(Collections.emptyList());
        when(externalApiService.searchArticlesByQuery(testQuery))
                .thenReturn(emptyResult);

        schedulerService.triggerTaskManually();

        verify(externalApiService).searchArticlesByQuery(testQuery);
        verify(articleService, never()).saveArticle(any());
    }

    @Test
    void triggerTaskManually_WhenApiReturnsNull_ShouldHandleGracefully() {
        when(externalApiService.searchArticlesByQuery(testQuery))
                .thenReturn(null);

        schedulerService.triggerTaskManually();

        verify(externalApiService).searchArticlesByQuery(testQuery);
        verify(articleService, never()).saveArticle(any());
    }

    @Test
    void triggerTaskManually_WhenApiThrowsException_ShouldHandleGracefully() {
        when(externalApiService.searchArticlesByQuery(testQuery))
                .thenThrow(new RuntimeException("API Error"));

        schedulerService.triggerTaskManually();

        verify(externalApiService).searchArticlesByQuery(testQuery);
        verify(articleService, never()).saveArticle(any());
    }
}
