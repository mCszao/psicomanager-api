package com.psicomanager.api.domain.user.mapper;

import com.psicomanager.api.domain.user.dto.UserRegisterDTO;
import com.psicomanager.api.repositories.user.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User dtoToEntity(UserRegisterDTO dto) {
        User entity = new User();
        entity.setUsername(dto.username());
        String encryptedPass = new BCryptPasswordEncoder().encode(dto.password());
        entity.setPassword(encryptedPass);
        entity.setPhone(dto.phone());
        entity.setEmail(dto.email());

        return entity;
    }
}
