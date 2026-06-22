package com.example.mentorisebackend.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserPrincipal implements UserDetails {

    private final Long   userId;
    private final String email;
    private final String passwordHash;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long userId, String email, String passwordHash,
                         Collection<? extends GrantedAuthority> authorities) {
        this.userId       = userId;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.authorities  = authorities;
    }

    public Long getUserId() { return userId; }

    @Override public String getUsername()  { return email; }
    @Override public String getPassword()  { return passwordHash; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
