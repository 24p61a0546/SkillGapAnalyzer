package com.skillgap.service;

import com.skillgap.dao.UserDAO;
import com.skillgap.model.User;
import com.skillgap.util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User authenticate(String email, String rawPassword) throws SQLException {
        if (email == null || rawPassword == null) {
            return null;
        }

        User user = userDAO.findByEmail(email.trim());
        if (user != null && PasswordUtil.verifyPassword(rawPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public User register(String fullName, String email, String rawPassword, String department, String yearOfStudy, Integer targetRoleId) throws SQLException, IllegalArgumentException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        User existing = userDAO.findByEmail(email.trim());
        if (existing != null) {
            throw new IllegalArgumentException("Email is already registered. Please login.");
        }

        String hashedPassword = PasswordUtil.hashPassword(rawPassword);
        User user = new User(0, fullName.trim(), email.trim().toLowerCase(), hashedPassword, department, yearOfStudy, targetRoleId);
        boolean created = userDAO.create(user);
        if (created) {
            return userDAO.findById(user.getId());
        }
        return null;
    }
}
