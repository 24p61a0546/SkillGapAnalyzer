package com.skillgap.controller;

import com.google.gson.JsonObject;
import com.skillgap.dao.AssessmentDAO;
import com.skillgap.dao.JobRoleDAO;
import com.skillgap.dao.UserDAO;
import com.skillgap.model.*;
import com.skillgap.service.GapAnalysisService;
import com.skillgap.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final JobRoleDAO jobRoleDAO = new JobRoleDAO();
    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final GapAnalysisService gapAnalysisService = new GapAnalysisService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated.");
            return;
        }

        User sessionUser = (User) session.getAttribute("user");
        try {
            User user = userDAO.findById(sessionUser.getId());
            if (user == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "User profile not found.");
                return;
            }

            int targetRoleId = (user.getTargetRoleId() != null && user.getTargetRoleId() > 0)
                    ? user.getTargetRoleId() : 1;

            JobRole targetRole = jobRoleDAO.getRoleById(targetRoleId);
            int assessedCount = assessmentDAO.getAssessmentCount(user.getId());
            GapAnalysisReport report = gapAnalysisService.analyze(user.getId(), targetRoleId);

            // Extract top priority gaps to improve
            List<SkillGapResult> topGaps = new ArrayList<>();
            for (SkillGapResult res : report.getSkillGaps()) {
                if (res.getGap() > 0) {
                    topGaps.add(res);
                    if (topGaps.size() >= 3) break;
                }
            }

            JsonObject dashboardData = new JsonObject();
            dashboardData.add("user", JsonUtil.getGson().toJsonTree(user));
            dashboardData.add("targetRole", JsonUtil.getGson().toJsonTree(targetRole));
            dashboardData.addProperty("assessedCount", assessedCount);
            dashboardData.addProperty("readinessPercentage", report.getReadinessPercentage());
            dashboardData.addProperty("readinessLevel", report.getReadinessLevel());
            dashboardData.addProperty("totalRequiredSkills", report.getTotalSkillsRequired());
            dashboardData.addProperty("skillsMetCount", report.getSkillsMetCount());
            dashboardData.addProperty("skillsWithGapCount", report.getSkillsWithGapCount());
            dashboardData.addProperty("criticalGapCount", report.getCriticalGapCount());
            dashboardData.add("topGaps", JsonUtil.getGson().toJsonTree(topGaps));
            dashboardData.add("report", JsonUtil.getGson().toJsonTree(report));

            JsonUtil.sendSuccess(resp, "Dashboard data loaded successfully", dashboardData);
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
