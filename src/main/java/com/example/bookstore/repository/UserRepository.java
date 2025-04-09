package com.example.bookstore.repository;

import com.example.bookstore.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
       Optional<User> findByUsername(String username);

       Optional<User> findByEmail(String email);

       Optional<User> findByPhone(String phone);

       Long countByRole(String role);

       Long countByStatus(String status);

       Page<User> findByRole(String role, Pageable pageable);

       Page<User> findByStatus(String status, Pageable pageable);

       Page<User> findByRoleAndStatus(String role, String status, Pageable pageable);

       Page<User> findByUsernameContainingOrEmailContainingOrPhoneContaining(
                     String username, String email, String phone, Pageable pageable);

       @Query("SELECT u FROM User u WHERE " +
                     "(:keyword IS NULL OR u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.phone LIKE %:keyword%) "
                     +
                     "AND (:role IS NULL OR u.role = :role) " +
                     "AND (:status IS NULL OR u.status = :status)")
       List<User> searchAndFilterUsers(
                     @Param("keyword") String keyword,
                     @Param("role") String role,
                     @Param("status") String status);
}