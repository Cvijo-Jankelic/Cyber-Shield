package com.project.cybershield.services;

import com.project.cybershield.auth.PasswordHasher;
import com.project.cybershield.entities.User;
import com.project.cybershield.exceptions.AuthenticationException;
import com.project.cybershield.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * LOGIN SERVICE sa pepper brute-force demonstracijom
 */
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
    private final UserRepo userRepo;

    public LoginService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * Login user - demonstrira pepper provjeru
     */
    public User login(String username, String rawPassword)
            throws AuthenticationException, SQLException, IOException {

        logger.info("[LOGIN] Login attempt for user: {}", username);

        // Provjera da li user postoji
        Optional<User> userOpt = userRepo.findByUsername(username);

        if (userOpt.isEmpty()) {
            logger.warn("[LOGIN] ✗ User not found: {}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        User user = userOpt.get();

        // VERIFIKACIJA sa pepper brute-force (demonstracija!)
        boolean passwordValid;
        try {
            // Ovdje će se ispisati sve pepper provjere
            passwordValid = PasswordHasher.verify(rawPassword, username, user.getPassword());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("[LOGIN] Password verification error", e);
            throw new AuthenticationException("Authentication failed");
        }

        if (!passwordValid) {
            logger.warn("[LOGIN] ✗ Invalid password for user: {}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        logger.info("[LOGIN] ✓ User logged in successfully: {}", username);
        return user;
    }
}