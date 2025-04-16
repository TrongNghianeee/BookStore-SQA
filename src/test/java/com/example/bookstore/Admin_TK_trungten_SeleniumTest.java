package com.example.bookstore;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Admin_TK_trungten_SeleniumTest {

    private static WebDriver driver;
    private static final String BASE_URL = "http://localhost:8080";
    private static final String EXISTING_USERNAME = "admin"; // Tên tài khoản đã tồn tại

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
        //Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"));
    }

    @Test
    @Order(2)
    void testAddDuplicateUserAccount() {
        // Navigate to the "Add User" page
        driver.get(BASE_URL + "/dashboard/users/add");

        // Fill in the form with an existing username
        driver.findElement(By.id("username")).sendKeys(EXISTING_USERNAME); // Tên đã tồn tại
        driver.findElement(By.id("password")).sendKeys("duplicate123");
        driver.findElement(By.id("email")).sendKeys("duplicate@gmail.com");
        driver.findElement(By.id("phone")).sendKeys("0123456789");
        driver.findElement(By.id("address")).sendKeys("Hà Nội");
        Select roleSelect = new Select(driver.findElement(By.id("role")));
        roleSelect.selectByVisibleText("Nhân viên");

        // Submit the form
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Verify error message is displayed
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Tên tài khoản đã tồn tại"), "Error message for duplicate username not displayed.");
    }
}