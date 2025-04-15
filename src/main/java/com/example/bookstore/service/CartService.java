package com.example.bookstore.service;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.CartItem;
import com.example.bookstore.model.InventoryTransaction;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderDetail;
import com.example.bookstore.model.Payment;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.OrderDetailRepository;
import com.example.bookstore.repository.OrderRepository;
import com.example.bookstore.repository.PaymentRepository;
import com.example.bookstore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    @Autowired
    private BookService bookService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // Store cart items per user (userId -> map of bookId -> CartItem)
    private Map<Integer, Map<Integer, CartItem>> userCarts = new HashMap<>();

    /**
     * Add an item to the user's cart
     * 
     * @param userId   The ID of the user
     * @param bookId   The ID of the book to add
     * @param quantity The quantity to add
     * @return The updated cart item
     */
    public CartItem addToCart(Integer userId, Integer bookId, Integer quantity) {
        Book book = bookService.getBookById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Không tìm thấy sách với ID: " + bookId);
        }

        // Check if quantity is valid
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        // Check if there's enough stock
        if (book.getStockQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Không đủ số lượng trong kho. Hiện chỉ còn " + book.getStockQuantity() + " cuốn.");
        }

        // Get the user's cart or create a new one if it doesn't exist
        Map<Integer, CartItem> userCart = userCarts.computeIfAbsent(userId, k -> new HashMap<>());

        // Check if the item is already in the cart
        CartItem cartItem = userCart.get(bookId);
        if (cartItem != null) {
            // Update the quantity if the item already exists
            int newQuantity = cartItem.getQuantity() + quantity;
            if (book.getStockQuantity() < newQuantity) {
                throw new IllegalArgumentException(
                        "Không đủ số lượng trong kho. Hiện chỉ còn " + book.getStockQuantity() + " cuốn.");
            }
            cartItem.updateQuantity(newQuantity);
        } else {
            // Add a new cart item
            String imageUrl = bookService.getPrimaryImageUrl(bookId);
            book.setImageUrl(imageUrl);
            cartItem = new CartItem(book, quantity);
            userCart.put(bookId, cartItem);
        }

        return cartItem;
    }

    /**
     * Update the quantity of an item in the cart
     * 
     * @param userId   The ID of the user
     * @param bookId   The ID of the book to update
     * @param quantity The new quantity
     * @return The updated cart item
     */
    public CartItem updateCartItemQuantity(Integer userId, Integer bookId, Integer quantity) {
        Map<Integer, CartItem> userCart = getUserCart(userId);
        CartItem cartItem = userCart.get(bookId);

        if (cartItem == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ hàng");
        }

        if (quantity <= 0) {
            // Remove the item if quantity is 0 or negative
            userCart.remove(bookId);
            return null;
        }

        // Check if there's enough stock
        Book book = bookService.getBookById(bookId);
        if (book.getStockQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Không đủ số lượng trong kho. Hiện chỉ còn " + book.getStockQuantity() + " cuốn.");
        }

        cartItem.updateQuantity(quantity);
        return cartItem;
    }

    /**
     * Remove an item from the cart
     * 
     * @param userId The ID of the user
     * @param bookId The ID of the book to remove
     */
    public void removeFromCart(Integer userId, Integer bookId) {
        Map<Integer, CartItem> userCart = getUserCart(userId);
        userCart.remove(bookId);
    }

    /**
     * Clear the user's cart
     * 
     * @param userId The ID of the user
     */
    public void clearCart(Integer userId) {
        userCarts.remove(userId);
    }

    /**
     * Get all items in the user's cart
     * 
     * @param userId The ID of the user
     * @return A list of all cart items
     */
    public List<CartItem> getCartItems(Integer userId) {
        Map<Integer, CartItem> userCart = getUserCart(userId);
        return new ArrayList<>(userCart.values());
    }

    /**
     * Get the total number of items in the user's cart
     * 
     * @param userId The ID of the user
     * @return The total number of items
     */
    public int getCartItemCount(Integer userId) {
        Map<Integer, CartItem> userCart = getUserCart(userId);
        return userCart.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    /**
     * Get the total price of all items in the user's cart
     * 
     * @param userId The ID of the user
     * @return The total price
     */
    public double getTotalPrice(Integer userId) {
        Map<Integer, CartItem> userCart = getUserCart(userId);
        return userCart.values().stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    /**
     * Get the user's cart or create a new one if it doesn't exist
     * 
     * @param userId The ID of the user
     * @return The user's cart
     */
    private Map<Integer, CartItem> getUserCart(Integer userId) {
        return userCarts.computeIfAbsent(userId, k -> new HashMap<>());
    }

    /**
     * Process checkout for user's cart
     * 
     * @param userId          The ID of the user
     * @param shippingAddress The shipping address for the order
     * @param paymentMethod   The payment method
     * @return true if checkout was successful, false otherwise
     */
    @Transactional
    public boolean checkout(Integer userId, String shippingAddress, String paymentMethod) {
        try {
            // Get user's cart
            List<CartItem> cartItems = getCartItems(userId);
            if (cartItems.isEmpty()) {
                throw new IllegalArgumentException("Giỏ hàng trống, không thể thanh toán");
            }

            // Get user information
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người dùng"));

            // Tính tổng giá trị đơn hàng
            double totalAmount = getTotalPrice(userId);

            // Tạo đơn hàng mới
            Order order = new Order();
            order.setUser(user);
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(BigDecimal.valueOf(totalAmount));
            order.setStatus("Đang xử lý");

            // Lưu đơn hàng vào database và lấy id đã được tạo
            order = orderRepository.save(order);

            // Tạo danh sách chi tiết đơn hàng
            List<OrderDetail> orderDetails = new ArrayList<>();

            // Process each item in cart
            for (CartItem item : cartItems) {
                Book book = item.getBook();
                Integer quantity = item.getQuantity();

                // Check stock one more time
                Book freshBook = bookRepository.findById(book.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách: " + book.getTitle()));

                if (freshBook.getStockQuantity() < quantity) {
                    throw new IllegalArgumentException(
                            "Không đủ số lượng sách '" + freshBook.getTitle() +
                                    "' trong kho. Hiện chỉ còn " + freshBook.getStockQuantity() + " cuốn.");
                }

                // Tạo chi tiết đơn hàng
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);
                orderDetail.setBook(freshBook);
                orderDetail.setQuantity(quantity);
                orderDetail.setPriceAtOrder(BigDecimal.valueOf(book.getPrice()));
                orderDetails.add(orderDetail);

                // Create inventory transaction for exporting books
                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setBook(freshBook);
                transaction.setTransactionType(InventoryTransaction.TYPE_EXPORT);
                transaction.setQuantity(quantity);
                transaction.setTransactionDate(LocalDateTime.now());
                transaction.setPriceAtTransaction(BigDecimal.valueOf(book.getPrice()));
                transaction.setUser(user);

                // Process the transaction (this will update the book's stock)
                inventoryService.processTransaction(transaction);
            }

            // Lưu chi tiết đơn hàng vào database
            orderDetailRepository.saveAll(orderDetails);

            // Tạo và lưu thông tin thanh toán
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(BigDecimal.valueOf(totalAmount));
            payment.setPaymentMethod(paymentMethod);
            paymentRepository.save(payment);

            // Clear the user's cart after successful checkout
            clearCart(userId);

            return true;
        } catch (Exception e) {
            // Log the error
            System.err.println("Error processing checkout: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw the exception to be handled by the controller
        }
    }
}