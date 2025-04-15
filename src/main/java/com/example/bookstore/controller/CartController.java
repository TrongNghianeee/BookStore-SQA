package com.example.bookstore.controller;

import com.example.bookstore.model.CartItem;
import com.example.bookstore.model.User;
import com.example.bookstore.service.CartService;
import com.example.bookstore.service.InventoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartService.getCartItems(user.getId());
        double totalPrice = cartService.getTotalPrice(user.getId());

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("username", user.getUsername());

        return "cart";
    }

    @PostMapping("/api/cart/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam("bookId") Integer bookId,
            @RequestParam("quantity") Integer quantity,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            CartItem cartItem = cartService.addToCart(user.getId(), bookId, quantity);
            int cartItemCount = cartService.getCartItemCount(user.getId());

            response.put("success", true);
            response.put("message", "Đã thêm sản phẩm vào giỏ hàng");
            response.put("cartItem", cartItem);
            response.put("cartItemCount", cartItemCount);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra, vui lòng thử lại sau");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/cart/update")
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @RequestParam("bookId") Integer bookId,
            @RequestParam("quantity") Integer quantity,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để cập nhật giỏ hàng");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            CartItem cartItem = cartService.updateCartItemQuantity(user.getId(), bookId, quantity);
            int cartItemCount = cartService.getCartItemCount(user.getId());
            double totalPrice = cartService.getTotalPrice(user.getId());

            response.put("success", true);
            response.put("message", "Đã cập nhật giỏ hàng");
            response.put("cartItem", cartItem);
            response.put("cartItemCount", cartItemCount);
            response.put("totalPrice", totalPrice);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra, vui lòng thử lại sau");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/cart/remove")
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @RequestParam("bookId") Integer bookId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để xóa sản phẩm khỏi giỏ hàng");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            cartService.removeFromCart(user.getId(), bookId);
            int cartItemCount = cartService.getCartItemCount(user.getId());
            double totalPrice = cartService.getTotalPrice(user.getId());

            response.put("success", true);
            response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng");
            response.put("cartItemCount", cartItemCount);
            response.put("totalPrice", totalPrice);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra, vui lòng thử lại sau");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/api/cart/count")
    public ResponseEntity<Map<String, Object>> getCartItemCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("cartItemCount", 0);
            return ResponseEntity.ok(response);
        }

        int cartItemCount = cartService.getCartItemCount(user.getId());
        response.put("cartItemCount", cartItemCount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/cart/clear")
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để xóa giỏ hàng");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            cartService.clearCart(user.getId());

            response.put("success", true);
            response.put("message", "Đã xóa tất cả sản phẩm khỏi giỏ hàng");
            response.put("cartItemCount", 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra, vui lòng thử lại sau");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/cart/checkout")
    public ResponseEntity<Map<String, Object>> checkout(
            @RequestParam(required = false) String shippingAddress,
            @RequestParam(required = false) String paymentMethod,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để thanh toán");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Nếu không nhập địa chỉ giao hàng, sử dụng địa chỉ của người dùng
            if (shippingAddress == null || shippingAddress.isEmpty()) {
                shippingAddress = user.getAddress();
            }

            // Nếu không chọn phương thức thanh toán, mặc định là "Tiền mặt khi nhận hàng"
            if (paymentMethod == null || paymentMethod.isEmpty()) {
                paymentMethod = "Tiền mặt khi nhận hàng";
            }

            // Gọi service để xử lý thanh toán và tạo các giao dịch xuất kho
            boolean checkoutSuccess = cartService.checkout(user.getId(), shippingAddress, paymentMethod);

            if (checkoutSuccess) {
                response.put("success", true);
                response.put("message", "Đặt hàng thành công!");
                response.put("cartItemCount", 0); // Giỏ hàng đã bị xóa sau khi thanh toán
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Đặt hàng không thành công, vui lòng thử lại sau");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra, vui lòng thử lại sau: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}