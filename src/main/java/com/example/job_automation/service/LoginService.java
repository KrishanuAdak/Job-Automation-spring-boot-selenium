package com.example.job_automation.service;

import java.io.File;
import java.time.Duration;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
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

    public void login() throws Exception {

        driver.get("https://www.naukri.com/nlogin/login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        // Give page time to fully render
        Thread.sleep(5000);

        System.out.println("URL: " + driver.getCurrentUrl());
        System.out.println("TITLE: " + driver.getTitle());
        System.out.println("PAGE SOURCE LENGTH: " + driver.getPageSource().length());

        // Take screenshot to see what page looks like
        takeScreenshot("after_load.png");

        // Close any popup/overlay if present
        try {
            WebElement closeBtn = driver.findElement(
                By.xpath("//*[contains(@class,'close') or contains(@class,'modal')]//button | //button[contains(@class,'close')]")
            );
            closeBtn.click();
            Thread.sleep(1000);
            System.out.println("Closed popup");
        } catch (Exception ignored) {
            System.out.println("No popup found");
        }

        // FIX: Use placeholder-based xpath instead of type='text'
        WebElement username = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='Enter your active Email ID / Username']")
            )
        );
        username.click();
        username.clear();
        username.sendKeys(naukri_username);
        System.out.println("Entered username");

        WebElement password = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@placeholder='Enter your password']")
            )
        );
        password.click();
        password.clear();
        password.sendKeys(naukri_password);
        System.out.println("Entered password");

        // FIX: Use specific login button xpath
        WebElement loginBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Login']")
            )
        );
        loginBtn.click();
        System.out.println("Clicked login");

        Thread.sleep(5000);
        takeScreenshot("after_login.png");
        System.out.println("Login done. Current URL: " + driver.getCurrentUrl());
    }

    private void takeScreenshot(String filename) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(src, new File(filename));
            System.out.println("Screenshot saved: " + filename);
        } catch (Exception e) {
            System.out.println("Screenshot failed: " + e.getMessage());
        }
    }
}