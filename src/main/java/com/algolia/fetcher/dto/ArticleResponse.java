package com.algolia.fetcher.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class ArticleResponse {
    private String articleId;
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
    private List<String> tags;
}
