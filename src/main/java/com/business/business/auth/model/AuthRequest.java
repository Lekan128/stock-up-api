package com.business.business.auth.model;


import com.business.business.user.Role;
import jakarta.validation.constraints.NotBlank;

public class AuthRequest {
    @NotBlank
    public String email;
    @NotBlank
    public String password;

//    public Role role;
}
