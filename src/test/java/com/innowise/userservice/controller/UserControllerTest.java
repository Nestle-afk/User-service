package com.innowise.userservice.controller;

import com.innowise.userservice.dto.UserRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.exception.GlobalExceptionHandler;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserRequest userRequest = new UserRequest("John", "Doe",
            LocalDate.of(1990, 1, 1), "john.doe@example.com");

    private final UserResponse userResponse = new UserResponse(1L, "John", "Doe",
            LocalDate.of(1990, 1, 1), "john.doe@example.com");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Настроим ObjectMapper для правильной работы с LocalDate
        objectMapper.findAndRegisterModules();
    }

    @Test
    void createUser_ShouldReturnCreated() throws Exception {
        when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).createUser(any(UserRequest.class));
    }

    @Test
    void createUser_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        UserRequest invalidRequest = new UserRequest("", "", null, "invalid-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John"));

        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(1L)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(1L);
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUsers() throws Exception {
        Page<UserResponse> userPage = new PageImpl<>(
                Collections.singletonList(userResponse),
                PageRequest.of(0, 10),
                1
        );
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("John"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUser() throws Exception {
        when(userService.getUserByEmail("john.doe@example.com")).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).getUserByEmail("john.doe@example.com");
    }

    @Test
    void updateUser_WhenUserExists_ShouldReturnUpdatedUser() throws Exception {
        when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(userService).updateUser(eq(1L), any(UserRequest.class));
    }

    @Test
    void deleteUser_WhenUserExists_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUserById(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserById(1L);
    }
}
