package com.example.bookstore;

import com.example.bookstore.model.*;
import com.example.bookstore.repository.*;
import com.example.bookstore.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private BookService bookService;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private PaymentRepository paymentRepository;

    private Book book;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        book = new Book();
        book.setId(1);
        book.setPrice(100.0);
        book.setStockQuantity(10);

        user = new User();
        user.setId(1);
    }

    @Test
    void testAddToCart_NewItem() {
        when(bookService.getBookById(1)).thenReturn(book);
        when(bookService.getPrimaryImageUrl(1)).thenReturn("/image.jpg");

        CartItem item = cartService.addToCart(1, 1, 2);

        assertNotNull(item);
        assertEquals(2, item.getQuantity());
        assertEquals(1, item.getBook().getId());
    }

    @Test
    void testAddToCart_ExistingItem_UpdatedQuantity() {
        when(bookService.getBookById(1)).thenReturn(book);
        when(bookService.getPrimaryImageUrl(1)).thenReturn("/image.jpg");

        cartService.addToCart(1, 1, 2);
        CartItem updated = cartService.addToCart(1, 1, 3);

        assertEquals(5, updated.getQuantity());
    }

    @Test
    void testUpdateCartItemQuantity() {
        when(bookService.getBookById(1)).thenReturn(book);
        cartService.addToCart(1, 1, 2);

        CartItem updated = cartService.updateCartItemQuantity(1, 1, 5);

        assertNotNull(updated);
        assertEquals(5, updated.getQuantity());
    }

    @Test
    void testRemoveFromCart() {
        when(bookService.getBookById(1)).thenReturn(book);
        cartService.addToCart(1, 1, 2);
        cartService.removeFromCart(1, 1);

        assertEquals(0, cartService.getCartItems(1).size());
    }

    @Test
    void testClearCart() {
        when(bookService.getBookById(1)).thenReturn(book);
        cartService.addToCart(1, 1, 2);

        cartService.clearCart(1);

        assertEquals(0, cartService.getCartItems(1).size());
    }

    @Test
    void testGetCartItemCount() {
        when(bookService.getBookById(1)).thenReturn(book);
        cartService.addToCart(1, 1, 2);

        assertEquals(2, cartService.getCartItemCount(1));
    }

    @Test
    void testGetTotalPrice() {
        when(bookService.getBookById(1)).thenReturn(book);
        cartService.addToCart(1, 1, 3);

        assertEquals(300, cartService.getTotalPrice(1));
    }

    @Test
    void testCheckout() {
        int userId = 1; int bookId = 1;
        CartItem item = new CartItem(book, 2);
        book.setPrice(200.0);
        Map<Integer, CartItem> cart = new HashMap<>();
        cart.put(1, item);
        User user = new User();
        user.setId(userId);

        // 1. Khởi tạo book & user
        book.setId(bookId);
        book.setPrice(200.0);
        book.setStockQuantity(10);

        user.setId(userId);

        // 2. Mock các hành vi
        when(bookService.getBookById(bookId)).thenReturn(book);
        when(bookService.getPrimaryImageUrl(bookId)).thenReturn("/image.jpg");
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        when(orderDetailRepository.saveAll(any())).thenReturn(Collections.emptyList());
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // 3. Thêm vào giỏ hàng
        cartService.addToCart(userId, bookId, 2);

        // 4. Thực hiện checkout
        boolean result = cartService.checkout(userId, "Hanoi", "COD");

        // 5. Kiểm tra kết quả
        assertTrue(result);
        assertEquals(0, cartService.getCartItemCount(userId)); // Giỏ hàng đã được clear

    }
}
