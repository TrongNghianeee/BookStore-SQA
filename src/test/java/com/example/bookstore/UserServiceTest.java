package com.example.bookstore;

import com.example.bookstore.model.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Setup a mock user for testing
        user = new User();
        user.setId(37);
        user.setUsername("abc");
        user.setPassword("123456");
        user.setEmail("abc@example.com");
        user.setPhone("123456789");
        user.setRole("KH");
        user.setStatus("Active");
        user.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetUserStats() {
        when(userRepository.countByRole("Qly")).thenReturn(5L);
        when(userRepository.countByRole("Nvien")).thenReturn(10L);
        when(userRepository.countByRole("KH")).thenReturn(50L);
        when(userRepository.countByStatus("Active")).thenReturn(40L);
        when(userRepository.countByStatus("Lock")).thenReturn(10L);

        var stats = userService.getUserStats();

        assertEquals(5L, stats.get("managers"));
        assertEquals(10L, stats.get("employees"));
        assertEquals(50L, stats.get("customers"));
        assertEquals(40L, stats.get("active"));
        assertEquals(10L, stats.get("locked"));

        verify(userRepository).countByRole("Qly");
        verify(userRepository).countByRole("Nvien");
        verify(userRepository).countByRole("KH");
        verify(userRepository).countByStatus("Active");
        verify(userRepository).countByStatus("Lock");
    }

    @Test
    void testGetUsersWithFilters() {
        User user1 = new User();
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUsername("user2");

        List<User> userList = Arrays.asList(user1, user2);
        when(userRepository.findByRoleAndStatus("KH", "Active", PageRequest.of(0, 7)))
            .thenReturn(new PageImpl<>(userList));

        var usersPage = userService.getUsers(null, "KH", "Active", 1, 7);

        assertNotNull(usersPage);
        assertEquals(2, usersPage.getContent().size());
        verify(userRepository).findByRoleAndStatus("KH", "Active", PageRequest.of(0, 7));
    }

    @Test
    void testIsUsernameTaken() {
        when(userRepository.findByUsername("kh6")).thenReturn(Optional.of(user));

        boolean result = userService.isUsernameTaken("kh6");

        assertTrue(result);
        verify(userRepository).findByUsername("kh6");
    }

    @Test
    void testIsEmailTaken() {
        when(userRepository.findByEmail("kh3@gmail.com")).thenReturn(Optional.of(user));

        boolean result = userService.isEmailTaken("kh3@gmail.com");

        assertTrue(result);
        verify(userRepository).findByEmail("kh3@gmail.com");
    }

    @Test
    void testIsPhoneTaken() {
        when(userRepository.findByPhone("0967890123")).thenReturn(Optional.of(user));

        boolean result = userService.isPhoneTaken("0967890123");

        assertTrue(result);
        verify(userRepository).findByPhone("0967890123");
    }

    @Test
    void testSaveUser() {
        User newUser = new User();
        newUser.setUsername("xyz");
        newUser.setEmail("xyz@gmail.com");

        when(userRepository.save(newUser)).thenReturn(newUser);

        userService.saveUser(newUser);

        verify(userRepository).save(newUser);
    }

    @Test
    void testAuthenticateSuccessful() {
        when(userRepository.findByUsername("abc")).thenReturn(Optional.of(user));

        User authenticatedUser = userService.authenticate("abc", "123456");

        assertNotNull(authenticatedUser);
        assertEquals("abc", authenticatedUser.getUsername());
        verify(userRepository).findByUsername("abc");
    }

    @Test
    void testAuthenticateFailure() {
        when(userRepository.findByUsername("abc")).thenReturn(Optional.of(user));

        User authenticatedUser = userService.authenticate("abc", "wrongpassword");

        assertNull(authenticatedUser);
        verify(userRepository).findByUsername("abc");
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(37)).thenReturn(Optional.of(user));

        Optional<User> retrievedUser = userService.getUserById(37);

        assertTrue(retrievedUser.isPresent());
        assertEquals("abc", retrievedUser.get().getUsername());
        verify(userRepository).findById(37);
    }

    @Test
    void testUpdateUser() {
        // Create the updated user
        User updatedUser = new User();
        updatedUser.setId(38);
        updatedUser.setUsername("aaa_updated");

        // Create the existing user that will be fetched by findById
        User existingUser = new User();
        existingUser.setId(38); // Match the ID of updatedUser
        existingUser.setUsername("aaa");
        existingUser.setPassword("123456");
        existingUser.setEmail("aaa@example.com");
        existingUser.setPhone("111222333");
        existingUser.setRole("KH");
        existingUser.setStatus("Active");
        existingUser.setCreatedAt(LocalDateTime.now());

        // Mock repository behavior
        when(userRepository.findById(38)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("aaa_updated")).thenReturn(Optional.empty());
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        userService.updateUser(updatedUser);

        assertEquals("aaa_updated", existingUser.getUsername());
        verify(userRepository).save(existingUser);
    }

    @Test
    void testToggleUserStatus() {
        when(userRepository.findById(8)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.toggleUserStatus(8);

        assertEquals("Lock", user.getStatus()); // Since initial status was "Active"
        verify(userRepository).save(user);
    }
}
