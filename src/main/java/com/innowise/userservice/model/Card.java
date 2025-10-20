package com.innowise.userservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "card_info", indexes = {
        @Index(name = "idx_card_info_user_id", columnList = "user_id"),
        @Index(name = "idx_card_info_number", columnList = "number")
})
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card_info_user_id"))
    private User user;

    @Column(name = "number", nullable = false, length = 19)
    private String number;

    @Column(name = "holder", nullable = false, length = 255)
    private String holder;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    public Card() {}

    public Card(User user, String number, String holder, LocalDate expirationDate) {
        this.user = user;
        this.number = number;
        this.holder = holder;
        this.expirationDate = expirationDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getHolder() { return holder; }
    public void setHolder(String holder) { this.holder = holder; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
}
