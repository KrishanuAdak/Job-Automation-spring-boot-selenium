package com.example.job_automation.service;

import java.io.File;
import java.time.Duration;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
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

    @Value("${NAUKRI_USERNAME}")
    private String naukri_username;

    @Value("${NAUKRI_PASSWORD}")
    private String naukri_password;

    @Autowired
    WebDriver driver;

    public void login() throws Exception {

        driver.get("https://www.naukri.com/nlogin/login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        Thread.sleep(5000);

        System.out.println("URL: " + driver.getCurrentUrl());
        System.out.println("TITLE: " + driver.getTitle());
        takeScreenshot("after_load.png");

        // ✅ Find and fill username using id directly
        WebElement username = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("usernameField"))
        );
        // ✅ Use JS to set value — more reliable in headless
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].value = '';", username);
        username.click();
        Thread.sleep(500);
        username.sendKeys(naukri_username);
        Thread.sleep(500);
        System.out.println("Entered username: " + driver.findElement(
            By.id("usernameField")).getAttribute("value"));

        // ✅ Find and fill password using id directly
        WebElement password = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("passwordField"))
        );
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].value = '';", password);
        password.click();
        Thread.sleep(500);
        password.sendKeys(naukri_password);
        Thread.sleep(500);
        System.out.println("Entered password");

        takeScreenshot("before_login_click.png");

        // ✅ Try 3 ways to click login button
        WebElement loginBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Login']")
            )
        );

        // Method 1: Normal click
        try {
            loginBtn.click();
            System.out.println("Clicked login - normal click");
        } catch (Exception e) {
            System.out.println("Normal click failed, trying JS click");
            // Method 2: JavaScript click
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();", loginBtn);
            System.out.println("Clicked login - JS click");
        }

        Thread.sleep(2000);
        takeScreenshot("after_click.png");

        // Method 3: If still on login page, press Enter
        if (driver.getCurrentUrl().contains("nlogin/login")) {
            System.out.println("Still on login page, trying Enter key");
            password.sendKeys(Keys.ENTER);
            Thread.sleep(2000);
            takeScreenshot("after_enter.png");
        }

        // ✅ Wait for redirect away from login page
        try {
            WebDriverWait redirectWait = new WebDriverWait(driver, Duration.ofSeconds(20));
            redirectWait.until(ExpectedConditions.not(
                ExpectedConditions.urlContains("nlogin/login")
            ));
            System.out.println("✅ Login successful!");
        } catch (Exception e) {
            takeScreenshot("login_failed.png");
            System.out.println("❌ Still on login page after all attempts");
            System.out.println("Page source length: " + driver.getPageSource().length());
            throw new RuntimeException("Login failed!");
        }

        Thread.sleep(2000);
        takeScreenshot("after_login.png");
        System.out.println("✅ Login done. Current URL: " + driver.getCurrentUrl());
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