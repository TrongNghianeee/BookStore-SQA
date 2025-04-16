package com.example.bookstore;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.BookImage;
import com.example.bookstore.model.Category;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.BookImageRepository;
import com.example.bookstore.repository.CategoryRepository;
import com.example.bookstore.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookImageRepository bookImageRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetBooks() {
        Page<Book> mockPage = new PageImpl<>(List.of(new Book()));
        when(bookRepository.findBooksWithFilters(anyString(), any(), anyString(), any()))
                .thenReturn(mockPage);

        Page<Book> result = bookService.getBooks("keyword", 1, "in-stock", "priceAsc", 1, 5);
        assertNotNull(result);
        verify(bookRepository, times(1)).findBooksWithFilters(anyString(), any(), anyString(), any());
    }

    @Test
    void testGetAllCategories() {
        List<Category> categories = List.of(new Category());
        when(categoryRepository.findAll()).thenReturn(categories);

        List<Category> result = bookService.getAllCategories();
        assertEquals(1, result.size());
        verify(categoryRepository).findAll();
    }

    @Test
    void testSaveBook() {
        Book book = new Book();
        Category category = new Category();
        category.setId(1);
        book.setCategory(category);

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(bookRepository.save(book)).thenReturn(book);

        Book saved = bookService.saveBook(book);
        assertNotNull(saved);
        verify(bookRepository).save(book);
    }

    @Test
    void testGetBookById() {
        Book book = new Book();
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        Book result = bookService.getBookById(1);
        assertNotNull(result);
    }

    @Test
    void testUpdateBook() {
        Book book = new Book();
        book.setId(1);
        book.setTitle("Updated Title");

        Book existing = new Book();
        existing.setId(1);

        Category category = new Category();
        category.setId(2);
        book.setCategory(category);

        when(bookRepository.findById(1)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenReturn(existing);

        Book result = bookService.updateBook(book);
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void testGetBookImages() {
        List<BookImage> images = List.of(new BookImage());
        when(bookImageRepository.findByBookIdOrderByIsPrimaryDesc(1)).thenReturn(images);

        List<BookImage> result = bookService.getBookImages(1);
        assertEquals(1, result.size());
    }

    @Test
    void testGetBooksByCategory() {
        List<Book> books = List.of(new Book());
        when(bookRepository.findByCategoryId(1)).thenReturn(books);

        List<Book> result = bookService.getBooksByCategory(1);
        assertEquals(1, result.size());
    }
}

