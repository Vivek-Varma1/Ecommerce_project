package com.ecommerce.project.security.jwt.service;

import com.ecommerce.project.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomUserDetailsImpl implements UserDetails {

    private static final long serialVersionUID=1L;
    @Getter

    private Long id;


    private String username;
    @Getter
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority>authorities;

    public CustomUserDetailsImpl(Long id, @NotBlank @Size(max = 20) String username, @NotBlank @Size(max = 120) String password, @NotBlank @Size(max = 50) @Email String email, List<GrantedAuthority> authorities) {
        this.id=id;
        this.username=username;
        this.password=password;
        this.email=email;
        this.authorities=authorities;
    }

    public static CustomUserDetailsImpl build(User user){
        List<GrantedAuthority>authorities=user.getRoles().stream()
                .map(role->new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toList());

        return new CustomUserDetailsImpl(
                user.getUserId(),
                user.getUserName(),
                user.getPassword(),
                user.getEmail(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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

    @Override
    public boolean equals(Object o){
        if (this==o)
                return true;
        if(o==null || getClass()!=o.getClass())
                return false;
        CustomUserDetailsImpl user=(CustomUserDetailsImpl) o;
        return Objects.equals(id,user.id);
    }

}
