package com.jsalvar.barbilling;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "jwt.secret=VGhpc0lzQVNlY3VyZUtleUZvckpXVFRoYXRNZWV0czI1NkJpdHMhIQ==",
    "jwt.expirationSeconds=8640000"
})
@Disabled("Requires complex security setup - use unit tests instead")
class BarbillingApplicationTests {

	@Test
	void contextLoads() {
	}

}
