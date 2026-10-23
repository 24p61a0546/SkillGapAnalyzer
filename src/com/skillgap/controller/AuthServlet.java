package com.skillgap.controller;

import com.google.gson.JsonObject;
import com.skillgap.dao.UserDAO;
import com.skillgap.model.User;
import com.skillgap.service.AuthService;
import com.skillgap.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        switch (pathInfo) {
            case "/start":
                handleStart(req, resp);
                break;
            case "/login":
                handleLogin(req, resp);
                break;
            case "/register":
                handleRegister(req, resp);
                break;
            case "/logout":
                handleLogout(req, resp);
                break;
            default:
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Auth endpoint not found: " + pathInfo);
                break;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        if ("/me".equals(pathInfo) || "".equals(pathInfo)) {
            handleGetCurrentUser(req, resp);
        } else if ("/logout".equals(pathInfo)) {
            handleLogout(req, resp);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Auth endpoint not found");
        }
    }

    /**
     * Handles the frictionless Start Analysis entry point.
     * Takes User Name (required), Email (optional), and Target Role.
     * No password or login required.
     */
    private void handleStart(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject body = JsonUtil.readRequestBody(req, JsonObject.class);
            if (body == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body required.");
                return;
            }

            String fullName = body.has("fullName") ? body.get("fullName").getAsString() : "";
            if (fullName == null || fullName.trim().isEmpty()) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "User name is required to start analysis.");
                return;
            }

            String email = (body.has("email") && !body.get("email").isJsonNull())
                    ? body.get("email").getAsString() : "";

            int targetRoleId = 1;
            if (body.has("targetRoleId") && !body.get("targetRoleId").isJsonNull()) {
                try {
                    targetRoleId = body.get("targetRoleId").getAsInt();
                } catch (Exception ignored) {}
            }

            User user = userDAO.getOrCreateGuestUser(fullName.trim(), email.trim(), targetRoleId);

            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(3600 * 24);

            JsonUtil.sendSuccess(resp, "Welcome " + user.getFullName() + "! Profile initialized.", user);

        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to start analysis: " + e.getMessage());
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject body = JsonUtil.readRequestBody(req, JsonObject.class);
            if (body == null || !body.has("email") || !body.has("password")) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required.");
                return;
            }

            String email = body.get("email").getAsString();
            String password = body.get("password").getAsString();

            User user = authService.authenticate(email, password);
            if (user != null) {
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(3600 * 4); // 4 hours

                JsonUtil.sendSuccess(resp, "Login successful", user);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed request: " + e.getMessage());
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject body = JsonUtil.readRequestBody(req, JsonObject.class);
            if (body == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body required.");
                return;
            }

            String fullName = body.has("fullName") ? body.get("fullName").getAsString() : null;
            String email = body.has("email") ? body.get("email").getAsString() : null;
            String password = body.has("password") ? body.get("password").getAsString() : null;
            String department = body.has("department") ? body.get("department").getAsString() : "Computer Science";
            String yearOfStudy = body.has("yearOfStudy") ? body.get("yearOfStudy").getAsString() : "3rd Year";
            Integer targetRoleId = body.has("targetRoleId") && !body.get("targetRoleId").isJsonNull()
                    ? body.get("targetRoleId").getAsInt() : 1;

            User newUser = authService.register(fullName, email, password, department, yearOfStudy, targetRoleId);
            if (newUser != null) {
                HttpSession session = req.getSession(true);
                session.setAttribute("user", newUser);
                JsonUtil.sendSuccess(resp, "Registration successful!", newUser);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration failed, please try again.");
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error during registration: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request: " + e.getMessage());
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        JsonUtil.sendSuccess(resp, "Session reset successfully.", null);
    }

    private void handleGetCurrentUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(true);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            user = userDAO.getOrCreateGuestUser("Student Analyst", "", 1);
            if (session != null) {
                session.setAttribute("user", user);
            }
        }
        JsonUtil.sendSuccess(resp, "Current user retrieved", user);
    }
}
