package com.innowise.userservice.service;


import com.innowise.userservice.dto.CardRequest;
import com.innowise.userservice.dto.CardResponse;
import com.innowise.userservice.mapper.CardMapper;
import com.innowise.userservice.model.Card;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.CardRepository;
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
class CardServiceTest {

    @Mock
    private CardRepository cardInfoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private final User user = new User("John", "Doe",
            LocalDate.of(1990, 1, 1), "john.doe@example.com");

    private final CardRequest cardRequest = new CardRequest(1L, "1234567812345678",
            "John Doe", LocalDate.of(2025, 12, 31));

    private final Card card = new Card(user, "1234567812345678",
            "John Doe", LocalDate.of(2025, 12, 31));

    private final CardResponse cardResponse = new CardResponse(1L, "1234567812345678",
            "John Doe", LocalDate.of(2025, 12, 31), 1L);

    @Test
    void createCard_WhenUserExists_ShouldReturnCardResponse() {
        user.setId(1L);
        card.setId(1L);

        when(userRepository.findById(cardRequest.getUserId())).thenReturn(Optional.of(user));
        when(cardMapper.toEntity(cardRequest)).thenReturn(card);
        when(cardInfoRepository.save(card)).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardResponse);

        CardResponse result = cardService.createCard(cardRequest);

        assertNotNull(result);
        assertEquals(cardResponse.getNumber(), result.getNumber());
        assertEquals(cardResponse.getUserId(), result.getUserId());
        verify(userRepository).findById(cardRequest.getUserId());
        verify(cardMapper).toEntity(cardRequest);
        verify(cardInfoRepository).save(card);
        verify(cardMapper).toDto(card);
    }

    @Test
    void createCard_WhenUserNotExists_ShouldThrowException() {
        when(userRepository.findById(cardRequest.getUserId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> cardService.createCard(cardRequest));
        verify(userRepository).findById(cardRequest.getUserId());
        verify(cardMapper, never()).toEntity(any());
        verify(cardInfoRepository, never()).save(any());
    }

    @Test
    void getCardById_WhenCardExists_ShouldReturnCardResponse() {
        Long cardId = 1L;
        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardResponse);

        CardResponse result = cardService.getCardById(cardId);

        assertNotNull(result);
        assertEquals(cardResponse.getId(), result.getId());
        verify(cardInfoRepository).findById(cardId);
        verify(cardMapper).toDto(card);
    }

    @Test
    void getCardById_WhenCardNotExists_ShouldThrowException() {
        Long cardId = 1L;
        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> cardService.getCardById(cardId));
        verify(cardInfoRepository).findById(cardId);
        verify(cardMapper, never()).toDto(any());
    }

    @Test
    void getAllCards_ShouldReturnPageOfCardResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(card));
        when(cardInfoRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(card)).thenReturn(cardResponse);

        Page<CardResponse> result = cardService.getAllCards(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cardInfoRepository).findAll(pageable);
        verify(cardMapper).toDto(card);
    }

    @Test
    void updateCard_WhenCardExists_ShouldReturnUpdatedCardResponse() {
        Long cardId = 1L;
        user.setId(1L);
        card.setId(cardId);

        CardRequest updateRequest = new CardRequest(2L, "8765432187654321",
                "Jane Smith", LocalDate.of(2026, 6, 30));

        User newUser = new User("Jane", "Smith",
                LocalDate.of(1995, 5, 5), "jane.smith@example.com");
        newUser.setId(2L);

        Card updatedCard = new Card(newUser, "8765432187654321",
                "Jane Smith", LocalDate.of(2026, 6, 30));
        updatedCard.setId(cardId);

        CardResponse updatedResponse = new CardResponse(cardId, "8765432187654321",
                "Jane Smith", LocalDate.of(2026, 6, 30), 2L);

        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(userRepository.findById(updateRequest.getUserId())).thenReturn(Optional.of(newUser));
        doNothing().when(cardMapper).updateCardFromRequest(updateRequest, card);
        when(cardInfoRepository.save(card)).thenReturn(updatedCard);
        when(cardMapper.toDto(updatedCard)).thenReturn(updatedResponse);

        CardResponse result = cardService.updateCard(cardId, updateRequest);

        assertNotNull(result);
        assertEquals("8765432187654321", result.getNumber());
        assertEquals("Jane Smith", result.getHolder());
        verify(cardInfoRepository).findById(cardId);
        verify(userRepository).findById(updateRequest.getUserId());
        verify(cardMapper).updateCardFromRequest(updateRequest, card);
        verify(cardInfoRepository).save(card);
        verify(cardMapper).toDto(updatedCard);
    }

    @Test
    void updateCard_WhenCardNotExists_ShouldThrowException() {
        Long cardId = 1L;
        CardRequest updateRequest = new CardRequest(1L, "8765432187654321",
                "Jane Smith", LocalDate.of(2026, 6, 30));

        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> cardService.updateCard(cardId, updateRequest));
        verify(cardInfoRepository).findById(cardId);
        verify(userRepository, never()).findById(any());
        verify(cardMapper, never()).updateCardFromRequest(any(), any());
        verify(cardInfoRepository, never()).save(any());
    }

    @Test
    void updateCard_WhenUserNotExists_ShouldThrowException() {
        Long cardId = 1L;
        card.setId(cardId);
        user.setId(1L);

        CardRequest updateRequest = new CardRequest(2L, "8765432187654321",
                "Jane Smith", LocalDate.of(2026, 6, 30));

        when(cardInfoRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(userRepository.findById(updateRequest.getUserId())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> cardService.updateCard(cardId, updateRequest));
        verify(cardInfoRepository).findById(cardId);
        verify(userRepository).findById(updateRequest.getUserId());
        verify(cardMapper, never()).updateCardFromRequest(any(), any());
        verify(cardInfoRepository, never()).save(any());
    }

    @Test
    void deleteCard_WhenCardExists_ShouldDeleteCard() {
        Long cardId = 1L;
        when(cardInfoRepository.existsById(cardId)).thenReturn(true);
        doNothing().when(cardInfoRepository).deleteUserById(cardId);

        // Act
        cardService.deleteCardById(cardId);

        // Assert
        verify(cardInfoRepository).existsById(cardId);
        verify(cardInfoRepository).deleteUserById(cardId);
    }

    @Test
    void deleteCard_WhenCardNotExists_ShouldThrowException() {
        // Arrange
        Long cardId = 1L;
        when(cardInfoRepository.existsById(cardId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> cardService.deleteCardById(cardId));
        verify(cardInfoRepository).existsById(cardId);
        verify(cardInfoRepository, never()).deleteById(cardId);
    }
}
