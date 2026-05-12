package com.example.findit.security;

import com.example.findit.feature.auth.security.TokenProvider;
import com.example.findit.feature.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TokenProviderTest {

    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProvider();
        // Manually inject values since we aren't loading the full Spring context
        ReflectionTestUtils.setField(tokenProvider, "secretKey", "64CharLongSecretKeyForTestingPurposesOnlyMustBeAtLeastSixtyFourChars!");
        ReflectionTestUtils.setField(tokenProvider, "validityInMs", 3600000L);
    }

    @Test
    void createToken_ShouldGenerateValidJWT() {
        User user = new User();
        user.setId(123L);

        String token = tokenProvider.createToken(user);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(123L, tokenProvider.getUserIdFromToken(token));
    }

    @Test
    void validateToken_ShouldReturnFalseForTamperedToken() {
        User user = new User();
        user.setId(1L);
        String token = tokenProvider.createToken(user);
        String tamperedToken = token + "modified";

        assertFalse(tokenProvider.validateToken(tamperedToken));
    }
}