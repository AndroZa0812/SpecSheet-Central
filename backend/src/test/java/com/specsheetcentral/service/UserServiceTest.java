package com.specsheetcentral.service;

import com.specsheetcentral.model.User;
import com.specsheetcentral.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_newEmail_createsUser() {
        String email = "test@example.com";
        String password = "password123";
        User.Role role = User.Role.USER;

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User result = userService.register(email, password, role);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("hashedPassword", result.getPasswordHash());
        assertEquals(role, result.getRole());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        String email = "existing@example.com";
        String password = "password123";
        User.Role role = User.Role.USER;

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.register(email, password, role)
        );
        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByEmail_existingUser_returnsUser() {
        String email = "test@example.com";

        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setRole(User.Role.USER);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.findByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmail_nonExistingUser_throwsException() {
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> userService.findByEmail(email)
        );
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail(email);
    }
}