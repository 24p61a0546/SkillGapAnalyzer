package com.skillgap.service;

import com.skillgap.dao.RecommendationDAO;
import com.skillgap.model.GapAnalysisReport;
import com.skillgap.model.Recommendation;
import com.skillgap.model.SkillGapResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecommendationService {

    private final RecommendationDAO recommendationDAO = new RecommendationDAO();
    private final GapAnalysisService gapAnalysisService = new GapAnalysisService();

    public List<Recommendation> getRecommendationsForStudentGaps(int userId, int targetRoleId) throws SQLException {
        GapAnalysisReport report = gapAnalysisService.analyze(userId, targetRoleId);
        List<Recommendation> roadmap = new ArrayList<>();

        for (SkillGapResult res : report.getSkillGaps()) {
            if (res.getGap() > 0) {
                roadmap.addAll(res.getRecommendations());
            }
        }
        return roadmap;
    }

    public List<Recommendation> getRecommendationsBySkill(int skillId) throws SQLException {
        return recommendationDAO.getRecommendationsBySkillId(skillId);
    }
}
