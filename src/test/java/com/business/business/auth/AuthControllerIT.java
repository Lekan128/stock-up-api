package com.business.business.auth;

import com.business.business.integration.AbstractIntegrationTest;
import com.business.business.store.Store;
import com.business.business.store.StoreRepository;
import com.business.business.user.User;
import com.business.business.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private StoreRepository storeRepository;

    User user;

    @BeforeEach
    @Transactional
    void setup(){
        Store store = new Store();
        store.name = "Two's";
        storeRepository.save(store);

        user = User.builder()
                .fistName("Two")
                .lastName("Three")
                .email("one@two.com")
                .password(passwordEncoder.encode("Wordpass123"))
                .store(store)
                .build();

        userRepository.save(user);
    }
    @AfterEach
    @Transactional
    void tearDown(){
        storeRepository.deleteAll();
        userRepository.deleteAll();
    }


//    @Test
    void shouldCreateUser() {
        String registerRequest = """
                {
                    "firstName": "La",
                    "lastName": "Cali",
                    "email": "la@side.com",
                    "password": "Wordpass123",
                    "mobileNumber": "090",
                    "StoreName": "La Delicious"
                }
                """;

        webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty();
    }

    @Test
    void shouldLogin() {
        String loginRequest = """
                {
                    "email": "joe",
                    "password": "Wordpass123"
                }
                """;

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty();

        /*String accessToken = authService.generateTokensSaveTokensAndDeleteExpiredTokens(user).accessToken;
        .header(HttpHeaders.AUTHORIZATION, accessToken)*/
    }
}
