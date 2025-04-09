package com.example.bookstore.controller;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.User;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.DashboardService;
import com.example.bookstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService; // Thêm BookService

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("page", "overview");

        Map<String, Object> stats = dashboardService.getOverviewStats();
        model.addAttribute("stats", stats);

        return "dashboard";
    }

    @GetMapping("/users")
    public String users(
            HttpSession session,
            Model model,
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "7") int size) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("page", "users");

        Map<String, Long> userStats = userService.getUserStats();
        model.addAttribute("userStats", userStats);

        Page<User> userPage = userService.getUsers(keyword, role, status, page, size);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("pageSize", size);

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);

        return "dashboard";
    }

    @GetMapping("/users/add")
    public String showAddUserForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("Qly")) {
            return "redirect:/dashboard/users";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("page", "add-user");
        if (!model.containsAttribute("newUser")) {
            model.addAttribute("newUser", new User());
        }
        return "dashboard";
    }

    @PostMapping("/users/add")
    public String addUser(
            @ModelAttribute("newUser") User newUser,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("Qly")) {
            return "redirect:/dashboard/users";
        }

        // Kiểm tra các trường bắt buộc
        if (newUser.getUsername() == null || newUser.getUsername().isEmpty() ||
                newUser.getPassword() == null || newUser.getPassword().isEmpty() ||
                newUser.getRole() == null || newUser.getRole().isEmpty() ||
                newUser.getEmail() == null || newUser.getEmail().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ các trường bắt buộc!");
            redirectAttributes.addFlashAttribute("newUser", newUser); // Giữ lại dữ liệu đã nhập
            return "redirect:/dashboard/users/add";
        }

        // Kiểm tra trùng lặp
        if (userService.isUsernameTaken(newUser.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Tên người dùng đã tồn tại!");
            redirectAttributes.addFlashAttribute("newUser", newUser);
            return "redirect:/dashboard/users/add";
        }

        if (userService.isEmailTaken(newUser.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
            redirectAttributes.addFlashAttribute("newUser", newUser);
            return "redirect:/dashboard/users/add";
        }

        if (userService.isPhoneTaken(newUser.getPhone())) {
            redirectAttributes.addFlashAttribute("error", "Số điện thoại đã tồn tại!");
            redirectAttributes.addFlashAttribute("newUser", newUser);
            return "redirect:/dashboard/users/add";
        }

        try {
            // Lưu tài khoản mới
            userService.saveUser(newUser);
            redirectAttributes.addFlashAttribute("success", "Thêm tài khoản thành công!");
            return "redirect:/dashboard/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm tài khoản. Vui lòng thử lại!");
            redirectAttributes.addFlashAttribute("newUser", newUser);
            return "redirect:/dashboard/users/add";
        }
    }

    @GetMapping("/books")
    public String books(
            HttpSession session,
            Model model,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "stockStatus", required = false) String stockStatus,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("page", "books");

        // Lấy danh sách thể loại để hiển thị trong bộ lọc
        model.addAttribute("categories", bookService.getAllCategories());

        // Lấy danh sách sách với phân trang
        Page<Book> bookPage = bookService.getBooks(keyword, categoryId, stockStatus, sortBy, page, size);
        // Lấy URL ảnh chính cho từng sách
        for (Book book : bookPage.getContent()) {
            String imageUrl = bookService.getPrimaryImageUrl(book.getId());
            book.setImageUrl(imageUrl); // Giả sử bạn đã thêm thuộc tính imageUrl vào Book
        }
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalItems", bookPage.getTotalElements());
        model.addAttribute("pageSize", size);

        // Truyền các giá trị bộ lọc để giữ trạng thái
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStockStatus", stockStatus);
        model.addAttribute("selectedSortBy", sortBy);

        return "dashboard";
    }

    // Khai báo endpoint cho nút "Thêm loại sách"
    @GetMapping("/books/add-category")
    public String showAddCategoryForm(HttpSession session, Model model) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("page", "add-category");
        return "dashboard";
    }

    // Khai báo endpoint cho nút "Thêm sản phẩm"
    @GetMapping("/books/add")
    public String showAddBookForm(HttpSession session, Model model) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("page", "add-book");
        return "dashboard";
    }

    // Khai báo endpoint cho nút "Chỉnh sửa"
    @GetMapping("/books/edit")
    public String showEditBookForm(
            @RequestParam("id") Integer bookId,
            HttpSession session,
            Model model) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("page", "edit-book");
        model.addAttribute("bookId", bookId);
        return "dashboard";
    }

    // Khai báo endpoint cho nút "Xem chi tiết"
    @GetMapping("/books/details")
    public String showBookDetails(
            @RequestParam("id") Integer bookId,
            HttpSession session,
            Model model) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("page", "book-details");
        model.addAttribute("bookId", bookId);
        return "dashboard";
    }

    @GetMapping("/inventory")
    public String inventory(HttpSession session, Model model) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("page", "inventory");
        return "dashboard";
    }

    @GetMapping("/reports")
    public String reports(HttpSession session, Model model) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("page", "reports");
        return "dashboard";
    }

    private boolean isAuthorized(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return false;
        }

        String role = user.getRole();
        return role.equals("Qly") || role.equals("Nvien");
    }
}