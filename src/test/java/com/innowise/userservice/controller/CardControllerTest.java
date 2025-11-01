package com.innowise.userservice.controller;

import com.innowise.userservice.dto.CardRequest;
import com.innowise.userservice.dto.CardResponse;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.GlobalExceptionHandler;
import com.innowise.userservice.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
class CardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CardRequest cardRequest = new CardRequest(1L, "1234567812345678",
            "John Doe", LocalDate.of(2025, 12, 31));

    private final CardResponse cardResponse = new CardResponse(1L, "1234567812345678",
            "John Doe", LocalDate.of(2025, 12, 31), 1L);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper.findAndRegisterModules();
    }

    @Test
    void createCard_ShouldReturnCreated() throws Exception {
        when(cardService.createCard(any(CardRequest.class))).thenReturn(cardResponse);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.number").value("1234567812345678"))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(cardService).createCard(any(CardRequest.class));
    }

    @Test
    void createCard_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        CardRequest invalidRequest = new CardRequest();
        invalidRequest.setUserId(null);
        invalidRequest.setNumber("123");
        invalidRequest.setHolder("");
        invalidRequest.setExpirationDate(null);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardById_WhenCardExists_ShouldReturnCard() throws Exception {
        when(cardService.getCardById(1L)).thenReturn(cardResponse);

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.number").value("1234567812345678"));

        verify(cardService).getCardById(1L);
    }

    @Test
    void getCardById_WhenCardNotExists_ShouldReturnNotFound() throws Exception {
        when(cardService.getCardById(1L)).thenThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isNotFound());

        verify(cardService).getCardById(1L);
    }

    @Test
    void getAllCards_ShouldReturnPageOfCards() throws Exception {
        Page<CardResponse> cardPage = new PageImpl<>(
                Collections.singletonList(cardResponse),
                PageRequest.of(0, 10),
                1
        );

        when(cardService.getAllCards(any(Pageable.class))).thenReturn(cardPage);

        mockMvc.perform(get("/api/cards").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].holder").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(cardService).getAllCards(any(Pageable.class));
    }

    @Test
    void updateCard_WhenCardExists_ShouldReturnUpdatedCard() throws Exception {
        System.out.println("=== CARD REQUEST STATE ===");
        System.out.println("userId: " + cardRequest.getUserId());
        System.out.println("number: " + cardRequest.getNumber());
        System.out.println("holder: " + cardRequest.getHolder());
        System.out.println("expirationDate: " + cardRequest.getExpirationDate());
        System.out.println("=== END STATE ===");

        System.out.println("Valid request: " + objectMapper.writeValueAsString(cardRequest));

        when(cardService.updateCard(eq(1L), any(CardRequest.class))).thenReturn(cardResponse);

        mockMvc.perform(put("/api/cards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andDo(result -> {
                    System.out.println("=== RESPONSE ANALYSIS ===");
                    System.out.println("Status: " + result.getResponse().getStatus());
                    System.out.println("Response body: " + result.getResponse().getContentAsString());
                    System.out.println("Was service called? " +
                            Mockito.mockingDetails(cardService).getInvocations().size() + " invocations");
                    System.out.println("=== END ANALYSIS ===");
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(cardService).updateCard(eq(1L), any(CardRequest.class));
    }

    @Test
    void deleteCard_WhenCardExists_ShouldReturnNoContent() throws Exception {
        doNothing().when(cardService).deleteCardById(1L);

        mockMvc.perform(delete("/api/cards/1"))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCardById(1L);
    }
}