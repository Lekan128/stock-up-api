package com.business.business.auth;

import com.business.business.auth.model.AuthRequest;
import com.business.business.auth.model.RefreshTokenRequest;
import com.business.business.auth.model.RegisterRequest;
import com.business.business.config.JwtService;
import com.business.business.exception.AuthenticationException;
import com.business.business.store.Store;
import com.business.business.store.StoreDto;
import com.business.business.store.StoreService;
import com.business.business.token.Token;
import com.business.business.token.TokenRepository;
import com.business.business.user.Role;
import com.business.business.user.User;
import com.business.business.user.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;
    private final StoreService storeService;
    private final TokenRepository tokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest){
        String password = passwordEncoder.encode(registerRequest.password);
        User user = userService.createUser(registerRequest, password);
        var store = storeService.createStore(new StoreDto(registerRequest.storeName, registerRequest.storeAddress));
        userService.updateUserStore(store, user);
        return generateTokensSaveTokensAndDeleteExpiredTokens(user);
    }
    AuthenticationManager authenticationManager;
    public AuthResponse login(AuthRequest authRequest){
        User user = userService.loadUserByUsername(authRequest.email);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        authRequest.email,
                        authRequest.password,
                        Role.getGrantedAuthorities(user.getRole())
                );
        authenticationManager.authenticate(authenticationToken);
        return generateTokensSaveTokensAndDeleteExpiredTokens(user);
    }

    public void logout(){
        User user = getCurrentAuthenticatedUser();
        tokenRepository.deleteAllByUserId(user.getId());
    }

    public static User getCurrentAuthenticatedUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static Store getCurrentAuthenticatedUserStore(){
        return getCurrentAuthenticatedUser().getStore();
    }

    private AuthResponse generateTokensSaveTokensAndDeleteExpiredTokens(User user){
        String accessToken = jwtService.generateAccessToken(user);

        deleteAllUserExpiredTokensFor(user);
        tokenRepository.save(Token.newInstance(accessToken, user));

        return AuthResponse.getInstance(
                accessToken,
                jwtService.generateRefreshToken(user)
        );
    }

    public void deleteAllUserExpiredTokensFor(User user) {
        List<Token> allTokenByUser = tokenRepository.findAllTokenByUser(user.getId());
        List<Token> expiredTokens = allTokenByUser.stream().filter(token -> {
            try{
                return jwtService.isTokenExpired(token.value);
            } catch (ExpiredJwtException e){
                return true;
            }
        }).toList();
        tokenRepository.deleteAll(expiredTokens);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.refreshToke());
        User user = userService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(request.refreshToke(), user)){
            throw new AuthenticationException("Bad refreshToke");
        }
        return generateTokensSaveTokensAndDeleteExpiredTokens(user);
    }
}
