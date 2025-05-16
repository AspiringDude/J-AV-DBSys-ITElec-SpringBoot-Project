package com.example.pos.repository;

import com.example.pos.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    // Add this method to check if a user with the given username and password exists
    boolean existsByUsernameAndPassword(String username, String password);

}
