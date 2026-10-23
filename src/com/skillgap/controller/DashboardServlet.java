package com.skillgap.controller;

import com.google.gson.JsonObject;
import com.skillgap.dao.AssessmentDAO;
import com.skillgap.dao.JobRoleDAO;
import com.skillgap.dao.SkillDAO;
import com.skillgap.dao.UserDAO;
import com.skillgap.model.*;
import com.skillgap.service.GapAnalysisService;
import com.skillgap.service.RecommendationService;
import com.skillgap.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class DashboardServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final JobRoleDAO jobRoleDAO = new JobRoleDAO();
    private final SkillDAO skillDAO = new SkillDAO();
    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final GapAnalysisService gapAnalysisService = new GapAnalysisService();
    private final RecommendationService recommendationService = new RecommendationService();

    private User getOrCreateSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            user = userDAO.getOrCreateGuestUser("Student Analyst", "", 1);
            session.setAttribute("user", user);
        }
        return user;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getOrCreateSessionUser(req);

        try {
            int targetRoleId = (user.getTargetRoleId() != null && user.getTargetRoleId() > 0)
                    ? user.getTargetRoleId() : 1;

            JobRole targetRole = jobRoleDAO.getRoleById(targetRoleId);
            if (targetRole != null) {
                user.setTargetRoleTitle(targetRole.getTitle());
            }

            // Session ratings cache
            Map<Integer, Integer> sessionRatings = null;
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("userRatings") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> ratings = (Map<Integer, Integer>) session.getAttribute("userRatings");
                sessionRatings = ratings;
            }

            GapAnalysisReport report = gapAnalysisService.analyze(user.getId(), targetRoleId, sessionRatings);

            // Separate current skills and missing skills
            List<Map<String, Object>> currentSkills = new ArrayList<>();
            List<SkillGapResult> missingSkills = new ArrayList<>();
            List<SkillGapResult> topGaps = new ArrayList<>();

            for (SkillGapResult res : report.getSkillGaps()) {
                if (res.getCurrentProficiency() > 0) {
                    Map<String, Object> cur = new HashMap<>();
                    cur.put("skillId", res.getSkillId());
                    cur.put("skillName", res.getSkillName());
                    cur.put("category", res.getCategory());
                    cur.put("proficiency", res.getCurrentProficiency());
                    cur.put("requiredProficiency", res.getRequiredProficiency());
                    cur.put("status", res.getStatus());
                    currentSkills.add(cur);
                }

                if (res.getGap() > 0) {
                    missingSkills.add(res);
                    if (topGaps.size() < 4) {
                        topGaps.add(res);
                    }
                }
            }

            // Learning Roadmap
            List<Recommendation> roadmap = new ArrayList<>();
            try {
                for (SkillGapResult res : report.getSkillGaps()) {
                    if (res.getGap() > 0 && res.getRecommendations() != null) {
                        roadmap.addAll(res.getRecommendations());
                    }
                }
            } catch (Exception ignored) {}

            int assessedCount = currentSkills.size();

            JsonObject dashboardData = new JsonObject();
            dashboardData.add("user", JsonUtil.getGson().toJsonTree(user));
            dashboardData.add("targetRole", JsonUtil.getGson().toJsonTree(targetRole));
            dashboardData.addProperty("assessedCount", assessedCount);
            dashboardData.addProperty("readinessPercentage", report.getReadinessPercentage());
            dashboardData.addProperty("skillGapPercentage", report.getSkillGapPercentage());
            dashboardData.addProperty("readinessLevel", report.getReadinessLevel());
            dashboardData.addProperty("totalRequiredSkills", report.getTotalSkillsRequired());
            dashboardData.addProperty("skillsMetCount", report.getSkillsMetCount());
            dashboardData.addProperty("skillsWithGapCount", report.getSkillsWithGapCount());
            dashboardData.addProperty("criticalGapCount", report.getCriticalGapCount());
            dashboardData.add("currentSkills", JsonUtil.getGson().toJsonTree(currentSkills));
            dashboardData.add("missingSkills", JsonUtil.getGson().toJsonTree(missingSkills));
            dashboardData.add("topGaps", JsonUtil.getGson().toJsonTree(topGaps));
            dashboardData.add("learningRoadmap", JsonUtil.getGson().toJsonTree(roadmap));
            dashboardData.add("report", JsonUtil.getGson().toJsonTree(report));

            JsonUtil.sendSuccess(resp, "Dashboard data loaded successfully", dashboardData);
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
