package com.algolia.fetcher.services;

import com.algolia.fetcher.entities.UserEntity;
import com.algolia.fetcher.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private final String testUsername = "testuser";
    private final String testPassword = "testpass";
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .username(testUsername)
                .password("encodedPassword")
                .roles(Set.of("USER"))
                .build();
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsJwtToken() {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, testUser.getPassword())).thenReturn(true);
        String testToken = "test.jwt.token";
        when(jwtService.generateJWT(eq(testUsername), anyMap())).thenReturn(testToken);

        String result = authenticationService.authenticateUser(testUsername, testPassword);

        assertNotNull(result);
        assertEquals(testToken, result);
        verify(userRepository).findByUsername(testUsername);
        verify(passwordEncoder).matches(testPassword, testUser.getPassword());
        verify(jwtService).generateJWT(eq(testUsername), anyMap());
    }

    @Test
    void authenticateUser_UserNotFound_ThrowsUnauthorized() {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.authenticateUser(testUsername, testPassword)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("401 UNAUTHORIZED \"Username or password is incorrect.\"", exception.getMessage());
        verify(userRepository).findByUsername(testUsername);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticateUser_InvalidPassword_ThrowsUnauthorized() {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, testUser.getPassword())).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.authenticateUser(testUsername, testPassword)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("401 UNAUTHORIZED \"Username or password is incorrect.\"", exception.getMessage());
        verify(userRepository).findByUsername(testUsername);
        verify(passwordEncoder).matches(testPassword, testUser.getPassword());
        verifyNoInteractions(jwtService);
    }

}
