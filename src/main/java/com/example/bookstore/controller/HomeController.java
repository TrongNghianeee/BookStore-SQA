package com.example.bookstore.controller;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.Category;
import com.example.bookstore.model.User;
import com.example.bookstore.service.BookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private BookService bookService;

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home(
            HttpSession session,
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "8") int size) {
        // Check if user is logged in
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Check if user has customer role
        if (!"KH".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        // Add user details to model
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());

        // Get categories for filter dropdown
        List<Category> categories = bookService.getAllCategories();
        model.addAttribute("categories", categories);

        // Đảm bảo keyword không phải null để tránh lỗi
        if (keyword != null) {
            keyword = keyword.trim();
        }

        // Get books with pagination - Sử dụng chuỗi rỗng thay vì "InStock" để hiển thị
        // tất cả sách
        Page<Book> bookPage = bookService.getBooks(keyword, categoryId, "", sortBy, page, size);

        // Thêm kiểm tra xem có sách hay không và log thông tin để debug
        System.out.println("Tìm thấy " + bookPage.getTotalElements() + " sách");

        // Add image URLs for each book
        for (Book book : bookPage.getContent()) {
            String imageUrl = bookService.getPrimaryImageUrl(book.getId());
            book.setImageUrl(imageUrl);
            // Log để debug
            System.out.println("Sách: " + book.getTitle() + ", ImageURL: " + imageUrl);
        }

        // Add books and pagination data to model
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());
        model.addAttribute("pageSize", size);

        // Add filter parameters to model
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedSortBy", sortBy);

        return "home";
    }
}