package com.psicomanager.api.services;

import com.psicomanager.api.domain.User;
import com.psicomanager.api.dtos.UserRegisterDTO;
import com.psicomanager.api.infra.security.SecurityConfig;
import com.psicomanager.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private UserRepository userRepo;

    public boolean save(UserRegisterDTO dto){
        if(dto.username() != null && dto.password() != null){
            String encryptedPass = securityConfig.getPasswordEncoder().encode(dto.password());
            userRepo.save(new User(dto, encryptedPass));
            return true;
        }
        return false;
    }
}
