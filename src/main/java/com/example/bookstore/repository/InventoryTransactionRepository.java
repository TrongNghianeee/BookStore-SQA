package com.example.bookstore.repository;

import com.example.bookstore.model.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {

    /**
     * Find the most recent transactions
     * 
     * @param pageable Pagination information including sorting
     * @return A page of the most recent transactions
     */
    Page<InventoryTransaction> findTop10ByOrderByTransactionDateDesc(Pageable pageable);

    /**
     * Find transactions by type
     * 
     * @param type     Transaction type (Nhập/Xuất)
     * @param pageable Pagination information
     * @return A page of transactions matching the type
     */
    Page<InventoryTransaction> findByTransactionType(String type, Pageable pageable);

    /**
     * Find transactions by date range
     * 
     * @param startDate Start date for filtering
     * @param endDate   End date for filtering
     * @param pageable  Pagination information
     * @return A page of transactions within the date range
     */
    Page<InventoryTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find transactions by type and date range
     * 
     * @param type      Transaction type (Nhập/Xuất)
     * @param startDate Start date for filtering
     * @param endDate   End date for filtering
     * @param pageable  Pagination information
     * @return A page of transactions matching both criteria
     */
    Page<InventoryTransaction> findByTransactionTypeAndTransactionDateBetween(
            String type, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}