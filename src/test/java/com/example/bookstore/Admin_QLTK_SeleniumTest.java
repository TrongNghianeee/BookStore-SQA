package com.example.bookstore;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.support.ui.Select;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Admin_QLTK_SeleniumTest {

    private static WebDriver driver;

    // Biến dùng chung cho user test
    private final String testUsername = "nhan16";
    private final String testPassword = "nhan123";
    private final String testEmail = "nhan16@gmail.com";
    private final String testPhone = "0343699860";
    private final String testAddress = "Sài Gòn";
    private final String updatedAddress = "Đồng Tháp";

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

    @Test @Order(1)
    void testAdminLogin() {
        driver.get("http://localhost:8080/login");
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("admin123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        //Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"));
    }

    @Test @Order(2)
    void testAdminAddUserAccount() {
        driver.get("http://localhost:8080/dashboard/users/add");
        driver.findElement(By.id("username")).sendKeys(testUsername);
        driver.findElement(By.id("password")).sendKeys(testPassword);
        driver.findElement(By.id("email")).sendKeys(testEmail);
        driver.findElement(By.id("phone")).sendKeys(testPhone);
        driver.findElement(By.id("address")).sendKeys(testAddress);
        Select roleSelect = new Select(driver.findElement(By.id("role")));
        roleSelect.selectByVisibleText("Nhân viên");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard/users"));
    }

    @Test @Order(3)
    void testAdminSearchUserByName() {
        boolean found = false;
        int page = 1;
        while (true) {
            driver.get("http://localhost:8080/dashboard/users?page=" + page + "&keyword=" + testUsername);
            if (driver.getPageSource().contains(testUsername)) {
                found = true;
                break;
            }
            // Kiểm tra nếu không còn nút phân trang "next" thì dừng
            List<WebElement> nextButtons = driver.findElements(By.cssSelector(".pagination li:last-child:not(.disabled) a"));
            if (nextButtons.isEmpty()) {
                break;
            }
            page++;
        }
        Assertions.assertTrue(found, "Không tìm thấy tài khoản " + testUsername + " trong tất cả các trang.");
    }

    @Test @Order(4)
    void testAdminSearchUserByRole() {
        boolean found = false;
        int page = 1;
        while (true) {
            driver.get("http://localhost:8080/dashboard/users?page=" + page + "&role=Nvien");
            if (driver.getPageSource().contains(testUsername)) {
                found = true;
                break;
            }
            List<WebElement> nextButtons = driver.findElements(By.cssSelector(".pagination li:last-child:not(.disabled) a"));
            if (nextButtons.isEmpty()) {
                break;
            }
            page++;
        }
        Assertions.assertTrue(found, "Không tìm thấy tài khoản " + testUsername + " theo vai trò 'Nv' trong tất cả các trang.");
    }

    @Test @Order(5)
    void testAdminSearchUserByStatus() {
        boolean found = false;
        int page = 1;
        while (true) {
            driver.get("http://localhost:8080/dashboard/users?page=" + page + "&status=Active");
            if (driver.getPageSource().contains(testUsername)) {
                found = true;
                break;
            }
            List<WebElement> nextButtons = driver.findElements(By.cssSelector(".pagination li:last-child:not(.disabled) a"));
            if (nextButtons.isEmpty()) {
                break;
            }
            page++;
        }
        Assertions.assertTrue(found, "Không tìm thấy tài khoản " + testUsername + " có trạng thái Active.");
    }

    @Test @Order(6)
    void testAdminLockUnlockUserAccount() throws InterruptedException {
        driver.get("http://localhost:8080/dashboard/users?keyword=" + testUsername);
        // Lặp lại thao tác 2 lần để kiểm tra khoá - mở khoá
        for (int i = 0; i < 2; i++) {
            List<WebElement> buttons = driver.findElements(By.cssSelector("button[title='Khóa tài khoản']"));
            String action = "Khóa";

            if (buttons.isEmpty()) {
                buttons = driver.findElements(By.cssSelector("button[title='Mở khóa tài khoản']"));
                action = "Mở khóa";
            }

            Assertions.assertFalse(buttons.isEmpty(), "Không tìm thấy nút " + action);
            buttons.get(0).click();

            try {
                Alert alert = driver.switchTo().alert();
                alert.accept();
            } catch (NoAlertPresentException ignored) {}

            Thread.sleep(1000);
            driver.navigate().refresh();
        }
        Assertions.assertTrue(driver.getPageSource().contains(testUsername));
    }

    @Test @Order(7)
    void testAdminViewUserDetails() {
        driver.get("http://localhost:8080/dashboard/users?keyword=" + testUsername);
        Assertions.assertTrue(driver.getPageSource().contains("ID"));
        Assertions.assertTrue(driver.getPageSource().contains("Username"));
        Assertions.assertTrue(driver.getPageSource().contains("Quyền hạn"));
        Assertions.assertTrue(driver.getPageSource().contains("Email"));
        Assertions.assertTrue(driver.getPageSource().contains("Số điện thoại"));
        Assertions.assertTrue(driver.getPageSource().contains("Địa chỉ"));
        Assertions.assertTrue(driver.getPageSource().contains("Trạng thái"));
        Assertions.assertTrue(driver.getPageSource().contains("Ngày tạo"));
    }

    @Test @Order(8)
    void testAdminEditUserAddress() throws InterruptedException {
        driver.get("http://localhost:8080/dashboard/users?keyword=" + testUsername);
        WebElement editBtn = driver.findElement(By.cssSelector("button[title='Chỉnh sửa']"));
        editBtn.click();
        Thread.sleep(500);
        WebElement modal = driver.findElement(By.id("editUserModal"));
        WebElement addressField = modal.findElement(By.id("editAddress"));
        addressField.clear();
        addressField.sendKeys(updatedAddress);
        WebElement saveBtn = modal.findElement(By.cssSelector("button.btn.btn-primary"));
        saveBtn.click();
        Thread.sleep(1000);
        driver.navigate().refresh();

    }
}
