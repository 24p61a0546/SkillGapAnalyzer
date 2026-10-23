package com.skillgap.dao;

import com.skillgap.model.Recommendation;
import com.skillgap.util.DBConnection;
import com.skillgap.util.DemoCatalog;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecommendationDAO {

    public List<Recommendation> getRecommendationsBySkillId(int skillId) throws SQLException {
        List<Recommendation> list = new ArrayList<>();
        String sql = "SELECT r.id, r.skill_id, s.name AS skill_name, r.title, r.type, r.provider, "
                   + "r.url, r.estimated_hours, r.difficulty_level "
                   + "FROM recommendations r "
                   + "JOIN skills s ON r.skill_id = s.id "
                   + "WHERE r.skill_id = ? "
                   + "ORDER BY r.type, r.difficulty_level";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, skillId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRecommendation(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("[RecommendationDAO] getRecommendationsBySkillId fallback: " + e.getMessage());
        }

        if (list.isEmpty()) {
            list = DemoCatalog.getRecommendationsBySkillId(skillId);
        }
        return list;
    }

    public List<Recommendation> getRecommendationsForSkillList(List<Integer> skillIds) throws SQLException {
        if (skillIds == null || skillIds.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < skillIds.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        String sql = "SELECT r.id, r.skill_id, s.name AS skill_name, r.title, r.type, r.provider, "
                   + "r.url, r.estimated_hours, r.difficulty_level "
                   + "FROM recommendations r "
                   + "JOIN skills s ON r.skill_id = s.id "
                   + "WHERE r.skill_id IN (" + placeholders + ") "
                   + "ORDER BY s.name, r.type";

        List<Recommendation> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < skillIds.size(); i++) {
                ps.setInt(i + 1, skillIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRecommendation(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("[RecommendationDAO] getRecommendationsForSkillList fallback: " + e.getMessage());
        }

        if (list.isEmpty()) {
            list = DemoCatalog.getRecommendationsForSkillList(skillIds);
        }
        return list;
    }

    private Recommendation mapRecommendation(ResultSet rs) throws SQLException {
        return new Recommendation(
                rs.getInt("id"),
                rs.getInt("skill_id"),
                rs.getString("skill_name"),
                rs.getString("title"),
                rs.getString("type"),
                rs.getString("provider"),
                rs.getString("url"),
                rs.getString("estimated_hours"),
                rs.getString("difficulty_level")
        );
    }
}
