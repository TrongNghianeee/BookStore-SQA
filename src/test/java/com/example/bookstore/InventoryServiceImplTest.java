package com.example.bookstore;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.InventoryTransaction;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.InventoryTransactionRepository;
import com.example.bookstore.service.InventoryServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InventoryServiceImplTest {

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Mock
    private InventoryTransactionRepository transactionRepository;

    @Mock
    private BookRepository bookRepository;

    private Book book;
    private InventoryTransaction transaction;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock book data
        book = new Book();
        book.setId(1);
        book.setTitle("Tắt Đèn");
        book.setStockQuantity(10);

        // Mock transaction data
        transaction = new InventoryTransaction();
        transaction.setBook(book);
        transaction.setQuantity(5);
        transaction.setTransactionType(InventoryTransaction.TYPE_IMPORT);
    }

    @Test
    void testProcessTransactionImport() {
        when(bookRepository.findById(1)).thenReturn(java.util.Optional.of(book));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // Process import transaction
        InventoryTransaction result = inventoryService.processTransaction(transaction);

        assertNotNull(result);
        assertEquals(15, book.getStockQuantity()); // 10 + 5 = 15
        verify(bookRepository).save(book);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void testProcessTransactionExport() {
        when(bookRepository.findById(1)).thenReturn(java.util.Optional.of(book));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        // Process export transaction
        transaction.setTransactionType(InventoryTransaction.TYPE_EXPORT);
        InventoryTransaction result = inventoryService.processTransaction(transaction);

        assertNotNull(result);
        assertEquals(5, book.getStockQuantity()); // 10 - 5 = 5
        verify(bookRepository).save(book);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void testProcessTransactionExportNotEnoughStock() {
        when(bookRepository.findById(1)).thenReturn(java.util.Optional.of(book));
        transaction.setTransactionType(InventoryTransaction.TYPE_EXPORT);
        transaction.setQuantity(15); // More than available stock

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.processTransaction(transaction);
        });

        assertEquals("Không đủ số lượng trong kho. Hiện tại chỉ có: 10", exception.getMessage());
    }

    @Test
    void testGetRecentTransactions() {
        InventoryTransaction transaction1 = new InventoryTransaction();
        transaction1.setTransactionType(InventoryTransaction.TYPE_IMPORT);
        transaction1.setQuantity(10);

        InventoryTransaction transaction2 = new InventoryTransaction();
        transaction2.setTransactionType(InventoryTransaction.TYPE_EXPORT);
        transaction2.setQuantity(5);

        List<InventoryTransaction> transactions = List.of(transaction1, transaction2);
        when(transactionRepository.findTop10ByOrderByTransactionDateDesc(PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "transactionDate"))))
                .thenReturn(new PageImpl<>(transactions));

        List<InventoryTransaction> recentTransactions = inventoryService.getRecentTransactions(2);

        assertEquals(2, recentTransactions.size());
        assertEquals(10, recentTransactions.get(0).getQuantity());
        verify(transactionRepository).findTop10ByOrderByTransactionDateDesc(PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "transactionDate")));
    }

    @Test
    void testGetTotalTransactionsCount() {
        when(transactionRepository.count()).thenReturn(100L);

        long totalCount = inventoryService.getTotalTransactionsCount();

        assertEquals(100L, totalCount);
        verify(transactionRepository).count();
    }

    @Test
    void testGetTransactionsPaginated() {
        InventoryTransaction transaction1 = new InventoryTransaction();
        transaction1.setTransactionType(InventoryTransaction.TYPE_IMPORT);
        transaction1.setQuantity(10);

        InventoryTransaction transaction2 = new InventoryTransaction();
        transaction2.setTransactionType(InventoryTransaction.TYPE_EXPORT);
        transaction2.setQuantity(5);

        List<InventoryTransaction> transactions = List.of(transaction1, transaction2);
        Page<InventoryTransaction> transactionPage = new PageImpl<>(transactions);

        when(transactionRepository.findAll(PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "transactionDate")))).thenReturn(transactionPage);

        Page<InventoryTransaction> resultPage = inventoryService.getTransactionsPaginated(0, 2);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());
        verify(transactionRepository).findAll(PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "transactionDate")));
    }

    @Test
    void testGetTransactionsPaginatedWithFilters() {
        InventoryTransaction transaction1 = new InventoryTransaction();
        transaction1.setTransactionType(InventoryTransaction.TYPE_IMPORT);
        transaction1.setQuantity(10);

        InventoryTransaction transaction2 = new InventoryTransaction();
        transaction2.setTransactionType(InventoryTransaction.TYPE_EXPORT);
        transaction2.setQuantity(5);

        List<InventoryTransaction> transactions = List.of(transaction1, transaction2);
        Page<InventoryTransaction> transactionPage = new PageImpl<>(transactions);

        String type = "Nhập";
        String fromDate = "2025-03-01";
        String toDate = "2025-05-31";

        LocalDateTime startDate = LocalDateTime.parse(fromDate + "T00:00:00");
        LocalDateTime endDate = LocalDateTime.parse(toDate + "T23:59:59");

        when(transactionRepository.findByTransactionTypeAndTransactionDateBetween(type, startDate, endDate, PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "transactionDate"))))
                .thenReturn(transactionPage);

        Page<InventoryTransaction> resultPage = inventoryService.getTransactionsPaginated(0, 2, type, fromDate, toDate);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());
        verify(transactionRepository).findByTransactionTypeAndTransactionDateBetween(type, startDate, endDate, PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "transactionDate")));
    }
}
