package com.example.job_automation.service;

import java.io.FileOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Service
public class JobAutomationService {

    private final WebDriver driver;
    private final String job_url =
        "https://www.naukri.com/java-spring-boot-developer-jobs?experience=3&jobAge=1";

    private static final String EXPECTED_SALARY = "700000";
    private static final String DATE_OF_BIRTH = "26/04/2000";
    private static final String EXPERIENCE_YEARS = "2";
    private static final String NOTICE_PERIOD = "30";

    private int totalApplied = 0;
    private int totalSkipped = 0;

    // Excel tracking
    private Workbook workbook;
    private Sheet sheet;
    private int excelRowNum = 1;

    public JobAutomationService(WebDriver driver) {
        this.driver = driver;
    }

    private void initExcel() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Job Applications");

        // Header row
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
        sheet.setColumnWidth(1, 12000); // Job Title wider
        sheet.setColumnWidth(5, 15000); // URL wider
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

        // Color code by status
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

    private void saveExcel() {
        try {
            String fileName = "naukri_applications_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm")) + ".xlsx";
            String filePath = System.getProperty("user.home") + "/Desktop/" + fileName;
            FileOutputStream fos = new FileOutputStream(filePath);
            workbook.write(fos);
            fos.close();
            workbook.close();
            System.out.println("\n📊 Excel saved to: " + filePath);
        } catch (Exception e) {
            System.out.println("❌ Failed to save Excel: " + e.getMessage());
        }
    }

    public void processJobs() throws InterruptedException {
        initExcel();
        driver.get(job_url);
        handleLocationPopup(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        int pageNum = 1;
        int jobNum = 0;

        while (true) {
            System.out.println("\n========== PAGE " + pageNum + " ==========");

            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".cust-job-tuple")));
            } catch (Exception e) {
                System.out.println("No jobs found on page " + pageNum + ", stopping.");
                break;
            }

            List<WebElement> jobs = driver.findElements(By.cssSelector(".cust-job-tuple"));
            System.out.println("Jobs on this page: " + jobs.size());

            String mainTab = driver.getWindowHandle();

            for (int i = 0; i < jobs.size(); i++) {
                String title = "Unknown";
                String company = "Unknown";
                String jobUrl = "";

                try {
                    jobs = driver.findElements(By.cssSelector(".cust-job-tuple"));
                    WebElement job = jobs.get(i);

                    title = job.findElement(By.cssSelector("a.title")).getText();

                    // Try to get company name
                    try {
                        company = job.findElement(
                            By.cssSelector(".comp-name, [class*='comp-name'], a.comp-name"))
                            .getText();
                    } catch (Exception ignored) {}

                    jobUrl = job.findElement(By.cssSelector("a.title")).getAttribute("href");
                    jobNum++;

                    System.out.println("\n[Page " + pageNum + " | " + (i + 1) + "/" +
                        jobs.size() + "] " + title + " @ " + company);

                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView(true);", job);
                    Thread.sleep(500);

                    ((JavascriptExecutor) driver).executeScript(
                        "window.open(arguments[0]);", jobUrl);

                    List<String> tabs = new ArrayList<>(driver.getWindowHandles());
                    driver.switchTo().window(tabs.get(tabs.size() - 1));
                    Thread.sleep(3000);

                    handleLocationPopup(driver);

                    List<WebElement> applyButtons = driver.findElements(
                        By.cssSelector(".apply-button"));
                    WebElement applyBtn = null;
                    for (WebElement btn : applyButtons) {
                        if (btn.getText().trim().equalsIgnoreCase("Apply")) {
                            applyBtn = btn;
                            break;
                        }
                    }

                    if (applyBtn == null) {
                        System.out.println("  ⚠ No Apply button, skipping.");
                        totalSkipped++;
                        addExcelRow(jobNum, title, company, "⚠ Skipped", "No Apply button", jobUrl);
                    } else {
                        ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].click();", applyBtn);
                        System.out.println("  ✓ Clicked Apply!");
                        Thread.sleep(2000);
                        handleLocationPopup(driver);
                        handleChatbotQuestions(driver, wait);
                        totalApplied++;
                        System.out.println("  ✅ Applied to: " + title);
                        addExcelRow(jobNum, title, company, "✅ Applied", "", jobUrl);
                    }

                } catch (Exception e) {
                    System.out.println("  ❌ Error: " + e.getMessage());
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

            // ===== PAGINATION =====
            try {
                ((JavascriptExecutor) driver).executeScript(
                    "window.scrollTo(0, document.body.scrollHeight)");
                Thread.sleep(1000);

                List<WebElement> nextBtns = driver.findElements(
                    By.xpath("//a[contains(@class,'btn-secondary') and .//span[text()='Next']]"));

                if (nextBtns.isEmpty() || !nextBtns.get(0).isDisplayed()) {
                    System.out.println("\n✅ No more pages, all done!");
                    break;
                }

                String nextUrl = nextBtns.get(0).getAttribute("href");
                System.out.println("\n→ Going to page " + (pageNum + 1) + "...");
                driver.get(nextUrl);
                pageNum++;
                Thread.sleep(3000);
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");
                Thread.sleep(1000);

            } catch (Exception e) {
                System.out.println("Pagination error: " + e.getMessage() + ", stopping.");
                break;
            }
        }

        // Save Excel at the end
        saveExcel();

        System.out.println("\n==============================");
        System.out.println("✅ Total Applied : " + totalApplied);
        System.out.println("⚠  Total Skipped : " + totalSkipped);
        System.out.println("==============================");
    }

    private void handleChatbotQuestions(WebDriver driver, WebDriverWait wait) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            shortWait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".chatbot_Drawer, [class*='chatbot']")));

            System.out.println("  → Chatbot detected, answering questions...");

            int maxRounds = 10;
            for (int round = 0; round < maxRounds; round++) {
                Thread.sleep(1500);

                List<WebElement> chatbot = driver.findElements(
                    By.cssSelector(".chatbot_Drawer, [class*='chatbot_Drawer']"));
                if (chatbot.isEmpty() || !chatbot.get(0).isDisplayed()) {
                    System.out.println("  → Chatbot closed, done!");
                    break;
                }

                List<WebElement> botMessages = driver.findElements(
                    By.cssSelector(".botMsg, [class*='botMsg']"));
                String lastQuestion = "";
                if (!botMessages.isEmpty()) {
                    lastQuestion = botMessages.get(botMessages.size() - 1)
                        .getText().toLowerCase().trim();
                    System.out.println("  → Question: " + lastQuestion);
                }

                // DEBUG
                List<WebElement> contentEditable = driver.findElements(
                    By.xpath("//*[@contenteditable='true']"));
                for (WebElement el : contentEditable) {
                    if (el.isDisplayed()) {
                        System.out.println("    EDITABLE tag=[" + el.getTagName() +
                            "] id=[" + el.getAttribute("id") +
                            "] class=[" + el.getAttribute("class") + "]");
                    }
                }
                List<WebElement> chatbotChildren = driver.findElements(
                    By.cssSelector(".chatbot_Drawer *"));
                for (WebElement el : chatbotChildren) {
                    String tag = el.getTagName();
                    if (el.isDisplayed() && (tag.equals("input") || tag.equals("textarea") ||
                        (tag.equals("div") && el.getAttribute("contenteditable") != null))) {
                        System.out.println("    tag=[" + tag +
                            "] contenteditable=[" + el.getAttribute("contenteditable") +
                            "] class=[" + el.getAttribute("class") +
                            "] id=[" + el.getAttribute("id") + "]");
                    }
                }

                boolean answered = false;

                // 1. Radio buttons
                List<WebElement> radioOptions = driver.findElements(
                    By.cssSelector("[class*='singleselect-radiobutton'] [class*='container'], " +
                        "[id*='SingleSelectRadioButton'] div[id*='src']"));
                if (!radioOptions.isEmpty()) {
                    for (WebElement option : radioOptions) {
                        if (option.isDisplayed()) {
                            ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].click();", option);
                            System.out.println("  → Selected radio: " + option.getText());
                            answered = true;
                            Thread.sleep(1000);
                            break;
                        }
                    }
                }

                // 2. Contenteditable
                if (!answered) {
                    for (WebElement el : contentEditable) {
                        if (el.isDisplayed()) {
                            ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].innerHTML = '';", el);
                            String answer = getAnswerForQuestion(lastQuestion);
                            el.sendKeys(answer);
                            System.out.println("  → Typed in contenteditable: " + answer);
                            Thread.sleep(500);
                            el.sendKeys(Keys.ENTER);
                            answered = true;
                            Thread.sleep(1000);
                            break;
                        }
                    }
                }

                // 3. Any visible input
                if (!answered) {
                    List<WebElement> allInputs = driver.findElements(By.tagName("input"));
                    for (WebElement input : allInputs) {
                        try {
                            String type = input.getAttribute("type");
                            if (input.isDisplayed() && !type.equals("hidden")) {
                                input.clear();
                                String answer = getAnswerForQuestion(lastQuestion);
                                input.sendKeys(answer);
                                System.out.println("  → Typed in input: " + answer);
                                Thread.sleep(500);
                                input.sendKeys(Keys.ENTER);
                                answered = true;
                                Thread.sleep(1000);
                                break;
                            }
                        } catch (Exception ignored) {}
                    }
                }

                // 4. Chatbot button
                if (!answered) {
                    List<WebElement> sendBtns = driver.findElements(
                        By.cssSelector(".chatbot_Drawer button, [class*='chatbot'] button"));
                    for (WebElement btn : sendBtns) {
                        if (btn.isDisplayed() && btn.isEnabled()) {
                            ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].click();", btn);
                            System.out.println("  → Clicked chatbot button: " + btn.getText());
                            answered = true;
                            Thread.sleep(1000);
                            break;
                        }
                    }
                }

                if (!answered) {
                    System.out.println("  → Could not answer round " + round + ", stopping.");
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("  → Chatbot not found or error: " + e.getMessage());
        }
    }

    private String getAnswerForQuestion(String question) {
        if (question.contains("salary") || question.contains("ctc") ||
            question.contains("package")) {
            return EXPECTED_SALARY;
        } else if (question.contains("dob") || question.contains("birth")) {
            return DATE_OF_BIRTH;
        } else if (question.contains("experience") || question.contains("exp")) {
            return EXPERIENCE_YEARS;
        } else if (question.contains("notice") || question.contains("joining")) {
            return NOTICE_PERIOD;
        } else if (question.contains("relocat")) {
            return "Yes";
        } else {
            return EXPERIENCE_YEARS;
        }
    }

    private void handleLocationPopup(WebDriver driver) {
        try {
            WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            alertWait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
            System.out.println("  → Browser location alert accepted");
        } catch (Exception ignored) {}

        try {
            List<WebElement> allowBtns = driver.findElements(
                By.xpath("//*[contains(text(),'Allow') or contains(text(),'Yes') " +
                    "or contains(text(),'OK')]"));
            for (WebElement btn : allowBtns) {
                if (btn.isDisplayed()) {
                    btn.click();
                    System.out.println("  → Location popup dismissed");
                    Thread.sleep(1000);
                    break;
                }
            }
        } catch (Exception ignored) {}
    }
}
// ```

// After the run you'll find the Excel file on your **Desktop** named like:
// ```
// naukri_applications_08-03-2026_19-45.xlsx