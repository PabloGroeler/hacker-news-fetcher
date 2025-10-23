package com.algolia.fetcher.controllers;

import com.algolia.fetcher.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication")
@RequestMapping("api/v1/auth")
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping(value = "login", produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Authenticates the user's credentials and returns a JWT Token.",
      description = "Checks the login credentials and if they match with the one on our database, it will create a JWT Token for that user.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
          @ApiResponse(responseCode = "400", description = "Bad Request"),
          @ApiResponse(responseCode = "401", description = "Failed to authenticate user credentials")
      }
  )
  public ResponseEntity<String> authenticateUser(
      @RequestHeader String username,
      @RequestHeader String password) {
    String token = authenticationService.authenticateUser(username, password);
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_PLAIN)
        .body(token);
  }
}
