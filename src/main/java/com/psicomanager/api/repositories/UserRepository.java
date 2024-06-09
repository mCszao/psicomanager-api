package com.psicomanager.api.repositories;

import com.psicomanager.api.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<UserDetails> findByUsername (String username);
    User findByEmail(String email);
    User findByPhone(String phone);

}
