package com.business.business.auth.model;

import com.business.business.user.Role;
import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {
    @NotBlank
    public String firstName;
    @NotBlank
    public String lastName;
    @NotBlank
    public String email;
    public String phoneNumber;
    @NotBlank
    public String password;
    @NotBlank
    public String storeName;
    public String storeAddress;
//    @NotBlank
    public Role role;
}
