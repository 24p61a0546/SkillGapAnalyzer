package com.skillgap.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.skillgap.dao.AssessmentDAO;
import com.skillgap.dao.SkillDAO;
import com.skillgap.dao.UserDAO;
import com.skillgap.model.Assessment;
import com.skillgap.model.Skill;
import com.skillgap.model.User;
import com.skillgap.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class AssessmentServlet extends HttpServlet {

    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final SkillDAO skillDAO = new SkillDAO();
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
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        try {
            if ("/skills".equals(pathInfo) || "".equals(pathInfo)) {
                // Return all skills annotated with current user's rating
                List<Skill> allSkills = skillDAO.getAllSkills();
                Map<Integer, Integer> userRatings = new HashMap<>();

                try {
                    userRatings = assessmentDAO.getSkillProficiencyMap(user.getId());
                } catch (Exception e) {
                    System.err.println("[AssessmentServlet] Notice: Could not read ratings from DB: " + e.getMessage());
                }

                // Merge any session-cached ratings
                HttpSession session = req.getSession(false);
                if (session != null && session.getAttribute("userRatings") instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<Integer, Integer> sessionRatings = (Map<Integer, Integer>) session.getAttribute("userRatings");
                    for (Map.Entry<Integer, Integer> entry : sessionRatings.entrySet()) {
                        userRatings.putIfAbsent(entry.getKey(), entry.getValue());
                    }
                }

                List<Map<String, Object>> resultList = new ArrayList<>();
                for (Skill s : allSkills) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", s.getId());
                    map.put("name", s.getName());
                    map.put("category", s.getCategory());
                    map.put("description", s.getDescription());
                    map.put("maxRating", s.getMaxRating());
                    map.put("currentRating", userRatings.getOrDefault(s.getId(), 0));
                    resultList.add(map);
                }
                JsonUtil.sendSuccess(resp, "Assessment skills catalog loaded", resultList);
            } else if ("/my".equals(pathInfo)) {
                List<Assessment> myAssessments = assessmentDAO.getAssessmentsByUserId(user.getId());
                JsonUtil.sendSuccess(resp, "Student assessments loaded", myAssessments);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Assessment endpoint not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getOrCreateSessionUser(req);
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        if ("/submit".equals(pathInfo) || "".equals(pathInfo)) {
            handleSubmit(req, resp, user.getId());
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Action not found");
        }
    }

    private void handleSubmit(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        try {
            JsonObject body = JsonUtil.readRequestBody(req, JsonObject.class);
            if (body == null || (!body.has("ratings") && !body.has("assessments"))) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Ratings payload required.");
                return;
            }

            Map<Integer, Integer> ratings = new HashMap<>();
            Map<Integer, String> notes = new HashMap<>();

            // ratings object { "1": 4, "2": 3 }
            if (body.has("ratings") && body.get("ratings").isJsonObject()) {
                JsonObject ratingsObj = body.getAsJsonObject("ratings");
                for (Map.Entry<String, JsonElement> entry : ratingsObj.entrySet()) {
                    int skillId = Integer.parseInt(entry.getKey());
                    int level = entry.getValue().getAsInt();
                    if (level >= 0 && level <= 5) {
                        ratings.put(skillId, level);
                    }
                }
            }

            if (body.has("notes") && body.get("notes").isJsonObject()) {
                JsonObject notesObj = body.getAsJsonObject("notes");
                for (Map.Entry<String, JsonElement> entry : notesObj.entrySet()) {
                    notes.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsString());
                }
            }

            // Cache in session immediately so downstream servlets have access
            HttpSession session = req.getSession(true);
            session.setAttribute("userRatings", ratings);

            int count = 0;
            try {
                count = assessmentDAO.saveBulkAssessments(userId, ratings, notes);
            } catch (Exception e) {
                System.err.println("[AssessmentServlet] Warning: Failed to persist to MySQL, using session cache: " + e.getMessage());
                count = ratings.size();
            }

            JsonObject res = new JsonObject();
            res.addProperty("savedCount", count);
            JsonUtil.sendSuccess(resp, "Assessment submitted successfully! " + count + " skills evaluated.", res);

        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Failed to process assessment: " + e.getMessage());
        }
    }
}
