package com.example.job_automation.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class LoginService {
       @Value("${naukri.username}")
       private String naukri_username;
       @Value("${naukri.password}")
       private String naukri_password;
       @Autowired
       WebDriver driver;
        public void login() throws InterruptedException {

        driver.get("https://www.naukri.com/nlogin/login");

        Thread.sleep(4000);

        driver.findElement(By.id("usernameField"))
                .sendKeys(naukri_username);

        driver.findElement(By.id("passwordField"))
                .sendKeys(naukri_password);

        driver.findElement(By.xpath("//button[@type='submit']"))
                .click();

        Thread.sleep(6000);

        System.out.println("Login successful");
    }


}
