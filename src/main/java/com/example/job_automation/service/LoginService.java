package com.example.job_automation.service;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class LoginService {
       @Value("${NAUKRI_USERNAME:${naukri.username}}")
       private String naukri_username;
       @Value("${NAUKRI_PASSWORD:${naukri.password}}")
       private String naukri_password;
       @Autowired
       WebDriver driver;
           public void login() {

        driver.get("https://www.naukri.com/nlogin/login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Debug logs (VERY IMPORTANT)
        System.out.println("URL: " + driver.getCurrentUrl());
        System.out.println("Title: " + driver.getTitle());

        // Username field (more precise)
        WebElement username = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='Enter your active Email ID / Username']")
            )
        );
        username.sendKeys(naukri_username);

        // Password field
        WebElement password = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='password']")
            )
        );
        password.sendKeys(naukri_password);

        // Login button
        WebElement loginBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[text()='Login']")
            )
        );
        loginBtn.click();

        // Wait after login (IMPORTANT)
        wait.until(ExpectedConditions.urlContains("naukri.com"));

        System.out.println("Login attempted...");
       System.out.println("Login successful...");
}

     


}
