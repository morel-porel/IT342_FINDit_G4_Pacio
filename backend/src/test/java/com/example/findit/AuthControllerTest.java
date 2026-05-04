package com.example.findit;

import com.example.findit.feature.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=ThisIsADummySecretKeyThatIsAtLeastSixtyFourCharactersLongForTestingHS512",
        "jwt.expiration=900000",
        "spring.datasource.url=jdbc:postgresql://localhost:5432/dummy",
        "spring.datasource.username=dummy",
        "spring.datasource.password=dummy",
        "spring.security.oauth2.client.registration.google.client-id=dummy",
        "spring.security.oauth2.client.registration.google.client-secret=dummy"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuthService authService;

    @Test
    void register_ShouldReturnOk() throws Exception {
        String json = "{\"fullName\":\"Test\",\"email\":\"t@t.com\",\"password\":\"p\",\"confirmPassword\":\"p\"}";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void getMe_WithoutToken_ShouldReturn401() throws Exception {
        // Spring Security will intercept this before it hits the controller
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
