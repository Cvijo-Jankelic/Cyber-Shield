package com.project.cybershield.repository;

import com.project.cybershield.builders.UserBuilder;
import com.project.cybershield.db.DatabaseConfig;
import com.project.cybershield.entities.User;
import com.project.cybershield.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepo {

    private static final Logger logger = LoggerFactory.getLogger(UserRepo.class);

    public long insert(User user) throws SQLException, IOException {
        String sql = "INSERT INTO users (username, password_hash, role, created_at) " +
                "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection con = DatabaseConfig.connectionToDatabase();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole().name());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1); // ili rs.getLong("id")
                }
                throw new SQLException("No id returned from INSERT ... RETURNING.");
            }
        }catch (SQLException ex){
            logger.error("Error with inserting new user into database", ex);
            throw ex;
        }
    }

    public List<User> getAll() {
        String sql = "SELECT id, username, password_hash, role, created_at FROM users";
        List<User> list = new ArrayList<>();

        try (Connection con = DatabaseConfig.connectionToDatabase();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new UserBuilder()
                        .setId(rs.getLong("id"))
                        .setUsername(rs.getString("username"))
                        .setPassword(rs.getString("password_hash"))
                        .setRole(Role.valueOf(rs.getString("role")))
                        .createUser();
                list.add(user);
            }

        } catch (SQLException | IOException ex) {
            logger.info("Error with connection check log file");
            logger.error("Query failed for User", ex);
        }

        return list;
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection connection = DatabaseConfig.connectionToDatabase();
             PreparedStatement ps = connection.prepareStatement(sql);) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException | IOException ex) {
            logger.error("Error with fetching email from database", ex);
            throw new SQLException(ex);
        }

    }

    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT 1 from users WHERE username = ? LIMIT 1";

        try (Connection connection = DatabaseConfig.connectionToDatabase();
             PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (IOException ex) {
            logger.info("Error with fetching username from database", ex.getCause());
            logger.error(ex.getMessage());
            throw new RuntimeException(ex);
        }

    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = """
    SELECT id, email, username, password_hash, role
    FROM users
    WHERE email = ?
  """;

        try (Connection connection = DatabaseConfig.connectionToDatabase();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                return Optional.of(new User(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        Role.valueOf(rs.getString("role")),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException, IOException {
        String sql = "SELECT id, username, password_hash, role, created_at FROM users WHERE username = ?";
        try (Connection con = DatabaseConfig.connectionToDatabase();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                long id = rs.getLong("userId");
                String usernameTmp = rs.getString("username");
                String hash = rs.getString("passwordHash");
                Role role = Role.valueOf(rs.getString("role"));
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

                User u = new User(id, usernameTmp, hash, role, createdAt);
                return Optional.of(u);
            }
        }
    }
}
