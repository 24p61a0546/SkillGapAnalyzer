package com.skillgap.dao;

import com.skillgap.model.User;
import com.skillgap.util.DBConnection;

import java.sql.*;

public class UserDAO {

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT u.id, u.full_name, u.email, u.password_hash, u.department, u.year_of_study, "
                   + "u.target_role_id, r.title AS target_role_title, u.created_at "
                   + "FROM users u "
                   + "LEFT JOIN job_roles r ON u.target_role_id = r.id "
                   + "WHERE LOWER(u.email) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT u.id, u.full_name, u.email, u.password_hash, u.department, u.year_of_study, "
                   + "u.target_role_id, r.title AS target_role_title, u.created_at "
                   + "FROM users u "
                   + "LEFT JOIN job_roles r ON u.target_role_id = r.id "
                   + "WHERE u.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    public boolean create(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password_hash, department, year_of_study, target_role_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail().toLowerCase().trim());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getDepartment() != null ? user.getDepartment() : "Computer Science");
            ps.setString(5, user.getYearOfStudy() != null ? user.getYearOfStudy() : "3rd Year");
            if (user.getTargetRoleId() != null && user.getTargetRoleId() > 0) {
                ps.setInt(6, user.getTargetRoleId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateTargetRole(int userId, int roleId) throws SQLException {
        String sql = "UPDATE users SET target_role_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, department = ?, year_of_study = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getDepartment());
            ps.setString(3, user.getYearOfStudy());
            ps.setInt(4, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Obtains or creates a guest user profile for frictionless analysis without passwords or logins.
     * Persists to the users table via JDBC when available, with a resilient fallback.
     */
    public User getOrCreateGuestUser(String fullName, String email, int targetRoleId) {
        String cleanName = (fullName != null && !fullName.trim().isEmpty()) ? fullName.trim() : "Student Analyst";
        String cleanEmail = (email != null && !email.trim().isEmpty())
                ? email.trim().toLowerCase()
                : "guest_" + System.currentTimeMillis() + "@skillgap.local";
        int roleId = (targetRoleId > 0) ? targetRoleId : 1;

        try {
            User existing = findByEmail(cleanEmail);
            if (existing != null) {
                existing.setFullName(cleanName);
                existing.setTargetRoleId(roleId);
                updateProfile(existing);
                updateTargetRole(existing.getId(), roleId);
                return existing;
            }

            User newUser = new User();
            newUser.setFullName(cleanName);
            newUser.setEmail(cleanEmail);
            newUser.setPasswordHash("GUEST_ACCESS");
            newUser.setDepartment("Computer Science & Engineering");
            newUser.setYearOfStudy("3rd Year");
            newUser.setTargetRoleId(roleId);

            boolean created = create(newUser);
            if (created && newUser.getId() > 0) {
                return newUser;
            }
        } catch (Exception e) {
            System.err.println("[UserDAO] Notice: Using in-memory guest user profile (" + e.getMessage() + ")");
        }

        // Resilient fallback (e.g. if DB is temporarily offline or in transition)
        User fallbackUser = new User();
        fallbackUser.setId(1);
        fallbackUser.setFullName(cleanName);
        fallbackUser.setEmail(cleanEmail);
        fallbackUser.setDepartment("Computer Science & Engineering");
        fallbackUser.setYearOfStudy("3rd Year");
        fallbackUser.setTargetRoleId(roleId);
        return fallbackUser;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setDepartment(rs.getString("department"));
        user.setYearOfStudy(rs.getString("year_of_study"));
        int targetRoleId = rs.getInt("target_role_id");
        if (!rs.wasNull()) {
            user.setTargetRoleId(targetRoleId);
        }
        user.setTargetRoleTitle(rs.getString("target_role_title"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
