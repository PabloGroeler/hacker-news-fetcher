package com.algolia.fetcher.controllers;

import com.algolia.fetcher.services.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private final String testUsername = "testuser";
    private final String testPassword = "testpass";

    @Test
    void authenticateUser_ValidCredentials_ReturnsToken() {
        String testToken = "test.jwt.token";
        when(authService.authenticateUser(testUsername, testPassword))
                .thenReturn(testToken);

        ResponseEntity<String> response = authenticationController.authenticateUser(testUsername, testPassword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().getContentType());
        assertEquals(testToken, response.getBody());
        
        verify(authService, times(1)).authenticateUser(testUsername, testPassword);
    }

    @Test
    void authenticateUser_InvalidCredentials_ThrowsBadCredentialsException() {
        String invalidCredentials = "Invalid username or password";
        when(authService.authenticateUser(testUsername, testPassword))
                .thenThrow(new BadCredentialsException(invalidCredentials));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authenticationController.authenticateUser(testUsername, testPassword)
        );
        
        assertEquals(invalidCredentials, exception.getMessage());
        verify(authService, times(1)).authenticateUser(testUsername, testPassword);
    }
}
