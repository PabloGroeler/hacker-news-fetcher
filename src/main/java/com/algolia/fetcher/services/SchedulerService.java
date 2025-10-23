package com.algolia.fetcher.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    public static final int ARTICLE_SYNC_INTERVAL_MS = 1_000 * 60 * 60;
    public static final String QUERY = "java";
    private final ExternalApiService externalApiService;
    private final ArticleService articleService;

    @Scheduled(fixedRate = ARTICLE_SYNC_INTERVAL_MS)
    public void triggerTaskManually() {
        log.info("Task started");

        try {
            var result = externalApiService.searchArticlesByQuery(QUERY);
            if (result == null || result.getHits() == null || result.getHits().isEmpty()) {
                log.warn("No articles returned for query '{}'. Result: {}", QUERY, result);
                return;
            }

            result.getHits().forEach(articleService::saveArticle);
            log.info("Fetched {} articles for query '{}'.", result.getHits().size(), QUERY);
        } catch (Exception e) {
            log.error("Error while fetching articles: {}", e.getMessage(), e);
        }
    }
}
