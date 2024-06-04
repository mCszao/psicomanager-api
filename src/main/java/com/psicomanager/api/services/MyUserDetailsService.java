package com.psicomanager.api.services;

import com.psicomanager.api.domain.user.User;
import com.psicomanager.api.domain.user.UserRegisterDTO;
import com.psicomanager.api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("Username not found"));
    }


    public boolean save(UserRegisterDTO dto){
        var differentUsername = userRepo.findByUsername(dto.username()).isEmpty();
        if(differentUsername){
            String encryptedPass = new BCryptPasswordEncoder().encode(dto.password());
            userRepo.save(new User(dto, encryptedPass));
            return true;
        }
        return false;
    }
}
