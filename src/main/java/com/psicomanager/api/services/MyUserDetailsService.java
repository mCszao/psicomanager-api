package com.psicomanager.api.services;

import com.psicomanager.api.domain.user.User;
import com.psicomanager.api.domain.user.UserLoginDTO;
import com.psicomanager.api.domain.user.UserRegisterDTO;
import com.psicomanager.api.exceptions.user.DuplicateUserEntryException;
import com.psicomanager.api.exceptions.user.UserNotFoundException;
import com.psicomanager.api.infra.security.TokenService;
import com.psicomanager.api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepo;


    @Override
    public UserDetails loadUserByUsername(String input) {
        var userHasEmail = userRepo.findByEmail(input) != null;
        if(userHasEmail){
            return userRepo.findByEmail(input);
        };
        return userRepo.findByUsername(input).orElseThrow(() -> new UserNotFoundException("Usuário ou senha incorretos"));
    }

    public UserLoginDTO login(UserLoginDTO dto, User authUser){
        var token = tokenService.generateJWT(authUser);
        return new UserLoginDTO(dto.username(),token);
    }

    @Transactional
    public void register(UserRegisterDTO dto){
        if(!(userRepo.findByUsername(dto.username()).isEmpty())) throw new DuplicateUserEntryException("Esse usuário");
        if(userRepo.findByEmail(dto.email() == null ? "Não cadastrado" : dto.email()) != null) throw new DuplicateUserEntryException("Esse email");
        if(userRepo.findByPhone(dto.phone() == null ? "Não cadastrado" : dto.phone()) != null) throw new DuplicateUserEntryException("Esse telefone");
        String encryptedPass = new BCryptPasswordEncoder().encode(dto.password());
        userRepo.save(new User(dto, encryptedPass));

    }
}
