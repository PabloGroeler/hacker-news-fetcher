package com.algolia.fetcher.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class HitsResult {

    private String author;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("comment_text")
    private String commentText;

    @JsonProperty("story_title")
    private String storyTitle;

    @JsonProperty("story_url")
    private String storyUrl;

    @JsonProperty("num_comments")
    private Integer numComments;

    @JsonProperty("objectID")
    private String objectID;

    private Integer points;

    @JsonProperty("story_id")
    private Integer storyId;

    private String title;

    @JsonProperty("created_at_i")
    private Long createdAtI;

    @JsonProperty("updated_at")
    private String updatedAt;

    private String url;

    @JsonProperty("_tags")
    private String[] tags;
}
