package com.example.bookstore;

import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.DashboardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetOverviewStats() {
        // Mocking repository methods
        when(bookRepository.count()).thenReturn(10L);
        when(categoryRepository.count()).thenReturn(5L);
        when(userRepository.count()).thenReturn(20L);
        when(orderRepository.count()).thenReturn(50L);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfThisMonth.minusSeconds(1);

        // Mocking count for orders within date range
        when(orderRepository.countByOrderDateBetween(startOfThisMonth, now)).thenReturn(120L);
        when(orderRepository.countByOrderDateBetween(startOfLastMonth, endOfLastMonth)).thenReturn(100L);

            // Calling the method to test

        Map<String, Object> stats = dashboardService.getOverviewStats();

        // Verifying the stats map
        assertNotNull(stats);
        assertEquals(10L, stats.get("totalBooks"));
        assertEquals(5L, stats.get("totalCategories"));
        assertEquals(20L, stats.get("totalCustomers"));
        assertEquals(50L, stats.get("totalOrders"));
       
        assertEquals(20.0, (double) stats.get("orderPercentageChange"), 0.01);
        assertEquals(12.0, (double) stats.get("bookPercentageChange"), 0.01);
        assertEquals(5.0, (double) stats.get("categoryPercentageChange"), 0.01);
        assertEquals(8.0, (double) stats.get("customerPercentageChange"), 0.01);

        double orderPercentageChange = (120L - 100L) / 100.0 * 100; // (120-100)/100 * 100 = 20%
        assertEquals(20.0, orderPercentageChange); // Compare with 20.0 (as a double)
        assertEquals(12.0, stats.get("bookPercentageChange")); // Mocked value
        assertEquals(5.0, stats.get("categoryPercentageChange")); // Mocked value
        assertEquals(8.0, stats.get("customerPercentageChange")); // Mocked value
    }

    @Test
    void testOrderPercentageChangeWhenLastMonthZero() {
        // When last month's orders are 0, the percentage change should be 0.
        when(orderRepository.countByOrderDateBetween(any(), any())).thenReturn(0L);
        when(orderRepository.countByOrderDateBetween(any(), any())).thenReturn(0L); // last month is zero

        // Calling the method to test
        Map<String, Object> stats = dashboardService.getOverviewStats();

        // Verifying the order percentage change
        assertEquals(0.0, stats.get("orderPercentageChange"));
    }
}
