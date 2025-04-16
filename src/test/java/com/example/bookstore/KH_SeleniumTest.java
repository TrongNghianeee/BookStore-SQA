package com.example.bookstore;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

public class KH_SeleniumTest {

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
    void testLoginKH2() {

        driver.get("http://localhost:8080/login");
        driver.findElement(By.id("username")).sendKeys("kh2");
        driver.findElement(By.id("password")).sendKeys("kh123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // // Navigate to login page
        // driver.get("http://localhost:8080/login");
    
        // // Enter username and password
        // WebElement usernameInput = driver.findElement(By.id("username"));
        // WebElement passwordInput = driver.findElement(By.id("password"));
        // usernameInput.clear();
        // usernameInput.sendKeys("kh2");
        // passwordInput.clear();
        // passwordInput.sendKeys("kh123");
    
        // // Submit the login form
        // WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        // loginButton.click();
    
        // // Verify successful login
        // assertTrue(driver.getCurrentUrl().contains("/home"), "Login failed or not redirected to home.");
    }
    
    @Test
    @Order(2)
    void testSearchByCategory() {
        // Login as customer
        //testLoginKH2();

        // Navigate to home page
        driver.get("http://localhost:8080/home");

        // Select category "Văn học"
        WebElement categoryDropdown = driver.findElement(By.id("categoryFilter"));
        categoryDropdown.click();
        WebElement categoryOption = driver.findElement(By.xpath("//option[text()='Văn học']"));
        categoryOption.click();

        // Wait for results to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement resultsContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("row")));

        // Verify results
        assertTrue(resultsContainer.isDisplayed(), "Search results are not displayed.");

        // Verify that all results belong to the "Văn học" category
        List<WebElement> categoryBadges = resultsContainer.findElements(By.className("category-badge"));
        for (WebElement badge : categoryBadges) {
            assertTrue(badge.getText().equals("Văn học"), "A book not in the 'Văn học' category was found.");
        }
    }

    @Test
    @Order(3)
    void testSortByPriceAscending() {
        // Login as customer
        //testLoginKH2();
    
        // Navigate to homepage
        driver.get("http://localhost:8080/home");
    
        // Select sorting option: Giá: Thấp đến cao
        WebElement sortDropdown = driver.findElement(By.id("sortFilter"));
        sortDropdown.click();
        WebElement sortOption = driver.findElement(By.xpath("//option[@value='priceAsc']"));
        sortOption.click();
    
        // Wait for results to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement resultsContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("row")));
    
        // Verify results
        assertTrue(resultsContainer.isDisplayed(), "Search results are not displayed.");
    
        // Verify that prices are sorted in ascending order
        List<WebElement> priceElements = resultsContainer.findElements(By.className("book-price"));
        int previousPrice = 0;
        for (WebElement priceElement : priceElements) {
            String priceText = priceElement.getText().replace(",", "").replace(" đ", "");
            int price = Integer.parseInt(priceText);
    
            // Check if the prices are sorted in ascending order
            assertTrue(price >= previousPrice, "Prices are not sorted in ascending order.");
            previousPrice = price;
        }
    }

    @Test
    @Order(4)
    void testAddToCart() {
        // Login as customer
        //testLoginKH2();

        // Navigate to homepage
        driver.get("http://localhost:8080/home");

        // Search for the product "Lão Hạc"
        WebElement searchInput = driver.findElement(By.name("keyword"));
        searchInput.clear();
        searchInput.sendKeys("Lão");
        searchInput.sendKeys(Keys.ENTER); // Press Enter to search

        // Wait for the product to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement productCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("book-card")));

        // Select quantity
        WebElement quantityInput = productCard.findElement(By.cssSelector("input.quantity-input"));
        quantityInput.clear();
        quantityInput.sendKeys("2"); // Set quantity to 2

        // Wait for the "Add to Cart" button to be clickable
        WebElement addToCartButton = productCard.findElement(By.xpath(".//button[contains(@onclick, 'addToCart')]"));

        // Scroll the page to ensure the button is visible
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, arguments[0]);", addToCartButton.getLocation().getY());

        // Ensure the button is visible and enabled
        assertTrue(addToCartButton.isDisplayed(), "Add to Cart button is not visible.");
        assertTrue(addToCartButton.isEnabled(), "Add to Cart button is not enabled.");

        // Retry clicking the "Add to Cart" button if the first attempt fails
        try {
            addToCartButton.click();
        } catch (ElementClickInterceptedException e) {
            // Scroll again and retry clicking
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, arguments[0]);", addToCartButton.getLocation().getY());
            addToCartButton.click();
        }

