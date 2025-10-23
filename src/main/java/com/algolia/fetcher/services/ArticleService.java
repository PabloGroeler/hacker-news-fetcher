package com.algolia.fetcher.services;

import com.algolia.fetcher.dto.ArticleResponse;
import com.algolia.fetcher.entities.ArticleEntity;
import com.algolia.fetcher.mapper.ArticleMapper;
import com.algolia.fetcher.model.HitsResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Month;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final MongoTemplate mongoTemplate;

    public Page<ArticleResponse> getArticles(String author, String title, String tag, String month, int page) {

        log.info("Getting articles filtered: author: {}, title: {}, tag: {}, month: {} ", author, title, tag, month);
        Pageable pageable = PageRequest.of(Math.max(page, 0), 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        Query query = new Query().with(pageable);

        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.add(new Criteria().orOperator(
            Criteria.where("removedAt").exists(false),
            Criteria.where("removedAt").is(null)
        ));

        if (author != null && !author.isBlank()) {
            criteriaList.add(Criteria.where("author").is(author));
        }
        if (title != null && !title.isBlank()) {
            criteriaList.add(Criteria.where("title").regex(Pattern.compile(title, Pattern.CASE_INSENSITIVE)));
        }
        if (tag != null && !tag.isBlank()) {
            criteriaList.add(Criteria.where("tags").in(tag));
        }

        buildMonthCriteria(month, criteriaList);

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ArticleEntity.class);
        List<ArticleEntity> articles = mongoTemplate.find(query, ArticleEntity.class);

        List<ArticleResponse> responses = articles.stream()
                .map(ArticleMapper::toResponse)
                .toList();

        return new PageImpl<>(responses, pageable, total);
    }

    private void buildMonthCriteria(String month, List<Criteria> criteriaList) {
        if (month == null || month.isEmpty()) {
            return;
        }

        try {
            String regexPattern;

            if (month.matches("^\\d{4}-\\d{2}$")) {
                regexPattern = "^" + month;
            } else {
                Month parsedMonth = Month.valueOf(month.toUpperCase(Locale.ROOT));
                regexPattern = String.format("^\\d{4}-%02d", parsedMonth.getValue());
            }

            criteriaList.add(Criteria.where("createdAt").regex(Pattern.compile(regexPattern)));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid month format: {}", month, e);
            criteriaList.add(Criteria.where("id").is("__invalid__"));
        }
    }


    public void removeArticle(String id) {
        Query query = new Query(Criteria.where("id").is(new ObjectId(id)));
        Update update = new Update().set("removedAt", Instant.now());
        mongoTemplate.updateFirst(query, update, ArticleEntity.class);
    }

    public void saveArticle(@Valid HitsResult article) {
        mongoTemplate.save(ArticleMapper.toEntity(article));
    }
}
