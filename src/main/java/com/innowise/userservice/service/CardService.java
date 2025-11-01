package com.innowise.userservice.service;

import com.innowise.userservice.dto.CardRequest;
import com.innowise.userservice.dto.CardResponse;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.model.*;
import com.innowise.userservice.mapper.CardMapper;
import com.innowise.userservice.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;

    public CardService(CardRepository cardRepository, UserRepository userRepository, CardMapper cardMapper) {
        this.cardMapper = cardMapper;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    @Caching(put = {
            @CachePut(value = "users", key = "#cardRequest.userId")
    })
    @Transactional
    public CardResponse createCard(CardRequest cardRequest) {
        if (cardRequest.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(cardRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + cardRequest.getUserId()));

        Card card = cardMapper.toEntity(cardRequest);
        card.setUser(user);
        Card savedCard = cardRepository.save(card);

        return cardMapper.toDto(savedCard);
    }

    public CardResponse getCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        return cardMapper.toDto(card);
    }

    public Page<CardResponse> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(cardMapper::toDto);
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "#cardRequest.userId"),
            @CacheEvict(value = "users", key = "#result.userId", condition = "#cardRequest.userId != #result.userId")
    })
    @Transactional
    public CardResponse updateCard(Long id, CardRequest cardRequest) {
        Card currentCard = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (!currentCard.getUser().getId().equals(cardRequest.getUserId())) {
            User user = userRepository.findById(cardRequest.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(id));

            currentCard.setUser(user);
        }

        cardMapper.updateCardFromRequest(cardRequest, currentCard);
        cardRepository.updateCard(currentCard);

        return cardMapper.toDto(currentCard);
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "#result.user.id", condition = "#result != null")
    })
    @Transactional
    public void deleteCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        cardRepository.deleteCardById(id);
    }
}
