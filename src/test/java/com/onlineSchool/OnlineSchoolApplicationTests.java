package com.onlineSchool;

import com.onlineSchool.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {OnlineSchoolApplication.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class OnlineSchoolApplicationTests {

	@Test
	void contextLoads() {
	}

} 