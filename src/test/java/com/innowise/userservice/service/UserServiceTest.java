package com.innowise.userservice.service;

import com.innowise.userservice.dto.UserRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private final UserRequest userRequest = new UserRequest("John", "Doe",
            LocalDate.of(1990, 1, 1), "john.doe@example.com");

    private final User user = new User("John", "Doe",
            LocalDate.of(1990, 1, 1), "john.doe@example.com");

    private final UserResponse userResponse = new UserResponse(1L, "John", "Doe",
            LocalDate.of(1990, 1, 1), "john.doe@example.com");

    @Test
    void createUser_ShouldReturnUserResponse() {
        when(userMapper.toEntity(userRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponse result = userService.createUser(userRequest);

        assertNotNull(result);
        assertEquals(userResponse.getId(), result.getId());
        assertEquals(userResponse.getEmail(), result.getEmail());
        verify(userMapper).toEntity(userRequest);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserResponse() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userResponse.getId(), result.getId());
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowException() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.getUserById(userId));
        verify(userRepository).findById(userId);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUserResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDto(user)).thenReturn(userResponse);

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(pageable);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUserResponse() {
        String email = "john.doe@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(userResponse.getEmail(), result.getEmail());
        verify(userRepository).findByEmail(email);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserByEmail_WhenUserNotExists_ShouldThrowException() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.getUserByEmail(email));
        verify(userRepository).findByEmail(email);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void updateUser_WhenUserExists_ShouldReturnUpdatedUserResponse() {
        Long userId = 1L;
        UserRequest updateRequest = new UserRequest("Jane", "Smith",
                LocalDate.of(1995, 5, 5), "jane.smith@example.com");

        User existingUser = new User("John", "Doe",
                LocalDate.of(1990, 1, 1), "john.doe@example.com");
        existingUser.setId(userId);

        UserResponse updatedResponse = new UserResponse(userId, "Jane", "Smith",
                LocalDate.of(1995, 5, 5), "jane.smith@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userMapper).updateUserFromRequest(updateRequest, existingUser);

        doNothing().when(userRepository).updateUser(existingUser);

        when(userMapper.toDto(eq(existingUser))).thenReturn(updatedResponse);

        UserResponse result = userService.updateUser(userId, updateRequest);

        assertNotNull(result);
        assertEquals("Jane", result.getName());
        assertEquals("Smith", result.getSurname());
        verify(userRepository).findById(userId);
        verify(userMapper).updateUserFromRequest(updateRequest, existingUser);
        verify(userRepository).updateUser(existingUser);
        verify(userMapper).toDto(existingUser);
    }

    @Test
    void updateUser_WhenUserNotExists_ShouldThrowException() {
        Long userId = 1L;
        UserRequest updateRequest = new UserRequest("Jane", "Smith",
                LocalDate.of(1995, 5, 5), "jane.smith@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.updateUser(userId, updateRequest));
        verify(userRepository).findById(userId);
        verify(userMapper, never()).updateUserFromRequest(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteUserById(userId);

        userService.deleteUserById(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteUserById(userId);
    }

    @Test
    void deleteUser_WhenUserNotExists_ShouldThrowException() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> userService.deleteUserById(userId));
        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }
}
