package com.innowise.userservice.controller;

import com.innowise.userservice.dto.CardRequest;
import com.innowise.userservice.dto.CardResponse;
import com.innowise.userservice.service.CardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;

    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@RequestBody @Valid CardRequest cardRequest) {
        CardResponse cardResponse = cardService.createCard(cardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(cardResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable Long id) {
        CardResponse cardResponse = cardService.getCardById(id);
        return ResponseEntity.ok(cardResponse);
    }

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAllCards(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(defaultValue = "id") String sortBy,
                                                          @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CardResponse> cards = cardService.getAllCards(pageable);

        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardResponse> updateCard(@PathVariable Long id, @RequestBody @Valid CardRequest cardRequest) {
        CardResponse cardResponse = cardService.updateCard(id, cardRequest);
        return ResponseEntity.ok(cardResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCardById(id);
        return ResponseEntity.noContent().build();
    }
}
