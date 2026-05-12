package com.example.findit;

import com.example.findit.feature.item.ItemService;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
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
class ItemControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ItemService itemService;
    @Test
    @WithMockUser(username = "1", roles = "USER")
    void createItem_ShouldReturn200() throws Exception {
        String itemJson = "{\"type\":\"LOST\",\"name\":\"Keys\",\"category\":\"Other\",\"location\":\"Gym\",\"dateLostFound\":\"2023-10-10\"}";

        mockMvc.perform(post("/api/items")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(itemJson))
                .andExpect(status().isOk());
    }
    @Test
    void getAllItems_ShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/items"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getItemDetail_ShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk());
    }

}
