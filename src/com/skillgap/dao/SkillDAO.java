package com.skillgap.dao;

import com.skillgap.model.Skill;
import com.skillgap.util.DBConnection;
import com.skillgap.util.DemoCatalog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SkillDAO {

    public List<Skill> getAllSkills() throws SQLException {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT id, name, category, description, max_rating FROM skills ORDER BY category, name";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapSkill(rs));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SkillDAO] Database notice: " + e.getMessage());
        }

        if (list.isEmpty()) {
            return DemoCatalog.getAllSkills();
        }
        return list;
    }

    public Skill getSkillById(int id) throws SQLException {
        String sql = "SELECT id, name, category, description, max_rating FROM skills WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return mapSkill(rs);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SkillDAO] Database notice in getSkillById: " + e.getMessage());
        }
        return DemoCatalog.getSkillById(id);
    }

    public List<Skill> getSkillsByCategory(String category) throws SQLException {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT id, name, category, description, max_rating FROM skills WHERE category = ? ORDER BY name";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, category);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(mapSkill(rs));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SkillDAO] Database notice in getSkillsByCategory: " + e.getMessage());
        }

        if (list.isEmpty()) {
            for (Skill s : DemoCatalog.getAllSkills()) {
                if (s.getCategory().equalsIgnoreCase(category)) {
                    list.add(s);
                }
            }
        }
        return list;
    }

    public List<Skill> getSkillsForRole(int roleId) throws SQLException {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT s.id, s.name, s.category, s.description, s.max_rating "
                   + "FROM skills s "
                   + "JOIN role_skills rs ON s.id = rs.skill_id "
                   + "WHERE rs.role_id = ? "
                   + "ORDER BY rs.importance_level DESC, s.name";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, roleId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(mapSkill(rs));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SkillDAO] Database notice in getSkillsForRole: " + e.getMessage());
        }

        if (list.isEmpty()) {
            var roleSkills = DemoCatalog.getSkillsForRole(roleId);
            for (var rs : roleSkills) {
                Skill s = DemoCatalog.getSkillById(rs.getSkillId());
                if (s != null) list.add(s);
            }
        }
        return list;
    }

    public List<String> getAllCategories() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM skills ORDER BY category";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(rs.getString("category"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SkillDAO] Database notice in getAllCategories: " + e.getMessage());
        }

        if (list.isEmpty()) {
            for (Skill s : DemoCatalog.getAllSkills()) {
                if (!list.contains(s.getCategory())) {
                    list.add(s.getCategory());
                }
            }
        }
        return list;
    }

    private Skill mapSkill(ResultSet rs) throws SQLException {
        return new Skill(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getInt("max_rating")
        );
    }
}
