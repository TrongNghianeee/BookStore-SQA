package com.example.bookstore.controller;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.InventoryTransaction;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookRestController {

    @Autowired
    private BookService bookService;

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Integer id) {
        Book book = bookService.getBookById(id);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(book);
    }

    @GetMapping("/books/by-category/{categoryId}")
    public ResponseEntity<List<Book>> getBooksByCategory(@PathVariable Integer categoryId) {
        List<Book> books = bookService.getBooksByCategory(categoryId);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/inventory/recent-transactions")
    public ResponseEntity<List<InventoryTransaction>> getRecentTransactions() {
        List<InventoryTransaction> transactions = inventoryService.getRecentTransactions(10);
        return ResponseEntity.ok(transactions);
    }
}