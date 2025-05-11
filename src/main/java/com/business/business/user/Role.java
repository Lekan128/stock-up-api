package com.business.business.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public enum Role {
    USER,
    ADMIN;

    public static List<GrantedAuthority> getGrantedAuthorities(Role role){
        return List.of(new SimpleGrantedAuthority("Role_" + role.name()));
    }
}
