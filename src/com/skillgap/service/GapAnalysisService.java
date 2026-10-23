package com.skillgap.service;

import com.skillgap.dao.AssessmentDAO;
import com.skillgap.dao.JobRoleDAO;
import com.skillgap.dao.RecommendationDAO;
import com.skillgap.model.*;

import java.sql.SQLException;
import java.util.*;

public class GapAnalysisService {

    private final JobRoleDAO jobRoleDAO = new JobRoleDAO();
    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final RecommendationDAO recommendationDAO = new RecommendationDAO();

    public GapAnalysisReport analyze(int userId, int roleId) throws SQLException {
        return analyze(userId, roleId, null);
    }

    public GapAnalysisReport analyze(int userId, int roleId, Map<Integer, Integer> sessionRatings) throws SQLException {
        JobRole role = jobRoleDAO.getRoleById(roleId);
        if (role == null) {
            throw new IllegalArgumentException("Job role not found with id: " + roleId);
        }

        Map<Integer, Integer> userRatings = new HashMap<>();
        try {
            userRatings = assessmentDAO.getSkillProficiencyMap(userId);
        } catch (Exception e) {
            System.err.println("[GapAnalysisService] Notice: Could not read ratings from DB: " + e.getMessage());
        }

        if (sessionRatings != null) {
            for (Map.Entry<Integer, Integer> entry : sessionRatings.entrySet()) {
                userRatings.put(entry.getKey(), entry.getValue());
            }
        }
        List<RoleSkill> requiredSkills = role.getRequiredSkills();

        GapAnalysisReport report = new GapAnalysisReport();
        report.setRoleId(role.getId());
        report.setRoleTitle(role.getTitle());
        report.setRoleCategory(role.getCategory());
        report.setTotalSkillsRequired(requiredSkills.size());

        List<SkillGapResult> gapResults = new ArrayList<>();
        List<Integer> gapSkillIds = new ArrayList<>();

        double weightedTotalRequired = 0;
        double weightedCurrentScored = 0;
        int metCount = 0;
        int gapCount = 0;
        int criticalGapCount = 0;

        for (RoleSkill rs : requiredSkills) {
            int currentLevel = userRatings.getOrDefault(rs.getSkillId(), 0);
            int requiredLevel = rs.getRequiredProficiency();

            // Calculate weight based on importance
            double weight = 2.0;
            if ("Critical".equalsIgnoreCase(rs.getImportance())) {
                weight = 3.0;
            } else if ("Medium".equalsIgnoreCase(rs.getImportance())) {
                weight = 1.0;
            }

            weightedTotalRequired += (weight * requiredLevel);
            weightedCurrentScored += (weight * Math.min(currentLevel, requiredLevel));

            SkillGapResult result = new SkillGapResult(
                    rs.getSkillId(),
                    rs.getSkillName(),
                    rs.getCategory(),
                    requiredLevel,
                    currentLevel,
                    rs.getImportance()
            );

            if (result.getGap() == 0) {
                metCount++;
            } else {
                gapCount++;
                gapSkillIds.add(rs.getSkillId());
                if ("Critical Gap".equals(result.getStatus()) || "Critical".equalsIgnoreCase(rs.getImportance())) {
                    criticalGapCount++;
                }
            }

            gapResults.add(result);
        }

        // Attach recommendations for skills with gaps
        if (!gapSkillIds.isEmpty()) {
            List<Recommendation> allRecs = recommendationDAO.getRecommendationsForSkillList(gapSkillIds);
            Map<Integer, List<Recommendation>> recsBySkill = new HashMap<>();
            for (Recommendation r : allRecs) {
                recsBySkill.computeIfAbsent(r.getSkillId(), k -> new ArrayList<>()).add(r);
            }

            for (SkillGapResult res : gapResults) {
                if (res.getGap() > 0 && recsBySkill.containsKey(res.getSkillId())) {
                    res.setRecommendations(recsBySkill.get(res.getSkillId()));
                }
            }
        }

        // Calculate overall percentage
        double readinessPct = 0.0;
        if (weightedTotalRequired > 0) {
            readinessPct = (weightedCurrentScored / weightedTotalRequired) * 100.0;
        }
        readinessPct = Math.round(readinessPct * 10.0) / 10.0; // round to 1 decimal

        report.setReadinessPercentage(readinessPct);
        report.setSkillsMetCount(metCount);
        report.setSkillsWithGapCount(gapCount);
        report.setCriticalGapCount(criticalGapCount);

        if (readinessPct >= 85.0) {
            report.setReadinessLevel("Industry Ready");
        } else if (readinessPct >= 65.0) {
            report.setReadinessLevel("High Potential (Moderate Gap)");
        } else if (readinessPct >= 40.0) {
            report.setReadinessLevel("Intermediate Learner");
        } else {
            report.setReadinessLevel("Foundational Phase");
        }

        report.setSkillGaps(gapResults);
        return report;
    }
}
