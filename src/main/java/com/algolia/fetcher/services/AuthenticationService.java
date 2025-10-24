package com.algolia.fetcher.services;

import com.algolia.fetcher.entities.UserEntity;
import com.algolia.fetcher.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

  public static final String JWT_SCOPES_CLAIM = "scope";

  public static final String JWT_USER_CLAIM = "user";

  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;


  @PostConstruct
  public void createInitialUser() {
    userRepository.findByUsername("admin").ifPresentOrElse(
            user -> log.debug("Admin user already exists"),
            () -> {
              userRepository.save(UserEntity.builder()
                      .username("admin")
                      .password(passwordEncoder.encode("admin"))
                      .roles(Set.of("ADMIN"))
                      .build());
              log.info("Created default admin user with username 'admin' and password 'admin' and ADMIN roles");
            }
    );
  }


  public String authenticateUser(String username, String password) {

    Optional<UserEntity> optionalUser = userRepository.findByUsername(username);

    if (optionalUser.isEmpty() || !passwordEncoder.matches(password,
        optionalUser.get().getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
          "Username or password is incorrect.");
    }

    return jwtService.generateJWT(username,
        Map.of(JWT_SCOPES_CLAIM, optionalUser.get().getRoles(), JWT_USER_CLAIM, optionalUser.get().getUsername()));
  }
}
