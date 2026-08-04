package com.java_refresh.day4_demo.controller;

import com.java_refresh.day4_demo.dto.LoginRequestDto;
import com.java_refresh.day4_demo.dto.LoginResponseDto;
import com.java_refresh.day4_demo.dto.RegisterRequestDto;
import com.java_refresh.day4_demo.dto.RegisterResponseDto;
import com.java_refresh.day4_demo.exceptions.UsernameAlreadyExistsException;
import com.java_refresh.day4_demo.model.User;
import com.java_refresh.day4_demo.repository.UserRepository;
import com.java_refresh.day4_demo.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequest) {

        if (userRepository.findByUsername(registerRequest.username()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists, please use another username");
        }

        User newUser = new User(registerRequest.username(), passwordEncoder.encode(registerRequest.password()), "USER");
        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponseDto("Register Successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String jwt = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok(new LoginResponseDto(jwt));
    }
}
