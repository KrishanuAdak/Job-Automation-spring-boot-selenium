package com.example.job_automation.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bonigarcia.wdm.WebDriverManager;

@Configuration
public class DriverConfig {
   @Bean
    public WebDriver webDriver() {

    WebDriverManager.firefoxdriver().setup();

    FirefoxOptions options = new FirefoxOptions();

    // 🔥 VERY IMPORTANT
    options.addArguments("--headless");

    // Optional but recommended for CI
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");

    return new FirefoxDriver(options);
}

    
}
