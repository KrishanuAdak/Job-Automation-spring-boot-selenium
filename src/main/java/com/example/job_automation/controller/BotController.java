package com.example.job_automation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.job_automation.service.EmailService;
import com.example.job_automation.service.JobApplyService;
import com.example.job_automation.service.JobAutomationService;
import com.example.job_automation.service.LoginService;

@RestController
public class BotController {
    @Autowired
    private  LoginService loginService;
    private JobAutomationService jobSearchService;
    private JobApplyService jobApplyService;
    private EmailService emailService;

    public BotController(LoginService loginService,
                         JobAutomationService jobSearchService,
                         JobApplyService jobApplyService, EmailService emailService) {

        this.loginService = loginService;
        this.jobSearchService = jobSearchService;
        this.jobApplyService = jobApplyService;
        this.emailService = emailService;
    }

    @GetMapping("/run-bot")
    public String runBot() throws Exception {

        //loginService.login();

        jobSearchService.processJobs();

        //jobApplyService.applyToJobs();

        return "Auto apply completed";
    }
    // @GetMapping("/send-email")
    // public String sendEmail() {
    //     String body = "This is a test email from the Job Automation Bot.";
    //     emailService.sendEmail(body);
    //     return "Email sent";
    // }

}