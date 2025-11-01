package com.innowise.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.dto.CardRequest;
import com.innowise.userservice.model.Card;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CardControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(new User("Jane", "Doe", LocalDate.of(1990, 2, 2), "jane@example.com"));
        if (cacheManager.getCache("users") != null) {
            cacheManager.getCache("users").clear();
        }
    }

    @Test
    void whenCreateCard_thenReturnsCreatedCard() throws Exception {
        CardRequest request = new CardRequest();
        request.setUserId(testUser.getId());
        request.setNumber("4111111111111111");
        request.setHolder("JANE DOE");
        request.setExpirationDate(LocalDate.of(2028, 12, 31));

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value("4111111111111111"))
                .andExpect(jsonPath("$.holder").value("JANE DOE"))
                .andExpect(jsonPath("$.userId").value(testUser.getId().intValue()));

        assertThat(cardRepository.findAll()).hasSize(1);
    }

    @Test
    void whenGetCardById_thenReturnsCard() throws Exception {
        Card saved = cardRepository.save(new Card(testUser, "5555444433332222", "JANE DOE", LocalDate.of(2027, 11, 30)));

        mockMvc.perform(get("/api/cards/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("5555444433332222"))
                .andExpect(jsonPath("$.holder").value("JANE DOE"))
                .andExpect(jsonPath("$.userId").value(testUser.getId().intValue()));
    }

    @Test
    void whenGetAllCards_thenReturnsPagedResult() throws Exception {
        cardRepository.save(new Card(testUser, "4111111111111111", "JANE DOE", LocalDate.of(2028, 12, 31)));
        cardRepository.save(new Card(testUser, "4000000000000002", "JANE DOE", LocalDate.of(2026, 6, 30)));

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void whenUpdateCard_thenCardIsUpdated() throws Exception {
        Card saved = cardRepository.save(new Card(
                testUser,
                "4000000000000002",
                "OLD HOLDER",
                LocalDate.of(2025, 10, 1)
        ));

        CardRequest update = new CardRequest();
        update.setUserId(testUser.getId());
        update.setNumber("4000000000009999");
        update.setHolder("NEW HOLDER");
        update.setExpirationDate(LocalDate.of(2030, 1, 1));

        mockMvc.perform(put("/api/cards/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("4000000000009999"))
                .andExpect(jsonPath("$.holder").value("NEW HOLDER"));
    }


    @Test
    void whenDeleteCard_thenCardIsRemoved() throws Exception {
        Card saved = cardRepository.save(new Card(testUser, "4000000000000003", "JANE DOE", LocalDate.of(2026, 5, 5)));

        mockMvc.perform(delete("/api/cards/{id}", saved.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        assertThat(cardRepository.existsById(saved.getId())).isFalse();
    }
}

