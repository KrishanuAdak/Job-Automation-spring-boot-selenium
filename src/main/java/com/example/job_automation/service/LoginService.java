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

        // Print all inputs found on page
        System.out.println("=== ALL INPUTS ON PAGE ===");
        driver.findElements(By.tagName("input")).forEach(el -> {
            System.out.println("Input - type: " + el.getAttribute("type")
                + " | placeholder: " + el.getAttribute("placeholder")
                + " | id: " + el.getAttribute("id")
                + " | name: " + el.getAttribute("name"));
        });
        System.out.println("=== END INPUTS ===");

        // Close popup if present
        try {
            WebElement closeBtn = driver.findElement(
                By.xpath("//button[contains(@class,'close')] | //*[contains(@class,'modal')]//button")
            );
            closeBtn.click();
            Thread.sleep(1000);
            System.out.println("Closed popup");
        } catch (Exception ignored) {
            System.out.println("No popup found");
        }

        // Try multiple xpaths to find username field
        WebElement username = null;
        String[] usernameXpaths = {
            "//input[@id='usernameField']",
            "//input[@id='username']",
            "//input[contains(@placeholder,'Email ID')]",
            "//input[contains(@placeholder,'Email')]",
            "//input[contains(@placeholder,'email')]",
            "//input[contains(@placeholder,'Username')]",
            "//input[contains(@placeholder,'username')]",
            "//input[@type='text']",
            "//input[@name='username']",
            "//input[@name='email']",
        };

        for (String xpath : usernameXpaths) {
            try {
                username = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                System.out.println("✅ Found username field with xpath: " + xpath);
                break;
            } catch (Exception e) {
                System.out.println("❌ xpath failed: " + xpath);
            }
        }

        if (username == null) {
            takeScreenshot("username_not_found.png");
            throw new RuntimeException("Could not find username field!");
        }

        username.click();
        username.clear();
        username.sendKeys(naukri_username);
        System.out.println("Entered username");

        // Try multiple xpaths to find password field
        WebElement password = null;
        String[] passwordXpaths = {
            "//input[@type='password']",
            "//input[contains(@placeholder,'Password')]",
            "//input[contains(@placeholder,'password')]",
            "//input[@id='passwordField']",
            "//input[@id='password']",
            "//input[@name='password']",
        };

        for (String xpath : passwordXpaths) {
            try {
                password = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                System.out.println("✅ Found password field with xpath: " + xpath);
                break;
            } catch (Exception e) {
                System.out.println("❌ xpath failed: " + xpath);
            }
        }

        if (password == null) {
            takeScreenshot("password_not_found.png");
            throw new RuntimeException("Could not find password field!");
        }

        password.click();
        password.clear();
        password.sendKeys(naukri_password);
        System.out.println("Entered password");

        // Try multiple xpaths for login button
        WebElement loginBtn = null;
        String[] loginBtnXpaths = {
            "//button[normalize-space()='Login']",
            "//button[contains(text(),'Login')]",
            "//button[@type='submit']",
            "//input[@type='submit']",
            "//button[contains(@class,'login')]",
        };

        for (String xpath : loginBtnXpaths) {
            try {
                loginBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
                System.out.println("✅ Found login button with xpath: " + xpath);
                break;
            } catch (Exception e) {
                System.out.println("❌ xpath failed: " + xpath);
            }
        }

        if (loginBtn == null) {
            takeScreenshot("loginbtn_not_found.png");
            throw new RuntimeException("Could not find login button!");
        }

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