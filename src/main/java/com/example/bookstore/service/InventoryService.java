package com.example.bookstore.service;

import com.example.bookstore.model.InventoryTransaction;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    /**
     * Process a new inventory transaction (import or export)
     * 
     * @param transaction The transaction to process
     * @return The processed transaction
     * @throws IllegalArgumentException If transaction is invalid (e.g., trying to
     *                                  export more than available stock)
     */
    InventoryTransaction processTransaction(InventoryTransaction transaction);

    /**
     * Get recent transactions
     * 
     * @param limit The maximum number of transactions to return
     * @return A list of the most recent transactions
     */
    List<InventoryTransaction> getRecentTransactions(int limit);

    /**
     * Get total number of transactions
     * 
     * @return Total count of all transactions
     */
    long getTotalTransactionsCount();

    /**
     * Get paginated transactions
     * 
     * @param page The page number (0-based)
     * @param size The page size
     * @return A page of transactions
     */
    Page<InventoryTransaction> getTransactionsPaginated(int page, int size);

    /**
     * Get paginated transactions with filtering
     * 
     * @param page     The page number (0-based)
     * @param size     The page size
     * @param type     The transaction type filter (Nhập/Xuất or null for all)
     * @param fromDate Start date for filtering (or null)
     * @param toDate   End date for filtering (or null)
     * @return A page of transactions matching the filters
     */
    Page<InventoryTransaction> getTransactionsPaginated(int page, int size, String type, String fromDate,
            String toDate);
}