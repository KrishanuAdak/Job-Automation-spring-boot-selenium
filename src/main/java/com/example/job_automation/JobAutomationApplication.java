package com.example.job_automation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.job_automation.service.JobAutomationService;
import com.example.job_automation.service.LoginService;

@SpringBootApplication
public class JobAutomationApplication {
	// public JobAutomationApplication(JobAutomationService automation) throws InterruptedException {
	// 	automation.searchJobs();
	// }

	public static void main(String[] args) {
		SpringApplication.run(JobAutomationApplication.class, args);
		
	}
	    @Bean
    public CommandLineRunner run(LoginService loginService, JobAutomationService jobAutomationService) {
        return args -> {
            loginService.login();
            jobAutomationService.processJobs();
        };
    }

}
