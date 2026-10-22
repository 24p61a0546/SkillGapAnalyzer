package com.skillgap.dao;

import com.skillgap.model.JobRole;
import com.skillgap.model.RoleSkill;
import com.skillgap.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobRoleDAO {

    public List<JobRole> getAllRoles() throws SQLException {
        List<JobRole> roles = new ArrayList<>();
        String sql = "SELECT id, title, slug, category, description, icon, avg_salary, demand_level, experience_level FROM job_roles ORDER BY title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                JobRole role = mapRole(rs);
                roles.add(role);
            }
        }

        // Attach skills for each role
        for (JobRole r : roles) {
            r.setRequiredSkills(getSkillsForRole(r.getId()));
        }
        return roles;
    }

    public JobRole getRoleById(int id) throws SQLException {
        String sql = "SELECT id, title, slug, category, description, icon, avg_salary, demand_level, experience_level FROM job_roles WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JobRole role = mapRole(rs);
                    role.setRequiredSkills(getSkillsForRole(id));
                    return role;
                }
            }
        }
        return null;
    }

    public List<RoleSkill> getSkillsForRole(int roleId) throws SQLException {
        List<RoleSkill> list = new ArrayList<>();
        String sql = "SELECT rs.role_id, rs.skill_id, s.name AS skill_name, s.category, rs.required_proficiency, rs.importance "
                   + "FROM role_skills rs "
                   + "JOIN skills s ON rs.skill_id = s.id "
                   + "WHERE rs.role_id = ? "
                   + "ORDER BY rs.importance = 'Critical' DESC, rs.importance = 'High' DESC, s.name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new RoleSkill(
                            rs.getInt("role_id"),
                            rs.getInt("skill_id"),
                            rs.getString("skill_name"),
                            rs.getString("category"),
                            rs.getInt("required_proficiency"),
                            rs.getString("importance")
                    ));
                }
            }
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
