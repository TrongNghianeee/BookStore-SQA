package com.example.bookstore.repository;

import com.example.bookstore.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    long count();

    @Query("SELECT b FROM Book b WHERE " +
            "(:keyword IS NULL OR b.title LIKE %:keyword%) " +
            "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
            "AND (:stockStatus IS NULL OR " +
            "(:stockStatus = 'InStock' AND b.stockQuantity > 0) OR " +
            "(:stockStatus = 'OutOfStock' AND b.stockQuantity = 0))")
    List<Book> searchAndFilterBooks(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("stockStatus") String stockStatus);

    @Query("SELECT b FROM Book b LEFT JOIN b.category c " +
            "WHERE (:keyword = '' OR " +
            "LOWER(b.title) LIKE %:keyword% OR " +
            "LOWER(b.author) LIKE %:keyword%) " +
            "AND (:categoryId IS NULL OR c.id = :categoryId) " +
            "AND (:stockStatus = '' OR " +
            "(:stockStatus = 'InStock' AND b.stockQuantity > 0) OR " +
            "(:stockStatus = 'OutOfStock' AND b.stockQuantity = 0))")
    Page<Book> findBooksWithFilters(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("stockStatus") String stockStatus,
            Pageable pageable);

    @Query("SELECT bi.imageUrl FROM BookImage bi WHERE bi.book.id = :bookId AND bi.isPrimary = true")
    String findPrimaryImageUrlByBookId(@Param("bookId") Integer bookId);
}