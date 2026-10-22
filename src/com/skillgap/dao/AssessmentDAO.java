package com.skillgap.dao;

import com.skillgap.model.Assessment;
import com.skillgap.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssessmentDAO {

    public List<Assessment> getAssessmentsByUserId(int userId) throws SQLException {
        List<Assessment> list = new ArrayList<>();
        String sql = "SELECT a.id, a.user_id, a.skill_id, s.name AS skill_name, s.category, "
                   + "a.proficiency_level, a.notes, a.assessed_at "
                   + "FROM student_assessments a "
                   + "JOIN skills s ON a.skill_id = s.id "
                   + "WHERE a.user_id = ? "
                   + "ORDER BY s.category, s.name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Assessment(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("skill_id"),
                            rs.getString("skill_name"),
                            rs.getString("category"),
                            rs.getInt("proficiency_level"),
                            rs.getString("notes"),
                            rs.getTimestamp("assessed_at")
                    ));
                }
            }
        }
        return list;
    }

    public Map<Integer, Integer> getSkillProficiencyMap(int userId) throws SQLException {
        Map<Integer, Integer> map = new HashMap<>();
        String sql = "SELECT skill_id, proficiency_level FROM student_assessments WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("skill_id"), rs.getInt("proficiency_level"));
                }
            }
        }
        return map;
    }

    public boolean upsertAssessment(int userId, int skillId, int proficiencyLevel, String notes) throws SQLException {
        String sql = "INSERT INTO student_assessments (user_id, skill_id, proficiency_level, notes) "
                   + "VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE proficiency_level = VALUES(proficiency_level), notes = VALUES(notes), assessed_at = CURRENT_TIMESTAMP";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, skillId);
            ps.setInt(3, proficiencyLevel);
            ps.setString(4, notes);
            return ps.executeUpdate() > 0;
        }
    }

    public int saveBulkAssessments(int userId, Map<Integer, Integer> ratings, Map<Integer, String> notes) throws SQLException {
        String sql = "INSERT INTO student_assessments (user_id, skill_id, proficiency_level, notes) "
                   + "VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE proficiency_level = VALUES(proficiency_level), notes = VALUES(notes), assessed_at = CURRENT_TIMESTAMP";

        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            try {
                for (Map.Entry<Integer, Integer> entry : ratings.entrySet()) {
                    int skillId = entry.getKey();
                    int level = entry.getValue();
                    String note = (notes != null && notes.containsKey(skillId)) ? notes.get(skillId) : null;

                    ps.setInt(1, userId);
                    ps.setInt(2, skillId);
                    ps.setInt(3, level);
                    ps.setString(4, note);
                    ps.addBatch();
                }
                int[] results = ps.executeBatch();
                conn.commit();
                count = results.length;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return count;
    }

    public int getAssessmentCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM student_assessments WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
