package com.algolia.fetcher.controllers;

import com.algolia.fetcher.dto.ArticleResponse;
import com.algolia.fetcher.services.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ArticleControllerTest {

    private static final String BASE_PATH = "/api/v1/articles";
    private MockMvc mockMvc;

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private ArticleController articleController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(articleController).build();
    }

    @Test
    void getArticles_ShouldReturnPageOfArticles() throws Exception {
        ArticleResponse article = ArticleResponse.builder()
                .articleId("1")
                .author("testAuthor")
                .title("Test Title")
                .tags(List.of("java", "spring"))
                .createdAt(Instant.now().toString())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<ArticleResponse> page = new PageImpl<>(List.of(article), pageable, 1);
        
        when(articleService.getArticles(any(), any(), any(), any(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get(BASE_PATH)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].articleId", is(article.getArticleId())))
                .andExpect(jsonPath("$.content[0].author", is(article.getAuthor())))
                .andExpect(jsonPath("$.content[0].title", is(article.getTitle())));

        verify(articleService, times(1)).getArticles(null, null, null, null, 0);
    }

    @Test
    void getArticles_WithFilters_ShouldReturnFilteredResults() throws Exception {
        String author = "testAuthor";
        String title = "Test";
        String tag = "java";
        String month = "2023-10";
        int page = 0;

        Pageable pageable = PageRequest.of(page, 10);
        Page<ArticleResponse> pageResponse = new PageImpl<>(List.of(), pageable, 0);
        when(articleService.getArticles(eq(author), eq(title), eq(tag), eq(month), eq(page)))
                .thenReturn(pageResponse);

        mockMvc.perform(get(BASE_PATH)
                        .param("author", author)
                        .param("title", title)
                        .param("tag", tag)
                        .param("month", month)
                        .param("page", String.valueOf(page)))
                .andExpect(status().isOk());

        verify(articleService, times(1)).getArticles(author, title, tag, month, page);
    }

    @Test
    void removeArticle_ShouldReturnNoContentAndCallService() throws Exception {
        String articleId = "123";
        doNothing().when(articleService).removeArticle(articleId);

        mockMvc.perform(delete(BASE_PATH + "/" + articleId))
                .andExpect(status().isNoContent());

        verify(articleService, times(1)).removeArticle(articleId);
    }

    @Test
    void getArticles_WithInvalidPageParam_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_PATH)
                        .param("page", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
