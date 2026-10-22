package com.skillgap.filter;

import com.skillgap.model.User;
import com.skillgap.util.JsonUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Exempt public endpoints: auth, static assets, roles catalog, home
        boolean isPublic = path.contains("/api/auth/")
                || path.equals(req.getContextPath() + "/api/roles")
                || path.startsWith(req.getContextPath() + "/assets/")
                || path.endsWith("login.html")
                || path.endsWith("register.html")
                || path.endsWith("index.html")
                || path.equals(req.getContextPath() + "/")
                || path.equals(req.getContextPath());

        if (isPublic || user != null) {
            chain.doFilter(request, response);
            return;
        }

        // Handle unauthenticated requests
        if (path.contains("/api/")) {
            JsonUtil.sendError(res, HttpServletResponse.SC_UNAUTHORIZED, "Session expired or user not logged in. Please log in.");
        } else {
            res.sendRedirect(req.getContextPath() + "/login.html");
        }
    }

    @Override
    public void destroy() {}
}
