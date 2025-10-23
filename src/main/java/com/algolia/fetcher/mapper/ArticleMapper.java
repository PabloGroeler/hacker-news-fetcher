package com.algolia.fetcher.mapper;

import com.algolia.fetcher.dto.ArticleResponse;
import com.algolia.fetcher.entities.ArticleEntity;
import com.algolia.fetcher.model.HitsResult;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class ArticleMapper {

    private ArticleMapper() {}

    public static ArticleEntity toEntity(HitsResult hitsResult) {
        if (hitsResult == null) {
            return null;
        }

        return ArticleEntity.builder()
                .id(hitsResult.getObjectID())
                .author(hitsResult.getAuthor())
                .title(hitsResult.getTitle() != null ? hitsResult.getTitle() : hitsResult.getStoryTitle())
                .url(hitsResult.getUrl() != null ? hitsResult.getUrl() : hitsResult.getStoryUrl())
                .commentText(hitsResult.getCommentText())
                .createdAtI(hitsResult.getCreatedAtI() != null ? hitsResult.getCreatedAtI().intValue() : 0)
                .numComments(hitsResult.getNumComments() != null ? hitsResult.getNumComments() : 0)
                .points(hitsResult.getPoints() != null ? hitsResult.getPoints() : 0)
                .storyId(hitsResult.getStoryId() != null ? hitsResult.getStoryId() : 0)
                .updatedAt(hitsResult.getUpdatedAt())
                .createdAt(hitsResult.getCreatedAt())
                .lastSyncAt(Instant.now())
                .tags(hitsResult.getTags() != null ? Arrays.stream(hitsResult.getTags()).toList() : List.of())
                .build();
    }

    public static ArticleResponse toResponse(ArticleEntity entity) {
        if (entity == null) {
            return null;
        }

        return ArticleResponse.builder()
                .articleId(entity.getId())
                .author(entity.getAuthor())
                .title(entity.getTitle())
                .url(entity.getUrl())
                .commentText(entity.getCommentText())
                .createdAtI(entity.getCreatedAtI())
                .numComments(entity.getNumComments())
                .points(entity.getPoints())
                .storyId(entity.getStoryId())
                .updatedAt(entity.getUpdatedAt())
                .createdAt(entity.getCreatedAt())
                .tags(entity.getTags())
                .build();
    }
}
