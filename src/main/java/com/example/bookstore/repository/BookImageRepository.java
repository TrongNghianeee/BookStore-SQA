package com.example.bookstore.repository;

import com.example.bookstore.model.BookImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookImageRepository extends JpaRepository<BookImage, Integer> {

    /**
     * Tìm tất cả các ảnh của một cuốn sách, sắp xếp sao cho ảnh chính ở đầu tiên
     * 
     * @param bookId ID của cuốn sách
     * @return Danh sách các ảnh của cuốn sách
     */
    List<BookImage> findByBookIdOrderByIsPrimaryDesc(Integer bookId);
}