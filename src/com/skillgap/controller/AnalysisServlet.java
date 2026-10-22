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

public class AnalysisServlet extends HttpServlet {

    private final GapAnalysisService gapAnalysisService = new GapAnalysisService();
    private final UserDAO userDAO = new UserDAO();

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

            int roleId = 1; // fallback
            String roleParam = req.getParameter("roleId");
            if (roleParam != null && !roleParam.trim().isEmpty()) {
                roleId = Integer.parseInt(roleParam.trim());
            } else if (user.getTargetRoleId() != null && user.getTargetRoleId() > 0) {
                roleId = user.getTargetRoleId();
            }

            GapAnalysisReport report = gapAnalysisService.analyze(user.getId(), roleId);
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
