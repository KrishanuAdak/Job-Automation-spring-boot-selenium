package com.example.job_automation.service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;

@Service
public class JobAutomationService {

    private final EmailService emailService;
    private final WebDriver driver;

    private final String job_url =
            "https://www.naukri.com/java-spring-boot-developer-jobs?experience=3&jobAge=1";

    private static final int MAX_DURATION_MINUTES = 35;

    private int totalApplied = 0;
    private int totalSkipped = 0;

    // Excel
    private Workbook workbook;
    private Sheet sheet;
    private int excelRowNum = 1;

    public JobAutomationService(WebDriver driver,
                                EmailService emailService) {
        this.driver = driver;
        this.emailService = emailService;
    }

    private void initExcel() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Job Applications");
        Row header = sheet.createRow(0);
        String[] columns = {"#", "Job Title", "Company", "Status", "Reason", "Job URL", "Timestamp"};

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 6000);
        }
        sheet.setColumnWidth(1, 12000);
        sheet.setColumnWidth(5, 15000);
    }

    private void addExcelRow(int num, String title, String company,
                             String status, String reason, String url) {
        Row row = sheet.createRow(excelRowNum++);
        row.createCell(0).setCellValue(num);
        row.createCell(1).setCellValue(title);
        row.createCell(2).setCellValue(company);
        row.createCell(3).setCellValue(status);
        row.createCell(4).setCellValue(reason);
        row.createCell(5).setCellValue(url);
        row.createCell(6).setCellValue(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        CellStyle style = workbook.createCellStyle();
        if (status.equals("✅ Applied")) {
            style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        } else if (status.equals("⚠ Skipped")) {
            style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        } else {
            style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        for (int i = 0; i <= 6; i++) {
            row.getCell(i).setCellStyle(style);
        }
    }

    private File saveExcel() {
        try {
            String fileName = "naukri_applications_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm")) + ".xlsx";

            // ✅ Works on both local and GitHub Actions
            File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            System.out.println("\n📊 Excel saved to: " + file.getAbsolutePath());
            return file;

        } catch (Exception e) {
            System.out.println("❌ Failed to save Excel: " + e.getMessage());
            return null;
        }
    }

    // ✅ Check if 30 minutes has passed
    private boolean isTimeUp(Instant startTime) {
        long elapsedMinutes = Duration.between(startTime, Instant.now()).toMinutes();
        if (elapsedMinutes >= MAX_DURATION_MINUTES) {
            System.out.println("\n⏰ 30 minutes reached! Stopping job applications.");
            return true;
        }
        return false;
    }

    public void processJobs() throws InterruptedException, MessagingException {

        initExcel();

        // ✅ Start the 30-minute timer
        Instant startTime = Instant.now();
        System.out.println("⏱ Started at: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss")) + " | Will stop after 30 minutes.");

        driver.get(job_url);
        handleLocationPopup(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        int pageNum = 1;
        int jobNum = 0;

        while (true) {

            // ✅ Check time at start of each page
            if (isTimeUp(startTime)) break;

            System.out.println("\n========== PAGE " + pageNum + " ==========");

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".cust-job-tuple")));
            } catch (Exception e) {
                System.out.println("No jobs found on page " + pageNum);
                break;
            }

            List<WebElement> jobs = driver.findElements(By.cssSelector(".cust-job-tuple"));
            System.out.println("Jobs on this page: " + jobs.size());

            String mainTab = driver.getWindowHandle();

            for (int i = 0; i < jobs.size(); i++) {

                // ✅ Check time before each job
                if (isTimeUp(startTime)) break;

                // ✅ Print remaining time
                long elapsed = Duration.between(startTime, Instant.now()).toMinutes();
                System.out.println("⏱ Time elapsed: " + elapsed + "/" + MAX_DURATION_MINUTES + " mins");

                String title = "Unknown";
                String company = "Unknown";
                String jobUrl = "";

                try {
                    jobs = driver.findElements(By.cssSelector(".cust-job-tuple"));
                    WebElement job = jobs.get(i);

                    title = job.findElement(By.cssSelector("a.title")).getText();

                    try {
                        company = job.findElement(
                                By.cssSelector(".comp-name, [class*='comp-name'], a.comp-name")).getText();
                    } catch (Exception ignored) {}

                    jobUrl = job.findElement(By.cssSelector("a.title")).getAttribute("href");
                    jobNum++;

                    System.out.println("\n[Page " + pageNum + " | " + (i + 1) + "/" + jobs.size() + "] "
                            + title + " @ " + company);

                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", job);
                    Thread.sleep(1000);

                    ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", jobUrl);

                    List<String> tabs = new ArrayList<>(driver.getWindowHandles());
                    driver.switchTo().window(tabs.get(tabs.size() - 1));
                    Thread.sleep(3000);

                    handleLocationPopup(driver);

                    List<WebElement> applyButtons = driver.findElements(By.cssSelector(".apply-button"));
                    WebElement applyBtn = null;

                    for (WebElement btn : applyButtons) {
                        if (btn.getText().trim().equalsIgnoreCase("Apply")) {
                            applyBtn = btn;
                            break;
                        }
                    }

                    if (applyBtn == null) {
                        System.out.println("⚠ No Apply button, skipping.");
                        totalSkipped++;
                        addExcelRow(jobNum, title, company, "⚠ Skipped", "No Apply button", jobUrl);
                    } else {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", applyBtn);
                        System.out.println("✓ Clicked Apply!");
                        Thread.sleep(2000);

                        handleLocationPopup(driver);
                        handleChatbotQuestions(driver, wait);

                        totalApplied++;
                        System.out.println("✅ Applied to: " + title);
                        addExcelRow(jobNum, title, company, "✅ Applied", "", jobUrl);
                    }

                } catch (Exception e) {
                    System.out.println("❌ Error: " + e.getMessage());
                    totalSkipped++;
                    addExcelRow(jobNum, title, company, "❌ Error", e.getMessage(), jobUrl);

                } finally {
                    try {
                        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
                        if (tabs.size() > 1) driver.close();
                        driver.switchTo().window(mainTab);
                        Thread.sleep(1000);
                    } catch (Exception ignored) {}
                }
            }

            // ✅ Check time before going to next page
            if (isTimeUp(startTime)) break;

            try {
                ((JavascriptExecutor) driver).executeScript(
                        "window.scrollTo(0, document.body.scrollHeight)");
                Thread.sleep(1000);

                List<WebElement> nextBtns = driver.findElements(
                        By.xpath("//a[contains(@class,'btn-secondary') and .//span[text()='Next']]"));

                if (nextBtns.isEmpty() || !nextBtns.get(0).isDisplayed()) {
                    System.out.println("\n✅ No more pages.");
                    break;
                }

                String nextUrl = nextBtns.get(0).getAttribute("href");
                System.out.println("\n→ Going to page " + (pageNum + 1));
                driver.get(nextUrl);
                pageNum++;
                Thread.sleep(3000);

            } catch (Exception e) {
                System.out.println("Pagination error: " + e.getMessage());
                break;
            }
        }

        // ✅ Save and email regardless of why loop ended
        System.out.println("\n⏱ Total time: " +
                Duration.between(startTime, Instant.now()).toMinutes() + " minutes");

        File excelFile = saveExcel();

        if (excelFile != null && excelFile.exists()) {
            emailService.sendEmail(excelFile);
            System.out.println("📧 Email sent successfully.");
        } else {
            System.out.println("❌ Excel file not found.");
        }

        System.out.println("\n==============================");
        System.out.println("✅ Total Applied : " + totalApplied);
        System.out.println("⚠ Total Skipped : " + totalSkipped);
        System.out.println("==============================");
    }

    private void handleChatbotQuestions(WebDriver driver, WebDriverWait wait) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            shortWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".chatbot_Drawer, [class*='chatbot']")));
            System.out.println("→ Chatbot detected");
        } catch (Exception e) {
            System.out.println("→ Chatbot not found");
        }
    }

    private void handleLocationPopup(WebDriver driver) {
        try {
            WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            alertWait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}
    }
}