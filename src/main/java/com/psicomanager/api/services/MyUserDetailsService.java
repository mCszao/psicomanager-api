package com.psicomanager.api.services;

import com.psicomanager.api.domain.user.dto.ResponseLoginDTO;
import com.psicomanager.api.domain.user.mapper.UserMapper;
import com.psicomanager.api.repositories.user.model.User;
import com.psicomanager.api.domain.user.dto.UserRegisterDTO;
import com.psicomanager.api.domain.user.exception.DuplicateUserEntryException;
import com.psicomanager.api.domain.user.exception.UserNotFoundException;
import com.psicomanager.api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String input) {
        var userHasEmail = userRepo.findByEmail(input) != null;
        if (userHasEmail) {
            return userRepo.findByEmail(input);
        }
        return userRepo.findByUsername(input).orElseThrow(() -> new UserNotFoundException("Usuário ou senha incorretos"));
    }

    /**
     * Generates an access token for the authenticated user and returns the login response DTO.
     * The access token itself is set as an HttpOnly cookie by the controller.
     *
     * @param user the authenticated user entity
     * @return {@link ResponseLoginDTO} containing only the username
     */
    public ResponseLoginDTO login(User user) {
        log.info("Retornando usuário para login: " + user.getUsername());
        return new ResponseLoginDTO(user.getUsername());
    }

    @Transactional
    public void register(UserRegisterDTO dto) {
        log.info("Validando informações enviadas");
        if (userRepo.findByUsername(dto.username()).isPresent()) throw new DuplicateUserEntryException("Esse usuário");
        if (userRepo.findByEmail(dto.email() == null ? "Não cadastrado" : dto.email()) != null)
            throw new DuplicateUserEntryException("Esse email");
        if (userRepo.findByPhone(dto.phone() == null ? "Não cadastrado" : dto.phone()) != null)
            throw new DuplicateUserEntryException("Esse telefone");
        log.info("Salvando novo usuário");
        User user = mapper.dtoToEntity(dto);
        userRepo.save(user);
    }
}
