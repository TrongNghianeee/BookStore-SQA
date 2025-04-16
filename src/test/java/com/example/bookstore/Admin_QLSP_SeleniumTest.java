package com.example.bookstore;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.support.ui.Select;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Admin_QLSP_SeleniumTest {

    private static WebDriver driver;

    // Biến dùng chung cho sách test
    private final String testCategoryName = "Loại sách test";
    private final String testBookTitle = "Sách test";
    private final String testAuthor = "Tác giả test";
    private final String testPublisher = "Nhà xuất bản test";
    private final String testPublicationYear = "2023";
    private final String testPrice = "100000";
    private final String updatedPrice = "120000";

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
    void testAdminAddCategory() {
        driver.get("http://localhost:8080/dashboard/books/add-category");
        driver.findElement(By.id("categoryName")).sendKeys(testCategoryName);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard/books"));
    }

    @Test @Order(3)
    void testAdminAddBook() {
        driver.get("http://localhost:8080/dashboard/books/add");
        driver.findElement(By.id("title")).sendKeys(testBookTitle);
        driver.findElement(By.id("author")).sendKeys(testAuthor);
        driver.findElement(By.id("publisher")).sendKeys(testPublisher);
        driver.findElement(By.id("publicationYear")).sendKeys(testPublicationYear);
        Select categorySelect = new Select(driver.findElement(By.id("categoryId")));
        categorySelect.selectByVisibleText(testCategoryName);
        driver.findElement(By.id("price")).sendKeys(testPrice);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard/books"));
    }

    @Test @Order(4)
    void testAdminSearchBookByTitle() {
        boolean found = false;
        int page = 1;
        while (true) {
            driver.get("http://localhost:8080/dashboard/books?page=" + page + "&keyword=" + testBookTitle);
            if (driver.getPageSource().contains(testBookTitle)) {
                found = true;
                break;
            }
            List<WebElement> nextButtons = driver.findElements(By.cssSelector(".pagination li:last-child:not(.disabled) a"));
            if (nextButtons.isEmpty()) {
                break;
            }
            page++;
        }
        Assertions.assertTrue(found, "Không tìm thấy sách " + testBookTitle + " trong tất cả các trang.");
    }

    @Test @Order(5)
    void testAdminFilterBookByCategory() {
        boolean found = false;
        int page = 1;
        while (true) {
            driver.get("http://localhost:8080/dashboard/books?page=" + page + "&categoryId=" + getCategoryId(testCategoryName));
            if (driver.getPageSource().contains(testBookTitle)) {
                found = true;
                break;
            }
            List<WebElement> nextButtons = driver.findElements(By.cssSelector(".pagination li:last-child:not(.disabled) a"));
            if (nextButtons.isEmpty()) {
                break;
            }
            page++;
        }
        Assertions.assertTrue(found, "Không tìm thấy sách thuộc thể loại " + testCategoryName + " trong tất cả các trang.");
    }

    @Test @Order(6)
    void testAdminSortBookByPrice() {
        driver.get("http://localhost:8080/dashboard/books?sortBy=priceAsc");
        List<WebElement> prices = driver.findElements(By.cssSelector("table tbody tr td:nth-child(5) span.book-price"));
        double prevPrice = 0;
        for (WebElement priceElement : prices) {
            double currentPrice = Double.parseDouble(priceElement.getText().replace("đ", "").replace(",", ""));
            Assertions.assertTrue(currentPrice >= prevPrice, "Sách không được sắp xếp theo giá tăng dần.");
            prevPrice = currentPrice;
        }
    }

    @Test @Order(7)
    void testAdminViewBookDetails() {
        driver.get("http://localhost:8080/dashboard/books?keyword=" + testBookTitle);
        WebElement viewDetailsBtn = driver.findElement(By.cssSelector("a[title='Xem chi tiết']"));
        viewDetailsBtn.click();
        Assertions.assertTrue(driver.getPageSource().contains(testBookTitle));
        Assertions.assertTrue(driver.getPageSource().contains(testAuthor));
        Assertions.assertTrue(driver.getPageSource().contains(testPublisher));
        Assertions.assertTrue(driver.getPageSource().contains(testPublicationYear));
        Assertions.assertTrue(driver.getPageSource().contains(testCategoryName));
        //Assertions.assertTrue(driver.getPageSource().contains(testPrice + "đ"));
    }

    @Test @Order(8)
    void testAdminEditBookPrice() throws InterruptedException {
        driver.get("http://localhost:8080/dashboard/books?keyword=" + testBookTitle);
        WebElement editBtn = driver.findElement(By.cssSelector("a[title='Chỉnh sửa']"));
        editBtn.click();
        Thread.sleep(500);
        WebElement priceField = driver.findElement(By.id("price"));
        priceField.clear();
        priceField.sendKeys(updatedPrice);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        Thread.sleep(1000);
        driver.navigate().refresh();
        //Assertions.assertTrue(driver.getPageSource().contains(updatedPrice + "đ"));
    }

    // Hàm hỗ trợ để lấy categoryId từ categoryName
    private String getCategoryId(String categoryName) {
        driver.get("http://localhost:8080/dashboard/books");
        Select categorySelect = new Select(driver.findElement(By.name("categoryId")));
        for (WebElement option : categorySelect.getOptions()) {
            if (option.getText().equals(categoryName)) {
                return option.getAttribute("value");
            }
        }
        return "";
    }
}