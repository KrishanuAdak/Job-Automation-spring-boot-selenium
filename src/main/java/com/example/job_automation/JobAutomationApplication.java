package com.example.job_automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JobAutomationApplication {
	// public JobAutomationApplication(JobAutomationService automation) throws InterruptedException {
	// 	automation.searchJobs();
	// }

	public static void main(String[] args) {
		SpringApplication.run(JobAutomationApplication.class, args);
	}

}
