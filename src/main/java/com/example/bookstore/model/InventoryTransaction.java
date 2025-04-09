package com.example.bookstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Inventory_Transactions")
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "price_at_transaction")
    private BigDecimal priceAtTransaction;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}