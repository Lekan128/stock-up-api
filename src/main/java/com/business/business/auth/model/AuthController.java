package com.business.business.auth.model;

import com.business.business.auth.AuthResponse;
import com.business.business.auth.AuthService;
import com.business.business.user.Role;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("health")
    public String register(){
        return "Up, running and alive!";
    }

    @PostMapping("register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest registerRequest){
        return authService.register(registerRequest);
    }

    @PostMapping("login")
    public AuthResponse login(@Valid @RequestBody AuthRequest authRequest){
        return authService.login(authRequest);
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout(){
        authService.logout();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("refresh-refreshToke")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request){
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("in")
    public String login(){
        return "Working";
    }

}
