package com.example.job_automation.service;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

@Service
public class JobApplyService {

    private final WebDriver driver;

    //private String resumePath;

    public JobApplyService(WebDriver driver) {
        this.driver = driver;
    }

    public void applyToJobs() throws InterruptedException {

        List<WebElement> jobs =
                driver.findElements(By.cssSelector(".cust-job-tuple"));

        System.out.println("Jobs detected: " + jobs.size());

        for (int i = 0; i < jobs.size(); i++) {

            jobs = driver.findElements(By.cssSelector(".cust-job-tuple"));

            WebElement job = jobs.get(i);

            String title =
                    job.findElement(By.cssSelector("a.title")).getText();

            System.out.println("Opening job: " + title);

            job.click();

            Thread.sleep(5000);

            applyIfPossible(title);

            Thread.sleep(4000);
        }
    }

    private void applyIfPossible(String title) {

        try {

            System.out.println("Checking apply option for: " + title);

            // WebDriverWait wait =
            //         new WebDriverWait(driver, Duration.ofSeconds(10));

            // wait.until(ExpectedConditions.presenceOfElementLocated(
            //         By.cssSelector("button")));

            // List<WebElement> buttons =
            //         driver.findElements(By.tagName("button"));

            // System.out.println(buttons.size() + " buttons detected");

            // for (WebElement btn : buttons) {

            //     String text = btn.getText().toLowerCase();

            //     System.out.println("Button text: " + text);

            //     if (text.contains("apply on naukri")) {

            //         btn.click();

            //         System.out.println("Apply clicked for: " + title);

            //         Thread.sleep(3000);

            //         uploadResume();

            //         sendMessageToRecruiter();

            //         return;
            //     }
            // }

            // System.out.println("Apply button not available for: " + title);

        } catch (Exception e) {

            System.out.println("Apply failed for: " + title);
            e.printStackTrace();
        }
    }

    // private void uploadResume() {

    //     try {

    //         WebElement upload =
    //                 driver.findElement(By.xpath("//input[@type='file']"));

    //         upload.sendKeys(resumePath);

    //         System.out.println("Resume uploaded");

    //     } catch (Exception e) {

    //         System.out.println("Resume upload not required");
    //     }
    // }

    // private void sendMessageToRecruiter() {

    //     try {

    //         WebElement msgBox =
    //                 driver.findElement(By.tagName("textarea"));

    //         msgBox.sendKeys(
    //                 "Hello, I am a Java Spring Boot developer with experience in microservices and backend systems. I would love to discuss this opportunity.");

    //         System.out.println("Recruiter message sent");

    //     } catch (Exception e) {

    //         System.out.println("Recruiter message option not available");
    //     }
    // }
}