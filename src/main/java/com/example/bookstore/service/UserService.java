package com.example.bookstore.service;

import com.example.bookstore.model.User;
import com.example.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Lấy thống kê nhanh (Quản lý, Nhân viên, Khách hàng, Hoạt động, Tạm khóa)
    public Map<String, Long> getUserStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("managers", userRepository.countByRole("Qly"));
        stats.put("employees", userRepository.countByRole("Nvien"));
        stats.put("customers", userRepository.countByRole("KH")); // Sửa từ "Khach" thành "KH"
        stats.put("active", userRepository.countByStatus("Active")); // Sửa từ "HoatDong" thành "Active"
        stats.put("locked", userRepository.countByStatus("Lock")); // Sửa từ "TamKhoa" thành "Lock"
        return stats;
    }

    // Lấy danh sách tài khoản với tìm kiếm, lọc và phân trang
    public Page<User> getUsers(String keyword, String role, String status, int page, int size) {
        size = 7; // Fixed page size to 7 items
        Pageable pageable = PageRequest.of(page - 1, size);

        if (keyword != null && !keyword.trim().isEmpty()) {
            return userRepository.findByUsernameContainingOrEmailContainingOrPhoneContaining(
                    keyword.trim(), keyword.trim(), keyword.trim(), pageable);
        }

        if (role != null && !role.isEmpty() && status != null && !status.isEmpty()) {
            return userRepository.findByRoleAndStatus(role, status, pageable);
        } else if (role != null && !role.isEmpty()) {
            return userRepository.findByRole(role, pageable);
        } else if (status != null && !status.isEmpty()) {
            return userRepository.findByStatus(status, pageable);
        }

        return userRepository.findAll(pageable);
    }

    // Kiểm tra username đã tồn tại chưa
    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // Kiểm tra email đã tồn tại chưa
    public boolean isEmailTaken(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // Kiểm tra phone đã tồn tại chưa
    public boolean isPhoneTaken(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false; // Nếu phone không được nhập, không cần kiểm tra
        }
        return userRepository.findByPhone(phone).isPresent();
    }

    // Lưu tài khoản mới
    public void saveUser(User user) {
        user.setStatus("Active"); // Mặc định trạng thái là Active
        user.setCreatedAt(LocalDateTime.now()); // Lấy thời gian hiện tại
        userRepository.save(user);
    }

    public User authenticate(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (password.equals(user.getPassword()) && "Active".equals(user.getStatus())) {
                return user;
            }
        }
        return null;
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public void updateUser(User updatedUser) {
        Optional<User> existingUser = userRepository.findById(updatedUser.getId());
        if (existingUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User currentUser = existingUser.get();

        // Validate username if changed
        if (!currentUser.getUsername().equals(updatedUser.getUsername()) &&
                userRepository.findByUsername(updatedUser.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Validate email if changed
        if (!currentUser.getEmail().equals(updatedUser.getEmail()) &&
                userRepository.findByEmail(updatedUser.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Validate phone if changed
        if (updatedUser.getPhone() != null && !updatedUser.getPhone().equals(currentUser.getPhone()) &&
                userRepository.findByPhone(updatedUser.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        // Update fields
        currentUser.setUsername(updatedUser.getUsername());
        currentUser.setRole(updatedUser.getRole());
        currentUser.setEmail(updatedUser.getEmail());
        currentUser.setPhone(updatedUser.getPhone());
        currentUser.setAddress(updatedUser.getAddress());

        // Update password only if provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            currentUser.setPassword(updatedUser.getPassword());
        }

        userRepository.save(currentUser);
    }

    public void toggleUserStatus(Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        user.setStatus(user.getStatus().equals("Active") ? "Lock" : "Active");
        userRepository.save(user);
    }
}