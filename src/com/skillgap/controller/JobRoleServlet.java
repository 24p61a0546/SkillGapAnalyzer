package com.skillgap.controller;

import com.google.gson.JsonObject;
import com.skillgap.dao.JobRoleDAO;
import com.skillgap.dao.UserDAO;
import com.skillgap.model.JobRole;
import com.skillgap.model.User;
import com.skillgap.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class JobRoleServlet extends HttpServlet {

    private final JobRoleDAO jobRoleDAO = new JobRoleDAO();
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
        String roleIdParam = req.getParameter("id");
        try {
            if (roleIdParam != null && !roleIdParam.trim().isEmpty()) {
                int roleId = Integer.parseInt(roleIdParam.trim());
                JobRole role = jobRoleDAO.getRoleById(roleId);
                if (role != null) {
                    JsonUtil.sendSuccess(resp, "Role retrieved", role);
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Job role not found");
                }
            } else {
                List<JobRole> roles = jobRoleDAO.getAllRoles();
                JsonUtil.sendSuccess(resp, "Roles retrieved successfully", roles);
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid role id format.");
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if ("/select".equals(pathInfo)) {
            handleSelectTargetRole(req, resp);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Action not supported.");
        }
    }

    private void handleSelectTargetRole(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getOrCreateSessionUser(req);
        HttpSession session = req.getSession(true);

        try {
            JsonObject body = JsonUtil.readRequestBody(req, JsonObject.class);
            if (body == null || !body.has("roleId")) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "roleId is required.");
                return;
            }

            int roleId = body.get("roleId").getAsInt();
            JobRole targetRole = jobRoleDAO.getRoleById(roleId);
            if (targetRole == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Invalid job role id.");
                return;
            }

            user.setTargetRoleId(roleId);
            user.setTargetRoleTitle(targetRole.getTitle());
            session.setAttribute("user", user);

            try {
                userDAO.updateTargetRole(user.getId(), roleId);
            } catch (Exception e) {
                System.err.println("[JobRoleServlet] Notice: Could not update target role in DB: " + e.getMessage());
            }

            JsonUtil.sendSuccess(resp, "Target role updated to " + targetRole.getTitle(), targetRole);

        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Error parsing request: " + e.getMessage());
        }
    }
}
