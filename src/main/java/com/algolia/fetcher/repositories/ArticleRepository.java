package com.algolia.fetcher.repositories;

import com.algolia.fetcher.entities.ArticleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArticleRepository extends MongoRepository<ArticleEntity, String> {
}