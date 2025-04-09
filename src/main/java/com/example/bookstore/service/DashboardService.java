package com.example.bookstore.service;

import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    public Map<String, Object> getOverviewStats() {
        Map<String, Object> stats = new HashMap<>();

        // Tổng số sách
        long totalBooks = bookRepository.count();
        stats.put("totalBooks", totalBooks);

        // Tổng loại sách
        long totalCategories = categoryRepository.count();
        stats.put("totalCategories", totalCategories);

        // Tổng số khách hàng (sử dụng UserRepository)
        long totalCustomers = userRepository.count();
        stats.put("totalCustomers", totalCustomers);

        // Tổng số đơn hàng
        long totalOrders = orderRepository.count();
        stats.put("totalOrders", totalOrders);

        // Tính phần trăm thay đổi (so với tháng trước)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfThisMonth.minusSeconds(1);

        // Tính phần trăm thay đổi cho đơn hàng
        long ordersThisMonth = orderRepository.countByOrderDateBetween(startOfThisMonth, now);
        long ordersLastMonth = orderRepository.countByOrderDateBetween(startOfLastMonth, endOfLastMonth);
        double orderPercentageChange = ordersLastMonth > 0 ?
                ((double) (ordersThisMonth - ordersLastMonth) / ordersLastMonth) * 100 : 0;
        stats.put("orderPercentageChange", orderPercentageChange);

        // Tạm thời gán giá trị phần trăm cho các mục khác (có thể thêm logic tương tự nếu cần)
        stats.put("bookPercentageChange", 12.0); // Giả lập 12%
        stats.put("categoryPercentageChange", 5.0); // Giả lập 5%
        stats.put("customerPercentageChange", 8.0); // Giả lập 8%

        return stats;
    }
}