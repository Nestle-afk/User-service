package com.innowise.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.UserServiceApplication;
import com.innowise.userservice.dto.UserRequest;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = UserServiceApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerIntegrationTest extends com.innowise.userservice.it.BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void whenCreateUser_thenReturnsCreatedUser() throws Exception {
        UserRequest request = new UserRequest("John", "Doe", LocalDate.of(1990, 5, 10), "john.doe@example.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void whenGetUserById_thenReturnsUser() throws Exception {
        User saved = userRepository.save(new User("Alice", "Smith", LocalDate.of(1992, 3, 15), "alice@example.com"));

        mockMvc.perform(get("/api/users/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void whenUpdateUser_thenUserIsUpdated() throws Exception {
        User saved = userRepository.save(new User("Bob", "Brown", LocalDate.of(1985, 7, 20), "bob@example.com"));

        UserRequest update = new UserRequest("Robert", "Brown", LocalDate.of(1985, 7, 20), "robert@example.com");

        mockMvc.perform(put("/api/users/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("robert@example.com"));
    }

    @Test
    void whenDeleteUser_thenUserIsRemoved() throws Exception {
        User saved = userRepository.save(new User("Tom", "Fox", LocalDate.of(1995, 8, 1), "tom@example.com"));

        mockMvc.perform(delete("/api/users/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void whenGetAllUsers_thenReturnsPagedResult() throws Exception {
        userRepository.deleteAll();
        userRepository.save(new User("Alice", "Smith",
                LocalDate.of(1990, 5, 12), "alice@example.com"));
        userRepository.save(new User("Bob", "Johnson",
                LocalDate.of(1985, 3, 8), "bob@example.com"));

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[1][0].name").value("Alice"))
                .andExpect(jsonPath("$.content[1][0].email").value("alice@example.com"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}

