package com.example.bookstore;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Admin_KhoHang_SeleniumTest {

    private static WebDriver driver;

    @BeforeAll
    static void setup() {
        System.setProperty("webdriver.chrome.driver", "D:\\Game\\chromedriver-win64\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @AfterAll
    static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testAdminLogin() {
        driver.get("http://localhost:8080/login");
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        //assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Login failed or not redirected to dashboard.");
    }

    @Test
    @Order(2)
    void testNhapKho_Success() {
        // Login as admin
        //testAdminLogin();
    
        // Navigate to inventory management page
        driver.get("http://localhost:8080/dashboard/inventory");
    
        // Select category
        WebElement categorySelect = driver.findElement(By.id("categoryId"));
        categorySelect.click();
        WebElement categoryOption = categorySelect.findElement(By.xpath("//option[text()='Khoa học']"));
        categoryOption.click();
    
        // Select book
        WebElement bookSelect = driver.findElement(By.id("bookId"));
        bookSelect.click();
        WebElement bookOption = bookSelect.findElement(By.xpath("//option[contains(text(),'Lược Sử Thời Gian')]"));
        bookOption.click();
    
        // Select transaction type: Nhập
        Select transactionTypeSelect = new Select(driver.findElement(By.id("transactionType")));
        transactionTypeSelect.selectByVisibleText("Nhập kho");
    
        // Enter quantity and price
        WebElement quantityInput = driver.findElement(By.id("quantity"));
        WebElement priceInput = driver.findElement(By.id("priceAtTransaction"));
        quantityInput.clear();
        quantityInput.sendKeys("10");
        priceInput.clear();
        priceInput.sendKeys("99000");
    
        // Submit the form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
    
        // Wait for success message to appear
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement alertSuccess = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
    
        // Verify success message
        String successMessage = alertSuccess.getText();
        assertTrue(successMessage.contains("Giao dịch xuất kho đã được lưu thành công"),
                "Success message not displayed or incorrect. Actual message: " + successMessage);
    }

    @Test
    @Order(3)
    void testXuatKho_Success() {
        // Login as admin
        //testAdminLogin();
    
        // Navigate to inventory management page
        driver.get("http://localhost:8080/dashboard/inventory");
    
        // Select category
        WebElement categorySelect = driver.findElement(By.id("categoryId"));
        categorySelect.click();
        WebElement categoryOption = categorySelect.findElement(By.xpath("//option[text()='Khoa học']"));
        categoryOption.click();
    
        // Select book
        WebElement bookSelect = driver.findElement(By.id("bookId"));
        bookSelect.click();
        WebElement bookOption = bookSelect.findElement(By.xpath("//option[contains(text(),'Lược Sử Thời Gian')]"));
        bookOption.click();
    
        // Select transaction type: Xuất
        Select transactionTypeSelect = new Select(driver.findElement(By.id("transactionType")));
        transactionTypeSelect.selectByVisibleText("Xuất kho");
    
        // Enter quantity and price
        WebElement quantityInput = driver.findElement(By.id("quantity"));
        WebElement priceInput = driver.findElement(By.id("priceAtTransaction"));
        quantityInput.clear();
        quantityInput.sendKeys("2");
        priceInput.clear();
        priceInput.sendKeys("130000");
    
        // Submit the form
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
    
        // Verify success message
        WebElement alertSuccess = driver.findElement(By.className("alert-success"));
        assertTrue(alertSuccess.getText().contains("Giao dịch xuất kho đã được lưu thành công"),
                "Success message not displayed or incorrect.");
    }

    @Test
    @Order(4)
    void testXemLichSuNhapKho() {
        // Login as admin
        //testAdminLogin();
    
        // Navigate to transaction history page
        driver.get("http://localhost:8080/dashboard/inventory/transactions");
    
        // Wait for the transaction type filter to be visible
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement transactionTypeFilterElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("type")));
    
        // Select transaction type: Nhập kho
        Select transactionTypeFilter = new Select(transactionTypeFilterElement);
        transactionTypeFilter.selectByVisibleText("Nhập kho");
    
        // Enter date range: from 10/04/2025 to 15/04/2025
        WebElement startDateInput = driver.findElement(By.name("fromDate"));
        WebElement endDateInput = driver.findElement(By.name("toDate"));
        startDateInput.clear();
        startDateInput.sendKeys("04/10/2025");
        endDateInput.clear();
        endDateInput.sendKeys("04/15/2025");
    
        // Submit the filter form
        WebElement filterButton = driver.findElement(By.cssSelector("button[type='submit']"));
        filterButton.click();

        // Wait for the transaction table to be visible
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.table")));

        // Verify the filtered results
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        assertTrue(rows.size() > 1, "Transaction table does not contain any rows.");

        // Verify that all rows match the filter criteria
        for (WebElement row : rows.subList(1, rows.size())) { // Skip the header row
            List<WebElement> cells = row.findElements(By.tagName("td"));
            String transactionType = cells.get(2).getText(); // Assuming "Loại GD" is in the 3rd column

            // Check transaction type
            assertTrue(transactionType.contains("Nhập kho"), "Transaction type does not match filter criteria.");
        }
    }

    @Test
    @Order(5)
    void testXemLichSuXuatKho() {
        // Login as admin
        //testAdminLogin();
    
        // Navigate to transaction history page
        driver.get("http://localhost:8080/dashboard/inventory/transactions");
    
        // Wait for the transaction type filter to be visible
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement transactionTypeFilterElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("type")));
    
        // Select transaction type: Xuất kho
        Select transactionTypeFilter = new Select(transactionTypeFilterElement);
        transactionTypeFilter.selectByVisibleText("Xuất kho");
    
        // Enter date range: from 10/04/2025 to 15/04/2025
        WebElement startDateInput = driver.findElement(By.name("fromDate"));
        WebElement endDateInput = driver.findElement(By.name("toDate"));
        startDateInput.clear();
        startDateInput.sendKeys("04/10/2025");
        endDateInput.clear();
        endDateInput.sendKeys("04/15/2025");
    
        // Submit the filter form
        WebElement filterButton = driver.findElement(By.cssSelector("button[type='submit']"));
        filterButton.click();
    
        // Wait for the transaction table to be visible
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.table")));
        assertTrue(table.isDisplayed(), "Transaction table is not displayed.");
    
        // Verify the filtered results
        List<WebElement> rows = table.findElements(By.tagName("tr"));
        assertTrue(rows.size() > 1, "Transaction table does not contain any rows.");
    
        // Verify that all rows match the filter criteria
        for (WebElement row : rows.subList(1, rows.size())) { // Skip the header row
            List<WebElement> cells = row.findElements(By.tagName("td"));
            String transactionType = cells.get(2).getText(); // Assuming "Loại GD" is in the 3rd column
    
            // Check transaction type
            assertTrue(transactionType.contains("Xuất kho"), "Transaction type does not match filter criteria.");
    
        }
    }
}