package ru.urfu.slivky.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.slivky.exception.BadRequestException;
import ru.urfu.slivky.model.User;
import ru.urfu.slivky.model.UserRole;
import ru.urfu.slivky.repository.UserRepository;
import ru.urfu.slivky.security.JwtService;
import ru.urfu.slivky.web.dto.AuthResponse;
import ru.urfu.slivky.web.dto.LoginRequest;
import ru.urfu.slivky.web.dto.RegisterRequest;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (req.role() == UserRole.ADMIN) {
            throw new BadRequestException("Cannot self-register as ADMIN");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new BadRequestException("Email already registered");
        }
        User user = new User();
        user.setEmail(req.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setFullName(req.fullName().trim());
        user.setRole(req.role());

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole(), user.getFullName());
    }
}
