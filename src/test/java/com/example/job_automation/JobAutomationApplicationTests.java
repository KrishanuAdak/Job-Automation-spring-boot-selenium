package com.example.job_automation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Disabled("Disabled to prevent accidental execution during development. Enable when ready to run tests.")
class JobAutomationApplicationTests {

	@Test
	void contextLoads() {
	}

}
