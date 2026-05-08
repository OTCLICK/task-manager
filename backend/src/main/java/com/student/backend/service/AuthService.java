package com.student.backend.service;

import com.student.backend.dto.AuthRequest;
import com.student.backend.dto.AuthResponse;
import com.student.backend.dto.UserCreateRequest;
import com.student.backend.exception.ValidationException;
import com.student.backend.model.FullName;
import com.student.backend.model.User;
import com.student.backend.repository.UserRepository;
import com.student.backend.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponse register(UserCreateRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = new User(
                request.email(),
                encodedPassword,
                new FullName(
                        request.fullName().name(),
                        request.fullName().surname(),
                        request.fullName().patronymic()
                        )
//                request.role()
        );

        userRepository.save(user);

        String token = jwtUtils.generateToken(
                user.getId(),
                user.getEmail()
//                user.getRole().name()
        );

        return new AuthResponse(token);
    }

    public AuthResponse authenticate(AuthRequest request) {
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ValidationException("Неверный email или пароль"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ValidationException("Неверный email или пароль");
        }

        String token = jwtUtils.generateToken(
                user.getId(),
                user.getEmail()
//                user.getRole().name()
        );

        return new AuthResponse(token);
    }
}
