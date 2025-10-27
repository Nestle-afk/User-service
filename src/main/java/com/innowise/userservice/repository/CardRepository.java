package com.innowise.userservice.repository;

import com.innowise.userservice.model.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>  {
    @NonNull
    Optional<Card> findById( @NonNull Long id);

    @NonNull
    Page<Card> findAll( @NonNull Pageable pageable);

    @Modifying
    @Query("UPDATE Card c SET c.expirationDate = :#{#card.expirationDate}, c.holder = :#{#card.holder}, c.number = :#{#card.number}, c.user = :#{#card.user} WHERE c.id = :#{#card.id}")
    void updateCard(@Param("card") Card card);

    @Modifying
    @Query("UPDATE Card c SET c.expirationDate = :expirationDate, c.holder = :holder, c.number = :number WHERE c.id = :id")
    void updateCard(@Param("id") Long id,
                    @Param("holder") String holder,
                    @Param("number") String number,
                    @Param("expirationDate") java.time.LocalDate expirationDate);

    @Modifying
    @Query(value = "DELETE FROM card_info WHERE id = :id", nativeQuery = true)
    void deleteCardById(@Param("id") Long id);

    @NonNull
    boolean existsById(@NonNull Long id);
}
