package com.example.CollabAuth.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface UserRepo extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Boolean existsByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Boolean existsByEmail(@Param("email") String email);
}
