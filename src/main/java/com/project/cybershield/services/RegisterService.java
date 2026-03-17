package com.project.cybershield.services;

import com.project.cybershield.auth.PasswordHasher;
import com.project.cybershield.builders.UserBuilder;
import com.project.cybershield.entities.User;
import com.project.cybershield.enums.Role;
import com.project.cybershield.exceptions.RegistrationException;
import com.project.cybershield.exceptions.UsernameAlreadyExistsException;
import com.project.cybershield.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class RegisterService {

    private static final Logger logger = LoggerFactory.getLogger(RegisterService.class);
    private  UserRepo userRepo;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    public RegisterService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }
    public RegisterService() {}

    /**
     * Register new user sa sigurnosnim validacijama
     */
    public User register(String username, String rawPassword, String confirm, Role role)
            throws RegistrationException, SQLException, IOException {

        logger.info("[REGISTER] Attempting to register user: {}", username);

        validateUsername(username);
        validatePassword(rawPassword, confirm);

        if (userRepo.existsByUsername(username)) {
            logger.warn("[REGISTER] Username already exists: {}", username);
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        // HASH PASSWORD sa deterministički sol (iz username-a) + papar
        String passwordHash;
        try {
            passwordHash = PasswordHasher.hash(rawPassword, username);
            logger.info("[REGISTER] Password hashed successfully for user: {}", username);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("[REGISTER] Password hashing failed", e);
            throw new RegistrationException("Password hashing failed: " + e.getMessage());
        }

        User userToInsert = new UserBuilder()
                .setUsername(username)
                .setPassword(passwordHash)
                .setRole(role)
                .createUser();

        // Insert u bazu
        long newId = userRepo.insert(userToInsert);
        logger.info("[REGISTER] ✓ User registered successfully with ID: {}", newId);

        return new User(newId, username, passwordHash, role, LocalDateTime.now());
    }

    private void validateUsername(String username) throws RegistrationException {
        if (username == null || username.isEmpty()) {
            throw new RegistrationException("Username cannot be empty");
        }
        if (username.length() < 3) {
            throw new RegistrationException("Username must be at least 3 characters");
        }
    }

    private void validatePassword(String password, String confirm)
            throws RegistrationException {

        if (password == null || password.isEmpty()) {
            throw new RegistrationException("Password cannot be empty");
        }

        if (password.length() < 8) {
            throw new RegistrationException("Password must be at least 8 characters");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new RegistrationException(
                    "Password must contain: uppercase, lowercase, digit, and special character"
            );
        }

        if (!password.equals(confirm)) {
            throw new RegistrationException("Passwords do not match");
        }
    }
}