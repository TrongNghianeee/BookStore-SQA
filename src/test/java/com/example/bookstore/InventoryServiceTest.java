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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class InventoryServiceTest {

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private InventoryTransactionRepository transactionRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessTransaction() {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionType(InventoryTransaction.TYPE_IMPORT);
        transaction.setQuantity(10);

        Book book = new Book();
        book.setId(1);
        book.setStockQuantity(5);

        transaction.setBook(book);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        InventoryTransaction processedTransaction = inventoryService.processTransaction(transaction);

        assertNotNull(processedTransaction);
        assertEquals(10, processedTransaction.getQuantity());
        verify(transactionRepository).save(transaction);
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

        // Convert string dates to LocalDateTime
        LocalDateTime startDate = LocalDateTime.parse(fromDate + "T00:00:00");
        LocalDateTime endDate = LocalDateTime.parse(toDate + "T23:59:59");

        when(transactionRepository.findByTransactionTypeAndTransactionDateBetween(type, startDate, endDate, 
            PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "transactionDate"))))
                .thenReturn(new PageImpl<>(transactions));

        Page<InventoryTransaction> resultPage = inventoryService.getTransactionsPaginated(0, 2, type, fromDate, toDate);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getContent().size());
        verify(transactionRepository).findByTransactionTypeAndTransactionDateBetween(type, startDate, endDate, 
            PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "transactionDate")));
    }
}
