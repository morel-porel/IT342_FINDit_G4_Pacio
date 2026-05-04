package com.example.findit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "jwt.secret=ThisIsADummySecretKeyThatIsAtLeastSixtyFourCharactersLongForTestingHS512",
        "jwt.expiration=900000",
        "spring.datasource.url=jdbc:postgresql://localhost:5432/dummy",
        "spring.datasource.username=dummy",
        "spring.datasource.password=dummy",
        "spring.security.oauth2.client.registration.google.client-id=dummy",
        "spring.security.oauth2.client.registration.google.client-secret=dummy"
})
@ActiveProfiles("test")
class FinditApplicationTests {

	@Test
	void contextLoads() {
	}

}
