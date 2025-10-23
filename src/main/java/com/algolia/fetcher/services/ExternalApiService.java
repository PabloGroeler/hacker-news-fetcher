package com.algolia.fetcher.services;

import com.algolia.fetcher.model.NewsSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiService {

    private final RestTemplate restTemplate;

    @Value("${api.external.hacker-news.search_by_date}")
    private String searchByDateUrl;

    public NewsSearchResult searchArticlesByQuery(String query) {
        URI requestUri = UriComponentsBuilder
                .fromUriString(searchByDateUrl)
                .queryParam("query", query)
                .build()
                .encode()
                .toUri();

        log.debug("Fetching articles from external API: {}", requestUri);

        try {
            NewsSearchResult response = restTemplate.getForObject(requestUri, NewsSearchResult.class);
            if (response == null) {
                log.warn("External API returned null for query '{}'", query);
                return new NewsSearchResult();
            }
            return response;
        } catch (Exception ex) {
            log.error("Failed to fetch articles from external API for query '{}': {}", query, ex.getMessage());
            return new NewsSearchResult();
        }
    }
}
