package com.algolia.fetcher;

import com.algolia.fetcher.config.JwtConfiguration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtConfiguration.class)
@ConfigurationPropertiesScan("com.algolia.fetcher.config")
@OpenAPIDefinition(
		info = @io.swagger.v3.oas.annotations.info.Info(
				title = "Hacker News Fetcher API",
				version = "1.0.0",
				description = "A Spring Boot service that connects to the Hacker News API, fetches recent articles, and indexes them for querying and analysis."
		)
)
public class HackerNewsFetcherApplication {

	public static void main(String[] args) {
		SpringApplication.run(HackerNewsFetcherApplication.class, args);
	}
}
