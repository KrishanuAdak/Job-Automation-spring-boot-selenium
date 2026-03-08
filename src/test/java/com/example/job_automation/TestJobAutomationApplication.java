package com.example.job_automation;

import org.springframework.boot.SpringApplication;

public class TestJobAutomationApplication {

	public static void main(String[] args) {
		SpringApplication.from(JobAutomationApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
