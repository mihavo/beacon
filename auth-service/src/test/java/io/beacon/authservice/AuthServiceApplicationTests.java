package io.beacon.authservice;

import io.beacon.authservice.utils.JWTUtility;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.private-key-path=ignored-for-tests",
    "jwt.public-key-path=ignored-for-tests",
})
class AuthServiceApplicationTests {

  @MockitoBean JWTUtility jwtUtility;

  @Test
    void contextLoads() {
    }

}
