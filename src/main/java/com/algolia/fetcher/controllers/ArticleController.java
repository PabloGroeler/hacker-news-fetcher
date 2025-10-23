package com.algolia.fetcher.controllers;

import com.algolia.fetcher.dto.ArticleResponse;
import com.algolia.fetcher.services.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/articles")
@Tag(name = "Articles")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    @Operation(
            summary = "Filtered Search for articles",
            description = "Search all articles based on filters. Returns a paginated result of articles.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated articles", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleResponse[].class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                    @ApiResponse(responseCode = "401", description = "Authentication required"),
                    @ApiResponse(responseCode = "403", description = "Insufficient permissions to access this resource")
            }
    )
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Page<ArticleResponse> getArticles(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "0") int page) {
        return articleService.getArticles(author, title, tag, month, page);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Remove an article based on its ID.",
            description = "Sets the field 'removedAt' for the article found by the articleId param.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad Request")
            }
    )
    public void removeArticle(@PathVariable String id) {
        articleService.removeArticle(id);
    }


}
