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

       public void login() throws Exception {

    driver.get("https://www.naukri.com/nlogin/login");

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

    Thread.sleep(3000); // anti-bot delay

    System.out.println("URL: " + driver.getCurrentUrl());
    System.out.println("TITLE: " + driver.getTitle());

    WebElement username = wait.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@type='text']")
        )
    );
    username.sendKeys(naukri_username);

    WebElement password = wait.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//input[@type='password']")
        )
    );
    password.sendKeys(naukri_password);

    WebElement loginBtn = wait.until(
        ExpectedConditions.elementToBeClickable(
            By.xpath("//button")
        )
    );
    loginBtn.click();
}
         
     


}
