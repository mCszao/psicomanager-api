package com.psicomanager.api.domain.user.model;

import com.psicomanager.api.domain.user.dto.UserRegisterDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity(name = "users")
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "USERNAME"),
        @UniqueConstraint(columnNames = "PHONE")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {
    @Id()
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull
    @Size(max = 255)
    @Column(name = "USERNAME", nullable = false, unique = true, length = 255)
    private String username;

    @Size(max = 255)
    @Email
    @Column(name = "EMAIL", length = 255)
    private String email;

    @NotNull
    @Size(max = 255)
    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Size(max = 255)
    @Column(name = "PHONE", unique = true, length = 255)
    private String phone;

    public User(UserRegisterDTO dto, String encryptedPassword) {
        this.username = dto.username();
        this.password = encryptedPassword;
        this.phone = dto.phone();
        this.email = dto.email();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername(){
        return this.username;
    }
    @Override
    public String getPassword(){
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
