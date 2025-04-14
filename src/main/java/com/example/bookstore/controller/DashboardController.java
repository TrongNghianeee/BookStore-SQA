package com.example.bookstore.controller;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.Category;
import com.example.bookstore.model.User;
import com.example.bookstore.model.BookImage;
import com.example.bookstore.model.InventoryTransaction;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.DashboardService;
import com.example.bookstore.service.InventoryService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private InventoryService inventoryService;

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

        // Thêm đối tượng Category mới vào model
        model.addAttribute("newCategory", new Category());

        return "dashboard";
    }

    @PostMapping("/books/add-category")
    public String addCategory(
            @ModelAttribute("newCategory") Category newCategory,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        // Kiểm tra dữ liệu đầu vào
        if (newCategory.getCategoryName() == null || newCategory.getCategoryName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập tên loại sách!");
            return "redirect:/dashboard/books/add-category";
        }

        try {
            // Lưu loại sách mới
            bookService.saveCategory(newCategory);
            redirectAttributes.addFlashAttribute("success", "Thêm loại sách thành công!");
            return "redirect:/dashboard/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm loại sách. Vui lòng thử lại!");
            return "redirect:/dashboard/books/add-category";
        }
    }

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

        // Thêm đối tượng Book mới vào model
        Book newBook = new Book();
        newBook.setStockQuantity(0); // Đặt số lượng mặc định là 0
        model.addAttribute("newBook", newBook);

        // Lấy danh sách các loại sách để hiển thị trong form
        model.addAttribute("categories", bookService.getAllCategories());

        return "dashboard";
    }

    @PostMapping("/books/add")
    public String addBook(
            @ModelAttribute("newBook") Book newBook,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        // Kiểm tra dữ liệu đầu vào
        if (newBook.getTitle() == null || newBook.getTitle().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập tiêu đề sách!");
            redirectAttributes.addFlashAttribute("newBook", newBook);
            return "redirect:/dashboard/books/add";
        }

        if (newBook.getAuthor() == null || newBook.getAuthor().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập tên tác giả!");
            redirectAttributes.addFlashAttribute("newBook", newBook);
            return "redirect:/dashboard/books/add";
        }

        if (newBook.getCategory() == null || newBook.getCategory().getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn thể loại sách!");
            redirectAttributes.addFlashAttribute("newBook", newBook);
            return "redirect:/dashboard/books/add";
        }

        if (newBook.getPrice() == null || newBook.getPrice() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập giá hợp lệ!");
            redirectAttributes.addFlashAttribute("newBook", newBook);
            return "redirect:/dashboard/books/add";
        }

        // Đặt mặc định số lượng là 0 nếu không nhập
        if (newBook.getStockQuantity() == null) {
            newBook.setStockQuantity(0);
        }

        try {
            // Lưu sách mới
            bookService.saveBook(newBook);
            redirectAttributes.addFlashAttribute("success", "Thêm sách thành công!");
            return "redirect:/dashboard/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm sách. Vui lòng thử lại!");
            redirectAttributes.addFlashAttribute("newBook", newBook);
            return "redirect:/dashboard/books/add";
        }
    }

    @GetMapping("/books/edit")
    public String showEditBookForm(
            @RequestParam("id") Integer bookId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("page", "edit-book");

        // Lấy thông tin sách cần chỉnh sửa
        Book book = bookService.getBookById(bookId);
        if (book == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sách với ID: " + bookId);
            return "redirect:/dashboard/books";
        }

        model.addAttribute("editBook", book);

        // Lấy danh sách các loại sách để hiển thị trong form
        model.addAttribute("categories", bookService.getAllCategories());

        return "dashboard";
    }

    @PostMapping("/books/edit")
    public String editBook(
            @ModelAttribute("editBook") Book editBook,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        // Kiểm tra dữ liệu đầu vào
        if (editBook.getTitle() == null || editBook.getTitle().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập tiêu đề sách!");
            return "redirect:/dashboard/books/edit?id=" + editBook.getId();
        }

        if (editBook.getAuthor() == null || editBook.getAuthor().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập tên tác giả!");
            return "redirect:/dashboard/books/edit?id=" + editBook.getId();
        }

        if (editBook.getCategory() == null || editBook.getCategory().getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn thể loại sách!");
            return "redirect:/dashboard/books/edit?id=" + editBook.getId();
        }

        if (editBook.getPrice() == null || editBook.getPrice() <= 0) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập giá hợp lệ!");
            return "redirect:/dashboard/books/edit?id=" + editBook.getId();
        }

        // Đảm bảo số lượng không âm
        if (editBook.getStockQuantity() == null || editBook.getStockQuantity() < 0) {
            editBook.setStockQuantity(0);
        }

        try {
            // Lấy thông tin sách hiện tại từ database
            Book existingBook = bookService.getBookById(editBook.getId());
            if (existingBook == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sách với ID: " + editBook.getId());
                return "redirect:/dashboard/books";
            }

            // Cập nhật sách
            bookService.updateBook(editBook);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin sách thành công!");
            return "redirect:/dashboard/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật sách: " + e.getMessage());
            return "redirect:/dashboard/books/edit?id=" + editBook.getId();
        }
    }

    @GetMapping("/books/details")
    public String showBookDetails(
            @RequestParam("id") Integer bookId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("page", "book-details");

        // Lấy thông tin chi tiết sách
        Book book = bookService.getBookById(bookId);
        if (book == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sách với ID: " + bookId);
            return "redirect:/dashboard/books";
        }

        // Lấy URL ảnh chính cho sách
        String imageUrl = bookService.getPrimaryImageUrl(bookId);
        book.setImageUrl(imageUrl);

        model.addAttribute("bookDetails", book);

        // Lấy danh sách ảnh của sách (nếu có)
        List<BookImage> bookImages = bookService.getBookImages(bookId);
        model.addAttribute("bookImages", bookImages);

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

        // Add empty inventory transaction object to model
        InventoryTransaction newTransaction = new InventoryTransaction();
        newTransaction.setBook(new Book());
        model.addAttribute("inventoryTransaction", newTransaction);

        // Add categories for category dropdown
        model.addAttribute("categories", bookService.getAllCategories());

        // Get recent transactions for display
        List<InventoryTransaction> recentTransactions = inventoryService.getRecentTransactions(10);
        model.addAttribute("recentTransactions", recentTransactions);

        // Get total transactions count
        long totalTransactions = inventoryService.getTotalTransactionsCount();
        model.addAttribute("totalTransactions", totalTransactions);

        return "dashboard";
    }

    @PostMapping("/inventory/transaction")
    public String processInventoryTransaction(
            @ModelAttribute("inventoryTransaction") InventoryTransaction transaction,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");

        try {
            // Validate the transaction
            if (transaction.getBook() == null || transaction.getBook().getId() == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn sách!");
                return "redirect:/dashboard/inventory";
            }

            if (transaction.getQuantity() == null || transaction.getQuantity() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Số lượng phải lớn hơn 0!");
                return "redirect:/dashboard/inventory";
            }

            if (transaction.getPriceAtTransaction() == null || transaction.getPriceAtTransaction().doubleValue() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Giá phải lớn hơn 0!");
                return "redirect:/dashboard/inventory";
            }

            if (transaction.getTransactionType() == null || transaction.getTransactionType().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn phương thức nhập/xuất kho!");
                return "redirect:/dashboard/inventory";
            }

            // Set the current user and transaction date
            transaction.setUser(user);
            transaction.setTransactionDate(LocalDateTime.now());

            // Process and save the transaction
            inventoryService.processTransaction(transaction);

            redirectAttributes.addFlashAttribute("success", "Giao dịch " +
                    (transaction.getTransactionType().equals("IMPORT") ? "nhập kho" : "xuất kho") +
                    " đã được lưu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard/inventory";
    }

    @GetMapping("/inventory/transactions")
    public String viewAllTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Model model,
            HttpSession session) {

        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        // Phân trang với kích thước trang là 20
        int pageSize = 20;

        // Filter transactions by type and date range
        Page<InventoryTransaction> transactionsPage = inventoryService.getTransactionsPaginated(page - 1, pageSize,
                type, fromDate, toDate);

        model.addAttribute("transactions", transactionsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactionsPage.getTotalPages());
        model.addAttribute("totalItems", transactionsPage.getTotalElements());

        // Add filter values to model to maintain state in the view
        model.addAttribute("selectedType", type);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        // User info for navbar
        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());

        model.addAttribute("page", "inventory-transactions");
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