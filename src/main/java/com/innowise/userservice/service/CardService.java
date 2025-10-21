package com.innowise.userservice.service;

import com.innowise.userservice.dto.CardRequest;
import com.innowise.userservice.dto.CardResponse;
import com.innowise.userservice.model.*;
import com.innowise.userservice.mapper.CardMapper;
import com.innowise.userservice.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

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

    public CardResponse createCard(CardRequest cardRequest) {
        User user = userRepository.findById(cardRequest.getUserId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        Card card = cardMapper.toEntity(cardRequest);
        card.setUser(user);
        Card savedCard = cardRepository.save(card);

        return cardMapper.toDto(savedCard);
    }

    public CardResponse getCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        return cardMapper.toDto(card);
    }

    public Page<CardResponse> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(cardMapper::toDto);
    }

    @Transactional
    public CardResponse updateCard(Long id, CardRequest cardRequest) {
        Card currentCard = cardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        if (!currentCard.getUser().getId().equals(cardRequest.getUserId())) {
            User user = userRepository.findById(cardRequest.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

            currentCard.setUser(user);
        }

        cardMapper.updateCardFromRequest(cardRequest, currentCard);
        Card updatedCard = cardRepository.save(currentCard);

        return cardMapper.toDto(updatedCard);
    }

    @Transactional
    public void deleteCardById(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        cardRepository.deleteUserById(id);
    }
}