        // Verify success notification
        WebElement successNotification = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("toast-body")));
        assertTrue(successNotification.getText().contains("Đã thêm"), "Add to cart notification not displayed or incorrect.");
    }


    @Test
    @Order(5)
    void testCheckout() {
        // Login as customer
        //testLoginKH2();
    
        // Navigate to cart page
        driver.get("http://localhost:8080/cart");
    
        // Verify cart is displayed
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement cartTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("cart-item")));
        assertTrue(cartTable.isDisplayed(), "Cart is not displayed.");
    
        // Click the "Thanh toán" button
        WebElement checkoutButton = driver.findElement(By.cssSelector("button.btn-success"));
        checkoutButton.click();
    
        // Wait for the checkout modal to appear
        WebElement checkoutModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkoutModal")));
        assertTrue(checkoutModal.isDisplayed(), "Checkout modal is not displayed.");
    
        // Enter shipping address
        WebElement addressInput = checkoutModal.findElement(By.id("shippingAddress"));
        addressInput.clear();
        addressInput.sendKeys("123 Đường ABC, TP. Hồ Chí Minh");
    
        // Select payment method
        WebElement paymentOption = checkoutModal.findElement(By.xpath("//div[@data-method='Tiền mặt khi nhận hàng']"));
        paymentOption.click();
    
        // Wait for the "Xác nhận đặt hàng" button to be clickable
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(.,'Xác nhận đặt hàng')]")
        ));
    
        // Click the confirm button
        confirmButton.click();
    
        // Verify success notification
        WebElement successNotification = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("toast-body")));
        assertTrue(successNotification.getText().contains("Đặt hàng thành công"), "Checkout failed or success message not displayed.");
    }

    @Test
    @Order(6)
    void testCheckoutWithCardAndAddProduct() {
        // Login as customer
        testLoginKH2();
    
        // Navigate to homepage
        driver.get("http://localhost:8080/home");
    
        // Search for the product "Lược Sử Thời Gian"
        WebElement searchInput = driver.findElement(By.name("keyword"));
        searchInput.clear();
        searchInput.sendKeys("Lược Sử Thời Gian");
        searchInput.sendKeys(Keys.ENTER); // Press Enter to search
    
        // Wait for the product to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement productCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("book-card")));
    
        // Select quantity
        WebElement quantityInput = productCard.findElement(By.cssSelector("input.quantity-input"));
        quantityInput.clear();
        quantityInput.sendKeys("1"); // Set quantity to 1
    
        // Wait for the "Add to Cart" button to be clickable
        WebElement addToCartButton = productCard.findElement(By.xpath(".//button[contains(@onclick, 'addToCart')]"));
    
        // Scroll to the button and click
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addToCartButton);
        addToCartButton.click();
    
        // Verify success notification
        WebElement successNotification = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("toast-body")));
        assertTrue(successNotification.getText().contains("Đã thêm"), "Add to cart notification not displayed or incorrect.");
    
        // Navigate to cart page
        driver.get("http://localhost:8080/cart");
    
        // Verify cart is displayed
        WebElement cartTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("cart-item")));
        assertTrue(cartTable.isDisplayed(), "Cart is not displayed.");
    
        // Click the "Thanh toán" button
        WebElement checkoutButton = driver.findElement(By.cssSelector("button.btn-success"));
        checkoutButton.click();
    
        // Wait for the checkout modal to appear
        WebElement checkoutModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkoutModal")));
        assertTrue(checkoutModal.isDisplayed(), "Checkout modal is not displayed.");
    
        // Enter shipping address
        WebElement addressInput = checkoutModal.findElement(By.id("shippingAddress"));
        addressInput.clear();
        addressInput.sendKeys("123 Đường ABC, TP. Hồ Chí Minh");
    
        // Select payment method: Thẻ ngân hàng
        WebElement paymentOption = checkoutModal.findElement(By.xpath("//div[@data-method='Thẻ ngân hàng']"));
        paymentOption.click();
    
        // Wait for the "Xác nhận đặt hàng" button to be clickable
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(.,'Xác nhận đặt hàng')]")
        ));
    
        // Click the confirm button
        confirmButton.click();
    
        // Verify success notification
        WebElement finalSuccessNotification = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("toast-body")));
        assertTrue(finalSuccessNotification.getText().contains("Đặt hàng thành công"), "Checkout with card failed or success message not displayed.");
    }

}