package com.example.bookstore.service;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.BookImage;
import com.example.bookstore.model.Category;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.BookImageRepository;
import com.example.bookstore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookImageRepository bookImageRepository;

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

    /**
     * Lưu thông tin loại sách mới
     * 
     * @param category Đối tượng loại sách cần lưu
     * @return Loại sách đã được lưu
     */
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Lưu thông tin sách mới
     * 
     * @param book Đối tượng sách cần lưu
     * @return Sách đã được lưu
     */
    public Book saveBook(Book book) {
        // Lấy thông tin category từ database để đảm bảo nó hợp lệ
        if (book.getCategory() != null && book.getCategory().getId() != null) {
            Category category = categoryRepository.findById(book.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            book.setCategory(category);
        }

        return bookRepository.save(book);
    }

    /**
     * Lấy thông tin sách theo ID
     * 
     * @param id ID của sách cần tìm
     * @return Sách tìm thấy hoặc null nếu không tìm thấy
     */
    public Book getBookById(Integer id) {
        return bookRepository.findById(id).orElse(null);
    }

    /**
     * Lấy thông tin loại sách theo ID
     * 
     * @param id ID của loại sách cần tìm
     * @return Loại sách tìm thấy hoặc null nếu không tìm thấy
     */
    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    /**
     * Cập nhật thông tin sách
     * 
     * @param book Đối tượng sách với thông tin đã cập nhật
     * @return Sách đã được cập nhật
     */
    public Book updateBook(Book book) {
        // Kiểm tra xem sách có tồn tại không
        Book existingBook = bookRepository.findById(book.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách với ID: " + book.getId()));

        // Lấy thông tin category từ database để đảm bảo nó hợp lệ
        if (book.getCategory() != null && book.getCategory().getId() != null) {
            Category category = categoryRepository.findById(book.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy loại sách với ID: " + book.getCategory().getId()));
            book.setCategory(category);
        }

        // Cập nhật thông tin sách
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPublisher(book.getPublisher());
        existingBook.setPublicationYear(book.getPublicationYear());
        existingBook.setCategory(book.getCategory());
        existingBook.setPrice(book.getPrice());
        existingBook.setStockQuantity(book.getStockQuantity());

        return bookRepository.save(existingBook);
    }

    /**
     * Lấy danh sách hình ảnh của sách
     * 
     * @param bookId ID của sách cần lấy ảnh
     * @return Danh sách các hình ảnh của sách
     */
    public List<BookImage> getBookImages(Integer bookId) {
        try {
            return bookImageRepository.findByBookIdOrderByIsPrimaryDesc(bookId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Lấy danh sách sách theo thể loại
     * 
     * @param categoryId ID của thể loại
     * @return Danh sách các sách thuộc thể loại đó
     */
    public List<Book> getBooksByCategory(Integer categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }
}