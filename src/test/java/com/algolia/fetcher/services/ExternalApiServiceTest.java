package com.algolia.fetcher.services;

import com.algolia.fetcher.model.HitsResult;
import com.algolia.fetcher.model.NewsSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExternalApiService externalApiService;

    private final String query = "test";

    @BeforeEach
    void setUp() {
        String apiUrl = "http://test.api/search";
        ReflectionTestUtils.setField(externalApiService, "searchByDateUrl", apiUrl);
    }

    @Test
    void searchArticlesByQuery_ShouldReturnSearchResults() {
        NewsSearchResult expectedResult = new NewsSearchResult();
        HitsResult hit = new HitsResult();
        hit.setTitle("Test Article");
        expectedResult.setHits(Collections.singletonList(hit));

        when(restTemplate.getForObject(any(URI.class), eq(NewsSearchResult.class)))
                .thenReturn(expectedResult);

        NewsSearchResult result = externalApiService.searchArticlesByQuery(query);

        assertThat(result).isNotNull();
        assertThat(result.getHits()).hasSize(1);
        assertThat(result.getHits().getFirst().getTitle()).isEqualTo("Test Article");
    }

    @Test
    void searchArticlesByQuery_WhenApiReturnsNull_ShouldReturnEmptyResult() {
        when(restTemplate.getForObject(any(URI.class), eq(NewsSearchResult.class)))
                .thenReturn(null);

        NewsSearchResult result = externalApiService.searchArticlesByQuery(query);

        assertThat(result).isNotNull();
        assertThat(result.getHits()).isNull();
    }

    @Test
    void searchArticlesByQuery_WhenApiThrowsException_ShouldReturnEmptyResult() {
        when(restTemplate.getForObject(any(URI.class), eq(NewsSearchResult.class)))
                .thenThrow(new RestClientException("API Error"));

        NewsSearchResult result = externalApiService.searchArticlesByQuery(query);

        assertThat(result).isNotNull();
        assertThat(result.getHits()).isNull();
    }
}
