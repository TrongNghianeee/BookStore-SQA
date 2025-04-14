package com.example.bookstore.service;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.InventoryTransaction;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.InventoryTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryTransactionRepository transactionRepository;

    @Autowired
    private BookRepository bookRepository;

    @Override
    @Transactional
    public InventoryTransaction processTransaction(InventoryTransaction transaction) {
        // Get the book from repository to ensure we have the latest data
        Book book = bookRepository.findById(transaction.getBook().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy sách với ID: " + transaction.getBook().getId()));

        // Set the book in transaction
        transaction.setBook(book);

        // Process based on transaction type
        switch (transaction.getTransactionType()) {
            case InventoryTransaction.TYPE_IMPORT:
                // Import: add to stock
                book.setStockQuantity(book.getStockQuantity() + transaction.getQuantity());
                break;

            case InventoryTransaction.TYPE_EXPORT:
                // Export: remove from stock
                if (book.getStockQuantity() < transaction.getQuantity()) {
                    throw new IllegalArgumentException(
                            "Không đủ số lượng trong kho. Hiện tại chỉ có: " + book.getStockQuantity());
                }
                book.setStockQuantity(book.getStockQuantity() - transaction.getQuantity());
                break;

            default:
                throw new IllegalArgumentException("Loại giao dịch không hợp lệ: " + transaction.getTransactionType());
        }

        // Save the updated book
        bookRepository.save(book);

        // Save and return the transaction
        return transactionRepository.save(transaction);
    }

    @Override
    public List<InventoryTransaction> getRecentTransactions(int limit) {
        return transactionRepository.findTop10ByOrderByTransactionDateDesc(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "transactionDate"))).getContent();
    }

    @Override
    public long getTotalTransactionsCount() {
        return transactionRepository.count();
    }

    @Override
    public Page<InventoryTransaction> getTransactionsPaginated(int page, int size) {
        return transactionRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate")));
    }

    @Override
    public Page<InventoryTransaction> getTransactionsPaginated(int page, int size, String type, String fromDate,
            String toDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));

        // If no filters are applied, return all transactions
        if ((type == null || type.isEmpty()) && (fromDate == null || fromDate.isEmpty())
                && (toDate == null || toDate.isEmpty())) {
            return transactionRepository.findAll(pageable);
        }

        // Parse date strings to LocalDateTime if provided
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (fromDate != null && !fromDate.isEmpty()) {
            startDate = LocalDateTime.parse(fromDate + "T00:00:00");
        }

        if (toDate != null && !toDate.isEmpty()) {
            endDate = LocalDateTime.parse(toDate + "T23:59:59");
        }

        // Apply filters based on which parameters are provided
        if (type != null && !type.isEmpty()) {
            if (startDate != null && endDate != null) {
                // Filter by both type and date range
                return transactionRepository.findByTransactionTypeAndTransactionDateBetween(type, startDate, endDate,
                        pageable);
            } else {
                // Filter by type only
                return transactionRepository.findByTransactionType(type, pageable);
            }
        } else if (startDate != null && endDate != null) {
            // Filter by date range only
            return transactionRepository.findByTransactionDateBetween(startDate, endDate, pageable);
        } else if (startDate != null) {
            // Filter by start date onwards
            return transactionRepository.findByTransactionDateBetween(startDate, LocalDateTime.now(), pageable);
        } else if (endDate != null) {
            // Filter by end date backwards
            return transactionRepository.findByTransactionDateBetween(LocalDateTime.MIN, endDate, pageable);
        }

        // Should never reach here, but just in case
        return transactionRepository.findAll(pageable);
    }
}