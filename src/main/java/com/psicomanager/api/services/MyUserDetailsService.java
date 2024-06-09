package com.psicomanager.api.services;

import com.psicomanager.api.domain.user.model.User;
import com.psicomanager.api.domain.user.dto.UserLoginDTO;
import com.psicomanager.api.domain.user.dto.UserRegisterDTO;
import com.psicomanager.api.domain.user.exception.DuplicateUserEntryException;
import com.psicomanager.api.domain.user.exception.UserNotFoundException;
import com.psicomanager.api.infra.security.TokenService;
import com.psicomanager.api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
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
        log.info("Retornando usuário para login");
        return new UserLoginDTO(dto.username(),token);
    }

    @Transactional
    public void register(UserRegisterDTO dto){
        log.info("Validando informações enviadas");
        if(!(userRepo.findByUsername(dto.username()).isEmpty())) throw new DuplicateUserEntryException("Esse usuário");
        if(userRepo.findByEmail(dto.email() == null ? "Não cadastrado" : dto.email()) != null) throw new DuplicateUserEntryException("Esse email");
        if(userRepo.findByPhone(dto.phone() == null ? "Não cadastrado" : dto.phone()) != null) throw new DuplicateUserEntryException("Esse telefone");
        String encryptedPass = new BCryptPasswordEncoder().encode(dto.password());
        log.info("Salvando novo usuário");
        userRepo.save(new User(dto, encryptedPass));

    }
}
