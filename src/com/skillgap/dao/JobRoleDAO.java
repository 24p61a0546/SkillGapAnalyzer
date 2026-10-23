package com.skillgap.dao;

import com.skillgap.model.JobRole;
import com.skillgap.model.RoleSkill;
import com.skillgap.util.DBConnection;
import com.skillgap.util.DemoCatalog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobRoleDAO {

    public List<JobRole> getAllRoles() throws SQLException {
        List<JobRole> roles = new ArrayList<>();
        String sql = "SELECT id, title, slug, category, description, icon, avg_salary, demand_level, experience_level FROM job_roles ORDER BY id";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        roles.add(mapRole(rs));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[JobRoleDAO] Database notice: " + e.getMessage());
        }

        // Resilient safe fallback if DB empty or unavailable
        if (roles.isEmpty()) {
            return DemoCatalog.getAllRoles();
        }

        // Attach skills for each role
        for (JobRole r : roles) {
            r.setRequiredSkills(getSkillsForRole(r.getId()));
        }
        return roles;
    }

    public JobRole getRoleById(int id) throws SQLException {
        String sql = "SELECT id, title, slug, category, description, icon, avg_salary, demand_level, experience_level FROM job_roles WHERE id = ?";

        JobRole role = null;
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            role = mapRole(rs);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[JobRoleDAO] Database notice in getRoleById: " + e.getMessage());
        }

        if (role == null) {
            return DemoCatalog.getRoleById(id);
        }

        role.setRequiredSkills(getSkillsForRole(role.getId()));
        return role;
    }

    public List<RoleSkill> getSkillsForRole(int roleId) {
        List<RoleSkill> list = new ArrayList<>();
        String sql = "SELECT rs.id, rs.role_id, rs.skill_id, s.name AS skill_name, s.category, "
                   + "rs.required_proficiency, rs.importance_level "
                   + "FROM role_skills rs "
                   + "JOIN skills s ON rs.skill_id = s.id "
                   + "WHERE rs.role_id = ? "
                   + "ORDER BY rs.importance_level DESC, s.name";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, roleId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            list.add(new RoleSkill(
                                    rs.getInt("role_id"),
                                    rs.getInt("skill_id"),
                                    rs.getString("skill_name"),
                                    rs.getString("category"),
                                    rs.getInt("required_proficiency"),
                                    rs.getString("importance_level")
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[JobRoleDAO] DB notice getSkillsForRole: " + e.getMessage());
        }

        if (list.isEmpty()) {
            return DemoCatalog.getSkillsForRole(roleId);
        }
        return list;
    }

    private JobRole mapRole(ResultSet rs) throws SQLException {
        return new JobRole(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("slug"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("icon"),
                rs.getString("avg_salary"),
                rs.getString("demand_level"),
                rs.getString("experience_level")
        );
    }
}
