package com.skillgap.controller;

import com.skillgap.dao.UserDAO;
import com.skillgap.model.Recommendation;
import com.skillgap.model.User;
import com.skillgap.service.RecommendationService;
import com.skillgap.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/recommendations")
public class RecommendationServlet extends HttpServlet {

    private final RecommendationService recommendationService = new RecommendationService();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated.");
            return;
        }

        User sessionUser = (User) session.getAttribute("user");
        String skillParam = req.getParameter("skillId");

        try {
            if (skillParam != null && !skillParam.trim().isEmpty()) {
                int skillId = Integer.parseInt(skillParam.trim());
                List<Recommendation> recs = recommendationService.getRecommendationsBySkill(skillId);
                JsonUtil.sendSuccess(resp, "Recommendations for skill loaded", recs);
            } else {
                User user = userDAO.findById(sessionUser.getId());
                int targetRoleId = (user != null && user.getTargetRoleId() != null && user.getTargetRoleId() > 0)
                        ? user.getTargetRoleId() : 1;

                List<Recommendation> roadmap = recommendationService.getRecommendationsForStudentGaps(user.getId(), targetRoleId);
                JsonUtil.sendSuccess(resp, "Personalized learning roadmap loaded", roadmap);
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid skillId parameter.");
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
