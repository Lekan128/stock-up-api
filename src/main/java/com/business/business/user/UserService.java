package com.business.business.user;

import com.business.business.auth.model.RegisterRequest;
import com.business.business.exception.UserAlreadyExistsException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User loadUserByUsername(String username){
        return userRepository.findByEmail(username)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));
    }

    public User createUser(RegisterRequest registerRequest, String password) {
        if (userRepository.findByEmail(registerRequest.email).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }
        if (userRepository.findByEmail(registerRequest.email).isPresent()) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        User user = User.builder()
                .fistName(registerRequest.firstName)
                .lastName(registerRequest.lastName)
                .email(registerRequest.email)
                .phoneNumber(registerRequest.phoneNumber)
                .password(password)
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
}
