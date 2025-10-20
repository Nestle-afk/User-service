package com.innowise.userservice.repository;

import com.innowise.userservice.model.User;
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
public interface UserRepository extends JpaRepository<User, Long>  {
    @NonNull
    Optional<User> findById( @NonNull Long id);

    @NonNull
    Page<User> findAll( @NonNull Pageable pageable);

    Optional<User> findByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.name = :name, u.surname = :surname, u.birthDate = :birthDate, u.email = :email WHERE u.id = :id")
    void updateUser(@Param("id") Long id,
                    @Param("name") String name,
                    @Param("surname") String surname,
                    @Param("birthDate") java.time.LocalDate birthDate,
                    @Param("email") String email);

    @Modifying
    @Query(value = "DELETE FROM users WHERE id = :id", nativeQuery = true)
    void deleteUserById(@Param("id") Long id);
}
