package com.example.bookstore.service;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.Category;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public Page<Book> getBooks(String keyword, Integer categoryId, String stockStatus, String sortBy, int page,
            int size) {
        Sort sort = Sort.unsorted();
        if ("priceAsc".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("priceDesc".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        }

        Pageable pageable = PageRequest.of(page - 1, size, sort);

        if (keyword == null)
            keyword = "";
        if (stockStatus == null)
            stockStatus = "";

        return bookRepository.findBooksWithFilters(
                keyword.toLowerCase(),
                categoryId,
                stockStatus,
                pageable);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public String getPrimaryImageUrl(Integer bookId) {
        String imageUrl = bookRepository.findPrimaryImageUrlByBookId(bookId);
        return imageUrl != null ? imageUrl : "/images/default.png";
    }
}