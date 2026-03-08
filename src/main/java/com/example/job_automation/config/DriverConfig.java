package com.example.job_automation.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bonigarcia.wdm.WebDriverManager;

@Configuration
public class DriverConfig {
    @Bean
    public WebDriver webDriver() {

        WebDriverManager.firefoxdriver().setup();

        WebDriver driver = new FirefoxDriver();

        return driver;
    }

    
}
