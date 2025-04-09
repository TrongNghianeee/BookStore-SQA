package com.example.bookstore.repository;

import com.example.bookstore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    long count(); // Đếm tổng số đơn hàng
    long countByOrderDateBetween(LocalDateTime start, LocalDateTime end); // Đếm đơn hàng trong khoảng thời gian
}
