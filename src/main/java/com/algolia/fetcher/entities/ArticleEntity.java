package com.algolia.fetcher.entities;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Document(collection = "articles")
public class ArticleEntity {

    @Id
    private String id;
    private String author;
    private String title;
    private String url;
    private String commentText;
    private int createdAtI;
    private int numComments;
    private int points;
    private int storyId;
    private String updatedAt;
    private String createdAt;
    private Instant lastSyncAt;
    private Instant removedAt;
    private List<String> tags;

}
