package com.algolia.fetcher.services;

import com.algolia.fetcher.HackerNewsFetcherApplication;
import com.algolia.fetcher.config.MongoTestConfig;
import com.algolia.fetcher.entities.UserEntity;
import com.algolia.fetcher.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {HackerNewsFetcherApplication.class, MongoTestConfig.class})
@Testcontainers
@ActiveProfiles("test")
class AuthenticationServiceIT {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String testUsername = "testuser";
    private final String testPassword = "testpass";

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        UserEntity testUser = UserEntity.builder()
                .username(testUsername)
                .password(passwordEncoder.encode(testPassword))
                .roles(Set.of("USER"))
                .build();

        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void authenticateUser_WithValidCredentials_ReturnsToken() {
        String token = authenticationService.authenticateUser(testUsername, testPassword);

        assertThat(token).isNotBlank();
    }

    @Test
    void authenticateUser_WithInvalidPassword_ThrowsException() {
        Exception exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> authenticationService.authenticateUser(testUsername, "wrongpassword")
        );
        
        assertThat(exception.getMessage()).contains("401 UNAUTHORIZED");
    }

    @Test
    void createInitialUser_ShouldCreateAdminUserIfNotExists() {
        userRepository.deleteAll();
        
        authenticationService.createInitialUser();
        
        assertTrue(userRepository.findByUsername("admin").isPresent(), "Admin user should be created");
        UserEntity adminUser = userRepository.findByUsername("admin").orElseThrow();
        assertTrue(passwordEncoder.matches("admin", adminUser.getPassword()), "Password should match");
        assertThat(adminUser.getRoles()).contains("ADMIN");
    }
}
