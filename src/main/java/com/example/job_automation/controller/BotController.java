package com.example.job_automation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.job_automation.service.JobApplyService;
import com.example.job_automation.service.JobAutomationService;
import com.example.job_automation.service.LoginService;

@RestController
public class BotController {

    private final LoginService loginService;
    private final JobAutomationService jobSearchService;
    private final JobApplyService jobApplyService;

    public BotController(LoginService loginService,
                         JobAutomationService jobSearchService,
                         JobApplyService jobApplyService) {

        this.loginService = loginService;
        this.jobSearchService = jobSearchService;
        this.jobApplyService = jobApplyService;
    }

    @GetMapping("/run-bot")
    public String runBot() throws Exception {

        loginService.login();

        jobSearchService.processJobs();

        //jobApplyService.applyToJobs();

        return "Auto apply completed";
    }
}