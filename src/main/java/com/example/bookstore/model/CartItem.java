package com.example.bookstore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Integer id;
    private Book book;
    private Integer quantity;
    private Double totalPrice;

    public CartItem(Book book, Integer quantity) {
        this.id = book.getId();
        this.book = book;
        this.quantity = quantity;
        this.totalPrice = book.getPrice() * quantity;
    }

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
        this.totalPrice = this.book.getPrice() * quantity;
    }
}