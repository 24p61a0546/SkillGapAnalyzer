package com.skillgap.controller;

import com.skillgap.dao.UserDAO;
import com.skillgap.model.GapAnalysisReport;
import com.skillgap.model.User;
import com.skillgap.service.GapAnalysisService;
import com.skillgap.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class AnalysisServlet extends HttpServlet {

    private final GapAnalysisService gapAnalysisService = new GapAnalysisService();
    private final UserDAO userDAO = new UserDAO();

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
            int roleId = (user.getTargetRoleId() != null && user.getTargetRoleId() > 0)
                    ? user.getTargetRoleId() : 1;

            String roleParam = req.getParameter("roleId");
            if (roleParam != null && !roleParam.trim().isEmpty()) {
                roleId = Integer.parseInt(roleParam.trim());
            }

            // Retrieve session ratings if available
            Map<Integer, Integer> sessionRatings = null;
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("userRatings") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Integer, Integer> ratings = (Map<Integer, Integer>) session.getAttribute("userRatings");
                sessionRatings = ratings;
            }

            GapAnalysisReport report = gapAnalysisService.analyze(user.getId(), roleId, sessionRatings);
            JsonUtil.sendSuccess(resp, "Gap analysis report generated successfully", report);

        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid roleId format.");
        } catch (IllegalArgumentException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
