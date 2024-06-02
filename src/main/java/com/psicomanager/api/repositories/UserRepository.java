package com.psicomanager.api.repositories;

import com.psicomanager.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
