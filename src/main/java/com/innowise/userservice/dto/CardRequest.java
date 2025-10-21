package com.innowise.userservice.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CardRequest {
    @NotBlank
    private long userId;

    @NotBlank
    @Size(max = 19)
    private String number;

    @NotBlank
    @Size(max = 255)
    private String holder;

    @NotBlank
    @Future
    private LocalDate expirationDate;

    public CardRequest() {}

    public CardRequest(Long userId, String number, String holder, LocalDate expirationDate) {
        this.userId = userId;
        this.number = number;
        this.holder = holder;
        this.expirationDate = expirationDate;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getHolder() { return holder; }
    public void setHolder(String holder) { this.holder = holder; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
}
